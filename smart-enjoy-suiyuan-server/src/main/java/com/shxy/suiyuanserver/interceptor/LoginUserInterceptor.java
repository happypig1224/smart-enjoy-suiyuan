package com.shxy.suiyuanserver.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.constant.JwtClaimConstant;
import com.shxy.suiyuancommon.properties.JwtProperties;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.shxy.suiyuancommon.constant.RedisConstant.USER_TOKEN_KEY_PREFIX;

/**
 * 用户登录拦截器
 * @author Wu, Hui Ming
 * @version 2.0
 * @School Suihua University
 * @since 2026/4/7 12:47
 */
@Component
@Slf4j
public class LoginUserInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String RENEW_LOCK_PREFIX = "lock:token:renew:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 检查方法是否标注了 RequireLogin 注解
        boolean requireLogin = handlerMethod.hasMethodAnnotation(RequireLogin.class);

        String token = request.getHeader(jwtProperties.getUserTokenName());

        if (token == null || token.isEmpty()) {
            if (requireLogin) {
                log.warn("需要登录但未提供token: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(Result.fail("未登录")));
                return false;
            }
            return true;
        }

        try {
            BaseContext.remove();
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimConstant.USER_ID).toString());

            long ttl = redisTemplate.getExpire(USER_TOKEN_KEY_PREFIX + userId);
            if (ttl > 0 && ttl < 1800) {
                renewToken(userId, response);
            }

            BaseContext.setCurrentUserId(userId);
            return true;

        } catch (Exception e) {
            if (requireLogin) {
                log.warn("token解析失败: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(Result.fail("token无效或者过期")));
                return false;
            }
            return true;
        }
    }

    /**
     * Token续期：当剩余有效期不足30分钟时自动续期
     * <p>
     * 使用分布式锁防止并发续期，通过Lua脚本保证原子性
     * </p>
     *
     * @param userId 用户ID
     * @param response HTTP响应
     */
    private void renewToken(Long userId, HttpServletResponse response) {
        String lockKey = RENEW_LOCK_PREFIX + userId;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 5, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                long currentTtl = redisTemplate.getExpire(USER_TOKEN_KEY_PREFIX + userId);
                if (currentTtl > 0 && currentTtl < 1800) {
                    Map<String, Object> newClaims = new HashMap<>();
                    newClaims.put(JwtClaimConstant.USER_ID, userId);
                    String newToken = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), newClaims);

                    redisTemplate.opsForValue().set(USER_TOKEN_KEY_PREFIX + userId, newToken, jwtProperties.getUserTtl() / 1000, TimeUnit.SECONDS);
                    response.setHeader(jwtProperties.getUserTokenName(), newToken);
                    log.info("用户{}的token续期成功", userId);
                }
            } finally {
                unlockRenewLock(lockKey, lockValue);
            }
        }
    }

    /**
     * 安全释放续期锁(使用Lua脚本保证原子性)
     */
    private void unlockRenewLock(String lockKey, String lockValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try {
            redisTemplate.execute(
                    (RedisCallback<Long>) connection ->
                            connection.eval(
                                    script.getBytes(),
                                    ReturnType.INTEGER,
                                    1,
                                    lockKey.getBytes(),
                                    lockValue.getBytes()
                            )
            );
        } catch (Exception e) {
            log.error("释放续期锁失败, userId: {}", lockKey.replace(RENEW_LOCK_PREFIX, ""), e);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.remove();
    }
}
