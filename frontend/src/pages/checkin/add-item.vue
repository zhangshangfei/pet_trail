<template>
  <view class="add-item-page">
    <!-- 导航栏 -->
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">添加打卡项</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px', paddingBottom: '180rpx' }">
      <view class="page-content">

        <!-- 预览卡片 -->
        <view class="preview-card">
          <view class="preview-header">
            <text class="preview-label">效果预览</text>
          </view>
          <view class="preview-body">
            <view class="preview-icon-wrap">
              <text class="preview-icon">{{ newItem.icon }}</text>
            </view>
            <view class="preview-info">
              <text class="preview-name">{{ newItem.name || '打卡项名称' }}</text>
              <view class="preview-badge">
                <text class="preview-badge-text">{{ getTypeLabel(newItem.type) }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 表单卡片 -->
        <view class="form-card">

          <!-- 名称输入 -->
          <view class="form-section">
            <view class="section-header">
              <view class="section-icon">
                <text class="section-icon-text">✏️</text>
              </view>
              <text class="section-title">名称</text>
            </view>
            <view class="input-wrapper">
              <input
                class="form-input"
                v-model="newItem.name"
                placeholder="给打卡项起个名字"
                :maxlength="10"
                focus
              />
              <text class="input-count">{{ newItem.name.length }}/10</text>
            </view>
          </view>

          <!-- 分割线 -->
          <view class="divider"></view>

          <!-- 图标选择 -->
          <view class="form-section">
            <view class="section-header">
              <view class="section-icon">
                <text class="section-icon-text">😊</text>
              </view>
              <text class="section-title">选择图标</text>
            </view>
            <view class="emoji-grid">
              <view
                v-for="(emoji, index) in emojiOptions"
                :key="emoji"
                class="emoji-item"
                :class="{ active: newItem.icon === emoji }"
                :style="{ animationDelay: index * 20 + 'ms' }"
                @tap="newItem.icon = emoji"
              >
                <text class="emoji-text">{{ emoji }}</text>
              </view>
            </view>
          </view>

          <!-- 分割线 -->
          <view class="divider"></view>

          <!-- 分类选择 -->
          <view class="form-section">
            <view class="section-header">
              <view class="section-icon">
                <text class="section-icon-text">📂</text>
              </view>
              <text class="section-title">分类</text>
            </view>
            <view class="type-options">
              <view
                v-for="t in typeOptions"
                :key="t.value"
                class="type-option"
                :class="{ active: newItem.type === t.value }"
                @tap="newItem.type = t.value"
              >
                <view class="type-option-icon">
                  <text class="type-icon-text">{{ t.icon }}</text>
                </view>
                <text class="type-option-label">{{ t.label }}</text>
                <view v-if="newItem.type === t.value" class="type-check">
                  <text class="check-icon">✓</text>
                </view>
              </view>
            </view>
          </view>

        </view>

        <!-- 提示卡片 -->
        <view class="tip-card">
          <view class="tip-icon-box">
            <text class="tip-icon">💡</text>
          </view>
          <view class="tip-content">
            <text class="tip-title">温馨提示</text>
            <text class="tip-desc">自定义打卡项仅自己可见，可在管理中随时隐藏或删除</text>
          </view>
        </view>

      </view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <view class="bottom-bar-inner" :style="{ paddingBottom: 'max(16rpx, env(safe-area-inset-bottom))' }">
        <view class="save-btn" @tap="onSave">
          <text class="save-btn-text">保存打卡项</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLogin } from '@/utils/index'
import { createCheckinItem } from '@/api/checkin'

const EMOJI_OPTIONS = [
  '🍖', '🦴', '🚶', '🧹', '💊', '🛁', '🎾', '🎓',
  '🪮', '🪥', '🩺', '💉', '🏠', '🥛', '💤', '🐕',
  '🐈', '🐹', '🐰', '🦜', '🐢', '🐟', '🐍', '🦎'
]

const TYPE_OPTIONS = [
  { value: 1, label: '日常', icon: '☀️' },
  { value: 2, label: '健康', icon: '💊' },
  { value: 3, label: '训练', icon: '🎓' }
]

