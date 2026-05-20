import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { userApi } from '../api/user'

const STORAGE_KEY = 'appUserSettings'

const defaultSettings = () => ({
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
})

export const useSettingsStore = defineStore('settings', () => {
  const themeColor = ref('#10B981')
  const layoutStyle = ref('light')
  const deviceGroups = ref(defaultSettings().deviceGroups)
  const notifications = reactive({ ...defaultSettings().notifications })
  const security = reactive({ ...defaultSettings().security })
  const loaded = ref(false)

  function applySettings(data) {
    if (!data) return
    if (data.themeColor) themeColor.value = data.themeColor
    if (data.layoutStyle) layoutStyle.value = data.layoutStyle
    if (data.deviceGroups) deviceGroups.value = data.deviceGroups
    if (data.notifications) Object.assign(notifications, data.notifications)
    if (data.security) Object.assign(security, data.security)
    if (data.theme) themeColor.value = data.theme
    if (typeof data.notifications === 'boolean') {
      notifications.deviceOffline = data.notifications
    }
  }

  function persistLocal() {
    const payload = {
      themeColor: themeColor.value,
      layoutStyle: layoutStyle.value,
      deviceGroups: deviceGroups.value,
      notifications: { ...notifications },
      security: { ...security }
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
    localStorage.setItem('themeColor', themeColor.value)
    return payload
  }

  async function load() {
    if (loaded.value) return
    const local = localStorage.getItem(STORAGE_KEY)
    if (local) {
      try {
        applySettings(JSON.parse(local))
      } catch {
        applySettings(defaultSettings())
      }
    }
    try {
      const remote = await userApi.getSettings()
      applySettings(remote)
      persistLocal()
    } catch {
      // 使用本地缓存
    }
    loaded.value = true
  }

  async function save() {
    const payload = persistLocal()
    try {
      await userApi.updateSettings(payload)
    } catch (error) {
      console.warn('同步设置到服务器失败，已保存到本地', error)
    }
    return payload
  }

  return {
    themeColor,
    layoutStyle,
    deviceGroups,
    notifications,
    security,
    loaded,
    load,
    save,
    persistLocal
  }
})
