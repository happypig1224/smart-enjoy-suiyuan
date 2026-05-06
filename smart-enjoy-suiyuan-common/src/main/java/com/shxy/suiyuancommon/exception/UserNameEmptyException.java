package com.shxy.suiyuancommon.exception;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/5/4 16:59
 */
public class UserNameEmptyException extends RuntimeException {
    public UserNameEmptyException() {
        super("用户名不能为空");
    }
}
