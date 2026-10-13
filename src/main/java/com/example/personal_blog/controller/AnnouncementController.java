package com.example.personal_blog.controller;

import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.AnnouncementQuery;
import com.example.personal_blog.entity.Announcement;
import com.example.personal_blog.service.AnnouncementService;
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
 * 通知公告接口。
 * <p>
 * | 方法   | 路径                          | 说明                             |
 * |--------|-------------------------------|----------------------------------|
 * | GET    | /api/announcement             | 分页 + 关键字/类型/状态/优先级过滤 |
 * | GET    | /api/announcement/active      | 当前有效公告（前台展示用）        |
 * | GET    | /api/announcement/{id}        | 详情                             |
 * | POST   | /api/announcement             | 新增                             |
 * | PUT    | /api/announcement/{id}        | 修改                             |
 * | DELETE | /api/announcement/{id}        | 删除                             |
 * | PATCH  | /api/announcement/{id}/status | 发布/撤回/转草稿                  |
 * | PATCH  | /api/announcement/{id}/top    | 置顶/取消置顶                     |
 * | POST   | /api/announcement/{id}/view   | 查看次数 +1                       |
 */
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public Result<List<Announcement>> list(AnnouncementQuery query) {
        return announcementService.page(query);
    }

    /** 前台公告栏：只返回已发布且未过期的公告 */
    @GetMapping("/active")
    public Result<List<Announcement>> active(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(announcementService.listActive(limit));
    }

    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        Announcement a = announcementService.getById(id);
        return a == null ? Result.<Announcement>fail(404, "公告不存在：id=" + id) : Result.ok(a);
    }

    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement announcement) {
        announcementService.create(announcement);
        return Result.ok("新增成功", announcementService.getById(announcement.getId()));
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcementService.update(id, announcement);
        return Result.ok("修改成功", announcementService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.ok("删除成功", null);
    }

    /** 状态：0-草稿 1-已发布 2-已撤回 */
    @PatchMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        announcementService.changeStatus(id, status);
        return Result.ok("状态已更新", null);
    }

    @PatchMapping("/{id}/top")
    public Result<Void> changeTop(@PathVariable Long id, @RequestParam Integer isTop) {
        announcementService.changeTop(id, isTop);
        return Result.ok(isTop == 1 ? "已置顶" : "已取消置顶", null);
    }

    @PostMapping("/{id}/view")
    public Result<Void> view(@PathVariable Long id) {
        announcementService.increaseViewCount(id);
        return Result.ok("已记录查看", null);
    }
}
