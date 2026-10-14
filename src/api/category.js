import request from './request'

/** 启用状态的分类树，节点形如 { id, name, children: [...] }，可直接用于 el-tree-select */
export function getCategoryTree() {
  return request.get('/category/tree', { params: { status: 1 } })
}

/** 分类详情（前台归档页取分类名） */
export function getCategoryDetail(id) {
  return request.get(`/category/${id}`)
}
