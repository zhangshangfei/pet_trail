<template>
  <div class="merchants-page">
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

    <!-- 商户列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">商户管理</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="搜索商户名称/联系人" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="typeFilter" placeholder="类型" clearable style="width: 120px" @change="loadData">
              <el-option label="宠物医院" value="vet_clinic" />
              <el-option label="宠物商店" value="pet_shop" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Plus" @click="openCreate">新增商户</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="商户名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'vet_clinic' ? 'success' : row.type === 'pet_shop' ? 'warning' : 'info'" effect="light" size="small">
              {{ { vet_clinic: '宠物医院', pet_shop: '宠物商店', other: '其他' }[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="12"><Clock /></el-icon>
              <span>{{ row.createdAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="EditPen" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1" size="small" text type="warning" :icon="CircleClose" @click="handleStatus(row, 0)">禁用</el-button>
              <el-button v-if="row.status === 0" size="small" text type="success" :icon="CircleCheck" @click="handleStatus(row, 1)">启用</el-button>
              <el-button size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑商户' : '新增商户'" width="500px" destroy-on-close class="merchant-dialog">
      <div v-if="isEdit && form.id" class="dialog-profile">
        <div class="dialog-profile-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><Shop /></el-icon>
        </div>
        <div class="dialog-profile-info">
          <div class="dialog-profile-name">{{ form.name || '未命名商户' }}</div>
          <div class="dialog-profile-meta">
            <el-tag :type="form.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>{{ form.status === 1 ? '正常' : '已禁用' }}</el-tag>
            <el-tag :type="form.type === 'vet_clinic' ? 'success' : form.type === 'pet_shop' ? 'warning' : 'info'" size="small" effect="light" round>
              {{ { vet_clinic: '宠物医院', pet_shop: '宠物商店', other: '其他' }[form.type] || form.type }}
            </el-tag>
          </div>
        </div>
      </div>
      <el-divider v-if="isEdit && form.id" class="dialog-divider" />
      <el-form :model="form" label-width="100px" class="dialog-form">
        <el-form-item label="商户名称" required><el-input v-model="form.name" placeholder="请输入商户名称" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="宠物医院" value="vet_clinic" />
            <el-option label="宠物商店" value="pet_shop" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" placeholder="请输入联系人" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" placeholder="请输入联系电话" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="正常" :value="1" /><el-option label="禁用" :value="0" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, EditPen, Delete, CircleCheck, CircleClose, Clock, Shop } from '@element-plus/icons-vue'
import { getMerchantList, getMerchantDetail, createMerchant, updateMerchant, updateMerchantStatus, deleteMerchant } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const keyword = ref('')
const typeFilter = ref(null)
const statusFilter = ref(null)

const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})

const overviewCards = computed(() => [
  { key: 'total', label: '总商户数', value: total.value, icon: 'Shop', color: '#409eff', bg: '#ecf5ff' },
  { key: 'clinic', label: '宠物医院', value: tableData.value.filter(m => m.type === 'vet_clinic').length, icon: 'OfficeBuilding', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'shop', label: '宠物商店', value: tableData.value.filter(m => m.type === 'pet_shop').length, icon: 'ShoppingCart', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'disabled', label: '已禁用', value: tableData.value.filter(m => m.status === 0).length, icon: 'CircleClose', color: '#f56c6c', bg: '#fef0f0' }
])

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (typeFilter.value) params.type = typeFilter.value
    if (statusFilter.value != null) params.status = statusFilter.value
    const res = await getMerchantList(params)
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { type: 'vet_clinic', status: 1 }
  showDialog.value = true
}

async function openEdit(row) {
  try {
    const res = await getMerchantDetail(row.id)
    const detail = res.data || row
    isEdit.value = true
    editId.value = detail.id
    form.value = { ...detail }
    showDialog.value = true
  } catch (e) {}
}

async function submitForm() {
  if (!form.value.name || !form.value.type) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    if (isEdit.value) {
      await updateMerchant(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createMerchant(form.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadData()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

async function handleStatus(row, status) {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}商户 "${row.name}" 吗？`, '确认', { type: 'warning' })
    await updateMerchantStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('操作失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商户"${row.name}"？`, '提示', { type: 'warning' })
    await deleteMerchant(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.merchants-page { padding: 0; }

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

.time-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* 弹窗样式 */
.merchant-dialog :deep(.el-dialog__body) { padding-top: 8px; }
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
.dialog-divider { margin: 4px 0 16px; }
.dialog-form :deep(.el-form-item) { margin-bottom: 16px; }
</style>
