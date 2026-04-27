<template>
  <div class="users-page">
    <el-card shadow="hover">
      <template #header>
        <div class="page-header">
          <span class="card-title">用户管理</span>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="搜索昵称/OpenID" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="正常" :value="1" />
              <el-option label="已禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.avatar" icon="UserFilled" />
              <div class="user-info">
                <span class="user-name">{{ row.nickname || '未设置' }}</span>
                <span class="user-id">ID: {{ row.id }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="openid" label="OpenID" min-width="180" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1 && canManage" type="danger" size="small" text @click="handleStatus(row, 0)">禁用</el-button>
            <el-button v-if="row.status !== 1 && canManage" type="success" size="small" text @click="handleStatus(row, 1)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="showDetail" title="用户详情" width="600px">
      <el-descriptions :column="2" border v-if="detailUser">
        <el-descriptions-item label="ID">{{ detailUser.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailUser.nickname || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detailUser.gender === 1 ? '男' : detailUser.gender === 2 ? '女' : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailUser.status === 1 ? 'success' : 'danger'" size="small">{{ detailUser.status === 1 ? '正常' : '已禁用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="OpenID" :span="2">{{ detailUser.openid }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailUser.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ detailUser.lastLoginAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>用户统计</el-divider>
      <el-row :gutter="16" v-if="userStats">
        <el-col :span="8" v-for="item in userStatCards" :key="item.key">
          <div class="stat-mini">
            <div class="stat-mini-value">{{ item.value }}</div>
            <div class="stat-mini-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, getUserStats, updateUserStatus, exportUsers } from '../api/admin'
import { useAdminStore } from '@/store/admin'

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref(null)
const showDetail = ref(false)
const detailUser = ref(null)
const userStats = ref(null)

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasPermission('user:manage'))

const userStatCards = computed(() => [
  { key: 'pets', label: '宠物数', value: userStats.value?.petCount || 0 },
  { key: 'posts', label: '动态数', value: userStats.value?.postCount || 0 },
  { key: 'likes', label: '获赞数', value: userStats.value?.likeCount || 0 },
  { key: 'followers', label: '粉丝数', value: userStats.value?.followerCount || 0 },
  { key: 'following', label: '关注数', value: userStats.value?.followingCount || 0 }
])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: page.value, size: size.value, keyword: keyword.value || undefined, status: statusFilter.value })
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  detailUser.value = row
  userStats.value = null
  showDetail.value = true
  try {
    const res = await getUserStats(row.id)
    if (res.data) userStats.value = res.data
  } catch (e) {}
}

const handleStatus = async (row, status) => {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定要${action}用户 "${row.nickname || row.id}" 吗？`, '确认', { type: 'warning' })
  try {
    await updateUserStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {}
}

const handleExport = async () => {
  try {
    const res = await exportUsers({ keyword: keyword.value || undefined, status: statusFilter.value ?? undefined })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `用户数据_${new Date().toISOString().slice(0, 10)}.xlsx`
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
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: 14px; font-weight: 500; }
.user-id { font-size: 12px; color: #999; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.stat-mini { text-align: center; padding: 12px 0; background: #f8f9fa; border-radius: 8px; }
.stat-mini-value { font-size: 24px; font-weight: 700; color: #333; }
.stat-mini-label { font-size: 12px; color: #999; margin-top: 4px; }
</style>
