<template>
  <view class="feedback-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">意见反馈</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">
        <view class="section-card">
          <text class="section-label">反馈类型</text>
          <view class="type-list">
            <view
              v-for="t in typeOptions"
              :key="t.value"
              class="type-chip"
              :class="{ active: form.type === t.value }"
              @tap="form.type = t.value"
            >
              <text class="type-chip-text">{{ t.label }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <text class="section-label">反馈内容</text>
          <textarea
            class="feedback-textarea"
            v-model="form.content"
            placeholder="请详细描述您遇到的问题或建议..."
            maxlength="500"
            :auto-height="false"
          />
          <text class="char-count">{{ form.content.length }}/500</text>
        </view>

        <view class="section-card">
          <text class="section-label">联系方式（选填）</text>
          <input class="contact-input" v-model="form.contact" placeholder="手机号或微信号，方便我们联系您" />
        </view>

        <view class="section-card">
          <text class="section-label">上传截图（选填）</text>
          <view class="image-list">
            <view v-for="(img, index) in imageList" :key="index" class="image-item">
              <image class="preview-image" :src="img" mode="aspectFill" @tap="previewImage(img)" />
              <view class="image-delete" @tap="removeImage(index)">
                <text class="delete-icon">✕</text>
              </view>
            </view>
            <view v-if="imageList.length < 4" class="image-add" @tap="chooseImage">
              <text class="add-icon">＋</text>
              <text class="add-text">添加图片</text>
            </view>
          </view>
        </view>

        <view class="submit-btn" :class="{ disabled: !canSubmit }" @tap="onSubmit">
          <text class="submit-text">提交反馈</text>
        </view>

        <view class="tip-section">
          <text class="tip-icon">💡</text>
          <text class="tip-text">我们会在1-3个工作日内处理您的反馈，感谢您的支持！</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 20,
      form: {
        type: 'bug',
        content: '',
        contact: ''
      },
      imageList: [],
      typeOptions: [
        { value: 'bug', label: '🐛 Bug反馈' },
        { value: 'feature', label: '💡 功能建议' },
        { value: 'experience', label: '🎨 体验优化' },
        { value: 'other', label: '📝 其他' }
      ],
      submitting: false
    }
  },
  computed: {
    canSubmit() {
      return this.form.content.trim().length >= 10 && !this.submitting
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    chooseImage() {
      const self = this
      uni.chooseImage({
        count: 4 - self.imageList.length,
        sizeType: ['compressed'],
        mediaType: ['image'],
        success(res) {
          if (res.tempFilePaths) {
            self.imageList = [...self.imageList, ...res.tempFilePaths].slice(0, 4)
          }
        }
      })
    },
    removeImage(index) {
      this.imageList.splice(index, 1)
    },
    previewImage(url) {
      uni.previewImage({ current: url, urls: this.imageList })
    },
    async onSubmit() {
      if (!this.canSubmit) return
      const loggedIn = await checkLogin('请先登录后再提交反馈')
      if (!loggedIn) return

      this.submitting = true
      try {
        const res = await uni.$request.post('/api/feedback', {
          type: this.form.type,
          content: this.form.content.trim(),
          contact: this.form.contact.trim(),
          images: this.imageList
        })
        if (res && res.success) {
          uni.showToast({ title: '提交成功', icon: 'success' })
          setTimeout(() => { uni.navigateBack() }, 1500)
        } else {
          uni.showToast({ title: (res && res.message) || '提交失败', icon: 'none' })
        }
      } catch (e) {
        console.error('提交反馈失败:', e)
        uni.showToast({ title: '提交失败，请稍后重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.feedback-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.section-card {
  background: $card-bg; border-radius: 24rpx; padding: 28rpx; margin-bottom: 20rpx;
}
.section-label { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; margin-bottom: 20rpx; }

.type-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.type-chip {
  padding: 14rpx 28rpx; border-radius: 32rpx; background: #f5f5f5;
  border: 2rpx solid transparent; transition: all 0.2s;
}
.type-chip.active { border-color: $primary; background: #fff0ea; }
.type-chip-text { font-size: 26rpx; color: $text-secondary; font-weight: 500; }
.type-chip.active .type-chip-text { color: $primary; }

.feedback-textarea {
  width: 100%; height: 240rpx; font-size: 28rpx; color: $text-primary;
  line-height: 1.6; box-sizing: border-box;
}
.char-count { display: block; text-align: right; font-size: 24rpx; color: $text-light; margin-top: 8rpx; }

.contact-input {
  width: 100%; height: 80rpx; border: 2rpx solid #e8e8e8; border-radius: 16rpx;
  padding: 0 24rpx; font-size: 28rpx; color: $text-primary; box-sizing: border-box;
}

.image-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.image-item { position: relative; width: 160rpx; height: 160rpx; }
.preview-image { width: 100%; height: 100%; border-radius: 12rpx; }
.image-delete {
  position: absolute; top: -8rpx; right: -8rpx; width: 36rpx; height: 36rpx;
  background: rgba(0,0,0,0.6); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.delete-icon { font-size: 20rpx; color: #fff; }
.image-add {
  width: 160rpx; height: 160rpx; border-radius: 12rpx; border: 2rpx dashed #d1d5db;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: #fafafa;
}
.add-icon { font-size: 44rpx; color: #9ca3af; }
.add-text { font-size: 22rpx; color: #9ca3af; margin-top: 4rpx; }

.submit-btn {
  height: 96rpx; border-radius: 48rpx; display: flex; align-items: center;
  justify-content: center; background: $primary; margin: 32rpx 0 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(255,106,61,0.3);
}
.submit-btn:active { opacity: 0.9; }
.submit-btn.disabled { background: #ccc; box-shadow: none; }
.submit-text { font-size: 32rpx; font-weight: 600; color: #fff; }

.tip-section {
  display: flex; align-items: flex-start; padding: 24rpx;
  background: #fffbf0; border-radius: 16rpx; margin-bottom: 40rpx;
}
.tip-icon { font-size: 28rpx; margin-right: 12rpx; flex-shrink: 0; }
.tip-text { font-size: 24rpx; color: #b8860b; line-height: 1.6; }
</style>
