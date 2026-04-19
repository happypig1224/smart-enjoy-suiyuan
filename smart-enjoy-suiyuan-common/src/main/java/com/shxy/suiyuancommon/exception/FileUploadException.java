package com.shxy.suiyuancommon.exception;

/**
 * 资源文件上传异常
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4
 */
public class FileUploadException extends BaseException {
    public FileUploadException() {
        super("文件上传失败!");
    }

    public FileUploadException(String message) {
        super(message);
    }
}
