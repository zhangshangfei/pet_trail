<template>
  <div class="logs-page">
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

    <!-- 日志列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">操作日志</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-input v-model="adminName" placeholder="操作人" clearable style="width: 150px" @clear="loadData" @keyup.enter="loadData">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
            <el-select v-model="module" placeholder="模块" clearable style="width: 130px" @change="loadData">
              <el-option label="用户" value="user" /><el-option label="动态" value="post" />
              <el-option label="评论" value="comment" /><el-option label="举报" value="report" />
              <el-option label="通知" value="notification" /><el-option label="管理员" value="admin" />
              <el-option label="设置" value="setting" /><el-option label="反馈" value="feedback" />
              <el-option label="宠物" value="pet" />
            </el-select>
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 260px" @change="loadData" />
            <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
            <el-button type="success" :icon="Download" @click="handleExport" v-if="canExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column label="模块" width="90" align="center">
          <template #default="{ row }">
            <el-tag effect="light" size="small">{{ row.module || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="动作" width="90" />
        <el-table-column label="目标" width="150">
          <template #default="{ row }">{{ row.targetType || '-' }} #{{ row.targetId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" show-overflow-tooltip min-width="200" />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="12"><Clock /></el-icon>
              <span>{{ row.createdAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="View" @click="viewDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-if="total > 0" v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="showDetail" title="日志详情" width="640px" destroy-on-close class="log-detail-dialog">
      <div v-if="detail" class="ld-header">
        <div class="ld-icon" :style="{ background: moduleColorMap[detail.module] || '#ecf5ff', color: moduleColorMap[detail.module] ? '#fff' : '#409eff' }">
          <el-icon :size="24"><component :is="moduleIconMap[detail.module] || 'Document'" /></el-icon>
        </div>
        <div class="ld-header-info">
          <div class="ld-name">{{ detail.adminName || '未知操作人' }}</div>
          <div class="ld-tags">
            <el-tag effect="light" size="small" round>{{ detail.module || '-' }}</el-tag>
            <el-tag :type="actionTagMap[detail.action] || ''" effect="light" size="small" round>{{ detail.action || '-' }}</el-tag>
            <span class="ld-tag-item">
              <el-icon :size="12"><Clock /></el-icon>
              {{ detail.createdAt }}
            </span>
          </div>
        </div>
      </div>

      <div class="ld-body">
        <div class="ld-section">
          <div class="ld-section-title">操作详情</div>
          <div class="ld-content">{{ detail.detail || '-' }}</div>
        </div>

        <div class="ld-section">
          <div class="ld-section-title">数据概览</div>
          <div class="ld-grid">
            <div class="ld-cell">
              <span class="ld-cell-label">日志ID</span>
              <span class="ld-cell-value">{{ detail.id }}</span>
            </div>
            <div class="ld-cell">
              <span class="ld-cell-label">操作人</span>
              <span class="ld-cell-value">{{ detail.adminName || '-' }}</span>
            </div>
            <div class="ld-cell">
              <span class="ld-cell-label">模块</span>
              <span class="ld-cell-value">{{ detail.module || '-' }}</span>
            </div>
            <div class="ld-cell">
              <span class="ld-cell-label">动作</span>
              <span class="ld-cell-value">{{ detail.action || '-' }}</span>
            </div>
            <div class="ld-cell">
              <span class="ld-cell-label">目标类型</span>
              <span class="ld-cell-value">{{ detail.targetType || '-' }}</span>
            </div>
            <div class="ld-cell">
              <span class="ld-cell-label">目标ID</span>
              <span class="ld-cell-value">{{ detail.targetId || '-' }}</span>
            </div>
            <div class="ld-cell">
              <span class="ld-cell-label">IP地址</span>
              <span class="ld-cell-value">{{ detail.ip || '-' }}</span>
            </div>
            <div class="ld-cell">
              <span class="ld-cell-label">操作时间</span>
              <span class="ld-cell-value">{{ detail.createdAt }}</span>
            </div>
          </div>
        </div>

        <div class="ld-section" v-if="detail.userAgent">
          <div class="ld-section-title">User-Agent</div>
          <div class="ld-content" style="font-size: 12px; color: #909399;">{{ detail.userAgent }}</div>
        </div>

        <div class="ld-section" v-if="detail.requestData">
          <div class="ld-section-title">请求参数</div>
          <div class="ld-content" style="font-size: 12px; max-height: 200px; overflow-y: auto; white-space: pre-wrap;">{{ detail.requestData }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, View, Clock, User, Document } from '@element-plus/icons-vue'
import { getOperationLogs, exportLogs } from '../api/admin'
import { useAdminStore } from '@/store/admin'

const adminStore = useAdminStore()
const canExport = computed(() => adminStore.hasButton('export'))

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

const moduleIconMap = { user: 'User', post: 'ChatDotRound', comment: 'ChatLineRound', report: 'Warning', notification: 'Bell', admin: 'UserFilled', setting: 'Setting', feedback: 'Message', pet: 'PawPrint' }
const moduleColorMap = { user: '#409eff', post: '#67c23a', comment: '#e6a23c', report: '#f56c6c', notification: '#909399', admin: '#409eff', setting: '#909399', feedback: '#e6a23c', pet: '#67c23a' }
const actionTagMap = { create: 'success', update: 'primary', delete: 'danger', login: '', logout: 'info', export: 'warning' }

const overviewCards = computed(() => [
  { key: 'total', label: '总日志数', value: total.value, icon: 'Document', color: '#409eff', bg: '#ecf5ff' },
  { key: 'today', label: '今日操作', value: list.value.filter(l => l.createdAt && l.createdAt.startsWith(new Date().toISOString().slice(0, 10))).length, icon: 'Calendar', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'modules', label: '涉及模块', value: new Set(list.value.map(l => l.module).filter(Boolean)).size, icon: 'Grid', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'admins', label: '操作人员', value: new Set(list.value.map(l => l.adminName).filter(Boolean)).size, icon: 'User', color: '#f56c6c', bg: '#fef0f0' }
])

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

const viewDetail = (row) => {
  detail.value = { ...row }
  showDetail.value = true
}

const handleExport = async () => {
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
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
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.logs-page { padding: 0; }

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

/* 详情弹窗 */
.log-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.log-detail-dialog :deep(.el-dialog__header) { padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }

.ld-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.ld-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.ld-header-info { flex: 1; }
.ld-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.ld-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.ld-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.ld-body { padding: 20px 24px; }

.ld-section { margin-bottom: 24px; }
.ld-section:last-child { margin-bottom: 0; }

.ld-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.ld-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.ld-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.ld-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.ld-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.ld-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
