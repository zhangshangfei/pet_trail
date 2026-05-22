<template>
  <view class="apm-modal" @tap="emitClose" @touchmove.stop.prevent>
    <!-- 遮罩层 -->
    <view class="apm-overlay"></view>

    <!-- 主面板 -->
    <view class="apm-panel" @tap.stop>
      <!-- 头部 -->
      <view class="apm-header">
        <view class="apm-header-left">
          <text class="apm-title">{{ localForm.id ? '编辑宠物' : '添加宠物' }}</text>
          <view class="apm-title-decorator"></view>
        </view>
        <view class="apm-close-btn" @tap="emitClose">
          <text class="apm-close-icon">×</text>
        </view>
      </view>

      <!-- 滚动内容 -->
      <scroll-view class="apm-content" scroll-y @tap.stop>
        <!-- 头像上传区 -->
        <view class="apm-avatar-section">
          <view class="apm-avatar-container" @tap="openCameraModal">
            <view class="apm-avatar-ring">
              <view v-if="!localForm.avatar" class="apm-avatar-empty">
                <text class="apm-avatar-text">宠</text>
              </view>
              <image v-else class="apm-avatar-img" :src="localForm.avatar" mode="aspectFill" />
            </view>
            <view class="apm-camera-badge">
              <text class="apm-camera-icon">📷</text>
            </view>
          </view>
          <text class="apm-avatar-tip">点击更换头像</text>
        </view>

        <!-- 基本信息卡片 -->
        <view class="apm-card">
          <view class="apm-card-header">
            <view class="apm-card-icon-wrap">
              <text class="apm-card-icon">📝</text>
            </view>
            <text class="apm-card-title">基本信息</text>
          </view>

          <view class="apm-form-group">
            <!-- 昵称 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">昵称</text>
                <text class="apm-required">必填</text>
              </view>
              <input 
                class="apm-input" 
                v-model="localForm.name" 
                placeholder="给宠物起个名字吧"
                placeholder-class="apm-placeholder"
              />
            </view>

            <!-- 类别 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">类别</text>
              </view>
              <picker :range="categoryOptions" range-key="label" :value="categoryIndex" @change="onCategoryPick">
                <view class="apm-picker" :class="{ 'is-active': categoryIndex > 0 }">
                  <text :class="categoryIndex > 0 ? 'apm-value' : 'apm-hint'">
                    {{ categoryOptions[categoryIndex]?.label || '请选择类别' }}
                  </text>
                  <text class="apm-picker-arrow">▾</text>
                </view>
              </picker>
            </view>

            <!-- 品种 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">品种</text>
              </view>
              <input 
                class="apm-input" 
                v-model="localForm.breed" 
                placeholder="如：英短蓝猫、柯基"
                placeholder-class="apm-placeholder"
              />
            </view>
          </view>
        </view>

        <!-- 详细信息卡片 -->
        <view class="apm-card">
          <view class="apm-card-header">
            <view class="apm-card-icon-wrap apm-icon-blue">
              <text class="apm-card-icon">📋</text>
            </view>
            <text class="apm-card-title">详细信息</text>
          </view>

          <view class="apm-form-group">
            <!-- 性别 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">性别</text>
              </view>
              <picker :range="genderOptions" range-key="label" :value="genderIndex" @change="onGenderPick">
                <view class="apm-picker" :class="{ 'is-active': genderIndex > 0 }">
                  <text :class="genderIndex > 0 ? 'apm-value' : 'apm-hint'">
                    {{ genderOptions[genderIndex]?.label || '请选择性别' }}
                  </text>
                  <text class="apm-picker-arrow">▾</text>
                </view>
              </picker>
            </view>

            <!-- 生日 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">生日</text>
              </view>
              <picker mode="date" :value="localForm.birthday" @change="onBirthdayPick">
                <view class="apm-picker" :class="{ 'is-active': localForm.birthday }">
                  <text :class="localForm.birthday ? 'apm-value' : 'apm-hint'">
                    {{ localForm.birthday || '请选择出生日期' }}
                  </text>
                  <text class="apm-picker-arrow">▾</text>
                </view>
              </picker>
            </view>

            <!-- 体重 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">体重</text>
                <text class="apm-unit">kg</text>
              </view>
              <input 
                class="apm-input" 
                type="digit" 
                v-model="localForm.weight" 
                placeholder="0.00"
                placeholder-class="apm-placeholder"
              />
            </view>

            <!-- 毛色 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">毛色</text>
              </view>
              <input 
                class="apm-input" 
                v-model="localForm.color" 
                placeholder="如：橘色、黑白花"
                placeholder-class="apm-placeholder"
              />
            </view>

            <!-- 绝育状态 -->
            <view class="apm-form-item">
              <view class="apm-label-row">
                <text class="apm-label">绝育状态</text>
              </view>
              <picker :range="sterilizedOptions" range-key="label" :value="sterilizedIndex" @change="onSterilizedPick">
                <view class="apm-picker" :class="{ 'is-active': sterilizedIndex > 0 }">
                  <text :class="sterilizedIndex > 0 ? 'apm-value' : 'apm-hint'">
                    {{ sterilizedOptions[sterilizedIndex]?.label || '请选择状态' }}
                  </text>
                  <text class="apm-picker-arrow">▾</text>
                </view>
              </picker>
            </view>
          </view>
        </view>

        <!-- 按钮组 -->
        <view class="apm-actions">
          <button class="apm-btn apm-btn-cancel" @tap="emitClose">取消</button>
          <button class="apm-btn apm-btn-primary" @tap="emitSave">
            <text class="apm-btn-text">保存</text>
          </button>
        </view>
      </scroll-view>
    </view>

    <!-- 相机选择弹窗 -->
    <view v-if="showCameraModal" class="camera-modal" @tap="onCameraMaskTap" @touchmove.stop.prevent>
      <view class="camera-overlay"></view>
      <view class="camera-panel" @tap.stop>
        <view class="camera-handle"></view>
        
        <view class="camera-header">
          <text class="camera-title">选择头像</text>
          <view class="camera-close" @tap="closeCameraModal">
            <text class="camera-close-icon">×</text>
          </view>
        </view>

        <view class="camera-options">
          <view class="camera-option" @tap="chooseFromGallery">
            <view class="option-icon option-gallery">
              <text>🖼️</text>
            </view>
            <view class="option-info">
              <text class="option-name">相册</text>
              <text class="option-desc">从相册中选择照片</text>
            </view>
            <text class="option-arrow">›</text>
          </view>

          <view class="camera-option" @tap="takePhoto">
            <view class="option-icon option-photo">
              <text>📷</text>
            </view>
            <view class="option-info">
              <text class="option-name">拍照</text>
              <text class="option-desc">使用相机拍摄</text>
            </view>
            <text class="option-arrow">›</text>
          </view>
        </view>

        <view class="camera-cancel" @tap="closeCameraModal">
          <text class="camera-cancel-text">取消</text>
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
  </view>
