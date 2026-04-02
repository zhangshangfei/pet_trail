<template>
  <view class="edit-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">编辑宠物信息</text>
    </view>

    <!-- 宠物头像 -->
    <view class="avatar-section">
      <view class="avatar-container" @click="chooseAvatar">
        <image v-if="form.avatar" :src="form.avatar" mode="aspectFill" class="avatar"></image>
        <view v-else class="avatar-placeholder">
          <text class="placeholder-icon">📷</text>
          <text class="placeholder-text">点击上传头像</text>
        </view>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <!-- 宠物名称 -->
      <view class="form-item">
        <text class="form-label">宠物名称 *</text>
        <input
          class="form-input"
          v-model="form.name"
          placeholder="请输入宠物名称"
          maxlength="20"
        />
      </view>

      <!-- 品种 -->
      <view class="form-item">
        <text class="form-label">品种</text>
        <input
          class="form-input"
          v-model="form.breed"
          placeholder="例如：英短、金毛等"
          maxlength="30"
        />
      </view>

      <!-- 性别 -->
      <view class="form-item">
        <text class="form-label">性别</text>
        <radio-group class="form-radio-group" @change="onGenderChange">
          <label class="radio-label">
            <radio value="1" :checked="form.gender === 1" />
            <text>♂ 弟弟</text>
          </label>
          <label class="radio-label">
            <radio value="2" :checked="form.gender === 2" />
            <text>♀ 妹妹</text>
          </label>
          <label class="radio-label">
            <radio value="0" :checked="form.gender === 0" />
            <text>❓ 未知</text>
          </label>
        </radio-group>
      </view>

      <!-- 类别 -->
      <view class="form-item">
        <text class="form-label">宠物类别</text>
        <picker
          mode="selector"
          :range="categoryOptions"
          :value="categoryIndex"
          @change="onCategoryChange"
        >
          <view class="picker">
            <text>{{ categoryOptions[categoryIndex] }}</text>
          </view>
        </picker>
      </view>

      <!-- 生日 -->
      <view class="form-item">
        <text class="form-label">生日</text>
        <picker
          mode="date"
          :value="form.birthday"
          @change="onBirthdayChange"
        >
          <view class="picker">
            <text v-if="form.birthday">{{ form.birthday }}</text>
            <text v-else class="picker-placeholder">请选择生日</text>
          </view>
        </picker>
      </view>

      <!-- 体重 -->
      <view class="form-item">
        <text class="form-label">体重 (kg)</text>
        <input
          class="form-input"
          type="digit"
          v-model="form.weight"
          placeholder="请输入体重"
        />
      </view>

      <!-- 毛色 -->
      <view class="form-item">
        <text class="form-label">毛色</text>
        <input
          class="form-input"
          v-model="form.color"
          placeholder="例如：白色、黑色、花色等"
          maxlength="20"
        />
      </view>

      <!-- 绝育状态 -->
      <view class="form-item">
        <text class="form-label">绝育状态</text>
        <picker
          mode="selector"
          :range="sterilizedOptions"
          :value="sterilizedIndex"
          @change="onSterilizedChange"
        >
          <view class="picker">
            <text>{{ sterilizedOptions[sterilizedIndex] }}</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="action-section">
      <button class="save-btn" type="primary" @click="handleSubmit" :loading="loading">
        保存修改
      </button>
    </view>

    <!-- 删除宠物按钮 -->
    <view class="danger-section">
      <button class="delete-btn" @click="showDeleteConfirm">
        删除宠物
      </button>
    </view>
  </view>
</template>

<script>
import { pet as petApi } from '@/api'

