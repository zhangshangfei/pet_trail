<template>
  <view class="album-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">成长相册</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px', paddingBottom: '160rpx' }">
      <view class="page-content">
        <view v-if="pets.length === 0" class="empty-pet">
          <text class="empty-icon">🐾</text>
          <text class="empty-text">请先添加宠物</text>
        </view>

        <view v-else>
          <view class="pet-tabs">
            <scroll-view scroll-x class="pet-tabs-scroll">
              <view
                v-for="pet in pets"
                :key="pet.id"
                class="pet-tab"
                :class="{ active: currentPetId === pet.id }"
                @tap="switchPet(pet.id)"
              >
                <image class="pet-tab-avatar" :src="pet.avatar || defaultPetAvatar" mode="aspectFill" />
                <text class="pet-tab-name">{{ pet.name }}</text>
              </view>
            </scroll-view>
          </view>

          <view class="month-picker">
            <view class="month-arrow" @tap="prevMonth"><text class="arrow-icon">‹</text></view>
            <text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
            <view class="month-arrow" @tap="nextMonth"><text class="arrow-icon">›</text></view>
          </view>

          <view v-if="photos.length" class="photo-grid">
            <view class="photo-item" v-for="photo in photos" :key="photo.id" @tap="onPreviewPhoto(photo)" @longpress="onPhotoLongPress(photo)">
              <image class="photo-image" :src="photo.imageUrl" mode="aspectFill" />
              <view v-if="photo.title" class="photo-title-bar">
                <text class="photo-title">{{ photo.title }}</text>
              </view>
              <text class="photo-date">{{ formatDate(photo.recordDate) }}</text>
            </view>
          </view>

          <view v-else class="empty-album">
            <text class="empty-icon">📷</text>
            <text class="empty-text">本月暂无照片</text>
            <text class="empty-hint">点击下方按钮记录成长瞬间</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="bottom-bar-inner" :style="{ paddingBottom: 'max(16rpx, env(safe-area-inset-bottom))' }">
        <view class="add-btn" @tap="onAddPhoto">
          <text class="add-btn-text">＋ 添加照片</text>
        </view>
      </view>
    </view>

    <view v-if="showAddModal" class="popup-mask" @tap="closeModal">
      <view class="popup-content" @tap.stop>
        <view class="popup-header">
          <text class="popup-title">添加照片</text>
          <view class="popup-close" @tap="closeModal"><text class="popup-close-icon">✕</text></view>
        </view>
        <scroll-view scroll-y class="popup-scroll">
          <view class="upload-area" @tap="onChooseImage">
            <image v-if="form.imageUrl" class="upload-preview" :src="form.imageUrl" mode="aspectFill" />
            <view v-else class="upload-placeholder">
              <text class="upload-icon">📷</text>
              <text class="upload-text">点击上传照片</text>
            </view>
          </view>
          <view class="form-group">
            <text class="form-label">标题（选填）</text>
            <input class="form-input" v-model="form.title" placeholder="给照片起个名字" maxlength="20" />
          </view>
          <view class="form-group">
            <text class="form-label">备注（选填）</text>
            <input class="form-input" v-model="form.note" placeholder="记录这一刻" maxlength="50" />
          </view>
          <view class="form-group">
            <text class="form-label">日期</text>
            <picker mode="date" :value="form.recordDate" @change="onDateChange">
              <view class="time-picker">
                <text class="time-value">{{ form.recordDate || '请选择日期' }}</text>
                <text class="time-arrow">›</text>
              </view>
            </picker>
          </view>
        </scroll-view>
        <view class="popup-footer" :style="{ paddingBottom: 'max(24rpx, env(safe-area-inset-bottom))' }">
          <view class="popup-btn cancel" @tap="closeModal"><text class="popup-btn-text">取消</text></view>
          <view class="popup-btn confirm" @tap="onSavePhoto"><text class="popup-btn-text confirm-text">保存</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { DEFAULT_PET_AVATAR_URL } from '@/utils/index'
import * as petApi from '@/api/pet'
import config from '@/config/env'

