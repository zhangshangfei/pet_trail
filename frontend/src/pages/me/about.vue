<template>
  <view class="about-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">关于我们</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">
        <view class="app-header">
          <view class="app-logo-wrap">
            <text class="app-logo">🐾</text>
          </view>
          <text class="app-name">宠迹</text>
          <text class="app-slogan">记录宠物的每一天</text>
          <text class="app-version">v1.0.0</text>
        </view>

        <view class="info-card">
          <view class="info-item" @tap="copyText('pettrail@openclaw.com')">
            <text class="info-icon">📧</text>
            <view class="info-content">
              <text class="info-label">联系邮箱</text>
              <text class="info-value">pettrail@openclaw.com</text>
            </view>
            <text class="info-arrow">复制</text>
          </view>
          <view class="info-item" @tap="copyText('https://pettrail.openclaw.com')">
            <text class="info-icon">🌐</text>
            <view class="info-content">
              <text class="info-label">官方网站</text>
              <text class="info-value">pettrail.openclaw.com</text>
            </view>
            <text class="info-arrow">复制</text>
          </view>
        </view>

        <view class="desc-card">
          <text class="desc-title">关于宠迹</text>
          <text class="desc-text">宠迹是一款专为宠物主人打造的记录管理工具，帮助您轻松记录宠物的日常生活、健康状况、疫苗接种等重要信息，让每一只宠物都能得到更好的关爱。</text>
        </view>

        <view class="feature-card">
          <text class="desc-title">核心功能</text>
          <view class="feature-list">
            <view class="feature-row">
              <text class="feature-dot">📸</text>
              <text class="feature-text">发布动态，记录宠物精彩瞬间</text>
            </view>
            <view class="feature-row">
              <text class="feature-dot">📋</text>
              <text class="feature-text">每日打卡，养成养宠好习惯</text>
            </view>
            <view class="feature-row">
              <text class="feature-dot">💉</text>
              <text class="feature-text">疫苗驱虫提醒，守护宠物健康</text>
            </view>
            <view class="feature-row">
              <text class="feature-dot">🔔</text>
              <text class="feature-text">喂食提醒，科学喂养不遗忘</text>
            </view>
            <view class="feature-row">
              <text class="feature-dot">👥</text>
              <text class="feature-text">发现宠友，分享养宠经验</text>
            </view>
          </view>
        </view>

        <view class="footer">
          <text class="footer-text">© 2026 宠迹 PetTrail</text>
          <text class="footer-text">Made with ❤️ for pets</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 20
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    copyText(text) {
      uni.setClipboardData({
        data: text,
        success() {
          uni.showToast({ title: '已复制', icon: 'success' })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.about-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.app-header {
  display: flex; flex-direction: column; align-items: center;
  padding: 48rpx 0 32rpx;
}
.app-logo-wrap {
  width: 140rpx; height: 140rpx; border-radius: 36rpx;
  background: linear-gradient(135deg, #ffe8d6, #ffecd2);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20rpx; box-shadow: 0 8rpx 24rpx rgba(255,106,61,0.2);
}
.app-logo { font-size: 72rpx; }
.app-name { font-size: 40rpx; font-weight: 700; color: $text-primary; margin-bottom: 8rpx; }
.app-slogan { font-size: 28rpx; color: $text-secondary; margin-bottom: 8rpx; }
.app-version { font-size: 24rpx; color: $text-light; }

.info-card {
  background: $card-bg; border-radius: 24rpx; margin-bottom: 20rpx;
  overflow: hidden;
}
.info-item {
  display: flex; align-items: center; padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}
.info-item:last-child { border-bottom: none; }
.info-icon { font-size: 36rpx; margin-right: 20rpx; flex-shrink: 0; }
.info-content { flex: 1; }
.info-label { display: block; font-size: 24rpx; color: $text-light; margin-bottom: 4rpx; }
.info-value { display: block; font-size: 28rpx; color: $text-primary; }
.info-arrow { font-size: 24rpx; color: $primary; font-weight: 500; }

.desc-card, .feature-card {
  background: $card-bg; border-radius: 24rpx; padding: 28rpx; margin-bottom: 20rpx;
}
.desc-title { display: block; font-size: 30rpx; font-weight: 700; color: $text-primary; margin-bottom: 16rpx; }
.desc-text { font-size: 28rpx; color: $text-secondary; line-height: 1.8; }

.feature-list { display: flex; flex-direction: column; gap: 20rpx; }
.feature-row { display: flex; align-items: center; }
.feature-dot { font-size: 28rpx; margin-right: 16rpx; flex-shrink: 0; }
.feature-text { font-size: 28rpx; color: $text-secondary; }

.footer {
  display: flex; flex-direction: column; align-items: center;
  padding: 40rpx 0 80rpx;
}
.footer-text { font-size: 24rpx; color: $text-light; margin-bottom: 8rpx; }
</style>
