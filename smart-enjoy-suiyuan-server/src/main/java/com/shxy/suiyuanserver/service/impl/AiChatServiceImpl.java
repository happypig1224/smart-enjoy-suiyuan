package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.entity.AiSession;
import com.shxy.suiyuanentity.entity.ChatMessage;
import com.shxy.suiyuanentity.entity.McpRequest;
import com.shxy.suiyuanentity.entity.McpResponse;
import com.shxy.suiyuanentity.vo.ChatMessageVO;
import com.shxy.suiyuanentity.vo.ChatResponseVO;
import com.shxy.suiyuanentity.vo.SessionVO;
import com.shxy.suiyuanserver.agent.McpClient;
import com.shxy.suiyuanserver.mapper.AiSessionMapper;
import com.shxy.suiyuanserver.mapper.ChatMessageMapper;
import com.shxy.suiyuanserver.service.AiChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/27 17:37
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Resource
    private McpClient mcpClient;

    @Resource
    private AiSessionMapper aiSessionMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseVO chat(String query, Long sessionId) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            throw new BaseException("用户未登录!");
        }
        log.info("会话ID: {}", sessionId);
        // 1. 获取或创建会话
        AiSession session = getOrCreateSession(userId, sessionId, query);
        Long activeSessionId = session.getId();

        // 查询该会话下最近的 10 条消息(避免token爆炸)
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionId, activeSessionId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT 10");
        List<ChatMessage> historyList = chatMessageMapper.selectList(queryWrapper);

        // 因为是倒序查询(最新在前)
        Collections.reverse(historyList);

        // 将实体类转换为 Python 容易解析的字典列表
        List<Map<String, String>> historyPayload = new ArrayList<>();
        for (ChatMessage msg : historyList) {
            Map<String, String> map = new HashMap<>();
            map.put("role", msg.getRole());
            map.put("content", msg.getContent());
            historyPayload.add(map);
        }

        // 2. 组装 MCP 请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("userId", userId);
        params.put("sessionId", activeSessionId);
        params.put("history", historyPayload); // 把历史记录塞进 Payload

        McpRequest request = McpRequest.builder()
                .tool("chat_agent")
                .params(params)
                .build();

        // 3. 调用 Python 端 Agent
        McpResponse response = mcpClient.call(request);

        // 4. 解析结果
        String aiReply;
        if (response != null && response.getCode() == 200) {
            aiReply = response.getResult();
        } else {
            aiReply = response != null ? response.getResult() : "系统繁忙，请稍后再试。";
            log.warn("Agent 调用异常: {}", response != null ? response.getMessage() : "null");
        }

        // 5. 保存聊天记录到数据库 (用户问题 + AI 回复)
        saveChatMessage(activeSessionId, userId, query, aiReply);

        // 6. 返回包含 sessionId 和回复的响应
        return ChatResponseVO.builder()
                .sessionId(activeSessionId)
                .reply(aiReply)
                .build();
    }

    @Override
    public List<SessionVO> getHistory() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null){
            throw new BaseException("用户未登录!");
        }
        
        LambdaQueryWrapper<AiSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiSession::getUserId, userId);
        queryWrapper.orderByDesc(AiSession::getCreateTime);
        List<AiSession> sessionList = aiSessionMapper.selectList(queryWrapper);
        
        if (sessionList.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 转换为SessionVO
        return sessionList.stream().map(session -> 
            SessionVO.builder()
                .sessionId(session.getId())
                .title(session.getTitle())
                .createTime(session.getCreateTime())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getHistoryMessage(Long sessionId) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null){
            throw new BaseException("用户未登录!");
        }
        
        if (sessionId == null) {
            throw new BaseException("会话ID不能为空!");
        }
        
        // 验证会话是否存在且属于当前用户
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BaseException("会话不存在或无权访问!");
        }
        
        // 查询该会话下的所有消息，按时间正序排列
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionId, sessionId);
        queryWrapper.eq(ChatMessage::getUserId, userId);
        queryWrapper.orderByAsc(ChatMessage::getCreateTime);
        List<ChatMessage> messageList = chatMessageMapper.selectList(queryWrapper);
        
        // 转换为ChatMessageVO
        return messageList.stream().map(message -> 
            ChatMessageVO.builder()
                .role(message.getRole())
                .content(message.getContent())
                .createTime(message.getCreateTime())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteSession(Long sessionId) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录!");
        }
        
        if (sessionId == null) {
            return Result.fail("会话ID不能为空!");
        }
        
        // 验证会话是否存在且属于当前用户
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权访问!");
        }
        
        // 删除该会话下的所有消息记录---逻辑删除
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(queryWrapper);
        
        // 删除会话记录---逻辑删除
        aiSessionMapper.deleteById(sessionId);
        
        log.info("用户 {} 删除了会话 {}", userId, sessionId);
        return Result.success("会话删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> renameSession(Long sessionId, String title) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录!");
        }
        if (sessionId == null || title == null || title.trim().isEmpty()) {
            return Result.fail("参数不能为空!");
        }
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权访问!");
        }
        session.setTitle(title.trim());
        aiSessionMapper.updateById(session);
        log.info("用户 {} 重命名了会话 {} 为 {}", userId, sessionId, title);
        return Result.success("重命名成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> batchDeleteSessions(List<Long> sessionIds) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录!");
        }
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Result.fail("会话列表不能为空!");
        }
        for (Long sessionId : sessionIds) {
            AiSession session = aiSessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                continue;
            }
            // 删除消息记录
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessage::getSessionId, sessionId);
            chatMessageMapper.delete(queryWrapper);
            // 删除会话记录
            aiSessionMapper.deleteById(sessionId);
        }
        log.info("用户 {} 批量删除了 {} 个会话", userId, sessionIds.size());
        return Result.success("批量删除成功");
    }

    /**
     * 内部方法：获取或创建会话
     */
    private AiSession getOrCreateSession(Long userId, Long sessionId, String firstQuery) {
        // 如果前端传了 sessionId，尝试从数据库获取
        if (sessionId != null) {
            AiSession existingSession = aiSessionMapper.selectById(sessionId);
            // 校验：会话必须存在，且必须属于当前操作的用户（防止越权访问）
            if (existingSession != null && existingSession.getUserId().equals(userId)) {
                return existingSession;
            }
        }

        // 如果没传 sessionId，或者传入的无效，则新建一个会话
        // 截取用户第一句话的前 15 个字作为会话标题
        String title = firstQuery.length() > 15 ? firstQuery.substring(0, 15) + "..." : firstQuery;

        AiSession newSession = AiSession.builder()
                .userId(userId)
                .title(title)
                .build();

        // 插入数据库后，MyBatis-Plus 会自动将生成的自增主键回写到 newSession 对象的 id 字段中
        aiSessionMapper.insert(newSession);

        return newSession;
    }

    /**
     * 内部方法：保存一轮完整的对话记录
     */
    private void saveChatMessage(Long sessionId, Long userId, String userQuery, String aiReply) {
        // 1. 插入用户发送的问题
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role("user")
                .content(userQuery)
                .build();
        chatMessageMapper.insert(userMessage);

        // 2. 插入 AI 的回复
        ChatMessage aiMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId) // AI 的回复同样归属在这个用户下
                .role("assistant")
                .content(aiReply)
                .build();
        chatMessageMapper.insert(aiMessage);
    }
}