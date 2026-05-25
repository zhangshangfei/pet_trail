<template>
  <view class="pet-edit-page">
    <view class="pe-header">
      <view class="pe-header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="pe-back" @tap="goBack">
          <view class="pe-back-arrow"></view>
        </view>
        <text class="pe-title">{{ isEdit ? '编辑宠物' : '添加宠物' }}</text>
        <view class="pe-header-right"></view>
      </view>
    </view>

    <scroll-view scroll-y class="pe-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="pe-content">
        <view class="pe-avatar-section" @tap="onPickAvatar">
          <image v-if="localForm.avatar" class="pe-avatar" :src="localForm.avatar" mode="aspectFill" />
          <view v-else class="pe-avatar-empty">
            <text class="pe-avatar-letter">{{ localForm.name ? localForm.name[0] : '宠' }}</text>
          </view>
          <view class="pe-avatar-badge">
            <text class="pe-avatar-icon">📷</text>
          </view>
          <text class="pe-avatar-tip">点击更换头像</text>
        </view>

        <view class="pe-form-card">
          <view class="pe-section-title">
            <text class="pe-section-text">基本信息</text>
            <view class="pe-section-line"></view>
          </view>

          <view class="pe-field">
            <text class="pe-label"><text class="pe-required">*</text>昵称</text>
            <input class="pe-input" v-model="localForm.name" placeholder="请输入宠物昵称" maxlength="20" />
          </view>

          <view class="pe-field">
            <text class="pe-label">品种</text>
            <input class="pe-input" v-model="localForm.breed" placeholder="如：泰迪、英短、布偶猫" maxlength="30" />
          </view>

          <view class="pe-field">
            <text class="pe-label">类别</text>
            <picker :range="categoryLabels" :value="categoryIndex" @change="onCategoryChange">
              <view class="pe-select-wrap">
                <text :class="['pe-select-text', categoryIndex === 0 && !localForm.category && 'pe-select-placeholder']">{{ categoryLabels[categoryIndex] }}</text>
                <text class="pe-arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="pe-field">
            <text class="pe-label">性别</text>
            <picker :range="genderLabels" :value="genderIndex" @change="onGenderChange">
              <view class="pe-select-wrap">
                <text :class="['pe-select-text', genderIndex === 0 && !localForm.gender && 'pe-select-placeholder']">{{ genderLabels[genderIndex] }}</text>
                <text class="pe-arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="pe-field">
            <text class="pe-label">生日</text>
            <picker mode="date" :value="localForm.birthday || ''" :start="'1970-01-01'" :end="todayStr" @change="onBirthdayChange">
              <view class="pe-select-wrap">
                <text :class="['pe-select-text', !localForm.birthday && 'pe-select-placeholder']">{{ localForm.birthday || '请选择日期' }}</text>
                <text class="pe-arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="pe-divider"></view>

          <view class="pe-section-title">
            <text class="pe-section-text">外貌特征</text>
            <view class="pe-section-line"></view>
          </view>

          <view class="pe-field">
            <text class="pe-label">毛色</text>
            <input class="pe-input" v-model="localForm.color" placeholder="如：白色、棕色、花色" maxlength="20" />
          </view>

          <view class="pe-field">
            <text class="pe-label">体重(kg)</text>
            <input class="pe-input" v-model="localForm.weight" type="digit" placeholder="请输入体重" maxlength="6" />
          </view>

          <view class="pe-divider"></view>

          <view class="pe-section-title">
            <text class="pe-section-text">健康状态</text>
            <view class="pe-section-line"></view>
          </view>

          <view class="pe-field pe-field--last">
            <text class="pe-label">绝育状态</text>
            <picker :range="sterilizedLabels" :value="sterilizedIndex" @change="onSterilizedChange">
              <view class="pe-select-wrap">
                <text :class="['pe-select-text', sterilizedIndex === 0 && !localForm.sterilized && 'pe-select-placeholder']">{{ sterilizedLabels[sterilizedIndex] }}</text>
                <text class="pe-arrow">›</text>
              </view>
            </picker>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="pe-bottom-bar">
      <view class="pe-save-btn" :class="{ 'pe-save-btn--disabled': saving }" @tap="onSave">
        <text class="pe-save-btn-text">{{ saving ? '保存中...' : '保存' }}</text>
      </view>
    </view>

    <image-cropper
      :visible="showCropper"
      :image-src="cropperImageSrc"
      :circular="true"
      @confirm="onCropConfirm"
      @cancel="onCropCancel"
    />
  </view>
