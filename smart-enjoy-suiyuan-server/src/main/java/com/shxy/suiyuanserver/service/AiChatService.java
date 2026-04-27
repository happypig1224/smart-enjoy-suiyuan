package com.shxy.suiyuanserver.service;

import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.ChatMessage;
import com.shxy.suiyuanentity.vo.ChatMessageVO;
import com.shxy.suiyuanentity.vo.ChatResponseVO;
import com.shxy.suiyuanentity.vo.SessionVO;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/27 17:37
 */
public interface AiChatService {
    /**
     * AI 聊天
     * @param query 用户问题
     * @param sessionId 会话ID（首次为null）
     * @return 包含sessionId和AI回复的响应
     */
    ChatResponseVO chat(String query, Long sessionId);

    /**
     * 获取所有历史会话列表
     * @return 会话列表（包含会话ID和标题）
     */
    List<SessionVO> getHistory();

    /**
     * 获取指定会话的历史消息
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    List<ChatMessageVO> getHistoryMessage(Long sessionId);

    /**
     * 删除指定会话及其所有消息
     * @param sessionId 会话ID
     * @return 操作结果
     */
    Result<String> deleteSession(Long sessionId);
}
