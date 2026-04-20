<template>
  <view class="achievement-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="header-title">成就墙</text>
        <view class="header-right">
          <text class="progress-text">{{ unlockedCount }}/{{ totalCount }}</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="achievement-scroll" :style="{ top: headerHeight + 'px' }">
      <view class="achievement-content">
        <view class="progress-bar-wrap">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
          </view>
          <text class="progress-label">已解锁 {{ progressPercent }}%</text>
        </view>

        <view class="type-tabs">
          <view
            v-for="tab in typeTabs"
            :key="tab.value"
            class="type-tab"
            :class="{ active: activeType === tab.value }"
            @tap="switchType(tab.value)"
          >
            <text class="tab-icon">{{ tab.icon }}</text>
            <text class="tab-text">{{ tab.label }}</text>
          </view>
        </view>

        <view class="achievement-grid">
          <view
            v-for="item in filteredAchievements"
            :key="item.id"
            class="achievement-card"
            :class="{ unlocked: item.unlocked, locked: !item.unlocked }"
            @tap="onAchievementTap(item)"
          >
            <view class="achievement-icon-wrap" :class="{ 'icon-glow': item.unlocked }">
              <text class="achievement-icon">{{ item.unlocked ? item.icon : '🔒' }}</text>
            </view>
            <text class="achievement-name">{{ item.name }}</text>
            <text class="achievement-desc">{{ item.description }}</text>
            <view class="achievement-progress" v-if="!item.unlocked">
              <view class="mini-progress-bar">
                <view class="mini-progress-fill" :style="{ width: getProgressPercent(item) + '%' }"></view>
              </view>
              <text class="mini-progress-text">{{ item.currentProgress || 0 }}/{{ item.conditionValue }}</text>
            </view>
            <view class="achievement-badge" v-if="item.unlocked">
              <text class="badge-check">✓</text>
            </view>
          </view>
        </view>

        <view class="empty-tip" v-if="filteredAchievements.length === 0">
          <text class="empty-icon">🏆</text>
          <text class="empty-text">暂无该类型成就</text>
        </view>
      </view>
    </scroll-view>

    <view class="unlock-popup" v-if="showUnlockPopup" @tap="closeUnlockPopup">
      <view class="popup-content" @tap.stop>
        <view class="popup-glow"></view>
        <text class="popup-icon">{{ unlockItem.icon }}</text>
        <text class="popup-title">🎉 成就解锁！</text>
        <text class="popup-name">{{ unlockItem.name }}</text>
        <text class="popup-desc">{{ unlockItem.description }}</text>
        <view class="popup-btn" @tap="closeUnlockPopup">
          <text class="popup-btn-text">太棒了！</text>
        </view>
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
      headerHeight: 70,
      achievements: [],
      unlockedCount: 0,
      totalCount: 0,
      activeType: 0,
      showUnlockPopup: false,
      unlockItem: {},
      typeTabs: [
        { label: '全部', value: 0, icon: '🏆' },
        { label: '打卡', value: 1, icon: '✅' },
        { label: '健康', value: 2, icon: '💪' },
        { label: '社交', value: 3, icon: '📱' },
        { label: '成长', value: 4, icon: '🌱' }
      ]
    }
  },
  computed: {
    filteredAchievements() {
      if (this.activeType === 0) return this.achievements
      return this.achievements.filter(a => a.type === this.activeType)
    },
    progressPercent() {
      if (this.totalCount === 0) return 0
      return Math.round((this.unlockedCount / this.totalCount) * 100)
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      this.headerHeight = this.statusBarHeight + 50
    } catch (e) {
      this.statusBarHeight = 20
      this.headerHeight = 70
    }
    this.loadAchievements()
  },
  onShow() {
    this.loadAchievements()
  },
  methods: {
    async loadAchievements() {
      try {
        const res = await achievementApi.getAchievements()
        if (res && res.success) {
          this.achievements = res.data.achievements || []
          this.unlockedCount = res.data.unlockedCount || 0
          this.totalCount = res.data.totalCount || 0
        }
      } catch (e) {
        console.error('加载成就失败:', e)
      }
    },
    switchType(type) {
      this.activeType = type
    },
    getProgressPercent(item) {
      if (!item.conditionValue || item.conditionValue === 0) return 0
      return Math.min(100, Math.round(((item.currentProgress || 0) / item.conditionValue) * 100))
    },
    onAchievementTap(item) {
      if (item.unlocked) {
        this.unlockItem = item
        this.showUnlockPopup = true
      } else {
        uni.showToast({
          title: `还需 ${item.conditionValue - (item.currentProgress || 0)} 步解锁`,
          icon: 'none'
        })
      }
    },
    closeUnlockPopup() {
      this.showUnlockPopup = false
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.achievement-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(20px);
}

.header-inner {
  display: flex;
  align-items: center;
  height: 50px;
  padding: 0 20rpx;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 40rpx;
  color: #fff;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.header-right {
  padding: 4rpx 16rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 999rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #fbbf24;
  font-weight: 600;
}

.achievement-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}

