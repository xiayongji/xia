<template>
  <div class="settings">
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">系统设置</h1>
        <p class="page-subtitle">自定义您的智能家居体验</p>
      </div>
    </div>

    <div class="settings-grid">
      <div class="settings-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><Brush /></el-icon>
          </div>
          <h3>主题设置</h3>
        </div>
        <div class="card-content">
          <div class="setting-item">
            <label class="setting-label">主题颜色</label>
            <div class="color-picker-wrapper">
              <el-color-picker 
                v-model="themeColor" 
                @change="updateTheme"
                class="color-picker"
              />
              <div class="preset-colors">
                <button 
                  v-for="color in presetColors" 
                  :key="color.value"
                  class="preset-color"
                  :style="{ background: color.value }"
                  @click="selectPresetColor(color.value)"
                ></button>
              </div>
            </div>
          </div>
          
          <div class="setting-item">
            <label class="setting-label">布局风格</label>
            <div class="style-options">
              <label class="style-option">
                <input type="radio" v-model="layoutStyle" value="light" />
                <div class="style-preview light">
                  <div class="preview-header"></div>
                  <div class="preview-body">
                    <div class="preview-item"></div>
                    <div class="preview-item"></div>
                  </div>
                </div>
                <span>浅色模式</span>
              </label>
              <label class="style-option">
                <input type="radio" v-model="layoutStyle" value="dark" />
                <div class="style-preview dark">
                  <div class="preview-header"></div>
                  <div class="preview-body">
                    <div class="preview-item"></div>
                    <div class="preview-item"></div>
                  </div>
                </div>
                <span>深色模式</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="settings-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><Grid /></el-icon>
          </div>
          <h3>设备分组</h3>
        </div>
        <div class="card-content">
          <div class="groups-list">
            <div 
              v-for="group in deviceGroups" 
              :key="group.id" 
              class="group-item"
            >
              <div class="group-info">
                <div class="group-icon">
                  <el-icon :size="20"><HomeFilled /></el-icon>
                </div>
                <div class="group-details">
                  <span class="group-name">{{ group.name }}</span>
                  <span class="group-count">{{ group.deviceCount }} 台设备</span>
                </div>
              </div>
              <div class="group-actions">
                <button class="action-btn" @click="editGroup(group)">
                  <el-icon><Edit /></el-icon>
                </button>
                <button class="action-btn" @click="deleteGroup(group)">
                  <el-icon><Delete /></el-icon>
                </button>
              </div>
            </div>
          </div>
          <button class="add-group-btn" @click="addGroup">
            <el-icon><Plus /></el-icon>
            <span>添加分组</span>
          </button>
        </div>
      </div>

      <div class="settings-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><Bell /></el-icon>
          </div>
          <h3>通知设置</h3>
        </div>
        <div class="card-content">
          <div class="notification-item">
            <div class="notification-info">
              <h4>设备离线通知</h4>
              <p>当设备离线时发送通知</p>
            </div>
            <el-switch v-model="notifications.deviceOffline" active-color="#10B981" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <h4>能耗异常通知</h4>
              <p>当能耗异常时发送通知</p>
            </div>
            <el-switch v-model="notifications.energyAlert" active-color="#10B981" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <h4>场景触发通知</h4>
              <p>当场景触发时发送通知</p>
            </div>
            <el-switch v-model="notifications.sceneTrigger" active-color="#10B981" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <h4>系统更新通知</h4>
              <p>当有系统更新时发送通知</p>
            </div>
            <el-switch v-model="notifications.systemUpdate" active-color="#10B981" />
          </div>
        </div>
      </div>

      <div class="settings-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><Checked /></el-icon>
          </div>
          <h3>安全设置</h3>
        </div>
        <div class="card-content">
          <div class="security-item">
            <div class="security-info">
              <h4>双重验证</h4>
              <p>登录时需要额外验证</p>
            </div>
            <el-switch v-model="security.twoFactor" active-color="#10B981" />
          </div>
          <div class="security-item">
            <div class="security-info">
              <h4>登录提醒</h4>
              <p>每次登录发送提醒通知</p>
            </div>
            <el-switch v-model="security.loginAlert" active-color="#10B981" />
          </div>
          <div class="security-item">
            <div class="security-info">
              <h4>自动锁定</h4>
              <p>闲置一段时间后自动锁定</p>
            </div>
            <el-switch v-model="security.autoLock" active-color="#10B981" />
          </div>
          <div class="security-item" v-if="security.autoLock">
            <label class="setting-label">锁定时间</label>
            <el-select v-model="security.lockTime" placeholder="选择时间">
              <el-option label="5分钟" value="5" />
              <el-option label="10分钟" value="10" />
              <el-option label="15分钟" value="15" />
              <el-option label="30分钟" value="30" />
            </el-select>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Brush, Grid, Bell, Checked, HomeFilled, Edit, Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const themeColor = ref('#10B981')
