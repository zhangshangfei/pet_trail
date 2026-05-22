<template>
  <view class="report-page">
    <!-- 导航栏 -->
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">打卡报表</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: navHeight + 'px', paddingBottom: '120rpx' }">
      <view class="page-content">

        <!-- 时间周期切换 - iOS Segmented Control -->
        <view class="period-switcher">
          <view class="switcher-track">
            <view
              class="switcher-indicator"
              :class="{ 'indicator-right': period === 'month' }"
            ></view>
            <view
              class="switcher-option"
              :class="{ active: period === 'week' }"
              @click="switchPeriod('week')"
            >
              <text class="option-text">周报</text>
            </view>
            <view
              class="switcher-option"
              :class="{ active: period === 'month' }"
              @click="switchPeriod('month')"
            >
              <text class="option-text">月报</text>
            </view>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading-card">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 报表内容 -->
        <template v-else-if="report">

          <!-- 日期范围卡片 - 轻量信息展示 -->
          <view class="date-range-card">
            <view class="date-header">
              <text class="date-label">{{ period === 'week' ? '本周' : '本月' }}报表</text>
              <view class="date-badge">
                <text class="badge-icon">📅</text>
                <text class="badge-text">{{ period === 'week' ? '7天' : '30天' }}</text>
              </view>
            </view>
            <view class="date-body">
              <text class="date-start">{{ report.startDate }}</text>
              <view class="date-separator">
                <view class="separator-line"></view>
                <text class="separator-text">至</text>
                <view class="separator-line"></view>
              </view>
              <text class="date-end">{{ report.endDate }}</text>
            </view>
          </view>

          <!-- 完成率圆环卡片 -->
          <view class="rate-card">
            <view class="card-header">
              <text class="card-title">完成率</text>
              <text class="card-subtitle">Completion Rate</text>
            </view>
            <view class="rate-circle-wrapper">
              <view class="rate-circle" :style="{ background: getCircleStyle(report.completionRate) }">
                <view class="rate-circle-inner">
                  <text class="rate-value">{{ report.completionRate }}<text class="rate-unit">%</text></text>
                  <text class="rate-label">总体完成</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 核心统计数据 -->
          <view class="stats-grid">
            <view class="stat-card">
              <view class="stat-icon-wrap stat-icon-primary">
                <text class="stat-icon">✓</text>
              </view>
              <text class="stat-value stat-value-primary">{{ report.checkinDays }}</text>
              <text class="stat-label">打卡天数</text>
            </view>
            <view class="stat-card">
              <view class="stat-icon-wrap stat-icon-secondary">
                <text class="stat-icon">📅</text>
              </view>
              <text class="stat-value">{{ report.totalDays }}</text>
              <text class="stat-label">总天数</text>
            </view>
            <view class="stat-card">
              <view class="stat-icon-wrap stat-icon-primary">
                <text class="stat-icon">⚡</text>
              </view>
              <text class="stat-value stat-value-primary">{{ report.totalCheckins }}</text>
              <text class="stat-label">累计打卡</text>
            </view>
          </view>

          <!-- 连续打卡成就 -->
          <view class="streak-section">
            <view class="section-header">
              <text class="section-title">连续打卡</text>
              <text class="section-subtitle">Streak Records</text>
            </view>
            <view class="streak-grid">
              <view class="streak-card streak-fire">
                <view class="streak-emoji">🔥</view>
                <text class="streak-value">{{ report.currentStreak }}<text class="streak-unit">天</text></text>
                <text class="streak-label">当前连续</text>
              </view>
              <view class="streak-card streak-trophy">
                <view class="streak-emoji">🏆</view>
                <text class="streak-value">{{ report.maxStreak }}<text class="streak-unit">天</text></text>
                <text class="streak-label">最长连续</text>
              </view>
            </view>
          </view>

          <!-- 打卡项详细统计 -->
          <view v-if="report.itemStats && report.itemStats.length" class="items-section">
            <view class="section-header">
              <text class="section-title">各打卡项统计</text>
              <text class="section-subtitle">Item Details</text>
            </view>
            <view class="items-list">
              <view
                v-for="(item, index) in report.itemStats"
                :key="item.itemId"
                class="item-row"
                :style="{ animationDelay: index * 50 + 'ms' }"
              >
                <view class="item-left">
                  <view class="item-icon-box">
                    <text class="item-icon">{{ item.itemIcon || '📋' }}</text>
                  </view>
                  <view class="item-info">
                    <text class="item-name">{{ item.itemName }}</text>
                    <text class="item-hint">共完成 {{ item.totalCount }} 次</text>
                  </view>
                </view>
                <view class="item-right">
                  <text class="item-percent">{{ getItemPercent(item.totalCount) }}%</text>
                  <view class="progress-bar">
                    <view class="progress-fill" :style="{ width: getItemPercent(item.totalCount) + '%' }"></view>
                  </view>
                </view>
              </view>
            </view>
          </view>

        </template>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <view class="empty-icon-wrap">
            <text class="empty-icon">📊</text>
          </view>
          <text class="empty-title">暂无报表数据</text>
          <text class="empty-desc">开始打卡后这里会显示你的统计数据</text>
        </view>

      </view>
    </scroll-view>
  </view>
