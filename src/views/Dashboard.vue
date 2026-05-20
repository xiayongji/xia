<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <div class="header-info">
        <h1 class="page-title">设备监控</h1>
        <p class="page-subtitle">实时查看和控制您的智能设备</p>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canManageDevices"
          type="primary"
          class="add-device-btn"
          @click="showAddDevice = true"
        >
          <el-icon><Plus /></el-icon>
          <span>添加设备</span>
        </el-button>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon :size="28"><Grid /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalDevices }}</div>
          <div class="stat-label">设备总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon online">
          <el-icon :size="28"><Connection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ onlineDevices }}</div>
          <div class="stat-label">在线设备</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon offline">
          <el-icon :size="28"><Link /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ offlineDevices }}</div>
          <div class="stat-label">离线设备</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon warning">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ warningDevices }}</div>
          <div class="stat-label">异常设备</div>
        </div>
      </div>
    </div>

    <div class="device-section">
      <div class="section-header">
        <h2>设备列表</h2>
        <div class="section-actions">
          <el-input
            v-model="searchQuery"
            placeholder="搜索设备"
            prefix-icon="el-icon-search"
            class="search-input"
          />
          <el-select v-model="filterStatus" placeholder="筛选状态" class="status-filter">
            <el-option label="全部" value="" />
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="异常" value="warning" />
          </el-select>
        </div>
      </div>

      <div class="device-grid">
        <div 
          v-for="device in filteredDevices" 
          :key="device.id" 
          class="device-card"
          :class="{ 
            'device-online': device.status === 'online',
            'device-offline': device.status === 'offline',
            'device-warning': device.status === 'warning'
          }"
          @click="showDeviceDetail(device)"
        >
          <div class="device-header">
            <div class="device-icon" :class="device.type">
              <component :is="markRaw(getDeviceIcon(device))" :size="32" />
            </div>
            <div class="device-actions">
              <span :class="device.status">{{ getStatusText(device.status) }}</span>
              <button
                v-if="canManageDevices"
                class="delete-btn"
                @click.stop="confirmDelete(device)"
              >
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </div>
          
          <div class="device-info">
            <h3 class="device-name">{{ device.name }}</h3>
            <p class="device-type">{{ device.type }}</p>
          </div>
          
          <div class="device-controls" v-if="device.status === 'online'">
            <div class="control-row" v-if="device.type === '照明' || device.type === 'LIGHT' || device.type === '智能控制' || device.type === 'SMART'">
              <span class="control-label">开关</span>
              <el-switch 
                v-model="device.isOn"
                @change="(val) => toggleDevice(device, val)"
                active-color="#10B981"
                inactive-color="#94A3B8"
              />
            </div>
            <div class="control-row" v-if="device.type === '照明' || device.type === 'LIGHT'">
              <span class="control-label">亮度</span>
              <el-slider 
                v-model="device.brightness" 
                :min="0" 
                :max="100" 
                @change="adjustDevice(device)"
                active-color="#10B981"
              />
            </div>
            <div class="control-row" v-if="device.type === '空调' || device.type === 'AC'">
              <span class="control-label">温度</span>
              <div class="temp-control">
                <el-button size="small" @click="adjustTemp(device, -1)">-</el-button>
                <span class="temp-value">{{ device.temperature }}°C</span>
                <el-button size="small" @click="adjustTemp(device, 1)">+</el-button>
              </div>
            </div>
          </div>
          
          <div class="device-footer">
            <span class="last-active">最后活跃: {{ device.lastActive }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="quick-scenes">
      <div class="section-header">
        <h2>快捷场景</h2>
        <el-button type="primary" link class="view-all-btn" @click="goToScenes">查看全部</el-button>
      </div>
      
      <div class="scene-grid">
        <div 
          v-for="scene in quickScenes" 
          :key="scene.id" 
          class="scene-card"
          @click="triggerScene(scene)"
        >
          <div class="scene-icon" :style="{ background: scene.bgColor }">
            <component :is="scene.icon" :size="28" />
          </div>
          <h3 class="scene-name">{{ scene.name }}</h3>
          <p class="scene-desc">{{ scene.description }}</p>
          <button class="scene-trigger">
            <el-icon><VideoPlay /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="showAddDevice" 
      title="添加设备" 
      width="480px"
      class="add-device-dialog"
    >
      <el-form :model="newDevice" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="newDevice.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="newDevice.type" placeholder="请选择设备类型">
            <el-option label="💡 照明" value="照明" />
            <el-option label="❄️ 空调" value="空调" />
            <el-option label="📦 家电" value="家电" />
            <el-option label="🖥️ 智能控制" value="智能控制" />
            <el-option label="🧊 冰箱" value="冰箱" />
            <el-option label="🔄 洗衣机" value="洗衣机" />
            <el-option label="🚿 热水器" value="热水器" />
            <el-option label="📹 摄像头" value="摄像头" />
            <el-option label="🔒 门锁" value="门锁" />
            <el-option label="🪟 窗帘" value="窗帘" />
            <el-option label="🔌 开关" value="开关" />
            <el-option label="🔗 插座" value="插座" />
            <el-option label="📊 传感器" value="传感器" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备位置">
          <el-input v-model="newDevice.location" placeholder="例如：客厅、卧室" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDevice = false">取消</el-button>
        <el-button type="primary" @click="addDevice">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, markRaw } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useDeviceStore } from '../stores/devices'
