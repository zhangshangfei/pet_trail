<template>
  <view class="me-page">
    <!-- 顶部用户栏 -->
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="avatarUrl"
      :name="userName"
      :show-discover="false"
      :show-bell="false"
      @userTap="onTopUserTap"
      @loginTap="onTopUserTap"
    />

    <!-- 主内容区 -->
    <scroll-view scroll-y class="me-scroll" :style="{ height: scrollHeight + 'px', paddingTop: headerHeight + 'px', paddingBottom: '140rpx' }">
      <view class="me-content">

        <!-- Bento Grid 布局 -->
        <view class="bento-grid">

          <!-- 宠物区域 - 全新设计 -->
          <view class="bento-card bento-pets-new">
            <!-- 顶部标题区 -->
            <view class="pets-header">
              <view class="pets-title-area">
                <text class="pets-title">🐾 我的宠物</text>
                <view class="pets-count">{{ pets.length }}只</view>
              </view>
            </view>

            <!-- 分隔线 -->
            <view class="pets-divider"></view>

            <!-- 宠物网格 -->
            <view class="pets-grid">
              <!-- 宠物卡片 -->
              <view
                v-for="(pet, index) in pets"
                :key="pet.id || index"
                class="pet-card"
                @tap="goPetDetail(pet.id)"
              >
                <view class="pet-avatar-box">
                  <avatar-view :src="pet.avatar || ''" :name="pet.name || ''" :id="pet.id" :size="90" />
                  <view class="pet-online-dot"></view>
                </view>
                <text class="pet-card-name">{{ pet.name || ('宠物' + (index + 1)) }}</text>
              </view>

              <!-- 添加宠物卡片 -->
              <view class="pet-card pet-card-add" @tap.stop="goAddPet">
                <view class="pet-add-box">
                  <text class="pet-add-icon-big">＋</text>
                </view>
                <text class="pet-card-add-text">添加新宠物</text>
              </view>
            </view>

            <!-- 底部快捷操作 - 一行并排 -->
            <view class="pets-footer-row">
              <view class="footer-btn-inline footer-btn-primary" @tap="goToHealthRecord">
                <text class="footer-btn-icon-text">📊</text>
                <text class="footer-btn-label">查看健康记录</text>
                <text class="footer-btn-arrow">›</text>
              </view>
              <view class="footer-btn-inline footer-btn-secondary" @tap="goToFeedingReminder">
                <text class="footer-btn-icon-text">⏰</text>
                <text class="footer-btn-label">设置喂食提醒</text>
                <text class="footer-btn-arrow">›</text>
              </view>
            </view>
          </view>

          <!-- 功能网格 - 6个功能项 -->
          <view class="bento-card bento-features">
            <view class="card-header">
              <view class="header-left">
                <text class="card-title">常用功能</text>
                <view class="feature-count">{{ visibleFeatures.length }}个功能</view>
              </view>
            </view>

            <view class="features-grid">
              <view
                v-for="(feature, index) in visibleFeatures"
                :key="feature.name"
                class="feature-cell"
                :class="'feature-' + (index % 3)"
                @tap="feature.action"
              >
                <view class="feature-icon-box">
                  <text class="feature-icon">{{ feature.icon }}</text>
                </view>
                <text class="feature-name">{{ feature.name }}</text>
              </view>
            </view>
          </view>

          <!-- 设置列表 -->
          <view class="bento-card bento-settings">
            <view class="card-header">
              <view class="header-left">
                <text class="card-title">更多选项</text>
              </view>
            </view>

            <view class="settings-list">
              <view
                v-for="(option, index) in settingsOptions"
                :key="option.name"
                class="settings-item"
                @tap="option.action"
              >
                <view class="settings-icon-box">
                  <text class="settings-icon">{{ option.icon }}</text>
                </view>
                <text class="settings-label">{{ option.name }}</text>
                <view class="settings-right">
                  <text class="settings-hint" v-if="option.hint">{{ option.hint }}</text>
                  <view class="settings-arrow-box">
                    <text class="settings-arrow">›</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

        </view>

        <!-- 底部版本信息 -->
        <view class="footer-info">
          <text class="footer-text">Pet Trail v2.0.0 · 用心记录每一个成长瞬间</text>
        </view>

      </view>
    </scroll-view>
  </view>
</template>

<script>
import UserTopBar from '@/components/UserTopBar.vue'
import AvatarView from '@/components/AvatarView.vue'
import { checkLogin, getUserAvatar, getPetAvatar, DEFAULT_USER_AVATAR, DEFAULT_PET_AVATAR_URL } from '@/utils/index'
import * as authApi from '@/api/auth'
import * as petApi from '@/api/pet'

