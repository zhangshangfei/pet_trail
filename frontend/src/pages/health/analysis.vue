<template>
  <view class="analysis-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="isLoggedIn ? (userName || '萌宠主人') : '请登录'"
      :show-login-button="!isLoggedIn"
      :show-discover="false"
      :show-bell="false"
      @userTap="goBack"
    />

    <scroll-view scroll-y class="analysis-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="analysis-content">
        <view v-if="loading" class="loading-wrap">
          <view class="loading-spinner"></view>
          <text class="loading-text">正在分析健康数据...</text>
        </view>

        <template v-else-if="analysis">
          <view class="score-section">
            <view class="score-ring-wrap">
              <view class="score-ring" :class="scoreClass">
                <text class="score-number">{{ analysis.score }}</text>
                <text class="score-label">健康评分</text>
              </view>
            </view>
            <text class="level-badge" :class="levelClass">{{ analysis.level }}</text>
          </view>

          <view class="detail-section">
            <text class="section-title">📊 分项评分</text>
            <view class="detail-grid">
              <view class="detail-card">
                <text class="detail-icon">💉</text>
                <text class="detail-name">疫苗</text>
                <view class="detail-bar-bg">
                  <view class="detail-bar-fill vaccine" :style="{ width: (analysis.detail.vaccineScore || 0) + '%' }"></view>
                </view>
                <text class="detail-score">{{ analysis.detail.vaccineScore || 0 }}</text>
              </view>
              <view class="detail-card">
                <text class="detail-icon">🐛</text>
                <text class="detail-name">驱虫</text>
                <view class="detail-bar-bg">
                  <view class="detail-bar-fill parasite" :style="{ width: (analysis.detail.parasiteScore || 0) + '%' }"></view>
                </view>
                <text class="detail-score">{{ analysis.detail.parasiteScore || 0 }}</text>
              </view>
              <view class="detail-card">
                <text class="detail-icon">⚖️</text>
                <text class="detail-name">体重</text>
                <view class="detail-bar-bg">
                  <view class="detail-bar-fill weight" :style="{ width: (analysis.detail.weightScore || 0) + '%' }"></view>
                </view>
                <text class="detail-score">{{ analysis.detail.weightScore || 0 }}</text>
              </view>
              <view class="detail-card">
                <text class="detail-icon">✅</text>
                <text class="detail-name">打卡</text>
                <view class="detail-bar-bg">
                  <view class="detail-bar-fill checkin" :style="{ width: (analysis.detail.checkinScore || 0) + '%' }"></view>
                </view>
                <text class="detail-score">{{ analysis.detail.checkinScore || 0 }}</text>
              </view>
            </view>
          </view>

          <view v-if="analysis.trends && Object.keys(analysis.trends).length" class="trends-section">
            <text class="section-title">📈 健康趋势</text>
            <view class="trends-list">
              <view v-for="(value, key) in analysis.trends" :key="key" class="trend-item">
                <text class="trend-key">{{ trendLabels[key] || key }}</text>
                <text class="trend-value" :class="trendClass(value)">{{ value }}</text>
              </view>
            </view>
          </view>

          <view v-if="analysis.warnings && analysis.warnings.length" class="warnings-section">
            <text class="section-title">⚠️ 健康预警</text>
            <view class="warnings-list">
              <view v-for="(w, i) in analysis.warnings" :key="i" class="warning-item" :class="'severity-' + w.severity">
                <text class="warning-icon">{{ w.severity === 'high' ? '🔴' : w.severity === 'medium' ? '🟡' : '🟢' }}</text>
                <view class="warning-content">
                  <text class="warning-msg">{{ w.message }}</text>
                </view>
              </view>
            </view>
          </view>

          <view v-if="analysis.suggestions && analysis.suggestions.length" class="suggestions-section">
            <text class="section-title">💡 健康建议</text>
            <view class="suggestions-list">
              <view v-for="(s, i) in analysis.suggestions" :key="i" class="suggestion-item">
                <text class="suggestion-num">{{ i + 1 }}</text>
                <text class="suggestion-text">{{ s }}</text>
              </view>
            </view>
          </view>

          <view v-if="analysis.detail && analysis.detail.weightAnalysis" class="weight-section">
            <text class="section-title">⚖️ 体重分析</text>
            <view class="weight-detail">
              <view class="weight-item">
                <text class="weight-label">当前体重</text>
                <text class="weight-value">{{ analysis.detail.weightAnalysis.currentWeight || '-' }} kg</text>
              </view>
              <view class="weight-item">
                <text class="weight-label">30日均值</text>
                <text class="weight-value">{{ analysis.detail.weightAnalysis.avgWeight30d || '-' }} kg</text>
              </view>
              <view class="weight-item">
                <text class="weight-label">波动率</text>
                <text class="weight-value">{{ analysis.detail.weightAnalysis.weightChangeRate || '-' }}%</text>
              </view>
              <view class="weight-item">
                <text class="weight-label">趋势</text>
                <text class="weight-value" :class="trendClass(analysis.detail.weightAnalysis.trend)">{{ analysis.detail.weightAnalysis.trend }}</text>
              </view>
            </view>
          </view>

          <view v-if="analysis.detail && analysis.detail.vaccineAnalysis" class="vaccine-section">
            <text class="section-title">💉 疫苗分析</text>
            <view class="vaccine-detail">
              <view class="vaccine-item">
                <text class="vaccine-label">总记录</text>
                <text class="vaccine-value">{{ analysis.detail.vaccineAnalysis.total || 0 }}</text>
              </view>
              <view class="vaccine-item">
                <text class="vaccine-label">已完成</text>
                <text class="vaccine-value vaccine-done">{{ analysis.detail.vaccineAnalysis.completed || 0 }}</text>
              </view>
              <view class="vaccine-item">
                <text class="vaccine-label">已逾期</text>
                <text class="vaccine-value" :class="analysis.detail.vaccineAnalysis.overdue > 0 ? 'vaccine-overdue' : ''">{{ analysis.detail.vaccineAnalysis.overdue || 0 }}</text>
              </view>
              <view v-if="analysis.detail.vaccineAnalysis.nextVaccineName" class="vaccine-item">
                <text class="vaccine-label">下次接种</text>
                <text class="vaccine-value">{{ analysis.detail.vaccineAnalysis.nextVaccineName }} ({{ analysis.detail.vaccineAnalysis.nextVaccineDate }})</text>
              </view>
            </view>
          </view>

          <view v-if="analysis.aiAnalysis" class="ai-section">
            <text class="section-title">🤖 AI 深度分析</text>
            <view class="ai-card">
              <text class="ai-text">{{ analysis.aiAnalysis }}</text>
            </view>
          </view>

          <view class="action-section">
            <button class="back-btn" @tap="goBack">返回</button>
            <button class="reanalyze-btn" @tap="runAnalysis" :disabled="loading">重新分析</button>
          </view>
        </template>

        <view v-else class="empty-wrap">
          <text class="empty-icon">🏥</text>
          <text class="empty-text">暂无健康分析数据</text>
          <button class="analyze-btn" @tap="runAnalysis">开始分析</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getHealthAnalysis } from '@/api/health'
