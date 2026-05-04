<template>
  <view class="detail-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">&#8249;</text>
        </view>
        <text class="header-title">挑战详情</text>
        <view class="header-right"></view>
      </view>
    </view>

    <scroll-view scroll-y class="detail-scroll" :style="{ top: headerHeight + 'px' }">
      <view class="detail-content" v-if="challenge">
        <view class="cover-section">
          <image v-if="challenge.coverImage" :src="challenge.coverImage" class="cover-img" mode="aspectFill" />
          <view v-else class="cover-placeholder">
            <text class="cover-icon">{{ getTypeIcon(challenge.type) }}</text>
          </view>
          <view class="cover-overlay">
            <view class="badge" :class="'badge-type-' + challenge.type">
              <text class="badge-text">{{ getTypeName(challenge.type) }}</text>
            </view>
            <text class="cover-title">{{ challenge.title }}</text>
          </view>
        </view>

        <view class="info-card">
          <view class="info-row">
            <view class="info-item">
              <text class="info-icon">&#128101;</text>
              <text class="info-value">{{ challenge.participantCount || 0 }}</text>
              <text class="info-label">参与人数</text>
            </view>
            <view class="info-divider"></view>
            <view class="info-item">
              <text class="info-icon">&#128197;</text>
              <text class="info-value">{{ formatRemaining() }}</text>
              <text class="info-label">剩余时间</text>
            </view>
            <view class="info-divider"></view>
            <view class="info-item">
              <text class="info-icon">&#127919;</text>
              <text class="info-value">{{ challenge.conditionValue || 0 }}</text>
              <text class="info-label">目标次数</text>
            </view>
          </view>
        </view>

        <view class="section-card" v-if="challenge.description">
          <text class="section-title">挑战说明</text>
          <text class="desc-text">{{ challenge.description }}</text>
        </view>

        <view class="section-card" v-if="challenge.rewardDescription">
          <text class="section-title">&#127873; 奖励</text>
          <text class="reward-text">{{ challenge.rewardDescription }}</text>
        </view>

        <view class="section-card" v-if="myProgress">
          <text class="section-title">我的进度</text>
          <view class="progress-bar-wrap">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
            </view>
            <text class="progress-text">{{ myProgress.progress || 0 }} / {{ challenge.conditionValue || 0 }}</text>
          </view>
          <view class="progress-status">
            <text v-if="myProgress.completed" class="status-completed">&#9989; 已完成</text>
            <text v-else class="status-ongoing">&#128170; 进行中</text>
          </view>
          <view class="update-btn" v-if="!myProgress.completed" @tap="onUpdateProgress">
            <text class="update-btn-text">更新进度</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-header">
            <text class="section-title">&#127942; 排行榜</text>
            <text class="section-sub">TOP {{ leaderboard.length }}</text>
          </view>
          <view class="leaderboard-list" v-if="leaderboard.length > 0">
            <view
              v-for="(item, index) in leaderboard"
              :key="item.id"
              class="leaderboard-item"
              :class="{ 'is-me': item.userId === currentUserId }"
            >
              <view class="rank-wrap">
                <text v-if="index === 0" class="rank-medal">&#129351;</text>
                <text v-else-if="index === 1" class="rank-medal">&#129352;</text>
                <text v-else-if="index === 2" class="rank-medal">&#129353;</text>
                <text v-else class="rank-num">{{ index + 1 }}</text>
              </view>
              <view class="rank-info">
                <text class="rank-name">{{ item.nickname || '用户' + item.userId }}</text>
                <text class="rank-progress">{{ item.progress }}次</text>
              </view>
              <view class="rank-status" v-if="item.completed">
                <text class="rank-done">&#9989;</text>
              </view>
            </view>
          </view>
          <view class="empty-leaderboard" v-else>
            <text class="empty-text">暂无排行数据</text>
          </view>
        </view>

        <view class="bottom-spacer"></view>
      </view>

      <view class="loading-wrap" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>

    <view class="bottom-bar" v-if="challenge && !myProgress">
      <view class="join-btn" @tap="onJoin">
        <text class="join-btn-text">参加挑战</text>
      </view>
    </view>
  </view>
</template>

