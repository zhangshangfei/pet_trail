<template>
  <view class="vaccine-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <text class="nav-back-icon">←</text>
        </view>
        <text class="nav-title">疫苗提醒</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="vaccine-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="vaccine-content">
        <view v-if="pet" class="pet-info-card">
          <image class="pet-info-avatar" :src="pet.avatar || ''" mode="aspectFill" />
          <view class="pet-info-meta">
            <text class="pet-info-name">{{ pet.name }}</text>
            <text class="pet-info-breed">{{ pet.breed || '' }}</text>
          </view>
        </view>

        <view class="section" v-if="upcomingReminders.length > 0">
          <view class="section-header">
            <view class="section-label">
              <text class="section-label-icon">⏰</text>
              <text class="section-label-text">即将到期</text>
            </view>
            <view class="badge">
              <text class="badge-text">{{ upcomingReminders.length }}</text>
            </view>
          </view>
          <view class="reminder-list">
            <view class="reminder-item" v-for="reminder in upcomingReminders" :key="reminder.id">
              <view class="reminder-icon-wrap"><text class="reminder-emoji">💉</text></view>
              <view class="reminder-content">
                <text class="reminder-name">{{ reminder.vaccineName }}</text>
                <text class="reminder-date">{{ formatDate(reminder.nextDate) }}</text>
                <text class="reminder-days">{{ reminder.daysUntil }} 天后</text>
              </view>
              <view class="reminder-actions">
                <view class="action-btn action-btn-done" @click="showStatusModal(reminder, 1)"><text class="action-btn-text">完成</text></view>
                <view class="action-btn action-btn-edit" @click="showEditModal(reminder)"><text class="action-btn-text">编辑</text></view>
              </view>
            </view>
          </view>
        </view>

        <view class="section" v-if="completedReminders.length > 0">
          <view class="section-header">
            <view class="section-label">
              <text class="section-label-icon">✅</text>
              <text class="section-label-text">已完成</text>
            </view>
            <view class="badge badge-green">
              <text class="badge-text badge-text-green">{{ completedReminders.length }}</text>
            </view>
          </view>
          <view class="reminder-list">
            <view class="reminder-item" v-for="reminder in completedReminders" :key="reminder.id">
              <view class="reminder-icon-wrap reminder-icon-wrap-done"><text class="reminder-emoji">✅</text></view>
              <view class="reminder-content">
                <text class="reminder-name reminder-name-done">{{ reminder.vaccineName }}</text>
                <text class="reminder-date reminder-date-done">{{ formatDate(reminder.nextDate) }}</text>
              </view>
              <view class="reminder-actions">
                <view class="action-btn action-btn-reset" @click="showStatusModal(reminder, 0)"><text class="action-btn-text">重置</text></view>
              </view>
            </view>
          </view>
        </view>

        <view class="add-section" @click="showAddModal">
          <view class="add-item">
            <text class="add-icon">＋</text>
            <text class="add-text">添加疫苗提醒</text>
          </view>
        </view>

        <view class="empty-section" v-if="upcomingReminders.length === 0 && completedReminders.length === 0">
          <text class="empty-emoji">💉</text>
          <text class="empty-text">暂无疫苗提醒</text>
          <view class="empty-btn" @click="showAddModal"><text class="empty-btn-text">添加第一个提醒</text></view>
        </view>
      </view>
    </scroll-view>

    <view class="modal-mask" v-if="showModal" @click="hideModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑提醒' : '添加提醒' }}</text>
          <text class="modal-close" @click="hideModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-group">
            <view class="form-label">
              <text class="label-emoji">💉</text>
              <text class="label-text">疫苗名称 *</text>
            </view>
            <input
              class="form-input"
              v-model="form.vaccineName"
              placeholder="请输入疫苗名称"
            />
          </view>
          <view class="form-group">
            <view class="form-label">
              <text class="label-emoji">📅</text>
              <text class="label-text">接种日期 *</text>
            </view>
            <picker
              mode="date"
              :value="form.nextDate"
              @change="onDateChange"
            >
              <view class="picker-value">
                <text v-if="form.nextDate" class="value-text">{{ formatDate(form.nextDate) }}</text>
                <text v-else class="picker-placeholder">请选择日期</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn modal-btn-cancel" @click="hideModal"><text class="modal-btn-text-cancel">取消</text></view>
          <view class="modal-btn modal-btn-confirm" @click="submitForm"><text class="modal-btn-text-confirm">确定</text></view>
        </view>
      </view>
    </view>

    <view class="modal-mask" v-if="showStatusModalFlag" @click="hideStatusModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">修改状态</text>
          <text class="modal-close" @click="hideStatusModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="status-options">
            <view
              class="status-option"
              :class="{ active: tempStatus === 1 }"
              @click="tempStatus = 1"
            >
              <text class="status-emoji">✅</text>
              <text class="status-label">已完成</text>
            </view>
            <view
              class="status-option"
              :class="{ active: tempStatus === 0 }"
              @click="tempStatus = 0"
            >
              <text class="status-emoji">⏰</text>
              <text class="status-label">未完成</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn modal-btn-cancel" @click="hideStatusModal"><text class="modal-btn-text-cancel">取消</text></view>
          <view class="modal-btn modal-btn-confirm" @click="updateStatus"><text class="modal-btn-text-confirm">确定</text></view>
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
      pet: null,
      upcomingReminders: [],
      completedReminders: [],
      showModal: false,
      showStatusModalFlag: false,
      isEditing: false,
      currentReminder: null,
      tempStatus: 0,
      form: {
        vaccineName: '',
        nextDate: ''
      }
    };
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
    } catch (e) {
      this.statusBarHeight = 20;
    }
    if (options.petId) {
      this.petId = options.petId;
      this.loadPetInfo();
      this.loadReminders();
    }
  },
  onShow() {
    if (this.petId) {
      this.loadReminders();
    }
  },
  methods: {
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.switchTab({ url: '/pages/dashboard/index' });
      }
    },
    async loadPetInfo() {
      try {
        const res = await uni.$request.get(`/api/pets/${this.petId}`);
        if (res.success) {
          this.pet = res.data;
        }
      } catch (error) {
        console.error('加载宠物信息失败:', error);
      }
    },
    async loadReminders() {
      try {
        const res = await uni.$request.get(`/api/pets/${this.petId}/vaccine-reminders`);
        if (res.success) {
          this.upcomingReminders = res.data.filter(r => r.status === 0);
          this.completedReminders = res.data.filter(r => r.status === 1);
          this.calculateDays();
        }
      } catch (error) {
        console.error('加载提醒失败:', error);
      }
    },
    calculateDays() {
      const now = new Date();
      this.upcomingReminders.forEach(r => {
        const target = new Date(r.nextDate);
        const diff = Math.ceil((target - now) / (1000 * 60 * 60 * 24));
        r.daysUntil = diff;
      });
    },
    formatDate(date) {
      if (!date) return '-';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },
    async showAddModal() {
      const loggedIn = await checkLogin('请先登录后再添加疫苗提醒')
      if (!loggedIn) return
      this.showModal = true;
      this.isEditing = false;
      this.form = {
        vaccineName: '',
        nextDate: ''
      };
    },
    showEditModal(reminder) {
      this.showModal = true;
      this.isEditing = true;
      this.currentReminder = reminder;
      this.form = {
        vaccineName: reminder.vaccineName,
        nextDate: reminder.nextDate
      };
    },
    showStatusModal(reminder, status) {
      this.showStatusModalFlag = true;
      this.currentReminder = reminder;
      this.tempStatus = status;
    },
    hideModal() {
      this.showModal = false;
      this.currentReminder = null;
    },
    hideStatusModal() {
      this.showStatusModalFlag = false;
      this.currentReminder = null;
    },
    onDateChange(e) {
      this.form.nextDate = e.detail.value;
    },
    async submitForm() {
      const loggedIn = await checkLogin('请先登录后再保存疫苗提醒')
      if (!loggedIn) return

      if (!this.form.vaccineName) {
        uni.showToast({ title: '请输入疫苗名称', icon: 'none' });
        return;
      }

      try {
        const res = this.isEditing
          ? await uni.$request.put(`/api/pets/${this.petId}/vaccine-reminders/${this.currentReminder.id}`, this.form)
          : await uni.$request.post(`/api/pets/${this.petId}/vaccine-reminders`, this.form);

        if (res.success) {
          uni.showToast({ title: this.isEditing ? '修改成功' : '添加成功', icon: 'success' });
          this.hideModal();
          this.loadReminders();
        } else {
          uni.showToast({ title: res.message || '操作失败', icon: 'none' });
        }
      } catch (error) {
        console.error('操作失败:', error);
        uni.showToast({ title: '网络错误', icon: 'none' });
      }
    },
    async updateStatus() {
      const loggedIn = await checkLogin('请先登录后再修改状态')
      if (!loggedIn) return

      try {
        const res = await uni.$request.put(`/api/pets/${this.petId}/vaccine-reminders/${this.currentReminder.id}/status`, {
          status: this.tempStatus
        });

        if (res.success) {
          uni.showToast({ title: '状态修改成功', icon: 'success' });
          this.hideStatusModal();
          this.loadReminders();
        } else {
          uni.showToast({ title: res.message || '修改失败', icon: 'none' });
        }
      } catch (error) {
        console.error('修改状态失败:', error);
        uni.showToast({ title: '网络错误', icon: 'none' });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.vaccine-page {
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
  padding: 0 16rpx;
}

.nav-back {
  width: 68rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-icon {
  font-size: 36rpx;
  color: #fff;
  font-weight: 600;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
}

.nav-placeholder {
  width: 68rpx;
}

.vaccine-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.vaccine-content {
  padding: 20rpx 24rpx 40rpx;
}

.pet-info-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  padding: 24rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.pet-info-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 44rpx;
  margin-right: 20rpx;
  background: #f3f4f6;
}

.pet-info-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
  display: block;
}

.pet-info-breed {
  font-size: 24rpx;
  color: #6b7280;
  display: block;
  margin-top: 4rpx;
}

.section {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-label {
  display: flex;
  align-items: center;
}

.section-label-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.section-label-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #111827;
}

.badge {
  padding: 4rpx 16rpx;
  background: rgba(255, 77, 79, 0.1);
  border-radius: 999rpx;
}

.badge-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #ff4d4f;
}

