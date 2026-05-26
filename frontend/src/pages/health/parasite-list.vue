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

    <!-- 编辑弹窗 -->
    <view class="modal-mask" v-if="showEditModal" @click="hideEditModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">编辑驱虫提醒</text>
          <text class="modal-close" @click="hideEditModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-group">
            <text class="form-label">驱虫类型</text>
            <picker :range="typeNames" :value="editForm.typeIndex" @change="onTypeChange">
              <view class="picker-value">
                <text class="value-text">{{ typeNames[editForm.typeIndex] || '请选择' }}</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <view v-if="editForm.typeIndex === typeNames.length - 1" class="form-group">
            <text class="form-label">自定义驱虫名称</text>
            <input class="form-input" v-model="editForm.customName" placeholder="请输入自定义驱虫名称" maxlength="30" />
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
      typeNames: ['体内驱虫', '体外驱虫', '内外同驱', '自定义'],
      typeMap: { 1: '体内驱虫', 2: '体外驱虫', 3: '内外同驱' },
      typeValueMap: { 0: 1, 1: 2, 2: 3 },
      showEditModal: false,
      editingItem: null,
      editForm: {
        typeIndex: 0,
        customName: '',
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
        const res = await petApi.getParasiteReminders(this.petId)
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
              name: r.typeName || this.typeMap[r.type] || "驱虫",
              customName: r.customName || "",
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
              const result = await petApi.updateParasiteReminderStatus(this.petId, item.id, { status: 1 })
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
    onEdit(item) {
      this.editingItem = item
      const typeKeys = Object.keys(this.typeMap)
      const typeIdx = typeKeys.findIndex(k => this.typeMap[k] === item.name)
      const isCustomType = typeIdx < 0 || item.customName
      this.editForm = {
        typeIndex: isCustomType ? this.typeNames.length - 1 : typeIdx,
        customName: isCustomType ? (item.customName || item.name) : '',
        nextDate: item.date || '',
        note: item.note || ''
      }
      this.showEditModal = true
    },
    hideEditModal() {
      this.showEditModal = false
      this.editingItem = null
    },
    onTypeChange(e) {
      this.editForm.typeIndex = Number(e.detail.value || 0)
    },
    onEditDateChange(e) {
      this.editForm.nextDate = e.detail.value
    },
    async submitEdit() {
      const isCustom = this.editForm.typeIndex === this.typeNames.length - 1
      if (isCustom && !this.editForm.customName.trim()) {
        uni.showToast({ title: '请输入自定义驱虫名称', icon: 'none' })
        return
      }
      try {
        const typeValue = isCustom ? 0 : (this.typeValueMap[this.editForm.typeIndex] || 1)
        const submitData = {
          type: typeValue,
          nextDate: this.editForm.nextDate,
          note: this.editForm.note
        }
        if (isCustom) {
          submitData.customName = this.editForm.customName.trim()
        }
        const res = await petApi.updateParasiteReminder(this.petId, this.editingItem.id, submitData)
        if (res && res.success) {
          uni.showToast({ title: '修改成功', icon: 'success' })
          this.hideEditModal()
          this.loadRecords()
        } else {
          uni.showToast({ title: (res && res.message) || '修改失败', icon: 'none' })
        }
      } catch (e) {
        console.error('修改驱虫提醒失败:', e)
        uni.showToast({ title: '网络不给力，请稍后重试', icon: 'none' })
      }
    },
    onDelete(item) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除"${item.name}"的驱虫提醒吗？`,
        confirmText: '删除',
        confirmColor: '#ff4d4f',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await petApi.deleteParasiteReminder(this.petId, item.id)
              if (result && result.success) {
                uni.showToast({ title: '删除成功', icon: 'success' })
                this.loadRecords()
              } else {
                uni.showToast({ title: (result && result.message) || '删除失败', icon: 'none' })
              }
            } catch (e) {
              console.error('删除驱虫提醒失败:', e)
              uni.showToast({ title: '网络不给力，请稍后重试', icon: 'none' })
            }
          }
        }
      })
    },
    addRecord() {
      uni.navigateTo({ url: `/pages/health/index?petId=${this.petId}&tab=2` })
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   驱虫记录列表 - 紫色主题设计 v2.0
   与日历/相册/挑战赛风格统一
   ============================================ */

/* ========== 设计变量 ========== */
$primary: #ff5500;
$primary-soft: #ff7a3d;
$parasite: #9333ea;
$parasite-soft: #a855f7;
$parasite-bg: #f3e8ff;
$parasite-light: #ede9fe;

$bg: #fff5f0;
$white: #ffffff;
$text-dark: #1c1917;
$text-mid: #44403c;
$text-light: #a8a29e;

/* ========== 页面基础 ========== */
.list-page {
  min-height: 100vh;
  background: $bg;
}

/* ========== 导航栏 - 白底 + CSS箭头 ========== */
.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: $white;
  border-bottom: 2rpx solid #f5ebe5;
}

.status-bar {
  width: 100%;
}

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
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
  font-weight: 900;
  color: $text-dark;
  letter-spacing: 0.5rpx;
}

.nav-placeholder {
  width: 64rpx;
}

/* ========== 滚动区域 ========== */
.list-scroll {
  height: 100vh;
}

.list-content {
  padding: 24rpx 28rpx 200rpx;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* ========== 记录卡片 - 白底 + 左侧紫色竖条 + 橙色阴影 ========== */
.record-card {
  position: relative;
  background: $white;
  border-radius: 24rpx;
  box-shadow:
    0 4rpx 24rpx rgba(255, 85, 0, 0.08),
    0 1rpx 4rpx rgba(255, 85, 0, 0.03);
  border: 1.5rpx solid rgba(255, 122, 61, 0.08);
  padding: 28rpx 24rpx 28rpx 30rpx;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 20rpx;
    bottom: 20rpx;
    width: 6rpx;
    background: linear-gradient(180deg, $parasite 0%, $parasite-soft 100%);
    border-radius: 0 3rpx 3rpx 0;
  }

  /* 紧急状态：淡紫渐变背景 + 紫色边框 */
  &.urgent {
    background: linear-gradient(135deg, #faf5ff 0%, $parasite-light 100%);
    border-color: rgba($parasite, 0.25);
    box-shadow:
      0 4rpx 24rpx rgba($parasite, 0.12),
      0 1rpx 4rpx rgba($parasite, 0.05);

    &::before {
      background: linear-gradient(180deg, $parasite 0%, #c026d3 100%);
      box-shadow: 0 0 10rpx rgba($parasite, 0.4);
    }
  }

  /* 完成状态：降低透明度 */
  &.completed {
    opacity: 0.65;
  }
}

/* ========== 卡片头部 ========== */
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
  gap: 10rpx;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
}

.record-emoji {
  font-size: 32rpx;
  line-height: 1;
}

.record-name {
  font-size: 30rpx;
  font-weight: 800;
  color: $text-dark;
  letter-spacing: 0.3rpx;
}

/* ========== 状态标签 - 药丸形 ========== */
.status-tag {
  padding: 5rpx 16rpx;
  border-radius: 999rpx;
  margin-left: 6rpx;
}

.status-tag--done {
  background: #dcfce7;
}

.status-tag--urgent {
  background: linear-gradient(135deg, $parasite 0%, #c026d3 100%);
  animation: urgentPulse 2s ease-in-out infinite;
}

.status-tag--pending {
  background: $parasite-light;
}

.status-tag-text {
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

.status-tag--done .status-tag-text {
  color: #15803d;
}

.status-tag--urgent .status-tag-text {
  color: #fff;
}

.status-tag--pending .status-tag-text {
  color: $parasite;
}

@keyframes urgentPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.75; }
}

/* ========== 日期与备注 ========== */
.record-date {
  display: block;
  font-size: 25rpx;
  color: $text-light;
  margin-bottom: 8rpx;
  font-weight: 500;
}

.record-note {
  display: block;
  font-size: 23rpx;
  color: $text-light;
  line-height: 1.55;
  padding: 10rpx 14rpx;
  background: #fafaf9;
  border-radius: 12rpx;
  margin-top: 8rpx;
}

/* ========== 倒计时 - 紫色药丸标签 ========== */
.record-countdown {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  background: linear-gradient(135deg, $parasite-bg 0%, $parasite-light 100%);
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
  border: 1.5rpx solid rgba($parasite, 0.15);
  box-shadow: 0 2rpx 10rpx rgba($parasite, 0.1);
}

.record-card.urgent .record-countdown {
  background: linear-gradient(135deg, $parasite 0%, #c026d3 100%);
  border-color: transparent;
  box-shadow: 0 4rpx 16rpx rgba($parasite, 0.35);
}

.countdown-number {
  font-size: 38rpx;
  font-weight: 900;
  color: $parasite;
  margin-right: 4rpx;
  line-height: 1;
}

.countdown-unit {
  font-size: 22rpx;
  color: $parasite;
  font-weight: 600;
}

.record-card.urgent .countdown-number,
.record-card.urgent .countdown-unit {
  color: #fff;
}

/* ========== 进度条 ========== */
.record-progress {
  margin-bottom: 22rpx;
}

.progress-bar {
  height: 12rpx;
  background: #fef3e8;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 8rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $parasite 0%, $parasite-soft 100%);
  border-radius: 6rpx;
  transition: width 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.progress-text {
  font-size: 21rpx;
  color: $text-light;
  text-align: right;
  display: block;
  font-weight: 500;
}

/* ========== 操作按钮区 ========== */
.record-actions {
  display: flex;
  justify-content: center;
  gap: 14rpx;
  flex-wrap: wrap;
}

/* 主操作按钮 - 橙色渐变胶囊形 */
.btn-action {
  background: linear-gradient(135deg, $primary 0%, $primary-soft 100%);
  color: $white;
  border: none;
  border-radius: 999rpx;
  padding: 18rpx 52rpx;
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1.2;
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.35);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);

  &::after { border: none; }

  &:active {
    transform: scale(0.95);
    box-shadow: 0 2rpx 8rpx rgba($primary, 0.25);
  }

  /* 已完成态 */
  &.completed {
    background: #dcfce7;
    box-shadow: none;
  }
}

.btn-action-text {
  color: $white;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

.btn-action.completed .btn-action-text {
  color: #15803d;
}

/* 编辑按钮 - 淡紫底紫色文字 */
.btn-edit {
  background: $parasite-bg !important;
  box-shadow: 0 2rpx 10rpx rgba($parasite, 0.12) !important;

  &:active {
    transform: scale(0.95);
    background: #eadaff !important;
  }
}

.btn-edit .btn-action-text {
  color: $parasite !important;
}

/* 删除按钮 - 淡红底红色文字 */
.btn-delete {
  background: #fef2f2 !important;
  box-shadow: 0 2rpx 10rpx rgba(239, 68, 68, 0.1) !important;

  &:active {
    transform: scale(0.95);
    background: #fde5e5 !important;
  }
}

.btn-delete .btn-action-text {
  color: #ef4444 !important;
}

/* ========== 空状态 ========== */
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
  animation: floatEmoji 3s ease-in-out infinite;
}

.empty-text {
  font-size: 32rpx;
  font-weight: 800;
  color: $text-mid;
  margin-bottom: 14rpx;
  letter-spacing: 0.5rpx;
}

.empty-hint {
  font-size: 25rpx;
  color: $text-light;
  font-weight: 500;
}

@keyframes floatEmoji {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10rpx); }
}

/* ========== FAB - 圆角方形 + 橙色渐变 ========== */
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
  box-shadow:
    0 8rpx 32rpx rgba($primary, 0.4),
    0 2rpx 8rpx rgba($primary, 0.2);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fab:active .fab-inner {
  transform: scale(0.9) rotate(90deg);
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.3);
}

.fab-icon-wrapper {
  position: relative;
  width: 38rpx;
  height: 38rpx;
}

.fab-hbar,
.fab-vbar {
  position: absolute;
  background: $white;
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

/* ========== 弹窗 - 圆角24rpx ========== */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(28, 25, 23, 0.45);
  backdrop-filter: blur(8rpx);
  -webkit-backdrop-filter: blur(8rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

.modal-content {
  width: 82%;
  max-height: 80vh;
  background: $white;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow:
    0 20rpx 60rpx rgba(0, 0, 0, 0.15),
    0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  animation: modalSlideUp 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 28rpx;
  border-bottom: 1.5rpx solid #f5ebe5;
}

.modal-title {
  font-size: 33rpx;
  font-weight: 900;
  color: $text-dark;
  letter-spacing: 0.5rpx;
}

.modal-close {
  font-size: 36rpx;
  color: $text-light;
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 26rpx;
  background: #fafaf9;
  transition: all 0.2s ease;

  &:active {
    background: #f5f5f4;
    transform: scale(0.9);
  }
}

.modal-body {
  padding: 28rpx 28rpx 16rpx;
}

.form-group {
  margin-bottom: 26rpx;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 27rpx;
  font-weight: 700;
  color: $text-dark;
  margin-bottom: 14rpx;
  display: block;
  letter-spacing: 0.3rpx;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  background: #fafaf9;
  border: 1.5rpx solid #e7e5e4;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  min-height: 80rpx;
  font-size: 28rpx;
  color: $text-dark;
  transition: border-color 0.2s ease;

  &:focus {
    border-color: $parasite;
    background: $white;
  }
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fafaf9;
  border: 1.5rpx solid #e7e5e4;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  min-height: 80rpx;
  transition: border-color 0.2s ease;

  &:active {
    border-color: $parasite;
  }
}

.value-text {
  font-size: 28rpx;
  color: $text-dark;
  font-weight: 500;
}

.picker-placeholder {
  font-size: 28rpx;
  color: $text-light;
}

.picker-arrow {
  font-size: 18rpx;
  color: $text-light;
}

/* ========== 弹窗底部按钮 ========== */
.modal-footer {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 28rpx 28rpx;
  border-top: 1.5rpx solid #f5ebe5;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);

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
  letter-spacing: 0.5rpx;
}

.modal-btn-confirm {
  background: linear-gradient(135deg, $primary 0%, $primary-soft 100%);
  box-shadow: 0 4rpx 14rpx rgba($primary, 0.3);
}

.modal-btn-text-confirm {
  font-size: 28rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.5rpx;
}
</style>
