package com.example.personal_blog.controller;

import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.KeywordQuery;
import com.example.personal_blog.entity.Category;
import com.example.personal_blog.service.CategoryService;
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
 * 分类管理接口。
 * <p>
 * | 方法   | 路径                          | 说明                     |
 * |--------|-------------------------------|--------------------------|
 * | GET    | /api/category                 | 分页列表（平铺）          |
 * | GET    | /api/category/tree            | 树形结构                  |
 * | GET    | /api/category/{id}            | 查询单个                  |
 * | GET    | /api/category/slug/{slug}     | 按别名查询                |
 * | POST   | /api/category                 | 新增                      |
 * | PUT    | /api/category/{id}            | 修改                      |
 * | DELETE | /api/category/{id}            | 逻辑删除（有子类/文章则拒绝）|
 * | PATCH  | /api/category/{id}/status     | 启用/禁用                 |
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** 分页列表。例：GET /api/category?page=1&size=10&keyword=java&status=1 */
    @GetMapping
    public Result<List<Category>> list(KeywordQuery query,
                                      @RequestParam(required = false) Integer parentId) {
        return categoryService.page(query, parentId);
    }

    /** 树形结构。例：GET /api/category/tree 或 /api/category/tree?status=1 */
    @GetMapping("/tree")
    public Result<List<Category>> tree(@RequestParam(required = false) Integer status) {
        return Result.ok(categoryService.tree(status));
    }

    @GetMapping("/{id}")
    public Result<Category> detail(@PathVariable Integer id) {
        Category c = categoryService.getById(id);
        return c == null ? Result.<Category>fail(404, "分类不存在：id=" + id) : Result.ok(c);
    }

    /** 按 slug 查询，方便前台用 SEO 友好 URL */
    @GetMapping("/slug/{slug}")
    public Result<Category> detailBySlug(@PathVariable String slug) {
        Category c = categoryService.getBySlug(slug);
        return c == null ? Result.<Category>fail(404, "分类不存在：slug=" + slug) : Result.ok(c);
    }

    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        categoryService.create(category);
        return Result.ok("新增成功", categoryService.getById(category.getId()));
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Integer id, @RequestBody Category category) {
        categoryService.update(id, category);
        return Result.ok("修改成功", categoryService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return Result.ok("删除成功", null);
    }

    @PatchMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Integer id, @RequestParam Integer status) {
        categoryService.changeStatus(id, status);
        return Result.ok(status == 1 ? "已启用" : "已禁用", null);
    }
}
