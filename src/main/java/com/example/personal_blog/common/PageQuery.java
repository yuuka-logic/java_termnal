package com.example.personal_blog.common;

import java.util.List;

/**
 * 分页查询入参基类：统一处理分页参数规范化与 LIMIT/OFFSET 换算。
 * 各模块的查询条件继承它，避免每个 Service 重复写一遍边界判断。
 */
public class PageQuery {

    /** 每页最大条数，防止前端传 size=100000 拖垮数据库 */
    public static final int MAX_SIZE = 100;

    private Integer page = 1;
    private Integer size = 10;

    public int safePage() {
        return (page == null || page < 1) ? 1 : page;
    }

    public int safeSize() {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, MAX_SIZE);
    }

    public int offset() {
        return (safePage() - 1) * safeSize();
    }

    /** 便捷方法：把查询结果包装成统一分页响应 */
    public <T> Result<List<T>> toResult(List<T> list, long total) {
        return Result.page(list, total, safePage(), safeSize());
    }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}
