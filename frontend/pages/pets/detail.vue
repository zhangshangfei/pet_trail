<template>
  <view class="pet-detail">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">宠物详情</text>
      <button class="back-btn" size="mini" @click="goBack">返回</button>
    </view>

    <!-- 宠物信息卡片 -->
    <view class="pet-info-card">
      <view class="pet-avatar-large">
        <image v-if="pet.avatar" :src="pet.avatar" mode="aspectFill"></image>
        <view v-else class="avatar-placeholder-large">
          <text class="placeholder-text">{{ pet.name ? pet.name[0] : '宠' }}</text>
        </view>
      </view>
      <view class="pet-info">
        <view class="pet-name">{{ pet.name }}</view>
        <view class="pet-breed">{{ pet.breed || '未填写品种' }}</view>
        <view class="pet-meta">
          <view class="meta-item">
            <text class="meta-label">性别</text>
            <text class="meta-value">{{ getGenderText(pet.gender) }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">体重</text>
            <text class="meta-value">{{ pet.weight ? pet.weight + ' kg' : '-' }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">生日</text>
            <text class="meta-value">{{ formatDate(pet.birthday) }}</text>
          </view>
        </view>
      </view>
      <view class="pet-actions">
        <button class="action-btn" size="mini" @click="goToEdit">编辑</button>
        <button class="action-btn danger" size="mini" @click="showDeleteModal">删除</button>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-item" @click="goToWeightRecords">
        <view class="menu-icon">📈</view>
        <text class="menu-text">体重记录</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToVaccineReminders">
        <view class="menu-icon">💉</view>
        <text class="menu-text">疫苗提醒</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToParasiteReminders">
        <view class="menu-icon">🦠</view>
        <text class="menu-text">驱虫提醒</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 最近体重记录 -->
    <view class="section" v-if="recentWeightRecords.length > 0">
      <view class="section-header">
        <text class="section-title">最近体重记录</text>
        <text class="section-link" @click="goToWeightRecords">查看全部</text>
      </view>
      <view class="record-list">
        <view class="record-item" v-for="record in recentWeightRecords" :key="record.id">
          <view class="record-content">
            <text class="record-weight">{{ record.weight }} kg</text>
            <text class="record-date">{{ formatDate(record.recordDate) }}</text>
          </view>
          <view class="record-change" v-if="record.changePercent">
            <text :class="record.changePercent > 0 ? 'text-up' : 'text-down'">
              {{ record.changePercent > 0 ? '+' : '' }}{{ record.changePercent }}%
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 即将到期的提醒 -->
    <view class="section" v-if="upcomingReminders.length > 0">
      <view class="section-header">
        <text class="section-title">即将到期</text>
        <text class="section-link" @click="goToVaccineReminders">查看全部</text>
      </view>
      <view class="reminder-list">
        <view class="reminder-item" v-for="reminder in upcomingReminders" :key="reminder.id">
          <view class="reminder-icon">{{ reminder.type === 'vaccine' ? '💉' : '🦠' }}</view>
          <view class="reminder-content">
            <text class="reminder-name">{{ reminder.name }}</text>
            <text class="reminder-date">{{ formatDate(reminder.date) }}</text>
          </view>
          <view class="reminder-days">
            <text class="days-text">{{ reminder.daysUntil }} 天后</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-section" v-if="recentWeightRecords.length === 0 && upcomingReminders.length === 0">
      <text class="empty-text">暂无记录</text>
    </view>

    <!-- 删除确认弹窗 -->
    <view class="modal-mask" v-if="showDeleteModal" @click="hideDeleteModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">确认删除</text>
        </view>
        <view class="modal-body">
          <text class="modal-text">确定要删除宠物 "{{ pet.name }}" 吗？此操作不可恢复。</text>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="hideDeleteModal">取消</button>
          <button class="modal-btn danger" @click="deletePet">确定删除</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      pet: null,
      recentWeightRecords: [],
      upcomingReminders: [],
      showDeleteModal: false
    };
  },
  onLoad(options) {
    if (options.id) {
      this.loadPetDetail(options.id);
    }
  },
  methods: {
    // 加载宠物详情
    async loadPetDetail(petId) {
      try {
        const res = await uni.$request.get(`/api/pets/${petId}`);
        if (res.success) {
          this.pet = res.data;
          await this.loadRecentRecords();
          await this.loadUpcomingReminders();
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('加载宠物详情失败:', error);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    },

    // 加载最近体重记录
    async loadRecentRecords() {
      try {
        const res = await uni.$request.get(`/api/pets/${this.pet.id}/weight-records`);
        if (res.success) {
          this.recentWeightRecords = res.data.slice(0, 3);
          this.calculateChanges();
        }
      } catch (error) {
        console.error('加载体重记录失败:', error);
      }
    },

    // 加载即将到期的提醒
    async loadUpcomingReminders() {
      try {
        const [vaccineRes, parasiteRes] = await Promise.all([
          uni.$request.get(`/api/pets/${this.pet.id}/vaccine-reminders/upcoming`),
          uni.$request.get(`/api/pets/${this.pet.id}/parasite-reminders/upcoming`)
        ]);

        if (vaccineRes.success) {
          this.upcomingReminders = vaccineRes.data.map(r => ({
            ...r,
            type: 'vaccine'
          }));
        }

        if (parasiteRes.success) {
          this.upcomingReminders = [...this.upcomingReminders, ...parasiteRes.data.map(r => ({
            ...r,
            type: 'parasite'
          }))];
        }

        this.upcomingReminders.sort((a, b) => a.date - b.date);
      } catch (error) {
        console.error('加载提醒失败:', error);
      }
    },

    // 计算体重变化
    calculateChanges() {
      if (this.recentWeightRecords.length < 2) return;

      for (let i = 1; i < this.recentWeightRecords.length; i++) {
        const prev = this.recentWeightRecords[i - 1];
        const curr = this.recentWeightRecords[i];
        if (prev.weight && curr.weight) {
          const change = ((curr.weight - prev.weight) / prev.weight) * 100;
          this.$set(this.recentWeightRecords[i], 'changePercent', change.toFixed(1));
        }
      }
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '-';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    // 获取性别文本
    getGenderText(gender) {
      const map = {
        0: '未知',
        1: '公',
        2: '母'
      };
      return map[gender] || '未知';
    },

    // 返回
    goBack() {
      uni.navigateBack();
    },

    // 前往编辑
    goToEdit() {
      uni.navigateTo({
        url: `/pages/pets/edit?id=${this.pet.id}`
      });
    },

    // 前往体重记录
    goToWeightRecords() {
      uni.navigateTo({
        url: `/pages/health/record?petId=${this.pet.id}`
      });
    },

    // 前往疫苗提醒
    goToVaccineReminders() {
      uni.navigateTo({
        url: `/pages/health/vaccine?petId=${this.pet.id}`
      });
    },

    // 前往驱虫提醒
    goToParasiteReminders() {
      uni.navigateTo({
        url: `/pages/health/parasite?petId=${this.pet.id}`
      });
    },

    // 显示删除确认
    showDeleteModal() {
      this.showDeleteModal = true;
    },

    // 隐藏删除确认
    hideDeleteModal() {
      this.showDeleteModal = false;
    },

    // 删除宠物
    async deletePet() {
      try {
        const res = await uni.$request.delete(`/api/pets/${this.pet.id}`);

        if (res.success) {
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          });
          this.hideDeleteModal();
          setTimeout(() => {
            uni.navigateBack();
          }, 1000);
        } else {
          uni.showToast({
            title: res.message || '删除失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('删除宠物失败:', error);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.pet-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background-color: #ffffff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.back-btn {
  padding: 8rpx 24rpx;
  font-size: 24rpx;
}

.pet-info-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  margin: 20rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.pet-avatar-large {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 24rpx;
}

.pet-avatar-large image {
  width: 100%;
  height: 100%;
}

.avatar-placeholder-large {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder-large text {
  font-size: 64rpx;
  color: #ffffff;
  font-weight: bold;
}

.pet-info {
  flex: 1;
}

.pet-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8rpx;
}

.pet-breed {
  font-size: 26rpx;
  color: #999999;
  margin-bottom: 16rpx;
}

.pet-meta {
  display: flex;
  gap: 24rpx;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  font-size: 22rpx;
  color: #999999;
  margin-bottom: 4rpx;
}

.meta-value {
  font-size: 26rpx;
  color: #333333;
  font-weight: 500;
}

.pet-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  padding: 16rpx 32rpx;
  font-size: 24rpx;
  border-radius: 8rpx;
}

.action-btn.danger {
  background-color: #ffebee;
  color: #f44336;
}

.menu-section {
  margin: 20rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.menu-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
}

.menu-arrow {
  font-size: 48rpx;
  color: #cccccc;
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

.record-list,
.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.record-item,
.reminder-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
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

.reminder-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.reminder-content {
  flex: 1;
}

.reminder-name {
  font-size: 28rpx;
  color: #333333;
  display: block;
  margin-bottom: 8rpx;
}

.reminder-date {
  font-size: 24rpx;
  color: #999999;
}

.reminder-days {
  margin-left: 20rpx;
}

.days-text {
  font-size: 24rpx;
  color: #f5a623;
  padding: 4rpx 12rpx;
  background-color: #fff3e0;
  border-radius: 8rpx;
}

.empty-section {
  padding: 60rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
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
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.modal-body {
  padding: 30rpx;
}

.modal-text {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.6;
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

.modal-btn.danger {
  background-color: #f44336;
  color: #ffffff;
}
</style>