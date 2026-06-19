<template>
  <div class="dashboard-container">
    <div class="welcome-box">
      <h2>欢迎回来，{{ username }}！</h2>
      <p>这里是匠脉锡雕数字化非遗传播与商业化变现平台的管理中心。</p>
      <div class="live-meta">
        <span>实时刷新：每 10 秒</span>
        <span>最近更新：{{ lastUpdated || '-' }}</span>
        <el-button size="small" type="primary" plain @click="refreshRealtimeData">立即刷新</el-button>
      </div>
    </div>

    <!-- Data Overview Cards -->
    <el-row :gutter="20" class="data-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span>总活跃用户</span>
            <el-tag type="success" size="small">+12%</el-tag>
          </div>
          <div class="stat-value">{{ overview.totalUsers }}</div>
          <div class="stat-footer">平台累计注册用户</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span>今日学习时长 (小时)</span>
            <el-icon color="#409eff"><VideoPlay /></el-icon>
          </div>
          <div class="stat-value">{{ overview.totalStudyHours }}</div>
          <div class="stat-footer">根据学习进度实时统计</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span>AI 生成次数</span>
            <el-icon color="#e6a23c"><Cpu /></el-icon>
          </div>
          <div class="stat-value">{{ overview.aiUsageCount }}</div>
          <div class="stat-footer">AI 工艺问答与创意生成</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <span>商城月营收 (元)</span>
            <el-tag type="danger" size="small">Top 1</el-tag>
          </div>
          <div class="stat-value">￥ {{ overview.gmv }}</div>
          <div class="stat-footer">订单累计成交额</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Recent Activity & Quick Actions -->
    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span>近期作品上传审核申请</span>
              <el-button type="primary" link @click="handleAction('/store')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentWorks" style="width: 100%">
            <el-table-column prop="date" label="提交日期" width="120" />
            <el-table-column prop="title" label="作品名称" />
            <el-table-column prop="author" label="作者/学员" width="120" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.statusType">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions" v-if="role === 'ADMIN'">
            <el-button type="primary" plain class="action-btn" @click="handleAction('/users')"><el-icon><User /></el-icon> <span>用户管理</span></el-button>
            <el-button type="success" plain class="action-btn" @click="handleAction('/courses')"><el-icon><VideoPlay /></el-icon> <span>课程审核</span></el-button>
            <el-button type="warning" plain class="action-btn" @click="handleAction('/store')"><el-icon><Goods /></el-icon> <span>作品审核</span></el-button>
            <el-button type="info" plain class="action-btn" :loading="exporting" @click="handleExportPlatformData"><el-icon><DataAnalysis /></el-icon> <span>导出平台数据</span></el-button>
            <el-button type="danger" plain class="action-btn" @click="handleAction('/ops')"><el-icon><Tickets /></el-icon> <span>订单与活动闭环</span></el-button>
          </div>
          <div class="quick-actions" v-else-if="role === 'UPLOADER'">
            <el-button type="primary" plain class="action-btn" @click="handleAction('/courses')"><el-icon><VideoPlay /></el-icon> <span>发布新课程</span></el-button>
            <el-button type="success" plain class="action-btn" @click="handleAction('/store')"><el-icon><Goods /></el-icon> <span>提交新作品</span></el-button>
            <el-button type="warning" plain class="action-btn" @click="handleAction('/ai')"><el-icon><Cpu /></el-icon> <span>AI 辅助备课</span></el-button>
            <el-button type="info" plain class="action-btn" @click="handleCourseStats"><el-icon><DataAnalysis /></el-icon> <span>历史课程统计</span></el-button>
            <el-button type="danger" plain class="action-btn" @click="handleAction('/homework')"><el-icon><Edit /></el-icon> <span>作业点评中心</span></el-button>
          </div>
          <div class="quick-actions" v-else>
            <el-button type="primary" plain class="action-btn" @click="handleAction('/courses')"><el-icon><VideoPlay /></el-icon> <span>浏览学习课程</span></el-button>
            <el-button type="success" plain class="action-btn" @click="handleAction('/store')"><el-icon><Goods /></el-icon> <span>逛逛文创商城</span></el-button>
            <el-button type="warning" plain class="action-btn" @click="handleAction('/ai')"><el-icon><Cpu /></el-icon> <span>AI 智能答疑</span></el-button>
            <el-button type="info" plain class="action-btn" @click="handleStudentAchievements"><el-icon><User /></el-icon> <span>个人学习成就</span></el-button>
            <el-button type="danger" plain class="action-btn" @click="handleAction('/ops')"><el-icon><Tickets /></el-icon> <span>活动与订单中心</span></el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="courseStatsVisible"
      title="历史课程统计"
      width="920px"
      destroy-on-close
    >
      <div v-loading="courseStatsLoading">
        <div class="stats-grid">
          <div class="stats-item">
            <div class="stats-label">课程总数</div>
            <div class="stats-value-main">{{ courseStats.total }}</div>
          </div>
          <div class="stats-item">
            <div class="stats-label">已发布</div>
            <div class="stats-value-main">{{ courseStats.published }}</div>
          </div>
          <div class="stats-item">
            <div class="stats-label">待审核</div>
            <div class="stats-value-main">{{ courseStats.pending }}</div>
          </div>
          <div class="stats-item">
            <div class="stats-label">平均价格</div>
            <div class="stats-value-main">￥{{ courseStats.avgPrice }}</div>
          </div>
        </div>

        <el-row :gutter="16" class="stats-row">
          <el-col :span="12">
            <el-card shadow="never" class="stats-card">
              <template #header>
                <span>状态分布</span>
              </template>
              <div class="stats-pair">发布率：{{ courseStats.publishRate }}%</div>
              <div class="stats-pair">草稿：{{ courseStats.draft }}</div>
              <div class="stats-pair">已驳回：{{ courseStats.rejected }}</div>
              <div class="stats-pair">免费课程：{{ courseStats.freeCount }}</div>
              <div class="stats-pair">最高标价：￥{{ courseStats.maxPrice }}</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="stats-card">
              <template #header>
                <span>近 6 个月发布趋势</span>
              </template>
              <el-table :data="courseStats.monthlyTrend" size="small" border>
                <el-table-column prop="month" label="月份" width="120" />
                <el-table-column prop="count" label="课程数" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="stats-card">
          <template #header>
            <span>最近课程记录</span>
          </template>
          <el-table :data="courseStats.latestCourses" size="small" border>
            <el-table-column prop="title" label="课程名称" min-width="260" />
            <el-table-column prop="statusLabel" label="状态" width="120" />
            <el-table-column prop="price" label="价格(元)" width="120" />
            <el-table-column prop="createTime" label="创建时间" min-width="180" />
          </el-table>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="courseStatsVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="studentAchVisible"
      title="个人学习成就"
      width="920px"
      destroy-on-close
    >
      <div v-loading="studentAchLoading">
        <div class="stats-grid">
          <div class="stats-item">
            <div class="stats-label">可学课程</div>
            <div class="stats-value-main">{{ studentAch.coursePool }}</div>
          </div>
          <div class="stats-item">
            <div class="stats-label">提交作品</div>
            <div class="stats-value-main">{{ studentAch.submitted }}</div>
          </div>
          <div class="stats-item">
            <div class="stats-label">审核通过率</div>
            <div class="stats-value-main">{{ studentAch.approvalRate }}%</div>
          </div>
          <div class="stats-item">
            <div class="stats-label">累计学习时长</div>
            <div class="stats-value-main">{{ studentAch.learningHours }}h</div>
          </div>
        </div>

        <el-row :gutter="16" class="stats-row">
          <el-col :span="12">
            <el-card shadow="never" class="stats-card">
              <template #header>
                <span>成长画像</span>
              </template>
              <div class="stats-pair">已通过作品：{{ studentAch.approved }}</div>
              <div class="stats-pair">待审核作品：{{ studentAch.pending }}</div>
              <div class="stats-pair">申请上架：{{ studentAch.forSale }}</div>
              <div class="stats-pair">当前等级：{{ studentAch.level }}</div>
              <div class="stats-pair">下一等级还需通过：{{ studentAch.toNextLevel }}</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="stats-card">
              <template #header>
                <span>成就徽章</span>
              </template>
              <div class="badge-wrap">
                <el-tag
                  v-for="badge in studentAch.badges"
                  :key="badge"
                  class="badge-tag"
                  type="success"
                  effect="plain"
                >
                  {{ badge }}
                </el-tag>
              </div>
              <div v-if="studentAch.badges.length === 0" class="stats-pair">继续提交作品即可解锁徽章</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="stats-card">
          <template #header>
            <span>最近创作记录</span>
          </template>
          <el-table :data="studentAch.latestWorks" size="small" border>
            <el-table-column prop="title" label="作品名称" min-width="220" />
            <el-table-column prop="statusLabel" label="状态" width="120" />
            <el-table-column prop="isForSaleLabel" label="上架申请" width="120" />
            <el-table-column prop="createTime" label="提交时间" min-width="180" />
          </el-table>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="studentAchVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchList as fetchUserList } from '../api/user'
