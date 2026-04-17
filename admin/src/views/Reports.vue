<template>
  <div class="reports-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">举报管理</span>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="处理状态" clearable style="width: 140px" @change="loadData">
              <el-option label="待处理" :value="0" />
              <el-option label="已处理" :value="1" />
              <el-option label="已驳回" :value="2" />
            </el-select>
            <el-select v-model="typeFilter" placeholder="举报类型" clearable style="width: 140px" @change="loadData">
              <el-option label="动态" value="post" />
              <el-option label="评论" value="comment" />
              <el-option label="用户" value="user" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reporterId" label="举报人ID" width="100" />
        <el-table-column prop="targetType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ { post: '动态', comment: '评论', user: '用户' }[row.targetType] || row.targetType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" size="small">
              {{ row.status === 1 ? '已处理' : row.status === 2 ? '已驳回' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="举报时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" text @click="handleReport(row, 1)">处理</el-button>
            <el-button v-if="row.status === 0" type="info" size="small" text @click="handleReport(row, 2)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, handleReport as handleReportApi } from '../api/admin'

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const typeFilter = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReportList({ page: page.value, size: size.value, status: statusFilter.value, targetType: typeFilter.value || undefined })
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

const handleReport = async (row, status) => {
  const action = status === 1 ? '处理' : '驳回'
  await ElMessageBox.confirm(`确定要${action}该举报吗？`, '确认', { type: 'warning' })
  try {
    await handleReportApi(row.id, { status })
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: 600; font-size: 16px; }
.header-actions { display: flex; gap: 12px; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
