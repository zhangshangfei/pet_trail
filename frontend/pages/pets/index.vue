<template>
  <view class="pet-list">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">我的宠物</text>
      <button class="add-btn" type="primary" @click="showAddModal">+ 添加宠物</button>
    </view>

    <!-- 宠物列表 -->
    <view class="pet-list-container" v-if="pets.length > 0">
      <view class="pet-item" v-for="pet in pets" :key="pet.id" @click="goToDetail(pet.id)">
        <view class="pet-avatar">
          <image v-if="pet.avatar" :src="pet.avatar" mode="aspectFill"></image>
          <view v-else class="avatar-placeholder">
            <text class="placeholder-text">{{ pet.name ? pet.name[0] : '宠' }}</text>
          </view>
        </view>
        <view class="pet-info">
          <view class="pet-name">{{ pet.name }}</view>
          <view class="pet-breed">{{ pet.breed || '未填写品种' }}</view>
          <view class="pet-meta">
            <text class="pet-gender" v-if="pet.gender !== undefined && pet.gender !== null">
              {{ getGenderText(pet.gender) }}
            </text>
            <text class="pet-weight" v-if="pet.weight">
              {{ pet.weight }} kg
            </text>
            <text class="pet-birthday" v-if="pet.birthday">
              {{ formatDate(pet.birthday) }}
            </text>
          </view>
        </view>
        <view class="pet-action">
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <view class="empty-icon">🐾</view>
      <text class="empty-text">还没有宠物哦</text>
      <button class="add-btn-large" type="primary" @click="showAddModal">添加第一只宠物</button>
    </view>

    <!-- 添加宠物弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="hideAddModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">添加宠物</text>
          <text class="modal-close" @click="hideAddModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">宠物名称 *</text>
            <input class="form-input" v-model="form.name" placeholder="请输入宠物名称" />
          </view>
          <view class="form-item">
            <text class="form-label">宠物类别 *</text>
            <radio-group class="form-radio-group" @change="onCategoryChange">
              <label class="radio-label">
                <radio value="1" :checked="form.category === 1" />
                <text>🐱 猫</text>
              </label>
              <label class="radio-label">
                <radio value="2" :checked="form.category === 2" />
                <text>🐶 狗</text>
              </label>
              <label class="radio-label">
                <radio value="0" :checked="form.category === 0" />
                <text>🐾 其他</text>
              </label>
            </radio-group>
          </view>
          <view class="form-item">
            <text class="form-label">品种</text>
            <input class="form-input" v-model="form.breed" placeholder="请输入品种" />
          </view>
          <view class="form-item">
            <text class="form-label">性别</text>
            <radio-group class="form-radio-group" @change="onGenderChange">
              <label class="radio-label">
                <radio value="1" :checked="form.gender === 1" />
                <text>♂ 公</text>
              </label>
              <label class="radio-label">
                <radio value="2" :checked="form.gender === 2" />
                <text>♀ 母</text>
              </label>
              <label class="radio-label">
                <radio value="0" :checked="form.gender === 0" />
                <text>❓ 未知</text>
              </label>
            </radio-group>
          </view>
          <view class="form-item">
            <text class="form-label">是否绝育</text>
            <radio-group class="form-radio-group" @change="onSterilizedChange">
              <label class="radio-label">
                <radio value="1" :checked="form.sterilized === 1" />
                <text>✅ 已绝育</text>
              </label>
              <label class="radio-label">
                <radio value="0" :checked="form.sterilized === 0" />
                <text>❌ 未绝育</text>
              </label>
            </radio-group>
          </view>
          <view class="form-item">
            <text class="form-label">生日</text>
            <picker mode="date" :value="form.birthday" @change="onBirthdayChange">
              <view class="picker">
                <text v-if="form.birthday">{{ formatDate(form.birthday) }}</text>
                <text v-else class="picker-placeholder">请选择生日</text>
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">体重 (kg)</text>
            <input class="form-input" type="digit" v-model="form.weight" placeholder="请输入体重" />
          </view>
          <view class="form-item">
            <text class="form-label">毛色</text>
            <input class="form-input" v-model="form.color" placeholder="请输入毛色，如：白色、黑白相间" />
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="hideAddModal">取消</button>
          <button class="modal-btn confirm" type="primary" @click="submitForm">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { pet as petApi } from '@/api'
import { formatDate, getGenderText } from '@/utils'

