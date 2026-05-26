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
        
        <!-- 反馈类型卡片 -->
        <view class="fb-card">
          <view class="card-header">
            <view class="card-icon-wrap card-icon-orange">
              <text class="card-icon">🏷️</text>
            </view>
            <text class="card-title">反馈类型</text>
          </view>

          <view class="type-grid">
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

        <!-- 反馈内容卡片 -->
        <view class="fb-card">
          <view class="card-header">
            <view class="card-icon-wrap card-icon-pink">
              <text class="card-icon">💬</text>
            </view>
            <text class="card-title">反馈内容</text>
          </view>

          <view class="textarea-wrap">
            <textarea
              class="feedback-textarea"
              v-model="form.content"
              placeholder="请详细描述您遇到的问题或者建议，至少5字哦～我们会认真对待每一条反馈💕"
              maxlength="500"
              :auto-height="false"
            />
          </view>
          <view class="char-counter">
            <text :class="{ 'char-warning': form.content.length > 450 }">{{ form.content.length }}/500</text>
          </view>
        </view>

        <!-- 联系方式卡片 -->
        <view class="fb-card">
          <view class="card-header">
            <view class="card-icon-wrap card-icon-blue">
              <text class="card-icon">📱</text>
            </view>
            <text class="card-title">联系方式（选填）</text>
          </view>

          <input 
            class="contact-input" 
            v-model="form.contact" 
            placeholder="手机号或微信号，方便我们联系您" 
            placeholder-class="input-placeholder"
          />
        </view>

        <!-- 上传截图卡片 -->
        <view class="fb-card">
          <view class="card-header">
            <view class="card-icon-wrap card-icon-purple">
              <text class="card-icon">🖼️</text>
            </view>
            <text class="card-title">上传截图（选填）</text>
            <text class="image-count">{{ imageList.length }}/4</text>
          </view>

          <view class="image-list">
            <view v-for="(img, index) in imageList" :key="index" class="image-item">
              <image class="preview-image" :src="img" mode="aspectFill" @tap="previewImage(img)" />
              <view class="image-delete" @tap="removeImage(index)">
                <text class="delete-icon">×</text>
              </view>
            </view>
            <view v-if="imageList.length < 4" class="image-add" @tap="chooseImage">
              <text class="add-icon">＋</text>
              <text class="add-text">添加图片</text>
            </view>
          </view>
        </view>

        <!-- 提交按钮 -->
        <button class="submit-btn" :class="{ disabled: !canSubmit }" :disabled="!canSubmit" @tap="onSubmit">
          <text class="submit-btn-text">提交反馈</text>
        </button>

        <text v-if="form.content && form.content.trim().length > 0 && form.content.trim().length < 5" class="submit-hint">
          还需输入{{ 5 - form.content.trim().length }}个字才能提交哦～
        </text>

        <!-- 查看历史记录 -->
        <view class="history-link" @tap="goFeedbackList">
          <view class="history-icon-wrap">
            <text class="history-icon">📋</text>
          </view>
          <text class="history-text">查看反馈记录</text>
          <text class="history-arrow">›</text>
        </view>

        <!-- 温馨提示 -->
        <view class="tip-box">
          <view class="tip-icon-wrap">
            <text class="tip-icon">💡</text>
          </view>
          <text class="tip-text">我们会在1-3个工作日内处理您的反馈，感谢您的支持！</text>
        </view>

      </view>
    </scroll-view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'
