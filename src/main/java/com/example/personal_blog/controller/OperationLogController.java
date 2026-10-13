package com.example.personal_blog.controller;

import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.OperationLogQuery;
import com.example.personal_blog.entity.OperationLog;
import com.example.personal_blog.service.OperationLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作日志接口（只读 + 清理）。
 * 日志由拦截器自动采集，不提供手工新增接口。
 * <p>
 * | 方法   | 路径                              | 说明                     |
 * |--------|-----------------------------------|--------------------------|
 * | GET    | /api/operation-log                | 分页 + 多条件过滤         |
 * | GET    | /api/operation-log/{id}           | 日志详情                 |
 * | GET    | /api/operation-log/stats/module   | 按模块统计               |
 * | DELETE | /api/operation-log/clean          | 清理指定时间之前的日志    |
 * | DELETE | /api/operation-log/all            | 清空全部日志             |
 */
@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 分页查询。
     * 例：GET /api/operation-log?page=1&size=20&username=admin&module=文章管理&status=1
     *     &startTime=2026-10-13T00:00:00&endTime=2026-10-13T23:59:59
     */
    @GetMapping
    public Result<List<OperationLog>> list(OperationLogQuery query) {
        return operationLogService.page(query);
    }

    @GetMapping("/{id}")
    public Result<OperationLog> detail(@PathVariable Long id) {
        OperationLog log = operationLogService.getById(id);
        return log == null ? Result.<OperationLog>fail(404, "日志不存在：id=" + id) : Result.ok(log);
    }

    /** 按模块统计操作次数，用于概览面板 */
    @GetMapping("/stats/module")
    public Result<List<Map<String, Object>>> statsByModule(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime) {
        return Result.ok(operationLogService.statsByModule(startTime));
    }

    /** 清理指定时间之前的日志 */
    @DeleteMapping("/clean")
    public Result<Integer> clean(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        int n = operationLogService.cleanBefore(before);
        return Result.ok("已清理 " + n + " 条日志", n);
    }

    /** 清空全部日志 */
    @DeleteMapping("/all")
    public Result<Integer> cleanAll() {
        int n = operationLogService.cleanAll();
        return Result.ok("已清空 " + n + " 条日志", n);
    }
}
