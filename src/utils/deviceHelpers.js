export const TYPE_TO_LABEL = {
  LIGHT: '照明',
  AC: '空调',
  CAMERA: '摄像头',
  SENSOR: '传感器',
  LOCK: '门锁',
  CURTAIN: '窗帘',
  SWITCH: '开关',
  APPLIANCE: '家电',
  SMART: '智能控制'
}

export const LABEL_TO_TYPE = Object.fromEntries(
  Object.entries(TYPE_TO_LABEL).map(([k, v]) => [v, k])
)

export function getDeviceId(device) {
  if (!device) return ''
  if (device.deviceId) return String(device.deviceId)
  if (device.id != null && typeof device.id === 'string' && device.id.includes('-')) {
    return device.id
  }
  if (device.id != null) return String(device.id)
  return ''
}

export function mapDeviceFromApi(raw, uiState = {}) {
  const deviceId = raw.deviceId ? String(raw.deviceId) : getDeviceId(raw)
  const typeLabel = TYPE_TO_LABEL[raw.type] || raw.type
  const status = raw.status || 'offline'
  const ui = uiState[deviceId] || {}

  const lastActive = raw.statusUpdateTime
    ? new Date(raw.statusUpdateTime).toLocaleString('zh-CN')
    : (raw.updatedAt
        ? new Date(raw.updatedAt).toLocaleString('zh-CN')
        : (ui.lastActive || '无记录'))

  return {
    ...raw,
    id: deviceId,
    deviceId,
    type: typeLabel,
    status,
    isOn: status === 'online',
    brightness: ui.brightness ?? (raw.type === 'LIGHT' || typeLabel === '照明' ? 50 : undefined),
    temperature: raw.temperature ?? ui.temperature ?? (raw.type === 'AC' || typeLabel === '空调' ? 26 : undefined),
    power: raw.power ?? null,
    humidity: raw.humidity ?? null,
    lastActive
  }
}

export function loadUiState() {
  try {
    return JSON.parse(localStorage.getItem('deviceUiState') || '{}')
  } catch {
    return {}
  }
}

export function saveUiState(deviceId, partial) {
  const all = loadUiState()
  all[deviceId] = { ...(all[deviceId] || {}), ...partial }
  localStorage.setItem('deviceUiState', JSON.stringify(all))
}
