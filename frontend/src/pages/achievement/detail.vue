<template>
  <view class="detail-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">&#8249;</text>
        </view>
        <text class="header-title">成就详情</text>
        <view class="header-right"></view>
      </view>
    </view>

    <scroll-view scroll-y class="detail-scroll" :style="{ top: headerHeight + 'px' }">
      <view class="detail-content" v-if="achievement">
        <!-- 成就图标区 -->
        <view class="hero-section">
          <view class="hero-icon-wrap" :class="{ unlocked: achievement.unlocked }">
            <text class="hero-icon">{{ achievement.unlocked ? (achievement.icon || '&#127942;') : '&#128274;' }}</text>
          </view>
          <text class="hero-name">{{ achievement.name }}</text>
          <view class="hero-type-badge">
            <text class="hero-type-text">{{ achievement.typeName }}</text>
          </view>
        </view>

        <!-- 进度卡片 -->
        <view class="info-card">
          <view class="info-row">
            <view class="info-item">
              <text class="info-value">{{ achievement.currentProgress || 0 }}</text>
              <text class="info-label">当前进度</text>
            </view>
            <view class="info-divider"></view>
            <view class="info-item">
              <text class="info-value">{{ achievement.conditionValue || 0 }}</text>
              <text class="info-label">目标值</text>
            </view>
            <view class="info-divider"></view>
            <view class="info-item">
              <text class="info-value">{{ progressPercent }}%</text>
              <text class="info-label">完成度</text>
            </view>
          </view>
          <view class="progress-bar-wrap">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
            </view>
          </view>
        </view>

        <!-- 描述 -->
        <view class="section-card" v-if="achievement.description">
          <text class="section-title">成就说明</text>
          <text class="desc-text">{{ achievement.description }}</text>
        </view>

        <!-- 解锁条件 -->
        <view class="section-card">
          <text class="section-title">解锁条件</text>
          <view class="condition-row">
            <text class="condition-label">类型</text>
            <text class="condition-value">{{ getConditionName(achievement.conditionType) }}</text>
          </view>
          <view class="condition-row">
            <text class="condition-label">目标</text>
            <text class="condition-value">{{ achievement.conditionValue }} {{ getConditionUnit(achievement.conditionType) }}</text>
          </view>
        </view>

        <!-- 状态信息 -->
        <view class="section-card" v-if="achievement.unlocked">
          <text class="section-title">状态</text>
          <view class="status-row">
            <text class="status-label">解锁状态</text>
            <text class="status-value unlocked-text" v-if="achievement.status === 3">&#9989; 已领取</text>
            <text class="status-value claimable-text" v-else-if="achievement.status === 2">&#127873; 可领取</text>
            <text class="status-value" v-else>&#127942; 已解锁</text>
          </view>
          <view class="status-row" v-if="achievement.unlockedAt">
            <text class="status-label">解锁时间</text>
            <text class="status-value">{{ formatTime(achievement.unlockedAt) }}</text>
          </view>
        </view>

        <view class="bottom-spacer"></view>
      </view>

      <view class="loading-wrap" v-if="loading">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>

    <!-- 底部领取按钮 -->
    <view class="bottom-bar" v-if="achievement && achievement.status === 2">
      <view class="claim-btn" @tap="onClaim">
        <text class="claim-btn-text">领取奖励</text>
      </view>
    </view>
  </view>
</template>

