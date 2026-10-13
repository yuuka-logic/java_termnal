package com.example.personal_blog.controller;

import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.ArticleForm;
import com.example.personal_blog.dto.ArticleQuery;
import com.example.personal_blog.dto.ArticleVO;
import com.example.personal_blog.service.ArticleService;
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
 * 文章管理接口。
 * <p>
 * | 方法   | 路径                              | 说明                       |
 * |--------|-----------------------------------|----------------------------|
 * | GET    | /api/article                      | 分页 + 多维过滤             |
 * | GET    | /api/article/{id}                 | 详情（含正文与标签）        |
 * | POST   | /api/article                      | 新增（可带 tagIds）         |
 * | PUT    | /api/article/{id}                 | 修改（tagIds 传 null 不动标签）|
 * | DELETE | /api/article/{id}                 | 删除（级联清理标签关联）    |
 * | PATCH  | /api/article/{id}/status          | 改状态，发布时自动补发布时间 |
 * | PATCH  | /api/article/{id}/top             | 置顶/取消置顶               |
 * | PATCH  | /api/article/{id}/like            | 点赞（like=false 取消）     |
 * | POST   | /api/article/{id}/view            | 浏览量 +1                   |
 * | PUT    | /api/article/{id}/tags            | 单独设置标签                |
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 分页列表。
     * 例：GET /api/article?page=1&size=10&keyword=Java&categoryId=1&status=1&tagId=2&authorId=3
     */
    @GetMapping
    public Result<List<ArticleVO>> list(ArticleQuery query) {
        return articleService.page(query);
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        ArticleVO vo = articleService.getDetail(id);
        return vo == null ? Result.<ArticleVO>fail(404, "文章不存在：id=" + id) : Result.ok(vo);
    }

    @PostMapping
    public Result<ArticleVO> create(@RequestBody ArticleForm form) {
        articleService.create(form);
        return Result.ok("新增成功", articleService.getDetail(form.getId()));
    }

    @PutMapping("/{id}")
    public Result<ArticleVO> update(@PathVariable Long id, @RequestBody ArticleForm form) {
        articleService.update(id, form);
        return Result.ok("修改成功", articleService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok("删除成功", null);
    }

    /** 改状态：0-草稿 1-已发布 2-已下架 */
    @PatchMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        articleService.changeStatus(id, status);
        return Result.ok("状态已更新", null);
    }

    /** 置顶/取消置顶 */
    @PatchMapping("/{id}/top")
    public Result<Void> changeTop(@PathVariable Long id, @RequestParam Integer isTop) {
        articleService.changeTop(id, isTop);
        return Result.ok(isTop == 1 ? "已置顶" : "已取消置顶", null);
    }

    /** 点赞；like=false 表示取消点赞 */
    @PatchMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id,
                             @RequestParam(defaultValue = "true") boolean like) {
        articleService.changeLikeCount(id, like);
        return Result.ok(like ? "点赞成功" : "已取消点赞", null);
    }

    /** 浏览量 +1 */
    @PostMapping("/{id}/view")
    public Result<Void> view(@PathVariable Long id) {
        articleService.increaseViewCount(id);
        return Result.ok("已记录浏览", null);
    }

    /** 单独设置标签，body 形如 [1,2,3] */
    @PutMapping("/{id}/tags")
    public Result<Void> setTags(@PathVariable Long id, @RequestBody List<Integer> tagIds) {
        articleService.setTags(id, tagIds);
        return Result.ok("标签已更新", null);
    }
}
