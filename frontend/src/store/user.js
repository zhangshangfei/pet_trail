import { defineStore } from 'pinia'
import { auth } from '@/api'
import { getUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    tokenExpireTime: uni.getStorageSync('tokenExpireTime') || 0,
    userInfo: uni.getStorageSync('userInfo') || {}
  }),

  getters: {
    isLoggedIn: (state) => {
      return state.token && Date.now() < state.tokenExpireTime
    },
    userId: (state) => {
      return state.userInfo?.id || null
    },
    nickname: (state) => {
      return state.userInfo?.nickname || '宠物管家'
    },
    avatar: (state) => {
      return getUserAvatar(state.userInfo?.id, state.userInfo?.avatar)
    }
  },

  actions: {
    // 设置登录信息
    setLoginInfo(token, userInfo) {
      const tokenExpireTime = Date.now() + 7 * 24 * 60 * 60 * 1000 // 7 天
      this.token = token
      this.tokenExpireTime = tokenExpireTime
      this.userInfo = userInfo

      // 持久化存储
      uni.setStorageSync('token', token)
      uni.setStorageSync('tokenExpireTime', tokenExpireTime)
      uni.setStorageSync('userInfo', userInfo)
    },

    // 退出登录
    logout() {
      this.token = ''
      this.tokenExpireTime = 0
      this.userInfo = {}

      // 清除本地存储
      uni.removeStorageSync('token')
      uni.removeStorageSync('tokenExpireTime')
      uni.removeStorageSync('userInfo')
    },

    // 更新用户信息
    updateUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      uni.setStorageSync('userInfo', this.userInfo)
    },

    // 微信登录
    async wechatLogin(code) {
      try {
        const res = await auth.loginByCode(code)
        if (res.success && res.data?.token) {
          this.setLoginInfo(res.data.token, res.data)
          return { success: true }
        }
        throw new Error(res.message || '登录失败')
      } catch (error) {
        console.error('微信登录失败:', error)
        return { success: false, message: error.message }
      }
    },

    // 开发环境登录（模拟）
    async devLogin() {
      try {
        const openid = 'dev-openid-' + Date.now()
        const res = await auth.register(openid, '', '测试用户', '')
        if (res.success && res.data?.token) {
          this.setLoginInfo(res.data.token, res.data)
          return { success: true }
        }
        throw new Error(res.message || '登录失败')
      } catch (error) {
        console.error('开发环境登录失败:', error)
        return { success: false, message: error.message }
      }
    }
  }
})
