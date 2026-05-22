<template>
  <view class="reminder-page">
    <!-- 导航栏 -->
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">打卡提醒</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: navHeight + 'px', paddingBottom: '140rpx' }">
      <view class="page-content">

        <!-- 区块标题+添加按钮 -->
        <view class="section-header">
          <view class="header-left">
            <text class="section-title">提醒列表</text>
            <view class="count-badge">
              <text class="count-text">{{ reminders.length }}</text>
            </view>
          </view>
          <view class="add-btn" @click="openAddModal">
            <text class="add-icon">+</text>
            <text class="add-text">添加</text>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading-card">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 提醒列表 -->
        <view v-else-if="reminders.length > 0" class="reminder-list">
          <view
            v-for="(item, index) in reminders"
            :key="item.id"
            class="reminder-card"
            :style="{ animationDelay: index * 60 + 'ms' }"
          >
            <!-- 左侧装饰条 -->
            <view class="card-accent"></view>

            <!-- 主要内容区 -->
            <view class="card-body">
              <!-- 时间显示 -->
              <view class="time-section">
                <view class="time-icon-wrap">
                  <text class="time-icon">⏰</text>
                </view>
                <text class="time-value">{{ formatTime(item.remindTime) }}</text>
              </view>

              <!-- 目标信息 -->
              <view class="target-info">
                <text class="target-name">{{ getItemDisplayName(item) }}</text>
                <view class="status-badge" :class="{ active: item.isEnabled }">
                  <text class="status-dot"></text>
                  <text class="status-label">{{ item.isEnabled ? '已启用' : '已停用' }}</text>
                </view>
              </view>
            </view>

            <!-- 操作区域 -->
            <view class="card-actions">
              <switch
                :checked="item.isEnabled"
                color="#ff6b35"
                style="transform: scale(0.9);"
                @change="onToggleReminder(item, $event)"
              />
              <view class="delete-btn" @click="onDeleteReminder(item)">
                <text class="delete-icon">🗑️</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <view class="empty-illustration">
            <view class="empty-circle">
              <text class="empty-emoji">⏰</text>
            </view>
            <view class="empty-ring"></view>
          </view>
          <text class="empty-title">暂无打卡提醒</text>
          <text class="empty-desc">点击右上角添加提醒，养成打卡好习惯</text>
          <view class="empty-action" @click="openAddModal">
            <text class="action-text">立即添加</text>
            <text class="action-arrow">→</text>
          </view>
        </view>

        <!-- 提示卡片 -->
        <view class="tips-card">
          <view class="tips-header">
            <view class="tips-icon-box">
              <text class="tips-icon">💡</text>
            </view>
            <text class="tips-title">温馨提示</text>
          </view>
          <view class="tips-body">
            <text class="tips-text">打卡提醒通过系统通知推送，请确保已开启通知权限以正常接收提醒。</text>
          </view>
        </view>

      </view>
    </scroll-view>

    <!-- 添加弹窗 -->
    <view v-if="showAddModal" class="modal-mask" @click="showAddModal = false">
      <view class="modal-container" @click.stop>
        <!-- 弹窗头部 -->
        <view class="modal-header">
          <text class="modal-title">添加打卡提醒</text>
          <view class="modal-close" @click="showAddModal = false">
            <text class="close-icon">✕</text>
          </view>
        </view>

        <!-- 弹窗主体 -->
        <scroll-view scroll-y class="modal-scroll">
          <view class="modal-body">

            <!-- 时间选择 -->
            <view class="form-section">
              <view class="section-label">
                <text class="label-icon">🕐</text>
                <text class="label-text">提醒时间</text>
              </view>
              <picker mode="time" :value="newRemindTime" @change="onTimeChange">
                <view class="time-picker">
                  <text class="time-picker-value">{{ newRemindTime }}</text>
                  <text class="time-picker-hint">点击选择时间</text>
                  <view class="picker-arrow">
                    <text class="arrow-icon">›</text>
                  </view>
                </view>
              </picker>
            </view>

            <!-- 打卡项选择 -->
            <view class="form-section">
              <view class="section-label">
                <text class="label-icon">📋</text>
                <text class="label-text">选择打卡项</text>
              </view>
              <view class="item-selector">

                <!-- 全部选项 -->
                <view
                  class="item-option"
                  :class="{ active: selectedItemId === null }"
                  @click="selectedItemId = null"
                >
                  <view class="option-left">
                    <view class="option-icon-box option-icon-all">
                      <text class="option-icon">🐾</text>
                    </view>
                    <view class="option-info">
                      <text class="option-name">全部打卡项</text>
                      <text class="option-desc">每次都提醒</text>
                    </view>
                  </view>
                  <view class="option-check" :class="{ show: selectedItemId === null }">
                    <text class="check-mark">✓</text>
                  </view>
                </view>

                <!-- 默认打卡项组 -->
                <view v-if="defaultItems.length > 0" class="item-group">
                  <view class="group-divider">
                    <view class="divider-line"></view>
                    <text class="group-label">默认打卡项</text>
                    <view class="divider-line"></view>
                  </view>
                  <view
                    v-for="item in defaultItems"
                    :key="'d-' + item.id"
                    class="item-option"
                    :class="{ active: selectedItemId === item.id }"
                    @click="selectedItemId = item.id"
                  >
                    <view class="option-left">
                      <view class="option-icon-box">
                        <text class="option-icon">{{ item.icon || '📋' }}</text>
                      </view>
                      <view class="option-info">
                        <text class="option-name">{{ item.name }}</text>
                        <text class="option-desc">系统预设</text>
                      </view>
                    </view>
                    <view class="option-check" :class="{ show: selectedItemId === item.id }">
                      <text class="check-mark">✓</text>
                    </view>
                  </view>
                </view>

                <!-- 自定义打卡项组 -->
                <view v-if="customItems.length > 0" class="item-group">
                  <view class="group-divider">
                    <view class="divider-line"></view>
                    <text class="group-label">自定义打卡项</text>
                    <view class="divider-line"></view>
                  </view>
                  <view
                    v-for="item in customItems"
                    :key="'c-' + item.id"
                    class="item-option"
                    :class="{ active: selectedItemId === item.id }"
                    @click="selectedItemId = item.id"
                  >
                    <view class="option-left">
                      <view class="option-icon-box option-icon-custom">
                        <text class="option-icon">{{ item.icon || '⭐' }}</text>
                      </view>
                      <view class="option-info">
                        <text class="option-name">{{ item.name }}</text>
                        <text class="option-desc">自定义项目</text>
                      </view>
                    </view>
                    <view class="option-check" :class="{ show: selectedItemId === item.id }">
                      <text class="check-mark">✓</text>
                    </view>
                  </view>
                </view>

              </view>
            </view>

          </view>
        </scroll-view>

        <!-- 弹窗底部按钮 -->
        <view class="modal-footer">
          <view class="footer-btn cancel-btn" @click="showAddModal = false">
            <text class="btn-text cancel-text">取消</text>
          </view>
          <view class="footer-btn confirm-btn" @click="onAddReminder">
            <text class="btn-text confirm-text">确定添加</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as checkinApi from '@/api/checkin'
