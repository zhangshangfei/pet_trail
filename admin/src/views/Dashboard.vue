<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">趋势统计</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button :value="7">7天</el-radio-button>
                <el-radio-button :value="30">30天</el-radio-button>
                <el-radio-button :value="90">90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="card-title">审核统计</span></template>
          <div ref="auditChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">今日数据</span></template>
          <div class="today-stats">
            <div class="today-item">
              <span class="today-label">今日新增用户</span>
              <span class="today-value">{{ todayStats.todayNewUsers || 0 }}</span>
            </div>
            <div class="today-item">
              <span class="today-label">今日新增动态</span>
              <span class="today-value">{{ todayStats.todayNewPosts || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">待处理事项</span></template>
          <div class="today-stats">
            <div class="today-item">
              <span class="today-label">待审核动态</span>
              <span class="today-value pending">{{ auditStats.pending || 0 }}</span>
            </div>
            <div class="today-item">
              <span class="today-label">待处理举报</span>
              <span class="today-value pending">{{ stats.pendingReports || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { getDashboardStats, getTodayStats, getDashboardTrend, getAuditStats } from '../api/admin'
import * as echarts from 'echarts'

const stats = ref({})
const todayStats = ref({})
const auditStats = ref({})
const trendDays = ref(30)
const trendChartRef = ref(null)
const auditChartRef = ref(null)
let trendChart = null
let auditChart = null

const statCards = computed(() => [
  { key: 'users', label: '总用户数', value: stats.value.totalUsers || 0, icon: 'User', color: '#409eff' },
  { key: 'pets', label: '总宠物数', value: stats.value.totalPets || 0, icon: 'ChatDotRound', color: '#67c23a' },
  { key: 'posts', label: '总动态数', value: stats.value.totalPosts || 0, icon: 'Document', color: '#e6a23c' },
  { key: 'reports', label: '总举报数', value: stats.value.totalReports || 0, icon: 'Warning', color: '#f56c6c' }
])

const loadTrend = async () => {
  try {
    const res = await getDashboardTrend(trendDays.value)
    if (res.data) {
      await nextTick()
      renderTrendChart(res.data)
    }
  } catch (e) {}
}

const renderTrendChart = (data) => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增动态'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.dates || [], axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '新增用户', type: 'line', smooth: true, data: data.newUsers || [], itemStyle: { color: '#409eff' }, areaStyle: { color: 'rgba(64,158,255,0.1)' } },
      { name: '新增动态', type: 'line', smooth: true, data: data.newPosts || [], itemStyle: { color: '#e6a23c' }, areaStyle: { color: 'rgba(230,162,60,0.1)' } }
    ]
  })
}

const renderAuditChart = (data) => {
  if (!auditChartRef.value) return
  if (!auditChart) {
    auditChart = echarts.init(auditChartRef.value)
  }
  auditChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: 'bottom' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}' },
      data: [
        { value: data.pending || 0, name: '待审核', itemStyle: { color: '#e6a23c' } },
        { value: data.approved || 0, name: '已通过', itemStyle: { color: '#67c23a' } },
        { value: data.rejected || 0, name: '已拒绝', itemStyle: { color: '#f56c6c' } }
      ]
    }]
  })
}

const handleResize = () => {
  trendChart?.resize()
  auditChart?.resize()
}

onMounted(async () => {
  try {
    const [statsRes, todayRes, auditRes] = await Promise.all([
      getDashboardStats(), getTodayStats(), getAuditStats()
    ])
    if (statsRes.data) stats.value = statsRes.data
    if (todayRes.data) todayStats.value = todayRes.data
    if (auditRes.data) {
      auditStats.value = auditRes.data
      await nextTick()
      renderAuditChart(auditRes.data)
    }
  } catch (e) {}
  loadTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  auditChart?.dispose()
})
</script>

<style scoped>
.stat-card { cursor: default; }
.stat-content { display: flex; align-items: center; justify-content: space-between; }
.stat-label { font-size: 14px; color: #999; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #333; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; }
.card-title { font-weight: 600; font-size: 16px; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.today-stats { display: flex; flex-direction: column; gap: 16px; }
.today-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.today-item:last-child { border-bottom: none; }
.today-label { color: #666; font-size: 14px; }
.today-value { font-size: 24px; font-weight: 700; color: #333; }
.today-value.pending { color: #f56c6c; }
</style>
