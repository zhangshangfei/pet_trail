<template>
  <view class="add-item-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">添加打卡项</text>
        <view class="nav-save" @tap="onSave">
          <text class="nav-save-text">保存</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">

        <view class="preview-card">
          <view class="preview-item">
            <view class="preview-icon-box">
              <text class="preview-emoji">{{ newItem.icon }}</text>
            </view>
            <text class="preview-name">{{ newItem.name || '打卡项名称' }}</text>
            <text class="preview-type">{{ getTypeLabel(newItem.type) }}</text>
          </view>
        </view>

        <view class="form-card">
          <view class="form-group">
            <text class="form-label">名称</text>
            <input
              class="form-input"
              v-model="newItem.name"
              placeholder="给打卡项起个名字"
              maxlength="10"
              focus
            />
            <text class="form-count">{{ newItem.name.length }}/10</text>
          </view>

          <view class="form-group">
            <text class="form-label">选择图标</text>
            <view class="emoji-grid">
              <view
                v-for="emoji in emojiOptions"
                :key="emoji"
                class="emoji-item"
                :class="{ active: newItem.icon === emoji }"
                @tap="newItem.icon = emoji"
              >
                <text class="emoji-text">{{ emoji }}</text>
              </view>
            </view>
          </view>

          <view class="form-group">
            <text class="form-label">分类</text>
            <view class="type-row">
              <view
                v-for="t in typeOptions"
                :key="t.value"
                class="type-chip"
                :class="{ active: newItem.type === t.value }"
                @tap="newItem.type = t.value"
              >
                <text class="type-chip-icon">{{ t.icon }}</text>
                <text class="type-chip-text">{{ t.label }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="tip-card">
          <text class="tip-icon">💡</text>
          <text class="tip-text">自定义打卡项仅自己可见，可在管理中随时隐藏或删除</text>
        </view>

      </view>
    </scroll-view>
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
$primary: #ff6a3d;
$primary-light: #fff0ea;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.add-item-page { min-height: 100vh; background: $bg; }

.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar {
  height: 92rpx; display: flex; align-items: center;
  justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0;
}
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-save { padding: 12rpx 28rpx; border-radius: 28rpx; background: $primary; }
.nav-save-text { font-size: 28rpx; color: #fff; font-weight: 600; }

.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.preview-card {
  background: $card-bg; border-radius: 24rpx; padding: 32rpx;
  margin-bottom: 20rpx; display: flex; justify-content: center;
}
.preview-item {
  display: flex; flex-direction: column; align-items: center;
  width: 200rpx;
}
.preview-icon-box {
  width: 120rpx; height: 120rpx; border-radius: 28rpx;
  background: $primary-light; display: flex;
  align-items: center; justify-content: center; margin-bottom: 16rpx;
}
.preview-emoji { font-size: 64rpx; }
.preview-name {
  font-size: 28rpx; font-weight: 600; color: $text-primary;
  margin-bottom: 4rpx; max-width: 200rpx;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.preview-type { font-size: 22rpx; color: $text-light; }

.form-card {
  background: $card-bg; border-radius: 24rpx; padding: 28rpx; margin-bottom: 20rpx;
}
.form-group { margin-bottom: 32rpx; }
.form-group:last-child { margin-bottom: 0; }
.form-label { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; margin-bottom: 16rpx; }
.form-input {
  width: 100%; height: 88rpx; border: 2rpx solid #e8e8e8; border-radius: 16rpx;
  padding: 0 24rpx; font-size: 30rpx; color: $text-primary; box-sizing: border-box;
}
.form-count { display: block; text-align: right; font-size: 22rpx; color: $text-light; margin-top: 8rpx; }

.emoji-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16rpx; }
.emoji-item {
  aspect-ratio: 1; display: flex; align-items: center; justify-content: center;
  border-radius: 16rpx; background: #f5f5f5; border: 2rpx solid transparent;
  transition: all 0.2s;
}
.emoji-item.active { border-color: $primary; background: $primary-light; transform: scale(1.05); }
.emoji-text { font-size: 40rpx; }

.type-row { display: flex; gap: 16rpx; }
.type-chip {
  flex: 1; display: flex; align-items: center; justify-content: center;
  gap: 8rpx; height: 80rpx; border-radius: 16rpx; background: #f5f5f5;
  border: 2rpx solid transparent; transition: all 0.2s;
}
.type-chip.active { border-color: $primary; background: $primary-light; }
.type-chip-icon { font-size: 28rpx; }
.type-chip-text { font-size: 28rpx; color: $text-secondary; font-weight: 500; }
.type-chip.active .type-chip-text { color: $primary; font-weight: 600; }

.tip-card {
  display: flex; align-items: flex-start; padding: 24rpx;
  background: #fffbf0; border-radius: 16rpx; margin-bottom: 40rpx;
}
.tip-icon { font-size: 28rpx; margin-right: 12rpx; flex-shrink: 0; }
.tip-text { font-size: 24rpx; color: #b8860b; line-height: 1.6; }
</style>
