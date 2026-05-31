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
        <!-- 品牌展示区 -->
        <view class="brand-section">
          <view class="logo-container">
            <view class="logo-ring">
              <text class="logo-icon">🐾</text>
            </view>
          </view>
          <text class="app-name">宠迹</text>
          <view class="slogan-box">
            <text class="app-slogan">记录宠物的每一天</text>
          </view>
          <view class="version-badge">
            <text class="version-text">v1.0.0</text>
          </view>
        </view>

        <!-- 联系信息卡片 -->
        <view class="info-card">
          <view class="card-header">
            <view class="card-icon-wrap">
              <text class="card-icon">📬</text>
            </view>
            <text class="card-title">联系我们</text>
          </view>

          <view class="contact-list">
            <view class="contact-item" @tap="copyText('zhangshangfei1007@163.com')">
              <view class="contact-icon-wrap contact-email">
                <text class="contact-icon">📧</text>
              </view>
              <view class="contact-info">
                <text class="contact-label">联系邮箱</text>
                <text class="contact-value">zhangshangfei1007@163.com</text>
              </view>
              <view class="contact-action">
                <text class="action-text">复制</text>
              </view>
            </view>

            <view class="contact-item" @tap="copyText('https://aipetfamily.cn/')">
              <view class="contact-icon-wrap contact-web">
                <text class="contact-icon">🌐</text>
              </view>
              <view class="contact-info">
                <text class="contact-label">官方网站</text>
                <text class="contact-value">aipetfamily.cn</text>
              </view>
              <view class="contact-action">
                <text class="action-text">复制</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 关于宠迹卡片 -->
        <view class="desc-card">
          <view class="desc-header">
            <view class="desc-icon-wrap">
              <text class="desc-icon">💝</text>
            </view>
            <text class="desc-title">关于宠迹</text>
          </view>
          <view class="desc-content">
            <text class="desc-text">宠迹是一款专为宠物主人打造的记录管理工具，帮助您轻松记录宠物的日常生活、健康状况、疫苗接种等重要信息，让每一只宠物都能得到更好的关爱。</text>
          </view>
        </view>

        <!-- 核心功能卡片 -->
        <view class="feature-card">
          <view class="feature-header">
            <view class="feature-icon-wrap">
              <text class="feature-icon">✨</text>
            </view>
            <text class="feature-title">核心功能</text>
          </view>

          <view class="feature-grid">
            <view class="feature-item">
              <view class="feature-dot feature-photo"></view>
              <text class="feature-text">发布动态，记录宠物精彩瞬间</text>
            </view>
            <view class="feature-item">
              <view class="feature-dot feature-checkin"></view>
              <text class="feature-text">每日打卡，养成养宠好习惯</text>
            </view>
            <view class="feature-item">
              <view class="feature-dot feature-vaccine"></view>
              <text class="feature-text">疫苗驱虫提醒，守护宠物健康</text>
            </view>
            <view class="feature-item">
              <view class="feature-dot feature-feed"></view>
              <text class="feature-text">喂食提醒，科学喂养不遗忘</text>
            </view>
            <view class="feature-item">
              <view class="feature-dot feature-social"></view>
              <text class="feature-text">发现宠友，分享养宠经验</text>
            </view>
          </view>
        </view>

        <!-- 页脚 -->
        <view class="footer-section">
          <view class="footer-divider"></view>
          <text class="footer-line1">© 2026 宠迹 PetTrail</text>
          <text class="footer-line2">Made with ❤️ for pets</text>
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
/* ============================================
   关于我们 - 统一设计系统 v2.0
   与 me/index.vue 风格完全一致
   ============================================ */

/* 设计变量 */
$primary: #ff6b35;
$primary-light: #ff8c5a;
$bg-color: #f2f2f7;
$white: #ffffff;
$gray-50: #fafafa;
$gray-100: #f5f5f7;
$gray-200: #e5e5ea;
$gray-300: #d1d1d6;
$gray-500: #8e8e93;
$gray-800: #1c1c1e;

$radius-sm: 12rpx;
$radius-md: 20rpx;
$radius-lg: 28rpx;

/* ========== 页面基础 ========== */
.about-page { min-height: 100vh; background: $bg-color; }

.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: $white; }
.status-bar { width: 100%; }

.nav-bar { 
  height: 92rpx; 
  display: flex; align-items: center; justify-content: space-between; 
  padding: 0 32rpx; 
  border-bottom: 1rpx solid $gray-100;
}

.nav-back { 
  width: 64rpx; height: 64rpx; 
  display: flex; align-items: center; justify-content: center;
  border-radius: 32rpx;
  background: $gray-50;
  
  &:active { background: $gray-200; transform: scale(0.92); }
}

.nav-back-arrow { 
  width: 18rpx; height: 18rpx; 
  border-left: 3rpx solid $gray-800; border-bottom: 3rpx solid $gray-800; 
  transform: rotate(45deg); margin-left: -4rpx;
}

.nav-title { font-size: 34rpx; font-weight: 700; color: $gray-800; letter-spacing: 0.5rpx; }
.nav-placeholder { width: 64rpx; }

.page-scroll { height: 100vh; }
.page-content { padding: 24rpx 28rpx; }

/* ========== 品牌展示区 ========== */
.brand-section {
  display: flex; flex-direction: column; align-items: center;
  padding: 48rpx 0 40rpx;
}

.logo-container {
  margin-bottom: 24rpx;
  
  &:active { transform: scale(0.96); }
}

