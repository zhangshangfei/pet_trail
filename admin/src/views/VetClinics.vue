<template>
  <div class="vet-clinics-page">
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

    <!-- 医院列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">医院信息管理</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="营业" :value="1" /><el-option label="停业" :value="0" />
            </el-select>
            <el-select v-model="partnerFilter" placeholder="合作" clearable style="width: 120px" @change="loadData">
              <el-option label="合作医院" :value="true" /><el-option label="非合作" :value="false" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Plus" @click="openCreate">新增医院</el-button>
            <el-button :icon="Calendar" @click="openAppointments">查看预约</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 50px; height: 50px; border-radius: 6px;" fit="cover" :preview-src-list="[row.coverImage]" preview-teleported />
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="医院名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="businessHours" label="营业时间" width="130" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="80" align="center">
          <template #default="{ row }">
            <div class="rating-cell">
              <el-icon :size="12" style="color: #f7ba2a"><Star /></el-icon>
              <span>{{ row.rating || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="specialties" label="专科" width="120" show-overflow-tooltip />
        <el-table-column label="合作" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isPartner ? 'success' : 'info'" effect="light" size="small">{{ row.isPartner ? '合作' : '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" size="small">{{ row.status === 1 ? '营业' : '停业' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="EditPen" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1" size="small" text type="warning" :icon="CircleClose" @click="handleStatus(row, 0)">停业</el-button>
              <el-button v-if="row.status === 0" size="small" text type="success" :icon="CircleCheck" @click="handleStatus(row, 1)">营业</el-button>
              <el-button v-if="!row.isPartner" size="small" text type="primary" :icon="Connection" @click="handlePartner(row, true)">设为合作</el-button>
              <el-button v-if="row.isPartner" size="small" text type="info" :icon="Connection" @click="handlePartner(row, false)">取消合作</el-button>
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
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑医院' : '新增医院'" width="700px" destroy-on-close class="clinic-dialog">
      <div v-if="isEdit && form.id" class="dialog-profile">
        <div class="dialog-profile-icon" style="background: #ecf5ff; color: #409eff;">
          <el-icon :size="28"><OfficeBuilding /></el-icon>
        </div>
        <div class="dialog-profile-info">
          <div class="dialog-profile-name">{{ form.name || '未命名医院' }}</div>
          <div class="dialog-profile-meta">
            <el-tag :type="form.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>{{ form.status === 1 ? '营业中' : '已停业' }}</el-tag>
            <el-tag v-if="form.isPartner" type="success" size="small" effect="light" round>合作医院</el-tag>
          </div>
        </div>
      </div>
      <el-divider v-if="isEdit && form.id" class="dialog-divider" />
      <el-form :model="form" label-width="100px" class="dialog-form">
        <el-form-item label="医院名称" required><el-input v-model="form.name" placeholder="请输入医院名称" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入医院描述" /></el-form-item>
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
        <el-form-item label="地址" required><el-input v-model="form.address" placeholder="请输入医院地址" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="纬度"><el-input-number v-model="form.latitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度"><el-input-number v-model="form.longitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系电话" required><el-input v-model="form.phone" placeholder="请输入联系电话" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" placeholder="如: 周一至周日 09:00-21:00" /></el-form-item>
        <el-form-item label="评分"><el-input-number v-model="form.rating" :min="0" :max="5" :precision="1" :step="0.1" /></el-form-item>
        <el-form-item label="专科特色"><el-input v-model="form.specialties" placeholder="如: 内科,外科,皮肤科" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option label="营业" :value="1" /><el-option label="停业" :value="0" /></el-select>
        </el-form-item>
        <el-form-item label="合作医院">
          <el-switch v-model="form.isPartner" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预约管理弹窗 -->
    <el-dialog v-model="showAppointments" title="预约管理" width="1050px" destroy-on-close>
      <div style="margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap">
        <el-select v-model="appointmentStatusFilter" placeholder="预约状态" clearable style="width: 130px" @change="loadAppointments">
          <el-option label="待确认" :value="0" /><el-option label="已确认" :value="1" />
          <el-option label="已完成" :value="2" /><el-option label="已取消" :value="3" />
        </el-select>
        <el-select v-model="appointmentClinicFilter" placeholder="医院" clearable filterable style="width: 180px" @change="loadAppointments">
          <el-option v-for="c in allClinics" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="appointmentKeyword" placeholder="搜索症状" clearable style="width: 160px" @clear="loadAppointments" @keyup.enter="loadAppointments">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="loadAppointments">查询</el-button>
      </div>
      <el-table :data="appointmentList" v-loading="appointmentLoading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userName" label="用户" width="100" show-overflow-tooltip />
        <el-table-column prop="clinicName" label="医院" width="140" show-overflow-tooltip />
        <el-table-column prop="petName" label="宠物" width="100" show-overflow-tooltip />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" />
        <el-table-column prop="appointmentTime" label="预约时间" width="110" />
        <el-table-column prop="symptom" label="症状" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="{ 0: 'warning', 1: '', 2: 'success', 3: 'danger' }[row.status]" effect="light" size="small">
              {{ { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button v-if="row.status === 0" type="success" size="small" text :icon="CircleCheck" @click="changeAppointmentStatus(row.id, 1)">确认</el-button>
              <el-button v-if="row.status === 1" type="success" size="small" text :icon="CircleCheck" @click="changeAppointmentStatus(row.id, 2)">完成</el-button>
              <el-button v-if="row.status === 0 || row.status === 1" type="danger" size="small" text :icon="CircleClose" @click="changeAppointmentStatus(row.id, 3)">取消</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap" style="margin-top: 12px">
        <el-pagination v-model:current-page="appointmentPage" v-model:page-size="appointmentSize" :total="appointmentTotal" :page-sizes="[10, 20]" layout="total, prev, pager, next" @current-change="loadAppointments" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, EditPen, Delete, CircleCheck, CircleClose, Star, Calendar, Connection } from '@element-plus/icons-vue'
import { useAdminStore } from '@/store/admin'
import { getClinicList, getClinicDetail, createClinic, updateClinic, deleteClinic, updateClinicStatus, setClinicPartner, getClinicStats, getAppointmentList, updateAppointmentStatus } from '@/api/admin'

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('vet-clinic:manage'))

const uploadUrl = (import.meta.env.VITE_API_BASE_URL || '') + '/api/upload'
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + (adminStore.token || '') }))

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const partnerFilter = ref(null)
const stats = ref({ totalClinics: 0, partnerClinics: 0, totalAppointments: 0, pendingAppointments: 0 })

const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})

