<template>
  <view class="simple-page">
    <view class="simple-card">
      <text class="simple-title">社区</text>
      <text class="simple-desc">这里后续接入动态/话题内容。</text>
    </view>
  </view>
</template>

<script>
export default {
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
  }
};
</script>

<style lang="scss" scoped>
.simple-page {
  min-height: 100vh;
  background: var(--pt-bg);
  padding: 160rpx 20rpx 220rpx;
}

.simple-card {
  background: var(--pt-card);
  border-radius: 26rpx;
  box-shadow: var(--pt-shadow-soft);
  padding: 40rpx 24rpx;
}

.simple-title {
  font-size: 36rpx;
  font-weight: 900;
  color: #111827;
  display: block;
}

.simple-desc {
  font-size: 26rpx;
  color: var(--pt-muted);
  margin-top: 18rpx;
  display: block;
  line-height: 38rpx;
}
</style>