.badge-green {
  background: rgba(16, 185, 129, 0.1);
}

.badge-text-green {
  color: #10b981;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.reminder-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f9fafb;
  border-radius: 16rpx;
}

.reminder-icon-wrap {
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

.reminder-icon-wrap-done {
  background: rgba(16, 185, 129, 0.1);
}

.reminder-emoji {
  font-size: 28rpx;
}

.reminder-content {
  flex: 1;
  min-width: 0;
}

.reminder-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
  display: block;
  margin-bottom: 6rpx;
}

.reminder-name-done {
  text-decoration: line-through;
  color: #9ca3af;
}

.reminder-date {
  font-size: 24rpx;
  color: #6b7280;
  display: block;
}

.reminder-date-done {
  color: #d1d5db;
}

.reminder-days {
  font-size: 22rpx;
  color: #ff7a3d;
  font-weight: 600;
  margin-top: 4rpx;
  display: block;
}

.reminder-actions {
  display: flex;
  gap: 12rpx;
  margin-left: 12rpx;
  flex-shrink: 0;
}

.action-btn {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn-done {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
}

.action-btn-done .action-btn-text {
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
}

.action-btn-edit {
  background: #f3f4f6;
}

.action-btn-edit .action-btn-text {
  color: #6b7280;
  font-size: 22rpx;
  font-weight: 600;
}

.action-btn-reset {
  background: #f3f4f6;
}

.action-btn-reset .action-btn-text {
  color: #6b7280;
  font-size: 22rpx;
  font-weight: 600;
}

.add-section {
  margin-bottom: 20rpx;
}

.add-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  border: 2rpx dashed rgba(255, 122, 61, 0.3);
}