.logo-ring {
  width: 160rpx; height: 160rpx; border-radius: 80rpx;
  padding: 4rpx;
  background: linear-gradient(135deg, $primary, $primary-light, #ffb347);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 12rpx 32rpx rgba(255, 107, 53, 0.25);
}

.logo-icon { font-size: 72rpx; }

.app-name { 
  font-size: 42rpx; font-weight: 700; color: $gray-800; 
  margin-bottom: 16rpx; letter-spacing: 2rpx;
}

.slogan-box {
  padding: 10rpx 28rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08), rgba(255, 107, 53, 0.03));
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.app-slogan { 
  font-size: 26rpx; color: $primary; font-weight: 600;
  letter-spacing: 1rpx;
}

.version-badge {
  padding: 6rpx 24rpx;
  background: $gray-50;
  border-radius: 16rpx;
  border: 1rpx solid $gray-200;
}

.version-text { font-size: 24rpx; color: $gray-500; font-weight: 500; }

/* ========== 卡片容器通用 ========== */
.info-card, .desc-card, .feature-card {
  background: $white; border-radius: $radius-md; margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

/* ========== 联系信息卡片 ========== */
.card-header {
  display: flex; align-items: center; gap: 16rpx;
  padding: 28rpx 28rpx 20rpx;
  background: linear-gradient(180deg, $white 0%, #fff9f5 100%);
  border-bottom: 1rpx solid rgba(255, 107, 53, 0.08);
}

.card-icon-wrap {
  width: 44rpx; height: 44rpx; border-radius: 12rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05));
  display: flex; align-items: center; justify-content: center;
}

.card-icon { font-size: 24rpx; }

.card-title { font-size: 28rpx; font-weight: 600; color: $gray-800; letter-spacing: 0.3rpx; }

.contact-list { padding: 8rpx 20rpx 24rpx; }

.contact-item {
  display: flex; align-items: center; padding: 24rpx 8rpx;
  transition: all 0.25s ease;

  &:active { background: $gray-50; border-radius: $radius-sm; }
}

.contact-icon-wrap {
  width: 56rpx; height: 56rpx; border-radius: 14rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: 16rpx; flex-shrink: 0;

  &.contact-email { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
  &.contact-web { background: linear-gradient(135deg, #fef3c7, #fde68a); }
}

.contact-icon { font-size: 28rpx; }

.contact-info { flex: 1; }

.contact-label { display: block; font-size: 24rpx; color: $gray-500; margin-bottom: 6rpx; }
.contact-value { display: block; font-size: 28rpx; color: $gray-800; font-weight: 500; }

.contact-action {
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08), rgba(255, 107, 53, 0.03));
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 107, 53, 0.15);
}

.action-text { font-size: 24rpx; color: $primary; font-weight: 600; }

/* ========== 关于描述卡片 ========== */
.desc-header {
  display: flex; align-items: center; gap: 16rpx;
  padding: 28rpx 28rpx 20rpx;
}

.desc-icon-wrap {
  width: 44rpx; height: 44rpx; border-radius: 12rpx;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(236, 72, 153, 0.05));
  display: flex; align-items: center; justify-content: center;
}

.desc-icon { font-size: 24rpx; }

.desc-title { font-size: 28rpx; font-weight: 600; color: $gray-800; letter-spacing: 0.3rpx; }

.desc-content { padding: 0 28rpx 28rpx; }

.desc-text { 
  font-size: 28rpx; color: $gray-500; line-height: 1.8; 
  font-weight: 400;
}

/* ========== 核心功能卡片 ========== */
.feature-header {
  display: flex; align-items: center; gap: 16rpx;
  padding: 28rpx 28rpx 20rpx;
}

.feature-icon-wrap {
  width: 44rpx; height: 44rpx; border-radius: 12rpx;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
  display: flex; align-items: center; justify-content: center;
}

.feature-icon { font-size: 24rpx; }

.feature-title { font-size: 28rpx; font-weight: 600; color: $gray-800; letter-spacing: 0.3rpx; }

.feature-grid { padding: 8rpx 28rpx 28rpx; display: flex; flex-direction: column; gap: 20rpx; }

.feature-item {
  display: flex; align-items: center;
  padding: 16rpx;
  border-radius: $radius-sm;
  background: $gray-50;
  transition: all 0.25s ease;

  &:active { background: $gray-100; transform: translateX(4rpx); }
}

.feature-dot {
  width: 36rpx; height: 36rpx; border-radius: 10rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: 14rpx; flex-shrink: 0;
  font-size: 20rpx;

  &.feature-photo { background: linear-gradient(135deg, #fce7f3, #fbcfe8); }
  &.feature-checkin { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
  &.feature-vaccine { background: linear-gradient(135deg, #dcfce7, #bbf7d0); }
  &.feature-feed { background: linear-gradient(135deg, #fef3c7, #fde68a); }
  &.feature-social { background: linear-gradient(135deg, #f3e8ff, #e9d5ff); }
}

.feature-text { font-size: 27rpx; color: $gray-800; font-weight: 500; line-height: 1.4; }

/* ========== 页脚 ========== */
.footer-section {
  display: flex; flex-direction: column; align-items: center;
  padding: 40rpx 0 80rpx;
}

.footer-divider {
  width: 120rpx; height: 3rpx;
  background: linear-gradient(90deg, transparent, $gray-300, transparent);
  margin-bottom: 24rpx;
  border-radius: 2rpx;
}

.footer-line1 { font-size: 26rpx; color: $gray-500; margin-bottom: 8rpx; font-weight: 500; }
.footer-line2 { font-size: 24rpx; color: $gray-300; font-weight: 400; }
</style>
