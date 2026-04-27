package com.shxy.suiyuanserver.service;

import com.shxy.suiyuanentity.vo.ChatResponseVO;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/27 17:37
 */
public interface AiChatService {
    /**
     * AI 聊天
     * @param userId 用户ID
     * @param query 用户问题
     * @param sessionId 会话ID（首次为null）
     * @return 包含sessionId和AI回复的响应
     */
    ChatResponseVO chat(Long userId, String query, Long sessionId);
}
