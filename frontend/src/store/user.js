import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(null)
  const isLoggedIn = ref(!!token.value)

  try {
    const cachedInfo = uni.getStorageSync('userInfo')
    if (cachedInfo && typeof cachedInfo === 'object') {
      userInfo.value = cachedInfo
    }
  } catch (e) {
  }

  const userName = computed(() => userInfo.value?.nickname || '')
  const avatarUrl = computed(() => userInfo.value?.avatar || '')

  async function loadUserInfo() {
    if (!token.value) {
      isLoggedIn.value = false
      userInfo.value = null
      return
    }
    try {
      const res = await uni.$request.get('/api/users/profile')
      if (res.success && res.data) {
        userInfo.value = res.data
        isLoggedIn.value = true
        uni.setStorageSync('userInfo', res.data)
      }
    } catch (e) {
      console.error('加载用户信息失败:', e)
    }
  }

  function setToken(newToken) {
    token.value = newToken
    isLoggedIn.value = !!newToken
    if (newToken) {
      uni.setStorageSync('token', newToken)
    } else {
      uni.removeStorageSync('token')
      uni.removeStorageSync('tokenExpireTime')
      uni.removeStorageSync('userInfo')
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    uni.removeStorageSync('token')
    uni.removeStorageSync('tokenExpireTime')
    uni.removeStorageSync('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userName,
    avatarUrl,
    loadUserInfo,
    setToken,
    logout
  }
})
