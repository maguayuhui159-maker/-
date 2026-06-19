<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="线下活动" name="activity">
        <el-card v-if="role === 'ADMIN'" shadow="never" style="margin-bottom: 12px">
          <template #header><span>发布活动</span></template>
          <el-form :model="activityForm" inline>
            <el-form-item label="标题"><el-input v-model="activityForm.title" /></el-form-item>
            <el-form-item label="城市"><el-input v-model="activityForm.city" /></el-form-item>
            <el-form-item label="地点"><el-input v-model="activityForm.location" /></el-form-item>
            <el-form-item label="时间"><el-date-picker v-model="activityForm.activityTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
            <el-form-item label="名额"><el-input-number v-model="activityForm.quota" :min="1" /></el-form-item>
            <el-form-item label="价格"><el-input-number v-model="activityForm.price" :min="0" :precision="2" /></el-form-item>
            <el-form-item><el-button type="success" :loading="creatingActivity" @click="handleCreateActivity">发布</el-button></el-form-item>
          </el-form>
        </el-card>

        <el-table :data="activities" border v-loading="loadingActivities">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="活动" min-width="180" />
          <el-table-column prop="city" label="城市" width="120" />
          <el-table-column prop="location" label="地点" min-width="180" />
          <el-table-column prop="activityTime" label="时间" width="180" />
          <el-table-column label="名额" width="120">
            <template #default="{row}">{{ row.bookedCount }}/{{ row.quota }}</template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100" />
          <el-table-column label="操作" width="120" v-if="role !== 'ADMIN'">
            <template #default="{row}">
              <el-button type="primary" size="small" :disabled="row.status !== 'OPEN'" @click="book(row)">报名</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的订单" name="order">
        <div style="display:flex; gap:10px; margin-bottom: 10px" v-if="role==='ADMIN'">
          <el-input v-model="orderQuery.keyword" placeholder="订单号/备注" style="width:220px" />
          <el-select v-model="orderQuery.status" clearable placeholder="状态" style="width:140px">
            <el-option label="已支付" value="PAID" />
            <el-option label="退款中" value="REFUNDING" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
          <el-button type="primary" @click="loadOrders">查询</el-button>
        </div>
        <el-table :data="orders" border v-loading="loadingOrders">
          <el-table-column prop="orderNo" label="订单号" min-width="170" />
          <el-table-column prop="buyerName" label="买家" width="120" />
          <el-table-column prop="workTitle" label="作品" min-width="180" />
          <el-table-column prop="amount" label="金额" width="90" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="240">
            <template #default="{row}">
              <el-button v-if="role !== 'ADMIN'" size="small" type="warning" plain @click="openAfterSale(row)">申请售后</el-button>
              <el-select v-if="role === 'ADMIN'" :model-value="row.status" size="small" style="width:110px" @change="(v)=>changeOrderStatus(row,v)">
                <el-option label="PAID" value="PAID" />
                <el-option label="SHIPPED" value="SHIPPED" />
                <el-option label="RECEIVED" value="RECEIVED" />
                <el-option label="REFUNDED" value="REFUNDED" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>

        <el-divider>售后申请</el-divider>
        <el-table :data="afterSales" border v-loading="loadingAfterSales">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="orderNo" label="订单号" min-width="160" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="reason" label="原因" min-width="220" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="reviewComment" label="审核备注" min-width="160" />
          <el-table-column label="审核" width="180" v-if="role==='ADMIN'">
            <template #default="{row}">
              <el-button size="small" type="success" plain @click="review(row,'APPROVED')">通过</el-button>
              <el-button size="small" type="danger" plain @click="review(row,'REJECTED')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的活动报名" name="booking">
        <el-table :data="bookings" border v-loading="loadingBookings">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="activityTitle" label="活动" min-width="180" />
          <el-table-column prop="city" label="城市" width="120" />
          <el-table-column prop="activityTime" label="活动时间" width="180" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="payAmount" label="金额" width="90" />
          <el-table-column label="管理" width="220" v-if="role==='ADMIN'">
            <template #default="{row}">
              <el-button size="small" @click="changeBooking(row,'CHECKED_IN')">核销</el-button>
              <el-button size="small" type="warning" @click="changeBooking(row,'REFUNDED')">退款</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="afterSaleVisible" title="申请售后" width="480px">
      <el-form :model="afterSaleForm" label-width="90px">
        <el-form-item label="类型">
          <el-select v-model="afterSaleForm.type" style="width: 100%">
            <el-option label="退款" value="REFUND" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="afterSaleForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="afterSaleVisible=false">取消</el-button>
        <el-button type="primary" :loading="submittingAfterSale" @click="submitAfterSale">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchOfflineActivities,
  createOfflineActivity,
  createOfflineBooking,
  fetchMyOfflineBookings,
  fetchAllOfflineBookings,
  updateOfflineBookingStatus
} from '../api/offline'
import {
  fetchMyOrders,
  fetchAllOrders,
  updateOrderStatus,
  applyAfterSale,
  fetchAfterSaleList,
  reviewAfterSale
} from '../api/order'

