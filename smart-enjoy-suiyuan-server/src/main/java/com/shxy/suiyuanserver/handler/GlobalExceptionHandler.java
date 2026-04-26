package com.shxy.suiyuanserver.handler;

import com.shxy.suiyuancommon.constant.MessageConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 * @author Wu, Hui Ming
 * @version 2.0
 * @School Suihua University
 * @since 2026/4/5 13:11
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler
    public Result<Object> handleBusinessException(BaseException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", errorMsg);
        return Result.fail(errorMsg);
    }

    /**
     * 处理404资源未找到异常
     */
    @ExceptionHandler
    public Result<Object> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());
        return Result.fail("请求的资源不存在");
    }

    /**
     * 处理SQL异常，如唯一键冲突
     */
    @ExceptionHandler
    public Result<Object> handleSqlException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        log.error("SQL异常: {}", message);
        if (message.contains("Duplicate entry")) {
            return Result.fail("数据已存在,请检查输入");
        } else {
            return Result.fail(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 处理未知异常,用于兜底
     * 生产环境禁止向客户端暴露堆栈信息
     */
    @ExceptionHandler
    public Result<Object> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.fail(MessageConstant.UNKNOWN_ERROR);
    }
}
