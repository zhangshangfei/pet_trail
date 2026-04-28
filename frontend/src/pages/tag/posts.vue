<template>
  <view class="tag-posts-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">#{{ tagName }}</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <view class="tag-info-bar" :style="{ top: navHeight + 'px' }" v-if="tagInfo">
      <view class="tag-info-inner">
        <text class="tag-info-name">#{{ tagInfo.name }}</text>
        <text class="tag-info-count">{{ tagInfo.usageCount || 0 }} 条动态</text>
      </view>
    </view>

    <view
      class="posts-scroll"
      :style="{ paddingTop: (navHeight + (tagInfo ? tagInfoHeight : 0)) + 'px' }"
    >
      <view class="feed-list">
        <view
          v-for="post in postList"
          :key="post.id"
          class="post-card"
        >
          <view class="post-header">
            <image class="post-avatar" :src="post.avatar" mode="aspectFill" @tap="goUserProfile(post)" />
            <view class="post-info" @tap="goUserProfile(post)">
              <text class="post-name">{{ post.userName }}</text>
              <view class="post-pet-info">
                <text class="pet-icon">{{ getPetIcon(post.petType) }}</text>
                <text class="pet-name">{{ post.petName || '未知宠物' }}</text>
                <text class="pet-age" v-if="post.petAge"> | {{ post.petAge }}</text>
              </view>
            </view>
            <text class="post-time">{{ post.relativeTime }}</text>
          </view>

          <view class="post-content-wrap" @tap="goPostDetail(post)">
            <text
              class="post-content"
              :class="{ 'line-clamp': !expandedPosts[post.id] && post.content.length > 100 }"
            >{{ post.content }}</text>
            <text
              v-if="post.content.length > 100 && !expandedPosts[post.id]"
              class="expand-btn"
              @tap.stop="toggleExpand(post.id)"
            >展开</text>
          </view>

          <view
            v-if="post.images && post.images.length"
            class="post-image-wrap"
            @tap="goPostDetail(post)"
          >
            <image
              v-for="(img, idx) in post.images"
              :key="idx"
              class="post-image"
              :class="getImageClass(post.images.length)"
              :src="img"
              mode="aspectFill"
              @tap.stop="previewImage(post.images, idx)"
            />
          </view>

          <view v-if="post.stickers && post.stickers.length" class="post-stickers">
            <text v-for="(s, i) in post.stickers" :key="i" class="post-sticker">{{ s }}</text>
          </view>

          <view v-if="post.bubble" class="post-bubble" :style="{ background: post.bubble.bgColor || '#ff6a3d' }">
            <text class="post-bubble-text" :style="{ color: post.bubble.textColor || '#ffffff' }">{{ post.bubble.text }}</text>
          </view>

          <view v-if="post.location" class="post-location">
            <text class="location-icon">📍</text>
            <text class="location-text">{{ post.location }}</text>
          </view>

          <view v-if="post.tags && post.tags.length" class="post-tags">
            <text v-for="(tag, tidx) in post.tags" :key="tidx" class="post-tag" :class="{ 'post-tag--active': tag === tagName }" @tap="onTagTap(tag)">#{{ tag }}</text>
          </view>

          <view class="post-actions">
            <view class="actions-left">
              <view class="action-item" @tap="onLikeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.liked }">{{ post.liked ? '❤️' : '🤍' }}</text>
                <text class="action-count">{{ formatNumber(post.likes) }}</text>
              </view>
              <view class="action-item" @tap="onEeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.eeLiked }">{{ post.eeLiked ? '⭐' : '☆' }}</text>
                <text class="action-count">{{ formatNumber(post.eeCount) }}</text>
              </view>
              <view class="action-item" @tap="goPostDetail(post)">
                <text class="action-icon">💬</text>
                <text class="action-count">{{ formatNumber(post.comments) }}</text>
              </view>
              <view class="action-item post-time-item">
                <text class="action-time">{{ post.relativeTime }}</text>
              </view>
            </view>
            <view class="action-item share-btn-wrap">
              <button class="share-btn" open-type="share" :data-id="post.id">
                <text class="action-icon">↗️</text>
              </button>
            </view>
          </view>
        </view>

        <view v-if="loading" class="loading-text">加载中...</view>
        <view v-if="!hasMore && postList.length > 0" class="no-more-text">没有更多了</view>
        <view v-if="isEmpty" class="empty-state">
          <text class="empty-icon">🏷️</text>
          <text class="empty-text">该标签下暂无动态</text>
          <text class="empty-subtext">成为第一个发布该标签动态的人吧</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as tagApi from '@/api/tag'
