package com.shxy.suiyuancommon.utils;

import com.shxy.suiyuancommon.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * 限流工具类 - 基于Redis Lua脚本保证原子性
 * @author Wu, Hui Ming
 * @School SuiHua  University
 * @since 2026/4/13 11:07
 */
@Slf4j
public class RateLimitUtil {

    /**
     * Lua脚本：原子性执行计数器递增和过期时间设置
     */
    private static final String RATE_LIMIT_LUA_SCRIPT =
            "local count = redis.call('INCR', KEYS[1]) " +
            "if count == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return count";

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();

    static {
        RATE_LIMIT_SCRIPT.setScriptText(RATE_LIMIT_LUA_SCRIPT);
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    /**
     * 检查是否触发限流
     *
     * @param stringRedisTemplate Redis模板
     * @param key 限流key
     * @param timeWindow 时间窗口(秒)
     * @param maxRequests 最大请求次数
     * @throws RateLimitException 触发限流时抛出
     */
    public static void checkRateLimit(StringRedisTemplate stringRedisTemplate, String key,
                                      int timeWindow, int maxRequests) {
        try {
            Long count = stringRedisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(timeWindow)
            );

            if (count != null && count > maxRequests) {
                log.warn("触发限流: key={}, count={}/{}", key, count, maxRequests);
                throw new RateLimitException();
            }

            log.debug("限流检查通过: key={}, count={}/{}", key, count, maxRequests);

        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("限流检查异常，降级为拒绝请求: key={}", key, e);
            throw new RateLimitException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 获取剩余请求次数
     *
     * @param stringRedisTemplate Redis模板
     * @param key 限流key
     * @param maxRequests 最大请求次数
     * @return 剩余次数
     */
    public static long getRemainingRequests(StringRedisTemplate stringRedisTemplate, String key, int maxRequests) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            Long count = value != null ? Long.parseLong(value) : 0;
            return Math.max(0, maxRequests - count);
        } catch (Exception e) {
            log.error("获取剩余请求次数异常: key={}", key, e);
            return maxRequests;
        }
    }
}
