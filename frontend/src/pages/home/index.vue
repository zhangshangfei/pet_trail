<template>
  <view class="home-page">
    <!-- 顶部用户栏 -->
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="avatarUrl"
      :name="isLoggedIn ? (userName || '萌宠主人') : '请登录'"
      :unread-count="unreadNotificationCount"
      @rightTap="onBellTap"
      @userTap="onTopUserTap"
    />

    <!-- 分段控制器栏 -->
    <view class="segment-bar" :style="{ top: segmentBarTop + 'px' }">
      <view class="segmented-control">
        <view
          class="segment-item"
          :class="{ active: currentTab === 'all' }"
          @click="switchTab('all')"
        >
          <text class="segment-text">全部</text>
        </view>
        <view
          class="segment-item"
          :class="{ active: currentTab === 'collect' }"
          @click="switchTab('collect')"
        >
          <text class="segment-text">收藏</text>
        </view>
        <view
          class="segment-item"
          :class="{ active: currentTab === 'follow' }"
          @click="switchTab('follow')"
        >
          <text class="segment-text">关注</text>
        </view>
        <view
          class="segment-item"
          :class="{ active: currentTab === 'recommend' }"
          @click="switchTab('recommend')"
        >
          <text class="segment-text">推荐</text>
        </view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view
      scroll-y
      class="home-scroll"
      :style="{ paddingTop: headerHeight + 'px' }"
      @scrolltolower="loadMore"
    >
      <view class="feed-list">
        <!-- 动态卡片 -->
        <view
          v-for="(post, index) in postList"
          :key="post.id"
          class="post-card"
        >
          <!-- 用户信息头部 -->
          <view class="post-header">
            <image class="post-avatar" :src="post.avatar" mode="aspectFill" @tap="goUserProfile(post)" />
            <view class="post-info" @tap="goUserProfile(post)">
              <text class="post-name">{{ post.userName }}</text>
              <view class="post-pet-info">
                <text class="pet-icon">{{ getPetIcon(post.petType) }}</text>
                <text class="pet-name">{{ post.petName || '未知宠物' }}</text>
                <text class="pet-age" v-if="post.petAge"> | {{ post.petAge }}岁</text>
              </view>
            </view>
            <text class="post-time">{{ post.time }}</text>
          </view>

          <!-- 内容区 -->
          <view class="post-content-wrap">
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

          <!-- 图片展示 -->
          <view
            v-if="post.images && post.images.length"
            class="post-image-wrap"
            @dblclick="handleDoubleClick(post)"
          >
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

          <!-- 视频展示 -->
          <view
            v-if="post.videos && post.videos.length"
            class="post-video-wrap"
          >
            <view
              v-for="(vid, vidx) in post.videos"
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
                poster=""
              />
            </view>
          </view>

          <!-- 操作栏 -->
          <view class="post-actions">
            <view class="actions-left">
              <view class="action-item" @click="onLikeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.liked }">{{ post.liked ? '❤️' : '🤍' }}</text>
                <text class="action-count">{{ formatNumber(post.likes) }}</text>
              </view>
              <view class="action-item" @click="onEeTap(post)">
                <text class="action-icon" :class="{ 'action-icon--liked': post.eeLiked }">{{ post.eeLiked ? '⭐' : '☆' }}</text>
                <text class="action-count">{{ formatNumber(post.eeCount) }}</text>
              </view>
              <view class="action-item" @click="openComments(post)">
                <text class="action-icon">💬</text>
                <text class="action-count">{{ formatNumber(post.comments) }}</text>
              </view>
              <view class="action-item post-time-item">
                <text class="action-time">{{ post.relativeTime }}</text>
              </view>
            </view>
            <view class="action-item" @click="onShareTap(post)">
              <text class="action-icon">↗️</text>
            </view>
          </view>

          <!-- 评论预览 -->
          <view v-if="post.previewComments && post.previewComments.length > 0" class="comment-preview">
            <view
              v-for="comment in post.previewComments"
              :key="comment.id"
              class="preview-item"
            >
              <text class="comment-user">{{ comment.user }}：</text>
              <text class="comment-text">{{ comment.text }}</text>
            </view>
            <text class="view-all-comments" @click="openComments(post)">
              查看全部评论 ({{ post.comments }})
            </text>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading-text">加载中...</view>
        <view v-if="!hasMore && postList.length > 0" class="no-more-text">没有更多了</view>
        <view v-if="isEmpty && currentTab === 'follow'" class="empty-follow-state">
          <view class="empty-follow-emoji">👥</view>
          <text class="empty-follow-text">还没有关注任何人</text>
          <text class="empty-follow-subtext">关注感兴趣的用户，获取他们的最新动态</text>
          <button class="follow-guide-btn" @click="goToDiscover">去发现有趣的人</button>
        </view>
        <view v-if="isEmpty && currentTab === 'collect'" class="empty-follow-state">
          <view class="empty-follow-emoji">⭐</view>
          <text class="empty-follow-text">还没有收藏任何动态</text>
          <text class="empty-follow-subtext">看到喜欢的内容可以收藏起来，方便以后查看</text>
        </view>
        <view v-if="isEmpty && currentTab !== 'follow' && currentTab !== 'collect'" class="empty-state">
          <text class="empty-state-text">暂无动态</text>
        </view>
      </view>
    </scroll-view>

    <!-- 悬浮发布按钮 -->
    <view class="fab-button" @click="onPublishTap">
      <text class="fab-icon">+</text>
    </view>

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
import UserTopBar from '@/components/UserTopBar.vue'
import * as postApi from '@/api/post'
import * as notificationApi from '@/api/notification'
import { checkLogin, wechatLogin } from '@/utils/index'

