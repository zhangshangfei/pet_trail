<template>
  <view class="post-detail-page">
    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>

    <scroll-view v-else-if="post" scroll-y class="detail-scroll">
      <view class="post-header">
        <image class="post-avatar" :src="post.userAvatar" mode="aspectFill" @tap="goUserProfile" />
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

      <view class="post-actions">
        <view class="actions-left">
          <view class="action-item" @click="onLikeTap">
            <text class="action-icon" :class="{ 'action-icon--liked': post.liked }">{{ post.liked ? '❤️' : '🤍' }}</text>
            <text class="action-count">{{ formatNumber(post.likes) }}</text>
          </view>
          <view class="action-item" @click="onEeTap">
            <text class="action-icon" :class="{ 'action-icon--liked': post.eeLiked }">{{ post.eeLiked ? '⭐' : '☆' }}</text>
            <text class="action-count">{{ formatNumber(post.eeCount) }}</text>
          </view>
          <view class="action-item" @click="focusCommentInput">
            <text class="action-icon">💬</text>
            <text class="action-count">{{ formatNumber(post.comments) }}</text>
          </view>
        </view>
        <view class="action-item" @click="onShareTap">
          <text class="action-icon">↗️</text>
        </view>
      </view>

      <view class="comment-section">
        <view class="comment-header">
          <text class="comment-title">全部评论 ({{ totalCommentCount }})</text>
        </view>

        <view v-if="commentList.length === 0" class="no-comment">
          <text class="no-comment-text">暂无评论，快来抢沙发吧~</text>
        </view>

        <view v-else class="comment-list">
          <view
            v-for="comment in commentList"
            :key="comment.id"
            class="comment-item"
          >
            <image class="comment-avatar" :src="comment.userAvatar || ''" mode="aspectFill" />
            <view class="comment-info">
              <text class="comment-username">{{ comment.userName }}</text>
              <text class="comment-content">{{ comment.content }}</text>
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
                  <image class="reply-avatar" :src="reply.userAvatar || ''" mode="aspectFill" />
                  <view class="reply-info">
                    <text class="reply-username">{{ reply.userName }}</text>
                    <text v-if="reply.replyToUserName" class="reply-to">回复 {{ reply.replyToUserName }}</text>
                    <text class="reply-content">{{ reply.content }}</text>
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
    </scroll-view>

    <view class="comment-input-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <view v-if="replyTarget" class="reply-hint">
        <text class="reply-hint-text">回复 {{ replyTarget.userName }}</text>
        <text class="reply-hint-close" @click="cancelReply">✕</text>
      </view>
      <view class="comment-input-row">
        <input
          ref="commentInputRef"
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
import { checkLogin } from '@/utils/index'

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
      replyTarget: null,
      replyParentComment: null
    }
  },
  onLoad(options) {
    if (options.id) {
      this.postId = options.id
      this.loadPostDetail()
    } else {
      uni.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }

    try {
      const sys = uni.getSystemInfoSync()
      this.safeAreaBottom = sys.safeAreaInsets ? sys.safeAreaInsets.bottom : 0
    } catch (e) {
      this.safeAreaBottom = 0
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
            userAvatar: post.userAvatar || '',
            petName: post.petName || '',
            petType: post.petType || 0,
            petAge: post.petAge || 0,
            relativeTime: this.getRelativeTime(post.createdAt),
            createdAt: post.createdAt
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

    onShareTap() {
      uni.showToast({ title: '分享功能开发中', icon: 'none' })
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
      uni.previewImage({
        urls: images,
        current: index
      })
    },

    getPetIcon(type) {
      switch(type) {
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

    getImageClass(count) {
      if (count === 1) return 'post-image--1'
      if (count === 2) return 'post-image--2'
      if (count === 3) return 'post-image--3'
      return 'post-image--grid'
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

    goUserProfile() {
      if (this.post.userId) {
        uni.navigateTo({ url: `/pages/user/detail?id=${this.post.userId}` })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f9fafb;
  padding-bottom: 140rpx;
}

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.detail-scroll {
  height: 100vh;
}

.post-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #fff;
  margin-bottom: 20rpx;
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
  padding: 0 24rpx 24rpx;
  background: #fff;
  margin-bottom: 20rpx;
}

.post-content {
  display: block;
  font-size: 28rpx;
  color: #374151;
  line-height: 40rpx;
}

.post-image-wrap {
  padding: 0 24rpx 24rpx;
  background: #fff;
  margin-bottom: 20rpx;
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

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #fff;
  margin-bottom: 20rpx;
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

.comment-section {
  background: #fff;
  padding: 24rpx;
}

.comment-header {
  margin-bottom: 24rpx;
}

.comment-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}

.no-comment {
  padding: 60rpx 0;
  text-align: center;
}

.no-comment-text {
  font-size: 26rpx;
  color: #999;
}

.comment-list {
  padding-bottom: 20rpx;
}

.comment-item {
  display: flex;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #f3f4f6;
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

.comment-username {
  font-size: 26rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8rpx;
}

.comment-content {
  font-size: 26rpx;
  color: #374151;
  line-height: 36rpx;
  margin-bottom: 12rpx;
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
  color: #3b82f6;
}

.reply-list {
  margin-top: 16rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 12rpx;
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

.reply-username {
  font-size: 24rpx;
  font-weight: 600;
  color: #374151;
  margin-right: 8rpx;
}

.reply-to {
  font-size: 22rpx;
  color: #9ca3af;
  margin-right: 8rpx;
}

.reply-content {
  font-size: 24rpx;
  color: #374151;
  line-height: 34rpx;
  margin-top: 4rpx;
}

.comment-input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  padding: 16rpx 24rpx;
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8rpx 12rpx;
  margin-bottom: 12rpx;
  background: #f3f4f6;
  border-radius: 8rpx;
}

.reply-hint-text {
  font-size: 24rpx;
  color: #6b7280;
}

.reply-hint-close {
  font-size: 28rpx;
  color: #9ca3af;
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
  background: #f3f4f6;
  border-radius: 999rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.send-btn {
  padding: 16rpx 32rpx;
  background: #3b82f6;
  color: #fff;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 600;
}
</style>