.achievement-content {
  padding: 20rpx 24rpx 200rpx;
}

.progress-bar-wrap {
  margin-bottom: 28rpx;
}

.progress-bar {
  height: 12rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 999rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
  border-radius: 999rpx;
  transition: width 0.5s ease;
}

.progress-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 8rpx;
  display: block;
  text-align: right;
}

.type-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 28rpx;
  overflow-x: auto;
  white-space: nowrap;
  padding-bottom: 8rpx;
}

.type-tab {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.08);
  flex-shrink: 0;

  &.active {
    background: linear-gradient(135deg, #f59e0b, #fbbf24);
  }
}

.tab-icon {
  font-size: 24rpx;
}

.tab-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.type-tab.active .tab-text {
  color: #1a1a2e;
  font-weight: 600;
}

.achievement-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.achievement-card {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 20rpx;
  padding: 28rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;

  &.unlocked {
    background: rgba(251, 191, 36, 0.12);
    border: 1rpx solid rgba(251, 191, 36, 0.3);
  }

  &.locked {
    opacity: 0.7;
  }

  &:active {
    transform: scale(0.97);
  }
}

.achievement-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;

  &.icon-glow {
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.3), rgba(245, 158, 11, 0.2));
    box-shadow: 0 0 30rpx rgba(251, 191, 36, 0.3);
  }
}

.achievement-icon {
  font-size: 44rpx;
}

.achievement-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
  margin-bottom: 6rpx;
  text-align: center;
}

.achievement-desc {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
  margin-bottom: 12rpx;
  line-height: 1.4;
}

.achievement-progress {
  width: 100%;
}

.mini-progress-bar {
  height: 6rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 999rpx;
  overflow: hidden;
  margin-bottom: 6rpx;
}

.mini-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 999rpx;
  transition: width 0.3s ease;
}

.mini-progress-text {
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.4);
  text-align: center;
  display: block;
}

.achievement-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-check {
  font-size: 20rpx;
  color: #1a1a2e;
  font-weight: 700;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.empty-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.4);
}

.unlock-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.popup-content {
  width: 560rpx;
  background: linear-gradient(180deg, #1e293b, #0f172a);
  border-radius: 32rpx;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(251, 191, 36, 0.3);
}

.popup-glow {
  position: absolute;
  top: -60rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 300rpx;
  height: 300rpx;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.2), transparent);
  border-radius: 50%;
}

.popup-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
  position: relative;
  z-index: 1;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #fbbf24;
  margin-bottom: 12rpx;
  position: relative;
  z-index: 1;
}

.popup-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8rpx;
  position: relative;
  z-index: 1;
}

.popup-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 40rpx;
  text-align: center;
  position: relative;
  z-index: 1;
}

.popup-btn {
  width: 100%;
  height: 80rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;

  &:active {
    transform: scale(0.97);
  }
}

.popup-btn-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a2e;
}
</style>
