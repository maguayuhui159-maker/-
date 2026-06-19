<template>
  <div class="gallery-container">
    <!-- 顶部高奢横幅 -->
    <div class="page-hero" style="background-image: url('/images/course_banner.png');">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">匠心工坊</h1>
        <p class="hero-subtitle">探索国家级非遗名师的巅峰技艺</p>
        <el-button v-if="isLoggedIn && role === 'UPLOADER'" class="action-btn upload-btn" type="primary" size="large" @click="handleCreate" plain>
          发 布 新 课 程
        </el-button>
      </div>
    </div>

    <!-- 画廊主体区域 -->
    <div class="gallery-main" v-loading="listLoading">
      <div v-if="tableData.length === 0 && !listLoading" class="empty-state">
        <el-icon class="empty-icon"><Box /></el-icon>
        <p>此间静谧，尚无课程展出</p>
      </div>

      <div class="gallery-grid" v-else>
        <div class="course-card" v-for="row in tableData" :key="row.id">
          <!-- 课程媒体区 (视频或封面) -->
          <div class="card-media">
            <!-- 播放器优先展示 -->
            <iframe
              v-if="isBilibiliVideoUrl(row.videoUrl)"
              :src="toBilibiliEmbedUrl(row.videoUrl)"
              frameborder="0"
              allowfullscreen
              class="media-player"
            ></iframe>
            <video
              v-else-if="row.videoUrl"
              :src="row.videoUrl"
              controls
              preload="metadata"
              class="media-player"
            ></video>
            <!-- 只有封面时 -->
            <el-image v-else class="media-cover" :src="row.coverUrl" fit="cover">
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                  <span>无影像</span>
                </div>
              </template>
            </el-image>

            <!-- 状态角标 -->
            <div class="status-badge" :class="'badge-' + row.status.toLowerCase()">
              {{ statusLabel(row.status) }}
            </div>
          </div>

          <!-- 卡片信息区 -->
          <div class="card-info">
            <h3 class="course-title">{{ row.title }}</h3>
            <p class="course-desc">{{ row.description }}</p>

            <div class="card-footer">
              <div class="price-box">
                <span class="currency">¥</span>
                <span class="amount">{{ row.price }}</span>
              </div>
              
              <!-- 客户端交互区 -->
              <div class="action-box">
                <template v-if="!isLoggedIn">
                  <el-button type="info" size="small" @click="handleGuestAction" round>解锁</el-button>
                </template>
                <template v-else-if="role === 'STUDENT'">
                  <div class="student-action">
                    <span class="progress-text" v-if="isPurchased(row.id)">进度 {{ learningMap[row.id]?.progress || 0 }}%</span>
                    <el-button
                      v-if="!isPurchased(row.id)"
                      type="success"
                      size="small"
                      @click="handlePurchase(row)"
                      round
                    >
                      典藏课程
                    </el-button>
                    <el-button
                      v-else
                      type="primary"
                      size="small"
                      @click="openLearningDialog(row)"
                      round
                    >
                      研 习
                    </el-button>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- (后台管控) 管理员悬浮涂层：完全隐式 -->
          <div class="admin-overlay" v-if="isLoggedIn && (role === 'ADMIN' || (role === 'UPLOADER' && row.status !== 'PUBLISHED'))">
            <div class="admin-tools">
              <el-button v-if="role === 'UPLOADER'" type="primary" size="small" icon="Edit" circle title="编辑草稿" />
              <template v-if="role === 'ADMIN' && row.status === 'PENDING'">
                <el-button type="success" size="small" icon="Check" circle @click="handleReview(row, 'PUBLISHED')" title="御批通过" />
                <el-button type="danger" size="small" icon="Close" circle @click="handleReview(row, 'REJECTED')" title="驳回" />
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialog for Create -->
    <el-dialog title="发布新课程" v-model="dialogFormVisible">
      <el-form ref="dataForm" :model="temp" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="temp.title" />
        </el-form-item>
        <el-form-item label="课程简介" prop="description">
          <el-input type="textarea" v-model="temp.description" />
        </el-form-item>
        <el-form-item label="课程封面" prop="coverUrl">
          <el-upload
            class="cover-uploader"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleCoverChange"
            accept="image/*"
          >
            <el-image v-if="temp.coverUrl" :src="temp.coverUrl" style="width: 178px; height: 120px" fit="cover" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击上方区域选择图片</div>
        </el-form-item>
        <el-form-item label="课程视频" prop="videoUrl">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleVideoChange"
            accept="video/*"
          >
            <el-button type="primary" plain>选择视频文件</el-button>
          </el-upload>
          <div class="upload-tip">
            {{ videoFile ? `已选择: ${videoFile.name}` : '支持 mp4/webm/mov，最大 200MB' }}
          </div>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="temp.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="createData()" :loading="submitting">发布</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="学习进度更新" v-model="learningDialogVisible" width="500px">
      <el-form label-width="120px">
        <el-form-item label="课程名称">
          <span>{{ currentLearningCourse.title || '-' }}</span>
        </el-form-item>
        <el-form-item label="当前进度">
          <el-slider v-model="learningForm.progress" :min="0" :max="100" :step="1" show-input />
        </el-form-item>
        <el-form-item label="本次学习时长(分)">
          <el-input-number v-model="learningForm.studyMinutes" :min="0" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="learningDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="learningSubmitting" @click="submitLearningProgress">保存进度</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  fetchCourseList,
  fetchAllCourseList,
  fetchMyUploadedCourses,
  fetchMyLearningCourses,
  purchaseCourse,
  updateLearningProgress,
  uploadCourse,
  uploadCourseVideo
} from '../api/course'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const isLoggedIn = ref(!!localStorage.getItem('token'))
const role = ref(localStorage.getItem('role') || 'STUDENT')
const listLoading = ref(false)
const tableData = ref([])
const dialogFormVisible = ref(false)
const submitting = ref(false)
const coverFile = ref(null)
const videoFile = ref(null)
const learningDialogVisible = ref(false)
const learningSubmitting = ref(false)
const currentLearningCourse = ref({})
const learningMap = reactive({})
const learningForm = reactive({
  courseId: null,
  progress: 0,
  studyMinutes: 30
})

