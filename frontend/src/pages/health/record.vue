<template>
  <view class="record-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-back" @tap="goBack">
        <text class="header-back-icon">‹</text>
        <text class="header-back-text">返回</text>
      </view>
      <text class="page-title">体重记录</text>
      <text class="page-subtitle">{{ pet && pet.name ? pet.name : "请选择宠物" }}</text>
      <picker class="pet-picker" :range="petNames" :value="petIndex" @change="onPetChange">
        <view class="pet-picker-inner">
          <text class="pet-picker-text">{{ pet && pet.name ? pet.name : "选择宠物" }}</text>
          <text class="pet-picker-arrow">⌄</text>
        </view>
      </picker>
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
    if (options.petId || options.id) {
      this.petId = options.petId || options.id;
    }
    this.loadPets();
  },
  methods: {
    async loadPets() {
      const token = uni.getStorageSync('token');
      try {
        const res = await uni.request({
          url: "http://localhost:8080/api/pets",
          method: "GET",
          header: {
            'Authorization': `Bearer ${token}` || ""
          }
        });
        if (res.data && res.data.success && Array.isArray(res.data.data)) {
          this.pets = res.data.data;
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
    // 加载宠物信息
    async loadPetInfo() {
      const token = uni.getStorageSync('token');
      if (!this.petId) return;
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/pets/${this.petId}`,
          method: 'GET',
          header: {
            'Authorization': `Bearer ${token}` || ""
          }
        });
        if (res.data.success) {
          this.pet = res.data.data;
          this.currentWeight = this.pet.weight;
        }
      } catch (error) {
        console.error('加载宠物信息失败:', error);
      }
    },

    // 加载记录
    async loadRecords() {
      const token = uni.getStorageSync('token');
      if (!this.petId) return;
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/pets/${this.petId}/weight-records`,
          method: 'GET',
          header: {
            'Authorization': `Bearer ${token}` || ""
          }
        });
        if (res.data.success) {
          this.records = res.data.data;
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
    async onPetChange(e) {
      const idx = Number(e.detail.value || 0);
      const pet = this.pets[idx];
      if (!pet || !pet.id) return;
      this.petId = pet.id;
      await this.loadPetInfo();
      await this.loadRecords();
    },

    // 提交记录
    async submitRecord() {
      const token = uni.getStorageSync('token');
      if (!this.form.weight) {
        uni.showToast({
          title: '请输入体重',
          icon: 'none'
        });
        return;
      }

      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/pets/${this.petId}/weight-records`,
          method: 'POST',
          header: {
            'Authorization': `Bearer ${token}` || "",
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          data: {
            weight: this.form.weight,
            recordDate: this.form.recordDate
          }
        });

        if (res.data.success) {
          uni.showToast({
            title: '记录成功',
            icon: 'success'
          });
          this.hideModal();
          this.loadRecords();
          this.loadPetInfo(); // 刷新宠物信息
        } else {
          uni.showToast({
            title: res.data.message || '记录失败',
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
      uni.switchTab({
        url: `/pages/health/record-all?petId=${this.petId}`
      });
    },
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.navigateTo({ url: "/pages/dashboard/index" });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.record-page {
  min-height: 100vh;
  background-color: var(--pt-bg);
  padding-top: env(safe-area-inset-top);
}

.page-header {
  padding: 34rpx 30rpx 22rpx;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 55%, rgba(247, 243, 239, 0) 100%);
}

.header-back {
  display: inline-flex;
  align-items: center;
  margin-bottom: 10rpx;
  padding: 8rpx 12rpx 8rpx 0;
}

.header-back-icon {
  font-size: 42rpx;
  line-height: 32rpx;
  color: #ffffff;
  margin-right: 6rpx;
}

.header-back-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 700;
}

.pet-picker {
  margin-top: 12rpx;
  display: inline-block;
}

.pet-picker-inner {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.2);
}

.pet-picker-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 700;
}

.pet-picker-arrow {
  font-size: 20rpx;
  color: #ffffff;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
  display: block;
}

.page-subtitle {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  display: block;
  margin-top: 8rpx;
}

.input-card {
  margin: 20rpx;
  padding: 40rpx;
  background-color: var(--pt-card);
  border-radius: 24rpx;
  box-shadow: var(--pt-shadow-soft);
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
  color: var(--pt-primary);
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
  border-radius: 999rpx;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
}

.section {
  margin: 20rpx;
  padding: 30rpx;
  background-color: var(--pt-card);
  border-radius: 24rpx;
  box-shadow: var(--pt-shadow-soft);
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
  color: var(--pt-primary);
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