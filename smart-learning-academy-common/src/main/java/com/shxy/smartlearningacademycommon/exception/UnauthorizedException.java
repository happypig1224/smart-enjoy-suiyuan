package com.shxy.smartlearningacademycommon.exception;

/**
 * 认证授权失败异常（Token失效、无权限等）
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4
 */
public class UnauthorizedException extends BaseException {
    public UnauthorizedException() {
        super("认证失败，请重新登录!");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
