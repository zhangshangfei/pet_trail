<template>
  <view class="pet-edit-page">
    <!-- 顶部导航 -->
    <view class="pe-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="pe-nav-inner">
        <view class="pe-back" @tap="goBack">
          <text class="pe-back-icon">←</text>
        </view>
        <text class="pe-nav-title">{{ isEdit ? '编辑宠物' : '添加宠物' }}</text>
        <view class="pe-action-btn" :class="{ saving: submitting }" @tap="onSave">
          <text class="pe-action-text">保存</text>
        </view>
      </view>
    </view>

    <!-- 滚动内容区 -->
    <scroll-view scroll-y class="pe-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px', paddingBottom: '40rpx' }">
      <view class="pe-body">

        <!-- 头像区域 -->
        <view class="pe-avatar-zone">
          <view class="pe-avatar-ring" @tap="openCameraModal">
            <image v-if="localForm.avatar" class="pe-avatar-img" :src="localForm.avatar" mode="aspectFill" />
            <view v-else class="pe-avatar-placeholder">
              <text class="pe-avatar-letter">{{ localForm.name ? localForm.name[0] : '宠' }}</text>
            </view>
          </view>
          <view class="pe-camera-badge" @tap="openCameraModal">
            <text class="pe-camera-icon">📷</text>
          </view>
          <text class="pe-avatar-hint">点击更换头像</text>
        </view>

        <!-- 基本信息 -->
        <view class="pe-section">
          <view class="pe-section-header">
            <view class="pe-section-icon pe-icon-primary">
              <text>📝</text>
            </view>
            <text class="pe-section-title">基本信息</text>
          </view>

          <view class="pe-form">
            <view class="pe-field">
              <view class="pe-field-label">
                <text class="pe-label-text">昵称</text>
                <text class="pe-required-tag">必填</text>
              </view>
              <input
                class="pe-input"
                v-model="localForm.name"
                placeholder="给宠物起个名字吧"
                placeholder-class="pe-placeholder"
                maxlength="20"
              />
            </view>

            <view class="pe-field">
              <view class="pe-field-label">
                <text class="pe-label-text">类别</text>
              </view>
              <picker :range="categoryOptions" range-key="label" :value="categoryIndex" @change="onCategoryPick">
                <view class="pe-picker" :class="{ active: categoryIndex > 0 }">
                  <text :class="categoryIndex > 0 ? 'pe-value' : 'pe-hint'">
                    {{ categoryOptions[categoryIndex]?.label || '请选择类别' }}
                  </text>
                  <text class="pe-arrow">▾</text>
                </view>
              </picker>
            </view>

            <view class="pe-field">
              <view class="pe-field-label">
                <text class="pe-label-text">品种</text>
              </view>
              <input
                class="pe-input"
                v-model="localForm.breed"
                placeholder="如：英短蓝猫、柯基"
                placeholder-class="pe-placeholder"
                maxlength="30"
              />
            </view>
          </view>
        </view>

        <!-- 详细信息 -->
        <view class="pe-section">
          <view class="pe-section-header">
            <view class="pe-section-icon pe-icon-blue">
              <text>📋</text>
            </view>
            <text class="pe-section-title">详细信息</text>
          </view>

          <view class="pe-form">
            <view class="pe-row">
              <view class="pe-field pe-field-half">
                <view class="pe-field-label"><text class="pe-label-text">性别</text></view>
                <picker :range="genderOptions" range-key="label" :value="genderIndex" @change="onGenderPick">
                  <view class="pe-picker" :class="{ active: genderIndex > 0 }">
                    <text :class="genderIndex > 0 ? 'pe-value' : 'pe-hint'">
                      {{ genderOptions[genderIndex]?.label || '请选择' }}
                    </text>
                    <text class="pe-arrow">▾</text>
                  </view>
                </picker>
              </view>

              <view class="pe-field pe-field-half">
                <view class="pe-field-label"><text class="pe-label-text">绝育状态</text></view>
                <picker :range="sterilizedOptions" range-key="label" :value="sterilizedIndex" @change="onSterilizedPick">
                  <view class="pe-picker" :class="{ active: sterilizedIndex > 0 }">
                    <text :class="sterilizedIndex > 0 ? 'pe-value' : 'pe-hint'">
                      {{ sterilizedOptions[sterilizedIndex]?.label || '请选择' }}
                    </text>
                    <text class="pe-arrow">▾</text>
                  </view>
                </picker>
              </view>
            </view>

            <view class="pe-row">
              <view class="pe-field pe-field-half">
                <view class="pe-field-label"><text class="pe-label-text">生日</text></view>
                <picker mode="date" :value="localForm.birthday" @change="onBirthdayPick">
                  <view class="pe-picker" :class="{ active: localForm.birthday }">
                    <text :class="localForm.birthday ? 'pe-value' : 'pe-hint'">
                      {{ localForm.birthday || '选择日期' }}
                    </text>
                    <text class="pe-arrow">▾</text>
                  </view>
                </picker>
              </view>

              <view class="pe-field pe-field-half">
                <view class="pe-field-label">
                  <text class="pe-label-text">体重</text>
                  <text class="pe-unit">kg</text>
                </view>
                <input
                  class="pe-input"
                  type="digit"
                  v-model="localForm.weight"
                  placeholder="0.00"
                  placeholder-class="pe-placeholder"
                />
              </view>
            </view>

            <view class="pe-field">
              <view class="pe-field-label"><text class="pe-label-text">毛色</text></view>
              <input
                class="pe-input"
                v-model="localForm.color"
                placeholder="如：橘色、黑白花"
                placeholder-class="pe-placeholder"
                maxlength="30"
              />
            </view>
          </view>
        </view>

      </view>
    </scroll-view>

    <!-- 相机选择弹窗 -->
    <view v-if="showCameraModal" class="cam-modal" @tap="onCameraMaskTap" @touchmove.stop.prevent>
      <view class="cam-overlay"></view>
      <view class="cam-panel" @tap.stop>
        <view class="cam-handle"></view>
        <view class="cam-header">
          <text class="cam-title">选择头像</text>
          <view class="cam-close" @tap="closeCameraModal">×</view>
        </view>
        <view class="cam-options">
          <view class="cam-option" @tap="chooseFromGallery">
            <view class="cam-option-icon cam-icon-gallery">🖼️</view>
            <view class="cam-option-info">
              <text class="cam-option-name">相册</text>
              <text class="cam-option-desc">从相册中选择照片</text>
            </view>
            <text class="cam-arrow">›</text>
          </view>
          <view class="cam-option" @tap="takePhoto">
            <view class="cam-option-icon cam-icon-photo">📷</view>
            <view class="cam-option-info">
              <text class="cam-option-name">拍照</text>
              <text class="cam-option-desc">使用相机拍摄</text>
            </view>
            <text class="cam-arrow">›</text>
          </view>
        </view>
        <view class="cam-cancel" @tap="closeCameraModal">
          <text class="cam-cancel-text">取消</text>
        </view>
      </view>
    </view>

    <!-- 图片裁剪器 -->
    <image-cropper
      :visible="showCropper"
      :image-src="cropperImageSrc"
      :circular="true"
      @confirm="onCropConfirm"
      @cancel="onCropCancel"
    />

    <!-- 保存成功提示 -->
    <view v-if="showSuccessTip" class="success-tip">
      <text class="success-tip-text">✓ 保存成功</text>
    </view>
  </view>
