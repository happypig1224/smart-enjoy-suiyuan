package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuanentity.entity.PrivateConversation;
import com.shxy.suiyuanentity.entity.PrivateMessage;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.entity.UserReadCursor;
import com.shxy.suiyuanentity.vo.ConversationVO;
import com.shxy.suiyuanentity.vo.MessageAckVO;
import com.shxy.suiyuanentity.vo.PrivateMessageVO;
import com.shxy.suiyuanentity.vo.UnreadCountVO;
import com.shxy.suiyuanserver.mapper.PrivateConversationMapper;
import com.shxy.suiyuanserver.mapper.PrivateMessageMapper;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.mapper.UserReadCursorMapper;
import com.shxy.suiyuanserver.service.PrivateMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 私信消息服务实现
 */
@Slf4j
@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    @Resource
    private PrivateConversationMapper conversationMapper;

    @Resource
    private PrivateMessageMapper messageMapper;

    @Resource
    private UserReadCursorMapper readCursorMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    /** Redis 消息去重 Key 前缀 */
    private static final String MSG_DEDUP_PREFIX = "msg:dedup:";

    /** Redis 未读计数 Key 前缀 */
    private static final String UNREAD_PREFIX = "msg:unread:";

    /** 消息去重 TTL（秒） */
    private static final long DEDUP_TTL_SECONDS = 300;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageAckVO sendMessage(Long senderId, Long receiverId, String messageType, String content, String clientMsgId) {
        // 1. 幂等去重
        String dedupKey = MSG_DEDUP_PREFIX + senderId + ":" + clientMsgId;
        Boolean dedupSuccess = stringRedisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", Duration.ofSeconds(DEDUP_TTL_SECONDS));
        if (!Boolean.TRUE.equals(dedupSuccess)) {
            log.warn("消息重复，已忽略: senderId={}, clientMsgId={}", senderId, clientMsgId);
            throw new BaseException("消息重复发送");
        }

        // 2. 不允许给自己发消息
        if (senderId.equals(receiverId)) {
            throw new BaseException("不能给自己发送私信");
        }

        // 3. 获取或创建会话（保证 user1 < user2）
        PrivateConversation conversation = getOrCreateConversationEntity(senderId, receiverId);

        // 4. 生成会话维度的 seq_id
        String seqKey = "msg:seq:" + conversation.getId();
        Long seqId = stringRedisTemplate.opsForValue().increment(seqKey);

        // 5. 写入消息
        PrivateMessage message = PrivateMessage.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .receiverId(receiverId)
                .messageType(messageType)
                .content(content)
                .seqId(seqId)
                .status("SENT")
                .clientMsgId(clientMsgId)
                .build();
        messageMapper.insert(message);

        // 6. 更新会话最后一条消息
        String lastMsg = messageType.equals("TEXT")
                ? (content.length() > 50 ? content.substring(0, 50) : content)
                : "[" + messageType + "]";
        conversation.setLastMessage(lastMsg);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        // 7. 接收者在 → 推送消息
        boolean receiverOnline = Boolean.TRUE.equals(
                stringRedisTemplate.hasKey("user:ws:" + receiverId));
        if (receiverOnline && messagingTemplate != null) {
            try {
                // 组装推送 VO
                User sender = userMapper.selectById(senderId);
                PrivateMessageVO pushVO = toMessageVO(message, sender);
                pushVO.setStatus("DELIVERED");
                messagingTemplate.convertAndSendToUser(
                        receiverId.toString(), "/queue/chat", pushVO);
                // 更新状态为已送达
                message.setStatus("DELIVERED");
                messageMapper.updateById(message);
            } catch (Exception e) {
                log.error("推送消息到用户 {} 失败", receiverId, e);
            }
        }

        // 8. 更新接收者未读计数
        String unreadKey = UNREAD_PREFIX + receiverId + ":" + conversation.getId();
        stringRedisTemplate.opsForValue().increment(unreadKey);
        // 推送未读计数变更（接收者在线时）
        if (receiverOnline && messagingTemplate != null) {
            try {
                Integer totalUnread = getTotalUnreadFromRedis(receiverId);
                messagingTemplate.convertAndSendToUser(
                        receiverId.toString(), "/queue/unread",
                        UnreadCountVO.builder()
                                .totalUnread(totalUnread)
                                .conversationUnread(getConvUnreadFromRedis(receiverId, conversation.getId()))
                                .build());
            } catch (Exception e) {
                log.error("推送未读计数变更失败", e);
            }
        }

        // 9. 返回回执给发送者
        return MessageAckVO.builder()
                .clientMsgId(clientMsgId)
                .messageId(message.getId())
                .seqId(seqId)
                .conversationId(conversation.getId())
                .status(message.getStatus())
                .build();
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        // 查询 user1_id = userId 或 user2_id = userId 的所有会话
        LambdaQueryWrapper<PrivateConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateConversation::getUser1Id, userId)
                .or()
                .eq(PrivateConversation::getUser2Id, userId);
        wrapper.orderByDesc(PrivateConversation::getLastMessageAt);
        List<PrivateConversation> conversations = conversationMapper.selectList(wrapper);

        if (conversations.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询对方用户信息
        Set<Long> targetUserIds = conversations.stream()
                .map(c -> c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id())
                .collect(Collectors.toSet());
        Map<Long, User> userMap = queryUserMap(targetUserIds);

        // 组装 VO
        return conversations.stream().map(c -> {
            Long targetUserId = c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id();
            User targetUser = userMap.get(targetUserId);
            return ConversationVO.builder()
                    .conversationId(c.getId())
                    .targetUserId(targetUserId)
                    .targetUserName(targetUser != null ? targetUser.getUserName() : "未知用户")
                    .targetUserAvatar(targetUser != null ? targetUser.getAvatar() : null)
                    .lastMessage(c.getLastMessage())
                    .lastMessageAt(c.getLastMessageAt())
                    .unreadCount(getConvUnreadFromRedis(userId, c.getId()))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public ConversationVO getOrCreateConversation(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BaseException("不能与自己创建会话");
        }

        PrivateConversation conversation = getOrCreateConversationEntity(userId, targetUserId);
        User targetUser = userMapper.selectById(targetUserId);

        return ConversationVO.builder()
                .conversationId(conversation.getId())
                .targetUserId(targetUserId)
                .targetUserName(targetUser != null ? targetUser.getUserName() : "未知用户")
                .targetUserAvatar(targetUser != null ? targetUser.getAvatar() : null)
                .lastMessage(conversation.getLastMessage())
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(getConvUnreadFromRedis(userId, conversation.getId()))
                .build();
    }

    @Override
    public List<PrivateMessageVO> getHistoryMessages(Long userId, Long conversationId, Long fromSeq, Integer limit) {
        // 验证该用户属于此会话
        PrivateConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BaseException("会话不存在");
        }
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BaseException("无权访问此会话");
        }

        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 50;

        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getConversationId, conversationId)
                .gt(fromSeq != null && fromSeq > 0, PrivateMessage::getSeqId, fromSeq)
                .orderByAsc(PrivateMessage::getSeqId)
                .last("LIMIT " + pageSize);

        List<PrivateMessage> messages = messageMapper.selectList(wrapper);

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量获取用户信息
        Set<Long> userIds = new HashSet<>();
        messages.forEach(m -> {
            userIds.add(m.getSenderId());
            userIds.add(m.getReceiverId());
        });
        Map<Long, User> userMap = queryUserMap(userIds);

        return messages.stream()
                .map(m -> toMessageVO(m, userMap.get(m.getSenderId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long conversationId, Long lastReadSeq) {
        // 更新位点表（使用 UPSERT 逻辑避免唯一键冲突）
        LambdaQueryWrapper<UserReadCursor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserReadCursor::getUserId, userId)
                .eq(UserReadCursor::getConversationId, conversationId);
        UserReadCursor cursor = readCursorMapper.selectOne(wrapper);

        if (cursor == null) {
            cursor = UserReadCursor.builder()
                    .userId(userId)
                    .conversationId(conversationId)
                    .lastReadSeq(lastReadSeq)
                    .build();
            try {
                readCursorMapper.insert(cursor);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                log.warn("并发插入已读游标，转为更新: userId={}, conversationId={}", userId, conversationId);
                cursor = readCursorMapper.selectOne(wrapper);
                if (cursor != null && lastReadSeq > cursor.getLastReadSeq()) {
                    cursor.setLastReadSeq(lastReadSeq);
                    readCursorMapper.updateById(cursor);
                }
            }
        } else if (lastReadSeq > cursor.getLastReadSeq()) {
            cursor.setLastReadSeq(lastReadSeq);
            readCursorMapper.updateById(cursor);
        }

        // 清除该会话的 Redis 未读计数
        String unreadKey = UNREAD_PREFIX + userId + ":" + conversationId;
        stringRedisTemplate.delete(unreadKey);
    }

    @Override
    public UnreadCountVO getUnreadCount(Long userId, Long conversationId) {
        Integer total = getTotalUnreadFromRedis(userId);
        Integer convUnread = null;
        if (conversationId != null) {
            convUnread = getConvUnreadFromRedis(userId, conversationId);
        }
        return UnreadCountVO.builder()
                .totalUnread(total)
                .conversationUnread(convUnread)
                .build();
    }

    // ==================== 内部方法 ====================

    /**
     * 获取或创建会话实体（保证 user1 < user2）
     */
    private PrivateConversation getOrCreateConversationEntity(Long userId1, Long userId2) {
        long u1 = Math.min(userId1, userId2);
        long u2 = Math.max(userId1, userId2);

        LambdaQueryWrapper<PrivateConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateConversation::getUser1Id, u1)
                .eq(PrivateConversation::getUser2Id, u2);
        PrivateConversation conversation = conversationMapper.selectOne(wrapper);

        if (conversation == null) {
            conversation = PrivateConversation.builder()
                    .user1Id(u1)
                    .user2Id(u2)
                    .lastMessageAt(LocalDateTime.now())
                    .build();
            conversationMapper.insert(conversation);
        }
        return conversation;
    }

    /**
     * 实体 → VO
     */
    private PrivateMessageVO toMessageVO(PrivateMessage msg, User sender) {
        return PrivateMessageVO.builder()
                .messageId(msg.getId())
                .conversationId(msg.getConversationId())
                .senderId(msg.getSenderId())
                .senderName(sender != null ? sender.getUserName() : "未知用户")
                .senderAvatar(sender != null ? sender.getAvatar() : null)
                .receiverId(msg.getReceiverId())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .seqId(msg.getSeqId())
                .status(msg.getStatus())
                .createTime(msg.getCreateTime())
                .build();
    }

    /**
     * 批量查询用户信息
     */
    private Map<Long, User> queryUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    /**
     * 按会话维度获取未读数
     */
    private Integer getConvUnreadFromRedis(Long userId, Long conversationId) {
        String key = UNREAD_PREFIX + userId + ":" + conversationId;
        String value = stringRedisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 获取用户总未读数（遍历 Redis keys）
     */
    private Integer getTotalUnreadFromRedis(Long userId) {
        String pattern = UNREAD_PREFIX + userId + ":*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (String key : keys) {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null) {
                total += Integer.parseInt(value);
            }
        }
        return total;
    }
}
