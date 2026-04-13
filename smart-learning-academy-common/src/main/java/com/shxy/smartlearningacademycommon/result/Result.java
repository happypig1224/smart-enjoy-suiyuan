package com.shxy.smartlearningacademycommon.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 封装统一响应结果集
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 22:21
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;  // 状态码,200代表成功
    private String message;  // 错误信息
    private T data;  // 响应数据

    //1、成功
    // 1.1无信息无数据
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        return result;
    }
    // 1.2带信息无数据
    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = message;
        return result;
    }
    // 1.3无信息带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }
    // 1.4带信息带数据
    public static <T> Result<T> success(String message,T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = message;
        result.data = data;
        return result;
    }
    //2、失败
    // 2.1无信息无数据
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.code = 500;
        result.message = "操作失败";
        return result;
    }
    // 2.2带信息无数据
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.message = message;
        return result;
    }
    // 2.3无信息带数据
    public static <T> Result<T> fail(T data) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.message = "操作失败";
        result.data = data;
        return result;
    }
    // 2.4带信息带数据
    public static <T> Result<T> fail(String message,T data) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.message = message;
        result.data = data;
        return result;
    }
}
