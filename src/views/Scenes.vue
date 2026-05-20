<template>
  <div class="scenes">
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">智能场景</h1>
        <p class="page-subtitle">创建和管理您的智能场景</p>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canManageScenes"
          type="primary"
          class="add-scene-btn"
          @click="showAddScene = true"
        >
          <el-icon><Plus /></el-icon>
          <span>创建场景</span>
        </el-button>
        <el-tag v-else type="info">普通用户仅可触发场景</el-tag>
      </div>
    </div>

    <div class="scenes-grid">
      <div 
        v-for="scene in scenes" 
        :key="scene.id" 
        class="scene-card"
        :class="{ 'scene-active': scene.enabled }"
      >
        <div class="scene-header">
          <div class="scene-icon" :style="{ background: scene.bgColor }">
            <component v-if="scene.icon" :is="scene.icon" :size="32" />
            <el-icon v-else :size="32"><Sunny /></el-icon>
          </div>
          <div class="scene-status">
            <el-switch
              v-if="canManageScenes"
              v-model="scene.enabled"
              @change="toggleScene(scene)"
              active-color="#10B981"
              inactive-color="#94A3B8"
            />
          </div>
        </div>
        
        <div class="scene-content">
          <h3 class="scene-name">{{ scene.name }}</h3>
          <p class="scene-desc">{{ scene.description }}</p>
          
          <div class="scene-devices">
            <div 
              v-for="action in scene.actions.slice(0, 3)" 
              :key="action.deviceId"
              class="device-tag"
            >
              {{ getDeviceName(action.deviceId) }}
            </div>
            <span v-if="scene.actions.length > 3" class="more-devices">
              +{{ scene.actions.length - 3 }}
            </span>
          </div>
        </div>
        
        <div class="scene-footer">
          <div class="scene-time">
            <el-icon :size="14"><Clock /></el-icon>
            <span>{{ scene.triggerTime || '手动触发' }}</span>
          </div>
          <button class="trigger-btn" @click="triggerScene(scene)">
            <el-icon><VideoPlay /></el-icon>
            <span>触发</span>
          </button>
        </div>
        
        <div v-if="canManageScenes" class="scene-actions">
          <button class="action-btn edit" @click="editScene(scene)">
            <el-icon><Edit /></el-icon>
          </button>
          <button class="action-btn delete" @click="deleteScene(scene)">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="addQuickAction('all-on')">
          <div class="action-icon on">
            <el-icon :size="28"><Open /></el-icon>
          </div>
          <span class="action-name">全部开启</span>
        </div>
        <div class="action-card" @click="addQuickAction('all-off')">
          <div class="action-icon off">
            <el-icon :size="28"><TurnOff /></el-icon>
          </div>
          <span class="action-name">全部关闭</span>
        </div>
        <div class="action-card" @click="addQuickAction('light-on')">
          <div class="action-icon light">
            <el-icon :size="28"><Sunny /></el-icon>
          </div>
          <span class="action-name">打开灯光</span>
        </div>
        <div class="action-card" @click="addQuickAction('ac-on')">
          <div class="action-icon ac">
            <el-icon :size="28"><WindPower /></el-icon>
          </div>
          <span class="action-name">开启空调</span>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="showAddScene" 
      title="创建场景" 
      width="600px"
      class="scene-dialog"
    >
      <el-form :model="newScene" label-width="100px">
        <el-form-item label="场景名称">
          <el-input v-model="newScene.name" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input v-model="newScene.description" type="textarea" placeholder="请输入场景描述" />
        </el-form-item>
        <el-form-item label="场景图标">
          <div class="icon-selector">
            <button 
              v-for="icon in availableIcons" 
              :key="icon.name"
              class="icon-option"
              :class="{ 'icon-selected': newScene.iconName === icon.name }"
              @click="newScene.iconName = icon.name"
            >
              <component :is="icon.component" :size="24" />
            </button>
          </div>
        </el-form-item>
        <el-form-item label="触发方式">
          <el-select v-model="newScene.triggerType" placeholder="请选择触发方式">
            <el-option label="手动触发" value="manual" />
            <el-option label="定时触发" value="time" />
            <el-option label="设备触发" value="device" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发时间" v-if="newScene.triggerType === 'time'">
          <el-time-picker v-model="newScene.triggerTime" format="HH:mm" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="关联设备">
          <el-select v-model="newScene.selectedDevices" multiple placeholder="请选择设备">
            <el-option v-for="device in devices" :key="device.id" :label="device.name" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行动作">
          <div v-for="(deviceId, index) in newScene.selectedDevices" :key="deviceId" class="action-row">
            <span class="device-name">{{ getDeviceName(deviceId) }}</span>
            <el-select v-model="newScene.actions[index]" placeholder="选择动作">
              <el-option label="开启" value="on" />
              <el-option label="关闭" value="off" />
              <el-option label="调亮" value="brighten" />
              <el-option label="调暗" value="dim" />
            </el-select>
            <el-button 
              type="danger" 
              size="small" 
              circle
              @click="removeDeviceFromNewScene(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div v-if="newScene.selectedDevices.length === 0" class="no-devices">
            请在上方"关联设备"中选择设备
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddScene = false">取消</el-button>
        <el-button type="primary" @click="saveScene">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑场景对话框 -->
    <el-dialog 
      v-model="showEditScene" 
      title="编辑场景" 
      width="600px"
      class="scene-dialog"
    >
      <el-form :model="editSceneData" label-width="100px">
        <el-form-item label="场景名称">
          <el-input v-model="editSceneData.name" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input v-model="editSceneData.description" type="textarea" placeholder="请输入场景描述" />
        </el-form-item>
        <el-form-item label="场景图标">
          <div class="icon-selector">
            <button 
              v-for="icon in availableIcons" 
              :key="icon.name"
              class="icon-option"
              :class="{ 'icon-selected': editSceneData.iconName === icon.name }"
              @click="editSceneData.iconName = icon.name"
            >
              <component :is="icon.component" :size="24" />
            </button>
          </div>
        </el-form-item>
        <el-form-item label="触发方式">
          <el-select v-model="editSceneData.triggerType" placeholder="请选择触发方式">
            <el-option label="手动触发" value="manual" />
            <el-option label="定时触发" value="time" />
            <el-option label="设备触发" value="device" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发时间" v-if="editSceneData.triggerType === 'time'">
          <el-time-picker v-model="editSceneData.triggerTime" format="HH:mm" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="关联设备">
          <el-select v-model="editSceneData.selectedDevices" multiple placeholder="请选择设备">
            <el-option v-for="device in devices" :key="device.id" :label="device.name" :value="device.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行动作">
          <div v-for="(deviceId, index) in editSceneData.selectedDevices" :key="deviceId" class="action-row">
            <span class="device-name">{{ getDeviceName(deviceId) }}</span>
            <el-select v-model="editSceneData.actions[index]" placeholder="选择动作">
              <el-option label="开启" value="on" />
              <el-option label="关闭" value="off" />
              <el-option label="调亮" value="brighten" />
              <el-option label="调暗" value="dim" />
            </el-select>
            <el-button 
              type="danger" 
              size="small" 
              circle
              @click="removeDeviceFromScene(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div v-if="editSceneData.selectedDevices.length === 0" class="no-devices">
            请在上方"关联设备"中选择设备
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditScene = false">取消</el-button>
        <el-button type="primary" @click="saveEditedScene">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated, markRaw } from 'vue'
