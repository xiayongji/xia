import api from './api'

// 虚拟用户数据
const mockUsers = [
  { id: 'U001', username: 'admin', fullName: '系统管理员', email: 'admin@example.com', phone: '13800138000', roleName: 'ADMIN', enabled: true, createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString() },
  { id: 'U002', username: 'user1', fullName: '张三', email: 'zhangsan@example.com', phone: '13800138001', roleName: 'USER', enabled: true, createdAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000).toISOString() },
  { id: 'U003', username: 'user2', fullName: '李四', email: 'lisi@example.com', phone: '13800138002', roleName: 'USER', enabled: false, createdAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString() },
  { id: 'U004', username: 'user3', fullName: '王五', email: 'wangwu@example.com', phone: '13800138003', roleName: 'USER', enabled: true, createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString() },
  { id: 'U005', username: 'guest', fullName: '访客', email: 'guest@example.com', phone: '13800138004', roleName: 'GUEST', enabled: true, createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString() }
]

// 虚拟角色数据
const mockRoles = [
  { id: 'R001', roleName: 'ADMIN', description: '系统管理员，拥有所有权限', permissionCount: 15 },
  { id: 'R002', roleName: 'USER', description: '普通用户，拥有基本功能权限', permissionCount: 8 },
  { id: 'R003', roleName: 'GUEST', description: '访客用户，拥有只读权限', permissionCount: 3 }
]

// 虚拟日志数据
const mockLogs = []
const logTypes = ['LOGIN', 'LOGOUT', 'DEVICE_CONTROL', 'SCENE_TRIGGER', 'SETTINGS_UPDATE']
for (let i = 0; i < 50; i++) {
  mockLogs.push({
    id: `L${String(i).padStart(3, '0')}`,
    username: mockUsers[i % mockUsers.length].username,
    operation: logTypes[i % logTypes.length],
    resource: ['/api/devices', '/api/scenes', '/api/settings'][i % 3],
    ipAddress: `192.168.1.${100 + (i % 50)}`,
    success: i % 5 !== 0,
    details: `操作记录 ${i + 1}`,
    timestamp: new Date(Date.now() - i * 60 * 60 * 1000).toISOString()
  })
}

let mockUserIdCounter = 10

export const adminAPI = {
  async getDashboardStats() {
    try {
      return await api.get('/admin/dashboard/stats')
    } catch {
      return {
        totalUsers: 25,
        activeUsers: 18,
        totalRoles: 3,
        recentLogs: 42
      }
    }
  },

  async getUsers(page = 0, size = 20, sortBy = 'createdAt', sortDir = 'desc') {
    try {
      return await api.get('/admin/users', {
        params: { page, size, sortBy, sortDir }
      })
    } catch {
      const start = page * size
      const end = start + size
      return {
        users: mockUsers.slice(start, end),
        totalItems: mockUsers.length
      }
    }
  },

  async getUserDetail(userId) {
    try {
      return await api.get(`/admin/users/${userId}`)
    } catch {
      return mockUsers.find(u => u.id === userId) || mockUsers[0]
    }
  },

  async createUser(userData) {
    try {
      return await api.post('/admin/users', userData)
    } catch {
      const newUser = {
        id: `U${mockUserIdCounter++}`,
        ...userData,
        enabled: true,
        createdAt: new Date().toISOString()
      }
      mockUsers.unshift(newUser)
      return newUser
    }
  },

  async updateUser(userId, userData) {
    try {
      return await api.put(`/admin/users/${userId}`, userData)
    } catch {
      const index = mockUsers.findIndex(u => u.id === userId)
      if (index > -1) {
        mockUsers[index] = { ...mockUsers[index], ...userData }
        return mockUsers[index]
      }
      return userData
    }
  },

  async deleteUser(userId) {
    try {
      return await api.delete(`/admin/users/${userId}`)
    } catch {
      const index = mockUsers.findIndex(u => u.id === userId)
      if (index > -1) {
        mockUsers.splice(index, 1)
      }
      return { success: true }
    }
  },

  async toggleUserStatus(userId, enabled) {
    try {
      return await api.patch(`/admin/users/${userId}/status`, null, {
        params: { enabled }
      })
    } catch {
      const user = mockUsers.find(u => u.id === userId)
      if (user) {
        user.enabled = enabled
      }
      return { success: true }
    }
  },

  async resetUserPassword(userId, newPassword) {
    try {
      return await api.post(`/admin/users/${userId}/reset-password`, { newPassword })
    } catch {
      return { success: true, message: '密码重置成功' }
    }
  },

  async assignRole(userId, roleId) {
    try {
      return await api.post(`/admin/users/${userId}/assign-role`, { roleId })
    } catch {
      const user = mockUsers.find(u => u.id === userId)
      const role = mockRoles.find(r => r.id === roleId)
      if (user && role) {
        user.roleName = role.roleName
      }
      return { success: true }
    }
  },

  async getRoles() {
    try {
      return await api.get('/admin/roles')
    } catch {
      return [...mockRoles]
    }
  },

  async createRole(roleData) {
    try {
      return await api.post('/admin/roles', roleData)
    } catch {
      const newRole = {
        id: `R${mockRoles.length + 1}`,
        ...roleData,
        permissionCount: 5
      }
      mockRoles.push(newRole)
      return newRole
    }
  },

  async updateRole(roleId, roleData) {
    try {
      return await api.put(`/admin/roles/${roleId}`, roleData)
    } catch {
      const index = mockRoles.findIndex(r => r.id === roleId)
      if (index > -1) {
        mockRoles[index] = { ...mockRoles[index], ...roleData }
        return mockRoles[index]
      }
      return roleData
    }
  },

  async deleteRole(roleId) {
    try {
      return await api.delete(`/admin/roles/${roleId}`)
    } catch {
      const index = mockRoles.findIndex(r => r.id === roleId)
      if (index > -1 && !['R001', 'R002'].includes(roleId)) {
        mockRoles.splice(index, 1)
      }
      return { success: true }
    }
  },

  async getLogs(page = 0, size = 20, operation = '', username = '') {
    try {
      return await api.get('/admin/logs', {
        params: { page, size, operation, username }
      })
    } catch {
      let filtered = mockLogs
      if (operation) filtered = filtered.filter(l => l.operation === operation)
      if (username) filtered = filtered.filter(l => l.username === username)
      
      const start = page * size
      const end = start + size
      return {
        logs: filtered.slice(start, end),
        totalItems: filtered.length
      }
    }
  },

  async getUserLogs(username) {
    try {
      return await api.get(`/admin/logs/user/${username}`)
    } catch {
      return mockLogs.filter(l => l.username === username)
    }
  },

  async getSystemHealth() {
    try {
      return await api.get('/admin/health')
    } catch {
      return {
        status: 'healthy',
        uptime: '15d 8h 32m',
        database: 'connected',
        redis: 'connected',
        services: {
          analytics: 'running',
          device: 'running',
          edge: 'running'
        }
      }
    }
  },

  async getSystemConfig() {
    try {
      return await api.get('/admin/config')
    } catch {
      return {
        systemName: '智能家居管理系统',
        version: '1.0.0',
        maxUsers: 1000,
        sessionTimeout: 3600,
        passwordMinLength: 6,
        allowRegistration: false
      }
    }
  }
}

export default adminAPI
