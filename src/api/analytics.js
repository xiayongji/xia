import api from './api'

export const analyticsApi = {
  getEnergyStatistics(params) {
    return api.get('/analytics/energy', { params })
  },
  
  getEnergyByDevice(deviceId, params) {
    return api.get(`/analytics/energy/device/${deviceId}`, { params })
  },
  
  getEnergyTrend(params) {
    return api.get('/analytics/energy/trend', { params })
  },
  
  getAnomalyDetection(params) {
    return api.get('/analytics/anomaly', { params })
  },
  
  resolveAnomaly(id) {
    return api.post(`/analytics/anomaly/${id}/resolve`)
  },
  
  getUserBehavior(params) {
    return api.get('/analytics/behavior', { params })
  },
  
  getUserBehaviorPatterns(userId) {
    return api.get(`/analytics/behavior/patterns/${userId}`)
  }
}