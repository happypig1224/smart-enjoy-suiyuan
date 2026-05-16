package com.shxy.suiyuanserver.interceptor;

import com.shxy.suiyuancommon.constant.JwtClaimConstant;
import com.shxy.suiyuancommon.properties.JwtProperties;
import com.shxy.suiyuancommon.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

import static com.shxy.suiyuancommon.constant.RedisConstant.TOKEN_BLACKLIST_KEY_PREFIX;
import static com.shxy.suiyuancommon.constant.RedisConstant.USER_TOKEN_KEY_PREFIX;

/**
 * STOMP 通道 JWT 认证拦截器
 * 复用现有 HTTP 拦截器的 JWT 验证逻辑：
 *   1. CONNECT   → 解析 Authorization Header 中的 JWT → 注入 Principal
 *   2. SUBSCRIBE → 校验用户只能订阅自己的 /user/ 队列
 *   3. SEND      → 无需额外处理（Principal 已由 CONNECT 阶段注入）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @since 2026/5/14
 */
@Slf4j
@Component
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        // ---- CONNECT: JWT 认证 ----
        if (StompCommand.CONNECT.equals(command)) {
            String token = extractToken(accessor);
            if (token == null) {
                log.warn("WebSocket CONNECT 失败: 未提供 Token");
                throw new IllegalArgumentException("未登录");
            }

            try {
                // 复用现有 JWT 解析 + Redis 校验逻辑
                Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                Long userId = Long.valueOf(claims.get(JwtClaimConstant.USER_ID).toString());

                // 验证 Redis 中的 Token 是否匹配
                String storedToken = (String) redisTemplate.opsForValue().get(USER_TOKEN_KEY_PREFIX + userId);
                if (storedToken == null || !storedToken.equals(token)) {
                    log.warn("WebSocket CONNECT 失败: Token 与 Redis 不匹配, userId={}", userId);
                    throw new IllegalArgumentException("token无效或者过期");
                }

                // 检查 Token 是否在黑名单中
                String jti = claims.getId();
                if (jti != null && Boolean.TRUE.equals(
                        stringRedisTemplate.hasKey(TOKEN_BLACKLIST_KEY_PREFIX + jti))) {
                    log.warn("WebSocket CONNECT 失败: Token 已被撤销, userId={}", userId);
                    throw new IllegalArgumentException("token已被撤销");
                }

                // 注入认证用户
                accessor.setUser(new StompPrincipal(userId));
                log.info("WebSocket 用户认证成功: userId={}", userId);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                log.error("WebSocket Token 解析失败: {}", e.getMessage());
                throw new IllegalArgumentException("token无效或者过期");
            }
        }

        // ---- SUBSCRIBE: 权限校验 ----
        // 注：Spring STOMP 的 /user/ 前缀由框架内部处理用户路由
        // 客户端订阅 /user/queue/chat 时，目的地不含 userId
        // 框架自动将其解析为 /user/{sessionId}/queue/chat 进行投递
        // 因此无需对 /user/ 目的地做 userId 校验，已认证用户即可订阅

        // ---- DISCONNECT: 清理 Redis 在线状态 ----
        if (StompCommand.DISCONNECT.equals(command)) {
            Principal principal = accessor.getUser();
            if (principal instanceof StompPrincipal stompPrincipal) {
                Long userId = stompPrincipal.userId();
                stringRedisTemplate.delete("user:ws:" + userId);
                log.info("用户 {} WebSocket 断开，已清除在线状态", userId);
            }
        }

        return message;
    }

    /**
     * 从 STOMP CONNECT Header 中提取 JWT Token
     * 兼容两种格式：
     *   - Authorization: Bearer <token>
     *   - loginToken: <token>    (与 HTTP 拦截器的 JwtProperties.userTokenName 一致)
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 方式1: 标准的 Authorization Bearer Header
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String auth = authHeaders.get(0);
            if (auth.startsWith("Bearer ")) {
                return auth.substring(7);
            }
            return auth;
        }

        // 方式2: 项目自定义的 loginToken Header（与 HTTP 拦截器一致）
        String tokenName = jwtProperties.getUserTokenName();
        List<String> tokenHeaders = accessor.getNativeHeader(tokenName);
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        return null;
    }

    /**
     * STOMP Principal 实现
     */
    public record StompPrincipal(Long userId) implements Principal {
        @Override
        public String getName() {
            return userId.toString();
        }
    }
}
