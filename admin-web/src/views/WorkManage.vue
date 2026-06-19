<template>
  <div class="gallery-container">
    <!-- 顶部艺术殿堂横幅 -->
    <div class="page-hero" style="background-image: url('/images/store_banner.png');">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">数字珍藏展厅</h1>
        <p class="hero-subtitle">发现与收藏极具现代审美流动的古老金属</p>
        <el-button v-if="role !== 'ADMIN'" class="action-btn upload-btn" type="primary" size="large" @click="handleCreate" plain>
          提 交 新 作 品
        </el-button>
      </div>
    </div>

    <!-- 画廊主体区域 -->
    <div class="gallery-main" v-loading="listLoading">
      <div v-if="tableData.length === 0 && !listLoading" class="empty-state">
        <el-icon class="empty-icon"><PictureRounded /></el-icon>
        <p>展厅空空如也，静候佳作挂壁</p>
      </div>

      <div class="gallery-grid" v-else>
        <div class="work-card" v-for="row in tableData" :key="row.id">
          <!-- 艺术品影像区 -->
          <div class="card-media clickable" @click="openPreview(row)">
            <el-image class="media-cover" :src="row.imageUrl" fit="cover">
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

          <!-- 艺术品信息区 -->
          <div class="card-info">
            <h3 class="work-title clickable" @click="openPreview(row)">{{ row.title }}</h3>
            <p class="work-desc">{{ row.description }}</p>

            <div class="card-footer">
              <div class="price-box" v-if="row.isForSale === 1">
                <span class="currency">¥</span>
                <span class="amount">{{ row.price }}</span>
              </div>
              <div class="price-box not-for-sale" v-else>
                <span class="tag">非卖展示品</span>
              </div>
              
              <!-- 客户端点阅/购买 -->
              <div class="action-box" v-if="role === 'STUDENT'">
                <el-button
                  class="card-action-btn"
                  :type="isFavorited(row.id) ? 'danger' : 'default'"
                  size="small"
                  @click="handleFavorite(row)"
                  round
                >
                  {{ isFavorited(row.id) ? '取消收藏' : '收藏此作' }}
                </el-button>
                <el-button
                  v-if="row.isForSale === 1"
                  class="card-action-btn"
                  type="success"
                  size="small"
                  @click="handleOrder(row)"
                  round
                >
                  立即下单
                </el-button>
                <el-button v-else class="card-action-btn" type="info" size="small" disabled round>仅供观赏</el-button>
              </div>
            </div>
          </div>

          <!-- (后台管控) 管理员悬浮涂层：完全隐式 -->
          <div class="admin-overlay" v-if="role === 'ADMIN'">
            <div class="admin-tools">
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" size="small" icon="Check" circle @click="handleReview(row, 'APPROVED')" title="通过展出" />
                <el-button type="danger" size="small" icon="Close" circle @click="handleReview(row, 'REJECTED')" title="拒绝收录" />
              </template>
              <template v-else>
                 <span class="review-done-msg">{{ statusLabel(row.status) }}结论已定</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialog for Upload -->
    <el-dialog title="提交新作品" v-model="dialogFormVisible">
      <el-form ref="dataForm" :model="temp" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="作品标题" prop="title">
          <el-input v-model="temp.title" />
        </el-form-item>
        <el-form-item label="工艺描述" prop="description">
          <el-input type="textarea" v-model="temp.description" />
        </el-form-item>
        <el-form-item label="作品图片" prop="imageUrl">
          <el-upload
            class="image-uploader"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
            accept="image/*"
          >
            <el-image v-if="temp.imageUrl" :src="temp.imageUrl" style="width: 178px; height: 140px" fit="cover" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击上方区域选择图片</div>
        </el-form-item>
        <el-form-item label="申请售卖" prop="isForSale">
          <el-switch v-model="temp.isForSale" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="标价" prop="price" v-if="temp.isForSale === 1">
          <el-input-number v-model="temp.price" :precision="2" :step="10" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitWorkData()" :loading="submitting">提交审核</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="作品详情" width="720px" class="preview-dialog">
      <div v-if="previewWork.id" class="preview-layout">
        <el-image class="preview-image" :src="previewWork.imageUrl" fit="cover">
          <template #error>
            <div class="image-slot"><span>无影像</span></div>
          </template>
        </el-image>
        <div class="preview-content">
          <h2>{{ previewWork.title }}</h2>
          <p>{{ previewWork.description || '这件作品暂时还没有补充介绍。' }}</p>
          <div class="preview-meta">
            <span>{{ previewWork.isForSale === 1 ? `售价 ¥${previewWork.price}` : '非卖展示品' }}</span>
            <span>{{ statusLabel(previewWork.status) }}</span>
          </div>
          <div class="preview-actions" v-if="role === 'STUDENT'">
            <el-button :type="isFavorited(previewWork.id) ? 'danger' : 'default'" round @click="handleFavorite(previewWork)">
              {{ isFavorited(previewWork.id) ? '取消收藏' : '收藏此作' }}
            </el-button>
            <el-button v-if="previewWork.isForSale === 1" type="success" round @click="handleOrder(previewWork)">立即下单</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { favoriteWork, fetchAllWorkList, fetchMyFavoriteWorks, fetchMyWorkList, fetchWorkList, recordWorkView, unfavoriteWork, uploadWork } from '../api/work'
