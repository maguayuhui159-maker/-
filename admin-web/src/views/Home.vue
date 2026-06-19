<template>
  <div class="home-page">
    <!-- Hero 版块 -->
    <section class="hero-section">
      <div class="hero-bg"></div>
      <div class="hero-bg-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">传承数字温度，<br>让锡雕技艺焕发新生</h1>
        <p class="hero-subtitle">全域管理的非物质文化遗产数字化传承与商业化变现平台</p>
        <div class="hero-actions">
          <el-button class="btn-start" size="large" @click="goStart">开始探索</el-button>
          <el-button class="btn-learn" size="large" plain @click="scrollToFeatures">了解更多</el-button>
        </div>
      </div>
    </section>

    <!-- 平台特色版块 -->
    <section id="features" class="features-section">
      <h2 class="section-title">核心驱动力</h2>
      
      <div class="feature-showcase showcase-right">
        <div class="showcase-content glass-card">
          <div class="feature-icon"><el-icon><VideoPlay /></el-icon></div>
          <h3 class="feature-title">体系化名师工坊传承</h3>
          <p class="feature-desc">打破时间与地域限制，由国家级非遗传承人与高级工艺美术师亲自授课。通过多视角高清录播与阶段性闯关练习，让古老技艺随时随地沉浸式融入现代人的生活之中。</p>
        </div>
        <div class="showcase-image">
          <img src="/images/feature_education.png" alt="非遗教学传承" class="parallax-img" />
        </div>
      </div>

      <div class="feature-showcase showcase-left">
        <div class="showcase-image">
          <img src="/images/feature_commercial.png" alt="非遗商业变现" class="parallax-img" />
        </div>
        <div class="showcase-content glass-card">
          <div class="feature-icon"><el-icon><Goods /></el-icon></div>
          <h3 class="feature-title">现代审美的高端商业化</h3>
          <p class="feature-desc">传承不是守旧。平台提供从作品创意孵化到最终线上展厅全品类覆盖。学员与创作者的优秀实物可通过作品广场直达高端市场，实现传统文化基因与现代商业审美的有机闭环。</p>
        </div>
      </div>
      
      <div class="feature-showcase showcase-center">
        <div class="showcase-content glass-card wide-card">
          <div class="feature-icon"><el-icon><Cpu /></el-icon></div>
          <h3 class="feature-title">AI 大模型灵感智能辅学</h3>
          <p class="feature-desc">全面集成先进的人工智能算法。不仅能即时解答锡雕退火、錾刻等工艺难题，更能根据学员的一句话提示自动生成融合了传统云纹、回纹的创新现代首饰草图，大幅缩短创作周期。</p>
        </div>
      </div>
    </section>

    <!-- 传世技艺长廊 -->
    <section class="artisan-section">
      <div class="artisan-container">
        <div class="artisan-image-wrapper">
          <img src="/images/artisan_craft.png" alt="非遗技艺" class="artisan-img" />
          <div class="artisan-image-overlay"></div>
        </div>
        <div class="artisan-content glass-card">
          <h2 class="artisan-title">千锤百炼 · 传世之美</h2>
          <p class="artisan-desc">一件顶级的锡雕器物，需历经化锡、制模、铸造、锻打、车光、焊接、錾刻与抛光等十余道古法工序。在《匠脉》，您将通过多维数字化拆解，无限次近距离观摩国家级大师的每一抹雕痕与每一次锤击。</p>
          <div class="technique-tags">
            <span class="t-tag">退火锻打</span>
            <span class="t-tag">立体錾刻</span>
            <span class="t-tag">无缝焊接</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 数字传承足迹看板 -->
    <section class="data-milestone-section" ref="milestoneRef">
      <div class="milestone-grid">
        <div class="m-item">
          <div class="m-number">{{ Math.floor(m1) }}+</div>
          <div class="m-label">古法锡雕数字图纸</div>
        </div>
        <div class="m-item">
          <div class="m-number">{{ Math.floor(m2) }}</div>
          <div class="m-label">国家级/省级传承人入驻</div>
        </div>
        <div class="m-item">
          <div class="m-number">{{ Math.floor(m3) }}+</div>
          <div class="m-label">在线研学学徒</div>
        </div>
        <div class="m-item">
          <div class="m-number">{{ Math.floor(m4) }}万</div>
          <div class="m-label">次 AI 灵感交互演算</div>
        </div>
      </div>
    </section>

    <!-- 课程预览版块 -->
    <section class="courses-preview">
      <div class="section-header">
        <h2 class="section-title">精品锡雕课程</h2>
        <a class="view-more" @click="router.push('/courses')">查看全部 <el-icon><ArrowRight /></el-icon></a>
      </div>
      <div v-if="courses.length > 0" class="course-carousel">
        <el-carousel :interval="4000" type="card" height="340px">
          <el-carousel-item v-for="course in courses" :key="course.id">
            <div class="course-card" @click="router.push('/courses')">
              <div class="course-cover">
                <el-image :src="course.coverUrl" fit="cover" class="cover-img">
                  <template #error><div class="image-placeholder"><el-icon><Picture /></el-icon></div></template>
                </el-image>
                <div class="course-price">¥{{ course.price }}</div>
              </div>
              <div class="course-info">
                <h3 class="course-title">{{ course.title }}</h3>
                <p class="course-desc">{{ course.description }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <div v-if="courses.length === 0" class="empty-state">
        <el-icon class="empty-icon"><Box /></el-icon>
        <p>此间静谧，尚无课程展出</p>
      </div>
    </section>

    <!-- CTA 版块 -->
    <section v-if="!isLoggedIn" class="cta-section">
      <div class="cta-box">
        <h2 class="cta-title">加入匠脉，成为非遗传承人</h2>
        <p class="cta-desc">现在注册即可体验完整课程学习、AI绘画辅导与作品展示特权</p>
        <el-button class="btn-register-cta" size="large" @click="router.push({ path: '/login', query: { tab: 'register' }})">
          免费注册体验
        </el-button>
      </div>
    </section>

    <!-- Footer -->
    <footer class="pub-footer">
      <div class="footer-content">
        <div class="footer-logo">⚒ 匠脉锡雕</div>
        <p class="footer-text">© 2026 匠脉锡雕数字化传承平台. All Rights Reserved.</p>
        <div class="footer-links">
          <a @click="router.push('/agreement')">用户协议</a>
          <span class="divider">|</span>
          <a @click="router.push('/agreement')">隐私政策</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCourseList } from '../api/course'

