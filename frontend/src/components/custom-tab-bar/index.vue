<template>
  <view v-if="!hidden" class="tab-bar-wrapper">
    <view class="tab-bar">
      <view
        v-for="(item, index) in tabList"
        :key="index"
        class="tab-item"
        :class="{ active: selected === index }"
        @tap="switchTab(item, index)"
      >
        <view class="tab-icon-wrap">
          <view :class="['tabbar-icon-css', item.iconClass]"></view>
        </view>
        <text class="tab-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onShow } from '@dcloudio/uni-app'

const tabList = [
  { pagePath: '/pages/home/index', text: '首页', iconClass: 'icon-home' },
  { pagePath: '/pages/dashboard/index', text: '健康', iconClass: 'icon-health' },
  { pagePath: '/pages/checkin/index', text: '打卡', iconClass: 'icon-checkin' },
  { pagePath: '/pages/me/index', text: '我的', iconClass: 'icon-me' }
]

const selected = ref(0)
const hidden = ref(false)

function updateSelected() {
  const pages = getCurrentPages()
  if (!pages.length) return
  const currentPage = pages[pages.length - 1]
  if (!currentPage) return
  const route = currentPage.route || currentPage.__route__ || ''
  const index = tabList.findIndex(item => '/' + route === item.pagePath || route === item.pagePath)
  if (index !== -1) {
    selected.value = index
  }
}

function switchTab(item, index) {
  if (selected.value === index) return
  selected.value = index
  uni.switchTab({ url: item.pagePath })
}

onMounted(() => {
  updateSelected()
})

onShow(() => {
  updateSelected()
})
</script>

<style scoped>
.tab-bar-wrapper {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  padding-bottom: env(safe-area-inset-bottom);
  background: linear-gradient(to top, rgba(255, 255, 255, 0.7) 0%, rgba(255, 255, 255, 0.3) 50%, rgba(255, 255, 255, 0) 100%);
  padding-top: 50rpx;
}

.tab-bar {
  margin: 0 24rpx 18rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.35);
  border-radius: 60rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  padding: 0 16rpx;
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #b0b8c9;
  transition: color 0.25s ease;
}

.tab-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.25s ease;
}

.tab-item.active .tab-icon-wrap {
  background: rgba(255, 106, 61, 0.1);
}

.tabbar-icon-css {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-text {
  font-size: 22rpx;
  margin-top: 4rpx;
  font-weight: 500;
  color: inherit;
  transition: color 0.25s ease;
}

.tab-item.active {
  color: #ff6a3d;
}

.tab-item.active .tab-text {
  font-weight: 600;
}

.icon-home {
  width: 28rpx;
  height: 24rpx;
  border: 3rpx solid currentColor;
  border-radius: 4rpx 4rpx 0 0;
  position: relative;
}
.icon-home::before {
  content: '';
  position: absolute;
  top: -14rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 20rpx solid transparent;
  border-right: 20rpx solid transparent;
  border-bottom: 14rpx solid currentColor;
}
.icon-home::after {
  content: '';
  position: absolute;
  bottom: -3rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 10rpx;
  height: 12rpx;
  border: 3rpx solid currentColor;
  border-top: none;
  border-radius: 0 0 2rpx 2rpx;
}

.icon-health {
  width: 26rpx;
  height: 24rpx;
  position: relative;
}
.icon-health::before {
  content: '';
  position: absolute;
  top: 0;
  left: 2rpx;
  width: 14rpx;
  height: 14rpx;
  border: 3rpx solid currentColor;
  border-radius: 50% 50% 0 0;
  transform: rotate(-45deg);
  transform-origin: right bottom;
}
.icon-health::after {
  content: '';
  position: absolute;
  top: 0;
  right: 2rpx;
  width: 14rpx;
  height: 14rpx;
  border: 3rpx solid currentColor;
  border-radius: 50% 50% 0 0;
  transform: rotate(45deg);
  transform-origin: left bottom;
}

.icon-checkin {
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid currentColor;
  border-radius: 50%;
  position: relative;
}
.icon-checkin::after {
  content: '';
  position: absolute;
  top: 4rpx;
  left: 8rpx;
  width: 10rpx;
  height: 6rpx;
  border-left: 3rpx solid currentColor;
  border-bottom: 3rpx solid currentColor;
  transform: rotate(-45deg);
}

.icon-me {
  width: 28rpx;
  height: 28rpx;
  position: relative;
}
.icon-me::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 14rpx;
  height: 14rpx;
  border: 3rpx solid currentColor;
  border-radius: 50%;
}
.icon-me::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 22rpx;
  height: 10rpx;
  border: 3rpx solid currentColor;
  border-radius: 10rpx 10rpx 0 0;
}
</style>