import { createOrder } from '../api/order'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const role = ref(localStorage.getItem('role') || 'STUDENT')
const listLoading = ref(false)
const tableData = ref([])
const dialogFormVisible = ref(false)
const submitting = ref(false)
const imageFile = ref(null)
const favoriteWorkIds = ref([])
const previewVisible = ref(false)
const previewWork = reactive({})

const temp = reactive({
  title: '',
  description: '',
  imageUrl: '',
  isForSale: 0,
  price: 0.00
})

const statusLabel = (status) => {
  const map = { APPROVED: '已通过', PENDING: '待审核', REJECTED: '已拒绝' }
  return map[status] || status
}

const isFavorited = (workId) => favoriteWorkIds.value.includes(Number(workId))

const syncFavoriteIds = async () => {
  if (role.value !== 'STUDENT') {
    favoriteWorkIds.value = []
    return
  }
  try {
    const res = await fetchMyFavoriteWorks()
    favoriteWorkIds.value = (res.data || []).map((item) => Number(item.id)).filter((id) => Number.isFinite(id))
  } catch {
    favoriteWorkIds.value = []
  }
}

const getList = () => {
  listLoading.value = true
  const fetcher = role.value === 'ADMIN'
    ? fetchAllWorkList
    : role.value === 'UPLOADER'
      ? fetchMyWorkList
      : fetchWorkList
  fetcher().then(res => {
    tableData.value = res.data
    listLoading.value = false
  }).catch((error) => {
    tableData.value = []
    listLoading.value = false
    ElMessage.error(error?.response?.data?.message || error?.message || '作品加载失败')
  })
}

const handleFavorite = async (row) => {
  const workId = Number(row?.id)
  if (!Number.isFinite(workId)) {
    ElMessage.error('收藏失败：作品信息异常')
    return
  }
  try {
    if (isFavorited(workId)) {
      await unfavoriteWork(workId)
      favoriteWorkIds.value = favoriteWorkIds.value.filter((id) => id !== workId)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteWork(workId)
      favoriteWorkIds.value = [workId, ...favoriteWorkIds.value.filter((id) => id !== workId)]
      ElMessage.success('收藏成功，可在个人中心查看')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '收藏操作失败')
  }
}

const handleOrder = async (row) => {
  try {
    await createOrder(row.id)
    ElMessage.success('下单成功，可在业务闭环中心查看订单')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '下单失败')
  }
}

const handleReview = (row, newStatus) => {
  request({
    url: `/api/works/${row.id}/review`,
    method: 'put',
    data: { status: newStatus }
  }).then(() => {
    ElMessage.success(newStatus === 'APPROVED' ? '作品审核通过' : '作品已拒绝')
    getList()
  }).catch((error) => {
    ElMessage.error(error?.response?.data?.message || error?.message || '审核失败')
  })
}

onMounted(() => {
  syncFavoriteIds()
  getList()
})

const openPreview = async (row) => {
  Object.assign(previewWork, row)
  previewVisible.value = true
  try {
    await recordWorkView(row.id)
  } catch {
    // ignore preview tracking failures
  }
}

const handleCreate = () => {
  Object.assign(temp, {
    title: '',
    description: '',
    imageUrl: '',
    isForSale: 0,
    price: 0.00
  })
  imageFile.value = null
  dialogFormVisible.value = true
}

