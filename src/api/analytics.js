import api from './api'

// 生成虚拟能耗趋势数据
function generateMockEnergyTrend(days = 30) {
  const data = []
  const now = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    const energy = 3 + Math.random() * 5 // 3-8 kWh
    data.push({
      date: date.toISOString().split('T')[0],
      day: `${date.getMonth() + 1}/${date.getDate()}`,
      energy: energy.toFixed(1),
      totalEnergy: energy,
      cost: (energy * 0.5).toFixed(2)
    })
  }
  return data
}

// 虚拟高能耗设备
const mockHighEnergyDevices = [
  { deviceName: '空调', energy: 45.2, percentage: 37.5, deviceId: 'D002' },
  { deviceName: '洗衣机', energy: 28.6, percentage: 23.7, deviceId: 'D007' },
  { deviceName: '冰箱', energy: 22.8, percentage: 18.9, deviceId: 'D008' },
  { deviceName: '照明', energy: 15.3, percentage: 12.7, deviceId: 'D001' },
  { deviceName: '其他', energy: 8.6, percentage: 7.2, deviceId: 'D009' }
]

export const analyticsApi = {
  async getTodayEnergy() {
    try {
      return await api.get('/analytics/energy/today')
    } catch {
      return {
        totalEnergy: 5.2,
        energy: 5.2,
        totalCost: 2.6,
        cost: 2.6,
        avgDailyEnergy: 4.8,
        savedEnergy: 1.2
      }
    }
  },

  async getEnergyStatistics(startTime, endTime) {
    try {
      return await api.get('/analytics/energy/stats', {
        params: { startTime, endTime }
      })
    } catch {
      return {
        totalEnergy: 120.5,
        totalCost: 60.25,
        avgDailyEnergy: 4.0,
        savedEnergy: 23.8
      }
    }
  },

  async getEnergyByDevice(deviceId, startTime, endTime) {
    try {
      return await api.get(`/analytics/energy/device/${deviceId}`, {
        params: { startTime, endTime }
      })
    } catch {
      return {
        deviceId,
        energy: Math.random() * 20 + 5,
        cost: Math.random() * 10 + 2
      }
    }
  },

  async getEnergyTrend(days = 7) {
    try {
      return await api.get('/analytics/energy/trend', { params: { days } })
    } catch {
      return generateMockEnergyTrend(days)
    }
  },

  async getHighEnergyDevices(limit = 5) {
    try {
      return await api.get('/analytics/energy/high-energy-devices', { params: { limit } })
    } catch {
      return mockHighEnergyDevices.slice(0, limit)
    }
  },

  async getPendingAnomalies() {
    try {
      return await api.get('/analytics/anomaly/pending')
    } catch {
      return [
        { id: 'A001', deviceId: 'D006', type: 'voltage', severity: 'warning', message: '电压异常', createdAt: new Date().toISOString() },
        { id: 'A002', deviceId: 'D003', type: 'connectivity', severity: 'info', message: '连接不稳定', createdAt: new Date().toISOString() }
      ]
    }
  },

  async getAnomalyStats() {
    try {
      return await api.get('/analytics/anomaly/stats')
    } catch {
      return {
        total: 12,
        warning: 3,
        critical: 1,
        resolved: 8
      }
    }
  },

  async resolveAnomaly(id) {
    try {
      return await api.put(`/analytics/anomaly/${id}/resolve`)
    } catch {
      return { success: true }
    }
  },

  async getUserBehaviorStats(userId) {
    try {
      return await api.get(`/analytics/behavior/user/${userId}/stats`)
    } catch {
      return {
        totalDevices: 8,
        activeHours: 12,
        favoriteDevice: '空调',
        scenesUsed: 5
      }
    }
  },

  async getUserBehaviorPatterns(userId) {
    try {
      return await api.get(`/analytics/behavior/user/${userId}/patterns`)
    } catch {
      return [
        { time: '07:00', action: '开灯', frequency: 5 },
        { time: '18:00', action: '开空调', frequency: 7 },
        { time: '22:00', action: '关灯', frequency: 6 }
      ]
    }
  },

  async recordEnergy(data) {
    try {
      return await api.post('/analytics/energy', data)
    } catch {
      return { success: true, recordedAt: new Date().toISOString() }
    }
  },

  async recordBehavior(data) {
    try {
      return await api.post('/analytics/behavior', data)
    } catch {
      return { success: true, recordedAt: new Date().toISOString() }
    }
  },

  async getSceneRecommendations(userId, limit = 4) {
    try {
      return await api.get(`/analytics/recommend/scenes/${userId}`, { params: { limit } })
    } catch {
      return [
        { id: 'R001', name: '节能模式', reason: '基于历史数据分析', confidence: 0.85 },
        { id: 'R002', name: '睡眠模式', reason: '根据作息习惯推荐', confidence: 0.78 }
      ]
    }
  }
}
