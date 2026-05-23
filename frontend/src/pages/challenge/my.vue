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
            <view class="nav-back-arrow"></view>
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
    <scroll-view scroll-y class="content-scroll" :style="{ paddingTop: (statusBarHeight + 200) + 'px' }" @scrolltolower="onScrollToLower">
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
      const type = p.type || 0
      const now = new Date()
      const endDate = p.endDate ? new Date(p.endDate) : null

      let status = STATUS_ONGOING
      if (p.completed) {
        status = STATUS_COMPLETED
      } else if (endDate && now > endDate) {
        status = STATUS_EXPIRED
      }

      const conditionValue = p.conditionValue || 100
      const progress = p.progress || 0
      const progressPercent = Math.min(100, Math.round((progress / conditionValue) * 100))

      return {
        challengeId: p.challengeId,
        title: p.title || '未知挑战',
        description: p.description || '',
        type,
        status,
        progress,
        progressPercent,
        completedAt: p.completedAt,
        participantCount: p.participantCount || 0,
        startDate: p.startDate,
        endDate: p.endDate,
        rewardDescription: p.rewardDescription || ''
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
   我的挑战赛 - 高饱和活力设计 v5.0
   
   全局高饱和、强对比、有冲击力
   ============================================ */

/* ========== 设计变量 - 高饱和 ========== */
$primary: #ff5500;
$primary-deep: #e04a00;
$primary-soft: #ff7a3d;

$success: #16a34a;
$success-bright: #22c55e;
$info: #2563eb;
$info-bright: #3b82f6;
$muted: #78716c;

$bg: #fff5f0;
$white: #ffffff;
$text-dark: #1c1917;
$text-mid: #44403c;
$text-light: #a8a29e;

/* ========== 页面基础 ========== */
.challenge-page {
  min-height: 100vh;
  background: $bg;
}

/* ========== 头部区域 ========== */
.wave-header {
  position: fixed;
  top: 0; left: 0; right: 0;
  background: $white;
  z-index: 30;
}

.wave-container { display: none; }
.wave { display: none; }

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
  width: 64rpx; height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 32rpx;
  background: #f5f5f4;

  &:active {
    transform: scale(0.9);
    background: #e7e5e4;
  }
}

.nav-back-arrow {
  width: 18rpx; height: 18rpx;
  border-left: 3rpx solid $text-dark;
  border-bottom: 3rpx solid $text-dark;
  transform: rotate(45deg);
  margin-left: -4rpx;
}

.nav-title {
  font-size: 38rpx;
  font-weight: 900;
  color: $text-dark;
  letter-spacing: 1rpx;
}
.nav-placeholder { width: 68rpx; }

/* ========== 统计区 - 强色彩卡片 ========== */
.stats-section {
  position: relative;
  z-index: 5;
  padding: 12rpx 24rpx 36rpx;
}

