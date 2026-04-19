package com.shxy.suiyuancommon.exception;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/5 22:00
 */
public class SmsVerifyCodeSendFailException extends RuntimeException {
    public SmsVerifyCodeSendFailException(String message) {
        super("发送短信验证码失败：" + message);
    }
}
