<template>
  <view class="list-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">疫苗记录</text>
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
                  <text class="record-emoji">💉</text>
                  <text class="record-name">{{ item.name }}</text>
                  <view v-if="item.isCompleted" class="status-tag status-tag--done">
                    <text class="status-tag-text">已完成</text>
                  </view>
                  <view v-else-if="item.isUrgent" class="status-tag status-tag--urgent">
                    <text class="status-tag-text">紧急</text>
                  </view>
                  <view v-else class="status-tag status-tag--pending">
                    <text class="status-tag-text">待接种</text>
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
              <button v-if="!item.isCompleted" class="btn-action btn-edit" @tap="onEdit(item)">
                <text class="btn-action-text">编辑</text>
              </button>
              <button v-if="!item.isCompleted" class="btn-action btn-delete" @tap="onDelete(item)">
                <text class="btn-action-text">删除</text>
              </button>
            </view>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="empty-emoji">💉</text>
          <text class="empty-text">暂无疫苗记录</text>
          <text class="empty-hint">点击右下角按钮添加疫苗提醒</text>
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

    <!-- 编辑弹窗 -->
    <view class="modal-mask" v-if="showEditModal" @click="hideEditModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">编辑疫苗提醒</text>
          <text class="modal-close" @click="hideEditModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-group">
            <text class="form-label">疫苗名称</text>
            <input class="form-input" v-model="editForm.vaccineName" placeholder="请输入疫苗名称" />
          </view>
          <view class="form-group">
            <text class="form-label">计划日期</text>
            <picker mode="date" :value="editForm.nextDate" @change="onEditDateChange">
              <view class="picker-value">
                <text :class="editForm.nextDate ? 'value-text' : 'picker-placeholder'">{{ editForm.nextDate || '请选择日期' }}</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <view class="form-group">
            <text class="form-label">备注</text>
            <input class="form-input" v-model="editForm.note" placeholder="选填" />
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn modal-btn-cancel" @click="hideEditModal"><text class="modal-btn-text-cancel">取消</text></view>
          <view class="modal-btn modal-btn-confirm" @click="submitEdit"><text class="modal-btn-text-confirm">保存</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'
import * as petApi from '@/api/pet'

