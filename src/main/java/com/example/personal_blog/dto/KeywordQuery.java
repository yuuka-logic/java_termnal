package com.example.personal_blog.dto;

import com.example.personal_blog.common.PageQuery;

/**
 * 通用关键字查询条件：绝大多数列表接口都需要「关键字 + 状态」过滤。
 */
public class KeywordQuery extends PageQuery {

    /** 关键字，具体匹配哪些列由各模块的 SQL 决定 */
    private String keyword;

    /** 状态过滤，null 表示不过滤 */
    private Integer status;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    /** SQL 里用 trim 后的值做 LIKE，避免前后空格导致匹配不到 */
    public String safeKeyword() {
        return (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
    }
}
