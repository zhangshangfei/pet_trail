<template>
  <view class="settings-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">设置</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">

        <!-- 通知设置组 -->
        <view class="settings-group">
          <text class="group-title">通知设置</text>
          <view class="settings-card">
            <view class="setting-item">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-bell">
                  <text class="setting-icon">🔔</text>
                </view>
                <text class="setting-label">推送通知</text>
              </view>
              <switch :checked="settings.pushEnabled" @change="onTogglePush" color="#ff6b35" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-vaccine">
                  <text class="setting-icon">💉</text>
                </view>
                <text class="setting-label">疫苗提醒</text>
              </view>
              <switch :checked="settings.vaccineReminder" @change="onToggleVaccine" color="#ff6b35" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-parasite">
                  <text class="setting-icon">🐛</text>
                </view>
                <text class="setting-label">驱虫提醒</text>
              </view>
              <switch :checked="settings.parasiteReminder" @change="onToggleParasite" color="#ff6b35" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-feed">
                  <text class="setting-icon">🍖</text>
                </view>
                <text class="setting-label">喂食提醒</text>
              </view>
              <switch :checked="settings.feedingReminder" @change="onToggleFeeding" color="#ff6b35" />
            </view>
          </view>
        </view>

        <!-- 隐私设置组 -->
        <view class="settings-group">
          <text class="group-title">隐私设置</text>
          <view class="settings-card">
            <view class="setting-item">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-eye">
                  <text class="setting-icon">👁️</text>
                </view>
                <text class="setting-label">公开我的动态</text>
              </view>
              <switch :checked="settings.publicPosts" @change="onTogglePublicPosts" color="#ff6b35" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-location">
                  <text class="setting-icon">📍</text>
                </view>
                <text class="setting-label">显示位置信息</text>
              </view>
              <switch :checked="settings.showLocation" @change="onToggleShowLocation" color="#ff6b35" />
            </view>
          </view>
        </view>

        <!-- 通用设置组 -->
        <view class="settings-group">
          <text class="group-title">通用</text>
          <view class="settings-card">
            <view class="setting-item">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-moon">
                  <text class="setting-icon">🌙</text>
                </view>
                <text class="setting-label">深色模式</text>
              </view>
              <switch :checked="isDarkMode" @change="onToggleDarkMode" color="#ff6b35" />
            </view>
            
            <view class="divider-line"></view>
            
            <view class="setting-item tap-item" @tap="clearCache">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-trash">
                  <text class="setting-icon">🗑️</text>
                </view>
                <text class="setting-label">清除缓存</text>
              </view>
              <view class="setting-right">
                <text class="setting-value">{{ cacheSize }}</text>
                <text class="setting-arrow">›</text>
              </view>
            </view>

            <view class="divider-line"></view>

            <view class="setting-item tap-item" @tap="goAbout">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-info">
                  <text class="setting-icon">ℹ️</text>
                </view>
                <text class="setting-label">关于我们</text>
              </view>
              <view class="setting-right">
                <text class="setting-arrow">›</text>
              </view>
            </view>

            <view class="divider-line"></view>

            <view class="setting-item tap-item" @tap="goFeedback">
              <view class="setting-left">
                <view class="setting-icon-wrap icon-feedback">
                  <text class="setting-icon">📝</text>
                </view>
                <text class="setting-label">意见反馈</text>
              </view>
              <view class="setting-right">
                <text class="setting-arrow">›</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 退出登录按钮 -->
        <view class="settings-group" v-if="isLoggedIn">
          <view class="action-card danger-card">
            <view class="action-item logout-item" @tap="onLogout">
              <text class="logout-text">退出登录</text>
            </view>
          </view>
        </view>

        <view class="page-bottom-safe"></view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 20,
      isLoggedIn: false,
      isDarkMode: false,
      cacheSize: '0KB',
      settings: {
        pushEnabled: true,
        vaccineReminder: true,
        parasiteReminder: true,
        feedingReminder: true,
        publicPosts: true,
        showLocation: false
      }
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    this.isLoggedIn = !!uni.getStorageSync('token')
    this.isDarkMode = uni.getStorageSync('app_theme') === 'dark'
    this.loadSettings()
    this.calcCacheSize()
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    loadSettings() {
      try {
        const saved = uni.getStorageSync('userSettings')
        if (saved) {
          this.settings = { ...this.settings, ...JSON.parse(saved) }
        }
      } catch (e) { /* ignore */ }
    },
    saveSettings() {
      try {
        uni.setStorageSync('userSettings', JSON.stringify(this.settings))
      } catch (e) { /* ignore */ }
    },
    onTogglePush(e) { this.settings.pushEnabled = e.detail.value; this.saveSettings() },
    onToggleVaccine(e) { this.settings.vaccineReminder = e.detail.value; this.saveSettings() },
    onToggleParasite(e) { this.settings.parasiteReminder = e.detail.value; this.saveSettings() },
    onToggleFeeding(e) { this.settings.feedingReminder = e.detail.value; this.saveSettings() },
    onTogglePublicPosts(e) { this.settings.publicPosts = e.detail.value; this.saveSettings() },
    onToggleShowLocation(e) { this.settings.showLocation = e.detail.value; this.saveSettings() },
    onToggleDarkMode(e) {
      const isDark = e.detail.value
      this.isDarkMode = isDark
      const theme = isDark ? 'dark' : 'light'
      uni.setStorageSync('app_theme', theme)
      const pages = getCurrentPages()
      if (pages.length > 0) {
        const page = pages[pages.length - 1]
        if (isDark) {
          page.$page && uni.setPageStyle({ pageStyle: { darkmode: true } })
        }
      }
    },
    calcCacheSize() {
      try {
        const res = uni.getStorageInfoSync()
        const size = res.currentSize || 0
        this.cacheSize = size > 1024 ? `${(size / 1024).toFixed(1)}MB` : `${size}KB`
      } catch (e) {
        this.cacheSize = '未知'
      }
    },
    clearCache() {
      const self = this
      uni.showModal({
        title: '清除缓存',
        content: '确定清除本地缓存数据吗？（不会清除登录状态）',
        async success(res) {
          if (!res.confirm) return
          try {
            const token = uni.getStorageSync('token')
            const userInfo = uni.getStorageSync('userInfo')
            const userSettings = uni.getStorageSync('userSettings')
            uni.clearStorageSync()
            if (token) uni.setStorageSync('token', token)
            if (userInfo) uni.setStorageSync('userInfo', userInfo)
            if (userSettings) uni.setStorageSync('userSettings', userSettings)
            self.calcCacheSize()
            uni.showToast({ title: '缓存已清除', icon: 'success' })
          } catch (e) {
            uni.showToast({ title: '清除失败', icon: 'none' })
          }
        }
      })
    },
    goAbout() {
      uni.navigateTo({ url: '/pages/me/about' })
    },
    goFeedback() {
      uni.navigateTo({ url: '/pages/me/feedback' })
    },
    onLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        confirmColor: '#ff6b35',
        success(res) {
          if (!res.confirm) return
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.removeStorageSync('tokenExpireTime')
          uni.showToast({ title: '已退出登录', icon: 'success' })
          setTimeout(() => {
            uni.switchTab({ url: '/pages/home/index' })
          }, 1500)
        }
      })
    },
    onDeactivate() {
      const self = this
      uni.showModal({
        title: '注销账号',
        content: '注销后您的所有数据将被永久删除且无法恢复，确定要注销吗？',
        confirmColor: '#ef4444',
        confirmText: '确认注销',
        cancelText: '再想想',
        async success(res) {
          if (!res.confirm) return
          uni.showModal({
            title: '二次确认',
            content: '这是最后确认，注销后无法找回任何数据，确定继续？',
            confirmColor: '#ef4444',
            confirmText: '确认注销',
            cancelText: '取消',
            async success(res2) {
              if (!res2.confirm) return
              try {
                const result = await authApi.deleteAccount()
                if (result && result.success) {
                  uni.clearStorageSync()
                  uni.showToast({ title: '账号已注销', icon: 'success' })
                  setTimeout(() => {
                    uni.switchTab({ url: '/pages/home/index' })
                  }, 1500)
                } else {
                  uni.showToast({ title: (result && result.message) || '注销失败', icon: 'none' })
                }
              } catch (e) {
                console.error('注销账号失败:', e)
                uni.showToast({ title: '网络错误', icon: 'none' })
              }
            }
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   系统设置 - 统一设计系统 v2.0
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
.settings-page { min-height: 100vh; background: $bg-color; }

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

/* ========== 设置分组 ========== */
.settings-group { margin-bottom: 28rpx; }

.group-title { 
  display: block; font-size: 24rpx; color: $gray-500; 
  margin-bottom: 14rpx; padding-left: 10rpx; font-weight: 600;
  letter-spacing: 0.5rpx;
}

/* ========== 设置卡片 ========== */
.settings-card {
  background: $white; border-radius: $radius-md; overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.setting-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 28rpx 24rpx;
  transition: all 0.25s ease;

  &.tap-item {
    &:active { background: $gray-50; }
  }
}

.setting-left { display: flex; align-items: center; flex: 1; gap: 16rpx; }

.setting-icon-wrap {
  width: 48rpx; height: 48rpx; border-radius: 12rpx;
  display: flex; align-items: center; justify-content: center;

  &.icon-bell { background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05)); }
  &.icon-vaccine { background: linear-gradient(135deg, rgba(34, 197, 94, 0.1), rgba(34, 197, 94, 0.05)); }
  &.icon-parasite { background: linear-gradient(135deg, rgba(168, 85, 247, 0.1), rgba(168, 85, 247, 0.05)); }
  &.icon-feed { background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.05)); }
  &.icon-eye { background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05)); }
  &.icon-location { background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(236, 72, 153, 0.05)); }
  &.icon-moon { background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(99, 102, 241, 0.05)); }
  &.icon-trash { background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(239, 68, 68, 0.05)); }
  &.icon-info { background: linear-gradient(135deg, rgba(107, 114, 128, 0.1), rgba(107, 114, 128, 0.05)); }
  &.icon-feedback { background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05)); }
}

