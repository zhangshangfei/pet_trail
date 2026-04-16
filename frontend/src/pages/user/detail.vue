<template>
  <view class="user-detail-page">
    <view class="user-header">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">←</text>
        </view>
        <view v-if="isSelf" class="header-edit-btn" @tap="goEditProfile">
          <text class="header-edit-text">编辑</text>
        </view>
      </view>

      <view class="user-info-section">
        <view class="avatar-wrap">
          <image class="user-avatar" :src="getUserAvatar(userId, userInfo.avatar)" mode="aspectFill" />
        </view>
        <text class="user-nickname">{{ userInfo.nickname || '萌宠主人' }}</text>
        <view class="user-meta">
          <text class="user-gender" v-if="userInfo.gender">{{ genderText }}</text>
          <text class="user-join" v-if="userInfo.createdAt">加入于 {{ joinDate }}</text>
        </view>

        <view class="user-stats">
          <view class="stat-item" @tap="onStatTap('posts')">
            <text class="stat-num">{{ userInfo.postCount || 0 }}</text>
            <text class="stat-label">动态</text>
          </view>
          <view class="stat-item" @tap="onStatTap('followers')">
            <text class="stat-num">{{ userInfo.followerCount || 0 }}</text>
            <text class="stat-label">粉丝</text>
          </view>
          <view class="stat-item" @tap="onStatTap('following')">
            <text class="stat-num">{{ userInfo.followeeCount || 0 }}</text>
            <text class="stat-label">关注</text>
          </view>
        </view>

        <view v-if="!isSelf" class="action-row">
          <button
            class="follow-btn"
            :class="{ 'follow-btn--following': userInfo.isFollowing }"
            @tap="onToggleFollow"
            :loading="followLoading"
          >
            {{ userInfo.isFollowing ? '已关注' : '+ 关注' }}
          </button>
        </view>
      </view>
    </view>

    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'posts' }"
        @tap="switchTab('posts')"
      >
        <text class="tab-text">动态</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'likes' }"
        @tap="switchTab('likes')"
      >
        <text class="tab-text">喜欢</text>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="post-scroll"
      @scrolltolower="loadMore"
    >
      <view class="post-list">
        <view
          v-for="post in postList"
          :key="post.id"
          class="post-card"
        >
          <view class="post-header">
            <image class="post-avatar" :src="post.avatar" mode="aspectFill" />
            <view class="post-info">
              <text class="post-name">{{ post.userName }}</text>
              <view class="post-pet-info" v-if="post.petName">
                <text class="pet-icon">{{ getPetIcon(post.petType) }}</text>
                <text class="pet-name">{{ post.petName }}</text>
                <text class="pet-age" v-if="post.petAge"> | {{ post.petAge }}岁</text>
              </view>
            </view>
            <text class="post-time">{{ post.relativeTime || post.time }}</text>
          </view>

          <view class="post-content-wrap" v-if="post.content">
            <text
              class="post-content"
              :class="{ 'line-clamp': !expandedPosts[post.id] && post.content.length > 100 }"
            >{{ post.content }}</text>
            <text
              v-if="post.content.length > 100 && !expandedPosts[post.id]"
              class="expand-btn"
              @click="toggleExpand(post.id)"
            >展开</text>
          </view>

          <view
            v-if="post.imageList && post.imageList.length"
            class="post-image-wrap"
          >
            <image
              v-for="(img, idx) in post.imageList"
              :key="idx"
              class="post-image"
              :class="getImageClass(post.imageList.length)"
              :src="img"
              mode="aspectFill"
              @click="previewImage(post.imageList, idx)"
            />
          </view>

          <view
            v-if="post.videoList && post.videoList.length"
            class="post-video-wrap"
          >
            <view
              v-for="(vid, vidx) in post.videoList"
              :key="'v' + vidx"
              class="post-video-item"
              @click="playVideo(vid)"
            >
              <video
                class="post-video"
                :src="vid"
                :autoplay="false"
                :show-play-btn="true"
                :show-center-play-btn="true"
                :enable-progress-gesture="false"
                object-fit="cover"
              />
            </view>
          </view>

          <view class="post-actions">
            <view class="actions-left">
              <view class="action-item" @click="onLikeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.liked }">{{ post.liked ? '❤️' : '🤍' }}</text>
                <text class="action-count">{{ post.likeCount || 0 }}</text>
              </view>
              <view class="action-item" @click="onEeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.eeLiked }">{{ post.eeLiked ? '⭐' : '☆' }}</text>
                <text class="action-count">{{ post.eeCount || 0 }}</text>
              </view>
              <view class="action-item" @click="openPostDetail(post)">
                <text class="action-icon">💬</text>
                <text class="action-count">{{ post.commentCount || 0 }}</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="loading" class="loading-text">加载中...</view>
        <view v-if="!hasMore && postList.length > 0" class="no-more-text">没有更多了</view>
        <view v-if="!loading && postList.length === 0" class="empty-state">
          <text class="empty-emoji">📝</text>
          <text class="empty-text">暂无动态</text>
        </view>
      </view>
    </scroll-view>

    <!-- 视频播放弹窗 -->
    <view v-if="showVideoPlayer" class="video-player-mask" @click="closeVideoPlayer">
      <view class="video-player-container" @click.stop>
        <view class="video-player-close" @click="closeVideoPlayer">✕</view>
        <video
          class="video-player-video"
          :src="currentVideoUrl"
          :autoplay="true"
          :show-play-btn="true"
          :show-center-play-btn="true"
          :enable-progress-gesture="true"
          :show-fullscreen-btn="true"
          object-fit="contain"
        />
      </view>
    </view>
  </view>
