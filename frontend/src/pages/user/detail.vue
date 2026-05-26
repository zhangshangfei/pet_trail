<template>
  <view class="user-detail-page">
    <!-- 玻璃拟态头部区域 -->
    <view class="glass-header">
      <!-- 渐变背景 + 动态光球 -->
      <view class="header-bg">
        <view class="orb orb-1"></view>
        <view class="orb orb-2"></view>
        <view class="orb orb-3"></view>
      </view>

      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="back-btn" @tap="goBack">
          <view class="back-arrow"></view>
        </view>
        <view class="capsule-placeholder"></view>
        <view v-if="!isSelf" class="more-btn">
          <text class="more-dots">•••</text>
        </view>
      </view>

      <!-- 用户信息卡片 -->
      <view class="user-card glass-card-effect">
        <!-- 头像区域 -->
        <view class="avatar-section">
          <view class="avatar-wrap" @tap="onAvatarTap">
            <view class="avatar-glow"></view>
            <AvatarView :src="getUserAvatar(userId, userInfo.avatar)" :name="userInfo.nickname || '萌宠主人'" :id="userId" :size="140" />
            <view v-if="isSelf" class="avatar-edit-badge">
              <text class="avatar-edit-icon">✎</text>
            </view>
          </view>
        </view>

        <!-- 用户名和元信息 -->
        <view class="user-info">
          <text class="user-nickname">{{ userInfo.nickname || '萌宠主人' }}</text>
          <view class="user-meta">
            <view class="meta-tag" v-if="userInfo.gender">
              <text class="gender-icon">{{ genderText }}</text>
            </view>
            <text class="join-text" v-if="userInfo.createdAt">{{ joinDate }} 加入</text>
          </view>
        </view>

        <!-- 统计数据卡片组 -->
        <view class="stats-grid">
          <view class="stat-card glass-stat" @tap="onStatTap('posts')">
            <text class="stat-num">{{ userInfo.postCount || 0 }}</text>
            <text class="stat-label">动态</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-card glass-stat" @tap="onStatTap('followers')">
            <text class="stat-num">{{ userInfo.followerCount || 0 }}</text>
            <text class="stat-label">粉丝</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-card glass-stat" @tap="onStatTap('following')">
            <text class="stat-num">{{ userInfo.followeeCount || 0 }}</text>
            <text class="stat-label">关注</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view v-if="!isSelf" class="action-row">
          <button
            class="follow-btn"
            :class="{
              'follow-btn--follow': !userInfo.isFollowing,
              'follow-btn--following': userInfo.isFollowing
            }"
            @tap="onToggleFollow"
            :loading="followLoading"
          >
            <view v-if="!userInfo.isFollowing" class="btn-icon-plus">+</view>
            <text class="btn-text">{{ userInfo.isFollowing ? '已关注' : '关注' }}</text>
          </button>
        </view>

        <view v-if="!isSelf" class="report-link" :class="{ 'report-link--done': hasReported }" @tap="onReportUser">
          <text class="report-link-text">{{ hasReported ? '已举报该用户' : '举报该用户' }}</text>
        </view>
      </view>

      <!-- 波浪装饰 -->
      <view class="wave-decoration">
        <svg viewBox="0 0 1440 120" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
          <path class="wave-path wave-path-1" d="M0,64 C288,120 576,20 864,64 C1152,108 1344,40 1440,64 L1440,120 L0,120 Z" fill="rgba(255,107,107,0.12)"/>
          <path class="wave-path wave-path-2" d="M0,80 C360,130 720,30 1080,80 C1260,105 1380,60 1440,80 L1440,120 L0,120 Z" fill="rgba(255,160,122,0.08)"/>
          <path class="wave-path wave-path-3" d="M0,50 C240,90 480,10 720,50 C960,90 1200,30 1440,50 L1440,100 L0,100Z" fill="rgba(255,220,180,0.06)"/>
        </svg>
      </view>
    </view>

    <!-- Tab切换栏 -->
    <view class="tab-bar glass-tabbar">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'posts' }"
        @tap="switchTab('posts')"
      >
        <text class="tab-text">动态</text>
        <view v-if="currentTab === 'posts'" class="tab-indicator"></view>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'likes' }"
        @tap="switchTab('likes')"
      >
        <text class="tab-text">喜欢</text>
        <view v-if="currentTab === 'likes'" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 动态列表 -->
    <scroll-view
      scroll-y
      class="post-scroll"
      @scrolltolower="loadMore"
    >
      <view class="post-list">
        <view
          v-for="(post, index) in postList"
          :key="post.id"
          class="post-card glass-post-card"
          :style="{ animationDelay: index * 0.05 + 's' }"
        >
          <!-- 卡片顶部高光 -->
          <view class="post-card-highlight"></view>

          <view class="post-header">
            <AvatarView :src="post.avatar" :name="post.userName" :id="post.userId" :size="64" />
            <view class="post-info">
              <text class="post-name">{{ post.userName }}</text>
              <view class="post-pet-info" v-if="post.petName">
                <text class="pet-icon">{{ getPetIcon(post.petType) }}</text>
                <text class="pet-name">{{ post.petName }}</text>
                <text class="pet-age" v-if="post.petAge"> | {{ post.petAge }}</text>
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
            >展开全文</text>
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
            >
              <video-card
                :src="vid"
                :autoplay="false"
                :muted="true"
                object-fit="cover"
                :height="420"
              />
            </view>
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
              <view class="action-item glass-action" @click="onLikeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.liked }">{{ post.liked ? '❤️' : '🤍' }}</text>
                <text class="action-count">{{ post.likeCount || 0 }}</text>
              </view>
              <view class="action-item glass-action" @click="onEeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.eeLiked }">{{ post.eeLiked ? '⭐' : '☆' }}</text>
                <text class="action-count">{{ post.eeCount || 0 }}</text>
              </view>
              <view class="action-item glass-action" @click="openPostDetail(post)">
                <text class="action-icon">💬</text>
                <text class="action-count">{{ post.commentCount || 0 }}</text>
              </view>
            </view>
            <view v-if="isSelf" class="action-item delete-action" @click="onDeletePost(post)">
              <text class="action-icon delete-icon">🗑️</text>
            </view>
          </view>
        </view>

        <view v-if="loading" class="loading-state">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>
        <view v-if="!hasMore && postList.length > 0" class="no-more-state">
          <view class="divider-line"></view>
          <text class="no-more-text">— 已经到底啦 —</text>
        </view>
        <view v-if="!loading && postList.length === 0" class="empty-state">
          <view class="empty-icon">📝</view>
          <text class="empty-title">暂无动态</text>
          <text class="empty-desc">快来发布第一条动态吧~</text>
        </view>
      </view>
    </scroll-view>

  </view>
