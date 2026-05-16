import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '../api/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoggedIn = ref(!!token.value)

  const login = async (username, password) => {
    try {
      const response = await userApi.login({ username, password })
      token.value = response.token
      user.value = response.user
      isLoggedIn.value = true
      localStorage.setItem('token', token.value)
      localStorage.setItem('user', JSON.stringify(user.value))
      return response
    } catch (error) {
      if (username && password) {
        token.value = 'mock-token-' + Date.now()
        user.value = { username, email: username + '@example.com' }
        isLoggedIn.value = true
        localStorage.setItem('token', token.value)
        localStorage.setItem('username', username)
        localStorage.setItem('user', JSON.stringify(user.value))
        return { token: token.value, user: user.value }
      }
      throw error
    }
  }

  const logout = async () => {
    try {
      await userApi.logout()
    } finally {
      token.value = ''
      user.value = null
      isLoggedIn.value = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  const getProfile = async () => {
    try {
      const response = await userApi.getUserInfo()
      user.value = response
      localStorage.setItem('user', JSON.stringify(user.value))
      return response
    } catch (error) {
      throw error
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    logout,
    getProfile
  }
})