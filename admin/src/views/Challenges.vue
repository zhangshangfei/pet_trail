<template>
  <div class="challenges-page">
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

    <!-- 挑战赛列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="page-header">
          <div class="header-left">
            <span class="card-title">挑战赛列表</span>
            <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
              <el-option label="进行中" :value="1" />
              <el-option label="已下线" :value="0" />
              <el-option label="已结束" :value="2" />
            </el-select>
            <el-select v-model="typeFilter" placeholder="类型" clearable style="width: 120px" @change="loadData">
              <el-option label="打卡" :value="1" />
              <el-option label="社交" :value="2" />
              <el-option label="健康" :value="3" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button type="success" :icon="Plus" @click="openCreate">新增</el-button>
            <el-button :icon="Download" @click="handleExport" v-if="canExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'warning' : row.type === 2 ? 'primary' : 'success'" effect="light" size="small">
              {{ { 1: '打卡', 2: '社交', 3: '健康' }[row.type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conditionType" label="条件类型" width="130" />
        <el-table-column prop="conditionValue" label="目标值" width="90" align="center" />
        <el-table-column prop="participantCount" label="参与人数" width="100" align="center" />
        <el-table-column prop="rewardDescription" label="奖励" width="140" show-overflow-tooltip />
        <el-table-column label="时间范围" width="180">
          <template #default="{ row }">
            <div class="time-range">
              <el-icon :size="12"><Calendar /></el-icon>
              <span>{{ row.startDate?.substring(0, 10) }} ~ {{ row.endDate?.substring(0, 10) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'danger'" effect="light" size="small">
              {{ { 0: '已下线', 1: '进行中', 2: '已结束' }[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" text type="primary" :icon="TrendCharts" @click="viewStats(row)">统计</el-button>
              <el-button size="small" text type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1" size="small" text type="warning" :icon="CircleClose" @click="changeStatus(row.id, 0)">下线</el-button>
              <el-button v-if="row.status === 0" size="small" text type="success" :icon="CircleCheck" @click="changeStatus(row.id, 1)">上线</el-button>
              <el-button v-if="canManage" size="small" text type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑挑战赛' : '新增挑战赛'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required><el-input v-model="form.title" placeholder="请输入挑战赛标题" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入挑战赛描述" /></el-form-item>
        <el-form-item label="封面图"><el-input v-model="form.coverImage" placeholder="图片URL" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="打卡" :value="1" /><el-option label="社交" :value="2" /><el-option label="健康" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件类型" required>
          <el-select v-model="form.conditionType" placeholder="请选择条件类型">
            <el-option label="累计打卡" value="checkin_count" /><el-option label="连续打卡" value="checkin_streak" />
            <el-option label="健康记录" value="health_record_count" /><el-option label="发布动态" value="post_count" />
            <el-option label="获得点赞" value="like_received" /><el-option label="粉丝数" value="follower_count" />
            <el-option label="宠物数" value="pet_count" /><el-option label="评论数" value="comment_count" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值" required><el-input-number v-model="form.conditionValue" :min="1" style="width: 180px" /></el-form-item>
        <el-form-item label="奖励描述"><el-input v-model="form.rewardDescription" placeholder="请输入奖励描述" /></el-form-item>
        <el-form-item label="开始时间" required><el-date-picker v-model="form.startDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" style="width: 100%" /></el-form-item>
        <el-form-item label="结束时间" required><el-date-picker v-model="form.endDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" style="width: 100%" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" style="width: 180px" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 统计弹窗 -->
    <el-dialog v-model="showStatsDialog" title="挑战赛统计" width="640px" destroy-on-close class="stats-dialog">
      <div v-if="statsData" class="stats-overview">
        <div class="stats-card">
          <div class="stats-icon" style="background: #ecf5ff; color: #409eff;">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ statsData.totalParticipants || 0 }}</div>
            <div class="stats-label">总参与人数</div>
          </div>
        </div>
        <div class="stats-card">
          <div class="stats-icon" style="background: #f0f9eb; color: #67c23a;">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ statsData.completedCount || 0 }}</div>
            <div class="stats-label">已完成人数</div>
          </div>
        </div>
        <div class="stats-card">
          <div class="stats-icon" style="background: #fdf6ec; color: #e6a23c;">
            <el-icon :size="24"><TrendCharts /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ statsData.completionRate || '0%' }}</div>
            <div class="stats-label">完成率</div>
          </div>
        </div>
      </div>

      <div class="stats-section-title">参与用户列表</div>
      <el-table :data="participantList" v-loading="participantLoading" stripe max-height="350">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="progress" label="进度" width="100" align="center" />
        <el-table-column label="是否完成" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.completed ? 'success' : 'info'" effect="light" size="small">{{ row.completed ? '已完成' : '进行中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="参与时间" min-width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="12"><Clock /></el-icon>
              <span>{{ row.createdAt }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/store/admin'
import { getChallengeList, getChallengeDetail, createChallenge, updateChallenge, deleteChallenge, updateChallengeStatus, getChallengeStats, getChallengeParticipants, exportChallenges } from '@/api/admin'
import { Search, Plus, Download, Edit, Delete, CircleCheck, CircleClose, TrendCharts, Calendar, Clock, User } from '@element-plus/icons-vue'

const adminStore = useAdminStore()
const canManage = computed(() => adminStore.hasButton('challenge:manage'))
const canExport = computed(() => adminStore.hasButton('export'))

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const typeFilter = ref(null)

const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})

const showStatsDialog = ref(false)
const statsData = ref(null)
const participantList = ref([])
const participantLoading = ref(false)

// 顶部概览数据
const overviewCards = computed(() => [
  { key: 'total', label: '总挑战赛', value: total.value, icon: 'Trophy', color: '#409eff', bg: '#ecf5ff' },
  { key: 'active', label: '进行中', value: tableData.value.filter(c => c.status === 1).length, icon: 'CircleCheck', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'offline', label: '已下线', value: tableData.value.filter(c => c.status === 0).length, icon: 'CircleClose', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'ended', label: '已结束', value: tableData.value.filter(c => c.status === 2).length, icon: 'Timer', color: '#909399', bg: '#f4f4f5' }
])

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value != null) params.status = statusFilter.value
    if (typeFilter.value != null) params.type = typeFilter.value
    const res = await getChallengeList(params)
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e); ElMessage.error('加载失败') } finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.value = { type: 1, conditionType: 'checkin_count', conditionValue: 7, status: 1, sortOrder: 0 }
  showDialog.value = true
}

