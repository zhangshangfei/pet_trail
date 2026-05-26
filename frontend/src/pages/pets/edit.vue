<template>
  <view class="pet-edit-page">
    <view class="pe-header">
      <view class="header-bg">
        <view class="orb orb-1"></view>
        <view class="orb orb-2"></view>
        <view class="orb orb-3"></view>
      </view>
      <view class="pe-header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="pe-back" @tap="goBack">
          <view class="pe-back-arrow"></view>
        </view>
        <text class="pe-title">{{ isEdit ? '编辑宠物' : '添加宠物' }}</text>
        <view class="pe-header-right"></view>
      </view>

      <view class="pe-avatar-section" @tap="onPickAvatar">
        <view class="avatar-glow"></view>
        <image v-if="localForm.avatar" class="pe-avatar" :src="localForm.avatar" mode="aspectFill" />
        <view v-else class="pe-avatar-empty">
          <text class="pe-avatar-letter">{{ localForm.name ? localForm.name[0] : '宠' }}</text>
        </view>
        <view class="pe-avatar-badge">
          <text class="pe-badge-icon">✎</text>
        </view>
      </view>

      <view class="wave-decoration">
        <svg viewBox="0 0 1440 120" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
          <path class="wave-path wave-path-1" d="M0,64 C288,120 576,20 864,64 C1152,108 1344,40 1440,64 L1440,120 L0,120 Z" fill="rgba(255,107,107,0.12)"/>
          <path class="wave-path wave-path-2" d="M0,80 C360,130 720,30 1080,80 C1260,105 1380,60 1440,80 L1440,120 L0,120 Z" fill="rgba(255,160,122,0.08)"/>
          <path class="wave-path wave-path-3" d="M0,50 C240,90 480,10 720,50 C960,90 1200,30 1440,50 L1440,100 L0,100Z" fill="rgba(255,220,180,0.06)"/>
        </svg>
      </view>
    </view>

    <scroll-view scroll-y class="pe-scroll" :style="{ paddingTop: (statusBarHeight + 200) + 'px' }">
      <view class="pe-content">
        <view class="glass-form-card">
          <view class="gfc-section">
            <view class="gfc-icon-wrap gfc-icon--info">
              <text class="gfc-icon">📋</text>
            </view>
            <text class="gfc-title">基本信息</text>
            <view class="gfc-line"></view>
          </view>

          <view class="gfc-field" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">昵称</text>
              <text class="gfc-required">*</text>
            </view>
            <input class="gfc-input" v-model="localForm.name" placeholder="请输入宠物昵称" maxlength="20" />
          </view>

          <view class="gfc-field" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">品种</text>
            </view>
            <input class="gfc-input" v-model="localForm.breed" placeholder="如：泰迪、英短、布偶猫" maxlength="30" />
          </view>

          <view class="gfc-field" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">类别</text>
            </view>
            <picker :range="categoryLabels" :value="categoryIndex" @change="onCategoryChange">
              <view class="gfc-picker">
                <text :class="['gfc-value', categoryIndex === 0 && !localForm.category && 'gfc-placeholder']">{{ categoryLabels[categoryIndex] }}</text>
                <view class="gfc-chevron"></view>
              </view>
            </picker>
          </view>

          <view class="gfc-field" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">性别</text>
            </view>
            <picker :range="genderLabels" :value="genderIndex" @change="onGenderChange">
              <view class="gfc-picker">
                <text :class="['gfc-value', genderIndex === 0 && !localForm.gender && 'gfc-placeholder']">{{ genderLabels[genderIndex] }}</text>
                <view class="gfc-chevron"></view>
              </view>
            </picker>
          </view>

          <view class="gfc-field" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">生日</text>
            </view>
            <picker mode="date" :value="localForm.birthday || ''" :start="'1970-01-01'" :end="todayStr" @change="onBirthdayChange">
              <view class="gfc-picker">
                <text :class="['gfc-value', !localForm.birthday && 'gfc-placeholder']">{{ localForm.birthday || '请选择日期' }}</text>
                <view class="gfc-chevron"></view>
              </view>
            </picker>
          </view>
        </view>

        <view class="glass-form-card">
          <view class="gfc-section">
            <view class="gfc-icon-wrap gfc-icon--look">
              <text class="gfc-icon">🎨</text>
            </view>
            <text class="gfc-title">外貌特征</text>
            <view class="gfc-line"></view>
          </view>

          <view class="gfc-field" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">毛色</text>
            </view>
            <input class="gfc-input" v-model="localForm.color" placeholder="如：白色、棕色、花色" maxlength="20" />
          </view>

          <view class="gfc-field gfc-field--last" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">体重</text>
            </view>
            <view class="gfc-input-with-unit">
              <input class="gfc-input" v-model="localForm.weight" type="digit" placeholder="请输入体重" maxlength="6" />
              <text class="gfc-unit">kg</text>
            </view>
          </view>
        </view>

        <view class="glass-form-card">
          <view class="gfc-section">
            <view class="gfc-icon-wrap gfc-icon--health">
              <text class="gfc-icon">💊</text>
            </view>
            <text class="gfc-title">健康状态</text>
            <view class="gfc-line"></view>
          </view>

          <view class="gfc-field gfc-field--last" hover-class="gfc-field--hover">
            <view class="gfc-label-row">
              <text class="gfc-label">绝育状态</text>
            </view>
            <picker :range="sterilizedLabels" :value="sterilizedIndex" @change="onSterilizedChange">
              <view class="gfc-picker">
                <text :class="['gfc-value', sterilizedIndex === 0 && !localForm.sterilized && 'gfc-placeholder']">{{ sterilizedLabels[sterilizedIndex] }}</text>
                <view class="gfc-chevron"></view>
              </view>
            </picker>
          </view>
        </view>

        <view class="bottom-spacer"></view>
      </view>
    </scroll-view>

    <view class="pe-bottom-bar">
      <view class="pe-save-btn" :class="{ 'pe-save-btn--disabled': saving }" @tap="onSave">
        <view class="btn-shine"></view>
        <text class="pe-save-btn-text">{{ saving ? '保存中...' : '保存修改' }}</text>
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
          res = await petApi.updatePet(this.localForm.id, payload)
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

