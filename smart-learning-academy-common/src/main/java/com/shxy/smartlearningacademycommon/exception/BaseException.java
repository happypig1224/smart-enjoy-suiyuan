package com.shxy.smartlearningacademycommon.exception;

/**
 * 基础自定义异常类
 * 其他自定义异常均继承此类
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4
 */
public class BaseException extends RuntimeException {
    public BaseException(String message) {
        super(message);
    }
}