export default {
  components: {
    UserTopBar,
    AvatarView
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 70,
      scrollHeight: 0,
      avatarUrl: DEFAULT_USER_AVATAR,
      defaultPetAvatar: DEFAULT_PET_AVATAR_URL,
      userName: "",
      pets: []
    };
  },
  computed: {
    visibleFeatures() {
      return [
        { name: '健康监测', icon: '📈', action: this.goToHealth },
        { name: '消息通知', icon: '🔔', action: this.goToNotification },
        { name: '个人主页', icon: '👤', action: this.goToUserDetail },
        { name: '发现用户', icon: '🔍', action: this.goToDiscover },
        { name: '成就墙', icon: '🏆', action: this.goToAchievement },
        { name: '挑战赛', icon: '🎯', action: this.goToChallenge }
      ]
    },
    settingsOptions() {
      return [
        { name: '关于我们', icon: 'ℹ️', hint: '', action: this.goToAbout },
        { name: '意见反馈', icon: '💬', hint: '', action: this.goToFeedback },
        { name: '系统设置', icon: '⚙️', hint: '', action: this.goToSettings }
      ]
    }
  },
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
    this.loadPets();
    this.loadUserInfo();

    uni.$on('loginSuccess', () => {
      this.loadUserInfo()
      this.loadPets()
    })
  },
  onHide() {
  },
  onUnload() {
    uni.$off('loginSuccess')
  },
  onPullDownRefresh() {
    this.loadUserInfo()
    this.loadPets()
    setTimeout(() => { uni.stopPullDownRefresh() }, 800)
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync();
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20;
      this.headerHeight = this.statusBarHeight + 54;
      this.scrollHeight = sys && sys.windowHeight ? sys.windowHeight : 0;
    } catch (e) {
      this.statusBarHeight = 20;
      this.headerHeight = 70;
      this.scrollHeight = 0;
    }
    this.loadUserInfo();
  },
  methods: {
    async loadUserInfo() {
      const userInfo = uni.getStorageSync('userInfo');
      const token = uni.getStorageSync('token');
      if (userInfo) {
        this.avatarUrl = getUserAvatar(userInfo.id, userInfo.avatar);
        this.userName = userInfo.nickname || '';
      } else if (!token) {
        this.avatarUrl = DEFAULT_USER_AVATAR;
        this.userName = '';
      }
      if (token) {
        try {
          const res = await authApi.getProfile();
          if (res && res.success) {
            const userData = res.data;
            this.avatarUrl = getUserAvatar(userData.id, userData.avatar);
            this.userName = userData.nickname || '';
            uni.setStorageSync('userInfo', userData);
          }
        } catch (e) {
          console.error('获取用户信息失败:', e);
        }
      }
    },
    async loadPets() {
      try {
        const res = await petApi.getPetList();
        if (res && res.success) {
          this.pets = Array.isArray(res.data) ? res.data : [];
        } else {
          this.pets = [];
        }
      } catch (e) {
        this.pets = [];
      }
    },
    onBellTap() {
      uni.navigateTo({ url: '/pages/notification/index' });
    },
    onTopUserTap() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo && userInfo.id) {
        uni.navigateTo({ url: `/pages/user/detail?id=${userInfo.id}` })
      }
    },
    async goAddPet() {
      const loggedIn = await checkLogin('请先登录后再添加宠物')
      if (!loggedIn) return
      uni.navigateTo({ url: '/pages/pet/edit' })
    },
    goPetDetail(petId) {
      if (!petId) return;
      uni.navigateTo({ url: `/pages/pets/detail?id=${petId}` });
    },
    goToHealthRecord() {
      uni.switchTab({ url: "/pages/dashboard/index" });
    },
    goToFeedingReminder() {
      uni.navigateTo({ url: '/pages/me/feeding-reminder' });
    },
    goToHealth() {
      uni.navigateTo({ url: "/pages/health/index" });
    },
    goToNotification() {
      uni.navigateTo({ url: '/pages/notification/index' });
    },
    goToUserDetail() {
      this.onTopUserTap();
    },
    goToDiscover() {
      uni.navigateTo({ url: '/pages/discover/index' });
    },
    goToMembership() {
      uni.navigateTo({ url: '/pages/me/membership' });
    },
    goToAchievement() {
      uni.navigateTo({ url: '/pages/me/achievement' });
    },
    goToChallenge() {
      uni.navigateTo({ url: '/pages/challenge/index' });
    },
    goToShop() {
      uni.navigateTo({ url: '/pages/shop/index' });
    },
    goToVet() {
      uni.navigateTo({ url: '/pages/vet/index' });
    },
    goToMyAppointments() {
      uni.navigateTo({ url: '/pages/vet/appointments' });
    },
    onDiscoverTap() {
      uni.navigateTo({ url: '/pages/discover/index' });
    },
    goToAbout() {
      uni.navigateTo({ url: '/pages/me/about' });
    },
    goToFeedback() {
      uni.navigateTo({ url: '/pages/me/feedback' });
    },
    goToSettings() {
      uni.navigateTo({ url: '/pages/me/settings' });
    }
  }
};
</script>