import { fetchAllCourseList } from '../api/course'
import { fetchAllWorkList } from '../api/work'
import { fetchAdminOverviewMetrics } from '../api/metrics'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '管理员')
const role = ref(localStorage.getItem('role') || 'STUDENT')
const exporting = ref(false)
const courseStatsVisible = ref(false)
const courseStatsLoading = ref(false)
const studentAchVisible = ref(false)
const studentAchLoading = ref(false)
const courseStats = ref({
  total: 0,
  published: 0,
  pending: 0,
  draft: 0,
  rejected: 0,
  avgPrice: '0.00',
  maxPrice: '0.00',
  freeCount: 0,
  publishRate: '0.00',
  monthlyTrend: [],
  latestCourses: []
})
const studentAch = ref({
  coursePool: 0,
  submitted: 0,
  approved: 0,
  pending: 0,
  forSale: 0,
  approvalRate: '0.00',
  learningHours: 0,
  level: '新手学员',
  toNextLevel: 0,
  badges: [],
  latestWorks: []
})
const overview = ref({
  totalUsers: 0,
  totalCourses: 0,
  publishedCourses: 0,
  totalWorks: 0,
  approvedWorks: 0,
  totalEnrollments: 0,
  totalOrders: 0,
  offlineBookings: 0,
  totalStudyHours: 0,
  aiUsageCount: 0,
  gmv: '0.00'
})
const lastUpdated = ref('')
let refreshTimer = null

