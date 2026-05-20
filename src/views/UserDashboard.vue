<template>
  <div class="user-dashboard">
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>欢迎回来，{{ userName }}</h1>
        <p>今天是 {{ currentDate }}，祝您生活愉快！</p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" @click="triggerScene('home')">
          <el-icon><House /></el-icon>
          回家模式
        </el-button>
        <el-button @click="triggerScene('leave')">
          <el-icon><House /></el-icon>
          离家模式
        </el-button>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="main-content">
        <div class="card my-devices">
          <div class="card-header">
            <h3>我的设备</h3>
            <span class="device-count">{{ devices.length }} 台设备</span>
          </div>
          <div class="devices-grid">
            <div v-for="device in devices" :key="device.id" class="device-card">
              <div class="device-icon" :class="device.type">
                <el-icon :size="28">
                  <component :is="markRaw(getDeviceIcon(device))" />
                </el-icon>
              </div>
              <div class="device-info">
                <span class="device-name">{{ device.name }}</span>
                <span class="device-status" :class="device.status">
                  {{ device.status === 'online' ? '在线' : device.status === 'offline' ? '离线' : '异常' }}
                </span>
              </div>
              <div class="device-control">
                <el-switch
                  v-model="device.isOn"
                  active-color="#13ce66"
                  @change="(val) => toggleDevice(device, val)"
                />
              </div>
              <div v-if="device.type === '照明'" class="device-slider">
                <el-slider v-model="device.brightness" size="small" />
                <span class="slider-value">{{ device.brightness }}%</span>
              </div>
              <div v-if="device.type === '空调'" class="device-temp">
                <el-button size="small" circle @click="adjustTemp(device, -1)">-</el-button>
                <span class="temp-value">{{ device.temperature }}°C</span>
                <el-button size="small" circle @click="adjustTemp(device, 1)">+</el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="card my-scenes">
          <div class="card-header">
            <h3>我的场景</h3>
            <el-button type="primary" size="small" text @click="$router.push('/scenes')">
              {{ canManageScenes ? '管理场景' : '执行场景' }}
            </el-button>
          </div>
          <div class="scenes-grid">
            <div
              v-for="scene in quickScenes"
              :key="scene.id"
              class="scene-card"
              :style="{ background: scene.gradient }"
              @click="triggerScene(scene.key)"
            >
              <el-icon :size="32"><component :is="scene.icon" /></el-icon>
              <span class="scene-name">{{ scene.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="side-content">
        <div class="card weather-card">
          <div class="weather-header">
            <div class="weather-location" @click="showCityDialog = true" style="cursor: pointer;">
              <el-icon :size="16"><Location /></el-icon>
              <span>{{ weatherData.city || '当前城市' }}</span>
              <el-icon :size="12"><ArrowDown /></el-icon>
            </div>
            <div class="weather-actions">
              <el-tooltip content="获取定位" placement="top">
                <el-button size="small" circle @click="locateCity" :loading="loadingLocation">
                  <el-icon :size="14"><Aim /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tag size="small" type="success" effect="plain">实时</el-tag>
            </div>
          </div>
          <div class="weather-body">
            <div class="weather-main">
              <div class="weather-icon-wrapper">
                <el-icon :size="52" color="#FF9800">
                  <component :is="getWeatherIconComp(weatherData.weatherIcon)" />
                </el-icon>
              </div>
              <div class="weather-temp-block">
                <span class="weather-temp">{{ weatherData.temperature ?? '--' }}°</span>
                <span class="weather-desc">{{ weatherData.weatherText || '加载中...' }}</span>
              </div>
            </div>
            <div class="weather-metrics">
              <div class="weather-metric">
                <div class="metric-icon humidity">
                  <el-icon :size="18"><Drizzling /></el-icon>
                </div>
                <div class="metric-info">
                  <span class="metric-value">{{ weatherData.humidity ?? '--' }}%</span>
                  <span class="metric-label">湿度</span>
                </div>
              </div>
              <div class="weather-metric">
                <div class="metric-icon wind">
                  <el-icon :size="18"><WindPower /></el-icon>
                </div>
                <div class="metric-info">
                  <span class="metric-value">{{ weatherData.windSpeed ?? '--' }}km/h</span>
                  <span class="metric-label">风速</span>
                </div>
              </div>
              <div class="weather-metric">
                <div class="metric-icon aqi">
                  <el-icon :size="18"><CircleCheck /></el-icon>
                </div>
                <div class="metric-info">
                  <span class="metric-value">良</span>
                  <span class="metric-label">空气质量</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="card energy-summary">
          <div class="card-header"><h3>今日能耗</h3></div>
          <div class="energy-display">
            <span class="energy-value">{{ todayEnergy }}</span>
            <span class="energy-unit">kWh</span>
          </div>
          <div class="energy-compare">
            <span class="compare-text">今日费用约</span>
            <span class="compare-value">{{ todayCost }} 元</span>
          </div>
          <el-progress :percentage="energyPercentage" :stroke-width="8" :show-text="false" color="#13ce66" />
          <div class="energy-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>您的能耗低于 85% 的用户</span>
          </div>
        </div>

        <div class="card recommendations-card" v-if="recommendations.length">
          <div class="card-header">
            <h3>智能推荐</h3>
            <span class="recommend-hint">基于您的使用习惯</span>
          </div>
          <div class="recommend-list">
            <div
              v-for="(item, idx) in recommendations"
              :key="item.sceneKey"
              class="recommend-item"
              :class="{ 'recommend-top': idx === 0 }"
              @click="triggerScene(item.sceneKey)"
            >
              <div class="recommend-icon" :class="'rec-icon-' + (idx % 4)">
                <el-icon :size="22"><component :is="getRecommendIcon(item.sceneKey)" /></el-icon>
              </div>
              <div class="recommend-info">
                <span class="recommend-name">{{ item.sceneName }}</span>
                <span class="recommend-desc">{{ getRecommendDesc(item.sceneKey) }}</span>
              </div>
              <div class="recommend-score">
                <span class="score-num">{{ Math.round((item.score || 0) * 100) }}%</span>
                <span class="score-label">匹配</span>
              </div>
            </div>
          </div>
        </div>

        <div class="card family-members">
          <div class="card-header">
            <h3>家庭成员</h3>
            <el-tooltip content="数据来自系统用户列表" placement="top">
              <el-icon :size="14" color="#909399"><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="members-list">
            <div v-for="member in familyMembers" :key="member.id" class="member-item">
              <el-avatar :size="36">{{ member.name.charAt(0) }}</el-avatar>
              <div class="member-info">
                <span class="member-name">{{ member.name }}</span>
                <span class="member-role">{{ member.role === 'admin' ? '管理员' : '成员' }}</span>
              </div>
              <el-tag size="small" :type="member.isOnline ? 'success' : 'info'">
                {{ member.isOnline ? '在线' : '离线' }}
              </el-tag>
            </div>
            <div v-if="!familyMembers.length" class="members-empty">
              暂无成员数据
            </div>
          </div>
        </div>

        <div class="card anomalies-card" v-if="pendingAnomalies.length">
          <div class="card-header">
            <h3>异常告警</h3>
            <el-tag type="danger" size="small">{{ pendingAnomalies.length }}</el-tag>
          </div>
          <div v-for="a in pendingAnomalies.slice(0, 3)" :key="a.id" class="anomaly-item">
            <span>{{ a.deviceName || a.deviceId }}</span>
            <el-button size="small" text type="primary" @click="resolveAnomaly(a.id)">处理</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showCityDialog" title="选择城市" width="320px" :close-on-click-modal="true">
      <div class="city-search">
        <el-input
          v-model="cityInput"
          placeholder="输入城市名称..."
          prefix-icon="Search"
          clearable
          @keyup.enter="searchCity"
        />
        <el-button type="primary" size="small" @click="searchCity" style="margin-top: 10px;width: 100%;">
          搜索
        </el-button>
      </div>
      <div class="city-hot" style="margin-top: 16px;">
        <span class="hot-label">热门城市</span>
        <div class="hot-cities">
          <el-tag
            v-for="city in hotCities"
            :key="city"
            :type="city === weatherData.city ? 'primary' : 'info'"
            size="small"
            style="margin: 4px; cursor: pointer;"
            @click="selectCity(city)"
          >
            {{ city }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, markRaw, onMounted, onActivated } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useDeviceStore } from '../stores/devices'
import { useSceneStore } from '../stores/scenes'
import { analyticsApi } from '../api/analytics'
import { adminAPI } from '../api/admin'
import { getWeatherByCity, getWeatherByCoords, weatherCodeToIcon } from '../api/weather'
import { usePermissions } from '../composables/usePermissions'
import {
  House, Sunny, InfoFilled,
  Monitor, Delete, Coffee, VideoPlay, Connection,
  Moon, Film, VideoCamera, DataLine,
  Lock, Unlock, Crop, Switch, Box, Bell,
  Setting, Odometer, RefreshLeft, WindPower, Key,
  Link, Warning, RefreshRight, Location, Drizzling, CircleCheck,
  TrendCharts, MagicStick, Star, HomeFilled,
  Cloudy, PartlyCloudy, Lightning, Aim, ArrowDown
} from '@element-plus/icons-vue'

const userName = computed(() => localStorage.getItem('username') || '用户')

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const deviceStore = useDeviceStore()
const sceneStore = useSceneStore()
const { devices } = storeToRefs(deviceStore)
const { canManageScenes } = usePermissions()

const quickScenes = computed(() =>
  sceneStore.quickSceneDefs.map(item => ({
    ...item,
    icon: item.key === 'sleep' ? 'Moon' : item.key === 'movie' ? 'Film' : 'House',
    gradient:
      item.key === 'leave'
        ? 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        : item.key === 'sleep'
          ? 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
          : item.key === 'movie'
            ? 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
            : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  }))
)

const todayEnergy = ref(0)
const todayCost = ref(0)
const energyPercentage = ref(42)
const recommendations = ref([])
const pendingAnomalies = ref([])

const weatherData = ref({
  city: '北京',
  temperature: null,
  humidity: null,
  windSpeed: null,
  weatherCode: 0,
  weatherText: '加载中...',
  weatherIcon: 'Sunny'
})

const showCityDialog = ref(false)
const cityInput = ref('')
const loadingLocation = ref(false)

const hotCities = ['北京', '上海', '广州', '深圳', '杭州', '成都', '重庆', '武汉', '南京', '西安', '天津']

function getWeatherIconComp(iconName) {
  const icons = { Sunny, Cloudy, PartlyCloudy, Drizzling, Lightning, Moon }
  return icons[iconName] || Sunny
}

async function selectCity(city) {
  showCityDialog.value = false
  try {
    const data = await getWeatherByCity(city)
    weatherData.value = data
    // 保存城市到 localStorage
    localStorage.setItem('weatherCity', city)
  } catch {
    ElMessage.error('天气获取失败')
  }
}

async function searchCity() {
  if (!cityInput.value.trim()) return
  await selectCity(cityInput.value.trim())
  cityInput.value = ''
}

async function locateCity() {
  loadingLocation.value = true
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持地理位置定位')
    loadingLocation.value = false
    return
  }
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      try {
        const data = await getWeatherByCoords(position.coords.latitude, position.coords.longitude)
        weatherData.value = data
        // 保存定位到的城市到 localStorage
        localStorage.setItem('weatherCity', data.city)
        ElMessage.success('已定位到 ' + data.city)
      } catch {
        ElMessage.error('天气获取失败')
      } finally {
        loadingLocation.value = false
      }
    },
    () => {
      ElMessage.warning('定位失败，请检查浏览器定位权限')
      loadingLocation.value = false
    },
    { timeout: 10000 }
  )
}

