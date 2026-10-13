package com.example.personal_blog.controller;

import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.LoginRequest;
import com.example.personal_blog.entity.User;
import com.example.personal_blog.interceptor.LoginContextInterceptor;
import com.example.personal_blog.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证接口。
 * <p>
 * | 方法 | 路径                | 说明                       |
 * |------|---------------------|----------------------------|
 * | POST | /api/auth/login     | 登录，返回 token            |
 * | POST | /api/auth/register  | 注册（默认角色：注册用户）  |
 * | POST | /api/auth/logout    | 退出登录，作废 token        |
 * | GET  | /api/auth/me        | 当前登录用户信息            |
 * <p>
 * 除本控制器外，所有写接口都要求在请求头带 {@code token}。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        Map<String, Object> data = authService.login(req, clientIp(request));
        return Result.ok("登录成功", data);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody LoginRequest req, HttpServletRequest request) {
        User user = authService.register(req, clientIp(request));
        return Result.ok("注册成功", user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = LoginContextInterceptor.TOKEN_HEADER, required = false) String token) {
        authService.logout(token);
        return Result.ok("已退出登录", null);
    }

    @GetMapping("/me")
    public Result<User> me() {
        return Result.ok(authService.currentUser());
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty() && !"unknown".equalsIgnoreCase(xff)) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        return request.getRemoteAddr();
    }
}