<style lang="scss" scoped>
/* ============================================
   我的页面 - 精致Bento Grid v5.1
   设计原则：简洁、精致、现代、有质感
   ============================================ */

.me-page {
  --color-primary: #ff6b35;
  --color-primary-light: #ff8c5a;
  --color-bg: #f2f2f7;
  --color-white: #ffffff;
  --color-gray-50: #fafafa;
  --color-gray-100: #f5f5f7;
  --color-gray-200: #e5e5ea;
  --color-gray-300: #d1d1d6;
  --color-gray-500: #8e8e93;
  --color-gray-800: #1c1c1e;
  --radius-sm: 12rpx;
  --radius-md: 16rpx;
  --radius-lg: 24rpx;

  min-height: 100vh;
  background: var(--color-bg);
}

.me-scroll {
  height: auto;
}

.me-content {
  padding: 24rpx;
}

/* ====== Bento Grid ====== */
.bento-grid {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.bento-card {
  background: var(--color-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow:
    0 2rpx 16rpx rgba(0, 0, 0, 0.04),
    0 1rpx 4rpx rgba(0, 0, 0, 0.02);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
  }
}

/* ====== 卡片头部 ====== */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 28rpx 20rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--color-gray-800);
  letter-spacing: -0.5rpx;
}

.pet-count-badge {
  min-width: 44rpx;
  height: 44rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 14rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: white;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.25);
}

.feature-count {
  font-size: 23rpx;
  color: var(--color-gray-500);
  font-weight: 500;
}

.add-btn-small {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08), rgba(255, 107, 53, 0.03));
  border: 1.5rpx solid rgba(255, 107, 53, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.92);
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.15), rgba(255, 107, 53, 0.08));
  }
}

.add-btn-icon {
  font-size: 36rpx;
  color: var(--color-primary);
  font-weight: 300;
}

/* ====== 宠物区域 - 全新设计 v6.0 ====== */
.bento-pets-new {
  background: linear-gradient(180deg, #ffffff 0%, #fffbf8 100%);
  border: 2rpx solid rgba(255, 107, 53, 0.12);
  
  .pets-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 28rpx 28rpx 20rpx;
  }

  .pets-title-area {
    display: flex;
    align-items: center;
    gap: 14rpx;
  }

  .pets-title {
    font-size: 32rpx;
    font-weight: 700;
    color: var(--color-gray-800);
  }

  .pets-count {
    padding: 6rpx 16rpx;
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    border-radius: 20rpx;
    font-size: 23rpx;
    font-weight: 600;
    color: white;
    box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.25);
  }

  /* 分隔线 */
  .pets-divider {
    height: 1rpx;
    margin: 0 28rpx;
    background: linear-gradient(90deg, transparent, rgba(255, 107, 53, 0.15), transparent);
  }

  /* 宠物网格 */
  .pets-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    padding: 28rpx;
  }

  .pet-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 20rpx 12rpx;
    background: var(--color-white);
    border-radius: var(--radius-md);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: var(--radius-md);
      border: 2rpx solid transparent;
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)) border-box;
      -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
      mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
      -webkit-mask-composite: xor;
      mask-composite: exclude;
      opacity: 0;
      transition: opacity 0.25s ease;
    }

    &:active {
      transform: scale(0.95) translateY(-4rpx);
      box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.15);

      &::before {
        opacity: 1;
      }
    }
  }

  .pet-avatar-box {
    width: 110rpx;
    height: 110rpx;
    border-radius: 50%;
    overflow: hidden;
    position: relative;
  }

  .pet-online-dot {
    position: absolute;
    bottom: 4rpx;
    right: 4rpx;
    width: 18rpx;
    height: 18rpx;
    border-radius: 50%;
    background: #34c759;
    border: 3rpx solid white;
    box-shadow: 0 2rpx 8rpx rgba(52, 199, 89, 0.4);
  }

  .pet-card-name {
    font-size: 24rpx;
    font-weight: 600;
    color: var(--color-gray-800);
    text-align: center;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* 添加宠物卡片 */
  .pet-card-add {
    background: linear-gradient(135deg, #fff9f5, #fffbf8);
    border: 2.5rpx dashed rgba(255, 107, 53, 0.3);

    .pet-add-box {
      width: 110rpx;
      height: 110rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(255, 107, 53, 0.08), rgba(255, 107, 53, 0.03));
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.25s ease;
    }

    &:active .pet-add-box {
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
      transform: scale(0.95) rotate(90deg);
    }

    .pet-add-icon-big {
      font-size: 52rpx;
      color: var(--color-primary);
      font-weight: 300;
      transition: all 0.25s ease;
    }

    &:active .pet-add-icon-big {
      color: white;
    }

    .pet-card-add-text {
      font-size: 23rpx;
      font-weight: 600;
      color: var(--color-primary);
    }
  }

  /* 底部快捷操作 - 一行并排 */
  .pets-footer-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16rpx;
    margin: 0 20rpx 24rpx;
  }

  .footer-btn-inline {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 22rpx 20rpx;
    border-radius: var(--radius-md);
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.97);
    }
  }

  .footer-btn-primary {
    background: linear-gradient(135deg, var(--color-primary), #e55a2b);
    color: white;
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.3);

    &:active {
      box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.2);
    }
  }

  .footer-btn-secondary {
    background: var(--color-white);
    color: var(--color-gray-800);
    border: 1.5rpx solid var(--color-gray-200);

    &:active {
      background: var(--color-gray-50);
      border-color: var(--color-gray-300);
    }
  }

  .footer-btn-icon-text {
    font-size: 28rpx;
    margin-right: 10rpx;
  }

  .footer-btn-label {
    flex: 1;
    font-size: 26rpx;
    font-weight: 600;
    text-align: center;
  }

  .footer-btn-arrow {
    font-size: 28rpx;
    opacity: 0.6;
    font-weight: 300;
  }
}

