package com.example.personal_blog.mapper;

import com.example.personal_blog.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志表数据访问接口。
 */
@Mapper
public interface OperationLogMapper {

    List<OperationLog> selectPage(@Param("username") String username,
                                  @Param("module") String module,
                                  @Param("operation") String operation,
                                  @Param("status") Integer status,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    long countByCondition(@Param("username") String username,
                          @Param("module") String module,
                          @Param("operation") String operation,
                          @Param("status") Integer status,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    OperationLog selectById(@Param("id") Long id);

    int insert(OperationLog log);

    /** 删除指定时间之前的日志，用于定期清理 */
    int deleteBefore(@Param("time") LocalDateTime time);

    /** 清空全部日志 */
    int truncateAll();

    /** 按模块统计，用于概览面板 */
    List<java.util.Map<String, Object>> countGroupByModule(@Param("startTime") LocalDateTime startTime);
}
