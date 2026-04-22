<template>
  <view class="analysis-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="userName"
      :show-discover="false"
      :show-bell="false"
      @userTap="goBack"
    />

    <scroll-view scroll-y class="analysis-scroll" :style="{ paddingTop: (statusBarHeight + 54) + 'px' }">
      <view class="analysis-content">
        <view v-if="loading" class="loading-wrap">
          <view class="loading-spinner"></view>
          <text class="loading-text">正在分析健康数据...</text>
        </view>

        <template v-else-if="analysis">
          <!-- 顶部评分区域 -->
          <view class="score-card">
            <view class="score-left">
              <view class="score-ring" :class="scoreClass">
                <text class="score-number">{{ analysis.score }}</text>
                <text class="score-label">健康评分</text>
              </view>
            </view>
            <view class="score-right">
              <view class="level-badge" :class="levelClass">{{ analysis.level }}</view>
              <text class="score-desc">基于疫苗、驱虫、体重等多维度综合评估</text>
            </view>
          </view>

          <!-- 分项评分 -->
          <view class="detail-section">
            <view class="section-header">
              <image class="section-icon" src="/static/icons/chart.png" mode="aspectFit" />
              <text class="section-title">分项评分</text>
            </view>
            <view class="detail-list">
              <view class="detail-row">
                <view class="detail-label">
                  <text class="detail-icon vaccine-icon">💉</text>
                  <text class="detail-name">疫苗</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg">
                    <view class="detail-bar-fill vaccine" :style="{ width: (analysis.detail.vaccineScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ analysis.detail.vaccineScore || 0 }}</text>
              </view>
              <view class="detail-row">
                <view class="detail-label">
                  <text class="detail-icon parasite-icon">🐛</text>
                  <text class="detail-name">驱虫</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg">
                    <view class="detail-bar-fill parasite" :style="{ width: (analysis.detail.parasiteScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ analysis.detail.parasiteScore || 0 }}</text>
              </view>
              <view class="detail-row">
                <view class="detail-label">
                  <text class="detail-icon weight-icon">⚖️</text>
                  <text class="detail-name">体重</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg">
                    <view class="detail-bar-fill weight" :style="{ width: (analysis.detail.weightScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ analysis.detail.weightScore || 0 }}</text>
              </view>
              <view class="detail-row">
                <view class="detail-label">
                  <text class="detail-icon checkin-icon">✅</text>
                  <text class="detail-name">打卡</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg">
                    <view class="detail-bar-fill checkin" :style="{ width: (analysis.detail.checkinScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ analysis.detail.checkinScore || 0 }}</text>
              </view>
            </view>
          </view>

          <!-- 健康趋势 -->
          <view v-if="analysis.trends && Object.keys(analysis.trends).length" class="trends-section">
            <view class="section-header">
              <image class="section-icon" src="/static/icons/trend.png" mode="aspectFit" />
              <text class="section-title">健康趋势</text>
            </view>
            <view class="trends-list">
              <view v-for="(value, key) in analysis.trends" :key="key" class="trend-tag">
                <text class="trend-key">{{ trendLabels[key] || key }}</text>
                <text class="trend-value" :class="trendClass(value)">{{ value }}</text>
              </view>
            </view>
          </view>

          <!-- 健康预警 -->
          <view v-if="analysis.warnings && analysis.warnings.length" class="warnings-section">
            <view class="section-header">
              <image class="section-icon" src="/static/icons/warning.png" mode="aspectFit" />
              <text class="section-title">健康预警</text>
            </view>
            <view class="warnings-list">
              <view v-for="(w, i) in analysis.warnings" :key="i" class="warning-item" :class="'severity-' + w.severity">
                <view class="warning-icon-wrap">
                  <text class="warning-icon">{{ w.severity === 'high' ? '!' : w.severity === 'medium' ? '⚠' : 'ℹ' }}</text>
                </view>
                <text class="warning-msg">{{ w.message }}</text>
              </view>
            </view>
          </view>

          <!-- 健康建议 -->
          <view v-if="analysis.suggestions && analysis.suggestions.length" class="suggestions-section">
            <view class="section-header">
              <image class="section-icon" src="/static/icons/lightbulb.png" mode="aspectFit" />
              <text class="section-title">健康建议</text>
            </view>
            <view class="suggestions-list">
              <view v-for="(s, i) in analysis.suggestions" :key="i" class="suggestion-item">
                <view class="suggestion-dot"></view>
                <text class="suggestion-text">{{ s }}</text>
              </view>
            </view>
          </view>

          <!-- 体重分析 -->
          <view v-if="analysis.detail && analysis.detail.weightAnalysis" class="weight-section">
            <view class="section-header">
              <image class="section-icon" src="/static/icons/weight.png" mode="aspectFit" />
              <text class="section-title">体重分析</text>
            </view>
            <view class="weight-grid">
              <view class="weight-cell">
                <text class="weight-cell-label">当前体重</text>
                <text class="weight-cell-value">{{ analysis.detail.weightAnalysis.currentWeight || '-' }} <text class="weight-unit">kg</text></text>
              </view>
              <view class="weight-cell">
                <text class="weight-cell-label">30日均值</text>
                <text class="weight-cell-value">{{ analysis.detail.weightAnalysis.avgWeight30d || '-' }} <text class="weight-unit">kg</text></text>
              </view>
              <view class="weight-cell">
                <text class="weight-cell-label">波动率</text>
                <text class="weight-cell-value">{{ analysis.detail.weightAnalysis.weightChangeRate || '-' }}%</text>
              </view>
              <view class="weight-cell">
                <text class="weight-cell-label">趋势</text>
                <text class="weight-cell-value" :class="trendClass(analysis.detail.weightAnalysis.trend)">{{ analysis.detail.weightAnalysis.trend }}</text>
              </view>
            </view>
          </view>

          <!-- 疫苗分析 -->
          <view v-if="analysis.detail && analysis.detail.vaccineAnalysis" class="vaccine-section">
            <view class="section-header">
              <image class="section-icon" src="/static/icons/vaccine.png" mode="aspectFit" />
              <text class="section-title">疫苗分析</text>
            </view>
            <view class="vaccine-grid">
              <view class="vaccine-cell">
                <text class="vaccine-cell-label">总记录</text>
                <text class="vaccine-cell-value">{{ analysis.detail.vaccineAnalysis.total || 0 }}</text>
              </view>
              <view class="vaccine-cell">
                <text class="vaccine-cell-label">已完成</text>
                <text class="vaccine-cell-value text-green">{{ analysis.detail.vaccineAnalysis.completed || 0 }}</text>
              </view>
              <view class="vaccine-cell">
                <text class="vaccine-cell-label">已逾期</text>
                <text class="vaccine-cell-value" :class="analysis.detail.vaccineAnalysis.overdue > 0 ? 'text-red' : ''">{{ analysis.detail.vaccineAnalysis.overdue || 0 }}</text>
              </view>
            </view>
            <view v-if="analysis.detail.vaccineAnalysis.nextVaccineName" class="vaccine-next">
              <view class="vaccine-next-icon">📅</view>
              <text class="vaccine-next-text">下次接种: {{ analysis.detail.vaccineAnalysis.nextVaccineName }} ({{ analysis.detail.vaccineAnalysis.nextVaccineDate }})</text>
            </view>
          </view>

          <!-- AI 快速分析 -->
          <view v-if="analysis.aiAnalysis" class="ai-section">
            <view class="section-header">
              <text class="ai-section-title">🤖 大模型快速分析</text>
            </view>
            <view class="ai-card">
              <text class="ai-text">{{ analysis.aiAnalysis }}</text>
            </view>
          </view>

          <!-- 底部按钮 -->
          <view class="action-section">
            <view class="btn-group">
              <view class="btn-back" @tap="goBack">
                <text class="btn-back-text">返回</text>
              </view>
              <view class="btn-reanalyze" @tap="runAnalysis" :class="{ disabled: loading }">
                <text class="btn-reanalyze-text">重新分析</text>
              </view>
            </view>
          </view>
        </template>

        <view v-else class="empty-wrap">
          <view class="empty-illustration">
            <text class="empty-icon">🏥</text>
          </view>
          <text class="empty-text">暂无健康分析数据</text>
          <text class="empty-sub">点击下方按钮开始为您的宠物进行健康分析</text>
          <view class="btn-analyze" @tap="runAnalysis">
            <text class="btn-analyze-text">开始分析</text>
          </view>
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
  } else {
    try {
      const res = await getPetList()
      if (res.code === 200 && res.data && res.data.length > 0) {
        currentPetId.value = res.data[0].id
        uni.setStorageSync('currentPetId', res.data[0].id)
      }
    } catch (e) {}
  }

  if (currentPetId.value) {
    await runAnalysis()
  }
})
</script>

