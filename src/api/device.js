import api from './api'

export const deviceApi = {
  getDevices() {
    return api.get('/devices/with-status')
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
    return api.post(`/devices/${id}/command`, command)
  },
  
  getDeviceStatus(id) {
    return api.get(`/devices/${id}/status`)
  },
  
  updateDeviceStatus(id, status) {
    return api.put(`/devices/${id}/status`, { status })
  },
  
  getDeviceShadow(id) {
    return api.get(`/devices/${id}/shadow`)
  },
  
  updateDeviceShadow(id, data) {
    return api.put(`/devices/${id}/shadow`, data)
  }
}