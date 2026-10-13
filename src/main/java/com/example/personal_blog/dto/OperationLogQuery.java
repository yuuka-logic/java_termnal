package com.example.personal_blog.dto;

import com.example.personal_blog.common.PageQuery;

import java.time.LocalDateTime;

/**
 * 操作日志查询条件。
 */
public class OperationLogQuery extends PageQuery {

    /** 操作人用户名，模糊匹配 */
    private String username;

    /** 模块，精确匹配 */
    private String module;

    /** 操作类型，精确匹配 */
    private String operation;

    /** 结果：0-失败 1-成功 */
    private Integer status;

    /** 时间范围起（含） */
    private LocalDateTime startTime;

    /** 时间范围止（含） */
    private LocalDateTime endTime;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
