package com.shxy.smartlearningacademycommon.exception;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/5 21:58
 */
public class SmsVerifyFailException extends RuntimeException {
    public SmsVerifyFailException(String message) {
        super("短信验证失败：" + message);
    }
}