</template>

<script>
import * as postApi from '@/api/post'
import { getUserById } from '@/api/auth'
import { checkLogin, getUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'

const defaultAvatar = DEFAULT_USER_AVATAR

export default {
  data() {
    return {
      userId: null,
      defaultAvatar,
      userInfo: {},
      postList: [],
      currentTab: 'posts',
      page: 1,
      size: 20,
      loading: false,
      hasMore: true,
      followLoading: false,
      expandedPosts: {},
      showVideoPlayer: false,
      currentVideoUrl: ''
    }
  },
  computed: {
    isSelf() {
      const currentUserId = uni.getStorageSync('userInfo')?.id
      return currentUserId && this.userId && Number(currentUserId) === Number(this.userId)
    },
    genderText() {
      const map = { 1: '♂', 2: '♀' }
      return map[this.userInfo.gender] || ''
    },
    joinDate() {
      if (!this.userInfo.createdAt) return ''
      const d = new Date(this.userInfo.createdAt)
      return `${d.getFullYear()}年${d.getMonth() + 1}月`
    }
  },
  onLoad(options) {
    if (options.id) {
      this.userId = Number(options.id)
      this.loadUserInfo()
      this.loadPosts()
    }
  },
  onShow() {
    if (this.userId) {
      this.loadUserInfo()
    }
  },
  onPullDownRefresh() {
    if (this.userId) {
      this.loadUserInfo()
      this.loadPosts()
    }
    setTimeout(() => { uni.stopPullDownRefresh() }, 800)
  },
  methods: {
    getUserAvatar,
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    async loadUserInfo() {
      try {
        const res = await getUserById(this.userId)
        if (res && res.success) {
          this.userInfo = res.data
        }
      } catch (e) {
        console.error('获取用户信息失败:', e)
      }
    },
    async loadPosts() {
      if (this.loading) return
      this.loading = true
      try {
        let res
        if (this.currentTab === 'likes') {
          res = await postApi.getUserLikedPosts(this.userId, this.page, this.size)
        } else {
          res = await postApi.getUserPosts(this.userId, this.page, this.size)
        }
        if (res && res.success && Array.isArray(res.data)) {
          const newPosts = res.data.map(post => ({
            ...post,
            avatar: getUserAvatar(post.userId, post.userAvatar),
            userName: post.userName || '萌宠主人',
            likes: post.likeCount || 0,
            comments: post.commentCount || 0,
            time: this.formatTime(post.createdAt),
            relativeTime: this.getRelativeTime(post.createdAt)
          }))
          if (this.page === 1) {
            this.postList = newPosts
          } else {
            this.postList = [...this.postList, ...newPosts]
          }
          this.hasMore = newPosts.length >= this.size
        } else {
          if (this.page === 1) this.postList = []
          this.hasMore = false
        }
      } catch (e) {
        console.error('获取动态失败:', e)
        if (this.page === 1) this.postList = []
        this.hasMore = false
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadPosts()
    },
    switchTab(tab) {
      if (this.currentTab === tab) return
      this.currentTab = tab
      this.page = 1
      this.postList = []
      this.hasMore = true
      this.loadPosts()
    },
    async onToggleFollow() {
      const loggedIn = await checkLogin('请先登录后再关注')
      if (!loggedIn) return

      if (this.followLoading) return
      this.followLoading = true
      try {
        const res = await postApi.toggleFollow(this.userId)
        if (res && res.success) {
          this.userInfo.isFollowing = res.data.following
          this.userInfo.followerCount = res.data.followerCount || this.userInfo.followerCount
          uni.showToast({
            title: res.data.following ? '关注成功' : '已取消关注',
            icon: 'none'
          })
        }
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      } finally {
        this.followLoading = false
      }
    },
    async onLikeTap(post) {
      const loggedIn = await checkLogin('请先登录后再点赞')
      if (!loggedIn) return
      try {
        const res = await postApi.toggleLike(post.id)
        if (res && res.success) {
          post.liked = res.data.liked
          post.likeCount = res.data.likeCount || post.likeCount
        }
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    async onEeTap(post) {
      const loggedIn = await checkLogin('请先登录后再收藏')
      if (!loggedIn) return
      try {
        const res = await postApi.toggleEe(post.id)
        if (res && res.success) {
          post.eeLiked = res.data.eeLiked
          post.eeCount = res.data.eeCount || post.eeCount
        }
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    openPostDetail(post) {
      uni.navigateTo({ url: `/pages/post/detail?id=${post.id}` })
    },
    toggleExpand(postId) {
      this.$set(this.expandedPosts, postId, true)
    },
    previewImage(images, index) {
      uni.previewImage({
        urls: images,
        current: index
      })
    },
    playVideo(url) {
      this.currentVideoUrl = url
      this.showVideoPlayer = true
    },
    closeVideoPlayer() {
      this.showVideoPlayer = false
      this.currentVideoUrl = ''
    },
    getPetIcon(type) {
      const map = { 1: '🐱', 2: '🐶' }
      return map[type] || '🐾'
    },
    getImageClass(count) {
      if (count === 1) return 'post-image--single'
      if (count === 2) return 'post-image--double'
      return 'post-image--grid'
    },
    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      return `${d.getMonth() + 1}-${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    getRelativeTime(dateStr) {
      if (!dateStr) return ''
      const now = Date.now()
      const then = new Date(dateStr).getTime()
      const diff = now - then
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      return this.formatTime(dateStr)
    },
    onStatTap(type) {
      if (type === 'posts') {
        this.switchTab('posts')
        return
      }
      uni.navigateTo({ url: `/pages/follows/index?userId=${this.userId}&type=${type}` })
    },
    goEditProfile() {
      uni.navigateTo({ url: '/pages/user/edit' })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.user-header {
  position: relative;
  background: #fff;
  padding-bottom: 24rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 360rpx;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.header-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  padding-top: calc(var(--status-bar-height, 20px) + 12rpx);
  height: 92rpx;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  color: #fff;
  font-size: 36rpx;
  font-weight: 700;
}

.header-edit-btn {
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.25);
}

.header-edit-text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
}

.user-info-section {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20rpx;
}

.avatar-wrap {
  position: relative;
  display: inline-flex;
}

.user-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 80rpx;
  border: 6rpx solid #fff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
  background: #e5e7eb;
}

.user-nickname {
  margin-top: 16rpx;
  font-size: 36rpx;
  font-weight: 800;
  color: #111827;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 8rpx;
}

.user-gender {
  font-size: 26rpx;
}

.user-join {
  font-size: 24rpx;
  color: #9ca3af;
}

.user-stats {
  display: flex;
  align-items: center;
  gap: 60rpx;
  margin-top: 28rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 34rpx;
  font-weight: 800;
  color: #111827;
}

.stat-label {
  font-size: 22rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.action-row {
  margin-top: 24rpx;
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 0 80rpx;
}

.follow-btn {
  width: 100%;
  height: 76rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  border: none;

  &--following {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.tab-bar {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #e5e7eb;
  margin-top: 16rpx;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  position: relative;

  &.active {
    .tab-text {
      color: #ff6a3d;
      font-weight: 700;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 6rpx;
      border-radius: 3rpx;
      background: #ff6a3d;
    }
  }
}

.tab-text {
  font-size: 28rpx;
  color: #6b7280;
}

.post-scroll {
  height: calc(100vh - 560rpx);
}

.post-list {
  padding: 16rpx 20rpx 200rpx;
}

.post-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.post-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 36rpx;
  margin-right: 16rpx;
  background: #e5e7eb;
}

.post-info {
  flex: 1;
}

.post-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
}

.post-pet-info {
  display: flex;
  align-items: center;
  margin-top: 4rpx;
}

.pet-icon {
  font-size: 22rpx;
  margin-right: 4rpx;
}

.pet-name {
  font-size: 22rpx;
  color: #6b7280;
}

.pet-age {
  font-size: 22rpx;
  color: #9ca3af;
}

.post-time {
  font-size: 22rpx;
  color: #9ca3af;
}

.post-content-wrap {
  margin-bottom: 16rpx;
}

.post-content {
  font-size: 28rpx;
  color: #1f2937;
  line-height: 1.6;
  word-break: break-all;

  &.line-clamp {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 4;
    overflow: hidden;
  }
}

.expand-btn {
  font-size: 26rpx;
  color: #3b82f6;
  margin-top: 8rpx;
}

.post-image-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.post-image {
  border-radius: 12rpx;
  overflow: hidden;

  &--single {
    width: 100%;
    height: 400rpx;
  }

  &--double {
    width: calc(50% - 4rpx);
    height: 280rpx;
  }

  &--grid {
    width: calc(33.33% - 6rpx);
    height: 220rpx;
  }
}

.post-video-wrap {
  margin-bottom: 16rpx;
}

.post-video-item {
  border-radius: 12rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.post-video {
  width: 100%;
  height: 420rpx;
  border-radius: 12rpx;
}

.post-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12rpx;
  border-top: 1rpx solid #f3f4f6;
}

.actions-left {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.action-icon {
  font-size: 30rpx;

  &--liked {
    animation: likeAnim 0.3s ease;
  }
}

@keyframes likeAnim {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.action-count {
  font-size: 24rpx;
  color: #6b7280;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: 32rpx 0;
  font-size: 24rpx;
  color: #9ca3af;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-emoji {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9ca3af;
}

.video-player-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player-container {
  position: relative;
  width: 100%;
}

.video-player-close {
  position: absolute;
  top: -80rpx;
  right: 20rpx;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.video-player-video {
  width: 100%;
  height: 420rpx;
}
</style>
