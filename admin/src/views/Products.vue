<template>
  <div class="products-page">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="上架商品" :value="stats.totalProducts" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="总订单" :value="stats.totalOrders" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="已支付" :value="stats.paidOrders" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="支付率" :value="payRate" /></el-card></el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="page-header">
          <span class="card-title">商城管理</span>
          <div class="header-actions">
            <el-select v-model="categoryFilter" placeholder="分类" clearable style="width: 120px" @change="loadData">
              <el-option label="粮食" value="food" /><el-option label="玩具" value="toy" />
              <el-option label="保健" value="health" /><el-option label="用品" value="supplies" />
            </el-select>
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="上架" :value="1" /><el-option label="下架" :value="0" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="openCreate">新增商品</el-button>
            <el-button @click="showOrders = true">查看订单</el-button>
            <el-button @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 50px; height: 50px" fit="cover" :preview-src-list="[row.coverImage]" preview-teleported />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ { food: '粮食', toy: '玩具', health: '保健', supplies: '用品' }[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" width="100" show-overflow-tooltip />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">¥{{ row.price }}</span>
            <span v-if="row.originalPrice" style="text-decoration: line-through; color: #bbb; font-size: 12px; margin-left: 4px">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column prop="petType" label="适用" width="80">
          <template #default="{ row }">{{ { 0: '通用', 1: '猫', 2: '狗' }[row.petType] || '通用' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" text @click="changeStatus(row.id, 0)">下架</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" text @click="changeStatus(row.id, 1)">上架</el-button>
            <el-button v-if="canManage" type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑商品' : '新增商品'" width="650px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="封面图">
          <el-upload
            class="cover-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            name="file"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            accept="image/*"
          >
            <el-image v-if="form.coverImage" :src="form.coverImage" fit="cover" class="cover-preview" preview-teleported />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <el-input v-model="form.coverImage" placeholder="或手动输入图片URL" style="margin-top: 8px" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category"><el-option label="粮食" value="food" /><el-option label="玩具" value="toy" /><el-option label="保健" value="health" /><el-option label="用品" value="supplies" /></el-select>
        </el-form-item>
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="售价" required><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="佣金比例"><el-input-number v-model="form.commissionRate" :min="0" :max="1" :precision="4" :step="0.01" /></el-form-item>
        <el-form-item label="来源链接"><el-input v-model="form.sourceUrl" /></el-form-item>
        <el-form-item label="来源平台">
          <el-select v-model="form.sourcePlatform"><el-option label="京东" value="jd" /><el-option label="淘宝" value="tb" /><el-option label="拼多多" value="pdd" /></el-select>
        </el-form-item>
        <el-form-item label="适用宠物">
          <el-select v-model="form.petType"><el-option label="通用" :value="0" /><el-option label="猫" :value="1" /><el-option label="狗" :value="2" /></el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showOrders" title="订单列表" width="900px" destroy-on-close>
      <div style="margin-bottom: 12px; display: flex; gap: 12px;">
        <el-select v-model="orderStatusFilter" placeholder="订单状态" clearable style="width: 140px" @change="loadOrders">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已退款" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
      </div>
      <el-table :data="orderList" v-loading="orderLoading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="plan" label="套餐" width="100" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }"><span style="color: #f56c6c">¥{{ row.amount }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="{ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[row.status]" size="small">
              {{ { 0: '待支付', 1: '已支付', 2: '已退款', 3: '已取消' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>
      <div class="pagination-wrap" style="margin-top: 12px">
        <el-pagination v-model:current-page="orderPage" v-model:page-size="orderSize" :total="orderTotal" :page-sizes="[10, 20]" layout="total, prev, pager, next" @current-change="loadOrders" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAdminStore } from '@/store/admin'
import { getProductList, getProductDetail, createProduct, updateProduct, deleteProduct, updateProductStatus, getProductStats, getOrderList, exportProducts } from '@/api/admin'

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('product:manage'))

const uploadUrl = (import.meta.env.VITE_API_BASE_URL || '') + '/api/upload'
const uploadHeaders = { Authorization: 'Bearer ' + (localStorage.getItem('admin_token') || '') }

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const categoryFilter = ref(null)
const statusFilter = ref(null)
const stats = ref({ totalProducts: 0, totalOrders: 0, paidOrders: 0 })
const payRate = computed(() => {
  if (!stats.value.totalOrders) return '0%'
  return (stats.value.paidOrders / stats.value.totalOrders * 100).toFixed(1) + '%'
})

const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})

const showOrders = ref(false)
const orderList = ref([])
const orderLoading = ref(false)
const orderPage = ref(1)
const orderSize = ref(20)
const orderTotal = ref(0)
const orderStatusFilter = ref(null)

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (categoryFilter.value) params.category = categoryFilter.value
    if (statusFilter.value != null) params.status = statusFilter.value
    const res = await getProductList(params)
    if (res.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) { console.error(e); ElMessage.error('加载失败') } finally { loading.value = false }
}

async function loadStats() {
  try { const res = await getProductStats(); if (res.data) stats.value = res.data } catch (e) { console.error(e) }
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const params = { page: orderPage.value, size: orderSize.value }
    if (orderStatusFilter.value != null) params.status = orderStatusFilter.value
    const res = await getOrderList(params)
    if (res.data) { orderList.value = res.data.records || []; orderTotal.value = res.data.total || 0 }
  } catch (e) { console.error(e) } finally { orderLoading.value = false }
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.value = { category: 'food', petType: 0, status: 1, sortOrder: 0, commissionRate: 0 }
  showDialog.value = true
}

function handleCoverSuccess(response) {
  if (response.success && response.data && response.data.url) {
    form.value.coverImage = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

function beforeCoverUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过5MB'); return false }
  return true
}

async function openEdit(row) {
  try {
    const res = await getProductDetail(row.id)
    const detail = res.data || row
    isEdit.value = true; editId.value = detail.id
    form.value = { ...detail }
    showDialog.value = true
  } catch (e) {
    console.error(e); ElMessage.error('获取详情失败')
  }
}

async function submitForm() {
  if (!form.value.name || !form.value.category || form.value.price == null) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    if (isEdit.value) { await updateProduct(editId.value, form.value); ElMessage.success('更新成功') }
    else { await createProduct(form.value); ElMessage.success('创建成功') }
    showDialog.value = false; loadData(); loadStats()
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

async function changeStatus(id, status) {
  try { await updateProductStatus(id, status); ElMessage.success('状态更新成功'); loadData(); loadStats() } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商品"${row.name}"？`, '提示', { type: 'warning' })
    await deleteProduct(row.id); ElMessage.success('删除成功'); loadData(); loadStats()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

async function handleExport() {
  try {
    const res = await exportProducts({ status: statusFilter.value ?? undefined })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `商品数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) { ElMessage.error('导出失败') }
}

onMounted(() => { loadData(); loadStats() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.stats-row .el-card { text-align: center; }
.cover-uploader :deep(.el-upload) { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; width: 120px; height: 120px; display: flex; align-items: center; justify-content: center; }
.cover-uploader :deep(.el-upload:hover) { border-color: #409eff; }
.cover-preview { width: 120px; height: 120px; }
.cover-uploader-icon { font-size: 28px; color: #8c939d; }
</style>
