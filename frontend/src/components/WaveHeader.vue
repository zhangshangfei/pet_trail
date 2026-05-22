<template>
  <view class="wave-header" :class="[`wave-header--${variant}`, customClass]">
    <!-- 背景渐变 -->
    <view class="wave-header__bg"></view>

    <!-- 动态装饰元素 -->
    <view class="wave-header__orbs">
      <view class="orb orb--1"></view>
      <view class="orb orb--2"></view>
      <view class="orb orb--3"></view>
    </view>

    <!-- 波浪层 -->
    <view class="wave-header__waves">
      <!-- 第一层波浪 -->
      <svg
        class="wave wave--1"
        viewBox="0 0 1440 120"
        preserveAspectRatio="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          :fill="waveColors.primary"
          d="M0,64 C288,120 576,20 864,64 C1152,108 1344,40 1440,64 L1440,120 L0,120 Z"
        />
      </svg>

      <!-- 第二层波浪 -->
      <svg
        class="wave wave--2"
        viewBox="0 0 1440 120"
        preserveAspectRatio="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          :fill="waveColors.secondary"
          d="M0,80 C360,130 720,30 1080,80 C1260,105 1380,60 1440,80 L1440,120 L0,120 Z"
        />
      </svg>

      <!-- 第三层波浪（最前面） -->
      <svg
        class="wave wave--3"
        viewBox="0 0 1440 100"
        preserveAspectRatio="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          :fill="waveColors.tertiary"
          d="M0,50 C240,90 480,10 720,50 C960,90 1200,30 1440,50 L1440,100 L0,100 Z"
        />
      </svg>
    </view>

    <!-- 内容插槽 -->
    <view class="wave-header__content">
      <slot></slot>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'gradient',
    validator: (value) => ['gradient', 'solid', 'minimal'].includes(value)
  },
  height: {
    type: String,
    default: '400rpx'
  },
  primaryColor: {
    type: String,
    default: ''
  },
  secondaryColor: {
    type: String,
    default: ''
  },
  customClass: {
    type: String,
    default: ''
  }
})

const waveColors = computed(() => ({
  primary: props.primaryColor || 'rgba(255, 107, 107, 0.15)',
  secondary: props.secondaryColor || 'rgba(255, 160, 122, 0.1)',
  tertiary: 'rgba(255, 220, 180, 0.08)'
}))
</script>

<style scoped>
.wave-header {
  position: relative;
  height: v-bind(height);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 背景渐变 */
.wave-header__bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.wave-header--gradient .wave-header__bg {
  background: linear-gradient(
    135deg,
    rgba(255, 122, 61, 1) 0%,
    rgba(255, 90, 61, 0.95) 25%,
    rgba(255, 77, 79, 0.85) 50%,
    rgba(255, 107, 107, 0.7) 75%,
    rgba(255, 160, 122, 0.6) 100%
  );
}

.wave-header--solid .wave-header__bg {
  background: linear-gradient(
    180deg,
    var(--pt-primary) 0%,
    var(--pt-primary-2) 100%
  );
}

.wave-header--minimal .wave-header__bg {
  background: linear-gradient(
    135deg,
    rgba(255, 122, 61, 0.9) 0%,
    rgba(255, 160, 122, 0.8) 100%
  );
}

/* 动态光球 */
.wave-header__orbs {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.6;
}

.orb--1 {
  width: 300rpx;
  height: 300rpx;
  background: rgba(255, 255, 255, 0.3);
  top: -100rpx;
  right: 100rpx;
  animation: floatOrb1 8s ease-in-out infinite;
}

.orb--2 {
  width: 200rpx;
  height: 200rpx;
  background: rgba(255, 220, 180, 0.4);
  bottom: 50rpx;
  left: -50rpx;
  animation: floatOrb2 10s ease-in-out infinite;
}

.orb--3 {
  width: 150rpx;
  height: 150rpx;
  background: rgba(255, 255, 255, 0.2);
  top: 50%;
  left: 50%;
  animation: floatOrb3 12s ease-in-out infinite;
}

@keyframes floatOrb1 {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(-30rpx, 20rpx); }
  66% { transform: translate(20rpx, -15rpx); }
}

@keyframes floatOrb2 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(40rpx, -25rpx); }
}

@keyframes floatOrb3 {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.2); }
}

/* 波浪容器 */
.wave-header__waves {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 140rpx;
  z-index: 3;
  line-height: 0;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
}

.wave--1 {
  height: 120rpx;
  animation: waveAnim1 12s ease-in-out infinite;
}

.wave--2 {
  height: 110rpx;
  animation: waveAnim2 10s ease-in-out infinite reverse;
  opacity: 0.8;
}

.wave--3 {
  height: 95rpx;
  animation: waveAnim3 14s ease-in-out infinite;
  opacity: 0.6;
}

@keyframes waveAnim1 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-30rpx); }
}

@keyframes waveAnim2 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(25rpx); }
}

@keyframes waveAnim3 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-20rpx); }
}

/* 内容区域 */
.wave-header__content {
  position: relative;
  z-index: 10;
  padding: 48rpx;
  color: #ffffff;
  text-align: center;
}
</style>