const router = useRouter()
const courses = ref([])
const isLoggedIn = ref(false)

// 数据看板动画控制
const milestoneRef = ref(null)
const m1 = ref(0)
const m2 = ref(0)
const m3 = ref(0)
const m4 = ref(0)
let observer = null

const startCounterAnimation = () => {
  const duration = 2000
  const frames = 60
  const interval = duration / frames
  let currentFrame = 0

  const timer = setInterval(() => {
    currentFrame++
    const progress = currentFrame / frames
    m1.value = 1200 * progress
    m2.value = 15 * progress
    m3.value = 35000 * progress
    m4.value = 18 * progress

    if (currentFrame >= frames) {
      clearInterval(timer)
      m1.value = 1200
      m2.value = 15
      m3.value = 35000
      m4.value = 18
    }
  }, interval)
}

const loadCourses = async () => {
  try {
    const res = await fetchCourseList()
    courses.value = (res.data || []).slice(0, 6)
  } catch {
    courses.value = []
  }
}

const goStart = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/dashboard')
  } else {
    router.push('/courses')
  }
}

const scrollToFeatures = () => {
  const el = document.getElementById('features')
  if (el) {
    const y = el.getBoundingClientRect().top + window.scrollY - 80
    window.scrollTo({ top: y, behavior: 'smooth' })
  }
}

onMounted(() => {
  // 实时判断登录态
  isLoggedIn.value = !!localStorage.getItem('token')
  loadCourses()

  // 监听数据区滚动
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      startCounterAnimation()
      observer.disconnect() // 仅执行一次
    }
  }, { threshold: 0.2 })
  if (milestoneRef.value) {
    observer.observe(milestoneRef.value)
  }
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.home-page {
  width: 100%;
  overflow-x: hidden;
  background-color: #0b0c10;
  color: #c5c6c7;
}

/* --- Hero Section --- */
.hero-section {
  position: relative;
  height: 85vh;
  min-height: 600px;
  max-height: 900px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background-image: url('/images/hero_bg.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  transform: scale(1.05);
  z-index: 0;
}

.hero-bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11,12,16,0.5) 0%, rgba(11,12,16,1) 100%);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 900px;
  padding: 0 20px;
  animation: fadeInUp 1s cubic-bezier(0.165, 0.84, 0.44, 1) forwards;
}

.hero-title {
  font-size: 52px;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 24px;
  letter-spacing: 2px;
  text-shadow: 0 4px 20px rgba(0,0,0,0.8);
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 40px;
  line-height: 1.6;
  color: #c5c6c7;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn-start {
  background: #66fcf1;
  border: none;
  color: #0b0c10;
  font-weight: 700;
  padding: 0 40px;
  border-radius: 30px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.btn-start:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 252, 241, 0.4);
}