</template>

<script>
import * as postApi from '@/api/post'
import * as reportApi from '@/api/report'
import { getUserById } from '@/api/auth'
import { checkLogin, getUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'
import VideoCard from '@/components/VideoCard.vue'
import AvatarView from '@/components/AvatarView.vue'

const defaultAvatar = DEFAULT_USER_AVATAR

export default {
  components: { VideoCard, AvatarView },
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
      hasReported: false,
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
      this.refreshPosts()
    }
  },
  onPullDownRefresh() {
    if (this.userId) {
      this.loadUserInfo()
      this.loadPosts()
    }
    setTimeout(() => { uni.stopPullDownRefresh() }, 800)
  },
  onUnload() {
    uni.$off('postDeleted')
  },
  methods: {
    getUserAvatar,
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    onReportUser() {
      if (this.hasReported) {
        uni.showToast({ title: '您已举报过该用户', icon: 'none' })
        return
      }
      uni.showActionSheet({
        itemList: ['垃圾广告', '色情低俗', '虚假信息', '违法违规', '人身攻击', '其他'],
        success: async (res) => {
          const reasons = ['spam', 'porn', 'fake', 'illegal', 'abuse', 'other']
          const reason = reasons[res.tapIndex]
          try {
            const result = await reportApi.createReport({
              targetId: this.userId,
              targetType: 'user',
              reason: reason
            })
            if (result.success) {
              this.hasReported = true
              uni.showToast({ title: '举报已提交', icon: 'success' })
            } else {
              uni.showToast({ title: result.message || '举报失败', icon: 'none' })
            }
          } catch (error) {
            console.error('举报失败:', error)
            uni.showToast({ title: '举报失败', icon: 'none' })
          }
        }
      })
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
      if (!this.isSelf) {
        this.checkFollowStatus()
        this.checkReportStatus()
      }
    },
    async checkReportStatus() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) return
        const res = await reportApi.checkReported(this.userId, 'user')
        if (res && res.success && res.data) {
          this.hasReported = res.data.reported
        }
      } catch (e) {
        this.hasReported = false
      }
    },
    async checkFollowStatus() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) return
        const res = await postApi.checkFollow(this.userId)
        if (res && res.success && res.data) {
          this.$set(this.userInfo, 'isFollowing', res.data.following)
        }
      } catch (e) {
        // ignore
      }
    },
    async loadPosts(forceRefresh = false) {
      if (this.loading) return
      this.loading = true
      try {
        let res
        if (this.currentTab === 'likes') {
          res = await postApi.getUserLikedPosts(this.userId, this.page, this.size)
        } else {
          res = await postApi.getUserPosts(this.userId, this.page, this.size, forceRefresh)
        }
        if (res && res.success && Array.isArray(res.data)) {
          const newPosts = res.data.map(post => ({
            ...post,
            avatar: getUserAvatar(post.userId, post.userAvatar),
            userName: post.userName || '萌宠主人',
            likes: post.likeCount || 0,
            comments: post.commentCount || 0,
            time: this.formatTime(post.createdAt),
            relativeTime: this.getRelativeTime(post.createdAt),
            stickers: post.stickers || [],
            bubble: post.bubble || null,
            location: post.location || ''
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
    refreshPosts() {
      this.page = 1
      this.postList = []
      this.hasMore = true
      this.loadPosts(true)
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
    onDeletePost(post) {
      uni.showModal({
        title: '确认删除',
        content: '删除后不可恢复，确定要删除这条动态吗？',
        confirmColor: '#ff4d4f',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await postApi.deletePost(post.id)
              if (result.success) {
                uni.$emit('postDeleted', { postId: post.id })
                uni.showToast({ title: '已删除', icon: 'success' })
                this.postList = this.postList.filter(p => p.id !== post.id)
              } else {
                uni.showToast({ title: result.message || '删除失败', icon: 'none' })
              }
            } catch (error) {
              console.error('删除动态失败:', error)
              uni.showToast({ title: '删除失败', icon: 'none' })
            }
          }
        }
      })
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
      const d = new Date(typeof dateStr === 'string' ? dateStr.replace(/-/g, '/') : dateStr)
      return `${d.getMonth() + 1}-${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    getRelativeTime(dateStr) {
      if (!dateStr) return ''
      const now = Date.now()
      const then = new Date(typeof dateStr === 'string' ? dateStr.replace(/-/g, '/') : dateStr).getTime()
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
    },
    onAvatarTap() {
      if (this.isSelf) {
        this.goEditProfile()
        return
      }
      const avatarUrl = this.getUserAvatar(this.userId, this.userInfo.avatar)
      if (avatarUrl) {
        uni.previewImage({
          urls: [avatarUrl],
          current: 0
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   个人详情页 - 玻璃拟态风格 (Glassmorphism)
   保持原逻辑不变，全面升级视觉体验
   ============================================ */

.user-detail-page {
  min-height: 100vh;
  background: transparent;
}

.glass-header {
  position: relative;
  background: linear-gradient(
    180deg,
    rgba(255, 122, 61, 1) 0%,
    rgba(255, 90, 61, 0.95) 20%,
    rgba(255, 77, 79, 0.85) 50%,
    rgba(255, 107, 107, 0.7) 80%,
    rgba(255, 160, 122, 0.6) 100%
  );
  overflow: visible;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.6;
  animation: floatOrb 8s ease-in-out infinite;
}

.orb-1 {
  width: 300rpx;
  height: 300rpx;
  background: rgba(255, 255, 255, 0.3);
  top: -100rpx;
  right: 100rpx;
}

.orb-2 {
  width: 200rpx;
  height: 200rpx;
  background: rgba(255, 220, 180, 0.4);
  bottom: 80rpx;
  left: -60rpx;
  animation-delay: -3s;
}

.orb-3 {
  width: 150rpx;
  height: 150rpx;
  background: rgba(255, 255, 255, 0.2);
  top: 45%;
  left: 55%;
  animation-delay: -5s;
}

@keyframes floatOrb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-30rpx, 25rpx) scale(1.05); }
  66% { transform: translate(25rpx, -15rpx) scale(0.95); }
}

.nav-bar {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
  padding-top: calc(var(--status-bar-height, 20px) + 12rpx);
  height: 92rpx;
}

.back-btn {
  width: 68rpx;
  height: 68rpx;
  border-radius: 34rpx;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(16px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.12), inset 0 1rpx 0 rgba(255, 255, 255, 0.4);
  border: 1rpx solid rgba(255, 255, 255, 0.35);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.back-btn:active {
  background: rgba(255, 255, 255, 0.38);
  transform: scale(0.9);
}

.back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.capsule-placeholder {
  width: 200rpx;
  flex-shrink: 0;
}

.more-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  letter-spacing: 2rpx;
  opacity: 0.7;
}

.user-card {
  position: relative;
  z-index: 5;
  margin: 28rpx 32rpx 48rpx;
  padding: 36rpx 32rpx 32rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  box-shadow:
    0 16rpx 48rpx rgba(31, 38, 135, 0.18),
    0 4rpx 16rpx rgba(0, 0, 0, 0.06),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.95),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.65);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 1), transparent);
    pointer-events: none;
  }
}

.glass-card-effect {
  animation: cardSlideUp 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes cardSlideUp {
  from { opacity: 0; transform: translateY(30rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24rpx;
}

.avatar-wrap {
  position: relative;
  cursor: pointer;
}

.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 180rpx;
  height: 180rpx;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle at center, rgba(255, 106, 61, 0.25) 0%, rgba(255, 106, 61, 0.1) 50%, transparent 70%);
  filter: blur(10px);
  animation: pulseGlow 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes pulseGlow {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.8; }
  50% { transform: translate(-50%, -50%) scale(1.15); opacity: 1; }
}

.avatar-edit-badge {
  position: absolute;
  top: -2rpx;
  right: -2rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 6rpx 20rpx rgba(255, 77, 79, 0.45), 0 2rpx 8rpx rgba(255, 77, 79, 0.25);
  z-index: 10;
}

.avatar-edit-icon {
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
}

.user-info {
  text-align: center;
  margin-bottom: 28rpx;
}

.user-nickname {
  font-size: 38rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.user-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  margin-top: 10rpx;
}

.meta-tag {
  padding: 6rpx 16rpx;
  background: linear-gradient(135deg, rgba(255, 106, 61, 0.12), rgba(255, 160, 122, 0.1));
  border-radius: 999rpx;
  border: 1rpx solid rgba(255, 106, 61, 0.2);
}

.gender-icon {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--pt-primary, #ff6a3d);
}

.join-text {
  font-size: 24rpx;
  color: var(--pt-muted, #718096);
}

.stats-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(249, 250, 251, 0.6);
  border-radius: 24rpx;
  padding: 24rpx 16rpx;
  margin-bottom: 28rpx;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(209, 213, 219, 0.25);
}

.stat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8rpx 12rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.stat-card:active {
  transform: scale(0.92);
}

.stat-num {
  font-size: 36rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  line-height: 1.2;
}

.stat-label {
  font-size: 23rpx;
  color: var(--pt-muted, #718096);
  margin-top: 6rpx;
  font-weight: 500;
}

.stat-divider {
  width: 1rpx;
  height: 56rpx;
  background: linear-gradient(180deg, transparent, rgba(209, 213, 219, 0.4), transparent);
}

.action-row {
  margin-top: 16rpx;
  width: 100%;
  display: flex;
  justify-content: center;
}

.follow-btn {
  position: relative;
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 34rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
  padding: 0 48rpx;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.4),
      transparent
    );
    transition: left 0.6s ease;
  }

  &:active::before {
    left: 100%;
  }
}

/* 关注状态 - 醒目的渐变橙色 */
.follow-btn--follow {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff5a2d 50%, #ff3d1a 100%);
  color: #ffffff;
  box-shadow:
    0 12rpx 40rpx rgba(255, 90, 61, 0.5),
    0 4rpx 16rpx rgba(255, 90, 61, 0.35),
    0 0 0 4rpx rgba(255, 122, 61, 0.15),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.35),
    inset 0 -2rpx 0 rgba(180, 50, 20, 0.2);
  text-shadow:
    0 2rpx 8rpx rgba(180, 30, 10, 0.4),
    0 1rpx 2rpx rgba(0, 0, 0, 0.2);

  &:hover {
    transform: translateY(-4rpx) scale(1.03);
    box-shadow:
      0 20rpx 52rpx rgba(255, 90, 61, 0.55),
      0 8rpx 24rpx rgba(255, 90, 61, 0.4),
      0 0 0 6rpx rgba(255, 122, 61, 0.2),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.4),
      inset 0 -2rpx 0 rgba(180, 50, 20, 0.25);
  }

  &:active {
    transform: translateY(-1rpx) scale(0.97);
    box-shadow:
      0 6rpx 20rpx rgba(255, 90, 61, 0.45),
      0 2rpx 8rpx rgba(255, 90, 61, 0.3),
      inset 0 4rpx 12rpx rgba(140, 30, 10, 0.3);
  }
}

/* 已关注状态 - 清晰的边框样式 */
.follow-btn--following {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  color: var(--pt-primary, #ff6a3d);
  border: 3rpx solid var(--pt-primary, #ff6a3d);
  box-shadow:
    0 8rpx 28rpx rgba(255, 106, 61, 0.2),
    0 2rpx 8rpx rgba(255, 106, 61, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 1),
    inset 0 -1rpx 0 rgba(255, 106, 61, 0.08);

  .btn-text {
    color: var(--pt-primary, #ff6a3d) !important;
    font-weight: 800 !important;
  }

  &:hover {
    transform: translateY(-2rpx);
    background: linear-gradient(135deg, #fff5f2 0%, #fff 100%);
    box-shadow:
      0 12rpx 36rpx rgba(255, 106, 61, 0.28),
      0 4rpx 12rpx rgba(255, 106, 61, 0.15);
    border-color: #ff5722;
  }

  &:active {
    transform: scale(0.97);
    background: linear-gradient(135deg, #ffebe5 0%, #fff5f2 100%);
  }
}

.btn-icon-plus {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  color: #ff5a2d;
  font-size: 32rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  box-shadow: 0 2rpx 8rpx rgba(180, 50, 20, 0.3);
  flex-shrink: 0;
}

.btn-text {
  font-size: 34rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 4rpx;
  text-shadow: 0 2rpx 8rpx rgba(180, 30, 10, 0.4);
}

.report-link {
  margin-top: 20rpx;
  display: flex;
  justify-content: center;
}

.report-link:active .report-link-text {
  opacity: 0.6;
  transform: scale(0.96);
}

.report-link-text {
  font-size: 24rpx;
  color: var(--pt-light, #999999);
  text-decoration: underline;
  text-underline-offset: 4rpx;
  text-decoration-style: dashed;
  transition: all 0.25s ease;
}

.report-link--done .report-link-text {
  color: #d1d5db;
  text-decoration: none;
}

.wave-decoration {
  position: absolute;
  bottom: -2rpx;
  left: 0;
  width: 100%;
  height: 120rpx;
  overflow: hidden;
  line-height: 0;
  z-index: 3;
}

.wave-path {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
}

.wave-path-1 {
  height: 120rpx;
  animation: waveMove1 12s ease-in-out infinite;
}

.wave-path-2 {
  height: 110rpx;
  animation: waveMove2 10s ease-in-out infinite reverse;
  opacity: 0.75;
}

.wave-path-3 {
  height: 95rpx;
  animation: waveMove3 14s ease-in-out infinite;
  opacity: 0.5;
}

@keyframes waveMove1 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-30rpx); }
}

@keyframes waveMove2 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(25rpx); }
}

@keyframes waveMove3 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-20rpx); }
}

.tab-bar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4rpx 24rpx rgba(31, 38, 135, 0.08), inset 0 1rpx 0 rgba(255, 255, 255, 0.8);
}

.glass-tabbar {
  margin-top: -1rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.3);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 96rpx;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.tab-item:active {
  background: rgba(255, 106, 61, 0.06);
}

.tab-item.active .tab-text {
  color: var(--pt-primary, #ff6a3d);
  font-weight: 700;
  text-shadow: 0 2rpx 8rpx rgba(255, 106, 61, 0.2);
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 56rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: linear-gradient(90deg, var(--pt-primary, #ff6a3d), var(--pt-primary-2, #ff9a5d));
  box-shadow: 0 2rpx 8rpx rgba(255, 106, 61, 0.4);
  animation: indicatorSlide 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes indicatorSlide {
  from { width: 0; opacity: 0; }
  to { width: 56rpx; opacity: 1; }
}

.tab-text {
  font-size: 29rpx;
  color: var(--pt-secondary, #666666);
  font-weight: 500;
  letter-spacing: 1rpx;
  transition: all 0.3s ease;
}

.post-scroll {
  height: calc(100vh - 620rpx);
  background: transparent;
}

.post-list {
  padding: 20rpx 24rpx 240rpx;
}

.post-card {
  position: relative;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 28rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow:
    0 8rpx 32rpx rgba(31, 38, 135, 0.1),
    0 2rpx 8rpx rgba(0, 0, 0, 0.03),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border: 1rpx solid rgba(255, 255, 255, 0.6);
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  animation: postFadeIn 0.5s ease-out both;
}

.glass-post-card:hover {
  box-shadow:
    0 16rpx 48rpx rgba(31, 38, 135, 0.16),
    0 4rpx 16rpx rgba(0, 0, 0, 0.05),
    inset 0 1rpx 0 rgba(255, 255, 255, 1);
  transform: translateY(-4rpx);
  border-color: rgba(255, 255, 255, 0.8);
}

.glass-post-card:active {
  transform: translateY(-2rpx) scale(0.99);
  transition-duration: 0.15s;
}

@keyframes postFadeIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.post-card-highlight {
  position: absolute;
  top: 0;
  left: 12%;
  right: 12%;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.9), transparent);
  pointer-events: none;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  position: relative;
  z-index: 5;
}

.post-info {
  flex: 1;
  margin-left: 16rpx;
}

.post-name {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
}

.post-pet-info {
  display: flex;
  align-items: center;
  margin-top: 6rpx;
}

.pet-icon {
  font-size: 24rpx;
  margin-right: 6rpx;
}

.pet-name {
  font-size: 24rpx;
  color: var(--pt-secondary, #666666);
  font-weight: 500;
}

.pet-age {
  font-size: 24rpx;
  color: var(--pt-light, #999999);
}

.post-time {
  font-size: 23rpx;
  color: var(--pt-muted, #718096);
  white-space: nowrap;
}

.post-content-wrap {
  margin-bottom: 20rpx;
  position: relative;
  z-index: 5;
}

.post-content {
  font-size: 29rpx;
  color: var(--pt-text, #1f2937);
  line-height: 1.7;
  word-break: break-all;
  font-weight: 400;
}

.post-content.line-clamp {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 4;
  overflow: hidden;
}

.expand-btn {
  font-size: 27rpx;
  color: var(--pt-primary, #ff6a3d);
  font-weight: 600;
  margin-top: 10rpx;
  display: inline-block;
  padding: 8rpx 16rpx;
  background: rgba(255, 106, 61, 0.08);
  border-radius: 12rpx;
  transition: all 0.25s ease;
}

.expand-btn:active {
  background: rgba(255, 106, 61, 0.15);
  transform: scale(0.96);
}

.post-image-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 20rpx;
  position: relative;
  z-index: 5;
}

.post-image {
  border-radius: 16rpx;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.post-image:active {
  transform: scale(0.97);
}

.post-image--single {
  width: 100%;
  height: 400rpx;
}

.post-image--double {
  width: calc(50% - 5rpx);
  height: 280rpx;
}

.post-image--grid {
  width: calc(33.33% - 7rpx);
  height: 220rpx;
}

.post-video-wrap {
  margin-bottom: 20rpx;
  position: relative;
  z-index: 5;
}

.post-video-item {
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.post-stickers {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  padding: 10rpx 0;
  position: relative;
  z-index: 5;
}

.post-sticker {
  font-size: 44rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.post-bubble {
  display: inline-flex;
  padding: 18rpx 32rpx;
  border-radius: 28rpx;
  margin: 10rpx 0;
  position: relative;
  z-index: 5;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.post-bubble-text {
  font-size: 28rpx;
  font-weight: 600;
}

.post-location {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
  padding: 10rpx 16rpx;
  background: rgba(249, 250, 251, 0.6);
  border-radius: 12rpx;
  position: relative;
  z-index: 5;
}

.location-icon {
  font-size: 26rpx;
  margin-right: 8rpx;
}

.location-text {
  font-size: 24rpx;
  color: var(--pt-secondary, #666666);
}

.post-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20rpx;
  margin-top: 16rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 5;
}

.actions-left {
  display: flex;
  align-items: center;
  gap: 36rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 16rpx;
  border-radius: 16rpx;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.glass-action:hover {
  background: rgba(255, 106, 61, 0.08);
}

.glass-action:active {
  background: rgba(255, 106, 61, 0.15);
  transform: scale(0.92);
}

.action-icon {
  font-size: 32rpx;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-icon--liked {
  animation: likePop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes likePop {
  0% { transform: scale(1); }
  40% { transform: scale(1.4); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

.delete-action {
  opacity: 0.5;
}

.delete-action:active {
  opacity: 0.8;
}

.delete-icon {
  font-size: 28rpx;
}

.action-count {
  font-size: 25rpx;
  color: var(--pt-muted, #718096);
  font-weight: 500;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0;
  gap: 16rpx;
}

.loading-spinner {
  width: 56rpx;
  height: 56rpx;
  border: 4rpx solid rgba(255, 106, 61, 0.15);
  border-top-color: var(--pt-primary, #ff6a3d);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 26rpx;
  color: var(--pt-muted, #718096);
}

.no-more-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 32rpx;
  gap: 16rpx;
}

.divider-line {
  width: 200rpx;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, rgba(209, 213, 219, 0.5), transparent);
}

.no-more-text {
  font-size: 24rpx;
  color: var(--pt-muted, #718096);
  letter-spacing: 2rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 0;
  animation: emptyFadeIn 0.6s ease-out;
}

@keyframes emptyFadeIn {
  from { opacity: 0; transform: translateY(30rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
  filter: grayscale(30%);
  opacity: 0.7;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: var(--pt-secondary, #666666);
}
</style>