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
      @discoverTap="onDiscoverTap"
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

    <!-- 系统消息滚动通知 -->
    <view v-if="showSysNotice && sysNoticeList.length" class="sys-notice-bar" :style="{ top: (headerHeight - 2) + 'px' }">
      <view class="sys-notice-inner">
        <text class="sys-notice-icon">📢</text>
        <swiper class="sys-notice-swiper" vertical autoplay circular :interval="3000" :duration="500">
          <swiper-item v-for="(msg, idx) in sysNoticeList" :key="idx">
            <text class="sys-notice-text" @tap="onSysNoticeTap(msg)">{{ msg.content }}</text>
          </swiper-item>
        </swiper>
        <view class="sys-notice-close" @tap="closeSysNotice">
          <text class="sys-notice-close-text">✕</text>
        </view>
      </view>
    </view>

    <!-- 主内容区 -->
    <view
      class="home-scroll"
      :style="{ paddingTop: headerHeight + 'px' }"
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
                <text class="pet-age" v-if="post.petAge"> | {{ post.petAge }}</text>
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

          <!-- 贴纸展示 -->
          <view v-if="post.stickers && post.stickers.length" class="post-stickers">
            <text v-for="(s, i) in post.stickers" :key="i" class="post-sticker">{{ s }}</text>
          </view>

          <!-- 文字气泡 -->
          <view v-if="post.bubble" class="post-bubble" :style="{ background: post.bubble.bgColor || '#ff6a3d' }">
            <text class="post-bubble-text" :style="{ color: post.bubble.textColor || '#ffffff' }">{{ post.bubble.text }}</text>
          </view>

          <!-- 位置信息 -->
          <view v-if="post.location" class="post-location">
            <text class="location-icon">📍</text>
            <text class="location-text">{{ post.location }}</text>
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
            <view class="action-item share-btn-wrap">
              <button class="share-btn" open-type="share" :data-id="post.id">
                <text class="action-icon">↗️</text>
              </button>
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
    </view>

    <!-- 悬浮发布按钮 -->
    <view class="fab-button" @click="onPublishTap">
      <view class="fab-inner">
        <view class="fab-icon-wrapper">
          <view class="fab-hbar"></view>
          <view class="fab-vbar"></view>
        </view>
      </view>
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
import { checkLogin, wechatLogin, getUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'

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
      avatarUrl: DEFAULT_USER_AVATAR,
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
      unreadNotificationCount: 0,
      showSysNotice: false,
      sysNoticeList: [],
      sysNoticeTimer: null
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
    this.loadCachedPosts();

    uni.$on('loginSuccess', () => {
      this.checkLoginStatus()
      this.fetchUnreadCount()
    })
  },
  onHide() {
  },
  onUnload() {
    if (this.sysNoticeTimer) {
      clearTimeout(this.sysNoticeTimer)
      this.sysNoticeTimer = null
    }
    uni.$off('loginSuccess')
  },
  onPullDownRefresh() {
    this.page = 1
    this.postList = []
    this.hasMore = true
    this.checkLoginStatus()
    this.fetchUnreadCount()
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
        this.recordShare(postId)
        return {
          title: post.content ? post.content.substring(0, 30) + (post.content.length > 30 ? '...' : '') : '萌宠动态',
          path: `/pages/post/detail?id=${postId}`,
          imageUrl: post.images && post.images.length > 0 ? post.images[0] : ''
        }
      }
    }
    return {
      title: 'PetTrail - 萌宠社区',
      path: '/pages/home/index'
    }
  },
  onShareTimeline() {
    const lastPost = this.postList.length > 0 ? this.postList[0] : null
    return {
      title: lastPost ? (lastPost.content ? lastPost.content.substring(0, 30) : '萌宠动态') : 'PetTrail - 萌宠社区',
      query: lastPost ? `postId=${lastPost.id}` : '',
      imageUrl: lastPost && lastPost.images && lastPost.images.length > 0 ? lastPost.images[0] : ''
    }
  },
  methods: {
    loadCachedPosts() {
      try {
        const cacheKey = 'home:posts:' + this.currentTab
        const cached = uni.getStorageSync(cacheKey)
        if (cached && cached.posts && cached.posts.length > 0) {
          this.postList = cached.posts
          this.page = cached.page || 2
          this.hasMore = cached.hasMore !== false
        }
      } catch (e) {
        console.warn('读取缓存动态失败:', e)
      }
    },

    saveCachedPosts() {
      try {
        const cacheKey = 'home:posts:' + this.currentTab
        uni.setStorageSync(cacheKey, {
          posts: this.postList.slice(0, 20),
          page: this.page,
          hasMore: this.hasMore,
          timestamp: Date.now()
        })
      } catch (e) {
        console.warn('保存缓存动态失败:', e)
      }
    },

    async loadUserInfo() {
      const token = uni.getStorageSync('token');
      if (!token) {
        this.isLoggedIn = false;
        this.userName = '请登录';
        this.avatarUrl = DEFAULT_USER_AVATAR;
        return;
      }
      const cachedUserInfo = uni.getStorageSync('userInfo');
      if (cachedUserInfo) {
        this.isLoggedIn = true;
        this.userName = cachedUserInfo.nickname || '萌宠主人';
        this.avatarUrl = getUserAvatar(cachedUserInfo.id, cachedUserInfo.avatar);
      }
      try {
        const res = await uni.$request.get('/api/users/profile');
        if (res.success) {
          const userData = res.data;
          this.isLoggedIn = true;
          this.userName = userData.nickname || '萌宠主人';
          this.avatarUrl = getUserAvatar(userData.id, userData.avatar);
          uni.setStorageSync('userInfo', userData);
        } else if (!cachedUserInfo) {
          this.isLoggedIn = false;
          this.userName = '请登录';
          this.avatarUrl = DEFAULT_USER_AVATAR;
        }
      } catch (error) {
        console.error('获取用户资料失败:', error);
        if (!cachedUserInfo) {
          this.isLoggedIn = false;
          this.userName = '请登录';
          this.avatarUrl = DEFAULT_USER_AVATAR;
        }
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
        this.loadSysNotices()
      } catch (e) {
        console.error('获取未读通知数失败:', e);
      }
    },
    async loadSysNotices() {
      if (!this.isLoggedIn) return
      try {
        const res = await uni.$request.get('/api/notifications', { page: 1, size: 10 })
        if (res && res.success && Array.isArray(res.data)) {
          const sysMsgs = res.data.filter(n => n.type === 'system' && !n.read)
          if (sysMsgs.length > 0) {
            this.sysNoticeList = sysMsgs
            const lastDismiss = uni.getStorageSync('sysNoticeDismissTime')
            const now = Date.now()
            if (!lastDismiss || now - lastDismiss > 300000) {
              this.showSysNotice = true
              this.startSysNoticeTimer()
            }
          }
        }
      } catch (e) {
        console.error('加载系统消息失败', e)
      }
    },
    startSysNoticeTimer() {
      if (this.sysNoticeTimer) clearTimeout(this.sysNoticeTimer)
      this.sysNoticeTimer = setTimeout(() => {
        this.showSysNotice = false
      }, 30000)
    },
    closeSysNotice() {
      this.showSysNotice = false
      if (this.sysNoticeTimer) {
        clearTimeout(this.sysNoticeTimer)
        this.sysNoticeTimer = null
      }
      uni.setStorageSync('sysNoticeDismissTime', Date.now())
    },
    onSysNoticeTap(msg) {
      uni.navigateTo({ url: '/pages/notification/index' })
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
                uni.setStorageSync('tokenExpireTime', Date.now() + 7 * 24 * 60 * 60 * 1000);
                uni.setStorageSync('userInfo', userInfo);

                self.isLoggedIn = true;
                self.userName = userInfo?.nickname || '萌宠主人';
                self.avatarUrl = getUserAvatar(userInfo?.id, userInfo?.avatar);

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

    onDiscoverTap() {
      uni.navigateTo({ url: '/pages/discover/index' });
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
      this.loadCachedPosts();
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

    async recordShare(postId) {
      try {
        await postApi.sharePost(postId)
      } catch (e) {
        console.warn('分享统计失败:', e)
      }
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
            userAvatar: getUserAvatar(post.userId, post.userAvatar),
            avatar: getUserAvatar(post.userId, post.userAvatar),
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
            previewComments: post.previewComments || [],
            stickers: post.stickers || [],
            bubble: post.bubble || null,
            location: post.location || ''
          }));

          if (newPosts.length < this.size) {
            this.hasMore = false;
          }
          this.postList = [...this.postList, ...newPosts];
          this.page++;
          this.saveCachedPosts();
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
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.6) 0%, rgba(255, 255, 255, 0.2) 50%, rgba(255, 255, 255, 0) 100%);
  padding-bottom: 24rpx;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.sys-notice-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  z-index: 89;
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 100%);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 122, 61, 0.15);
  border: 1rpx solid rgba(255, 122, 61, 0.15);
  overflow: hidden;
}

