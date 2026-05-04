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
        const res = await petApi.getPetAlbum(this.currentPetId, { page: this.albumPage, size: 20 })
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
        const res = await petApi.addPetAlbumPhoto(this.currentPetId, { url: uploadRes.data.url })
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
$primary: #ff6a3d;
$primary-light: #fff0ea;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.album-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.empty-pet { display: flex; flex-direction: column; align-items: center; padding: 120rpx 0; }
.empty-icon { font-size: 80rpx; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: $text-secondary; }

.pet-tabs { margin-bottom: 20rpx; }
.pet-tabs-scroll { white-space: nowrap; }
.pet-tab { display: inline-flex; flex-direction: column; align-items: center; margin-right: 24rpx; padding: 12rpx; }
.pet-tab-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; border: 3rpx solid #e5e7eb; margin-bottom: 8rpx; }
.pet-tab.active .pet-tab-avatar { border-color: $primary; }
.pet-tab-name { font-size: 24rpx; color: $text-secondary; }
.pet-tab.active .pet-tab-name { color: $primary; font-weight: 600; }

.month-picker {
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 24rpx; gap: 32rpx;
}
.month-arrow { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; border-radius: 28rpx; background: #fff; }
.arrow-icon { font-size: 36rpx; color: $primary; font-weight: 700; }
.month-text { font-size: 30rpx; font-weight: 600; color: $text-primary; }

.photo-grid {
  display: flex; flex-wrap: wrap; gap: 12rpx;
}
.photo-item {
  width: calc(33.33% - 8rpx); aspect-ratio: 1; border-radius: 16rpx;
  overflow: hidden; position: relative; background: #eee;
}
.photo-image { width: 100%; height: 100%; }
.photo-title-bar {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 8rpx 12rpx; background: linear-gradient(transparent, rgba(0,0,0,0.6));
}
.photo-title { font-size: 22rpx; color: #fff; }
.photo-date {
  position: absolute; top: 8rpx; right: 8rpx;
  font-size: 20rpx; color: #fff; background: rgba(0,0,0,0.4);
  padding: 2rpx 10rpx; border-radius: 8rpx;
}

.empty-album {
  display: flex; flex-direction: column; align-items: center;
  padding: 80rpx 0;
}
.empty-hint { font-size: 24rpx; color: $text-light; margin-top: 8rpx; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 30; background: #fff; border-top: 1rpx solid #f0f0f0; }
.bottom-bar-inner { padding: 16rpx 32rpx; }
.add-btn { height: 96rpx; border-radius: 48rpx; display: flex; align-items: center; justify-content: center; background: $primary; box-shadow: 0 8rpx 24rpx rgba(255,106,61,0.3); }
.add-btn:active { opacity: 0.9; }
.add-btn-text { font-size: 30rpx; font-weight: 600; color: #fff; }

.popup-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.popup-content { width: 100%; background: #fff; border-radius: 32rpx 32rpx 0 0; max-height: 80vh; }
.popup-header { display: flex; align-items: center; justify-content: space-between; padding: 32rpx 32rpx 16rpx; }
.popup-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.popup-close { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; border-radius: 28rpx; background: #f5f5f5; }
.popup-close-icon { font-size: 28rpx; color: $text-secondary; }
.popup-scroll { max-height: 55vh; padding: 16rpx 32rpx; }

.upload-area { width: 100%; height: 360rpx; border-radius: 20rpx; overflow: hidden; margin-bottom: 24rpx; background: #f5f5f5; }
.upload-preview { width: 100%; height: 100%; }
.upload-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.upload-icon { font-size: 64rpx; margin-bottom: 12rpx; }
.upload-text { font-size: 26rpx; color: $text-light; }

.form-group { margin-bottom: 24rpx; }
.form-label { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; margin-bottom: 12rpx; }
.form-input { width: 100%; height: 80rpx; border: 2rpx solid #e8e8e8; border-radius: 16rpx; padding: 0 24rpx; font-size: 28rpx; color: $text-primary; box-sizing: border-box; }

.time-picker { display: flex; align-items: center; justify-content: space-between; height: 80rpx; border: 2rpx solid #e8e8e8; border-radius: 16rpx; padding: 0 24rpx; }
.time-value { font-size: 28rpx; color: $text-primary; }
.time-arrow { font-size: 32rpx; color: #d1d5db; }

.popup-footer { display: flex; gap: 16rpx; padding: 16rpx 32rpx; }
.popup-btn { flex: 1; height: 88rpx; display: flex; align-items: center; justify-content: center; border-radius: 44rpx; }
.popup-btn.cancel { background: #f5f5f5; }
.popup-btn.confirm { background: $primary; }
.popup-btn-text { font-size: 30rpx; color: $text-secondary; }
.confirm-text { color: #fff; font-weight: 600; }
</style>
