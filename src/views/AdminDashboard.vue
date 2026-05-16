<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>管理控制台</h1>
      <div class="header-stats">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalUsers }}</span>
            <span class="stat-label">总用户数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalDevices }}</span>
            <span class="stat-label">设备总数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.todayEnergy }} kWh</span>
            <span class="stat-label">今日能耗</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon red">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.alerts }}</span>
            <span class="stat-label">待处理告警</span>
          </div>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="content-grid">
        <div class="card user-activity">
          <div class="card-header">
            <h3>用户活动趋势</h3>
            <el-radio-group v-model="activityRange" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon :size="48"><DataLine /></el-icon>
              <span>用户活动趋势图表</span>
            </div>
          </div>
        </div>

        <div class="card system-status">
          <div class="card-header">
            <h3>系统状态</h3>
            <el-tag :type="systemHealth === 'healthy' ? 'success' : 'danger'">
              {{ systemHealth === 'healthy' ? '运行正常' : '异常' }}
            </el-tag>
          </div>
          <div class="status-list">
            <div class="status-item">
              <span class="status-label">API 服务</span>
              <el-switch v-model="services.api" active-color="#13ce66" />
            </div>
            <div class="status-item">
              <span class="status-label">数据库</span>
              <el-switch v-model="services.database" active-color="#13ce66" />
            </div>
            <div class="status-item">
              <span class="status-label">Redis 缓存</span>
              <el-switch v-model="services.redis" active-color="#13ce66" />
            </div>
            <div class="status-item">
              <span class="status-label">消息队列</span>
              <el-switch v-model="services.mq" active-color="#13ce66" />
            </div>
          </div>
        </div>

        <div class="card recent-users">
          <div class="card-header">
            <h3>最近用户</h3>
            <el-button type="primary" size="small" text @click="$router.push('/users')">
              管理用户
            </el-button>
          </div>
          <div class="user-list">
            <div v-for="user in recentUsers" :key="user.id" class="user-item">
              <el-avatar :size="32">{{ user.name.charAt(0) }}</el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ user.name }}</span>
                <span class="user-time">{{ user.lastActive }}</span>
              </div>
              <el-tag size="small" :type="user.role === 'admin' ? 'danger' : 'info'">
                {{ user.role === 'admin' ? '管理员' : '用户' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="card alerts-panel">
          <div class="card-header">
            <h3>实时告警</h3>
            <el-badge :value="alerts.length" class="alert-badge">
              <el-button size="small">查看全部</el-button>
            </el-badge>
          </div>
          <div class="alerts-list">
            <div v-for="alert in alerts" :key="alert.id" class="alert-item">
              <div class="alert-icon" :class="alert.level">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="alert-content">
                <span class="alert-title">{{ alert.title }}</span>
                <span class="alert-time">{{ alert.time }}</span>
              </div>
              <el-button size="small" type="primary" text>处理</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { User, Monitor, Lightning, Warning, DataLine } from '@element-plus/icons-vue'

const activityRange = ref('week')
const systemHealth = ref('healthy')

const services = ref({
  api: true,
  database: true,
  redis: true,
  mq: true
})

const stats = ref({
  totalUsers: 156,
  totalDevices: 423,
  todayEnergy: 128.5,
  alerts: 3
})

const recentUsers = ref([
  { id: 1, name: '张三', role: 'user', lastActive: '2分钟前' },
  { id: 2, name: '李四', role: 'user', lastActive: '5分钟前' },
  { id: 3, name: '王五', role: 'admin', lastActive: '10分钟前' },
  { id: 4, name: '赵六', role: 'user', lastActive: '30分钟前' }
])

const alerts = ref([
  { id: 1, level: 'warning', title: '设备 D005 能耗异常', time: '10分钟前' },
  { id: 2, level: 'info', title: '新用户注册: 陈七', time: '30分钟前' },
  { id: 3, level: 'danger', title: '空调设备连接中断', time: '1小时前' }
])
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.dashboard-header h1 {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.blue { background: #e6f0ff; color: #409eff; }
.stat-icon.green { background: #e6f7ff; color: #13ce66; }
.stat-icon.orange { background: #fff7e6; color: #ff9800; }
.stat-icon.red { background: #ffebee; color: #f56c6c; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: auto auto;
  gap: 20px;
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

.chart-container {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #909399;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.status-label {
  font-size: 14px;
  color: #606266;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.user-time {
  font-size: 12px;
  color: #909399;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.alert-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.alert-icon.warning { background: #fff7e6; color: #ff9800; }
.alert-icon.info { background: #e6f0ff; color: #409eff; }
.alert-icon.danger { background: #ffebee; color: #f56c6c; }

.alert-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.alert-title {
  font-size: 14px;
  color: #303133;
}

.alert-time {
  font-size: 12px;
  color: #909399;
}

.user-activity {
  grid-column: 1;
  grid-row: 1;
}

.system-status {
  grid-column: 2;
  grid-row: 1;
}

.recent-users {
  grid-column: 1;
  grid-row: 2;
}

.alerts-panel {
  grid-column: 2;
  grid-row: 2;
}

@media (max-width: 1200px) {
  .header-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .card {
    grid-column: 1 !important;
    grid-row: auto !important;
  }
}
</style>
