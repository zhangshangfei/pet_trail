<template>
  <view class="record-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">反馈记录</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">
        <view v-if="feedbackList.length" class="feedback-list">
          <view
            v-for="item in feedbackList"
            :key="item.id"
            class="feedback-card"
            @tap="toggleExpand(item.id)"
          >
            <view class="feedback-header">
              <view class="feedback-type-tag" :class="'type-' + item.type">
                <text class="type-tag-text">{{ getTypeLabel(item.type) }}</text>
              </view>
              <view class="feedback-status" :class="'status-' + item.status">
                <text class="status-text">{{ getStatusLabel(item.status) }}</text>
              </view>
            </view>

            <view class="feedback-body">
              <text class="feedback-content">{{ item.content }}</text>
            </view>

            <view class="feedback-meta">
              <text class="feedback-time">{{ formatTime(item.createdAt) }}</text>
            </view>

            <view v-if="item.reply" class="feedback-reply">
              <view class="reply-header">
                <text class="reply-icon">💬</text>
                <text class="reply-label">官方回复</text>
              </view>
              <text class="reply-content">{{ item.reply }}</text>
              <text v-if="item.updatedAt" class="reply-time">{{ formatTime(item.updatedAt) }}</text>
            </view>

            <view v-if="expandedId === item.id && item.images" class="feedback-images">
              <image
                v-for="(img, idx) in parseImages(item.images)"
                :key="idx"
                class="feedback-image"
                :src="img"
                mode="aspectFill"
                @tap="previewImage(img, parseImages(item.images))"
              />
            </view>

            <view class="expand-hint">
              <text class="expand-text">{{ expandedId === item.id ? '收起' : '展开详情' }}</text>
              <text class="expand-arrow" :class="{ rotated: expandedId === item.id }">›</text>
            </view>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="empty-emoji">📝</text>
          <text class="empty-text">暂无反馈记录</text>
          <text class="empty-hint">提交反馈后可在此查看处理进度</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 20,
      feedbackList: [],
      expandedId: null
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
  },
  onShow() {
    this.loadFeedbackList()
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    async loadFeedbackList() {
      const loggedIn = await checkLogin('')
      if (!loggedIn) {
        this.feedbackList = []
        return
      }
      try {
        const res = await uni.$request.get('/api/feedback/my')
        if (res && res.success && Array.isArray(res.data)) {
          this.feedbackList = res.data
        } else {
          this.feedbackList = []
        }
      } catch (e) {
        console.error('加载反馈列表失败:', e)
        this.feedbackList = []
      }
    },
    toggleExpand(id) {
      this.expandedId = this.expandedId === id ? null : id
    },
    getTypeLabel(type) {
      const map = { bug: 'Bug反馈', feature: '功能建议', experience: '体验优化', other: '其他' }
      return map[type] || '其他'
    },
    getStatusLabel(status) {
      const map = { 0: '待处理', 1: '处理中', 2: '已回复' }
      return map[status] || '待处理'
    },
    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      if (Number.isNaN(d.getTime())) return ''
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    parseImages(images) {
      if (!images) return []
      try {
        const parsed = JSON.parse(images)
        return Array.isArray(parsed) ? parsed : []
      } catch (e) {
        return typeof images === 'string' && images.startsWith('http') ? [images] : []
      }
    },
    previewImage(current, urls) {
      uni.previewImage({ current, urls })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.record-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%); }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; }
.nav-back { width: 64rpx; height: 64rpx; border-radius: 32rpx; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; }
.nav-back:active { background: rgba(255,255,255,0.35); }
.nav-back-arrow { width: 18rpx; height: 18rpx; border-left: 4rpx solid #fff; border-bottom: 4rpx solid #fff; transform: rotate(45deg) translate(2rpx, -2rpx); }
.nav-title { font-size: 34rpx; font-weight: 700; color: #fff; }
.nav-placeholder { width: 64rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 20rpx 24rpx 60rpx; }

.feedback-list { display: flex; flex-direction: column; gap: 20rpx; }

.feedback-card {
  background: $card-bg; border-radius: 24rpx; padding: 28rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}

.feedback-header {
  display: flex; flex-direction: row; align-items: center; justify-content: space-between;
  margin-bottom: 16rpx;
}

.feedback-type-tag {
  padding: 6rpx 18rpx; border-radius: 20rpx;
}
.type-bug { background: #fee2e2; }
.type-feature { background: #dbeafe; }
.type-experience { background: #fef3c7; }
.type-other { background: #f3f4f6; }
.type-tag-text { font-size: 22rpx; font-weight: 500; }
.type-bug .type-tag-text { color: #dc2626; }
.type-feature .type-tag-text { color: #2563eb; }
.type-experience .type-tag-text { color: #d97706; }
.type-other .type-tag-text { color: #6b7280; }

.feedback-status { padding: 6rpx 18rpx; border-radius: 20rpx; }
.status-0 { background: #fff4e6; }
.status-1 { background: #dbeafe; }
.status-2 { background: #d1fae5; }
.status-text { font-size: 22rpx; font-weight: 500; }
.status-0 .status-text { color: #ff7a3d; }
.status-1 .status-text { color: #2563eb; }
.status-2 .status-text { color: #047857; }

.feedback-body { margin-bottom: 12rpx; }
.feedback-content { font-size: 28rpx; color: $text-primary; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }

.feedback-meta { margin-bottom: 8rpx; }
.feedback-time { font-size: 22rpx; color: $text-light; }

.feedback-reply {
  background: #f0fdf4; border-radius: 16rpx; padding: 20rpx; margin-top: 16rpx;
  border: 1rpx solid #bbf7d0;
}
.reply-header { display: flex; flex-direction: row; align-items: center; margin-bottom: 10rpx; }
.reply-icon { font-size: 24rpx; margin-right: 8rpx; }
.reply-label { font-size: 24rpx; font-weight: 600; color: #047857; }
.reply-content { font-size: 26rpx; color: $text-primary; line-height: 1.6; }
.reply-time { display: block; font-size: 20rpx; color: $text-light; margin-top: 8rpx; text-align: right; }

.feedback-images { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.feedback-image { width: 160rpx; height: 160rpx; border-radius: 12rpx; background: #f3f4f6; }

.expand-hint {
  display: flex; flex-direction: row; align-items: center; justify-content: center;
  margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f3f4f6;
}
.expand-text { font-size: 24rpx; color: $primary; font-weight: 500; }
.expand-arrow { font-size: 28rpx; color: $primary; margin-left: 6rpx; transition: transform 0.2s; }
.expand-arrow.rotated { transform: rotate(90deg); }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 40rpx; }
.empty-emoji { font-size: 80rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 30rpx; font-weight: 600; color: #6b7280; margin-bottom: 12rpx; }
.empty-hint { font-size: 24rpx; color: #9ca3af; }
</style>
