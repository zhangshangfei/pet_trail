import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProfile } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || '{}'))
  const token = ref(localStorage.getItem('admin_token') || '')
  const menus = ref(JSON.parse(localStorage.getItem('admin_menus') || '[]'))

  const isSuperAdmin = computed(() => adminInfo.value?.role === 'SUPER_ADMIN')
  const isLoggedIn = computed(() => !!token.value)
  const permissions = computed(() => {
    const perms = adminInfo.value?.permissions || ''
    if (!perms) return []
    return perms.split(',').map(p => p.trim()).filter(Boolean)
  })
  const role = computed(() => adminInfo.value?.role || '')
  const merchantId = computed(() => adminInfo.value?.merchantId || null)
  const isMerchant = computed(() => role.value === 'MERCHANT_ADMIN' || role.value === 'MERCHANT_STAFF')

  function hasPermission(code) {
    if (isSuperAdmin.value) return true
    return permissions.value.includes(code)
  }

  function hasAnyPermission(codes) {
    if (isSuperAdmin.value) return true
    return codes.some(code => permissions.value.includes(code))
  }

  async function fetchProfile() {
    try {
      const res = await getProfile()
      if (res.data) {
        adminInfo.value = res.data.admin || res.data
        localStorage.setItem('admin_info', JSON.stringify(res.data.admin || res.data))
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
    isSuperAdmin,
    isLoggedIn,
    permissions,
    role,
    merchantId,
    isMerchant,
    hasPermission,
    hasAnyPermission,
    fetchProfile,
    setLoginInfo,
    logout
  }
})
