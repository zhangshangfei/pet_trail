<template>
  <view class="record-page">
    <!-- 导航栏 - 渐变玻璃 -->
    <view class="nav-fixed glass-nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar glass-nav-bar">
        <view class="nav-back glass-nav-btn" @tap="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">体重记录</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 主内容滚动区 -->
    <scroll-view scroll-y class="record-scroll glass-scroll-container" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="record-content">

        <!-- 宠物信息卡片 - 玻璃卡片（增强版宠物切换） -->
        <view v-if="pet" class="pet-info-card glass-card pet-selector-enhanced">
          <view class="pet-selector-main" @click="showPetSelector = true">
            <image class="pet-info-avatar glass-avatar-pet" :src="pet.avatar || ''" mode="aspectFill" />
            <view class="pet-info-meta">
              <text class="pet-info-name">{{ pet.name }}</text>
              <text class="pet-info-breed">{{ pet.breed || '未设置品种' }}</text>
            </view>
            <view class="pet-switch-area glass-pet-switcher">
              <view class="switch-indicator">
                <text class="switch-icon">🔄</text>
                <text class="switch-text">切换</text>
              </view>
              <text class="switch-arrow">›</text>
            </view>
          </view>
        </view>

        <!-- 宠物选择弹窗 - 玻璃拟态 -->
        <view v-if="showPetSelector" class="modal-mask glass-modal-mask" @click="showPetSelector = false">
          <view class="modal-content pet-select-modal glass-modal-card" @click.stop>
            <view class="modal-header glass-modal-header">
              <text class="modal-title">选择宠物</text>
              <text class="modal-close glass-modal-close" @click="showPetSelector = false">✕</text>
            </view>
            <view class="pet-list-container glass-list-container">
              <view
                v-for="(petItem, idx) in pets"
                :key="petItem.id"
                class="pet-option-item glass-pet-option"
                :class="{ active: pet && pet.id === petItem.id }"
                @click="selectPetDirectly(petItem)"
              >
                <image class="option-avatar glass-option-avatar" :src="petItem.avatar || ''" mode="aspectFill" />
                <view class="option-info">
                  <text class="option-name">{{ petItem.name }}</text>
                  <text class="option-breed">{{ petItem.breed || '未设置品种' }}</text>
                </view>
                <view v-if="pet && pet.id === petItem.id" class="option-check">✓</view>
              </view>
            </view>
          </view>
        </view>

        <!-- 体重显示卡片 - 玻璃卡片（重点区域） -->
        <view class="weight-card glass-card-weight">
          <view class="weight-label-wrap">
            <text class="weight-label-icon">⚖️</text>
            <text class="weight-label-text">当前体重</text>
          </view>
          <view class="weight-display">
            <text class="weight-value glass-weight-number">{{ currentWeight || '-' }}</text>
            <text class="weight-unit">kg</text>
          </view>
          <view class="weight-btn-wrap glass-record-btn" @click="showRecordModal">
            <text class="weight-btn-text">+ 记录体重</text>
          </view>
        </view>

        <!-- 记录列表区域 - 玻璃卡片 -->
        <view v-if="records.length > 0" class="section glass-card-list">
          <view class="section-header">
            <view class="section-label glass-section-title">
              <text class="section-label-icon">📊</text>
              <text class="section-label-text">最近记录</text>
            </view>
            <text class="section-link glass-link-btn" @click="viewAllRecords">查看全部 ›</text>
          </view>
          <view class="record-list">
            <view
              class="record-item glass-record-item"
              v-for="(record, index) in records"
              :key="record.id"
            >
              <view class="record-icon-wrap glass-icon-circle">
                <text class="record-emoji">⚖️</text>
              </view>
              <view class="record-info">
                <text class="record-weight">{{ record.weight }} kg</text>
                <text class="record-date">{{ formatDate(record.recordDate) }}</text>
              </view>
              <view class="record-change glass-change-badge" v-if="index < records.length - 1 && records[index + 1].weight">
                <text class="change-text" :class="record.change > 0 ? 'up' : 'down'">
                  {{ record.change > 0 ? '+' : '' }}{{ record.change }}%
                </text>
              </view>
              <view class="record-ops">
                <text class="op-link glass-op-edit" @click="onEditRecord(record)">编辑</text>
                <text class="op-link op-danger glass-op-delete" @click="onDeleteRecord(record)">删除</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 - 玻璃卡片 -->
        <view class="empty-section glass-card-empty" v-if="records.length === 0">
          <text class="empty-emoji">⚖️</text>
          <text class="empty-text">暂无体重记录</text>
          <view class="empty-btn glass-empty-btn" @click="showRecordModal">
            <text class="empty-btn-text">添加第一条记录</text>
          </view>
        </view>

      </view>
    </scroll-view>

    <!-- 模态弹窗 - 玻璃拟态 -->
    <view class="modal-mask glass-modal-mask" v-if="showModal" @click="hideModal">
      <view class="modal-content glass-modal-card" @click.stop>
        <!-- 弹窗头部 -->
        <view class="modal-header glass-modal-header">
          <text class="modal-title">{{ editingRecord ? '编辑体重' : '记录体重' }}</text>
          <text class="modal-close glass-modal-close" @click="hideModal">✕</text>
        </view>

        <!-- 弹窗内容 -->
        <view class="modal-body">
          <!-- 体重输入组 -->
          <view class="form-group">
            <view class="form-label glass-form-label">
              <text class="label-emoji">⚖️</text>
              <text class="label-text">体重 (kg)</text>
            </view>
            <input
              class="form-input glass-form-input"
              type="digit"
              v-model="form.weight"
              placeholder="请输入体重"
            />
          </view>

          <!-- 日期选择组 -->
          <view class="form-group">
            <view class="form-label glass-form-label">
              <text class="label-emoji">📅</text>
              <text class="label-text">记录日期</text>
            </view>
            <picker
              mode="date"
              :value="form.recordDate"
              @change="onDateChange"
            >
              <view class="picker-value glass-form-input">
                <text v-if="form.recordDate" class="value-text">{{ formatDate(form.recordDate) }}</text>
                <text v-else class="picker-placeholder">请选择日期</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
        </view>

        <!-- 弹窗底部按钮 -->
        <view class="modal-footer glass-modal-footer">
          <view class="modal-btn modal-btn-cancel glass-btn-cancel" @click="hideModal">
            <text class="modal-btn-text-cancel">取消</text>
          </view>
          <view class="modal-btn modal-btn-confirm glass-btn-submit" @click="submitRecord">
            <text class="modal-btn-text-confirm">确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'
