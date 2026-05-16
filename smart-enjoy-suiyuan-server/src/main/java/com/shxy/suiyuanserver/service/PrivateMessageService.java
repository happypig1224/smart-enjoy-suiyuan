package com.shxy.suiyuanserver.service;

import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.vo.ConversationVO;
import com.shxy.suiyuanentity.vo.MessageAckVO;
import com.shxy.suiyuanentity.vo.PrivateMessageVO;
import com.shxy.suiyuanentity.vo.UnreadCountVO;

import java.util.List;

/**
 * 私信消息服务接口
 */
public interface PrivateMessageService {

    /**
     * 发送私信消息
     *
     * @param senderId   发送者用户ID
     * @param receiverId 接收者用户ID
     * @param messageType 消息类型: TEXT / IMAGE / FILE
     * @param content    消息内容
     * @param clientMsgId 客户端消息ID（去重用）
     * @return 消息回执（含 messageId + seqId）
     */
    MessageAckVO sendMessage(Long senderId, Long receiverId, String messageType, String content, String clientMsgId);

    /**
     * 获取会话列表（按最后消息时间降序）
     *
     * @param userId 当前用户ID
     * @return 会话列表
     */
    List<ConversationVO> getConversations(Long userId);

    /**
     * 获取或创建与指定用户的会话
     *
     * @param userId       当前用户ID
     * @param targetUserId 对方用户ID
     * @return 会话VO（含会话ID + 对方信息）
     */
    ConversationVO getOrCreateConversation(Long userId, Long targetUserId);

    /**
     * 获取历史消息（从指定 seqId 开始拉取，返回 > fromSeq 的消息）
     *
     * @param userId         当前用户ID
     * @param conversationId 会话ID
     * @param fromSeq        起始 seqId（不包含），传 0 表示从头拉取
     * @param limit          每页条数
     * @return 历史消息列表（按 seq_id 升序）
     */
    List<PrivateMessageVO> getHistoryMessages(Long userId, Long conversationId, Long fromSeq, Integer limit);

    /**
     * 标记会话已读
     *
     * @param userId         当前用户ID
     * @param conversationId 会话ID
     * @param lastReadSeq    已读到的最大 seqId
     */
    void markAsRead(Long userId, Long conversationId, Long lastReadSeq);

    /**
     * 获取未读消息数
     *
     * @param userId         当前用户ID
     * @param conversationId 会话ID（传 null 则返回总未读数）
     * @return 未读计数
     */
    UnreadCountVO getUnreadCount(Long userId, Long conversationId);
}