</template>

<script>
import * as checkinApi from '@/api/checkin'

export default {
  data() {
    return {
      statusBarHeight: 20,
      navHeight: 64,
      period: 'week',
      report: null,
      loading: false
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      this.navHeight = this.statusBarHeight + 44
    } catch (e) {
      this.navHeight = 64
    }
    this.loadReport()
  },
  methods: {
    async loadReport() {
      this.loading = true
      try {
        const res = await checkinApi.getCheckinReport(this.period)
        if (res && res.success && res.data) {
          this.report = res.data
          if (!this.report.startDate || !this.report.endDate) {
            this.fillDateRange()
          }
        }
      } catch (e) {
        console.error('加载报表失败:', e)
      } finally {
        this.loading = false
      }
    },
    fillDateRange() {
      const now = new Date()
      if (this.period === 'week') {
        const dayOfWeek = now.getDay() || 7
        const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - dayOfWeek + 1)
        const weekEnd = new Date(weekStart.getTime() + 6 * 86400000)
        this.report.startDate = this.formatMD(weekStart)
        this.report.endDate = this.formatMD(weekEnd)
      } else {
        const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
        const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0)
        this.report.startDate = this.formatMD(monthStart)
        this.report.endDate = this.formatMD(monthEnd)
      }
    },
    formatMD(date) {
      return `${date.getMonth() + 1}月${date.getDate()}日`
    },
    switchPeriod(p) {
      if (this.period === p) return
      this.period = p
      this.loadReport()
    },
    getItemPercent(count) {
      if (!this.report || !this.report.totalDays) return 0
      return Math.min(100, Math.round((count / this.report.totalDays) * 100))
    },
    getCircleStyle(rate) {
      const deg = Math.round(rate * 3.6)
      return `conic-gradient(var(--color-primary) 0deg, var(--color-primary-light) ${deg}deg, var(--color-border) ${deg}deg, var(--color-border) 360deg)`
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   打卡报表页面 - 专业UI设计系统 v2.0
   设计原则：数据可视化清晰、层次分明、高可读性
   ============================================ */

/* ====== CSS变量系统 ====== */
.report-page {
  --color-primary: #ff6b35;
  --color-primary-light: #ff8c5a;
  --color-primary-dark: #e55a2b;
  --color-success: #10b981;
  --color-warning: #f59e0b;
  --color-surface: #ffffff;
  --color-surface-elevated: #fafafa;
  --color-background: #f5f5f7;
  --color-text-primary: #1d1d1f;
  --color-text-secondary: #6e6e73;
  --color-text-tertiary: #aeaeb2;
  --color-border: rgba(0, 0, 0, 0.06);
  --color-border-strong: rgba(0, 0, 0, 0.12);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.03);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.08), 0 4px 8px rgba(0, 0, 0, 0.04);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.12), 0 8px 16px rgba(0, 0, 0, 0.06);
  --radius-sm: 12rpx;
  --radius-md: 20rpx;
  --radius-lg: 28rpx;
  --radius-xl: 36rpx;
  --transition-fast: 150ms cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 250ms cubic-bezier(0.4, 0, 0.2, 1);
  --transition-spring: 400ms cubic-bezier(0.34, 1.56, 0.64, 1);

  min-height: 100vh;
  background: var(--color-background);
}

/* ====== 导航栏 ====== */
.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
}

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.nav-back {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);

  &:active {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(0.92);
  }
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg);
  margin-left: 4rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5rpx;
}

.nav-placeholder {
  width: 72rpx;
}

/* ====== 滚动容器 ====== */
.page-scroll {
  height: 100vh;
}

.page-content {
  padding: 28rpx 28rpx;
}

