package com.example.personal_blog.interceptor;

import com.example.personal_blog.common.GlobalExceptionHandler;
import com.example.personal_blog.common.RepeatableReadRequestWrapper;
import com.example.personal_blog.common.UserContext;
import com.example.personal_blog.entity.OperationLog;
import com.example.personal_blog.service.OperationLogService;
import com.example.personal_blog.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志拦截器：自动采集写操作日志，业务代码零侵入。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>只记录 POST/PUT/PATCH/DELETE 等写操作，GET 查询量大且无审计价值，默认不记；</li>
 *   <li>用 RepeatableReadRequestWrapper 缓存请求体，避免读流导致 @RequestBody 拿到空数据；</li>
 *   <li>请求结束才写库，能同时记录耗时 duration 与成功/失败状态；</li>
 *   <li>跳过日志接口自身，防止"查日志"这个动作又产生日志。</li>
 * </ul>
 */
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    /** 请求属性名：记录开始时间 */
    private static final String ATTR_START = "opLog.start";
    /** 请求属性名：解析出的模块 */
    private static final String ATTR_MODULE = "opLog.module";
    /** 请求属性名：解析出的操作类型 */
    private static final String ATTR_OPERATION = "opLog.operation";

    /** 请求体最大记录长度，超出截断，避免日志表被超大正文撑爆 */
    private static final int MAX_PARAMS_LENGTH = 2000;
    private static final int MAX_ERROR_LENGTH = 500;

    /** 路径段 -> 模块名 */
    private static final Map<String, String> MODULE_MAP = new LinkedHashMap<>();
    /** HTTP 方法 -> 操作类型 */
    private static final Map<String, String> OPERATION_MAP = new LinkedHashMap<>();

    static {
        MODULE_MAP.put("user", "用户管理");
        MODULE_MAP.put("category", "分类管理");
        MODULE_MAP.put("tag", "标签管理");
        MODULE_MAP.put("article", "文章管理");
        MODULE_MAP.put("announcement", "通知公告");
        MODULE_MAP.put("operation-log", "操作日志");
        MODULE_MAP.put("auth", "认证");

        OPERATION_MAP.put("POST", "新增");
        OPERATION_MAP.put("PUT", "修改");
        OPERATION_MAP.put("PATCH", "修改");
        OPERATION_MAP.put("DELETE", "删除");
        OPERATION_MAP.put("GET", "查询");
    }

    private final OperationLogService operationLogService;

    public OperationLogInterceptor(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        request.setAttribute(ATTR_MODULE, resolveModule(request.getRequestURI()));
        request.setAttribute(ATTR_OPERATION, OPERATION_MAP.getOrDefault(request.getMethod(), request.getMethod()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            if (shouldSkip(request)) {
                return;
            }
            OperationLog entity = new OperationLog();
            entity.setUserId(UserContext.getUserId());
            entity.setUsername(UserContext.getUsername());
            entity.setModule((String) request.getAttribute(ATTR_MODULE));
            entity.setOperation((String) request.getAttribute(ATTR_OPERATION));
            entity.setDescription(buildDescription(request));
            entity.setRequestMethod(request.getMethod());
            entity.setRequestUrl(truncate(request.getRequestURI()
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString()), 255));
            entity.setRequestParams(extractParams(request));
            entity.setIp(clientIp(request));
            entity.setUserAgent(truncate(request.getHeader("User-Agent"), 500));

            Object start = request.getAttribute(ATTR_START);
            if (start instanceof Long) {
                entity.setDuration((int) (System.currentTimeMillis() - (Long) start));
            }

            // 本项目业务码放在响应体里、HTTP 状态恒为 200，
            // 所以失败标记由 GlobalExceptionHandler 写进 request attribute，不能只看 response.getStatus()
            Object bizError = request.getAttribute(GlobalExceptionHandler.ATTR_BUSINESS_ERROR);
            boolean failed = ex != null || bizError != null || response.getStatus() >= 400;
            entity.setStatus(failed ? 0 : 1);
            if (ex != null) {
                entity.setErrorMsg(truncate(ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                        MAX_ERROR_LENGTH));
            } else if (bizError != null) {
                entity.setErrorMsg(truncate(String.valueOf(bizError), MAX_ERROR_LENGTH));
            } else if (response.getStatus() >= 400) {
                entity.setErrorMsg("HTTP " + response.getStatus());
            }

            operationLogService.save(entity);
        } catch (Exception e) {
            // 日志失败不影响业务，仅记录到控制台
            org.slf4j.LoggerFactory.getLogger(OperationLogInterceptor.class)
                    .warn("采集操作日志异常: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------ 私有

    /** 只记写操作；日志接口自身不记，避免自我循环 */
    private boolean shouldSkip(HttpServletRequest request) {
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri.startsWith("/api/operation-log");
    }

    private String resolveModule(String uri) {
        if (uri == null) {
            return "未知";
        }
        for (Map.Entry<String, String> e : MODULE_MAP.entrySet()) {
            if (uri.contains("/" + e.getKey())) {
                return e.getValue();
            }
        }
        return "其他";
    }

    private String buildDescription(HttpServletRequest request) {
        String module = (String) request.getAttribute(ATTR_MODULE);
        String operation = (String) request.getAttribute(ATTR_OPERATION);
        return module + "-" + operation + " " + request.getMethod() + " " + request.getRequestURI();
    }

    /** 请求体：表单直接取 parameter，JSON 从缓存流里取 */
    private String extractParams(HttpServletRequest request) {
        if (request instanceof RepeatableReadRequestWrapper) {
            String body = ((RepeatableReadRequestWrapper) request).getBodyAsString();
            if (StringUtils.isNotBlank(body)) {
                return truncate(body, MAX_PARAMS_LENGTH);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(String.join(",", e.getValue()));
        }
        return sb.length() == 0 ? null : truncate(sb.toString(), MAX_PARAMS_LENGTH);
    }

    /** 取真实客户端 IP，兼容反向代理 */
    private String clientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String h : headers) {
            String v = request.getHeader(h);
            if (StringUtils.isNotBlank(v) && !"unknown".equalsIgnoreCase(v)) {
                int comma = v.indexOf(',');
                return truncate(comma > 0 ? v.substring(0, comma).trim() : v.trim(), 45);
            }
        }
        return truncate(request.getRemoteAddr(), 45);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }
}
