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
          <view class="summary-card">
            <view class="summary-header">
              <text class="summary-title">{{ period === 'week' ? '本周' : '本月' }}概览</text>
              <text class="summary-date">{{ report.startDate }} ~ {{ report.endDate }}</text>
            </view>
            <view class="summary-grid">
              <view class="summary-item">
                <text class="summary-value">{{ report.checkinDays }}</text>
                <text class="summary-label">打卡天数</text>
              </view>
              <view class="summary-divider"></view>
              <view class="summary-item">
                <text class="summary-value">{{ report.totalDays }}</text>
                <text class="summary-label">总天数</text>
              </view>
              <view class="summary-divider"></view>
              <view class="summary-item">
                <text class="summary-value highlight">{{ report.completionRate }}%</text>
                <text class="summary-label">完成率</text>
              </view>
            </view>
          </view>

          <view class="streak-card">
            <view class="streak-row">
              <view class="streak-item">
                <text class="streak-icon">🔥</text>
                <view class="streak-info">
                  <text class="streak-value">{{ report.currentStreak }}天</text>
                  <text class="streak-label">当前连续</text>
                </view>
              </view>
              <view class="streak-divider"></view>
              <view class="streak-item">
                <text class="streak-icon">🏆</text>
                <view class="streak-info">
                  <text class="streak-value">{{ report.maxStreak }}天</text>
                  <text class="streak-label">最长连续</text>
                </view>
              </view>
            </view>
          </view>

          <view class="progress-card">
            <text class="card-title">完成率</text>
            <view class="progress-bar-wrap">
              <view class="progress-bar-bg">
                <view class="progress-bar-fill" :style="{ width: report.completionRate + '%' }"></view>
              </view>
              <text class="progress-percent">{{ report.completionRate }}%</text>
            </view>
          </view>

          <view v-if="report.itemStats && report.itemStats.length" class="items-card">
            <text class="card-title">各打卡项统计</text>
            <view class="item-list">
              <view v-for="item in report.itemStats" :key="item.itemId" class="item-row">
                <text class="item-icon">{{ item.itemIcon || '📋' }}</text>
                <text class="item-name">{{ item.itemName }}</text>
                <view class="item-bar-wrap">
                  <view class="item-bar-bg">
                    <view class="item-bar-fill" :style="{ width: getItemPercent(item.totalCount) + '%' }"></view>
                  </view>
                </view>
                <text class="item-count">{{ item.totalCount }}次</text>
              </view>
            </view>
          </view>

          <view class="total-card">
            <text class="total-label">累计打卡总数</text>
            <text class="total-value">{{ report.totalCheckins }}</text>
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
  padding: 6rpx; margin-bottom: 24rpx;
}
.period-tab {
  flex: 1; display: flex; align-items: center; justify-content: center;
  padding: 16rpx 0; border-radius: 20rpx; transition: all 0.3s;
}
.period-tab.active { background: $primary; }
.period-tab-text { font-size: 28rpx; font-weight: 600; color: $text-secondary; }
.period-tab.active .period-tab-text { color: #fff; }

.summary-card {
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
}
.summary-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24rpx;
}
.summary-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.summary-date { font-size: 24rpx; color: $text-light; }
.summary-grid { display: flex; align-items: center; }
.summary-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.summary-value { font-size: 40rpx; font-weight: 700; color: $text-primary; margin-bottom: 4rpx; }
.summary-value.highlight { color: $primary; }
.summary-label { font-size: 22rpx; color: $text-secondary; }
.summary-divider { width: 1rpx; height: 48rpx; background: #e5e7eb; }

.streak-card {
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
}
.streak-row { display: flex; align-items: center; }
.streak-item { flex: 1; display: flex; align-items: center; gap: 16rpx; justify-content: center; }
.streak-icon { font-size: 48rpx; }
.streak-info { display: flex; flex-direction: column; }
.streak-value { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.streak-label { font-size: 22rpx; color: $text-secondary; }
.streak-divider { width: 1rpx; height: 48rpx; background: #e5e7eb; }

.progress-card {
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
}
.card-title { font-size: 28rpx; font-weight: 700; color: $text-primary; margin-bottom: 20rpx; display: block; }
.progress-bar-wrap { display: flex; align-items: center; gap: 16rpx; }
.progress-bar-bg { flex: 1; height: 20rpx; background: #f0f0f0; border-radius: 10rpx; overflow: hidden; }
.progress-bar-fill { height: 100%; background: linear-gradient(90deg, $primary, #ff8a5c); border-radius: 10rpx; transition: width 0.5s ease; }
.progress-percent { font-size: 28rpx; font-weight: 700; color: $primary; min-width: 80rpx; text-align: right; }

.items-card {
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
}
.item-list { display: flex; flex-direction: column; gap: 20rpx; }
.item-row { display: flex; align-items: center; gap: 16rpx; }
.item-icon { font-size: 32rpx; }
.item-name { font-size: 26rpx; color: $text-secondary; min-width: 100rpx; }
.item-bar-wrap { flex: 1; }
.item-bar-bg { height: 12rpx; background: #f0f0f0; border-radius: 6rpx; overflow: hidden; }
.item-bar-fill { height: 100%; background: $primary; border-radius: 6rpx; transition: width 0.3s ease; }
.item-count { font-size: 24rpx; color: $text-light; min-width: 80rpx; text-align: right; }

.total-card {
  background: $card-bg; border-radius: $radius;
  padding: 28rpx; margin-bottom: 24rpx;
  display: flex; justify-content: space-between; align-items: center;
}
.total-label { font-size: 28rpx; color: $text-secondary; }
.total-value { font-size: 40rpx; font-weight: 700; color: $primary; }

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
