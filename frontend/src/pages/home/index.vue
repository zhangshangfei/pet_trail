<template>
  <view class="home-page">
    <!-- Custom top navigation -->
    <view class="home-nav">
      <view class="home-statusbar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="home-nav-inner">
        <view v-if="isLoggedIn" class="home-user-info">
          <image class="home-avatar" :src="avatarUrl" mode="aspectFill" />
          <text class="home-name">{{ userName }}</text>
        </view>
        <view v-else @click="onWechatLogin" class="wechat-login-btn-wrap">
          <text class="wechat-login-btn">微信登录</text>
        </view>
        <view class="home-nav-right" @click="onBellTap">
          <text class="home-bell">🔔</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="home-scroll" :style="{ paddingTop: headerHeight + 'px' }">
      <view class="home-content">
        <!-- Quick action buttons -->
        <view class="quick-actions">
          <view class="quick-btn quick-btn-1" @click="onQuickAction1">
            <text class="quick-icon">🩺</text>
            <text class="quick-text" >一键记录健康</text>
          </view>
          <!-- 发布动态功能暂时隐藏
          <view class="quick-btn quick-btn-2" @click="onQuickAction2">
            <text class="quick-icon">📷</text>
            <text class="quick-text">发布动态</text>
          </view>
          -->
        </view>

        <!-- Health dashboard card -->
        <view class="health-card">
          <image class="health-bg-pattern" :src="patternUrl" mode="aspectFill" />

          <view class="health-card-inner">
            <text class="health-title">健康看板</text>

            <view class="stats-row">
              <view class="stat-block">
                <view class="ring" :style="stepsRingStyle">
                  <view class="ring-inner">
                    <text class="ring-value">2.1k</text>
                  </view>
                </view>
                <text class="stat-label">今日步数</text>
              </view>

              <view class="stat-block">
                <view class="ring ring--green" :style="waterRingStyle">
                  <view class="ring-inner">
                    <text class="ring-value">1.2L</text>
                  </view>
                </view>
                <text class="stat-label">饮水量</text>
              </view>

              <view class="stat-block">
                <view class="trend-img-wrap">
                  <image class="trend-img" :src="weightChartUrl" mode="aspectFit" />
                </view>
                <text class="stat-label">体重趋势</text>
              </view>
            </view>

            <view class="detail-row">
              <text class="detail-link" @click="onViewDetail">
                查看详情
                <text class="detail-arrow">›</text>
              </text>
            </view>
          </view>
        </view>

        <!-- 宠物动态标题行暂时隐藏
        <view class="feed-title-row">
          <text class="feed-title">宠物动态</text>
          <text class="feed-filter" @click="onFilterTap">⏷</text>
        </view>

        <view class="feed-list">
          <view
            v-for="post in posts"
            :key="post.id"
            class="post-card"
          >
            <view class="post-header">
              <image class="post-avatar" :src="post.avatar" mode="aspectFill" />
              <view class="post-meta">
                <text class="post-name">{{ post.name }}</text>
                <text class="post-time">{{ post.time }}</text>
              </view>
            </view>

            <text class="post-content">{{ post.content }}</text>

            <view v-if="post.images && post.images.length" class="post-images">
              <image
                v-for="(img, idx) in post.images"
                :key="img"
                class="post-image"
                :class="post.images.length === 2 ? 'post-image--2' : post.images.length === 3 ? 'post-image--3' : 'post-image--1'"
                :src="img"
                mode="aspectFill"
              />
            </view>

            <view class="post-actions">
              <view class="post-actions-left">
                <view class="post-action" @click="onLikeTap(post)">
                  <text class="post-action-icon">♡</text>
                  <text class="post-action-count">{{ post.likes }}</text>
                </view>
                <view class="post-action" @click="onCommentTap(post)">
                  <text class="post-action-icon">💬</text>
                  <text class="post-action-count">{{ post.comments }}</text>
                </view>
              </view>
              <text class="post-tags">{{ post.tags }}</text>
            </view>
          </view>
        </view>
        -->
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 0,
      userName: '',
      avatarUrl: '',
      isLoggedIn: false,
      patternUrl: 'https://ai-public.mastergo.com/ai/img_res/1774535762852vN4rT8wY3zA6bC9dE2.jpg',
      weightChartUrl: 'https://ai-public.mastergo.com/ai/img_res/1774535762852wY3zA6bC9dE2fG5hJ8.jpg',
      // 宠物动态数据暂时隐藏
      /* posts: [
        {
          id: 1,
          avatar: 'https://ai-public.mastergo.com/ai/img_res/1774535762852xA6bC9dE2fG5hJ8kL3.jpg',
          name: '金毛球球',
          time: '2小时前',
          content: '今天带球球去公园玩，它超级开心！',
          images: [
            'https://ai-public.mastergo.com/ai/img_res/1774535762852bC9dE2fG5hJ8kL3mP6.jpg',
            'https://ai-public.mastergo.com/ai/img_res/1774535762852cD0eF3gH6iK9lM4nQ7.jpg'
          ],
          likes: 24,
          comments: 8,
          tags: '#金毛 #日常'
        },
        {
          id: 2,
          avatar: 'https://ai-public.mastergo.com/ai/img_res/1774535762852dE2fG5hJ8kL3mP6qR9.jpg',
          name: '布偶猫奶茶',
          time: '4小时前',
          content: '奶茶今天特别乖，一直在阳台上晒太阳。',
          images: [
            'https://ai-public.mastergo.com/ai/img_res/1774535762852eF3gH6iK9lM4nQ7sT2.jpg'
          ],
          likes: 42,
          comments: 12,
          tags: '#布偶猫 #日常'
        },
        {
          id: 3,
          avatar: 'https://ai-public.mastergo.com/ai/img_res/1774535762852fG5hJ8kL3mP6qR9sT2.jpg',
          name: '腊肠豆豆',
          time: '6小时前',
          content: '豆豆学会了新技能，可以握手了！',
          images: [
            'https://ai-public.mastergo.com/ai/img_res/1774535762852gH6iK9lM4nQ7sT2uV5.jpg',
            'https://ai-public.mastergo.com/ai/img_res/1774535762852hJ8kL3mP6qR9sT2uV5wX8.jpg',
            'https://ai-public.mastergo.com/ai/img_res/1774535762852iK9lM4nQ7sT2uV5wX8yZ1.jpg'
          ],
          likes: 31,
          comments: 5,
          tags: '#腊肠 #训练'
        }
      ] */
    };
  },
  computed: {
    stepsRingStyle() {
      const percent = 70;
      return {
        background: `conic-gradient(#3B82F6 0% ${percent}%, #E5E7EB ${percent}% 100%)`
      };
    },
    waterRingStyle() {
      const percent = 60;
      return {
        background: `conic-gradient(#10B981 0% ${percent}%, #E5E7EB ${percent}% 100%)`
      };
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });

    // 检查登录状态
    this.checkLoginStatus();
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
      // 顶部导航高度 = 状态栏高度 + 导航栏内容高度 (约 50px: padding 16rpx*2 + 内容)
      const headerHeight = this.statusBarHeight + 50;
      this.headerHeight = headerHeight;
    } catch (e) {
      this.statusBarHeight = 20;
      this.headerHeight = 70;
    }

    // 检查登录状态
    this.checkLoginStatus();
  },
  methods: {
    // 加载用户信息（供外部调用）
    async loadUserInfo() {
      const token = uni.getStorageSync('token');
      if (!token) {
        this.isLoggedIn = false;
        this.userName = '小萌宠主人';
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
          // 保存用户信息到本地存储，供其他页面使用
          uni.setStorageSync('userInfo', userData);
        } else {
          this.isLoggedIn = false;
          this.userName = '小萌宠主人';
          this.avatarUrl = 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';
        }
      } catch (error) {
        console.error('获取用户资料失败:', error);
        this.isLoggedIn = false;
        this.userName = '小萌宠主人';
        this.avatarUrl = 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';
      }
    },
    // 检查登录状态（兼容旧方法名）
    async checkLoginStatus() {
      return this.loadUserInfo();
    },

    // 微信一键登录
    onWechatLogin() {
      const self = this; // 保存 this 引用
      uni.showLoading({
        title: '登录中...',
        mask: true
      });

      // 1. 获取微信登录 code
      wx.login({
        success: async (res) => {
          if (res.code) {
            try {
              // 2. 将 code 发送到后端
              const loginRes = await uni.$request.post('/api/users/login', { code: res.code });
              
              uni.hideLoading();
              console.log('登录响应:', loginRes);

              if (loginRes.success) {
                // 3. 保存 token 和用户信息
                // 后端返回结构：{ success: true, code: 200, data: { user: {...}, token: "..." }, message: "..." }
                const responseData = loginRes.data;
                const token = responseData.token;
                const userInfo = responseData.user;

                uni.setStorageSync('token', token);
                uni.setStorageSync('userInfo', userInfo);
                console.log('token 已保存:', token);
                console.log('用户信息:', userInfo);
                console.log('用户昵称:', userInfo?.nickname);
                console.log('用户头像:', userInfo?.avatar);

                // 4. 直接更新页面数据
                self.isLoggedIn = true;
                self.userName = userInfo?.nickname || '萌宠主人';
                self.avatarUrl = userInfo?.avatar || 'https://ai-public.mastergo.com/ai/img_res/1774535762852mP2xQ7vN4rT8wY3zA6.jpg';
                console.log('页面数据已更新 - isLoggedIn:', self.isLoggedIn, 'userName:', self.userName, 'avatarUrl:', self.avatarUrl);

                uni.showToast({
                  title: '登录成功',
                  icon: 'success',
                  duration: 2000
                });
              } else {
                uni.showToast({
                  title: loginRes.message || '登录失败',
                  icon: 'none',
                  duration: 2000
                });
              }
            } catch (err) {
              uni.hideLoading();
              console.error('网络请求失败:', err);
              uni.showToast({
                title: '网络错误，请重试',
                icon: 'none',
                duration: 2000
              });
            }
          } else {
            uni.hideLoading();
            uni.showToast({
              title: '获取登录凭证失败',
              icon: 'none',
              duration: 2000
            });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('wx.login 失败:', err);
          uni.showToast({
            title: '登录失败，请重试',
            icon: 'none',
            duration: 2000
          });
        }
      });
    },

    onBellTap() {
      uni.showToast({ title: '未实现', icon: 'none' });
    },
    onQuickAction1() {
      // uni.showToast({ title: '一键记录健康', icon: 'none' });
      uni.navigateTo({ url: "/pages/health/index" });
    },
    // 发布动态功能暂时隐藏
    // onQuickAction2() {
    //   uni.showToast({ title: '发布动态', icon: 'none' });
    // },
    onViewDetail() {
      uni.switchTab({ url: '/pages/dashboard/index' });
    },
    onFilterTap() {
      uni.showToast({ title: '筛选未实现', icon: 'none' });
    },
    // 宠物动态相关事件暂时隐藏
    // onLikeTap(post) {
    //   uni.showToast({ title: `点赞 ${post.likes}`, icon: 'none' });
    // },
    // onCommentTap(post) {
    //   uni.showToast({ title: `评论 ${post.comments}`, icon: 'none' });
    // }
  },
  addRecord() {
    uni.navigateTo({ url: "/pages/health/index" });
  }
};
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: var(--pt-bg);
}