import { loadWxSubscribeTemplates, requestWxSubscribe } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 20,
      navHeight: 64,
      reminders: [],
      loading: false,
      showAddModal: false,
      newRemindTime: '09:00',
      checkinItems: [],
      selectedItemId: null
    }
  },
  computed: {
    defaultItems() {
      return this.checkinItems.filter(item => item.isDefault === 1)
    },
    customItems() {
      return this.checkinItems.filter(item => item.isDefault !== 1)
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
    this.loadReminders()
    this.loadCheckinItems()
    loadWxSubscribeTemplates()
  },
  methods: {
    openAddModal() {
      this.selectedItemId = null
      this.showAddModal = true
    },
    getItemDisplayName(reminder) {
      if (!reminder.itemId) return '全部打卡项'
      const item = this.checkinItems.find(i => i.id === reminder.itemId)
      if (item) {
        const tag = item.isDefault === 1 ? '' : ' [自定义]'
        return (item.icon || '📋') + ' ' + item.name + tag
      }
      return '未知打卡项'
    },
    async loadReminders() {
      this.loading = true
      try {
        const res = await checkinApi.getCheckinReminders()
        if (res && res.success && Array.isArray(res.data)) {
          this.reminders = res.data
        }
      } catch (e) {
        console.error('加载提醒失败:', e)
      } finally {
        this.loading = false
      }
    },
    async onToggleReminder(item, event) {
      const enabled = event.detail.value
      try {
        await checkinApi.updateCheckinReminder(item.id, { isEnabled: enabled })
        item.isEnabled = enabled
      } catch (e) {
        console.error('更新提醒失败:', e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    async onDeleteReminder(item) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个提醒吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await checkinApi.deleteCheckinReminder(item.id)
              this.reminders = this.reminders.filter(r => r.id !== item.id)
              uni.showToast({ title: '已删除', icon: 'none' })
            } catch (e) {
              console.error('删除提醒失败:', e)
              uni.showToast({ title: '删除失败', icon: 'none' })
            }
          }
        }
      })
    },
    onTimeChange(event) {
      this.newRemindTime = event.detail.value
    },
    async loadCheckinItems() {
      try {
        const res = await checkinApi.getCheckinItems()
        if (res && res.success && Array.isArray(res.data)) {
          this.checkinItems = res.data.filter(item => item.isEnabled !== 0 && !item.hidden)
        }
      } catch (e) {
        console.error('加载打卡项失败:', e)
      }
    },
    async onAddReminder() {
      requestWxSubscribe(['checkin'])
      try {
        const params = { remindTime: this.newRemindTime }
        if (this.selectedItemId !== null) {
          params.itemId = this.selectedItemId
        }
        const res = await checkinApi.createCheckinReminder(params)
        if (res && res.success) {
          uni.showToast({ title: '添加成功', icon: 'success' })
          this.showAddModal = false
          this.selectedItemId = null
          this.loadReminders()
        }
      } catch (e) {
        console.error('添加提醒失败:', e)
        uni.showToast({ title: '添加失败', icon: 'none' })
      }
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      if (typeof timeStr === 'object' && timeStr.hour !== undefined) {
        const h = String(timeStr.hour).padStart(2, '0')
        const m = String(timeStr.minute !== undefined ? timeStr.minute : 0).padStart(2, '0')
        return h + ':' + m
      }
      return String(timeStr).substring(0, 5)
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   打卡提醒页面 - 专业UI设计系统 v2.0
   设计原则：精致点缀式配色、清晰层次、流畅交互
   ============================================ */

/* ====== CSS变量系统 ====== */
.reminder-page {
  --color-primary: #ff6b35;
  --color-primary-light: #ff8c5a;
  --color-primary-dark: #e55a2b;
  --color-success: #10b981;
  --color-danger: #ef4444;
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

/* ====== 区块标题 ====== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding: 4rpx 0;
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

.count-text {
  font-size: 23rpx;
  font-weight: 700;
  color: #fff;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 26rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.12) 0%, rgba(255, 107, 53, 0.06) 100%);
  border-radius: 999rpx;
  border: 1.5rpx solid rgba(255, 107, 53, 0.25);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.08);
  transition: all var(--transition-normal);

  &:active {
    transform: scale(0.95);
    box-shadow: 0 2rpx 6rpx rgba(255, 107, 53, 0.12);
  }
}

.add-icon {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--color-primary);
}

.add-text {
  font-size: 25rpx;
  font-weight: 700;
  color: var(--color-primary);
}

/* ====== 加载状态 ====== */
.loading-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  animation: slideUp 0.5s var(--transition-spring);
}

.loading-spinner {
  width: 64rpx;
  height: 64rpx;
  border: 4rpx solid var(--color-border-strong);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 24rpx;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 28rpx;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* ====== 提醒列表 ====== */
.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.reminder-card {
  background: linear-gradient(135deg, #fff9f5 0%, #fffaf7 50%, #fef9f5 100%);
  border-radius: var(--radius-xl);
  overflow: hidden;
  display: flex;
  box-shadow:
    var(--shadow-md),
    0 4rpx 20rpx rgba(255, 107, 53, 0.06);
  border: 1.5rpx solid rgba(255, 107, 53, 0.12);
  position: relative;
  animation: slideInUp 0.45s var(--transition-spring) both;

  &:active {
    transform: scale(0.98);
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

.card-accent {
  width: 6rpx;
  background: linear-gradient(180deg,
    var(--color-primary) 0%,
    var(--color-primary-light) 50%,
    var(--color-primary) 100%
  );
  box-shadow: 2rpx 0 12rpx rgba(255, 107, 53, 0.25);
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  padding: 24rpx 20rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.time-section {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.time-icon-wrap {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, rgba(255, 107, 53, 0.08) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.time-icon {
  font-size: 28rpx;
}

.time-value {
  font-size: 38rpx;
  font-weight: 800;
  color: var(--color-primary);
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.1);
}

.target-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.target-name {
  font-size: 27rpx;
  font-weight: 600;
  color: var(--color-text-secondary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: var(--color-surface-elevated);
  border: 1rpx solid var(--color-border);
  flex-shrink: 0;
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
    background: var(--color-success);
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
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: var(--color-text-tertiary);
  transition: all var(--transition-fast);
}

.status-label {
  font-size: 22rpx;
  color: var(--color-text-tertiary);
  font-weight: 600;
  transition: all var(--transition-fast);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 16rpx 20rpx;
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
  font-size: 28rpx;
}

/* ====== 空状态 ====== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  animation: slideUp 0.5s var(--transition-spring);
}

.empty-illustration {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 36rpx;
}

.empty-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #fff5f0 0%, #ffe8d6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow:
    0 8rpx 32rpx rgba(255, 107, 53, 0.15),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.8);
}

.empty-emoji {
  font-size: 72rpx;
}

.empty-ring {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  border: 3rpx solid transparent;
  border-top-color: rgba(255, 107, 53, 0.2);
  border-right-color: rgba(255, 107, 53, 0.1);
  animation: ringRotate 6s linear infinite;
}

@keyframes ringRotate {
  to { transform: rotate(360deg); }
}

.empty-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  text-align: center;
  line-height: 1.6;
  margin-bottom: 32rpx;
  font-weight: 500;
}

.empty-action {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 18rpx 36rpx;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-radius: 999rpx;
  box-shadow:
    0 8rpx 24rpx rgba(255, 107, 53, 0.35),
    0 4rpx 12rpx rgba(255, 107, 53, 0.2),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.2);
  transition: all var(--transition-normal);

  &:active {
    transform: scale(0.95);
    box-shadow:
      0 4rpx 12rpx rgba(255, 107, 53, 0.3),
      0 2rpx 6rpx rgba(255, 107, 53, 0.15),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.2);
  }
}

.action-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}

.action-arrow {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
}

/* ====== 提示卡片 ====== */
.tips-card {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-radius: var(--radius-lg);
  padding: 28rpx;
  margin-top: 28rpx;
  border: 1rpx solid rgba(245, 158, 11, 0.15);
  box-shadow: var(--shadow-sm);
  animation: slideUp 0.5s var(--transition-spring) 0.2s both;
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
  font-size: 25rpx;
  color: #a16207;
  line-height: 1.7;
  font-weight: 500;
}

/* ====== 弹窗遮罩 ====== */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.55);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  animation: fadeIn 0.25s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  width: 90%;
  max-width: 650rpx;
  max-height: 85vh;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow:
    0 24rpx 80rpx rgba(0, 0, 0, 0.25),
    0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.35s var(--transition-spring);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    background: linear-gradient(90deg,
      var(--color-primary) 0%,
      var(--color-primary-light) 50%,
      var(--color-primary) 100%
    );
  }
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(40rpx) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ====== 弹窗头部 ====== */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid var(--color-border);
  background: linear-gradient(135deg, #fff9f5 0%, #ffffff 100%);
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--color-text-primary);
}

.modal-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: var(--color-surface-elevated);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);

  &:active {
    background: var(--color-border-strong);
    transform: scale(0.9);
  }
}

.close-icon {
  font-size: 28rpx;
  color: var(--color-text-tertiary);
  font-weight: 700;
}

/* ====== 弹窗滚动区 ====== */
.modal-scroll {
  flex: 1;
  max-height: calc(85vh - 220rpx);
}

.modal-body {
  padding: 28rpx;
}

.form-section {
  margin-bottom: 28rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-label {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.label-icon {
  font-size: 28rpx;
}

.label-text {
  font-size: 27rpx;
  font-weight: 700;
  color: var(--color-text-primary);
}

/* ====== 时间选择器 ====== */
.time-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  background: linear-gradient(135deg, #fff9f5 0%, #ffffff 100%);
  border-radius: var(--radius-md);
  border: 1.5rpx solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);

  &:active {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
  }
}

.time-picker-value {
  font-size: 38rpx;
  font-weight: 800;
  color: var(--color-primary);
  letter-spacing: 2rpx;
}

.time-picker-hint {
  font-size: 23rpx;
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.picker-arrow {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: var(--color-surface-elevated);
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  font-size: 32rpx;
  color: var(--color-text-tertiary);
  font-weight: 700;
}

/* ====== 打卡项选择器 ====== */
.item-selector {
  background: var(--color-surface-elevated);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1rpx solid var(--color-border);
}

.item-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
  border-bottom: 1rpx solid var(--color-border);
  transition: all var(--transition-normal);
  position: relative;

  &:last-child {
    border-bottom: none;
  }

  &.active {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 107, 53, 0.04) 100%);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4rpx;
      background: var(--color-primary);
    }
  }

  &:active {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.12) 0%, rgba(255, 107, 53, 0.06) 100%);
  }
}