</template>

<script>
import api from '@/api'
import ImageCropper from '@/components/ImageCropper.vue'

export default {
  name: "AddPetModal",
  components: { ImageCropper },
  props: {
    initialForm: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      showCameraModal: false,
      uploading: false,
      showCropper: false,
      cropperImageSrc: '',
      localForm: this.normalizeForm(this.initialForm)
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
  watch: {
    initialForm: {
      deep: true,
      handler(v) {
        this.localForm = this.normalizeForm(v)
      }
    }
  },
  methods: {
    emitClose() {
      this.$emit("close")
    },
    normalizeForm(v) {
      const src = v || {}
      return {
        id: src.id || null,
        name: src.name || "",
        breed: src.breed || "",
        gender: typeof src.gender === "number" ? src.gender : 0,
        birthday: src.birthday || "",
        weight: src.weight || "",
        color: src.color || "",
        avatar: src.avatar || "",
        category: typeof src.category === "number" ? src.category : 0,
        sterilized: typeof src.sterilized === "number" ? src.sterilized : 0
      }
    },
    emitSave() {
      if (!this.localForm.name.trim()) {
        uni.showToast({ title: "请输入宠物昵称", icon: "none" })
        return
      }
      this.$emit("save", { ...this.localForm })
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
      const idx = Number(e.detail.value)
      const opt = this.genderOptions[idx]
      if (!opt || opt.value === -1) return
      this.localForm.gender = opt.value
    },
    onBirthdayPick(e) {
      this.localForm.birthday = e.detail.value
    },
    onCategoryPick(e) {
      const idx = Number(e.detail.value)
      const opt = this.categoryOptions[idx]
      if (!opt || opt.value === -1) return
      this.localForm.category = opt.value
    },
    onSterilizedPick(e) {
      const idx = Number(e.detail.value)
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
/* ============================================
   AddPetModal 统一设计系统 v4.0
   与 me/index.vue 风格完全一致
   ============================================ */

/* 设计变量 - 与 me/index.vue 统一 */
$primary: #ff6b35;
$primary-light: #ff8c5a;
$bg-color: #f2f2f7;
$white: #ffffff;
$gray-50: #fafafa;
$gray-100: #f5f5f7;
$gray-200: #e5e5ea;
$gray-300: #d1d1d6;
$gray-500: #8e8e93;
$gray-800: #1c1c1e;

$radius-sm: 12rpx;
$radius-md: 20rpx;
$radius-lg: 28rpx;

/* ========== 主弹窗 ========== */
.apm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20000;
  display: flex;
  align-items: flex-end;
}

.apm-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  animation: overlayFadeIn 0.25s ease-out;
}

@keyframes overlayFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.apm-panel {
  position: relative;
  width: 100%;
  max-height: 90vh;
  background: $white;
  border-top-left-radius: $radius-lg;
  border-top-right-radius: $radius-lg;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 -10rpx 40rpx rgba(0, 0, 0, 0.12);
  animation: panelSlideUp 0.35s cubic-bezier(0.32, 0.72, 0, 1);
}

@keyframes panelSlideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

/* ========== 头部 ========== */
.apm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid $gray-100;
}

.apm-header-left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.apm-title {
  font-size: 34rpx;
  font-weight: 700;
  color: $gray-800;
  letter-spacing: 0.5rpx;
}

.apm-title-decorator {
  width: 48rpx;
  height: 4rpx;
  background: linear-gradient(90deg, $primary, $primary-light);
  border-radius: 2rpx;
}

.apm-close-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 30rpx;
  background: $gray-50;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    transform: scale(0.92);
    background: $gray-200;
  }
}

