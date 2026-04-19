package com.shxy.suiyuancommon.utils;

import com.shxy.suiyuancommon.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 限流工具类 - 基于Redis
 *
 * @author Wu, Hui Ming
 * @since 2026/4/13
 */
@Slf4j
public class RateLimitUtil {
    
    /**
     * 检查是否触发限流
     *
     * @param redisTemplate Redis模板
     * @param key 限流key
     * @param timeWindow 时间窗口(秒)
     * @param maxRequests 最大请求次数
     * @throws RateLimitException 触发限流时抛出
     */
    public static void checkRateLimit(RedisTemplate<String, Object> redisTemplate, String key,
                                      int timeWindow, int maxRequests) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            
            // 第一次请求,设置过期时间
            if (count != null && count == 1) {
                redisTemplate.expire(key, timeWindow, TimeUnit.SECONDS);
            }
            
            // 超过限制次数
            if (count != null && count > maxRequests) {
                log.warn("触发限流: key={}, count={}/{}", key, count, maxRequests);
                throw new RateLimitException();
            }
            
            log.debug("限流检查通过: key={}, count={}/{}", key, count, maxRequests);
            
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("限流检查异常: key={}", key, e);
        }
    }
    
    /**
     * 获取剩余请求次数
     *
     * @param redisTemplate Redis模板
     * @param key 限流key
     * @param maxRequests 最大请求次数
     * @return 剩余次数
     */
    public static long getRemainingRequests(StringRedisTemplate redisTemplate, String key, int maxRequests) {
        try {
            Long count = redisTemplate.opsForValue().get(key) != null ? 
                Long.parseLong(redisTemplate.opsForValue().get(key)) : 0;
            return Math.max(0, maxRequests - count);
        } catch (Exception e) {
            log.error("获取剩余请求次数异常: key={}", key, e);
            return maxRequests;
        }
    }
}
