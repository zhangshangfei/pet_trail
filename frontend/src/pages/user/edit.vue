<template>
  <view class="edit-page">
    <view class="edit-header">
      <view class="edit-header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="edit-back" @tap="goBack">
          <text class="edit-back-icon">←</text>
        </view>
        <text class="edit-title">编辑资料</text>
        <view class="edit-save" @tap="onSave">
          <text class="edit-save-text">保存</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="edit-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="edit-content">
        <view class="edit-avatar-section" @tap="onPickAvatar">
          <image class="edit-avatar" :src="form.avatar || defaultAvatar" mode="aspectFill" />
          <view class="edit-avatar-badge">
            <text class="edit-avatar-icon">📷</text>
          </view>
        </view>

        <view class="edit-form">
          <view class="edit-field">
            <text class="edit-label">昵称</text>
            <input class="edit-input" v-model="form.nickname" placeholder="请输入昵称" maxlength="20" />
          </view>

          <view class="edit-field">
            <text class="edit-label">性别</text>
            <picker :range="genderLabels" :value="genderIndex" @change="onGenderChange">
              <view class="edit-select">
                <text v-if="genderIndex >= 0" :class="['edit-select-text', genderIndex === 0 && 'edit-select-placeholder']">{{ genderLabels[genderIndex] }}</text>
                <text v-else class="edit-select-placeholder">请选择</text>
              </view>
            </picker>
          </view>

          <view class="edit-field">
            <text class="edit-label">手机号</text>
            <input class="edit-input" v-model="form.phone" placeholder="请输入手机号" type="number" maxlength="11" />
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getProfile, updateProfile } from '@/api/auth'
import { uploadImage } from '@/api/pet'

const defaultAvatar = 'https://ai-public.mastergo.com/ai/img_res/1774537096721a3K9mP2xQ7vN4rT8wY.jpg'

export default {
  data() {
    return {
      statusBarHeight: 20,
      defaultAvatar,
      form: {
        nickname: '',
        avatar: '',
        gender: 0,
        phone: ''
      },
      saving: false
    }
  },
  computed: {
    genderLabels() {
      return ['未知', '♂ 男', '♀ 女']
    },
    genderIndex: {
      get() {
        return this.form.gender || 0
      },
      set(val) {
        this.form.gender = val
      }
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    this.loadProfile()
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    async loadProfile() {
      try {
        const res = await getProfile()
        if (res && res.success && res.data) {
          this.form.nickname = res.data.nickname || ''
          this.form.avatar = res.data.avatar || ''
          this.form.gender = res.data.gender || 0
          this.form.phone = res.data.phone || ''
        }
      } catch (e) {
        console.error('加载用户资料失败:', e)
      }
    },

    onGenderChange(e) {
      this.form.gender = Number(e.detail.value)
    },

    onPickAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          if (res.tempFilePaths && res.tempFilePaths.length > 0) {
            const filePath = res.tempFilePaths[0]
            uni.showLoading({ title: '上传中...' })
            try {
              const uploadRes = await uploadImage(filePath)
              if (uploadRes && uploadRes.success && uploadRes.data) {
                this.form.avatar = uploadRes.data.url
              } else {
                uni.showToast({ title: '上传失败', icon: 'none' })
              }
            } catch (err) {
              console.error('上传头像失败:', err)
              uni.showToast({ title: '上传失败', icon: 'none' })
            } finally {
              uni.hideLoading()
            }
          }
        }
      })
    },

    async onSave() {
      if (this.saving) return

      if (!this.form.nickname.trim()) {
        uni.showToast({ title: '请输入昵称', icon: 'none' })
        return
      }

      this.saving = true
      uni.showLoading({ title: '保存中...', mask: true })
      try {
        const res = await updateProfile({
          nickname: this.form.nickname.trim(),
          avatar: this.form.avatar,
          gender: this.form.gender,
          phone: this.form.phone
        })
        uni.hideLoading()
        if (res && res.success) {
          uni.setStorageSync('userInfo', res.data)
          uni.$emit('loginSuccess')
          uni.showToast({ title: '保存成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack({ delta: 1 })
          }, 1000)
        } else {
          uni.showToast({ title: res.message || '保存失败', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        console.error('保存用户资料失败:', e)
        uni.showToast({ title: '保存失败', icon: 'none' })
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: #f9fafb;
}

.edit-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
}

.edit-header-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.edit-back {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-back-icon {
  color: #fff;
  font-size: 36rpx;
  font-weight: 700;
}

.edit-title {
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
}

.edit-save {
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.25);
}

.edit-save-text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
}

.edit-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.edit-content {
  padding: 32rpx;
}

.edit-avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 48rpx;
  position: relative;
}

.edit-avatar {
  width: 180rpx;
  height: 180rpx;
  border-radius: 90rpx;
  border: 6rpx solid #fff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  background: #e5e7eb;
}

.edit-avatar-badge {
  position: absolute;
  bottom: 0;
  right: calc(50% - 100rpx);
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: #ff7a3d;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #fff;
  box-shadow: 0 4rpx 12rpx rgba(255, 122, 61, 0.4);
}

.edit-avatar-icon {
  font-size: 24rpx;
}

.edit-form {
  background: #fff;
  border-radius: 24rpx;
  padding: 0 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.edit-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.edit-field:last-child {
  border-bottom: none;
}

.edit-label {
  font-size: 28rpx;
  color: #374151;
  font-weight: 600;
  flex-shrink: 0;
  width: 140rpx;
}

.edit-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  color: #111827;
}

.edit-select {
  flex: 1;
  text-align: right;
}

.edit-select-text {
  font-size: 28rpx;
  color: #111827;
}

.edit-select-placeholder {
  font-size: 28rpx;
  color: #9ca3af;
}
</style>
