import { ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

const TOKEN_KEY = 'pb_token'
const USER_KEY = 'pb_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(safeParse(localStorage.getItem(USER_KEY)))

  async function login(username, password) {
    const { data } = await loginApi({ username, password })
    token.value = data.token
    user.value = data.user
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data.user))
  }

  async function logout() {
    // token 过期时退出接口会返回 401，忽略即可，本地登录态照常清理
    try {
      await logoutApi()
    } catch {
      /* noop */
    }
    clear()
  }

  function clear() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, login, logout, clear }
})

function safeParse(raw) {
  try {
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}