.option-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.option-icon-box {
  width: 56rpx;
  height: 56rpx;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.option-icon-custom {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.1) 0%, rgba(168, 85, 247, 0.05) 100%);
}

.option-icon-all {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(59, 130, 246, 0.05) 100%);
}

.option-icon {
  font-size: 30rpx;
}

.option-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.option-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-text-primary);
}

.option-desc {
  font-size: 23rpx;
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.option-check {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: var(--color-surface);
  border: 2rpx solid var(--color-border-strong);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-spring);
  opacity: 0;
  transform: scale(0.5);

  &.show {
    opacity: 1;
    transform: scale(1);
    background: var(--color-primary);
    border-color: var(--color-primary);
  }
}

.check-mark {
  color: #fff;
  font-size: 26rpx;
  font-weight: 800;
}

.item-group {
  border-top: 2rpx solid var(--color-border-strong);
}

.group-divider {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: var(--color-surface);
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background: linear-gradient(90deg, transparent, var(--color-border), transparent);
}

.group-label {
  font-size: 22rpx;
  color: var(--color-text-tertiary);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  white-space: nowrap;
}

/* ====== 弹窗底部 ====== */
.modal-footer {
  display: flex;
  border-top: 1rpx solid var(--color-border);
  background: var(--color-surface-elevated);
}

