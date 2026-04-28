<template>
  <view class="record-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">体重记录</text>
      <text class="page-subtitle">{{ pet.name }}</text>
    </view>

    <!-- 体重输入卡片 -->
    <view class="input-card">
      <view class="input-label">当前体重</view>
      <view class="weight-display">
        <text class="weight-value">{{ currentWeight || '-' }}</text>
        <text class="weight-unit">kg</text>
      </view>
      <button class="record-btn" type="primary" @click="showRecordModal">
        + 记录体重
      </button>
    </view>

    <!-- 最近记录 -->
    <view class="section" v-if="records.length > 0">
      <view class="section-header">
        <text class="section-title">最近记录</text>
        <text class="section-link" @click="viewAllRecords">查看全部</text>
      </view>
      <view class="record-list">
        <view class="record-item" v-for="(record, index) in records" :key="record.id">
          <view class="record-icon">⚖️</view>
          <view class="record-content">
            <text class="record-weight">{{ record.weight }} kg</text>
            <text class="record-date">{{ formatDate(record.recordDate) }}</text>
          </view>
          <view class="record-change" v-if="index < records.length - 1 && records[index + 1].weight">
            <text :class="record.change > 0 ? 'text-up' : 'text-down'">
              {{ record.change > 0 ? '+' : '' }}{{ record.change }}%
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-section" v-if="records.length === 0">
      <text class="empty-text">暂无体重记录</text>
      <button class="add-btn" type="primary" @click="showRecordModal">添加第一条记录</button>
    </view>

    <!-- 记录弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="hideModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">记录体重</text>
          <text class="modal-close" @click="hideModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">体重 (kg)</text>
            <input
              class="form-input"
              type="digit"
              v-model="form.weight"
              placeholder="请输入体重"
              :keyboard-type="'number-pad'"
            />
          </view>
          <view class="form-item">
            <text class="form-label">记录日期</text>
            <picker
              mode="date"
              :value="form.recordDate"
              @change="onDateChange"
            >
              <view class="picker">
                <text v-if="form.recordDate">{{ formatDate(form.recordDate) }}</text>
                <text v-else class="picker-placeholder">请选择日期</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="hideModal">取消</button>
          <button class="modal-btn confirm" type="primary" @click="submitRecord">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      petId: null,
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
  onLoad(options) {
    if (options.petId || options.id) {
      this.petId = options.petId || options.id;
      this.loadPetInfo();
      this.loadRecords();
    }
  },
  methods: {
    // 加载宠物信息
    async loadPetInfo() {
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

    // 加载记录
    async loadRecords() {
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

    // 计算变化
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

    // 格式化日期
    formatDate(date) {
      if (!date) return '-';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    // 显示弹窗
    showRecordModal() {
      this.showModal = true;
      this.form.recordDate = this.form.recordDate || new Date().toISOString().split('T')[0];
    },

    // 隐藏弹窗
    hideModal() {
      this.showModal = false;
    },

    // 日期变化
    onDateChange(e) {
      this.form.recordDate = e.detail.value;
    },

    // 提交记录
    async submitRecord() {
      if (!this.form.weight) {
        uni.showToast({
          title: '请输入体重',
          icon: 'none'
        });
        return;
      }

      try {
        const res = await uni.$request.post(`/api/pets/${this.petId}/weight-records`, {
          weight: this.form.weight,
          recordDate: this.form.recordDate
        }, {
          'Content-Type': 'application/x-www-form-urlencoded'
        });

        if (res.success) {
          uni.showToast({
            title: '记录成功',
            icon: 'success'
          });
          this.hideModal();
          this.loadRecords();
          this.loadPetInfo(); // 刷新宠物信息
        } else {
          uni.showToast({
            title: res.message || '记录失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('提交记录失败:', error);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    },

    // 查看全部
    viewAllRecords() {
      uni.navigateTo({
        url: `/pages/health/record-all?petId=${this.petId}`
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.record-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  padding: 30rpx;
  background-color: #ffffff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.page-subtitle {
  font-size: 24rpx;
  color: #999999;
  display: block;
  margin-top: 8rpx;
}

.input-card {
  margin: 20rpx;
  padding: 40rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  text-align: center;
}

.input-label {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 30rpx;
  display: block;
}

.weight-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 30rpx;
}

.weight-value {
  font-size: 80rpx;
  font-weight: bold;
  color: #667eea;
}

.weight-unit {
  font-size: 32rpx;
  color: #999999;
  margin-left: 8rpx;
}

.record-btn {
  width: 100%;
  padding: 24rpx;
  font-size: 32rpx;
  border-radius: 12rpx;
}

.section {
  margin: 20rpx;
  padding: 30rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.section-link {
  font-size: 24rpx;
  color: #667eea;
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
  background-color: #f8f8f8;
  border-radius: 12rpx;
}

.record-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.record-content {
  flex: 1;
}

.record-weight {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
  display: block;
  margin-bottom: 8rpx;
}

.record-date {
  font-size: 24rpx;
  color: #999999;
}

.record-change {
  margin-left: 20rpx;
}

.text-up {
  font-size: 24rpx;
  color: #4caf50;
}

.text-down {
  font-size: 24rpx;
  color: #f44336;
}

.empty-section {
  padding: 100rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
  display: block;
  margin-bottom: 30rpx;
}

.add-btn {
  width: 200rpx;
  padding: 24rpx;
  font-size: 28rpx;
  border-radius: 12rpx;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 80%;
  max-height: 80vh;
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.modal-close {
  font-size: 40rpx;
  color: #999999;
  cursor: pointer;
}

.modal-body {
  padding: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 12rpx;
  display: block;
}

.form-input {
  width: 100%;
  padding: 20rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.picker {
  width: 100%;
  padding: 20rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333333;
}

.picker-placeholder {
  color: #999999;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  padding: 24rpx;
  font-size: 28rpx;
  border-radius: 12rpx;
}

.modal-btn.cancel {
  background-color: #f5f5f5;
  color: #666666;
}

.modal-btn.confirm {
  background-color: #667eea;
  color: #ffffff;
}
</style>