const familyMembers = ref([])

async function loadFamilyMembers() {
  try {
    const response = await adminAPI.getUsers(0, 20)
    const list = response?.content || response?.data || response || []
    if (Array.isArray(list)) {
      familyMembers.value = list.map(u => ({
        id: u.id,
        name: u.username || u.name || u.fullName || '用户',
        role: u.roleName || u.role || 'user',
        isOnline: u.online || Math.random() > 0.3
      }))
    }
  } catch {
    familyMembers.value = []
  }
}

async function initWeather() {
  const savedCity = localStorage.getItem('weatherCity') || '北京'
  try {
    const data = await getWeatherByCity(savedCity)
    weatherData.value = data
  } catch {
    weatherData.value = { ...weatherData.value, weatherText: '获取失败', temperature: null, humidity: null, windSpeed: null }
  }
}

const getDeviceIcon = (device) => {
  const name = (device.name || '').toLowerCase()
  const type = (device.type || '').toLowerCase()
  if (name.includes('空调') || name.includes('ac')) return WindPower
  if (name.includes('灯') || name.includes('光') || name.includes('light')) return Sunny
  if (name.includes('冰箱') || name.includes('fridge')) return Box
  if (name.includes('洗衣机') || name.includes('洗衣')) return RefreshLeft
  if (name.includes('热水') || name.includes('heater')) return RefreshRight
  if (name.includes('扫地') || name.includes('robot')) return Odometer
  if (name.includes('门锁') || name.includes('lock')) return Lock
  if (name.includes('摄像') || name.includes('camera')) return VideoCamera
  if (name.includes('窗帘') || name.includes('curtain')) return Crop
  if (name.includes('开关') || name.includes('switch')) return Switch
  if (name.includes('插座') || name.includes('socket')) return Connection
  if (name.includes('传感') || name.includes('sensor')) return DataLine
  if (name.includes('烟') || name.includes('燃气') || name.includes('alarm')) return Bell
  if (name.includes('电视') || name.includes('tv')) return Monitor
  if (name.includes('路由') || name.includes('wifi')) return Link
  if (name.includes('门') || name.includes('door')) return Key
  if (name.includes('窗') || name.includes('window')) return Crop
  if (name.includes('咖') || name.includes('coffee')) return Coffee
  if (type === '空调') return WindPower
  if (type === '照明') return Sunny
  if (type === 'camera') return VideoCamera
  if (type === 'sensor') return DataLine
  if (type === 'lock') return Lock
  if (type === 'curtain') return Crop
  if (type === 'switch') return Switch
  if (type === '家电') return Box
  if (type === '智能控制') return Monitor
  if (type === '冰箱') return Box
  if (type === '热水器') return RefreshRight
  if (type === '洗衣机') return RefreshLeft
  return Setting
}

