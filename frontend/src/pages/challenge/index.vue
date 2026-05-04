<template>
  <view class="challenge-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">&#8249;</text>
        </view>
        <text class="header-title">挑战赛</text>
        <view class="header-right" @tap="goMyChallenges">
          <text class="my-btn">我的挑战</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="challenge-scroll" :style="{ top: headerHeight + 'px' }" @scrolltolower="loadMore">
      <view class="challenge-content">
        <!-- 空状态 -->
        <view class="empty-tip" v-if="!loading && challenges.length === 0">
          <view class="empty-icon">&#127942;</view>
          <text class="empty-text">暂无进行中的挑战赛</text>
        </view>

        <!-- 加载中 -->
        <view class="loading-tip" v-if="loading && challenges.length === 0">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 挑战列表 -->
        <view class="challenge-list" v-if="challenges.length > 0">
          <view
            v-for="item in challenges"
            :key="item.id"
            class="challenge-card"
            @tap="goDetail(item)"
          >
            <view class="card-cover">
              <image v-if="item.coverImage" :src="item.coverImage" class="cover-img" mode="aspectFill" />
              <view v-else class="cover-placeholder">
                <text class="cover-icon">{{ getTypeIcon(item.type) }}</text>
              </view>
              <view class="card-badge" :class="'badge-type-' + item.type">
                <text class="badge-text">{{ getTypeName(item.type) }}</text>
              </view>
              <view class="card-overlay">
                <text class="overlay-title">{{ item.title }}</text>
              </view>
            </view>
            <view class="card-body">
              <text class="card-desc">{{ item.description }}</text>
              <view class="card-meta">
                <view class="meta-item">
                  <text class="meta-icon">&#128101;</text>
                  <text class="meta-text">{{ item.participantCount || 0 }}人参与</text>
                </view>
                <view class="meta-item">
                  <text class="meta-icon">&#127873;</text>
                  <text class="meta-text">{{ item.rewardDescription || '神秘奖励' }}</text>
                </view>
              </view>
              <view class="card-footer">
                <text class="time-text">{{ formatTimeRange(item) }}</text>
                <view class="join-btn" @tap.stop="onJoin(item)">
                  <text class="join-btn-text">参加挑战</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import * as challengeApi from '@/api/challenge'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 74,
      challenges: [],
      loading: false
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
    this.loadChallenges()
  },
  methods: {
    async loadChallenges() {
      this.loading = true
      try {
        const res = await challengeApi.getChallenges()
        if (res.success) {
          this.challenges = res.data || []
        }
      } catch (e) {
        console.error('加载挑战赛失败:', e)
      } finally {
        this.loading = false
      }
    },
    async onJoin(item) {
      try {
        const res = await challengeApi.joinChallenge(item.id)
        if (res.success) {
          uni.showToast({ title: '参加成功！', icon: 'success' })
          this.loadChallenges()
        }
      } catch (e) {
        uni.showToast({ title: e.message || '参加失败', icon: 'none' })
      }
    },
    goDetail(item) {
      uni.navigateTo({ url: `/pages/challenge/detail?id=${item.id}` })
    },
    goMyChallenges() {
      uni.navigateTo({ url: '/pages/challenge/my' })
    },
    goBack() {
      uni.navigateBack()
    },
    getTypeIcon(type) {
      const icons = { 1: '&#9989;', 2: '&#128241;', 3: '&#128170;' }
      return icons[type] || '&#127942;'
    },
    getTypeName(type) {
      const names = { 1: '打卡', 2: '社交', 3: '健康' }
      return names[type] || '挑战'
    },
    formatTimeRange(item) {
      if (!item.startDate || !item.endDate) return ''
      const start = new Date(item.startDate)
      const end = new Date(item.endDate)
      return `${start.getMonth() + 1}/${start.getDate()} - ${end.getMonth() + 1}/${end.getDate()}`
    }
  }
}
</script>

<style scoped>
.challenge-page {
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
  padding: 8rpx 20rpx;
}

.my-btn {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 500;
}

/* 滚动区域 */
.challenge-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}

.challenge-content {
  padding: 20rpx;
}

/* 空状态 - 与首页 empty-follow-state 风格一致 */
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 60rpx;
  text-align: center;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 30rpx;
  color: #9ca3af;
  font-weight: 500;
}

/* 加载中 */
.loading-tip {
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

/* 挑战卡片 - 与首页 post-card 风格一致 */
.challenge-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.card-cover {
  position: relative;
  height: 320rpx;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff7a3d, #ff5a3d);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 80rpx;
}

/* 类型标签 */
.card-badge {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
}

.badge-type-1 { background: rgba(255, 106, 61, 0.9); }
.badge-type-2 { background: rgba(64, 158, 255, 0.9); }
.badge-type-3 { background: rgba(82, 196, 26, 0.9); }

.badge-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}

/* 封面底部渐变遮罩 + 标题 */
.card-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 60rpx 24rpx 16rpx 24rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
}

.overlay-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
}

/* 卡片内容区 */
.card-body {
  padding: 24rpx;
}

.card-desc {
  font-size: 28rpx;
  color: #374151;
  line-height: 1.6;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 26rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #6b7280;
}

/* 卡片底部 - 与首页 post-actions 风格一致 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #f3f4f6;
}

.time-text {
  font-size: 24rpx;
  color: #9ca3af;
}

.join-btn {
  padding: 14rpx 36rpx;
  background: linear-gradient(135deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
}

.join-btn-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 500;
}
</style>