import { 
  Plus, VideoPlay, Clock, Edit, Delete, Open, TurnOff, Sunny, 
  WindPower, HomeFilled, Sunset, Moon, Coffee, Present, StarFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sceneApi } from '../api/scene'
import { useDeviceStore } from '../stores/devices'
import { useSceneStore } from '../stores/scenes'
import { storeToRefs } from 'pinia'
import { getDeviceId } from '../utils/deviceHelpers'
import { normalizeScene } from '../utils/sceneHelpers'
import { usePermissions } from '../composables/usePermissions'

const deviceStore = useDeviceStore()
const sceneStore = useSceneStore()
const { devices } = storeToRefs(deviceStore)
const { canManageScenes } = usePermissions()

const scenes = ref([])

const availableIcons = [
  { name: 'home', component: HomeFilled },
  { name: 'sunset', component: Sunset },
  { name: 'moon', component: Moon },
  { name: 'coffee', component: Coffee },
  { name: 'music', component: VideoPlay },
  { name: 'party', component: StarFilled },
  { name: 'power', component: Open },
  { name: 'light', component: Sunny },
  { name: 'ac', component: WindPower }
]

const iconColors = {
  'home': 'linear-gradient(135deg, #10B981 0%, #059669 100%)',
  'sunset': 'linear-gradient(135deg, #FB923C 0%, #F57C00 100%)',
  'moon': 'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)',
  'coffee': 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
  'music': 'linear-gradient(135deg, #EC4899 0%, #BE185D 100%)',
  'party': 'linear-gradient(135deg, #EC4899 0%, #BE185D 100%)',
  'power': 'linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%)',
  'light': 'linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%)',
  'ac': 'linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%)'
}