export default {
  components: {
    UserTopBar
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 0,
      segmentBarTop: 0,
      userName: '',
      avatarUrl: '',
      isLoggedIn: false,

      currentTab: 'all',

      postList: [],
      page: 1,
      size: 10,
      loading: false,
      hasMore: true,

      expandedPosts: {},
      showVideoPlayer: false,
      currentVideoUrl: '',
      unreadNotificationCount: 0
    };
  },
  computed: {
    isEmpty() {
      return !this.loading && this.postList.length === 0
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });

    this.checkLoginStatus();
    this.fetchUnreadCount();
    this.page = 1;
    this.postList = [];
    this.hasMore = true;
    this.loadPosts();
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
      const userTopBarHeight = this.statusBarHeight + 92;
      this.segmentBarTop = userTopBarHeight - 30;
      this.headerHeight = userTopBarHeight + 26;
    } catch (e) {
      this.statusBarHeight = 20;
      this.segmentBarTop = 82;
      this.headerHeight = 138;
    }

    this.checkLoginStatus();

    uni.$on('loginSuccess', () => {
      this.checkLoginStatus()
      this.fetchUnreadCount()
    })
  },
  onHide() {
    uni.$off('loginSuccess')
  },
  onUnload() {
    uni.$off('loginSuccess')
  },
  methods: {
    async loadUserInfo() {
      const token = uni.getStorageSync('token');
      if (!token) {
        this.isLoggedIn = false;
        this.userName = '请登录';
        this.avatarUrl = 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';
        return;
      }
      try {
        const res = await uni.$request.get('/api/users/profile');
        if (res.success) {
          const userData = res.data;
          this.isLoggedIn = true;
          this.userName = userData.nickname || '萌宠主人';
          this.avatarUrl = userData.avatar || 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';
          uni.setStorageSync('userInfo', userData);
        } else {
          this.isLoggedIn = false;
          this.userName = '请登录';
          this.avatarUrl = 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';
        }
      } catch (error) {
        console.error('获取用户资料失败:', error);
        this.isLoggedIn = false;
        this.userName = '请登录';
        this.avatarUrl = 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';
      }
    },

    async checkLoginStatus() {
      return this.loadUserInfo();
    },

    async fetchUnreadCount() {
      const token = uni.getStorageSync('token');
      if (!token) {
        this.unreadNotificationCount = 0;
        return;
      }
      try {
        const res = await notificationApi.getUnreadCount();
        if (res.success && res.data) {
          this.unreadNotificationCount = res.data.count || 0;
        }
      } catch (e) {
        console.error('获取未读通知数失败:', e);
      }
    },

    onWechatLogin() {
      const self = this;
      uni.showLoading({ title: '登录中...', mask: true });

      wx.login({
        success: async (res) => {
          if (res.code) {
            try {
              const loginRes = await uni.$request.post('/api/users/login', { code: res.code });
              uni.hideLoading();

              if (loginRes.success) {
                const responseData = loginRes.data;
                const token = responseData.token;
                const userInfo = responseData.user;

                uni.setStorageSync('token', token);
                uni.setStorageSync('userInfo', userInfo);

                self.isLoggedIn = true;
                self.userName = userInfo?.nickname || '萌宠主人';
                self.avatarUrl = userInfo?.avatar || 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';

                uni.showToast({ title: '登录成功', icon: 'success', duration: 2000 });
                
                setTimeout(() => {
                  self.page = 1;
                  self.postList = [];
                  self.hasMore = true;
                  self.loadPosts();
                }, 1500);
              } else {
                uni.showToast({ title: loginRes.message || '登录失败', icon: 'none', duration: 2000 });
              }
            } catch (err) {
              uni.hideLoading();
              uni.showToast({ title: '网络错误，请重试', icon: 'none', duration: 2000 });
            }
          } else {
            uni.hideLoading();
            uni.showToast({ title: '获取登录凭证失败', icon: 'none', duration: 2000 });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          uni.showToast({ title: '登录失败，请重试', icon: 'none', duration: 2000 });
        }
      });
    },

    onTopUserTap() {
      if (!this.isLoggedIn) {
        this.onWechatLogin();
      } else {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && userInfo.id) {
          uni.navigateTo({ url: `/pages/user/detail?id=${userInfo.id}` })
        }
      }
    },

    onBellTap() {
      uni.navigateTo({ url: '/pages/notification/index' });
    },

    async onPublishTap() {
      const loggedIn = await checkLogin('请先登录后再发布动态')
      if (!loggedIn) return
      uni.navigateTo({ url: '/pages/publish/index' });
    },

    goUserProfile(post) {
      if (post.userId) {
        uni.navigateTo({ url: `/pages/user/detail?id=${post.userId}` })
      }
    },

    async switchTab(tab) {
      if (tab !== 'all') {
        const loggedIn = await checkLogin('请先登录后查看')
        if (!loggedIn) return
      }

      this.currentTab = tab;
      this.page = 1;
      this.postList = [];
      this.hasMore = true;
      console.log('switchTab - 准备加载数据, currentTab:', this.currentTab);
      this.loadPosts();
    },

    getPetIcon(type) {
      switch(type) {
        case 1: return '🐱';
        case 2: return '🐕';
        case 3: return '🐰';
        case 4: return '🐦';
        default: return '🐾';
      }
    },

    formatNumber(num) {
      if (!num) return '0';
      if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
      }
      return num.toString();
    },

    toggleExpand(postId) {
      this.$set(this.expandedPosts, postId, true);
    },

    handleDoubleClick(post) {
      if (!post.liked) {
        this.onLikeTap(post);
      }
    },

    getImageClass(count) {
      if (count === 1) return 'post-image--1';
      if (count === 2) return 'post-image--2';
      if (count === 3) return 'post-image--3';
      return 'post-image--grid';
    },

    previewImage(images, index) {
      const imageList = Array.isArray(images) ? images : [images];
      uni.previewImage({
        urls: imageList,
        current: index
      });
    },

    playVideo(url) {
      this.currentVideoUrl = url
      this.showVideoPlayer = true
    },
    closeVideoPlayer() {
      this.showVideoPlayer = false
      this.currentVideoUrl = ''
    },

    async onLikeTap(post) {
      const loggedIn = await checkLogin('请先登录后再点赞')
      if (!loggedIn) return

      try {
        const res = await postApi.toggleLike(post.id);
        if (res.success) {
          this.$set(post, 'liked', res.data.liked);
          this.$set(post, 'likes', res.data.likeCount);
        }
      } catch (error) {
        console.error('点赞失败:', error);
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },

    async onEeTap(post) {
      const loggedIn = await checkLogin('请先登录后再收藏')
      if (!loggedIn) return

      try {
        const res = await postApi.toggleEe(post.id);
        if (res.success && res.data) {
          const { eeLiked, eeCount } = res.data;
          this.$set(post, 'eeLiked', eeLiked);
          this.$set(post, 'eeCount', eeCount);
        }
      } catch (error) {
        console.error('收藏失败:', error);
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },

    onShareTap(post) {
      uni.showToast({ title: '分享功能开发中', icon: 'none' });
    },

    goToDiscover() {
      uni.showToast({
        title: '用户发现页设计中，敬请期待',
        icon: 'none',
        duration: 2000
      });
    },

    openComments(post) {
      uni.navigateTo({
        url: `/pages/post/detail?id=${post.id}`
      });
    },

    async loadPosts() {
      if (this.loading || !this.hasMore) return;

      if (this.currentTab !== 'all') {
        const loggedIn = await checkLogin('请先登录后查看')
        if (!loggedIn) return
      }

      this.loading = true;
      try {
        const res = await postApi.getFeed(this.page, this.size, this.currentTab);
        console.log('getFeed response:', res);
        if (res.success && Array.isArray(res.data)) {
          console.log('获取到动态数量:', res.data.length);
          const newPosts = res.data.map(post => ({
            ...post,
            userName: post.userName || '未知用户',
            userAvatar: post.userAvatar || 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg',
            avatar: post.userAvatar || 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg',
            petName: post.petName || '',
            petType: post.petType || 0,
            petAge: post.petAge || 0,
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
            previewComments: post.previewComments || []
          }));

          if (newPosts.length < this.size) {
            this.hasMore = false;
          }
          this.postList = [...this.postList, ...newPosts];
          this.page++;
        }
      } catch (error) {
        if (error.statusCode !== 401) {
          uni.showToast({ title: '加载失败', icon: 'none' });
        }
      } finally {
        this.loading = false;
      }
    },

    loadMore() {
      if (this.hasMore && !this.loading) {
        this.loadPosts();
      }
    },

    formatTime(timestamp) {
      if (!timestamp) return '';
      const now = Date.now();
      const diff = now - timestamp;

      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';

      const date = new Date(timestamp);
      return `${date.getMonth() + 1}/${date.getDate()}`;
    },

    getRelativeTime(timestamp) {
      if (!timestamp) return '';
      
      let timeMs = 0;
      if (typeof timestamp === 'number') {
        timeMs = timestamp;
      } else if (typeof timestamp === 'string') {
        const dateStr = timestamp.replace(' ', 'T');
        timeMs = new Date(dateStr).getTime();
      }
      
      if (isNaN(timeMs) || timeMs === 0) return '';
      
      const now = Date.now();
      const diff = now - timeMs;
      
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
      if (diff < 172800000) return '昨天';
      if (diff < 259200000) return Math.floor(diff / 86400000) + '天前';
      
      const date = new Date(timeMs);
      return `${date.getMonth() + 1}/${date.getDate()}`;
    }
  }
};
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: #f9fafb;
}