import * as feedbackApi from '@/api/feedback'

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
      return this.form.content.trim().length >= 5 && !this.submitting
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
    goFeedbackList() {
      uni.navigateTo({ url: '/pages/me/feedback-list' })
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
      if (!this.form.content || !this.form.content.trim()) {
        uni.showToast({ title: '请输入反馈内容', icon: 'none' })
        return
      }
      if (this.form.content.trim().length < 5) {
        uni.showToast({ title: '反馈内容至少5个字', icon: 'none' })
        return
      }
      if (this.submitting) return
      const loggedIn = await checkLogin('请先登录后再提交反馈')
      if (!loggedIn) return

      this.submitting = true
      try {
        const res = await feedbackApi.createFeedback( {
          type: this.form.type,
          content: this.form.content.trim(),
          contact: this.form.contact.trim(),
          images: this.imageList
        })
        if (res && res.success) {
          uni.showToast({ title: '提交成功', icon: 'success' })
          setTimeout(() => {
            uni.redirectTo({ url: '/pages/me/feedback-list' })
          }, 1500)
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
/* ============================================
   意见反馈 - 统一设计系统 v2.0
   与 me/index.vue 风格完全一致
   ============================================ */

/* 设计变量 */
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

/* ========== 页面基础 ========== */
.feedback-page { min-height: 100vh; background: $bg-color; }

.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: $white; }
.status-bar { width: 100%; }

.nav-bar { 
  height: 92rpx; 
  display: flex; align-items: center; justify-content: space-between; 
  padding: 0 32rpx; 
  border-bottom: 1rpx solid $gray-100;
}

.nav-back { 
  width: 64rpx; height: 64rpx; 
  display: flex; align-items: center; justify-content: center;
  border-radius: 32rpx;
  background: $gray-50;
  
  &:active { background: $gray-200; transform: scale(0.92); }
}

.nav-back-arrow { 
  width: 18rpx; height: 18rpx; 
  border-left: 3rpx solid $gray-800; border-bottom: 3rpx solid $gray-800; 
  transform: rotate(45deg); margin-left: -4rpx;
}

.nav-title { font-size: 34rpx; font-weight: 700; color: $gray-800; letter-spacing: 0.5rpx; }
.nav-placeholder { width: 64rpx; }

.page-scroll { height: 100vh; }
.page-content { padding: 24rpx 28rpx; }

/* ========== 卡片容器 ========== */
.fb-card {
  background: $white; border-radius: $radius-md; margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex; align-items: center; gap: 16rpx;
  padding: 28rpx 28rpx 20rpx;
  background: linear-gradient(180deg, $white 0%, #fff9f5 100%);
  border-bottom: 1rpx solid rgba(255, 107, 53, 0.08);
}

.card-icon-wrap {
  width: 44rpx; height: 44rpx; border-radius: 12rpx;
  display: flex; align-items: center; justify-content: center;

  &.card-icon-orange { background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05)); }
  &.card-icon-pink { background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(236, 72, 153, 0.05)); }
  &.card-icon-blue { background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05)); }
  &.card-icon-purple { background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(139, 92, 246, 0.05)); }
}

.card-icon { font-size: 24rpx; }

.card-title { font-size: 28rpx; font-weight: 600; color: $gray-800; letter-spacing: 0.3rpx; flex: 1; }

.image-count { font-size: 24rpx; color: $gray-500; font-weight: 500; }

/* ========== 反馈类型选择器 ========== */
.type-grid { display: flex; flex-wrap: wrap; gap: 16rpx; padding: 20rpx 24rpx 28rpx; }

.type-chip {
  padding: 16rpx 28rpx; border-radius: 24rpx; background: $gray-50;
  border: 2rpx solid transparent; transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:active { transform: scale(0.96); }

  &.active {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.06), rgba(255, 107, 53, 0.02));
    border-color: rgba(255, 107, 53, 0.35);
    
    .type-chip-text { color: $primary; font-weight: 600; }
  }
}

.type-chip-text { font-size: 26rpx; color: $gray-500; font-weight: 500; transition: all 0.25s ease; }

/* ========== 文本输入区 ========== */
.textarea-wrap { padding: 8rpx 24rpx 16rpx; }

.feedback-textarea {
  width: 100%; min-height: 240rpx; font-size: 28rpx; color: $gray-800;
  line-height: 1.7; box-sizing: border-box;
  background: $gray-50; border-radius: $radius-sm;
  padding: 20rpx;
  border: 2rpx solid transparent;
  transition: all 0.25s ease;

  &:focus {
    background: $white;
    border-color: rgba(255, 107, 53, 0.3);
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.06);
  }
}

.char-counter { text-align: right; padding: 8rpx 24rpx 20rpx; }

.char-counter text { font-size: 24rpx; color: $gray-300; font-weight: 500; }

.char-counter .char-warning { color: $primary; font-weight: 700; }