const showAddScene = ref(false)
const newScene = reactive({
  name: '',
  description: '',
  iconName: 'home',
  triggerType: 'manual',
  triggerTime: '',
  selectedDevices: [],
  actions: []
})

const getDeviceName = (deviceId) => {
  const device = devices.value.find(d => d.id === deviceId)
  return device ? device.name : deviceId
}

const toggleScene = async (scene) => {
  if (!scene.id || typeof scene.id !== 'number') {
    ElMessage.info(scene.enabled ? `${scene.name}已启用` : `${scene.name}已禁用`)
    return
  }
  try {
    await sceneStore.toggleScene(scene.id, scene.enabled)
    ElMessage.success(scene.enabled ? `${scene.name}已启用` : `${scene.name}已禁用`)
  } catch (error) {
    scene.enabled = !scene.enabled
    ElMessage.error('更新场景状态失败')
  }
}

const triggerScene = async (scene) => {
  ElMessage.info(`正在执行${scene.name}...`)

  if (scene.id && typeof scene.id === 'number') {
    try {
      await sceneStore.triggerSceneById(scene.id)
      ElMessage.success(`${scene.name}执行完成`)
      return
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || '场景执行失败')
      return
    }
  }

  const quick = sceneStore.quickSceneDefs.find(
    q => scene.name?.includes(q.name.replace('模式', ''))
  )
  if (quick) {
    const result = await sceneStore.triggerQuickScene(quick)
    ElMessage.success(result.ok ? `${scene.name}执行完成` : '场景执行失败')
    return
  }

  ElMessage.warning('该场景没有配置设备动作')
}

const deleteScene = async (scene) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除场景「${scene.name}」吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    try {
      if (scene.id && typeof scene.id === 'number') {
        await sceneApi.deleteScene(scene.id)
      }
    } catch (apiError) {
      console.warn('后端删除失败，仅删除本地记录')
    }
    
    const index = scenes.value.findIndex(s => s.id === scene.id)
    if (index > -1) {
      scenes.value.splice(index, 1)
      localStorage.setItem('scenes', JSON.stringify(scenes.value))
      ElMessage.success(`场景「${scene.name}」已删除`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.info('已取消删除')
    }
  }
}

const addQuickAction = async (action) => {
  if (devices.value.length === 0) {
    await deviceStore.fetchDevices(true)
  }
  if (devices.value.length === 0) {
    ElMessage.warning('暂无设备，请先添加设备')
    return
  }

  let targetDevices = []
  let actionType = ''
  let successMessage = ''

  switch (action) {
    case 'all-on':
      targetDevices = [...devices.value]
      actionType = 'on'
      successMessage = '已开启所有设备'
      break
    case 'all-off':
      targetDevices = [...devices.value]
      actionType = 'off'
      successMessage = '已关闭所有设备'
      break
    case 'light-on':
      targetDevices = devices.value.filter(d => d.type === '照明')
      actionType = 'on'
      successMessage = '已打开所有灯光'
      break
    case 'ac-on':
      targetDevices = devices.value.filter(d => d.type === '空调')
      actionType = 'on'
      successMessage = '已开启所有空调'
      break
    default:
      return
  }

  if (targetDevices.length === 0) {
    ElMessage.warning(`暂无${action === 'light-on' ? '灯光' : action === 'ac-on' ? '空调' : ''}设备`)
    return
  }

  ElMessage.info(`正在${actionType === 'on' ? '开启' : '关闭'}设备...`)

  let successCount = 0
  for (const device of targetDevices) {
    try {
      await deviceStore.setDevicePower(device, actionType === 'on')
      successCount++
    } catch (error) {
      console.warn(`控制设备 ${getDeviceId(device)} 失败`, error)
    }
  }
  await deviceStore.fetchDevices(true)

  if (successCount === targetDevices.length) {
    ElMessage.success(successMessage)
  } else if (successCount > 0) {
    ElMessage.success(`已${actionType === 'on' ? '开启' : '关闭'} ${successCount}/${targetDevices.length} 个设备`)
  } else {
    ElMessage.error('控制设备失败')
  }
}

