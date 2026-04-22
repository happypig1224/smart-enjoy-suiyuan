package com.shxy.suiyuanserver.handler;

import com.shxy.suiyuancommon.constant.MessageConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 *
 * @author Wu, Hui Ming
 * @version 1.0
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
        log.error("业务异常：{} | Code: {}", e.getMessage(), e.getClass().getSimpleName());
        return Result.fail(e.getMessage());
    }

    /**
     * 处理SQL异常，如唯一键冲突
     */
    @ExceptionHandler
    public Result<Object> handleSqlException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        log.error("SQL异常：{}", message);
        if (message.contains("Duplicate  entry")) {
            return Result.fail("数据已存在,请检查输入");
        } else {
            return Result.fail(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 处理未知异常,用于兜底
     */
    @ExceptionHandler
    public Result<Object> handleException(Exception e) {
        log.error("未知异常：{}，异常类：{}", e.getMessage(), e.getClass().getName());
        // 不返回具体的异常信息给前端，防止敏感信息泄露
        return Result.fail(MessageConstant.UNKNOWN_ERROR);
    }
}
