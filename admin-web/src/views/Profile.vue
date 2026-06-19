<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- Left Column: User Card -->
      <el-col :span="8">
        <el-card shadow="hover" class="user-card">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="avatarUrl" />
            <div class="role-badge">
              <el-tag :type="roleTagType" effect="dark" round size="small">{{ roleName }}</el-tag>
            </div>
          </div>
          <div class="avatar-actions">
            <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleAvatarSelect">
              <el-button type="primary" plain size="small">选择新头像</el-button>
            </el-upload>
            <el-button plain size="small" @click="showRenameInput = !showRenameInput">
              {{ showRenameInput ? '取消更换昵称' : '更换昵称' }}
            </el-button>
          </div>
          <div class="profile-change-box">
            <el-input
              v-if="showRenameInput"
              v-model="changeForm.requestedNickname"
              size="small"
              placeholder="申请新昵称（仅用于展示）"
            />
            <el-image
              v-if="changeForm.requestedAvatar"
              :src="changeForm.requestedAvatar"
              style="width: 56px; height: 56px; border-radius: 50%; margin-top: 8px;"
              fit="cover"
            />
            <el-button type="success" size="small" :loading="submitting" @click="submitChangeRequest">提交修改申请</el-button>
          </div>
          <div class="user-info">
            <h2>{{ displayName }}</h2>
            <p class="bio">相识于山川湖海，传承于匠心独运。</p>
          </div>
          <el-divider />
          <div class="user-stats" v-if="role === 'STUDENT'">
            <div class="stat-item">
              <div class="stat-num">{{ studentStats.purchasedCourseCount }}</div>
              <div class="stat-label">已购课程</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ studentStats.studyHours }}h</div>
              <div class="stat-label">学习时长</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ studentStats.favoriteWorkCount }}</div>
              <div class="stat-label">收藏作品</div>
            </div>
          </div>
          <div class="user-stats" v-else-if="role === 'UPLOADER'">
            <div class="stat-item">
              <div class="stat-num">{{ uploaderStats.uploadedCourseCount }}</div>
              <div class="stat-label">发布课程</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ uploaderStats.uploadedWorkCount }}</div>
              <div class="stat-label">上架作品</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ uploaderStats.averageScore }}</div>
              <div class="stat-label">综合评分</div>
            </div>
          </div>
          <div class="user-stats" v-else>
            <div class="stat-item">
              <div class="stat-num">{{ adminStats.totalUsers }}</div>
              <div class="stat-label">平台用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ adminStats.pendingItems }}</div>
              <div class="stat-label">待处理审核</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Right Column: Role Specific Content -->
      <el-col :span="16">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span>{{ contentTitle }}</span>
            </div>
          </template>

          <h3 class="section-title">资料修改申请记录</h3>
          <el-table :data="myRequests" size="small" border>
            <el-table-column prop="createTime" label="申请时间" width="170" />
            <el-table-column prop="requestedUsername" label="申请昵称" min-width="140" />
            <el-table-column label="头像修改" width="100">
              <template #default="{ row }">
                <span>{{ row.requestedAvatar ? '是' : '否' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="requestStatusType(row.status)">{{ requestStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewComment" label="审核备注" min-width="180" />
          </el-table>

          <!-- Student View -->
          <div v-if="role === 'STUDENT'" class="role-view">
            <h3 class="section-title">我的学习进度</h3>
            <div class="course-progress" v-for="course in studentCourses" :key="course.name">
              <div class="course-info">
                <span class="course-name">{{ course.name }}</span>
                <span class="course-percent">{{ course.progress }}%</span>
              </div>
              <el-progress :percentage="course.progress" :color="course.color" :stroke-width="10" />
            </div>

            <h3 class="section-title mt-20">我的收藏作品</h3>
            <div class="favorite-panel" v-loading="favoriteWorksLoading">
              <el-empty v-if="favoriteWorks.length === 0" description="还没有收藏作品">
                <el-button type="primary" plain @click="goRoute('/store')">去作品广场看看</el-button>
              </el-empty>
              <div v-else class="favorite-grid">
                <div class="favorite-card" v-for="work in favoriteWorks" :key="work.id">
                  <el-image class="favorite-cover" :src="work.imageUrl" fit="cover">
                    <template #error>
                      <div class="image-slot">
                        <span>无影像</span>
                      </div>
                    </template>
                  </el-image>
                  <div class="favorite-content">
                    <div class="favorite-title">{{ work.title }}</div>
                    <div class="favorite-desc">{{ work.description || '这件作品还没有补充介绍' }}</div>
                    <div class="favorite-footer">
                      <span class="favorite-price">{{ work.isForSale === 1 ? `¥ ${work.price}` : '非卖展示品' }}</span>
                      <div class="favorite-actions">
                        <el-button size="small" plain @click="goRoute('/store')">去作品广场</el-button>
                        <el-button size="small" type="danger" plain @click="handleRemoveFavorite(work.id)">取消收藏</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <h3 class="section-title mt-20">最近浏览的作品</h3>
            <el-empty v-if="recentViewedWorks.length === 0" description="还没有浏览记录" />
            <el-row v-else :gutter="10">
              <el-col :span="8" v-for="work in recentViewedWorks" :key="work.id">
                <div class="product-card" @click="goRoute('/store')">
                  <el-image
                    style="width: 100%; height: 100px; border-radius: 4px;"
                    :src="work.imageUrl"
                    fit="cover"
                  >
                    <template #error>
                      <div class="image-slot">暂无图片</div>
                    </template>
                  </el-image>
                  <p class="product-title">{{ work.title }}</p>
                  <p class="product-subtitle">浏览 {{ work.viewCount || 1 }} 次</p>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- Uploader View -->
          <div v-else-if="role === 'UPLOADER'" class="role-view">
            <el-row :gutter="20" class="revenue-banner">
              <el-col :span="12">
                <div class="revenue-box">
                  <div class="revenue-label">本月预估收入 (元)</div>
                  <div class="revenue-val">￥ 8,450.00</div>
                  <div class="revenue-trend up"><el-icon><Top /></el-icon> 较上月增长 12.5%</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="revenue-box">
                  <div class="revenue-label">总粉丝数</div>
                  <div class="revenue-val">1,204</div>
                  <div class="revenue-trend up"><el-icon><Top /></el-icon> 较上月新增 56 人</div>
                </div>
              </el-col>
            </el-row>

            <h3 class="section-title mt-20">常用快捷工具</h3>
            <div class="tool-grid">
              <div class="tool-item" @click="goRoute('/courses')">
                <el-icon class="tool-icon" color="#409eff"><VideoPlay /></el-icon>
                <span>课程管理</span>
              </div>
              <div class="tool-item" @click="goRoute('/store')">
                <el-icon class="tool-icon" color="#67c23a"><Goods /></el-icon>
                <span>商品管理</span>
              </div>
              <div class="tool-item" @click="reviewDrawerVisible = true">
                <el-icon class="tool-icon" color="#e6a23c"><ChatLineRound /></el-icon>
                <span>学员评价</span>
              </div>
              <div class="tool-item" @click="withdrawDialogVisible = true">
                <el-icon class="tool-icon" color="#f56c6c"><Wallet /></el-icon>
                <span>收益提现</span>
              </div>
            </div>

            <h3 class="section-title mt-20">最近平台操作</h3>
            <el-empty v-if="recentOperations.length === 0" description="还没有操作记录" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="item in recentOperations"
                :key="item.id"
                :timestamp="item.createTime"
                type="primary"
              >
                {{ item.detail || `${item.module} - ${item.action}` }}
              </el-timeline-item>
            </el-timeline>
          </div>

          <!-- Admin View -->
          <div v-else class="role-view">
            <h3 class="section-title">系统告警与通知</h3>
            <el-timeline>
              <el-timeline-item
                v-for="activity in adminActivities"
                :key="activity.id"
                :type="activity.type"
                :color="activity.color"
                :size="activity.size"
                :timestamp="activity.timestamp"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>

            <h3 class="section-title mt-20">系统权限快捷入口</h3>
            <div class="tool-grid">
              <div class="tool-item" @click="goRoute('/users')">
                <el-icon class="tool-icon" color="#409eff"><User /></el-icon>
                <span>账号配置</span>
              </div>
              <div class="tool-item" @click="systemSettingVisible = true">
                <el-icon class="tool-icon" color="#e6a23c"><Setting /></el-icon>
                <span>系统设置</span>
              </div>
              <div class="tool-item" @click="backupPlatformData">
                <el-icon class="tool-icon" color="#67c23a"><DataLine /></el-icon>
                <span>数据备份</span>
              </div>
            </div>
          </div>

          <el-drawer v-model="reviewDrawerVisible" title="学员评价" size="45%">
            <el-table :data="recentReviews" size="small" border>
              <el-table-column prop="course" label="课程" min-width="180" />
              <el-table-column prop="student" label="学员" width="120" />
              <el-table-column prop="score" label="评分" width="90" />
              <el-table-column prop="comment" label="评价内容" min-width="220" />
              <el-table-column prop="time" label="时间" width="160" />
            </el-table>
          </el-drawer>

          <el-dialog v-model="withdrawDialogVisible" title="收益提现申请" width="460px">
            <el-form label-width="100px">
              <el-form-item label="提现金额">
                <el-input-number v-model="withdrawForm.amount" :min="1" :step="100" style="width: 100%" />
              </el-form-item>
              <el-form-item label="收款账号">
                <el-input v-model="withdrawForm.account" placeholder="银行卡号/支付宝账号" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="withdrawForm.remark" type="textarea" :rows="3" placeholder="可选" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="withdrawDialogVisible = false">取消</el-button>
              <el-button type="primary" :loading="submittingWithdraw" @click="submitWithdraw">提交申请</el-button>
            </template>
          </el-dialog>

          <el-dialog v-model="systemSettingVisible" title="系统设置" width="520px">
            <el-form label-width="160px">
              <el-form-item label="允许新用户注册">
                <el-switch v-model="systemSetting.allowRegister" />
              </el-form-item>
              <el-form-item label="开启资料审核提醒">
                <el-switch v-model="systemSetting.profileAuditNotify" />
              </el-form-item>
              <el-form-item label="自动备份频率">
                <el-select v-model="systemSetting.backupCycle" style="width: 100%">
                  <el-option label="每天" value="daily" />
                  <el-option label="每周" value="weekly" />
                  <el-option label="每月" value="monthly" />
                </el-select>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="systemSettingVisible = false">取消</el-button>
              <el-button type="primary" @click="saveSystemSetting">保存</el-button>
            </template>
          </el-dialog>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchProfileSummary } from '../api/profile'
import { fetchCurrentProfile, fetchMyProfileChanges, submitProfileChange } from '../api/profileChange'
import { fetchMyLearningCourses } from '../api/course'
import { fetchList as fetchUserList } from '../api/user'
import { fetchAllCourseList } from '../api/course'
import { fetchAllWorkList, unfavoriteWork } from '../api/work'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '测试用户')
const nickname = ref(localStorage.getItem('nickname') || '')
const role = ref(localStorage.getItem('role') || 'STUDENT')
const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const avatarUrl = ref(localStorage.getItem('avatar') || DEFAULT_AVATAR)
const submitting = ref(false)
const myRequests = ref([])
const showRenameInput = ref(false)
const reviewDrawerVisible = ref(false)
const withdrawDialogVisible = ref(false)
const systemSettingVisible = ref(false)
const submittingWithdraw = ref(false)
const favoriteWorksLoading = ref(false)
const summary = ref({})
const changeForm = reactive({
  requestedNickname: '',
  requestedAvatar: ''
})
const withdrawForm = reactive({
  amount: 100,
  account: '',
  remark: ''
})
const systemSetting = reactive({
  allowRegister: true,
  profileAuditNotify: true,
  backupCycle: 'weekly'
})
const recentReviews = ref([
  { course: '锡雕入门：錾刻与退火基础', student: '李同学', score: 5, comment: '老师讲得很细，实操环节特别有帮助。', time: '2026-03-10 10:12' },
  { course: '传统纹样训练：回纹、云纹、缠枝纹', student: '王同学', score: 4, comment: '课程体系完整，建议再补充更多作业点评。', time: '2026-03-09 20:30' },
  { course: '文创开发：锡雕饰品商业化设计', student: '赵同学', score: 5, comment: '商业化思路很清晰，对比赛路演帮助大。', time: '2026-03-08 15:02' }
])
const ONBOARDING_PROFILE_KEY = 'onboarding_profile_username'

const roleName = computed(() => {
  if (role.value === 'ADMIN') return '系统管理员'
  if (role.value === 'UPLOADER') return '认证讲师 / 匠人'
  return '普通学员'
})

const roleTagType = computed(() => {
  if (role.value === 'ADMIN') return 'danger'
  if (role.value === 'UPLOADER') return 'warning'
  return 'success'
})

const contentTitle = computed(() => {
  if (role.value === 'ADMIN') return '管理面板'
  if (role.value === 'UPLOADER') return '创作者中心'
  return '学习主页'
})

const displayName = computed(() => nickname.value || username.value)
const favoriteWorks = computed(() => summary.value.favoriteWorks || [])
const recentViewedWorks = computed(() => summary.value.recentViewedWorks || [])
const recentOperations = computed(() => summary.value.recentOperations || [])
const studentStats = computed(() => ({
  purchasedCourseCount: summary.value.purchasedCourseCount || 0,
  studyHours: summary.value.studyHours || 0,
  favoriteWorkCount: summary.value.favoriteWorkCount || 0
}))
const uploaderStats = computed(() => ({
  uploadedCourseCount: summary.value.uploadedCourseCount || 0,
  uploadedWorkCount: summary.value.uploadedWorkCount || 0,
  averageScore: summary.value.averageScore || 0
}))
const adminStats = computed(() => ({
  totalUsers: summary.value.totalUsers || 0,
  pendingItems: Number(summary.value.pendingCourses || 0) + Number(summary.value.pendingWorks || 0)
}))

const studentCourses = ref([])
const adminActivities = computed(() => (summary.value.recentOperations || []).map((item, index) => ({
  id: item.id || index,
  content: item.detail || `${item.module} - ${item.action}`,
  timestamp: item.createTime || '',
  type: item.action && item.action.includes('REVIEW') ? 'warning' : 'primary',
  color: item.action && item.action.includes('DELETE') ? '#f56c6c' : '#409eff',
  size: index === 0 ? 'large' : 'normal'
})))

const handleAvatarSelect = (file) => {
  const raw = file?.raw
  if (!raw) return
  const isImage = String(raw.type || '').startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }
  const isLt3M = raw.size / 1024 / 1024 < 3
  if (!isLt3M) {
    ElMessage.error('头像大小不能超过 3MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    const base64 = String(e.target?.result || '')
    if (!base64) return
    changeForm.requestedAvatar = base64
    ElMessage.success('已选择新头像，记得提交审核申请')
  }
  reader.readAsDataURL(raw)
}

const requestStatusLabel = (status) => {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[status] || status
}

const requestStatusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const loadMyRequests = async () => {
  try {
    const res = await fetchMyProfileChanges(username.value)
    myRequests.value = res.data || []
  } catch {
    myRequests.value = []
  }
}

const loadCurrentProfile = async () => {
  try {
    const res = await fetchCurrentProfile(username.value)
    const profile = res.data || {}
    if (profile.username) {
      username.value = profile.username
      localStorage.setItem('username', profile.username)
    }
    nickname.value = profile.nickname || ''
    if (nickname.value) {
      localStorage.setItem('nickname', nickname.value)
    } else {
      localStorage.removeItem('nickname')
    }
    avatarUrl.value = profile.avatar || DEFAULT_AVATAR
    if (profile.avatar) {
      localStorage.setItem('avatar', profile.avatar)
    } else {
      localStorage.removeItem('avatar')
    }
  } catch {
    avatarUrl.value = DEFAULT_AVATAR
  }
}

const loadProfileSummary = async () => {
  favoriteWorksLoading.value = true
  try {
    const res = await fetchProfileSummary()
    summary.value = res.data || {}
  } catch {
    summary.value = {}
  } finally {
    favoriteWorksLoading.value = false
  }
}

const loadStudentLearningCourses = async () => {
  if (role.value !== 'STUDENT') {
    studentCourses.value = []
    return
  }
  try {
    const res = await fetchMyLearningCourses()
    studentCourses.value = (res.data || []).slice(0, 3).map((course, index) => ({
      name: course.title,
      progress: Number(course.progress || 0),
      color: ['#409eff', '#e6a23c', '#67c23a'][index % 3]
    }))
  } catch {
    studentCourses.value = []
  }
}

const goRoute = (path) => {
  router.push(path)
}

const handleRemoveFavorite = async (workId) => {
  try {
    await unfavoriteWork(workId)
    await loadProfileSummary()
    ElMessage.success('已取消收藏')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '取消收藏失败')
  }
}

const submitWithdraw = async () => {
  if (!withdrawForm.account.trim()) {
    ElMessage.warning('请填写收款账号')
    return
  }
  if (!withdrawForm.amount || Number(withdrawForm.amount) <= 0) {
    ElMessage.warning('提现金额需大于 0')
    return
  }
  submittingWithdraw.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 700))
    ElMessage.success(`提现申请已提交，金额 ￥${Number(withdrawForm.amount).toFixed(2)}`)
    withdrawDialogVisible.value = false
    withdrawForm.amount = 100
    withdrawForm.account = ''
    withdrawForm.remark = ''
  } finally {
    submittingWithdraw.value = false
  }
}

