package com.example.personal_blog.controller;

import com.example.personal_blog.common.Result;
import com.example.personal_blog.entity.User;
import com.example.personal_blog.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理接口。
 * <p>
 * | 方法   | 路径                              | 说明             |
 * |--------|-----------------------------------|------------------|
 * | GET    | /api/user                         | 分页 + 条件查询   |
 * | GET    | /api/user/{id}                    | 查询单个用户      |
 * | POST   | /api/user                         | 新增用户         |
 * | PUT    | /api/user/{id}                    | 修改用户         |
 * | DELETE | /api/user/{id}                    | 逻辑删除         |
 * | PATCH  | /api/user/{id}/status?status=1    | 启用/禁用        |
 * | PATCH  | /api/user/{id}/password           | 重置密码         |
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页查询用户列表。
     * 例：GET /api/user?page=1&size=10&keyword=zhang&status=1
     */
    @GetMapping
    public Result<List<User>> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return userService.page(keyword, status, page, size);
    }

    /** 按 ID 查询单个用户 */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        return user == null ? Result.<User>fail(404, "用户不存在：id=" + id) : Result.ok(user);
    }

    /** 新增用户 */
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        userService.create(user);
        // 返回带自增 id 的实体，注意 password 因 WRITE_ONLY 不会出现在响应里
        return Result.ok("新增成功", user);
    }

    /** 修改用户（只更新传入的字段） */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        userService.update(id, user);
        return Result.ok("修改成功", userService.getById(id));
    }

    /** 逻辑删除用户 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok("删除成功", null);
    }

    /** 启用/禁用：status=1 启用，status=0 禁用 */
    @PatchMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return Result.ok(status == 1 ? "已启用" : "已禁用", null);
    }

    /** 重置密码，JSON 体：{"password":"新密码"} */
    @PatchMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody User body) {
        userService.resetPassword(id, body.getPassword());
        return Result.ok("密码重置成功", null);
    }
}
