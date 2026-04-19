package com.shxy.suiyuancommon.exception;

/**
 * 密码错误异常
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4
 */
public class PasswordErrorException extends BaseException {
    public PasswordErrorException() {
        super("密码错误!");
    }

    public PasswordErrorException(String message) {
        super(message);
    }
}
