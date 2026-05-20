<template>
  <div class="admin-dashboard">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="240px" class="admin-sidebar">
        <div class="sidebar-header">
          <h2>管理员面板</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="roles">
            <el-icon><Key /></el-icon>
            <span>角色权限</span>
          </el-menu-item>
          <el-menu-item index="logs">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
          <el-divider />
          <el-menu-item index="back">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回主页</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="admin-header">
          <div class="header-left">
            <h3>{{ pageTitle }}</h3>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="32">{{ username?.charAt(0).toUpperCase() }}</el-avatar>
                <span class="username">{{ username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容区域 -->
        <el-main class="admin-content">
          <!-- 仪表盘视图 -->
          <div v-if="activeMenu === 'dashboard'" class="dashboard-view">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-icon users">
                    <el-icon :size="32"><User /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                    <div class="stat-label">总用户数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-icon active">
                    <el-icon :size="32"><UserFilled /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.activeUsers || 0 }}</div>
                    <div class="stat-label">活跃用户</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-icon roles">
                    <el-icon :size="32"><Key /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.totalRoles || 0 }}</div>
                    <div class="stat-label">角色数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-icon logs">
                    <el-icon :size="32"><Document /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.recentLogs || 0 }}</div>
                    <div class="stat-label">今日操作</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="16">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>系统概览</span>
                    </div>
                  </template>
                  <div class="overview-content">
                    <div class="overview-item">
                      <span class="label">系统版本:</span>
                      <span class="value">v1.0.0</span>
                    </div>
                    <div class="overview-item">
                      <span class="label">系统状态:</span>
                      <el-tag type="success">正常运行</el-tag>
                    </div>
                    <div class="overview-item">
                      <span class="label">数据库连接:</span>
                      <el-tag type="success">已连接</el-tag>
                    </div>
                    <div class="overview-item">
                      <span class="label">Redis缓存:</span>
                      <el-tag type="success">已连接</el-tag>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>快捷操作</span>
                    </div>
                  </template>
                  <div class="quick-actions">
                    <el-button type="primary" @click="activeMenu = 'users'">
                      <el-icon><Plus /></el-icon>
                      添加用户
                    </el-button>
                    <el-button type="success" @click="activeMenu = 'roles'">
                      <el-icon><Key /></el-icon>
                      管理角色
                    </el-button>
                    <el-button type="warning" @click="activeMenu = 'logs'">
                      <el-icon><Document /></el-icon>
                      查看日志
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 用户管理视图 -->
          <div v-if="activeMenu === 'users'" class="users-view">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>用户列表</span>
                  <el-button type="primary" @click="showAddUserDialog">
                    <el-icon><Plus /></el-icon>
                    添加用户
                  </el-button>
                </div>
              </template>

              <el-table :data="users" stripe v-loading="loading">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="fullName" label="姓名" width="120" />
                <el-table-column prop="email" label="邮箱" width="180" />
                <el-table-column prop="phone" label="电话" width="130" />
                <el-table-column prop="roleName" label="角色" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getRoleTagType(row.roleName)">
                      {{ row.roleName || '无角色' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="enabled" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.enabled ? 'success' : 'danger'">
                      {{ row.enabled ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                  <template #default="{ row }">
                    {{ formatDate(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" type="primary" link @click="editUser(row)">
                      编辑
                    </el-button>
                    <el-button size="small" type="warning" link @click="resetPassword(row)">
                      重置密码
                    </el-button>
                    <el-button 
                      size="small" 
                      :type="row.enabled ? 'danger' : 'success'" 
                      link 
                      @click="toggleUserStatus(row)"
                    >
                      {{ row.enabled ? '禁用' : '启用' }}
                    </el-button>
                    <el-button size="small" type="danger" link @click="deleteUser(row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="totalUsers"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadUsers"
                @current-change="loadUsers"
                style="margin-top: 20px"
              />
            </el-card>
          </div>

          <!-- 角色管理视图 -->
          <div v-if="activeMenu === 'roles'" class="roles-view">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>角色管理</span>
                  <el-button type="primary" @click="showAddRoleDialog">
                    <el-icon><Plus /></el-icon>
                    添加角色
                  </el-button>
                </div>
              </template>

              <el-table :data="roles" stripe v-loading="loading">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="roleName" label="角色名称" width="150" />
                <el-table-column prop="description" label="描述" />
                <el-table-column prop="permissionCount" label="权限数量" width="120">
                  <template #default="{ row }">
                    <el-tag type="info">{{ row.permissionCount }} 个权限</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" type="primary" link @click="editRole(row)">
                      编辑
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      link 
                      @click="deleteRole(row)"
                      :disabled="row.roleName === 'ROLE_ADMIN' || row.roleName === 'ROLE_USER'"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>

          <!-- 操作日志视图 -->
          <div v-if="activeMenu === 'logs'" class="logs-view">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>操作日志</span>
                  <el-button @click="loadLogs">
                    <el-icon><Refresh /></el-icon>
                    刷新
                  </el-button>
                </div>
              </template>

              <el-table :data="logs" stripe v-loading="loading">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="username" label="操作用户" width="120" />
                <el-table-column prop="operation" label="操作类型" width="150" />
                <el-table-column prop="resource" label="资源" width="100" />
                <el-table-column prop="ipAddress" label="IP地址" width="140" />
                <el-table-column prop="success" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.success ? 'success' : 'danger'">
                      {{ row.success ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="details" label="详情" />
                <el-table-column prop="timestamp" label="时间" width="180">
                  <template #default="{ row }">
                    {{ formatDate(row.timestamp) }}
                  </template>
                </el-table-column>
              </el-table>

              <el-pagination
                v-model:current-page="logCurrentPage"
                v-model:page-size="logPageSize"
                :total="totalLogs"
                layout="total, prev, pager, next"
                @size-change="loadLogs"
                @current-change="loadLogs"
                style="margin-top: 20px"
              />
            </el-card>
          </div>

          <!-- 系统设置视图 -->
          <div v-if="activeMenu === 'settings'" class="settings-view">
            <el-card>
              <template #header>
                <span>系统设置</span>
              </template>
              <el-form :model="systemConfig" label-width="150px">
                <el-form-item label="系统名称">
                  <el-input v-model="systemConfig.systemName" />
                </el-form-item>
                <el-form-item label="版本号">
                  <el-input v-model="systemConfig.version" disabled />
                </el-form-item>
                <el-form-item label="最大用户数">
                  <el-input-number v-model="systemConfig.maxUsers" :min="10" :max="10000" />
                </el-form-item>
                <el-form-item label="会话超时(秒)">
                  <el-input-number v-model="systemConfig.sessionTimeout" :min="300" :max="86400" />
                </el-form-item>
                <el-form-item label="密码最小长度">
                  <el-input-number v-model="systemConfig.passwordMinLength" :min="6" :max="20" />
                </el-form-item>
                <el-form-item label="允许注册">
                  <el-switch v-model="systemConfig.allowRegistration" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveSystemConfig">保存设置</el-button>
                  <el-button @click="loadSystemConfig">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="isEditUser ? '编辑用户' : '添加用户'"
      width="600px"
    >
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEditUser" />
        </el-form-item>
        <el-form-item label="姓名" prop="fullName">
          <el-input v-model="userForm.fullName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" type="email" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEditUser">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="选择角色">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      :title="isEditRole ? '编辑角色' : '添加角色'"
      width="500px"
    >
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" :disabled="isEditRole && roleForm.roleName === 'ROLE_ADMIN'" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPasswordDialogVisible" title="重置密码" width="400px">
      <el-form :model="resetPasswordForm" label-width="100px">
        <el-form-item label="新密码">
          <el-input v-model="resetPasswordForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, Key, Document, Setting, Plus, Refresh, 
  DataBoard, UserFilled, ArrowDown, ArrowLeft
} from '@element-plus/icons-vue'
import adminAPI from '../api/admin'

const router = useRouter()
const username = localStorage.getItem('username')

const activeMenu = ref('dashboard')
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalUsers = ref(0)
const logCurrentPage = ref(1)
const logPageSize = ref(20)
const totalLogs = ref(0)

// 数据
const stats = ref({})
const users = ref([])
const roles = ref([])
const logs = ref([])
const systemConfig = reactive({
  systemName: '智能家居管理系统',
  version: '1.0.0',
  maxUsers: 1000,
  sessionTimeout: 3600,
  passwordMinLength: 6,
  allowRegistration: false
})

// 对话框状态
const userDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const resetPasswordDialogVisible = ref(false)
const isEditUser = ref(false)
const isEditRole = ref(false)

// 表单
const userFormRef = ref(null)
const roleFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  fullName: '',
  email: '',
  phone: '',
  password: '',
  roleId: null
})
const roleForm = reactive({
  id: null,
  roleName: '',
  description: ''
})
const resetPasswordForm = reactive({
  userId: null,
  newPassword: ''
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少8位', trigger: 'blur' }
  ]
}

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const pageTitle = computed(() => {
  const titles = {
    dashboard: '仪表盘',
    users: '用户管理',
    roles: '角色权限',
    logs: '操作日志',
    settings: '系统设置'
  }
  return titles[activeMenu.value] || '仪表盘'
})

onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  const tab = router.currentRoute.value.query.tab
  if (tab && ['dashboard', 'users', 'roles', 'logs', 'settings'].includes(String(tab))) {
    activeMenu.value = String(tab)
    if (tab === 'users') loadUsers()
    if (tab === 'roles') loadRoles()
    if (tab === 'logs') loadLogs()
    if (tab === 'settings') loadSystemConfig()
  } else {
    loadDashboardStats()
  }
})

const handleMenuSelect = (index) => {
  if (index === 'back') {
    router.push('/user')
    return
  }
  activeMenu.value = index
  
  if (index === 'users') loadUsers()
  if (index === 'roles') loadRoles()
  if (index === 'logs') loadLogs()
  if (index === 'settings') loadSystemConfig()
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      router.push('/login')
    })
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const loadDashboardStats = async () => {
  try {
    const res = await adminAPI.getDashboardStats()
    stats.value = res
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getUsers(currentPage.value - 1, pageSize.value)
    users.value = res.users
    totalUsers.value = res.totalItems
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getRoles()
    roles.value = res
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getLogs(logCurrentPage.value - 1, logPageSize.value)
    logs.value = res.logs
    totalLogs.value = res.totalItems
  } catch (error) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

const loadSystemConfig = async () => {
  try {
    const res = await adminAPI.getSystemConfig()
    Object.assign(systemConfig, res)
  } catch (error) {
    ElMessage.error('加载系统配置失败')
  }
}

const showAddUserDialog = () => {
  isEditUser.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    fullName: '',
    email: '',
    phone: '',
    password: '',
    roleId: null
  })
  userDialogVisible.value = true
}

const editUser = (user) => {
  isEditUser.value = true
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    fullName: user.fullName,
    email: user.email,
    phone: user.phone,
    password: '',
    roleId: user.roleId
  })
  userDialogVisible.value = true
}

