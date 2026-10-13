package com.example.personal_blog.service;

import com.example.personal_blog.common.BusinessException;
import com.example.personal_blog.common.Result;
import com.example.personal_blog.entity.User;
import com.example.personal_blog.mapper.UserMapper;
import com.example.personal_blog.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 用户管理业务层。
 * 负责参数校验、唯一性校验、密码加密与逻辑删除，Controller 只做协议转换。
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    /** 构造器注入：依赖不可变，也便于单元测试 */
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 分页查询用户列表。
     *
     * @param keyword 模糊匹配 用户名/昵称/邮箱，可为空
     * @param status  状态过滤 0-禁用 1-正常，可为空
     * @param page    页码，从 1 开始
     * @param size    每页条数，最大 100
     */
    public Result<List<User>> page(String keyword, Integer status, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        long total = userMapper.countByCondition(kw, status);
        List<User> list = total == 0
                ? Collections.<User>emptyList()
                : userMapper.selectPage(kw, status, (page - 1) * size, size);
        return Result.page(list, total, page, size);
    }

    /** 按主键查询，不存在返回 null */
    public User getById(Long id) {
        return id == null ? null : userMapper.selectById(id);
    }

    /** 按用户名查询，不存在返回 null */
    public User getByUsername(String username) {
        if (!notBlank(username)) {
            return null;
        }
        return userMapper.selectByUsername(username.trim());
    }

    /**
     * 新增用户。用户名/邮箱重复时抛 BusinessException。
     */
    @Transactional(rollbackFor = Exception.class)
    public int create(User user) {
        checkRequired(user);
        String username = user.getUsername().trim();
        if (userMapper.countByUsername(username, null) > 0) {
            throw new BusinessException(409, "用户名已存在：" + username);
        }
        if (notBlank(user.getEmail()) && userMapper.countByEmail(user.getEmail().trim(), null) > 0) {
            throw new BusinessException(409, "邮箱已被注册：" + user.getEmail());
        }

        user.setUsername(username);
        user.setEmail(notBlank(user.getEmail()) ? user.getEmail().trim() : null);
        // 密码统一加密后入库，绝不存明文
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        applyDefaults(user);
        return userMapper.insert(user);
    }

    /**
     * 按主键更新。只更新传入的非空字段；传了 password 则加密后更新。
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(Long id, User user) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(404, "用户不存在：id=" + id);
        }
        user.setId(id);

        if (notBlank(user.getUsername())) {
            String username = user.getUsername().trim();
            if (userMapper.countByUsername(username, id) > 0) {
                throw new BusinessException(409, "用户名已存在：" + username);
            }
            user.setUsername(username);
        } else {
            user.setUsername(null);   // 空字符串视为不修改
        }

        if (notBlank(user.getEmail())) {
            String email = user.getEmail().trim();
            if (userMapper.countByEmail(email, id) > 0) {
                throw new BusinessException(409, "邮箱已被注册：" + email);
            }
            user.setEmail(email);
        }

        if (notBlank(user.getPassword())) {
            user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        } else {
            user.setPassword(null);   // 不修改密码
        }

        return userMapper.updateById(user);
    }

    /** 逻辑删除 */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        if (id == null || userMapper.selectById(id) == null) {
            throw new BusinessException(404, "用户不存在：id=" + id);
        }
        return userMapper.softDeleteById(id);
    }

    /** 启用/禁用 */
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status 只能为 0(禁用) 或 1(正常)");
        }
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(404, "用户不存在：id=" + id);
        }
        return userMapper.updateStatus(id, status);
    }

    /** 重置密码 */
    @Transactional(rollbackFor = Exception.class)
    public int resetPassword(Long id, String rawPassword) {
        if (!notBlank(rawPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        if (rawPassword.length() < 6) {
            throw new BusinessException("密码长度不能少于 6 位");
        }
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(404, "用户不存在：id=" + id);
        }
        return userMapper.updatePassword(id, PasswordUtil.encrypt(rawPassword));
    }

    /** 记录登录成功信息 */
    @Transactional(rollbackFor = Exception.class)
    public int recordLogin(Long id, String ip) {
        return userMapper.updateLoginInfo(id, ip);
    }

    // ------------------------------------------------------------------ 私有

    private void checkRequired(User user) {
        if (user == null) {
            throw new BusinessException("用户信息不能为空");
        }
        if (!notBlank(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!notBlank(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (user.getUsername().trim().length() > 50) {
            throw new BusinessException("用户名长度不能超过 50");
        }
    }

    /** 填充默认值，避免前端不传导致列为 null */
    private void applyDefaults(User user) {
        if (user.getNickname() == null) {
            user.setNickname("");
        }
        if (user.getGender() == null) {
            user.setGender(0);
        }
        if (user.getRole() == null) {
            user.setRole(2);        // 默认注册用户
        }
        if (user.getStatus() == null) {
            user.setStatus(1);      // 默认正常
        }
        user.setIsDeleted(0);
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
