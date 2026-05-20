import api from './api'

export const analyticsApi = {
  getTodayEnergy() {
    return api.get('/analytics/energy/today')
  },

  getEnergyStatistics(startTime, endTime) {
    return api.get('/analytics/energy/stats', {
      params: { startTime, endTime }
    })
  },

  getEnergyByDevice(deviceId, startTime, endTime) {
    return api.get(`/analytics/energy/device/${deviceId}`, {
      params: { startTime, endTime }
    })
  },

  getEnergyTrend(days = 7) {
    return api.get('/analytics/energy/trend', { params: { days } })
  },

  getHighEnergyDevices(limit = 5) {
    return api.get('/analytics/energy/high-energy-devices', { params: { limit } })
  },

  getPendingAnomalies() {
    return api.get('/analytics/anomaly/pending')
  },

  getAnomalyStats() {
    return api.get('/analytics/anomaly/stats')
  },

  resolveAnomaly(id) {
    return api.put(`/analytics/anomaly/${id}/resolve`)
  },

  getUserBehaviorStats(userId) {
    return api.get(`/analytics/behavior/user/${userId}/stats`)
  },

  getUserBehaviorPatterns(userId) {
    return api.get(`/analytics/behavior/user/${userId}/patterns`)
  },

  recordEnergy(data) {
    return api.post('/analytics/energy', data)
  },

  recordBehavior(data) {
    return api.post('/analytics/behavior', data)
  },

  getSceneRecommendations(userId, limit = 4) {
    return api.get(`/analytics/recommend/scenes/${userId}`, { params: { limit } })
  }
}
