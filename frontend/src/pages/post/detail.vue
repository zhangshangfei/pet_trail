<template>
  <view class="post-detail-page">
    <!-- 加载中 -->
    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 动态详情 -->
    <scroll-view v-else-if="post" scroll-y class="detail-scroll">
      <!-- 用户信息头部 -->
      <view class="post-header">
        <image class="post-avatar" :src="post.userAvatar" mode="aspectFill" />
        <view class="post-info">
          <text class="post-name">{{ post.userName }}</text>
          <view class="post-pet-info">
            <text class="pet-icon">{{ getPetIcon(post.petType) }}</text>
            <text class="pet-name">{{ post.petName || '未知宠物' }}</text>
            <text class="pet-age" v-if="post.petAge"> | {{ post.petAge }}岁</text>
          </view>
        </view>
        <text class="post-time">{{ post.relativeTime }}</text>
      </view>

      <!-- 内容区 -->
      <view class="post-content-wrap">
        <text class="post-content">{{ post.content }}</text>
      </view>

      <!-- 图片展示 -->
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

      <!-- 操作栏 -->
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
          <view class="action-item">
            <text class="action-icon">💬</text>
            <text class="action-count">{{ formatNumber(post.comments) }}</text>
          </view>
        </view>
        <view class="action-item" @click="onShareTap">
          <text class="action-icon">↗️</text>
        </view>
      </view>

      <!-- 评论列表 -->
      <view class="comment-section">
        <view class="comment-header">
          <text class="comment-title">全部评论 ({{ commentList.length }})</text>
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
            <image class="comment-avatar" :src="comment.userAvatar" mode="aspectFill" />
            <view class="comment-info">
              <text class="comment-username">{{ comment.userName }}</text>
              <text class="comment-content">{{ comment.content }}</text>
              <view class="comment-footer">
                <text class="comment-time">{{ comment.relativeTime }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部评论输入框 -->
    <view class="comment-input-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <input
        class="comment-input"
        v-model="newComment"
        placeholder="写评论..."
        @confirm="addComment"
      />
      <text class="send-btn" @click="addComment">发送</text>
    </view>
  </view>
</template>

<script>
import * as postApi from '@/api/post'

export default {
  data() {
    return {
      postId: null,
      post: null,
      commentList: [],
      loading: true,
      newComment: '',
      safeAreaBottom: 0
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
    // 加载动态详情
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
        }
      } catch (error) {
        console.error('加载动态详情失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    // 加载评论列表
    async loadComments() {
      try {
        const res = await postApi.getComments(this.postId, 1, 100)
        if (res.success && Array.isArray(res.data)) {
          this.commentList = res.data.map(comment => ({
            id: comment.id,
            userId: comment.userId,
            userName: comment.userName || '未知用户',
            userAvatar: comment.userAvatar || '',
            content: comment.content || '',
            createdAt: comment.createdAt,
            relativeTime: this.getRelativeTime(comment.createdAt)
          }))
        }
      } catch (error) {
        console.error('加载评论失败:', error)
      }
    },

    // 点赞
    async onLikeTap() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showModal({
          title: '提示',
          content: '请先登录后再点赞',
          showCancel: true,
          confirmText: '去登录',
          success: (res) => {
            if (res.confirm) {
              this.onWechatLogin()
            }
          }
        })
        return
      }

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

    // 收藏
    async onEeTap() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showModal({
          title: '提示',
          content: '请先登录后再收藏',
          showCancel: true,
          confirmText: '去登录',
          success: (res) => {
            if (res.confirm) {
              this.onWechatLogin()
            }
          }
        })
        return
      }

      try {
        const res = await postApi.toggleEe(this.postId)
        if (res.success && res.data) {
          this.post.eeLiked = res.data.eeLiked
          this.post.eeCount = res.data.eeCount
        }
      } catch (error) {
        console.error('收藏失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    // 分享
    onShareTap() {
      uni.showToast({ title: '分享功能开发中', icon: 'none' })
    },

    // 添加评论
    async addComment() {
      if (!this.newComment.trim()) {
        uni.showToast({ title: '请输入评论内容', icon: 'none' })
        return
      }

      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showModal({
          title: '提示',
          content: '请先登录后再评论',
          showCancel: true,
          confirmText: '去登录',
          success: (res) => {
            if (res.confirm) {
              this.onWechatLogin()
            }
          }
        })
        return
      }

      try {
        // TODO: 调用后端API添加评论
        // const res = await postApi.createComment(this.postId, { content: this.newComment })
        
        // 暂时本地添加
        const userInfo = uni.getStorageSync('userInfo') || {}
        const newComment = {
          id: Date.now(),
          userId: userInfo.id || 0,
          userName: userInfo.nickname || '我',
          userAvatar: userInfo.avatar || '',
          content: this.newComment,
          createdAt: new Date().toISOString(),
          relativeTime: '刚刚'
        }
        
        this.commentList.push(newComment)
        this.post.comments = (this.post.comments || 0) + 1
        this.newComment = ''
        
        uni.showToast({ title: '评论成功', icon: 'success' })
      } catch (error) {
        console.error('评论失败:', error)
        uni.showToast({ title: '评论失败', icon: 'none' })
      }
    },

    // 预览图片
    previewImage(images, index) {
      uni.previewImage({
        urls: images,
        current: index
      })
    },

    // 获取宠物图标
    getPetIcon(type) {
      switch(type) {
        case 1: return '🐱'
        case 2: return '🐕'
        case 3: return '🐰'
        case 4: return '🐦'
        default: return '🐾'
      }
    },

    // 格式化数字
    formatNumber(num) {
      if (!num) return '0'
      if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k'
      }
      return num.toString()
    },

    // 获取图片样式
    getImageClass(count) {
      if (count === 1) return 'post-image--1'
      if (count === 2) return 'post-image--2'
      if (count === 3) return 'post-image--3'
      return 'post-image--grid'
    },

    // 获取相对时间
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

    // 微信登录
    onWechatLogin() {
      uni.showLoading({ title: '登录中...', mask: true })
      wx.login({
        success: async (res) => {
          if (res.code) {
            try {
              const loginRes = await uni.$request.post('/api/users/login', { code: res.code })
              uni.hideLoading()
              if (loginRes.success) {
                uni.setStorageSync('token', loginRes.data.token)
                uni.setStorageSync('userInfo', loginRes.data.user)
                uni.showToast({ title: '登录成功', icon: 'success' })
              } else {
                uni.showToast({ title: loginRes.message || '登录失败', icon: 'none' })
              }
            } catch (err) {
              uni.hideLoading()
              uni.showToast({ title: '网络错误', icon: 'none' })
            }
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f9fafb;
  padding-bottom: 120rpx;
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

/* 动态头部 */
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

/* 内容区 */
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

/* 图片展示 */
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

/* 操作栏 */
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
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

.action-count {
  font-size: 24rpx;
  font-weight: 600;
}

/* 评论区域 */
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
}

.comment-info {
  flex: 1;
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
}

.comment-time {
  font-size: 22rpx;
  color: #9ca3af;
}

/* 底部输入框 */
.comment-input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
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
