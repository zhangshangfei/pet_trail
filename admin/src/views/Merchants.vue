<template>
  <div class="merchants-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">商户管理</span>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="搜索商户名称/联系人" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
            <el-select v-model="typeFilter" placeholder="类型" clearable style="width: 120px" @change="loadData">
              <el-option label="宠物医院" value="vet_clinic" />
              <el-option label="宠物商店" value="pet_shop" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="openCreate">新增商户</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="商户名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'vet_clinic' ? 'success' : row.type === 'pet_shop' ? 'warning' : 'info'" size="small">
              {{ { vet_clinic: '宠物医院', pet_shop: '宠物商店', other: '其他' }[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" text @click="changeStatus(row.id, 0)">禁用</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" text @click="changeStatus(row.id, 1)">启用</el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑商户' : '新增商户'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="商户名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="宠物医院" value="vet_clinic" />
            <el-option label="宠物商店" value="pet_shop" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantList, createMerchant, updateMerchant, updateMerchantStatus, deleteMerchant } from '@/api/admin'

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

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = { ...row }
  showDialog.value = true
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

async function changeStatus(id, status) {
  try {
    await updateMerchantStatus(id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
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
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
