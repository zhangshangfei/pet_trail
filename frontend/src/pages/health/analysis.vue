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
    <view class="back-btn" :style="{ top: (statusBarHeight + 12) + 'px' }" @tap="goBack">
      <text class="back-btn-icon">‹</text>
    </view>

    <scroll-view scroll-y class="analysis-scroll" :style="{ paddingTop: (statusBarHeight + 54) + 'px' }">
      <view class="analysis-content">
        <view v-if="loading" class="loading-wrap">
          <view class="glass-loading-spinner"></view>
          <text class="loading-text">正在分析健康数据...</text>
        </view>

        <template v-else-if="analysis">
          <!-- 顶部评分区域 - 玻璃拟态 -->
          <view class="score-card glass-score-card">
            <view class="score-left">
              <view class="score-ring-wrap">
                <view class="score-ring-bg"></view>
                <view class="score-ring-progress" :style="getRingStyle(analysis.score)"></view>
                <view class="score-ring-inner glass-score-inner">
                  <text class="score-number">{{ formatScore(analysis.score) }}</text>
                  <text class="score-label">健康评分</text>
                </view>
              </view>
            </view>
            <view class="score-right">
              <view class="level-badge" :class="levelClass">{{ analysis.level }}</view>
              <text class="score-desc">基于疫苗、驱虫、体重等多维度综合评估</text>
            </view>
          </view>

          <!-- 分项评分 - 玻璃卡片 -->
          <view class="detail-section glass-section-card">
            <view class="section-header">
              <text class="section-emoji">📊</text>
              <text class="section-title">分项评分</text>
            </view>
            <view class="detail-list">
              <view class="detail-row glass-detail-row">
                <view class="detail-label">
                  <text class="detail-icon vaccine-icon">💉</text>
                  <text class="detail-name">疫苗</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg glass-bar-bg">
                    <view class="detail-bar-fill vaccine" :style="{ width: (analysis.detail.vaccineScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ formatScore(analysis.detail.vaccineScore || 0) }}</text>
              </view>
              <view class="detail-row glass-detail-row">
                <view class="detail-label">
                  <text class="detail-icon parasite-icon">🐛</text>
                  <text class="detail-name">驱虫</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg glass-bar-bg">
                    <view class="detail-bar-fill parasite" :style="{ width: (analysis.detail.parasiteScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ formatScore(analysis.detail.parasiteScore || 0) }}</text>
              </view>
              <view class="detail-row glass-detail-row">
                <view class="detail-label">
                  <text class="detail-icon weight-icon">⚖️</text>
                  <text class="detail-name">体重</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg glass-bar-bg">
                    <view class="detail-bar-fill weight" :style="{ width: (analysis.detail.weightScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ formatScore(analysis.detail.weightScore || 0) }}</text>
              </view>
              <view class="detail-row glass-detail-row">
                <view class="detail-label">
                  <text class="detail-icon checkin-icon">✅</text>
                  <text class="detail-name">打卡</text>
                </view>
                <view class="detail-bar-wrap">
                  <view class="detail-bar-bg glass-bar-bg">
                    <view class="detail-bar-fill checkin" :style="{ width: (analysis.detail.checkinScore || 0) + '%' }"></view>
                  </view>
                </view>
                <text class="detail-score">{{ formatScore(analysis.detail.checkinScore || 0) }}</text>
              </view>
            </view>
          </view>

          <!-- 健康趋势 - 玻璃卡片 -->
          <view v-if="analysis.trends && Object.keys(analysis.trends).length" class="trends-section glass-section-card">
            <view class="section-header">
              <text class="section-emoji">📈</text>
              <text class="section-title">健康趋势</text>
            </view>
            <view class="trends-list">
              <view v-for="(value, key) in analysis.trends" :key="key" class="trend-tag glass-trend-tag">
                <text class="trend-key">{{ trendLabels[key] || key }}</text>
                <text class="trend-value" :class="trendClass(value)">{{ value }}</text>
              </view>
            </view>
          </view>

          <!-- 健康预警 - 玻璃卡片 -->
          <view v-if="analysis.warnings && analysis.warnings.length" class="warnings-section glass-section-card">
            <view class="section-header">
              <text class="section-emoji">⚠️</text>
              <text class="section-title">健康预警</text>
            </view>
            <view class="warnings-list">
              <view v-for="(w, i) in analysis.warnings" :key="i" class="warning-item glass-warning-item" :class="'severity-' + w.severity">
                <view class="warning-icon-wrap glass-warning-icon">
                  <text class="warning-icon">{{ w.severity === 'high' ? '!' : w.severity === 'medium' ? '⚠' : 'ℹ' }}</text>
                </view>
                <text class="warning-msg">{{ w.message }}</text>
              </view>
            </view>
          </view>

          <!-- 健康建议 - 玻璃卡片 -->
          <view v-if="analysis.suggestions && analysis.suggestions.length" class="suggestions-section glass-section-card">
            <view class="section-header">
              <text class="section-emoji">💡</text>
              <text class="section-title">健康建议</text>
            </view>
            <view class="suggestions-list">
              <view v-for="(s, i) in analysis.suggestions" :key="i" class="suggestion-item glass-suggestion-item">
                <view class="suggestion-dot"></view>
                <text class="suggestion-text">{{ s }}</text>
              </view>
            </view>
          </view>

          <!-- 体重分析 - 玻璃网格 -->
          <view v-if="analysis.detail && analysis.detail.weightAnalysis" class="weight-section glass-section-card">
            <view class="section-header">
              <text class="section-emoji">⚖️</text>
              <text class="section-title">体重分析</text>
            </view>
            <view class="weight-grid">
              <view class="weight-cell glass-weight-cell">
                <text class="weight-cell-label">当前体重</text>
                <text class="weight-cell-value">{{ analysis.detail.weightAnalysis.currentWeight || '-' }} <text class="weight-unit">kg</text></text>
              </view>
              <view class="weight-cell glass-weight-cell">
                <text class="weight-cell-label">30日均值</text>
                <text class="weight-cell-value">{{ analysis.detail.weightAnalysis.avgWeight30d || '-' }} <text class="weight-unit">kg</text></text>
              </view>
              <view class="weight-cell glass-weight-cell">
                <text class="weight-cell-label">波动率</text>
                <text class="weight-cell-value">{{ analysis.detail.weightAnalysis.weightChangeRate || '-' }}%</text>
              </view>
              <view class="weight-cell glass-weight-cell">
                <text class="weight-cell-label">趋势</text>
                <text class="weight-cell-value" :class="trendClass(analysis.detail.weightAnalysis.trend)">{{ analysis.detail.weightAnalysis.trend }}</text>
              </view>
            </view>
          </view>

          <!-- 疫苗分析 - 玻璃网格 -->
          <view v-if="analysis.detail && analysis.detail.vaccineAnalysis" class="vaccine-section glass-section-card">
            <view class="section-header">
              <text class="section-emoji">💉</text>
              <text class="section-title">疫苗分析</text>
            </view>
            <view class="vaccine-grid">
              <view class="vaccine-cell glass-vaccine-cell">
                <text class="vaccine-cell-label">总记录</text>
                <text class="vaccine-cell-value">{{ analysis.detail.vaccineAnalysis.total || 0 }}</text>
              </view>
              <view class="vaccine-cell glass-vaccine-cell">
                <text class="vaccine-cell-label">已完成</text>
                <text class="vaccine-cell-value text-green">{{ analysis.detail.vaccineAnalysis.completed || 0 }}</text>
              </view>
              <view class="vaccine-cell glass-vaccine-cell">
                <text class="vaccine-cell-label">已逾期</text>
                <text class="vaccine-cell-value" :class="analysis.detail.vaccineAnalysis.overdue > 0 ? 'text-red' : ''">{{ analysis.detail.vaccineAnalysis.overdue || 0 }}</text>
              </view>
            </view>
            <view v-if="analysis.detail.vaccineAnalysis.nextVaccineName" class="vaccine-next glass-vaccine-next">
              <view class="vaccine-next-icon">📅</view>
              <text class="vaccine-next-text">下次接种: {{ analysis.detail.vaccineAnalysis.nextVaccineName }} ({{ analysis.detail.vaccineAnalysis.nextVaccineDate }})</text>
            </view>
          </view>

          <!-- AI 快速分析 - 增强玻璃渐变 -->
          <view v-if="analysis.aiAnalysis" class="ai-section glass-ai-section">
            <view class="section-header">
              <text class="ai-section-title">🤖 大模型快速分析</text>
              <view class="ai-generated-badge glass-ai-badge">
                <text class="ai-generated-icon">✨</text>
                <text class="ai-generated-text">AI生成</text>
              </view>
            </view>
            <view class="ai-card glass-ai-card">
              <text class="ai-text">{{ analysis.aiAnalysis }}</text>
              <view class="ai-disclaimer">
                <text class="ai-disclaimer-text">以上内容由AI大模型生成，仅供参考，不构成专业医疗建议。如有疑问请咨询宠物医生。</text>
              </view>
            </view>
          </view>

          <!-- 底部按钮 - 玻璃操作栏 -->
          <view class="action-section">
            <view class="btn-group">
              <view class="btn-back glass-btn-secondary" @tap="goBack">
                <text class="btn-back-text">返回</text>
              </view>
              <view class="btn-reanalyze glass-btn-primary" @tap="runAnalysis" :class="{ disabled: loading }">
                <text class="btn-reanalyze-text">重新分析</text>
              </view>
            </view>
          </view>
        </template>

        <view v-else class="empty-wrap">
          <view class="empty-illustration glass-empty-illustration">
            <text class="empty-icon">🏥</text>
          </view>
          <text class="empty-text">暂无健康分析数据</text>
          <text class="empty-sub">点击下方按钮开始为您的宠物进行健康分析</text>
          <view class="btn-analyze glass-btn-primary-large" @tap="runAnalysis">
            <text class="btn-analyze-text">开始分析</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getHealthAnalysis } from '@/api/health'
