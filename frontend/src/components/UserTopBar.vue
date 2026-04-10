<template>
  <view class="user-topbar">
    <view class="user-topbar-statusbar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="user-topbar-inner">
      <view v-if="showLoginButton" class="user-topbar-login-wrap" @tap="$emit('loginTap')">
        <text class="user-topbar-login-text">{{ loginText }}</text>
      </view>
      <view v-else class="user-topbar-user" @tap="$emit('userTap')">
        <image class="user-topbar-avatar" :src="avatar" mode="aspectFill" />
        <text class="user-topbar-name">{{ name }}</text>
      </view>
      <view class="user-topbar-right" @tap="$emit('rightTap')">
        <text class="user-topbar-right-icon">{{ rightIcon }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'UserTopBar',
  props: {
    statusBarHeight: {
      type: Number,
      default: 20
    },
    avatar: {
      type: String,
      default: ''
    },
    name: {
      type: String,
      default: '小萌宠主人'
    },
    rightIcon: {
      type: String,
      default: '⋯'
    },
    showLoginButton: {
      type: Boolean,
      default: false
    },
    loginText: {
      type: String,
      default: '微信登录'
    }
  },
  emits: ['rightTap', 'loginTap', 'userTap']
}
</script>

<style scoped>
.user-topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 55%, rgba(255, 255, 255, 0) 100%);
}

.user-topbar-statusbar {
  width: 100%;
}

.user-topbar-inner {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.user-topbar-user {
  display: flex;
  align-items: center;
}

.user-topbar-login-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 56rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.22);
}

.user-topbar-login-text {
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 600;
}

.user-topbar-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 14rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.85);
}

.user-topbar-name {
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 600;
}

.user-topbar-right {
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.25);
}

.user-topbar-right-icon {
  color: #ffffff;
  font-size: 34rpx;
  line-height: 1;
}
</style>
