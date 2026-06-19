<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-info">
        <h1 class="logo-title" @click="router.push('/home')" style="cursor: pointer;" title="返回首页">匠脉锡雕</h1>
        <p class="logo-subtitle">数字化非遗传承与创新平台</p>
        <div class="features">
          <div class="feature-item"><el-icon><Monitor /></el-icon> 全域管理</div>
          <div class="feature-item"><el-icon><Connection /></el-icon> AI赋能</div>
          <div class="feature-item"><el-icon><DataLine /></el-icon> 数据驱动</div>
        </div>
      </div>
      <div class="login-form-box">
        <div class="form-header">
          <h2 class="form-title">系统通行证</h2>
          <el-button link class="back-link" @click="router.push('/home')">
            返回首页
          </el-button>
        </div>
        <el-tabs v-model="activeTab" class="auth-tabs">
          <el-tab-pane label="登录" name="login">
        <el-form :model="loginForm" ref="loginFormRef" :rules="rules" status-icon>
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入注册时姓名" 
              prefix-icon="User" 
              size="large">
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="管理员密码" 
              prefix-icon="Lock" 
              size="large"
              @keyup.enter="handleLogin">
            </el-input>
          </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" size="large" @click="handleLogin" :loading="loading">
                登 录
              </el-button>
            </el-form-item>
            <div class="agreement-tip">
              登录即代表同意
              <span class="link-text" @click="goAgreement">《用户协议》与《隐私政策》</span>
            </div>
          </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" ref="registerFormRef" :rules="rules" status-icon>
            <el-form-item prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="请输入你的姓名（将作为登录名）" 
                prefix-icon="User" 
                size="large">
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="设置密码" 
                prefix-icon="Lock" 
                size="large">
              </el-input>
            </el-form-item>
            <el-form-item prop="role">
              <el-select v-model="registerForm.role" placeholder="选择注册身份" size="large" style="width: 100%">
                <template #prefix><el-icon><Stamp /></el-icon></template>
                <el-option label="我是普通学生 (基础学习与提交作品)" value="STUDENT"></el-option>
                <el-option label="我是课程讲师/文化推广者 (发课与审核)" value="UPLOADER"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="success" class="login-btn register-btn" size="large" @click="handleRegister" :loading="loading">
                注 册
              </el-button>
            </el-form-item>
          </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'
import { fetchCurrentProfile } from '../api/profileChange'

const router = useRouter()
const activeTab = ref('login')
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  role: 'STUDENT'
})

const ONBOARDING_PROFILE_KEY = 'onboarding_profile_username'

const rules = {
  username: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

const inferRoleByUsername = (username) => {
  let role = 'STUDENT'
  if (username === 'admin') role = 'ADMIN'
  if (username === 'uploader') role = 'UPLOADER'
  return role
}

const parseJwtPayload = (token) => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return {}
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), '=')
    const decoded = decodeURIComponent(
      atob(padded)
        .split('')
        .map((char) => `%${(`00${char.charCodeAt(0).toString(16)}`).slice(-2)}`)
        .join('')
    )
    return JSON.parse(decoded)
  } catch {
    return {}
  }
}

const syncProfileCache = async (username) => {
  try {
    const res = await fetchCurrentProfile(username)
    const profile = res?.data || {}
    if (profile.username) localStorage.setItem('username', profile.username)
    if (profile.nickname) localStorage.setItem('nickname', profile.nickname)
    if (profile.avatar) {
      localStorage.setItem('avatar', profile.avatar)
    } else {
      localStorage.removeItem('avatar')
    }
  } catch {
    // 忽略资料同步失败，保证登录流程不中断
  }
}

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      login(loginForm).then(async (res) => {
        const token = res.data
        const payload = parseJwtPayload(token)
        localStorage.setItem('token', token)
        localStorage.setItem('username', payload.username || loginForm.username)
        localStorage.removeItem('nickname')
        localStorage.removeItem('avatar')

        localStorage.setItem('role', payload.role || inferRoleByUsername(loginForm.username))
        await syncProfileCache(payload.username || loginForm.username)

        ElMessage.success('登录成功，欢迎访问匠脉服务端！')
        const onboardingUser = localStorage.getItem(ONBOARDING_PROFILE_KEY)
        if (onboardingUser && onboardingUser === (payload.username || loginForm.username)) {
          router.push('/profile')
        } else {
          router.push('/dashboard')
        }
      }).catch(err => {
        const msg = err?.response?.data?.message || err?.message || '登录失败，请检查账号密码'
        ElMessage.error(msg)
      }).finally(() => {
        loading.value = false
      })
    }
  })
}

