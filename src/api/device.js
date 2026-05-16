import api from './api'

export const deviceApi = {
  getDevices() {
    return api.get('/devices')
  },
  
  getDevice(id) {
    return api.get(`/devices/${id}`)
  },
  
  createDevice(data) {
    return api.post('/devices', data)
  },
  
  updateDevice(id, data) {
    return api.put(`/devices/${id}`, data)
  },
  
  deleteDevice(id) {
    return api.delete(`/devices/${id}`)
  },
  
  controlDevice(id, command) {
    return api.post(`/devices/${id}/control`, command)
  },
  
  getDeviceStatus(id) {
    return api.get(`/devices/${id}/status`)
  },
  
  getDeviceShadow(id) {
    return api.get(`/devices/${id}/shadow`)
  },
  
  updateDeviceShadow(id, data) {
    return api.put(`/devices/${id}/shadow`, data)
  }
}