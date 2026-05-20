<template>
  <div class="user-management">
    <div class="page-header">
      <h1>用户管理</h1>
      <div class="header-actions">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户..."
          prefix-icon="Search"
          clearable
          style="width: 240px;"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
    </div>

    <div class="users-table">
      <el-table :data="filteredUsers" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="40">{{ (row.username || row.name || '').charAt(0) }}</el-avatar>
              <div class="user-info">
                <span class="user-name">{{ row.username || row.name }}</span>
                <span class="user-email">{{ row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="(row.role === 'admin' || row.role === 'ROLE_ADMIN') ? 'danger' : 'info'" size="small">
              {{ (row.role === 'admin' || row.role === 'ROLE_ADMIN') ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'warning'" size="small">
              {{ row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="设备数" width="100" align="center">
          <template #default="{ row }">
            <span class="device-count">{{ row.deviceCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="120">
          <template #default="{ row }">
            <span class="time-text">{{ row.createdAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="120">
          <template #default="{ row }">
            <span class="time-text">{{ row.lastLogin }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.role !== 'admin' && row.role !== 'ROLE_ADMIN'"
              type="danger"
              size="small"
              text
              @click="deleteUser(row)"
            >
              删除
            </el-button>
            <el-button
              :type="row.status === 'active' ? 'warning' : 'success'"
              size="small"
              text
              @click="toggleStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="info"
              size="small"
              text
              @click="openResetPasswordDialog(row)"
            >
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '添加用户' : '编辑用户'"
      width="500px"
    >
      <el-form ref="formRef" :model="userForm" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="选择角色" style="width: 100%;">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="dialogMode === 'add'" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'add'" label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <div class="reset-password-content">
        <p class="reset-info">
          正在为用户 <strong>{{ selectedUser.username || selectedUser.name }}</strong> 重置密码
        </p>
        <el-form :model="resetPasswordForm" label-width="80px">
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="resetPasswordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="resetPasswordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminAPI } from '../api/admin'

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const submitting = ref(false)

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const resetPasswordDialogVisible = ref(false)
const selectedUser = ref({})
const resetPasswordForm = ref({
  newPassword: '',
  confirmPassword: ''
})

const userForm = ref({
  id: '',
  username: '',
  email: '',
  phone: '',
  role: 'user',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== userForm.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const users = ref([])

async function loadUsersApi() {
  try {
    const response = await adminAPI.getUsers(currentPage.value - 1, pageSize.value)
    const content = response?.content || response?.data || response
    if (Array.isArray(content)) {
      users.value = content.map(u => ({
        id: u.id,
        username: u.username || u.name,
        name: u.fullName || u.username || u.name,
        email: u.email,
        phone: u.phone || '',
        role: u.roleName || u.role || 'user',
        status: u.enabled !== false ? 'active' : 'disabled',
        deviceCount: u.deviceCount || 0,
        createdAt: u.createdAt || '',
        lastLogin: u.lastLogin || ''
      }))
    } else {
      users.value = []
    }
    total.value = response?.totalElements || users.value.length
  } catch {
    ElMessage.error('获取用户列表失败')
  }
}

const filteredUsers = computed(() => {
  let list = users.value
  if (searchQuery.value) {
    list = list.filter(user =>
      (user.username || user.name || '').includes(searchQuery.value) ||
      (user.email || '').includes(searchQuery.value) ||
      (user.phone || '').includes(searchQuery.value)
    )
  }
  total.value = list.length
  const start = (currentPage.value - 1) * pageSize.value
  return list.slice(start, start + pageSize.value)
})

const openAddDialog = () => {
  dialogMode.value = 'add'
  userForm.value = {
    id: '',
    username: '',
    email: '',
    phone: '',
    role: 'user',
    password: '',
    confirmPassword: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (user) => {
  dialogMode.value = 'edit'
  userForm.value = {
    id: user.id,
    username: user.username || user.name,
    email: user.email,
    phone: user.phone,
    role: user.role,
    password: '',
    confirmPassword: ''
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (dialogMode.value === 'add') {
        const payload = {
          username: userForm.value.username,
          email: userForm.value.email,
          phone: userForm.value.phone,
          password: userForm.value.password,
          fullName: userForm.value.username
        }
        await adminAPI.createUser(payload)
        ElMessage.success('用户添加成功')
      } else {
        await adminAPI.updateUser(userForm.value.id, {
          username: userForm.value.username,
          email: userForm.value.email,
          phone: userForm.value.phone
        })
        ElMessage.success('用户信息更新成功')
      }
      dialogVisible.value = false
      await loadUsersApi()
    } catch (error) {
      const msg = error?.response?.data || error?.message || '操作失败'
      ElMessage.error(typeof msg === 'string' ? msg : JSON.stringify(msg))
    } finally {
      submitting.value = false
    }
  })
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username || user.name}" 吗？此操作不可恢复。`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    await adminAPI.deleteUser(user.id)
    ElMessage.success('用户已删除')
    await loadUsersApi()
  } catch {
    ElMessage.info('已取消删除')
  }
}

const toggleStatus = async (user) => {
  const newEnabled = user.status !== 'active'
  try {
    await adminAPI.toggleUserStatus(user.id, newEnabled)
    user.status = newEnabled ? 'active' : 'disabled'
    ElMessage.success(`用户已${newEnabled ? '启用' : '禁用'}`)
  } catch {
    ElMessage.error('操作失败')
  }
}

const openResetPasswordDialog = (user) => {
  selectedUser.value = user
  resetPasswordForm.value = { newPassword: '', confirmPassword: '' }
  resetPasswordDialogVisible.value = true
}

const resetPassword = async () => {
  if (!resetPasswordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (resetPasswordForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  if (resetPasswordForm.value.newPassword !== resetPasswordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  try {
    await adminAPI.resetUserPassword(selectedUser.value.id, resetPasswordForm.value.newPassword)
    ElMessage.success(`用户 ${selectedUser.value.username || selectedUser.value.name} 的密码已重置`)
    resetPasswordDialogVisible.value = false
  } catch {
    ElMessage.error('密码重置失败')
  }
}

onMounted(() => { loadUsersApi() })
</script>

<style scoped>
.user-management {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.users-table {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
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

.device-count {
  font-weight: 500;
  color: #409eff;
}

.time-text {
  font-size: 13px;
  color: #606266;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}

.reset-password-content {
  padding: 10px 0;
}

.reset-info {
  margin: 0 0 20px 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
}

.reset-info strong {
  color: #303133;
  font-weight: 600;
}
</style>
