package com.example.personal_blog.entity;

import java.time.LocalDateTime;

/**
 * 标签实体，对应 personal_blog.tag 表。
 * 与文章是多对多关系，通过 article_tag 中间表关联。
 */
public class Tag {

    /** 标签ID，主键 */
    private Integer id;

    /** 标签名称，唯一 */
    private String name;

    /** 标签别名，唯一，用于生成 URL */
    private String slug;

    /** 标签描述 */
    private String description;

    /** 该标签下的文章数（冗余统计字段） */
    private Integer articleCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 逻辑删除：0-未删除 1-已删除 */
    private Integer isDeleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getArticleCount() { return articleCount; }
    public void setArticleCount(Integer articleCount) { this.articleCount = articleCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    @Override
    public String toString() {
        return "Tag{id=" + id + ", name='" + name + "', slug='" + slug
                + "', articleCount=" + articleCount + '}';
    }
}