const role = ref(localStorage.getItem('role') || 'STUDENT')
const activeTab = ref('activity')
const loadingActivities = ref(false)
const loadingOrders = ref(false)
const loadingAfterSales = ref(false)
const loadingBookings = ref(false)
const creatingActivity = ref(false)
const afterSaleVisible = ref(false)
const submittingAfterSale = ref(false)
const currentOrderId = ref(null)

const activities = ref([])
const orders = ref([])
const afterSales = ref([])
const bookings = ref([])

const orderQuery = reactive({ status: '', keyword: '' })
const activityForm = reactive({ title: '', city: '', location: '', activityTime: '', quota: 30, price: 0, description: '' })
const afterSaleForm = reactive({ type: 'REFUND', reason: '' })

const loadActivities = async () => {
  loadingActivities.value = true
  try {
    const res = await fetchOfflineActivities('OPEN')
    activities.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '活动加载失败')
  } finally {
    loadingActivities.value = false
  }
}

const loadOrders = async () => {
  loadingOrders.value = true
  try {
    const res = role.value === 'ADMIN' ? await fetchAllOrders(orderQuery) : await fetchMyOrders()
    orders.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '订单加载失败')
  } finally {
    loadingOrders.value = false
  }
}

const loadAfterSales = async () => {
  if (role.value !== 'ADMIN') return
  loadingAfterSales.value = true
  try {
    const res = await fetchAfterSaleList()
    afterSales.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '售后加载失败')
  } finally {
    loadingAfterSales.value = false
  }
}

const loadBookings = async () => {
  loadingBookings.value = true
  try {
    const res = role.value === 'ADMIN' ? await fetchAllOfflineBookings() : await fetchMyOfflineBookings()
    bookings.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '报名加载失败')
  } finally {
    loadingBookings.value = false
  }
}

const handleCreateActivity = async () => {
  if (!activityForm.title) {
    ElMessage.warning('请填写活动标题')
    return
  }
  creatingActivity.value = true
  try {
    const payload = {
      ...activityForm,
      activityTime: activityForm.activityTime ? String(activityForm.activityTime).replace(' ', 'T') : ''
    }
    await createOfflineActivity(payload)
    ElMessage.success('活动已发布')
    activityForm.title = ''
    activityForm.city = ''
    activityForm.location = ''
    activityForm.activityTime = ''
    activityForm.description = ''
    await loadActivities()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '发布失败')
  } finally {
    creatingActivity.value = false
  }
}

const book = async (row) => {
  try {
    await createOfflineBooking(row.id)
    ElMessage.success('报名成功')
    await loadActivities()
    await loadBookings()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '报名失败')
  }
}

const changeOrderStatus = async (row, status) => {
  try {
    await updateOrderStatus(row.id, status)
    ElMessage.success('订单状态已更新')
    await loadOrders()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '更新失败')
  }
}

const openAfterSale = (row) => {
  currentOrderId.value = row.id
  afterSaleForm.type = 'REFUND'
  afterSaleForm.reason = ''
  afterSaleVisible.value = true
}

const submitAfterSale = async () => {
  if (!currentOrderId.value) return
  submittingAfterSale.value = true
  try {
    await applyAfterSale(currentOrderId.value, afterSaleForm)
    ElMessage.success('售后申请已提交')
    afterSaleVisible.value = false
    await loadOrders()
    await loadAfterSales()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交失败')
  } finally {
    submittingAfterSale.value = false
  }
}

const review = async (row, status) => {
  try {
    await reviewAfterSale(row.id, { status, reviewComment: status === 'APPROVED' ? '同意售后' : '不符合售后条件' })
    ElMessage.success('审核完成')
    await loadAfterSales()
    await loadOrders()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '审核失败')
  }
}

const changeBooking = async (row, status) => {
  try {
    await updateOfflineBookingStatus(row.id, status)
    ElMessage.success('报名状态已更新')
    await loadBookings()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '更新失败')
  }
}

onMounted(async () => {
  await Promise.all([loadActivities(), loadOrders(), loadBookings(), loadAfterSales()])
})
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
</style>
