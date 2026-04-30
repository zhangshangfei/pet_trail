<template>
  <div class="feedbacks-page">
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

    <!-- 反馈列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">反馈列表</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px" @change="loadData">
              <el-option label="待处理" :value="0" />
              <el-option label="处理中" :value="1" />
              <el-option label="已发布" :value="2" />
            </el-select>
            <el-select v-model="typeFilter" placeholder="类型筛选" clearable style="width: 120px" @change="loadData">
              <el-option label="Bug反馈" value="bug" />
              <el-option label="功能建议" value="feature" />
              <el-option label="体验优化" value="experience" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="feedbackList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户信息" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.userAvatar" icon="UserFilled" />
              <div class="user-info">
                <div class="user-name">{{ row.userNickname || '未知用户' }}</div>
                <div class="user-meta">
                  <span class="user-id">ID: {{ row.userId }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type] || ''" effect="light" size="small">
              {{ typeLabelMap[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status] || ''" effect="light" size="small">
              {{ statusLabelMap[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply" class="reply-text">{{ row.reply }}</span>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
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
              <el-button size="small" text type="primary" :icon="View" @click="showDetail(row)">详情</el-button>
              <el-button size="small" text type="warning" :icon="ChatDotRound" @click="openReplyDialog(row)">回复</el-button>
              <el-button size="small" text type="success" :icon="CircleCheck" @click="openStatusDialog(row, 2)" :disabled="row.status === 2">已发布</el-button>
              <el-button size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 反馈详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="反馈详情" width="640px" destroy-on-close class="feedback-detail-dialog">
      <div v-if="detail.id" class="fd-header">
        <div class="fd-avatar">
          <el-avatar :size="56" :src="detail.userAvatar" icon="UserFilled" />
        </div>
        <div class="fd-header-info">
          <div class="fd-name">{{ detail.userNickname || '未知用户' }}</div>
          <div class="fd-tags">
            <el-tag :type="typeTagMap[detail.type] || ''" size="small" effect="light" round>
              {{ typeLabelMap[detail.type] || detail.type }}
            </el-tag>
            <el-tag :type="statusTagMap[detail.status] || ''" size="small" effect="light" round>
              {{ statusLabelMap[detail.status] || '未知' }}
            </el-tag>
            <span class="fd-tag-item">
              <el-icon :size="12"><Clock /></el-icon>
              {{ detail.createdAt }}
            </span>
          </div>
        </div>
      </div>

      <div class="fd-body">
        <div class="fd-section">
          <div class="fd-section-title">反馈内容</div>
          <div class="fd-content">{{ detail.content }}</div>
          <div class="fd-images" v-if="detail.images">
            <el-image
              v-for="(img, idx) in parseImages(detail.images)"
              :key="idx"
              :src="img"
              :preview-src-list="parseImages(detail.images)"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 8px;"
              preview-teleported
            />
          </div>
        </div>

        <div class="fd-section" v-if="detail.reply">
          <div class="fd-section-title">回复内容</div>
          <div class="fd-reply">{{ detail.reply }}</div>
        </div>

        <div class="fd-section">
          <div class="fd-section-title">数据概览</div>
          <div class="fd-grid">
            <div class="fd-cell">
              <span class="fd-cell-label">反馈ID</span>
              <span class="fd-cell-value">{{ detail.id }}</span>
            </div>
            <div class="fd-cell">
              <span class="fd-cell-label">用户ID</span>
              <span class="fd-cell-value">{{ detail.userId }}</span>
            </div>
            <div class="fd-cell">
              <span class="fd-cell-label">联系方式</span>
              <span class="fd-cell-value">{{ detail.contact || '-' }}</span>
            </div>
            <div class="fd-cell">
              <span class="fd-cell-label">提交时间</span>
              <span class="fd-cell-value">{{ detail.createdAt }}</span>
            </div>
            <div class="fd-cell">
              <span class="fd-cell-label">更新时间</span>
              <span class="fd-cell-value">{{ detail.updatedAt || '-' }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="warning" :icon="ChatDotRound" @click="openReplyDialog(detail); detailDialogVisible = false;">回复</el-button>
        <el-button type="success" :icon="CircleCheck" @click="openStatusDialog(detail, 2); detailDialogVisible = false;" :disabled="detail.status === 2">标记已发布</el-button>
      </template>
    </el-dialog>

    <!-- 状态更新弹窗 -->
    <el-dialog v-model="statusDialogVisible" :title="statusDialogTitle" width="500px" destroy-on-close>
      <div class="audit-info">
        <div class="audit-info-label">目标状态</div>
        <el-tag :type="statusForm.targetStatus === 2 ? 'success' : 'primary'" size="large" effect="light">
          {{ statusLabelMap[statusForm.targetStatus] || '未知' }}
        </el-tag>
      </div>
      <el-form :model="statusForm" label-width="80px" class="audit-form">
        <el-form-item label="反馈内容">
          <div class="preview-content">{{ statusForm.content }}</div>
        </el-form-item>
        <el-form-item label="备注内容">
          <el-input v-model="statusForm.reply" type="textarea" :rows="4" placeholder="请输入备注内容（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复反馈" width="500px" destroy-on-close>
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="反馈内容">
          <div class="preview-content">{{ replyForm.content }}</div>
        </el-form-item>
        <el-form-item label="回复内容" required>
          <el-input v-model="replyForm.reply" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="submitting">回复并标记处理中</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getFeedbackList, updateFeedbackStatus, deleteFeedback, getFeedbackDetail, replyFeedback } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, Clock, ChatDotRound, CircleCheck, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const feedbackList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const typeFilter = ref('')

const statusDialogVisible = ref(false)
const submitting = ref(false)
const statusForm = ref({ id: null, content: '', reply: '', targetStatus: null })

const detailDialogVisible = ref(false)
const detail = ref({})

const typeLabelMap = { bug: 'Bug反馈', feature: '功能建议', experience: '体验优化', other: '其他' }
const typeTagMap = { bug: 'danger', feature: '', experience: 'warning', other: 'info' }
const statusLabelMap = { 0: '待处理', 1: '处理中', 2: '已发布' }
const statusTagMap = { 0: 'warning', 1: 'primary', 2: 'success' }

const statusDialogTitle = ref('')

const replyDialogVisible = ref(false)
const replyForm = ref({ id: null, content: '', reply: '' })

// 顶部概览数据
const overviewCards = computed(() => [
  { key: 'total', label: '总反馈数', value: total.value, icon: 'MessageBox', color: '#409eff', bg: '#ecf5ff' },
  { key: 'pending', label: '待处理', value: feedbackList.value.filter(f => f.status === 0).length, icon: 'Clock', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'processing', label: '处理中', value: feedbackList.value.filter(f => f.status === 1).length, icon: 'Loading', color: '#409eff', bg: '#ecf5ff' },
  { key: 'published', label: '已发布', value: feedbackList.value.filter(f => f.status === 2).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' }
])

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (statusFilter.value !== null && statusFilter.value !== '') params.status = statusFilter.value
    if (typeFilter.value) params.type = typeFilter.value
    const res = await getFeedbackList(params)
    if (res.data) {
      feedbackList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载反馈列表失败:', e)
  } finally {
    loading.value = false
  }
}

const openStatusDialog = (row, targetStatus) => {
  statusForm.value = { id: row.id, content: row.content, reply: row.reply || '', targetStatus }
  statusDialogTitle.value = targetStatus === 1 ? '标记为处理中' : '标记为已发布'
  statusDialogVisible.value = true
}

const submitStatus = async () => {
  submitting.value = true
  try {
    await updateFeedbackStatus(statusForm.value.id, {
      status: statusForm.value.targetStatus,
      reply: statusForm.value.reply || null
    })
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('状态更新失败')
  } finally {
    submitting.value = false
  }
}

const openReplyDialog = (row) => {
  replyForm.value = { id: row.id, content: row.content, reply: row.reply || '' }
  replyDialogVisible.value = true
}

const submitReply = async () => {
  if (!replyForm.value.reply || !replyForm.value.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitting.value = true
  try {
    await replyFeedback(replyForm.value.id, { reply: replyForm.value.reply.trim() })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

const showDetail = async (row) => {
  try {
    const res = await getFeedbackDetail(row.id)
    if (res.data) {
      detail.value = res.data
      detailDialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该反馈吗？', '确认删除', { type: 'warning' })
    await deleteFeedback(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const parseImages = (images) => {
  if (!images) return []
  try {
    const parsed = JSON.parse(images)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    return typeof images === 'string' && images.startsWith('http') ? [images] : []
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.feedbacks-page { padding: 0; }

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

/* 回复文字 */
.reply-text { color: #047857; }

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

/* 预览内容 */
.preview-content {
  color: #606266;
  line-height: 1.6;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

/* ========== 详情弹窗 ========== */
.feedback-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.feedback-detail-dialog :deep(.el-dialog__header) { padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }

.fd-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
.fd-avatar :deep(.el-avatar) {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.fd-header-info { flex: 1; }
.fd-name { font-size: 18px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.fd-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.fd-tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 10px;
  border-radius: 12px;
}

.fd-body { padding: 20px 24px; }

.fd-section { margin-bottom: 24px; }
.fd-section:last-child { margin-bottom: 0; }

.fd-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.fd-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
  margin-bottom: 12px;
}

.fd-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.fd-reply {
  font-size: 14px;
  color: #047857;
  line-height: 1.8;
  padding: 12px;
  background: #ecfdf5;
  border-radius: 8px;
}

.fd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 16px;
}
.fd-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 8px;
}
.fd-cell-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.fd-cell-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 状态弹窗 */
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
.audit-form :deep(.el-form-item) { margin-bottom: 12px; }
.audit-form :deep(.el-form-item:last-child) { margin-bottom: 0; }
</style>
