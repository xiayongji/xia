import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { deviceApi } from '../api/device'
import {
  getDeviceId,
  mapDeviceFromApi,
  loadUiState,
  saveUiState,
  LABEL_TO_TYPE
} from '../utils/deviceHelpers'

const SCENE_PRESETS = {
  home: {
    name: '回家模式',
    actions: [
      { match: ['灯', 'light'], command: 'on' },
      { match: ['空调', 'ac'], command: 'on' }
    ]
  },
  leave: {
    name: '离家模式',
    actions: [
      { match: ['灯', 'light'], command: 'off' },
      { match: ['空调', 'ac'], command: 'off' }
    ]
  },
  sleep: {
    name: '睡眠模式',
    actions: [
      { match: ['灯', 'light'], command: 'off' },
      { match: ['电视', 'tv'], command: 'off' }
    ]
  },
  movie: {
    name: '阅读模式',
    actions: [{ match: ['灯', 'light'], command: 'on' }]
  }
}

export const useDeviceStore = defineStore('devices', () => {
  const devices = ref([])
  const loading = ref(false)
  const lastFetchedAt = ref(0)
  let pollInterval = null
  
  const POLL_INTERVAL = 5000

  function applyLocal(devicesList) {
    devices.value = devicesList
    localStorage.setItem('devicesCache', JSON.stringify(devicesList))
  }

  async function fetchDevices(force = false) {
    if (!force && devices.value.length && Date.now() - lastFetchedAt.value < 5000) {
      return devices.value
    }
    loading.value = true
    const uiState = loadUiState()
    try {
      const data = await deviceApi.getDevices()
      const list = Array.isArray(data) ? data : []
      applyLocal(list.map(d => mapDeviceFromApi(d, uiState)))
      lastFetchedAt.value = Date.now()
    } catch (error) {
      console.error('加载设备失败', error)
      if (!devices.value.length) {
        const cached = localStorage.getItem('devicesCache')
        if (cached) {
          devices.value = JSON.parse(cached)
        }
      }
    } finally {
      loading.value = false
    }
    return devices.value
  }

  async function setDevicePower(device, turnOn) {
    const deviceId = getDeviceId(device)
    if (!deviceId) {
      throw new Error('设备 ID 无效')
    }
    const command = turnOn ? 'on' : 'off'

    await deviceApi.controlDevice(deviceId, { command })

    const newStatus = turnOn ? 'online' : 'offline'
    const target = devices.value.find(d => getDeviceId(d) === deviceId)
    if (target) {
      target.isOn = turnOn
      target.status = newStatus
      target.lastActive = new Date().toLocaleString('zh-CN')
    }
    device.isOn = turnOn
    device.status = newStatus
    device.lastActive = new Date().toLocaleString('zh-CN')

    const allStored = JSON.parse(localStorage.getItem('deviceStates') || '{}')
    allStored[deviceId] = newStatus
    localStorage.setItem('deviceStates', JSON.stringify(allStored))

    applyLocal([...devices.value])
    return target || device
  }

  async function sendCommand(device, command, extra = {}) {
    const deviceId = getDeviceId(device)
    
    await deviceApi.controlDevice(deviceId, { command, ...extra })

    if (command === 'on' || command === 'off') {
      const turnOn = command === 'on'
      
      await deviceApi.updateDeviceStatus(deviceId, { status: turnOn ? 'online' : 'offline' })
      device.isOn = turnOn
      device.status = turnOn ? 'online' : 'offline'
      device.lastActive = new Date().toLocaleString('zh-CN')
      const target = devices.value.find(d => getDeviceId(d) === deviceId)
      if (target) {
        target.isOn = device.isOn
        target.status = device.status
        target.lastActive = device.lastActive
      }
      
      const allStored = JSON.parse(localStorage.getItem('deviceStates') || '{}')
      allStored[deviceId] = device.status
      localStorage.setItem('deviceStates', JSON.stringify(allStored))
      
      applyLocal([...devices.value])
    }

    if (command === 'setTemp' && extra.value != null) {
      saveUiState(deviceId, { temperature: extra.value })
      device.temperature = extra.value
      const target = devices.value.find(d => getDeviceId(d) === deviceId)
      if (target) {
        target.temperature = extra.value
      }
    }
  }

  async function addDevice(payload) {
    const body = {
      name: payload.name,
      type: LABEL_TO_TYPE[payload.type] || payload.type,
      protocol: (payload.protocol || 'wifi').toLowerCase(),
      status: payload.status || 'offline'
    }
    await deviceApi.createDevice(body)
    await fetchDevices(true)
  }

  async function removeDevice(device) {
    await deviceApi.deleteDevice(getDeviceId(device))
    await fetchDevices(true)
  }

  function matchDevice(device, keywords) {
    const name = (device.name || '').toLowerCase()
    const type = (device.type || '').toLowerCase()
    return keywords.some(k => name.includes(k.toLowerCase()) || type.includes(k.toLowerCase()))
  }

  async function runScenePreset(presetKey) {
    const preset = SCENE_PRESETS[presetKey]
    if (!preset) return { ok: false, message: '未知场景' }

    if (!devices.value.length) {
      await fetchDevices(true)
    }

    let count = 0
    for (const action of preset.actions) {
      const targets = devices.value.filter(d => matchDevice(d, action.match))
      for (const device of targets) {
        await setDevicePower(device, action.command === 'on')
        count++
      }
    }
    return { ok: true, name: preset.name, count }
  }

  function startPolling() {
    if (pollInterval) return
    pollInterval = setInterval(() => {
      if (!document.hidden) {
        fetchDevices(true)
      }
    }, POLL_INTERVAL)
  }

  function stopPolling() {
    if (pollInterval) {
      clearInterval(pollInterval)
      pollInterval = null
    }
  }

  watch(() => document.hidden, (isHidden) => {
    if (isHidden) {
      stopPolling()
    } else {
      startPolling()
    }
  })

  return {
    devices,
    loading,
    fetchDevices,
    setDevicePower,
    sendCommand,
    addDevice,
    removeDevice,
    runScenePreset,
    scenePresets: SCENE_PRESETS,
    startPolling,
    stopPolling
  }
})
