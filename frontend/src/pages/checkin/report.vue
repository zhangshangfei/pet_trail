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
          <!-- 日期范围 -->
          <view class="date-range-bar">
            <text class="date-range-text">{{ period === 'week' ? '本周' : '本月' }}：{{ report.startDate }} ~ {{ report.endDate }}</text>
          </view>

          <!-- 完成率大圆环 -->
          <view class="rate-card">
            <view class="rate-circle" :style="{ background: getCircleStyle(report.completionRate) }">
              <view class="rate-circle-inner">
                <text class="rate-value">{{ report.completionRate }}%</text>
                <text class="rate-label">完成率</text>
              </view>
            </view>
          </view>

          <!-- 核心数据 -->
          <view class="stats-row">
            <view class="stat-box">
              <text class="stat-box-value" style="color: #ff6a3d;">{{ report.checkinDays }}</text>
              <text class="stat-box-label">打卡天数</text>
            </view>
            <view class="stat-box">
              <text class="stat-box-value">{{ report.totalDays }}</text>
              <text class="stat-box-label">总天数</text>
            </view>
            <view class="stat-box">
              <text class="stat-box-value" style="color: #ff6a3d;">{{ report.totalCheckins }}</text>
              <text class="stat-box-label">累计打卡</text>
            </view>
          </view>

          <!-- 连续打卡 -->
          <view class="streak-row">
            <view class="streak-box">
              <text class="streak-box-icon">🔥</text>
              <text class="streak-box-value">{{ report.currentStreak }}天</text>
              <text class="streak-box-label">当前连续</text>
            </view>
            <view class="streak-box">
              <text class="streak-box-icon">🏆</text>
              <text class="streak-box-value">{{ report.maxStreak }}天</text>
              <text class="streak-box-label">最长连续</text>
            </view>
          </view>

          <!-- 打卡项统计 -->
          <view v-if="report.itemStats && report.itemStats.length" class="items-section">
            <text class="items-section-title">各打卡项统计</text>
            <view class="items-list">
              <view v-for="item in report.itemStats" :key="item.itemId" class="item-row">
                <view class="item-row-left">
                  <text class="item-row-icon">{{ item.itemIcon || '📋' }}</text>
                  <text class="item-row-name">{{ item.itemName }}</text>
                </view>
                <view class="item-row-right">
                  <text class="item-row-count">{{ item.totalCount }}次</text>
                  <view class="item-row-bar-bg">
                    <view class="item-row-bar-fill" :style="{ width: getItemPercent(item.totalCount) + '%' }"></view>
                  </view>
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
        this.report.startDate = this.formatMD(monthStart)
        this.report.endDate = this.formatMD(now)
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
      return `conic-gradient(#ff6a3d 0deg, #ff8f6b ${deg}deg, #f0f0f0 ${deg}deg, #f0f0f0 360deg)`
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.report-page { min-height: 100vh; background: #f5f5f5; }

.status-bar { width: 100%; background: #fff; }
.nav-bar {
  height: 88rpx; display: flex; align-items: center;
  justify-content: space-between; padding: 0 28rpx;
  background: #fff; border-bottom: 1rpx solid #f0f0f0;
}
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow {
  width: 20rpx; height: 20rpx; border-left: 4rpx solid #1a1a1a;
  border-bottom: 4rpx solid #1a1a1a; transform: rotate(45deg);
}
.nav-title { font-size: 32rpx; font-weight: 700; color: #1a1a1a; }
.nav-placeholder { width: 60rpx; }

.report-scroll { position: fixed; left: 0; right: 0; bottom: 0; }
.report-content { padding: 24rpx; }

.period-tabs {
  display: flex; background: #fff; border-radius: 24rpx;
  padding: 6rpx; margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.period-tab {
  flex: 1; display: flex; align-items: center; justify-content: center;
  padding: 16rpx 0; border-radius: 20rpx; transition: all 0.3s;
}
.period-tab.active {
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
  box-shadow: 0 4rpx 12rpx rgba(255,106,61,0.3);
}
.period-tab-text { font-size: 28rpx; font-weight: 600; color: #666; }
.period-tab.active .period-tab-text { color: #fff; }

.date-range-bar {
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
  border-radius: 24rpx;
  padding: 24rpx 32rpx;
  margin-bottom: 24rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(255,106,61,0.25);
}
.date-range-text { font-size: 30rpx; font-weight: 700; color: #fff; letter-spacing: 1rpx; }

.rate-card {
  display: flex; justify-content: center;
  background: #fff; border-radius: 24rpx;
  padding: 40rpx 0;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.rate-circle {
  width: 240rpx; height: 240rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  padding: 16rpx;
}
.rate-circle-inner {
  width: 208rpx; height: 208rpx; border-radius: 50%;
  background: #fff;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
.rate-value { font-size: 56rpx; font-weight: 800; color: #ff6a3d; }
.rate-label { font-size: 24rpx; color: #999; margin-top: 4rpx; }

.stats-row {
  display: flex; gap: 16rpx;
  margin-bottom: 24rpx;
}
.stat-box {
  flex: 1;
  background: #fff; border-radius: 24rpx;
  padding: 28rpx 0;
  display: flex; flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.stat-box-value { font-size: 44rpx; font-weight: 700; color: #1a1a1a; margin-bottom: 8rpx; }
.stat-box-label { font-size: 24rpx; color: #999; }

.streak-row {
  display: flex; gap: 16rpx;
  margin-bottom: 24rpx;
}
.streak-box {
  flex: 1;
  background: #fff; border-radius: 24rpx;
  padding: 28rpx 0;
  display: flex; flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.streak-box-icon { font-size: 48rpx; margin-bottom: 8rpx; }
.streak-box-value { font-size: 36rpx; font-weight: 700; color: #1a1a1a; margin-bottom: 4rpx; }
.streak-box-label { font-size: 24rpx; color: #999; }

.items-section {
  background: #fff; border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.items-section-title { font-size: 30rpx; font-weight: 700; color: #1a1a1a; margin-bottom: 24rpx; display: block; }
.items-list { display: flex; flex-direction: column; gap: 20rpx; }
.item-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.item-row:last-child { border-bottom: none; }
.item-row-left { display: flex; align-items: center; gap: 16rpx; }
.item-row-icon { font-size: 40rpx; }
.item-row-name { font-size: 28rpx; color: #333; }
.item-row-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8rpx; }
.item-row-count { font-size: 28rpx; font-weight: 600; color: #ff6a3d; }
.item-row-bar-bg { width: 160rpx; height: 10rpx; background: #f0f0f0; border-radius: 5rpx; overflow: hidden; }
.item-row-bar-fill { height: 100%; background: linear-gradient(90deg, #ff6a3d, #ff8f6b); border-radius: 5rpx; transition: width 0.3s ease; }

.loading-state { display: flex; justify-content: center; padding: 100rpx 0; }
.loading-text { font-size: 28rpx; color: #999; }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 100rpx 0;
}
.empty-emoji { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999; }

.page-bottom-safe { height: calc(24rpx + env(safe-area-inset-bottom)); }
</style>
