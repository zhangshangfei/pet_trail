<template>
  <div class="posts-page">
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

    <!-- 动态列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">动态列表</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-input v-model="userIdFilter" placeholder="用户ID" clearable style="width: 120px" @clear="loadData" @keyup.enter="loadData" />
            <el-select v-model="auditFilter" placeholder="全部状态" clearable style="width: 110px" @change="loadData">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Download" @click="handleExport" v-if="canExport">导出</el-button>
            <el-button type="warning" :icon="Delete" @click="loadDeleted" v-if="canManage">回收站</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户信息" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-info">
                <div class="user-name">{{ row.userNickname || '未知用户' }}</div>
                <div class="user-meta">
                  <span class="user-id">ID: {{ row.userId }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <el-image v-if="row.images" :src="parseFirstImage(row.images)" style="width: 50px; height: 50px; border-radius: 4px;" fit="cover" :preview-src-list="[parseFirstImage(row.images)]" preview-teleported />
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="互动" width="100" align="center">
          <template #default="{ row }">
            <div class="interact-cell">
              <span><el-icon :size="12"><Star /></el-icon> {{ row.likeCount || 0 }}</span>
              <span><el-icon :size="12"><ChatDotRound /></el-icon> {{ row.commentCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="审核" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'" effect="light" size="small">
              {{ row.auditStatus === 1 ? '通过' : row.auditStatus === 2 ? '拒绝' : '待审' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
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
              <el-button v-if="row.auditStatus !== 1" size="small" text type="success" :icon="CircleCheck" @click="openAudit(row, 1)">通过</el-button>
              <el-button v-if="row.auditStatus !== 2" size="small" text type="danger" :icon="CircleClose" @click="openAudit(row, 2)">拒绝</el-button>
              <el-button v-if="canManage" size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span>已选 {{ selectedRows.length }} 项</span>
        <el-button type="success" size="small" @click="batchAudit(1)">批量通过</el-button>
        <el-button type="danger" size="small" @click="batchAudit(2)">批量拒绝</el-button>
      </div>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 动态详情弹窗 -->
    <el-dialog v-model="showDetail" title="动态详情" width="640px" destroy-on-close class="post-detail-dialog">
      <div v-if="detailPost" class="pd-header">
        <div class="pd-avatar">
          <el-avatar :size="56" :src="detailPost.userAvatar" icon="UserFilled" />
        </div>
        <div class="pd-header-info">
          <div class="pd-name">{{ detailPost.userNickname || '未知用户' }}</div>
          <div class="pd-tags">
            <el-tag :type="detailPost.auditStatus === 1 ? 'success' : detailPost.auditStatus === 2 ? 'danger' : 'warning'" size="small" effect="light" round>
              {{ detailPost.auditStatus === 1 ? '已通过' : detailPost.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
            <span class="pd-tag-item">
              <el-icon :size="12"><Clock /></el-icon>
              {{ detailPost.createdAt }}
            </span>
          </div>
        </div>
      </div>

      <div class="pd-body">
        <div class="pd-section">
          <div class="pd-section-title">动态内容</div>
          <div class="pd-content">{{ detailPost.content }}</div>
          <div class="pd-images" v-if="detailPost.images">
            <el-image v-for="(img, i) in parseImages(detailPost.images)" :key="i" :src="img" style="width: 100px; height: 100px; border-radius: 8px;" fit="cover" :preview-src-list="parseImages(detailPost.images)" preview-teleported />
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">数据概览</div>
          <div class="pd-grid">
            <div class="pd-cell">
              <span class="pd-cell-label">动态ID</span>
              <span class="pd-cell-value">{{ detailPost.id }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">用户ID</span>
              <span class="pd-cell-value">{{ detailPost.userId }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">点赞数</span>
              <span class="pd-cell-value">{{ detailPost.likeCount || 0 }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">评论数</span>
              <span class="pd-cell-value">{{ detailPost.commentCount || 0 }}</span>
            </div>
            <div class="pd-cell">
              <span class="pd-cell-label">审核备注</span>
              <span class="pd-cell-value">{{ detailPost.auditRemark || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="showAudit" title="审核动态" width="480px" destroy-on-close>
      <div class="audit-info">
        <div class="audit-info-label">审核结果</div>
        <el-tag :type="auditStatus === 1 ? 'success' : 'danger'" size="large" effect="light">{{ auditStatus === 1 ? '通过' : '拒绝' }}</el-tag>
      </div>
      <el-form label-width="80px" class="audit-form">
        <el-form-item label="审核备注">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAudit = false">取消</el-button>
        <el-button :type="auditStatus === 1 ? 'success' : 'danger'" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 回收站弹窗 -->
    <el-dialog v-model="showDeleted" title="已删除动态（回收站）" width="900px" destroy-on-close>
      <el-table :data="deletedList" v-loading="deletedLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="userNickname" label="用户名" width="120" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="success" @click="handleRestore(row)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostList, getPostDetail, auditPost, deletePost, batchAuditPosts, getDeletedPosts, restorePost, exportPosts } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { Search, Download, View, Clock, Star, ChatDotRound, CircleCheck, CircleClose, Delete } from '@element-plus/icons-vue'

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const userIdFilter = ref('')
const auditFilter = ref(null)
const selectedRows = ref([])
const showDetail = ref(false)
const detailPost = ref(null)
const showAudit = ref(false)
const auditPostId = ref(null)
const auditStatus = ref(1)
const auditRemark = ref('')
const showDeleted = ref(false)
const deletedList = ref([])
const deletedLoading = ref(false)

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('post:manage'))
const canExport = computed(() => adminStore.hasButton('export'))

// 顶部概览数据
const overviewCards = computed(() => [
  { key: 'total', label: '总动态数', value: total.value, icon: 'Document', color: '#409eff', bg: '#ecf5ff' },
  { key: 'pending', label: '待审核', value: tableData.value.filter(p => p.auditStatus === 0).length, icon: 'Clock', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'approved', label: '已通过', value: tableData.value.filter(p => p.auditStatus === 1).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'rejected', label: '已拒绝', value: tableData.value.filter(p => p.auditStatus === 2).length, icon: 'CircleClose', color: '#f56c6c', bg: '#fef0f0' }
])

const parseFirstImage = (images) => {
  try { const arr = JSON.parse(images); return arr && arr.length > 0 ? arr[0] : '' } catch (e) { return '' }
}
const parseImages = (images) => {
  try { return JSON.parse(images) || [] } catch (e) { return [] }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPostList({ page: page.value, size: size.value, userId: userIdFilter.value || undefined, auditStatus: auditFilter.value })
    if (res.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) {} finally { loading.value = false }
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }

const viewDetail = async (row) => {
  try {
    const res = await getPostDetail(row.id)
    detailPost.value = res.data
    showDetail.value = true
  } catch (e) {}
}

const openAudit = (row, status) => {
  auditPostId.value = row.id
  auditStatus.value = status
  auditRemark.value = ''
  showAudit.value = true
}

const submitAudit = async () => {
  try {
    await auditPost(auditPostId.value, { auditStatus: auditStatus.value, auditRemark: auditRemark.value || undefined })
    ElMessage.success(auditStatus.value === 1 ? '已通过' : '已拒绝')
    showAudit.value = false
    loadData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该动态吗？', '确认删除', { type: 'warning' })
  try { await deletePost(row.id); ElMessage.success('删除成功'); loadData() } catch (e) {}
}

const batchAudit = async (status) => {
  const ids = selectedRows.value.map(r => r.id)
  if (ids.length === 0) return
  const action = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定要批量${action} ${ids.length} 条动态吗？`, '确认')
  try {
    await batchAuditPosts({ postIds: ids, auditStatus: status })
    ElMessage.success(`批量${action}成功`)
    loadData()
  } catch (e) {}
}

const loadDeleted = async () => {
  showDeleted.value = true
  deletedLoading.value = true
  try {
    const res = await getDeletedPosts({ page: 1, size: 50 })
    deletedList.value = res.data?.records || []
  } catch (e) {} finally { deletedLoading.value = false }
}

const handleRestore = async (row) => {
  try {
    await restorePost(row.id)
    ElMessage.success('恢复成功')
    loadDeleted()
  } catch (e) {}
}

const handleExport = async () => {
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
  try {
    const res = await exportPosts({ auditStatus: auditFilter.value ?? undefined })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `动态数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {}
}

onMounted(loadData)
</script>

<style scoped>
.posts-page { padding: 0; }

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

/* 互动数据 */
.interact-cell { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #606266; }
.interact-cell span { display: inline-flex; align-items: center; gap: 3px; }

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

/* 批量操作 */
.batch-bar { display: flex; align-items: center; gap: 12px; padding: 12px 0; color: #666; font-size: 14px; }

/* ========== 详情弹窗 ========== */
.post-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.post-detail-dialog :deep(.el-dialog__header) { padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }

.pd-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.pd-avatar :deep(.el-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.pd-header-info { flex: 1; }
.pd-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.pd-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.pd-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.pd-body { padding: 20px 24px; }

.pd-section { margin-bottom: 24px; }
.pd-section:last-child { margin-bottom: 0; }

.pd-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.pd-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
  margin-bottom: 12px;
}

.pd-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.pd-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.pd-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.pd-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 审核弹窗 */
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
