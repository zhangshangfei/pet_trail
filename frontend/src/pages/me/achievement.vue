<template>
  <view class="achievement-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">&#8249;</text>
        </view>
        <text class="header-title">成就墙</text>
        <view class="header-right">
          <text class="progress-text">{{ unlockedCount }}/{{ totalCount }}</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="achievement-scroll" :style="{ top: headerHeight + 'px' }">
      <view class="achievement-content">
        <!-- 总进度条 -->
        <view class="progress-card">
          <view class="progress-info">
            <text class="progress-label">成就进度</text>
            <text class="progress-percent">{{ progressPercent }}%</text>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
          </view>
          <text class="progress-sub">已解锁 {{ unlockedCount }} / {{ totalCount }} 个成就</text>
        </view>

        <!-- 类型筛选 -->
        <view class="type-tabs">
          <view
            v-for="tab in typeTabs"
            :key="tab.value"
            class="type-tab"
            :class="{ active: activeType === tab.value }"
            @tap="switchType(tab.value)"
          >
            <text class="tab-text">{{ tab.label }}</text>
          </view>
        </view>

        <!-- 成就网格 -->
        <view class="achievement-grid" v-if="filteredAchievements.length > 0">
          <view
            v-for="item in filteredAchievements"
            :key="item.id"
            class="achievement-card"
            :class="{ unlocked: item.unlocked, claimable: item.status === 2, locked: !item.unlocked }"
            @tap="onAchievementTap(item)"
          >
            <view class="card-icon-wrap" :class="{ 'icon-glow': item.unlocked }">
              <text class="card-icon">{{ item.unlocked ? (item.icon || '&#127942;') : '&#128274;' }}</text>
            </view>
            <text class="card-name">{{ item.name }}</text>
            <text class="card-desc">{{ item.description }}</text>
            <view class="card-progress" v-if="!item.unlocked">
              <view class="mini-bar">
                <view class="mini-fill" :style="{ width: getProgressPercent(item) + '%' }"></view>
              </view>
              <text class="mini-text">{{ item.currentProgress || 0 }}/{{ item.conditionValue }}</text>
            </view>
            <view class="card-badge" v-if="item.status === 3">
              <text class="badge-check">&#9989;</text>
            </view>
            <view class="claim-tag" v-if="item.status === 2">
              <text class="claim-tag-text">领取</text>
            </view>
          </view>
        </view>

        <view class="empty-tip" v-if="filteredAchievements.length === 0">
          <view class="empty-icon">&#127942;</view>
          <text class="empty-text">暂无该类型成就</text>
        </view>
      </view>
    </scroll-view>

  </view>
</template>

<script>
import * as achievementApi from '@/api/achievement'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 74,
      achievements: [],
      unlockedCount: 0,
      totalCount: 0,
      activeType: 0,
      typeTabs: [
        { label: '全部', value: 0 },
        { label: '打卡', value: 1 },
        { label: '健康', value: 2 },
        { label: '社交', value: 3 },
        { label: '成长', value: 4 }
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
    } catch (e) {
      this.statusBarHeight = 20
    }
    this.headerHeight = this.statusBarHeight + 54
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
      uni.navigateTo({ url: `/pages/achievement/detail?id=${item.id}` })
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.achievement-page {
  min-height: 100vh;
  background: #f9fafb;
}

/* 头部 - 与首页 UserTopBar 一致 */
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
  padding: 6rpx 20rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
}

/* 滚动区域 */
.achievement-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}

.achievement-content {
  padding: 20rpx;
}

/* 进度卡片 - 与首页 post-card 一致 */
.progress-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.progress-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.progress-percent {
  font-size: 28rpx;
  font-weight: 700;
  color: #ff6a3d;
}

.progress-bar {
  height: 16rpx;
  background: #f3f4f6;
  border-radius: 999rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
  transition: width 0.5s ease;
}

.progress-sub {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 类型筛选 - 与首页 segmented-control 一致 */
.type-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
  background: rgba(243, 244, 246, 0.6);
  border-radius: 999rpx;
  padding: 6rpx;
}

.type-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12rpx 0;
  border-radius: 999rpx;
}

.type-tab.active {
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.tab-text {
  font-size: 26rpx;
  color: #6b7280;
}

.type-tab.active .tab-text {
  color: #111827;
  font-weight: 700;
}

/* 成就网格 */
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.achievement-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.achievement-card.locked {
  opacity: 0.65;
}

.achievement-card.unlocked {
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 100%);
  border: 1rpx solid rgba(255, 122, 61, 0.15);
}

.achievement-card.claimable {
  border: 2rpx solid #ff6a3d;
}

.card-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.card-icon-wrap.icon-glow {
  background: linear-gradient(135deg, rgba(255, 122, 61, 0.2), rgba(255, 77, 79, 0.15));
}

.card-icon {
  font-size: 44rpx;
}

.card-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6rpx;
  text-align: center;
}

.card-desc {
  font-size: 22rpx;
  color: #6b7280;
  text-align: center;
  margin-bottom: 12rpx;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-progress {
  width: 100%;
}

.mini-bar {
  height: 6rpx;
  background: #f3f4f6;
  border-radius: 999rpx;
  overflow: hidden;
  margin-bottom: 6rpx;
}

.mini-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
  transition: width 0.3s ease;
}

.mini-text {
  font-size: 20rpx;
  color: #9ca3af;
  text-align: center;
  display: block;
}

.card-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
}

.badge-check {
  font-size: 28rpx;
}

.claim-tag {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 4rpx 16rpx;
  background: linear-gradient(135deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
}

.claim-tag-text {
  font-size: 20rpx;
  color: #ffffff;
  font-weight: 600;
}

/* 空状态 */
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #9ca3af;
}
</style>