.btn-learn {
  background: transparent;
  border: 2px solid rgba(255,255,255,0.4);
  color: white;
  font-weight: 600;
  padding: 0 40px;
  border-radius: 30px;
  transition: all 0.3s;
  backdrop-filter: blur(5px);
}

.btn-learn:hover {
  background: rgba(255,255,255,1);
  color: #0b0c10;
  border-color: #fff;
}

/* --- Features --- */
.features-section {
  padding: 100px 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

.section-title {
  text-align: center;
  font-size: 38px;
  font-weight: 800;
  color: #f5f5f7;
  margin-bottom: 80px;
  position: relative;
  letter-spacing: 2px;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #45a29e, #66fcf1);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(102, 252, 241, 0.5);
}

.feature-showcase {
  display: flex;
  align-items: center;
  gap: 60px;
  margin-bottom: 100px;
}

.feature-showcase.showcase-right, .feature-showcase.showcase-left {
  flex-direction: row;
}

.feature-showcase.showcase-center {
  justify-content: center;
  margin-bottom: 60px;
}

.showcase-image {
  flex: 1;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0,0,0,0.5);
  position: relative;
  height: 450px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.parallax-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.8s cubic-bezier(0.25, 1, 0.5, 1);
}

.showcase-image:hover .parallax-img {
  transform: scale(1.08); /* 深邃悬浮视差 */
}

.showcase-content {
  flex: 1;
  padding: 40px;
}

.glass-card {
  background: rgba(31, 40, 51, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  transition: transform 0.4s ease, box-shadow 0.4s ease, border-color 0.4s ease;
}

.glass-card.wide-card {
  max-width: 800px;
  text-align: center;
}

.glass-card.wide-card .feature-icon {
  margin: 0 auto 24px auto;
}

.glass-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.6);
  border-color: rgba(102, 252, 241, 0.4);
}

.feature-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #1f2833 0%, #0b0c10 100%);
  border: 1px solid rgba(102, 252, 241, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #66fcf1;
  margin-bottom: 24px;
  box-shadow: inset 0 0 20px rgba(102, 252, 241, 0.1);
}

.feature-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #fff;
  line-height: 1.3;
}

.feature-desc {
  color: #8892b0;
  line-height: 1.8;
  font-size: 16px;
}

/* --- Artisan Section (传世工艺长廊) --- */
.artisan-section {
  padding: 100px 20px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
}

.artisan-container {
  display: flex;
  align-items: center;
  position: relative;
  height: 600px;
}

.artisan-image-wrapper {
  width: 70%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  border-radius: 20px;
  overflow: hidden;
  -webkit-mask-image: linear-gradient(to right, black 50%, transparent 100%);
  mask-image: linear-gradient(to right, black 50%, transparent 100%);
}

.artisan-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 1s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.artisan-image-wrapper:hover .artisan-img {
  transform: scale(1.05);
}

.artisan-image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(11,12,16,0.2) 0%, rgba(11,12,16,0.8) 100%);
}

.artisan-content {
  position: absolute;
  right: 0;
  width: 500px;
  padding: 60px 50px;
  background: rgba(31, 40, 51, 0.6) !important;
  backdrop-filter: blur(24px) !important;
  -webkit-backdrop-filter: blur(24px) !important;
  border: 1px solid rgba(102, 252, 241, 0.15) !important;
  box-shadow: -20px 20px 50px rgba(0,0,0,0.8), inset 0 0 30px rgba(102, 252, 241, 0.05) !important;
  transform: translateY(20px);
  z-index: 2;
  transition: all 0.5s;
}

.artisan-content:hover {
  transform: translateY(0);
  border-color: rgba(102, 252, 241, 0.4) !important;
  box-shadow: -20px 30px 60px rgba(0,0,0,0.9), inset 0 0 30px rgba(102, 252, 241, 0.1) !important;
}