import * as petApi from '@/api/pet'
import * as healthApi from '@/api/health'

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
      showPetSelector: false,
      editingRecord: null,
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
        const res = await petApi.getPetList();
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
        const res = await petApi.getPetDetail(this.petId);
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
        const res = await healthApi.getWeightRecords(this.petId);
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
      for (let i = 0; i < this.records.length - 1; i++) {
        const curr = this.records[i];
        const prev = this.records[i + 1];
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
      this.editingRecord = null
      this.form.weight = ''
      this.form.recordDate = new Date().toISOString().split('T')[0]
      this.showModal = true;
    },
    onEditRecord(record) {
      this.editingRecord = record
      this.form.weight = String(record.weight)
      this.form.recordDate = record.recordDate ? record.recordDate.split('T')[0] : ''
      this.showModal = true
    },
    onDeleteRecord(record) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除 ${record.weight}kg 的体重记录吗？`,
        confirmText: '删除',
        confirmColor: '#ff4d4f',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await healthApi.deleteWeightRecord(this.petId, record.id)
              if (result && result.success) {
                uni.showToast({ title: '删除成功', icon: 'success' })
                this.loadRecords()
                this.loadPetInfo()
              } else {
                uni.showToast({ title: (result && result.message) || '删除失败', icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '网络不给力，请稍后重试', icon: 'none' })
            }
          }
        }
      })
    },
    hideModal() {
      this.showModal = false;
      this.editingRecord = null;
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
    async selectPetDirectly(petItem) {
      if (!petItem || !petItem.id) return;
      this.petId = petItem.id;
      this.showPetSelector = false;
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

      if (this.editingRecord) {
        await this.updateRecord(this.editingRecord.id);
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
        const res = await healthApi.createWeightRecord(this.petId, this.weight, this.form.date);

        if (res && res.success) {
          uni.showToast({ title: '记录成功', icon: 'success' });
          this.hideModal();
          this.loadRecords();
          this.loadPetInfo();
        } else {
          uni.showToast({ title: (res && res.message) || '记录失败', icon: 'none' });
        }
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '网络不给力，请稍后重试', icon: 'none' });
      }
    },
    async updateRecord(recordId) {
      try {
        const res = await healthApi.updateWeightRecord(this.petId, recordId, { weight: this.editWeight, recordDate: this.editDate });

        if (res && res.success) {
          uni.showToast({ title: '修改成功', icon: 'success' });
          this.hideModal();
          this.loadRecords();
          this.loadPetInfo();
        } else {
          uni.showToast({ title: (res && res.message) || '修改失败', icon: 'none' });
        }
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '修改失败', icon: 'none' });
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
/* ============================================
   体重记录页 - 玻璃拟态风格 (Glassmorphism)
   保持原逻辑不变，全面升级视觉体验
   ============================================ */

.record-page {
  min-height: 100vh;
  background: transparent;
}

/* ====== 导航栏 - 渐变玻璃 ====== */
.glass-nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.glass-nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.glass-nav-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(16px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4rpx 16rpx rgba(0, 0, 0, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.35);
  border: 1rpx solid rgba(255, 255, 255, 0.35);
  transition: all 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    background: rgba(255, 255, 255, 0.4);
    transform: scale(0.92);
  }
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(180, 30, 10, 0.25);
}

.nav-placeholder {
  width: 64rpx;
}

/* ====== 滚动容器 ====== */
.glass-scroll-container {
  height: 100vh;
  box-sizing: border-box;
  background: linear-gradient(180deg, rgba(255, 245, 243, 0.95) 0%, rgba(250, 250, 252, 0.98) 100%);
}

.record-content {
  padding: 16rpx 24rpx 40rpx;
}

/* ====== 卡片 - 统一玻璃样式 ====== */
.glass-card {
  position: relative;
  background: rgba(255, 255, 255, 0.86);
  border-radius: 26rpx;
  padding: 26rpx;
  margin-bottom: 22rpx;
  box-shadow:
    0 6rpx 24rpx rgba(31, 38, 135, 0.08),
    0 2rpx 8rpx rgba(0, 0, 0, 0.03),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.02);
  backdrop-filter: blur(20px);
  border: 1rpx solid rgba(255, 255, 255, 0.62);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.88), transparent);
    pointer-events: none;
  }
}

/* ====== 宠物信息卡片 ====== */
.pet-info-card {
  display: flex;
  align-items: center;
}

.pet-selector-enhanced {
  cursor: pointer;
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: scale(0.98);
    box-shadow:
      0 8rpx 28rpx rgba(31, 38, 135, 0.12),
      0 3rpx 10rpx rgba(0, 0, 0, 0.04),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.92);
  }
}

.pet-selector-main {
  display: flex;
  align-items: center;
  width: 100%;
}

.glass-avatar-pet {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 22rpx;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  flex-shrink: 0;
  box-shadow:
    0 8rpx 24rpx rgba(255, 122, 61, 0.15),
    0 4rpx 12rpx rgba(0, 0, 0, 0.06),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.5);
  border: 3rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;

  .pet-selector-enhanced:active & {
    transform: scale(0.95) rotate(-3deg);
    box-shadow:
      0 6rpx 18rpx rgba(255, 122, 61, 0.2),
      0 2rpx 8rpx rgba(0, 0, 0, 0.08),
      inset 0 1rpx 2rpx rgba(255, 255, 255, 0.6);
  }
}

.pet-info-meta {
  flex: 1;
  min-width: 0;
}

.pet-info-name {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  display: block;
  letter-spacing: 0.8rpx;
  margin-bottom: 6rpx;
}

.pet-info-breed {
  font-size: 25rpx;
  color: var(--pt-muted, #9ca3af);
  display: block;
  font-weight: 500;
}

.glass-pet-switcher {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 14rpx 24rpx;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.85) 0%, rgba(255, 250, 247, 0.78) 100%);
  border-radius: 999rpx;
  backdrop-filter: blur(10px);
  border: 1.5rpx solid rgba(255, 122, 61, 0.2);
  box-shadow:
    0 4rpx 14rpx rgba(255, 106, 61, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;

  &:active {
    transform: scale(0.94);
    background: linear-gradient(135deg, rgba(255, 235, 230, 0.92) 0%, rgba(255, 245, 240, 0.88) 100%);
    box-shadow:
      0 6rpx 18rpx rgba(255, 106, 61, 0.18),
      inset 0 1rpx 2rpx rgba(255, 122, 61, 0.15);
    border-color: rgba(255, 122, 61, 0.35);
  }
}

.switch-indicator {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.switch-icon {
  font-size: 26rpx;
  animation: switchRotate 2s linear infinite;
}

@keyframes switchRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.switch-text {
  font-size: 25rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 700;
  letter-spacing: 1rpx;
}

.switch-arrow {
  font-size: 32rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 600;
  transition: transform 0.28s ease;

  .glass-pet-switcher:active & {
    transform: translateX(4rpx);
  }
}

/* ====== 体重显示卡片 - 重点设计区域 ====== */
.glass-card-weight {
  text-align: center;
  padding: 48rpx 32rpx;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.88) 0%, rgba(255, 250, 250, 0.85) 100%);
  border: 2rpx solid rgba(255, 122, 61, 0.15);
  box-shadow:
    0 10rpx 36rpx rgba(31, 38, 135, 0.1),
    0 4rpx 14rpx rgba(255, 106, 61, 0.06),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.94),
    inset 0 -1rpx 0 rgba(255, 106, 61, 0.05);
  animation: cardGlow 3s ease-in-out infinite alternate;

  &::before {
    background: linear-gradient(90deg, transparent, rgba(255, 122, 61, 0.35), transparent);
  }
}

@keyframes cardGlow {
  0% {
    box-shadow:
      0 10rpx 36rpx rgba(31, 38, 135, 0.1),
      0 4rpx 14rpx rgba(255, 106, 61, 0.06),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.94);
  }
  100% {
    box-shadow:
      0 12rpx 42rpx rgba(31, 38, 135, 0.12),
      0 6rpx 18rpx rgba(255, 106, 61, 0.1),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.96);
  }
}

.weight-label-wrap {
  display: inline-flex;
  align-items: center;
  margin-bottom: 24rpx;
  padding: 8rpx 20rpx;
  background: rgba(255, 245, 240, 0.65);
  border-radius: 999rpx;
  backdrop-filter: blur(6px);
}

.weight-label-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.weight-label-text {
  font-size: 27rpx;
  color: var(--pt-secondary, #6b7280);
  font-weight: 600;
  letter-spacing: 1rpx;
}

.weight-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 38rpx;
}

.glass-weight-number {
  font-size: 90rpx;
  font-weight: 900;
  color: var(--pt-primary, #ff7a3d);
  line-height: 1;
  letter-spacing: -2rpx;
  text-shadow:
    0 4rpx 16rpx rgba(255, 122, 61, 0.2),
    0 2rpx 8rpx rgba(255, 77, 79, 0.15);
  background: linear-gradient(180deg, #ff7a3d 0%, #ff5a52 50%, #ff4d4f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.weight-unit {
  font-size: 34rpx;
  color: var(--pt-muted, #9ca3af);
  margin-left: 10rpx;
  font-weight: 600;
}

.glass-record-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 22rpx 68rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 999rpx;
  box-shadow:
    0 10rpx 32rpx rgba(255, 90, 61, 0.4),
    0 4rpx 12rpx rgba(255, 90, 61, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 0 rgba(180, 50, 20, 0.2);
  position: relative;
  overflow: hidden;
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
    transition: left 0.55s ease;
  }

  &:active {
    transform: scale(0.96);
    opacity: 0.95;
  }

  &:active::after {
    left: 100%;
  }
}

.weight-btn-text {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2rpx;
}

/* ====== 列表区域卡片 ====== */
.glass-card-list {
  padding: 28rpx 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22rpx;
}

.glass-section-title {
  display: flex;
  align-items: center;
}

.section-label-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.section-label-text {
  font-size: 29rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

.glass-link-btn {
  font-size: 25rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 700;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 122, 61, 0.08);
  transition: all 0.28s ease;

  &:active {
    background: rgba(255, 122, 61, 0.15);
    transform: scale(0.96);
  }
}

/* ====== 记录列表项 ====== */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.glass-record-item {
  display: flex;
  align-items: center;
  padding: 22rpx 18rpx;
  background: rgba(249, 250, 251, 0.65);
  border-radius: 18rpx;
  backdrop-filter: blur(8px);
  border: 1rpx solid rgba(209, 213, 219, 0.15);
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.85);
    transform: translateX(4rpx);
  }

  &:active {
    transform: scale(0.98);
  }
}

.glass-icon-circle {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 245, 240, 0.85) 0%, rgba(255, 250, 247, 0.78) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18rpx;
  flex-shrink: 0;
  border: 1.5rpx solid rgba(255, 122, 61, 0.15);
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.08);
}

.record-emoji {
  font-size: 30rpx;
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-weight {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
  display: block;
  margin-bottom: 4rpx;
  letter-spacing: 0.3rpx;
}

.record-date {
  font-size: 24rpx;
  color: var(--pt-muted, #9ca3af);
  display: block;
  font-weight: 500;
}

.glass-change-badge {
  margin-left: 16rpx;
  flex-shrink: 0;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  backdrop-filter: blur(6px);
  border: 1rpx solid transparent;
}

.change-text {
  font-size: 24rpx;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

.change-text.up {
  color: #10b981;
  .glass-change-badge & {
    background: rgba(16, 185, 129, 0.1);
    border-color: rgba(16, 185, 129, 0.2);
  }
}

.change-text.down {
  color: #ff4d4f;
  .glass-change-badge & {
    background: rgba(255, 77, 79, 0.1);
    border-color: rgba(255, 77, 79, 0.2);
  }
}

.record-ops {
  display: flex;
  gap: 16rpx;
  margin-left: 12rpx;
  flex-shrink: 0;
}

.glass-op-edit {
  font-size: 24rpx;
  color: var(--pt-primary, #ff7a3d);
  font-weight: 600;
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  background: rgba(255, 122, 61, 0.08);
  transition: all 0.25s ease;

  &:active {
    background: rgba(255, 122, 61, 0.18);
    transform: scale(0.93);
  }
}

.op-danger.glass-op-delete {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.08);

  &:active {
    background: rgba(255, 77, 79, 0.18);
  }
}

/* ====== 空状态卡片 ====== */
.glass-card-empty {
  padding: 120rpx 24rpx;
  text-align: center;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.82) 0%, rgba(250, 250, 252, 0.78) 100%);
}

.empty-emoji {
  font-size: 90rpx;
  display: block;
  margin-bottom: 24rpx;
  filter: grayscale(20%) brightness(1.05);
}

.empty-text {
  font-size: 29rpx;
  color: var(--pt-muted, #9ca3af);
  display: block;
  margin-bottom: 36rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.glass-empty-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 22rpx 56rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 999rpx;
  box-shadow:
    0 10rpx 32rpx rgba(255, 90, 61, 0.4),
    0 4rpx 12rpx rgba(255, 90, 61, 0.25),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.96);
    opacity: 0.95;
  }
}

.empty-btn-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2rpx;
}

/* ====== 模态框系统 - 玻璃拟态 ====== */
.glass-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  animation: modalFadeIn 0.25s ease-out both;
}

@keyframes modalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.glass-modal-card {
  width: 80%;
  max-height: 80vh;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 28rpx;
  overflow: hidden;
  backdrop-filter: blur(24px);
  border: 1rpx solid rgba(255, 255, 255, 0.72);
  box-shadow:
    0 24rpx 60rpx rgba(0, 0, 0, 0.2),
    0 8rpx 24rpx rgba(0, 0, 0, 0.1),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.95);
  animation: modalSlideUp 0.32s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.pet-select-modal {
  max-height: 70vh;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.glass-list-container {
  padding: 18rpx;
  max-height: 55vh;
  overflow-y: auto;
}

.glass-pet-option {
  display: flex;
  align-items: center;
  padding: 22rpx 18rpx;
  border-radius: 20rpx;
  margin-bottom: 12rpx;
  background: rgba(249, 250, 251, 0.6);
  backdrop-filter: blur(8px);
  border: 2rpx solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.97);
  }

  &.active {
    background: linear-gradient(135deg, rgba(255, 245, 240, 0.92) 0%, rgba(255, 250, 247, 0.88) 100%);
    border-color: var(--pt-primary, #ff7a3d);
    box-shadow:
      0 6rpx 20rpx rgba(255, 106, 61, 0.15),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.7);

    .option-name {
      color: var(--pt-primary, #ff7a3d);
      font-weight: 800;
    }

    .option-check {
      display: flex;
      opacity: 1;
      transform: scale(1);
    }
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.glass-option-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  margin-right: 18rpx;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(255, 122, 61, 0.12);
  border: 2rpx solid rgba(255, 255, 255, 0.8);
}

.option-info {
  flex: 1;
  min-width: 0;
}

.option-name {
  font-size: 29rpx;
  font-weight: 700;
  color: var(--pt-text, #374151);
  display: block;
  margin-bottom: 4rpx;
  transition: all 0.28s ease;
}

.option-breed {
  font-size: 24rpx;
  color: var(--pt-muted, #9ca3af);
  display: block;
  font-weight: 500;
}

.option-check {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 14rpx rgba(255, 90, 61, 0.3);
  opacity: 0;
  transform: scale(0.5);
  transition: all 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.glass-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 28rpx;
  border-bottom: 1rpx solid rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(8px);
}

.modal-title {
  font-size: 33rpx;
  font-weight: 800;
  color: var(--pt-text, #111827);
  letter-spacing: 0.5rpx;
}

.glass-modal-close {
  font-size: 34rpx;
  color: var(--pt-muted, #9ca3af);
  padding: 10rpx;
  border-radius: 50%;
  transition: all 0.25s ease;

  &:active {
    background: rgba(255, 77, 79, 0.1);
    color: #ff4d4f;
    transform: rotate(90deg);
  }
}

.modal-body {
  padding: 32rpx 28rpx;
}

.form-group {
  margin-bottom: 28rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.glass-form-label {
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
  font-weight: 700;
  color: var(--pt-text, #111827);
  letter-spacing: 0.3rpx;
}

.glass-form-input {
  width: 100%;
  box-sizing: border-box;
  background: rgba(249, 250, 251, 0.75);
  border: 1.5rpx solid rgba(209, 213, 219, 0.3);
  border-radius: 18rpx;
  padding: 22rpx 24rpx;
  min-height: 84rpx;
  font-size: 28rpx;
  color: var(--pt-text, #374151);
  backdrop-filter: blur(8px);
  transition: all 0.28s ease;

  &:focus {
    border-color: var(--pt-primary, #ff6a3d);
    background: rgba(255, 255, 255, 0.92);
    box-shadow: 0 0 0 5rpx rgba(255, 106, 61, 0.1);
  }
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.value-text {
  font-size: 28rpx;
  color: var(--pt-text, #374151);
  font-weight: 500;
}

.picker-placeholder {
  font-size: 28rpx;
  color: var(--pt-muted, #9ca3af);
}

.picker-arrow {
  font-size: 20rpx;
  color: var(--pt-muted, #9ca3af);
}

.glass-modal-footer {
  display: flex;
  gap: 18rpx;
  padding: 26rpx 28rpx;
  border-top: 1rpx solid rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(8px);
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.28s ease;
}

.glass-btn-cancel {
  background: rgba(249, 250, 251, 0.9);
  border: 1rpx solid rgba(209, 213, 219, 0.35);
  box-shadow:
    0 4rpx 14rpx rgba(0, 0, 0, 0.04),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.85);

  &:active {
    transform: scale(0.96);
    background: rgba(233, 234, 236, 0.98);
  }
}

.modal-btn-text-cancel {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--pt-secondary, #6b7280);
}

.glass-btn-submit {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow:
    0 8rpx 24rpx rgba(255, 90, 61, 0.35),
    0 4rpx 12rpx rgba(255, 90, 61, 0.2),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
    transition: left 0.55s ease;
  }

  &:active {
    transform: scale(0.97);
    opacity: 0.95;
  }

  &:active::after {
    left: 100%;
  }
}

.modal-btn-text-confirm {
  font-size: 28rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2rpx;
}

/* ====== 暗色模式适配 ====== */
page.dark .glass-nav-fixed {
  background: linear-gradient(180deg, rgba(80, 80, 110, 0.95) 0%, rgba(100, 60, 90, 0.95) 100%);
}

page.dark .glass-scroll-container {
  background: linear-gradient(180deg, rgba(40, 40, 55, 0.98) 0%, rgba(30, 30, 42, 0.99) 100%);
}

page.dark .glass-card {
  background: rgba(40, 40, 55, 0.84);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 6rpx 24rpx rgba(0, 0, 0, 0.3),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.1);
}

page.dark .glass-card-weight {
  background: linear-gradient(145deg, rgba(50, 50, 70, 0.88) 0%, rgba(45, 45, 62, 0.85) 100%);
  border-color: rgba(255, 122, 61, 0.2);
  box-shadow:
    0 10rpx 36rpx rgba(0, 0, 0, 0.4),
    0 4rpx 14rpx rgba(255, 106, 61, 0.08),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.08);
}

page.dark .glass-weight-number {
  background: linear-gradient(180deg, #ff9966 0%, #ff7766 50%, #ff6666 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

page.dark .glass-avatar {
  background: linear-gradient(135deg, rgba(60, 60, 80, 1) 0%, rgba(50, 50, 70, 1) 100%);
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.3);
}

page.dark .glass-avatar-pet {
  background: linear-gradient(135deg, rgba(255, 150, 100, 0.3) 0%, rgba(255, 120, 80, 0.2) 100%);
  border-color: rgba(255, 122, 61, 0.3);
  box-shadow:
    0 8rpx 24rpx rgba(255, 106, 61, 0.1),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.08);

  .pet-selector-enhanced:active & {
    transform: scale(0.95) rotate(-3deg);
    box-shadow:
      0 6rpx 18rpx rgba(255, 122, 61, 0.15),
      inset 0 1rpx 2rpx rgba(255, 255, 255, 0.1);
  }
}

page.dark .glass-switcher {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);

  &:active {
    background: rgba(255, 106, 61, 0.12);
  }
}

page.dark .glass-pet-switcher {
  background: linear-gradient(135deg, rgba(255, 120, 80, 0.12) 0%, rgba(255, 100, 80, 0.08) 100%);
  border-color: rgba(255, 122, 61, 0.25);
  box-shadow:
    0 4rpx 14rpx rgba(255, 106, 61, 0.08),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.05);

  &:active {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.18) 0%, rgba(255, 100, 80, 0.14) 100%);
    border-color: rgba(255, 122, 61, 0.4);
    box-shadow:
      0 6rpx 18rpx rgba(255, 106, 61, 0.15),
      inset 0 1rpx 2rpx rgba(255, 122, 61, 0.12);
  }
}

page.dark .glass-record-item {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);

  &:hover {
    background: rgba(255, 255, 255, 0.08);
  }
}

page.dark .glass-icon-circle {
  background: linear-gradient(135deg, rgba(255, 120, 80, 0.15) 0%, rgba(255, 100, 80, 0.1) 100%);
  border-color: rgba(255, 122, 61, 0.2);
}

page.dark .glass-change-badge {
  .up {
    background: rgba(16, 185, 129, 0.15);
    border-color: rgba(16, 185, 129, 0.25);
  }

  .down {
    background: rgba(255, 77, 79, 0.15);
    border-color: rgba(255, 77, 79, 0.25);
  }
}

page.dark .glass-op-edit {
  background: rgba(255, 122, 61, 0.12);

  &:active {
    background: rgba(255, 122, 61, 0.22);
  }
}

page.dark .op-danger.glass-op-delete {
  background: rgba(255, 77, 79, 0.12);

  &:active {
    background: rgba(255, 77, 79, 0.22);
  }
}

page.dark .glass-card-empty {
  background: linear-gradient(145deg, rgba(45, 45, 60, 0.84) 0%, rgba(40, 40, 55, 0.78) 100%);
}

page.dark .glass-modal-mask {
  background: rgba(0, 0, 0, 0.65);
}

page.dark .glass-modal-card {
  background: rgba(40, 40, 55, 0.95);
  border-color: rgba(255, 255, 255, 0.1);
}

page.dark .glass-pet-option {
  background: rgba(255, 255, 255, 0.04);

  &.active {
    background: linear-gradient(135deg, rgba(255, 120, 80, 0.2) 0%, rgba(255, 100, 80, 0.15) 100%);
    border-color: rgba(255, 122, 61, 0.4);
    box-shadow:
      0 6rpx 20rpx rgba(255, 106, 61, 0.12),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.08);

    .option-name {
      color: #ff9966;
    }
  }
}

page.dark .glass-option-avatar {
  background: linear-gradient(135deg, rgba(255, 150, 100, 0.25) 0%, rgba(255, 120, 80, 0.15) 100%);
  border-color: rgba(255, 122, 61, 0.3);
}

page.dark .glass-modal-header,
page.dark .glass-modal-footer {
  border-bottom-color: rgba(255, 255, 255, 0.08);
  border-top-color: rgba(255, 255, 255, 0.08);
}

page.dark .modal-title {
  color: #e5e5e5;
}

page.dark .glass-form-input {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.1);

  &:focus {
    background: rgba(255, 255, 255, 0.08);
    border-color: var(--pt-primary, #ff6a3d);
  }
}

page.dark .glass-btn-cancel {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);

  &:active {
    background: rgba(233, 234, 236, 0.15);
  }
}

page.dark .modal-btn-text-cancel {
  color: #aaaaaa;
}

page.dark .pet-info-name {
  color: #e5e5e5;
}

page.dark .pet-info-breed {
  color: #888888;
}

page.dark .record-weight {
  color: #e5e5e5;
}

page.dark .record-date {
  color: #888888;
}

page.dark .section-label-text {
  color: #e5e5e5;
}

page.dark .label-text {
  color: #e5e5e5;
}

page.dark .value-text {
  color: #e5e5e5;
}
</style>
