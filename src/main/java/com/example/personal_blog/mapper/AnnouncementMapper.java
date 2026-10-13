package com.example.personal_blog.mapper;

import com.example.personal_blog.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知公告表数据访问接口。
 */
@Mapper
public interface AnnouncementMapper {

    List<Announcement> selectPage(@Param("keyword") String keyword,
                                  @Param("type") Integer type,
                                  @Param("status") Integer status,
                                  @Param("priority") Integer priority,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("type") Integer type,
                          @Param("status") Integer status,
                          @Param("priority") Integer priority);

    Announcement selectById(@Param("id") Long id);

    /** 当前有效的已发布公告：未撤回、未过期，按置顶与优先级排序 */
    List<Announcement> selectActive(@Param("limit") int limit);

    int insert(Announcement announcement);

    int updateById(Announcement announcement);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateTop(@Param("id") Long id, @Param("isTop") Integer isTop);

    int increaseViewCount(@Param("id") Long id);

    int deleteById(@Param("id") Long id);

    int countByPublisher(@Param("publisherId") Long publisherId);
}