const saveScene = async () => {
  if (!newScene.name) {
    ElMessage.warning('请输入场景名称')
    return
  }
  
  const sceneData = {
    name: newScene.name,
    description: newScene.description,
    iconName: newScene.iconName,
    bgColor: iconColors[newScene.iconName] || iconColors['home'],
    enabled: true,
    triggerTime: newScene.triggerTime,
    triggerType: newScene.triggerType,
    actions: newScene.selectedDevices.map((deviceId, index) => ({
      deviceId,
      action: newScene.actions[index] || 'on'
    }))
  }
  
  try {
    const response = await sceneApi.createScene(sceneData)
    const createdScene = response.data || response
    
    scenes.value.push({
      ...createdScene,
      icon: getIconComponent(createdScene.iconName)
    })
  } catch (error) {
    console.warn('后端创建失败，使用本地存储')
    const localId = 'local-' + Date.now()
    scenes.value.push({
      ...sceneData,
      id: localId,
      icon: getIconComponent(sceneData.iconName)
    })
  }
  
  localStorage.setItem('scenes', JSON.stringify(scenes.value))
  ElMessage.success('场景创建成功')
  showAddScene.value = false
  
  newScene.name = ''
  newScene.description = ''
  newScene.iconName = 'home'
  newScene.triggerType = 'manual'
  newScene.triggerTime = ''
  newScene.selectedDevices = []
  newScene.actions = []
}

// 从新场景中删除设备
const removeDeviceFromNewScene = (index) => {
  newScene.selectedDevices.splice(index, 1)
  newScene.actions.splice(index, 1)
}

// 从编辑场景中删除设备
const removeDeviceFromScene = (index) => {
  editSceneData.selectedDevices.splice(index, 1)
  editSceneData.actions.splice(index, 1)
}

const loadDevices = () => deviceStore.fetchDevices(true)

const getDeviceStatus = (deviceId) => {
  const device = devices.value.find(d => getDeviceId(d) === deviceId)
  return device?.status || 'offline'
}

// 编辑场景相关变量
const showEditScene = ref(false)
const editingScene = ref(null)
const editSceneData = reactive({
  name: '',
  description: '',
  iconName: 'home',
  triggerType: 'manual',
  triggerTime: '',
  selectedDevices: [],
  actions: []
})

const loadScenes = async () => {
  try {
    const data = await sceneApi.getScenes()
    if (data && data.data && Array.isArray(data.data)) {
      scenes.value = data.data.map(scene => ({
        ...scene,
        icon: getIconComponent(scene.iconName)
      }))
    } else if (Array.isArray(data)) {
      scenes.value = data.map(scene => ({
        ...normalizeScene(scene),
        icon: getIconComponent(scene.iconName)
      }))
    }
    localStorage.setItem('scenes', JSON.stringify(scenes.value))
  } catch (error) {
    console.warn('后端加载失败，使用本地存储')
    const stored = localStorage.getItem('scenes')
    if (stored) {
      try {
        const parsed = JSON.parse(stored)
        scenes.value = parsed.map(scene => ({
          ...scene,
          icon: getIconComponent(scene.iconName)
        }))
      } catch (parseError) {
        console.error('解析本地存储失败', parseError)
      }
    }
  }
}

const getIconComponent = (iconName) => {
  const icon = availableIcons.find(i => i.name === (iconName || 'home'))
  return icon ? markRaw(icon.component) : markRaw(HomeFilled)
}
const editScene = (scene) => {
  editingScene.value = scene
  editSceneData.name = scene.name
  editSceneData.description = scene.description
  editSceneData.iconName = scene.iconName || 'home'
  editSceneData.triggerType = scene.triggerType || 'manual'
  editSceneData.triggerTime = scene.triggerTime || ''
  editSceneData.selectedDevices = scene.actions.map(a => a.deviceId)
  editSceneData.actions = scene.actions.map(a => a.action)
  showEditScene.value = true
}

