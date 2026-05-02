<template>
  <div class="products-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="item in overviewCards" :key="item.key">
        <el-card shadow="hover" class="overview-card" :body-style="{ padding: '16px 20px' }">
          <div class="overview-content">
            <div class="overview-icon" :style="{ background: item.bg, color: item.color }">
              <el-icon :size="20"><component :is="item.icon" /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ item.value }}</div>
              <div class="overview-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">商城管理</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-select v-model="categoryFilter" placeholder="分类" clearable style="width: 120px" @change="loadData">
              <el-option label="粮食" value="food" /><el-option label="玩具" value="toy" />
              <el-option label="保健" value="health" /><el-option label="用品" value="supplies" />
            </el-select>
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="上架" :value="1" /><el-option label="下架" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Plus" @click="openCreate">新增商品</el-button>
            <el-button :icon="List" @click="showOrders = true">查看订单</el-button>
            <el-button :icon="Download" @click="handleExport" v-if="canExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 50px; height: 50px; border-radius: 6px;" fit="cover" :preview-src-list="[row.coverImage]" preview-teleported />
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="分类" width="90" align="center">
          <template #default="{ row }">
            <el-tag effect="light" size="small">{{ { food: '粮食', toy: '玩具', health: '保健', supplies: '用品' }[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" width="100" show-overflow-tooltip />
        <el-table-column label="价格" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">¥{{ row.price }}</span>
            <span v-if="row.originalPrice" style="text-decoration: line-through; color: #bbb; font-size: 12px; margin-left: 4px">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="80" align="center" />
        <el-table-column label="评分" width="80" align="center">
          <template #default="{ row }">
            <div class="rating-cell">
              <el-icon :size="12" style="color: #f7ba2a"><Star /></el-icon>
              <span>{{ row.rating || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="适用" width="80" align="center">
          <template #default="{ row }">{{ { 0: '通用', 1: '猫', 2: '狗' }[row.petType] || '通用' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="EditPen" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1" size="small" text type="warning" :icon="CircleClose" @click="handleStatus(row, 0)">下架</el-button>
              <el-button v-if="row.status === 0" size="small" text type="success" :icon="CircleCheck" @click="handleStatus(row, 1)">上架</el-button>
              <el-button v-if="canManage" size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑商品' : '新增商品'" width="650px" destroy-on-close class="product-dialog">
      <div v-if="isEdit && form.id" class="dialog-profile">
        <div class="dialog-profile-icon" style="background: #fdf6ec; color: #e6a23c;">
          <el-icon :size="28"><Goods /></el-icon>
        </div>
        <div class="dialog-profile-info">
          <div class="dialog-profile-name">{{ form.name || '未命名商品' }}</div>
          <div class="dialog-profile-meta">
            <el-tag :type="form.status === 1 ? 'success' : 'info'" size="small" effect="light" round>{{ form.status === 1 ? '上架中' : '已下架' }}</el-tag>
            <span v-if="form.price" class="dialog-profile-price">¥{{ form.price }}</span>
          </div>
        </div>
      </div>
      <el-divider v-if="isEdit && form.id" class="dialog-divider" />
      <el-form :model="form" label-width="100px" class="dialog-form">
        <el-form-item label="商品名称" required><el-input v-model="form.name" placeholder="请输入商品名称" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" /></el-form-item>
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
        <el-form-item label="品牌"><el-input v-model="form.brand" placeholder="请输入品牌" /></el-form-item>
        <el-form-item label="售价" required><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="佣金比例"><el-input-number v-model="form.commissionRate" :min="0" :max="1" :precision="4" :step="0.01" /></el-form-item>
        <el-form-item label="来源链接"><el-input v-model="form.sourceUrl" placeholder="请输入来源链接" /></el-form-item>
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

    <!-- 订单列表弹窗 -->
    <el-dialog v-model="showOrders" title="订单列表" width="900px" destroy-on-close>
      <div style="margin-bottom: 12px; display: flex; gap: 12px;">
        <el-select v-model="orderStatusFilter" placeholder="订单状态" clearable style="width: 140px" @change="loadOrders">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已退款" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadOrders">查询</el-button>
      </div>
      <el-table :data="orderList" v-loading="orderLoading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="plan" label="套餐" width="100" />
        <el-table-column label="金额" width="100" align="center">
          <template #default="{ row }"><span style="color: #f56c6c; font-weight: 600">¥{{ row.amount }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="{ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[row.status]" effect="light" size="small">
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
import { Plus, Search, EditPen, Delete, CircleCheck, CircleClose, Star, Download, List, Goods } from '@element-plus/icons-vue'
import { useAdminStore } from '@/store/admin'
import { getProductList, getProductDetail, createProduct, updateProduct, deleteProduct, updateProductStatus, getProductStats, getOrderList, exportProducts } from '@/api/admin'

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('product:manage'))
const canExport = computed(() => adminStore.hasButton('export'))

const uploadUrl = (import.meta.env.VITE_API_BASE_URL || '') + '/api/upload'
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + (adminStore.token || '') }))

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

const overviewCards = computed(() => [
  { key: 'products', label: '上架商品', value: stats.value.totalProducts, icon: 'Goods', color: '#409eff', bg: '#ecf5ff' },
  { key: 'orders', label: '总订单', value: stats.value.totalOrders, icon: 'List', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'paid', label: '已支付', value: stats.value.paidOrders, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'rate', label: '支付率', value: payRate.value, icon: 'TrendCharts', color: '#f56c6c', bg: '#fef0f0' }
])

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

async function handleStatus(row, status) {
  const action = status === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要将商品 "${row.name}" ${action}吗？`, '确认', { type: 'warning' })
    await updateProductStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    loadData(); loadStats()
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('操作失败') } }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商品"${row.name}"？`, '提示', { type: 'warning' })
    await deleteProduct(row.id); ElMessage.success('删除成功'); loadData(); loadStats()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

async function handleExport() {
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
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
  } catch (e) {}
}

onMounted(() => { loadData(); loadStats() })
</script>

<style scoped>
.products-page { padding: 0; }

.stats-row { margin-bottom: 16px; }
.overview-card { transition: transform 0.2s; }
.overview-card:hover { transform: translateY(-2px); }
.overview-content { display: flex; align-items: center; gap: 14px; }
.overview-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.overview-info { flex: 1; min-width: 0; }
.overview-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.overview-label { font-size: 12px; color: #909399; margin-top: 2px; }

.table-card { min-height: 500px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

.rating-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

.cover-uploader :deep(.el-upload) { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; width: 120px; height: 120px; display: flex; align-items: center; justify-content: center; }
.cover-uploader :deep(.el-upload:hover) { border-color: #409eff; }
.cover-preview { width: 120px; height: 120px; }
.cover-uploader-icon { font-size: 28px; color: #8c939d; }

/* 弹窗样式 */
.product-dialog :deep(.el-dialog__body) { padding-top: 8px; }
.dialog-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 4px 12px;
}
.dialog-profile-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.dialog-profile-info { flex: 1; }
.dialog-profile-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.dialog-profile-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.dialog-profile-price { color: #f56c6c; font-weight: 600; font-size: 14px; }
.dialog-divider { margin: 4px 0 16px; }
.dialog-form :deep(.el-form-item) { margin-bottom: 16px; }
</style>