.sys-notice-inner {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16rpx 20rpx;
}

.sys-notice-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  flex-shrink: 0;
}

.sys-notice-swiper {
  flex: 1;
  height: 40rpx;
}

.sys-notice-text {
  font-size: 24rpx;
  color: #9a3412;
  line-height: 40rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sys-notice-close {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 12rpx;
  flex-shrink: 0;
}

.sys-notice-close-text {
  font-size: 24rpx;
  color: #d97706;
}

.segmented-control {
  display: flex;
  margin: 4rpx 24rpx 8rpx;
  background: rgba(243, 244, 246, 0.6);
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
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);

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
  right: 28rpx;
  bottom: calc(env(safe-area-inset-bottom) + 250rpx);
  z-index: 99;
}

.fab-inner {
  width: 104rpx;
  height: 104rpx;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(255, 77, 79, 0.4), 0 2rpx 8rpx rgba(255, 77, 79, 0.2);
  backdrop-filter: blur(12px);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.fab-button:active .fab-inner {
  transform: scale(0.92);
  box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.3);
}

.fab-icon-wrapper {
  position: relative;
  width: 38rpx;
  height: 38rpx;
}

.fab-hbar,
.fab-vbar {
  position: absolute;
  background: #fff;
  border-radius: 4rpx;
}

.fab-hbar {
  top: 50%;
  left: 0;
  width: 38rpx;
  height: 5rpx;
  transform: translateY(-50%);
}

.fab-vbar {
  left: 50%;
  top: 0;
  width: 5rpx;
  height: 38rpx;
  transform: translateX(-50%);
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
