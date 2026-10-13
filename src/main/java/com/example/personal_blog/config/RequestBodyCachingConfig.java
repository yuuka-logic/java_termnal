package com.example.personal_blog.config;

import com.example.personal_blog.common.RepeatableReadRequestWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求体缓存过滤器。
 * <p>
 * Servlet 请求体只能读一次：操作日志拦截器要读请求体做记录，
 * 若不先缓存，后面的 @RequestBody 就会拿到空流导致反序列化失败。
 * 这里在过滤器链最前面把请求包装成可重复读取，两个拦截器共用同一份缓存。
 * <p>
 * 只包装写操作与非表单请求，GET 与文件上传无需缓存，避免无谓的内存拷贝。
 */
@Configuration
public class RequestBodyCachingConfig {

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> requestBodyCachingFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {
                if (shouldWrap(request)) {
                    chain.doFilter(new RepeatableReadRequestWrapper(request), response);
                } else {
                    chain.doFilter(request, response);
                }
            }

            private boolean shouldWrap(HttpServletRequest request) {
                String method = request.getMethod();
                if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
                    return false;
                }
                // multipart 上传不缓存，避免把大文件读进内存
                String contentType = request.getContentType();
                return contentType == null || !contentType.toLowerCase().startsWith("multipart/");
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>(filter);
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        bean.setName("requestBodyCachingFilter");
        return bean;
    }
}