<script>
import * as achievementApi from '@/api/achievement'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 74,
      achievementId: null,
      achievement: null,
      loading: false
    }
  },
  computed: {
    progressPercent() {
      if (!this.achievement || !this.achievement.conditionValue) return 0
      return Math.min(100, Math.round(((this.achievement.currentProgress || 0) / this.achievement.conditionValue) * 100))
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    this.headerHeight = this.statusBarHeight + 54

    if (options.id) {
      this.achievementId = options.id
    }
  },
  onShow() {
    if (this.achievementId) {
      this.loadDetail()
    }
  },
  methods: {
    async loadDetail() {
      this.loading = true
      try {
        const res = await achievementApi.getAchievementDetail(this.achievementId)
        if (res && res.success) {
          this.achievement = res.data
        }
      } catch (e) {
        console.error('加载成就详情失败:', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    async onClaim() {
      try {
        const res = await achievementApi.claimAchievement(this.achievementId)
        if (res && res.success) {
          uni.showToast({ title: '领取成功！', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }
      } catch (e) {
        uni.showToast({ title: e.message || '领取失败', icon: 'none' })
      }
    },
    goBack() {
      uni.navigateBack()
    },
    getConditionName(type) {
      const names = {
        checkin_count: '累计打卡',
        checkin_streak: '连续打卡',
        health_record_count: '健康记录',
        post_count: '发布动态',
        like_received: '收到点赞',
        follower_count: '粉丝数',
        pet_count: '宠物数',
        comment_count: '评论数'
      }
      return names[type] || type
    },
    getConditionUnit(type) {
      const units = {
        checkin_count: '次',
        checkin_streak: '天',
        health_record_count: '条',
        post_count: '篇',
        like_received: '个',
        follower_count: '人',
        pet_count: '只',
        comment_count: '条'
      }
      return units[type] || ''
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const d = new Date(timestamp)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f9fafb;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 54px;
  padding: 0 24rpx;
}

.back-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 40rpx;
  color: #ffffff;
  font-weight: 300;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.header-right {
  width: 72rpx;
}

.detail-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}

.detail-content {
  padding: 0 0 32rpx 0;
}

/* 成就图标区 */
.hero-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0 32rpx;
}

.hero-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.hero-icon-wrap.unlocked {
  background: linear-gradient(135deg, rgba(255, 122, 61, 0.2), rgba(255, 77, 79, 0.15));
  box-shadow: 0 8rpx 32rpx rgba(255, 106, 61, 0.2);
}

.hero-icon {
  font-size: 80rpx;
}

.hero-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
  margin-bottom: 12rpx;
}

.hero-type-badge {
  padding: 6rpx 24rpx;
  background: #fff0ea;
  border-radius: 999rpx;
}

.hero-type-text {
  font-size: 24rpx;
  color: #ff6a3d;
  font-weight: 500;
}

/* 信息统计卡 */
.info-card {
  margin: 0 20rpx 20rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 20rpx;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.info-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
}

.info-label {
  font-size: 24rpx;
  color: #9ca3af;
}

.info-divider {
  width: 1rpx;
  height: 80rpx;
  background: #f3f4f6;
}

.progress-bar-wrap {
  margin-top: 8rpx;
}

.progress-bar {
  height: 16rpx;
  background: #f3f4f6;
  border-radius: 999rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
  transition: width 0.5s ease;
}

/* 通用卡片 */
.section-card {
  margin: 20rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16rpx;
  display: block;
}

.desc-text {
  font-size: 28rpx;
  color: #374151;
  line-height: 1.7;
}

/* 条件行 */
.condition-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.condition-row:last-child {
  border-bottom: none;
}

.condition-label {
  font-size: 28rpx;
  color: #6b7280;
}

.condition-value {
  font-size: 28rpx;
  color: #111827;
  font-weight: 500;
}

/* 状态行 */
.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.status-row:last-child {
  border-bottom: none;
}

.status-label {
  font-size: 28rpx;
  color: #6b7280;
}

.status-value {
  font-size: 28rpx;
  color: #111827;
  font-weight: 500;
}

.unlocked-text {
  color: #52c41a;
}

.claimable-text {
  color: #ff6a3d;
}

/* 底部间距 */
.bottom-spacer {
  height: 160rpx;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.claim-btn {
  padding: 24rpx 0;
  background: linear-gradient(135deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
  text-align: center;
}

.claim-btn-text {
  font-size: 30rpx;
  color: #ffffff;
  font-weight: 600;
}

/* 加载中 */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #f3f4f6;
  border-top-color: #ff6a3d;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 20rpx;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 26rpx;
  color: #9ca3af;
}
</style>
