<template>
  <div class="reports-page">
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

    <!-- 举报列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">举报列表</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
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
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Download" @click="handleExport" v-if="canExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="举报人" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.reporterAvatar" icon="UserFilled" />
              <div class="user-info">
                <div class="user-name">{{ row.reporterNickname || '未知用户' }}</div>
                <div class="user-meta">
                  <span class="user-id">ID: {{ row.reporterId }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.targetType] || ''" effect="light" size="small">
              {{ { post: '动态', comment: '评论', user: '用户' }[row.targetType] || row.targetType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="80" align="center" />
        <el-table-column prop="reason" label="举报原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" effect="light" size="small">
              {{ row.status === 1 ? '已处理' : row.status === 2 ? '已驳回' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="处理结果" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.result">{{ row.result }}</span>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="举报时间" width="170">
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
              <el-button size="small" text type="primary" :icon="View" @click="viewDetail(row)">详情</el-button>
              <el-button v-if="row.status === 0" size="small" text type="success" :icon="CircleCheck" @click="openHandle(row, 1)">处理</el-button>
              <el-button v-if="row.status === 0" size="small" text type="info" :icon="CircleClose" @click="openHandle(row, 2)">驳回</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 举报详情弹窗 -->
    <el-dialog v-model="showDetail" title="举报详情" width="640px" destroy-on-close class="report-detail-dialog">
      <div v-if="detailReport" class="rd-header">
        <div class="rd-avatar">
          <el-avatar :size="56" :src="detailReport.reporterAvatar" icon="UserFilled" />
        </div>
        <div class="rd-header-info">
          <div class="rd-name">{{ detailReport.reporterNickname || '未知用户' }}</div>
          <div class="rd-tags">
            <el-tag :type="detailReport.status === 1 ? 'success' : detailReport.status === 2 ? 'info' : 'warning'" size="small" effect="light" round>
              {{ detailReport.status === 1 ? '已处理' : detailReport.status === 2 ? '已驳回' : '待处理' }}
            </el-tag>
            <el-tag :type="typeTagMap[detailReport.targetType] || ''" size="small" effect="light" round>
              {{ { post: '动态', comment: '评论', user: '用户' }[detailReport.targetType] || detailReport.targetType }}
            </el-tag>
            <span class="rd-tag-item">
              <el-icon :size="12"><Clock /></el-icon>
              {{ detailReport.createdAt }}
            </span>
          </div>
        </div>
      </div>

      <div class="rd-body">
        <div class="rd-section">
          <div class="rd-section-title">举报原因</div>
          <div class="rd-content">{{ detailReport.reason }}</div>
        </div>

        <div class="rd-section">
          <div class="rd-section-title">数据概览</div>
          <div class="rd-grid">
            <div class="rd-cell">
              <span class="rd-cell-label">举报ID</span>
              <span class="rd-cell-value">{{ detailReport.id }}</span>
            </div>
            <div class="rd-cell">
              <span class="rd-cell-label">举报人ID</span>
              <span class="rd-cell-value">{{ detailReport.reporterId }}</span>
            </div>
            <div class="rd-cell">
              <span class="rd-cell-label">目标类型</span>
              <span class="rd-cell-value">{{ { post: '动态', comment: '评论', user: '用户' }[detailReport.targetType] || detailReport.targetType }}</span>
            </div>
            <div class="rd-cell">
              <span class="rd-cell-label">目标ID</span>
              <span class="rd-cell-value">{{ detailReport.targetId }}</span>
            </div>
            <div class="rd-cell">
              <span class="rd-cell-label">处理结果</span>
              <span class="rd-cell-value">{{ detailReport.result || '-' }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer v-if="detailReport && detailReport.status === 0">
        <el-button type="success" :icon="CircleCheck" @click="openHandle(detailReport, 1)">处理</el-button>
        <el-button type="info" :icon="CircleClose" @click="openHandle(detailReport, 2)">驳回</el-button>
      </template>
    </el-dialog>

    <!-- 处理举报弹窗 -->
    <el-dialog v-model="showHandle" title="处理举报" width="500px" destroy-on-close>
      <div class="audit-info">
        <div class="audit-info-label">处理结果</div>
        <el-tag :type="handleStatus === 1 ? 'success' : 'info'" size="large" effect="light">{{ handleStatus === 1 ? '处理' : '驳回' }}</el-tag>
      </div>
      <el-form label-width="100px" class="audit-form">
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReportList, handleReport as handleReportApi, deletePost, updateUserStatus, deleteComment, exportReports } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { Search, Download, View, Clock, CircleCheck, CircleClose, Warning } from '@element-plus/icons-vue'

const adminStore = useAdminStore()
const canExport = computed(() => adminStore.hasButton('export'))

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
const showDetail = ref(false)
const detailReport = ref(null)

const typeTagMap = { post: 'primary', comment: 'warning', user: 'success' }

// 顶部概览数据
const overviewCards = computed(() => [
  { key: 'total', label: '总举报数', value: total.value, icon: 'Warning', color: '#409eff', bg: '#ecf5ff' },
  { key: 'pending', label: '待处理', value: tableData.value.filter(r => r.status === 0).length, icon: 'Clock', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'handled', label: '已处理', value: tableData.value.filter(r => r.status === 1).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'rejected', label: '已驳回', value: tableData.value.filter(r => r.status === 2).length, icon: 'CircleClose', color: '#909399', bg: '#f4f4f5' }
])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReportList({ page: page.value, size: size.value, status: statusFilter.value, targetType: typeFilter.value || undefined })
    if (res.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) {} finally { loading.value = false }
}

const viewDetail = (row) => {
  detailReport.value = { ...row }
  showDetail.value = true
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
    showDetail.value = false
    loadData()
  } catch (e) {}
}

const handleExport = async () => {
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
  try {
    const res = await exportReports({ status: statusFilter.value ?? undefined })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `举报数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {}
}

onMounted(loadData)
</script>

<style scoped>
.reports-page { padding: 0; }

/* 顶部统计卡片 */
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

/* 表格卡片 */
.table-card { min-height: 500px; }
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

/* 用户单元格 */
.user-cell { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; flex-direction: column; gap: 4px; }
.user-name { font-size: 14px; font-weight: 500; color: #303133; }
.user-meta { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #909399; }

/* 时间单元格 */
.time-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

/* 操作按钮 */
.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}

/* 分页 */
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* ========== 详情弹窗 ========== */
.report-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.report-detail-dialog :deep(.el-dialog__header) { padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }

.rd-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.rd-avatar :deep(.el-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.rd-header-info { flex: 1; }
.rd-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.rd-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.rd-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.rd-body { padding: 20px 24px; }

.rd-section { margin-bottom: 24px; }
.rd-section:last-child { margin-bottom: 0; }

.rd-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.rd-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.rd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.rd-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.rd-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.rd-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 处理弹窗 */
.audit-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 4px 16px;
}
.audit-info-label {
  font-size: 14px;
  color: #606266;
}
.audit-form :deep(.el-form-item) { margin-bottom: 0; }
</style>