</template>

<script>
import api from '@/api'
import ImageCropper from '@/components/ImageCropper.vue'

export default {
  name: "PetEdit",
  components: { ImageCropper },
  data() {
    return {
      statusBarHeight: 20,
      isEdit: false,
      editId: null,
      showCameraModal: false,
      uploading: false,
      showCropper: false,
      cropperImageSrc: '',
      submitting: false,
      showSuccessTip: false,
      localForm: {
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
    }
  },
  computed: {
    genderOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "♂ 公", value: 1 },
        { label: "♀ 母", value: 2 },
        { label: "未知", value: 0 }
      ]
    },
    categoryOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "🐱 猫咪", value: 1 },
        { label: "🐶 狗狗", value: 2 },
        { label: "🐾 其他", value: 0 }
      ]
    },
    sterilizedOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "已绝育", value: 1 },
        { label: "未绝育", value: 0 }
      ]
    },
    genderIndex() {
      const idx = this.genderOptions.findIndex(x => x.value === this.localForm.gender)
      return idx === -1 ? 0 : idx
    },
    categoryIndex() {
      const idx = this.categoryOptions.findIndex(x => x.value === this.localForm.category)
      return idx === -1 ? 0 : idx
    },
    sterilizedIndex() {
      const idx = this.sterilizedOptions.findIndex(x => x.value === this.localForm.sterilized)
      return idx === -1 ? 0 : idx
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    if (options && options.id) {
      this.isEdit = true
      this.editId = options.id
      this.loadPetData(options.id)
    }
  },
  methods: {
    async loadPetData(id) {
      try {
        const res = await api.pet.getPetDetail(id)
        if (res && res.success && res.data) {
          const p = res.data
          this.localForm = {
            id: p.id,
            name: p.name || '',
            breed: p.breed || '',
            gender: typeof p.gender === 'number' ? p.gender : 0,
            birthday: p.birthday || '',
            weight: p.weight || '',
            color: p.color || '',
            avatar: p.avatar || '',
            category: typeof p.category === 'number' ? p.category : 0,
            sterilized: typeof p.sterilized === 'number' ? p.sterilized : 0
          }
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    goBack() {
      if (this.submitting) return
      uni.navigateBack()
    },
    normalizeForm(v) {
      const src = v || {}
      return {
        id: src.id || null,
        name: src.name || '',
        breed: src.breed || '',
        gender: typeof src.gender === 'number' ? src.gender : 0,
        birthday: src.birthday || '',
        weight: src.weight || '',
        color: src.color || '',
        avatar: src.avatar || '',
        category: typeof src.category === 'number' ? src.category : 0,
        sterilized: typeof src.sterilized === 'number' ? src.sterilized : 0
      }
    },
    async onSave() {
      if (!this.localForm.name.trim()) {
        uni.showToast({ title: '请输入宠物昵称', icon: 'none' })
        return
      }
      if (this.submitting) return
      this.submitting = true

      try {
        let res
        if (this.isEdit && this.editId) {
          res = await api.pet.updatePet(this.editId, this.localForm)
        } else {
          res = await api.pet.createPet(this.localForm)
        }

        if (res && res.success) {
          this.showSuccessTip = true
          setTimeout(() => {
            this.showSuccessTip = false
            uni.navigateBack()
          }, 1200)
        } else {
          uni.showToast({ title: (res && res.message) || '保存失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络错误', icon: 'none' })
      } finally {
        setTimeout(() => { this.submitting = false }, 800)
      }
    },
    openCameraModal() {
      this.showCameraModal = true
    },
    closeCameraModal() {
      this.showCameraModal = false
    },
    onCameraMaskTap(e) {
      if (e && e.target && e.currentTarget && e.target !== e.currentTarget) return
      this.closeCameraModal()
    },
    onGenderPick(e) {
      const idx = Number(e.detail.value || 0)
      const opt = this.genderOptions[idx]
      if (!opt || opt.value === -1) return
      this.localForm.gender = opt.value
    },
    onBirthdayPick(e) {
      this.localForm.birthday = e.detail.value
    },
    onCategoryPick(e) {
      const idx = Number(e.detail.value || 0)
      const opt = this.categoryOptions[idx]
      if (!opt || opt.value === -1) return
      this.localForm.category = opt.value
    },
    onSterilizedPick(e) {
      const idx = Number(e.detail.value || 0)
      const opt = this.sterilizedOptions[idx]
      if (!opt || opt.value === -1) return
      this.localForm.sterilized = opt.value
    },
    chooseFromGallery() {
      uni.chooseImage({
        count: 1,
        sourceType: ["album"],
        success: (res) => {
          const file = res.tempFilePaths?.[0]
          if (file) {
            this.cropperImageSrc = file
            this.showCropper = true
          }
          this.showCameraModal = false
        },
        fail: () => { this.showCameraModal = false }
      })
    },
    takePhoto() {
      uni.chooseImage({
        count: 1,
        sourceType: ["camera"],
        success: (res) => {
          const file = res.tempFilePaths?.[0]
          if (file) {
            this.cropperImageSrc = file
            this.showCropper = true
          }
          this.showCameraModal = false
        },
        fail: () => { this.showCameraModal = false }
      })
    },
    async onCropConfirm(croppedPath) {
      this.showCropper = false
      this.uploadImageToServer(croppedPath)
    },
    onCropCancel() {
      this.showCropper = false
    },
    async uploadImageToServer(filePath) {
      this.uploading = true
      uni.showLoading({ title: '上传中...' })
      try {
        const res = await api.pet.uploadImage(filePath)
        if (res.success && res.data?.url) {
          this.localForm.avatar = res.data.url
          uni.showToast({ title: '上传成功', icon: 'success' })
        } else {
          throw new Error(res.message || '上传失败')
        }
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' })
      } finally {
        this.uploading = false
        uni.hideLoading()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6b35;
$primary-light: #ff8c5a;
$gray-50: #fafafa;
$gray-100: #f5f5f7;
$gray-200: #e5e5ea;
$gray-300: #d1d1d6;
$gray-500: #8e8e93;
$gray-800: #1c1c1e;

/* ====== 页面容器 ====== */
.pet-edit-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff9f5 0%, #fafafa 100%);
}

/* ====== 导航栏 ====== */
.pe-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(209, 213, 219, 0.2);
}

.pe-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 46px;
  padding: 0 16px;
}

.pe-back {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;

  &:active {
    background: $gray-100;
  }
}

.pe-back-icon {
  font-size: 18px;
  font-weight: 700;
  color: $gray-800;
}

.pe-nav-title {
  font-size: 17px;
  font-weight: 700;
  color: $gray-800;
}

.pe-action-btn {
  padding: 7rpx 24rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, $primary, $primary-light);

  &.saving {
    opacity: 0.65;
    pointer-events: none;
  }

  &:active:not(.saving) {
    transform: scale(0.95);
    opacity: 0.9;
  }
}

.pe-action-text {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1rpx;
}

/* ====== 滚动区 ====== */
.pe-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.pe-body {
  padding: 0 28rpx;
}

/* ====== 头像区域 ====== */
.pe-avatar-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 0 32rpx;
}

.pe-avatar-ring {
  width: 180rpx;
  height: 180rpx;
  border-radius: 90rpx;
  padding: 4rpx;
  background: linear-gradient(135deg, $primary, $primary-light, #ffb347);
  position: relative;

  &:active {
    transform: scale(0.96);
  }
}

.pe-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 86rpx;
  background: #fff;
}

.pe-avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 86rpx;
  background: linear-gradient(135deg, $primary 0%, $primary-light 50%, #ffb347 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.pe-avatar-letter {
  font-size: 72rpx;
  color: #fff;
  font-weight: 700;
}

.pe-camera-badge {
  position: absolute;
  bottom: 4rpx;
  right: 4rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 26rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.12);
  border: 3rpx solid #fff;
}

.pe-camera-icon {
  font-size: 24rpx;
}

.pe-avatar-hint {
  margin-top: 16rpx;
  font-size: 25rpx;
  color: $gray-500;
  font-weight: 500;
}

/* ====== 卡片区块 ====== */
.pe-section {
  background: rgba(255, 255, 255, 0.88);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow:
    0 4rpx 20rpx rgba(31, 38, 135, 0.06),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.02);
  backdrop-filter: blur(16px);
  border: 1rpx solid rgba(200, 210, 220, 0.35);
}

