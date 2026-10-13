package com.example.personal_blog.dto;

import com.example.personal_blog.common.PageQuery;

/**
 * 通知公告查询条件。
 */
public class AnnouncementQuery extends PageQuery {

    private String keyword;

    /** 公告类型：1-系统通知 2-维护公告 3-版本更新 4-活动通知 */
    private Integer type;

    /** 状态：0-草稿 1-已发布 2-已撤回 */
    private Integer status;

    /** 优先级：0-普通 1-重要 2-紧急 */
    private Integer priority;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String safeKeyword() {
        return (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
    }
}