/* ========== 玻璃拟态头部 ========== */
.pe-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(
    180deg,
    rgba(255, 122, 61, 1) 0%,
    rgba(255, 90, 61, 0.95) 20%,
    rgba(255, 77, 79, 0.85) 50%,
    rgba(255, 107, 107, 0.7) 80%,
    rgba(255, 160, 122, 0.6) 100%
  );
  overflow: visible;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40rpx);
  opacity: 0.6;
  animation: floatOrb 8s ease-in-out infinite;
}

.orb-1 {
  width: 300rpx;
  height: 300rpx;
  background: rgba(255, 255, 255, 0.3);
  top: -100rpx;
  right: 100rpx;
}

.orb-2 {
  width: 200rpx;
  height: 200rpx;
  background: rgba(255, 220, 180, 0.4);
  bottom: 100rpx;
  left: -60rpx;
  animation-delay: -3s;
}

.orb-3 {
  width: 150rpx;
  height: 150rpx;
  background: rgba(255, 255, 255, 0.2);
  top: 45%;
  left: 55%;
  animation-delay: -5s;
}

@keyframes floatOrb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-30rpx, 25rpx) scale(1.05); }
  66% { transform: translate(25rpx, -15rpx) scale(0.95); }
}

.pe-header-bar {
  position: relative;
  z-index: 10;
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.pe-back {
  width: 68rpx;
  height: 68rpx;
  border-radius: 34rpx;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 6rpx 24rpx rgba(0, 0, 0, 0.12),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.4);
  border: 1rpx solid rgba(255, 255, 255, 0.35);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.pe-back:active {
  background: rgba(255, 255, 255, 0.38);
  transform: scale(0.9);
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
  font-weight: 800;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 8rpx rgba(180, 30, 10, 0.3);
}

.pe-header-right {
  width: 68rpx;
}

/* ========== 头像区域（在头部内） ========== */
.pe-avatar-section {
  position: relative;
  z-index: 5;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding-bottom: 48rpx;
  margin-top: 24rpx;
}

.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 220rpx;
  height: 220rpx;
  transform: translate(-50%, -55%);
  border-radius: 50%;
  background: radial-gradient(circle at center, rgba(255, 106, 61, 0.35) 0%, rgba(255, 106, 61, 0.08) 60%, transparent 70%);
  filter: blur(15rpx);
  animation: pulseGlow 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes pulseGlow {
  0%, 100% { transform: translate(-50%, -55%) scale(1); opacity: 0.7; }
  50% { transform: translate(-50%, -55%) scale(1.15); opacity: 1; }
}

.pe-avatar {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(255, 255, 255, 0.95);
  box-shadow:
    0 12rpx 40rpx rgba(180, 40, 10, 0.3),
    0 4rpx 12rpx rgba(0, 0, 0, 0.1),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.6);
  background: linear-gradient(135deg, #fef3ec 0%, #fce7db 100%);
  z-index: 2;
}

.pe-avatar-empty {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff8a50 0%, #ff6a3d 50%, #ff5722 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 6rpx solid rgba(255, 255, 255, 0.95);
  box-shadow:
    0 12rpx 40rpx rgba(180, 40, 10, 0.35),
    0 4rpx 12rpx rgba(0, 0, 0, 0.1),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.3);
  z-index: 2;
}

