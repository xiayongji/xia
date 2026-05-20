import api from './api'

export const userApi = {
  login(data) {
    return api.post('/user/auth/login', data)
  },

  register(data, roleName = 'ROLE_USER') {
    return api.post(`/user/auth/register?roleName=${roleName}`, data)
  },

  logout() {
    return api.post('/user/auth/logout')
  },

  refreshToken(refreshToken) {
    return api.post('/user/auth/refresh', { refreshToken })
  },

  getUserInfo() {
    return api.get('/user/profile')
  },

  updateUserInfo(data) {
    return api.put('/user/profile', data)
  },

  changePassword(data) {
    return api.put('/user/password', data)
  },

  getSettings() {
    return api.get('/user/settings')
  },

  updateSettings(data) {
    return api.put('/user/settings', data)
  }
}