.footer-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28rpx 0;
  transition: all var(--transition-fast);

  &:active {
    background: var(--color-border);
  }
}

.cancel-btn {
  border-right: 1rpx solid var(--color-border);
}

.confirm-btn {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.05) 0%, rgba(255, 107, 53, 0.02) 100%);

  &:active {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  }
}

.btn-text {
  font-size: 30rpx;
  font-weight: 700;
}

.cancel-text {
  color: var(--color-text-secondary);
}

.confirm-text {
  color: var(--color-primary);
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
page.dark .reminder-page {
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

page.dark .loading-card {
  background: var(--color-surface);
}

page.dark .reminder-card {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.04) 0%, rgba(255, 134, 58, 0.02) 100%);
  border-color: rgba(255, 134, 58, 0.12);
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.3),
    0 4rpx 20rpx rgba(255, 134, 58, 0.03);
}

page.dark .card-accent {
  box-shadow: 2rpx 0 12rpx rgba(255, 134, 58, 0.35);
}

page.dark .time-icon-wrap {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.2) 0%, rgba(255, 134, 58, 0.1) 100%);
}

page.dark .time-value {
  text-shadow: 0 2rpx 8rpx rgba(255, 134, 58, 0.2);
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

page.dark .delete-btn {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(239, 68, 68, 0.06) 100%);
}

