package com.shxy.suiyuancommon.exception;

/**
 * 资源相关异常
 * 
 * @author Tech Lead
 */
public class ResourceException extends BaseException {
    public ResourceException(String message) {
        super(message);
    }

    public static ResourceException notFound(String resourceId) {
        return new ResourceException("资源不存在: " + resourceId);
    }

    public static ResourceException unauthorized(String operation, String resourceId) {
        return new ResourceException("无权执行操作: " + operation + ", 资源ID: " + resourceId);
    }
}