import request from './request'

/**
 * 分页查询文章列表。
 * params: { page, size, keyword, categoryId, tagId, authorId, status }
 * 列表接口不返回正文 content（省流量）；排序为置顶优先、id 倒序。
 */
export function getArticles(params) {
  return request.get('/article', { params })
}

/** 文章详情（含 Markdown 正文 content 与完整 tags 对象数组），GET 无需 token */
export function getArticleDetail(id) {
  return request.get(`/article/${id}`)
}

/** 新增文章，body 见 ArticleForm：title / content / authorId 必填 */
export function createArticle(data) {
  return request.post('/article', data)
}

/** 修改文章；tagIds 语义：不传=不动标签，[]=清空，数组=整体替换 */
export function updateArticle(id, data) {
  return request.put(`/article/${id}`, data)
}

/** 删除文章（物理删除，标签关联自动清理），需要 token */
export function deleteArticle(id) {
  return request.delete(`/article/${id}`)
}

/** 变更状态：0-草稿 1-已发布 2-已下架；草稿转发布时服务端自动补发布时间 */
export function changeArticleStatus(id, status) {
  return request.patch(`/article/${id}/status`, null, { params: { status } })
}

/** 置顶 / 取消置顶：isTop 1-是 0-否 */
export function changeArticleTop(id, isTop) {
  return request.patch(`/article/${id}/top`, null, { params: { isTop } })
}

/** 点赞 / 取消点赞（like=false 取消），点赞数不会减到负数 */
export function likeArticle(id, like = true) {
  return request.patch(`/article/${id}/like`, null, { params: { like } })
}

/** 浏览量 +1（原子自增），前台进入详情页时调用，失败可静默 */
export function addArticleView(id) {
  return request.post(`/article/${id}/view`)
}
