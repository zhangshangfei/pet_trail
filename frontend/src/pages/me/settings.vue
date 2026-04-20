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

        <view class="settings-group">
          <text class="group-title">通知设置</text>
          <view class="settings-card">
            <view class="setting-item">
              <view class="setting-left">
                <text class="setting-icon">🔔</text>
                <text class="setting-label">推送通知</text>
              </view>
              <switch :checked="settings.pushEnabled" @change="onTogglePush" color="#ff6a3d" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <text class="setting-icon">💉</text>
                <text class="setting-label">疫苗提醒</text>
              </view>
              <switch :checked="settings.vaccineReminder" @change="onToggleVaccine" color="#ff6a3d" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <text class="setting-icon">🐛</text>
                <text class="setting-label">驱虫提醒</text>
              </view>
              <switch :checked="settings.parasiteReminder" @change="onToggleParasite" color="#ff6a3d" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <text class="setting-icon">🍖</text>
                <text class="setting-label">喂食提醒</text>
              </view>
              <switch :checked="settings.feedingReminder" @change="onToggleFeeding" color="#ff6a3d" />
            </view>
          </view>
        </view>

        <view class="settings-group">
          <text class="group-title">隐私设置</text>
          <view class="settings-card">
            <view class="setting-item">
              <view class="setting-left">
                <text class="setting-icon">👁️</text>
                <text class="setting-label">公开我的动态</text>
              </view>
              <switch :checked="settings.publicPosts" @change="onTogglePublicPosts" color="#ff6a3d" />
            </view>
            <view class="setting-item">
              <view class="setting-left">
                <text class="setting-icon">📍</text>
                <text class="setting-label">显示位置信息</text>
              </view>
              <switch :checked="settings.showLocation" @change="onToggleShowLocation" color="#ff6a3d" />
            </view>
          </view>
        </view>

        <view class="settings-group">
          <text class="group-title">通用</text>
          <view class="settings-card">
            <view class="setting-item">
              <view class="setting-left">
                <text class="setting-icon">🌙</text>
                <text class="setting-label">深色模式</text>
              </view>
              <switch :checked="isDarkMode" @change="onToggleDarkMode" color="#ff6a3d" />
            </view>
            <view class="setting-item" @tap="clearCache">
              <view class="setting-left">
                <text class="setting-icon">🗑️</text>
                <text class="setting-label">清除缓存</text>
              </view>
              <text class="setting-value">{{ cacheSize }}</text>
            </view>
            <view class="setting-item" @tap="goAbout">
              <view class="setting-left">
                <text class="setting-icon">ℹ️</text>
                <text class="setting-label">关于我们</text>
              </view>
              <text class="setting-arrow">›</text>
            </view>
            <view class="setting-item" @tap="goFeedback">
              <view class="setting-left">
                <text class="setting-icon">📝</text>
                <text class="setting-label">意见反馈</text>
              </view>
              <text class="setting-arrow">›</text>
            </view>
          </view>
        </view>

        <view class="settings-group" v-if="isLoggedIn">
          <view class="settings-card">
            <view class="setting-item logout-item" @tap="onLogout">
              <text class="logout-text">退出登录</text>
            </view>
          </view>
        </view>

        <!-- 账号注销入口暂时隐藏 -->
        <!--
        <view class="settings-group" v-if="isLoggedIn">
          <view class="settings-card">
            <view class="setting-item deactivate-item" @tap="onDeactivate">
              <text class="deactivate-text">注销账号</text>
            </view>
          </view>
          <text class="deactivate-hint">注销后账号数据将无法恢复，请谨慎操作</text>
        </view>
        -->

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
      if (isDark) {
        uni.setTabBarStyle({ backgroundColor: '#1a1a1a', borderStyle: 'black' })
      } else {
        uni.setTabBarStyle({ backgroundColor: '#ffffff', borderStyle: 'black' })
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
        confirmColor: '#ff6a3d',
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
        confirmColor: '#ff4d4f',
        confirmText: '确认注销',
        cancelText: '再想想',
        async success(res) {
          if (!res.confirm) return
          uni.showModal({
            title: '二次确认',
            content: '这是最后确认，注销后无法找回任何数据，确定继续？',
            confirmColor: '#ff4d4f',
            confirmText: '确认注销',
            cancelText: '取消',
            async success(res2) {
              if (!res2.confirm) return
              try {
                const result = await uni.$request.delete('/api/users/account')
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
$primary: #ff6a3d;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.settings-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.settings-group { margin-bottom: 24rpx; }
.group-title { display: block; font-size: 24rpx; color: $text-light; margin-bottom: 12rpx; padding-left: 8rpx; }

.settings-card {
  background: $card-bg; border-radius: 24rpx; overflow: hidden;
}
.setting-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 28rpx 24rpx; border-bottom: 1rpx solid #f5f5f5;
}
.setting-item:last-child { border-bottom: none; }
.setting-left { display: flex; align-items: center; flex: 1; }
.setting-icon { font-size: 32rpx; margin-right: 16rpx; }
.setting-label { font-size: 28rpx; color: $text-primary; }
.setting-value { font-size: 26rpx; color: $text-light; }
.setting-arrow { font-size: 32rpx; color: #d1d5db; }

.logout-item { justify-content: center; }
.logout-text { font-size: 30rpx; color: #ef4444; font-weight: 600; }

.deactivate-item { justify-content: center; }
.deactivate-text { font-size: 28rpx; color: #999; }
.deactivate-hint { display: block; font-size: 22rpx; color: #bbb; margin-top: 8rpx; padding-left: 8rpx; text-align: center; }

.page-bottom-safe { height: calc(40rpx + env(safe-area-inset-bottom)); }
</style>
