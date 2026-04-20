import { getTheme, applyTheme } from '@/utils/theme'

export default {
  onLoad() {
    this.applyAppTheme()
  },
  onShow() {
    this.applyAppTheme()
  },
  methods: {
    applyAppTheme() {
      const theme = getTheme()
      if (theme === 'dark') {
        uni.setPageStyle && uni.setPageStyle({ pageStyle: { darkmode: true } })
      }
      applyTheme(theme)
    }
  }
}
