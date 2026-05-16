package com.shxy.suiyuanserver.listener;

import com.shxy.suiyuanserver.interceptor.StompJwtChannelInterceptor.StompPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.security.Principal;
import java.util.Set;

/**
 * WebSocket 会话生命周期监听器
 * 管理 Redis 中的用户在线状态
 *
 * 设计：使用 INCR/DECR 计数器支持多端（多标签页）同时在线
 *   - 连接建立 → INCR，无 TTL（连接存在 = 在线）
 *   - 连接断开 → DECR，计数为 0 时删除 key
 *   - 启动时清理残留 key（处理服务器异常重启场景）
 *
 * @author Wu, Hui Ming
 * @version 2.0
 * @since 2026/5/12
 */
@Slf4j
@Component
public class WebSocketEventListener {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** Redis 在线状态 Key 前缀 */
    private static final String ONLINE_KEY_PREFIX = "user:ws:";

    /**
     * 启动时清理残留的在线状态 key
     * 处理场景：服务器异常重启，Redis 中的 key 未清理
     */
    @PostConstruct
    public void cleanupStaleKeys() {
        Set<String> keys = stringRedisTemplate.keys(ONLINE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.info("启动清理：移除 {} 个残留在线状态 key", keys.size());
        }
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal instanceof StompPrincipal stompPrincipal) {
            Long userId = stompPrincipal.userId();
            String key = ONLINE_KEY_PREFIX + userId;
            // INCR：支持多端/多标签页，不设 TTL
            // 返回值即当前连接数
            Long count = stringRedisTemplate.opsForValue().increment(key);
            log.info("WebSocket 连接建立: userId={}, sessionId={}, 当前连接数={}",
                    userId, accessor.getSessionId(), count);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (principal instanceof StompPrincipal stompPrincipal) {
            Long userId = stompPrincipal.userId();
            String key = ONLINE_KEY_PREFIX + userId;
            Long count = stringRedisTemplate.opsForValue().decrement(key);
            if (count != null && count <= 0) {
                stringRedisTemplate.delete(key);
            }
            log.info("WebSocket 连接断开: userId={}, sessionId={}, 剩余连接数={}",
                    userId, accessor.getSessionId(), count);
        }
    }
}