<script>
import * as challengeApi from '@/api/challenge'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 74,
      challengeId: null,
      challenge: null,
      myProgress: null,
      leaderboard: [],
      currentUserId: null,
      loading: false
    }
  },
  computed: {
    progressPercent() {
      if (!this.myProgress || !this.challenge || !this.challenge.conditionValue) return 0
      return Math.min(100, Math.round((this.myProgress.progress / this.challenge.conditionValue) * 100))
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
      this.challengeId = options.id
    }
    try {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo && userInfo.id) {
        this.currentUserId = userInfo.id
      }
    } catch (e) {}
  },
  onShow() {
    if (this.challengeId) {
      this.loadDetail()
    }
  },
  methods: {
    async loadDetail() {
      this.loading = true
      try {
        const [detailRes, myRes, lbRes] = await Promise.all([
          challengeApi.getChallengeDetail(this.challengeId),
          challengeApi.getMyChallenges(),
          challengeApi.getChallengeLeaderboard(this.challengeId, 50)
        ])
        if (detailRes.success) {
          this.challenge = detailRes.data
        }
        if (myRes.success && myRes.data) {
          this.myProgress = myRes.data.find(p => p.challengeId == this.challengeId) || null
        }
        if (lbRes.success) {
          this.leaderboard = lbRes.data || []
        }
      } catch (e) {
        console.error('加载挑战详情失败:', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    async onJoin() {
      try {
        const res = await challengeApi.joinChallenge(this.challengeId)
        if (res.success) {
          uni.showToast({ title: '参加成功！', icon: 'success' })
          this.loadDetail()
        }
      } catch (e) {
        uni.showToast({ title: e.message || '参加失败', icon: 'none' })
      }
    },
    async onUpdateProgress() {
      try {
        const res = await challengeApi.updateChallengeProgress(this.challengeId)
        if (res.success) {
          uni.showToast({ title: '进度已更新', icon: 'success' })
          this.loadDetail()
        }
      } catch (e) {
        uni.showToast({ title: e.message || '更新失败', icon: 'none' })
      }
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
    formatRemaining() {
      if (!this.challenge || !this.challenge.endDate) return '-'
      const now = new Date()
      const end = new Date(this.challenge.endDate)
      const diff = end.getTime() - now.getTime()
      if (diff <= 0) return '已结束'
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      if (days > 0) return days + '天'
      const hours = Math.floor(diff / (1000 * 60 * 60))
      if (hours > 0) return hours + '小时'
      return '即将结束'
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

.cover-section {
  position: relative;
  height: 440rpx;
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
  font-size: 128rpx;
}

.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
}

.badge {
  display: inline-block;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  margin-bottom: 16rpx;
}

.badge-type-1 { background: rgba(255, 106, 61, 0.9); }
.badge-type-2 { background: rgba(64, 158, 255, 0.9); }
.badge-type-3 { background: rgba(82, 196, 26, 0.9); }

.badge-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}

.cover-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
}

.info-card {
  margin: -40rpx 20rpx 20rpx 20rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.info-icon {
  font-size: 40rpx;
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

.section-card {
  margin: 20rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16rpx;
}

.section-header .section-title {
  margin-bottom: 0;
}

.section-sub {
  font-size: 24rpx;
  color: #9ca3af;
}

.desc-text {
  font-size: 28rpx;
  color: #374151;
  line-height: 1.7;
}

.reward-text {
  font-size: 28rpx;
  color: #ff6a3d;
  font-weight: 500;
}

.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 16rpx;
  background: #f3f4f6;
  border-radius: 999rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 26rpx;
  color: #6b7280;
  font-weight: 500;
  white-space: nowrap;
}

.progress-status {
  margin-bottom: 20rpx;
}

.status-completed {
  font-size: 26rpx;
  color: #52c41a;
}

.status-ongoing {
  font-size: 26rpx;
  color: #ff6a3d;
}

.update-btn {
  display: inline-block;
  padding: 16rpx 40rpx;
  background: linear-gradient(135deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
}

.update-btn-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 500;
}

.leaderboard-list {
  margin-top: 8rpx;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.leaderboard-item:last-child {
  border-bottom: none;
}

.leaderboard-item.is-me {
  background: rgba(255, 106, 61, 0.06);
  border-radius: 16rpx;
  padding: 16rpx;
  margin: 0 -8rpx;
  border-bottom: none;
}

.rank-wrap {
  width: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-medal {
  font-size: 36rpx;
}

.rank-num {
  font-size: 28rpx;
  font-weight: 600;
  color: #9ca3af;
}

.rank-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-left: 16rpx;
}

.rank-name {
  font-size: 28rpx;
  color: #111827;
}

.rank-progress {
  font-size: 26rpx;
  color: #ff6a3d;
  font-weight: 500;
}

.rank-status {
  margin-left: 16rpx;
}

.rank-done {
  font-size: 28rpx;
}

.empty-leaderboard {
  padding: 60rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 26rpx;
  color: #9ca3af;
}

.bottom-spacer {
  height: 160rpx;
}

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

.join-btn {
  padding: 24rpx 0;
  background: linear-gradient(135deg, #ff7a3d, #ff5a3d);
  border-radius: 999rpx;
  text-align: center;
}

.join-btn-text {
  font-size: 30rpx;
  color: #ffffff;
  font-weight: 600;
}

.loading-wrap {
  padding: 80rpx 0;
  text-align: center;
}

.loading-text {
  font-size: 26rpx;
  color: #9ca3af;
}
</style>
