package com.shxy.smartlearningacademycommon.exception;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/6 20:40
 */
public class PhoneExistsException extends RuntimeException {
    public PhoneExistsException() {
        super("手机号已存在!");
    }
}
