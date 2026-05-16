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
              <el-avatar :size="40">{{ row.name.charAt(0) }}</el-avatar>
              <div class="user-info">
                <span class="user-name">{{ row.name }}</span>
                <span class="user-email">{{ row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ row.role === 'admin' ? '管理员' : '普通用户' }}
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.role !== 'admin'"
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
        <el-form-item label="用户名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)

const userForm = ref({
  id: '',
  name: '',
  email: '',
  phone: '',
  role: 'user',
  password: ''
})

const formRules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const users = ref([
  { id: 1, name: '张三', email: 'zhangsan@example.com', phone: '138****001', role: 'user', status: 'active', deviceCount: 8, createdAt: '2024-01-15', lastLogin: '2024-03-20' },
  { id: 2, name: '李四', email: 'lisi@example.com', phone: '138****002', role: 'user', status: 'active', deviceCount: 12, createdAt: '2024-01-18', lastLogin: '2024-03-19' },
  { id: 3, name: '王五', email: 'wangwu@example.com', phone: '138****003', role: 'admin', status: 'active', deviceCount: 15, createdAt: '2024-01-10', lastLogin: '2024-03-20' },
  { id: 4, name: '赵六', email: 'zhaoliu@example.com', phone: '138****004', role: 'user', status: 'active', deviceCount: 6, createdAt: '2024-02-01', lastLogin: '2024-03-18' },
  { id: 5, name: '孙七', email: 'sunqi@example.com', phone: '138****005', role: 'user', status: 'disabled', deviceCount: 0, createdAt: '2024-02-10', lastLogin: '2024-02-28' },
  { id: 6, name: '周八', email: 'zhouba@example.com', phone: '138****006', role: 'user', status: 'active', deviceCount: 10, createdAt: '2024-02-15', lastLogin: '2024-03-17' },
  { id: 7, name: '吴九', email: 'wujiu@example.com', phone: '138****007', role: 'admin', status: 'active', deviceCount: 20, createdAt: '2024-01-05', lastLogin: '2024-03-20' },
  { id: 8, name: '郑十', email: 'zhengshi@example.com', phone: '138****008', role: 'user', status: 'active', deviceCount: 5, createdAt: '2024-03-01', lastLogin: '2024-03-15' }
])

total.value = users.value.length

const filteredUsers = computed(() => {
  if (!searchQuery.value) {
    return users.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
  }
  return users.value.filter(user =>
    user.name.includes(searchQuery.value) ||
    user.email.includes(searchQuery.value) ||
    user.phone.includes(searchQuery.value)
  ).slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
})

const openAddDialog = () => {
  dialogMode.value = 'add'
  userForm.value = {
    id: '',
    name: '',
    email: '',
    phone: '',
    role: 'user',
    password: ''
  }
  dialogVisible.value = true
}

const openEditDialog = (user) => {
  dialogMode.value = 'edit'
  userForm.value = { ...user, password: '' }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      if (dialogMode.value === 'add') {
        const newUser = {
          id: users.value.length + 1,
          ...userForm.value,
          status: 'active',
          deviceCount: 0,
          createdAt: new Date().toISOString().split('T')[0],
          lastLogin: '-'
        }
        delete newUser.password
        users.value.unshift(newUser)
        total.value = users.value.length
        ElMessage.success('用户添加成功')
      } else {
        const index = users.value.findIndex(u => u.id === userForm.value.id)
        if (index !== -1) {
          users.value[index] = { ...users.value[index], ...userForm.value }
          ElMessage.success('用户信息更新成功')
        }
      }
      dialogVisible.value = false
    }
  })
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const index = users.value.findIndex(u => u.id === user.id)
    if (index !== -1) {
      users.value.splice(index, 1)
      total.value = users.value.length
      ElMessage.success('用户已删除')
    }
  } catch {
    ElMessage.info('已取消删除')
  }
}

const toggleStatus = (user) => {
  const newStatus = user.status === 'active' ? 'disabled' : 'active'
  const action = newStatus === 'active' ? '启用' : '禁用'
  user.status = newStatus
  ElMessage.success(`用户已${action}`)
}
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
</style>
