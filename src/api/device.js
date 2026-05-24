import api from './api'

// 虚拟设备数据
const mockDevices = [
  { id: 'D001', name: '客厅主灯', type: '照明', status: 'online', isOn: true, brightness: 80, lastActive: '刚刚', location: '客厅' },
  { id: 'D002', name: '卧室空调', type: '空调', status: 'online', isOn: false, temperature: 24, lastActive: '10分钟前', location: '卧室' },
  { id: 'D003', name: '智能电视', type: '家电', status: 'offline', isOn: false, lastActive: '昨天', location: '客厅' },
  { id: 'D004', name: '厨房灯', type: '照明', status: 'online', isOn: false, brightness: 60, lastActive: '1小时前', location: '厨房' },
  { id: 'D005', name: '智能门锁', type: '门锁', status: 'online', isOn: true, lastActive: '30分钟前', location: '门口' },
  { id: 'D006', name: '客厅插座', type: '插座', status: 'warning', isOn: true, lastActive: '5分钟前', location: '客厅' }
]

let mockDeviceIdCounter = 100

export const deviceApi = {
  async getDevices() {
    try {
      return await api.get('/devices/with-status')
    } catch {
      // 返回虚拟数据
      return [...mockDevices]
    }
  },
  
  async getDevice(id) {
    try {
      return await api.get(`/devices/${id}`)
    } catch {
      return mockDevices.find(d => d.id === id) || mockDevices[0]
    }
  },
  
  async createDevice(data) {
    try {
      return await api.post('/devices', data)
    } catch {
      // 模拟创建设备
      const newDevice = {
        id: `D${mockDeviceIdCounter++}`,
        name: data.name,
        type: data.type,
        status: 'offline',
        isOn: false,
        brightness: 50,
        temperature: 24,
        lastActive: '刚刚',
        location: data.location || '客厅'
      }
      mockDevices.push(newDevice)
      return newDevice
    }
  },
  
  async updateDevice(id, data) {
    try {
      return await api.put(`/devices/${id}`, data)
    } catch {
      const index = mockDevices.findIndex(d => d.id === id)
      if (index > -1) {
        mockDevices[index] = { ...mockDevices[index], ...data }
        return mockDevices[index]
      }
      return data
    }
  },
  
  async deleteDevice(id) {
    try {
      return await api.delete(`/devices/${id}`)
    } catch {
      const index = mockDevices.findIndex(d => d.id === id)
      if (index > -1) {
        mockDevices.splice(index, 1)
      }
      return { success: true }
    }
  },
  
  async controlDevice(id, command) {
    try {
      return await api.post(`/devices/${id}/command`, command)
    } catch {
      const device = mockDevices.find(d => d.id === id)
      if (device) {
        if (command.command === 'on') {
          device.isOn = true
          device.status = 'online'
        } else if (command.command === 'off') {
          device.isOn = false
        }
        device.lastActive = '刚刚'
      }
      return { success: true }
    }
  },
  
  async getDeviceStatus(id) {
    try {
      return await api.get(`/devices/${id}/status`)
    } catch {
      const device = mockDevices.find(d => d.id === id)
      return device ? { status: device.status } : { status: 'offline' }
    }
  },
  
  async updateDeviceStatus(id, status) {
    try {
      return await api.put(`/devices/${id}/status`, { status })
    } catch {
      const device = mockDevices.find(d => d.id === id)
      if (device) {
        device.status = status.status
      }
      return { success: true }
    }
  },
  
  getDeviceShadow(id) {
    return api.get(`/devices/${id}/shadow`)
  },
  
  updateDeviceShadow(id, data) {
    return api.put(`/devices/${id}/shadow`, data)
  }
}