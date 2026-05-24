import api from './api'

// 虚拟用户设置数据
const mockUserSettings = {
  themeColor: '#10B981',
  layoutStyle: 'light',
  deviceGroups: [
    { id: 'G001', name: '客厅', deviceCount: 3 },
    { id: 'G002', name: '卧室', deviceCount: 2 }
  ],
  notifications: {
    deviceOffline: true,
    energyAlert: true,
    sceneTrigger: true,
    systemUpdate: true
  },
  security: {
    twoFactor: false,
    loginAlert: true,
    autoLock: true,
    lockTime: '10'
  }
}

// 虚拟用户信息
const mockUserInfo = {
  id: 'U001',
  username: 'admin',
  fullName: '系统管理员',
  email: 'admin@example.com',
  phone: '13800138000',
  role: 'ADMIN',
  avatar: null,
  createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString()
}

export const userApi = {
  async login(data) {
    try {
      return await api.post('/user/auth/login', data)
    } catch {
      // 模拟登录成功
      return {
        accessToken: 'mock_access_token_' + Date.now(),
        refreshToken: 'mock_refresh_token_' + Date.now(),
        user: mockUserInfo
      }
    }
  },

  async register(data, roleName = 'ROLE_USER') {
    try {
      return await api.post(`/user/auth/register?roleName=${roleName}`, data)
    } catch {
      return {
        success: true,
        message: '注册成功',
        user: {
          id: 'U' + Date.now(),
          username: data.username,
          email: data.email,
          role: roleName
        }
      }
    }
  },

  async logout() {
    try {
      return await api.post('/user/auth/logout')
    } catch {
      return { success: true }
    }
  },

  async refreshToken(refreshToken) {
    try {
      return await api.post('/user/auth/refresh', { refreshToken })
    } catch {
      return {
        accessToken: 'mock_access_token_' + Date.now(),
        refreshToken: 'mock_refresh_token_' + Date.now()
      }
    }
  },

  async getUserInfo() {
    try {
      return await api.get('/user/profile')
    } catch {
      return { ...mockUserInfo }
    }
  },

  async updateUserInfo(data) {
    try {
      return await api.put('/user/profile', data)
    } catch {
      Object.assign(mockUserInfo, data)
      return { ...mockUserInfo }
    }
  },

  async changePassword(data) {
    try {
      return await api.put('/user/password', data)
    } catch {
      return { success: true, message: '密码修改成功' }
    }
  },

  async getSettings() {
    try {
      return await api.get('/user/settings')
    } catch {
      return { ...mockUserSettings }
    }
  },

  async updateSettings(data) {
    try {
      return await api.put('/user/settings', data)
    } catch {
      Object.assign(mockUserSettings, data)
      return { ...mockUserSettings }
    }
  }
}
