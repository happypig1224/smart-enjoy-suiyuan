package com.shxy.suiyuanserver.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 管理员登录拦截器
 * <p>
 * 拦截策略：所有管理端接口强制校验登录状态
 * </p>
 *
 * @author Wu, Hui Ming
 * @version 2.0
 * @School Suihua University
 * @since 2026/4/4 20:56
 */
@Component
@Slf4j
public class LoginAdminInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ADMIN_RENEWW_LOCK_PREFIX = "lock:admin:token:renew:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getAdminTokenName());
        if (token == null || token.isEmpty()) {
            log.warn("管理员请求头中缺少token: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail("未登录")));
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimConstant.USER_ID).toString());

            long ttl = stringRedisTemplate.getExpire(USER_TOKEN_KEY_PREFIX + userId, TimeUnit.SECONDS);
            if (ttl > 0 && ttl < 1800) {
                renewToken(userId, response);
            }

            BaseContext.setCurrentUserId(userId);
            return true;

        } catch (Exception e) {
            log.warn("管理员token解析失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail("token无效或者已经过期")));
            return false;
        }
    }

    /**
     * Token续期：当剩余有效期不足30分钟时自动续期
     */
    private void renewToken(Long userId, HttpServletResponse response) {
        String lockKey = ADMIN_RENEWW_LOCK_PREFIX + userId;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 5, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                long currentTtl = stringRedisTemplate.getExpire(USER_TOKEN_KEY_PREFIX + userId, TimeUnit.SECONDS);
                if (currentTtl > 0 && currentTtl < 1800) {
                    Map<String, Object> newClaims = new HashMap<>();
                    newClaims.put(JwtClaimConstant.USER_ID, userId);
                    String newToken = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), newClaims);

                    stringRedisTemplate.opsForValue().set(USER_TOKEN_KEY_PREFIX + userId, newToken, jwtProperties.getAdminTtl() / 1000, TimeUnit.SECONDS);
                    response.setHeader(jwtProperties.getAdminTokenName(), newToken);
                    log.info("管理员{}的token续期成功", userId);
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
            stringRedisTemplate.execute(
                    org.springframework.data.redis.core.script.RedisScript.of(script, Long.class),
                    java.util.Collections.singletonList(lockKey),
                    lockValue
            );
        } catch (Exception e) {
            log.error("释放管理员续期锁失败, userId: {}", lockKey.replace(ADMIN_RENEWW_LOCK_PREFIX, ""), e);
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
