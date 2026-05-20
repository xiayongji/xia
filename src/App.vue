<template>
  <div class="app-container">
    <el-container class="main-container">
      <el-aside v-show="hasToken" :width="sidebarCollapsed ? '64px' : '240px'" class="sidebar">
        <div class="sidebar-header">
          <div class="logo-wrapper" @click="toggleSidebar">
            <div class="logo-icon">
              <el-icon :size="28"><HomeFilled /></el-icon>
            </div>
            <span v-show="!sidebarCollapsed" class="logo-text">美智家</span>
          </div>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          mode="vertical"
          @select="handleMenuSelect"
        >
          <!-- 业务模块：管理员与普通用户均可使用 -->
          <el-menu-item index="/user">
            <el-icon :size="22"><HomeFilled /></el-icon>
            <span v-show="!sidebarCollapsed">我的主页</span>
          </el-menu-item>
          <el-menu-item index="/dashboard">
            <el-icon :size="22"><Monitor /></el-icon>
            <span v-show="!sidebarCollapsed">设备监控</span>
          </el-menu-item>
          <el-menu-item index="/scenes">
            <el-icon :size="22"><Collection /></el-icon>
            <span v-show="!sidebarCollapsed">智能场景</span>
          </el-menu-item>
          <el-menu-item index="/energy">
            <el-icon :size="22"><Lightning /></el-icon>
            <span v-show="!sidebarCollapsed">能耗统计</span>
          </el-menu-item>
          <!-- 仅管理员：用户/角色/日志等管理功能 -->
          <el-menu-item v-if="isAdmin" index="/admin">
            <el-icon :size="22"><DataBoard /></el-icon>
            <span v-show="!sidebarCollapsed">管理控制台</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/settings">
            <el-icon :size="22"><Setting /></el-icon>
            <span v-show="!sidebarCollapsed">系统设置</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon :size="22"><UserFilled /></el-icon>
            <span v-show="!sidebarCollapsed">个人中心</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer" v-show="!sidebarCollapsed && hasToken">
          <div class="user-avatar-wrapper">
            <el-avatar :size="40" class="user-avatar" :style="{ background: isAdmin ? '#f56c6c' : '#409eff' }">
              {{ userName.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <span class="user-name">{{ userName }}</span>
              <el-tag size="small" :type="isAdmin ? 'danger' : 'primary'" effect="dark">
                {{ isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </div>
          </div>
          <el-button type="primary" link class="logout-btn" @click.stop="handleLogout">
            <el-icon><TurnOff /></el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </el-aside>
      
      <el-container class="main-content-container">
        <el-header v-show="hasToken" class="header">
          <div class="header-left">
            <button class="sidebar-toggle" @click="toggleSidebar">
              <el-icon :size="24"><Menu /></el-icon>
            </button>
            <h2 class="page-title">{{ pageTitle }}</h2>
          </div>
          <div class="header-right">
            <div class="header-actions">
              <el-badge :value="notificationCount" class="notification-badge">
                <button class="action-btn">
                  <el-icon :size="22"><Bell /></el-icon>
                </button>
              </el-badge>
              <button class="action-btn">
                <el-icon :size="22"><Search /></el-icon>
              </button>
              <div class="weather-info">
                <el-icon :size="18"><Sunny /></el-icon>
                <span>26°C</span>
              </div>
            </div>
          </div>
        </el-header>
        
        <el-main class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { 
  HomeFilled, 
  Collection, 
  Lightning, 
  Setting, 
  User, 
  TurnOff, 
  Menu, 
  Bell, 
  Search, 
  Sunny,
  Monitor,
  DataBoard,
  UserFilled,
  Tools
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const sidebarCollapsed = ref(false)
const notificationCount = ref(3)

const authStore = useAuthStore()
const { token, user, role } = storeToRefs(authStore)

const activeMenu = computed(() => route.path)
const hasToken = computed(() => !!token.value)
const userName = computed(() => user.value?.username || localStorage.getItem('username') || '用户')
const isAdmin = computed(() => role.value === 'admin')

onMounted(() => {
  authStore.syncFromStorage()
})

watch(
  () => route.path,
  () => {
    if (localStorage.getItem('token') && !token.value) {
      authStore.syncFromStorage()
    }
  }
)

const pageTitles = {
  '/dashboard': '设备监控',
  '/admin': '管理控制台',
  '/user': '我的主页',
  '/users': '用户管理',
  '/admin-settings': '管理员设置',
  '/scenes': '智能场景',
  '/energy': '能耗统计',
  '/settings': '系统设置',
  '/profile': '个人中心'
}

const pageTitle = computed(() => pageTitles[route.path] || '智能家居')

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleMenuSelect = (key) => {
  router.push(key)
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出登录',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )

    await authStore.logout()
    localStorage.removeItem('themeColor')
    sessionStorage.clear()

    ElMessage.success('退出成功')

    setTimeout(() => {
      window.location.replace('/login')
    }, 300)
  } catch (error) {
    ElMessage.info('已取消退出')
  }
}

const initTheme = () => {
  const savedColor = localStorage.getItem('themeColor') || '#10B981'
  
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

  const root = document.documentElement
  root.style.setProperty('--el-color-primary', savedColor, 'important')
  root.style.setProperty('--el-color-primary-light-3', lighten(savedColor, 30), 'important')
  root.style.setProperty('--el-color-primary-light-5', lighten(savedColor, 50), 'important')
  root.style.setProperty('--el-color-primary-light-7', lighten(savedColor, 70), 'important')
  root.style.setProperty('--el-color-primary-light-9', lighten(savedColor, 90), 'important')
  root.style.setProperty('--el-color-primary-dark-2', darken(savedColor, 15), 'important')

  const styleEl = document.getElementById('theme-override')
  if (styleEl) {
    styleEl.remove()
  }
  
  const style = document.createElement('style')
  style.id = 'theme-override'
  style.innerHTML = `
    .el-button--primary {
      --el-button-bg-color: ${savedColor} !important;
      --el-button-border-color: ${savedColor} !important;
      --el-button-hover-bg-color: ${lighten(savedColor, 15)} !important;
      --el-button-hover-border-color: ${lighten(savedColor, 15)} !important;
      --el-button-active-bg-color: ${darken(savedColor, 10)} !important;
      --el-button-active-border-color: ${darken(savedColor, 10)} !important;
    }
    .el-menu-item.is-active {
      background: linear-gradient(135deg, ${savedColor}33 0%, ${savedColor}22 100%) !important;
      color: ${savedColor} !important;
    }
    .el-switch.is-checked .el-switch__core {
      background-color: ${savedColor} !important;
      border-color: ${savedColor} !important;
    }
    .el-slider__bar {
      background-color: ${savedColor} !important;
    }
    .el-slider__button {
      border-color: ${savedColor} !important;
    }
  `
  document.head.appendChild(style)
}

onMounted(() => {
  initTheme()
})

watch(route, () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.main-container {
  height: 100%;
}

.sidebar {
  background: linear-gradient(180deg, #0F172A 0%, #1E293B 100%);
  color: #E2E8F0;
  transition: width 0.3s ease;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 100;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid #334155;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-menu {
  border: none;
  background: transparent;
  padding: 16px 0;
}

.sidebar-menu .el-menu-item {
  margin: 8px 12px;
  padding: 14px 16px;
  border-radius: 12px;
  color: #CBD5E1;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(16, 185, 129, 0.15);
  color: #34D399;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.25) 0%, rgba(5, 150, 105, 0.2) 100%);
  color: #10B981;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.sidebar-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  border-top: 1px solid #334155;
  background: rgba(15, 23, 42, 0.8);
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #F1F5F9;
}

.user-role {
  font-size: 12px;
  color: #94A3B8;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  color: #EF4444;
  border-radius: 8px;
  transition: background 0.3s ease;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.1);
}

.main-content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 90;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle {
  background: transparent;
  border: none;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  color: #475569;
  transition: background 0.3s ease;
}

.sidebar-toggle:hover {
  background: rgba(16, 185, 129, 0.1);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  background: transparent;
  border: none;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  color: #64748B;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #F1F5F9;
  color: #1E293B;
}

.notification-badge {
  --el-badge-bg-color: #EF4444;
}

.weather-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  border-radius: 20px;
  color: #92400E;
  font-size: 14px;
}

.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: transparent;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media screen and (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 200;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .header {
    padding: 0 16px;
  }

  .page-title {
    font-size: 18px;
  }

  .content-wrapper {
    padding: 16px;
  }

  .weather-info span {
    display: none;
  }
}
</style>