const handleRegister = () => {
  registerFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      register(registerForm).then(res => {
        ElMessage.success('注册成功，请登录！')
        localStorage.setItem(ONBOARDING_PROFILE_KEY, registerForm.username)
        activeTab.value = 'login'
        loginForm.username = registerForm.username
        loginForm.password = registerForm.password
      }).catch(err => {
        ElMessage.error(err?.response?.data?.message || err?.message || '注册失败')
      }).finally(() => {
        loading.value = false
      })
    }
  })
}

const goAgreement = () => {
  router.push('/agreement')
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('/images/hero_bg.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(11, 12, 16, 0.8) 0%, rgba(11, 12, 16, 0.98) 100%);
  z-index: 1;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 252, 241, 0.1) 0%, transparent 40%);
  animation: rotate 30s linear infinite;
  z-index: 1;
  pointer-events: none;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.login-wrapper {
  display: flex;
  width: 900px;
  height: 520px;
  background: rgba(31, 40, 51, 0.4);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 24px;
  box-shadow: 0 40px 80px rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 2;
  overflow: hidden;
}

.login-info {
  flex: 1;
  padding: 60px 40px;
  color: white;
  background: rgba(11, 12, 16, 0.2);
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.logo-title {
  font-size: 42px;
  margin: 0;
  font-weight: 800;
  letter-spacing: 2px;
  color: #fff;
  text-shadow: 0 4px 20px rgba(0,0,0,0.8);
}

.logo-subtitle {
  font-size: 16px;
  margin-top: 10px;
  opacity: 0.9;
  letter-spacing: 1px;
  color: #c5c6c7;
}

.features {
  margin-top: 50px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-top: 24px;
  font-size: 16px;
  color: #8892b0;
  transition: all 0.3s;
}

.feature-item:hover {
  transform: translateX(10px);
  color: #66fcf1;
}

.feature-item .el-icon {
  margin-right: 15px;
  font-size: 24px;
  color: #66fcf1;
}

.login-form-box {
  width: 420px;
  padding: 50px 40px;
  background: transparent;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.form-title {
  font-size: 28px;
  color: #fff;
  margin: 0;
  font-weight: 700;
  letter-spacing: 1px;
}

.back-link {
  font-size: 14px;
  color: #8892b0;
}

.back-link:hover {
  color: #66fcf1;
}

.login-btn {
  width: 100%;
  margin-top: 20px;
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  letter-spacing: 4px;
  background: #66fcf1;
  color: #0b0c10;
  font-weight: 800;
  border: none;
  transition: all 0.3s;
}

.login-btn:hover, .login-btn:focus {
  background: #fff;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 252, 241, 0.4);
  color: #0b0c10;
}

.register-btn {
  background: linear-gradient(90deg, #45a29e, #66fcf1);
  color: #0b0c10;
}

.register-btn:hover {
  background: #fff;
}

:deep(.auth-tabs .el-tabs__item) {
  font-size: 18px;
  font-weight: 500;
  color: #8892b0;
}

:deep(.auth-tabs .el-tabs__item.is-active), :deep(.auth-tabs .el-tabs__item:hover) {
  color: #66fcf1;
  text-shadow: 0 0 10px rgba(102, 252, 241, 0.4);
}

:deep(.auth-tabs .el-tabs__active-bar) {
  background-color: #66fcf1;
  box-shadow: 0 0 10px #66fcf1;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(255,255,255,0.05);
}

:deep(.el-input__wrapper), :deep(.el-select .el-input__wrapper) {
  background: rgba(11, 12, 16, 0.6) !important;
  border: 1px solid rgba(255,255,255,0.1) !important;
  box-shadow: none !important;
  border-radius: 12px;
}

:deep(.el-input__wrapper.is-focus), :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #66fcf1 !important;
  box-shadow: inset 0 0 0 1px #66fcf1 !important;
  background: rgba(31, 40, 51, 0.8) !important;
}

:deep(.el-input__inner) {
  color: #fff !important;
  height: 44px;
}

:deep(.el-input__prefix) {
  color: #45a29e;
}

:deep(.el-select-dropdown__item.selected) {
  color: #66fcf1;
}

.agreement-tip {
  margin-top: -4px;
  color: #8892b0;
  font-size: 13px;
  text-align: center;
}

.link-text {
  color: #45a29e;
  cursor: pointer;
  transition: all 0.3s;
}

.link-text:hover {
  color: #66fcf1;
  text-shadow: 0 0 10px rgba(102, 252, 241, 0.5);
}

@media (max-width: 960px) {
  .login-wrapper {
    width: 92vw;
    height: auto;
    min-height: 520px;
  }

  .login-info {
    display: none;
  }

  .login-form-box {
    width: 100%;
    padding: 36px 22px;
  }

  .form-title {
    margin-bottom: 22px;
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 14px;
  }

  .login-wrapper {
    width: 100%;
    min-height: 480px;
    border-radius: 14px;
  }

  .login-form-box {
    padding: 28px 16px;
  }
}
</style>