import { getPetList } from '@/api/pet'
import { useUserStore } from '@/store/user'
import UserTopBar from '@/components/UserTopBar.vue'

const userStore = useUserStore()

function formatScore(val) {
  if (val == null) return '0'
  const num = Number(val)
  if (Number.isInteger(num)) return String(num)
  return num.toFixed(1)
}
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

const getRingColor = (score) => {
  if (score >= 90) return '#34c759'
  if (score >= 75) return '#4a90d9'
  if (score >= 60) return '#ff9500'
  return '#ff3b30'
}

const getRingStyle = (score) => {
  const pct = Math.max(0, Math.min(100, score || 0))
  const color = getRingColor(score)
  return {
    background: `conic-gradient(${color} 0deg, ${color} ${pct * 3.6}deg, #e8e8e8 ${pct * 3.6}deg, #e8e8e8 360deg)`
  }
}

const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/dashboard/index' })
  }
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

onLoad((options) => {
  if (options && options.petId) {
    currentPetId.value = Number(options.petId)
    uni.setStorageSync('currentPetId', currentPetId.value)
  }
})

onMounted(async () => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 44

  if (userStore.isLoggedIn && !userStore.userInfo) {
    await userStore.loadUserInfo()
  }

  if (!currentPetId.value) {
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
  }

  if (currentPetId.value) {
    await runAnalysis()
  }

  uni.$on('loginSuccess', async () => {
    await userStore.loadUserInfo()
    if (!currentPetId.value) {
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
})

onUnmounted(() => {
  uni.$off('loginSuccess')
})
</script>

<style lang="scss" scoped>
/* ============================================
   健康详情页 - 玻璃拟态风格 (Glassmorphism)
   保持原逻辑不变，全面升级视觉体验
   ============================================ */

.analysis-page {
  min-height: 100vh;
  background: transparent;
}

.back-btn {
  position: fixed;
  left: 24rpx;
  z-index: 40;
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.back-btn:active {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(0.92);
}

.back-btn-icon {
  font-size: 44rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}

/* ====== 滚动区域 ====== */
.analysis-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.analysis-content {
  padding: 20rpx 24rpx 160rpx;
}

/* ====== 加载状态 ====== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
  animation: fadeInUp 0.5s ease-out both;
}

.glass-loading-spinner {
  width: 72rpx;
  height: 72rpx;
  border: 5rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: var(--pt-primary, #ff6a3d);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  backdrop-filter: blur(8px);
  box-shadow:
    0 8rpx 24rpx rgba(31, 38, 135, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.loading-text {
  margin-top: 28rpx;
  font-size: 28rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 500;
}

/* ====== 顶部评分卡片 - 玻璃拟态 ====== */
.glass-score-card {
  position: relative;
  background: rgba(255, 255, 255, 0.88);
  border-radius: 32rpx;
  padding: 36rpx 32rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  gap: 28rpx;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.65);
  box-shadow:
    0 10rpx 40rpx rgba(31, 38, 135, 0.12),
    0 4rpx 16rpx rgba(0, 0, 0, 0.04),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.95),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.03);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.95), transparent);
    pointer-events: none;
  }

  &:hover,
  &:active {
    transform: translateY(-4rpx);
    box-shadow:
      0 18rpx 56rpx rgba(31, 38, 135, 0.18),
      inset 0 1rpx 0 rgba(255, 255, 255, 1);
  }
}

