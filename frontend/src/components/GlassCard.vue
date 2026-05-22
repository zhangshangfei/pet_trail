<template>
  <view
    class="glass-card"
    :class="[
      { 'glass-card--hover': hoverEffect },
      { 'glass-card--pressed': pressedEffect },
      `glass-card--${size}`,
      customClass
    ]"
    :style="cardStyle"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
    @touchcancel="onTouchEnd"
  >
    <!-- 顶部高光 -->
    <view class="glass-card__highlight"></view>

    <!-- 波浪形装饰（可选） -->
    <view v-if="showWave" class="glass-card__wave">
      <svg viewBox="0 0 1440 120" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
        <path
          class="wave-path-primary"
          d="M0,64 C288,120 576,20 864,64 C1152,108 1344,40 1440,64 L1440,120 L0,120 Z"
          :fill="waveColor1"
        />
        <path
          class="wave-path-secondary"
          d="M0,80 C360,130 720,30 1080,80 C1260,105 1380,60 1440,80 L1440,120 L0,120 Z"
          :fill="waveColor2"
        />
      </svg>
    </view>

    <!-- 卡片内容插槽 -->
    <view class="glass-card__content">
      <slot></slot>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  hoverEffect: {
    type: Boolean,
    default: true
  },
  pressedEffect: {
    type: Boolean,
    default: true
  },
  showWave: {
    type: Boolean,
    default: false
  },
  waveColor1: {
    type: String,
    default: 'rgba(255, 107, 107, 0.12)'
  },
  waveColor2: {
    type: String,
    default: 'rgba(255, 160, 122, 0.08)'
  },
  blur: {
    type: String,
    default: '20px'
  },
  opacity: {
    type: [String, Number],
    default: 0.75
  },
  borderRadius: {
    type: String,
    default: ''
  },
  customClass: {
    type: String,
    default: ''
  }
})

const isPressed = ref(false)

const cardStyle = computed(() => ({
  '--glass-blur': props.blur,
  '--glass-opacity': typeof props.opacity === 'number' ? props.opacity : parseFloat(props.opacity),
  ...(props.borderRadius ? { '--glass-radius': props.borderRadius } : {})
}))

const onTouchStart = () => {
  if (props.pressedEffect) {
    isPressed.value = true
  }
}

const onTouchEnd = () => {
  if (props.pressedEffect) {
    setTimeout(() => {
      isPressed.value = false
    }, 150)
  }
}
</script>

<style scoped>
.glass-card {
  position: relative;
  background: rgba(255, 255, 255, var(--glass-opacity, 0.75));
  border-radius: var(--glass-radius, 32rpx);
  backdrop-filter: blur(var(--glass-blur, 20px));
  -webkit-backdrop-filter: blur(var(--glass-blur, 20px));
  border: 1rpx solid rgba(255, 255, 255, 0.5);
  box-shadow:
    0 8rpx 32rpx rgba(31, 38, 135, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.6);
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card__highlight {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1rpx;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.8) 50%,
    transparent 100%
  );
  pointer-events: none;
  z-index: 10;
}

.glass-card__wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120rpx;
  overflow: hidden;
  line-height: 0;
  pointer-events: none;
}

.glass-card__wave svg {
  position: relative;
  display: block;
  width: calc(100% + 1.3px);
  height: 120rpx;
}

.wave-path-primary {
  animation: waveMove 12s ease-in-out infinite;
}

.wave-path-secondary {
  animation: waveMove 10s ease-in-out infinite reverse;
  opacity: 0.7;
}

@keyframes waveMove {
  0%, 100% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(-25rpx);
  }
}

.glass-card__content {
  position: relative;
  z-index: 5;
  padding: 32rpx;
}

/* 尺寸变体 */
.glass-card--small .glass-card__content {
  padding: 24rpx;
}

.glass-card--small {
  border-radius: 24rpx;
}

.glass-card--large .glass-card__content {
  padding: 40rpx;
}

.glass-card--large {
  border-radius: 40rpx;
}

/* Hover 效果 */
.glass-card--hover:hover {
  box-shadow:
    0 16rpx 48rpx rgba(31, 38, 135, 0.2),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.7);
  transform: translateY(-6rpx);
  border-color: rgba(255, 255, 255, 0.65);
}

/* 按压效果 */
.glass-card--pressed:active {
  transform: translateY(0) scale(0.98);
  box-shadow:
    0 4rpx 16rpx rgba(31, 38, 135, 0.15),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.5);
  transition-duration: 0.1s;
}

/* 暗色主题适配 */
page.dark .glass-card {
  background: rgba(30, 30, 45, var(--glass-opacity, 0.75));
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 8rpx 32rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-card__highlight {
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.15) 50%,
    transparent 100%
  );
}

page.dark .glass-card--hover:hover {
  border-color: rgba(255, 255, 255, 0.15);
}
</style>