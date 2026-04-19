<template>
  <div class="page-container">
    <div class="page-header"><h2>评论管理</h2></div>
    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索评论内容" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
      <el-input v-model="postId" placeholder="动态ID" clearable style="width: 120px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="deleted" placeholder="状态" clearable style="width: 120px" @change="loadData">
        <el-option label="正常" :value="0" />
        <el-option label="已删除" :value="1" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip min-width="200" />
      <el-table-column prop="postId" label="动态ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="parentId" label="回复ID" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'danger' : 'success'" size="small">{{ row.status === 0 ? '已删除' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 0 && isSuperAdmin" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          <el-button v-if="row.status === 0 && isSuperAdmin" size="small" type="success" @click="handleRestore(row)">恢复</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" :current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" style="margin-top: 16px; justify-content: flex-end;" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCommentList, deleteComment, restoreComment } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const postId = ref('')
const deleted = ref(null)

const adminInfo = JSON.parse(localStorage.getItem('admin_info') || '{}')
const isSuperAdmin = computed(() => adminInfo.role === 'SUPER_ADMIN')

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (postId.value) params.postId = postId.value
    if (deleted.value !== null) params.deleted = deleted.value
    const res = await getCommentList(params)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {}
  loading.value = false
}

const handlePageChange = (p) => { page.value = p; loadData() }

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该评论？', '确认')
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

const handleRestore = async (row) => {
  try {
    await restoreComment(row.id)
    ElMessage.success('恢复成功')
    loadData()
  } catch (e) {}
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