</template>

<script>
import * as petApi from '@/api/pet'
import { uploadImage } from '@/api/upload'
import ImageCropper from '@/components/ImageCropper.vue'

export default {
  components: { ImageCropper },
  data() {
    return {
      statusBarHeight: 20,
      showCropper: false,
      cropperImageSrc: '',
      saving: false,
      isEdit: false,
      localForm: this.getDefaultForm()
    }
  },
  computed: {
    todayStr() {
      const d = new Date()
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    categoryLabels() {
      return ['未设置', '🐱 猫', '🐶 狗']
    },
    categoryIndex() {
      const c = typeof this.localForm.category === 'number' ? this.localForm.category : 0
      return c >= 0 && c <= 2 ? c : 0
    },
    genderLabels() {
      return ['未知', '♂ 公', '♀ 母']
    },
    genderIndex() {
      const g = typeof this.localForm.gender === 'number' ? this.localForm.gender : 0
      return g >= 0 && g <= 2 ? g : 0
    },
    sterilizedLabels() {
      return ['未知', '已绝育', '未绝育']
    },
    sterilizedIndex() {
      const s = typeof this.localForm.sterilized === 'number' ? this.localForm.sterilized : 0
      return s >= 0 && s <= 2 ? s : 0
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }

    if (options.id) {
      this.isEdit = true
      try {
        const formData = decodeURIComponent(options.data)
        this.localForm = JSON.parse(formData)
      } catch (e) {
        console.error('解析宠物数据失败:', e)
        this.localForm = this.getDefaultForm()
      }
    } else {
      this.isEdit = false
      this.localForm = this.getDefaultForm()
    }
  },
  methods: {
    getDefaultForm() {
      return {
        id: null,
        name: '',
        breed: '',
        gender: 0,
        birthday: '',
        weight: '',
        color: '',
        avatar: '',
        category: 0,
        sterilized: 0
      }
    },

    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    onCategoryChange(e) {
      this.localForm.category = Number(e.detail.value)
    },

    onGenderChange(e) {
      this.localForm.gender = Number(e.detail.value)
    },

    onBirthdayChange(e) {
      this.localForm.birthday = e.detail.value
    },

    onSterilizedChange(e) {
      this.localForm.sterilized = Number(e.detail.value)
    },

    onPickAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          if (res.tempFilePaths && res.tempFilePaths.length > 0) {
            this.cropperImageSrc = res.tempFilePaths[0]
            this.showCropper = true
          }
        }
      })
    },

    async onCropConfirm(croppedPath) {
      this.showCropper = false
      this.uploading = true
      uni.showLoading({ title: '上传中...', mask: true })
      try {
        const uploadRes = await uploadImage(croppedPath)
        if (uploadRes && uploadRes.success && uploadRes.data) {
          this.localForm.avatar = uploadRes.data.url
        } else {
          uni.showToast({ title: '上传失败', icon: 'none' })
        }
      } catch (err) {
        console.error('上传头像失败:', err)
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        this.uploading = false
        uni.hideLoading()
      }
    },

    onCropCancel() {
      this.showCropper = false
    },

    validateForm() {
      if (!this.localForm.name || !this.localForm.name.trim()) {
        uni.showToast({ title: '请输入宠物昵称', icon: 'none' })
        return false
      }

      if (!this.isEdit && this.localForm.name.length > 20) {
        uni.showToast({ title: '昵称不能超过20个字符', icon: 'none' })
        return false
      }

      if (this.localForm.weight !== '') {
        const w = parseFloat(this.localForm.weight)
        if (isNaN(w) || w <= 0 || w > 500) {
          uni.showToast({ title: '体重需在0-500之间', icon: 'none' })
          return false
        }
      }

      return true
    },

    async onSave() {
      if (this.saving) return
      if (!this.validateForm()) return

      this.saving = true
      uni.showLoading({ title: '保存中...', mask: true })

      try {
        let res
        const payload = {
          name: this.localForm.name.trim(),
          breed: this.localForm.breed || null,
          gender: typeof this.localForm.gender === 'number' ? this.localForm.gender : 0,
          birthday: this.localForm.birthday || null,
          weight: parseFloat(this.localForm.weight) > 0 ? parseFloat(this.localForm.weight).toFixed(2) : null,
          color: this.localForm.color || null,
          avatar: this.localForm.avatar || null,
          category: typeof this.localForm.category === 'number' ? this.localForm.category : 0,
          sterilized: typeof this.localForm.sterilized === 'number' ? this.localForm.sterilized : 0
        }

        if (this.isEdit && this.localForm.id) {
          payload.id = this.localForm.id
          res = await petApi.updatePet(payload)
        } else {
          res = await petApi.createPet(payload)
        }

        uni.hideLoading()

        if (res && res.success) {
          uni.$emit('petUpdated')
          uni.showToast({
            title: this.isEdit ? '修改成功' : '添加成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack({ delta: 1 })
          }, 800)
        } else {
          uni.showToast({ title: (res && res.message) || '操作失败', icon: 'none' })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('保存宠物信息失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.pet-edit-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.pe-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff5a3d 50%, #ff4d4f 100%);
}

.pe-header-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.pe-back {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
}

.pe-back:active {
  background: rgba(255, 255, 255, 0.38);
  transform: scale(0.92);
}

.pe-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.pe-title {
  color: #fff;
  font-size: 34rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}

.pe-header-right {
  width: 64rpx;
}

.pe-scroll {
  height: 100vh;
  box-sizing: border-box;
  padding-bottom: 160rpx;
}

.pe-content {
  padding: 24rpx 28rpx;
}

.pe-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 36rpx;
  padding-top: 16rpx;
  position: relative;
}

.pe-avatar {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  border: 6rpx solid #fff;
  box-shadow:
    0 8rpx 32rpx rgba(255, 106, 61, 0.25),
    0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #fef3ec 0%, #fce7db 100%);
}

.pe-avatar-empty {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff9a5d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 6rpx solid #fff;
  box-shadow: 0 8rpx 32rpx rgba(255, 106, 61, 0.25), 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.pe-avatar-letter {
  font-size: 72rpx;
  font-weight: 800;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(180, 40, 10, 0.3);
}

.pe-avatar-badge {
  position: absolute;
  right: calc(50% - 100rpx);
  bottom: 8rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #fff;
  box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.45);
}

.pe-avatar-icon {
  font-size: 24rpx;
}

.pe-avatar-tip {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #9ca3af;
}

.pe-form-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
}

.pe-section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 28rpx 32rpx 16rpx;
}