const layoutStyle = ref('light')

const presetColors = [
  { value: '#10B981', name: '翡翠绿' },
  { value: '#3B82F6', name: '蔚蓝' },
  { value: '#8B5CF6', name: '紫罗兰' },
  { value: '#EC4899', name: '玫瑰红' },
  { value: '#F59E0B', name: '琥珀橙' },
  { value: '#EF4444', name: '石榴红' }
]

const deviceGroups = ref([
  { id: 'G001', name: '客厅', deviceCount: 3 },
  { id: 'G002', name: '卧室', deviceCount: 2 },
  { id: 'G003', name: '厨房', deviceCount: 1 },
  { id: 'G004', name: '卫生间', deviceCount: 1 }
])

const notifications = reactive({
  deviceOffline: true,
  energyAlert: true,
  sceneTrigger: false,
  systemUpdate: true
})

const security = reactive({
  twoFactor: false,
  loginAlert: true,
  autoLock: true,
  lockTime: '10'
})

const updateTheme = (color) => {
  const root = document.documentElement
  root.style.setProperty('--el-color-primary', color)
  root.style.setProperty('--el-color-primary-light-3', lighten(color, 30))
  root.style.setProperty('--el-color-primary-light-5', lighten(color, 50))
  root.style.setProperty('--el-color-primary-light-7', lighten(color, 70))
  root.style.setProperty('--el-color-primary-light-9', lighten(color, 90))
  root.style.setProperty('--el-color-primary-dark-2', darken(color, 15))
  
  root.style.setProperty('--el-color-primary', color, 'important')
  root.style.setProperty('--el-color-primary-light-3', lighten(color, 30), 'important')
  root.style.setProperty('--el-color-primary-light-5', lighten(color, 50), 'important')
  root.style.setProperty('--el-color-primary-light-7', lighten(color, 70), 'important')
  root.style.setProperty('--el-color-primary-light-9', lighten(color, 90), 'important')
  root.style.setProperty('--el-color-primary-dark-2', darken(color, 15), 'important')
  
  const styleEl = document.getElementById('theme-override')
  if (styleEl) {
    styleEl.remove()
  }
  
  const style = document.createElement('style')
  style.id = 'theme-override'
  style.innerHTML = `
    .el-button--primary {
      --el-button-bg-color: ${color} !important;
      --el-button-border-color: ${color} !important;
      --el-button-hover-bg-color: ${lighten(color, 15)} !important;
      --el-button-hover-border-color: ${lighten(color, 15)} !important;
      --el-button-active-bg-color: ${darken(color, 10)} !important;
      --el-button-active-border-color: ${darken(color, 10)} !important;
    }
    .el-menu-item.is-active {
      background: linear-gradient(135deg, ${color}33 0%, ${color}22 100%) !important;
      color: ${color} !important;
    }
    .el-switch.is-checked .el-switch__core {
      background-color: ${color} !important;
      border-color: ${color} !important;
    }
    .el-slider__bar {
      background-color: ${color} !important;
    }
    .el-slider__button {
      border-color: ${color} !important;
    }
  `
  document.head.appendChild(style)
  
  localStorage.setItem('themeColor', color)
  ElMessage.success('主题颜色已更新')
}

