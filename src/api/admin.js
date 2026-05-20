import api from './api'

export const adminAPI = {
  getDashboardStats() {
    return api.get('/admin/dashboard/stats')
  },

  getUsers(page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') {
    return api.get('/admin/users', {
      params: { page, size, sortBy, sortDir }
    })
  },

  getUserDetail(userId) {
    return api.get(`/admin/users/${userId}`)
  },

  createUser(userData) {
    return api.post('/admin/users', userData)
  },

  updateUser(userId, userData) {
    return api.put(`/admin/users/${userId}`, userData)
  },

  deleteUser(userId) {
    return api.delete(`/admin/users/${userId}`)
  },

  toggleUserStatus(userId, enabled) {
    return api.patch(`/admin/users/${userId}/status`, null, {
      params: { enabled }
    })
  },

  resetUserPassword(userId, newPassword) {
    return api.post(`/admin/users/${userId}/reset-password`, { newPassword })
  },

  assignRole(userId, roleId) {
    return api.post(`/admin/users/${userId}/assign-role`, { roleId })
  },

  getRoles() {
    return api.get('/admin/roles')
  },

  createRole(roleData) {
    return api.post('/admin/roles', roleData)
  },

  updateRole(roleId, roleData) {
    return api.put(`/admin/roles/${roleId}`, roleData)
  },

  deleteRole(roleId) {
    return api.delete(`/admin/roles/${roleId}`)
  },

  getLogs(page = 0, size = 20, operation = '', username = '') {
    return api.get('/admin/logs', {
      params: { page, size, operation, username }
    })
  },

  getUserLogs(username) {
    return api.get(`/admin/logs/user/${username}`)
  },

  getSystemHealth() {
    return api.get('/admin/health')
  },

  getSystemConfig() {
    return api.get('/admin/config')
  }
}

export default adminAPI
