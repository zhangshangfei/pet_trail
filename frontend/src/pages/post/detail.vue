<template>
  <view class="post-detail-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="nav-back-icon">←</text>
      </view>
      <text class="nav-title">动态详情</text>
      <view class="nav-placeholder"></view>
    </view>

    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>

    <scroll-view v-else-if="post" scroll-y class="detail-scroll" :style="{ top: navHeight + 'px' }">
      <view class="feed-list">
        <view class="post-card">
          <view class="post-header">
            <image class="post-avatar" :src="getUserAvatar(post.userId, post.userAvatar)" mode="aspectFill" @tap="goUserProfile" />
            <view class="post-info" @tap="goUserProfile">
              <text class="post-name">{{ post.userName }}</text>
              <view class="post-pet-info">
                <text class="pet-icon">{{ getPetIcon(post.petType) }}</text>
                <text class="pet-name">{{ post.petName || '未知宠物' }}</text>
                <text class="pet-age" v-if="post.petAge"> | {{ post.petAge }}岁</text>
              </view>
            </view>
            <text class="post-time">{{ post.relativeTime }}</text>
          </view>

          <view class="post-content-wrap">
            <text class="post-content">{{ post.content }}</text>
          </view>

          <view v-if="post.images && post.images.length" class="post-image-wrap">
            <image
              v-for="(img, idx) in post.images"
              :key="idx"
              class="post-image"
              :class="getImageClass(post.images.length)"
              :src="img"
              mode="aspectFill"
              @click="previewImage(post.images, idx)"
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

          <view class="post-actions">
            <view class="actions-left">
              <view class="action-item" @click="onLikeTap">
                <text class="action-icon">{{ post.liked ? '❤️' : '🤍' }}</text>
                <text class="action-count">{{ formatNumber(post.likes) }}</text>
              </view>
              <view class="action-item" @click="onEeTap">
                <text class="action-icon">{{ post.eeLiked ? '⭐' : '☆' }}</text>
                <text class="action-count">{{ formatNumber(post.eeCount) }}</text>
              </view>
              <view class="action-item" @click="focusCommentInput">
                <text class="action-icon">💬</text>
                <text class="action-count">{{ formatNumber(post.comments) }}</text>
              </view>
            </view>
            <view class="action-item share-btn-wrap">
              <button class="share-btn" open-type="share" :data-id="post.id">
                <text class="action-icon">↗️</text>
              </button>
            </view>
          </view>
        </view>

        <view class="comment-card">
          <view class="comment-header">
            <text class="comment-title">全部评论 ({{ totalCommentCount }})</text>
          </view>

          <view v-if="commentList.length === 0" class="no-comment">
            <text class="no-comment-emoji">💬</text>
            <text class="no-comment-text">暂无评论，快来抢沙发吧~</text>
          </view>

          <view v-else class="comment-list">
            <view
              v-for="comment in commentList"
              :key="comment.id"
              class="comment-item"
            >
              <image class="comment-avatar" :src="getUserAvatar(comment.userId, comment.userAvatar)" mode="aspectFill" />
              <view class="comment-info">
                <view class="comment-main">
                  <text class="comment-username">{{ comment.userName }}：</text>
                  <text class="comment-content">{{ comment.content }}</text>
                </view>
                <view class="comment-footer">
                  <text class="comment-time">{{ comment.relativeTime }}</text>
                  <text class="comment-reply-btn" @click="onReplyTap(comment)">回复</text>
                </view>

                <view v-if="comment.replies && comment.replies.length" class="reply-list">
                  <view
                    v-for="reply in comment.replies"
                    :key="reply.id"
                    class="reply-item"
                  >
                    <image class="reply-avatar" :src="getUserAvatar(reply.userId, reply.userAvatar)" mode="aspectFill" />
                    <view class="reply-info">
                      <view class="reply-main">
                        <text class="reply-username">{{ reply.userName }}</text>
                        <text v-if="reply.replyToUserName" class="reply-to">回复 {{ reply.replyToUserName }}</text>
                        <text class="reply-colon">：</text>
                        <text class="reply-content">{{ reply.content }}</text>
                      </view>
                      <view class="comment-footer">
                        <text class="comment-time">{{ getRelativeTime(reply.createdAt) }}</text>
                        <text class="comment-reply-btn" @click="onReplyTap(reply, comment)">回复</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="comment-input-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <view v-if="replyTarget" class="reply-hint">
        <text class="reply-hint-text">回复 {{ replyTarget.userName }}</text>
        <text class="reply-hint-close" @click="cancelReply">✕</text>
      </view>
      <view class="comment-input-row">
        <input
          class="comment-input"
          v-model="newComment"
          :placeholder="replyTarget ? `回复 ${replyTarget.userName}...` : '写评论...'"
          @confirm="addComment"
        />
        <text class="send-btn" @click="addComment">发送</text>
      </view>
    </view>
  </view>
</template>

