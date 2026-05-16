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
          <el-icon><DoorOpen /></el-icon>
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
                  <component :is="getDeviceIcon(device.type)" />
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
                  @change="toggleDevice(device)"
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
              管理场景
            </el-button>
          </div>
          <div class="scenes-grid">
            <div
              v-for="scene in quickScenes"
              :key="scene.id"
              class="scene-card"
              :style="{ background: scene.gradient }"
              @click="triggerScene(scene.id)"
            >
              <el-icon :size="32"><component :is="scene.icon" /></el-icon>
              <span class="scene-name">{{ scene.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="side-content">
        <div class="card energy-summary">
          <div class="card-header">
            <h3>今日能耗</h3>
          </div>
          <div class="energy-display">
            <span class="energy-value">{{ todayEnergy }}</span>
            <span class="energy-unit">kWh</span>
          </div>
          <div class="energy-compare">
            <span class="compare-text">较昨日</span>
            <span class="compare-value negative">-12.5%</span>
          </div>
          <el-progress
            :percentage="energyPercentage"
            :stroke-width="8"
            :show-text="false"
            color="#13ce66"
          />
          <div class="energy-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>您的能耗低于 85% 的用户</span>
          </div>
        </div>

        <div class="card family-members">
          <div class="card-header">
            <h3>家庭成员</h3>
          </div>
          <div class="members-list">
            <div v-for="member in familyMembers" :key="member.id" class="member-item">
              <el-avatar :size="36">{{ member.name.charAt(0) }}</el-avatar>
              <div class="member-info">
                <span class="member-name">{{ member.name }}</span>
                <span class="member-role">{{ member.role }}</span>
              </div>
              <el-tag size="small" :type="member.isOnline ? 'success' : 'info'">
                {{ member.isOnline ? '在线' : '离线' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="card weather-card">
          <div class="card-header">
            <h3>当前天气</h3>
          </div>
          <div class="weather-content">
            <div class="weather-main">
              <el-icon :size="48" color="#409eff"><Sunny /></el-icon>
              <span class="weather-temp">26°C</span>
            </div>
            <span class="weather-desc">晴转多云</span>
            <div class="weather-detail">
              <span>湿度: 65%</span>
              <span>空气质量: 良</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  House, Sunny, InfoFilled,
  Monitor, Delete, Coffee, VideoPlay, Connection
} from '@element-plus/icons-vue'

const userName = computed(() => localStorage.getItem('username') || '用户')

const currentDate = computed(() => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return now.toLocaleDateString('zh-CN', options)
})

const devices = ref([
  { id: 'D001', name: '客厅灯', type: '照明', status: 'online', isOn: true, brightness: 80 },
  { id: 'D002', name: '卧室灯', type: '照明', status: 'online', isOn: false, brightness: 50 },
  { id: 'D003', name: '客厅空调', type: '空调', status: 'online', isOn: true, temperature: 26 },
  { id: 'D004', name: '冰箱', type: '家电', status: 'online', isOn: true },
  { id: 'D005', name: '电视', type: '家电', status: 'online', isOn: false }
])

const quickScenes = ref([
  { id: 'home', name: '回家', icon: 'House', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 'leave', name: '离家', icon: 'DoorOpen', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 'sleep', name: '睡眠', icon: 'Moon', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { id: 'movie', name: '影院', icon: 'Film', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
])

const todayEnergy = ref(12.8)
const energyPercentage = ref(42)
const energyTip = ref('您的能耗低于 85% 的用户')

const familyMembers = ref([
  { id: 1, name: '我', role: '户主', isOnline: true },
  { id: 2, name: '父母', role: '成员', isOnline: false }
])

const getDeviceIcon = (type) => {
  const icons = {
    '照明': Sunny,
    '空调': Connection,
    '家电': Monitor,
    '智能控制': Coffee
  }
  return icons[type] || VideoPlay
}

const toggleDevice = (device) => {
  ElMessage.success(`${device.name}已${device.isOn ? '开启' : '关闭'}`)
}

const adjustTemp = (device, delta) => {
  const newTemp = device.temperature + delta
  if (newTemp >= 16 && newTemp <= 30) {
    device.temperature = newTemp
    ElMessage.success(`${device.name}温度已调整为${newTemp}°C`)
  }
}

const triggerScene = (sceneId) => {
  const sceneNames = {
    'home': '回家模式',
    'leave': '离家模式',
    'sleep': '睡眠模式',
    'movie': '影院模式'
  }
  ElMessage.success(`已启动${sceneNames[sceneId] || '场景'}`)
}
</script>

<style scoped>
.user-dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
}

.welcome-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-content p {
  margin: 0;
  opacity: 0.9;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.quick-actions .el-button {
  padding: 12px 20px;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.device-count {
  font-size: 14px;
  color: #909399;
}

.devices-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.device-card {
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.3s;
}

.device-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.device-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.device-icon.照明 { background: #fff7e6; color: #ff9800; }
.device-icon.空调 { background: #e6f7ff; color: #13ce66; }
.device-icon.家电 { background: #f0f0f0; color: #909399; }
.device-icon.智能控制 { background: #f5f0ff; color: #8b5cf6; }

.device-info {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.device-status {
  font-size: 12px;
  color: #909399;
}

.device-status.online { color: #13ce66; }
.device-status.offline { color: #909399; }
.device-status.warning { color: #f56c6c; }

.device-slider {
  display: flex;
  align-items: center;
  gap: 8px;
}

.slider-value, .temp-value {
  font-size: 12px;
  color: #606266;
  min-width: 35px;
  text-align: center;
}

.device-temp {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.scenes-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.scene-card {
  padding: 20px 16px;
  border-radius: 12px;
  color: white;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.scene-card:hover {
  transform: scale(1.05);
}

.scene-name {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  font-weight: 500;
}

.side-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.energy-display {
  text-align: center;
  margin: 20px 0;
}

.energy-value {
  font-size: 48px;
  font-weight: 700;
  color: #303133;
}

.energy-unit {
  font-size: 18px;
  color: #909399;
  margin-left: 4px;
}

.energy-compare {
  text-align: center;
  margin-bottom: 16px;
}

.compare-text {
  color: #909399;
  font-size: 14px;
}

.compare-value {
  font-size: 14px;
  font-weight: 600;
  margin-left: 8px;
}

.compare-value.negative { color: #13ce66; }
.compare-value.positive { color: #f56c6c; }

.energy-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  font-size: 13px;
  color: #409eff;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.member-item:hover {
  background: #f5f7fa;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.member-role {
  font-size: 12px;
  color: #909399;
}

.weather-content {
  text-align: center;
}

.weather-main {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.weather-temp {
  font-size: 36px;
  font-weight: 600;
  color: #303133;
}

.weather-desc {
  display: block;
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}

.weather-detail {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }

  .scenes-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
