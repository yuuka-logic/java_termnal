package com.example.personal_blog.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理：把异常统一转成 Result，避免 500 堆栈直接抛给前端。
 * <p>
 * 注意：本项目业务码放在响应体的 code 字段里，HTTP 状态始终是 200。
 * 因此这里会把失败标记写进 request attribute，供操作日志拦截器判断本次操作是否成功，
 * 否则"失败的写操作"会被日志误记成成功。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 请求属性名：本次请求的业务失败标记，供 OperationLogInterceptor 读取 */
    public static final String ATTR_BUSINESS_ERROR = "opLog.businessError";

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：按异常自带 code 返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {}", e.getMessage());
        markFailed(request, e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 请求体 JSON 解析失败、缺少必填参数 */
    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public Result<Void> handleBadRequest(Exception e, HttpServletRequest request) {
        log.warn("请求参数错误: {}", e.getMessage());
        markFailed(request, e.getMessage());
        return Result.fail(400, "请求参数格式错误：" + e.getMessage());
    }

    /** 兜底：记录完整堆栈，但不把细节返回给前端 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e, HttpServletRequest request) {
        log.error("服务器内部错误", e);
        markFailed(request, e.getClass().getSimpleName() + ": " + e.getMessage());
        return Result.fail(500, "服务器内部错误，请联系管理员");
    }

    private void markFailed(HttpServletRequest request, String message) {
        if (request != null) {
            request.setAttribute(ATTR_BUSINESS_ERROR, message == null ? "业务处理失败" : message);
        }
    }
}
