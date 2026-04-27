import { useAdminStore } from '@/store/admin'

export const permission = {
  mounted(el, binding) {
    const code = binding.value
    if (!code) return
    const adminStore = useAdminStore()
    if (!adminStore.hasButton(code)) {
      el.parentNode?.removeChild(el)
    }
  }
}

export const hasPermission = (code) => {
  const adminStore = useAdminStore()
  return adminStore.hasButton(code)
}
