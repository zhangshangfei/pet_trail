const THEME_KEY = 'app_theme'

const USE_CUSTOM_TABBAR = true

export function getTheme() {
  return uni.getStorageSync(THEME_KEY) || 'light'
}

export function setTheme(theme) {
  uni.setStorageSync(THEME_KEY, theme)
}

export function toggleTheme() {
  const current = getTheme()
  const next = current === 'light' ? 'dark' : 'light'
  setTheme(next)
  return next
}

export function isDark() {
  return getTheme() === 'dark'
}

export function applyTheme(theme) {
  if (USE_CUSTOM_TABBAR) return
  const style = theme === 'dark'
    ? { backgroundColor: '#1a1a1a', borderStyle: 'black' }
    : { backgroundColor: '#ffffff', borderStyle: 'black' }
  uni.setTabBarStyle(style)
}