import { getPetList } from '@/api/pet'
import { useUserStore } from '@/store/user'
import UserTopBar from '@/components/UserTopBar.vue'

const userStore = useUserStore()
const statusBarHeight = ref(44)
const loading = ref(false)
const analysis = ref(null)
const currentPetId = ref(null)

const isLoggedIn = computed(() => !!userStore.token)
const userName = computed(() => userStore.userInfo?.nickname || '')
const userAvatar = computed(() => userStore.userInfo?.avatar || '')

const scoreClass = computed(() => {
  if (!analysis.value) return ''
  const s = analysis.value.score
  if (s >= 90) return 'score-excellent'
  if (s >= 75) return 'score-good'
  if (s >= 60) return 'score-fair'
  return 'score-poor'
})

const levelClass = computed(() => {
  if (!analysis.value) return ''
  const map = { '优秀': 'excellent', '良好': 'good', '一般': 'fair', '需关注': 'poor' }
  return 'level-' + (map[analysis.value.level] || 'fair')
})

const trendLabels = {
  vaccine: '疫苗',
  parasite: '驱虫',
  weight: '体重',
  activity: '活跃度'
}

const trendClass = (value) => {
  const map = {
    '稳定': 'stable', '已完成': 'done', '活跃': 'active',
    '进行中': 'progress', '上升': 'up', '需关注': 'attention',
    '不活跃': 'inactive', '下降': 'down', '无数据': 'nodata'
  }
  return 'trend-' + (map[value] || 'stable')
}

