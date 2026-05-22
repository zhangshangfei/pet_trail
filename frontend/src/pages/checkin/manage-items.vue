<template>
  <view class="manage-page">
    <!-- 导航栏 -->
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">管理打卡项</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: navHeight + 'px', paddingBottom: '140rpx' }">
      <view class="page-content">

        <!-- 默认打卡项区块 -->
        <view class="section-card">
          <view class="section-header">
            <view class="header-left">
              <text class="section-title">默认打卡项</text>
              <view class="count-badge">
                <text class="count-text">{{ defaultItems.length }}</text>
              </view>
            </view>
          </view>

          <view class="item-list">
            <view
              v-for="(item, index) in defaultItems"
              :key="item.id"
              class="manage-item"
              :style="{ animationDelay: index * 60 + 'ms' }"
            >
              <!-- 左侧装饰条 -->
              <view class="item-accent"></view>

              <!-- 主要内容区 -->
              <view class="item-body">
                <view class="item-left">
                  <view class="icon-wrap">
                    <text class="item-icon">{{ item.emoji || '📋' }}</text>
                  </view>
                  <view class="item-info">
                    <text class="item-name">{{ item.label }}</text>
                    <view class="status-badge" :class="{ active: !item.hidden }">
                      <text class="status-dot"></text>
                      <text class="status-label">{{ item.hidden ? '已隐藏' : '显示中' }}</text>
                    </view>
                  </view>
                </view>
              </view>

              <!-- 操作区域 -->
              <view class="item-actions">
                <switch
                  :checked="!item.hidden"
                  @change="onToggleItem(item)"
                  color="#ff6b35"
                  style="transform: scale(0.9);"
                />
              </view>
            </view>
          </view>
        </view>

        <!-- 自定义打卡项区块 -->
        <view class="section-card" v-if="customItems.length">
          <view class="section-header">
            <view class="header-left">
              <text class="section-title">自定义打卡项</text>
              <view class="count-badge count-badge-custom">
                <text class="count-text">{{ customItems.length }}</text>
              </view>
            </view>
          </view>

          <view class="item-list">
            <view
              v-for="(item, index) in customItems"
              :key="item.id"
              class="manage-item manage-item-custom"
              :style="{ animationDelay: index * 60 + 'ms' }"
            >
              <!-- 左侧装饰条 -->
              <view class="item-accent item-accent-custom"></view>

              <!-- 主要内容区 -->
              <view class="item-body">
                <view class="item-left">
                  <view class="icon-wrap icon-wrap-custom">
                    <text class="item-icon">{{ item.emoji || '⭐' }}</text>
                  </view>
                  <view class="item-info">
                    <text class="item-name">{{ item.label }}</text>
                    <view class="status-badge" :class="{ active: !item.hidden }">
                      <text class="status-dot"></text>
                      <text class="status-label">{{ item.hidden ? '已隐藏' : '显示中' }}</text>
                    </view>
                  </view>
                </view>
              </view>

              <!-- 操作区域 -->
              <view class="item-actions item-actions-wide">
                <switch
                  :checked="!item.hidden"
                  @change="onToggleItem(item)"
                  color="#ff6b35"
                  style="transform: scale(0.9);"
                />
                <view class="delete-btn" @tap="onDeleteItem(item)">
                  <text class="delete-icon">🗑️</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 添加自定义打卡项 -->
        <view class="add-entry" @tap="goAddItem">
          <view class="add-entry-icon-wrap">
            <text class="add-entry-icon">＋</text>
          </view>
          <text class="add-entry-text">添加自定义打卡项</text>
          <view class="add-arrow">
            <text class="arrow-icon">→</text>
          </view>
        </view>

        <!-- 提示卡片 -->
        <view class="tips-card">
          <view class="tips-header">
            <view class="tips-icon-box">
              <text class="tips-icon">💡</text>
            </view>
            <text class="tips-title">管理提示</text>
          </view>
          <view class="tips-body">
            <text class="tips-text">默认打卡项可以隐藏/显示，自定义打卡项支持删除操作。隐藏后的打卡项将不会出现在打卡列表中。</text>
          </view>
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
      navHeight: 64,
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
      this.navHeight = this.statusBarHeight + 44
    } catch (e) {
      this.navHeight = 64
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
/* ============================================
   打卡管理页面 - 专业UI设计系统 v3.0
   设计原则：精致点缀式配色、清晰层次、流畅交互
   ============================================ */

/* ====== CSS变量系统 ====== */
.manage-page {
  --color-primary: #ff6b35;
  --color-primary-light: #ff8c5a;
  --color-primary-dark: #e55a2b;
  --color-success: #10b981;
  --color-danger: #ef4444;
  --color-purple: #a855f7;
  --color-surface: #ffffff;
  --color-surface-elevated: #fafafa;
  --color-background: #f5f5f7;
  --color-text-primary: #1d1d1f;
  --color-text-secondary: #6e6e73;
  --color-text-tertiary: #aeaeb2;
  --color-border: rgba(0, 0, 0, 0.06);
  --color-border-strong: rgba(0, 0, 0, 0.12);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.03);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.08), 0 4px 8px rgba(0, 0, 0, 0.04);
  --radius-sm: 12rpx;
  --radius-md: 20rpx;
  --radius-lg: 28rpx;
  --radius-xl: 36rpx;
  --transition-fast: 150ms cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 250ms cubic-bezier(0.4, 0, 0.2, 1);
  --transition-spring: 400ms cubic-bezier(0.34, 1.56, 0.64, 1);

  min-height: 100vh;
  background: var(--color-background);
}

