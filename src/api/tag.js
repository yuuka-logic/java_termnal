import request from './request'

/** 全量标签（按文章数倒序），用于文章编辑页的标签多选和前台标签云 */
export function getAllTags() {
  return request.get('/tag/all')
}

/** 标签详情（前台归档页取标签名） */
export function getTagDetail(id) {
  return request.get(`/tag/${id}`)
}
