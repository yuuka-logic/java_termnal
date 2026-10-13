package com.example.personal_blog.mapper;

import com.example.personal_blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类表数据访问接口。
 */
@Mapper
public interface CategoryMapper {

    List<Category> selectPage(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("parentId") Integer parentId,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("status") Integer status,
                          @Param("parentId") Integer parentId);

    /** 查询全部启用分类，用于组装树形结构 */
    List<Category> selectAllForTree(@Param("status") Integer status);

    Category selectById(@Param("id") Integer id);

    Category selectBySlug(@Param("slug") String slug);

    int countBySlug(@Param("slug") String slug, @Param("excludeId") Integer excludeId);

    /** 是否存在子分类（删除前校验） */
    int countChildren(@Param("parentId") Integer parentId);

    /** 该分类下的文章数（删除前校验） */
    int countArticles(@Param("categoryId") Integer categoryId);

    int insert(Category category);

    int updateById(Category category);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int softDeleteById(@Param("id") Integer id);
}
