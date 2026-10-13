package com.example.personal_blog.mapper;

import com.example.personal_blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签表数据访问接口。
 */
@Mapper
public interface TagMapper {

    List<Tag> selectPage(@Param("keyword") String keyword,
                         @Param("offset") int offset,
                         @Param("limit") int limit);

    long countByCondition(@Param("keyword") String keyword);

    /** 全部标签，按文章数倒序，用于标签云 */
    List<Tag> selectAllOrderByCount();

    Tag selectById(@Param("id") Integer id);

    Tag selectByName(@Param("name") String name);

    int countByName(@Param("name") String name, @Param("excludeId") Integer excludeId);

    int countBySlug(@Param("slug") String slug, @Param("excludeId") Integer excludeId);

    /** 该标签关联的文章数（删除前校验） */
    int countArticles(@Param("tagId") Integer tagId);

    int insert(Tag tag);

    int updateById(Tag tag);

    /** 重算标签的文章数（增删文章后同步冗余统计字段） */
    int refreshArticleCount(@Param("tagId") Integer tagId);

    int refreshAllArticleCounts();

    int softDeleteById(@Param("id") Integer id);
}