.pe-avatar-letter {
  font-size: 64rpx;
  font-weight: 800;
  color: #fff;
  text-shadow: 0 2rpx 8rpx rgba(180, 40, 10, 0.4);
}

/* 头像编辑徽章 - 右上角 */
.pe-avatar-badge {
  position: absolute;
  top: 0;
  right: calc(50% - 80rpx);
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.98);
  box-shadow:
    0 6rpx 24rpx rgba(255, 77, 79, 0.5),
    0 2rpx 8rpx rgba(255, 77, 79, 0.25);
  z-index: 10;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.pe-avatar-section:active .pe-avatar-badge {
  transform: scale(1.15);
  box-shadow:
    0 10rpx 32rpx rgba(255, 77, 79, 0.6),
    0 4rpx 12rpx rgba(255, 77, 79, 0.35);
}

.pe-badge-icon {
  font-size: 22rpx;
  font-weight: 700;
  color: #fff;
}

/* ========== 波浪装饰 ========== */
.wave-decoration {
  position: absolute;
  bottom: -2rpx;
  left: 0;
  width: 100%;
  height: 120rpx;
  overflow: hidden;
  line-height: 0;
  z-index: 3;
  pointer-events: none;
}

.wave-path {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
}

.wave-path-1 {
  height: 120rpx;
  animation: waveMove1 12s ease-in-out infinite;
}

.wave-path-2 {
  height: 110rpx;
  animation: waveMove2 10s ease-in-out infinite reverse;
  opacity: 0.75;
}

.wave-path-3 {
  height: 95rpx;
  animation: waveMove3 14s ease-in-out infinite;
  opacity: 0.5;
}

@keyframes waveMove1 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-30rpx); }
}

@keyframes waveMove2 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(25rpx); }
}

@keyframes waveMove3 {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(-20rpx); }
}

/* ========== 滚动区域 ========== */
.pe-scroll {
  height: 100vh;
  box-sizing: border-box;
  padding-bottom: 200rpx;
}

.pe-content {
  padding: 0 28rpx;
}

/* ========== 玻璃拟态表单卡片 ========== */
.glass-form-card {
  margin-bottom: 24rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  box-shadow:
    0 8rpx 32rpx rgba(31, 38, 135, 0.12),
    0 2rpx 8rpx rgba(0, 0, 0, 0.04),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.92),
    inset 0 -1rpx 0 rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.65);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 1), transparent);
    pointer-events: none;
  }

  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: translateY(-2rpx) scale(1.005);
    box-shadow:
      0 16rpx 48rpx rgba(31, 38, 135, 0.18),
      0 4rpx 16rpx rgba(0, 0, 0, 0.06),
      inset 0 1rpx 0 rgba(255, 255, 255, 1),
      inset 0 -1rpx 0 rgba(0, 0, 0, 0.04);
  }
}

