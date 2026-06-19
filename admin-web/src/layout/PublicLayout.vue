<template>
  <div class="public-layout">
    <!-- 顶部导航栏 -->
    <header class="pub-header" :class="{ scrolled: isScrolled }">
      <div class="pub-header-inner">
        <div class="pub-logo" @click="router.push('/home')">
          <span class="pub-logo-icon">⚒</span>
          <span class="pub-logo-text">匠脉锡雕</span>
        </div>

        <!-- 桌面导航 -->
        <nav class="pub-nav" v-if="!isMobile">
          <a class="nav-link" :class="{ active: route.path === '/home' }" @click="router.push('/home')">首页</a>
          <a class="nav-link" :class="{ active: route.path === '/courses' }" @click="router.push('/courses')">精品课程</a>
          <a class="nav-link" :class="{ active: route.path === '/store' }" @click="router.push('/store')">作品广场</a>
          <a class="nav-link" @click="router.push('/ai')">AI 辅学</a>
        </nav>

        <!-- 右侧按钮 -->
        <div class="pub-actions">
          <template v-if="isLoggedIn">
            <el-dropdown trigger="hover" @command="handleCommand">
              <span class="user-chip">
                <el-avatar :size="28" :src="avatar" />
                <span class="chip-name" v-if="!isMobile">{{ nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 所有人都有的控制台 -->
                  <el-dropdown-item command="dashboard">数据总览</el-dropdown-item>
                  
                  <!-- ADMIN 专属管理菜单 -->
                  <template v-if="role === 'ADMIN'">
                    <el-dropdown-item command="users" divided>用户管理</el-dropdown-item>
                    <el-dropdown-item command="ops">业务闭环</el-dropdown-item>
                  </template>

                  <!-- 只要不是 GUEST 都有课程管理（Uploader是创作者，Admin是审核） -->
                  <template v-if="role === 'ADMIN' || role === 'UPLOADER'">
                    <el-dropdown-item command="courses" divided>课程审核与发布</el-dropdown-item>
                  </template>

                  <!-- 登录用户都可以看作业相关 -->
                  <el-dropdown-item command="homework" divided>作业点评中心</el-dropdown-item>
                  
                  <!-- 个人中心 & 退出 -->
                  <el-dropdown-item command="profile" divided>个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button class="btn-login" @click="router.push('/login')">登录</el-button>
            <el-button class="btn-register" type="primary" @click="goRegister">注册</el-button>
          </template>

          <!-- 移动端菜单按钮 -->
          <button v-if="isMobile" class="mobile-menu-btn" @click="mobileOpen = !mobileOpen">
            <span class="bar" :class="{ open: mobileOpen }"></span>
          </button>
        </div>
      </div>

      <!-- 移动端下拉菜单 -->
      <transition name="mobile-nav">
        <div v-if="isMobile && mobileOpen" class="mobile-nav">
          <a class="mobile-nav-link" @click="go('/home')">首页</a>
          <a class="mobile-nav-link" @click="go('/courses')">精品课程</a>
          <a class="mobile-nav-link" @click="go('/store')">作品广场</a>
          <a class="mobile-nav-link" @click="go('/ai')">AI 辅学</a>
          <div class="mobile-nav-actions" v-if="!isLoggedIn">
            <el-button size="small" @click="go('/login')">登录</el-button>
            <el-button size="small" type="primary" @click="goRegister">注册</el-button>
          </div>
        </div>
      </transition>
    </header>

    <!-- 页面内容 -->
    <main class="pub-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const isLoggedIn = ref(!!localStorage.getItem('token'))
const nickname = ref(localStorage.getItem('nickname') || localStorage.getItem('username') || '用户')
const avatar = ref(localStorage.getItem('avatar') || DEFAULT_AVATAR)
const role = ref(localStorage.getItem('role') || 'GUEST')
const isMobile = ref(false)
const mobileOpen = ref(false)
const isScrolled = ref(false)

const checkMobile = () => { isMobile.value = window.innerWidth <= 768 }
const onScroll = () => { isScrolled.value = window.scrollY > 40 }

const go = (path) => {
  mobileOpen.value = false
  router.push(path)
}

const goRegister = () => {
  router.push({ path: '/login', query: { tab: 'register' } })
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    ['token', 'username', 'nickname', 'avatar', 'role'].forEach(k => localStorage.removeItem(k))
    isLoggedIn.value = false
    router.push('/home')
  } else if (['dashboard', 'users', 'courses', 'homework', 'ops', 'profile'].includes(cmd)) {
    router.push(`/${cmd}`)
  }
}

const onStorageChange = () => {
  isLoggedIn.value = !!localStorage.getItem('token')
  nickname.value = localStorage.getItem('nickname') || localStorage.getItem('username') || '用户'
  avatar.value = localStorage.getItem('avatar') || DEFAULT_AVATAR
  role.value = localStorage.getItem('role') || 'GUEST'
}

onMounted(() => {
  checkMobile()
  onScroll()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('scroll', onScroll)
  window.addEventListener('storage', onStorageChange)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('storage', onStorageChange)
})
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  background: #fff;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* -------- Header -------- */
.pub-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  height: 64px;
  background: #ffffff; /* 最上方时纯白 */
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s, background 0.3s;
}

.pub-header.scrolled {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.pub-header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 32px;
}

.pub-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
  text-decoration: none;
}

.pub-logo-icon {
  font-size: 24px;
}

.pub-logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #1d2b64, #4e4376);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.pub-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-link {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 15px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  font-weight: 500;
}

.nav-link:hover, .nav-link.active {
  color: #1d2b64;
  background: rgba(29, 43, 100, 0.07);
}

.pub-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.btn-login {
  font-size: 14px;
  border-color: #d0d0d0;
  color: #333;
}

.btn-register {
  font-size: 14px;
  background: linear-gradient(135deg, #1d2b64, #4e4376);
  color: #fff;
  border: none;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 20px;
  background: #f5f5f7;
  transition: background 0.2s;
}

.user-chip:hover {
  background: #ebebef;
}

.chip-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* -------- Mobile burger -------- */
.mobile-menu-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.bar {
  display: block;
  width: 22px;
  height: 2px;
  background: #333;
  border-radius: 2px;
  transition: all 0.3s;
}

/* -------- Mobile nav dropdown -------- */
.mobile-nav {
  background: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 12px 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mobile-nav-link {
  padding: 10px 8px;
  font-size: 15px;
  color: #333;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.mobile-nav-link:hover {
  background: #f5f5f7;
}

.mobile-nav-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.mobile-nav-enter-active, .mobile-nav-leave-active { transition: all 0.25s ease; }
.mobile-nav-enter-from, .mobile-nav-leave-to { opacity: 0; transform: translateY(-8px); }

/* -------- Main -------- */
.pub-main {
  padding-top: 64px;
  min-height: 100vh;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