/* ====== 时间周期切换器 - iOS Segmented Control (活力版) ====== */
.period-switcher {
  margin-bottom: 28rpx;
  animation: slideUp 0.5s var(--transition-spring) both;
}

.switcher-track {
  display: flex;
  position: relative;
  background: linear-gradient(135deg, #fff5f0 0%, #fef0e8 100%);
  border-radius: 12rpx;
  padding: 4rpx;
  box-shadow:
    0 2rpx 12rpx rgba(255, 107, 53, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.8);
}

.switcher-indicator {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: calc(50% - 4rpx);
  height: calc(100% - 8rpx);
  background: linear-gradient(135deg, #ffffff 0%, #fffaf5 100%);
  border-radius: 10rpx;
  box-shadow:
    0 4rpx 16rpx rgba(255, 107, 53, 0.15),
    0 2rpx 6rpx rgba(255, 107, 53, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 3rpx;
    background: linear-gradient(90deg,
      transparent 0%,
      var(--color-primary) 30%,
      var(--color-primary-light) 50%,
      var(--color-primary) 70%,
      transparent 100%
    );
    border-radius: 0 0 4rpx 4rpx;
    opacity: 0.8;
  }

  &.indicator-right {
    transform: translateX(100%);
  }
}

.switcher-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22rpx 0;
  position: relative;
  z-index: 1;
  transition: all var(--transition-fast);

  &:active {
    opacity: 0.7;
  }

  &.active .option-text {
    color: var(--color-primary);
    font-weight: 800;
    text-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.15);
  }
}

.option-text {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

/* ====== 加载状态 ====== */
.loading-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  animation: slideUp 0.5s var(--transition-spring);
}

.loading-spinner {
  width: 64rpx;
  height: 64rpx;
  border: 4rpx solid var(--color-border-strong);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 24rpx;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 28rpx;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* ====== 日期范围卡片 - 活力精致版 ====== */
.date-range-card {
  background: linear-gradient(135deg, #fff9f5 0%, #fffaf7 50%, #fef9f5 100%);
  border-radius: var(--radius-xl);
  padding: 28rpx 32rpx;
  margin-bottom: 28rpx;
  box-shadow:
    var(--shadow-md),
    0 4rpx 20rpx rgba(255, 107, 53, 0.06);
  border: 1.5rpx solid rgba(255, 107, 53, 0.12);
  position: relative;
  overflow: hidden;
  animation: slideUp 0.5s var(--transition-spring) 0.05s both;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    width: 6rpx;
    background: linear-gradient(180deg,
      var(--color-primary) 0%,
      var(--color-primary-light) 50%,
      var(--color-primary) 100%
    );
    box-shadow: 2rpx 0 12rpx rgba(255, 107, 53, 0.25);
  }
}

.date-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  padding-left: 16rpx;
}

.date-label {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--color-text-primary);
}

.date-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 22rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, rgba(255, 140, 90, 0.1) 100%);
  border-radius: 999rpx;
  border: 1.5rpx solid rgba(255, 107, 53, 0.25);
  box-shadow:
    0 4rpx 12rpx rgba(255, 107, 53, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.6);
}

.badge-icon {
  font-size: 26rpx;
}

.badge-text {
  font-size: 24rpx;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 0.3rpx;
}

.date-body {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
  padding-left: 16rpx;
}

.date-start,
.date-end {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--color-primary);
  letter-spacing: 0.5rpx;
  text-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.1);
}

.date-separator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 0 14rpx;
}

.separator-line {
  width: 48rpx;
  height: 2.5rpx;
  background: linear-gradient(90deg,
    transparent,
    rgba(255, 107, 53, 0.3),
    transparent
  );
  border-radius: 2rpx;
}

.separator-text {
  font-size: 23rpx;
  color: var(--color-primary);
  font-weight: 700;
  opacity: 0.7;
  white-space: nowrap;
}

/* ====== 完成率圆环卡片 ====== */
.rate-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 36rpx 28rpx;
  margin-bottom: 28rpx;
  box-shadow: var(--shadow-lg);
  border: 1rpx solid var(--color-border);
  animation: slideUp 0.5s var(--transition-spring) 0.1s both;
}

.card-header {
  text-align: center;
  margin-bottom: 28rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--color-text-primary);
  display: block;
  margin-bottom: 4rpx;
}

