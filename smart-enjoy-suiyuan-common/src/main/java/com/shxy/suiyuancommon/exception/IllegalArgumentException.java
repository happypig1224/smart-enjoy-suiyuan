package com.shxy.suiyuancommon.exception;

/**
 * 非法参数异常（参数校验不通过等）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4
 */
public class IllegalArgumentException extends BaseException {
    public IllegalArgumentException() {
        super("参数不合法!");
    }

    public IllegalArgumentException(String message) {
        super(message);
    }
}
