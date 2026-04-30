<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon-wrap" :style="{ background: item.color + '15', color: item.color }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(item.value) }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header-row">
              <div class="header-left">
                <span class="card-title">趋势统计</span>
                <span class="card-subtitle">新增用户 / 活跃用户 / 新增动态</span>
              </div>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button :value="7">7天</el-radio-button>
                <el-radio-button :value="30">30天</el-radio-button>
                <el-radio-button :value="90">90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">审核统计</span>
          </template>
          <div ref="auditChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日数据 + 待处理事项 -->
    <el-row :gutter="16" class="bottom-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="header-left">
              <span class="card-title">今日数据</span>
              <el-tag size="small" type="success">实时</el-tag>
            </div>
          </template>
          <div class="data-grid">
            <div class="data-item">
              <div class="data-icon" style="background: #ecf5ff; color: #409eff;">
                <el-icon :size="20"><User /></el-icon>
              </div>
              <div class="data-info">
                <div class="data-value">{{ todayStats.todayNewUsers || 0 }}</div>
                <div class="data-label">新增用户</div>
              </div>
            </div>
            <div class="data-item">
              <div class="data-icon" style="background: #fdf6ec; color: #e6a23c;">
                <el-icon :size="20"><Document /></el-icon>
              </div>
              <div class="data-info">
                <div class="data-value">{{ todayStats.todayNewPosts || 0 }}</div>
                <div class="data-label">新增动态</div>
              </div>
            </div>
            <div class="data-item">
              <div class="data-icon" style="background: #f0f9eb; color: #67c23a;">
                <el-icon :size="20"><UserFilled /></el-icon>
              </div>
              <div class="data-info">
                <div class="data-value">{{ todayStats.todayActiveUsers || 0 }}</div>
                <div class="data-label">活跃用户</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="header-left">
              <span class="card-title">待处理事项</span>
              <el-tag size="small" type="danger">需关注</el-tag>
            </div>
          </template>
          <div class="data-grid">
            <div class="data-item warning">
              <div class="data-icon" style="background: #fdf6ec; color: #e6a23c;">
                <el-icon :size="20"><Clock /></el-icon>
              </div>
              <div class="data-info">
                <div class="data-value">{{ auditStats.pending || 0 }}</div>
                <div class="data-label">待审核动态</div>
              </div>
            </div>
            <div class="data-item warning">
              <div class="data-icon" style="background: #fef0f0; color: #f56c6c;">
                <el-icon :size="20"><Warning /></el-icon>
              </div>
              <div class="data-info">
                <div class="data-value">{{ stats.pendingReports || 0 }}</div>
                <div class="data-label">待处理举报</div>
              </div>
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
  { key: 'activeUsers', label: '今日活跃', value: stats.value.todayActiveUsers || 0, icon: 'UserFilled', color: '#67c23a' },
  { key: 'posts', label: '总动态数', value: stats.value.totalPosts || 0, icon: 'Document', color: '#e6a23c' },
  { key: 'reports', label: '总举报数', value: stats.value.totalReports || 0, icon: 'Warning', color: '#f56c6c' }
])

const formatNumber = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num
}

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
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133' },
      axisPointer: { type: 'line', lineStyle: { color: '#dcdfe6', type: 'dashed' } }
    },
    legend: {
      data: ['新增用户', '活跃用户', '新增动态'],
      top: 0,
      right: 0,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 12, color: '#606266' }
    },
    grid: { left: '2%', right: '3%', bottom: '2%', top: 40, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.dates || [],
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisTick: { show: false },
      axisLabel: { fontSize: 11, color: '#909399' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f2f6fc' } },
      axisLabel: { fontSize: 11, color: '#909399' }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: data.newUsers || [],
        itemStyle: { color: '#409eff' },
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.2)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' }
          ])
        }
      },
      {
        name: '活跃用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: data.activeUsers || [],
        itemStyle: { color: '#67c23a' },
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103,194,58,0.2)' },
            { offset: 1, color: 'rgba(103,194,58,0.02)' }
          ])
        }
      },
      {
        name: '新增动态',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: data.newPosts || [],
        itemStyle: { color: '#e6a23c' },
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230,162,60,0.2)' },
            { offset: 1, color: 'rgba(230,162,60,0.02)' }
          ])
        }
      }
    ]
  }, true)
}

const renderAuditChart = (data) => {
  if (!auditChartRef.value) return
  if (!auditChart) {
    auditChart = echarts.init(auditChartRef.value)
  }
  const total = (data.pending || 0) + (data.approved || 0) + (data.rejected || 0)
  auditChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133' },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 0,
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 12, color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
      label: {
        show: total > 0,
        position: 'center',
        formatter: () => `{total|${total}}\n{label|总计}`,
        rich: {
          total: { fontSize: 28, fontWeight: 'bold', color: '#303133', lineHeight: 36 },
          label: { fontSize: 12, color: '#909399', lineHeight: 18 }
        }
      },
      emphasis: {
        label: { show: true }
      },
      labelLine: { show: false },
      data: [
        { value: data.pending || 0, name: '待审核', itemStyle: { color: '#e6a23c' } },
        { value: data.approved || 0, name: '已通过', itemStyle: { color: '#67c23a' } },
        { value: data.rejected || 0, name: '已拒绝', itemStyle: { color: '#f56c6c' } }
      ]
    }]
  }, true)
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
.dashboard { padding: 0; }

/* 顶部统计卡片 */
.stat-card { transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-2px); }
.stat-content { display: flex; align-items: center; gap: 16px; }
.stat-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info { flex: 1; min-width: 0; }
.stat-value { font-size: 26px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

/* 图表区域 */
.chart-row { margin-top: 16px; }
.chart-card { height: 100%; }
.chart-container { height: 320px; }

/* 卡片头部 */
.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-title { font-weight: 600; font-size: 15px; color: #303133; }
.card-subtitle { font-size: 12px; color: #909399; margin-left: 8px; }

/* 底部数据卡片 */
.bottom-row { margin-top: 16px; }
.info-card { height: 100%; }
.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}
.data-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 10px;
  background: #fafbfc;
  transition: background 0.2s;
}
.data-item:hover { background: #f2f3f5; }
.data-item.warning:hover { background: #fff5f5; }
.data-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.data-info { flex: 1; min-width: 0; }
.data-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.data-label { font-size: 12px; color: #909399; margin-top: 2px; }
</style>