const goBack = () => {
  uni.navigateBack()
}

const runAnalysis = async () => {
  if (!currentPetId.value) {
    uni.showToast({ title: '请先选择宠物', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await getHealthAnalysis(currentPetId.value)
    if (res.code === 200) {
      analysis.value = res.data
    } else {
      uni.showToast({ title: res.message || '分析失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '分析失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 44

  const petId = uni.getStorageSync('currentPetId')
  if (petId) {
    currentPetId.value = petId
    await runAnalysis()
  } else {
    try {
      const res = await getPetList()
      if (res.code === 200 && res.data && res.data.length > 0) {
        currentPetId.value = res.data[0].id
        uni.setStorageSync('currentPetId', res.data[0].id)
        await runAnalysis()
      }
    } catch (e) {}
  }
})
</script>

<style lang="scss" scoped>
$bg: #f5f6fa;
$card-bg: #ffffff;
$text-primary: #1a1a2e;
$text-secondary: #6b7280;
$text-light: #9ca3af;
$accent: #ff6a3d;
$blue: #4a90d9;
$green: #34c759;
$orange: #ff9500;
$red: #ff3b30;

.analysis-page { min-height: 100vh; background: $bg; }
.analysis-scroll { height: 100vh; box-sizing: border-box; }
.analysis-content { padding: 20rpx 24rpx 120rpx; }

.loading-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
.loading-spinner { width: 60rpx; height: 60rpx; border: 4rpx solid #e0e0e0; border-top-color: $accent; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.loading-text { margin-top: 20rpx; font-size: 28rpx; color: $text-secondary; }

.score-section { display: flex; flex-direction: column; align-items: center; margin-bottom: 32rpx; }
.score-ring-wrap { margin-bottom: 16rpx; }
.score-ring { width: 200rpx; height: 200rpx; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; border: 8rpx solid #e0e0e0; }
.score-excellent { border-color: $green; }
.score-good { border-color: $blue; }
.score-fair { border-color: $orange; }
.score-poor { border-color: $red; }
.score-number { font-size: 56rpx; font-weight: 700; color: $text-primary; }
.score-label { font-size: 22rpx; color: $text-secondary; margin-top: 4rpx; }
.level-badge { font-size: 26rpx; font-weight: 600; padding: 6rpx 24rpx; border-radius: 20rpx; color: #fff; }
.level-excellent { background: $green; }
.level-good { background: $blue; }
.level-fair { background: $orange; }
.level-poor { background: $red; }

.section-title { font-size: 30rpx; font-weight: 600; color: $text-primary; margin-bottom: 16rpx; display: block; }

.detail-section { background: $card-bg; border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.detail-grid { display: flex; flex-direction: column; gap: 20rpx; }
.detail-card { display: flex; align-items: center; gap: 12rpx; }
.detail-icon { font-size: 32rpx; }
.detail-name { font-size: 26rpx; color: $text-secondary; min-width: 60rpx; }
.detail-bar-bg { flex: 1; height: 16rpx; background: #f0f0f0; border-radius: 8rpx; overflow: hidden; }
.detail-bar-fill { height: 100%; border-radius: 8rpx; transition: width 0.6s ease; }
.detail-bar-fill.vaccine { background: $blue; }
.detail-bar-fill.parasite { background: $green; }
.detail-bar-fill.weight { background: $orange; }
.detail-bar-fill.checkin { background: $accent; }
.detail-score { font-size: 26rpx; font-weight: 600; color: $text-primary; min-width: 50rpx; text-align: right; }

.trends-section { background: $card-bg; border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.trends-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.trend-item { display: flex; align-items: center; gap: 8rpx; background: #f8f9fb; padding: 12rpx 20rpx; border-radius: 12rpx; }
.trend-key { font-size: 24rpx; color: $text-secondary; }
.trend-value { font-size: 24rpx; font-weight: 600; }
.trend-stable, .trend-done, .trend-active { color: $green; }
.trend-progress { color: $orange; }
.trend-up, .trend-attention, .trend-inactive { color: $red; }
.trend-down { color: $blue; }
.trend-nodata { color: $text-light; }

.warnings-section { background: $card-bg; border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.warnings-list { display: flex; flex-direction: column; gap: 16rpx; }
.warning-item { display: flex; align-items: flex-start; gap: 12rpx; padding: 16rpx; border-radius: 12rpx; }
.severity-high { background: #fff5f5; }
.severity-medium { background: #fffbeb; }
.severity-low { background: #f0fdf4; }
.warning-icon { font-size: 28rpx; margin-top: 2rpx; }
.warning-content { flex: 1; }
.warning-msg { font-size: 26rpx; color: $text-primary; line-height: 1.5; }

.suggestions-section { background: $card-bg; border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.suggestions-list { display: flex; flex-direction: column; gap: 16rpx; }
.suggestion-item { display: flex; align-items: flex-start; gap: 12rpx; }
.suggestion-num { width: 36rpx; height: 36rpx; border-radius: 50%; background: $accent; color: #fff; font-size: 22rpx; font-weight: 600; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 2rpx; }
.suggestion-text { font-size: 26rpx; color: $text-primary; line-height: 1.6; flex: 1; }

.weight-section, .vaccine-section { background: $card-bg; border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.weight-detail, .vaccine-detail { display: flex; flex-direction: column; gap: 16rpx; }
.weight-item, .vaccine-item { display: flex; justify-content: space-between; align-items: center; padding: 8rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.weight-item:last-child, .vaccine-item:last-child { border-bottom: none; }
.weight-label, .vaccine-label { font-size: 26rpx; color: $text-secondary; }
.weight-value, .vaccine-value { font-size: 26rpx; font-weight: 600; color: $text-primary; }
.vaccine-done { color: $green; }
.vaccine-overdue { color: $red; }

.ai-section { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; }
.ai-section .section-title { color: #fff; }
.ai-card { background: rgba(255,255,255,0.15); border-radius: 16rpx; padding: 20rpx; }
.ai-text { font-size: 26rpx; color: #fff; line-height: 1.8; }

.action-section { margin-top: 16rpx; padding-bottom: 40rpx; display: flex; gap: 20rpx; }
.back-btn { width: 200rpx; height: 88rpx; line-height: 88rpx; background: #f0f0f0; color: #666; font-size: 28rpx; font-weight: 600; border-radius: 44rpx; border: none; }
.back-btn:active { background: #e0e0e0; }
.reanalyze-btn { flex: 1; height: 88rpx; line-height: 88rpx; background: $accent; color: #fff; font-size: 30rpx; font-weight: 600; border-radius: 44rpx; border: none; }
.reanalyze-btn[disabled] { opacity: 0.6; }

.empty-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: $text-secondary; margin-bottom: 40rpx; }
.analyze-btn { width: 320rpx; height: 80rpx; line-height: 80rpx; background: $accent; color: #fff; font-size: 28rpx; font-weight: 600; border-radius: 40rpx; border: none; }
</style>
