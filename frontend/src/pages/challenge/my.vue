<template>
  <view class="challenge-page">
    <!-- 波浪形顶部装饰背景 -->
    <view class="wave-header">
      <view class="wave-container">
        <view class="wave wave1"></view>
        <view class="wave wave2"></view>
        <view class="wave wave3"></view>
      </view>
      
      <!-- 导航栏 -->
      <view class="nav-fixed">
        <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
        <view class="nav-bar">
          <view class="nav-back" @tap="goBack">
            <text class="nav-back-icon">←</text>
          </view>
          <text class="nav-title">我的挑战赛</text>
          <view class="nav-placeholder"></view>
        </view>
      </view>

      <!-- 头部统计区 -->
      <view class="stats-section">
        <view class="glass-card stats-glass">
          <view class="stat-item">
            <text class="stat-number">{{ totalChallenges }}</text>
            <text class="stat-label">参与总数</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-number active">{{ completedCount }}</text>
            <text class="stat-label">已完成</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-number ongoing">{{ ongoingCount }}</text>
            <text class="stat-label">进行中</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="content-scroll" :style="{ paddingTop: (statusBarHeight + 280) + 'px' }" @scrolltolower="onScrollToLower">
      <view class="page-content">

        <!-- 筛选标签 -->
        <view class="filter-tabs">
          <view 
            v-for="(tab, index) in filterTabs" 
            :key="index"
            class="filter-tab"
            :class="{ active: currentTab === index }"
            @tap="switchTab(index)"
          >
            <text :class="currentTab === index ? 'tab-text-active' : 'tab-text'">{{ tab.label }}</text>
          </view>
        </view>

        <!-- 加载中 -->
        <view v-if="loading && myChallenges.length === 0" class="loading-state glass-card">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 挑战赛列表 -->
        <view v-else class="challenge-list">
          <view 
            v-for="item in filteredChallenges" 
            :key="item.challengeId"
            class="glass-card challenge-card"
            :class="'card-' + item.status"
            @tap="goChallengeDetail(item.challengeId)"
          >
            <!-- 卡片顶部装饰条 -->
            <view class="card-top-bar" :class="'bar-' + item.status"></view>

            <!-- 卡片内容 -->
            <view class="card-body">
              <!-- 标题行 -->
              <view class="card-header-row">
                <view class="challenge-icon-wrap" :class="'icon-type-' + item.type">
                  <text class="challenge-icon">{{ getTypeIcon(item.type) }}</text>
                </view>
                <view class="title-area">
                  <text class="challenge-title">{{ item.title }}</text>
                  <view class="badge-wrap">
                    <text class="status-badge" :class="'badge-' + item.status">
                      {{ getStatusText(item.status) }}
                    </text>
                  </view>
                </view>
              </view>

              <!-- 描述 -->
              <text class="challenge-desc">{{ item.description }}</text>

              <!-- 进度条（进行中才显示） -->
              <view class="progress-section" v-if="item.status === 'ongoing'">
                <view class="progress-info">
                  <text class="progress-text">完成进度</text>
                  <text class="progress-value">{{ item.progressPercent }}%</text>
                </view>
                <view class="progress-bar">
                  <view class="progress-fill" :style="{ width: item.progressPercent + '%' }"></view>
                </view>
              </view>

              <!-- 已完成标记 -->
              <view class="completed-mark" v-if="item.status === 'completed'">
                <text class="completed-icon">✓</text>
                <text class="completed-text">已完成 · {{ formatDate(item.completedAt) }}</text>
              </view>

              <!-- 底部信息栏 -->
              <view class="card-footer">
                <view class="footer-left">
                  <text class="time-icon">⏰</text>
                  <text class="time-text">{{ getTimeText(item) }}</text>
                </view>
                <view class="footer-right">
                  <text class="participants">👥 {{ item.participantCount || 0 }}人参与</text>
                </view>
              </view>
            </view>

            <!-- 玻璃高光效果 -->
            <view class="glass-highlight"></view>
          </view>

          <!-- 空状态 -->
          <view v-if="!loading && filteredChallenges.length === 0" class="empty-state glass-card">
            <text class="empty-icon">🏆</text>
            <text class="empty-title">{{ emptyTitle }}</text>
            <text class="empty-desc">{{ emptyDesc }}</text>
            <view class="empty-btn" @tap="goDiscover">
              <text class="empty-btn-text">探索挑战 ›</text>
            </view>
          </view>
        </view>

        <!-- 底部安全区域 -->
        <view class="bottom-safe"></view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import * as challengeApi from '@/api/challenge'
import { checkLogin } from '@/utils/index'

const TYPE_MAP = {
  1: { icon: '✅', name: '打卡', cls: 'checkin' },
  2: { icon: '🎉', name: '社交', cls: 'social' },
  3: { icon: '💪', name: '健康', cls: 'health' }
}

