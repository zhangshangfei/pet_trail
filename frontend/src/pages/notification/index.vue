<template>
  <view class="notification-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="nav-back-arrow"></view>
      </view>
      <text class="nav-title">消息通知</text>
      <view v-if="notificationList.length > 0" class="nav-action" @click="onMarkAllRead">
        <text class="nav-action-text">全部已读</text>
      </view>
      <view v-else class="nav-action"></view>
    </view>

    <scroll-view
      scroll-y
      class="notification-list"
      :style="{ height: scrollHeight + 'px' }"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && notificationList.length === 0" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="notificationList.length === 0" class="empty-state">
        <text class="empty-emoji">🔔</text>
        <text class="empty-text">暂无通知</text>
      </view>

      <view v-else>
        <view
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ 'notification-item--unread': !item.isRead }"
          @click="onNotificationTap(item)"
        >
          <view class="notification-avatar-wrap">
            <image
              v-if="item.fromUserAvatar"
              class="notification-avatar"
              :src="item.fromUserAvatar"
              mode="aspectFill"
            />
            <view v-else class="notification-avatar-placeholder">
              <text class="notification-avatar-icon">{{ getTypeIcon(item.type) }}</text>
            </view>
            <view class="notification-type-badge">
              <text class="notification-type-icon">{{ getTypeIcon(item.type) }}</text>
            </view>
          </view>
          <view class="notification-content">
            <view class="notification-header">
              <text class="notification-name">{{ item.fromUserName }}</text>
              <text class="notification-time">{{ formatTime(item.createdAt) }}</text>
            </view>
            <text class="notification-text">{{ item.content }}</text>
          </view>
          <view v-if="!item.isRead" class="notification-dot"></view>
        </view>

        <view v-if="loading" class="loading-more">
          <text class="loading-more-text">加载中...</text>
        </view>
        <view v-else-if="!hasMore" class="no-more">
          <text class="no-more-text">没有更多了</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import * as notificationApi from '@/api/notification'

export default {
  data() {
    return {
      statusBarHeight: 20,
      scrollHeight: 0,
      notificationList: [],
      page: 1,
      size: 20,
      loading: false,
      hasMore: true
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      const navHeight = this.statusBarHeight + 44
      this.scrollHeight = (sys && sys.windowHeight ? sys.windowHeight : 667) - navHeight
    } catch (e) {
      this.scrollHeight = 600
    }
    this.loadNotifications()
  },
  onShow() {
    if (this.notificationList.length > 0) {
      this.page = 1
      this.hasMore = true
      this.loadNotifications()
    }
  },
  methods: {
    async loadNotifications() {
      if (this.loading || !this.hasMore) return
      this.loading = true
      try {
        const res = await notificationApi.getNotifications(this.page, this.size)
        if (res.success && Array.isArray(res.data)) {
          if (this.page === 1) {
            this.notificationList = res.data
          } else {
            this.notificationList = [...this.notificationList, ...res.data]
          }
          if (res.data.length < this.size) {
            this.hasMore = false
          }
          this.page++
        }
      } catch (e) {
        console.error('加载通知失败:', e)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      this.loadNotifications()
    },
    async onNotificationTap(item) {
      if (!item.isRead) {
        try {
          await notificationApi.markAsRead(item.id)
          item.isRead = true
        } catch (e) {
          console.error('标记已读失败:', e)
        }
      }
      if (item.type === 'like' || item.type === 'comment' || item.type === 'favorite') {
        if (item.targetId) {
          uni.navigateTo({ url: `/pages/post/detail?id=${item.targetId}` })
        }
      } else if (item.type === 'follow') {
        if (item.fromUserId) {
          uni.navigateTo({ url: `/pages/user/detail?id=${item.fromUserId}` })
        }
      }
    },
    async onMarkAllRead() {
      try {
        await notificationApi.markAllAsRead()
        this.notificationList.forEach(item => {
          item.isRead = true
        })
        uni.showToast({ title: '已全部标记为已读', icon: 'none' })
      } catch (e) {
        console.error('全部已读失败:', e)
      }
    },
    goBack() {
      uni.navigateBack()
    },
    getTypeIcon(type) {
      const map = {
        like: '❤️',
        favorite: '⭐',
        comment: '💬',
        follow: '👤',
        system: '📢'
      }
      return map[type] || '🔔'
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      const minute = 60 * 1000
      const hour = 60 * minute
      const day = 24 * hour

      if (diff < minute) return '刚刚'
      if (diff < hour) return Math.floor(diff / minute) + '分钟前'
      if (diff < day) return Math.floor(diff / hour) + '小时前'
      if (diff < 7 * day) return Math.floor(diff / day) + '天前'
      const m = date.getMonth() + 1
      const d = date.getDate()
      return m + '月' + d + '日'
    }
  }
}
</script>

<style scoped>
.notification-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  width: 100%;
  background: #fff;
}

.nav-bar {
  height: 92rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, transform 0.15s;
}

.nav-back:active {
  background: rgba(0, 0, 0, 0.1);
  transform: scale(0.92);
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #333;
  border-bottom: 4rpx solid #333;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-action {
  min-width: 60rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.nav-action-text {
  font-size: 26rpx;
  color: #ff6a3d;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.empty-emoji {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9ca3af;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  background: #fff;
  border-bottom: 1rpx solid #f5f5f5;
  position: relative;
}

.notification-item--unread {
  background: #fff8f5;
}

.notification-avatar-wrap {
  position: relative;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.notification-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.notification-avatar-placeholder {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-avatar-icon {
  font-size: 36rpx;
}

.notification-type-badge {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #fff;
}

.notification-type-icon {
  font-size: 18rpx;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6rpx;
}

.notification-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 22rpx;
  color: #999;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.notification-text {
  font-size: 26rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #ff4d4f;
  flex-shrink: 0;
  margin-left: 12rpx;
}

.loading-more,
.no-more {
  padding: 24rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-more-text,
.no-more-text {
  font-size: 24rpx;
  color: #999;
}
</style>
