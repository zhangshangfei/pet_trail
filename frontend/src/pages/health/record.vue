<template>
  <view class="record-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <text class="nav-back-icon">←</text>
        </view>
        <text class="nav-title">体重记录</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="record-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="record-content">
        <view v-if="pet" class="pet-info-card">
          <image class="pet-info-avatar" :src="pet.avatar || ''" mode="aspectFill" />
          <view class="pet-info-meta">
            <text class="pet-info-name">{{ pet.name }}</text>
            <text class="pet-info-breed">{{ pet.breed || '' }}</text>
          </view>
          <picker class="pet-picker" :range="petNames" :value="petIndex" @change="onPetChange">
            <view class="pet-picker-inner">
              <text class="pet-picker-text">切换</text>
              <text class="pet-picker-arrow">▼</text>
            </view>
          </picker>
        </view>

        <view class="weight-card">
          <view class="weight-label">当前体重</view>
          <view class="weight-display">
            <text class="weight-value">{{ currentWeight || '-' }}</text>
            <text class="weight-unit">kg</text>
          </view>
          <view class="weight-btn-wrap" @click="showRecordModal">
            <text class="weight-btn-text">+ 记录体重</text>
          </view>
        </view>

        <view class="section" v-if="records.length > 0">
          <view class="section-header">
            <view class="section-label">
              <text class="section-label-icon">📊</text>
              <text class="section-label-text">最近记录</text>
            </view>
            <text class="section-link" @click="viewAllRecords">查看全部</text>
          </view>
          <view class="record-list">
            <view class="record-item" v-for="(record, index) in records" :key="record.id">
              <view class="record-icon-wrap"><text class="record-emoji">⚖️</text></view>
              <view class="record-info">
                <text class="record-weight">{{ record.weight }} kg</text>
                <text class="record-date">{{ formatDate(record.recordDate) }}</text>
              </view>
              <view class="record-change" v-if="index < records.length - 1 && records[index + 1].weight">
                <text class="change-text" :class="record.change > 0 ? 'up' : 'down'">
                  {{ record.change > 0 ? '+' : '' }}{{ record.change }}%
                </text>
              </view>
            </view>
          </view>
        </view>

        <view class="empty-section" v-if="records.length === 0">
          <text class="empty-emoji">⚖️</text>
          <text class="empty-text">暂无体重记录</text>
          <view class="empty-btn" @click="showRecordModal"><text class="empty-btn-text">添加第一条记录</text></view>
        </view>
      </view>
    </scroll-view>

    <view class="modal-mask" v-if="showModal" @click="hideModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">记录体重</text>
          <text class="modal-close" @click="hideModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-group">
            <view class="form-label">
              <text class="label-emoji">⚖️</text>
              <text class="label-text">体重 (kg)</text>
            </view>
            <input
              class="form-input"
              type="digit"
              v-model="form.weight"
              placeholder="请输入体重"
            />
          </view>
          <view class="form-group">
            <view class="form-label">
              <text class="label-emoji">📅</text>
              <text class="label-text">记录日期</text>
            </view>
            <picker
              mode="date"
              :value="form.recordDate"
              @change="onDateChange"
            >
              <view class="picker-value">
                <text v-if="form.recordDate" class="value-text">{{ formatDate(form.recordDate) }}</text>
                <text v-else class="picker-placeholder">请选择日期</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn modal-btn-cancel" @click="hideModal"><text class="modal-btn-text-cancel">取消</text></view>
          <view class="modal-btn modal-btn-confirm" @click="submitRecord"><text class="modal-btn-text-confirm">确定</text></view>
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
      pets: [],
      pet: null,
      records: [],
      currentWeight: null,
      showModal: false,
      form: {
        weight: '',
        recordDate: ''
      }
    };
  },
  computed: {
    petNames() {
      return this.pets.map((p) => p.name || "未命名宠物");
    },
    petIndex() {
      if (!this.petId) return 0;
      const idx = this.pets.findIndex((p) => String(p.id) === String(this.petId));
      return idx >= 0 ? idx : 0;
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
    } catch (e) {
      this.statusBarHeight = 20;
    }
    if (options.petId || options.id) {
      this.petId = options.petId || options.id;
    }
    this.loadPets();
  },
  onShow() {
    if (this.petId) {
      this.loadPetInfo();
      this.loadRecords();
    } else {
      this.loadPets();
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
    async loadPets() {
      try {
        const res = await uni.$request.get('/api/pets');
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data;
          if (!this.pets.length) {
            this.pet = null;
            this.records = [];
            this.currentWeight = null;
            return;
          }
          const hasPetId = this.pets.some((p) => String(p.id) === String(this.petId));
          this.petId = hasPetId ? this.petId : this.pets[0].id;
          await this.loadPetInfo();
          await this.loadRecords();
        }
      } catch (error) {
        console.error("加载宠物列表失败:", error);
      }
    },
    async loadPetInfo() {
      if (!this.petId) return;
      try {
        const res = await uni.$request.get(`/api/pets/${this.petId}`);
        if (res.success) {
          this.pet = res.data;
          this.currentWeight = this.pet.weight;
        }
      } catch (error) {
        console.error('加载宠物信息失败:', error);
      }
    },
    async loadRecords() {
      if (!this.petId) return;
      try {
        const res = await uni.$request.get(`/api/pets/${this.petId}/weight-records`);
        if (res.success) {
          this.records = res.data;
          this.calculateChanges();
        }
      } catch (error) {
        console.error('加载记录失败:', error);
      }
    },
    calculateChanges() {
      if (this.records.length < 2) return;
      for (let i = 1; i < this.records.length; i++) {
        const prev = this.records[i - 1];
        const curr = this.records[i];
        if (prev.weight && curr.weight) {
          const change = ((curr.weight - prev.weight) / prev.weight) * 100;
          this.$set(this.records[i], 'change', change.toFixed(1));
        }
      }
    },
    formatDate(date) {
      if (!date) return '-';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },
    async showRecordModal() {
      const loggedIn = await checkLogin('请先登录后再记录体重')
      if (!loggedIn) return
      this.showModal = true;
      this.form.recordDate = this.form.recordDate || new Date().toISOString().split('T')[0];
    },
    hideModal() {
      this.showModal = false;
    },
    onDateChange(e) {
      this.form.recordDate = e.detail.value;
    },
    async onPetChange(e) {
      const idx = Number(e.detail.value || 0);
      const pet = this.pets[idx];
      if (!pet || !pet.id) return;
      this.petId = pet.id;
      await this.loadPetInfo();
      await this.loadRecords();
    },
    async submitRecord() {
      const loggedIn = await checkLogin('请先登录后再记录体重')
      if (!loggedIn) return

      if (!this.form.weight) {
        uni.showToast({ title: '请输入体重', icon: 'none' });
        return;
      }

      const todayRecord = this.records.find(r => {
        const recordDate = new Date(r.recordDate);
        const recordDateStr = `${recordDate.getFullYear()}-${String(recordDate.getMonth() + 1).padStart(2, '0')}-${String(recordDate.getDate()).padStart(2, '0')}`;
        return recordDateStr === this.form.recordDate;
      });

      if (todayRecord) {
        uni.showModal({
          title: '提示',
          content: `当天已有体重记录（${todayRecord.weight}kg），是否修改？`,
          confirmText: '确认修改',
          cancelText: '取消',
          success: async (modalRes) => {
            if (modalRes.confirm) {
              await this.updateRecord(todayRecord.id);
            }
          }
        });
        return;
      }

      await this.createNewRecord();
    },
    async createNewRecord() {
      try {
        const res = await uni.$request.post(`/api/pets/${this.petId}/weight-records`, {
          weight: this.form.weight,
          recordDate: this.form.recordDate
        });

        if (res && res.success) {
          uni.showToast({ title: '记录成功', icon: 'success' });
          this.hideModal();
          this.loadRecords();
          this.loadPetInfo();
        } else {
          uni.showToast({ title: (res && res.message) || '记录失败', icon: 'none' });
        }
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '网络错误', icon: 'none' });
      }
    },
    async updateRecord(recordId) {
      try {
        const res = await uni.$request.put(`/api/pets/${this.petId}/weight-records/${recordId}`, {
          weight: this.form.weight,
          recordDate: this.form.recordDate
        });

        if (res && res.success) {
          uni.showToast({ title: '修改成功', icon: 'success' });
          this.hideModal();
          this.loadRecords();
          this.loadPetInfo();
        } else {
          uni.showToast({ title: (res && res.message) || '修改失败', icon: 'none' });
        }
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '网络错误', icon: 'none' });
      }
    },
    viewAllRecords() {
      uni.switchTab({
        url: `/pages/health/record-all?petId=${this.petId}`
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.record-page {
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

.record-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.record-content {
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
  flex-shrink: 0;
}

.pet-info-meta {
  flex: 1;
  min-width: 0;
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

.pet-picker {
  flex-shrink: 0;
}

.pet-picker-inner {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 20rpx;
  background: #f9fafb;
  border-radius: 999rpx;
}

.pet-picker-text {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 600;
}

.pet-picker-arrow {
  font-size: 18rpx;
  color: #9ca3af;
}

.weight-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  padding: 40rpx 32rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.weight-label {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 20rpx;
  display: block;
}

.weight-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 32rpx;
}

.weight-value {
  font-size: 80rpx;
  font-weight: 800;
  color: #ff7a3d;
  line-height: 1;
}

.weight-unit {
  font-size: 32rpx;
  color: #9ca3af;
  margin-left: 8rpx;
}

.weight-btn-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 64rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 999rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);
}

.weight-btn-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
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

.section-link {
  font-size: 24rpx;
  color: #ff7a3d;
  font-weight: 600;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f9fafb;
  border-radius: 16rpx;
}

.record-icon-wrap {
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

.record-emoji {
  font-size: 28rpx;
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-weight {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
  display: block;
  margin-bottom: 4rpx;
}

.record-date {
  font-size: 24rpx;
  color: #6b7280;
  display: block;
}

.record-change {
  margin-left: 16rpx;
  flex-shrink: 0;
}

.change-text {
  font-size: 24rpx;
  font-weight: 600;
}

.change-text.up {
  color: #10b981;
}

.change-text.down {
  color: #ff4d4f;
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
</style>
