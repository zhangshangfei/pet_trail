import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProfile } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || '{}'))
  const token = ref(localStorage.getItem('admin_token') || '')

  const isSuperAdmin = computed(() => adminInfo.value?.role === 'SUPER_ADMIN')
  const isLoggedIn = computed(() => !!token.value)

  async function fetchProfile() {
    try {
      const res = await getProfile()
      if (res.data) {
        adminInfo.value = res.data
        localStorage.setItem('admin_info', JSON.stringify(res.data))
      }
    } catch (e) {
      console.error('获取管理员信息失败:', e)
    }
  }

  function setLoginInfo(newToken, info) {
    token.value = newToken
    adminInfo.value = info
    localStorage.setItem('admin_token', newToken)
    localStorage.setItem('admin_info', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    adminInfo.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
  }

  return {
    adminInfo,
    token,
    isSuperAdmin,
    isLoggedIn,
    fetchProfile,
    setLoginInfo,
    logout
  }
})
