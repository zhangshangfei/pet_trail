<template>
  <view class="dashboard">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">健康看板</text>
    </view>

    <!-- 总览卡片 -->
    <view class="overview-cards">
      <view class="card">
        <view class="card-icon">🐾</view>
        <view class="card-value">{{ pets.length }}</view>
        <view class="card-label">宠物数量</view>
      </view>
      <view class="card">
        <view class="card-icon">📅</view>
        <view class="card-value">{{ upcomingReminders.length }}</view>
        <view class="card-label">即将到期</view>
      </view>
      <view class="card">
        <view class="card-icon">📈</view>
        <view class="card-value">{{ weightRecords.length }}</view>
        <view class="card-label">体重记录</view>
      </view>
    </view>

    <!-- 宠物卡片列表 -->
    <view class="pet-section" v-for="pet in pets" :key="pet.id">
      <view class="section-header">
        <view class="section-title">
          <image v-if="pet.avatar" :src="pet.avatar" class="pet-avatar-small"></image>
          <view v-else class="avatar-placeholder-small">{{ pet.name ? pet.name[0] : '宠' }}</view>
          <text class="pet-name">{{ pet.name }}</text>
        </view>
        <view class="pet-actions">
          <button class="action-btn" size="mini" @click="goToDetail(pet.id)">详情</button>
          <button class="action-btn" size="mini" type="primary" @click="goToAddRecord(pet.id)">记录</button>
        </view>
      </view>

      <!-- 宠物信息 -->
      <view class="pet-info-grid">
        <view class="info-item">
          <text class="info-label">品种</text>
          <text class="info-value">{{ pet.breed || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">性别</text>
          <text class="info-value">{{ getGenderText(pet.gender) }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">体重</text>
          <text class="info-value">{{ pet.weight ? pet.weight + ' kg' : '-' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">生日</text>
          <text class="info-value">{{ formatDate(pet.birthday) }}</text>
        </view>
      </view>

      <!-- 体重趋势图 -->
      <view class="chart-section" v-if="petWeightRecords[pet.id] && petWeightRecords[pet.id].length > 0">
        <view class="chart-header">
          <text class="chart-title">体重趋势</text>
          <view class="chart-tips">
            <text class="tip">最近 7 天</text>
          </view>
        </view>
        <view class="chart-container">
          <view class="chart-placeholder">
            <text class="placeholder-text">体重趋势图占位</text>
          </view>
        </view>
      </view>

      <!-- 疫苗提醒 -->
      <view class="reminders-section" v-if="vaccineReminders[pet.id] && vaccineReminders[pet.id].length > 0">
        <view class="reminders-header">
          <text class="reminders-title">疫苗提醒</text>
          <button class="view-all-btn" size="mini" @click="goToVaccineReminders(pet.id)">查看全部</button>
        </view>
        <view class="reminder-list">
          <view class="reminder-item" v-for="reminder in vaccineReminders[pet.id].slice(0, 2)" :key="reminder.id">
            <view class="reminder-icon">
              <text>{{ reminder.status === 1 ? '✅' : '⏰' }}</text>
            </view>
            <view class="reminder-content">
              <text class="reminder-name">{{ reminder.vaccineName }}</text>
              <text class="reminder-date">{{ formatDate(reminder.nextDate) }}</text>
            </view>
            <view class="reminder-status">
              <text class="status-text">{{ reminder.status === 1 ? '已接种' : '未接种' }}</text>
              <text class="status-badge" :class="{ 'active': reminder.status === 0 }">
                {{ reminder.status === 1 ? '已完成' : '待接种' }}
              </text>
            </view>
          </view>
        </view>
      </view>

    <!-- 空状态 -->
    <view class="empty-section" v-if="recentWeightRecords.length === 0 && upcomingReminders.length === 0">
      <text class="empty-text">暂无疫苗提醒</text>
    </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      pets: [],
      petWeightRecords: {},
      vaccineReminders: {}
    };
  },
  computed: {
    upcomingReminders() {
      let count = 0;
      for (const petId in this.vaccineReminders) {
        count += this.vaccineReminders[petId].filter(r => r.status === 0 && this.daysUntil(r.nextDate) <= 7).length;
      }
      return count;
    },
    weightRecords() {
      let count = 0;
      for (const petId in this.petWeightRecords) {
        count += this.petWeightRecords[petId].length;
      }
      return count;
    }
  },
  onLoad() {
    this.loadData();
  },
  methods: {
    // 加载数据
    async loadData() {
      await Promise.all([
        this.loadPets(),
        this.loadWeightRecords(),
        this.loadVaccineReminders()
      ]);
    },

    // 加载宠物列表
    async loadPets() {
      try {
        const res = await uni.$request.get('/api/pets');
        if (res.success) {
          this.pets = res.data;
        }
      } catch (error) {
        console.error('加载宠物列表失败:', error);
      }
    },

    // 加载体重记录
    async loadWeightRecords() {
      try {
        for (const pet of this.pets) {
          const res = await uni.$request.get(`/api/pets/${pet.id}/weight-records`);
          if (res.success) {
            this.$set(this.petWeightRecords, pet.id, res.data);
          }
        }
      } catch (error) {
        console.error('加载体重记录失败:', error);
      }
    },

    // 加载疫苗提醒
    async loadVaccineReminders() {
      try {
        for (const pet of this.pets) {
          const res = await uni.$request.get(`/api/pets/${pet.id}/vaccine-reminders`);
          if (res.success) {
            this.$set(this.vaccineReminders, pet.id, res.data);
          }
        }
      } catch (error) {
        console.error('加载疫苗提醒失败:', error);
      }
    },

    // 跳转到宠物详情
    goToDetail(petId) {
      uni.navigateTo({
        url: `/pages/pets/detail?id=${petId}`
      });
    },

    // 跳转到添加记录
    goToAddRecord() {
      uni.switchTab({
        url: "/pages/health/index"
      });
    },

    // 跳转到疫苗提醒
    goToVaccineReminders(petId) {
      uni.navigateTo({
        url: `/pages/health/vaccine?id=${petId}`
      });
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

    // 计算距离天数
    daysUntil(dateStr) {
      const target = new Date(dateStr);
      const now = new Date();
      const diff = Math.ceil((target - now) / (1000 * 60 * 60 * 24));
      return diff;
    }
  }
};
</script>

<style lang="scss" scoped>
.dashboard {
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
}

.overview-cards {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
}

.card {
  flex: 1;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  text-align: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.card-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.card-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8rpx;
}

.card-label {
  font-size: 24rpx;
  color: #999999;
}

.pet-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.pet-avatar-small {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.avatar-placeholder-small {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder-small text {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: bold;
}

.pet-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.pet-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  font-size: 24rpx;
}

.pet-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 20rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 24rpx;
  color: #999999;
  margin-bottom: 8rpx;
}

.info-value {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
}

.chart-section {
  margin-bottom: 24rpx;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.chart-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.chart-tips {
  display: flex;
  gap: 12rpx;
}

.tip {
  font-size: 24rpx;
  color: #999999;
  padding: 4rpx 12rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
}

.chart-container {
  height: 300rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
}

.placeholder-text {
  font-size: 28rpx;
  color: #999999;
}

.reminders-section {
  margin-bottom: 24rpx;
}

.reminders-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.reminders-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.view-all-btn {
  font-size: 24rpx;
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
  background-color: #f8f8f8;
  border-radius: 12rpx;
}

.reminder-icon {
  font-size: 32rpx;
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

.reminder-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.status-text {
  font-size: 24rpx;
  color: #666666;
}

.status-badge {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  color: #ffffff;
}

.status-badge.active {
  background-color: #f5a623;
}

.empty-section {
  padding: 40rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
}
</style>