const hexToRgb = (hex) => {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : null
}

const lighten = (color, percent) => {
  const rgb = hexToRgb(color)
  if (!rgb) return color
  const factor = percent / 100
  const r = Math.round(rgb.r + (255 - rgb.r) * factor)
  const g = Math.round(rgb.g + (255 - rgb.g) * factor)
  const b = Math.round(rgb.b + (255 - rgb.b) * factor)
  return `rgb(${r}, ${g}, ${b})`
}

const darken = (color, percent) => {
  const rgb = hexToRgb(color)
  if (!rgb) return color
  const factor = percent / 100
  const r = Math.round(rgb.r * (1 - factor))
  const g = Math.round(rgb.g * (1 - factor))
  const b = Math.round(rgb.b * (1 - factor))
  return `rgb(${r}, ${g}, ${b})`
}

const selectPresetColor = (color) => {
  themeColor.value = color
  updateTheme(color)
}

const addGroup = () => {
  ElMessage.info('打开添加分组对话框')
}

const editGroup = (group) => {
  ElMessage.info(`编辑分组: ${group.name}`)
}

const deleteGroup = (group) => {
  const index = deviceGroups.value.findIndex(g => g.id === group.id)
  if (index > -1) {
    deviceGroups.value.splice(index, 1)
    ElMessage.success('分组已删除')
  }
}

onMounted(() => {
  const savedColor = localStorage.getItem('themeColor')
  if (savedColor) {
    themeColor.value = savedColor
  }
})
</script>

<style scoped>
.settings {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
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

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.settings-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(16, 185, 129, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10B981;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.card-content {
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
}

.setting-item {
  margin-bottom: 20px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 12px;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.color-picker {
  width: 100px;
}

.preset-colors {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.preset-color {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preset-color:hover {
  transform: scale(1.1);
  border-color: #10B981;
}

.style-options {
  display: flex;
  gap: 20px;
}

.style-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.style-option input {
  display: none;
}

.style-preview {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  padding: 8px;
  transition: all 0.3s ease;
}

.style-option input:checked + .style-preview {
  border: 2px solid #10B981;
}

.style-preview.light {
  background: #F8FAFC;
}

.style-preview.light .preview-header {
  background: #fff;
  height: 20px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.style-preview.light .preview-body {
  display: flex;
  gap: 8px;
}

.style-preview.light .preview-item {
  flex: 1;
  height: 40px;
  background: #fff;
  border-radius: 6px;
}

.style-preview.dark {
  background: #1E293B;
}

.style-preview.dark .preview-header {
  background: #334155;
  height: 20px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.style-preview.dark .preview-body {
  display: flex;
  gap: 8px;
}

.style-preview.dark .preview-item {
  flex: 1;
  height: 40px;
  background: #334155;
  border-radius: 6px;
}

.style-option span {
  font-size: 13px;
  color: #64748B;
}

.groups-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 12px;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.group-icon {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: rgba(16, 185, 129, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10B981;
}

.group-details {
  display: flex;
  flex-direction: column;
}

.group-name {
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
}

.group-count {
  font-size: 12px;
  color: #64748B;
}

.group-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-actions .action-btn:first-child {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.group-actions .action-btn:first-child:hover {
  background: #3B82F6;
  color: #fff;
}

.group-actions .action-btn:last-child {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.group-actions .action-btn:last-child:hover {
  background: #EF4444;
  color: #fff;
}

.add-group-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px dashed #10B981;
  border-radius: 12px;
  color: #10B981;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-group-btn:hover {
  background: rgba(16, 185, 129, 0.2);
}

.notification-item,
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 12px;
  margin-bottom: 12px;
}

.notification-item:last-child,
.security-item:last-child {
  margin-bottom: 0;
}

.notification-info,
.security-info {
  flex: 1;
}

.notification-info h4,
.security-info h4 {
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
  margin: 0 0 4px;
}

.notification-info p,
.security-info p {
  font-size: 12px;
  color: #64748B;
  margin: 0;
}

@media screen and (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
  
  .style-options {
    flex-direction: column;
  }
  
  .color-picker-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>