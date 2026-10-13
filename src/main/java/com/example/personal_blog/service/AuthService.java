package com.example.personal_blog.service;

import com.example.personal_blog.common.BusinessException;
import com.example.personal_blog.common.UserContext;
import com.example.personal_blog.dto.LoginRequest;
import com.example.personal_blog.entity.User;
import com.example.personal_blog.mapper.UserMapper;
import com.example.personal_blog.util.PasswordUtil;
import com.example.personal_blog.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证业务层：登录、注册、退出。
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final SessionStore sessionStore;

    public AuthService(UserMapper userMapper, SessionStore sessionStore) {
        this.userMapper = userMapper;
        this.sessionStore = sessionStore;
    }

    /**
     * 登录。成功返回 token 与用户信息；失败抛业务异常。
     * 用户名不存在与密码错误返回同一句提示，避免被用来枚举已注册用户名。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> login(LoginRequest req, String ip) {
        if (req == null || StringUtils.isBlank(req.getUsername()) || StringUtils.isBlank(req.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        User user = userMapper.selectByUsername(req.getUsername().trim());
        if (user == null || !PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "该账号已被禁用，请联系管理员");
        }

        // 记录登录信息
        userMapper.updateLoginInfo(user.getId(), ip);

        UserContext.CurrentUser current = toCurrentUser(user);
        String token = sessionStore.create(current);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("expiresIn", 7200);
        data.put("user", sanitize(user));
        return data;
    }

    /** 注册：默认角色 2(注册用户)、状态 1(正常) */
    @Transactional(rollbackFor = Exception.class)
    public User register(LoginRequest req, String ip) {
        if (req == null || StringUtils.isBlank(req.getUsername()) || StringUtils.isBlank(req.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        String username = req.getUsername().trim();
        if (username.length() > 50) {
            throw new BusinessException("用户名长度不能超过 50");
        }
        if (req.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于 6 位");
        }
        if (userMapper.countByUsername(username, null) > 0) {
            throw new BusinessException(409, "用户名已存在：" + username);
        }
        String email = StringUtils.trimToNull(req.getEmail(), 100);
        if (email != null && userMapper.countByEmail(email, null) > 0) {
            throw new BusinessException(409, "邮箱已被注册：" + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encrypt(req.getPassword()));
        user.setNickname(StringUtils.isNotBlank(req.getNickname())
                ? req.getNickname().trim() : username);
        user.setEmail(email);
        user.setGender(0);
        user.setRole(2);
        user.setStatus(1);
        user.setIsDeleted(0);
        userMapper.insert(user);

        userMapper.updateLoginInfo(user.getId(), ip);
        return user;
    }

    /** 退出登录，作废 token */
    public boolean logout(String token) {
        return sessionStore.remove(token);
    }

    /** 当前登录用户信息 */
    public User currentUser() {
        Long id = UserContext.getUserId();
        if (id == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        return user;
    }

    // ------------------------------------------------------------------ 私有

    private UserContext.CurrentUser toCurrentUser(User u) {
        return new UserContext.CurrentUser(u.getId(), u.getUsername(), u.getNickname(), u.getRole());
    }

    /** 去掉密码字段，避免登录响应泄露密文 */
    private User sanitize(User u) {
        User copy = new User();
        copy.setId(u.getId());
        copy.setUsername(u.getUsername());
        copy.setNickname(u.getNickname());
        copy.setEmail(u.getEmail());
        copy.setPhone(u.getPhone());
        copy.setAvatar(u.getAvatar());
        copy.setGender(u.getGender());
        copy.setBio(u.getBio());
        copy.setRole(u.getRole());
        copy.setStatus(u.getStatus());
        copy.setLastLoginTime(u.getLastLoginTime());
        copy.setLastLoginIp(u.getLastLoginIp());
        copy.setCreatedAt(u.getCreatedAt());
        copy.setUpdatedAt(u.getUpdatedAt());
        copy.setIsDeleted(u.getIsDeleted());
        return copy;
    }
}