.add-icon {
  font-size: 40rpx;
  color: #ff7a3d;
  margin-right: 12rpx;
  font-weight: 600;
}

.add-text {
  font-size: 28rpx;
  color: #ff7a3d;
  font-weight: 600;
}

.empty-section {
  padding: 120rpx 0;
  text-align: center;
}

.empty-emoji {
  font-size: 80rpx;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9ca3af;
  display: block;
  margin-bottom: 30rpx;
}

.empty-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 48rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 999rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);
}

.empty-btn-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

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

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.modal-close {
  font-size: 36rpx;
  color: #9ca3af;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
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
  display: flex;
  align-items: center;
  margin-bottom: 14rpx;
}

.label-emoji {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.label-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  background: #f9fafb;
  border: none;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  min-height: 80rpx;
  font-size: 28rpx;
  color: #374151;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f9fafb;
  border: none;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  min-height: 80rpx;
}

.value-text {
  font-size: 28rpx;
  color: #374151;
}

.picker-placeholder {
  font-size: 28rpx;
  color: #9ca3af;
}

.picker-arrow {
  font-size: 20rpx;
  color: #9ca3af;
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

.modal-btn-cancel {
  background: #f3f4f6;
}

.modal-btn-text-cancel {
  font-size: 28rpx;
  font-weight: 600;
  color: #6b7280;
}

.modal-btn-confirm {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);
}

.modal-btn-text-confirm {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

.status-options {
  display: flex;
  gap: 20rpx;
}

.status-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 20rpx;
  transition: all 0.3s;
}

.status-option.active {
  border-color: #ff7a3d;
  background: rgba(255, 122, 61, 0.06);
}

.status-emoji {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.status-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #6b7280;
}

.status-option.active .status-label {
  color: #ff7a3d;
}
</style>
