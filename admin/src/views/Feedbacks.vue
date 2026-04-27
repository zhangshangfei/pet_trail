<template>
  <div class="page-container">
    <div class="page-header">
      <h2>反馈管理</h2>
      <div class="filter-group">
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
      </div>
    </div>

    <el-table :data="feedbackList" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="userNickname" label="用户名" width="120" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="typeTagMap[row.type]" size="small">{{ typeLabelMap[row.type] || row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagMap[row.status]" size="small">{{ statusLabelMap[row.status] || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reply" label="备注" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.reply">{{ row.reply }}</span>
          <span v-else style="color: #c0c4cc">暂无备注</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openStatusDialog(row, 1)" :disabled="row.status === 1">处理中</el-button>
          <el-button type="success" link size="small" @click="openStatusDialog(row, 2)" :disabled="row.status === 2">已发布</el-button>
          <el-button type="info" link size="small" @click="showDetail(row)">详情</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="statusDialogVisible" :title="statusDialogTitle" width="500px">
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="反馈内容">
          <div style="color: #606266; line-height: 1.6;">{{ statusForm.content }}</div>
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

    <el-dialog v-model="detailDialogVisible" title="反馈详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.userNickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeLabelMap[detail.type] || detail.type }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabelMap[detail.status] || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detail.contact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="反馈内容" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ detail.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="图片" :span="2" v-if="detail.images">
          <div class="detail-images">
            <el-image
              v-for="(img, idx) in parseImages(detail.images)"
              :key="idx"
              :src="img"
              :preview-src-list="parseImages(detail.images)"
              fit="cover"
              style="width: 80px; height: 80px; margin-right: 8px; border-radius: 4px;"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          <div v-if="detail.reply" style="white-space: pre-wrap; line-height: 1.6; color: #047857;">{{ detail.reply }}</div>
          <span v-else style="color: #c0c4cc">暂无备注</span>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updatedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFeedbackList, updateFeedbackStatus, deleteFeedback, getFeedbackDetail } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

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
const statusTagMap = { 0: 'warning', 1: '', 2: 'success' }

const statusDialogTitle = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
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
  statusForm.value = {
    id: row.id,
    content: row.content,
    reply: row.reply || '',
    targetStatus
  }
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
    await ElMessageBox.confirm('确定要删除该反馈吗？', '提示', { type: 'warning' })
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
}
.filter-group {
  display: flex;
  gap: 10px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.detail-images {
  display: flex;
  flex-wrap: wrap;
}
</style>
