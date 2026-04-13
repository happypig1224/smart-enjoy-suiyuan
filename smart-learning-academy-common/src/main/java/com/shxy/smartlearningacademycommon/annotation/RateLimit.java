package com.shxy.smartlearningacademycommon.annotation;

import java.lang.annotation.*;

/**
 * API限流注解
 *
 * @author Wu, Hui Ming
 * @since 2026/4/13
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流key前缀
     */
    String key() default "rate_limit";
    
    /**
     * 限流时间窗口(秒)
     */
    int timeWindow() default 60;
    
    /**
     * 时间窗口内最大请求次数
     */
    int maxRequests() default 10;
    
    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁,请稍后再试";
}
