import { ref } from 'vue'
import { useUserStore } from '@/store/user'
import { formatTime, getRelativeTime, normalizeDate, getDateKey, getTimeText, formatRecordDate, getAgeText } from '@/utils/format'

export function usePageCommon() {
  const statusBarHeight = ref(20)
  const headerHeight = ref(74)

  function initNavMetrics() {
    try {
      const sys = uni.getSystemInfoSync()
      statusBarHeight.value = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      statusBarHeight.value = 20
    }
    headerHeight.value = statusBarHeight.value + 54
  }

  async function loadUserInfo() {
    const userStore = useUserStore()
    await userStore.loadUserInfo()
  }

  function onTopUserTap(userId) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    if (userId) {
      uni.navigateTo({ url: `/pages/user/detail?id=${userId}` })
    }
  }

  return {
    statusBarHeight,
    headerHeight,
    initNavMetrics,
    loadUserInfo,
    onTopUserTap,
    formatTime,
    getRelativeTime,
    normalizeDate,
    getDateKey,
    getTimeText,
    formatRecordDate,
    getAgeText
  }
}