.pe-section-header {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 22rpx;
  padding-bottom: 18rpx;
  border-bottom: 1rpx solid rgba(240, 242, 245, 0.8);
}

.pe-section-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05));
}

.pe-icon-blue {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
}

.pe-section-title {
  font-size: 29rpx;
  font-weight: 700;
  color: $gray-800;
  letter-spacing: 0.3rpx;
}

/* ====== 表单 ====== */
.pe-form {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.pe-row {
  display: flex;
  gap: 18rpx;
}

.pe-field {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.pe-field-half {
  flex: 1;
}

.pe-field-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.pe-label-text {
  font-size: 27rpx;
  color: $gray-800;
  font-weight: 600;
  letter-spacing: 0.3rpx;
}

.pe-required-tag {
  font-size: 21rpx;
  color: #ef4444;
  font-weight: 600;
  background: rgba(239, 68, 68, 0.07);
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}

.pe-unit {
  font-size: 23rpx;
  color: $gray-500;
  font-weight: 500;
}

.pe-input {
  width: 100%;
  height: 80rpx;
  background: $gray-50;
  border-radius: 14rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: $gray-800;
  font-weight: 500;
  box-sizing: border-box;
  transition: all 0.25s ease;
  border: 2rpx solid transparent;

  &:focus {
    background: #fff;
    border-color: rgba(255, 107, 53, 0.35);
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.06);
  }
}

.pe-placeholder {
  color: $gray-300;
  font-weight: 400;
}

.pe-picker {
  min-height: 80rpx;
  background: $gray-50;
  border-radius: 14rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.25s ease;
  border: 2rpx solid transparent;

  &:active {
    background: $gray-100;
    transform: scale(0.99);

    .pe-arrow {
      transform: rotate(180deg);
      color: $primary;
    }
  }

  &.active {
    background: rgba(255, 107, 53, 0.03);
    border-color: rgba(255, 107, 53, 0.2);

    .pe-value {
      color: $primary;
      font-weight: 600;
    }

    .pe-arrow {
      color: $primary;
    }
  }
}

.pe-hint {
  font-size: 28rpx;
  color: $gray-300;
  font-weight: 400;
}

.pe-value {
  font-size: 28rpx;
  color: $gray-800;
  font-weight: 500;
}

.pe-arrow {
  font-size: 26rpx;
  color: $gray-300;
  font-weight: 600;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

/* ====== 相机弹窗 ====== */
.cam-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 20010;
  display: flex;
  align-items: flex-end;
}

.cam-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
}

