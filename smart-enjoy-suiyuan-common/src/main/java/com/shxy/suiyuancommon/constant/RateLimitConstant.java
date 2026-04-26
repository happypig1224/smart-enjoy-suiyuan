package com.shxy.suiyuancommon.constant;

/**
 * 限流相关常量
 * @author Wu, Hui Ming
 * @School SuiHua  University
 * @since 2026/4/13
 */
public class RateLimitConstant {

    // ==================== 限流 Key 前缀 ====================
    /**
     * 短信验证码限流：rate_limit:sms:{userId}
     */
    public static final String SMS_RATE_LIMIT_KEY = "rate_limit:sms:";

    /**
     * 文件上传限流：rate_limit:upload:{userId}
     */
    public static final String UPLOAD_RATE_LIMIT_KEY = "rate_limit:upload:";

    /**
     * 通用 API 限流：rate_limit:api:{userId}
     */
    public static final String API_RATE_LIMIT_KEY = "rate_limit:api:";

    // ==================== 限流配置（统一秒为单位） ====================
    /**
     * 短信验证码限流：60 秒内最多发送 1 次
     */
    public static final int SMS_TIME_WINDOW = 60;
    public static final int SMS_MAX_REQUESTS = 1;

    /**
     * 文件上传限流：60 秒内最多上传 5 次
     */
    public static final int UPLOAD_TIME_WINDOW = 60;
    public static final int UPLOAD_MAX_REQUESTS = 5;

    /**
     * 默认 API 限流：60 秒内最多请求 100 次
     */
    public static final int DEFAULT_TIME_WINDOW = 60;
    public static final int DEFAULT_MAX_REQUESTS = 100;

    /**
     * 评论限流：60 秒内最多评论 10 次
     */
    public static final int COMMENT_TIME_WINDOW = 60;
    public static final int COMMENT_MAX_REQUESTS = 10;
}
