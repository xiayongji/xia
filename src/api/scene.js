import api from './api'

// 虚拟场景数据
const mockScenes = [
  {
    id: 'S001',
    name: '回家模式',
    description: '打开客厅灯和空调',
    enabled: true,
    icon: 'home',
    bgColor: '#10B981',
    actions: [
      { deviceId: 'D001', command: 'on' },
      { deviceId: 'D002', command: 'on' }
    ],
    createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
    lastExecuted: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 'S002',
    name: '离家模式',
    description: '关闭所有设备',
    enabled: true,
    icon: 'leave',
    bgColor: '#3B82F6',
    actions: [
      { deviceId: 'D001', command: 'off' },
      { deviceId: 'D002', command: 'off' },
      { deviceId: 'D003', command: 'off' }
    ],
    createdAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000).toISOString(),
    lastExecuted: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 'S003',
    name: '睡眠模式',
    description: '关灯并降低空调温度',
    enabled: true,
    icon: 'sleep',
    bgColor: '#8B5CF6',
    actions: [
      { deviceId: 'D001', command: 'off' },
      { deviceId: 'D002', command: 'set_temp', value: 26 }
    ],
    createdAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString(),
    lastExecuted: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString()
  },
  {
    id: 'S004',
    name: '阅读模式',
    description: '调节灯光亮度适合阅读',
    enabled: false,
    icon: 'read',
    bgColor: '#F59E0B',
    actions: [
      { deviceId: 'D001', command: 'set_brightness', value: 70 }
    ],
    createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
    lastExecuted: null
  }
]

let mockSceneIdCounter = 10

export const sceneApi = {
  async getScenes() {
    try {
      return await api.get('/scene/scenes')
    } catch {
      return [...mockScenes]
    }
  },
  
  async getScene(id) {
    try {
      return await api.get(`/scene/scenes/${id}`)
    } catch {
      return mockScenes.find(s => s.id === id) || mockScenes[0]
    }
  },
  
  async createScene(data) {
    try {
      return await api.post('/scene/scenes', data)
    } catch {
      const newScene = {
        id: `S${mockSceneIdCounter++}`,
        name: data.name,
        description: data.description || '',
        enabled: true,
        icon: data.icon || 'default',
        bgColor: data.bgColor || '#10B981',
        actions: data.actions || [],
        createdAt: new Date().toISOString(),
        lastExecuted: null
      }
      mockScenes.unshift(newScene)
      return newScene
    }
  },
  
  async updateScene(id, data) {
    try {
      return await api.put(`/scene/scenes/${id}`, data)
    } catch {
      const index = mockScenes.findIndex(s => s.id === id)
      if (index > -1) {
        mockScenes[index] = { ...mockScenes[index], ...data }
        return mockScenes[index]
      }
      return data
    }
  },
  
  async deleteScene(id) {
    try {
      return await api.delete(`/scene/scenes/${id}`)
    } catch {
      const index = mockScenes.findIndex(s => s.id === id)
      if (index > -1) {
        mockScenes.splice(index, 1)
      }
      return { success: true }
    }
  },
  
  async executeScene(id) {
    try {
      return await api.post(`/scene/scenes/${id}/execute`)
    } catch {
      const scene = mockScenes.find(s => s.id === id)
      if (scene) {
        scene.lastExecuted = new Date().toISOString()
      }
      return { success: true, message: '场景执行成功' }
    }
  },

  async toggleScene(id, enabled) {
    try {
      return await api.put(`/scene/scenes/${id}/toggle`, null, { params: { enabled } })
    } catch {
      const scene = mockScenes.find(s => s.id === id)
      if (scene) {
        scene.enabled = enabled
      }
      return { success: true }
    }
  },
  
  async getSceneRules(id) {
    try {
      return await api.get(`/scene/scenes/${id}/rules`)
    } catch {
      return []
    }
  },
  
  async addSceneRule(id, data) {
    try {
      return await api.post(`/scene/scenes/${id}/rules`, data)
    } catch {
      return { success: true, id: 'R' + Date.now(), ...data }
    }
  },
  
  async getSceneExecutions(id) {
    try {
      return await api.get(`/scene/scenes/${id}/executions`)
    } catch {
      return []
    }
  }
}