<template>
  <view class="vaccine-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">疫苗提醒</text>
      <text class="page-subtitle">{{ pet ? pet.name : '' }}</text>
    </view>

    <!-- 加载中 -->
    <view class="loading-section" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 有数据时显示提醒列表 -->
    <template v-else-if="pet && (upcomingReminders.length > 0 || completedReminders.length > 0)">
      <!-- 即将到期提醒 -->
      <view class="section" v-if="upcomingReminders.length > 0">
        <view class="section-header">
          <text class="section-title">即将到期</text>
          <view class="badge">
            <text class="badge-text">{{ upcomingReminders.length }}</text>
          </view>
        </view>
        <view class="reminder-list">
          <view class="reminder-item" v-for="reminder in upcomingReminders" :key="reminder.id">
            <view class="reminder-icon">💉</view>
            <view class="reminder-content">
              <text class="reminder-name">{{ reminder.vaccineName }}</text>
              <text class="reminder-date">{{ formatDate(reminder.nextDate) }}</text>
              <text class="reminder-days">{{ reminder.daysUntil }} 天后</text>
            </view>
            <view class="reminder-actions">
              <button class="action-btn" size="mini" @click="showStatusModal(reminder, 1)">完成</button>
              <button class="action-btn" size="mini" type="primary" @click="showEditModal(reminder)">编辑</button>
            </view>
          </view>
        </view>
      </view>

      <!-- 已完成提醒 -->
      <view class="section" v-if="completedReminders.length > 0">
        <view class="section-header">
          <text class="section-title">已完成</text>
          <view class="badge completed">
            <text class="badge-text">{{ completedReminders.length }}</text>
          </view>
        </view>
        <view class="reminder-list">
          <view class="reminder-item" v-for="reminder in completedReminders" :key="reminder.id">
            <view class="reminder-icon completed">✅</view>
            <view class="reminder-content">
              <text class="reminder-name completed">{{ reminder.vaccineName }}</text>
              <text class="reminder-date completed">{{ formatDate(reminder.nextDate) }}</text>
            </view>
            <view class="reminder-actions">
              <button class="action-btn" size="mini" @click="showStatusModal(reminder, 0)">重置</button>
            </view>
          </view>
        </view>
      </view>
    </template>

    <!-- 空状态（没有数据时显示） -->
    <view class="empty-section" v-if="pet && upcomingReminders.length === 0 && completedReminders.length === 0">
      <text class="empty-text">暂无疫苗提醒</text>
      <button class="add-btn" type="primary" @click="showAddModal">添加第一个提醒</button>
    </view>

    <!-- 添加提醒按钮（有数据时显示） -->
    <view class="add-section" v-if="pet && (upcomingReminders.length > 0 || completedReminders.length > 0)">
      <view class="add-item" @click="showAddModal">
        <view class="add-icon">➕</view>
        <text class="add-text">添加疫苗提醒</text>
      </view>
    </view>

    <!-- 添加/编辑弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="hideModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEditing ? '编辑提醒' : '添加提醒' }}</text>
          <text class="modal-close" @click="hideModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">疫苗名称 *</text>
            <input
              class="form-input"
              v-model="form.vaccineName"
              placeholder="请输入疫苗名称"
            />
          </view>
          <view class="form-item">
            <text class="form-label">接种日期 *</text>
            <picker
              mode="date"
              :value="form.nextDate"
              @change="onDateChange"
            >
              <view class="picker">
                <text v-if="form.nextDate">{{ formatDate(form.nextDate) }}</text>
                <text v-else class="picker-placeholder">请选择日期</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="hideModal">取消</button>
          <button class="modal-btn confirm" type="primary" @click="submitForm">确定</button>
        </view>
      </view>
    </view>

    <!-- 状态修改弹窗 -->
    <view class="modal-mask" v-if="showStatusModal" @click="hideStatusModal">
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
              <view class="status-icon">✅</view>
              <text class="status-text">已完成</text>
            </view>
            <view
              class="status-option"
              :class="{ active: tempStatus === 0 }"
              @click="tempStatus = 0"
            >
              <view class="status-icon">⏰</view>
              <text class="status-text">未完成</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="hideStatusModal">取消</button>
          <button class="modal-btn confirm" type="primary" @click="updateStatus">确定</button>
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
      upcomingReminders: [],
      completedReminders: [],
      loading: true,
      showModal: false,
      showStatusModal: false,
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
    if (options.petId) {
      this.petId = options.petId;
      this.loadData();
    }
  },
  onShow() {
    if (this.petId) {
      this.loadData();
    }
  },
  methods: {
    // 统一加载所有数据
    async loadData() {
      this.loading = true;
      try {
        // 并行加载宠物信息和提醒数据
        const [petRes, reminderRes] = await Promise.all([
          uni.request({
            url: `http://localhost:8080/api/pets/${this.petId}`,
            method: 'GET',
            header: {
              'Authorization': uni.getStorageSync('token') || ''
            }
          }),
          uni.request({
            url: `http://localhost:8080/api/pets/${this.petId}/vaccine-reminders`,
            method: 'GET',
            header: {
              'Authorization': uni.getStorageSync('token') || ''
            }
          })
        ]);

        // 一次性更新所有数据，避免多次渲染
        if (petRes.data.success) {
          this.pet = petRes.data.data;
        }
        
        if (reminderRes.data.success) {
          const data = reminderRes.data.data;
          this.upcomingReminders = data.filter(r => r.status === 0);
          this.completedReminders = data.filter(r => r.status === 1);
          this.calculateDays();
        }
      } catch (error) {
        console.error('加载数据失败:', error);
      } finally {
        this.loading = false;
      }
    },

    // 加载宠物信息（保留但不再单独使用）
    async loadPetInfo() {
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/pets/${this.petId}`,
          method: 'GET',
          header: {
            'Authorization': uni.getStorageSync('token') || ''
          }
        });
        if (res.data.success) {
          this.pet = res.data.data;
        }
      } catch (error) {
        console.error('加载宠物信息失败:', error);
      }
    },

    // 加载提醒（保留但不再单独使用）
    async loadReminders() {
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/pets/${this.petId}/vaccine-reminders`,
          method: 'GET',
          header: {
            'Authorization': uni.getStorageSync('token') || ''
          }
        });
        if (res.data.success) {
          this.upcomingReminders = res.data.data.filter(r => r.status === 0);
          this.completedReminders = res.data.data.filter(r => r.status === 1);
          this.calculateDays();
        }
      } catch (error) {
        console.error('加载提醒失败:', error);
      }
    },

    // 计算天数
    calculateDays() {
      const now = new Date();
      this.upcomingReminders.forEach(r => {
        const target = new Date(r.nextDate);
        const diff = Math.ceil((target - now) / (1000 * 60 * 60 * 24));
        r.daysUntil = diff;
      });
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '-';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    // 显示添加弹窗
    showAddModal() {
      this.showModal = true;
      this.isEditing = false;
      this.form = {
        vaccineName: '',
        nextDate: ''
      };
    },

    // 显示编辑弹窗
    showEditModal(reminder) {
      this.showModal = true;
      this.isEditing = true;
      this.currentReminder = reminder;
      this.form = {
        vaccineName: reminder.vaccineName,
        nextDate: reminder.nextDate
      };
    },

    // 显示状态弹窗
    showStatusModal(reminder, status) {
      this.showStatusModal = true;
      this.currentReminder = reminder;
      this.tempStatus = status;
    },

    // 隐藏弹窗
    hideModal() {
      this.showModal = false;
      this.currentReminder = null;
    },

    // 隐藏状态弹窗
    hideStatusModal() {
      this.showStatusModal = false;
      this.currentReminder = null;
    },

    // 日期变化
    onDateChange(e) {
      this.form.nextDate = e.detail.value;
    },

    // 提交表单
    async submitForm() {
      if (!this.form.vaccineName) {
        uni.showToast({
          title: '请输入疫苗名称',
          icon: 'none'
        });
        return;
      }

      try {
        const url = this.isEditing
          ? `http://localhost:8080/api/pets/${this.petId}/vaccine-reminders/${this.currentReminder.id}`
          : `http://localhost:8080/api/pets/${this.petId}/vaccine-reminders`;

        const res = await uni.request({
          url: url,
          method: this.isEditing ? 'PUT' : 'POST',
          header: {
            'Authorization': uni.getStorageSync('token') || '',
            'Content-Type': 'application/json'
          },
          data: this.form
        });

        if (res.data.success) {
          uni.showToast({
            title: this.isEditing ? '修改成功' : '添加成功',
            icon: 'success'
          });
          this.hideModal();
          this.loadReminders();
        } else {
          uni.showToast({
            title: res.data.message || '操作失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('操作失败:', error);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    },

    // 更新状态
    async updateStatus() {
      try {
        const res = await uni.request({
          url: `http://localhost:8080/api/pets/${this.petId}/vaccine-reminders/${this.currentReminder.id}/status`,
          method: 'PUT',
          header: {
            'Authorization': uni.getStorageSync('token') || '',
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          data: {
            status: this.tempStatus
          }
        });

        if (res.data.success) {
          uni.showToast({
            title: '状态修改成功',
            icon: 'success'
          });
          this.hideStatusModal();
          this.loadReminders();
        } else {
          uni.showToast({
            title: res.data.message || '修改失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('修改状态失败:', error);
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
.vaccine-page {
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

.loading-section {
  padding: 100rpx 0;
  text-align: center;
}

.loading-text {
  font-size: 28rpx;
  color: #999999;
  display: block;
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

.badge {
  padding: 4rpx 16rpx;
  background-color: #ffebee;
  border-radius: 20rpx;
}

.badge-text {
  font-size: 24rpx;
  color: #f44336;
}

.badge.completed {
  background-color: #e8f5e9;
}

.badge.completed .badge-text {
  color: #4caf50;
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
  font-size: 40rpx;
  margin-right: 16rpx;
}

.reminder-icon.completed {
  opacity: 0.5;
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

.reminder-name.completed {
  text-decoration: line-through;
  color: #999999;
}

.reminder-date {
  font-size: 24rpx;
  color: #999999;
  display: block;
}

.reminder-date.completed {
  color: #cccccc;
}

.reminder-days {
  font-size: 24rpx;
  color: #f5a623;
  margin-top: 4rpx;
  display: block;
}

.reminder-actions {
  display: flex;
  gap: 12rpx;
  margin-left: 16rpx;
}

.action-btn {
  font-size: 22rpx;
  padding: 8rpx 16rpx;
}

.add-section {
  margin: 20rpx;
  margin-bottom: 120rpx;
  position: relative;
  z-index: 1;
}

.add-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.add-icon {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.add-text {
  font-size: 28rpx;
  color: #333333;
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

.status-options {
  display: flex;
  gap: 20rpx;
}

.status-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.status-option.active {
  border-color: #667eea;
  background-color: #f0f4ff;
}

.status-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.status-text {
  font-size: 26rpx;
  color: #666666;
}
</style>