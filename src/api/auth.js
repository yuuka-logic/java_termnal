import request from './request'

/** 登录：data = { username, password }，成功返回 { token, expiresIn, user } */
export function login(data) {
  return request.post('/auth/login', data)
}

/** 注册：data = { username, password, nickname?, email? }，成功返回用户对象（不含 token，需再登录） */
export function register(data) {
  return request.post('/auth/register', data)
}

/** 退出登录（会作废当前 token），token 头由拦截器自动携带 */
export function logout() {
  return request.post('/auth/logout')
}

/** 当前登录用户信息，未登录返回 code=401 */
export function getMe() {
  return request.get('/auth/me')
}