import * as postApi from '@/api/post'
import { checkLogin } from '@/utils/index'
import { getUserAvatar } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navHeight: 0,
      tagInfoHeight: 52,
      tagName: '',
      tagInfo: null,
      postList: [],
      page: 1,
      size: 10,
      loading: false,
      hasMore: true,
      expandedPosts: {}
    }
  },
  computed: {
    isEmpty() {
      return !this.loading && this.postList.length === 0
    }
  },
  onLoad(options) {
    const sysInfo = uni.getSystemInfoSync()
    this.statusBarHeight = sysInfo.statusBarHeight || 0
    this.navHeight = this.statusBarHeight + 46

    if (options.name) {
      this.tagName = decodeURIComponent(options.name)
    }
    this.loadTagInfo()
    this.loadPosts()
  },
  onPullDownRefresh() {
    this.page = 1
    this.postList = []
    this.hasMore = true
    this.loadTagInfo()
    this.loadPosts().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  onReachBottom() {
    this.loadMore()
  },
  onShareAppMessage(res) {
    if (res.from === 'button' && res.target) {
      const postId = res.target.dataset.id
      const post = this.postList.find(p => p.id === postId)
      if (post) {
        return {
          title: post.content ? post.content.substring(0, 30) + (post.content.length > 30 ? '...' : '') : '萌宠动态',
          path: `/pages/post/detail?id=${postId}`,
          imageUrl: post.images && post.images.length > 0 ? post.images[0] : ''
        }
      }
    }
    return {
      title: `#${this.tagName} - 宠迹`,
      path: `/pages/tag/posts?name=${encodeURIComponent(this.tagName)}`
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    async loadTagInfo() {
      if (!this.tagName) return
      try {
        const res = await tagApi.getTagByName(this.tagName)
        if (res && res.success && res.data) {
          this.tagInfo = res.data
        }
      } catch (e) {
        console.warn('获取标签信息失败:', e)
      }
    },

    async loadPosts() {
      if (this.loading || !this.tagName) return
      this.loading = true
      try {
        const res = await tagApi.getTagPostsByName(this.tagName, this.page, this.size)
        if (res && res.success && Array.isArray(res.data)) {
          const newPosts = res.data.map(post => ({
            ...post,
            userName: post.userName || '未知用户',
            userAvatar: getUserAvatar(post.userId, post.userAvatar),
            avatar: getUserAvatar(post.userId, post.userAvatar),
            petName: post.petName || '',
            petType: post.petType || 0,
            petAge: post.petAge || '',
            petAvatar: post.petAvatar || '',
            time: this.formatTime(post.createdAt),
            relativeTime: this.getRelativeTime(post.createdAt),
            likes: post.likeCount || 0,
            comments: post.commentCount || 0,
            eeCount: post.eeCount || 0,
            liked: post.liked || false,
            eeLiked: post.eeLiked || false,
            images: post.imageList || [],
            videos: post.videoList || [],
            stickers: post.stickers || [],
            bubble: post.bubble || null,
            location: post.location || ''
          }))

          if (newPosts.length < this.size) {
            this.hasMore = false
          }
          this.postList = [...this.postList, ...newPosts]
          this.page++
        }
      } catch (error) {
        if (error.statusCode !== 401) {
          uni.showToast({ title: '加载失败', icon: 'none' })
        }
      } finally {
        this.loading = false
      }
    },

    loadMore() {
      if (this.hasMore && !this.loading) {
        this.loadPosts()
      }
    },

    getPetIcon(type) {
      switch (type) {
        case 1: return '🐱'
        case 2: return '🐕'
        case 3: return '🐰'
        case 4: return '🐦'
        default: return '🐾'
      }
    },

    formatNumber(num) {
      if (!num) return '0'
      if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k'
      }
      return num.toString()
    },

    formatTime(timestamp) {
      if (!timestamp) return ''
      const now = Date.now()
      const diff = now - new Date(timestamp.replace(' ', 'T')).getTime()
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      const date = new Date(timestamp.replace(' ', 'T'))
      return `${date.getMonth() + 1}/${date.getDate()}`
    },

    getRelativeTime(timestamp) {
      if (!timestamp) return ''
      let timeMs = 0
      if (typeof timestamp === 'number') {
        timeMs = timestamp
      } else if (typeof timestamp === 'string') {
        const dateStr = timestamp.replace(' ', 'T')
        timeMs = new Date(dateStr).getTime()
      }
      if (isNaN(timeMs) || timeMs === 0) return ''
      const now = Date.now()
      const diff = now - timeMs
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 172800000) return '昨天'
      if (diff < 259200000) return Math.floor(diff / 86400000) + '天前'
      const date = new Date(timeMs)
      return `${date.getMonth() + 1}/${date.getDate()}`
    },

    toggleExpand(postId) {
      this.$set(this.expandedPosts, postId, true)
    },

    getImageClass(count) {
      if (count === 1) return 'post-image--1'
      if (count === 2) return 'post-image--2'
      if (count === 3) return 'post-image--3'
      return 'post-image--grid'
    },

    previewImage(images, index) {
      const imageList = Array.isArray(images) ? images : [images]
      uni.previewImage({
        urls: imageList,
        current: index
      })
    },

    async onLikeTap(post) {
      const loggedIn = await checkLogin('请先登录后再点赞')
      if (!loggedIn) return
      try {
        const res = await postApi.toggleLike(post.id)
        if (res.success) {
          this.$set(post, 'liked', res.data.liked)
          this.$set(post, 'likes', res.data.likeCount)
        }
      } catch (error) {
        console.error('点赞失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    async onEeTap(post) {
      const loggedIn = await checkLogin('请先登录后再收藏')
      if (!loggedIn) return
      try {
        const res = await postApi.toggleEe(post.id)
        if (res.success && res.data) {
          const { eeLiked, eeCount } = res.data
          this.$set(post, 'eeLiked', eeLiked)
          this.$set(post, 'eeCount', eeCount)
        }
      } catch (error) {
        console.error('收藏失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    goUserProfile(post) {
      if (post.userId) {
        uni.navigateTo({ url: `/pages/user/detail?id=${post.userId}` })
      }
    },

    goPostDetail(post) {
      uni.navigateTo({ url: `/pages/post/detail?id=${post.id}` })
    },

    onTagTap(tag) {
      if (tag === this.tagName) return
      uni.redirectTo({ url: `/pages/tag/posts?name=${encodeURIComponent(tag)}` })
    }
  }
}
</script>

<style lang="scss" scoped>
.tag-posts-page {
  min-height: 100vh;
  background: #f9fafb;
}

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.status-bar {
  width: 100%;
}

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: background 0.2s, transform 0.15s;
}

.nav-back:active {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(0.92);
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-placeholder {
  width: 64rpx;
}

.tag-info-bar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 29;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.tag-info-inner {
  display: flex;
  align-items: center;
  padding: 16rpx 28rpx;
}

.tag-info-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #ff6a3d;
  margin-right: 16rpx;
}

.tag-info-count {
  font-size: 24rpx;
  color: #999;
}

.posts-scroll {
  min-height: 100vh;
}

.feed-list {
  padding: 8rpx 20rpx;
  padding-bottom: 200rpx;
}

.post-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.post-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  background: #e5e7eb;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4rpx;
}

.post-pet-info {
  display: flex;
  align-items: center;
}

.pet-icon {
  font-size: 24rpx;
  margin-right: 4rpx;
}

.pet-name {
  font-size: 22rpx;
  color: #6b7280;
}

.pet-age {
  font-size: 22rpx;
  color: #6b7280;
}

.post-time {
  font-size: 22rpx;
  color: #9ca3af;
}

.post-content-wrap {
  margin-bottom: 16rpx;
}

.post-content {
  display: block;
  font-size: 28rpx;
  color: #374151;
  line-height: 40rpx;
}

.line-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.expand-btn {
  display: block;
  font-size: 24rpx;
  color: #3b82f6;
  margin-top: 8rpx;
}

.post-image-wrap {
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  overflow: hidden;
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.post-image {
  border-radius: 12rpx;
  background: #f3f4f6;
}

.post-image--1 {
  width: 100%;
  height: 400rpx;
}

.post-image--2 {
  width: calc(50% - 4rpx);
  height: 280rpx;
}

.post-image--3 {
  width: calc(33.333% - 6rpx);
  height: 220rpx;
}

.post-image--grid {
  width: calc(33.333% - 6rpx);
  height: 200rpx;
}

.post-stickers {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  padding: 8rpx 0;
}

.post-sticker {
  font-size: 40rpx;
}

.post-bubble {
  display: inline-flex;
  padding: 16rpx 28rpx;
  border-radius: 24rpx;
  margin: 8rpx 0;
}

.post-bubble-text {
  font-size: 28rpx;
  font-weight: 600;
}

.post-location {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
}

.location-icon {
  font-size: 24rpx;
  margin-right: 6rpx;
}

.location-text {
  font-size: 22rpx;
  color: #6b7280;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 8rpx;
}

.post-tag {
  font-size: 22rpx;
  color: #ff6a3d;
  font-weight: 500;
  padding: 4rpx 12rpx;
  background: #fff0ea;
  border-radius: 12rpx;
}

.post-tag--active {
  background: #ff6a3d;
  color: #fff;
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1rpx solid #f3f4f6;
}

.actions-left {
  display: flex;
  gap: 40rpx;
}

.action-item {
  display: flex;
  align-items: center;
  color: #6b7280;
}

.share-btn-wrap {
  padding: 0;
  margin: 0;
  line-height: 1;
}

.share-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  line-height: 1;
  font-size: inherit;

  &::after {
    border: none;
  }
}

.action-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
  transition: all 0.3s;
}

.action-icon--liked {
  animation: likeAnimation 0.3s ease;
}

@keyframes likeAnimation {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.action-count {
  font-size: 24rpx;
  font-weight: 600;
}

.action-time {
  font-size: 22rpx;
  color: #9ca3af;
}

.post-time-item {
  margin-left: auto;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 26rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 60rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 30rpx;
  color: #6b7280;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-subtext {
  font-size: 26rpx;
  color: #9ca3af;
}
</style>