const temp = reactive({
  title: '',
  description: '',
  coverUrl: '',
  videoUrl: '',
  price: 0.00,
  status: 'PENDING'
})

const statusLabel = (status) => {
  const map = { PUBLISHED: '已发布', PENDING: '待审核', DRAFT: '草稿', REJECTED: '已驳回', OFFLINE: '已下架' }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { PUBLISHED: 'success', PENDING: 'warning', DRAFT: 'info', REJECTED: 'danger', OFFLINE: 'info' }
  return map[status] || 'info'
}

const isBilibiliVideoUrl = (url) => /bilibili\.com\/video\/BV[0-9A-Za-z]+/i.test(String(url || ''))

const toBilibiliEmbedUrl = (url) => {
  const match = String(url || '').match(/bilibili\.com\/video\/(BV[0-9A-Za-z]+)/i)
  return match ? `https://player.bilibili.com/player.html?bvid=${match[1]}&page=1` : ''
}

const getList = () => {
  listLoading.value = true
  const fetcher = role.value === 'ADMIN'
    ? fetchAllCourseList
    : role.value === 'UPLOADER'
      ? fetchMyUploadedCourses
      : fetchCourseList
  fetcher().then(async (res) => {
    tableData.value = res.data || []
    if (role.value === 'STUDENT') {
      await loadMyLearningCourses()
    }
    listLoading.value = false
  }).catch(async (error) => {
    tableData.value = []
    if (role.value === 'STUDENT') {
      await loadMyLearningCourses()
    }
    listLoading.value = false
    ElMessage.error(error?.response?.data?.message || error?.message || '课程加载失败')
  })
}

