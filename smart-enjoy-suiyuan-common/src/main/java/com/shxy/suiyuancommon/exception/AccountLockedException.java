package com.shxy.suiyuancommon.exception;

/**
 * 账号被锁定或禁用异常
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4
 */
public class AccountLockedException extends BaseException {
    public AccountLockedException() {
        super("账号已被锁定，请联系管理员!");
    }

    public AccountLockedException(String message) {
        super(message);
    }
}