/* ====== 导航栏 ====== */
.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
}

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.nav-back {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);

  &:active {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(0.92);
  }
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg);
  margin-left: 4rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5rpx;
}

.nav-placeholder {
  width: 72rpx;
}

/* ====== 滚动容器 ====== */
.page-scroll {
  height: 100vh;
}

.page-content {
  padding: 28rpx 28rpx;
}

/* ====== 区块卡片 ====== */
.section-card {
  background: linear-gradient(135deg, #fff9f5 0%, #fffaf7 50%, #fef9f5 100%);
  border-radius: var(--radius-xl);
  margin-bottom: 28rpx;
  overflow: hidden;
  box-shadow:
    var(--shadow-md),
    0 4rpx 20rpx rgba(255, 107, 53, 0.05);
  border: 1.5rpx solid rgba(255, 107, 53, 0.1);
  animation: slideUp 0.5s var(--transition-spring) both;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 28rpx 20rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--color-text-primary);
}

.count-badge {
  min-width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.2);
}

.count-badge-custom {
  background: linear-gradient(135deg, var(--color-purple) 0%, #c084fc 100%);
  box-shadow: 0 2rpx 8rpx rgba(168, 85, 247, 0.2);
}

.count-text {
  font-size: 23rpx;
  font-weight: 700;
  color: #fff;
}

/* ====== 打卡项列表 ====== */
.item-list {
  padding: 8rpx 0;
}

.manage-item {
  display: flex;
  align-items: center;
  position: relative;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0.6) 100%);
  margin: 8rpx 16rpx;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1rpx solid rgba(255, 107, 53, 0.08);
  animation: slideInUp 0.45s var(--transition-spring) both;

  &:active {
    transform: scale(0.98);
  }

  &:last-child {
    margin-bottom: 16rpx;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.item-accent {
  width: 6rpx;
  background: linear-gradient(180deg,
    var(--color-primary) 0%,
    var(--color-primary-light) 50%,
    var(--color-primary) 100%
  );
  box-shadow: 2rpx 0 12rpx rgba(255, 107, 53, 0.25);
  flex-shrink: 0;
}

.item-accent-custom {
  background: linear-gradient(180deg,
    var(--color-purple) 0%,
    #c084fc 50%,
    var(--color-purple) 100%
  );
  box-shadow: 2rpx 0 12rpx rgba(168, 85, 247, 0.25);
}

.item-body {
  flex: 1;
  padding: 20rpx 16rpx;
  min-width: 0;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.12) 0%, rgba(255, 107, 53, 0.06) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-wrap-custom {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.12) 0%, rgba(168, 85, 247, 0.06) 100%);
}

.item-icon {
  font-size: 30rpx;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  display: block;
  font-size: 28rpx;
  color: var(--color-text-primary);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 6rpx;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: var(--color-surface-elevated);
  border: 1rpx solid var(--color-border);
  width: fit-content;
  transition: all var(--transition-normal);

  &.active {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.05) 100%);
    border-color: rgba(16, 185, 129, 0.2);
  }

  &:not(.active) {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.08) 0%, rgba(239, 68, 68, 0.04) 100%);
    border-color: rgba(239, 68, 68, 0.15);
  }

  &.active .status-dot {
    background: #10b981;
    box-shadow: 0 0 8rpx rgba(16, 185, 129, 0.4);
  }

  &:not(.active) .status-dot {
    background: #ef4444;
    box-shadow: 0 0 8rpx rgba(239, 68, 68, 0.3);
  }

  &.active .status-label {
    color: #10b981;
    font-weight: 700;
  }

  &:not(.active) .status-label {
    color: #ef4444;
    font-weight: 700;
  }
}

.status-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  transition: all var(--transition-fast);
}

.status-label {
  font-size: 20rpx;
  transition: all var(--transition-fast);
}

.item-actions {
  display: flex;
  align-items: center;
  padding: 16rpx;
  flex-shrink: 0;
}

.item-actions-wide {
  gap: 12rpx;
  padding: 16rpx;
  border-left: 1rpx solid var(--color-border);
}

.delete-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.08) 0%, rgba(239, 68, 68, 0.04) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);

  &:active {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.15) 0%, rgba(239, 68, 68, 0.08) 100%);
    transform: scale(0.9);
  }
}

.delete-icon {
  font-size: 26rpx;
}

