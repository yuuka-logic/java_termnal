package com.example.personal_blog.entity;

import java.time.LocalDateTime;

/**
 * 文章标签关联实体，对应 personal_blog.article_tag 表。
 * 联合主键 (article_id, tag_id)，是 article 与 tag 多对多关系的中间表。
 */
public class ArticleTagLink {

    /** 文章ID，联合主键之一 */
    private Long articleId;

    /** 标签ID，联合主键之一 */
    private Integer tagId;

    /** 关联创建时间 */
    private LocalDateTime createdAt;

    public ArticleTagLink() {
    }

    public ArticleTagLink(Long articleId, Integer tagId) {
        this.articleId = articleId;
        this.tagId = tagId;
    }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public Integer getTagId() { return tagId; }
    public void setTagId(Integer tagId) { this.tagId = tagId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ArticleTagLink{articleId=" + articleId + ", tagId=" + tagId + '}';
    }
}
