package com.shxy.suiyuanserver.service;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/27 17:37
 */
public interface AiChatService {
    String chat(Long userId, String query, Long sessionId);
}
