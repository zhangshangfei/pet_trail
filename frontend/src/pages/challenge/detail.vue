<template>
  <view class="detail-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">‹</text>
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
              <text class="info-icon">👥</text>
              <text class="info-value">{{ challenge.participantCount || 0 }}</text>
              <text class="info-label">参与人数</text>
            </view>
            <view class="info-divider"></view>
            <view class="info-item">
              <text class="info-icon">📅</text>
              <text class="info-value">{{ formatRemaining() }}</text>
              <text class="info-label">剩余时间</text>
            </view>
            <view class="info-divider"></view>
            <view class="info-item">
              <text class="info-icon">🎯</text>
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
          <text class="section-title">🎁 奖励</text>
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
            <text v-if="myProgress.completed" class="status-completed">✅ 已完成</text>
            <text v-else class="status-ongoing">💪 进行中</text>
          </view>
          <view class="update-btn" v-if="!myProgress.completed" @tap="onUpdateProgress">
            <text class="update-btn-text">更新进度</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-header">
            <text class="section-title">🏆 排行榜</text>
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
                <text v-if="index === 0" class="rank-medal">🥇</text>
                <text v-else-if="index === 1" class="rank-medal">🥈</text>
                <text v-else-if="index === 2" class="rank-medal">🥉</text>
                <text v-else class="rank-num">{{ index + 1 }}</text>
              </view>
              <view class="rank-info">
                <text class="rank-name">用户{{ item.userId }}</text>
                <text class="rank-progress">{{ item.progress }}次</text>
              </view>
              <view class="rank-status" v-if="item.completed">
                <text class="rank-done">✅</text>
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
      const icons = { 1: '✅', 2: '📱', 3: '💪' }
      return icons[type] || '🏆'
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
/* ========== 页面基础 ========== */
.detail-page {
  min-height: 100vh;
  background: var(--pt-bg);
}

/* ========== 顶部导航 ========== */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 54px;
  padding: 0 32rpx;
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 28px;
  color: #ffffff;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.header-right {
  width: 36px;
}

/* ========== 滚动区域 ========== */
.detail-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}

.detail-content {
  padding: 0 0 32rpx 0;
}

/* ========== 封面区 ========== */
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
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
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

.badge-type-1 { background: rgba(255, 90, 61, 0.9); }
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

/* ========== 信息卡 ========== */
.info-card {
  margin: -40rpx 32rpx 24rpx 32rpx;
  background: var(--pt-card);
  border-radius: var(--pt-radius);
  padding: 40rpx 32rpx;
  box-shadow: var(--pt-shadow-soft);
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
  color: var(--pt-text);
}

.info-label {
  font-size: 24rpx;
  color: var(--pt-muted);
}

.info-divider {
  width: 1px;
  height: 80rpx;
  background: var(--pt-border);
}

/* ========== 通用卡片 ========== */
.section-card {
  margin: 24rpx 32rpx;
  background: var(--pt-card);
  border-radius: var(--pt-radius);
  padding: 32rpx;
  box-shadow: var(--pt-shadow-soft);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--pt-text);
  margin-bottom: 20rpx;
}

.section-header .section-title {
  margin-bottom: 0;
}

.section-sub {
  font-size: 26rpx;
  color: var(--pt-muted);
}

/* ========== 文本样式 ========== */
.desc-text {
  font-size: 28rpx;
  color: var(--pt-secondary);
  line-height: 1.7;
}

.reward-text {
  font-size: 28rpx;
  color: var(--pt-primary);
  font-weight: 500;
}

/* ========== 进度条 ========== */
.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 20rpx;
}

.progress-bar {
  flex: 1;
  height: 20rpx;
  background: var(--pt-input-bg);
  border-radius: 999rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
  border-radius: 999rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 28rpx;
  color: var(--pt-secondary);
  font-weight: 500;
  white-space: nowrap;
}

.progress-status {
  margin-bottom: 24rpx;
}

.status-completed {
  font-size: 28rpx;
  color: #52c41a;
}

.status-ongoing {
  font-size: 28rpx;
  color: var(--pt-primary);
}

/* ========== 按钮 ========== */
.update-btn {
  display: inline-block;
  padding: 20rpx 48rpx;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
  border-radius: 999rpx;
}

.update-btn-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 500;
}

/* ========== 排行榜 ========== */
.leaderboard-list {
  margin-top: 8rpx;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px solid var(--pt-border);
}

.leaderboard-item:last-child {
  border-bottom: none;
}

.leaderboard-item.is-me {
  background: rgba(255, 90, 61, 0.06);
  border-radius: 16rpx;
  padding: 20rpx 16rpx;
  margin: 0 -16rpx;
  border-bottom: none;
}

.rank-wrap {
  width: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-medal {
  font-size: 40rpx;
}

.rank-num {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--pt-muted);
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
  color: var(--pt-text);
}

.rank-progress {
  font-size: 28rpx;
  color: var(--pt-primary);
  font-weight: 500;
}

.rank-status {
  margin-left: 16rpx;
}

.rank-done {
  font-size: 28rpx;
}

/* ========== 空状态 ========== */
.empty-leaderboard {
  padding: 60rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: var(--pt-muted);
}

/* ========== 底部栏 ========== */
.bottom-spacer {
  height: 200rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: var(--pt-card);
  box-shadow: var(--pt-shadow-soft);
  z-index: 100;
}

.join-btn {
  padding: 28rpx 0;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
  border-radius: 999rpx;
  text-align: center;
}

.join-btn-text {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: 600;
}

/* ========== 加载中 ========== */
.loading-wrap {
  padding: 80rpx 0;
  text-align: center;
}

.loading-text {
  font-size: 28rpx;
  color: var(--pt-muted);
}
</style>
