<template>
  <view class="me-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="avatarUrl"
      :name="userName"
      :show-discover="false"
      :show-bell="false"
      @userTap="onTopUserTap"
    />

    <scroll-view scroll-y class="me-scroll" :style="{ height: scrollHeight + 'px', paddingTop: headerHeight + 'px' }">
      <view class="me-content">
        <view class="card pet-card">
          <view class="card-label">
            <text class="card-label-icon">🐾</text>
            <text class="card-label-text">我的宠物</text>
            <text class="card-label-sub">共 {{ pets.length }} 只</text>
          </view>

          <view class="pet-avatars">
            <view
              v-for="(pet, index) in pets"
              :key="pet.id || index"
              class="pet-avatar-item"
              @tap="goPetDetail(pet.id)"
            >
              <image class="pet-avatar" :src="pet.avatar || defaultPetAvatar" mode="aspectFill" />
              <text class="pet-name-label">{{ pet.name || ('宠物' + (index + 1)) }}</text>
            </view>

            <view class="pet-avatar-add" @tap.stop="goAddPet">
              <text class="pet-plus">＋</text>
              <text class="pet-add-label">添加</text>
            </view>
          </view>

          <view class="pet-actions">
            <view class="pet-action-btn" @tap="goToHealthRecord">
              <text class="pet-action-icon">💙</text>
              <text class="pet-action-text">健康记录</text>
            </view>
            <view class="pet-action-btn" @tap="goToFeedingReminder">
              <text class="pet-action-icon">🔔</text>
              <text class="pet-action-text">喂食提醒</text>
            </view>
          </view>
        </view>

        <view class="card feature-card">
          <view class="card-label">
            <text class="card-label-icon">✨</text>
            <text class="card-label-text">常用功能</text>
          </view>
          <view class="feature-grid">
            <view class="feature-item" @tap="goToHealth">
              <view class="feature-icon-wrap feature-icon-orange"><text class="feature-emoji">📈</text></view>
              <text class="feature-text">健康监测</text>
            </view>
            <view class="feature-item" @tap="goToNotification">
              <view class="feature-icon-wrap feature-icon-red"><text class="feature-emoji">🔔</text></view>
              <text class="feature-text">消息通知</text>
            </view>
            <view class="feature-item" @tap="goToUserDetail">
              <view class="feature-icon-wrap feature-icon-blue"><text class="feature-emoji">👤</text></view>
              <text class="feature-text">个人主页</text>
            </view>
            <view class="feature-item" @tap="goToDiscover">
              <view class="feature-icon-wrap feature-icon-green"><text class="feature-emoji">🔍</text></view>
              <text class="feature-text">发现用户</text>
            </view>
          </view>
        </view>

        <view class="card settings-card">
          <view class="card-label">
            <text class="card-label-icon">⚙️</text>
            <text class="card-label-text">更多</text>
          </view>
          <view class="option-list">
            <view class="option-item" @tap="goToAbout">
              <text class="option-icon">ℹ️</text>
              <text class="option-text">关于我们</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item" @tap="goToFeedback">
              <text class="option-icon">📝</text>
              <text class="option-text">意见反馈</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item" @tap="goToSettings">
              <text class="option-icon">🔧</text>
              <text class="option-text">设置</text>
              <text class="option-arrow">›</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <AddPetModal
      v-if="showAddPetModal"
      :initialForm="addPetForm"
      @close="closeAddPetModal"
      @save="submitAddPet"
    />
  </view>
</template>

<script>
import UserTopBar from '@/components/UserTopBar.vue'
import AddPetModal from '@/components/AddPetModal.vue'
import { checkLogin, getUserAvatar, getPetAvatar, DEFAULT_USER_AVATAR, DEFAULT_PET_AVATAR_URL } from '@/utils/index'

