import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProfile } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || '{}'))
  const token = ref(localStorage.getItem('admin_token') || '')
  const menus = ref(JSON.parse(localStorage.getItem('admin_menus') || '[]'))

  const isLoggedIn = computed(() => !!token.value)
  const roleId = computed(() => adminInfo.value?.roleId || null)
  const roleCode = computed(() => adminInfo.value?.roleCode || '')
  const roleName = computed(() => adminInfo.value?.roleName || '')
  const merchantId = computed(() => adminInfo.value?.merchantId || null)
  const isSuperAdmin = computed(() => roleCode.value === 'SUPER_ADMIN')
  const isMerchant = computed(() => roleCode.value === 'MERCHANT_ADMIN' || roleCode.value === 'MERCHANT_STAFF')
  const buttons = computed(() => adminInfo.value?.buttons || [])

  function hasButton(code) {
    if (isSuperAdmin.value) return true
    return buttons.value.includes(code)
  }

  async function fetchProfile() {
    try {
      const res = await getProfile()
      if (res.data) {
        const admin = res.data.admin || res.data
        adminInfo.value = admin
        localStorage.setItem('admin_info', JSON.stringify(admin))
        if (res.data.menus) {
          menus.value = res.data.menus
          localStorage.setItem('admin_menus', JSON.stringify(res.data.menus))
        }
      }
    } catch (e) {
      console.error('获取管理员信息失败:', e)
    }
  }

  function setLoginInfo(newToken, info, menuList) {
    token.value = newToken
    adminInfo.value = info
    if (menuList) {
      menus.value = menuList
      localStorage.setItem('admin_menus', JSON.stringify(menuList))
    }
    localStorage.setItem('admin_token', newToken)
    localStorage.setItem('admin_info', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    adminInfo.value = {}
    menus.value = []
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
    localStorage.removeItem('admin_menus')
  }

  return {
    adminInfo,
    token,
    menus,
    isLoggedIn,
    roleId,
    roleCode,
    roleName,
    merchantId,
    isSuperAdmin,
    isMerchant,
    buttons,
    hasButton,
    fetchProfile,
    setLoginInfo,
    logout
  }
})
