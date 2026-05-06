package com.shxy.suiyuancommon.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int RATE_LIMITED = 429;
    public static final int SERVER_ERROR = 500;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.message = "操作成功";
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.message = message;
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.code = SERVER_ERROR;
        result.message = "操作失败";
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.code = SERVER_ERROR;
        result.message = message;
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> fail(T data) {
        Result<T> result = new Result<>();
        result.code = SERVER_ERROR;
        result.message = "操作失败";
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(String message, T data) {
        Result<T> result = new Result<>();
        result.code = SERVER_ERROR;
        result.message = message;
        result.data = data;
        return result;
    }
}
