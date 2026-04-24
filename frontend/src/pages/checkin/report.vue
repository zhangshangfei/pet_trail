<template>
  <view class="report-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="nav-back-arrow"></view>
      </view>
      <text class="nav-title">打卡报表</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="report-scroll" :style="{ top: navHeight + 'px' }">
      <view class="report-content">
        <view class="period-tabs">
          <view class="period-tab" :class="{ active: period === 'week' }" @click="switchPeriod('week')">
            <text class="period-tab-text">周报</text>
          </view>
          <view class="period-tab" :class="{ active: period === 'month' }" @click="switchPeriod('month')">
            <text class="period-tab-text">月报</text>
          </view>
        </view>

        <view v-if="loading" class="loading-state">
          <text class="loading-text">加载中...</text>
        </view>

        <template v-else-if="report">
          <view class="date-range-card">
            <view class="date-range-icon">📅</view>
            <view class="date-range-info">
              <text class="date-range-label">{{ period === 'week' ? '本周' : '本月' }}统计</text>
              <text class="date-range-value">{{ report.startDate }} — {{ report.endDate }}</text>
            </view>
          </view>

          <view class="summary-card">
            <view class="summary-ring-wrap">
              <view class="summary-ring">
                <text class="summary-ring-value">{{ report.completionRate }}</text>
                <text class="summary-ring-unit">%</text>
              </view>
              <text class="summary-ring-label">完成率</text>
            </view>
            <view class="summary-divider-v"></view>
            <view class="summary-stats">
              <view class="summary-stat">
                <text class="stat-value primary">{{ report.checkinDays }}</text>
                <text class="stat-label">打卡天数</text>
              </view>
              <view class="summary-stat">
                <text class="stat-value">{{ report.totalDays }}</text>
                <text class="stat-label">总天数</text>
              </view>
              <view class="summary-stat">
                <text class="stat-value highlight">{{ report.totalCheckins }}</text>
                <text class="stat-label">累计打卡</text>
              </view>
            </view>
          </view>

          <view class="streak-card">
            <view class="streak-item">
              <view class="streak-icon-wrap fire">
                <text class="streak-icon">🔥</text>
              </view>
              <view class="streak-info">
                <text class="streak-value">{{ report.currentStreak }}<text class="streak-unit">天</text></text>
                <text class="streak-label">当前连续</text>
              </view>
            </view>
            <view class="streak-divider"></view>
            <view class="streak-item">
              <view class="streak-icon-wrap trophy">
                <text class="streak-icon">🏆</text>
              </view>
              <view class="streak-info">
                <text class="streak-value">{{ report.maxStreak }}<text class="streak-unit">天</text></text>
                <text class="streak-label">最长连续</text>
              </view>
            </view>
          </view>

          <view class="progress-section">
            <text class="section-title">完成率趋势</text>
            <view class="progress-visual">
              <view class="progress-bar-bg">
                <view class="progress-bar-fill" :style="{ width: report.completionRate + '%' }"></view>
              </view>
              <text class="progress-percent">{{ report.completionRate }}%</text>
            </view>
            <view class="progress-marks">
              <text class="progress-mark">0%</text>
              <text class="progress-mark">50%</text>
              <text class="progress-mark">100%</text>
            </view>
          </view>

          <view v-if="report.itemStats && report.itemStats.length" class="items-section">
            <text class="section-title">各打卡项统计</text>
            <view class="items-grid">
              <view v-for="item in report.itemStats" :key="item.itemId" class="item-card">
                <text class="item-icon">{{ item.itemIcon || '📋' }}</text>
                <text class="item-name">{{ item.itemName }}</text>
                <text class="item-count">{{ item.totalCount }}<text class="item-count-unit">次</text></text>
                <view class="item-bar-bg">
                  <view class="item-bar-fill" :style="{ width: getItemPercent(item.totalCount) + '%' }"></view>
                </view>
              </view>
            </view>
          </view>
        </template>

        <view v-else class="empty-state">
          <text class="empty-emoji">📊</text>
          <text class="empty-text">暂无报表数据</text>
        </view>

        <view class="page-bottom-safe"></view>
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
        }
      } catch (e) {
        console.error('加载报表失败:', e)
      } finally {
        this.loading = false
      }
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
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$primary-light: #fff0ea;
$primary-gradient: linear-gradient(135deg, #ff6a3d, #ff8f6b);
$green: #52c41a;
$green-light: #f0fff0;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;
$radius: 24rpx;

.report-page { min-height: 100vh; background: $bg; }

.status-bar { width: 100%; background: $card-bg; }
.nav-bar {
  height: 88rpx; display: flex; align-items: center;
  justify-content: space-between; padding: 0 28rpx;
  background: $card-bg; border-bottom: 1rpx solid #f0f0f0;
}
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow {
  width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary;
  border-bottom: 4rpx solid $text-primary; transform: rotate(45deg);
}
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }

.report-scroll { position: fixed; left: 0; right: 0; bottom: 0; }
.report-content { padding: 24rpx; }