export default {
  data() {
    return {
      statusBarHeight: 20,
      defaultPetAvatar: DEFAULT_PET_AVATAR_URL,
      pets: [],
      currentPetId: null,
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      photos: [],
      showAddModal: false,
      form: {
        imageUrl: '',
        title: '',
        note: '',
        recordDate: ''
      },
      saving: false
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    if (options.petId) {
      this.currentPetId = Number(options.petId)
    }
  },
  async onShow() {
    await this.loadPets()
    this.loadPhotos()
  },
  methods: {
    goBack() { uni.navigateBack({ delta: 1 }) },

    async loadPets() {
      try {
        const res = await petApi.getPetList()
        if (res && res.success && Array.isArray(res.data)) {
          this.pets = res.data
          if (this.pets.length && !this.currentPetId) {
            this.currentPetId = this.pets[0].id
          }
        }
      } catch (e) {
        console.error('加载宠物失败:', e)
      }
    },

    async loadPhotos() {
      if (!this.currentPetId) { this.photos = []; return }
      try {
        const res = await petApi.getPetAlbum(this.currentPetId, {
          year: this.currentYear,
          month: this.currentMonth
        })
        if (res && res.success && Array.isArray(res.data)) {
          this.photos = res.data
        } else {
          this.photos = []
        }
      } catch (e) {
        console.error('加载相册失败:', e)
        this.photos = []
      }
    },

    switchPet(petId) {
      this.currentPetId = petId
      this.loadPhotos()
    },

    prevMonth() {
      if (this.currentMonth === 1) {
        this.currentMonth = 12
        this.currentYear--
      } else {
        this.currentMonth--
      }
      this.loadPhotos()
    },

    nextMonth() {
      const now = new Date()
      const nowYear = now.getFullYear()
      const nowMonth = now.getMonth() + 1
      if (this.currentYear === nowYear && this.currentMonth === nowMonth) return
      if (this.currentMonth === 12) {
        this.currentMonth = 1
        this.currentYear++
      } else {
        this.currentMonth++
      }
      this.loadPhotos()
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      return `${d.getMonth() + 1}/${d.getDate()}`
    },

    onPreviewPhoto(photo) {
      const urls = this.photos.map(p => p.imageUrl)
      uni.previewImage({
        current: photo.imageUrl,
        urls: urls
      })
    },

    onPhotoLongPress(photo) {
      const self = this
      uni.showActionSheet({
        itemList: ['删除照片'],
        success(res) {
          if (res.tapIndex === 0) {
            uni.showModal({
              title: '删除照片',
              content: '确定删除这张照片吗？',
              confirmColor: '#ff4d4f',
              async success(r) {
                if (!r.confirm) return
                try {
                  const result = await petApi.deletePetAlbumPhoto(self.currentPetId, photo.id)
                  if (result && result.success) {
                    self.photos = self.photos.filter(p => p.id !== photo.id)
                    uni.showToast({ title: '已删除', icon: 'success' })
                  }
                } catch (e) {
                  uni.showToast({ title: '删除失败', icon: 'none' })
                }
              }
            })
          }
        }
      })
    },

    onAddPhoto() {
      if (!this.currentPetId) {
        uni.showToast({ title: '请先选择宠物', icon: 'none' })
        return
      }
      this.form = {
        imageUrl: '',
        title: '',
        note: '',
        recordDate: new Date().toISOString().split('T')[0]
      }
      this.showAddModal = true
    },

    onChooseImage() {
      const self = this
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        async success(res) {
          const tempPath = res.tempFilePaths[0]
          try {
            uni.showLoading({ title: '上传中...' })
            const uploadRes = await new Promise((resolve, reject) => {
              uni.uploadFile({
                url: (config.VITE_UPLOAD_HTTP_BASE || config.VITE_API_BASE_URL || '').replace(/\/$/, '') + '/api/upload',
                filePath: tempPath,
                name: 'file',
                header: { Authorization: 'Bearer ' + uni.getStorageSync('token') },
                success: (r) => {
                  try {
                    const data = JSON.parse(r.data)
                    resolve(data)
                  } catch (e) { reject(e) }
                },
                fail: reject
              })
            })
            uni.hideLoading()
            if (uploadRes && uploadRes.success && uploadRes.data) {
              self.form.imageUrl = uploadRes.data.url || uploadRes.data
            } else {
              uni.showToast({ title: '上传失败', icon: 'none' })
            }
          } catch (e) {
            uni.hideLoading()
            console.error('上传图片失败:', e)
            uni.showToast({ title: '上传失败', icon: 'none' })
          }
        }
      })
    },

    onDateChange(e) {
      this.form.recordDate = e.detail.value
    },

    closeModal() {
      this.showAddModal = false
    },

    async onSavePhoto() {
      if (!this.form.imageUrl) {
        uni.showToast({ title: '请上传照片', icon: 'none' })
        return
      }
      if (this.saving) return
      this.saving = true

      try {
        const data = {
          imageUrl: this.form.imageUrl,
          title: this.form.title || null,
          note: this.form.note || null,
          recordDate: this.form.recordDate || null
        }
        const res = await petApi.addPetAlbumPhoto(this.currentPetId, data)
        if (res && res.success) {
          uni.showToast({ title: '保存成功', icon: 'success' })
          this.closeModal()
          await this.loadPhotos()
        } else {
          uni.showToast({ title: (res && res.message) || '保存失败', icon: 'none' })
        }
      } catch (e) {
        console.error('保存照片失败:', e)
        uni.showToast({ title: '网络错误', icon: 'none' })
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   成长相册 - 高饱和活力设计 v7.0
   与挑战赛/日历/设置风格统一
   ============================================ */

/* ========== 设计变量 ========== */
$primary: #ff5500;
$primary-soft: #ff7a3d;

$bg: #fff5f0;
$white: #ffffff;
$text-dark: #1c1917;
$text-mid: #44403c;
$text-light: #a8a29e;

/* ========== 页面基础 ========== */
.album-page {
  min-height: 100vh;
  background: $bg;
}

/* ========== 导航栏 ========== */
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: $white; }
.status-bar { width: 100%; }

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  border-bottom: 2rpx solid #f5ebe5;
}

.nav-back {
  width: 64rpx; height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 32rpx;
  background: #f5f5f4;

  &:active { transform: scale(0.9); background: #e7e5e4; }
}

.nav-back-arrow {
  width: 18rpx; height: 18rpx;
  border-left: 3rpx solid $text-dark;
  border-bottom: 3rpx solid $text-dark;
  transform: rotate(45deg);
  margin-left: -3rpx;
}

.nav-title { font-size: 36rpx; font-weight: 900; color: $text-dark; letter-spacing: 0.5rpx; }
.nav-placeholder { width: 64rpx; }

.page-scroll { height: 100vh; }
.page-content { padding: 24rpx 28rpx 140rpx; }

/* ========== 空宠物状态 ========== */
.empty-pet {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}
.empty-icon {
  font-size: 88rpx;
  margin-bottom: 20rpx;
  filter: grayscale(20%);
  opacity: 0.8;
}
.empty-text { font-size: 30rpx; font-weight: 700; color: $text-mid; }

/* ========== 宠物切换标签 ========== */
.pet-tabs { margin-bottom: 28rpx; }
.pet-tabs-scroll { white-space: nowrap; }

.pet-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20rpx;
  padding: 14rpx 18rpx;
  border-radius: 22rpx;
  background: $white;
  border: 2rpx solid transparent;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2rpx 12rpx rgba(255, 85, 0, 0.06);

  &.active {
    background: linear-gradient(135deg, #fff7f2, #fff);
    border-color: $primary-soft;
    box-shadow: 0 4rpx 20rpx rgba(255, 85, 0, 0.15);
  }
}

.pet-tab-avatar {
  width: 80rpx; height: 80rpx;
  border-radius: 50%;
  border: 3rpx solid #e7e5e4;
  margin-bottom: 8rpx;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  transition: all 0.25s ease;

  .pet-tab.active & { border-color: $primary; box-shadow: 0 0 0 3rpx rgba(255, 85, 0, 0.12); }
}

.pet-tab-name { font-size: 23rpx; color: $text-light; font-weight: 500; }
.pet-tab.active .pet-tab-name { color: $primary; font-weight: 800; }

/* ========== 月份切换器 ========== */
.month-picker {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 26rpx;
  gap: 36rpx;
}

.month-arrow {
  width: 60rpx; height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #fff5f2, #fff);
  border: 1.5rpx solid #ffe8dd;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.88);
    background: linear-gradient(135deg, #ffe8dd, #fff5f2);
    box-shadow: 0 2rpx 10rpx rgba(255, 85, 0, 0.15);
  }
}

.arrow-icon { font-size: 38rpx; color: $primary; font-weight: 900; line-height: 1; }

.month-text {
  font-size: 34rpx;
  font-weight: 900;
  color: $text-dark;
  letter-spacing: 1rpx;
  min-width: 200rpx;
  text-align: center;
}

/* ========== 照片瀑布流网格 ========== */
.photo-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.photo-item {
  width: calc(33.33% - 10rpx);
  aspect-ratio: 1;
  border-radius: 20rpx;
  overflow: hidden;
  position: relative;
  background: #eee;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);

  &:active {
    transform: scale(0.96) translateY(-4rpx);
    box-shadow:
      0 16rpx 40rpx rgba(0, 0, 0, 0.15),
      0 4rpx 12rpx rgba(255, 85, 0, 0.08);
  }
}

.photo-image {
  width: 100%;
  height: 100%;
  transition: transform 0.35s ease;

  .photo-item:active & { transform: scale(1.05); }
}

.photo-title-bar {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  padding: 14rpx 14rpx 10rpx;
  background: linear-gradient(transparent 30%, rgba(0, 0, 0, 0.65));
}

.photo-title {
  font-size: 22rpx;
  color: #fff;
  font-weight: 600;
  letter-spacing: 0.3rpx;
  text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.3);
}

.photo-date {
  position: absolute;
  top: 10rpx; right: 10rpx;
  font-size: 20rpx;
  color: #fff;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: 3rpx 12rpx;
  border-radius: 10rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.15);
}

