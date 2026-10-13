package com.example.personal_blog.service;

import com.example.personal_blog.common.BusinessException;
import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.OperationLogQuery;
import com.example.personal_blog.entity.OperationLog;
import com.example.personal_blog.mapper.OperationLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 操作日志业务层。
 * 日志由 OperationLogInterceptor 自动采集写入，正常不手工新增；
 * 这里提供查询、详情与清理能力。
 */
@Service
public class OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogService.class);

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public Result<List<OperationLog>> page(OperationLogQuery query) {
        long total = operationLogMapper.countByCondition(query.getUsername(), query.getModule(),
                query.getOperation(), query.getStatus(), query.getStartTime(), query.getEndTime());
        List<OperationLog> list = total == 0
                ? new ArrayList<OperationLog>()
                : operationLogMapper.selectPage(query.getUsername(), query.getModule(),
                        query.getOperation(), query.getStatus(), query.getStartTime(),
                        query.getEndTime(), query.offset(), query.safeSize());
        return query.toResult(list, total);
    }

    public OperationLog getById(Long id) {
        return id == null ? null : operationLogMapper.selectById(id);
    }

    /**
     * 保存一条日志。
     * 独立事务 + 吞异常：日志记录失败绝不能影响正常业务响应。
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class)
    public void save(OperationLog entity) {
        try {
            operationLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }
    }

    /** 清理指定时间之前的日志 */
    @Transactional(rollbackFor = Exception.class)
    public int cleanBefore(LocalDateTime time) {
        if (time == null) {
            throw new BusinessException("清理时间不能为空");
        }
        return operationLogMapper.deleteBefore(time);
    }

    /** 清空全部日志 */
    @Transactional(rollbackFor = Exception.class)
    public int cleanAll() {
        return operationLogMapper.truncateAll();
    }

    /** 按模块统计 */
    public List<Map<String, Object>> statsByModule(LocalDateTime startTime) {
        return operationLogMapper.countGroupByModule(startTime);
    }
}
