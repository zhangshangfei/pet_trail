<template>
  <view class="challenge-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="header-title">挑战赛</text>
        <view class="header-right" @tap="goMyChallenges">
          <text class="my-btn">我的挑战</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="challenge-scroll" :style="{ top: headerHeight + 'px' }" @scrolltolower="loadMore">
      <view class="challenge-content">
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
            </view>
            <view class="card-body">
              <text class="card-title">{{ item.title }}</text>
              <text class="card-desc">{{ item.description }}</text>
              <view class="card-meta">
                <text class="meta-item">👥 {{ item.participantCount || 0 }}人参与</text>
                <text class="meta-item">🎁 {{ item.rewardDescription || '神秘奖励' }}</text>
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

        <view class="empty-tip" v-if="!loading && challenges.length === 0">
          <text class="empty-icon">🏆</text>
          <text class="empty-text">暂无进行中的挑战赛</text>
        </view>

        <view class="loading-tip" v-if="loading">
          <text class="loading-text">加载中...</text>
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
      const icons = { 1: '✅', 2: '📱', 3: '💪' }
      return icons[type] || '🏆'
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
  background: linear-gradient(180deg, #ff6a3d 0%, #ff8f6b 15%, #f5f5f5 30%);
}
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 54px;
  padding: 0 16px;
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
  color: #fff;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}
.header-right {
  padding: 4px 12px;
}
.my-btn {
  font-size: 14px;
  color: #fff;
}
.challenge-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}
.challenge-content {
  padding: 12px 16px;
}
.challenge-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.card-cover {
  position: relative;
  height: 160px;
}
.cover-img {
  width: 100%;
  height: 100%;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-icon {
  font-size: 48px;
}
.card-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.5);
}
.badge-type-1 { background: rgba(255, 106, 61, 0.85); }
.badge-type-2 { background: rgba(64, 158, 255, 0.85); }
.badge-type-3 { background: rgba(82, 196, 26, 0.85); }
.badge-text {
  font-size: 12px;
  color: #fff;
}
.card-body {
  padding: 16px;
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}
.card-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}
.meta-item {
  font-size: 13px;
  color: #999;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.time-text {
  font-size: 13px;
  color: #999;
}
.join-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
  border-radius: 20px;
}
.join-btn-text {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
}
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.empty-text {
  font-size: 16px;
  color: #999;
}
.loading-tip {
  text-align: center;
  padding: 20px 0;
}
.loading-text {
  font-size: 14px;
  color: #999;
}
</style>
