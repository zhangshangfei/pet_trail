<template>
  <view class="appointments-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">我的预约</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">
        <view class="tab-bar">
          <view
            v-for="tab in tabs"
            :key="tab.value"
            class="tab-item"
            :class="{ active: activeTab === tab.value }"
            @tap="activeTab = tab.value"
          >
            <text class="tab-text">{{ tab.label }}</text>
            <view class="tab-count" v-if="getTabCount(tab.value) > 0">
              <text class="tab-count-text">{{ getTabCount(tab.value) }}</text>
            </view>
          </view>
        </view>

        <view class="appointment-list" v-if="filteredList.length > 0">
          <view
            v-for="item in filteredList"
            :key="item.id"
            class="appointment-card"
          >
            <view class="card-header">
              <view class="clinic-info" @tap="goClinicDetail(item)">
                <view class="clinic-icon-wrap">
                  <text class="clinic-emoji">🏥</text>
                </view>
                <view class="clinic-meta">
                  <text class="clinic-name">{{ item.clinicName || '未知医院' }}</text>
                  <text class="appointment-time">{{ item.appointmentDate }} {{ item.appointmentTime }}</text>
                </view>
              </view>
              <view class="status-tag" :class="'status-' + item.status">
                <text class="status-text">{{ getStatusLabel(item.status) }}</text>
              </view>
            </view>

            <view class="card-body">
              <view class="info-row" v-if="item.petName">
                <text class="info-label">宠物</text>
                <text class="info-value">{{ item.petName }}</text>
              </view>
              <view class="info-row" v-if="item.symptom">
                <text class="info-label">症状</text>
                <text class="info-value">{{ item.symptom }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">预约时间</text>
                <text class="info-value">{{ formatDateTime(item.createdAt) }}</text>
              </view>
              <view class="info-row" v-if="item.cancelReason">
                <text class="info-label">取消原因</text>
                <text class="info-value cancel-reason">{{ item.cancelReason }}</text>
              </view>
            </view>

            <view class="card-footer" v-if="item.status === 0 || item.status === 1">
              <view class="cancel-btn" @tap="showCancelModal(item)">
                <text class="cancel-btn-text">取消预约</text>
              </view>
            </view>
          </view>
        </view>

        <view class="empty-state" v-if="!loading && filteredList.length === 0">
          <text class="empty-emoji">📋</text>
          <text class="empty-text">{{ activeTab === 'all' ? '暂无预约记录' : '暂无' + getTabLabel(activeTab) + '的预约' }}</text>
          <view class="empty-btn" @tap="goVetIndex">
            <text class="empty-btn-text">去预约挂号</text>
          </view>
        </view>

        <view class="loading-state" v-if="loading">
          <text class="loading-text">加载中...</text>
        </view>
      </view>
    </scroll-view>

    <view class="modal-mask" v-if="showCancelPopup" @tap="hideCancelModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">取消预约</text>
          <text class="modal-close" @tap="hideCancelModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="cancel-info">
            <text class="cancel-info-text">确定要取消在「{{ cancelTarget.clinicName || '未知医院' }}」的预约吗？</text>
          </view>
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">取消原因（选填）</text>
            </view>
            <textarea
              class="form-input"
              v-model="cancelReason"
              placeholder="请输入取消原因"
              maxlength="200"
            />
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn modal-btn-cancel" @tap="hideCancelModal">
            <text class="modal-btn-text-cancel">再想想</text>
          </view>
          <view class="modal-btn modal-btn-confirm" @tap="confirmCancel">
            <text class="modal-btn-text-confirm">确认取消</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as vetApi from '@/api/vet'

export default {
  data() {
    return {
      statusBarHeight: 20,
      appointments: [],
      loading: false,
      activeTab: 'all',
      tabs: [
        { label: '全部', value: 'all' },
        { label: '待确认', value: 0 },
        { label: '已确认', value: 1 },
        { label: '已完成', value: 2 },
        { label: '已取消', value: 3 }
      ],
      showCancelPopup: false,
      cancelTarget: {},
      cancelReason: ''
    }
  },
  computed: {
    filteredList() {
      if (this.activeTab === 'all') return this.appointments
      return this.appointments.filter(item => item.status === this.activeTab)
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
    this.loadAppointments()
  },
  methods: {
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.switchTab({ url: '/pages/home/index' })
      }
    },
    async loadAppointments() {
      this.loading = true
      try {
        const res = await vetApi.getMyAppointments()
        if (res.success) {
          this.appointments = res.data || []
        }
      } catch (e) {
        console.error('加载预约列表失败:', e)
      } finally {
        this.loading = false
      }
    },
    getStatusLabel(status) {
      const map = { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' }
      return map[status] !== undefined ? map[status] : '未知'
    },
    getTabLabel(value) {
      if (value === 'all') return '全部'
      return this.getStatusLabel(value)
    },
    getTabCount(value) {
      if (value === 'all') return this.appointments.length
      return this.appointments.filter(item => item.status === value).length
    },
    formatDateTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      if (Number.isNaN(d.getTime())) return ''
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    showCancelModal(item) {
      this.cancelTarget = item
      this.cancelReason = ''
      this.showCancelPopup = true
    },
    hideCancelModal() {
      this.showCancelPopup = false
      this.cancelTarget = {}
      this.cancelReason = ''
    },
    async confirmCancel() {
      if (!this.cancelTarget.id) return
      try {
        const res = await vetApi.cancelAppointment(this.cancelTarget.id, this.cancelReason || undefined)
        if (res.success) {
          uni.showToast({ title: '取消成功', icon: 'success' })
          this.hideCancelModal()
          this.loadAppointments()
        }
      } catch (e) {
        uni.showToast({ title: e.message || '取消失败', icon: 'none' })
      }
    },
    goClinicDetail(item) {
      if (item.clinicId) {
        uni.navigateTo({ url: `/pages/vet/detail?id=${item.clinicId}` })
      }
    },
    goVetIndex() {
      uni.navigateTo({ url: '/pages/vet/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$primary-light: #ff8f6b;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.appointments-page { min-height: 100vh; background: $bg; }

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.status-bar { width: 100%; }

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
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back:active { background: rgba(255, 255, 255, 0.35); }

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title { font-size: 34rpx; font-weight: 700; color: #fff; }
.nav-placeholder { width: 64rpx; }

.page-scroll { height: 100vh; box-sizing: border-box; }
.page-content { padding: 20rpx 24rpx 40rpx; }

.tab-bar {
  display: flex;
  background: $card-bg;
  border-radius: 20rpx;
  padding: 8rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0;
  border-radius: 16rpx;
  position: relative;
  transition: all 0.3s;
}

.tab-item.active {
  background: linear-gradient(135deg, $primary, $primary-light);
}

.tab-text {
  font-size: 24rpx;
  color: $text-secondary;
  font-weight: 500;
}

.tab-item.active .tab-text {
  color: #fff;
  font-weight: 600;
}

.tab-count {
  margin-left: 6rpx;
  padding: 2rpx 10rpx;
  background: rgba(255, 106, 61, 0.15);
  border-radius: 20rpx;
}

.tab-item.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
}

.tab-count-text {
  font-size: 20rpx;
  color: $primary;
  font-weight: 600;
}

.tab-item.active .tab-count-text {
  color: #fff;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.appointment-card {
  background: $card-bg;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.clinic-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.clinic-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 122, 61, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.clinic-emoji { font-size: 28rpx; }

.clinic-meta {
  flex: 1;
  min-width: 0;
}

.clinic-name {
  font-size: 30rpx;
  font-weight: 700;
  color: $text-primary;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.appointment-time {
  font-size: 24rpx;
  color: $primary;
  font-weight: 500;
  display: block;
  margin-top: 4rpx;
}

.status-tag {
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.status-0 { background: #fff4e6; }
.status-1 { background: #dbeafe; }
.status-2 { background: #d1fae5; }
.status-3 { background: #f3f4f6; }

.status-text { font-size: 22rpx; font-weight: 500; }
.status-0 .status-text { color: #ff7a3d; }
.status-1 .status-text { color: #2563eb; }
.status-2 .status-text { color: #047857; }
.status-3 .status-text { color: #9ca3af; }

.card-body {
  padding: 16rpx 0;
  border-top: 1rpx solid #f3f4f6;
  border-bottom: 1rpx solid #f3f4f6;
}

.info-row {
  display: flex;
  align-items: flex-start;
  padding: 8rpx 0;
}

.info-label {
  font-size: 24rpx;
  color: $text-light;
  width: 140rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 24rpx;
  color: $text-primary;
  flex: 1;
}

.cancel-reason { color: #ef4444; }

.card-footer {
  padding-top: 20rpx;
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  padding: 12rpx 32rpx;
  border-radius: 999rpx;
  border: 2rpx solid #ef4444;
  background: #fff;
}

.cancel-btn:active { background: #fef2f2; }

.cancel-btn-text {
  font-size: 24rpx;
  color: #ef4444;
  font-weight: 500;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-emoji { font-size: 80rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 28rpx; color: $text-light; margin-bottom: 30rpx; }

.empty-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 48rpx;
  background: linear-gradient(135deg, $primary, $primary-light);
  border-radius: 999rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);
}

.empty-btn-text { font-size: 28rpx; font-weight: 600; color: #fff; }

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.loading-text { font-size: 28rpx; color: $text-light; }

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 80%;
  max-height: 80vh;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.modal-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }

.modal-close {
  font-size: 36rpx;
  color: #9ca3af;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body { padding: 32rpx; }

.cancel-info {
  background: #fef2f2;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.cancel-info-text {
  font-size: 26rpx;
  color: #991b1b;
  line-height: 1.6;
}

.form-group { margin-bottom: 0; }

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: 14rpx;
}

.label-text { font-size: 28rpx; font-weight: 600; color: $text-primary; }

.form-input {
  width: 100%;
  height: 160rpx;
  padding: 20rpx 24rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: $text-primary;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #f3f4f6;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-btn-cancel { background: #f3f4f6; }
.modal-btn-text-cancel { font-size: 28rpx; font-weight: 600; color: #6b7280; }

.modal-btn-confirm {
  background: #ef4444;
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
}

.modal-btn-text-confirm { font-size: 28rpx; font-weight: 600; color: #fff; }
</style>