const handleImageChange = (file) => {
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
  imageFile.value = file.raw
  // 将图片转为 base64 存储
  const reader = new FileReader()
  reader.onload = (e) => {
    temp.imageUrl = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const submitWorkData = () => {
  if (!temp.title || !temp.imageUrl) {
    ElMessage.warning('需填写标题并上传图片')
    return
  }
  submitting.value = true
  const data = { ...temp }
  uploadWork(data).then((res) => {
    ElMessage.success(res.message || '上传成功，流转至待审核')
    dialogFormVisible.value = false
    getList()
  }).catch((error) => {
    ElMessage.error(error?.response?.data?.message || error?.message || '作品提交失败')
  }).finally(() => {
    submitting.value = false
  })
}
</script>

<style scoped>
.gallery-container {
  min-height: 100vh;
  background-color: #121212;
  color: #e0e0e0;
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
  background: linear-gradient(to bottom, rgba(18, 18, 18, 0.2) 0%, rgba(18, 18, 18, 1) 100%);
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
  color: #f5f5f7;
  margin: 0 0 16px 0;
  text-shadow: 0 10px 30px rgba(0,0,0,0.8);
}

.hero-subtitle {
  font-size: 20px;
  color: rgba(245,245,247,0.7);
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
  color: #121212;
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
  color: #d4af37; /* 金色点缀 */
  opacity: 0.6;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

/* 瀑布流网络布局 */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 40px;
}

/* 作品卡片拟态 */
.work-card {
  background: rgba(30, 30, 30, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: transform 0.4s ease, box-shadow 0.4s ease;
  display: flex;
  flex-direction: column;
}

.work-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.5);
  border-color: rgba(212, 175, 55, 0.4); /* 金色边框悬停 */
}

.card-media {
  position: relative;
  aspect-ratio: 4 / 3; /* 艺术品特写常用比例 */
  background: #1a1a1a;
  overflow: hidden;
}

.clickable {
  cursor: pointer;
}

.media-cover {
  width: 100%;
  height: 100%;
  transition: transform 0.8s cubic-bezier(0.25, 1, 0.5, 1);
}

.work-card:hover .media-cover {
  transform: scale(1.1); /* 悬浮缓慢放大视差 */
}

.image-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #d4af37;
  font-size: 14px;
  background: #1a1a1a;
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
  background: rgba(0,0,0,0.6);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.15);
}

.badge-approved { color: #d4af37; border-color: rgba(212, 175, 55, 0.5); }
.badge-pending { color: #888; border-color: rgba(136, 136, 136, 0.5); }
.badge-rejected { color: #ff4b4b; border-color: rgba(255, 75, 75, 0.5); }

/* 信息文字区 */
.card-info {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.work-title {
  font-size: 20px;
  color: #f5f5f7;
  margin: 0 0 12px 0;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}

.work-desc {
  font-size: 14px;
  color: #a1a1a6;
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
  gap: 12px;
}

.price-box {
  color: #d4af37;
  font-weight: 600;
}

.price-box.not-for-sale {
  color: #888;
}

.currency { font-size: 14px; margin-right: 2px; }
.amount { font-size: 24px; font-family: "Georgia", serif; }
.tag { font-size: 14px; font-weight: normal; letter-spacing: 1px;}

.action-box {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.card-action-btn {
  min-width: 86px;
}

.preview-layout {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 20px;
}

.preview-image {
  width: 100%;
  height: 360px;
  border-radius: 16px;
  overflow: hidden;
}

.preview-content h2 {
  margin: 0 0 12px;
  color: #f5f5f7;
}

.preview-content p {
  color: #c5c6c7;
  line-height: 1.8;
}

.preview-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 18px;
  color: #d4af37;
}

.preview-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
}

/* 隐式管理悬浮层 */
.admin-overlay {
  position: absolute;
  inset: 0;
  background: rgba(18, 18, 18, 0.8);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 10;
}

.work-card:hover .admin-overlay {
  opacity: 1;
  visibility: visible;
}

.admin-tools {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  transform: translateY(20px);
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.work-card:hover .admin-tools {
  transform: translateY(0);
}

.review-done-msg {
  color: #fff;
  font-size: 14px;
  letter-spacing: 1px;
}

/* 上传相关样式保留 */
.image-uploader :deep(.el-upload) {
  border: 1px dashed #444;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 140px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #d4af37;
}

@media (max-width: 768px) {
  .hero-title { font-size: 36px; }
  .gallery-grid { grid-template-columns: 1fr; }
  .card-footer { flex-direction: column; align-items: flex-start; }
  .action-box { width: 100%; justify-content: flex-start; }
  .preview-layout { grid-template-columns: 1fr; }
  .preview-image { height: 240px; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