.artisan-title {
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 24px;
  background: linear-gradient(90deg, #fff, #66fcf1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}

.artisan-desc {
  color: #c5c6c7;
  font-size: 16px;
  line-height: 2;
  margin-bottom: 30px;
}

.technique-tags {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.t-tag {
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid rgba(102, 252, 241, 0.3);
  color: #66fcf1;
  font-size: 14px;
  background: rgba(102, 252, 241, 0.05);
  box-shadow: 0 0 10px rgba(102, 252, 241, 0.1);
}

/* --- Data Milestone (数字典藏看板) --- */
.data-milestone-section {
  padding: 80px 20px;
  background: linear-gradient(180deg, transparent 0%, rgba(31, 40, 51, 0.3) 50%, transparent 100%);
  border-top: 1px solid rgba(255,255,255,0.02);
  border-bottom: 1px solid rgba(255,255,255,0.02);
  margin-bottom: 40px;
}

.milestone-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  text-align: center;
}

.m-item {
  position: relative;
  padding: 30px 20px;
}

.m-number {
  font-size: 56px;
  font-weight: 900;
  color: #fff;
  margin-bottom: 10px;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  text-shadow: 0 0 30px rgba(102, 252, 241, 0.4);
  background: linear-gradient(180deg, #ffffff 0%, #66fcf1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.m-label {
  font-size: 16px;
  color: #8892b0;
  letter-spacing: 1px;
  font-weight: 500;
}

/* --- Courses Preview --- */
.courses-preview {
  padding: 80px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
}

.section-header .section-title {
  margin-bottom: 0;
}

.section-header .section-title::after {
  left: 0;
  transform: none;
}

.view-more {
  color: #45a29e;
  font-size: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  text-decoration: none;
  transition: opacity 0.2s, color 0.2s;
}

.view-more:hover {
  color: #66fcf1;
  opacity: 1;
}

.course-carousel {
  width: 100%;
  padding: 10px 0 40px; /* 给下方指示器留空 */
}

/* 隐藏默认两边侧边栏避免挡住视网膜 */
:deep(.el-carousel__mask) {
  background-color: rgba(11, 12, 16, 0.7);
  backdrop-filter: blur(2px);
}

:deep(.el-carousel__indicators--outside button) {
  background-color: #45a29e;
  opacity: 0.4;
  height: 4px;
  border-radius: 2px;
}

:deep(.el-carousel__indicator.is-active button) {
  background-color: #66fcf1;
  opacity: 1;
  box-shadow: 0 0 10px rgba(102, 252, 241, 0.6);
}

/* 轮播内部卡片铺满 */
.course-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(31, 40, 51, 0.9);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  cursor: pointer;
  transition: transform 0.4s, box-shadow 0.4s, border-color 0.4s;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
}

.course-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.5);
  border-color: rgba(69, 162, 158, 0.5);
}

.course-cover {
  position: relative;
  flex: 1; /* 图片占据剩余所有空间 */
  min-height: 200px;
  width: 100%;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background: #1f2833;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #45a29e;
}

.course-price {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0,0,0,0.8);
  color: #66fcf1;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 14px;
  border: 1px solid rgba(102, 252, 241, 0.3);
  backdrop-filter: blur(5px);
}

.course-info {
  padding: 20px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 10px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-desc {
  font-size: 14px;
  color: #8892b0;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

/* --- CTA Section --- */
.cta-section {
  padding: 80px 20px;
}

.cta-box {
  max-width: 1000px;
  margin: 0 auto;
  background: linear-gradient(135deg, rgba(31, 40, 51, 0.9) 0%, rgba(11, 12, 16, 0.9) 100%);
  border-radius: 24px;
  padding: 60px 40px;
  text-align: center;
  color: white;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(69, 162, 158, 0.3);
  position: relative;
  overflow: hidden;
}

.cta-box::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102,252,241,0.05) 0%, transparent 60%);
  pointer-events: none;
}

.cta-title {
  font-size: 36px;
  margin-bottom: 16px;
  font-weight: 700;
  position: relative;
  z-index: 1;
}

.cta-desc {
  font-size: 18px;
  opacity: 0.8;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.btn-register-cta {
  background: #66fcf1;
  color: #0b0c10;
  border: none;
  padding: 0 40px;
  font-size: 18px;
  font-weight: 700;
  border-radius: 30px;
  position: relative;
  z-index: 1;
  transition: all 0.3s;
}

.btn-register-cta:hover {
  background: #fff;
  transform: scale(1.05);
  box-shadow: 0 0 20px rgba(255,255,255,0.4);
}

/* --- Footer --- */
.pub-footer {
  background: #000;
  color: #8892b0;
  padding: 40px 20px 60px;
  text-align: center;
  border-top: 1px solid rgba(255,255,255,0.05);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.footer-logo {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  letter-spacing: 2px;
}

.footer-links {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.footer-links a {
  color: #8892b0;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #66fcf1;
}

.divider {
  opacity: 0.3;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 992px) {
  :deep(.el-carousel__container) {
    height: 300px !important;
  }
  .feature-showcase.showcase-right, .feature-showcase.showcase-left {
    flex-direction: column;
    gap: 40px;
  }
  .showcase-image {
    width: 100%;
    height: 350px;
  }
  .artisan-image-wrapper { width: 100%; -webkit-mask-image: none; opacity: 0.3; }
  .artisan-content { position: relative; width: 100%; transform: none; box-shadow: none !important;}
  .milestone-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .hero-title { font-size: 36px; }
  :deep(.el-carousel__container) {
    height: 260px !important;
  }
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
