import request from './request'

/** 前台有效公告：只返回已发布且未过期的，按置顶/优先级排序，limit 默认 5 最大 50 */
export function getActiveAnnouncements(limit = 5) {
  return request.get('/announcement/active', { params: { limit } })
}
