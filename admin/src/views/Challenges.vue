<template>
  <div class="challenges-page">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="进行中" :value="activeCount" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="已结束" :value="endedCount" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="总参与人次" :value="totalParticipants" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="总完成人次" :value="totalCompleted" /></el-card></el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="page-header">
          <span class="card-title">挑战赛配置</span>
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
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="openCreate">新增挑战赛</el-button>
            <el-button @click="handleExport" v-if="canExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'warning' : row.type === 2 ? '' : 'success'" size="small">
              {{ { 1: '打卡', 2: '社交', 3: '健康' }[row.type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conditionType" label="条件类型" width="130" />
        <el-table-column prop="conditionValue" label="目标值" width="90" />
        <el-table-column prop="participantCount" label="参与人数" width="100" />
        <el-table-column prop="rewardDescription" label="奖励" width="140" show-overflow-tooltip />
        <el-table-column label="时间范围" width="180">
          <template #default="{ row }">
            <div style="font-size: 12px; line-height: 1.5">{{ row.startDate?.substring(0, 10) }} ~ {{ row.endDate?.substring(0, 10) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'danger'" size="small">
              {{ { 0: '已下线', 1: '进行中', 2: '已结束' }[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text @click="viewStats(row)">统计</el-button>
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" text @click="changeStatus(row.id, 0)">下线</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" text @click="changeStatus(row.id, 1)">上线</el-button>
            <el-button v-if="canManage" type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑挑战赛' : '新增挑战赛'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="封面图"><el-input v-model="form.coverImage" placeholder="图片URL" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type"><el-option label="打卡" :value="1" /><el-option label="社交" :value="2" /><el-option label="健康" :value="3" /></el-select>
        </el-form-item>
        <el-form-item label="条件类型" required>
          <el-select v-model="form.conditionType">
            <el-option label="累计打卡" value="checkin_count" /><el-option label="连续打卡" value="checkin_streak" />
            <el-option label="健康记录" value="health_record_count" /><el-option label="发布动态" value="post_count" />
            <el-option label="获得点赞" value="like_received" /><el-option label="粉丝数" value="follower_count" />
            <el-option label="宠物数" value="pet_count" /><el-option label="评论数" value="comment_count" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值" required><el-input-number v-model="form.conditionValue" :min="1" /></el-form-item>
        <el-form-item label="奖励描述"><el-input v-model="form.rewardDescription" /></el-form-item>
        <el-form-item label="开始时间" required><el-date-picker v-model="form.startDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间" required><el-date-picker v-model="form.endDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showStatsDialog" title="挑战赛统计" width="500px">
      <el-descriptions :column="2" border v-if="statsData">
        <el-descriptions-item label="总参与人数">{{ statsData.totalParticipants }}</el-descriptions-item>
        <el-descriptions-item label="已完成人数">{{ statsData.completedCount }}</el-descriptions-item>
        <el-descriptions-item label="完成率">{{ statsData.completionRate }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="participantList" v-loading="participantLoading" stripe style="margin-top: 16px" max-height="300">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="progress" label="进度" width="100" />
        <el-table-column label="是否完成" width="100">
          <template #default="{ row }"><el-tag :type="row.completed ? 'success' : 'info'" size="small">{{ row.completed ? '已完成' : '进行中' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="参与时间" min-width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/store/admin'
import { getChallengeList, getChallengeDetail, createChallenge, updateChallenge, deleteChallenge, updateChallengeStatus, getChallengeStats, getChallengeParticipants, exportChallenges } from '@/api/admin'

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
const activeCount = ref(0)
const endedCount = ref(0)
const totalParticipants = ref(0)
const totalCompleted = ref(0)

const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({})

const showStatsDialog = ref(false)
const statsData = ref(null)
const participantList = ref([])
const participantLoading = ref(false)

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
    const allRes = await getChallengeList({ page: 1, size: 1, status: 1 })
    activeCount.value = allRes.data?.total || 0
    const endedRes = await getChallengeList({ page: 1, size: 1, status: 2 })
    endedCount.value = endedRes.data?.total || 0
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
    await ElMessageBox.confirm(`确定删除挑战赛"${row.title}"？`, '提示', { type: 'warning' })
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
  } catch (e) { ElMessage.error('导出失败') }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.stats-row .el-card { text-align: center; }
</style>