.cam-panel {
  position: relative;
  width: 100%;
  background: #fff;
  border-top-left-radius: 28rpx;
  border-top-right-radius: 28rpx;
  padding: 24rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -10rpx 40rpx rgba(0, 0, 0, 0.15);
}

.cam-handle {
  width: 64rpx;
  height: 5rpx;
  background: $gray-300;
  border-radius: 3rpx;
  margin: 0 auto 24rpx;
}

.cam-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
}

.cam-title {
  font-size: 33rpx;
  font-weight: 700;
  color: $gray-800;
}

.cam-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: $gray-50;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: $gray-500;

  &:active {
    background: $gray-200;
  }
}

.cam-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.cam-option {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  background: $gray-50;
  border-radius: 20rpx;
  transition: all 0.25s ease;

  &:active {
    background: rgba(255, 107, 53, 0.04);
    transform: scale(0.98);

    .cam-arrow {
      color: $primary;
    }
  }
}

.cam-option-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
  font-size: 38rpx;
}

.cam-icon-gallery {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}

.cam-icon-photo {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
}

.cam-option-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.cam-option-name {
  font-size: 29rpx;
  color: $gray-800;
  font-weight: 600;
}

.cam-option-desc {
  font-size: 24rpx;
  color: $gray-500;
}

.cam-arrow {
  font-size: 34rpx;
  color: $gray-300;
  font-weight: 600;
  flex-shrink: 0;
}

.cam-cancel {
  width: 100%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-50;
  border-radius: 20rpx;

  &:active {
    background: $gray-100;
  }
}

.cam-cancel-text {
  font-size: 29rpx;
  color: $gray-500;
  font-weight: 600;
}

/* ====== 成功提示 ====== */
.success-tip {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 30000;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  padding: 40rpx 64rpx;
  border-radius: 24rpx;
  box-shadow: 0 20rpx 56rpx rgba(82, 196, 26, 0.4), 0 8rpx 24rpx rgba(82, 196, 26, 0.2);
  animation: tipPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1.55);
}

@keyframes tipPop {
  from { opacity: 0; transform: translate(-50%, -50%) scale(0.6); }
  to { opacity: 1; transform: translate(-50%, -50%) scale(1); }
}

.success-tip-text {
  font-size: 32rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2rpx;
}
</style>