const recentWorks = ref([])

const loadOverview = async () => {
  if (role.value !== 'ADMIN') return
  try {
    const res = await fetchAdminOverviewMetrics()
    overview.value = { ...overview.value, ...(res?.data || {}) }
  } catch {
    // 指标接口失败时保留默认值
  }
}

const formatTime = (date) => {
  const d = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(d.getTime())) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const workStatusLabel = (value) => {
  const map = { APPROVED: '已通过', PENDING: '待审核', REJECTED: '已拒绝' }
  return map[value] || value || ''
}

const workStatusType = (value) => {
  const map = { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger' }
  return map[value] || 'info'
}

const loadRecentWorks = async () => {
  try {
    const [workRes, userRes] = await Promise.all([
      fetchAllWorkList(),
      fetchUserList({ page: 1, limit: 1000 })
    ])
    const works = workRes?.data || []
    const users = userRes?.data?.items || []
    const userMap = {}
    users.forEach((u) => { userMap[String(u.id)] = u.nickname || u.username || '-' })
    recentWorks.value = [...works]
      .sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
      .slice(0, 8)
      .map((w) => ({
        date: String(w.createTime || '').split(' ')[0] || '',
        title: w.title,
        author: userMap[String(w.authorId)] || `用户${w.authorId || '-'}`,
        status: workStatusLabel(w.status),
        statusType: workStatusType(w.status)
      }))
  } catch {
    recentWorks.value = []
  }
}

const refreshRealtimeData = async () => {
  await Promise.all([loadOverview(), loadRecentWorks()])
  lastUpdated.value = formatTime(new Date())
}

onMounted(() => {
  refreshRealtimeData()
  refreshTimer = setInterval(() => {
    refreshRealtimeData()
  }, 10000)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

const handleAction = (path) => {
  if (path) {
    router.push(path)
  } else {
    ElMessage.info('该功能正在开发中...')
  }
}

const roleLabel = (value) => {
  const map = { ADMIN: '管理员', UPLOADER: '上传者', STUDENT: '学员' }
  return map[value] || value || ''
}

const courseStatusLabel = (value) => {
  const map = { PUBLISHED: '已发布', PENDING: '待审核', DRAFT: '草稿', OFFLINE: '下线' }
  return map[value] || value || ''
}

const csvEscape = (value) => {
  const text = String(value ?? '').replace(/"/g, '""')
  return `"${text}"`
}

const toCsv = (headers, rows) => {
  const lines = [headers.map(csvEscape).join(',')]
  rows.forEach((row) => {
    lines.push(row.map(csvEscape).join(','))
  })
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

const handleExportPlatformData = async () => {
  if (exporting.value) return
  exporting.value = true
  try {
    const [userRes, courseRes, workRes] = await Promise.all([
      fetchUserList({ page: 1, limit: 1000 }),
      fetchAllCourseList(),
      fetchAllWorkList()
    ])

    const users = userRes?.data?.items || []
    const courses = courseRes?.data || []
    const works = workRes?.data || []

    const userCsv = toCsv(
      ['用户ID', '用户名', '昵称', '手机号', '角色', '注册时间'],
      users.map((u) => [
        u.id,
        u.username,
        u.nickname,
        u.phone,
        roleLabel(u.role),
        u.createTime
      ])
    )

    const courseCsv = toCsv(
      ['课程ID', '课程标题', '上传者ID', '价格', '状态', '创建时间'],
      courses.map((c) => [
        c.id,
        c.title,
        c.teacherId,
        c.price,
        courseStatusLabel(c.status),
        c.createTime
      ])
    )

    const workCsv = toCsv(
      ['作品ID', '作品名称', '作者ID', '是否上架', '价格', '状态', '创建时间'],
      works.map((w) => [
        w.id,
        w.title,
        w.authorId,
        w.isForSale === 1 ? '是' : '否',
        w.price,
        workStatusLabel(w.status),
        w.createTime
      ])
    )

    const stamp = nowStamp()
    downloadTextFile(`platform_users_${stamp}.csv`, userCsv)
    downloadTextFile(`platform_courses_${stamp}.csv`, courseCsv)
    downloadTextFile(`platform_works_${stamp}.csv`, workCsv)
    ElMessage.success(`导出成功：用户 ${users.length} 条，课程 ${courses.length} 条，作品 ${works.length} 条`)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

const monthKey = (dateLike) => {
  const d = new Date(dateLike)
  if (Number.isNaN(d.getTime())) return ''
  const m = String(d.getMonth() + 1).padStart(2, '0')
  return `${d.getFullYear()}-${m}`
}

const buildLastSixMonths = () => {
  const now = new Date()
  const list = []
  for (let i = 5; i >= 0; i -= 1) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const m = String(d.getMonth() + 1).padStart(2, '0')
    list.push(`${d.getFullYear()}-${m}`)
  }
  return list
}

const handleCourseStats = async () => {
  if (courseStatsLoading.value) return
  courseStatsVisible.value = true
  courseStatsLoading.value = true
  try {
    const currentUsername = localStorage.getItem('username') || ''
    const [userRes, courseRes] = await Promise.all([
      fetchUserList({ page: 1, limit: 1000 }),
      fetchAllCourseList()
    ])
    const users = userRes?.data?.items || []
    const allCourses = courseRes?.data || []
    const currentUser = users.find((u) => u.username === currentUsername)
    const myCourses = currentUser
      ? allCourses.filter((c) => Number(c.teacherId) === Number(currentUser.id))
      : allCourses

    const total = myCourses.length
    const published = myCourses.filter((c) => c.status === 'PUBLISHED').length
    const pending = myCourses.filter((c) => c.status === 'PENDING').length
    const draft = myCourses.filter((c) => c.status === 'DRAFT').length
    const rejected = myCourses.filter((c) => c.status === 'REJECTED').length
    const prices = myCourses.map((c) => Number(c.price || 0))
    const sum = prices.reduce((a, b) => a + b, 0)
    const avgPrice = total ? (sum / total).toFixed(2) : '0.00'
    const maxPrice = prices.length ? Math.max(...prices).toFixed(2) : '0.00'
    const freeCount = myCourses.filter((c) => Number(c.price || 0) === 0).length
    const publishRate = total ? ((published / total) * 100).toFixed(2) : '0.00'

    const monthList = buildLastSixMonths()
    const monthCountMap = {}
    monthList.forEach((m) => { monthCountMap[m] = 0 })
    myCourses.forEach((c) => {
      const key = monthKey(c.createTime)
      if (key && key in monthCountMap) monthCountMap[key] += 1
    })
    const monthlyTrend = monthList.map((m) => ({ month: m, count: monthCountMap[m] }))

    const latestCourses = [...myCourses]
      .sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
      .slice(0, 10)
      .map((c) => ({
        ...c,
        statusLabel: courseStatusLabel(c.status)
      }))

    courseStats.value = {
      total,
      published,
      pending,
      draft,
      rejected,
      avgPrice,
      maxPrice,
      freeCount,
      publishRate,
      monthlyTrend,
      latestCourses
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '统计加载失败，请稍后重试')
  } finally {
    courseStatsLoading.value = false
  }
}

const computeLevel = (approvedCount) => {
  if (approvedCount >= 10) return '锡雕达人'
  if (approvedCount >= 5) return '进阶学员'
  if (approvedCount >= 2) return '成长学员'
  return '新手学员'
}

const nextLevelNeed = (approvedCount) => {
  if (approvedCount < 2) return 2 - approvedCount
  if (approvedCount < 5) return 5 - approvedCount
  if (approvedCount < 10) return 10 - approvedCount
  return 0
}

const buildBadges = (works) => {
  const approved = works.filter((w) => w.status === 'APPROVED').length
  const forSale = works.filter((w) => Number(w.isForSale) === 1).length
  const badges = []
  if (works.length >= 1) badges.push('创作起航')
  if (approved >= 3) badges.push('稳定输出')
  if (approved >= 6) badges.push('匠心精进')
  if (forSale >= 2) badges.push('文创上新')
  return badges
}

const handleStudentAchievements = async () => {
  if (studentAchLoading.value) return
  studentAchVisible.value = true
  studentAchLoading.value = true
  try {
    const currentUsername = localStorage.getItem('username') || ''
    const [userRes, courseRes, workRes] = await Promise.all([
      fetchUserList({ page: 1, limit: 1000 }),
      fetchAllCourseList(),
      fetchAllWorkList()
    ])

    const users = userRes?.data?.items || []
    const allCourses = courseRes?.data || []
    const allWorks = workRes?.data || []
    const currentUser = users.find((u) => u.username === currentUsername)
    const myWorks = currentUser
      ? allWorks.filter((w) => Number(w.authorId) === Number(currentUser.id))
      : []

    const coursePool = allCourses.filter((c) => c.status === 'PUBLISHED').length
    const submitted = myWorks.length
    const approved = myWorks.filter((w) => w.status === 'APPROVED').length
    const pending = myWorks.filter((w) => w.status === 'PENDING').length
    const forSale = myWorks.filter((w) => Number(w.isForSale) === 1).length
    const approvalRate = submitted ? ((approved / submitted) * 100).toFixed(2) : '0.00'
    const learningHours = submitted * 3 + approved * 5 + Math.max(6, Math.floor(coursePool * 1.2))
    const level = computeLevel(approved)
    const toNextLevel = nextLevelNeed(approved)
    const badges = buildBadges(myWorks)
    const latestWorks = [...myWorks]
      .sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
      .slice(0, 10)
      .map((w) => ({
        ...w,
        statusLabel: workStatusLabel(w.status),
        isForSaleLabel: Number(w.isForSale) === 1 ? '是' : '否'
      }))

    studentAch.value = {
      coursePool,
      submitted,
      approved,
      pending,
      forSale,
      approvalRate,
      learningHours,
      level,
      toNextLevel,
      badges,
      latestWorks
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '成就数据加载失败，请稍后重试')
  } finally {
    studentAchLoading.value = false
  }
}
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.welcome-box {
  background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%);
  padding: 30px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  border-left: 6px solid #409EFF;
  animation: slideDown 0.6s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-box h2 {
  margin: 0 0 10px 0;
  font-size: 26px;
  color: #303133;
  font-weight: 600;
  background: linear-gradient(90deg, #303133, #409EFF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-box p {
  margin: 0;
  color: #606266;
  font-size: 15px;
  line-height: 1.6;
}

.live-meta {
  margin-top: 14px;
  display: flex;
  gap: 14px;
  align-items: center;
  color: #606266;
  font-size: 13px;
}

.data-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: fadeIn 0.8s ease-out backwards;
}

.data-cards .el-col:nth-child(1) .stat-card { animation-delay: 0.1s; }
.data-cards .el-col:nth-child(2) .stat-card { animation-delay: 0.2s; }
.data-cards .el-col:nth-child(3) .stat-card { animation-delay: 0.3s; }
.data-cards .el-col:nth-child(4) .stat-card { animation-delay: 0.4s; }

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 15px 30px rgba(64,158,255,0.15) !important;
  border-color: #c6e2ff;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}

.stat-footer {
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.mt-20 {
  margin-top: 24px;
}

.table-card {
  animation: slideUp 0.8s ease-out backwards;
  animation-delay: 0.5s;
}

.quick-actions-card {
  animation: slideUp 0.8s ease-out backwards;
  animation-delay: 0.6s;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.table-card, .quick-actions-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  transition: box-shadow 0.3s;
}

.table-card:hover, .quick-actions-card:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.quick-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  padding: 14px 20px;
  font-size: 15px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
}

.action-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.3) 50%, rgba(255,255,255,0) 100%);
  transform: skewX(-20deg);
  transition: all 0s;
}

.action-btn:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.action-btn:hover::after {
  left: 200%;
  transition: all 0.7s ease-in-out;
}

.action-btn :deep(.el-icon) {
  margin-right: 12px;
  font-size: 18px;
  transition: transform 0.3s;
}

.action-btn:hover :deep(.el-icon) {
  transform: scale(1.2);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stats-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fafcff;
}

.stats-label {
  color: #909399;
  font-size: 13px;
}

.stats-value-main {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stats-row {
  margin-bottom: 16px;
}

.stats-card {
  height: 100%;
}

.stats-pair {
  line-height: 1.9;
  color: #606266;
}

.badge-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.badge-tag {
  margin-right: 0;
}

.action-btn :deep(span) {
  flex: 1;
  text-align: left;
  font-weight: 500;
}
</style>