/* ====== 功能网格 ====== */
.bento-features {
  .features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12rpx;
    padding: 12rpx 28rpx 28rpx;
  }

  .feature-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 28rpx 10rpx 22rpx;
    border-radius: var(--radius-md);
    background: var(--color-gray-50);
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      width: 0;
      height: 3rpx;
      background: var(--color-primary);
      border-radius: 3rpx 3rpx 0 0;
      transform: translateX(-50%);
      transition: width 0.25s ease;
    }

    &:active {
      transform: scale(0.95);
      background: linear-gradient(180deg, var(--color-white), rgba(255, 107, 53, 0.05));

      &::after {
        width: 60%;
      }
    }
  }

  /* 功能项颜色变体 */
  &.feature-0:active::after { background: #ff6b35; }
  &.feature-1:active::after { background: #ff9500; }
  &.feature-2:active::after { background: #007aff; }
  &.feature-0:active { background: linear-gradient(180deg, #fff, rgba(255, 107, 53, 0.06)); }
  &.feature-1:active { background: linear-gradient(180deg, #fff, rgba(255, 149, 0, 0.06)); }
  &.feature-2:active { background: linear-gradient(180deg, #fff, rgba(0, 122, 255, 0.06)); }

  .feature-icon-box {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    background: var(--color-white);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 14rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
    transition: all 0.2s ease;

    .feature-cell:active & {
      transform: scale(1.05) translateY(-2rpx);
      box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
    }
  }

  .feature-icon {
    font-size: 42rpx;
  }

  .feature-name {
    font-size: 24rpx;
    font-weight: 600;
    color: var(--color-gray-800);
    text-align: center;
  }
}

/* ====== 设置列表 ====== */
.bento-settings {
  .settings-list {
    padding: 0 28rpx 8rpx;
  }

  .settings-item {
    display: flex;
    align-items: center;
    padding: 26rpx 0;
    border-bottom: 1rpx solid var(--color-gray-100);
    transition: all 0.15s ease;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 0;
      background: linear-gradient(90deg, rgba(255, 107, 53, 0.06), transparent);
      transition: width 0.2s ease;
    }

    &:last-child {
      border-bottom: none;
    }

    &:active {
      &::before {
        width: 100%;
      }
    }
  }

  .settings-icon-box {
    width: 52rpx;
    height: 52rpx;
    border-radius: var(--radius-sm);
    background: var(--color-gray-50);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 18rpx;
    transition: all 0.15s ease;

    .settings-item:active & {
      background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 107, 53, 0.05));
    }
  }

  .settings-icon {
    font-size: 30rpx;
  }

  .settings-label {
    flex: 1;
    font-size: 29rpx;
    font-weight: 600;
    color: var(--color-gray-800);
  }

  .settings-right {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  .settings-hint {
    font-size: 23rpx;
    color: var(--color-gray-500);
  }

  .settings-arrow-box {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: var(--color-gray-50);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.15s ease;

    .settings-item:active & {
      background: rgba(255, 107, 53, 0.1);
      transform: translateX(4rpx);
    }
  }

  .settings-arrow {
    font-size: 30rpx;
    color: var(--color-gray-400);
    font-weight: 300;
  }
}

/* ====== 底部信息 ====== */
.footer-info {
  text-align: center;
  padding: 32rpx 0 16rpx;
}

.footer-text {
  font-size: 23rpx;
  color: var(--color-gray-500);
  letter-spacing: 0.5rpx;
}

/* ====== 暗色模式 ====== */
page.dark .me-page {
  --color-bg: #000000;
  --color-white: #1c1c1e;
  --color-gray-50: #2c2c2e;
  --color-gray-100: #3a3a3c;
  --color-gray-200: #48484a;
  --color-gray-300: #636366;
  --color-gray-500: #98989d;
  --color-gray-800: #f5f5f7;
  --color-primary: #ff863a;
  --color-primary-light: #ffa066;
}

page.dark .bento-card {
  box-shadow:
    0 2rpx 16rpx rgba(0, 0, 0, 0.2),
    0 1rpx 4rpx rgba(0, 0, 0, 0.1);
}

page.dark .pet-avatar-border {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)) border-box;
}

/* ====== 宠物区域 - 暗色模式 ====== */
page.dark .bento-pets-new {
  background: linear-gradient(180deg, #1c1c1e 0%, rgba(255, 134, 58, 0.03) 100%);
  border-color: rgba(255, 134, 58, 0.15);

  .pets-add-btn {
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    box-shadow: 0 4rpx 16rpx rgba(255, 134, 58, 0.4);
  }

  .pets-divider {
    background: linear-gradient(90deg, transparent, rgba(255, 134, 58, 0.2), transparent);
  }

  .pet-card {
    background: #2c2c2e;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);

    &:active {
      box-shadow: 0 8rpx 24rpx rgba(255, 134, 58, 0.2);
    }
  }

  .pet-card-add {
    background: linear-gradient(135deg, rgba(255, 134, 58, 0.06), rgba(255, 134, 58, 0.02));
    border-color: rgba(255, 134, 58, 0.3);

    .pet-add-box {
      background: linear-gradient(135deg, rgba(255, 134, 58, 0.12), rgba(255, 134, 58, 0.06));
    }

    &:active .pet-add-box {
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    }
  }

  .pets-footer-row {
    /* 继承网格布局 */
  }

  .footer-btn-inline {
    &.footer-btn-primary {
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    }

    &.footer-btn-secondary {
      background: #2c2c2e;
      border-color: #3a3a3c;

      &:active {
        background: #3a3a3c;
      }
    }
  }
}

page.dark .pet-avatar-border {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)) border-box;
}

page.dark .pet-add-circle {
  background: #2c2c2e;
  border-color: #48484a;

  &:active {
    border-color: var(--color-primary);
    background: rgba(255, 134, 58, 0.08);
  }
}

page.dark .action-secondary {
  background: #2c2c2e;
  border-color: #3a3a3c;
  color: #f5f5f7;

  &:active {
    background: #3a3a3c;
    border-color: #48484a;
  }
}

page.dark .feature-cell {
  background: #2c2c2e;

  &:active {
    background: linear-gradient(180deg, #2c2c2e, rgba(255, 134, 58, 0.06));
  }

  &.feature-0:active { background: linear-gradient(180deg, #2c2c2e, rgba(255, 134, 58, 0.08)); }
  &.feature-1:active { background: linear-gradient(180deg, #2c2c2e, rgba(255, 149, 0, 0.08)); }
  &.feature-2:active { background: linear-gradient(180deg, #2c2c2e, rgba(0, 122, 255, 0.08)); }
}

page.dark .feature-icon-box {
  background: #3a3a3c;
}

page.dark .settings-item {
  border-bottom-color: #2c2c2e;

  &::before {
    background: linear-gradient(90deg, rgba(255, 134, 58, 0.08), transparent);
  }
}

page.dark .settings-icon-box {
  background: #2c2c2e;
}

page.dark .settings-arrow-box {
  background: #2c2c2e;
}
</style>
