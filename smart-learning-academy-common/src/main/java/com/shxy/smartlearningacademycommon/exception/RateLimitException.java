package com.shxy.smartlearningacademycommon.exception;

/**
 * 限流异常
 *
 * @author Wu, Hui Ming
 * @since 2026/4/13
 */
public class RateLimitException extends BaseException {
    
    public RateLimitException() {
        super("请求过于频繁,请稍后再试");
    }
    
    public RateLimitException(String message) {
        super(message);
    }
}
