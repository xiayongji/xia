<template>
  <div class="profile">
    <div class="profile-header">
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <el-avatar :size="100" class="user-avatar">
            <User />
          </el-avatar>
          <button class="edit-avatar-btn">
            <el-icon><CameraFilled /></el-icon>
          </button>
        </div>
        <h2 class="user-name">{{ userInfo.username }}</h2>
        <p class="user-role">智能生活管家</p>
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">8</div>
            <div class="stat-label">设备</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">6</div>
            <div class="stat-label">场景</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">120kWh</div>
            <div class="stat-label">本月能耗</div>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <div class="content-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <h3>个人信息</h3>
        </div>
        <div class="card-content">
          <el-form :model="userInfo" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled class="disabled-input" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userInfo.phone" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="userInfo.gender" placeholder="请选择性别">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
                <el-option label="保密" value="secret" />
              </el-select>
            </el-form-item>
            <el-form-item label="生日">
              <el-date-picker v-model="userInfo.birthday" type="date" placeholder="选择日期" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveInfo">保存信息</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="content-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><Lock /></el-icon>
          </div>
          <h3>密码修改</h3>
        </div>
        <div class="card-content">
          <el-form :model="passwordForm" label-width="100px" class="password-form">
            <el-form-item label="原密码">
              <el-input v-model="passwordForm.oldPassword" type="password" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="content-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><Checked /></el-icon>
          </div>
          <h3>账户安全</h3>
        </div>
        <div class="card-content">
          <div class="security-list">
            <div class="security-item">
              <div class="security-info">
                <div class="security-icon email">
                  <el-icon><Message /></el-icon>
                </div>
                <div class="security-detail">
                  <h4>邮箱验证</h4>
                  <p>{{ userInfo.email }}</p>
                </div>
              </div>
              <div class="security-status">
                <span class="status-badge verified">已验证</span>
                <button class="action-text">重新验证</button>
              </div>
            </div>
            <div class="security-item">
              <div class="security-info">
                <div class="security-icon phone">
                  <el-icon><PhoneFilled /></el-icon>
                </div>
                <div class="security-detail">
                  <h4>手机验证</h4>
                  <p>{{ userInfo.phone }}</p>
                </div>
              </div>
              <div class="security-status">
                <span class="status-badge verified">已验证</span>
                <button class="action-text">重新验证</button>
              </div>
            </div>
            <div class="security-item">
              <div class="security-info">
                <div class="security-icon twofactor">
                  <el-icon><Key /></el-icon>
                </div>
                <div class="security-detail">
                  <h4>两步验证</h4>
                  <p>登录时需要额外验证</p>
                </div>
              </div>
              <div class="security-status">
                <span class="status-badge pending">未开启</span>
                <button class="action-primary">开启</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="content-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="24"><HelpFilled /></el-icon>
          </div>
          <h3>帮助与支持</h3>
        </div>
        <div class="card-content">
          <div class="help-list">
            <button class="help-item" @click="openHelp('faq')">
              <el-icon><ChatLineRound /></el-icon>
              <span>常见问题</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </button>
            <button class="help-item" @click="openHelp('contact')">
              <el-icon><Headset /></el-icon>
              <span>联系客服</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </button>
            <button class="help-item" @click="openHelp('feedback')">
              <el-icon><ChatSquare /></el-icon>
              <span>意见反馈</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </button>
            <button class="help-item" @click="openHelp('about')">
              <el-icon><InfoFilled /></el-icon>
              <span>关于我们</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { 
  User, CameraFilled, Lock, Checked, Message, PhoneFilled, Key, 
  HelpFilled, ChatLineRound, Headset, ChatSquare, InfoFilled, ArrowRight 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userInfo = reactive({
  username: localStorage.getItem('username') || 'admin',
  email: 'admin@example.com',
  phone: '13800138000',
  gender: 'secret',
  birthday: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saveInfo = () => {
  ElMessage.success('个人信息保存成功')
}

const changePassword = () => {
  if (!passwordForm.value.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }
  ElMessage.success('密码修改成功')
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const openHelp = (type) => {
  const messages = {
    faq: '打开常见问题页面',
    contact: '打开客服联系方式',
    feedback: '打开意见反馈页面',
    about: '打开关于页面'
  }
  ElMessage.info(messages[type])
}
</script>

<style scoped>
.profile {
  max-width: 900px;
  margin: 0 auto;
}

.profile-header {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border-radius: 24px;
  padding: 40px;
  margin-bottom: 24px;
  text-align: center;
  color: #fff;
}

.avatar-section {
  position: relative;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.user-avatar {
  background: rgba(255, 255, 255, 0.2);
  border: 4px solid rgba(255, 255, 255, 0.3);
}

.edit-avatar-btn {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10B981;
  transition: all 0.3s ease;
}

.edit-avatar-btn:hover {
  transform: scale(1.1);
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
}

.user-role {
  font-size: 14px;
  opacity: 0.9;
  margin: 0 0 24px;
}

.stats-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

.profile-content {
  display: grid;
  gap: 20px;
}

.content-card {
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

.profile-form,
.password-form {
  max-width: 400px;
}

.disabled-input {
  background: #F8FAFC;
  color: #94A3B8;
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 12px;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.security-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.security-icon.email {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.security-icon.phone {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.security-icon.twofactor {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.security-detail h4 {
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
  margin: 0 0 4px;
}

.security-detail p {
  font-size: 12px;
  color: #64748B;
  margin: 0;
}

.security-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.verified {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.status-badge.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.action-text {
  font-size: 13px;
  color: #64748B;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.action-text:hover {
  color: #10B981;
}

.action-primary {
  font-size: 13px;
  color: #10B981;
  background: rgba(16, 185, 129, 0.1);
  border: none;
  border-radius: 8px;
  padding: 6px 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-primary:hover {
  background: #10B981;
  color: #fff;
}

.help-list {
  display: flex;
  flex-direction: column;
}

.help-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
}

.help-item:last-child {
  margin-bottom: 0;
}

.help-item:hover {
  background: #F1F5F9;
  transform: translateX(4px);
}

.help-item span {
  flex: 1;
  text-align: left;
  font-size: 14px;
  color: #475569;
}

.help-item .arrow {
  color: #94A3B8;
}

@media screen and (max-width: 768px) {
  .profile-header {
    padding: 24px;
  }
  
  .stats-row {
    gap: 16px;
  }
  
  .stat-num {
    font-size: 20px;
  }
  
  .profile-form,
  .password-form {
    max-width: 100%;
  }
}
</style>