.stats-glass {
  background: linear-gradient(135deg, #fff 0%, #fff7f2 50%, #ffefe8 100%);
  border: none;
  border-radius: 28rpx;
  box-shadow:
    0 6rpx 32rpx rgba(255, 85, 0, 0.15),
    0 2rpx 8rpx rgba(255, 85, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 30rpx 16rpx 26rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
  position: relative;
}

.stat-number {
  display: block;
  font-size: 56rpx;
  font-weight: 950;
  line-height: 1.05;
  letter-spacing: -3rpx;

  &,
  &:active { color: $primary; text-shadow: 0 4rpx 16rpx rgba(255, 85, 0, 0.25); }
  &.active { color: $success; text-shadow: 0 4rpx 16rpx rgba(22, 163, 74, 0.3); }
  &.ongoing { color: $info; text-shadow: 0 4rpx 16rpx rgba(37, 99, 235, 0.3); }
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: $muted;
  margin-top: 6rpx;
  font-weight: 700;
}

.stat-divider {
  width: 3rpx;
  height: 60rpx;
  background: linear-gradient(180deg, transparent, rgba(255, 85, 0, 0.18), transparent);
  border-radius: 2rpx;
}

/* ========== 内容滚动区 ========== */
.content-scroll {
  position: relative;
  z-index: 2;
  min-height: 100vh;
}
.page-content {
  padding: 0 24rpx 120rpx;
}

/* ========== 筛选标签 - 更鲜艳 ========== */
.filter-tabs {
  display: flex;
  gap: 14rpx;
  padding: 28rpx 0 24rpx;
}

.filter-tab {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  background: $white;
  border: 3rpx solid #ffe8dd;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 3rpx 14rpx rgba(255, 85, 0, 0.1);

  &:active { transform: scale(0.95); }

  &.active {
    background: $primary;
    border-color: $primary;
    box-shadow:
      0 8rpx 28rpx rgba(255, 85, 0, 0.45),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.2);
  }
}

.tab-text {
  font-size: 29rpx;
  color: $text-mid;
  font-weight: 700;
  transition: all 0.3s ease;
}
.tab-text-active {
  font-size: 29rpx;
  color: #fff;
  font-weight: 900;
  letter-spacing: 0.5rpx;
}

/* ========== 卡片通用样式 - 更强的阴影和边框 ========== */
.glass-card {
  background: $white;
  border: 2rpx solid #fff0e6;
  border-radius: 24rpx;
  box-shadow:
    0 6rpx 28rpx rgba(255, 85, 0, 0.1),
    0 2rpx 8rpx rgba(255, 85, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;

  &:active {
    transform: scale(0.97) translateY(-2rpx);
    box-shadow:
      0 16rpx 44rpx rgba(255, 85, 0, 0.2),
      0 6rpx 16rpx rgba(0, 0, 0, 0.06);
    border-color: $primary-soft;
  }

  .glass-highlight { display: none; }
}

/* ========== 挑战赛列表 ========== */
.challenge-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.challenge-card { margin-bottom: 0; }

/* 左侧装饰条 - 加粗加亮 */
.card-top-bar {
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 12rpx;
  border-radius: 24rpx 0 0 24rpx;

  &.bar-ongoing { background: linear-gradient(180deg, $info, #60a5fa); box-shadow: 2rpx 0 10rpx rgba(37, 99, 235, 0.3); }
  &.bar-completed { background: linear-gradient(180deg, $success, #4ade80); box-shadow: 2rpx 0 10rpx rgba(22, 163, 74, 0.3); }
  &.bar-expired { background: linear-gradient(180deg, #d6d3d1, #e7e5e4); }
}

.card-body {
  padding: 28rpx 24rpx 24rpx 34rpx;
  position: relative;
  z-index: 1;
}

.card-header-row {
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
  margin-bottom: 18rpx;
}

.challenge-icon-wrap {
  width: 82rpx; height: 82rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 18rpx rgba(0, 0, 0, 0.12);

  &.icon-type-1 { background: linear-gradient(145deg, #dbeafe, #bfdbfe); }
  &.icon-type-2 { background: linear-gradient(145deg, #fce7f3, #fbcfe8); }
  &.icon-type-3 { background: linear-gradient(145deg, #dcfce7, #bbf7d0); }
}
.challenge-icon { font-size: 40rpx; }

.title-area { flex: 1; }

.challenge-title {
  display: block;
  font-size: 33rpx;
  font-weight: 900;
  color: $text-dark;
  line-height: 1.35;
  margin-bottom: 10rpx;
}
.badge-wrap { display: inline-block; }

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  font-size: 23rpx;
  font-weight: 800;

  &.badge-ongoing {
    background: #dbeafe;
    color: #1d4ed8;
  }
  &.badge-completed {
    background: #dcfce7;
    color: #15803d;
  }
  &.badge-expired {
    background: #f5f5f4;
    color: $muted;
  }
}

.challenge-desc {
  display: block;
  font-size: 27rpx;
  color: $text-mid;
  line-height: 1.65;
  margin-bottom: 22rpx;
  font-weight: 500;
}

/* ========== 进度条 - 鲜艳面板 ========== */
.progress-section {
  margin-bottom: 22rpx;
  padding: 20rpx 22rpx;
  background: linear-gradient(135deg, #eff6ff, #f0f4ff);
  border-radius: 18rpx;
  border: 1rpx solid #dbeafe;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 13rpx;
}

.progress-text {
  font-size: 26rpx;
  color: $text-mid;
  font-weight: 700;
}
.progress-value {
  font-size: 30rpx;
  color: $info;
  font-weight: 900;
}

.progress-bar {
  height: 18rpx;
  background: #cbd5e1;
  border-radius: 9rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $info, #60a5fa, #93c5fd);
  border-radius: 9rpx;
  transition: width 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  box-shadow: 0 2rpx 8rpx rgba(37, 99, 235, 0.35);

  &::after {
    content: '';
    position: absolute;
    top: 2rpx; left: 4rpx; right: 4rpx; bottom: 0;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.55) 50%,
      transparent 100%
    );
    border-radius: 7rpx;
    animation: shimmer 2s infinite;
  }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* ========== 底部信息栏 ========== */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 18rpx;
  border-top: 2rpx solid #f5ebe5;
}

.footer-left, .footer-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.time-icon, .participants { font-size: 27rpx; }
.time-text {
  font-size: 26rpx;
  color: $text-mid;
  font-weight: 700;
}
.participants {
  font-size: 26rpx;
  color: $muted;
  font-weight: 600;
}

/* ========== 空状态 ========== */
.empty-state {
  padding: 96rpx 40rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18rpx;
}
.empty-icon { font-size: 92rpx; }
.empty-title {
  font-size: 35rpx;
  font-weight: 900;
  color: $text-dark;
}
.empty-desc {
  font-size: 28rpx;
  color: $muted;
  line-height: 1.65;
}

.empty-btn {
  margin-top: 22rpx;
  padding: 22rpx 52rpx;
  border-radius: 44rpx;
  background: linear-gradient(135deg, $primary, $primary-soft);
  box-shadow: 0 10rpx 32rpx rgba(255, 85, 0, 0.42);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.95);
    box-shadow: 0 5rpx 16rpx rgba(255, 85, 0, 0.55);
  }
}
.empty-btn-text {
  font-size: 31rpx;
  font-weight: 900;
  color: #fff;
  letter-spacing: 1rpx;
}

/* ========== 底部安全区 ========== */
.bottom-safe { height: calc(48rpx + env(safe-area-inset-bottom)); }

/* ========== 加载状态 ========== */
.loading-state {
  padding: 96rpx 40rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.loading-spinner {
  width: 52rpx; height: 52rpx;
  border: 6rpx solid rgba(255, 85, 0, 0.2);
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 0.75s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.loading-text {
  font-size: 28rpx;
  color: $text-mid;
  font-weight: 700;
}

/* ========== 已完成标记 ========== */
.completed-mark {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 18rpx;
  padding: 13rpx 22rpx;
  background: #dcfce7;
  border-radius: 16rpx;
  border: 1.5rpx solid #bbf7d0;
  width: fit-content;
}
.completed-icon {
  font-size: 28rpx;
  color: $success;
  font-weight: 950;
}
.completed-text {
  font-size: 26rpx;
  color: #15803d;
  font-weight: 800;
}
</style>