const STATUS_ONGOING = 'ongoing'
const STATUS_COMPLETED = 'completed'
const STATUS_EXPIRED = 'expired'

export default {
  data() {
    return {
      statusBarHeight: 20,
      loading: false,
      refreshing: false,
      currentTab: 0,
      filterTabs: [
        { label: '全部', value: 'all' },
        { label: '进行中', value: STATUS_ONGOING },
        { label: '已完成', value: STATUS_COMPLETED }
      ],
      myChallenges: []
    }
  },

  computed: {
    totalChallenges() {
      return this.myChallenges.length
    },
    completedCount() {
      return this.myChallenges.filter(c => c.status === STATUS_COMPLETED).length
    },
    ongoingCount() {
      return this.myChallenges.filter(c => c.status === STATUS_ONGOING).length
    },
    filteredChallenges() {
      if (this.currentTab === 0) return this.myChallenges
      const target = this.filterTabs[this.currentTab].value
      return this.myChallenges.filter(c => c.status === target)
    },
    emptyTitle() {
      if (this.currentTab === 1) return '暂无进行中的挑战'
      if (this.currentTab === 2) return '暂无已完成的挑战'
      return '暂无挑战赛记录'
    },
    emptyDesc() {
      if (this.currentTab === 1) return '去参加一个新挑战吧！'
      if (this.currentTab === 2) return '完成挑战后这里会有记录哦'
      return '去发现更多有趣的挑战吧！'
    }
  },

  onLoad() {
    this.initStatusBar()
  },

  onShow() {
    this.loadMyChallenges()
  },

  onPullDownRefresh() {
    this.refreshing = true
    this.loadMyChallenges().finally(() => {
      this.refreshing = false
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    initStatusBar() {
      try {
        const sys = uni.getSystemInfoSync()
        this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      } catch (e) {
        this.statusBarHeight = 20
      }
    },

    async loadMyChallenges() {
      const loggedIn = await checkLogin()
      if (!loggedIn) return

      this.loading = true
      try {
        const res = await challengeApi.getMyChallenges()
        if (res && res.success && Array.isArray(res.data)) {
          this.myChallenges = res.data.map(p => this.normalizeParticipant(p))
        } else {
          this.myChallenges = []
        }
      } catch (e) {
        console.error('加载我的挑战赛失败:', e)
        this.myChallenges = []
      } finally {
        this.loading = false
      }
    },

    normalizeParticipant(p) {
      const challenge = p.challenge || {}
      const type = challenge.type || 0
      const now = new Date()
      const endDate = challenge.endDate ? new Date(challenge.endDate) : null
      const startDate = challenge.startDate ? new Date(challenge.startDate) : null

      let status = STATUS_ONGOING
      if (p.completed) {
        status = STATUS_COMPLETED
      } else if (endDate && now > endDate) {
        status = STATUS_EXPIRED
      }

      const conditionValue = challenge.conditionValue || 100
      const progress = p.progress || 0
      const progressPercent = Math.min(100, Math.round((progress / conditionValue) * 100))

      return {
        challengeId: p.challengeId,
        title: challenge.title || '未知挑战',
        description: challenge.description || '',
        type,
        status,
        progress,
        progressPercent,
        completedAt: p.completedAt,
        participantCount: challenge.participantCount || 0,
        startDate: challenge.startDate,
        endDate: challenge.endDate,
        rewardDescription: challenge.rewardDescription || ''
      }
    },

    switchTab(index) {
      this.currentTab = index
    },

    getTypeIcon(type) {
      return (TYPE_MAP[type] && TYPE_MAP[type].icon) || '🏆'
    },

    getStatusText(status) {
      const map = {
        [STATUS_ONGOING]: '进行中',
        [STATUS_COMPLETED]: '已完成',
        [STATUS_EXPIRED]: '已过期'
      }
      return map[status] || '未知'
    },

    getTimeText(item) {
      if (item.status === STATUS_ONGOING && item.endDate) {
        const days = this.calcDaysLeft(item.endDate)
        return days > 0 ? `剩余 ${days} 天` : '即将结束'
      }
      if (item.status === STATUS_EXPIRED) {
        return '已结束'
      }
      if (item.startDate && item.endDate) {
        return this.formatDateRange(item.startDate, item.endDate)
      }
      return ''
    },

    calcDaysLeft(endDateStr) {
      try {
        const end = new Date(endDateStr)
        const now = new Date()
        const diff = Math.ceil((end - now) / (1000 * 60 * 60 * 24))
        return Math.max(0, diff)
      } catch (e) {
        return 0
      }
    },

    formatDateRange(startStr, endStr) {
      try {
        const s = new Date(startStr)
        const e = new Date(endStr)
        return `${s.getMonth() + 1}/${s.getDate()} - ${e.getMonth() + 1}/${e.getDate()}`
      } catch (e) {
        return ''
      }
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      try {
        const d = new Date(dateStr)
        return `${d.getMonth() + 1}月${d.getDate()}日`
      } catch (e) {
        return ''
      }
    },

    goChallengeDetail(challengeId) {
      if (!challengeId) return
      uni.navigateTo({ url: `/pages/challenge/detail?id=${challengeId}` })
    },

    goDiscover() {
      uni.navigateTo({ url: '/pages/challenge/index' })
    },

    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    onScrollToLower() {
      // 预留分页加载
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   我的挑战赛 - 玻璃拟态设计系统 Glassmorphism v1.0
   
   设计特点：
   ✦ 玻璃拟态卡片（backdrop-filter: blur）
   ✦ 柔和阴影与圆角
   ✦ 波浪形顶部装饰
   ✦ 轻盈通透的视觉感受
   ✦ hover/tap状态反馈
   ✦ 按压动画效果
   ============================================ */

/* ========== 设计变量 ========== */
$primary: #ff6b35;
$primary-light: #ff8c5a;
$primary-dark: #e55a2b;

$glass-bg: rgba(255, 255, 255, 0.25);
$glass-border: rgba(255, 255, 255, 0.4);
$glass-shadow: 0 8rpx 32rpx rgba(31, 38, 135, 0.15);

$success: #10b981;
$warning: #f59e0b;
$info: #3b82f6;

$radius-sm: 16rpx;
$radius-md: 24rpx;
$radius-lg: 32rpx;
$radius-xl: 40rpx;

/* ========== 页面基础 ========== */
.challenge-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  position: relative;
  overflow: hidden;
}

/* ========== 波浪形顶部装饰 ========== */
.wave-header {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 600rpx;
  background: linear-gradient(180deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.8) 100%);
  z-index: 1;
}

.wave-container {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 200rpx;
  overflow: hidden;
}

.wave {
  position: absolute;
  bottom: 0; left: -50%;
  width: 200%;
  height: 200rpx;
  border-radius: 45% 45% 0 0;
  animation: waveFloat 8s ease-in-out infinite;
}

.wave1 {
  background: rgba(255, 255, 255, 0.3);
  animation-delay: 0s;
  bottom: -20rpx;
}

.wave2 {
  background: rgba(255, 255, 255, 0.2);
  animation-delay: -2s;
  bottom: -30rpx;
}

.wave3 {
  background: rgba(255, 255, 255, 0.1);
  animation-delay: -4s;
  bottom: -40rpx;
}

@keyframes waveFloat {
  0%, 100% { transform: translateX(0) rotate(0deg); }
  25% { transform: translateX(5%) rotate(1deg); }
  50% { transform: translateX(0) rotate(0deg); }
  75% { transform: translateX(-5%) rotate(-1deg); }
}

/* ========== 导航栏 ========== */
.nav-fixed {
  position: relative;
  z-index: 10;
}

.status-bar { width: 100%; }

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
}

.nav-back {
  width: 68rpx; height: 68rpx;
  border-radius: 34rpx;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  background: $glass-bg;
  border: 1rpx solid $glass-border;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:active {
    transform: scale(0.92);
    background: rgba(255, 255, 255, 0.35);
  }
}

.nav-back-icon {
  font-size: 32rpx;
  color: #fff;
  font-weight: 700;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.nav-placeholder { width: 68rpx; }

/* ========== 统计区 ========== */
.stats-section {
  position: relative;
  z-index: 5;
  padding: 24rpx 28rpx 40rpx;
}

.stats-glass {
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background: $glass-bg;
  border: 1rpx solid $glass-border;
  border-radius: $radius-lg;
  box-shadow: $glass-shadow;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 32rpx 20rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
  position: relative;
}

.stat-number {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #fff;
  line-height: 1.2;
  text-shadow: 0 2rpx 12rpx rgba(102, 126, 234, 0.4);

  &.active {
    color: #fef08a;
    text-shadow: 0 2rpx 12rpx rgba(245, 158, 11, 0.4);
  }

  &.ongoing {
    color: #bbf7d0;
    text-shadow: 0 2rpx 12rpx rgba(16, 185, 129, 0.4);
  }
}

.stat-label {
  display: block;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8rpx;
  font-weight: 500;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.3), transparent);
}

/* ========== 内容滚动区 ========== */
.content-scroll {
  position: relative;
  z-index: 2;
  min-height: 100vh;
}

.page-content {
  padding: 0 28rpx 120rpx;
  margin-top: -20rpx;
}

/* ========== 筛选标签 ========== */
.filter-tabs {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 0 20rpx;
}

.filter-tab {
  flex: 1;
  height: 72rpx;
  border-radius: 36rpx;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  background: $glass-bg;
  border: 1rpx solid $glass-border;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background: rgba(255, 255, 255, 0.45);
    border-color: rgba(255, 255, 255, 0.7);
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.25);
  }
}