export default {
  data() {
    return {
      statusBarHeight: 20,
      newItem: { name: '', icon: '🍖', type: 1 },
      emojiOptions: EMOJI_OPTIONS,
      typeOptions: TYPE_OPTIONS
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
    getTypeLabel(type) {
      const t = TYPE_OPTIONS.find(o => o.value === type)
      return t ? t.label : '日常'
    },
    async onSave() {
      if (!this.newItem.name.trim()) {
        uni.showToast({ title: '请输入名称', icon: 'none' })
        return
      }
      const loggedIn = await checkLogin('请先登录')
      if (!loggedIn) return

      try {
        const res = await createCheckinItem({
          name: this.newItem.name.trim(),
          icon: this.newItem.icon,
          type: this.newItem.type
        })
        if (res && res.success) {
          uni.showToast({ title: '添加成功', icon: 'success' })
          setTimeout(() => { uni.navigateBack() }, 1000)
        } else {
          uni.showToast({ title: (res && res.message) || '添加失败', icon: 'none' })
        }
      } catch (e) {
        console.error('添加自定义打卡项失败:', e)
        uni.showToast({ title: '添加失败', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ============================================
   添加打卡项页面 - 专业UI设计系统 v2.0
   设计原则：简洁、清晰、高可读性
   ============================================ */

/* ====== CSS变量系统 ====== */
.add-item-page {
  --color-primary: #ff6b35;
  --color-primary-light: #ff8c5a;
  --color-primary-dark: #e55a2b;
  --color-surface: #ffffff;
  --color-surface-elevated: #fafafa;
  --color-background: #f5f5f7;
  --color-text-primary: #1d1d1f;
  --color-text-secondary: #6e6e73;
  --color-text-tertiary: #aeaeb2;
  --color-border: rgba(0, 0, 0, 0.06);
  --color-border-strong: rgba(0, 0, 0, 0.12);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.03);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.08), 0 4px 8px rgba(0, 0, 0, 0.04);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.12), 0 8px 16px rgba(0, 0, 0, 0.06);
  --radius-sm: 12rpx;
  --radius-md: 20rpx;
  --radius-lg: 28rpx;
  --radius-xl: 36rpx;
  --transition-fast: 150ms cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 250ms cubic-bezier(0.4, 0, 0.2, 1);
  --transition-spring: 400ms cubic-bezier(0.34, 1.56, 0.64, 1);

  min-height: 100vh;
  background: var(--color-background);
}

/* ====== 导航栏 ====== */
.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
}

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.nav-back {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);

  &:active {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(0.92);
  }
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg);
  margin-left: 4rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5rpx;
}

.nav-placeholder {
  width: 72rpx;
}

/* ====== 滚动容器 ====== */
.page-scroll {
  height: 100vh;
}

.page-content {
  padding: 32rpx 28rpx;
}

/* ====== 预览卡片 ====== */
.preview-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 32rpx;
  margin-bottom: 28rpx;
  box-shadow: var(--shadow-lg);
  border: 1rpx solid var(--color-border);
  overflow: hidden;

  animation: slideUp 0.5s var(--transition-spring);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.preview-header {
  margin-bottom: 24rpx;
}

.preview-label {
  font-size: 25rpx;
  font-weight: 600;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.preview-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28rpx 0;
  background: linear-gradient(135deg, #fff9f5 0%, #fffbf7 50%, #fef9f6 100%);
  border-radius: var(--radius-lg);
  border: 1rpx solid rgba(255, 107, 53, 0.08);
}

.preview-icon-wrap {
  width: 140rpx;
  height: 140rpx;
  border-radius: var(--radius-lg);
  background: linear-gradient(145deg, #ffffff 0%, #fff5ee 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  box-shadow:
    0 8rpx 24rpx rgba(255, 107, 53, 0.12),
    0 2rpx 8rpx rgba(0, 0, 0, 0.04),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.8);
  transition: all var(--transition-normal);

  &:active {
    transform: scale(0.96);
  }
}

.preview-icon {
  font-size: 72rpx;
  line-height: 1;
}

.preview-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.preview-name {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--color-text-primary);
  max-width: 320rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.preview-badge {
  padding: 10rpx 28rpx;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 107, 53, 0.04) 100%);
  border-radius: 999rpx;
  border: 1rpx solid rgba(255, 107, 53, 0.15);
}

.preview-badge-text {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--color-primary);
  letter-spacing: 0.5rpx;
}

/* ====== 表单卡片 ====== */
.form-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 8rpx 32rpx;
  margin-bottom: 28rpx;
  box-shadow: var(--shadow-lg);
  border: 1rpx solid var(--color-border);
  animation: slideUp 0.5s var(--transition-spring) 0.1s both;
}

.form-section {
  padding: 32rpx 0;

  &:first-child {
    padding-top: 24rpx;
  }

  &:last-child {
    padding-bottom: 24rpx;
  }
}

