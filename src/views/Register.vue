<template>
  <div class="register-container">
    <div class="register-bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
      <div class="bg-circle bg-circle-4"></div>
    </div>
    
    <div class="register-wrapper">
      <div class="register-content">
        <div class="register-brand">
          <div class="brand-icon">
            <el-icon :size="48"><HomeFilled /></el-icon>
          </div>
          <h1 class="brand-title">美智家</h1>
          <p class="brand-subtitle">智能生活，触手可及</p>
        </div>
        
        <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="register-form">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>开启您的智慧生活之旅</p>
          </div>
          
          <el-form-item prop="username">
            <div class="input-wrapper">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input 
                v-model="registerForm.username" 
                placeholder="用户名" 
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="email">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Message /></el-icon>
              <el-input 
                v-model="registerForm.email" 
                placeholder="邮箱" 
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="phone">
            <div class="input-wrapper">
              <el-icon class="input-icon"><PhoneFilled /></el-icon>
              <el-input 
                v-model="registerForm.phone" 
                placeholder="手机号" 
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="密码" 
                class="custom-input"
                :show-password="true"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input 
                v-model="registerForm.confirmPassword" 
                type="password" 
                placeholder="确认密码" 
                class="custom-input"
              />
            </div>
          </el-form-item>
          
          <label class="agree-checkbox">
            <input type="checkbox" v-model="agreeTerms" />
            <span>我已阅读并同意</span>
            <a href="#" class="terms-link">用户协议</a>
            <span>和</span>
            <a href="#" class="terms-link">隐私政策</a>
          </label>
          
          <el-button 
            type="primary" 
            class="register-btn"
            @click="handleRegister"
            :loading="loading"
            :disabled="!agreeTerms"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
          
          <div class="login-link">
            <span>已有账号?</span>
            <el-button type="text" @click="goToLogin">立即登录</el-button>
          </div>
        </el-form>
      </div>
      
      <div class="register-illustration">
        <div class="illustration-content">
          <div class="smart-home-icon">
            <el-icon :size="80"><StarFilled /></el-icon>
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
import { HomeFilled, User, Message, PhoneFilled, Lock, View, Hide, StarFilled, Checked, Lightning, MapLocation } from '@element-plus/icons-vue'

const router = useRouter()

const registerForm = ref({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const registerRules = {
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
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const registerFormRef = ref(null)
const showPassword = ref(false)
const agreeTerms = ref(false)
const loading = ref(false)

const handleRegister = () => {
  registerFormRef.value.validate((valid) => {
    if (valid && agreeTerms.value) {
      loading.value = true
      setTimeout(() => {
        localStorage.setItem('token', 'mock-token')
        localStorage.setItem('username', registerForm.value.username)
        ElMessage.success('注册成功')
        router.push('/dashboard')
        loading.value = false
      }, 1500)
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0F172A 0%, #1E293B 50%, #0F172A 100%);
  position: relative;
  overflow: hidden;
}

.register-bg-decoration {
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
  background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
  top: -100px;
  right: -100px;
}

.bg-circle-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #EC4899 0%, #BE185D 100%);
  top: 50%;
  left: -100px;
  transform: translateY(-50%);
}

.bg-circle-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  bottom: 50px;
  right: 20%;
}

.bg-circle-4 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);
  top: 30%;
  left: 30%;
}

.register-wrapper {
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

.register-content {
  flex: 1;
  min-width: 300px;
}

.register-brand {
  text-align: center;
  margin-bottom: 40px;
}

.brand-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 10px 30px rgba(139, 92, 246, 0.3);
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

.register-form {
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
  border-color: #8B5CF6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
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
  color: #8B5CF6;
}

.agree-checkbox {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  color: #94A3B8;
  font-size: 13px;
  margin: 12px 0;
  cursor: pointer;
}

.agree-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #8B5CF6;
}

.terms-link {
  color: #8B5CF6;
  text-decoration: none;
  transition: color 0.3s ease;
}

.terms-link:hover {
  color: #A78BFA;
}

.register-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(139, 92, 246, 0.4);
  transition: all 0.3s ease;
  margin-top: 12px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.5);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 24px;
  color: #94A3B8;
  font-size: 14px;
}

.login-link el-button {
  color: #8B5CF6;
  padding: 0;
  font-weight: 500;
}

.register-illustration {
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
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2) 0%, rgba(124, 58, 237, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: #8B5CF6;
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
  background: rgba(139, 92, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8B5CF6;
}

@media screen and (max-width: 768px) {
  .register-wrapper {
    flex-direction: column;
    gap: 32px;
    padding: 24px;
  }
  
  .register-illustration {
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