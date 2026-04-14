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
        <view class="tab-icon">
          <text>{{ item.icon }}</text>
        </view>
        <text class="tab-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onShow } from '@dcloudio/uni-app'

const tabList = [
  { pagePath: '/pages/home/index', text: '首页', icon: '⌂' },
  { pagePath: '/pages/dashboard/index', text: '健康', icon: '♥' },
  { pagePath: '/pages/checkin/index', text: '打卡', icon: '✔' },
  { pagePath: '/pages/me/index', text: '我的', icon: '♀' }
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
}

.tab-bar {
  display: flex;
  align-items: center;
  height: 100rpx;
  background-color: #ffffff;
  border-top: 1rpx solid rgba(0, 0, 0, 0.1);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.tab-icon {
  font-size: 40rpx;
  line-height: 1;
  color: #999999;
  transition: all 0.2s;
}

.tab-text {
  font-size: 22rpx;
  margin-top: 6rpx;
  color: #999999;
  transition: all 0.2s;
}

.tab-item.active .tab-icon,
.tab-item.active .tab-text {
  color: #ff6a3d;
}
</style>
