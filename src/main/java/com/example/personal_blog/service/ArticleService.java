package com.example.personal_blog.service;

import com.example.personal_blog.common.BusinessException;
import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.ArticleForm;
import com.example.personal_blog.dto.ArticleQuery;
import com.example.personal_blog.dto.ArticleVO;
import com.example.personal_blog.dto.TagBrief;
import com.example.personal_blog.entity.Article;
import com.example.personal_blog.entity.ArticleTagLink;
import com.example.personal_blog.entity.Tag;
import com.example.personal_blog.mapper.ArticleMapper;
import com.example.personal_blog.mapper.ArticleTagMapper;
import com.example.personal_blog.mapper.CategoryMapper;
import com.example.personal_blog.mapper.TagMapper;
import com.example.personal_blog.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文章管理业务层。
 * 是各模块里最复杂的一个：既要维护 article 主表，又要维护 article_tag 多对多关系，
 * 还要同步 tag.article_count 冗余统计字段。
 */
@Service
public class ArticleService {

    /** 状态：0-草稿 1-已发布 2-已下架 */
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_OFFLINE = 2;

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;

    public ArticleService(ArticleMapper articleMapper,
                          ArticleTagMapper articleTagMapper,
                          TagMapper tagMapper,
                          CategoryMapper categoryMapper) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.tagMapper = tagMapper;
        this.categoryMapper = categoryMapper;
    }

    /** 分页查询，并批量回填每篇文章的标签 */
    public Result<List<ArticleVO>> page(ArticleQuery query) {
        String kw = query.safeKeyword();
        long total = articleMapper.countByCondition(kw, query.getCategoryId(), query.getAuthorId(),
                query.getStatus(), query.getTagId());
        if (total == 0) {
            return query.toResult(new ArrayList<ArticleVO>(), total);
        }
        List<ArticleVO> list = articleMapper.selectPage(kw, query.getCategoryId(), query.getAuthorId(),
                query.getStatus(), query.getTagId(), query.offset(), query.safeSize());
        fillTags(list);
        return query.toResult(list, total);
    }

    /** 详情。不自动加浏览量，需要计数请调 increaseViewCount */
    public ArticleVO getDetail(Long id) {
        if (id == null) {
            return null;
        }
        ArticleVO vo = articleMapper.selectDetailById(id);
        if (vo != null) {
            vo.setTags(articleTagMapper.selectTagsByArticleId(id));
        }
        return vo;
    }

    public Article getById(Long id) {
        return id == null ? null : articleMapper.selectById(id);
    }

    /** 浏览量 +1 */
    @Transactional(rollbackFor = Exception.class)
    public int increaseViewCount(Long id) {
        checkExists(id);
        return articleMapper.increaseViewCount(id);
    }

    /** 点赞 / 取消点赞 */
    @Transactional(rollbackFor = Exception.class)
    public int changeLikeCount(Long id, boolean like) {
        checkExists(id);
        return articleMapper.changeLikeCount(id, like ? 1 : -1);
    }

    @Transactional(rollbackFor = Exception.class)
    public int create(ArticleForm form) {
        checkRequired(form);
        checkCategory(form.getCategoryId());

        if (form.getStatus() == null) {
            form.setStatus(STATUS_DRAFT);
        }
        validateStatus(form.getStatus());
        if (form.getIsTop() == null) {
            form.setIsTop(0);
        }
        if (form.getIsOriginal() == null) {
            form.setIsOriginal(1);
        }
        // 直接以发布状态创建时，补上发布时间
        if (form.getStatus() == STATUS_PUBLISHED && form.getPublishTime() == null) {
            form.setPublishTime(java.time.LocalDateTime.now());
        }

        articleMapper.insert(form);
        replaceTags(form.getId(), form.getTagIds());
        refreshTagCounts(distinct(form.getTagIds()));
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Long id, ArticleForm form) {
        checkExists(id);
        form.setId(id);

        if (form.getCategoryId() != null) {
            checkCategory(form.getCategoryId());
        }
        if (form.getStatus() != null) {
            validateStatus(form.getStatus());
        }

        // 从草稿变成已发布时补发布时间
        Article old = articleMapper.selectById(id);
        if (form.getStatus() != null && form.getStatus() == STATUS_PUBLISHED
                && old != null && old.getPublishTime() == null && form.getPublishTime() == null) {
            form.setPublishTime(java.time.LocalDateTime.now());
        }

        List<Integer> before = currentTagIds(id);
        int rows = articleMapper.updateById(form);

        if (form.getTagIds() != null) {
            List<Integer> after = distinct(form.getTagIds());
            replaceTags(id, after);
            refreshTagCounts(union(before, after));
        }
        return rows;
    }

    /**
     * 删除文章。用物理删除：article_tag 上的外键是 ON DELETE CASCADE，关联会自动清理。
     * 删除后重算受影响标签的文章数。
     */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        checkExists(id);
        List<Integer> affected = currentTagIds(id);
        int rows = articleMapper.deleteById(id);
        refreshTagCounts(affected);
        return rows;
    }

    /** 变更状态：0-草稿 1-已发布 2-已下架 */
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Long id, Integer status) {
        checkExists(id);
        validateStatus(status);
        int rows = articleMapper.updateStatus(id, status);
        // 发布/下架会影响标签文章数（只统计已发布文章）
        refreshTagCounts(currentTagIds(id));
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int changeTop(Long id, Integer isTop) {
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            throw new BusinessException("isTop 只能为 0(否) 或 1(是)");
        }
        checkExists(id);
        return articleMapper.updateTop(id, isTop);
    }

    /** 给文章设置标签 */
    @Transactional(rollbackFor = Exception.class)
    public int setTags(Long id, List<Integer> tagIds) {
        checkExists(id);
        List<Integer> before = currentTagIds(id);
        int n = replaceTags(id, tagIds);
        refreshTagCounts(union(before, distinct(tagIds)));
        return n;
    }

    // ------------------------------------------------------------------ 私有

    /**
     * 重建文章的标签关联：先删后插。
     * 会校验每个 tagId 是否真实存在，避免写入无效关联。
     */
    private int replaceTags(Long articleId, List<Integer> tagIds) {
        if (tagIds == null) {
            return 0;   // null 表示不修改标签
        }
        articleTagMapper.deleteByArticleId(articleId);
        List<Integer> ids = distinct(tagIds);
        if (ids.isEmpty()) {
            return 0;
        }
        List<ArticleTagLink> links = new ArrayList<>(ids.size());
        for (Integer tagId : ids) {
            if (tagMapper.selectById(tagId) == null) {
                throw new BusinessException("标签不存在：id=" + tagId);
            }
            links.add(new ArticleTagLink(articleId, tagId));
        }
        return articleTagMapper.batchInsert(links);
    }

    /** 一次查询取回当前页所有文章的标签并分组回填，避免 N+1 */
    private void fillTags(List<ArticleVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = new ArrayList<>(list.size());
        for (ArticleVO vo : list) {
            ids.add(vo.getId());
        }
        List<TagBrief> briefs = articleTagMapper.selectTagsByArticleIds(ids);
        Map<Long, List<Tag>> grouped = new LinkedHashMap<>();
        for (TagBrief b : briefs) {
            Tag t = new Tag();
            t.setId(b.getId());
            t.setName(b.getName());
            t.setSlug(b.getSlug());
            grouped.computeIfAbsent(b.getArticleId(), k -> new ArrayList<Tag>()).add(t);
        }
        for (ArticleVO vo : list) {
            List<Tag> tags = grouped.get(vo.getId());
            vo.setTags(tags == null ? Collections.<Tag>emptyList() : tags);
        }
    }

    private List<Integer> currentTagIds(Long articleId) {
        List<Tag> tags = articleTagMapper.selectTagsByArticleId(articleId);
        List<Integer> ids = new ArrayList<>(tags.size());
        for (Tag t : tags) {
            ids.add(t.getId());
        }
        return ids;
    }

    /** 重算这些标签的冗余文章数 */
    private void refreshTagCounts(List<Integer> tagIds) {
        if (tagIds == null) {
            return;
        }
        for (Integer tagId : distinct(tagIds)) {
            if (tagId != null) {
                tagMapper.refreshArticleCount(tagId);
            }
        }
    }

    private List<Integer> union(List<Integer> a, List<Integer> b) {
        List<Integer> all = new ArrayList<>();
        if (a != null) {
            all.addAll(a);
        }
        if (b != null) {
            all.addAll(b);
        }
        return distinct(all);
    }

    /** 去重并保持顺序，同时过滤 null，避免把无效 id 写进 SQL */
    private List<Integer> distinct(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Integer> set = new LinkedHashSet<>();
        for (Integer id : ids) {
            if (id != null) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }

    private void checkExists(Long id) {
        if (id == null || articleMapper.selectById(id) == null) {
            throw new BusinessException(404, "文章不存在：id=" + id);
        }
    }

    private void checkRequired(ArticleForm form) {
        if (form == null) {
            throw new BusinessException("文章信息不能为空");
        }
        if (StringUtils.isBlank(form.getTitle())) {
            throw new BusinessException("文章标题不能为空");
        }
        if (form.getTitle().trim().length() > 200) {
            throw new BusinessException("文章标题长度不能超过 200");
        }
        if (StringUtils.isBlank(form.getContent())) {
            throw new BusinessException("文章正文不能为空");
        }
        if (StringUtils.isBlank(form.getSummary())) {
            // 摘要不传时自动截取正文前 100 字，避免列表页空白
            String plain = form.getContent().replaceAll("\\s+", " ").trim();
            form.setSummary(plain.length() > 100 ? plain.substring(0, 100) : plain);
        }
        if (form.getAuthorId() == null) {
            throw new BusinessException("作者ID不能为空");
        }
        form.setTitle(form.getTitle().trim());
        if (form.getSummary() != null && form.getSummary().length() > 500) {
            form.setSummary(form.getSummary().substring(0, 500));
        }
    }

    private void checkCategory(Integer categoryId) {
        if (categoryId != null && categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException("分类不存在：id=" + categoryId);
        }
    }

    private void validateStatus(Integer status) {
        if (status == null
                || (status != STATUS_DRAFT && status != STATUS_PUBLISHED && status != STATUS_OFFLINE)) {
            throw new BusinessException("status 只能为 0(草稿) / 1(已发布) / 2(已下架)");
        }
    }
}