/* ========== 联系方式输入框 ========== */
.contact-input {
  margin: 8rpx 24rpx 24rpx;
  width: calc(100% - 48rpx); height: 80rpx; 
  background: $gray-50; border-radius: $radius-sm;
  padding: 0 24rpx; font-size: 28rpx; color: $gray-800; 
  box-sizing: border-box; border: 2rpx solid transparent;
  transition: all 0.25s ease;

  &:focus {
    background: $white;
    border-color: rgba(255, 107, 53, 0.3);
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.06);
  }
}

.input-placeholder { color: $gray-300; font-weight: 400; }

/* ========== 图片上传区域 ========== */
.image-list { display: flex; flex-wrap: wrap; gap: 16rpx; padding: 20rpx 24rpx 28rpx; }

.image-item { position: relative; width: 160rpx; height: 160rpx; }

.preview-image { width: 100%; height: 100%; border-radius: $radius-sm; }

.image-delete {
  position: absolute; top: -10rpx; right: -10rpx; width: 40rpx; height: 40rpx;
  background: linear-gradient(135deg, #ef4444, #dc2626); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.35);
}

.delete-icon { font-size: 22rpx; color: #fff; font-weight: 700; line-height: 1; }

.image-add {
  width: 160rpx; height: 160rpx; border-radius: $radius-sm; 
  border: 2rpx dashed $gray-300;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: $gray-50;
  transition: all 0.25s ease;

  &:active { background: $gray-100; border-color: $primary; }
}

.add-icon { font-size: 48rpx; color: $gray-300; line-height: 1; }
.add-text { font-size: 22rpx; color: $gray-300; margin-top: 8rpx; }

/* ========== 提交按钮 ========== */
.submit-btn {
  height: 96rpx; border-radius: $radius-md; display: flex; align-items: center;
  justify-content: center; 
  background: linear-gradient(135deg, $primary, $primary-light);
  margin: 32rpx 0 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35), 0 4rpx 12rpx rgba(255, 107, 53, 0.2);
  border: none; padding: 0; line-height: 96rpx;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &::after { border: none; }

  &:active { 
    transform: scale(0.97); 
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.45), 0 2rpx 8rpx rgba(255, 107, 53, 0.25);
  }

  &.disabled { 
    background: linear-gradient(135deg, $gray-300, $gray-200); 
    box-shadow: none; 
  }
}

.submit-btn[disabled] { background: linear-gradient(135deg, $gray-300, $gray-200); box-shadow: none; }

.submit-btn-text { font-size: 32rpx; font-weight: 700; color: #fff; letter-spacing: 2rpx; }

.submit-hint { 
  display: block; text-align: center; font-size: 24rpx; 
  color: $primary; margin-top: -8rpx; margin-bottom: 20rpx; font-weight: 600;
}

/* ========== 历史记录链接 ========== */
.history-link {
  display: flex; align-items: center; justify-content: center;
  height: 96rpx; border-radius: $radius-md; background: $white; margin: 20rpx 0;
  border: 2rpx solid $gray-200;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
  transition: all 0.25s ease;

  &:active { background: $gray-50; border-color: $gray-300; }
}

.history-icon-wrap {
  width: 44rpx; height: 44rpx; border-radius: 11rpx;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
  display: flex; align-items: center; justify-content: center;
  margin-right: 14rpx;
}

.history-icon { font-size: 24rpx; }

.history-text { font-size: 28rpx; font-weight: 600; color: $gray-800; flex: 1; text-align: center; }
.history-arrow { font-size: 34rpx; color: $gray-300; font-weight: 700; margin-left: 12rpx; }

/* ========== 温馨提示 ========== */
.tip-box {
  display: flex; align-items: flex-start; padding: 24rpx;
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  border-radius: $radius-md; margin-bottom: 40rpx;
  border: 1rpx solid rgba(245, 158, 11, 0.15);
}

.tip-icon-wrap {
  width: 36rpx; height: 36rpx; border-radius: 9rpx;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.15), rgba(245, 158, 11, 0.05));
  display: flex; align-items: center; justify-content: center;
  margin-right: 14rpx; flex-shrink: 0;
}

.tip-icon { font-size: 22rpx; }

.tip-text { font-size: 25rpx; color: #92400e; line-height: 1.6; font-weight: 500; }
</style>
