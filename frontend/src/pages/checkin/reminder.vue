<template>
  <view class="reminder-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="nav-back-arrow"></view>
      </view>
      <text class="nav-title">打卡提醒</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="reminder-scroll" :style="{ top: navHeight + 'px' }">
      <view class="reminder-content">
        <view class="section-header">
          <text class="section-title">提醒列表</text>
          <view class="add-btn" @click="showAddModal = true">
            <text class="add-btn-text">+ 添加提醒</text>
          </view>
        </view>

        <view v-if="loading" class="loading-state">
          <text class="loading-text">加载中...</text>
        </view>

        <view v-else-if="reminders.length === 0" class="empty-state">
          <text class="empty-emoji">⏰</text>
          <text class="empty-text">暂无打卡提醒</text>
          <text class="empty-hint">点击右上角添加提醒，不再忘记打卡</text>
        </view>

        <view v-else class="reminder-list">
          <view v-for="item in reminders" :key="item.id" class="reminder-card">
            <view class="reminder-info">
              <text class="reminder-time">{{ formatTime(item.remindTime) }}</text>
              <text class="reminder-target">{{ item.itemId ? '指定打卡项' : '全部打卡项' }}</text>
            </view>
            <view class="reminder-actions">
              <switch
                :checked="item.isEnabled"
                color="#ff6a3d"
                @change="onToggleReminder(item, $event)"
              />
              <view class="delete-btn" @click="onDeleteReminder(item)">
                <text class="delete-btn-text">删除</text>
              </view>
            </view>
          </view>
        </view>

        <view class="tips-card">
          <text class="tips-title">💡 提示</text>
          <text class="tips-text">打卡提醒通过系统通知推送，请确保已开启通知权限。</text>
        </view>

        <view class="page-bottom-safe"></view>
      </view>
    </scroll-view>

    <view v-if="showAddModal" class="modal-mask" @click="showAddModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">添加打卡提醒</text>
          <text class="modal-close" @click="showAddModal = false">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">提醒时间</text>
            <picker mode="time" :value="newRemindTime" @change="onTimeChange">
              <view class="time-picker">
                <text class="time-picker-text">{{ newRemindTime }}</text>
                <text class="time-picker-arrow">›</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <text class="modal-cancel" @click="showAddModal = false">取消</text>
          <text class="modal-confirm" @click="onAddReminder">确定</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as checkinApi from '@/api/checkin'

export default {
  data() {
    return {
      statusBarHeight: 20,
      navHeight: 64,
      reminders: [],
      loading: false,
      showAddModal: false,
      newRemindTime: '09:00'
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
  },
  methods: {
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
    async onAddReminder() {
      try {
        const res = await checkinApi.createCheckinReminder({
          remindTime: this.newRemindTime
        })
        if (res && res.success) {
          uni.showToast({ title: '添加成功', icon: 'success' })
          this.showAddModal = false
          this.loadReminders()
        }
      } catch (e) {
        console.error('添加提醒失败:', e)
        uni.showToast({ title: '添加失败', icon: 'none' })
      }
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      return timeStr.substring(0, 5)
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$primary-light: #fff0ea;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;
$radius: 24rpx;

.reminder-page { min-height: 100vh; background: $bg; }
.status-bar { width: 100%; background: $card-bg; }
.nav-bar {
  height: 88rpx; display: flex; align-items: center;
  justify-content: space-between; padding: 0 28rpx;
  background: $card-bg; border-bottom: 1rpx solid #f0f0f0;
}
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow {
  width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary;
  border-bottom: 4rpx solid $text-primary; transform: rotate(45deg);
}
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }

.reminder-scroll { position: fixed; left: 0; right: 0; bottom: 0; }
.reminder-content { padding: 24rpx; }

.section-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20rpx;
}
.section-title { font-size: 30rpx; font-weight: 700; color: $text-primary; }
.add-btn { padding: 10rpx 24rpx; border-radius: 24rpx; background: $primary-light; }
.add-btn-text { font-size: 24rpx; color: $primary; font-weight: 600; }

.reminder-list { display: flex; flex-direction: column; gap: 16rpx; }
.reminder-card {
  background: $card-bg; border-radius: $radius; padding: 24rpx;
  display: flex; justify-content: space-between; align-items: center;
}
.reminder-info { display: flex; flex-direction: column; gap: 4rpx; }
.reminder-time { font-size: 36rpx; font-weight: 700; color: $text-primary; }
.reminder-target { font-size: 24rpx; color: $text-light; }
.reminder-actions { display: flex; align-items: center; gap: 16rpx; }
.delete-btn { padding: 8rpx 16rpx; }
.delete-btn-text { font-size: 24rpx; color: #ff4d4f; }

.loading-state { display: flex; justify-content: center; padding: 100rpx 0; }
.loading-text { font-size: 28rpx; color: $text-light; }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 80rpx 0;
}
.empty-emoji { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: $text-secondary; margin-bottom: 8rpx; }
.empty-hint { font-size: 24rpx; color: $text-light; }

.tips-card {
  background: $card-bg; border-radius: $radius;
  padding: 24rpx; margin-top: 24rpx;
}
.tips-title { font-size: 26rpx; font-weight: 600; color: $text-primary; display: block; margin-bottom: 8rpx; }
.tips-text { font-size: 24rpx; color: $text-secondary; line-height: 36rpx; }

.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: center; justify-content: center;
}
.modal-card {
  width: 600rpx; background: $card-bg; border-radius: $radius;
  overflow: hidden;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 28rpx; border-bottom: 1rpx solid #f0f0f0;
}
.modal-title { font-size: 30rpx; font-weight: 700; color: $text-primary; }
.modal-close { font-size: 32rpx; color: $text-light; padding: 8rpx; }
.modal-body { padding: 28rpx; }
.form-item { margin-bottom: 20rpx; }
.form-label { font-size: 26rpx; color: $text-secondary; display: block; margin-bottom: 12rpx; }
.time-picker {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20rpx; background: #f5f5f5; border-radius: 16rpx;
}
.time-picker-text { font-size: 32rpx; font-weight: 600; color: $text-primary; }
.time-picker-arrow { font-size: 28rpx; color: $text-light; }
.modal-footer {
  display: flex; border-top: 1rpx solid #f0f0f0;
}
.modal-cancel, .modal-confirm {
  flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; font-weight: 600;
}
.modal-cancel { color: $text-secondary; }
.modal-confirm { color: $primary; }

.page-bottom-safe { height: calc(24rpx + env(safe-area-inset-bottom)); }
</style>