.divider {
  height: 1rpx;
  background: linear-gradient(90deg, transparent, var(--color-border-strong), transparent);
  margin: 0 16rpx;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 22rpx;
}

.section-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-icon-text {
  font-size: 26rpx;
}

.section-title {
  font-size: 29rpx;
  font-weight: 600;
  color: var(--color-text-primary);
  letter-spacing: 0.3rpx;
}

/* ====== 输入框 ====== */
.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  height: 96rpx;
  border: 2rpx solid var(--color-border-strong);
  border-radius: var(--radius-md);
  padding: 0 28rpx;
  font-size: 30rpx;
  color: var(--color-text-primary);
  background: var(--color-surface-elevated);
  box-sizing: border-box;
  transition: all var(--transition-fast);

  &::placeholder {
    color: var(--color-text-tertiary);
  }

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
    background: #fff;
  }
}

.input-count {
  position: absolute;
  right: 24rpx;
  bottom: -32rpx;
  font-size: 23rpx;
  color: var(--color-text-tertiary);
  font-weight: 500;
}

/* ====== Emoji网格 ====== */
.emoji-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16rpx;
}

.emoji-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  background: var(--color-surface-elevated);
  border: 2rpx solid transparent;
  transition: all var(--transition-spring);
  position: relative;
  overflow: hidden;

  &:active {
    transform: scale(0.9);
  }

  &.active {
    background: linear-gradient(135deg, #fff5f0 0%, #ffede0 100%);
    border-color: var(--color-primary);
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.18);
    transform: scale(1.02);

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: calc(var(--radius-md) - 2rpx);
      border: 2rpx solid var(--color-primary);
      opacity: 0.25;
    }
  }
}

.emoji-text {
  font-size: 44rpx;
  line-height: 1;
  transition: transform var(--transition-spring);

  .emoji-item.active & {
    transform: scale(1.08);
  }
}

/* ====== 分类选项 ====== */
.type-options {
  display: flex;
  gap: 20rpx;
}

.type-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  height: 120rpx;
  border-radius: var(--radius-md);
  background: var(--color-surface-elevated);
  border: 2rpx solid var(--color-border);
  position: relative;
  transition: all var(--transition-spring);
  overflow: hidden;

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background: linear-gradient(135deg, #fff5f0 0%, #ffede0 100%);
    border-color: var(--color-primary);
    box-shadow: 0 6rpx 20rpx rgba(255, 107, 53, 0.16);
    transform: scale(1.02);
  }
}

.type-option-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 107, 53, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-normal);

  .type-option.active & {
    background: var(--color-primary);
  }
}

.type-icon-text {
  font-size: 30rpx;
  transition: transform var(--transition-spring);

  .type-option.active & {
    transform: scale(1.1);
  }
}

.type-option-label {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);

  .type-option.active & {
    color: var(--color-primary);
    font-weight: 700;
  }
}

.type-check {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: popIn 0.3s var(--transition-spring);
}

@keyframes popIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.check-icon {
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
}

/* ====== 提示卡片 ====== */
.tip-card {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-radius: var(--radius-lg);
  padding: 28rpx;
  border: 1rpx solid rgba(245, 158, 11, 0.15);
  box-shadow: var(--shadow-sm);
  animation: slideUp 0.5s var(--transition-spring) 0.2s both;
}

.tip-icon-box {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.2);
}

.tip-icon {
  font-size: 28rpx;
}

.tip-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.tip-title {
  font-size: 27rpx;
  font-weight: 700;
  color: #92400e;
}

.tip-desc {
  font-size: 25rpx;
  color: #a16207;
  line-height: 1.6;
  font-weight: 500;
}

