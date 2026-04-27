<template>
  <view class="user-topbar">
    <view class="user-topbar-statusbar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="user-topbar-inner">
      <view v-if="!isLoggedIn" class="user-topbar-login-wrap" @tap="onLoginTap">
        <view class="user-topbar-avatar-wrap">
          <avatar-view src="" name="宠" :size="72" />
        </view>
        <view class="user-topbar-text-group">
          <text class="user-topbar-greeting">Hi～</text>
          <text class="user-topbar-name user-topbar-name--login">点击登录</text>
        </view>
        <view class="user-topbar-login-arrow">
          <text class="login-arrow-icon">›</text>
        </view>
      </view>
      <view v-else class="user-topbar-user" @tap="$emit('userTap')">
        <view class="user-topbar-avatar-wrap">
          <avatar-view :src="avatar" :name="name || '宠'" :size="72" />
        </view>
        <view class="user-topbar-text-group">
          <text class="user-topbar-greeting">Hi～</text>
          <text class="user-topbar-name">{{ name || '宠迹主人' }}</text>
        </view>
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
import { wechatLogin } from '@/utils/index'

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
      default: ''
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
      rightMargin: 0,
      isLoggedIn: false
    }
  },
  watch: {
    avatar() {
      this.checkLogin()
    },
    name() {
      this.checkLogin()
    }
  },
  created() {
    this.calcRightMargin()
    this.checkLogin()
    uni.$on('loginSuccess', this.checkLogin)
    uni.$on('logout', this.checkLogin)
  },
  activated() {
    this.checkLogin()
  },
  beforeDestroy() {
    uni.$off('loginSuccess', this.checkLogin)
    uni.$off('logout', this.checkLogin)
  },
  methods: {
    checkLogin() {
      this.isLoggedIn = !!uni.getStorageSync('token')
    },
    async onLoginTap() {
      this.$emit('loginTap')
      const result = await wechatLogin()
      if (result) {
        this.checkLogin()
      }
    },
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
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
}

.user-topbar-statusbar {
  width: 100%;
}

.user-topbar-inner {
  height: 108rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.user-topbar-avatar-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 36rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.12);
  overflow: hidden;
  flex-shrink: 0;
}

.user-topbar-text-group {
  display: flex;
  flex-direction: column;
  margin-left: 18rpx;
}

.user-topbar-greeting {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 400;
  line-height: 1.2;
}

.user-topbar-name {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: 700;
  line-height: 1.3;
  max-width: 240rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-topbar-name--login {
  font-weight: 600;
  font-size: 30rpx;
}

.user-topbar-login-wrap {
  display: flex;
  align-items: center;
  padding: 10rpx 20rpx 10rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.user-topbar-login-wrap:active {
  background: rgba(255, 255, 255, 0.28);
}

.user-topbar-login-arrow {
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8rpx;
}

.login-arrow-icon {
  font-size: 36rpx;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
  line-height: 1;
}

.user-topbar-user {
  display: flex;
  align-items: center;
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