const saveUser = async () => {
  try {
    await userFormRef.value.validate()
    
    if (isEditUser.value) {
      await adminAPI.updateUser(userForm.id, userForm)
      ElMessage.success('用户更新成功')
    } else {
      await adminAPI.createUser(userForm)
      ElMessage.success('用户创建成功')
    }
    
    userDialogVisible.value = false
    loadUsers()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminAPI.deleteUser(user.id)
    ElMessage.success('用户删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
    }
  }
}

const toggleUserStatus = async (user) => {
  try {
    await adminAPI.toggleUserStatus(user.id, !user.enabled)
    ElMessage.success(`用户${user.enabled ? '禁用' : '启用'}成功`)
    loadUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetPassword = (user) => {
  resetPasswordForm.userId = user.id
  resetPasswordForm.newPassword = ''
  resetPasswordDialogVisible.value = true
}

const confirmResetPassword = async () => {
  if (!resetPasswordForm.newPassword || resetPasswordForm.newPassword.length < 8) {
    ElMessage.error('密码长度至少8位')
    return
  }
  
  try {
    await adminAPI.resetUserPassword(resetPasswordForm.userId, resetPasswordForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error) {
    ElMessage.error('密码重置失败')
  }
}

const showAddRoleDialog = () => {
  isEditRole.value = false
  Object.assign(roleForm, {
    id: null,
    roleName: '',
    description: ''
  })
  roleDialogVisible.value = true
}

const editRole = (role) => {
  isEditRole.value = true
  Object.assign(roleForm, {
    id: role.id,
    roleName: role.roleName,
    description: role.description
  })
  roleDialogVisible.value = true
}

const saveRole = async () => {
  try {
    await roleFormRef.value.validate()
    
    if (isEditRole.value) {
      await adminAPI.updateRole(roleForm.id, roleForm)
      ElMessage.success('角色更新成功')
    } else {
      await adminAPI.createRole(roleForm)
      ElMessage.success('角色创建成功')
    }
    
    roleDialogVisible.value = false
    loadRoles()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

const deleteRole = async (role) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色 "${role.roleName}" 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminAPI.deleteRole(role.id)
    ElMessage.success('角色删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除角色失败')
    }
  }
}

const saveSystemConfig = async () => {
  ElMessage.success('系统配置保存成功')
}

const getRoleTagType = (roleName) => {
  const types = {
    'ADMIN': 'danger',
    'USER': 'primary',
    'GUEST': 'info'
  }
  return types[roleName] || ''
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  width: 100%;
}

.admin-sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  height: 50px;
  line-height: 50px;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.admin-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left h3 {
  margin: 0;
  color: #333;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-weight: 500;
}

.admin-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-icon.users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.active {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.roles {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.logs {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.chart-card {
  min-height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.overview-item .label {
  font-weight: 500;
  color: #666;
  width: 120px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-actions .el-button {
  width: 100%;
}
</style>
