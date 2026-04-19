<template>
  <view class="list-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">驱虫记录</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="list-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="list-content">
        <view v-if="records.length" class="record-list">
          <view
            v-for="item in records"
            :key="item.id"
            class="record-card"
            :class="{ urgent: item.isUrgent, completed: item.isCompleted }"
          >
            <view class="record-header">
              <view class="record-info">
                <view class="record-name-row">
                  <text class="record-emoji">💊</text>
                  <text class="record-name">{{ item.name }}</text>
                  <view v-if="item.isCompleted" class="status-tag status-tag--done">
                    <text class="status-tag-text">已完成</text>
                  </view>
                  <view v-else-if="item.isUrgent" class="status-tag status-tag--urgent">
                    <text class="status-tag-text">紧急</text>
                  </view>
                  <view v-else class="status-tag status-tag--pending">
                    <text class="status-tag-text">待驱虫</text>
                  </view>
                </view>
                <text class="record-date">计划日期: {{ item.date }}</text>
                <text v-if="item.note" class="record-note">{{ item.note }}</text>
              </view>
              <view class="record-countdown">
                <text class="countdown-number">{{ item.daysLeft }}</text>
                <text class="countdown-unit">天</text>
              </view>
            </view>

            <view class="record-progress">
              <view class="progress-bar">
                <view class="progress-fill" :style="{ width: item.progressPercent + '%' }"></view>
              </view>
              <text class="progress-text">{{ item.progressPercent }}%</text>
            </view>

            <view class="record-actions">
              <button
                class="btn-action"
                :class="{ completed: item.isCompleted }"
                :disabled="item.isCompleted"
                @tap="onMarkDone(item)"
              >
                <text class="btn-action-text">{{ item.isCompleted ? "已完成" : "标记完成" }}</text>
              </button>
            </view>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="empty-emoji">💊</text>
          <text class="empty-text">暂无驱虫记录</text>
          <text class="empty-hint">点击右下角按钮添加驱虫提醒</text>
        </view>
      </view>
    </scroll-view>

    <view class="fab" @tap="addRecord">
      <view class="fab-inner">
        <view class="fab-icon-wrapper">
          <view class="fab-hbar"></view>
          <view class="fab-vbar"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 20,
      petId: null,
      records: []
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    if (options.petId) {
      this.petId = parseInt(options.petId, 10)
    }
    this.loadRecords()
  },
  onShow() {
    this.loadRecords()
  },
  methods: {
    formatDateYMD(date) {
      if (!date) return "-"
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return "-"
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
    },
    async loadRecords() {
      if (!this.petId) return
      try {
        const res = await uni.$request.get(`/api/pets/${this.petId}/parasite-reminders`)
        if (res && res.success && Array.isArray(res.data)) {
          const now = new Date()
          const typeMap = { 1: "体内驱虫", 2: "体外驱虫", 3: "内外同驱" }
          this.records = res.data.map((r) => {
            const next = r.nextDate ? new Date(r.nextDate) : null
            const rawDays = next && !Number.isNaN(next.getTime())
              ? Math.ceil((next - now) / (86400000))
              : 0
            const done = Number(r.status) === 1
            return {
              id: r.id,
              name: typeMap[r.type] || "驱虫",
              date: this.formatDateYMD(r.nextDate),
              daysLeft: Math.max(0, rawDays),
              progressPercent: done ? 100 : 0,
              isCompleted: done,
              isUrgent: !done && rawDays <= 7,
              note: r.note || ""
            }
          })
        } else {
          this.records = []
        }
      } catch (e) {
        console.error("加载驱虫记录失败:", e)
        this.records = []
      }
    },
    async onMarkDone(item) {
      if (!item || item.isCompleted) return
      const loggedIn = await checkLogin('请先登录后再操作')
      if (!loggedIn) return
      uni.showModal({
        title: "确认完成",
        content: `确定要将"${item.name}"标记为已完成吗？`,
        confirmText: "确定",
        cancelText: "取消",
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await uni.$request.put(
                `/api/pets/${this.petId}/parasite-reminders/${item.id}/status`,
                { status: 1 }
              )
              if (result.success) {
                uni.showToast({ title: "已标记完成", icon: "success" })
                this.loadRecords()
              }
            } catch (e) {
              console.error("更新驱虫状态失败:", e)
              uni.showToast({ title: "操作失败", icon: "none" })
            }
          }
        }
      })
    },
    goBack() {
      uni.navigateBack()
    },
    addRecord() {
      uni.navigateTo({ url: `/pages/health/index?petId=${this.petId}&tab=2` })
    }
  }
}
</script>