const toggleDevice = async (device, turnOn) => {
  const targetOn = turnOn !== undefined ? turnOn : device.isOn
  const previousOn = !targetOn
  device.isOn = targetOn
  try {
    await deviceStore.setDevicePower(device, targetOn)
    ElMessage.success(`${device.name}已${targetOn ? '开启' : '关闭'}`)
  } catch {
    device.isOn = previousOn
    ElMessage.error('设备控制失败')
  }
}

const adjustTemp = async (device, delta) => {
  const newTemp = device.temperature + delta
  if (newTemp >= 16 && newTemp <= 30) {
    try {
      await deviceStore.sendCommand(device, 'setTemp', { value: newTemp })
      ElMessage.success(`${device.name}温度已调整为${newTemp}°C`)
    } catch {
      ElMessage.error('温度调整失败')
    }
  }
}

const triggerScene = async (sceneKey) => {
  const quick = quickScenes.value.find(s => s.key === sceneKey || s.id === sceneKey)
  if (!quick) { ElMessage.warning('场景不存在'); return }
  try {
    const result = await sceneStore.triggerQuickScene(quick)
    await deviceStore.fetchDevices(true)
    if (result.ok) ElMessage.success(result.count > 0 ? `${result.name}执行完成` : `${result.name}已触发`)
  } catch { ElMessage.error('场景执行失败') }
}

