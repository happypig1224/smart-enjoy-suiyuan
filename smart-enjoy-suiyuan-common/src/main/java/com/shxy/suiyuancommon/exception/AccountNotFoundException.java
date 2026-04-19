package com.shxy.suiyuancommon.exception;

/**
 * 账号未找到异常
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4
 */
public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException() {
        super("账号不存在!");
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}