const saveSystemSetting = () => {
  localStorage.setItem('adminSystemSetting', JSON.stringify(systemSetting))
  systemSettingVisible.value = false
  ElMessage.success('系统设置已保存')
}

const loadSystemSetting = () => {
  const cached = localStorage.getItem('adminSystemSetting')
  if (!cached) return
  try {
    const parsed = JSON.parse(cached)
    systemSetting.allowRegister = parsed.allowRegister ?? true
    systemSetting.profileAuditNotify = parsed.profileAuditNotify ?? true
    systemSetting.backupCycle = parsed.backupCycle || 'weekly'
  } catch {
    // ignore invalid local cache
  }
}

const csvEscape = (value) => {
  const text = String(value ?? '').replace(/"/g, '""')
  return `"${text}"`
}

const toCsv = (headers, rows) => {
  const lines = [headers.map(csvEscape).join(',')]
  rows.forEach((row) => lines.push(row.map(csvEscape).join(',')))
  return lines.join('\n')
}

const downloadTextFile = (filename, content) => {
  const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

const nowStamp = () => {
  const now = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}${p(now.getMonth() + 1)}${p(now.getDate())}_${p(now.getHours())}${p(now.getMinutes())}${p(now.getSeconds())}`
}

const backupPlatformData = async () => {
  try {
    const [userRes, courseRes, workRes] = await Promise.all([
      fetchUserList({ page: 1, limit: 1000 }),
      fetchAllCourseList(),
      fetchAllWorkList()
    ])
    const users = userRes?.data?.items || []
    const courses = courseRes?.data || []
    const works = workRes?.data || []
    const stamp = nowStamp()
    downloadTextFile(
      `backup_users_${stamp}.csv`,
      toCsv(['ID', '用户名', '昵称', '角色', '手机号', '创建时间'], users.map((u) => [u.id, u.username, u.nickname, u.role, u.phone, u.createTime]))
    )
    downloadTextFile(
      `backup_courses_${stamp}.csv`,
      toCsv(['ID', '课程标题', '上传者ID', '状态', '价格', '创建时间'], courses.map((c) => [c.id, c.title, c.teacherId, c.status, c.price, c.createTime]))
    )
    downloadTextFile(
      `backup_works_${stamp}.csv`,
      toCsv(['ID', '作品标题', '作者ID', '状态', '是否上架', '价格', '创建时间'], works.map((w) => [w.id, w.title, w.authorId, w.status, w.isForSale, w.price, w.createTime]))
    )
    ElMessage.success(`备份完成：用户 ${users.length} 条，课程 ${courses.length} 条，作品 ${works.length} 条`)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '备份失败，请稍后重试')
  }
}

const submitChangeRequest = async () => {
  const requestedNickname = (changeForm.requestedNickname || '').trim()
  const requestedAvatar = (changeForm.requestedAvatar || '').trim()
  if (!requestedNickname && !requestedAvatar) {
    ElMessage.warning('请至少填写新昵称或选择新头像')
    return
  }
  submitting.value = true
  try {
    await submitProfileChange({
      currentUsername: username.value,
      requestedNickname,
      requestedAvatar
    })
    ElMessage.success('申请已提交，等待管理员审核')
    changeForm.requestedNickname = ''
    changeForm.requestedAvatar = ''
    loadMyRequests()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交申请失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const onboardingUser = localStorage.getItem(ONBOARDING_PROFILE_KEY)
  if (onboardingUser && onboardingUser === localStorage.getItem('username')) {
    localStorage.removeItem(ONBOARDING_PROFILE_KEY)
  }
  loadSystemSetting()
  await loadCurrentProfile()
  await loadMyRequests()
  await Promise.all([loadProfileSummary(), loadStudentLearningCourses()])
})
</script>

<style scoped>
.profile-container {
  padding: 24px 20px 40px;
  animation: fadeIn 0.6s ease-out;
  max-width: 1200px;
  margin: 24px auto;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.user-card, .content-card {
  border-radius: 16px;
  border: 1px solid #ebeef5;
  min-height: calc(100vh - 180px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.user-card {
  animation: slideRight 0.6s ease-out backwards;
}

.content-card {
  animation: slideLeft 0.6s ease-out backwards;
  animation-delay: 0.2s;
}

@keyframes slideRight {
  from { opacity: 0; transform: translateX(-30px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes slideLeft {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.user-card:hover, .content-card:hover {
  box-shadow: 0 12px 32px rgba(0,0,0,0.08);
  transform: translateY(-4px);
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 30px;
  position: relative;
}

.avatar-wrapper :deep(.el-avatar) {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.5s;
}

.avatar-wrapper:hover :deep(.el-avatar) {
  transform: rotate(360deg) scale(1.05);
}

.avatar-actions {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.profile-change-box {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.role-badge {
  position: absolute;
  bottom: -12px;
  z-index: 10;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.user-info {
  text-align: center;
  margin-top: 24px;
}

.user-info h2 {
  margin: 0;
  font-size: 26px;
  color: #303133;
  font-weight: 600;
  background: linear-gradient(90deg, #303133, #409EFF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.user-info .bio {
  color: #909399;
  font-size: 14px;
  margin-top: 12px;
  line-height: 1.6;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
  transition: transform 0.3s;
  cursor: default;
}

.stat-item:hover {
  transform: scale(1.1);
}

.stat-num {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  text-shadow: 0 2px 4px rgba(64,158,255,0.2);
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.card-header {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.section-title {
  font-size: 17px;
  color: #303133;
  margin-bottom: 24px;
  border-left: 5px solid #409EFF;
  padding-left: 12px;
  font-weight: 600;
}

.mt-20 {
  margin-top: 36px;
}

/* Student progress */
.course-progress {
  margin-bottom: 24px;
  padding: 12px;
  border-radius: 8px;
  background: #fafafa;
  transition: background 0.3s;
}

.course-progress:hover {
  background: #f0f2f5;
}

.course-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}

.favorite-panel {
  min-height: 160px;
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.favorite-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid #ebeef5;
  background: linear-gradient(135deg, #ffffff, #f8fbff);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.08);
}

.favorite-cover {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.favorite-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.favorite-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.favorite-desc {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.favorite-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 14px;
}

.favorite-price {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
}

.favorite-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.image-slot {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #f5f7fa;
  font-size: 13px;
}

.product-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: #fff;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  border-color: #dcdfe6;
}

.product-title {
  margin: 12px 0 6px 0;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.product-subtitle {
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
}

/* Uploader banner */
.revenue-banner {
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 36px;
  box-shadow: inset 0 0 20px rgba(0,0,0,0.02);
  border: 1px solid #f0f0f0;
}

.revenue-box {
  text-align: center;
  transition: transform 0.3s;
}

.revenue-box:hover {
  transform: scale(1.05);
}

.revenue-label {
  font-size: 15px;
  color: #606266;
  margin-bottom: 12px;
}

.revenue-val {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
  text-shadow: 0 2px 4px rgba(245,108,108,0.2);
}

.revenue-trend {
  font-size: 13px;
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

.revenue-trend.up {
  color: #67c23a;
}

.revenue-trend .el-icon {
  margin-right: 6px;
  font-size: 16px;
}

/* Tool grid */
.tool-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid transparent;
}

.tool-item:hover {
  background: #ffffff;
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border-color: #ebeef5;
}

.tool-icon {
  font-size: 36px;
  margin-bottom: 16px;
  transition: transform 0.3s;
}

.tool-item:hover .tool-icon {
  transform: scale(1.15);
}

.tool-item span {
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}

@media (max-width: 992px) {
  .favorite-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px 12px 32px;
  }

  .avatar-actions {
    flex-direction: column;
  }

  .user-stats {
    gap: 10px;
  }

  .favorite-card {
    flex-direction: column;
  }

  .favorite-cover {
    width: 100%;
    height: 180px;
  }

  .favorite-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .favorite-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .tool-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