export default {
  data() {
    return {
      pets: [],
      showModal: false,
      form: {
        name: '',
        category: 0,
        breed: '',
        gender: 0,
        sterilized: 0,
        birthday: '',
        weight: '',
        color: '',
        avatar: ''
      }
    }
  },
  onLoad() {
    this.loadPets()
  },
  methods: {
    formatDate,
    getGenderText,

    // 加载宠物列表
    async loadPets() {
      try {
        const res = await petApi.getPetList()
        this.pets = res.data || []
      } catch (error) {
        console.error('加载宠物列表失败:', error)
        // 错误提示由 request 拦截器处理
      }
    },

    // 显示添加弹窗
    showAddModal() {
      this.showModal = true
      this.form = {
        name: '',
        category: 0,
        breed: '',
        gender: 0,
        sterilized: 0,
        birthday: '',
        weight: '',
        color: '',
        avatar: ''
      }
    },

    // 隐藏添加弹窗
    hideAddModal() {
      this.showModal = false
    },

    // 性别选择
    onGenderChange(e) {
      this.form.gender = parseInt(e.detail.value)
    },

    // 类别选择
    onCategoryChange(e) {
      this.form.category = parseInt(e.detail.value)
    },

    // 绝育选择
    onSterilizedChange(e) {
      this.form.sterilized = parseInt(e.detail.value)
    },

    // 生日选择
    onBirthdayChange(e) {
      this.form.birthday = e.detail.value
    },

    // 提交表单
    async submitForm() {
      if (!this.form.name || !this.form.name.trim()) {
        uni.showToast({
          title: '请输入宠物名称',
          icon: 'none'
        })
        return
      }

      try {
        const submitData = {
          name: this.form.name.trim(),
          category: this.form.category,
          breed: this.form.breed?.trim() || undefined,
          gender: this.form.gender,
          sterilized: this.form.sterilized,
          birthday: this.form.birthday || undefined,
          weight: this.form.weight ? parseFloat(this.form.weight) : undefined,
          color: this.form.color?.trim() || undefined,
          avatar: this.form.avatar || undefined
        }

        const res = await petApi.createPet(submitData)

        uni.showToast({
          title: '添加成功',
          icon: 'success'
        })

        this.hideAddModal()
        this.loadPets()
      } catch (error) {
        console.error('添加宠物失败:', error)
        // 错误提示由 request 拦截器处理
      }
    },

    // 跳转到宠物详情
    goToDetail(petId) {
      uni.navigateTo({
        url: `/pages/pets/detail?id=${petId}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.pet-list {
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

.add-btn {
  padding: 16rpx 32rpx;
  font-size: 28rpx;
  border-radius: 40rpx;
}

.pet-list-container {
  padding: 20rpx;
}

.pet-item {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.pet-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 24rpx;
}

.pet-avatar image {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 48rpx;
  color: #ffffff;
  font-weight: bold;
}

.pet-info {
  flex: 1;
}

.pet-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8rpx;
}

.pet-breed {
  font-size: 26rpx;
  color: #999999;
  margin-bottom: 8rpx;
}

.pet-meta {
  display: flex;
  gap: 20rpx;
}

.pet-gender {
  font-size: 24rpx;
  color: #667eea;
  padding: 4rpx 12rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
}

.pet-weight {
  font-size: 24rpx;
  color: #666666;
}

.pet-birthday {
  font-size: 24rpx;
  color: #666666;
}

.pet-action {
  margin-left: 20rpx;
}

.arrow {
  font-size: 48rpx;
  color: #cccccc;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 40rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
  margin-bottom: 40rpx;
}

.add-btn-large {
  padding: 24rpx 80rpx;
  font-size: 32rpx;
  border-radius: 40rpx;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding-top: 60rpx;
  overflow-y: auto;
}

.modal-content {
  width: 85%;
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: visible;
  margin-bottom: 40rpx;
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
  margin-bottom: 20rpx;
}

.form-label {
  font-size: 26rpx;
  color: #333333;
  margin-bottom: 10rpx;
  display: block;
}

.form-input {
  width: 100%;
  padding: 16rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 26rpx;
}

.form-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 8rpx;
}

.radio-label {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #666666;
  padding: 10rpx 16rpx;
  background-color: #f8f8f8;
  border-radius: 8rpx;
  border: 1rpx solid #e0e0e0;
}

.radio-label radio {
  margin-right: 8rpx;
  transform: scale(0.85);
}

.picker {
  width: 100%;
  padding: 16rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #333333;
}

.picker-placeholder {
  color: #999999;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  padding: 20rpx;
  font-size: 26rpx;
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
