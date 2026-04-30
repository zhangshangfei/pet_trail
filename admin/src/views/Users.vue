<template>
  <div class="users-page">
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

    <!-- 用户列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">用户列表</span>
            <el-tag size="small" type="info">共 {{ total }} 人</el-tag>
          </div>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="搜索昵称 / OpenID" clearable style="width: 220px" @clear="loadData" @keyup.enter="loadData">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 110px" @change="loadData">
              <el-option label="正常" :value="1" />
              <el-option label="已禁用" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Download" @click="handleExport" v-if="canExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="用户信息" min-width="220">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="40" :src="row.avatar" icon="UserFilled" />
              <div class="user-info">
                <div class="user-name">{{ row.nickname || '未设置昵称' }}</div>
                <div class="user-meta">
                  <span class="user-id">ID: {{ row.id }}</span>
                  <el-divider direction="vertical" />
                  <span class="user-gender">{{ row.gender === 1 ? '♂ 男' : row.gender === 2 ? '♀ 女' : '未知' }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="OpenID" min-width="180">
          <template #default="{ row }">
            <span class="openid-text">{{ row.openid }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" size="small">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
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
              <el-button v-if="canManage" size="small" text :type="row.status === 1 ? 'danger' : 'success'" :icon="row.status === 1 ? CircleClose : CircleCheck" @click="handleStatus(row, row.status === 1 ? 0 : 1)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="showDetail" title="用户详情" width="640px" destroy-on-close class="user-detail-dialog">
      <div v-if="detailUser" class="detail-profile">
        <div class="detail-avatar-wrap">
          <el-avatar :size="72" :src="detailUser.avatar" icon="UserFilled" />
        </div>
        <div class="detail-profile-info">
          <div class="detail-name">{{ detailUser.nickname || '未设置昵称' }}</div>
          <div class="detail-profile-meta">
            <el-tag :type="detailUser.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>
              {{ detailUser.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
            <span class="meta-item">
              <el-icon :size="12"><component :is="detailUser.gender === 2 ? 'Female' : 'Male'" /></el-icon>
              {{ detailUser.gender === 1 ? '男' : detailUser.gender === 2 ? '女' : '未知' }}
            </span>
            <span class="meta-item">
              <el-icon :size="12"><Clock /></el-icon>
              {{ detailUser.createdAt?.split(' ')[0] }} 注册
            </span>
          </div>
        </div>
      </div>

      <el-divider class="detail-divider" />

      <div class="detail-section">
        <div class="detail-section-title">
          <span class="section-icon" style="background: #ecf5ff; color: #409eff;"><el-icon><Document /></el-icon></span>
          基本信息
        </div>
        <div class="detail-info-grid">
          <div class="info-item">
            <span class="info-label">用户ID</span>
            <span class="info-value">{{ detailUser?.id }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">OpenID</span>
            <span class="info-value mono">{{ detailUser?.openid }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ detailUser?.createdAt }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">最后登录</span>
            <span class="info-value">{{ detailUser?.lastLoginAt || '从未登录' }}</span>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="detail-section-title">
          <span class="section-icon" style="background: #f0f9eb; color: #67c23a;"><el-icon><TrendCharts /></el-icon></span>
          用户统计
        </div>
        <div v-if="userStats" class="detail-stats-grid">
          <div class="stat-box" v-for="item in userStatCards" :key="item.key">
            <div class="stat-box-icon" :style="{ background: statIconMap[item.key]?.bg, color: statIconMap[item.key]?.color }">
              <el-icon :size="18"><component :is="statIconMap[item.key]?.icon" /></el-icon>
            </div>
            <div class="stat-box-info">
              <div class="stat-box-value">{{ item.value }}</div>
              <div class="stat-box-label">{{ item.label }}</div>
            </div>
          </div>
        </div>
        <el-skeleton :rows="2" animated v-else />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, View, Clock, CircleClose, CircleCheck, User, UserFilled, Document, Star, Connection, Male, Female, TrendCharts } from '@element-plus/icons-vue'
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
const canManage = computed(() => adminStore.hasButton('user:manage'))
const canExport = computed(() => adminStore.hasButton('export'))

// 顶部概览数据（从表格数据汇总）
const overviewCards = computed(() => [
  { key: 'total', label: '总用户数', value: total.value, icon: 'User', color: '#409eff', bg: '#ecf5ff' },
  { key: 'normal', label: '正常用户', value: tableData.value.filter(u => u.status === 1).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'disabled', label: '已禁用', value: tableData.value.filter(u => u.status !== 1).length, icon: 'CircleClose', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'male', label: '男性用户', value: tableData.value.filter(u => u.gender === 1).length, icon: 'UserFilled', color: '#e6a23c', bg: '#fdf6ec' }
])

const userStatCards = computed(() => [
  { key: 'pets', label: '宠物数', value: userStats.value?.petCount || 0 },
  { key: 'posts', label: '动态数', value: userStats.value?.postCount || 0 },
  { key: 'likes', label: '获赞数', value: userStats.value?.likeCount || 0 },
  { key: 'followers', label: '粉丝数', value: userStats.value?.followerCount || 0 },
  { key: 'following', label: '关注数', value: userStats.value?.followingCount || 0 }
])

const statIconMap = {
  pets: { icon: 'UserFilled', color: '#e6a23c', bg: '#fdf6ec' },
  posts: { icon: 'Document', color: '#409eff', bg: '#ecf5ff' },
  likes: { icon: 'Star', color: '#f56c6c', bg: '#fef0f0' },
  followers: { icon: 'Connection', color: '#67c23a', bg: '#f0f9eb' },
  following: { icon: 'User', color: '#909399', bg: '#f4f4f5' }
}

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
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
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
  } catch (e) {}
}

onMounted(loadData)
</script>

<style scoped>
.users-page { padding: 0; }

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
.user-meta :deep(.el-divider--vertical) { margin: 0 4px; }
.openid-text { font-family: monospace; font-size: 12px; color: #606266; }

/* 时间单元格 */
.time-cell { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }

/* 分页 */
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* 详情弹窗 */
.user-detail-dialog :deep(.el-dialog__body) { padding-top: 8px; }

.detail-profile {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 4px 16px;
}
.detail-avatar-wrap {
  flex-shrink: 0;
}
.detail-avatar-wrap :deep(.el-avatar) {
  border: 3px solid #e4e7ed;
}
.detail-profile-info { flex: 1; min-width: 0; }
.detail-name { font-size: 20px; font-weight: 600; color: #303133; margin-bottom: 10px; }
.detail-profile-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.detail-divider { margin: 4px 0 20px; }

.detail-section { margin-bottom: 24px; }
.detail-section:last-child { margin-bottom: 0; }

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}
.section-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-label {
  font-size: 12px;
  color: #909399;
}
.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  word-break: break-all;
}
.info-value.mono {
  font-family: 'SF Mono', Monaco, monospace;
  font-size: 12px;
  color: #606266;
}

.detail-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.stat-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fafbfc;
  border-radius: 10px;
  transition: background 0.2s, transform 0.2s;
}
.stat-box:hover {
  background: #f2f3f5;
  transform: translateY(-1px);
}
.stat-box-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-box-info { flex: 1; min-width: 0; }
.stat-box-value { font-size: 20px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-box-label { font-size: 12px; color: #909399; margin-top: 2px; }

.action-btns {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}
</style>
