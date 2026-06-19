<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select v-if="role !== 'STUDENT'" v-model="statusFilter" placeholder="作业状态" clearable style="width: 160px">
        <el-option label="待点评" value="PENDING" />
        <el-option label="已点评" value="REVIEWED" />
      </el-select>
      <el-button type="primary" @click="loadList">刷新</el-button>
    </div>

    <el-card v-if="role === 'STUDENT'" class="submit-card" shadow="never">
      <template #header><span>提交课程作业</span></template>
      <el-form :model="submitForm" label-width="100px" style="max-width: 680px">
        <el-form-item label="课程ID">
          <el-input-number v-model="submitForm.courseId" :min="1" />
        </el-form-item>
        <el-form-item label="作业说明">
          <el-input v-model="submitForm.content" type="textarea" :rows="3" placeholder="描述本次作业重点与问题" />
        </el-form-item>
        <el-form-item label="作品链接">
          <el-input v-model="submitForm.workUrl" placeholder="图片或视频URL（可选）" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="submitting" @click="handleSubmit">提交作业</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="tableData" border style="width:100%; margin-top: 16px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="学员" width="120" />
      <el-table-column prop="courseTitle" label="课程" min-width="180" />
      <el-table-column prop="content" label="作业说明" min-width="220" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'REVIEWED' ? 'success' : 'warning'">{{ row.status === 'REVIEWED' ? '已点评' : '待点评' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="评分" width="90" />
      <el-table-column prop="aiComment" label="AI点评" min-width="220" show-overflow-tooltip />
      <el-table-column prop="teacherComment" label="导师点评" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="140" v-if="role !== 'STUDENT'">
        <template #default="{row}">
          <el-button type="primary" size="small" plain @click="openReview(row)">点评</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="reviewVisible" title="导师点评" width="520px">
      <el-form :model="reviewForm" label-width="90px">
        <el-form-item label="评分">
          <el-input-number v-model="reviewForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="点评">
          <el-input v-model="reviewForm.teacherComment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible=false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitHomework, fetchMyHomework, fetchAllHomework, reviewHomework } from '../api/course'

const role = ref(localStorage.getItem('role') || 'STUDENT')
const loading = ref(false)
const submitting = ref(false)
const reviewing = ref(false)
const statusFilter = ref('')
const tableData = ref([])
const reviewVisible = ref(false)
const currentHomeworkId = ref(null)

const submitForm = reactive({
  courseId: 1,
  content: '',
  workUrl: ''
})

const reviewForm = reactive({
  score: 85,
  teacherComment: ''
})

const loadList = async () => {
  loading.value = true
  try {
    const res = role.value === 'STUDENT'
      ? await fetchMyHomework()
      : await fetchAllHomework(statusFilter.value || undefined)
    tableData.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!submitForm.courseId) {
    ElMessage.warning('请填写课程ID')
    return
  }
  submitting.value = true
  try {
    await submitHomework(submitForm)
    ElMessage.success('作业提交成功')
    submitForm.content = ''
    submitForm.workUrl = ''
    await loadList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const openReview = (row) => {
  currentHomeworkId.value = row.id
  reviewForm.score = row.score || 85
  reviewForm.teacherComment = row.teacherComment || ''
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!currentHomeworkId.value) return
  reviewing.value = true
  try {
    await reviewHomework(currentHomeworkId.value, reviewForm)
    ElMessage.success('点评成功')
    reviewVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '点评失败')
  } finally {
    reviewing.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.app-container {
  padding: 24px 20px 40px;
  background-color: white;
  min-height: calc(100vh - 110px);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  max-width: 1200px;
  margin: 24px auto;
}
.filter-container { display: flex; gap: 10px; align-items: center; }
.submit-card { margin-top: 14px; }
</style>
