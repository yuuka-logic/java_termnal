package com.example.personal_blog.dto;

import com.example.personal_blog.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章展示对象。
 * <p>
 * 刻意不继承 Article 实体：若继承，列表接口会把父类的 content getter 一起序列化出去
 * （值为 null 也会出现在 JSON 的字段列表里，污染接口契约）。
 * 这里显式声明字段，列表接口不查 content 则响应里就没有这个 key，详情接口查了才有。
 */
public class ArticleVO {

    private Long id;
    private String title;
    private String summary;

    /** 正文。只有详情接口会填充，列表接口为 null 且不会出现在 JSON 中 */
    private String content;

    private String coverImage;
    private Integer categoryId;
    private Long authorId;
    private Integer status;
    private Integer isTop;
    private Integer isOriginal;
    private String sourceUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime publishTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** ===== 联表补充字段 ===== */
    private String authorName;
    private String authorUsername;
    private String categoryName;

    /** 标签列表，来自 article_tag */
    private List<Tag> tags;

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

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}