/* ====== 底部按钮栏 ====== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1rpx solid var(--color-border);
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);
}

.bottom-bar-inner {
  padding: 20rpx 32rpx;
}

.save-btn {
  width: 100%;
  height: 104rpx;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 8rpx 24rpx rgba(255, 107, 53, 0.35),
    0 4rpx 12rpx rgba(255, 107, 53, 0.2),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  transition: all var(--transition-spring);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 50%;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.15) 0%, transparent 100%);
    pointer-events: none;
  }

  &:active {
    transform: scale(0.97) translateY(2rpx);
    box-shadow:
      0 4rpx 12rpx rgba(255, 107, 53, 0.3),
      0 2rpx 6rpx rgba(255, 107, 53, 0.15),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.2);
  }
}

.save-btn-text {
  font-size: 33rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2rpx;
}

/* ====== 暗色模式 ====== */
page.dark .add-item-page {
  --color-surface: #1c1c1e;
  --color-surface-elevated: #2c2c2e;
  --color-background: #000000;
  --color-text-primary: #f5f5f7;
  --color-text-secondary: #98989d;
  --color-text-tertiary: #636366;
  --color-border: rgba(255, 255, 255, 0.08);
  --color-border-strong: rgba(255, 255, 255, 0.14);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.2), 0 1px 2px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.3), 0 2px 4px rgba(0, 0, 0, 0.15);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.4), 0 4px 8px rgba(0, 0, 0, 0.2);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.5), 0 8px 16px rgba(0, 0, 0, 0.25);
  --color-primary: #ff863a;
  --color-primary-light: #ffa066;
  --color-primary-dark: #e66a2a;
}

page.dark .nav-fixed {
  background: linear-gradient(135deg, #2a2a2c 0%, #1c1c1e 100%);
}

page.dark .nav-back {
  background: rgba(255, 255, 255, 0.1);

  &:active {
    background: rgba(255, 255, 255, 0.18);
  }
}

page.dark .preview-card,
page.dark .form-card {
  background: var(--color-surface);
  border-color: var(--color-border);
}

page.dark .preview-body {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.05) 0%, rgba(255, 134, 58, 0.02) 100%);
  border-color: rgba(255, 134, 58, 0.1);
}

page.dark .preview-icon-wrap {
  background: linear-gradient(145deg, #2c2c2e 0%, #3a3a3c 100%);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
}

page.dark .preview-name {
  color: var(--color-text-primary);
}

page.dark .preview-badge {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.12) 0%, rgba(255, 134, 58, 0.06) 100%);
  border-color: rgba(255, 134, 58, 0.2);
}

page.dark .preview-badge-text {
  color: var(--color-primary-light);
}

page.dark .section-icon {
  background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
}

page.dark .section-title {
  color: var(--color-text-primary);
}

page.dark .form-input {
  background: var(--color-surface-elevated);
  border-color: var(--color-border-strong);
  color: var(--color-text-primary);

  &::placeholder {
    color: var(--color-text-tertiary);
  }

  &:focus {
    background: var(--color-surface);
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4rpx rgba(255, 134, 58, 0.15);
  }
}

page.dark .input-count {
  color: var(--color-text-tertiary);
}

page.dark .emoji-item {
  background: var(--color-surface-elevated);
  border-color: transparent;

  &.active {
    background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
    border-color: var(--color-primary);
    box-shadow: 0 4rpx 16rpx rgba(255, 134, 58, 0.2);

    &::after {
      border-color: var(--color-primary);
      opacity: 0.2;
    }
  }
}

page.dark .type-option {
  background: var(--color-surface-elevated);
  border-color: var(--color-border);

  &.active {
    background: linear-gradient(135deg, rgba(255, 134, 58, 0.15) 0%, rgba(255, 134, 58, 0.08) 100%);
    border-color: var(--color-primary);
    box-shadow: 0 6rpx 20rpx rgba(255, 134, 58, 0.2);
  }
}

page.dark .type-option-icon {
  background: rgba(255, 134, 58, 0.12);

  .type-option.active & {
    background: var(--color-primary);
  }
}

page.dark .type-option-label {
  color: var(--color-text-secondary);

  .type-option.active & {
    color: var(--color-primary-light);
  }
}

page.dark .type-check {
  background: var(--color-primary);
}

page.dark .tip-card {
  background: linear-gradient(135deg, rgba(255, 200, 80, 0.08) 0%, rgba(255, 180, 60, 0.05) 100%);
  border-color: rgba(245, 158, 11, 0.2);
}

page.dark .tip-icon-box {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.3) 0%, rgba(245, 158, 11, 0.2) 100%);
  box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.15);
}

page.dark .tip-title {
  color: #fbbf24;
}

page.dark .tip-desc {
  color: #d97706;
}

page.dark .bottom-bar {
  background: rgba(28, 28, 30, 0.94);
  border-top-color: var(--color-border);
}

page.dark .save-btn {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  box-shadow:
    0 8rpx 24rpx rgba(255, 134, 58, 0.35),
    0 4rpx 12rpx rgba(255, 134, 58, 0.2),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.1);

  &:active {
    box-shadow:
      0 4rpx 12rpx rgba(255, 134, 58, 0.25),
      0 2rpx 6rpx rgba(255, 134, 58, 0.12),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.1);
  }
}
</style>
