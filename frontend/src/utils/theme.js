const THEME_KEY = 'app_theme'

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
  if (theme === 'dark') {
    uni.setTabBarStyle({
      backgroundColor: '#1a1a1a',
      borderStyle: 'black'
    })
  } else {
    uni.setTabBarStyle({
      backgroundColor: '#ffffff',
      borderStyle: 'black'
    })
  }
}
