package com.example.personal_blog.common;

/**
 * 业务异常。抛出后由 GlobalExceptionHandler 统一转成 Result，避免 500 堆栈直接暴露给前端。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