.period-tabs {
  display: flex; background: $card-bg; border-radius: $radius;
  padding: 6rpx; margin-bottom: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.period-tab {
  flex: 1; display: flex; align-items: center; justify-content: center;
  padding: 16rpx 0; border-radius: 20rpx; transition: all 0.3s;
}
.period-tab.active { background: $primary-gradient; box-shadow: 0 4rpx 12rpx rgba(255,106,61,0.3); }
.period-tab-text { font-size: 28rpx; font-weight: 600; color: $text-secondary; }
.period-tab.active .period-tab-text { color: #fff; }

.date-range-card {
  display: flex; align-items: center; gap: 20rpx;
  background: $primary-gradient; border-radius: $radius;
  padding: 28rpx 32rpx; margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255,106,61,0.25);
}
.date-range-icon { font-size: 48rpx; }
.date-range-info { display: flex; flex-direction: column; }
.date-range-label { font-size: 24rpx; color: rgba(255,255,255,0.8); margin-bottom: 4rpx; }
.date-range-value { font-size: 32rpx; font-weight: 700; color: #fff; letter-spacing: 1rpx; }

.summary-card {
  display: flex; align-items: center;
  background: $card-bg; border-radius: $radius;
  padding: 32rpx; margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.summary-ring-wrap {
  display: flex; flex-direction: column; align-items: center;
  min-width: 160rpx;
}
.summary-ring {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  border: 8rpx solid $primary-light; display: flex;
  align-items: baseline; justify-content: center;
  background: $primary-light;
}
.summary-ring-value { font-size: 44rpx; font-weight: 800; color: $primary; }
.summary-ring-unit { font-size: 22rpx; font-weight: 600; color: $primary; margin-left: 2rpx; }
.summary-ring-label { font-size: 22rpx; color: $text-light; margin-top: 8rpx; }
.summary-divider-v { width: 1rpx; height: 120rpx; background: #e5e7eb; margin: 0 28rpx; }
.summary-stats { flex: 1; display: flex; justify-content: space-around; }
.summary-stat { display: flex; flex-direction: column; align-items: center; }
.stat-value { font-size: 40rpx; font-weight: 700; color: $text-primary; margin-bottom: 4rpx; }
.stat-value.primary { color: $primary; }
.stat-value.highlight { color: $primary; }
.stat-label { font-size: 22rpx; color: $text-light; }

.streak-card {
  display: flex; align-items: center;
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.streak-item { flex: 1; display: flex; align-items: center; gap: 16rpx; justify-content: center; }
.streak-icon-wrap {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.streak-icon-wrap.fire { background: #fff5f0; }
.streak-icon-wrap.trophy { background: #fffbe6; }
.streak-icon { font-size: 36rpx; }
.streak-info { display: flex; flex-direction: column; }
.streak-value { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.streak-unit { font-size: 22rpx; font-weight: 400; color: $text-light; margin-left: 2rpx; }
.streak-label { font-size: 22rpx; color: $text-light; }
.streak-divider { width: 1rpx; height: 72rpx; background: #e5e7eb; }

.progress-section {
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.section-title { font-size: 28rpx; font-weight: 700; color: $text-primary; margin-bottom: 24rpx; display: block; }
.progress-visual { display: flex; align-items: center; gap: 20rpx; }
.progress-bar-bg { flex: 1; height: 24rpx; background: #f0f0f0; border-radius: 12rpx; overflow: hidden; }
.progress-bar-fill { height: 100%; background: $primary-gradient; border-radius: 12rpx; transition: width 0.5s ease; }
.progress-percent { font-size: 30rpx; font-weight: 700; color: $primary; min-width: 90rpx; text-align: right; }
.progress-marks { display: flex; justify-content: space-between; margin-top: 8rpx; padding: 0 4rpx; }
.progress-mark { font-size: 20rpx; color: $text-light; }

.items-section {
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.items-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.item-card {
  width: calc(50% - 8rpx); background: #fafafa; border-radius: 16rpx;
  padding: 20rpx; display: flex; flex-direction: column; align-items: center; gap: 8rpx;
}
.item-icon { font-size: 36rpx; }
.item-name { font-size: 24rpx; color: $text-secondary; }
.item-count { font-size: 36rpx; font-weight: 700; color: $text-primary; }
.item-count-unit { font-size: 22rpx; font-weight: 400; color: $text-light; margin-left: 2rpx; }
.item-bar-bg { width: 100%; height: 8rpx; background: #e8e8e8; border-radius: 4rpx; overflow: hidden; }
.item-bar-fill { height: 100%; background: $primary-gradient; border-radius: 4rpx; transition: width 0.3s ease; }

.loading-state { display: flex; justify-content: center; padding: 100rpx 0; }
.loading-text { font-size: 28rpx; color: $text-light; }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 100rpx 0;
}
.empty-emoji { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: $text-light; }

.page-bottom-safe { height: calc(24rpx + env(safe-area-inset-bottom)); }
</style>