import { useSceneStore } from '../stores/scenes'
import { usePermissions } from '../composables/usePermissions'
import { getDeviceId } from '../utils/deviceHelpers'
import { 
  Plus, Grid, Connection, Link, Warning, VideoPlay,
  Sunny, WindPower, RefreshLeft, Monitor, HomeFilled,
  Delete, VideoCamera, Key, Crop, DataLine, Switch,
  Coffee, Printer, Box, Bell, Lock, Unlock,
  Setting, Odometer, RefreshRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
const router = useRouter()
const deviceStore = useDeviceStore()
const sceneStore = useSceneStore()
const { devices } = storeToRefs(deviceStore)
const { canManageDevices, canManageScenes } = usePermissions()

const searchQuery = ref('')
const filterStatus = ref('')
const showAddDevice = ref(false)
const newDevice = ref({ name: '', type: '', location: '' })

const quickScenes = ref([
  { id: 'S001', name: '回家模式', description: '开启灯光和空调', icon: markRaw(HomeFilled), bgColor: 'linear-gradient(135deg, #10B981 0%, #059669 100%)' },
  { id: 'S002', name: '睡眠模式', description: '关闭所有灯光', icon: markRaw(Sunny), bgColor: 'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)' },
  { id: 'S003', name: '离家模式', description: '关闭所有设备', icon: markRaw(Link), bgColor: 'linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%)' },
  { id: 'S004', name: '阅读模式', description: '调节灯光亮度', icon: markRaw(Sunny), bgColor: 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)' }
])

const totalDevices = computed(() => devices.value.length)
const onlineDevices = computed(() => devices.value.filter(d => d.status === 'online').length)
const offlineDevices = computed(() => devices.value.filter(d => d.status === 'offline').length)
const warningDevices = computed(() => devices.value.filter(d => d.status === 'warning').length)

const filteredDevices = computed(() => {
  let result = devices.value
  if (searchQuery.value) {
    result = result.filter(d => 
      d.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      d.type.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }
  if (filterStatus.value) {
    result = result.filter(d => d.status === filterStatus.value)
  }
  return result
})

const getDeviceIcon = (device) => {
  const name = (device.name || '').toLowerCase()
  const type = (device.type || '').toUpperCase()
  
  if (name.includes('空调') || name.includes('ac') || name.includes('air')) return WindPower
  if (name.includes('灯') || name.includes('光') || name.includes('light')) return Sunny
  if (name.includes('冰箱') || name.includes('冰') || name.includes('fridge')) return Box
  if (name.includes('洗衣机') || name.includes('洗衣')) return RefreshLeft
  if (name.includes('热水') || name.includes('heater')) return RefreshRight
  if (name.includes('扫地') || name.includes('robot')) return Odometer
  if (name.includes('门锁') || name.includes('lock')) return Lock
  if (name.includes('摄像') || name.includes('camera')) return VideoCamera
  if (name.includes('窗帘') || name.includes('curtain')) return Crop
  if (name.includes('开关') || name.includes('switch')) return Switch
  if (name.includes('插座') || name.includes('socket')) return Connection
  if (name.includes('传感') || name.includes('sensor')) return DataLine
  if (name.includes('温湿度')) return Odometer
  if (name.includes('烟') || name.includes('燃气') || name.includes('alarm')) return Bell
  if (name.includes('电视') || name.includes('tv')) return Monitor
  if (name.includes('音') || name.includes('音响') || name.includes('speaker')) return Bell
  if (name.includes('路由') || name.includes('wifi')) return Link
  if (name.includes('门') || name.includes('door')) return Key
  if (name.includes('窗') || name.includes('window')) return Crop
  if (name.includes('打印') || name.includes('printer')) return Box
  if (name.includes('咖') || name.includes('coffee')) return Coffee
  if (name.includes('微波') || name.includes('microwave')) return RefreshLeft
  
  if (type === 'AC' || type === '空调') return WindPower
  if (type === 'LIGHT' || type === '照明') return Sunny
  if (type === 'CAMERA') return VideoCamera
  if (type === 'SENSOR') return DataLine
  if (type === 'LOCK') return Lock
  if (type === 'CURTAIN') return Crop
  if (type === 'SWITCH') return Switch
  if (type === 'APPLIANCE' || type === '家电') return Box
  if (type === 'SMART' || type === '智能控制') return Monitor
  if (type === 'REFRIGERATOR' || type === '冰箱') return Box
  if (type === 'WATER_HEATER' || type === '热水器') return RefreshRight
  if (type === 'WASHER' || type === '洗衣机') return RefreshLeft
  
  return Setting
}

const getStatusText = (status) => {
  const texts = {
    'online': '在线',
    'offline': '离线',
    'warning': '异常'
  }
  return texts[status] || status
}

const toggleDevice = async (device, turnOn) => {
  const targetOn = turnOn !== undefined ? turnOn : device.isOn
  const previousOn = !targetOn
  device.isOn = targetOn
  try {
    await deviceStore.setDevicePower(device, targetOn)
    ElMessage.success(`${device.name}已${targetOn ? '开启' : '关闭'}`)
  } catch (error) {
    device.isOn = previousOn
    ElMessage.error(error?.response?.data || '控制失败')
  }
}

const adjustDevice = (device) => {
  ElMessage.info(`${device.name}参数已更新`)
}

const adjustTemp = async (device, delta) => {
  const newTemp = device.temperature + delta
  if (newTemp >= 16 && newTemp <= 32) {
    try {
      await deviceStore.sendCommand(device, 'setTemp', { value: newTemp })
      ElMessage.info(`${device.name}温度已调整为 ${newTemp}°C`)
    } catch (error) {
      ElMessage.error('温度调整失败')
    }
  }
}

const showDeviceDetail = (device) => {
  ElMessage.info(`查看设备详情: ${device.name}`)
}

const addDevice = async () => {
  if (!canManageDevices.value) {
    ElMessage.warning('普通用户无权添加设备')
    return
  }
  if (!newDevice.value.name || !newDevice.value.type) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    await deviceStore.addDevice({
      name: newDevice.value.name,
      type: newDevice.value.type,
      protocol: 'wifi'
    })
    ElMessage.success('设备添加成功')
    showAddDevice.value = false
    newDevice.value = { name: '', type: '', location: '' }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const confirmDelete = async (device) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备「${device.name}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteDevice(device)
  } catch (error) {
    ElMessage.info('已取消删除')
  }
}

const deleteDevice = async (device) => {
  if (!canManageDevices.value) {
    ElMessage.warning('普通用户无权删除设备')
    return
  }
  try {
    await deviceStore.removeDevice(device)
    ElMessage.success(`${device.name}已删除`)
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const triggerScene = async (scene) => {
  try {
    if (scene.id) {
      await sceneStore.triggerSceneById(scene.id)
    } else {
      const quick = sceneStore.quickSceneDefs.find(q => scene.name?.includes(q.name.replace('模式', '')))
      if (quick) await sceneStore.triggerQuickScene(quick)
    }
    await deviceStore.fetchDevices(true)
    ElMessage.success(`${scene.name}已触发`)
  } catch (error) {
    ElMessage.error('场景触发失败')
  }
}

const goToScenes = () => {
  router.push('/scenes')
}

const refreshDevices = () => deviceStore.fetchDevices(true)

onMounted(() => {
  deviceStore.startPolling()
  refreshDevices()
})
onActivated(refreshDevices)
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 0;
}

.add-device-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.total {
  background: linear-gradient(135deg, #E2E8F0 0%, #CBD5E1 100%);
  color: #64748B;
}

.stat-icon.online {
  background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
  color: #059669;
}

.stat-icon.offline {
  background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%);
  color: #9CA3AF;
}

.stat-icon.warning {
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  color: #D97706;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1E293B;
}

.stat-label {
  font-size: 13px;
  color: #64748B;
}

.device-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 200px;
  border-radius: 10px;
}

.status-filter {
  width: 120px;
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.device-card {
  background: #F8FAFC;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.device-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.device-online {
  border-color: rgba(16, 185, 129, 0.3);
}

.device-offline {
  opacity: 0.7;
}

.device-warning {
  border-color: rgba(234, 179, 8, 0.5);
  background: linear-gradient(135deg, #FEFCE8 0%, #FEF9C3 100%);
}

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.device-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-icon.照明 {
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  color: #D97706;
}

.device-icon.空调 {
  background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
  color: #2563EB;
}

.device-icon.家电 {
  background: linear-gradient(135deg, #E0E7FF 0%, #C7D2FE 100%);
  color: #6366F1;
}

.device-icon.智能控制 {
  background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
  color: #059669;
}

.device-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-actions span {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.device-actions .online {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.device-actions .offline {
  background: rgba(107, 114, 128, 0.1);
  color: #6B7280;
}

.device-actions .warning {
  background: rgba(234, 179, 8, 0.1);
  color: #D97706;
}

.delete-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: none;
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  opacity: 0;
}

.device-card:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  transform: scale(1.1);
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 4px;
}

.device-type {
  font-size: 13px;
  color: #64748B;
  margin: 0;
}

.device-controls {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
}

.control-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.control-row:last-child {
  margin-bottom: 0;
}

.control-label {
  font-size: 13px;
  color: #64748B;
}

.temp-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.temp-control button {
  width: 32px;
  height: 32px;
  border-radius: 8px;
}

.temp-value {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  min-width: 50px;
  text-align: center;
}

.device-footer {
  margin-top: 12px;
}

.last-active {
  font-size: 12px;
  color: #94A3B8;
}

.quick-scenes {
  margin-bottom: 32px;
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.scene-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.scene-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.scene-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  color: #fff;
}

.scene-name {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 4px;
}

.scene-desc {
  font-size: 12px;
  color: #64748B;
  margin: 0 0 16px;
}

.scene-trigger {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(16, 185, 129, 0.1);
  border: none;
  color: #10B981;
  cursor: pointer;
  transition: all 0.3s ease;
}

.scene-trigger:hover {
  background: #10B981;
  color: #fff;
  transform: scale(1.1);
}

.view-all-btn {
  color: #10B981;
}

.add-device-dialog {
  border-radius: 20px;
}

@media screen and (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .section-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .search-input, .status-filter {
    width: 100%;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .device-grid {
    grid-template-columns: 1fr;
  }
  
  .scene-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>