/* ========== 空相册状态 ========== */
.empty-album {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
}
.empty-hint { font-size: 27rpx; color: $text-light; margin-top: 12rpx; font-weight: 500; }

/* ========== 底部添加按钮栏 ========== */
.bottom-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  z-index: 30;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1.5rpx solid rgba(255, 122, 61, 0.1);
  box-shadow: 0 -4rpx 24rpx rgba(255, 85, 0, 0.06);
}

.bottom-bar-inner { padding: 18rpx 32rpx; padding-bottom: calc(18rpx + env(safe-area-inset-bottom)); }

.add-btn {
  height: 96rpx;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary, $primary-soft);
  box-shadow:
    0 8rpx 28rpx rgba(255, 85, 0, 0.38),
    0 2rpx 8px rgba(255, 85, 0, 0.2),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.25);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.97) translateY(-2rpx);
    box-shadow:
      0 4rpx 14rpx rgba(255, 85, 0, 0.45),
      inset 0 -2rpx 0 rgba(180, 50, 20, 0.15);
  }
}

.add-btn-text {
  font-size: 31rpx;
  font-weight: 900;
  color: #fff;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 6rpx rgba(180, 30, 10, 0.3);
}

/* ========== 弹窗蒙层与内容 ========== */
.popup-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.55);
  z-index: 100;
  display: flex;
  align-items: flex-end;
  animation: maskFadeIn 0.25s ease-out;
}