const showAppointments = ref(false)
const appointmentList = ref([])
const appointmentLoading = ref(false)
const appointmentPage = ref(1)
const appointmentSize = ref(20)
const appointmentTotal = ref(0)
const appointmentStatusFilter = ref(null)
const appointmentClinicFilter = ref(null)
const appointmentKeyword = ref('')
const allClinics = ref([])

const overviewCards = computed(() => [
  { key: 'total', label: '营业医院', value: stats.value.totalClinics, icon: 'OfficeBuilding', color: '#409eff', bg: '#ecf5ff' },
  { key: 'partner', label: '合作医院', value: stats.value.partnerClinics, icon: 'Connection', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'appointments', label: '总预约数', value: stats.value.totalAppointments, icon: 'Calendar', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'pending', label: '待处理预约', value: stats.value.pendingAppointments, icon: 'Clock', color: '#f56c6c', bg: '#fef0f0' }
])

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value != null) params.status = statusFilter.value
    if (partnerFilter.value != null) params.isPartner = partnerFilter.value
    const res = await getClinicList(params)
    if (res.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) { console.error(e); ElMessage.error('加载失败') } finally { loading.value = false }
}

async function loadStats() {
  try { const res = await getClinicStats(); if (res.data) stats.value = res.data } catch (e) { console.error(e) }
}

async function loadAppointments() {
  appointmentLoading.value = true
  try {
    const params = { page: appointmentPage.value, size: appointmentSize.value }
    if (appointmentStatusFilter.value != null) params.status = appointmentStatusFilter.value
    if (appointmentClinicFilter.value != null) params.clinicId = appointmentClinicFilter.value
    if (appointmentKeyword.value) params.keyword = appointmentKeyword.value
    const res = await getAppointmentList(params)
    if (res.data) { appointmentList.value = res.data.records || []; appointmentTotal.value = res.data.total || 0 }
  } catch (e) { console.error(e) } finally { appointmentLoading.value = false }
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.value = { status: 1, isPartner: false, rating: 5.0 }
  showDialog.value = true
}

async function openEdit(row) {
  try {
    const res = await getClinicDetail(row.id)
    const detail = res.data || row
    isEdit.value = true; editId.value = detail.id
    form.value = { ...detail }
    showDialog.value = true
  } catch (e) {
    console.error(e); ElMessage.error('获取详情失败')
  }
}

async function submitForm() {
  if (!form.value.name || !form.value.address || !form.value.phone) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    if (isEdit.value) { await updateClinic(editId.value, form.value); ElMessage.success('更新成功') }
    else { await createClinic(form.value); ElMessage.success('创建成功') }
    showDialog.value = false; loadData(); loadStats()
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

async function handleStatus(row, status) {
  const action = status === 1 ? '营业' : '停业'
  try {
    await ElMessageBox.confirm(`确定要将医院 "${row.name}" 设为${action}吗？`, '确认', { type: 'warning' })
    await updateClinicStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    loadData(); loadStats()
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('操作失败') } }
}

async function handlePartner(row, isPartner) {
  const action = isPartner ? '设为合作' : '取消合作'
  try {
    await ElMessageBox.confirm(`确定要将医院 "${row.name}" ${action}吗？`, '确认', { type: 'warning' })
    await setClinicPartner(row.id, isPartner)
    ElMessage.success(isPartner ? '已设为合作医院' : '已取消合作')
    loadData(); loadStats()
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('操作失败') } }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除医院"${row.name}"？`, '提示', { type: 'warning' })
    await deleteClinic(row.id); ElMessage.success('删除成功'); loadData(); loadStats()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

async function changeAppointmentStatus(id, status) {
  try { await updateAppointmentStatus(id, status); ElMessage.success('预约状态更新成功'); loadAppointments(); loadStats() } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

async function openAppointments() {
  showAppointments.value = true
  appointmentStatusFilter.value = null
  appointmentClinicFilter.value = null
  appointmentKeyword.value = ''
  appointmentPage.value = 1
  if (allClinics.value.length === 0) {
    try {
      const res = await getClinicList({ page: 1, size: 200 })
      if (res.data) allClinics.value = res.data.records || []
    } catch (e) { console.error(e) }
  }
  loadAppointments()
}

onMounted(() => { loadData(); loadStats() })

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
</script>

<style scoped>
.vet-clinics-page { padding: 0; }

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
.clinic-dialog :deep(.el-dialog__body) { padding-top: 8px; }
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