const loadMyLearningCourses = async () => {
  if (role.value !== 'STUDENT') return
  try {
    const res = await fetchMyLearningCourses()
    Object.keys(learningMap).forEach((k) => delete learningMap[k])
    ;(res.data || []).forEach((item) => {
      learningMap[item.courseId] = item
    })
  } catch {
    Object.keys(learningMap).forEach((k) => delete learningMap[k])
  }
}

const isPurchased = (courseId) => Boolean(learningMap[courseId])

const handleGuestAction = () => {
  ElMessageBox.confirm(
    '您当前处于游客模式，登录后即可购买课程并记录学习进度。是否现在去登录？',
    '提示',
    {
      confirmButtonText: '去登录',
      cancelButtonText: '先看看',
      type: 'info'
    }
  ).then(() => {
    router.push('/login')
  }).catch(() => {})
}

const handlePurchase = async (row) => {
  try {
    const res = await purchaseCourse(row.id)
    ElMessage.success(res.message || '购买成功')
    await loadMyLearningCourses()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '购买失败')
  }
}

const openLearningDialog = (row) => {
  const record = learningMap[row.id] || {}
  currentLearningCourse.value = row
  learningForm.courseId = row.id
  learningForm.progress = Number(record.progress || 0)
  learningForm.studyMinutes = 30
  learningDialogVisible.value = true
}

const submitLearningProgress = async () => {
  if (!learningForm.courseId) return
  learningSubmitting.value = true
  try {
    const res = await updateLearningProgress({
      courseId: learningForm.courseId,
      progress: learningForm.progress,
      studyMinutes: learningForm.studyMinutes
    })
    ElMessage.success(res.message || '学习进度已保存')
    learningDialogVisible.value = false
    await loadMyLearningCourses()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '保存失败')
  } finally {
    learningSubmitting.value = false
  }
}

const handleReview = (row, newStatus) => {
  request({
    url: `/api/courses/${row.id}/review`,
    method: 'put',
    data: { status: newStatus }
  }).then(() => {
    ElMessage.success(newStatus === 'PUBLISHED' ? '课程已通过审核' : '课程已驳回')
    getList()
  }).catch((error) => {
    ElMessage.error(error?.response?.data?.message || error?.message || '课程审核失败')
  })
}

onMounted(() => {
  getList()
})

const handleCreate = () => {
  Object.assign(temp, {
    title: '',
    description: '',
    coverUrl: '',
    videoUrl: '',
    price: 0,
    status: 'PENDING'
  })
  coverFile.value = null
  videoFile.value = null
  dialogFormVisible.value = true
}

const handleCoverChange = (file) => {
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return
  }
  const isLt5M = file.raw.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return
  }
  coverFile.value = file.raw
  // 将图片转为 base64 存储，这样 localStorage 可以持久化
  const reader = new FileReader()
  reader.onload = (e) => {
    temp.coverUrl = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleVideoChange = (file) => {
  const raw = file?.raw
  if (!raw) return
  const isVideo = String(raw.type || '').startsWith('video/')
  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return
  }
  const isLt200M = raw.size / 1024 / 1024 < 200
  if (!isLt200M) {
    ElMessage.error('视频大小不能超过 200MB!')
    return
  }
  videoFile.value = raw
}

const createData = () => {
  if (!temp.title) {
    ElMessage.warning('请输入标题')
    return
  }
  submitting.value = true

  const submit = async () => {
    const data = { ...temp }
    if (videoFile.value) {
      const uploadRes = await uploadCourseVideo(videoFile.value)
      data.videoUrl = uploadRes?.data?.url || ''
      temp.videoUrl = data.videoUrl
    }
    return uploadCourse(data)
  }

  submit().then((res) => {
    ElMessage.success(res.message || '发布成功')
    dialogFormVisible.value = false
    getList()
  }).catch((error) => {
    const backendMessage = error?.response?.data?.message
    ElMessage.error(backendMessage || '发布失败，请检查后端服务与数据库配置')
  }).finally(() => {
    submitting.value = false
  })
}
</script>

