<template>
  <div class="posts-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">动态管理</span>
          <div class="header-actions">
            <el-input v-model="userIdFilter" placeholder="用户ID" clearable style="width: 140px" @clear="loadData" @keyup.enter="loadData" />
            <el-select v-model="auditFilter" placeholder="审核状态" clearable style="width: 140px" @change="loadData">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.images" :src="parseFirstImage(row.images)" style="width: 50px; height: 50px" fit="cover" :preview-src-list="[parseFirstImage(row.images)]" preview-teleported />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column prop="auditStatus" label="审核" width="100">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'" size="small">
              {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" text @click="handleAudit(row, 1)">通过</el-button>
            <el-button type="danger" size="small" text @click="handleAudit(row, 2)">拒绝</el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
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
import { getPostList, auditPost, deletePost } from '../api/admin'

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const userIdFilter = ref('')
const auditFilter = ref(null)

const parseFirstImage = (images) => {
  try {
    const arr = JSON.parse(images)
    return arr && arr.length > 0 ? arr[0] : ''
  } catch (e) { return '' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPostList({ page: page.value, size: size.value, userId: userIdFilter.value || undefined, auditStatus: auditFilter.value })
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

const handleAudit = async (row, status) => {
  const action = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定要${action}该动态吗？`, '确认', { type: 'warning' })
  try {
    await auditPost(row.id, { auditStatus: status })
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该动态吗？此操作不可恢复', '确认删除', { type: 'warning' })
  try {
    await deletePost(row.id)
    ElMessage.success('删除成功')
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