<script>
import * as postApi from '@/api/post'
import { checkLogin, getUserAvatar as resolveUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'

export default {
  data() {
    return {
      postId: null,
      post: null,
      commentList: [],
      totalCommentCount: 0,
      loading: true,
      newComment: '',
      safeAreaBottom: 0,
      statusBarHeight: 20,
      navHeight: 64,
      replyTarget: null,
      replyParentComment: null
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      this.safeAreaBottom = sys.safeAreaInsets ? sys.safeAreaInsets.bottom : 0
      this.navHeight = this.statusBarHeight + 44
    } catch (e) {
      this.navHeight = 64
    }

    if (options.id) {
      this.postId = options.id
      this.loadPostDetail()
    } else {
      uni.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          uni.navigateBack()
        } else {
          uni.switchTab({ url: '/pages/home/index' })
        }
      }, 1500)
    }
  },
  onShareAppMessage() {
    this.recordShare()
    return {
      title: this.post && this.post.content ? this.post.content.substring(0, 30) + (this.post.content.length > 30 ? '...' : '') : '萌宠动态',
      path: `/pages/post/detail?id=${this.postId}`,
      imageUrl: this.post && this.post.images && this.post.images.length > 0 ? this.post.images[0] : ''
    }
  },
  onShareTimeline() {
    return {
      title: this.post && this.post.content ? this.post.content.substring(0, 30) : '萌宠动态',
      query: `id=${this.postId}`,
      imageUrl: this.post && this.post.images && this.post.images.length > 0 ? this.post.images[0] : ''
    }
  },
  methods: {
    async loadPostDetail() {
      this.loading = true
      try {
        const res = await postApi.getPostDetail(this.postId)
        if (res.success && res.data) {
          const post = res.data
          this.post = {
            id: post.id,
            userId: post.userId,
            petId: post.petId,
            content: post.content,
            images: post.imageList || [],
            likes: post.likeCount || 0,
            comments: post.commentCount || 0,
            eeCount: post.eeCount || 0,
            liked: post.liked || false,
            eeLiked: post.eeLiked || false,
            userName: post.userName || '未知用户',
            userAvatar: resolveUserAvatar(post.userId, post.userAvatar),
            petName: post.petName || '',
            petType: post.petType || 0,
            petAge: post.petAge || 0,
            relativeTime: this.getRelativeTime(post.createdAt),
            createdAt: post.createdAt,
            stickers: post.stickers || [],
            bubble: post.bubble || null,
            location: post.location || ''
          }
          this.totalCommentCount = post.commentCount || 0
          this.loadComments()
        }
      } catch (error) {
        console.error('加载动态详情失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    async loadComments() {
      try {
        const res = await postApi.getComments(this.postId, 1, 100)
        if (res.success && Array.isArray(res.data)) {
          this.commentList = res.data.map(comment => ({
            ...comment,
            relativeTime: this.getRelativeTime(comment.createdAt),
            replies: (comment.replies || []).map(r => ({
              ...r,
              relativeTime: this.getRelativeTime(r.createdAt)
            }))
          }))
        }
      } catch (error) {
        console.error('加载评论失败:', error)
      }
    },

    async onLikeTap() {
      const loggedIn = await checkLogin('请先登录后再点赞')
      if (!loggedIn) return
      try {
        const res = await postApi.toggleLike(this.postId)
        if (res.success) {
          this.post.liked = res.data.liked
          this.post.likes = res.data.likeCount
        }
      } catch (error) {
        console.error('点赞失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    async onEeTap() {
      const loggedIn = await checkLogin('请先登录后再收藏')
      if (!loggedIn) return
      try {
        const res = await postApi.toggleEe(this.postId)
        if (res.success && res.data) {
          this.post.eeLiked = res.data.eeLiked
          this.post.eeCount = res.data.eeCount
          uni.showToast({ title: res.data.eeLiked ? '收藏成功' : '取消收藏', icon: 'success' })
        }
      } catch (error) {
        console.error('收藏失败:', error)
        uni.showToast({ title: '操作失败，请重试', icon: 'none' })
      }
    },

    async recordShare() {
      try {
        await postApi.sharePost(this.postId)
      } catch (e) {
        console.warn('分享统计失败:', e)
      }
    },

    onReplyTap(comment, parentComment) {
      this.replyTarget = comment
      this.replyParentComment = parentComment || comment
    },

    cancelReply() {
      this.replyTarget = null
      this.replyParentComment = null
      this.newComment = ''
    },

    focusCommentInput() {
      this.cancelReply()
    },

    async addComment() {
      if (!this.newComment.trim()) {
        uni.showToast({ title: '请输入评论内容', icon: 'none' })
        return
      }
      const loggedIn = await checkLogin('请先登录后再评论')
      if (!loggedIn) return
      try {
        const data = { content: this.newComment.trim() }
        if (this.replyTarget && this.replyParentComment) {
          data.parentId = this.replyParentComment.id
          data.replyToId = this.replyTarget.id
        }
        const res = await postApi.createComment(this.postId, data)
        if (res.success) {
          this.newComment = ''
          this.replyTarget = null
          this.replyParentComment = null
          this.post.comments = (this.post.comments || 0) + 1
          this.totalCommentCount = (this.totalCommentCount || 0) + 1
          this.loadComments()
          uni.showToast({ title: '评论成功', icon: 'success' })
        } else {
          uni.showToast({ title: res.message || '评论失败', icon: 'none' })
        }
      } catch (error) {
        console.error('评论失败:', error)
        uni.showToast({ title: '评论失败', icon: 'none' })
      }
    },

    previewImage(images, index) {
      uni.previewImage({ urls: images, current: index })
    },

    getPetIcon(type) {
      const map = { 1: '🐱', 2: '🐕', 3: '🐰', 4: '🐦' }
      return map[type] || '🐾'
    },

    getUserAvatar(userId, avatarUrl) {
      return resolveUserAvatar(userId, avatarUrl)
    },

    formatNumber(num) {
      if (!num) return '0'
      if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
      return num.toString()
    },

    getImageClass(count) {
      if (count === 1) return 'post-image--1'
      if (count === 2) return 'post-image--2'
      if (count === 3) return 'post-image--3'
      return 'post-image--grid'
    },

    getRelativeTime(timestamp) {
      if (!timestamp) return ''
      let timeMs = 0
      if (typeof timestamp === 'number') timeMs = timestamp
      else if (typeof timestamp === 'string') timeMs = new Date(timestamp.replace(' ', 'T')).getTime()
      if (isNaN(timeMs) || timeMs === 0) return ''
      const diff = Date.now() - timeMs
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 172800000) return '昨天'
      if (diff < 259200000) return Math.floor(diff / 86400000) + '天前'
      const date = new Date(timeMs)
      return `${date.getMonth() + 1}/${date.getDate()}`
    },

    goUserProfile() {
      if (this.post.userId) {
        uni.navigateTo({ url: `/pages/user/detail?id=${this.post.userId}` })
      }
    },

    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.switchTab({ url: '/pages/home/index' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  width: 100%;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.nav-bar {
  height: 44px;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-icon {
  font-size: 36rpx;
  color: #fff;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

.nav-placeholder {
  width: 60rpx;
}

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.detail-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
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

.post-image-wrap {
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  overflow: hidden;
  display: flex;
  flex-wrap: wrap;
}

.post-image {
  border-radius: 16rpx;
  background: #f3f4f6;
}

.post-image--1 {
  width: 100%;
  height: 400rpx;
}

.post-image--2 {
  width: calc(50% - 6rpx);
  height: 280rpx;
  margin-right: 12rpx;
  margin-bottom: 12rpx;
}

.post-image--3 {
  width: calc(33.333% - 8rpx);
  height: 200rpx;
  margin-right: 12rpx;
  margin-bottom: 12rpx;
}

.post-image--grid {
  width: calc(33.333% - 8rpx);
  height: 180rpx;
  margin-right: 12rpx;
  margin-bottom: 12rpx;
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
}

.action-count {
  font-size: 24rpx;
  font-weight: 600;
}

.comment-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.comment-header {
  margin-bottom: 24rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.comment-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.no-comment {
  padding: 60rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.no-comment-emoji {
  font-size: 60rpx;
  margin-bottom: 16rpx;
}

.no-comment-text {
  font-size: 26rpx;
  color: #9ca3af;
}

.comment-list {
  padding-bottom: 20rpx;
}

.comment-item {
  display: flex;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.comment-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  background: #e5e7eb;
  flex-shrink: 0;
}

.comment-info {
  flex: 1;
  min-width: 0;
}

.comment-main {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 12rpx;
}

.comment-username {
  font-size: 26rpx;
  font-weight: 600;
  color: #ff6a3d;
}

.comment-content {
  font-size: 26rpx;
  color: #374151;
  line-height: 36rpx;
}

.comment-footer {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.comment-time {
  font-size: 22rpx;
  color: #9ca3af;
}

.comment-reply-btn {
  font-size: 22rpx;
  color: #ff6a3d;
}

.reply-list {
  margin-top: 16rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 16rpx;
}

.reply-item {
  display: flex;
  margin-bottom: 16rpx;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  background: #e5e7eb;
  flex-shrink: 0;
}

.reply-info {
  flex: 1;
  min-width: 0;
}

.reply-main {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 8rpx;
}

.reply-username {
  font-size: 24rpx;
  font-weight: 600;
  color: #ff6a3d;
  margin-right: 4rpx;
}

.reply-to {
  font-size: 22rpx;
  color: #9ca3af;
  margin-right: 4rpx;
}

.reply-colon {
  font-size: 24rpx;
  color: #374151;
  margin-right: 4rpx;
}

.reply-content {
  font-size: 24rpx;
  color: #374151;
  line-height: 34rpx;
}

.comment-input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
  padding: 16rpx 24rpx;
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 16rpx;
  margin-bottom: 12rpx;
  background: #fff5f0;
  border-radius: 12rpx;
  border: 1rpx solid #ffe0d0;
}

.reply-hint-text {
  font-size: 24rpx;
  color: #ff6a3d;
  font-weight: 500;
}

.reply-hint-close {
  font-size: 28rpx;
  color: #ff6a3d;
  padding: 0 8rpx;
}

.comment-input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.comment-input {
  flex: 1;
  height: 72rpx;
  background: #f5f5f5;
  border-radius: 999rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.send-btn {
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);
}
</style>
