<template>
  <view class="login-page">
    <!-- Logo 区域 -->
    <view class="logo-section">
      <view class="logo-icon">🐾</view>
      <text class="app-name">宠迹</text>
      <text class="app-slogan">记录宠物的每一个成长瞬间</text>
    </view>

    <!-- 登录按钮 -->
    <view class="login-section">
      <button class="login-btn" type="primary" @click="handleWechatLogin" :loading="loading">
        <text class="btn-icon">🔐</text>
        <text>微信一键登录</text>
      </button>
      
      <view class="tips-section">
        <text class="tips-text">登录即代表您同意</text>
        <text class="tips-link" @click="showUserAgreement">《用户协议》</text>
        <text class="tips-text">和</text>
        <text class="tips-link" @click="showPrivacyPolicy">《隐私政策》</text>
      </view>
    </view>

    <!-- 底部说明 -->
    <view class="footer-section">
      <text class="footer-text">科学养宠 · 健康成长</text>
    </view>
  </view>
</template>

<script>
import * as auth from '@/api/auth'

export default {
  data() {
    return {
      loading: false
    }
  },
  onLoad() {
    // 检查是否已登录
    this.checkLoginStatus()
  },
  methods: {
    // 检查登录状态
    checkLoginStatus() {
      const token = uni.getStorageSync('token')
      const tokenExpireTime = uni.getStorageSync('tokenExpireTime')
      
      if (token && tokenExpireTime && Date.now() < tokenExpireTime) {
        // 已登录，跳转到首页
        uni.reLaunch({
          url: '/pages/dashboard/index'
        })
      }
    },

    // 处理微信登录
    async handleWechatLogin() {
      if (this.loading) return
      
      this.loading = true
      
      try {
        // #ifdef MP-WEIXIN
        // 微信小程序登录
        const loginResult = await this.wxLogin()
        
        if (loginResult.code) {
          // 使用 code 换取 token
          await this.loginByCode(loginResult.code)
        } else {
          throw new Error('微信登录失败')
        }
        // #endif
        
        // #ifdef H5
        // H5 环境（开发测试用）
        await this.loginForDev()
        // #endif
        
        // 登录成功，跳转到首页
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/dashboard/index'
          })
        }, 1000)
        
      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({
          title: error.message || '登录失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    // 微信小程序登录
    wxLogin() {
      return new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: (res) => {
            console.log('微信登录成功:', res)
            resolve(res)
          },
          fail: (err) => {
            console.error('微信登录失败:', err)
            reject(new Error('微信登录失败'))
          }
        })
      })
    },

    // 使用 code 登录
    async loginByCode(code) {
      const res = await auth.loginByCode(code)

      console.log('[login] 登录响应 res:', res)
      console.log('[login] res.data:', res.data)
      console.log('[login] res.data.token:', res.data?.token)
      console.log('[login] res.data.expireTime:', res.data?.expireTime)
      console.log('[login] res.data.user:', res.data?.user)

      if (res.success && res.data) {
        // 后端返回格式：{ token, user, expireTime }
        const { token, expireTime, user } = res.data

        console.log('[login] 准备保存 - token:', token ? token.substring(0, 20) + '...' : '空')
        console.log('[login] 准备保存 - expireTime:', expireTime)
        console.log('[login] 准备保存 - user:', user)

        // 保存 token 和过期时间
        uni.setStorageSync('token', token)
        // 如果后端没有返回 expireTime，使用默认值（当前时间 + 7 天）
        uni.setStorageSync('tokenExpireTime', expireTime || (Date.now() + 7 * 24 * 60 * 60 * 1000))

        // 保存用户信息
        if (user) {
          uni.setStorageSync('userInfo', user)
        }

        // 验证保存结果
        console.log('[login] 验证保存 - token:', uni.getStorageSync('token') ? '已保存' : '未保存')
        console.log('[login] 验证保存 - expireTime:', uni.getStorageSync('tokenExpireTime'))

        return { success: true, data: res.data }
      } else {
        throw new Error(res.message || '登录失败')
      }
    },

    // H5 开发环境登录（模拟）
    async loginForDev() {
      // 开发环境可以使用固定的 openid 登录
      const res = await auth.register('dev-openid-' + Date.now(), '', '测试用户', '')

      if (res.success && res.data) {
        const { token, expireTime, user } = res.data
        uni.setStorageSync('token', token)
        uni.setStorageSync('tokenExpireTime', expireTime)
        uni.setStorageSync('userInfo', user)
        
        return { success: true, data: res.data }
      } else {
        throw new Error(res.message || '登录失败')
      }
    },

    // 显示用户协议
    showUserAgreement() {
      uni.showModal({
        title: '用户协议',
        content: '欢迎使用宠迹小程序！\n\n我们将为您提供宠物健康管理、成长记录等服务。请您在使用前仔细阅读用户协议。',
        showCancel: false,
        confirmText: '我知道了'
      })
    },

    // 显示隐私政策
    showPrivacyPolicy() {
      uni.showModal({
        title: '隐私政策',
        content: '我们非常重视您的隐私保护。\n\n我们仅会收集必要的信息以提供服务，包括宠物信息、健康记录等。所有数据都将严格保密。',
        showCancel: false,
        confirmText: '我知道了'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 120rpx;
}

.logo-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.app-name {
  font-size: 64rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.app-slogan {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-btn {
  width: 100%;
  max-width: 600rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.2);
  margin-bottom: 40rpx;
}

.login-btn::after {
  border: none;
}

.btn-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.tips-section {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.tips-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}

.tips-link {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: underline;
  margin: 0 8rpx;
}

.footer-section {
  position: absolute;
  bottom: 60rpx;
  text-align: center;
}

.footer-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.5);
}
</style>
