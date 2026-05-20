import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api/user'

function normalizeRole(roleName) {
  if (!roleName) return 'user'
  const upper = String(roleName).toUpperCase()
  return upper === 'ROLE_ADMIN' || upper === 'ADMIN' ? 'admin' : 'user'
}

function loadUserFromStorage() {
  try {
    const raw = localStorage.getItem('user')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(loadUserFromStorage())
  const isLoggedIn = ref(!!token.value)
  const role = computed(() => user.value?.role || normalizeRole(localStorage.getItem('role')))

  const login = async (username, password) => {
    const response = await userApi.login({ username, password })
    const accessToken = response.accessToken || response.token
    if (!accessToken) {
      throw new Error('登录响应缺少令牌')
    }

    const role = normalizeRole(response.role)
    token.value = accessToken
    user.value = {
      username: response.username || username,
      role
    }
    isLoggedIn.value = true

    localStorage.setItem('token', token.value)
    if (response.refreshToken) {
      localStorage.setItem('refreshToken', response.refreshToken)
    }
    localStorage.setItem('username', user.value.username)
    localStorage.setItem('role', role)
    localStorage.setItem('user', JSON.stringify(user.value))
    return response
  }

  const logout = async () => {
    try {
      await userApi.logout()
    } catch {
      // 忽略登出接口错误，仍清除本地状态
    } finally {
      token.value = ''
      user.value = null
      isLoggedIn.value = false
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('user')
    }
  }

  const getProfile = async () => {
    const response = await userApi.getUserInfo()
    const role = normalizeRole(response.role)
    user.value = { ...response, role }
    localStorage.setItem('user', JSON.stringify(user.value))
    localStorage.setItem('role', role)
    return response
  }

  function syncFromStorage() {
    token.value = localStorage.getItem('token') || ''
    user.value = loadUserFromStorage()
    isLoggedIn.value = !!token.value
  }

  return {
    token,
    user,
    role,
    isLoggedIn,
    login,
    logout,
    getProfile,
    syncFromStorage
  }
})