export default {
  components: {
    UserTopBar,
    AddPetModal
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 70,
      scrollHeight: 0,
      avatarUrl: DEFAULT_USER_AVATAR,
      defaultPetAvatar: DEFAULT_PET_AVATAR_URL,
      userName: "宠物管家",
      pets: [],
      showAddPetModal: false,
      addPetForm: {
        name: "",
        breed: "",
        gender: 0,
        birthday: "",
        weight: "",
        color: "",
        avatar: "",
        category: 0,
        sterilized: 0
      }
    };
  },
  computed: {},
  onShow() {
    const tabBar = this.getTabBar && this.getTabBar();
    if (tabBar && tabBar.setData) tabBar.setData({ hidden: false });
    this.loadPets();
    this.loadUserInfo();

    uni.$on('loginSuccess', () => {
      this.loadUserInfo()
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
      this.headerHeight = this.statusBarHeight + 50;
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
      if (userInfo && userInfo.avatar) {
        this.avatarUrl = getUserAvatar(userInfo.id, userInfo.avatar);
        this.userName = userInfo.nickname || '宠物管家';
      }
      const token = uni.getStorageSync('token');
      if (token) {
        try {
          const res = await uni.$request.get('/api/users/profile');
          if (res && res.success) {
            const userData = res.data;
            this.avatarUrl = getUserAvatar(userData.id, userData.avatar);
            this.userName = userData.nickname || this.userName;
            uni.setStorageSync('userInfo', userData);
          }
        } catch (e) {
          console.error('获取用户信息失败:', e);
        }
      }
    },
    async loadPets() {
      try {
        const res = await uni.$request.get('/api/pets');
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

      this.addPetForm = {
        name: "",
        breed: "",
        gender: 0,
        birthday: "",
        weight: "",
        color: "",
        avatar: "",
        category: 0,
        sterilized: 0
      };
      this.showAddPetModal = true;
    },
    closeAddPetModal() {
      this.showAddPetModal = false;
    },
    async submitAddPet(payload) {
      const loggedIn = await checkLogin('请先登录后再添加宠物')
      if (!loggedIn) return

      try {
        const res = await uni.$request.post('/api/pets', payload);
        if (res && res.success) {
          uni.showToast({ title: "添加成功", icon: "success" });
          this.showAddPetModal = false;
          this.loadPets();
        } else {
          uni.showToast({ title: (res && res.message) || "添加失败", icon: "none" });
        }
      } catch (e) {
        uni.showToast({ title: "网络错误", icon: "none" });
      }
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
.me-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.me-scroll {
  height: auto;
}

.me-content {
  padding: 8rpx 20rpx 220rpx;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.card-label {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.card-label-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.card-label-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.card-label-sub {
  font-size: 22rpx;
  color: #9ca3af;
  margin-left: auto;
}

.pet-avatars {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  flex-wrap: wrap;
  margin-bottom: 24rpx;
  padding: 16rpx 0;
}

.pet-avatar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pet-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #e5e7eb;
  border: 4rpx solid #fff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.pet-name-label {
  font-size: 22rpx;
  color: #6b7280;
  margin-top: 8rpx;
  max-width: 100rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.pet-avatar-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100rpx;
}

.pet-plus {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx dashed #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  color: #9ca3af;
  background: #fafafa;
}

.pet-add-label {
  font-size: 22rpx;
  color: #9ca3af;
  margin-top: 8rpx;
}

.pet-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.pet-action-btn {
  height: 84rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.25);

  &:active {
    transform: scale(0.98);
  }
}

.pet-action-icon {
  font-size: 28rpx;
}

.pet-action-text {
  font-size: 26rpx;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 10rpx;
  border-radius: 16rpx;
  background: #f9fafb;

  &:active {
    background: #f3f4f6;
  }
}

.feature-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.feature-emoji {
  font-size: 36rpx;
}

.feature-icon-orange { background: #fff5f0; }
.feature-icon-red { background: #fff1f2; }
.feature-icon-blue { background: #eff6ff; }
.feature-icon-green { background: #ecfdf5; }

.feature-text {
  font-size: 24rpx;
  color: #374151;
  font-weight: 500;
}

.settings-card {
  padding-bottom: 8rpx;
}

.option-list {
  border-radius: 16rpx;
  overflow: hidden;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 24rpx 16rpx;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.option-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.option-text {
  flex: 1;
  font-size: 28rpx;
  color: #374151;
}

.option-arrow {
  font-size: 32rpx;
  color: #d1d5db;
}
</style>
