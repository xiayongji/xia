import api from './api'

export const userApi = {
  login(data) {
    return api.post('/user/auth/login', data)
  },
  
  register(data) {
    return api.post('/user/auth/register', data)
  },
  
  logout() {
    return api.post('/user/auth/logout')
  },
  
  getUserInfo() {
    return api.get('/user/info')
  },
  
  updateUserInfo(data) {
    return api.put('/user/info', data)
  },
  
  changePassword(data) {
    return api.put('/user/password', data)
  },
  
  getRoles() {
    return api.get('/user/roles')
  },
  
  getRolePermissions(roleId) {
    return api.get(`/user/roles/${roleId}/permissions`)
  },
  
  getOperationLogs() {
    return api.get('/user/logs')
  }
}