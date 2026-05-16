<template>
  <div class="admin-settings">
    <div class="page-header">
      <h1>管理员设置</h1>
      <el-radio-group v-model="activeTab" class="tab-switcher">
        <el-radio-button label="promotion">申请管理员</el-radio-button>
        <el-radio-button label="management">权限管理</el-radio-button>
        <el-radio-button label="audit">操作日志</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="activeTab === 'promotion'" class="tab-content">
      <div class="promotion-card">
        <div class="card-header">
          <h3>申请管理员权限</h3>
          <el-tag v-if="currentUserRole === 'admin'" type="success" effect="dark">您已是管理员</el-tag>
          <el-tag v-else type="info" effect="plain">普通用户</el-tag>
        </div>

        <div v-if="currentUserRole !== 'admin'" class="promotion-form">
          <el-form :model="promotionForm" label-width="120px" class="form-container">
            <el-form-item label="申请理由">
              <el-input
                v-model="promotionForm.reason"
                type="textarea"
                :rows="4"
                placeholder="请详细说明您需要管理员权限的原因..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="真实姓名">
              <el-input v-model="promotionForm.realName" placeholder="请输入您的真实姓名" />
            </el-form-item>

            <el-form-item label="工号/学号">
              <el-input v-model="promotionForm.employeeId" placeholder="请输入工号或学号" />
            </el-form-item>

            <el-form-item label="部门/班级">
              <el-input v-model="promotionForm.department" placeholder="请输入部门或班级" />
            </el-form-item>

            <el-form-item label="联系电话">
              <el-input v-model="promotionForm.phone" placeholder="请输入联系电话" />
            </el-form-item>

            <el-form-item label="申请附件">
              <el-upload
                ref="uploadRef"
                action="#"
                :auto-upload="false"
                :limit="3"
                accept=".jpg,.png,.pdf"
                list-type="picture-card"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持 JPG、PNG、PDF 格式，最多上传3个文件</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" @click="submitPromotion">
                <el-icon><Select /></el-icon>
                提交申请
              </el-button>
              <el-button size="large" @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-else class="current-admin">
          <div class="admin-info">
            <el-avatar :size="80" class="admin-avatar">
              {{ currentUserName.charAt(0) }}
            </el-avatar>
            <div class="admin-details">
              <h2>{{ currentUserName }}</h2>
              <p>管理员</p>
              <div class="admin-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ adminStats.approved }}</span>
                  <span class="stat-label">已审批</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ adminStats.pending }}</span>
                  <span class="stat-label">待处理</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ adminStats.denied }}</span>
                  <span class="stat-label">已拒绝</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="promotion-history">
        <div class="card-header">
          <h3>申请记录</h3>
        </div>
        <el-table :data="promotionHistory" stripe>
          <el-table-column prop="id" label="申请ID" width="100" />
          <el-table-column label="申请类型" width="120">
            <template #default="{ row }">
              <el-tag :type="row.type === 'promotion' ? 'warning' : 'info'" size="small">
                {{ row.type === 'promotion' ? '申请管理员' : '权限变更' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="申请理由" min-width="200" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="160" />
          <el-table-column prop="reviewTime" label="处理时间" width="160" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="viewDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div v-if="activeTab === 'management'" class="tab-content">
      <div class="permissions-card">
        <div class="card-header">
          <h3>权限说明</h3>
        </div>
        <div class="permissions-grid">
          <div class="permission-item">
            <div class="permission-icon admin">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="permission-info">
              <h4>管理员权限</h4>
              <ul>
                <li>用户管理（增删改查）</li>
                <li>系统设置管理</li>
                <li>查看所有设备数据</li>
                <li>管理其他用户角色</li>
                <li>查看操作日志</li>
                <li>系统配置修改</li>
              </ul>
            </div>
          </div>
          <div class="permission-item">
            <div class="permission-icon user">
              <el-icon><User /></el-icon>
            </div>
            <div class="permission-info">
              <h4>普通用户权限</h4>
              <ul>
                <li>查看自己的设备</li>
                <li>控制自己的设备</li>
                <li>创建和管理场景</li>
                <li>查看能耗统计</li>
                <li>修改个人资料</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="role-users-card">
        <div class="card-header">
          <h3>管理员列表</h3>
          <el-button type="primary" @click="refreshAdmins">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <el-table :data="adminUsers" stripe>
          <el-table-column label="用户信息" min-width="200">
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar :size="40">{{ row.name.charAt(0) }}</el-avatar>
                <div class="user-info">
                  <span class="user-name">{{ row.name }}</span>
                  <span class="user-email">{{ row.email }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="department" label="部门" width="150" />
          <el-table-column label="权限级别" width="120">
            <template #default="{ row }">
              <el-tag :type="row.level === 'super' ? 'danger' : 'warning'" size="small">
                {{ row.level === 'super' ? '超级管理员' : '普通管理员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                {{ row.status === 'active' ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="appointedTime" label="任职时间" width="120" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="editPermissions(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="revokeAdmin(row)">撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div v-if="activeTab === 'audit'" class="tab-content">
      <div class="audit-filters">
        <el-select v-model="auditFilter.user" placeholder="选择用户" clearable filterable>
          <el-option v-for="user in allUsers" :key="user.id" :label="user.name" :value="user.id" />
        </el-select>
        <el-select v-model="auditFilter.action" placeholder="操作类型" clearable>
          <el-option label="用户管理" value="user" />
          <el-option label="设备控制" value="device" />
          <el-option label="系统设置" value="system" />
          <el-option label="权限变更" value="permission" />
        </el-select>
        <el-date-picker
          v-model="auditFilter.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
        <el-button type="primary" @click="searchAudit">查询</el-button>
        <el-button @click="resetAudit">重置</el-button>
      </div>

      <div class="audit-list">
        <el-timeline>
          <el-timeline-item
            v-for="item in auditLogs"
            :key="item.id"
            :timestamp="item.timestamp"
            :type="getAuditType(item.action)"
            :hollow="item.action === 'view'"
            placement="top"
          >
            <el-card shadow="hover">
              <div class="audit-item">
                <div class="audit-header">
                  <span class="audit-user">{{ item.userName }}</span>
                  <el-tag size="small" :type="getActionType(item.action)">
                    {{ getActionText(item.action) }}
                  </el-tag>
                </div>
                <div class="audit-content">{{ item.content }}</div>
                <div class="audit-meta">
                  <span>IP地址: {{ item.ip }}</span>
                  <span>设备: {{ item.device }}</span>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" title="申请详情" width="600px">
      <div v-if="selectedRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ selectedRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="申请类型">
            <el-tag :type="selectedRecord.type === 'promotion' ? 'warning' : 'info'" size="small">
              {{ selectedRecord.type === 'promotion' ? '申请管理员' : '权限变更' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">{{ selectedRecord.realName }}</el-descriptions-item>
          <el-descriptions-item label="部门/班级">{{ selectedRecord.department }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedRecord.phone }}</el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(selectedRecord.status)" size="small">
              {{ getStatusText(selectedRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">{{ selectedRecord.reason }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ selectedRecord.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ selectedRecord.reviewTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Select, UserFilled, User, Refresh
} from '@element-plus/icons-vue'

const activeTab = ref('promotion')
const detailDialogVisible = ref(false)
const selectedRecord = ref(null)

const currentUserName = computed(() => localStorage.getItem('username') || '管理员')
const currentUserRole = computed(() => localStorage.getItem('role') || 'user')

const promotionForm = ref({
  reason: '',
  realName: '',
  employeeId: '',
  department: '',
  phone: '',
  files: []
})

const adminStats = ref({
  approved: 3,
  pending: 1,
  denied: 1
})

const promotionHistory = ref([
  { id: 'APP001', type: 'promotion', reason: '需要管理系统用户', status: 'approved', submitTime: '2024-03-01 10:30', reviewTime: '2024-03-01 14:20' },
  { id: 'APP002', type: 'permission', reason: '申请更高权限', status: 'pending', submitTime: '2024-03-15 09:00', reviewTime: '-' },
  { id: 'APP003', type: 'promotion', reason: '系统维护需要', status: 'denied', submitTime: '2024-02-20 11:00', reviewTime: '2024-02-21 09:30' }
])

const adminUsers = ref([
  { id: 1, name: '王五', email: 'wangwu@example.com', department: '技术部', level: 'super', status: 'active', appointedTime: '2024-01-15' },
  { id: 2, name: '吴九', email: 'wujiu@example.com', department: '运维部', level: 'admin', status: 'active', appointedTime: '2024-02-01' }
])

const allUsers = ref([
  { id: 1, name: '王五' },
  { id: 2, name: '吴九' },
  { id: 3, name: '张三' },
  { id: 4, name: '李四' }
])

const auditFilter = ref({
  user: '',
  action: '',
  dateRange: []
})

const auditLogs = ref([
  { id: 1, userName: '王五', action: 'user_create', content: '创建了新用户 陈七', timestamp: '2024-03-20 14:30:25', ip: '192.168.1.100', device: 'Chrome/Windows' },
  { id: 2, userName: '吴九', action: 'permission_change', content: '将用户 李四 角色变更为管理员', timestamp: '2024-03-20 14:25:10', ip: '192.168.1.101', device: 'Firefox/Windows' },
  { id: 3, userName: '王五', action: 'system_config', content: '修改了系统主题颜色配置', timestamp: '2024-03-20 14:20:00', ip: '192.168.1.100', device: 'Chrome/Windows' },
  { id: 4, userName: '张三', action: 'device_control', content: '打开了客厅灯', timestamp: '2024-03-20 14:15:30', ip: '192.168.1.102', device: 'Safari/iOS' },
  { id: 5, userName: '李四', action: 'view', content: '查看了用户管理页面', timestamp: '2024-03-20 14:10:15', ip: '192.168.1.103', device: 'Chrome/Android' }
])

const getStatusType = (status) => {
  const types = { approved: 'success', pending: 'warning', denied: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { approved: '已通过', pending: '待处理', denied: '已拒绝' }
  return texts[status] || status
}

const getAuditType = (action) => {
  if (action.includes('create')) return 'success'
  if (action.includes('delete') || action.includes('revoke')) return 'danger'
  if (action.includes('update') || action.includes('change')) return 'warning'
  return 'info'
}

const getActionType = (action) => {
  if (action.includes('create')) return 'success'
  if (action.includes('delete') || action.includes('revoke')) return 'danger'
  if (action.includes('update') || action.includes('change')) return 'warning'
  return 'info'
}

const getActionText = (action) => {
  const texts = {
    user_create: '创建用户',
    user_delete: '删除用户',
    permission_change: '权限变更',
    system_config: '系统配置',
    device_control: '设备控制',
    view: '浏览'
  }
  return texts[action] || action
}

const submitPromotion = () => {
  if (!promotionForm.value.reason || !promotionForm.value.realName) {
    ElMessage.warning('请填写完整的申请信息')
    return
  }
  ElMessage.success('申请已提交，请等待审批')
  activeTab.value = 'management'
}

const resetForm = () => {
  promotionForm.value = {
    reason: '',
    realName: '',
    employeeId: '',
    department: '',
    phone: '',
    files: []
  }
}

const refreshAdmins = () => {
  ElMessage.success('管理员列表已刷新')
}

const editPermissions = (row) => {
  ElMessageBox.alert(`编辑 ${row.name} 的权限`, '权限编辑', {
    confirmButtonText: '确定'
  })
}

const revokeAdmin = (row) => {
  ElMessageBox.confirm(
    `确定要撤销 ${row.name} 的管理员权限吗？`,
    '撤销确认',
    {
      confirmButtonText: '确定撤销',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success(`已撤销 ${row.name} 的管理员权限`)
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

const viewDetail = (row) => {
  selectedRecord.value = row
  detailDialogVisible.value = true
}

const searchAudit = () => {
  ElMessage.success('查询成功')
}

const resetAudit = () => {
  auditFilter.value = { user: '', action: '', dateRange: [] }
  ElMessage.info('筛选条件已重置')
}
</script>

<style scoped>
.admin-settings {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.tab-switcher {
  margin-bottom: 0;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.promotion-card, .promotion-history, .permissions-card, .role-users-card, .audit-list {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.form-container {
  max-width: 600px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.current-admin {
  padding: 20px 0;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.admin-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 32px;
  font-weight: 600;
}

.admin-details h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.admin-details p {
  margin: 0 0 16px 0;
  color: #909399;
}

.admin-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-email {
  font-size: 12px;
  color: #909399;
}

.permissions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.permission-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.permission-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.permission-icon.admin {
  background: #fff2f0;
  color: #ff4d4f;
}

.permission-icon.user {
  background: #f0f9ff;
  color: #409eff;
}

.permission-info h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.permission-info ul {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  color: #606266;
  line-height: 2;
}

.audit-filters {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.audit-item {
  padding: 8px 0;
}

.audit-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.audit-user {
  font-weight: 600;
  color: #303133;
}

.audit-content {
  color: #606266;
  margin-bottom: 8px;
}

.audit-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.detail-content {
  padding: 10px 0;
}

@media (max-width: 768px) {
  .permissions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
