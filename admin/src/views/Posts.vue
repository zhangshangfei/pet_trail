<template>
  <div class="posts-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">动态管理</span>
          <div class="header-actions">
            <el-input v-model="userIdFilter" placeholder="用户ID" clearable style="width: 120px" @clear="loadData" @keyup.enter="loadData" />
            <el-select v-model="auditFilter" placeholder="审核状态" clearable style="width: 130px" @change="loadData">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
            <el-button type="warning" @click="loadDeleted" v-if="isSuperAdmin">回收站</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="userNickname" label="用户名" width="120" />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.images" :src="parseFirstImage(row.images)" style="width: 50px; height: 50px" fit="cover" :preview-src-list="[parseFirstImage(row.images)]" preview-teleported />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="赞" width="60" />
        <el-table-column prop="commentCount" label="评" width="60" />
        <el-table-column prop="auditStatus" label="审核" width="90">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'" size="small">
              {{ row.auditStatus === 1 ? '通过' : row.auditStatus === 2 ? '拒绝' : '待审' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="viewDetail(row)">详情</el-button>
            <el-button type="success" size="small" text @click="openAudit(row, 1)">通过</el-button>
            <el-button type="danger" size="small" text @click="openAudit(row, 2)">拒绝</el-button>
            <el-button v-if="isSuperAdmin" type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="showDetail" title="动态详情" width="600px">
      <el-descriptions :column="2" border v-if="detailPost">
        <el-descriptions-item label="ID">{{ detailPost.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailPost.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailPost.userNickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ detailPost.content }}</el-descriptions-item>
        <el-descriptions-item label="图片" :span="2" v-if="detailPost.images">
          <div class="detail-images">
            <el-image v-for="(img, i) in parseImages(detailPost.images)" :key="i" :src="img" style="width: 80px; height: 80px; margin-right: 8px" fit="cover" :preview-src-list="parseImages(detailPost.images)" preview-teleported />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="点赞">{{ detailPost.likeCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="评论">{{ detailPost.commentCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="detailPost.auditStatus === 1 ? 'success' : detailPost.auditStatus === 2 ? 'danger' : 'warning'" size="small">
            {{ detailPost.auditStatus === 1 ? '已通过' : detailPost.auditStatus === 2 ? '已拒绝' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核备注">{{ detailPost.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">{{ detailPost.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="showAudit" title="审核动态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-tag :type="auditStatus === 1 ? 'success' : 'danger'">{{ auditStatus === 1 ? '通过' : '拒绝' }}</el-tag>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAudit = false">取消</el-button>
        <el-button :type="auditStatus === 1 ? 'success' : 'danger'" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDeleted" title="已删除动态（回收站）" width="900px">
      <el-table :data="deletedList" v-loading="deletedLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="userNickname" label="用户名" width="120" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="success" size="small" text @click="handleRestore(row)">恢复</el-button>
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

const adminInfo = JSON.parse(localStorage.getItem('admin_info') || '{}')
const isSuperAdmin = computed(() => adminInfo.role === 'SUPER_ADMIN')

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
  try {
    const res = await exportPosts({ auditStatus: auditFilter.value ?? undefined })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `动态数据_${new Date().toISOString().slice(0, 10)}.xlsx`
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
.batch-bar { display: flex; align-items: center; gap: 12px; padding: 12px 0; color: #666; font-size: 14px; }
.detail-images { display: flex; flex-wrap: wrap; }
</style>
