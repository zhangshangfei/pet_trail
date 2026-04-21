<template>
  <view class="user-topbar">
    <view class="user-topbar-statusbar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="user-topbar-inner">
      <view v-if="showLoginButton" class="user-topbar-login-wrap" @tap="$emit('loginTap')">
        <avatar-view :src="avatar" :name="name || '宠'" :size="64" />
        <text class="user-topbar-name">请登录</text>
      </view>
      <view v-else class="user-topbar-user" @tap="$emit('userTap')">
        <avatar-view :src="avatar" :name="name" :size="64" />
        <text class="user-topbar-name">{{ name }}</text>
      </view>
      <view class="user-topbar-right-group" :style="{ marginRight: rightMargin + 'px' }">
        <view v-if="showDiscover" class="user-topbar-right" @tap="$emit('discoverTap')">
          <text class="user-topbar-right-icon">🔍</text>
        </view>
        <view v-if="showBell" class="user-topbar-right" @tap="$emit('rightTap')">
          <text class="user-topbar-right-icon">🔔</text>
          <view v-if="unreadCount > 0" class="user-topbar-badge">
            <text class="user-topbar-badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import AvatarView from './AvatarView.vue'

export default {
  name: 'UserTopBar',
  components: { AvatarView },
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
    showLoginButton: {
      type: Boolean,
      default: false
    },
    loginText: {
      type: String,
      default: '微信登录'
    },
    unreadCount: {
      type: Number,
      default: 0
    },
    showDiscover: {
      type: Boolean,
      default: true
    },
    showBell: {
      type: Boolean,
      default: true
    }
  },
  emits: ['rightTap', 'loginTap', 'userTap', 'discoverTap'],
  data() {
    return {
      rightMargin: 0
    }
  },
  created() {
    this.calcRightMargin()
  },
  methods: {
    calcRightMargin() {
      try {
        const menuButton = uni.getMenuButtonBoundingClientRect()
        const sysInfo = uni.getSystemInfoSync()
        this.rightMargin = sysInfo.windowWidth - menuButton.left + 8
      } catch (e) {
        this.rightMargin = 0
      }
    }
  }
}
</script>

<style scoped>
.user-topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
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
  margin-right: 14rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.85);
}

.user-topbar-name {
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 600;
}

.user-topbar-right-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.user-topbar-right {
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.25);
  position: relative;
}

.user-topbar-right-icon {
  color: #ffffff;
  font-size: 30rpx;
  line-height: 1;
}

.user-topbar-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  border-radius: 16rpx;
  background: #ff4d4f;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  border: 2rpx solid #ff7a3d;
}

.user-topbar-badge-text {
  font-size: 18rpx;
  color: #fff;
  font-weight: 600;
  line-height: 1;
}
</style>