async function openEdit(row) {
  try {
    const res = await getChallengeDetail(row.id)
    const detail = res.data || row
    isEdit.value = true; editId.value = detail.id
    form.value = { ...detail }
    showDialog.value = true
  } catch (e) {
    console.error(e); ElMessage.error('获取详情失败')
  }
}

async function submitForm() {
  if (!form.value.title || !form.value.conditionType || !form.value.conditionValue) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    if (isEdit.value) {
      await updateChallenge(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createChallenge(form.value)
      ElMessage.success('创建成功')
    }
    showDialog.value = false; loadData()
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

async function changeStatus(id, status) {
  try {
    await updateChallengeStatus(id, status)
    ElMessage.success('状态更新成功'); loadData()
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除挑战赛"${row.title}"？`, '确认删除', { type: 'warning' })
    await deleteChallenge(row.id)
    ElMessage.success('删除成功'); loadData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

async function viewStats(row) {
  showStatsDialog.value = true
  participantLoading.value = true
  try {
    const [statsRes, partRes] = await Promise.all([
      getChallengeStats(row.id),
      getChallengeParticipants(row.id, { page: 1, size: 50 })
    ])
    statsData.value = statsRes.data || {}
    participantList.value = partRes.data?.records || []
  } catch (e) { console.error(e); ElMessage.error('加载统计失败') } finally { participantLoading.value = false }
}

async function handleExport() {
  if (!canExport.value) { ElMessage.warning('无导出权限'); return }
  try {
    const res = await exportChallenges({ status: statusFilter.value ?? undefined })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `挑战赛数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {}
}

onMounted(() => { loadData() })
</script>

<style scoped>
.challenges-page { padding: 0; }

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

/* 时间范围 */
.time-range {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

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

/* ========== 统计弹窗 ========== */
.stats-dialog :deep(.el-dialog__body) { padding-top: 12px; }

.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stats-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 10px;
  border: 1px solid #ebeef5;
}
.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stats-info { flex: 1; }
.stats-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.stats-label { font-size: 12px; color: #909399; margin-top: 2px; }

.stats-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}
</style>