@keyframes maskFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.popup-content {
  width: 100%;
  background: $white;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 82vh;
  animation: slideUp 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 20rpx;
  border-bottom: 1.5rpx solid #f5ebe5;
}

.popup-title { font-size: 34rpx; font-weight: 900; color: $text-dark; letter-spacing: 0.5rpx; }

.popup-close {
  width: 58rpx; height: 58rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 29rpx;
  background: #f5f5f4;
  transition: all 0.2s ease;

  &:active { transform: scale(0.9); background: #e7e5e4; }
}

.popup-close-icon { font-size: 28rpx; color: $text-mid; font-weight: 700; }

.popup-scroll { max-height: 54vh; padding: 20rpx 32rpx; }

/* ========== 上传区域 ========== */
.upload-area {
  width: 100%;
  height: 380rpx;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 26rpx;
  background: linear-gradient(145deg, #fef7f4, #fff, #fff5f0);
  border: 2rpx dashed #ffcbb8;
  transition: all 0.25s ease;

  &:active {
    border-color: $primary;
    background: linear-gradient(145deg, #fff5f2, #fff);
  }
}

.upload-preview { width: 100%; height: 100%; }

.upload-placeholder {
  width: 100%; height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 72rpx;
  margin-bottom: 16rpx;
  opacity: 0.65;
}

.upload-text { font-size: 27rpx; color: $text-light; font-weight: 500; }

/* ========== 表单 ========== */
.form-group { margin-bottom: 26rpx; }

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: $text-dark;
  margin-bottom: 13rpx;
  letter-spacing: 0.3rpx;
}

.form-input {
  width: 100%;
  height: 84rpx;
  border: 2rpx solid #e5e5e0;
  border-radius: 18rpx;
  padding: 0 26rpx;
  font-size: 28rpx;
  color: $text-dark;
  box-sizing: border-box;
  background: #fafaf7;
  transition: all 0.25s ease;

  &:focus-within {
    border-color: $primary-soft;
    background: #fff;
    box-shadow: 0 0 0 4rpx rgba(255, 122, 61, 0.1);
  }
}

.time-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 84rpx;
  border: 2rpx solid #e5e5e0;
  border-radius: 18rpx;
  padding: 0 26rpx;
  background: #fafaf7;
  transition: all 0.25s ease;

  &:active { border-color: $primary-soft; }
}

.time-value { font-size: 28rpx; color: $text-dark; font-weight: 600; }
.time-arrow { font-size: 34rpx; color: #c9c9c0; font-weight: 700; }

/* ========== 弹窗底部按钮 ========== */
.popup-footer {
  display: flex;
  gap: 18rpx;
  padding: 18rpx 32rpx;
  padding-bottom: calc(18rpx + env(safe-area-inset-bottom));
}

.popup-btn {
  flex: 1;
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 45rpx;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active { transform: scale(0.96); }
}

.popup-btn.cancel {
  background: #f5f5f4;
  color: $text-mid;
  font-weight: 800;

  &:active { background: #eaeae7; }
}

.popup-btn.confirm {
  background: linear-gradient(135deg, $primary, $primary-soft);
  box-shadow: 0 6rpx 20rpx rgba(255, 85, 0, 0.35);
}

.popup-btn-text { font-size: 30rpx; color: $text-mid; font-weight: 800; }
.confirm-text {
  color: #fff;
  font-weight: 900;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 6rpx rgba(180, 30, 10, 0.3);
}
</style>