page.dark .empty-circle {
  background: linear-gradient(145deg, rgba(255, 134, 58, 0.08) 0%, rgba(255, 134, 58, 0.04) 100%);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.3);
}

page.dark .empty-ring {
  border-top-color: rgba(255, 134, 58, 0.25);
  border-right-color: rgba(255, 134, 58, 0.12);
}

page.dark .tips-card {
  background: linear-gradient(135deg, rgba(255, 200, 80, 0.08) 0%, rgba(255, 180, 60, 0.04) 100%);
  border-color: rgba(245, 158, 11, 0.2);
}

page.dark .tips-icon-box {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.3) 0%, rgba(245, 158, 11, 0.2) 100%);
  box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.15);
}

page.dark .modal-mask {
  background: rgba(0, 0, 0, 0.75);
}

page.dark .modal-container {
  background: var(--color-surface);
  box-shadow:
    0 24rpx 80rpx rgba(0, 0, 0, 0.6),
    0 8rpx 32rpx rgba(0, 0, 0, 0.3);
}

page.dark .modal-header {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.06) 0%, var(--color-surface) 100%);
}

page.dark .modal-close {
  background: #2c2c2e;
}

page.dark .time-picker {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.06) 0%, var(--color-surface) 100%);
  border-color: var(--color-border);

  &:active {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4rpx rgba(255, 134, 58, 0.15);
  }
}

