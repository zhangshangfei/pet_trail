<template>
  <div class="page-container">
    <div class="page-header"><h2>操作日志</h2></div>
    <div class="filter-bar">
      <el-input v-model="adminName" placeholder="操作人" clearable style="width: 150px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="module" placeholder="模块" clearable style="width: 130px" @change="loadData">
        <el-option label="用户" value="user" />
        <el-option label="动态" value="post" />
        <el-option label="评论" value="comment" />
        <el-option label="举报" value="report" />
        <el-option label="通知" value="notification" />
        <el-option label="管理员" value="admin" />
        <el-option label="设置" value="setting" />
        <el-option label="反馈" value="feedback" />
        <el-option label="宠物" value="pet" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 260px" @change="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="handleExport">导出Excel</el-button>
    </div>
    <el-card shadow="hover">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="90" />
        <el-table-column prop="action" label="动作" width="90" />
        <el-table-column label="目标" width="150">
          <template #default="{ row }">{{ row.targetType || '-' }} #{{ row.targetId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" show-overflow-tooltip min-width="200" />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-if="total > 0" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
      </div>
    </el-card>

    <el-dialog v-model="showDetail" title="日志详情" width="550px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.adminName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="动作">{{ detail.action || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ detail.targetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ detail.targetId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="详情" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ detail.detail || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detail.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent" :span="2">{{ detail.userAgent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2" v-if="detail.requestData">
          <div style="white-space: pre-wrap; line-height: 1.6; max-height: 200px; overflow-y: auto;">{{ detail.requestData }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOperationLogs, exportLogs } from '../api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const adminName = ref('')
const module = ref('')
const dateRange = ref(null)
const showDetail = ref(false)
const detail = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (adminName.value) params.adminName = adminName.value
    if (module.value) params.module = module.value
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getOperationLogs(params)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {}
  loading.value = false
}

const handlePageChange = (p) => { page.value = p; loadData() }

const viewDetail = (row) => {
  detail.value = { ...row }
  showDetail.value = true
}

const handleExport = async () => {
  try {
    const res = await exportLogs({ module: module.value || undefined })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `操作日志_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
