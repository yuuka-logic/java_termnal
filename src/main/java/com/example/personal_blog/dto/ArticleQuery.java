package com.example.personal_blog.dto;

import com.example.personal_blog.common.PageQuery;

/**
 * 文章查询条件。支持关键字、分类、作者、状态、标签五个维度的组合过滤。
 * <p>
 * 注意：这里刻意不继承 Article 实体。
 * MyBatis 在「参数对象类型 == resultType」时会直接把参数对象当结果返回（RETURN_SELF），
 * 若把 Article 混进查询条件，列表接口就会把未查询的字段（如正文 content）一起带出去。
 * 因此查询条件只保留过滤字段，与实体完全解耦。
 */
public class ArticleQuery extends PageQuery {

    /** 关键字：模糊匹配标题与摘要 */
    private String keyword;

    private Integer categoryId;

    private Long authorId;

    /** 状态：0-草稿 1-已发布 2-已下架 */
    private Integer status;

    /** 按标签过滤 */
    private Integer tagId;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getTagId() { return tagId; }
    public void setTagId(Integer tagId) { this.tagId = tagId; }

    public String safeKeyword() {
        return (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
    }
}
