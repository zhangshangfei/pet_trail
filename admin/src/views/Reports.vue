<template>
  <div class="reports-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">举报管理</span>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="处理状态" clearable style="width: 130px" @change="loadData">
              <el-option label="待处理" :value="0" />
              <el-option label="已处理" :value="1" />
              <el-option label="已驳回" :value="2" />
            </el-select>
            <el-select v-model="typeFilter" placeholder="举报类型" clearable style="width: 130px" @change="loadData">
              <el-option label="动态" value="post" />
              <el-option label="评论" value="comment" />
              <el-option label="用户" value="user" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reporterId" label="举报人" width="90" />
        <el-table-column prop="targetType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ { post: '动态', comment: '评论', user: '用户' }[row.targetType] || row.targetType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="reason" label="举报原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" size="small">
              {{ row.status === 1 ? '已处理' : row.status === 2 ? '已驳回' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="处理结果" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="举报时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" text @click="openHandle(row, 1)">处理</el-button>
            <el-button v-if="row.status === 0" type="info" size="small" text @click="openHandle(row, 2)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="showHandle" title="处理举报" width="500px">
      <el-form label-width="100px">
        <el-form-item label="处理结果">
          <el-tag :type="handleStatus === 1 ? 'success' : 'info'">{{ handleStatus === 1 ? '处理' : '驳回' }}</el-tag>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleResult" type="textarea" :rows="3" placeholder="请输入处理结果说明" />
        </el-form-item>
        <el-form-item label="联动处罚" v-if="handleStatus === 1">
          <el-checkbox v-model="punishAction.removeContent">下架举报内容</el-checkbox>
          <el-checkbox v-model="punishAction.banUser">禁言被举报用户</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHandle = false">取消</el-button>
        <el-button :type="handleStatus === 1 ? 'success' : 'info'" @click="submitHandle">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReportList, handleReport as handleReportApi, deletePost, updateUserStatus, deleteComment, exportReports } from '../api/admin'

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const typeFilter = ref('')
const showHandle = ref(false)
const handleReportId = ref(null)
const handleStatus = ref(1)
const handleResult = ref('')
const currentReport = ref(null)
const punishAction = ref({ removeContent: false, banUser: false })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReportList({ page: page.value, size: size.value, status: statusFilter.value, targetType: typeFilter.value || undefined })
    if (res.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) {} finally { loading.value = false }
}

const openHandle = (row, status) => {
  currentReport.value = row
  handleReportId.value = row.id
  handleStatus.value = status
  handleResult.value = ''
  punishAction.value = { removeContent: false, banUser: false }
  showHandle.value = true
}

const submitHandle = async () => {
  try {
    await handleReportApi(handleReportId.value, { status: handleStatus.value, result: handleResult.value || undefined })

    if (handleStatus.value === 1 && currentReport.value) {
      if (punishAction.value.removeContent) {
        try {
          if (currentReport.value.targetType === 'post') await deletePost(currentReport.value.targetId)
          else if (currentReport.value.targetType === 'comment') await deleteComment(currentReport.value.targetId)
        } catch (e) {}
      }
      if (punishAction.value.banUser && currentReport.value.targetType === 'user') {
        try { await updateUserStatus(currentReport.value.targetId, 0) } catch (e) {}
      }
    }

    ElMessage.success(handleStatus.value === 1 ? '处理成功' : '已驳回')
    showHandle.value = false
    loadData()
  } catch (e) {}
}

const handleExport = async () => {
  try {
    const res = await exportReports({ status: statusFilter.value ?? undefined })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `举报数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; font-size: 16px; }
.header-actions { display: flex; gap: 12px; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