.home-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 20;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 10rpx 30rpx rgba(17, 24, 39, 0.06);
  padding: 0 20rpx 10rpx;
}

.home-statusbar {
  width: 100%;
}

.home-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 未登录时的微信登录按钮 */
.wechat-login-btn-wrap {
  width: 64rpx;
  height: 64rpx;
  background: linear-gradient(135deg, #42b983, #339c7a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24rpx;
  font-weight: 800;
  margin-right: 18rpx;
  cursor: pointer;
}

.wechat-login-btn {
  font-size: 20rpx;
  font-weight: 800;
  color: #fff;
  text-align: center;
  line-height: 1.2;
}

/* 登录后的头像 */
.home-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 18rpx;
  background: #e5e7eb;
}

.home-user-info {
  display: flex;
  align-items: center;
}

.home-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
  flex: 1;
}

.home-nav-right {
  width: 96rpx;
  display: flex;
  justify-content: flex-end;
}

.home-bell {
  font-size: 38rpx;
  color: #6b7280;
}

.home-scroll {
  height: 100vh;
  /* padding-top 改为动态设置 */
}

.home-content {
  padding: 20rpx 20rpx 220rpx;
}

.quick-actions {
  display: flex;
  gap: 18rpx;
  margin-bottom: 26rpx;
}

