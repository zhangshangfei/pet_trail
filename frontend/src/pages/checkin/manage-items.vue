<template>
  <view class="manage-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">管理打卡项</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">

        <view class="section-card">
          <view class="section-header">
            <text class="section-title">默认打卡项</text>
            <text class="section-count">{{ defaultItems.length }}项</text>
          </view>
          <view class="item-list">
            <view v-for="item in defaultItems" :key="item.id" class="manage-item">
              <view class="item-left">
                <text class="item-icon">{{ item.emoji || '📋' }}</text>
                <view class="item-info">
                  <text class="item-name">{{ item.label }}</text>
                  <text :class="['item-status', item.hidden ? 'status-hidden' : 'status-visible']">
                    {{ item.hidden ? '已隐藏' : '显示中' }}
                  </text>
                </view>
              </view>
              <switch :checked="!item.hidden" @change="onToggleItem(item)" color="#ff6a3d" />
            </view>
          </view>
        </view>

        <view class="section-card" v-if="customItems.length">
          <view class="section-header">
            <text class="section-title">自定义打卡项</text>
            <text class="section-count">{{ customItems.length }}项</text>
          </view>
          <view class="item-list">
            <view v-for="item in customItems" :key="item.id" class="manage-item">
              <view class="item-left">
                <text class="item-icon">{{ item.emoji || '📋' }}</text>
                <view class="item-info">
                  <text class="item-name">{{ item.label }}</text>
                  <text :class="['item-status', item.hidden ? 'status-hidden' : 'status-visible']">
                    {{ item.hidden ? '已隐藏' : '显示中' }}
                  </text>
                </view>
              </view>
              <view class="item-right">
                <switch :checked="!item.hidden" @change="onToggleItem(item)" color="#ff6a3d" />
                <view class="delete-btn" @tap="onDeleteItem(item)">
                  <text class="delete-text">删除</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="add-entry" @tap="goAddItem">
          <text class="add-entry-icon">＋</text>
          <text class="add-entry-text">添加自定义打卡项</text>
        </view>

        <view class="page-bottom-safe"></view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'
import * as checkinApi from '@/api/checkin'
import { deleteCheckinItem, hideCheckinItem, showCheckinItem } from '@/api/checkin'

export default {
  data() {
    return {
      statusBarHeight: 20,
      allItems: []
    }
  },
  computed: {
    defaultItems() {
      return this.allItems.filter(i => i.isDefault)
    },
    customItems() {
      return this.allItems.filter(i => i.isCustom)
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
  },
  onShow() {
    this.loadItems()
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    async loadItems() {
      try {
        const res = await checkinApi.getCheckinItems()
        if (res && res.success && Array.isArray(res.data)) {
          this.allItems = res.data.map(item => ({
            id: item.id,
            name: item.name,
            label: item.name,
            emoji: item.icon || '📋',
            isCustom: item.isDefault === 0,
            isDefault: item.isDefault === 1,
            hidden: item.hidden === true
          }))
        }
      } catch (e) {
        console.error('加载打卡项失败:', e)
      }
    },
    async onToggleItem(item) {
      const loggedIn = await checkLogin('请先登录')
      if (!loggedIn) return
      try {
        if (item.hidden) {
          const res = await showCheckinItem(item.id)
          if (res && res.success) {
            item.hidden = false
            uni.showToast({ title: '已显示', icon: 'success' })
          }
        } else {
          const res = await hideCheckinItem(item.id)
          if (res && res.success) {
            item.hidden = true
            uni.showToast({ title: '已隐藏', icon: 'success' })
          }
        }
      } catch (e) {
        console.error('操作失败:', e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    onDeleteItem(item) {
      const self = this
      uni.showModal({
        title: '删除打卡项',
        content: `确定删除"${item.label}"吗？`,
        confirmColor: '#ff6a3d',
        async success(res) {
          if (!res.confirm) return
          try {
            const result = await deleteCheckinItem(item.id)
            if (result && result.success) {
              self.allItems = self.allItems.filter(i => i.id !== item.id)
              uni.showToast({ title: '已删除', icon: 'success' })
            }
          } catch (e) {
            uni.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      })
    },
    goAddItem() {
      uni.navigateTo({ url: '/pages/checkin/add-item' })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$primary-light: #fff0ea;
$green: #52c41a;
$green-light: #f0fff0;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.manage-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.section-card {
  background: $card-bg; border-radius: 24rpx; margin-bottom: 20rpx; overflow: hidden;
}
.section-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24rpx 28rpx 0;
}
.section-title { font-size: 28rpx; font-weight: 700; color: $text-primary; }
.section-count { font-size: 24rpx; color: $text-light; }

.item-list { padding: 8rpx 0; }
.manage-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24rpx 28rpx; border-bottom: 1rpx solid #f8f8f8;
}
.manage-item:last-child { border-bottom: none; }

.item-left { display: flex; align-items: center; flex: 1; min-width: 0; }
.item-icon { font-size: 36rpx; margin-right: 16rpx; flex-shrink: 0; }
.item-info { flex: 1; min-width: 0; }
.item-name { display: block; font-size: 28rpx; color: $text-primary; font-weight: 500; }
.item-status { display: block; font-size: 22rpx; margin-top: 4rpx; }
.status-visible { color: $green; }
.status-hidden { color: $text-light; }

.item-right { display: flex; align-items: center; gap: 16rpx; flex-shrink: 0; }
.delete-btn { padding: 8rpx 20rpx; border-radius: 20rpx; background: #fef2f2; }
.delete-text { font-size: 24rpx; color: #ef4444; }

.add-entry {
  display: flex; align-items: center; justify-content: center;
  padding: 32rpx; background: $card-bg; border-radius: 24rpx;
  margin-bottom: 20rpx; border: 2rpx dashed #e0e0e0;
}
.add-entry:active { background: #fafafa; }
.add-entry-icon { font-size: 36rpx; color: $primary; margin-right: 12rpx; font-weight: 600; }
.add-entry-text { font-size: 28rpx; color: $primary; font-weight: 500; }

.page-bottom-safe { height: calc(40rpx + env(safe-area-inset-bottom)); }
</style>
