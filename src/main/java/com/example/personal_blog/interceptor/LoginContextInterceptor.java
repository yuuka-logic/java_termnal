package com.example.personal_blog.interceptor;

import com.example.personal_blog.common.RepeatableReadRequestWrapper;
import com.example.personal_blog.common.UserContext;
import com.example.personal_blog.entity.User;
import com.example.personal_blog.service.SessionStore;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录上下文拦截器。
 * <p>
 * 权限约定（实训项目）：
 * <ul>
 *   <li>GET 查询接口开放，前台浏览文章/公告不需要登录；</li>
 *   <li>POST/PUT/PATCH/DELETE 写接口需要登录，请求头带 token；</li>
 *   <li>/api/auth/** 全部放行（登录、注册本身不能要求登录）。</li>
 * </ul>
 * 同时负责把请求体包装成可重复读取，供操作日志拦截器使用。
 */
@Component
public class LoginContextInterceptor implements HandlerInterceptor {

    public static final String TOKEN_HEADER = "token";

    private final SessionStore sessionStore;

    public LoginContextInterceptor(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // 1) 识别登录态
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.isEmpty()) {
            token = request.getParameter(TOKEN_HEADER);
        }
        if (token != null && !token.isEmpty()) {
            UserContext.CurrentUser user = sessionStore.get(token);
            if (user != null) {
                UserContext.set(user);
            }
        }

        // 2) 写接口要求登录
        if (needLogin(request) && !UserContext.isLogin()) {
            writeUnauthorized(response);
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理 ThreadLocal，否则 Tomcat 线程复用会导致用户串号
        UserContext.clear();
    }

    private boolean needLogin(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth")) {
            return false;
        }
        String method = request.getMethod();
        return !("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method));
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);   // 业务码放在 body 里，与前端约定一致
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":401,\"message\":\"未登录或登录已过期，请先调用 /api/auth/login 获取 token\","
                        + "\"data\":null,\"total\":null,\"page\":null,\"size\":null,\"pages\":null}");
        response.getWriter().flush();
    }

    /** 供过滤器调用：把请求包装成可重复读取 */
    public static RepeatableReadRequestWrapper wrap(HttpServletRequest request) throws IOException {
        return new RepeatableReadRequestWrapper(request);
    }
}
