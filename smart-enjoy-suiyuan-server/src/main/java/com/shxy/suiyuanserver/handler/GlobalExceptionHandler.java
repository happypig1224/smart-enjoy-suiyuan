package com.shxy.suiyuanserver.handler;

import com.shxy.suiyuancommon.constant.MessageConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.exception.RateLimitException;
import com.shxy.suiyuancommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result<Object> handleBusinessException(BaseException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Object> handleRateLimitException(RateLimitException e) {
        log.warn("限流异常: {}", e.getMessage());
        return Result.fail(Result.RATE_LIMITED, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", errorMsg);
        return Result.fail(Result.BAD_REQUEST, errorMsg);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.fail(Result.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Object> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return Result.fail(Result.NOT_FOUND, "请求的资源不存在");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getMessage());
        return Result.fail(Result.BAD_REQUEST, "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        String errorMsg = e.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("参数校验失败");
        log.warn("方法参数校验失败: {}", errorMsg);
        return Result.fail(Result.BAD_REQUEST, errorMsg);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return Result.fail(405, "不支持的请求方法");
    }

    @ExceptionHandler
    public Result<Object> handleSqlException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        log.error("SQL异常: {}", message);
        if (message.contains("Duplicate entry")) {
            return Result.fail(Result.BAD_REQUEST, "数据已存在,请检查输入");
        } else {
            return Result.fail(MessageConstant.UNKNOWN_ERROR);
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.fail(MessageConstant.UNKNOWN_ERROR);
    }
}
