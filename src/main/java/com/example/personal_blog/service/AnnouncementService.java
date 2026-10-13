package com.example.personal_blog.service;

import com.example.personal_blog.common.BusinessException;
import com.example.personal_blog.common.Result;
import com.example.personal_blog.dto.AnnouncementQuery;
import com.example.personal_blog.entity.Announcement;
import com.example.personal_blog.mapper.AnnouncementMapper;
import com.example.personal_blog.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告管理业务层。
 * 状态：0-草稿 1-已发布 2-已撤回；发布时自动补 publish_time。
 */
@Service
public class AnnouncementService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_REVOKED = 2;

    private final AnnouncementMapper announcementMapper;

    public AnnouncementService(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    public Result<List<Announcement>> page(AnnouncementQuery query) {
        String kw = query.safeKeyword();
        long total = announcementMapper.countByCondition(kw, query.getType(),
                query.getStatus(), query.getPriority());
        List<Announcement> list = total == 0
                ? new ArrayList<Announcement>()
                : announcementMapper.selectPage(kw, query.getType(), query.getStatus(),
                        query.getPriority(), query.offset(), query.safeSize());
        return query.toResult(list, total);
    }

    /** 前台"当前有效公告" */
    public List<Announcement> listActive(int limit) {
        if (limit < 1) {
            limit = 5;
        }
        if (limit > 50) {
            limit = 50;
        }
        return announcementMapper.selectActive(limit);
    }

    public Announcement getById(Long id) {
        return id == null ? null : announcementMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int create(Announcement a) {
        checkRequired(a);
        if (a.getType() == null) {
            a.setType(1);
        }
        checkType(a.getType());
        if (a.getPriority() == null) {
            a.setPriority(0);
        }
        checkPriority(a.getPriority());
        if (a.getStatus() == null) {
            a.setStatus(STATUS_DRAFT);
        }
        validateStatus(a.getStatus());
        if (a.getIsTop() == null) {
            a.setIsTop(0);
        }
        if (a.getStatus() == STATUS_PUBLISHED && a.getPublishTime() == null) {
            a.setPublishTime(LocalDateTime.now());
        }
        return announcementMapper.insert(a);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Long id, Announcement a) {
        checkExists(id);
        a.setId(id);

        if (StringUtils.isNotBlank(a.getTitle())) {
            if (a.getTitle().trim().length() > 200) {
                throw new BusinessException("公告标题长度不能超过 200");
            }
            a.setTitle(a.getTitle().trim());
        }
        if (a.getContent() != null && StringUtils.isBlank(a.getContent())) {
            throw new BusinessException("公告内容不能为空");
        }
        if (a.getType() != null) {
            checkType(a.getType());
        }
        if (a.getPriority() != null) {
            checkPriority(a.getPriority());
        }
        if (a.getStatus() != null) {
            validateStatus(a.getStatus());
        }

        Announcement old = announcementMapper.selectById(id);
        if (a.getStatus() != null && a.getStatus() == STATUS_PUBLISHED
                && old != null && old.getPublishTime() == null && a.getPublishTime() == null) {
            a.setPublishTime(LocalDateTime.now());
        }
        return announcementMapper.updateById(a);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        checkExists(id);
        return announcementMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Long id, Integer status) {
        checkExists(id);
        validateStatus(status);
        return announcementMapper.updateStatus(id, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public int changeTop(Long id, Integer isTop) {
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            throw new BusinessException("isTop 只能为 0(否) 或 1(是)");
        }
        checkExists(id);
        return announcementMapper.updateTop(id, isTop);
    }

    @Transactional(rollbackFor = Exception.class)
    public int increaseViewCount(Long id) {
        checkExists(id);
        return announcementMapper.increaseViewCount(id);
    }

    // ------------------------------------------------------------------ 私有

    private void checkExists(Long id) {
        if (id == null || announcementMapper.selectById(id) == null) {
            throw new BusinessException(404, "公告不存在：id=" + id);
        }
    }

    private void checkRequired(Announcement a) {
        if (a == null) {
            throw new BusinessException("公告信息不能为空");
        }
        if (StringUtils.isBlank(a.getTitle())) {
            throw new BusinessException("公告标题不能为空");
        }
        if (a.getTitle().trim().length() > 200) {
            throw new BusinessException("公告标题长度不能超过 200");
        }
        if (StringUtils.isBlank(a.getContent())) {
            throw new BusinessException("公告内容不能为空");
        }
        if (a.getPublisherId() == null) {
            throw new BusinessException("发布人ID不能为空");
        }
        a.setTitle(a.getTitle().trim());
    }

    private void checkType(Integer type) {
        if (type < 1 || type > 4) {
            throw new BusinessException("type 只能为 1(系统通知) / 2(维护公告) / 3(版本更新) / 4(活动通知)");
        }
    }

    private void checkPriority(Integer priority) {
        if (priority < 0 || priority > 2) {
            throw new BusinessException("priority 只能为 0(普通) / 1(重要) / 2(紧急)");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null
                || (status != STATUS_DRAFT && status != STATUS_PUBLISHED && status != STATUS_REVOKED)) {
            throw new BusinessException("status 只能为 0(草稿) / 1(已发布) / 2(已撤回)");
        }
    }
}
