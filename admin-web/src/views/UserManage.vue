<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input 
        v-model="listQuery.username" 
        placeholder="搜索用户名/昵称" 
        style="width: 200px;" 
        class="filter-item"
        @keyup.enter="handleFilter" 
      />
      <el-select v-model="listQuery.role" placeholder="选择角色" clearable style="width: 150px" class="filter-item">
        <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button class="filter-item" type="primary" icon="Search" @click="handleFilter">
        查询
      </el-button>
      <el-button class="filter-item" type="success" icon="Edit" @click="handleCreate">
        新增用户
      </el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="tableData"
      border
      fit
      highlight-current-row
      style="width: 100%; margin-top:20px;"
      header-cell-class-name="table-header"
    >
      <el-table-column label="ID" prop="id" sortable align="center" width="80" />
      
      <el-table-column label="用户名" width="150" align="center">
        <template #default="{row}">
          <span>{{ row.username }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="昵称" width="150" align="center">
        <template #default="{row}">
          <span>{{ row.nickname }}</span>
        </template>
      </el-table-column>

      <el-table-column label="手机号" align="center" width="150">
        <template #default="{row}">
          <span>{{ row.phone }}</span>
        </template>
      </el-table-column>

      <el-table-column label="角色" class-name="status-col" width="120" align="center">
        <template #default="{row}">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'UPLOADER' ? 'warning' : 'success'">
            {{ row.role === 'ADMIN' ? '管理员' : row.role === 'UPLOADER' ? '讲师/上传者' : '学员' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="注册时间" width="200" align="center" prop="createTime" />

      <el-table-column label="操作" align="center" fixed="right" width="200" class-name="small-padding fixed-width">
        <template #default="{row, $index}">
          <el-button type="primary" size="small" icon="Edit" @click="handleUpdate(row)">
            编辑
          </el-button>
          <el-popconfirm
            v-if="row.role !== 'ADMIN'"
            title="确定要删除此用户吗?"
            @confirm="handleDelete(row, $index)"
          >
            <template #reference>
              <el-button size="small" type="danger" icon="Delete">
                删除
              </el-button>
            </template>
          </el-popconfirm>
          <el-button v-else size="small" type="info" disabled>
            超级管理员
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="getList"
        @current-change="getList"
      />
    </div>

    <el-card class="review-card" shadow="never">
      <template #header>
        <div class="review-header">
          <span>资料修改审核</span>
          <el-button size="small" @click="loadChangeRequests">刷新</el-button>
        </div>
      </template>
      <el-table :data="changeRequests" border size="small" style="width: 100%">
        <el-table-column prop="id" label="申请ID" width="90" />
        <el-table-column prop="currentUsername" label="当前用户名" width="140" />
        <el-table-column prop="requestedUsername" label="申请昵称" width="140" />
        <el-table-column label="新头像" width="120" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.requestedAvatar"
              :src="row.requestedAvatar"
              style="width: 44px; height: 44px; border-radius: 50%;"
              fit="cover"
              :preview-src-list="[row.requestedAvatar]"
            />
            <span v-else>未改</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="170" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="requestStatusType(row.status)">{{ requestStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审核备注" min-width="160" />
        <el-table-column label="审核操作" width="180" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" @click="reviewRequest(row, 'APPROVED')">通过</el-button>
              <el-button size="small" type="danger" @click="reviewRequest(row, 'REJECTED')">驳回</el-button>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog for Create/Update -->
    <el-dialog :title="textMap[dialogStatus]" v-model="dialogFormVisible">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="right" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="temp.username" :disabled="dialogStatus==='update'" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogStatus==='create'">
          <el-input v-model="temp.password" type="password" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="temp.nickname" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="temp.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="temp.role" class="filter-item" placeholder="请选择">
            <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="dialogStatus==='create'?createData():updateData()">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { fetchList, updateUser, deleteUser } from '../api/user'
import { register } from '../api/auth'
import { fetchProfileChangeRequests, reviewProfileChangeRequest } from '../api/profileChange'

const listLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogFormVisible = ref(false)
const dialogStatus = ref('')
const textMap = { update: '编辑用户', create: '新增用户' }
const changeRequests = ref([])

const roles = [
  { label: '管理员', value: 'ADMIN' },
  { label: '学员', value: 'STUDENT' },
  { label: '讲师/上传者', value: 'UPLOADER' }
]

const listQuery = reactive({
  page: 1,
  limit: 10,
  username: undefined,
  role: undefined
})

const temp = reactive({
  id: undefined,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  role: 'STUDENT'
})

const dataForm = ref(null)

const rules = {
  username: [{ required: true, message: '用户名必填', trigger: 'blur' }],
  password: [{ required: true, message: '密码必填', trigger: 'blur' }],
  role: [{ required: true, message: '角色必填', trigger: 'change' }]
}

// Fetch real data from Backend
const getList = () => {
  listLoading.value = true
  fetchList(listQuery).then(res => {
    tableData.value = res.data.items
    total.value = res.data.total
    listLoading.value = false
  }).catch((e) => {
    tableData.value = []
    total.value = 0
    listLoading.value = false
    ElMessage.error(e?.response?.data?.message || e?.message || '用户列表加载失败')
  })
}

onMounted(() => {
  getList()
  loadChangeRequests()
})

const handleFilter = () => {
  listQuery.page = 1
  getList()
}

const resetTemp = () => {
  Object.assign(temp, {
    id: undefined,
    username: '',
    password: '',
    nickname: '',
    phone: '',
    role: 'STUDENT'
  })
}

const handleCreate = () => {
  resetTemp()
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
  if (dataForm.value) {
    dataForm.value.clearValidate()
  }
}

const createData = () => {
  dataForm.value.validate((valid) => {
    if (valid) {
      if (temp.role === 'ADMIN') {
        ElMessage.warning('管理员账号请先创建普通账号后再由管理员提升角色')
        return
      }
      register(temp).then(() => {
        dialogFormVisible.value = false
        ElNotification({ title: '成功', message: '增加用户成功', type: 'success', duration: 2000 })
        getList() // Refresh the list
      }).catch((e) => {
        ElMessage.error(e?.response?.data?.message || e?.message || '新增失败')
      })
    }
  })
}

const handleUpdate = (row) => {
  Object.assign(temp, row)
  dialogStatus.value = 'update'
  dialogFormVisible.value = true
  if (dataForm.value) {
    dataForm.value.clearValidate()
  }
}

const updateData = () => {
  dataForm.value.validate((valid) => {
    if (valid) {
      updateUser(temp.id, {
        nickname: temp.nickname,
        phone: temp.phone,
        role: temp.role
      }).then(() => {
        dialogFormVisible.value = false
        ElNotification({ title: '成功', message: '更新成功', type: 'success', duration: 2000 })
        getList()
      }).catch((e) => {
        ElMessage.error(e?.response?.data?.message || e?.message || '更新失败')
      })
    }
  })
}

const handleDelete = (row, index) => {
  deleteUser(row.id).then(() => {
    if (typeof index === 'number' && index >= 0) {
      tableData.value.splice(index, 1)
      total.value = Math.max(0, total.value - 1)
    } else {
      getList()
    }
    ElNotification({ title: '成功', message: '删除成功', type: 'success', duration: 2000 })
  }).catch((e) => {
    ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
  })
}

const requestStatusLabel = (status) => {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[status] || status
}

const requestStatusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const loadChangeRequests = () => {
  fetchProfileChangeRequests().then((res) => {
    changeRequests.value = res.data || []
  }).catch(() => {
    changeRequests.value = []
  })
}

const reviewRequest = (row, status) => {
  reviewProfileChangeRequest(row.id, {
    status,
    reviewComment: status === 'APPROVED' ? '资料变更已通过' : '资料变更未通过，请完善后重试',
    reviewedBy: localStorage.getItem('username') || 'admin'
  }).then(() => {
    ElMessage.success(status === 'APPROVED' ? '已通过申请' : '已驳回申请')
    loadChangeRequests()
    getList()
  }).catch((e) => {
    ElMessage.error(e?.response?.data?.message || e?.message || '审核失败')
  })
}
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

.filter-container {
  padding-bottom: 20px;
  display: flex;
  gap: 10px;
}

.filter-item {
  display: inline-block;
  vertical-align: middle;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.review-card {
  margin-top: 20px;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

:deep(.table-header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}
</style>
