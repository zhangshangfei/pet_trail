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
  --pt-primary-3: #ff9a5d;
  --pt-bg: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 50%, #f0f4f8 100%);
  --pt-bg-solid: #f5f7fa;
  --pt-card: rgba(255, 255, 255, 0.75);
  --pt-card-border: rgba(255, 255, 255, 0.5);
  --pt-glass-blur: 20px;
  --pt-text: #1a1a2e;
  --pt-text-secondary: #4a5568;
  --pt-muted: #718096;
  --pt-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  --pt-shadow-hover: 0 16px 48px rgba(31, 38, 135, 0.25);
  --pt-shadow-soft: 0 4px 16px rgba(31, 38, 135, 0.08);
  --pt-radius: 24rpx;
  --pt-radius-lg: 32rpx;
  --pt-radius-xl: 40rpx;
  --pt-border: rgba(255, 255, 255, 0.3);
  --pt-input-bg: rgba(255, 255, 255, 0.5);
  --pt-secondary: #666666;
  --pt-light: #999999;
  --pt-gradient-start: #ff6b6b;
  --pt-gradient-end: #ffa07a;
  --pt-wave-color-1: rgba(255, 107, 107, 0.15);
  --pt-wave-color-2: rgba(255, 160, 122, 0.12);
}

page.dark {
  --pt-primary: #ff6a3d;
  --pt-primary-2: #ff8a5d;
  --pt-primary-3: #ffaa7d;
  --pt-bg: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  --pt-bg-solid: #1a1a2e;
  --pt-card: rgba(30, 30, 45, 0.75);
  --pt-card-border: rgba(255, 255, 255, 0.1);
  --pt-glass-blur: 20px;
  --pt-text: #e5e5e5;
  --pt-text-secondary: #a0aec0;
  --pt-muted: #718096;
  --pt-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  --pt-shadow-hover: 0 16px 48px rgba(0, 0, 0, 0.5);
  --pt-shadow-soft: 0 4px 16px rgba(0, 0, 0, 0.25);
  --pt-border: rgba(255, 255, 255, 0.1);
  --pt-input-bg: rgba(255, 255, 255, 0.08);
  --pt-secondary: #aaaaaa;
  --pt-light: #777777;
  --pt-gradient-start: #ff6b6b;
  --pt-gradient-end: #ffa07a;
  --pt-wave-color-1: rgba(255, 107, 107, 0.2);
  --pt-wave-color-2: rgba(255, 160, 122, 0.15);
}

page {
  background: var(--pt-bg);
  background-attachment: fixed;
  color: var(--pt-text);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, "Noto Sans", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.pt-card {
  background: var(--pt-card);
  border-radius: var(--pt-radius-lg);
  box-shadow: var(--pt-shadow-soft);
  backdrop-filter: blur(var(--pt-glass-blur));
  -webkit-backdrop-filter: blur(var(--pt-glass-blur));
  border: 1rpx solid var(--pt-card-border);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.pt-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1rpx;
  background: linear-gradient(90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  pointer-events: none;
}

.pt-card:hover,
.pt-card.hover-effect {
  box-shadow: var(--pt-shadow-hover);
  transform: translateY(-4rpx);
}

.pt-card:active,
.pt-card.press-effect {
  transform: translateY(0) scale(0.98);
  transition-duration: 0.1s;
}

.pt-btn-primary {
  background: linear-gradient(135deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
  color: #ffffff;
  border-radius: 999rpx;
  padding: 24rpx 48rpx;
  font-weight: 600;
  font-size: 28rpx;
  letter-spacing: 1rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 90, 61, 0.35);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  border: none;
}

.pt-btn-primary::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: left 0.5s ease;
}

.pt-btn-primary.button-hover,
.pt-btn-primary:hover {
  transform: translateY(-4rpx) scale(1.02);
  box-shadow: 0 12rpx 32rpx rgba(255, 90, 61, 0.45);
}

.pt-btn-primary.button-hover::after,
.pt-btn-primary:hover::after {
  left: 100%;
}

.pt-btn-primary:active {
  transform: translateY(0) scale(0.96);
  box-shadow: 0 4rpx 16rpx rgba(255, 90, 61, 0.3);
  transition-duration: 0.1s;
}

.pt-header {
  background: linear-gradient(180deg,
    rgba(255, 122, 61, 0.95) 0%,
    rgba(255, 77, 79, 0.85) 55%,
    rgba(255, 255, 255, 0) 100%
  );
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.wave-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120rpx;
  overflow: hidden;
  line-height: 0;
}

.wave-decoration svg {
  position: relative;
  display: block;
  width: calc(100% + 1.3px);
  height: 120rpx;
}

.wave-decoration .wave-path {
  fill: var(--pt-wave-color-1);
}

.wave-decoration .wave-path-secondary {
  fill: var(--pt-wave-color-2);
  opacity: 0.6;
  animation: waveFloat 8s ease-in-out infinite;
}

@keyframes waveFloat {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-20rpx); }
}

.glass-input {
  background: var(--pt-input-bg);
  border: 1rpx solid var(--pt-border);
  border-radius: var(--pt-radius);
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  color: var(--pt-text);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: all 0.25s ease;
}

.glass-input:focus,
.glass-input:focus-within {
  border-color: var(--pt-primary);
  box-shadow: 0 0 0 4rpx rgba(255, 90, 61, 0.15);
  background: rgba(255, 255, 255, 0.85);
  outline: none;
}

.glass-tag {
  display: inline-flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.6);
  border: 1rpx solid var(--pt-border);
  border-radius: 999rpx;
  font-size: 24rpx;
  color: var(--pt-text-secondary);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: all 0.25s ease;
}

.glass-tag:active {
  background: rgba(255, 90, 61, 0.15);
  border-color: var(--pt-primary);
  color: var(--pt-primary);
  transform: scale(0.95);
}

.glass-divider {
  height: 1rpx;
  background: linear-gradient(90deg,
    transparent,
    var(--pt-border),
    transparent
  );
  margin: 32rpx 0;
}

.fade-in {
  animation: fadeIn 0.4s ease-out;
}

.slide-up {
  animation: slideUp 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pulse-glow {
  animation: pulseGlow 2s ease-in-out infinite;
}

@keyframes pulseGlow {
  0%, 100% {
    box-shadow: 0 0 20rpx rgba(255, 90, 61, 0.2);
  }
  50% {
    box-shadow: 0 0 40rpx rgba(255, 90, 61, 0.4);
  }
}
</style>