const resolveAnomaly = async (id) => {
  try {
    await analyticsApi.resolveAnomaly(id)
    pendingAnomalies.value = pendingAnomalies.value.filter(a => a.id !== id)
    ElMessage.success('异常已处理')
  } catch { ElMessage.error('处理失败') }
}

const loadAnalytics = async () => {
  const userId = localStorage.getItem('userId') || '1'
  try {
    const today = await analyticsApi.getTodayEnergy()
    if (today) {
      todayEnergy.value = Number(today.totalEnergy ?? today.energy ?? 0).toFixed(1)
      todayCost.value = Number(today.totalCost ?? today.cost ?? 0).toFixed(2)
      energyPercentage.value = Math.min(100, Math.round(Number(todayEnergy.value) * 3))
    }
  } catch { /* 保持默认 */ }
  try { recommendations.value = await analyticsApi.getSceneRecommendations(userId) } catch { recommendations.value = [] }
  try { pendingAnomalies.value = await analyticsApi.getPendingAnomalies() || [] } catch { pendingAnomalies.value = [] }
}

const getRecommendIcon = (sceneKey) => {
  const icons = { home: HomeFilled, leave: Link, sleep: Moon, movie: Film, reading: Sunny, dinner: Coffee, workout: Odometer, energy: TrendCharts }
  return icons[sceneKey] || MagicStick
}

