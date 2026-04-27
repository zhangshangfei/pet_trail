<template>
  <div class="vet-clinics-page">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="营业医院" :value="stats.totalClinics" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="合作医院" :value="stats.partnerClinics" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="总预约数" :value="stats.totalAppointments" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="待处理预约" :value="stats.pendingAppointments" /></el-card></el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="page-header">
          <span class="card-title">医院信息管理</span>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="营业" :value="1" /><el-option label="停业" :value="0" />
            </el-select>
            <el-select v-model="partnerFilter" placeholder="合作" clearable style="width: 120px" @change="loadData">
              <el-option label="合作医院" :value="true" /><el-option label="非合作" :value="false" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="openCreate">新增医院</el-button>
            <el-button @click="openAppointments">查看预约</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 50px; height: 50px" fit="cover" :preview-src-list="[row.coverImage]" preview-teleported />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="医院名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="businessHours" label="营业时间" width="130" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column prop="specialties" label="专科" width="120" show-overflow-tooltip />
        <el-table-column label="合作" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isPartner ? 'success' : 'info'" size="small">{{ row.isPartner ? '合作' : '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '营业' : '停业' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" text @click="changeStatus(row.id, 0)">停业</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" text @click="changeStatus(row.id, 1)">营业</el-button>
            <el-button v-if="!row.isPartner" type="primary" size="small" text @click="togglePartner(row.id, true)">设为合作</el-button>
            <el-button v-if="row.isPartner" type="info" size="small" text @click="togglePartner(row.id, false)">取消合作</el-button>
            <el-button v-if="canManage" type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑医院' : '新增医院'" width="700px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="医院名称" required><el-input v-model="form.name" /></el-form-item>
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
        <el-form-item label="地址" required><el-input v-model="form.address" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="纬度"><el-input-number v-model="form.latitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度"><el-input-number v-model="form.longitude" :precision="6" :step="0.001" :controls="false" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系电话" required><el-input v-model="form.phone" /></el-form-item>
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

    <el-dialog v-model="showAppointments" title="预约管理" width="1050px" destroy-on-close>
      <div style="margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap">
        <el-select v-model="appointmentStatusFilter" placeholder="预约状态" clearable style="width: 130px" @change="loadAppointments">
          <el-option label="待确认" :value="0" /><el-option label="已确认" :value="1" />
          <el-option label="已完成" :value="2" /><el-option label="已取消" :value="3" />
        </el-select>
        <el-select v-model="appointmentClinicFilter" placeholder="医院" clearable filterable style="width: 180px" @change="loadAppointments">
          <el-option v-for="c in allClinics" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="appointmentKeyword" placeholder="搜索症状" clearable style="width: 160px" @clear="loadAppointments" @keyup.enter="loadAppointments" />
        <el-button type="primary" @click="loadAppointments">查询</el-button>
      </div>
      <el-table :data="appointmentList" v-loading="appointmentLoading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userName" label="用户" width="100" show-overflow-tooltip />
        <el-table-column prop="clinicName" label="医院" width="140" show-overflow-tooltip />
        <el-table-column prop="petName" label="宠物" width="100" show-overflow-tooltip />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" />
        <el-table-column prop="appointmentTime" label="预约时间" width="110" />
        <el-table-column prop="symptom" label="症状" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="{ 0: 'warning', 1: '', 2: 'success', 3: 'danger' }[row.status]" size="small">
              {{ { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" text @click="changeAppointmentStatus(row.id, 1)">确认</el-button>
            <el-button v-if="row.status === 1" type="success" size="small" text @click="changeAppointmentStatus(row.id, 2)">完成</el-button>
            <el-button v-if="row.status === 0 || row.status === 1" type="danger" size="small" text @click="changeAppointmentStatus(row.id, 3)">取消</el-button>
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
import { Plus } from '@element-plus/icons-vue'
import { useAdminStore } from '@/store/admin'
import { getClinicList, getClinicDetail, createClinic, updateClinic, deleteClinic, updateClinicStatus, setClinicPartner, getClinicStats, getAppointmentList, updateAppointmentStatus } from '@/api/admin'

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasPermission('vet-clinic:manage'))

const uploadUrl = (import.meta.env.VITE_API_BASE_URL || '') + '/api/upload'
const uploadHeaders = { Authorization: 'Bearer ' + (localStorage.getItem('admin_token') || '') }

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

async function changeStatus(id, status) {
  try { await updateClinicStatus(id, status); ElMessage.success('状态更新成功'); loadData(); loadStats() } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

async function togglePartner(id, isPartner) {
  try { await setClinicPartner(id, isPartner); ElMessage.success(isPartner ? '已设为合作医院' : '已取消合作'); loadData(); loadStats() } catch (e) { console.error(e); ElMessage.error('操作失败') }
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