<style lang="scss" scoped>
$bg: #f0f2f5;
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
.analysis-content { padding: 16rpx 24rpx 160rpx; }

/* ===== 加载 ===== */
.loading-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
.loading-spinner { width: 60rpx; height: 60rpx; border: 4rpx solid #e0e0e0; border-top-color: $accent; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.loading-text { margin-top: 20rpx; font-size: 28rpx; color: $text-secondary; }

/* ===== 顶部评分卡片 ===== */
.score-card { background: $card-bg; border-radius: 24rpx; padding: 32rpx; margin-bottom: 20rpx; display: flex; align-items: center; gap: 28rpx; box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.04); }
.score-left { flex-shrink: 0; }
.score-ring { width: 180rpx; height: 180rpx; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; border: 10rpx solid #e8e8e8; position: relative; }
.score-ring::before { content: ''; position: absolute; top: -10rpx; left: -10rpx; right: -10rpx; bottom: -10rpx; border-radius: 50%; border: 10rpx solid transparent; border-top-color: currentColor; transform: rotate(-45deg); }
.score-excellent { color: $green; border-color: #e0f0e0; }
.score-excellent::before { border-top-color: $green; }
.score-good { color: $blue; border-color: #e0eaf5; }
.score-good::before { border-top-color: $blue; }
.score-fair { color: $orange; border-color: #f5f0e0; }
.score-fair::before { border-top-color: $orange; }
.score-poor { color: $red; border-color: #f5e0e0; }
.score-poor::before { border-top-color: $red; }
.score-number { font-size: 52rpx; font-weight: 800; color: $text-primary; line-height: 1; }
.score-label { font-size: 22rpx; color: $text-secondary; margin-top: 6rpx; }
.score-right { flex: 1; display: flex; flex-direction: column; gap: 12rpx; }
.level-badge { align-self: flex-start; font-size: 26rpx; font-weight: 700; padding: 8rpx 28rpx; border-radius: 28rpx; color: #fff; }
.level-excellent { background: linear-gradient(135deg, #34c759 0%, #2ecc71 100%); }
.level-good { background: linear-gradient(135deg, #4a90d9 0%, #5b9bd5 100%); }
.level-fair { background: linear-gradient(135deg, #ff9500 0%, #ffaa33 100%); }
.level-poor { background: linear-gradient(135deg, #ff3b30 0%, #ff5544 100%); }
.score-desc { font-size: 24rpx; color: $text-light; line-height: 1.5; }

/* ===== 通用区块 ===== */
.detail-section, .trends-section, .warnings-section, .suggestions-section, .weight-section, .vaccine-section, .ai-section {
  background: $card-bg; border-radius: 24rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.04);
}
.section-header { display: flex; align-items: center; gap: 10rpx; margin-bottom: 20rpx; }
.section-icon { width: 36rpx; height: 36rpx; }
.section-title { font-size: 30rpx; font-weight: 700; color: $text-primary; }

/* ===== 分项评分 ===== */
.detail-list { display: flex; flex-direction: column; gap: 24rpx; }
.detail-row { display: flex; align-items: center; gap: 16rpx; }
.detail-label { display: flex; align-items: center; gap: 10rpx; min-width: 100rpx; }
.detail-icon { font-size: 32rpx; width: 40rpx; height: 40rpx; display: flex; align-items: center; justify-content: center; }
.detail-name { font-size: 26rpx; color: $text-secondary; }
.detail-bar-wrap { flex: 1; }
.detail-bar-bg { height: 14rpx; background: #f0f0f0; border-radius: 7rpx; overflow: hidden; }
.detail-bar-fill { height: 100%; border-radius: 7rpx; transition: width 0.8s ease; }
.detail-bar-fill.vaccine { background: linear-gradient(90deg, #4a90d9, #6ba3e0); }
.detail-bar-fill.parasite { background: linear-gradient(90deg, #34c759, #5dd87a); }
.detail-bar-fill.weight { background: linear-gradient(90deg, #ff9500, #ffb347); }
.detail-bar-fill.checkin { background: linear-gradient(90deg, #ff6a3d, #ff8a5d); }
.detail-score { font-size: 28rpx; font-weight: 700; color: $text-primary; min-width: 60rpx; text-align: right; }

/* ===== 健康趋势 ===== */
.trends-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.trend-tag { display: flex; align-items: center; gap: 8rpx; background: #f5f7fa; padding: 12rpx 20rpx; border-radius: 12rpx; }
.trend-key { font-size: 24rpx; color: $text-secondary; }
.trend-value { font-size: 24rpx; font-weight: 600; }
.trend-stable, .trend-done, .trend-active { color: $green; }
.trend-progress { color: $orange; }
.trend-up, .trend-attention, .trend-inactive { color: $red; }
.trend-down { color: $blue; }
.trend-nodata { color: $text-light; }

/* ===== 健康预警 ===== */
.warnings-list { display: flex; flex-direction: column; gap: 14rpx; }
.warning-item { display: flex; align-items: center; gap: 16rpx; padding: 18rpx 20rpx; border-radius: 16rpx; }
.severity-high { background: #fff2f0; }
.severity-medium { background: #fffbe6; }
.severity-low { background: #f6ffed; }
.warning-icon-wrap { width: 48rpx; height: 48rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.severity-high .warning-icon-wrap { background: #ffccc7; }
.severity-medium .warning-icon-wrap { background: #ffe58f; }
.severity-low .warning-icon-wrap { background: #d9f7be; }
.warning-icon { font-size: 24rpx; font-weight: 700; color: $text-primary; }
.warning-msg { font-size: 26rpx; color: $text-primary; line-height: 1.5; flex: 1; }

/* ===== 健康建议 ===== */
.suggestions-list { display: flex; flex-direction: column; gap: 16rpx; }
.suggestion-item { display: flex; align-items: flex-start; gap: 14rpx; padding: 4rpx 0; }
.suggestion-dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: $accent; flex-shrink: 0; margin-top: 14rpx; }
.suggestion-text { font-size: 26rpx; color: $text-primary; line-height: 1.6; flex: 1; }

/* ===== 体重分析 ===== */
.weight-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16rpx; }
.weight-cell { background: #f8f9fb; border-radius: 16rpx; padding: 20rpx; display: flex; flex-direction: column; gap: 8rpx; }
.weight-cell-label { font-size: 24rpx; color: $text-secondary; }
.weight-cell-value { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.weight-unit { font-size: 22rpx; color: $text-light; font-weight: 400; }

/* ===== 疫苗分析 ===== */
.vaccine-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16rpx; margin-bottom: 16rpx; }
.vaccine-cell { background: #f8f9fb; border-radius: 16rpx; padding: 20rpx; display: flex; flex-direction: column; align-items: center; gap: 8rpx; }
.vaccine-cell-label { font-size: 24rpx; color: $text-secondary; }
.vaccine-cell-value { font-size: 36rpx; font-weight: 700; color: $text-primary; }
.text-green { color: $green; }
.text-red { color: $red; }
.vaccine-next { display: flex; align-items: center; gap: 12rpx; background: #f0f7ff; border-radius: 16rpx; padding: 18rpx 20rpx; }
.vaccine-next-icon { font-size: 32rpx; }
.vaccine-next-text { font-size: 26rpx; color: $text-primary; }

/* ===== AI 快速分析 ===== */
.ai-section { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.ai-section-title { font-size: 30rpx; font-weight: 700; color: #fff; }
.ai-card { background: rgba(255,255,255,0.12); border-radius: 16rpx; padding: 24rpx; margin-top: 8rpx; }
.ai-text { font-size: 26rpx; color: #fff; line-height: 1.8; }

/* ===== 底部按钮 ===== */
.action-section { margin-top: 8rpx; padding-bottom: 40rpx; }
.btn-group { display: flex; gap: 20rpx; }
.btn-back { flex: 1; height: 90rpx; border-radius: 45rpx; background: #f0f0f0; display: flex; align-items: center; justify-content: center; }
.btn-back-text { font-size: 28rpx; font-weight: 600; color: #666; }
.btn-back:active { background: #e5e5e5; }
.btn-reanalyze { flex: 2; height: 90rpx; border-radius: 45rpx; background: linear-gradient(135deg, #ff6a3d 0%, #ff4d4f 100%); display: flex; align-items: center; justify-content: center; box-shadow: 0 4rpx 16rpx rgba(255,106,61,0.3); }
.btn-reanalyze-text { font-size: 30rpx; font-weight: 700; color: #fff; }
.btn-reanalyze:active { opacity: 0.9; transform: scale(0.98); }
.btn-reanalyze.disabled { opacity: 0.5; pointer-events: none; }

/* ===== 空状态 ===== */
.empty-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 180rpx; }
.empty-illustration { width: 160rpx; height: 160rpx; background: #f0f2f5; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-bottom: 32rpx; }
.empty-icon { font-size: 72rpx; }
.empty-text { font-size: 32rpx; font-weight: 700; color: $text-primary; margin-bottom: 12rpx; }
.empty-sub { font-size: 26rpx; color: $text-light; margin-bottom: 48rpx; text-align: center; padding: 0 40rpx; }
.btn-analyze { width: 400rpx; height: 90rpx; border-radius: 45rpx; background: linear-gradient(135deg, #ff6a3d 0%, #ff4d4f 100%); display: flex; align-items: center; justify-content: center; box-shadow: 0 4rpx 16rpx rgba(255,106,61,0.3); }
.btn-analyze-text { font-size: 30rpx; font-weight: 700; color: #fff; }
.btn-analyze:active { opacity: 0.9; transform: scale(0.98); }
</style>
