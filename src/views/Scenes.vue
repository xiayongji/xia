<template>
  <div class="scenes">
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">智能场景</h1>
        <p class="page-subtitle">创建和管理您的智能场景</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="add-scene-btn" @click="showAddScene = true">
          <el-icon><Plus /></el-icon>
          <span>创建场景</span>
        </el-button>
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
        
        <div class="scene-actions">
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
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddScene = false">取消</el-button>
        <el-button type="primary" @click="saveScene">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  Plus, VideoPlay, Clock, Edit, Delete, Open, TurnOff, Sunny, 
  WindPower, HomeFilled, Sunset, Moon, Coffee, Present, StarFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const scenes = ref([
  {
    id: 'S001',
    name: '回家模式',
    description: '自动开启客厅灯光和空调，营造温馨氛围',
    icon: HomeFilled,
    bgColor: 'linear-gradient(135deg, #10B981 0%, #059669 100%)',
    enabled: true,
    triggerTime: '',
    actions: [
      { deviceId: 'D001', action: 'on' },
      { deviceId: 'D003', action: 'on' },
      { deviceId: 'D006', action: 'on' }
    ]
  },
  {
    id: 'S002',
    name: '睡眠模式',
    description: '关闭所有灯光，调暗氛围灯',
    icon: Moon,
    bgColor: 'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)',
    enabled: true,
    triggerTime: '22:00',
    actions: [
      { deviceId: 'D001', action: 'off' },
      { deviceId: 'D002', action: 'off' },
      { deviceId: 'D003', action: 'off' }
    ]
  },
  {
    id: 'S003',
    name: '离家模式',
    description: '关闭所有设备，启动安防系统',
    icon: TurnOff,
    bgColor: 'linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%)',
    enabled: true,
    triggerTime: '',
    actions: [
      { deviceId: 'D001', action: 'off' },
      { deviceId: 'D002', action: 'off' },
      { deviceId: 'D003', action: 'off' },
      { deviceId: 'D004', action: 'off' },
      { deviceId: 'D006', action: 'off' }
    ]
  },
  {
    id: 'S004',
    name: '阅读模式',
    description: '调节灯光亮度至舒适阅读水平',
    icon: Coffee,
    bgColor: 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
    enabled: false,
    triggerTime: '',
    actions: [
      { deviceId: 'D002', action: 'dim' }
    ]
  },
  {
    id: 'S005',
    name: '聚会模式',
    description: '开启氛围灯和音乐系统',
    icon: StarFilled,
    bgColor: 'linear-gradient(135deg, #EC4899 0%, #BE185D 100%)',
    enabled: false,
    triggerTime: '',
    actions: [
      { deviceId: 'D001', action: 'on' },
      { deviceId: 'D006', action: 'on' }
    ]
  },
  {
    id: 'S006',
    name: '日出模式',
    description: '清晨逐渐开启灯光',
    icon: Sunset,
    bgColor: 'linear-gradient(135deg, #FB923C 0%, #F57C00 100%)',
    enabled: true,
    triggerTime: '07:00',
    actions: [
      { deviceId: 'D002', action: 'brighten' }
    ]
  }
])

const devices = ref([
  { id: 'D001', name: '客厅灯', type: '照明' },
  { id: 'D002', name: '卧室灯', type: '照明' },
  { id: 'D003', name: '客厅空调', type: '空调' },
  { id: 'D004', name: '洗衣机', type: '家电' },
  { id: 'D005', name: '冰箱', type: '家电' },
  { id: 'D006', name: '客厅窗帘', type: '智能控制' }
])

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

const toggleScene = (scene) => {
  if (scene.enabled) {
    ElMessage.success(`${scene.name}已启用`)
  } else {
    ElMessage.info(`${scene.name}已禁用`)
  }
}

const triggerScene = (scene) => {
  ElMessage.success(`${scene.name}已触发`)
}

const editScene = (scene) => {
  ElMessage.info(`编辑场景: ${scene.name}`)
}

const deleteScene = (scene) => {
  const index = scenes.value.findIndex(s => s.id === scene.id)
  if (index > -1) {
    scenes.value.splice(index, 1)
    ElMessage.success(`场景已删除`)
  }
}

const addQuickAction = (action) => {
  const actions = {
    'all-on': '已开启所有设备',
    'all-off': '已关闭所有设备',
    'light-on': '已打开所有灯光',
    'ac-on': '已开启所有空调'
  }
  ElMessage.success(actions[action])
}

const saveScene = () => {
  if (!newScene.name) {
    ElMessage.warning('请输入场景名称')
    return
  }
  
  const icon = availableIcons.find(i => i.name === newScene.iconName)
  const newSceneItem = {
    id: 'S' + String(scenes.value.length + 1).padStart(3, '0'),
    name: newScene.name,
    description: newScene.description,
    icon: icon ? icon.component : HomeFilled,
    bgColor: iconColors[newScene.iconName] || iconColors['home'],
    enabled: true,
    triggerTime: newScene.triggerTime,
    actions: newScene.selectedDevices.map((deviceId, index) => ({
      deviceId,
      action: newScene.actions[index] || 'on'
    }))
  }
  
  scenes.value.push(newSceneItem)
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

onMounted(() => {
})
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
  padding: 8px 0;
  border-bottom: 1px solid #E2E8F0;
}

.action-row .device-name {
  font-size: 14px;
  color: #475569;
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