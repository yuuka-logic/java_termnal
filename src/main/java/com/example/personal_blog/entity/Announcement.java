package com.example.personal_blog.entity;

import java.time.LocalDateTime;

/**
 * 通知公告实体，对应 personal_blog.announcement 表。
 */
public class Announcement {

    /** 公告ID，主键 */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 公告类型：1-系统通知 2-维护公告 3-版本更新 4-活动通知 */
    private Integer type;

    /** 优先级：0-普通 1-重要 2-紧急 */
    private Integer priority;

    /** 状态：0-草稿 1-已发布 2-已撤回 */
    private Integer status;

    /** 发布人ID，关联 user.id */
    private Long publisherId;

    /** 查看次数 */
    private Integer viewCount;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 失效时间，NULL 表示永久有效 */
    private LocalDateTime expireTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 非数据库字段：发布人昵称，列表展示用 */
    private String publisherName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }

    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }

    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    @Override
    public String toString() {
        return "Announcement{id=" + id + ", title='" + title + "', type=" + type
                + ", status=" + status + ", publisherId=" + publisherId + '}';
    }
}
