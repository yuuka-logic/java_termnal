package com.example.personal_blog.service;

import com.example.personal_blog.common.BusinessException;
import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.KeywordQuery;
import com.example.personal_blog.entity.Category;
import com.example.personal_blog.mapper.CategoryMapper;
import com.example.personal_blog.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类管理业务层。
 * 分类是树形结构：parent_id = 0 为顶级分类，支持任意层级嵌套（实际使用建议不超过两层）。
 */
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /** 分页查询（平铺列表） */
    public Result<List<Category>> page(KeywordQuery query, Integer parentId) {
        String kw = query.safeKeyword();
        long total = categoryMapper.countByCondition(kw, query.getStatus(), parentId);
        List<Category> list = total == 0
                ? new ArrayList<Category>()
                : categoryMapper.selectPage(kw, query.getStatus(), parentId,
                        query.offset(), query.safeSize());
        return query.toResult(list, total);
    }

    /**
     * 查询树形结构。
     * 一次查出全部记录，再在内存里按 parent_id 组装，避免递归查库（N+1）。
     */
    public List<Category> tree(Integer status) {
        List<Category> all = categoryMapper.selectAllForTree(status);
        Map<Integer, Category> byId = new LinkedHashMap<>();
        for (Category c : all) {
            c.setChildren(new ArrayList<Category>());
            byId.put(c.getId(), c);
        }
        List<Category> roots = new ArrayList<>();
        for (Category c : all) {
            Integer pid = c.getParentId();
            Category parent = (pid == null || pid == 0) ? null : byId.get(pid);
            if (parent == null) {
                // parent_id 为 0，或父分类已被逻辑删除 -> 当作顶级节点返回，避免数据"消失"
                roots.add(c);
            } else {
                parent.getChildren().add(c);
            }
        }
        return roots;
    }

    public Category getById(Integer id) {
        return id == null ? null : categoryMapper.selectById(id);
    }

    public Category getBySlug(String slug) {
        return StringUtils.isBlank(slug) ? null : categoryMapper.selectBySlug(slug.trim());
    }

    @Transactional(rollbackFor = Exception.class)
    public int create(Category category) {
        if (StringUtils.isBlank(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        if (category.getName().trim().length() > 50) {
            throw new BusinessException("分类名称长度不能超过 50");
        }
        category.setName(category.getName().trim());

        // slug 不传时按名称自动生成
        String slug = StringUtils.trimToNull(category.getSlug());
        if (slug == null) {
            slug = StringUtils.toSlug(category.getName());
        }
        if (slug == null) {
            throw new BusinessException("无法根据名称生成 slug，请手动指定");
        }
        category.setSlug(StringUtils.trimToNull(slug, 50));
        if (categoryMapper.countBySlug(category.getSlug(), null) > 0) {
            throw new BusinessException(409, "分类别名已存在：" + category.getSlug());
        }

        checkParent(category.getParentId(), null);
        applyDefaults(category);
        return categoryMapper.insert(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Integer id, Category category) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(404, "分类不存在：id=" + id);
        }
        category.setId(id);

        if (StringUtils.isNotBlank(category.getName())) {
            category.setName(category.getName().trim());
        }

        if (StringUtils.isNotBlank(category.getSlug())) {
            String slug = StringUtils.trimToNull(category.getSlug(), 50);
            if (categoryMapper.countBySlug(slug, id) > 0) {
                throw new BusinessException(409, "分类别名已存在：" + slug);
            }
            category.setSlug(slug);
        } else {
            category.setSlug(null);   // 空值即不修改
        }

        if (category.getParentId() != null) {
            checkParent(category.getParentId(), id);
        }

        return categoryMapper.updateById(category);
    }

    /** 逻辑删除。有子分类或仍有文章时拒绝，避免产生孤儿数据 */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Integer id) {
        if (id == null || categoryMapper.selectById(id) == null) {
            throw new BusinessException(404, "分类不存在：id=" + id);
        }
        if (categoryMapper.countChildren(id) > 0) {
            throw new BusinessException(409, "该分类下还有子分类，请先删除子分类");
        }
        int articles = categoryMapper.countArticles(id);
        if (articles > 0) {
            throw new BusinessException(409, "该分类下还有 " + articles + " 篇文章，无法删除");
        }
        return categoryMapper.softDeleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Integer id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status 只能为 0(禁用) 或 1(启用)");
        }
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(404, "分类不存在：id=" + id);
        }
        return categoryMapper.updateStatus(id, status);
    }

    // ------------------------------------------------------------------ 私有

    /** 校验父分类：不能挂到自己身上，且父分类必须存在 */
    private void checkParent(Integer parentId, Integer selfId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        if (parentId.equals(selfId)) {
            throw new BusinessException("父分类不能是自己");
        }
        if (categoryMapper.selectById(parentId) == null) {
            throw new BusinessException("父分类不存在：id=" + parentId);
        }
    }

    private void applyDefaults(Category c) {
        if (c.getParentId() == null) {
            c.setParentId(0);
        }
        if (c.getSortOrder() == null) {
            c.setSortOrder(0);
        }
        if (c.getStatus() == null) {
            c.setStatus(1);
        }
        c.setIsDeleted(0);
    }
}
