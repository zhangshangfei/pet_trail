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
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats, getTodayStats } from '../api/admin'

const stats = ref({})
const todayStats = ref({})

const statCards = computed(() => [
  { key: 'users', label: '总用户数', value: stats.value.totalUsers || 0, icon: 'User', color: '#409eff' },
  { key: 'pets', label: '总宠物数', value: stats.value.totalPets || 0, icon: 'ChatDotRound', color: '#67c23a' },
  { key: 'posts', label: '总动态数', value: stats.value.totalPosts || 0, icon: 'Document', color: '#e6a23c' },
  { key: 'comments', label: '总评论数', value: stats.value.totalComments || 0, icon: 'ChatLineSquare', color: '#f56c6c' }
])

onMounted(async () => {
  try {
    const [statsRes, todayRes] = await Promise.all([getDashboardStats(), getTodayStats()])
    if (statsRes.data) stats.value = statsRes.data
    if (todayRes.data) todayStats.value = todayRes.data
  } catch (e) {}
})
</script>

<style scoped>
.stat-card { cursor: default; }
.stat-content { display: flex; align-items: center; justify-content: space-between; }
.stat-label { font-size: 14px; color: #999; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #333; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; }
.card-title { font-weight: 600; font-size: 16px; }
.today-stats { display: flex; flex-direction: column; gap: 16px; }
.today-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.today-item:last-child { border-bottom: none; }
.today-label { color: #666; font-size: 14px; }
.today-value { font-size: 24px; font-weight: 700; color: #333; }
.today-value.pending { color: #f56c6c; }
</style>
