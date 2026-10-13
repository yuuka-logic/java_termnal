package com.example.personal_blog.mapper;

import com.example.personal_blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表数据访问接口。
 * SQL 写在 resources/mapper/UserMapper.xml 中。
 */
@Mapper
public interface UserMapper {

    /** 分页查询用户列表（不含已逻辑删除） */
    List<User> selectPage(@Param("keyword") String keyword,
                          @Param("status") Integer status,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    /** 统计符合条件的用户总数，配合分页 */
    long countByCondition(@Param("keyword") String keyword,
                          @Param("status") Integer status);

    /** 按主键查询 */
    User selectById(@Param("id") Long id);

    /** 按用户名查询（用于登录与唯一性校验） */
    User selectByUsername(@Param("username") String username);

    /** 统计同名用户数量，排除指定 id（新增时传 null） */
    int countByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    /** 统计同邮箱用户数量，排除指定 id */
    int countByEmail(@Param("email") String email, @Param("excludeId") Long excludeId);

    /** 新增用户，成功后主键回填到实体 id */
    int insert(User user);

    /** 按主键动态更新非空字段 */
    int updateById(User user);

    /** 更新状态（启用/禁用） */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 更新密码 */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 记录登录信息 */
    int updateLoginInfo(@Param("id") Long id,
                        @Param("ip") String ip);

    /** 逻辑删除 */
    int softDeleteById(@Param("id") Long id);
}