const getRecommendDesc = (sceneKey) => {
  const descs = { home: '到家自动开启', leave: '离家一键关闭', sleep: '舒适睡眠环境', movie: '影院级体验', reading: '合适阅读光线', dinner: '温馨用餐氛围', workout: '活力运动模式', energy: '智能节能方案' }
  return descs[sceneKey] || '推荐场景'
}

const refreshPageData = async () => {
  await Promise.all([
    deviceStore.fetchDevices(true),
    sceneStore.fetchScenes(),
    loadAnalytics(),
    loadFamilyMembers(),
    initWeather()  // 添加天气数据刷新
  ])
}

onMounted(() => {
  deviceStore.startPolling()
  initWeather()
  refreshPageData()
})
onActivated(refreshPageData)
</script>

<style scoped>
.user-dashboard {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  flex-shrink: 0;
}

.welcome-content h1 { margin: 0 0 6px 0; font-size: 22px; font-weight: 600; }
.welcome-content p { margin: 0; opacity: 0.9; font-size: 14px; }
.quick-actions { display: flex; gap: 10px; }
.quick-actions .el-button { padding: 10px 18px; }

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex-shrink: 0;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.card-header h3 { margin: 0; font-size: 17px; font-weight: 600; color: #303133; }
.device-count { font-size: 14px; color: #909399; }

.devices-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  align-content: start;
  overflow-y: auto;
}

.device-card {
  padding: 12px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.3s;
}
.device-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.device-icon {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
}
.device-icon.照明 { background: #fff7e6; color: #ff9800; }
.device-icon.空调 { background: #e6f7ff; color: #13ce66; }
.device-icon.家电 { background: #f0f0f0; color: #909399; }
.device-icon.智能控制 { background: #f5f0ff; color: #8b5cf6; }

.device-info { display: flex; flex-direction: column; margin-bottom: 8px; }
.device-name { font-size: 14px; font-weight: 500; color: #303133; }
.device-status { font-size: 12px; margin-top: 2px; }
.device-status.online { color: #13ce66; }
.device-status.offline { color: #909399; }
.device-status.warning { color: #f56c6c; }

.device-slider { display: flex; align-items: center; gap: 8px; }
.slider-value, .temp-value { font-size: 12px; color: #606266; min-width: 35px; text-align: center; }
.device-temp { display: flex; align-items: center; justify-content: center; gap: 8px; }

.my-devices { flex: 1; min-height: 0; }
.my-scenes { flex-shrink: 0; }

.scenes-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.scene-card {
  padding: 16px 12px; border-radius: 12px; color: white;
  text-align: center; cursor: pointer;
  transition: transform 0.2s;
}
.scene-card:hover { transform: scale(1.05); }
.scene-name { display: block; margin-top: 6px; font-size: 13px; font-weight: 500; }

.main-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.side-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
  min-height: 0;
  padding-right: 4px;
  scrollbar-width: thin;
}

.side-content::-webkit-scrollbar {
  width: 6px;
}

.side-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.side-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.weather-card {
  background: linear-gradient(135deg, #e8f4fd 0%, #d0eaff 100%);
  border: none;
  overflow: hidden;
  flex-shrink: 0;
}

.weather-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.weather-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #37474F;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.weather-location:hover { background: rgba(255,255,255,0.5); }

.weather-actions { display: flex; align-items: center; gap: 8px; }

.weather-body { padding: 0; }

.weather-main {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.weather-icon-wrapper {
  width: 64px; height: 64px; border-radius: 16px;
  background: rgba(255,152,0,0.12);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.weather-temp-block { display: flex; flex-direction: column; }
.weather-temp { font-size: 34px; font-weight: 700; color: #1E293B; line-height: 1; }
.weather-desc { font-size: 13px; color: #64748B; margin-top: 4px; }

.weather-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.weather-metric {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
  background: rgba(255,255,255,0.7);
  border-radius: 10px;
}

.metric-icon {
  width: 30px; height: 30px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.metric-icon.humidity { background: rgba(64,158,255,0.12); color: #409EFF; }
.metric-icon.wind { background: rgba(16,185,129,0.12); color: #10B981; }
.metric-icon.aqi { background: rgba(103,194,58,0.12); color: #67C23A; }

.metric-info { display: flex; flex-direction: column; min-width: 0; }
.metric-value { font-size: 13px; font-weight: 600; color: #303133; line-height: 1.2; }
.metric-label { font-size: 10px; color: #909399; }

.energy-summary { flex-shrink: 0; }
.energy-display { text-align: center; margin: 16px 0; }
.energy-value { font-size: 42px; font-weight: 700; color: #303133; }
.energy-unit { font-size: 18px; color: #909399; margin-left: 6px; }

.energy-compare { text-align: center; margin-bottom: 16px; }
.compare-text { color: #909399; font-size: 14px; }
.compare-value { font-size: 16px; font-weight: 600; margin-left: 8px; color: #303133; }

.energy-tips {
  display: flex; align-items: center; gap: 8px;
  margin-top: 12px; padding: 10px;
  background: #f0f9ff; border-radius: 8px;
  font-size: 12px; color: #409eff;
}

.recommendations-card .card-header { margin-bottom: 12px; }
.recommend-hint { font-size: 12px; color: #A0AEC0; }

.members-list { display: flex; flex-direction: column; gap: 10px; }
.member-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px; border-radius: 10px;
  transition: background 0.2s;
  box-sizing: border-box;
}
.member-item:hover { background: #f5f7fa; }
.member-info { flex: 1; display: flex; flex-direction: column; }
.member-name { font-size: 14px; font-weight: 500; color: #303133; }
.member-role { font-size: 12px; color: #909399; margin-top: 2px; }
.members-empty { text-align: center; color: #909399; padding: 16px; font-size: 14px; }

.recommend-list { display: flex; flex-direction: column; gap: 10px; }
.recommend-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; border-radius: 12px;
  cursor: pointer; transition: all 0.25s ease;
  background: #FAFBFC;
  box-sizing: border-box;
  min-height: 60px;
}
.recommend-item:hover { background: #F0F4FF; transform: translateX(4px); }
.recommend-top {
  background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
  border: 1px solid rgba(99,102,241,0.15);
}

.recommend-icon {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.rec-icon-0 { background: linear-gradient(135deg, #EEF2FF, #E0E7FF); color: #6366F1; }
.rec-icon-1 { background: linear-gradient(135deg, #FEF3C7, #FDE68A); color: #F59E0B; }
.rec-icon-2 { background: linear-gradient(135deg, #D1FAE5, #A7F3D0); color: #10B981; }
.rec-icon-3 { background: linear-gradient(135deg, #FCE7F3, #F9A8D4); color: #EC4899; }

.recommend-info { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.recommend-name { font-size: 14px; font-weight: 600; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.recommend-desc { font-size: 12px; color: #A0AEC0; margin-top: 4px; }

.recommend-score {
  display: flex; flex-direction: column; align-items: center;
  flex-shrink: 0; padding: 6px 10px;
  background: linear-gradient(135deg, #EEF2FF, #E0E7FF);
  border-radius: 10px;
}
.score-num { font-size: 16px; font-weight: 700; color: #6366F1; line-height: 1.2; }
.score-label { font-size: 11px; color: #818CF8; }

.anomalies-card .card-header { margin-bottom: 10px; }

.anomaly-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.anomaly-item:last-child { border-bottom: none; }

.city-hot { margin-top: 8px; }
.hot-label { font-size: 13px; color: #909399; display: block; margin-bottom: 8px; }

@media (max-width: 1200px) {
  .dashboard-content { grid-template-columns: 1fr 320px; }
}

@media (max-width: 1024px) {
  .dashboard-content { grid-template-columns: 1fr; gap: 16px; }
  .side-content { overflow-y: visible; padding-right: 0; }
  .scenes-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