.pe-section-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #374151;
  letter-spacing: 1rpx;
}

.pe-section-line {
  flex: 1;
  height: 1rpx;
  background: linear-gradient(90deg, #e5e7eb, transparent);
}

.pe-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f3f4f6;
  min-height: 56rpx;
}

.pe-field--last {
  border-bottom: none;
}

.pe-required {
  color: #ef4444;
  margin-right: 4rpx;
  font-weight: 600;
}

.pe-label {
  font-size: 29rpx;
  color: #374151;
  font-weight: 600;
  flex-shrink: 0;
  width: 160rpx;
}

.pe-input {
  flex: 1;
  text-align: right;
  font-size: 29rpx;
  color: #111827;
}

.pe-select-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
}

.pe-select-text {
  font-size: 29rpx;
  color: #111827;
}

.pe-select-placeholder {
  color: #9ca3af;
}

.pe-arrow {
  font-size: 30rpx;
  color: #9ca3af;
  font-weight: 300;
}

.pe-divider {
  height: 16rpx;
  background: #f5f7fa;
}

.pe-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
}

.pe-save-btn {
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 8rpx 24rpx rgba(255, 77, 79, 0.35),
    0 2rpx 8rpx rgba(255, 77, 79, 0.15);
  transition: all 0.25s ease;
}

.pe-save-btn:active {
  transform: scale(0.97);
  opacity: 0.88;
}

.pe-save-btn--disabled {
  opacity: 0.55;
  pointer-events: none;
}

.pe-save-btn-text {
  color: #fff;
  font-size: 33rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
}
</style>
