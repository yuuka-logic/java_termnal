package com.example.personal_blog.config;

import com.example.personal_blog.interceptor.LoginContextInterceptor;
import com.example.personal_blog.interceptor.OperationLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册拦截器与跨域规则。
 * <p>
 * 拦截器顺序很重要：
 * <ol>
 *   <li>LoginContextInterceptor 先执行，负责识别登录态并写入 UserContext；</li>
 *   <li>OperationLogInterceptor 后执行，才能在写日志时拿到当前用户。</li>
 * </ol>
 * afterCompletion 是倒序执行的，所以清理 UserContext 放在 LoginContextInterceptor 里，
 * 会在日志写完之后才执行，顺序正好。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginContextInterceptor loginContextInterceptor;
    private final OperationLogInterceptor operationLogInterceptor;

    public WebMvcConfig(LoginContextInterceptor loginContextInterceptor,
                        OperationLogInterceptor operationLogInterceptor) {
        this.loginContextInterceptor = loginContextInterceptor;
        this.operationLogInterceptor = operationLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginContextInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/**")
                .order(2);
    }

    /** 前后端分离开发时允许跨域，生产环境应把 allowedOriginPatterns 收窄到具体域名 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
