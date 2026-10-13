package com.example.personal_blog.common;

import java.util.List;

/**
 * 统一响应体。
 * 约定：code=200 成功，400 参数错误，404 不存在，409 冲突，500 服务器错误。
 */
public class Result<T> {

    private int code;
    private String message;
    private T data;

    /** 分页相关字段，非分页接口为 null */
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 构造分页响应 */
    public static <T> Result<List<T>> page(List<T> list, long total, int page, int size) {
        Result<List<T>> r = new Result<>(200, "查询成功", list);
        r.total = total;
        r.page = page;
        r.size = size;
        r.pages = (int) Math.ceil((double) total / size);
        return r;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }
}
