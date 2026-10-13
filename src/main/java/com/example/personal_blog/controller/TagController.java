package com.example.personal_blog.controller;

import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.KeywordQuery;
import com.example.personal_blog.entity.Tag;
import com.example.personal_blog.service.TagService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签管理接口。
 * <p>
 * | 方法   | 路径                          | 说明                     |
 * |--------|-------------------------------|--------------------------|
 * | GET    | /api/tag                      | 分页列表                  |
 * | GET    | /api/tag/all                  | 全部标签（标签云，按文章数倒序）|
 * | GET    | /api/tag/{id}                 | 查询单个                  |
 * | POST   | /api/tag                      | 新增                      |
 * | PUT    | /api/tag/{id}                 | 修改                      |
 * | DELETE | /api/tag/{id}                 | 逻辑删除（有关联文章则拒绝）|
 * | POST   | /api/tag/refresh-count        | 全量重算文章数（数据修复）  |
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Result<List<Tag>> list(KeywordQuery query) {
        return tagService.page(query);
    }

    /** 标签云：全部标签，按文章数倒序 */
    @GetMapping("/all")
    public Result<List<Tag>> all() {
        return Result.ok(tagService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Tag> detail(@PathVariable Integer id) {
        Tag t = tagService.getById(id);
        return t == null ? Result.<Tag>fail(404, "标签不存在：id=" + id) : Result.ok(t);
    }

    @PostMapping
    public Result<Tag> create(@RequestBody Tag tag) {
        tagService.create(tag);
        return Result.ok("新增成功", tagService.getById(tag.getId()));
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@PathVariable Integer id, @RequestBody Tag tag) {
        tagService.update(id, tag);
        return Result.ok("修改成功", tagService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        tagService.delete(id);
        return Result.ok("删除成功", null);
    }

    /** 全量重算 tag.article_count，用于修复冗余统计字段 */
    @PostMapping("/refresh-count")
    public Result<Integer> refreshCount() {
        int n = tagService.refreshAllArticleCounts();
        return Result.ok("已重算 " + n + " 个标签的文章数", n);
    }
}
