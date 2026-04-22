package com.shxy.suiyuanserver.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API限流过滤器
 * 
 * @author Tech Lead
 */
@Slf4j
@Component
public class ApiRateLimitFilter extends HttpFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${smart.enjoy.suiyuan.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${smart.enjoy.suiyuan.rate-limit.default.requests:100}")
    private int defaultRequests;

    @Value("${smart.enjoy.suiyuan.rate-limit.default.window-seconds:60}")
    private int defaultWindowSeconds;

    // Lua脚本实现令牌桶算法
    private static final String RATE_LIMIT_SCRIPT =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = redis.call('GET', key) " +
            "if current == false then " +
            "  redis.call('SET', key, 1) " +
            "  redis.call('EXPIRE', key, window) " +
            "  return {1, limit - 1} " +
            "end " +
            "current = tonumber(current) " +
            "if current < limit then " +
            "  local new_current = redis.call('INCR', key) " +
            "  if new_current == 1 then " +
            "    redis.call('EXPIRE', key, window) " +
            "  end " +
            "  return {new_current, limit - new_current} " +
            "else " +
            "  return {current, -1} " +
            "end";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!rateLimitEnabled) {
            chain.doFilter(request, response);
            return;
        }

        // 对特定接口进行限流
        String uri = request.getRequestURI();
        if (shouldApplyRateLimit(uri)) {
            String clientIp = getClientIp(request);
            String rateLimitKey = "rate_limit:" + uri + ":" + clientIp;

            try {
                RedisScript<List> script = RedisScript.of(RATE_LIMIT_SCRIPT, List.class);
                List<Long> result = (List<Long>) redisTemplate.execute(script,
                        Collections.singletonList(rateLimitKey),
                        String.valueOf(defaultRequests),
                        String.valueOf(defaultWindowSeconds));

                Long currentRequests = result.get(0);
                Long remaining = result.get(1);

                if (remaining < 0) {
                    log.warn("API限流触发，IP: {}, URI: {}, 当前请求数: {}", clientIp, uri, currentRequests);
                    response.setStatus(429); // Too Many Requests
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(
                            Result.fail("请求过于频繁，请稍后再试")));
                    return;
                }

                // 设置响应头告知客户端剩余请求数
                response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
                response.setHeader("X-RateLimit-Limit", String.valueOf(defaultRequests));
                response.setHeader("X-RateLimit-Reset", 
                        String.valueOf(System.currentTimeMillis() + (defaultWindowSeconds * 1000)));

            } catch (Exception e) {
                log.error("限流检查异常", e);
                // 发生异常时不阻止请求，避免影响正常业务
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 判断是否需要应用限流
     */
    private boolean shouldApplyRateLimit(String uri) {
        // 对以下类型的接口进行限流
        return uri.startsWith("/user/") && 
               (uri.contains("/comment/") || 
                uri.contains("/post/") || 
                uri.contains("/resource/upload") ||
                uri.contains("/user/register") ||
                uri.contains("/user/login") ||
                uri.contains("/user/captcha"));
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个非unknown的有效IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}