page.dark .picker-arrow {
  background: #2c2c2e;
}

page.dark .item-selector {
  background: var(--color-surface-elevated);
  border-color: var(--color-border);
}

page.dark .item-option {
  border-bottom-color: var(--color-border);

  &.active {
    background: linear-gradient(135deg, rgba(255, 134, 58, 0.12) 0%, rgba(255, 134, 58, 0.06) 100%);
  }

  &:active {
    background: linear-gradient(135deg, rgba(255, 134, 58, 0.18) 0%, rgba(255, 134, 58, 0.09) 100%);
  }
}

page.dark .option-icon-box {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
}

page.dark .option-icon-custom {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.15) 0%, rgba(168, 85, 247, 0.08) 100%);
}

page.dark .option-icon-all {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(59, 130, 246, 0.08) 100%);
}

page.dark .option-check {
  background: #2c2c2e;
  border-color: var(--color-border-strong);

  &.show {
    background: var(--color-primary);
    border-color: var(--color-primary);
  }
}

page.dark .group-divider {
  background: var(--color-surface);
}

page.dark .modal-footer {
  background: var(--color-surface-elevated);
  border-top-color: var(--color-border);
}

page.dark .cancel-btn {
  border-right-color: var(--color-border);
}

page.dark .confirm-btn {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.08) 0%, rgba(255, 134, 58, 0.03) 100%);

  &:active {
    background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
  }
}
</style>
