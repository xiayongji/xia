<template>
  <div class="login-container">
    <div class="login-bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
      <div class="bg-circle bg-circle-4"></div>
    </div>
    
    <div class="login-wrapper">
      <div class="login-content">
        <div class="login-brand">
          <div class="brand-icon">
            <el-icon :size="48"><HomeFilled /></el-icon>
          </div>
          <h1 class="brand-title">美智家</h1>
          <p class="brand-subtitle">智能生活，触手可及</p>
        </div>
        
        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账号</p>
          </div>
          
          <el-form-item prop="username">
            <div class="input-wrapper">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input 
                v-model="loginForm.username" 
                placeholder="用户名" 
                class="custom-input"
                :focus="usernameFocus"
                @focus="usernameFocus = true"
                @blur="usernameFocus = false"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="密码" 
                class="custom-input"
                :show-password="true"
                :focus="passwordFocus"
                @focus="passwordFocus = true"
                @blur="passwordFocus = false"
              />
            </div>
          </el-form-item>
          
          <div class="form-options">
            <label class="remember-checkbox">
              <input type="checkbox" v-model="rememberMe" />
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-password">忘记密码?</a>
          </div>
          
          <el-button 
            type="primary" 
            class="login-btn"
            @click="handleLogin"
            :loading="loading"
          >
            {{ loading ? '登录中...' : '立即登录' }}
          </el-button>
          
          <div class="register-link">
            <span>还没有账号?</span>
            <el-button type="primary" link @click="goToRegister">立即注册</el-button>
          </div>
        </el-form>
        
        <div class="login-footer">
          <p>登录即表示同意我们的</p>
          <div class="footer-links">
            <a href="#">用户协议</a>
            <span>|</span>
            <a href="#">隐私政策</a>
          </div>
        </div>
      </div>
      
      <div class="login-illustration">
        <div class="illustration-content">
          <div class="smart-home-icon">
            <el-icon :size="80"><HomeFilled /></el-icon>
          </div>
          <h3>智能家居新生活</h3>
          <p>连接智能设备，开启智慧生活体验</p>
          
          <div class="feature-list">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon :size="24"><Checked /></el-icon>
              </div>
              <span>安全可靠</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon :size="24"><Lightning /></el-icon>
              </div>
              <span>智能节能</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon :size="24"><MapLocation /></el-icon>
              </div>
              <span>远程控制</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { HomeFilled, User, Lock, View, Hide, Checked, Lightning, MapLocation } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = ref({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ]
}

const loginFormRef = ref(null)
const showPassword = ref(false)
const rememberMe = ref(false)
const loading = ref(false)
const usernameFocus = ref(false)
const passwordFocus = ref(false)

const handleLogin = async () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(loginForm.value.username, loginForm.value.password)
        ElMessage.success('登录成功')
        await router.replace('/user')
      } catch (error) {
        const message = error.response?.data || error.message || '登录失败，请检查用户名和密码'
        ElMessage.error(typeof message === 'string' ? message : '登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0F172A 0%, #1E293B 50%, #0F172A 100%);
  position: relative;
  overflow: hidden;
}

.login-bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.bg-circle-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  top: -100px;
  left: -100px;
}

.bg-circle-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);
  top: 50%;
  right: -100px;
  transform: translateY(-50%);
}

.bg-circle-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
  bottom: 50px;
  left: 20%;
}

.bg-circle-4 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #EC4899 0%, #BE185D 100%);
  top: 30%;
  right: 30%;
}

.login-wrapper {
  display: flex;
  gap: 80px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 10;
  max-width: 900px;
  width: 90%;
}

.login-content {
  flex: 1;
  min-width: 300px;
}

.login-brand {
  text-align: center;
  margin-bottom: 40px;
}

.brand-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 10px 30px rgba(16, 185, 129, 0.3);
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
}

.brand-subtitle {
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
}

.login-form {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 32px;
}

.form-header {
  text-align: center;
  margin-bottom: 28px;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: #94A3B8;
  font-size: 18px;
  z-index: 1;
}

.custom-input {
  width: 100%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 14px 16px 14px 48px;
  color: #fff;
  font-size: 14px;
  transition: all 0.3s ease;
}

.custom-input:focus {
  border-color: #10B981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

.custom-input::placeholder {
  color: #64748B;
}

.password-toggle {
  position: absolute;
  right: 16px;
  background: transparent;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  transition: color 0.3s ease;
}

.password-toggle:hover {
  color: #10B981;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0;
}

.remember-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #94A3B8;
  font-size: 14px;
  cursor: pointer;
}

.remember-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #10B981;
}

.forgot-password {
  color: #10B981;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #34D399;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
}

.register-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 24px;
  color: #94A3B8;
  font-size: 14px;
}

.register-link el-button {
  color: #10B981;
  padding: 0;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  color: #64748B;
  font-size: 12px;
}

.footer-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.footer-links a {
  color: #64748B;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #94A3B8;
}

.login-illustration {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
  border-left: 1px solid rgba(255, 255, 255, 0.1);
}

.illustration-content {
  text-align: center;
}

.smart-home-icon {
  width: 120px;
  height: 120px;
  border-radius: 28px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: #10B981;
}

.illustration-content h3 {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 12px;
}

.illustration-content p {
  font-size: 14px;
  color: #94A3B8;
  margin: 0 0 32px;
}

.feature-list {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #94A3B8;
  font-size: 13px;
}

.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(16, 185, 129, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10B981;
}

@media screen and (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    gap: 32px;
    padding: 24px;
  }
  
  .login-illustration {
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    padding: 24px 0 0;
  }
  
  .feature-list {
    gap: 16px;
  }
  
  .brand-title {
    font-size: 24px;
  }
}
</style>