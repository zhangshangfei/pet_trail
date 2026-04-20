<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { getTheme, applyTheme } from '@/utils/theme'

onLaunch(() => {
  console.log('App Launch')
  const theme = getTheme()
  applyTheme(theme)
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style>
:root {
  --pt-primary: #ff5a3d;
  --pt-primary-2: #ff7a3d;
  --pt-bg: #f7f3ef;
  --pt-card: rgba(255, 255, 255, 0.96);
  --pt-text: #111827;
  --pt-muted: #8b93a6;
  --pt-shadow: 0 18rpx 44rpx rgba(0, 0, 0, 0.12);
  --pt-shadow-soft: 0 12rpx 28rpx rgba(17, 24, 39, 0.08);
  --pt-radius: 24rpx;
  --pt-border: #f0f0f0;
  --pt-input-bg: #f5f5f5;
  --pt-secondary: #666666;
  --pt-light: #999999;
}

page.dark {
  --pt-primary: #ff6a3d;
  --pt-primary-2: #ff8a5d;
  --pt-bg: #0d0d0d;
  --pt-card: rgba(30, 30, 30, 0.96);
  --pt-text: #e5e5e5;
  --pt-muted: #8b93a6;
  --pt-shadow: 0 18rpx 44rpx rgba(0, 0, 0, 0.4);
  --pt-shadow-soft: 0 12rpx 28rpx rgba(0, 0, 0, 0.3);
  --pt-border: #2a2a2a;
  --pt-input-bg: #1a1a1a;
  --pt-secondary: #aaaaaa;
  --pt-light: #777777;
}

page {
  background: var(--pt-bg);
  color: var(--pt-text);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, "Noto Sans", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.pt-card {
  background: var(--pt-card);
  border-radius: var(--pt-radius);
  box-shadow: var(--pt-shadow-soft);
}

.pt-btn-primary {
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
  color: #ffffff;
  border-radius: 999rpx;
}

.pt-btn-primary.button-hover,
.pt-btn-primary:active {
  opacity: 0.92;
}

.pt-header {
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 55%, rgba(255, 255, 255, 0) 100%);
}
</style>