/* ====== 添加入口 ====== */
.add-entry {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 32rpx 28rpx;
  background: linear-gradient(135deg, #fffaf7 0%, #ffffff 100%);
  border-radius: var(--radius-xl);
  margin-bottom: 28rpx;
  border: 2.5rpx dashed rgba(255, 107, 53, 0.25);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  animation: slideUp 0.5s var(--transition-spring) 0.2s both;

  &:active {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.04) 0%, rgba(255, 107, 53, 0.02) 100%);
    border-color: rgba(255, 107, 53, 0.4);
    transform: scale(0.98);
  }
}

.add-entry-icon-wrap {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.12) 0%, rgba(255, 107, 53, 0.06) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-entry-icon {
  font-size: 36rpx;
  color: var(--color-primary);
  font-weight: 700;
}

.add-entry-text {
  font-size: 28rpx;
  color: var(--color-primary);
  font-weight: 600;
}

.add-arrow {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 107, 53, 0.04) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  font-size: 26rpx;
  color: var(--color-primary);
  font-weight: 700;
}

/* ====== 提示卡片 ====== */
.tips-card {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-radius: var(--radius-lg);
  padding: 28rpx;
  border: 1rpx solid rgba(245, 158, 11, 0.15);
  box-shadow: var(--shadow-sm);
  animation: slideUp 0.5s var(--transition-spring) 0.3s both;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 14rpx;
}

.tips-icon-box {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.2);
  flex-shrink: 0;
}

.tips-icon {
  font-size: 26rpx;
}

.tips-title {
  font-size: 27rpx;
  font-weight: 700;
  color: #92400e;
}

.tips-body {
  padding-left: 62rpx;
}

.tips-text {
  font-size: 24rpx;
  color: #a16207;
  line-height: 1.7;
  font-weight: 500;
}

/* ====== 底部安全区域 ====== */
.page-bottom-safe {
  height: calc(40rpx + env(safe-area-inset-bottom));
}

/* ====== 全局动画 ====== */
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

/* ====== 暗色模式 ====== */
page.dark .manage-page {
  --color-surface: #1c1c1e;
  --color-surface-elevated: #2c2c2e;
  --color-background: #000000;
  --color-text-primary: #f5f5f7;
  --color-text-secondary: #98989d;
  --color-text-tertiary: #636366;
  --color-border: rgba(255, 255, 255, 0.08);
  --color-border-strong: rgba(255, 255, 255, 0.14);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.2), 0 1px 2px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.3), 0 2px 4px rgba(0, 0, 0, 0.15);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.4), 0 4px 8px rgba(0, 0, 0, 0.2);
  --color-primary: #ff863a;
  --color-primary-light: #ffa066;
  --color-primary-dark: #e66a2a;
}

page.dark .nav-fixed {
  background: linear-gradient(135deg, #2a2a2c 0%, #1c1c1e 100%);
}

page.dark .nav-back {
  background: rgba(255, 255, 255, 0.1);

  &:active {
    background: rgba(255, 255, 255, 0.18);
  }
}

page.dark .section-card {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.04) 0%, rgba(255, 134, 58, 0.02) 100%);
  border-color: rgba(255, 134, 58, 0.1);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.3),
    0 4rpx 20rpx rgba(255, 134, 58, 0.03);
}

page.dark .manage-item {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.03) 100%);
  border-color: rgba(255, 134, 58, 0.08);
}

page.dark .item-accent {
  box-shadow: 2rpx 0 12rpx rgba(255, 134, 58, 0.35);
}

page.dark .item-accent-custom {
  box-shadow: 2rpx 0 12rpx rgba(168, 85, 247, 0.35);
}

page.dark .icon-wrap {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
}

page.dark .icon-wrap-custom {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.15) 0%, rgba(168, 85, 247, 0.08) 100%);
}

page.dark .status-badge {
  background: #2c2c2e;
  border-color: var(--color-border);

  &.active {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(16, 185, 129, 0.08) 100%);
    border-color: rgba(16, 185, 129, 0.25);
  }

  &:not(.active) {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(239, 68, 68, 0.06) 100%);
    border-color: rgba(239, 68, 68, 0.2);
  }
}

page.dark .item-actions-wide {
  border-left-color: var(--color-border);
}

page.dark .delete-btn {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(239, 68, 68, 0.06) 100%);
}

page.dark .add-entry {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.04) 0%, var(--color-surface) 100%);
  border-color: rgba(255, 134, 58, 0.2);

  &:active {
    background: linear-gradient(135deg, rgba(255, 134, 58, 0.08) 0%, rgba(255, 134, 58, 0.04) 100%);
    border-color: rgba(255, 134, 58, 0.35);
  }
}

page.dark .add-entry-icon-wrap {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
}

page.dark .add-arrow {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.12) 0%, rgba(255, 134, 58, 0.06) 100%);
}

page.dark .tips-card {
  background: linear-gradient(135deg, rgba(255, 200, 80, 0.08) 0%, rgba(255, 180, 60, 0.04) 100%);
  border-color: rgba(245, 158, 11, 0.2);
}

page.dark .tips-icon-box {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.3) 0%, rgba(245, 158, 11, 0.2) 100%);
  box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.15);
}
</style>
