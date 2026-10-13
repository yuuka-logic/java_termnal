package com.example.personal_blog.mapper;

import com.example.personal_blog.dto.TagBrief;
import com.example.personal_blog.entity.ArticleTagLink;
import com.example.personal_blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 文章标签关联表数据访问接口。
 * 用「批量查询」而非逐条查询，避免列表页出现 N+1 查询。
 */
@Mapper
public interface ArticleTagMapper {

    /** 某篇文章的标签 */
    List<Tag> selectTagsByArticleId(@Param("articleId") Long articleId);

    /**
     * 批量查询多篇文章的标签，返回扁平结果由 Service 按 articleId 分组回填。
     */
    List<TagBrief> selectTagsByArticleIds(@Param("articleIds") Collection<Long> articleIds);

    /** 批量插入关联。主键是 (article_id, tag_id)，用 INSERT IGNORE 跳过重复 */
    int batchInsert(@Param("list") List<ArticleTagLink> list);

    int deleteByArticleId(@Param("articleId") Long articleId);

    int deleteByTagId(@Param("tagId") Integer tagId);

    int countByTagId(@Param("tagId") Integer tagId);
}