.apm-close-icon {
  font-size: 32rpx;
  color: $gray-500;
  line-height: 1;
}

/* ========== 内容滚动区 ========== */
.apm-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
}

/* ========== 头像区域 ========== */
.apm-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 36rpx;
  gap: 16rpx;
}

.apm-avatar-container {
  position: relative;
  
  &:active {
    transform: scale(0.96);
  }
}

.apm-avatar-ring {
  width: 180rpx;
  height: 180rpx;
  border-radius: 90rpx;
  padding: 4rpx;
  background: linear-gradient(135deg, $primary, $primary-light, #ffb347);
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.25);
}

.apm-avatar-empty {
  width: 100%;
  height: 100%;
  border-radius: 86rpx;
  background: linear-gradient(135deg, $primary 0%, $primary-light 50%, #ffb347 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.apm-avatar-text {
  font-size: 72rpx;
  color: $white;
  font-weight: 700;
}

.apm-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 86rpx;
  background: $white;
}

.apm-camera-badge {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 26rpx;
  background: $white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  border: 3rpx solid $white;
}

.apm-camera-icon {
  font-size: 24rpx;
}

.apm-avatar-tip {
  font-size: 24rpx;
  color: $gray-500;
  font-weight: 500;
}

/* ========== 卡片容器 ========== */
.apm-card {
  margin: 0 28rpx 24rpx;
  background: $white;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.apm-card-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 28rpx 20rpx;
  background: linear-gradient(180deg, $white 0%, #fff9f5 100%);
  border-bottom: 1rpx solid rgba(255, 107, 53, 0.08);
}

.apm-card-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  border-radius: 14rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05));
  display: flex;
  align-items: center;
  justify-content: center;

  &.apm-icon-blue {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
  }
}

.apm-card-icon {
  font-size: 26rpx;
}

.apm-card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $gray-800;
  letter-spacing: 0.3rpx;
}

