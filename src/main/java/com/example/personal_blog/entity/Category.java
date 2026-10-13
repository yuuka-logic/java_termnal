package com.example.personal_blog.entity;

import java.time.LocalDateTime;

/**
 * 分类实体，对应 personal_blog.category 表。
 * 支持父子层级：parent_id = 0 表示顶级分类。
 */
public class Category {

    /** 分类ID，主键 */
    private Integer id;

    /** 分类名称 */
    private String name;

    /** 分类别名，唯一，用于生成 URL */
    private String slug;

    /** 分类描述 */
    private String description;

    /** 父分类ID，0-顶级分类 */
    private Integer parentId;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 逻辑删除：0-未删除 1-已删除 */
    private Integer isDeleted;

    /** 非数据库字段：子分类列表，用于返回树形结构 */
    private java.util.List<Category> children;

    /** 非数据库字段：该分类下的文章数 */
    private Integer articleCount;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public java.util.List<Category> getChildren() { return children; }
    public void setChildren(java.util.List<Category> children) { this.children = children; }

    public Integer getArticleCount() { return articleCount; }
    public void setArticleCount(Integer articleCount) { this.articleCount = articleCount; }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "', slug='" + slug
                + "', parentId=" + parentId + ", status=" + status + '}';
    }
}
