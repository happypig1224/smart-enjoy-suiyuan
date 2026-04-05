package com.shxy.smartlearningacademycommon.exception;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 23:42
 */
public class AccountExistsException extends BaseException {
    public AccountExistsException() {
        super("账号已存在!");
    }
    
    public AccountExistsException(String message) {
        super(message);
    }
}
