package com.example.personal_blog.dto;

/**
 * 批量加载标签时用的扁平结果对象。
 * 一次查回 (articleId, tagId, tagName, tagSlug)，由 Service 按 articleId 分组，避免 N+1。
 */
public class TagBrief {

    private Long articleId;
    private Integer id;
    private String name;
    private String slug;

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
}
