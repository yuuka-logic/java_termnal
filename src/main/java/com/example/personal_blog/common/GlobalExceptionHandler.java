package com.example.personal_blog.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把异常统一转成 Result，避免 500 堆栈直接抛给前端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：按异常自带 code 返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 请求体 JSON 解析失败、缺少必填参数 */
    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public Result<Void> handleBadRequest(Exception e) {
        log.warn("请求参数错误: {}", e.getMessage());
        return Result.fail(400, "请求参数格式错误：" + e.getMessage());
    }

    /** 兜底：记录完整堆栈，但不把细节返回给前端 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("服务器内部错误", e);
        return Result.fail(500, "服务器内部错误，请联系管理员");
    }
}