.card-subtitle {
  font-size: 23rpx;
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.rate-circle-wrapper {
  display: flex;
  justify-content: center;
}

.rate-circle {
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18rpx;
  box-shadow:
    0 8rpx 32rpx rgba(255, 107, 53, 0.15),
    inset 0 4rpx 8rpx rgba(255, 255, 255, 0.5);
  transition: transform var(--transition-normal);

  &:active {
    transform: scale(0.97);
  }
}

.rate-circle-inner {
  width: 244rpx;
  height: 244rpx;
  border-radius: 50%;
  background: var(--color-surface);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow:
    inset 0 2rpx 8rpx rgba(0, 0, 0, 0.04),
    0 1px 0 rgba(255, 255, 255, 0.8);
}

.rate-value {
  font-size: 64rpx;
  font-weight: 800;
  color: var(--color-primary);
  line-height: 1;
}

.rate-unit {
  font-size: 36rpx;
  font-weight: 600;
  opacity: 0.8;
}

.rate-label {
  font-size: 24rpx;
  color: var(--color-text-secondary);
  margin-top: 8rpx;
  font-weight: 600;
}

/* ====== 核心统计数据网格 ====== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  margin-bottom: 28rpx;
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 28rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: var(--shadow-md);
  border: 1rpx solid var(--color-border);
  transition: all var(--transition-normal);
  animation: slideUp 0.5s var(--transition-spring) 0.15s both;

  &:active {
    transform: scale(0.96);
    box-shadow: var(--shadow-sm);
  }
}

.stat-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
}

.stat-icon-primary {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
}

.stat-icon-secondary {
  background: linear-gradient(135deg, rgba(110, 110, 115, 0.1) 0%, rgba(110, 110, 115, 0.05) 100%);
}

.stat-icon {
  font-size: 28rpx;
  font-weight: 700;
}

.stat-value {
  font-size: 40rpx;
  font-weight: 800;
  color: var(--color-text-primary);
  line-height: 1;
  margin-bottom: 8rpx;
}

.stat-value-primary {
  color: var(--color-primary);
}

.stat-label {
  font-size: 23rpx;
  color: var(--color-text-secondary);
  font-weight: 600;
  text-align: center;
}

/* ====== 连续打卡区域 ====== */
.streak-section {
  margin-bottom: 28rpx;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--color-text-primary);
}

.section-subtitle {
  font-size: 23rpx;
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5rpx;
}

.streak-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.streak-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 28rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: var(--shadow-md);
  border: 1rpx solid var(--color-border);
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
  animation: slideUp 0.5s var(--transition-spring) 0.2s both;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    opacity: 0.8;
  }

  &.streak-fire::before {
    background: linear-gradient(90deg, #fbbf24 0%, #f59e0b 100%);
  }

  &.streak-trophy::before {
    background: linear-gradient(90deg, #a78bfa 0%, #8b5cf6 100%);
  }

  &:active {
    transform: scale(0.96);
  }
}

.streak-emoji {
  font-size: 48rpx;
  margin-bottom: 12rpx;
  filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.08));
}

.streak-value {
  font-size: 42rpx;
  font-weight: 800;
  color: var(--color-text-primary);
  line-height: 1;
  margin-bottom: 6rpx;
}

.streak-unit {
  font-size: 26rpx;
  font-weight: 600;
  opacity: 0.7;
}

.streak-label {
  font-size: 24rpx;
  color: var(--color-text-secondary);
  font-weight: 600;
}

/* ====== 打卡项统计列表 ====== */
.items-section {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 32rpx 28rpx;
  box-shadow: var(--shadow-lg);
  border: 1rpx solid var(--color-border);
  animation: slideUp 0.5s var(--transition-spring) 0.25s both;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-top: 24rpx;
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid var(--color-border);
  animation: fadeInUp 0.4s var(--transition-spring) both;

  &:last-child {
    border-bottom: none;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.item-icon-box {
  width: 60rpx;
  height: 60rpx;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 107, 53, 0.04) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-icon {
  font-size: 32rpx;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-text-primary);
}

.item-hint {
  font-size: 23rpx;
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
  flex-shrink: 0;
  margin-left: 20rpx;
}

.item-percent {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--color-primary);
}

.progress-bar {
  width: 140rpx;
  height: 12rpx;
  background: var(--color-border-strong);
  border-radius: 6rpx;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  border-radius: 6rpx;
  transition: width 0.6s var(--transition-spring);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 4rpx;
    background: #fff;
    border-radius: 0 6rpx 6rpx 0;
    opacity: 0.6;
  }
}