/* 分组标题 */
.gfc-section {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 28rpx 32rpx 16rpx;
  position: relative;
}

.gfc-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.gfc-icon--info {
  background: linear-gradient(135deg, rgba(255, 106, 61, 0.12), rgba(255, 154, 93, 0.08));
}

.gfc-icon--look {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.12), rgba(167, 139, 250, 0.08));
}

.gfc-icon--health {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12), rgba(52, 211, 153, 0.08));
}

.gfc-icon {
  font-size: 26rpx;
}

.gfc-title {
  font-size: 27rpx;
  font-weight: 700;
  color: var(--pt-text, #111827);
  letter-spacing: 1rpx;
}

.gfc-line {
  flex: 1;
  height: 1rpx;
  background: linear-gradient(90deg, rgba(209, 213, 219, 0.5), transparent);
}

/* 表单字段 */
.gfc-field {
  display: flex;
  flex-direction: column;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid rgba(243, 244, 246, 0.8);
  min-height: 56rpx;
  transition: all 0.2s ease;
}

.gfc-field--last {
  border-bottom: none;
}

.gfc-field--hover {
  background: rgba(255, 106, 61, 0.03);
}

.gfc-label-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 10rpx;
}

.gfc-label {
  font-size: 25rpx;
  color: #6b7280;
  font-weight: 600;
}

.gfc-required {
  color: #ef4444;
  font-weight: 800;
  font-size: 26rpx;
}

.gfc-input {
  font-size: 30rpx;
  color: #111827;
  font-weight: 500;
  padding-top: 4rpx;
}

.gfc-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4rpx;
}

.gfc-value {
  font-size: 30rpx;
  color: #111827;
  font-weight: 500;
}

.gfc-placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.gfc-chevron {
  width: 16rpx;
  height: 16rpx;
  border-right: 3rpx solid #9ca3af;
  border-bottom: 3rpx solid #9ca3af;
  transform: rotate(-45deg);
  margin-left: 8rpx;
  opacity: 0.6;
}

.gfc-input-with-unit {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4rpx;
}

.gfc-unit {
  font-size: 27rpx;
  color: #9ca3af;
  font-weight: 500;
  flex-shrink: 0;
  margin-left: 12rpx;
}

.bottom-spacer {
  height: 40rpx;
}

/* ========== 底部保存栏 ========== */
.pe-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 20rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow:
    0 -4rpx 32rpx rgba(0, 0, 0, 0.06),
    inset 0 1rpx 0 rgba(255, 255, 255, 0.8);
  border-top: 1rpx solid rgba(255, 255, 255, 0.5);
}

.pe-save-btn {
  position: relative;
  overflow: hidden;
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff5a3d 40%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 12rpx 40rpx rgba(255, 90, 61, 0.45),
    0 4rpx 16rpx rgba(255, 90, 61, 0.25),
    0 0 0 4rpx rgba(255, 122, 61, 0.12),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
    inset 0 -2rpx 0 rgba(180, 50, 20, 0.15);
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.btn-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.35),
    transparent
  );
  transition: left 0.6s ease;
}

.pe-save-btn:active {
  transform: translateY(-1rpx) scale(0.97);
  box-shadow:
    0 6rpx 20rpx rgba(255, 90, 61, 0.45),
    0 2rpx 8rpx rgba(255, 90, 61, 0.3),
    inset 0 4rpx 12rpx rgba(140, 30, 10, 0.25);
}

.pe-save-btn:active .btn-shine {
  left: 100%;
}

.pe-save-btn--disabled {
  opacity: 0.5;
  pointer-events: none;
}

.pe-save-btn-text {
  position: relative;
  color: #fff;
  font-size: 33rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
  text-shadow: 0 2rpx 8rpx rgba(180, 30, 10, 0.35);
}
</style>
