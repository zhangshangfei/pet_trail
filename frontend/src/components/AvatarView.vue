<template>
  <view class="avatar-wrapper" :style="wrapperStyle">
    <image v-if="src && src.startsWith('http')" class="avatar-image" :src="src" mode="aspectFill" :style="wrapperStyle" />
    <view v-else-if="defaultIcon && !name" class="avatar-icon" :style="iconStyle">
      <text class="icon-person" :style="iconTextStyle">👤</text>
    </view>
    <view v-else class="avatar-text" :style="textStyle">
      <text class="avatar-char" :style="charStyle">{{ displayChar }}</text>
    </view>
  </view>
</template>

<script>
import { getFirstChar, getAvatarColor } from '@/utils/index'

export default {
  name: 'AvatarView',
  props: {
    src: { type: String, default: '' },
    name: { type: String, default: '' },
    id: { type: [String, Number], default: '' },
    size: { type: Number, default: 72 },
    defaultIcon: { type: Boolean, default: false }
  },
  computed: {
    displayChar() {
      return getFirstChar(this.name)
    },
    bgColor() {
      return getAvatarColor(this.id || this.name)
    },
    wrapperStyle() {
      return {
        width: this.size + 'rpx',
        height: this.size + 'rpx',
        borderRadius: (this.size / 2) + 'rpx',
        display: 'block',
        lineHeight: '0'
      }
    },
    textStyle() {
      return {
        width: this.size + 'rpx',
        height: this.size + 'rpx',
        borderRadius: (this.size / 2) + 'rpx',
        backgroundColor: this.bgColor,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        lineHeight: '0'
      }
    },
    iconStyle() {
      return {
        width: this.size + 'rpx',
        height: this.size + 'rpx',
        borderRadius: (this.size / 2) + 'rpx',
        backgroundColor: '#c0c4cc',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        lineHeight: '0'
      }
    },
    iconTextStyle() {
      const fontSize = Math.max(20, Math.round(this.size * 0.5))
      return {
        fontSize: fontSize + 'rpx',
        lineHeight: '1'
      }
    },
    charStyle() {
      const fontSize = Math.max(20, Math.round(this.size * 0.45))
      return {
        fontSize: fontSize + 'rpx',
        color: '#ffffff',
        fontWeight: '600',
        lineHeight: '1'
      }
    }
  }
}
</script>

<style scoped>
.avatar-wrapper { overflow: hidden; flex-shrink: 0; }
.avatar-image { width: 100%; height: 100%; }
.avatar-text { flex-shrink: 0; }
.avatar-icon { flex-shrink: 0; }
</style>
