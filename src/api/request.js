import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 统一请求封装。
 * - baseURL /api：开发环境走 vite 代理到 http://localhost:8080，生产由 nginx 转发
 * - 鉴权：后端约定的请求头名就是 token（不是 Authorization），写操作必须携带
 * - 响应解包：后端 HTTP 恒为 200，业务状态码在 body.code，code===200 才算成功
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('pb_token')
  if (token) {
    config.headers.token = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      // 返回完整信封 { code, message, data, total, page, size, pages }，分页字段才能取到
      return res
    }
    if (res.code === 401) {
      localStorage.removeItem('pb_token')
      localStorage.removeItem('pb_user')
      ElMessage.error(res.message || '登录已过期，请重新登录')
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      return Promise.reject(new Error(res.message || '未登录'))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || `业务错误：${res.code}`))
  },
  (error) => {
    ElMessage.error('网络异常，请确认后端服务已启动（localhost:8080）')
    return Promise.reject(error)
  },
)

export default request