.score-left {
  flex-shrink: 0;
}

.score-ring-wrap {
  width: 190rpx;
  height: 190rpx;
  border-radius: 50%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.score-ring-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #f0f0f0;
  box-shadow:
    inset 0 2rpx 8rpx rgba(0, 0, 0, 0.08),
    inset 0 -1rpx 0 rgba(255, 255, 255, 0.95);
}

.score-ring-progress {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  filter: drop-shadow(0 2rpx 6rpx rgba(0, 0, 0, 0.15));
}

.glass-score-inner {
  position: relative;
  z-index: 2;
  width: 158rpx;
  height: 158rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(20px);
  box-shadow:
    0 4rpx 20rpx rgba(0, 0, 0, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 1),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.05);
}

.score-number {
  font-size: 58rpx;
  font-weight: 800;
  color: var(--pt-text, #1a1a2e);
  line-height: 1;
  letter-spacing: -1rpx;
}

.score-label {
  font-size: 23rpx;
  color: var(--pt-secondary, #6b7280);
  margin-top: 8rpx;
  font-weight: 500;
}

.score-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.level-badge {
  align-self: flex-start;
  font-size: 27rpx;
  font-weight: 800;
  padding: 10rpx 32rpx;
  border-radius: 28rpx;
  color: #fff;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.15);
}

.level-excellent {
  background: linear-gradient(135deg, #34c759 0%, #2ecc71 100%);
}
.level-good {
  background: linear-gradient(135deg, #4a90d9 0%, #5b9bd5 100%);
}
.level-fair {
  background: linear-gradient(135deg, #ff9500 0%, #ffaa33 100%);
}
.level-poor {
  background: linear-gradient(135deg, #ff3b30 0%, #ff5544 100%);
}

.score-desc {
  font-size: 25rpx;
  color: var(--pt-muted, #9ca3af);
  line-height: 1.55;
  font-weight: 500;
}

/* ====== 通用区块卡片 - 玻璃拟态 ====== */
.glass-section-card {
  position: relative;
  background: rgba(255, 255, 255, 0.84);
  border-radius: 28rpx;
  padding: 30rpx 28rpx;
  margin-bottom: 22rpx;
  backdrop-filter: blur(22px);
  border: 1rpx solid rgba(255, 255, 255, 0.62);
  box-shadow:
    0 8rpx 32rpx rgba(31, 38, 135, 0.08),
    0 2rpx 8rpx rgba(0, 0, 0, 0.03),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.02);
  animation: sectionSlideIn 0.45s ease-out both;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.88), transparent);
    pointer-events: none;
  }
}

@keyframes sectionSlideIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.section-emoji {
  font-size: 38rpx;
  line-height: 1;
}

.section-title {
  font-size: 31rpx;
  font-weight: 700;
  color: var(--pt-text, #1a1a2e);
  letter-spacing: 0.5rpx;
}

/* ====== 分项评分 - 玻璃增强 ====== */
.detail-list {
  display: flex;
  flex-direction: column;
  gap: 26rpx;
}

.glass-detail-row {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 16rpx 18rpx;
  border-radius: 18rpx;
  background: rgba(249, 250, 251, 0.35);
  transition: all 0.3s ease;

  &:hover,
  &:active {
    background: rgba(255, 255, 255, 0.65);
    transform: translateX(6rpx);
  }
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 110rpx;
}

.detail-icon {
  font-size: 34rpx;
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-name {
  font-size: 27rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
}

.detail-bar-wrap {
  flex: 1;
}

.glass-bar-bg {
  height: 16rpx;
  background: rgba(240, 240, 245, 0.7);
  border-radius: 8rpx;
  overflow: hidden;
  backdrop-filter: blur(4px);
  box-shadow: inset 0 1rpx 3rpx rgba(0, 0, 0, 0.06);
}

.detail-bar-fill {
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.8s ease;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 50%;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.4), transparent);
    border-radius: 8rpx 8rpx 0 0;
  }
}

.detail-bar-fill.vaccine {
  background: linear-gradient(90deg, #4a90d9, #6ba3e0);
}
.detail-bar-fill.parasite {
  background: linear-gradient(90deg, #34c759, #5dd87a);
}
.detail-bar-fill.weight {
  background: linear-gradient(90deg, #ff9500, #ffb347);
}
.detail-bar-fill.checkin {
  background: linear-gradient(90deg, #ff6a3d, #ff8a5d);
}

.detail-score {
  font-size: 29rpx;
  font-weight: 800;
  color: var(--pt-text, #1a1a2e);
  min-width: 65rpx;
  text-align: right;
  letter-spacing: -0.5rpx;
}

/* ====== 健康趋势 - 玻璃标签 ====== */
.trends-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.glass-trend-tag {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: rgba(249, 250, 251, 0.65);
  padding: 14rpx 24rpx;
  border-radius: 16rpx;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
  transition: all 0.3s ease;

  &:hover,
  &:active {
    background: rgba(255, 255, 255, 0.88);
    transform: scale(1.03);
    box-shadow: 0 4rpx 14rpx rgba(31, 38, 135, 0.1);
  }
}

.trend-key {
  font-size: 25rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
}

.trend-value {
  font-size: 25rpx;
  font-weight: 700;
}

.trend-stable,
.trend-done,
.trend-active {
  color: #34c759;
}
.trend-progress {
  color: #ff9500;
}
.trend-up,
.trend-attention,
.trend-inactive {
  color: #ff3b30;
}
.trend-down {
  color: #4a90d9;
}
.trend-nodata {
  color: var(--pt-muted, #9ca3af);
}

/* ====== 健康预警 - 玻璃警告卡片 ====== */
.warnings-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.glass-warning-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  backdrop-filter: blur(12px);
  border: 1rpx solid rgba(255, 255, 255, 0.45);
  transition: all 0.3s ease;

  &:hover,
  &:active {
    transform: translateX(6rpx);
    box-shadow: 0 6rpx 20rpx rgba(31, 38, 135, 0.1);
  }
}

.severity-high {
  background: linear-gradient(135deg, rgba(255, 204, 199, 0.75) 0%, rgba(255, 235, 230, 0.65) 100%);
}
.severity-medium {
  background: linear-gradient(135deg, rgba(255, 229, 143, 0.75) 0%, rgba(255, 251, 230, 0.65) 100%);
}
.severity-low {
  background: linear-gradient(135deg, rgba(217, 247, 190, 0.75) 0%, rgba(246, 255, 237, 0.65) 100%);
}

.glass-warning-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(8px);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.severity-high .glass-warning-icon {
  background: rgba(255, 140, 120, 0.85);
}
.severity-medium .glass-warning-icon {
  background: rgba(255, 200, 80, 0.85);
}
.severity-low .glass-warning-icon {
  background: rgba(150, 220, 130, 0.85);
}

.warning-icon {
  font-size: 26rpx;
  font-weight: 800;
  color: var(--pt-text, #1a1a2e);
}

.warning-msg {
  font-size: 27rpx;
  color: var(--pt-text, #1a1a2e);
  line-height: 1.55;
  flex: 1;
  font-weight: 500;
}

/* ====== 健康建议 - 玻璃列表 ====== */
.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.glass-suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 8rpx 4rpx;
  transition: all 0.3s ease;

  &:hover,
  &:active {
    transform: translateX(6rpx);
  }
}

.suggestion-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: var(--pt-primary, #ff6a3d);
  flex-shrink: 0;
  margin-top: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(255, 106, 61, 0.35);
}

.suggestion-text {
  font-size: 27rpx;
  color: var(--pt-text, #1a1a2e);
  line-height: 1.65;
  flex: 1;
  font-weight: 500;
}

/* ====== 体重分析 - 玻璃网格 ====== */
.weight-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
}

.glass-weight-cell {
  background: rgba(248, 249, 251, 0.65);
  border-radius: 20rpx;
  padding: 24rpx 20rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  backdrop-filter: blur(10px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover,
  &:active {
    background: rgba(255, 255, 255, 0.82);
    transform: translateY(-4rpx) scale(1.02);
    box-shadow: 0 8rpx 24rpx rgba(31, 38, 135, 0.12);
  }
}

.weight-cell-label {
  font-size: 25rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
}

.weight-cell-value {
  font-size: 33rpx;
  font-weight: 800;
  color: var(--pt-text, #1a1a2e);
  letter-spacing: -0.5rpx;
}

.weight-unit {
  font-size: 23rpx;
  color: var(--pt-muted, #9ca3af);
  font-weight: 400;
}

/* ====== 疫苗分析 - 玻璃网格 ====== */
.vaccine-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 18rpx;
  margin-bottom: 18rpx;
}

.glass-vaccine-cell {
  background: rgba(248, 249, 251, 0.65);
  border-radius: 20rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  backdrop-filter: blur(10px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover,
  &:active {
    background: rgba(255, 255, 255, 0.82);
    transform: translateY(-4rpx) scale(1.02);
    box-shadow: 0 8rpx 24rpx rgba(31, 38, 135, 0.12);
  }
}

.vaccine-cell-label {
  font-size: 25rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
}

.vaccine-cell-value {
  font-size: 37rpx;
  font-weight: 800;
  color: var(--pt-text, #1a1a2e);
}

.text-green {
  color: #34c759;
}
.text-red {
  color: #ff3b30;
}

.glass-vaccine-next {
  display: flex;
  align-items: center;
  gap: 14rpx;
  background: linear-gradient(135deg, rgba(74, 144, 217, 0.09) 0%, rgba(102, 126, 234, 0.07) 100%);
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(74, 144, 217, 0.15);
  transition: all 0.3s ease;

  &:hover,
  &:active {
    background: linear-gradient(135deg, rgba(74, 144, 217, 0.13) 0%, rgba(102, 126, 234, 0.11) 100%);
    transform: translateX(4rpx);
  }
}

.vaccine-next-icon {
  font-size: 34rpx;
}

.vaccine-next-text {
  font-size: 27rpx;
  color: var(--pt-text, #1a1a2e);
  font-weight: 500;
}

/* ====== AI 快速分析 - 增强玻璃渐变 ====== */
.glass-ai-section {
  position: relative;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.92) 0%, rgba(118, 75, 162, 0.92) 100%);
  border-radius: 28rpx;
  padding: 30rpx 28rpx;
  margin-bottom: 22rpx;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.25);
  box-shadow:
    0 10rpx 40rpx rgba(102, 126, 234, 0.3),
    0 4rpx 16rpx rgba(118, 75, 162, 0.2),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.15);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle at center, rgba(255, 255, 255, 0.08), transparent 70%);
    pointer-events: none;
    animation: aiGlow 8s ease-in-out infinite alternate;
  }
}

@keyframes aiGlow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.glass-ai-section .section-header {
  justify-content: space-between;
}

.ai-section-title {
  font-size: 31rpx;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.glass-ai-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: rgba(255, 255, 255, 0.22);
  border: 1rpx solid rgba(255, 255, 255, 0.45);
  border-radius: 22rpx;
  padding: 8rpx 20rpx;
  backdrop-filter: blur(8px);
}

.ai-generated-icon {
  font-size: 24rpx;
}

.ai-generated-text {
  font-size: 23rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 1rpx;
}

.glass-ai-card {
  background: rgba(255, 255, 255, 0.14);
  border-radius: 20rpx;
  padding: 26rpx;
  margin-top: 12rpx;
  backdrop-filter: blur(12px);
  border: 1rpx solid rgba(255, 255, 255, 0.18);
  position: relative;
  z-index: 1;
}

.ai-text {
  font-size: 27rpx;
  color: #ffffff;
  line-height: 1.85;
  font-weight: 500;
}

.ai-disclaimer {
  margin-top: 22rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.22);
}

.ai-disclaimer-text {
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.6;
  font-weight: 400;
}

/* ====== 底部按钮 - 玻璃操作栏 ====== */
.action-section {
  margin-top: 12rpx;
  padding-bottom: 40rpx;
}

.btn-group {
  display: flex;
  gap: 20rpx;
}

.glass-btn-secondary {
  flex: 1;
  height: 96rpx;
  border-radius: 48rpx;
  background: rgba(249, 250, 251, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(12px);
  border: 1rpx solid rgba(209, 213, 219, 0.4);
  box-shadow:
    0 4rpx 16rpx rgba(0, 0, 0, 0.06),
    inset 0 1rpx 0 rgba(255, 255, 255, 1);
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.97);
    background: rgba(233, 234, 236, 0.95);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }
}

.btn-back-text {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-secondary, #666666);
}

.glass-btn-primary {
  flex: 2;
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 10rpx 32rpx rgba(255, 90, 61, 0.4),
    0 4rpx 12rpx rgba(255, 90, 61, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 0 rgba(180, 50, 20, 0.2);
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
    transition: left 0.55s ease;
  }

  &:active {
    opacity: 0.95;
    transform: scale(0.97);
    box-shadow:
      0 6rpx 20rpx rgba(255, 90, 61, 0.35),
      inset 0 4rpx 12rpx rgba(140, 30, 10, 0.2);
  }

  &:active::after {
    left: 100%;
  }
}

.btn-reanalyze-text {
  font-size: 31rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 3rpx;
  text-shadow:
    0 2rpx 8rpx rgba(180, 30, 10, 0.35),
    0 1rpx 2rpx rgba(0, 0, 0, 0.15);
}

.btn-reanalyze.disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* ====== 空状态 - 玻璃增强 ====== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 180rpx;
  animation: fadeInUp 0.6s ease-out both;
}

.glass-empty-illustration {
  width: 170rpx;
  height: 170rpx;
  background: linear-gradient(135deg, rgba(255, 122, 61, 0.1) 0%, rgba(255, 77, 79, 0.08) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 36rpx;
  backdrop-filter: blur(12px);
  border: 1rpx solid rgba(255, 122, 61, 0.15);
  box-shadow:
    0 8rpx 24rpx rgba(31, 38, 135, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);
}

.empty-icon {
  font-size: 76rpx;
}

.empty-text {
  font-size: 33rpx;
  font-weight: 700;
  color: var(--pt-text, #1a1a2e);
  margin-bottom: 14rpx;
}

.empty-sub {
  font-size: 27rpx;
  color: var(--pt-muted, #9ca3af);
  margin-bottom: 52rpx;
  text-align: center;
  padding: 0 40rpx;
  line-height: 1.6;
  font-weight: 500;
}

.glass-btn-primary-large {
  width: 420rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 10rpx 32rpx rgba(255, 90, 61, 0.4),
    0 4rpx 12rpx rgba(255, 90, 61, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 0 rgba(180, 50, 20, 0.2);
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
    transition: left 0.55s ease;
  }

  &:active {
    opacity: 0.95;
    transform: scale(0.97);
  }

  &:active::after {
    left: 100%;
  }
}

.btn-analyze-text {
  font-size: 31rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 3rpx;
  text-shadow:
    0 2rpx 8rpx rgba(180, 30, 10, 0.35),
    0 1rpx 2rpx rgba(0, 0, 0, 0.15);
}

/* ====== 暗色模式适配 ====== */
page.dark .glass-score-card {
  background: rgba(40, 40, 55, 0.86);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 10rpx 40rpx rgba(0, 0, 0, 0.35),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.12);
}

page.dark .glass-score-inner {
  background: rgba(50, 50, 65, 0.92);
}

page.dark .score-number {
  color: #e5e5e5;
}

page.dark .score-desc {
  color: #aaaaaa;
}

page.dark .glass-section-card {
  background: rgba(40, 40, 55, 0.78);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 8rpx 32rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-detail-row {
  background: rgba(255, 255, 255, 0.04);
}

page.dark .glass-detail-row:hover,
page.dark .glass-detail-row:active {
  background: rgba(255, 255, 255, 0.08);
}

page.dark .glass-bar-bg {
  background: rgba(255, 255, 255, 0.06);
}

page.dark .detail-name {
  color: #aaaaaa;
}

page.dark .detail-score {
  color: #e5e5e5;
}

page.dark .section-title {
  color: #e5e5e5;
}

page.dark .glass-trend-tag {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-trend-tag:hover,
page.dark .glass-trend-tag:active {
  background: rgba(255, 255, 255, 0.12);
}

page.dark .trend-key {
  color: #aaaaaa;
}

page.dark .glass-warning-item {
  border-color: rgba(255, 255, 255, 0.08);
}

page.dark .severity-high {
  background: linear-gradient(135deg, rgba(255, 80, 60, 0.25) 0%, rgba(255, 100, 80, 0.18) 100%);
}
page.dark .severity-medium {
  background: linear-gradient(135deg, rgba(255, 180, 60, 0.25) 0%, rgba(255, 200, 80, 0.18) 100%);
}
page.dark .severity-low {
  background: linear-gradient(135deg, rgba(80, 180, 100, 0.25) 0%, rgba(100, 200, 120, 0.18) 100%);
}

page.dark .warning-msg {
  color: #e5e5e5;
}

page.dark .suggestion-text {
  color: #e5e5e5;
}

page.dark .glass-weight-cell,
page.dark .glass-vaccine-cell {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
}

page.dark .glass-weight-cell:hover,
page.dark .glass-weight-cell:active,
page.dark .glass-vaccine-cell:hover,
page.dark .glass-vaccine-cell:active {
  background: rgba(255, 255, 255, 0.08);
}

page.dark .weight-cell-label,
page.dark .vaccine-cell-label {
  color: #aaaaaa;
}

page.dark .weight-cell-value,
page.dark .vaccine-cell-value {
  color: #e5e5e5;
}

page.dark .glass-vaccine-next {
  background: linear-gradient(135deg, rgba(74, 144, 217, 0.12) 0%, rgba(102, 126, 234, 0.08) 100%);
  border-color: rgba(74, 144, 217, 0.2);
}

page.dark .vaccine-next-text {
  color: #e5e5e5;
}

page.dark .glass-ai-section {
  background: linear-gradient(135deg, rgba(80, 100, 180, 0.88) 0%, rgba(90, 60, 130, 0.88) 100%);
  border-color: rgba(255, 255, 255, 0.15);
}

page.dark .glass-ai-card {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
}

page.dark .glass-btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .btn-back-text {
  color: #aaaaaa;
}

page.dark .empty-text {
  color: #e5e5e5;
}

page.dark .empty-sub {
  color: #888888;
}

page.dark .glass-empty-illustration {
  background: linear-gradient(135deg, rgba(255, 122, 61, 0.08) 0%, rgba(255, 77, 79, 0.06) 100%);
  border-color: rgba(255, 122, 61, 0.12);
}
</style>
