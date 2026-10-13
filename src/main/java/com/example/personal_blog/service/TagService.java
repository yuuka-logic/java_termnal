package com.example.personal_blog.service;

import com.example.personal_blog.common.BusinessException;
import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.KeywordQuery;
import com.example.personal_blog.entity.Tag;
import com.example.personal_blog.mapper.TagMapper;
import com.example.personal_blog.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签管理业务层。
 * tag.article_count 是冗余统计字段，文章增删改后由 ArticleService 调用 refreshArticleCount 重算。
 */
@Service
public class TagService {

    private final TagMapper tagMapper;

    public TagService(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public Result<List<Tag>> page(KeywordQuery query) {
        String kw = query.safeKeyword();
        long total = tagMapper.countByCondition(kw);
        List<Tag> list = total == 0
                ? new ArrayList<Tag>()
                : tagMapper.selectPage(kw, query.offset(), query.safeSize());
        return query.toResult(list, total);
    }

    /** 标签云：全部标签按文章数倒序 */
    public List<Tag> listAll() {
        return tagMapper.selectAllOrderByCount();
    }

    public Tag getById(Integer id) {
        return id == null ? null : tagMapper.selectById(id);
    }

    public Tag getByName(String name) {
        return StringUtils.isBlank(name) ? null : tagMapper.selectByName(name.trim());
    }

    @Transactional(rollbackFor = Exception.class)
    public int create(Tag tag) {
        if (StringUtils.isBlank(tag.getName())) {
            throw new BusinessException("标签名称不能为空");
        }
        String name = tag.getName().trim();
        if (name.length() > 50) {
            throw new BusinessException("标签名称长度不能超过 50");
        }
        if (tagMapper.countByName(name, null) > 0) {
            throw new BusinessException(409, "标签已存在：" + name);
        }
        tag.setName(name);

        String slug = StringUtils.trimToNull(tag.getSlug());
        if (slug == null) {
            slug = StringUtils.toSlug(name);
        }
        if (slug == null) {
            throw new BusinessException("无法根据名称生成 slug，请手动指定");
        }
        tag.setSlug(StringUtils.trimToNull(slug, 50));
        if (tagMapper.countBySlug(tag.getSlug(), null) > 0) {
            throw new BusinessException(409, "标签别名已存在：" + tag.getSlug());
        }

        tag.setArticleCount(0);
        tag.setIsDeleted(0);
        return tagMapper.insert(tag);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Integer id, Tag tag) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }
        if (tagMapper.selectById(id) == null) {
            throw new BusinessException(404, "标签不存在：id=" + id);
        }
        tag.setId(id);

        if (StringUtils.isNotBlank(tag.getName())) {
            String name = tag.getName().trim();
            if (tagMapper.countByName(name, id) > 0) {
                throw new BusinessException(409, "标签已存在：" + name);
            }
            tag.setName(name);
        } else {
            tag.setName(null);
        }

        if (StringUtils.isNotBlank(tag.getSlug())) {
            String slug = StringUtils.trimToNull(tag.getSlug(), 50);
            if (tagMapper.countBySlug(slug, id) > 0) {
                throw new BusinessException(409, "标签别名已存在：" + slug);
            }
            tag.setSlug(slug);
        } else {
            tag.setSlug(null);
        }

        return tagMapper.updateById(tag);
    }

    /** 逻辑删除。仍有文章关联时拒绝 */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Integer id) {
        if (id == null || tagMapper.selectById(id) == null) {
            throw new BusinessException(404, "标签不存在：id=" + id);
        }
        int articles = tagMapper.countArticles(id);
        if (articles > 0) {
            throw new BusinessException(409, "该标签还关联着 " + articles + " 篇文章，无法删除");
        }
        return tagMapper.softDeleteById(id);
    }

    /** 重算某标签的文章数（供 ArticleService 调用） */
    @Transactional(rollbackFor = Exception.class)
    public int refreshArticleCount(Integer tagId) {
        return tagMapper.refreshArticleCount(tagId);
    }

    /** 全量重算标签文章数 */
    @Transactional(rollbackFor = Exception.class)
    public int refreshAllArticleCounts() {
        return tagMapper.refreshAllArticleCounts();
    }
}