.quick-btn {
  flex: 1;
  height: 92rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-btn-1 {
  background: linear-gradient(180deg, #60a5fa 0%, #7c3aed 100%);
  color: #fff;
}

.quick-btn-2 {
  background: linear-gradient(180deg, #fb923c 0%, #ef4444 100%);
  color: #fff;
}

.quick-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.quick-text {
  font-size: 28rpx;
  font-weight: 800;
}

.health-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 26rpx;
  box-shadow: var(--pt-shadow-soft);
  overflow: hidden;
  position: relative;
  padding: 28rpx 20rpx 24rpx;
}

.health-bg-pattern {
  position: absolute;
  top: -28rpx;
  right: -24rpx;
  width: 160rpx;
  height: 160rpx;
  opacity: 0.12;
  border-radius: 20rpx;
}

.health-card-inner {
  position: relative;
  z-index: 1;
}

.health-title {
  display: block;
  font-size: 34rpx;
  font-weight: 900;
  margin-bottom: 22rpx;
  color: #111827;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.stat-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ring {
  width: 124rpx;
  height: 124rpx;
  border-radius: 62rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: conic-gradient(#3B82F6 0 70%, #E5E7EB 0 100%);
}

.ring--green {
  background: conic-gradient(#10B981 0 60%, #E5E7EB 0 100%);
}

.ring-inner {
  width: 86rpx;
  height: 86rpx;
  border-radius: 43rpx;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 10rpx 30rpx rgba(17, 24, 39, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-value {
  font-size: 26rpx;
  font-weight: 900;
  color: #4b5563;
}

.trend-img-wrap {
  width: 140rpx;
  height: 124rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-img {
  width: 140rpx;
  height: 90rpx;
}

.stat-label {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.detail-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 18rpx;
}

.detail-link {
  font-size: 24rpx;
  color: #3b82f6;
  font-weight: 700;
  display: flex;
  align-items: center;
}

.detail-arrow {
  margin-left: 8rpx;
  color: #3b82f6;
  font-size: 30rpx;
}

.feed-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 24rpx 8rpx 18rpx;
}

.feed-title {
  font-size: 30rpx;
  font-weight: 900;
  color: #111827;
}

.feed-filter {
  font-size: 32rpx;
  color: #6b7280;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.7);
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.post-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 22rpx;
  box-shadow: var(--pt-shadow-soft);
  padding: 22rpx 18rpx;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 14rpx;
}

.post-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.post-meta {
  display: flex;
  flex-direction: column;
}

.post-name {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
  line-height: 36rpx;
}

.post-time {
  font-size: 22rpx;
  color: #6b7280;
  margin-top: 6rpx;
}

.post-content {
  display: block;
  font-size: 26rpx;
  color: #374151;
  line-height: 36rpx;
  margin-bottom: 14rpx;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 10rpx;
}

.post-image {
  border-radius: 16rpx;
  background: #f3f4f6;
}

.post-image--2 {
  width: calc(50% - 6rpx);
  height: 220rpx;
}

.post-image--3 {
  width: calc(33.333% - 8rpx);
  height: 160rpx;
}

.post-image--1 {
  width: 100%;
  height: 260rpx;
}

.post-actions {
  border-top: 1rpx solid rgba(17, 24, 39, 0.08);
  padding-top: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-actions-left {
  display: flex;
  gap: 22rpx;
}

.post-action {
  display: flex;
  align-items: center;
  color: #6b7280;
}

.post-action-icon {
  font-size: 30rpx;
}

.post-action-count {
  margin-left: 8rpx;
  font-size: 22rpx;
  font-weight: 800;
}

.post-tags {
  font-size: 22rpx;
  color: #6b7280;
}
</style>