export default {
  data() {
    return {
      petId: null,
      loading: false,
      form: {
        name: '',
        breed: '',
        gender: 0,
        category: 0,
        birthday: '',
        weight: '',
        color: '',
        sterilized: 0,
        avatar: ''
      },
      categoryOptions: ['其他', '猫', '狗'],
      categoryIndex: 0,
      sterilizedOptions: ['未绝育', '已绝育'],
      sterilizedIndex: 0
    }
  },
  onLoad(options) {
    if (options.id) {
      this.petId = options.id
      this.loadPetDetail()
    }
  },
  methods: {
    // 加载宠物详情
    async loadPetDetail() {
      try {
        const res = await petApi.getPetDetail(this.petId)
        if (res.success && res.data) {
          const pet = res.data
          this.form = {
            name: pet.name || '',
            breed: pet.breed || '',
            gender: pet.gender ?? 0,
            category: pet.category ?? 0,
            birthday: pet.birthday || '',
            weight: pet.weight ? String(pet.weight) : '',
            color: pet.color || '',
            sterilized: pet.sterilized ?? 0,
            avatar: pet.avatar || ''
          }
          this.categoryIndex = this.form.category
          this.sterilizedIndex = this.form.sterilized
        }
      } catch (error) {
        console.error('加载宠物详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },

    // 选择头像
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0]
          this.form.avatar = tempFilePath
          
          // TODO: 上传图片到服务器
          // this.uploadAvatar(tempFilePath)
        }
      })
    },

    // 性别选择
    onGenderChange(e) {
      this.form.gender = parseInt(e.detail.value)
    },

    // 类别选择
    onCategoryChange(e) {
      this.categoryIndex = parseInt(e.detail.value)
      this.form.category = this.categoryIndex
    },

    // 生日选择
    onBirthdayChange(e) {
      this.form.birthday = e.detail.value
    },

    // 绝育状态选择
    onSterilizedChange(e) {
      this.sterilizedIndex = parseInt(e.detail.value)
      this.form.sterilized = this.sterilizedIndex
    },

    // 提交表单
    async handleSubmit() {
      // 验证必填项
      if (!this.form.name || !this.form.name.trim()) {
        uni.showToast({
          title: '请输入宠物名称',
          icon: 'none'
        })
        return
      }

      this.loading = true

      try {
        // 构建提交数据
        const submitData = {
          name: this.form.name.trim(),
          breed: this.form.breed?.trim() || undefined,
          gender: this.form.gender,
          category: this.form.category,
          birthday: this.form.birthday || undefined,
          weight: this.form.weight ? parseFloat(this.form.weight) : undefined,
          color: this.form.color?.trim() || undefined,
          sterilized: this.form.sterilized,
          avatar: this.form.avatar || undefined
        }

        const res = await petApi.updatePet(this.petId, submitData)

        if (res.success) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })

          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } else {
          throw new Error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        uni.showToast({
          title: error.message || '保存失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    // 显示删除确认
    showDeleteConfirm() {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除宠物"${this.form.name}"吗？此操作不可恢复，所有相关记录都将被删除。`,
        confirmColor: '#f44336',
        success: (res) => {
          if (res.confirm) {
            this.deletePet()
          }
        }
      })
    },

    // 删除宠物
    async deletePet() {
      try {
        const res = await petApi.deletePet(this.petId)

        if (res.success) {
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })

          setTimeout(() => {
            uni.navigateBack({
              delta: 2 // 返回到宠物列表页
            })
          }, 1000)
        } else {
          throw new Error(res.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        uni.showToast({
          title: error.message || '删除失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
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

.avatar-section {
  padding: 40rpx;
  background-color: #ffffff;
  margin: 20rpx;
  border-radius: 16rpx;
  display: flex;
  justify-content: center;
}

.avatar-container {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f5f5f5;
}

.avatar {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.placeholder-icon {
  font-size: 60rpx;
  margin-bottom: 8rpx;
}

.placeholder-text {
  font-size: 24rpx;
  color: #ffffff;
}

.form-section {
  margin: 20rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.form-item {
  margin-bottom: 40rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 16rpx;
  display: block;
}

.form-input {
  width: 100%;
  padding: 24rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #f8f8f8;
}

.form-radio-group {
  display: flex;
  gap: 40rpx;
}

.radio-label {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #666666;
}

.radio-label radio {
  margin-right: 12rpx;
}

.picker {
  width: 100%;
  padding: 24rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  background-color: #f8f8f8;
}

.picker-placeholder {
  color: #999999;
}

.action-section {
  margin: 40rpx 20rpx;
}

.save-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50rpx;
  font-size: 36rpx;
  color: #ffffff;
}

.danger-section {
  margin: 20rpx;
}

.delete-btn {
  width: 100%;
  height: 88rpx;
  background-color: #ffffff;
  color: #f44336;
  border: 1rpx solid #ffebee;
  border-radius: 12rpx;
  font-size: 28rpx;
}
</style>
