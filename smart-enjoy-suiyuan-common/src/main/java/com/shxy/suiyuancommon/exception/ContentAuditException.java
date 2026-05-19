package com.shxy.suiyuancommon.exception;

public class ContentAuditException extends BaseException {
    public ContentAuditException() {
        super("内容包含违规信息，发布失败");
    }

    public ContentAuditException(String message) {
        super(message);
    }
}
