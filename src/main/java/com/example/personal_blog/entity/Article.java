package com.example.personal_blog.entity;

import java.time.LocalDateTime;

/**
 * 文章实体，对应 personal_blog.article 表。
 * 通过 category_id 关联分类、author_id 关联作者；与标签的多对多关系走 article_tag 中间表。
 */
public class Article {

    /** 文章ID，主键 */
    private Long id;

    /** 文章标题 */
    private String title;

    /** 文章摘要，列表页展示 */
    private String summary;

    /** 文章正文，Markdown 原文 */
    private String content;

    /** 封面图 URL */
    private String coverImage;

    /** 所属分类ID，关联 category.id */
    private Integer categoryId;

    /** 作者ID，关联 user.id */
    private Long authorId;

    /** 状态：0-草稿 1-已发布 2-已下架 */
    private Integer status;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 是否原创：0-转载 1-原创 */
    private Integer isOriginal;

    /** 转载原文地址 */
    private String sourceUrl;

    /** 阅读量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 发布时间 */
    private LocalDateTime publishTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }

    public Integer getIsOriginal() { return isOriginal; }
    public void setIsOriginal(Integer isOriginal) { this.isOriginal = isOriginal; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Article{id=" + id + ", title='" + title + "', categoryId=" + categoryId
                + ", authorId=" + authorId + ", status=" + status + '}';
    }
}