<style lang="scss" scoped>
.list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.status-bar {
  width: 100%;
}

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: background 0.2s, transform 0.15s;
}

.nav-back:active {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(0.92);
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
}

.nav-placeholder {
  width: 64rpx;
}

.list-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.list-content {
  padding: 20rpx 24rpx 200rpx;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.record-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  padding: 28rpx 24rpx;
}

.record-card.urgent {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border: 2rpx solid rgba(255, 77, 79, 0.2);
}

.record-card.completed {
  opacity: 0.75;
}

.record-header {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.record-info {
  flex: 1;
  padding-right: 16rpx;
}

.record-name-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 10rpx;
}

.record-emoji {
  font-size: 30rpx;
}

.record-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
}

.status-tag {
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  margin-left: 8rpx;
}

.status-tag--done {
  background: #d1fae5;
}

.status-tag--urgent {
  background: #ff4d4f;
}

.status-tag--pending {
  background: #e0e7ff;
}

.status-tag-text {
  font-size: 20rpx;
  font-weight: 500;
}

.status-tag--done .status-tag-text {
  color: #047857;
}

.status-tag--urgent .status-tag-text {
  color: #fff;
}

.status-tag--pending .status-tag-text {
  color: #4f46e5;
}

.record-date {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
  margin-bottom: 6rpx;
}

.record-note {
  display: block;
  font-size: 22rpx;
  color: #9ca3af;
  line-height: 1.5;
}

.record-countdown {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  background: #d1fae5;
  padding: 12rpx 20rpx;
  border-radius: 30rpx;
  flex-shrink: 0;
}

.record-card.urgent .record-countdown {
  background: #ff4d4f;
}

.countdown-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
  margin-right: 4rpx;
}

.record-card.urgent .countdown-number,
.record-card.urgent .countdown-unit {
  color: #ffffff;
}

.countdown-unit {
  font-size: 22rpx;
  color: #6b7280;
}

.record-progress {
  margin-bottom: 20rpx;
}

.progress-bar {
  height: 12rpx;
  background: #fff4e6;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #d1fae5 0%, #10b981 100%);
  border-radius: 6rpx;
}

.progress-text {
  font-size: 20rpx;
  color: #9ca3af;
  text-align: right;
  display: block;
}

.record-actions {
  display: flex;
  justify-content: center;
}

.btn-action {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #ffffff;
  border: none;
  border-radius: 999rpx;
  padding: 16rpx 48rpx;
  font-size: 26rpx;
  font-weight: 500;
  line-height: 1.2;
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);
}

.btn-action.completed {
  background: #d1fae5;
  box-shadow: none;
}

.btn-action::after {
  border: none;
}

.btn-action-text {
  color: #ffffff;
}

.btn-action.completed .btn-action-text {
  color: #047857;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

.empty-emoji {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #9ca3af;
}

.fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(env(safe-area-inset-bottom) + 40rpx);
  z-index: 45;
}

.fab-inner {
  width: 104rpx;
  height: 104rpx;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(255, 77, 79, 0.4), 0 2rpx 8rpx rgba(255, 77, 79, 0.2);
  backdrop-filter: blur(12px);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.fab:active .fab-inner {
  transform: scale(0.92);
  box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.3);
}

.fab-icon-wrapper {
  position: relative;
  width: 38rpx;
  height: 38rpx;
}

.fab-hbar,
.fab-vbar {
  position: absolute;
  background: #fff;
  border-radius: 4rpx;
}

.fab-hbar {
  top: 50%;
  left: 0;
  width: 38rpx;
  height: 5rpx;
  transform: translateY(-50%);
}

.fab-vbar {
  left: 50%;
  top: 0;
  width: 5rpx;
  height: 38rpx;
  transform: translateX(-50%);
}
</style>