/* ====== 空状态 ====== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
  animation: slideUp 0.5s var(--transition-spring);
}

.empty-icon-wrap {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 107, 53, 0.04) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}

.empty-icon {
  font-size: 64rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  text-align: center;
  line-height: 1.6;
  font-weight: 500;
}

/* ====== 全局动画 ====== */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ====== 暗色模式 ====== */
page.dark .report-page {
  --color-surface: #1c1c1e;
  --color-surface-elevated: #2c2c2e;
  --color-background: #000000;
  --color-text-primary: #f5f5f7;
  --color-text-secondary: #98989d;
  --color-text-tertiary: #636366;
  --color-border: rgba(255, 255, 255, 0.08);
  --color-border-strong: rgba(255, 255, 255, 0.14);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.2), 0 1px 2px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.3), 0 2px 4px rgba(0, 0, 0, 0.15);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.4), 0 4px 8px rgba(0, 0, 0, 0.2);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.5), 0 8px 16px rgba(0, 0, 0, 0.25);
  --color-primary: #ff863a;
  --color-primary-light: #ffa066;
  --color-primary-dark: #e66a2a;
}

page.dark .nav-fixed {
  background: linear-gradient(135deg, #2a2a2c 0%, #1c1c1e 100%);
}

page.dark .nav-back {
  background: rgba(255, 255, 255, 0.1);

  &:active {
    background: rgba(255, 255, 255, 0.18);
  }
}

page.dark .switcher-track {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.08) 0%, rgba(255, 134, 58, 0.04) 100%);
}

page.dark .switcher-indicator {
  background: linear-gradient(135deg, #2c2c2e 0%, #353538 100%);
  box-shadow:
    0 4rpx 16rpx rgba(0, 0, 0, 0.4),
    0 2rpx 6rpx rgba(0, 0, 0, 0.2),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.05);
}

page.dark .loading-card {
  background: var(--color-surface);
}

page.dark .loading-spinner {
  border-color: var(--color-border-strong);
  border-top-color: var(--color-primary);
}

page.dark .date-range-card {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.06) 0%, rgba(255, 134, 58, 0.03) 100%);
  border-color: rgba(255, 134, 58, 0.15);
  box-shadow:
    0 4rpx 12rpx rgba(0, 0, 0, 0.3),
    0 4rpx 20rpx rgba(255, 134, 58, 0.04);

  &::before {
    box-shadow: 2rpx 0 12rpx rgba(255, 134, 58, 0.35);
  }
}

page.dark .date-badge {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.2) 0%, rgba(255, 134, 58, 0.12) 100%);
  border-color: rgba(255, 134, 58, 0.3);
  box-shadow:
    0 4rpx 12rpx rgba(255, 134, 58, 0.15),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.08);
}

page.dark .date-start,
page.dark .date-end {
  text-shadow: 0 2rpx 8rpx rgba(255, 134, 58, 0.2);
}

page.dark .separator-line {
  background: linear-gradient(90deg,
    transparent,
    rgba(255, 134, 58, 0.35),
    transparent
  );
}

page.dark .separator-text {
  color: var(--color-primary-light);
}

page.dark .rate-card,
page.dark .items-section {
  background: var(--color-surface);
  border-color: var(--color-border);
}

page.dark .rate-circle {
  box-shadow:
    0 8rpx 32rpx rgba(255, 134, 58, 0.12),
    inset 0 4rpx 8px rgba(255, 255, 255, 0.05);
}

page.dark .rate-circle-inner {
  background: var(--color-surface);
  box-shadow: inset 0 2rpx 8px rgba(0, 0, 0, 0.2);
}

page.dark .rate-value {
  color: var(--color-primary);
}

page.dark .stat-card,
page.dark .streak-card {
  background: var(--color-surface);
  border-color: var(--color-border);
}

page.dark .stat-icon-primary {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
}

page.dark .stat-icon-secondary {
  background: linear-gradient(135deg, rgba(152, 152, 157, 0.15) 0%, rgba(152, 152, 157, 0.08) 100%);
}

page.dark .item-icon-box {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.12) 0%, rgba(255, 134, 58, 0.06) 100%);
}

page.dark .progress-bar {
  background: var(--color-border-strong);
}

page.dark .progress-fill {
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-primary-light) 100%);

  &::after {
    opacity: 0.4;
  }
}

page.dark .empty-icon-wrap {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.12) 0%, rgba(255, 134, 58, 0.06) 100%);
}

page.dark .item-row {
  border-bottom-color: var(--color-border);
}
</style>
