package com.shxy.suiyuanserver.aop;

import com.shxy.suiyuancommon.annotation.RateLimit;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.RateLimitUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/5/05 13:11
 */
@Aspect
@Component("rateLimitAopAspect")
@Slf4j
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(rateLimit);
        RateLimitUtil.checkRateLimit(stringRedisTemplate, key, rateLimit.timeWindow(), rateLimit.maxRequests());
        return joinPoint.proceed();
    }

    private String buildKey(RateLimit rateLimit) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId != null && userId > 0) {
            return rateLimit.key() + ":user:" + userId;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getRemoteAddr();
            return rateLimit.key() + ":ip:" + ip;
        }

        return rateLimit.key() + ":unknown";
    }
}
