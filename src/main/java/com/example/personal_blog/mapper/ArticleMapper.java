package com.example.personal_blog.mapper;

import com.example.personal_blog.entity.Article;
import com.example.personal_blog.dto.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章表数据访问接口。
 */
@Mapper
public interface ArticleMapper {

    /** 分页查询（不含正文 content，列表页不需要，减少传输量） */
    List<ArticleVO> selectPage(@Param("keyword") String keyword,
                               @Param("categoryId") Integer categoryId,
                               @Param("authorId") Long authorId,
                               @Param("status") Integer status,
                               @Param("tagId") Integer tagId,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("categoryId") Integer categoryId,
                          @Param("authorId") Long authorId,
                          @Param("status") Integer status,
                          @Param("tagId") Integer tagId);

    /** 按主键查询详情（含正文 content 与作者、分类名称） */
    ArticleVO selectDetailById(@Param("id") Long id);

    Article selectById(@Param("id") Long id);

    int insert(Article article);

    int updateById(Article article);

    /** 浏览量 +1（并发安全，用列自增而非先查后写） */
    int increaseViewCount(@Param("id") Long id);

    /** 点赞数 +1；传 -1 即取消点赞。用 GREATEST 防止出现负数 */
    int changeLikeCount(@Param("id") Long id, @Param("delta") int delta);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateTop(@Param("id") Long id, @Param("isTop") Integer isTop);

    /** 物理删除。article_tag 上的外键是 ON DELETE CASCADE，关联会自动清理 */
    int deleteById(@Param("id") Long id);

    /** 按作者统计文章数（删除用户前校验用） */
    int countByAuthor(@Param("authorId") Long authorId);
}