<style scoped>
.gallery-container {
  min-height: 100vh;
  background-color: #0b0c10;
  color: #c5c6c7;
  padding-bottom: 60px;
}

/* 顶部画廊横幅 */
.page-hero {
  position: relative;
  height: 480px;
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(11, 12, 16, 0.2) 0%, rgba(11, 12, 16, 1) 100%);
}

.hero-content {
  position: relative;
  z-index: 2;
  animation: fadeInUp 1s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  letter-spacing: 4px;
  color: #fff;
  margin: 0 0 16px 0;
  text-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

.hero-subtitle {
  font-size: 20px;
  color: rgba(255,255,255,0.8);
  letter-spacing: 2px;
  margin-bottom: 40px;
}

.upload-btn {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.4);
  color: #fff;
  backdrop-filter: blur(5px);
  border-radius: 30px;
  padding: 0 40px;
  transition: all 0.3s;
}

.upload-btn:hover {
  background: #fff;
  color: #0b0c10;
  border-color: #fff;
  transform: scale(1.05);
}

/* 画廊主体 */
.gallery-main {
  max-width: 1400px;
  margin: -60px auto 0;
  padding: 0 20px;
  position: relative;
  z-index: 10;
}

.empty-state {
  text-align: center;
  padding: 100px 0;
  color: #66fcf1;
  opacity: 0.6;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

/* 瀑布流网络布局 */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 40px;
}

/* 课程卡片拟态 */
.course-card {
  background: rgba(31, 40, 51, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: transform 0.4s ease, box-shadow 0.4s ease;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.4);
  border-color: rgba(102, 252, 241, 0.4);
}

.card-media {
  position: relative;
  aspect-ratio: 16 / 9;
  background: #000;
  overflow: hidden;
}

.media-player {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-cover {
  width: 100%;
  height: 100%;
  transition: transform 0.6s;
}

.course-card:hover .media-cover {
  transform: scale(1.08); /* 悬浮视差 */
}

.image-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #45a29e;
  font-size: 14px;
  background: #1f2833;
}

.image-slot .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

/* 透明状态角标 */
.status-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  backdrop-filter: blur(10px);
  background: rgba(0,0,0,0.5);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.2);
}

.badge-published { color: #66fcf1; border-color: rgba(102,252,241,0.5); }
.badge-pending { color: #f2a900; border-color: rgba(242,169,0,0.5); }
.badge-rejected { color: #ff4b4b; border-color: rgba(255,75,75,0.5); }

/* 信息文字区 */
.card-info {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-title {
  font-size: 20px;
  color: #fff;
  margin: 0 0 12px 0;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}

.course-desc {
  font-size: 14px;
  color: #8892b0;
  line-height: 1.6;
  flex: 1;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  margin-top: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-box {
  color: #66fcf1;
  font-weight: 600;
}

.currency { font-size: 14px; margin-right: 2px; }
.amount { font-size: 24px; font-family: "Georgia", serif; }

.student-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 12px;
  color: #45a29e;
  background: rgba(69, 162, 158, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
}

/* 隐式管理悬浮层 */
.admin-overlay {
  position: absolute;
  inset: 0;
  background: rgba(11, 12, 16, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 10;
}

/* 只有把鼠标移到卡片上才会显露管理工具 */
.course-card:hover .admin-overlay {
  opacity: 1;
  visibility: visible;
}

.admin-tools {
  display: flex;
  gap: 16px;
  transform: translateY(20px);
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.course-card:hover .admin-tools {
  transform: translateY(0);
}

/* 其余弹窗修复 */
.cover-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

@media (max-width: 768px) {
  .hero-title { font-size: 36px; }
  .gallery-grid { grid-template-columns: 1fr; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
