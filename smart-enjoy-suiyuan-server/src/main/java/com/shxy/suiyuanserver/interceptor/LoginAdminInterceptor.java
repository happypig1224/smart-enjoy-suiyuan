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
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.shxy.suiyuancommon.constant.RedisConstant.USER_TOKEN_KEY_PREFIX;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 20:56
 */
@Component
@Slf4j
public class LoginAdminInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    private static final ObjectMapper objectMapper =new ObjectMapper();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 获取请求头中的token
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token == null || token.isEmpty()) {
            log.warn("请求头中缺少token: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail("未登录")));
            return false;
        }
        log.info("解析的token:{}", token);
        // 解析token
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimConstant.USER_ID).toString());
            log.info("解析的userId:{}", userId);
            long ttl = redisTemplate.getExpire(USER_TOKEN_KEY_PREFIX+userId);
            if (ttl > 0 && ttl < 300){
                redisTemplate.expire(USER_TOKEN_KEY_PREFIX+userId,jwtProperties.getAdminTtl(), TimeUnit.MILLISECONDS);
                log.info("用户:{}的token续期成功",userId);

                Map<String, Object> newClaims = new HashMap<>();
                newClaims.put(JwtClaimConstant.USER_ID, userId);
                String newToken = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), newClaims);

                redisTemplate.opsForValue().set(USER_TOKEN_KEY_PREFIX + userId, newToken, jwtProperties.getAdminTtl(), TimeUnit.MILLISECONDS);

                response.setHeader(jwtProperties.getAdminTokenName(), newToken);
                log.info("为用户:{}生成了新的JWT token", userId);
            }
            //将线程变量设置用户ID
            BaseContext.setCurrentUserId(userId);

        } catch (Exception e) {
            log.warn("token解析失败: {}",e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail("token无效或者已经过期")));
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除线程变量
        BaseContext.remove();
    }
}