.segment-bar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 90;
  background: #fff;
}

.segmented-control {
  display: flex;
  margin: 4rpx 24rpx 8rpx;
  background: #f3f4f6;
  border-radius: 999rpx;
  padding: 6rpx;
}

.segment-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx 0;
  border-radius: 999rpx;
  transition: all 0.3s;
}

.segment-item.active {
  background: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

  .segment-text {
    color: #111827;
    font-weight: 700;
  }
}

.segment-text {
  font-size: 26rpx;
  color: #6b7280;
}

.home-scroll {
  height: 100vh;
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

.post-video-wrap {
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.post-video-item {
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.post-video {
  width: 100%;
  height: 420rpx;
  border-radius: 16rpx;
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

.comment-preview {
  margin-top: 16rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 16rpx;
}

.preview-item {
  margin-bottom: 8rpx;
  font-size: 24rpx;
}

.comment-user {
  font-weight: 600;
  color: #111827;
}

.comment-text {
  color: #6b7280;
}

.view-all-comments {
  display: block;
  font-size: 24rpx;
  color: #3b82f6;
  margin-top: 8rpx;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 26rpx;
}

.empty-follow-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 60rpx;
  text-align: center;
}

.empty-follow-emoji {
  font-size: 160rpx;
  margin-bottom: 40rpx;
  opacity: 0.6;
}

.empty-follow-text {
  font-size: 32rpx;
  color: #6b7280;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.empty-follow-subtext {
  font-size: 26rpx;
  color: #9ca3af;
  line-height: 40rpx;
  margin-bottom: 48rpx;
}

.follow-guide-btn {
  width: 400rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 40rpx;
  border: none;
}

.follow-guide-btn:active {
  transform: scale(0.95);
  opacity: 0.9;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-state-text {
  font-size: 28rpx;
  color: #999;
}

.fab-button {
  position: fixed;
  right: 40rpx;
  bottom: calc(env(safe-area-inset-bottom) + 160rpx);
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.4);
  z-index: 99;
}

.fab-button:active {
  transform: scale(0.95);
}

.fab-icon {
  font-size: 56rpx;
  color: #fff;
  font-weight: 300;
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