.tab-text {
  font-size: 27rpx;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
  transition: all 0.3s ease;
}

.tab-text-active {
  font-size: 27rpx;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

/* ========== 玻璃拟态卡片通用样式 ========== */
.glass-card {
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  background: $glass-bg;
  border: 1rpx solid $glass-border;
  border-radius: $radius-lg;
  box-shadow: $glass-shadow;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  /* hover/按压状态 */
  &:active {
    transform: translateY(-4rpx) scale(0.98);
    box-shadow: 
      0 16rpx 48rpx rgba(31, 38, 135, 0.25),
      0 0 0 1rpx rgba(255, 255, 255, 0.5);
    background: rgba(255, 255, 255, 0.35);
  }

  /* 玻璃高光层 */
  .glass-highlight {
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 60%;
    background: linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.15) 0%,
      rgba(255, 255, 255, 0.02) 50%,
      transparent 100%
    );
    pointer-events: none;
    border-radius: $radius-lg $radius-lg 0 0;
  }
}

/* ========== 挑战赛卡片 ========== */
.challenge-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.challenge-card {
  margin-bottom: 8rpx;
}

.card-top-bar {
  height: 6rpx;
  border-radius: $radius-lg $radius-lg 0 0;
  transition: all 0.3s ease;

  &.bar-ongoing { background: linear-gradient(90deg, $info, #60a5fa); }
  &.bar-completed { background: linear-gradient(90deg, $success, #34d399); }
  &.bar-expired { background: linear-gradient(90deg, #d1d1d6, #c7c7cc); }
}

.card-body {
  padding: 28rpx 24rpx 24rpx;
  position: relative;
  z-index: 1;
}

.card-header-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.challenge-icon-wrap {
  width: 64rpx; height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);

  &.icon-type-1 { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
  &.icon-type-2 { background: linear-gradient(135deg, #fce7f3, #fbcfe8); }
  &.icon-type-3 { background: linear-gradient(135deg, #dcfce7, #bbf7d0); }
}

.challenge-icon { font-size: 32rpx; }

.title-area { flex: 1; }

.challenge-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.4;
  margin-bottom: 8rpx;
}

.badge-wrap { display: inline-block; }

.status-badge {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 600;

  &.badge-ongoing {
    background: rgba(59, 130, 246, 0.15);
    color: #2563eb;
  }

  &.badge-completed {
    background: rgba(16, 185, 129, 0.15);
    color: #059669;
  }

  &.badge-expired {
    background: rgba(156, 163, 175, 0.15);
    color: #6b7280;
  }
}

.challenge-desc {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 20rpx;
  font-weight: 400;
}

/* ========== 进度条 ========== */
.progress-section { margin-bottom: 20rpx; }

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 500;
}

.progress-value {
  font-size: 26rpx;
  color: $info;
  font-weight: 700;
}

.progress-bar {
  height: 12rpx;
  background: rgba(148, 163, 184, 0.2);
  border-radius: 6rpx;
  overflow: hidden;
  backdrop-filter: blur(4px);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $info, #60a5fa);
  border-radius: 6rpx;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  /* 内部光泽 */
  &::after {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.4) 50%,
      transparent 100%
    );
    animation: shimmer 2s infinite;
  }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* ========== 卡片底部信息 ========== */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1rpx solid rgba(148, 163, 184, 0.15);
}

.footer-left, .footer-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.time-icon, .participants {
  font-size: 24rpx;
}

.time-text {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 500;
}

.participants {
  font-size: 24rpx;
  color: #94a3b8;
  font-weight: 500;
}

/* ========== 空状态 ========== */
.empty-state {
  padding: 80rpx 40rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.empty-icon { font-size: 80rpx; }

.empty-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #475569;
}

.empty-desc {
  font-size: 26rpx;
  color: #94a3b8;
  line-height: 1.6;
}

.empty-btn {
  margin-top: 16rpx;
  padding: 18rpx 40rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, $primary, $primary-light);
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.45);
  }
}

.empty-btn-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
}

/* ========== 底部安全区域 ========== */
.bottom-safe {
  height: calc(60rpx + env(safe-area-inset-bottom));
}

/* ========== 加载状态 ========== */
.loading-state {
  padding: 80rpx 40rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

/* ========== 已完成标记 ========== */
.completed-mark {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
  padding: 10rpx 16rpx;
  background: rgba(16, 185, 129, 0.08);
  border-radius: 10rpx;
  width: fit-content;
}

.completed-icon {
  font-size: 24rpx;
  color: #10b981;
  font-weight: 700;
}

.completed-text {
  font-size: 24rpx;
  color: #10b981;
  font-weight: 600;
}
</style>