export default {
  data() {
    return {
      statusBarHeight: 20,
      petId: null,
      records: [],
      showEditModal: false,
      editingItem: null,
      editForm: {
        vaccineName: '',
        nextDate: '',
        note: ''
      }
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
        const res = await petApi.getVaccineReminders(this.petId)
        if (res && res.success && Array.isArray(res.data)) {
          const now = new Date()
          this.records = res.data.map((r) => {
            const next = r.nextDate ? new Date(r.nextDate) : null
            const rawDays = next && !Number.isNaN(next.getTime())
              ? Math.ceil((next - now) / (86400000))
              : 0
            const done = Number(r.status) === 1
            return {
              id: r.id,
              name: r.vaccineName || "疫苗",
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
        console.error("加载疫苗记录失败:", e)
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
              const result = await petApi.updateVaccineReminderStatus(this.petId, item.id, { status: 1 })
              if (result.success) {
                uni.showToast({ title: "已标记完成", icon: "success" })
                this.loadRecords()
              }
            } catch (e) {
              console.error("更新疫苗状态失败:", e)
              uni.showToast({ title: "操作失败", icon: "none" })
            }
          }
        }
      })
    },
    goBack() {
      uni.navigateBack()
    },
    onEdit(item) {
      this.editingItem = item
      this.editForm = {
        vaccineName: item.name || '',
        nextDate: item.date || '',
        note: item.note || ''
      }
      this.showEditModal = true
    },
    hideEditModal() {
      this.showEditModal = false
      this.editingItem = null
    },
    onEditDateChange(e) {
      this.editForm.nextDate = e.detail.value
    },
    async submitEdit() {
      if (!this.editForm.vaccineName) {
        uni.showToast({ title: '请输入疫苗名称', icon: 'none' })
        return
      }
      try {
        const res = await petApi.updateVaccineReminder(this.petId, this.editingItem.id, {
            vaccineName: this.editForm.vaccineName,
            nextDate: this.editForm.nextDate,
            note: this.editForm.note
          })
        if (res && res.success) {
          uni.showToast({ title: '修改成功', icon: 'success' })
          this.hideEditModal()
          this.loadRecords()
        } else {
          uni.showToast({ title: (res && res.message) || '修改失败', icon: 'none' })
        }
      } catch (e) {
        console.error('修改疫苗提醒失败:', e)
        uni.showToast({ title: '网络错误', icon: 'none' })
      }
    },
    onDelete(item) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除"${item.name}"的疫苗提醒吗？`,
        confirmText: '删除',
        confirmColor: '#ff4d4f',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await petApi.deleteVaccineReminder(this.petId, item.id)
              if (result && result.success) {
                uni.showToast({ title: '删除成功', icon: 'success' })
                this.loadRecords()
              } else {
                uni.showToast({ title: (result && result.message) || '删除失败', icon: 'none' })
              }
            } catch (e) {
              console.error('删除疫苗提醒失败:', e)
              uni.showToast({ title: '网络错误', icon: 'none' })
            }
          }
        }
      })
    },
    addRecord() {
      uni.navigateTo({ url: `/pages/health/index?petId=${this.petId}&tab=1` })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff5500;
$primary-soft: #ff7a3d;
$vaccine: #d97706;
$vaccine-light: #fbbf24;
$vaccine-bg: #fef3c7;
$text-dark: #1c1917;
$text-mid: #44403c;
$text-light: #a8a29e;
$page-bg: #fff5f0;

.list-page {
  min-height: 100vh;
  background: $page-bg;
}

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: #ffffff;
  box-shadow: 0 2rpx 16rpx rgba(255, 85, 0, 0.08);
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
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 32rpx;
  background: #f5f5f4;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.9);
    background: #e7e5e4;
  }
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 3rpx solid $text-dark;
  border-bottom: 3rpx solid $text-dark;
  transform: rotate(45deg);
  margin-left: -3rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 700;
  color: $text-dark;
}

.nav-placeholder {
  width: 64rpx;
}

.list-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.list-content {
  padding: 24rpx 28rpx 200rpx;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.record-card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 6rpx 24rpx rgba(255, 85, 0, 0.10);
  padding: 28rpx 24rpx 28rpx 30rpx;
  position: relative;
  overflow: hidden;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.25s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 20rpx;
    bottom: 20rpx;
    width: 6rpx;
    background: linear-gradient(180deg, $vaccine 0%, $vaccine-light 100%);
    border-radius: 0 6rpx 6rpx 0;
  }

  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 10rpx 32rpx rgba(255, 85, 0, 0.15);
  }
}

.record-card.urgent {
  background: linear-gradient(135deg, #fefce8 0%, $vaccine-bg 100%);
  border: 2rpx solid rgba($vaccine, 0.25);
  box-shadow: 0 6rpx 24rpx rgba($vaccine, 0.18);

  &::before {
    background: linear-gradient(180deg, $vaccine-light 0%, #f59e0b 100%);
    box-shadow: 0 0 12rpx rgba($vaccine, 0.4);
  }
}

.record-card.completed {
  opacity: 0.65;
}

.record-header {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 22rpx;
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
  margin-bottom: 12rpx;
  flex-wrap: wrap;
}

.record-emoji {
  font-size: 32rpx;
}

.record-name {
  font-size: 30rpx;
  font-weight: 700;
  color: $text-dark;
}

.status-tag {
  padding: 5rpx 16rpx;
  border-radius: 999rpx;
  margin-left: 8rpx;
}

.status-tag--done {
  background: #d1fae5;
}

.status-tag--urgent {
  background: linear-gradient(135deg, $vaccine 0%, $vaccine-light 100%);
  box-shadow: 0 2rpx 8rpx rgba($vaccine, 0.3);
}

.status-tag--pending {
  background: $vaccine-bg;
}

.status-tag-text {
  font-size: 20rpx;
  font-weight: 600;
}

.status-tag--done .status-tag-text {
  color: #047857;
}

.status-tag--urgent .status-tag-text {
  color: #ffffff;
}

.status-tag--pending .status-tag-text {
  color: $vaccine;
}

.record-date {
  display: block;
  font-size: 24rpx;
  color: $text-mid;
  margin-bottom: 8rpx;
}

.record-note {
  display: block;
  font-size: 22rpx;
  color: $text-light;
  line-height: 1.6;
}

.record-countdown {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  background: #d1fae5;
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.15);
}

.record-card.urgent .record-countdown {
  background: linear-gradient(135deg, $vaccine 0%, $vaccine-light 100%);
  box-shadow: 0 4rpx 14rpx rgba($vaccine, 0.35);
}

.countdown-number {
  font-size: 40rpx;
  font-weight: 900;
  color: $text-dark;
  margin-right: 4rpx;
}

.record-card.urgent .countdown-number,
.record-card.urgent .countdown-unit {
  color: #ffffff;
}

.countdown-unit {
  font-size: 22rpx;
  font-weight: 500;
  color: $text-mid;
}

.record-progress {
  margin-bottom: 22rpx;
}

.progress-bar {
  height: 12rpx;
  background: #fef3c7;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $vaccine 0%, $vaccine-light 100%);
  border-radius: 6rpx;
  transition: width 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.progress-text {
  font-size: 20rpx;
  color: $text-light;
  text-align: right;
  display: block;
}

.record-actions {
  display: flex;
  justify-content: center;
  gap: 14rpx;
}

.btn-action {
  background: linear-gradient(135deg, $primary 0%, $primary-soft 100%);
  color: #ffffff;
  border: none;
  border-radius: 999rpx;
  padding: 17rpx 48rpx;
  font-size: 26rpx;
  font-weight: 600;
  line-height: 1.2;
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.35);
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
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

.btn-edit {
  background: $vaccine-bg !important;
  box-shadow: none !important;
}

.btn-edit .btn-action-text {
  color: $vaccine !important;
}

.btn-delete {
  background: #fef2f2 !important;
  box-shadow: none !important;
}

.btn-delete .btn-action-text {
  color: #ef4444 !important;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 140rpx 40rpx;
}

.empty-emoji {
  font-size: 96rpx;
  margin-bottom: 28rpx;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12rpx); }
}

.empty-text {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-mid;
  margin-bottom: 14rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: $text-light;
}

.fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(env(safe-area-inset-bottom) + 40rpx);
  z-index: 45;
}

.fab-inner {
  width: 108rpx;
  height: 108rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, $primary 0%, $primary-soft 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 36rpx rgba($primary, 0.4), 0 3rpx 10rpx rgba($primary, 0.2);
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.25s ease;
}

.fab:active .fab-inner {
  transform: scale(0.90) rotate(90deg);
  box-shadow: 0 6rpx 20rpx rgba($primary, 0.3);
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

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(28, 25, 23, 0.55);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 82%;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 34rpx 32rpx;
  border-bottom: 1rpx solid #f5f5f4;
}

.modal-title {
  font-size: 33rpx;
  font-weight: 800;
  color: $text-dark;
}

.modal-close {
  font-size: 36rpx;
  color: $text-light;
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s ease;

  &:active {
    background: #f5f5f4;
  }
}

.modal-body {
  padding: 32rpx;
}

.form-group {
  margin-bottom: 28rpx;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 28rpx;
  font-weight: 700;
  color: $text-dark;
  margin-bottom: 14rpx;
  display: block;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  background: #fafaf9;
  border: 2rpx solid #e7e5e4;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  min-height: 80rpx;
  font-size: 28rpx;
  color: $text-dark;
  transition: border-color 0.2s ease;

  &:focus {
    border-color: $primary-soft;
  }
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fafaf9;
  border: 2rpx solid #e7e5e4;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  min-height: 80rpx;
  transition: border-color 0.2s ease;
}

.value-text {
  font-size: 28rpx;
  color: $text-dark;
}

.picker-placeholder {
  font-size: 28rpx;
  color: $text-light;
}

.picker-arrow {
  font-size: 20rpx;
  color: $text-light;
}

.modal-footer {
  display: flex;
  gap: 18rpx;
  padding: 26rpx 32rpx;
  border-top: 1rpx solid #f5f5f4;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.96);
  }
}

.modal-btn-cancel {
  background: #f5f5f4;
}

.modal-btn-text-cancel {
  font-size: 28rpx;
  font-weight: 700;
  color: $text-mid;
}

.modal-btn-confirm {
  background: linear-gradient(135deg, $primary 0%, $primary-soft 100%);
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.35);
}

.modal-btn-text-confirm {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}
</style>