/* ========== 表单组 ========== */
.apm-form-group {
  padding: 8rpx 28rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

/* ========== 表单项 ========== */
.apm-form-item {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.apm-label-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.apm-label {
  font-size: 27rpx;
  color: $gray-800;
  font-weight: 600;
  letter-spacing: 0.3rpx;
}

.apm-required {
  font-size: 22rpx;
  color: #ef4444;
  font-weight: 600;
  background: rgba(239, 68, 68, 0.08);
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}

.apm-unit {
  font-size: 24rpx;
  color: $gray-500;
  font-weight: 500;
}

/* ========== 输入框 ========== */
.apm-input {
  width: 88%;
  height: 76rpx;
  background: $gray-50;
  border-radius: $radius-sm;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: $gray-800;
  font-weight: 500;
  transition: all 0.25s ease;
  border: 2rpx solid transparent;

  &:focus {
    background: $white;
    border-color: rgba(255, 107, 53, 0.4);
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.08);
  }
}

.apm-placeholder {
  color: $gray-300;
  font-weight: 400;
}

/* ========== 选择器（现代化设计）========== */
.apm-picker {
  width: 88%;
  min-height: 76rpx;
  background: $gray-50;
  border-radius: $radius-sm;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.25s ease;
  border: 2rpx solid transparent;

  &:active {
    background: $gray-100;
    transform: scale(0.99);

    .apm-picker-arrow {
      transform: rotate(180deg);
      color: $primary;
    }
  }

  &.is-active {
    background: rgba(255, 107, 53, 0.03);
    border-color: rgba(255, 107, 53, 0.2);

    .apm-value {
      color: $primary;
      font-weight: 600;
    }

    .apm-picker-arrow {
      color: $primary;
    }
  }
}

.apm-hint {
  font-size: 28rpx;
  color: $gray-300;
  font-weight: 400;
}

.apm-value {
  font-size: 29rpx;
  color: $gray-800;
  font-weight: 500;
  transition: all 0.25s ease;
}

.apm-picker-arrow {
  font-size: 26rpx;
  color: $gray-300;
  font-weight: 600;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

/* ========== 按钮组 ========== */
.apm-actions {
  display: flex;
  gap: 20rpx;
  padding: 32rpx 28rpx 0;
}

.apm-btn {
  flex: 1;
  height: 88rpx;
  border-radius: $radius-md;
  font-size: 30rpx;
  font-weight: 700;
  border: none;
  line-height: 88rpx;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &::after {
    border: none;
  }

  &:active {
    transform: scale(0.97);
  }
}

.apm-btn-cancel {
  background: $gray-50;
  color: $gray-500;
  border: 2rpx solid $gray-200;

  &:active {
    background: $gray-100;
  }
}

.apm-btn-primary {
  background: linear-gradient(135deg, $primary, $primary-light);
  color: $white;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35), 0 4rpx 12rpx rgba(255, 107, 53, 0.2);

  &:active {
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.45), 0 2rpx 8rpx rgba(255, 107, 53, 0.25);
  }
}

.apm-btn-text {
  letter-spacing: 2rpx;
}

/* ========== 相机选择弹窗 ========== */
.camera-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20010;
  display: flex;
  align-items: flex-end;
}

.camera-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  animation: overlayFadeIn 0.2s ease-out;
}

.camera-panel {
  position: relative;
  width: 100%;
  background: $white;
  border-top-left-radius: $radius-lg;
  border-top-right-radius: $radius-lg;
  padding: 24rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -10rpx 40rpx rgba(0, 0, 0, 0.15);
  animation: panelSlideUp 0.3s cubic-bezier(0.32, 0.72, 0, 1);
}

.camera-handle {
  width: 64rpx;
  height: 5rpx;
  background: $gray-300;
  border-radius: 3rpx;
  margin: 0 auto 24rpx;
}

.camera-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
}

.camera-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $gray-800;
}

.camera-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: $gray-50;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    transform: scale(0.92);
    background: $gray-200;
  }
}

.camera-close-icon {
  font-size: 30rpx;
  color: $gray-500;
  line-height: 1;
}

.camera-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.camera-option {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  background: $gray-50;
  border-radius: $radius-md;
  transition: all 0.25s ease;

  &:active {
    background: rgba(255, 107, 53, 0.04);
    transform: scale(0.98);

    .option-arrow {
      color: $primary;
      transform: translateX(4rpx);
    }
  }
}

.option-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;

  &.option-gallery {
    background: linear-gradient(135deg, #fef3c7, #fde68a);
  }

  &.option-photo {
    background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  }

  text {
    font-size: 38rpx;
  }
}

.option-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.option-name {
  font-size: 29rpx;
  color: $gray-800;
  font-weight: 600;
}

.option-desc {
  font-size: 24rpx;
  color: $gray-500;
}

.option-arrow {
  font-size: 34rpx;
  color: $gray-300;
  font-weight: 600;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.camera-cancel {
  width: 100%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-50;
  border-radius: $radius-md;
  transition: all 0.2s ease;

  &:active {
    background: $gray-100;
  }
}

.camera-cancel-text {
  font-size: 29rpx;
  color: $gray-500;
  font-weight: 600;
}
</style>