.setting-icon { font-size: 26rpx; }

.setting-label { font-size: 29rpx; color: $gray-800; font-weight: 500; letter-spacing: 0.3rpx; }

.setting-right { display: flex; align-items: center; gap: 8rpx; }

.setting-value { font-size: 26rpx; color: $gray-500; font-weight: 500; }

.setting-arrow { font-size: 32rpx; color: $gray-300; font-weight: 700; }

/* 分隔线 */
.divider-line {
  height: 1rpx;
  background: linear-gradient(90deg, transparent, $gray-200, transparent);
  margin: 0 24rpx;
}

/* ========== 操作卡片 ========== */
.action-card {
  background: $white; border-radius: $radius-md; overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  &.danger-card { border: 2rpx solid rgba(239, 68, 68, 0.15); }
  &.warning-card { border: 2rpx solid rgba(245, 158, 11, 0.15); }
}

.action-item {
  display: flex; align-items: center; justify-content: center;
  padding: 28rpx 24rpx;
  transition: all 0.25s ease;

  &:active { transform: scale(0.98); }
}

.logout-item { background: linear-gradient(180deg, #fff, #fef2f2); }

.logout-text { font-size: 30rpx; color: #ef4444; font-weight: 700; letter-spacing: 1rpx; }

.deactivate-item { background: linear-gradient(180deg, #fff, #fffbeb); }

.deactivate-text { font-size: 28rpx; color: #d97706; font-weight: 600; }

.deactivate-hint { 
  display: block; text-align: center; font-size: 22rpx; 
  color: $gray-300; margin-top: 12rpx; padding-left: 8rpx; line-height: 1.5;
}

/* ========== 底部安全区域 ========== */
.page-bottom-safe { height: calc(40rpx + env(safe-area-inset-bottom)); }
</style>
