package com.shxy.suiyuancommon.exception;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException() {
        super("用户名已存在!");
    }
}
