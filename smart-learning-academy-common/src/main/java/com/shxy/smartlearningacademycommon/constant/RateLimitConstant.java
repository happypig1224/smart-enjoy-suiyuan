package com.shxy.smartlearningacademycommon.constant;

/**
 * 限流相关常量
 *
 * @author Wu, Hui Ming
 * @since 2026/4/13
 */
public class RateLimitConstant {
    
    /**
     * 短信验证码限流key前缀
     */
    public static final String SMS_RATE_LIMIT_KEY = "rate_limit:sms:";
    
    /**
     * 文件上传限流key前缀
     */
    public static final String UPLOAD_RATE_LIMIT_KEY = "rate_limit:upload:";
    
    /**
     * 通用API限流key前缀
     */
    public static final String API_RATE_LIMIT_KEY = "rate_limit:api:";
    
    /**
     * 短信验证码: 60秒内最多发送1次
     */
    public static final int SMS_TIME_WINDOW = 60;
    public static final int SMS_MAX_REQUESTS = 1;
    
    /**
     * 文件上传: 60秒内最多上传5次
     */
    public static final int UPLOAD_TIME_WINDOW = 60;
    public static final int UPLOAD_MAX_REQUESTS = 5;
    
    /**
     * 默认API限流: 60秒内最多请求100次
     */
    public static final int DEFAULT_TIME_WINDOW = 60;
    public static final int DEFAULT_MAX_REQUESTS = 100;
}