// 保存编辑的场景
const saveEditedScene = async () => {
  if (!editSceneData.name) {
    ElMessage.warning('请输入场景名称')
    return
  }
  
  const sceneData = {
    name: editSceneData.name,
    description: editSceneData.description,
    iconName: editSceneData.iconName,
    bgColor: iconColors[editSceneData.iconName] || iconColors['home'],
    enabled: scenes.value.find(s => s.id === editingScene.value.id)?.enabled || true,
    triggerTime: editSceneData.triggerTime,
    triggerType: editSceneData.triggerType,
    actions: editSceneData.selectedDevices.map((deviceId, idx) => ({
      deviceId,
      action: editSceneData.actions[idx] || 'on'
    }))
  }
  
  try {
    const sceneId = editingScene.value.id
    if (sceneId && typeof sceneId === 'number') {
      await sceneApi.updateScene(sceneId, sceneData)
    }
  } catch (error) {
    console.warn('后端更新失败，仅更新本地记录')
  }
  
  const index = scenes.value.findIndex(s => s.id === editingScene.value.id)
  if (index > -1) {
    scenes.value[index] = {
      ...scenes.value[index],
      ...sceneData,
      icon: getIconComponent(sceneData.iconName)
    }
  }
  
  localStorage.setItem('scenes', JSON.stringify(scenes.value))
  ElMessage.success('场景更新成功')
  showEditScene.value = false
}

const refreshPageData = async () => {
  await Promise.all([loadScenes(), loadDevices(), sceneStore.fetchScenes()])
}

onMounted(refreshPageData)
onActivated(refreshPageData)
</script>

<style scoped>
.scenes {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
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

.add-scene-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
}

.scenes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.scene-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  border: 2px solid transparent;
}

.scene-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.scene-active {
  border-color: rgba(16, 185, 129, 0.3);
}

.scene-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.scene-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.scene-content {
  margin-bottom: 20px;
}

.scene-name {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 8px;
}

.scene-desc {
  font-size: 14px;
  color: #64748B;
  margin: 0 0 16px;
}

.scene-devices {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.device-tag {
  padding: 4px 12px;
  background: #F1F5F9;
  border-radius: 20px;
  font-size: 12px;
  color: #64748B;
}

.more-devices {
  padding: 4px 12px;
  background: rgba(16, 185, 129, 0.1);
  border-radius: 20px;
  font-size: 12px;
  color: #10B981;
}

.scene-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
}

.scene-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #94A3B8;
}

.trigger-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(16, 185, 129, 0.1);
  border: none;
  border-radius: 8px;
  color: #10B981;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.trigger-btn:hover {
  background: #10B981;
  color: #fff;
}

.scene-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.scene-card:hover .scene-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn.edit {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.action-btn.edit:hover {
  background: #3B82F6;
  color: #fff;
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.action-btn.delete:hover {
  background: #EF4444;
  color: #fff;
}

.quick-actions {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.quick-actions h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 20px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: #F8FAFC;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon.on {
  background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
  color: #059669;
}

.action-icon.off {
  background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%);
  color: #6B7280;
}

.action-icon.light {
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  color: #D97706;
}

.action-icon.ac {
  background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
  color: #2563EB;
}

.action-name {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.scene-dialog {
  border-radius: 20px;
}

.icon-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.icon-option {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: 2px solid #E2E8F0;
  background: #F8FAFC;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #64748B;
}

.icon-option:hover {
  border-color: #10B981;
  background: rgba(16, 185, 129, 0.1);
}

.icon-selected {
  border-color: #10B981;
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #E2E8F0;
}

.action-row .device-name {
  flex: 1;
  font-size: 14px;
  color: #475569;
}

.action-row .el-select {
  width: 120px;
}

.no-devices {
  padding: 16px;
  text-align: center;
  color: #94A3B8;
  font-size: 14px;
  background: #F8FAFC;
  border-radius: 8px;
  margin-top: 8px;
}

@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .scenes-grid {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>