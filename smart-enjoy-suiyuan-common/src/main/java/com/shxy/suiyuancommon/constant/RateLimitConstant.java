package com.shxy.suiyuancommon.constant;

/**
 * 限流相关常量
 *
 * 命名规范：
 * 1. 所有字母统一使用小写
 * 2. 格式：模块：类型：标识（使用冒号分隔）
 * 3. 模块划分：rate_limit（限流）
 *
 * 示例：
 * - rate_limit:sms:{userId}       短信验证码限流
 * - rate_limit:upload:{userId}    文件上传限流
 * - rate_limit:api:{userId}       通用 API 限流
 *
 * @author Wu, Hui Ming
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
}
