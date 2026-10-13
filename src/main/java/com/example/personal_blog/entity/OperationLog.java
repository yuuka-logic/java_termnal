package com.example.personal_blog.entity;

import java.time.LocalDateTime;

/**
 * 操作日志实体，对应 personal_blog.operation_log 表。
 * 由 OperationLogInterceptor 自动写入，一般不通过接口手工新增。
 */
public class OperationLog {

    /** 日志ID，主键 */
    private Long id;

    /** 操作人ID，NULL 表示未登录 */
    private Long userId;

    /** 操作人用户名（冗余，用户被删除后仍可追溯） */
    private String username;

    /** 所属模块，如 文章管理、用户管理 */
    private String module;

    /** 操作类型，如 新增、修改、删除、查询、登录 */
    private String operation;

    /** 操作描述 */
    private String description;

    /** HTTP 请求方式 */
    private String requestMethod;

    /** 请求 URL */
    private String requestUrl;

    /** 请求参数（JSON 字符串） */
    private String requestParams;

    /** 操作IP，兼容 IPv6 */
    private String ip;

    /** 浏览器 User-Agent */
    private String userAgent;

    /** 耗时，单位毫秒 */
    private Integer duration;

    /** 操作结果：0-失败 1-成功 */
    private Integer status;

    /** 失败时的异常信息 */
    private String errorMsg;

    /** 操作时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }

    public String getRequestParams() { return requestParams; }
    public void setRequestParams(String requestParams) { this.requestParams = requestParams; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "OperationLog{id=" + id + ", username='" + username + "', module='" + module
                + "', operation='" + operation + "', status=" + status + '}';
    }
}
