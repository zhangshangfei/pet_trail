<template>
  <view class="me-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="avatarUrl"
      :name="userName"
      right-icon="🔔"
      @rightTap="onBellTap"
      @userTap="onTopUserTap"
    />

    <scroll-view scroll-y class="me-scroll" :style="{ height: scrollHeight + 'px', paddingTop: headerHeight + 'px' }">
      <view class="me-content">
        <!-- Pet management card -->
        <view class="card pet-card">
          <view class="pet-card-head">
            <text class="pet-card-title">我的宠物</text>
            <text class="pet-card-subtitle">共 {{ pets.length }} 只</text>
          </view>

          <view class="pet-avatars">
            <view
              v-for="(pet, index) in pets"
              :key="pet.id || index"
              class="pet-avatar-item"
              @tap="goPetDetail(pet.id)"
            >
              <image class="pet-avatar" :src="pet.avatar || defaultPetAvatar" mode="aspectFill" />
              <view class="pet-index-badge">{{ index + 1 }}</view>
            </view>

            <view class="pet-avatar-add" @tap.stop="goAddPet">
              <text class="pet-plus">＋</text>
            </view>
          </view>

          <view class="pet-actions">
            <view class="pet-action-btn pet-action-blue" @tap="goToHealthRecord">
              <text class="pet-action-icon">💙</text>
              <text class="pet-action-text">健康记录</text>
            </view>
            <view class="pet-action-btn pet-action-green" @tap="goToFeedingReminder">
              <text class="pet-action-icon">🔔</text>
              <text class="pet-action-text">喂食提醒</text>
            </view>
          </view>
        </view>

        <!-- Common features card -->
        <view class="card feature-card">
          <text class="feature-title">常用功能</text>
          <view class="feature-grid">
            <view class="feature-item" @tap="goToHealth">
              <view class="feature-icon feature-icon-blue"><text>📈</text></view>
              <text class="feature-text">健康监测</text>
            </view>
            <!-- 社区功能暂时隐藏
            <view class="feature-item" @tap="goToCommunity">
              <view class="feature-icon feature-icon-green"><text>👥</text></view>
              <text class="feature-text">宠物社区</text>
            </view>
            -->
            <!-- 商城功能暂时隐藏
            <view class="feature-item" @tap="goToShop">
              <view class="feature-icon feature-icon-orange"><text>🛒</text></view>
              <text class="feature-text">商城</text>
            </view>
            -->
            <!-- 训练功能暂时隐藏
            <view class="feature-item" @tap="goToTraining">
              <view class="feature-icon feature-icon-purple"><text>🎓</text></view>
              <text class="feature-text">训练</text>
            </view>
            -->
            <!-- 兽医功能暂时隐藏
            <view class="feature-item" @tap="goToVet">
              <view class="feature-icon feature-icon-red"><text>🩺</text></view>
              <text class="feature-text">兽医</text>
            </view>
            -->
            <!-- 相册功能暂时隐藏
            <view class="feature-item" @tap="goToGallery">
              <view class="feature-icon feature-icon-pink"><text>🖼️</text></view>
              <text class="feature-text">相册</text>
            </view>
            -->
          </view>
        </view>
      </view>
    </scroll-view>

    <view v-if="showAddPetModal" class="me-modal-mask" @tap="closeAddPetModal">
      <view class="me-modal-card" @tap.stop>
        <view class="me-modal-avatar" @tap="onPickAvatar">
          <view v-if="!addPetForm.avatar" class="me-modal-avatar-empty">
            <text class="me-modal-avatar-icon">📷</text>
            <text class="me-modal-avatar-tip">点击上传</text>
          </view>
          <image v-else class="me-modal-avatar-img" :src="addPetForm.avatar" mode="aspectFill" />
        </view>

        <view class="me-modal-grid">
          <view class="me-modal-field me-modal-field-full">
            <text class="me-modal-label">宠物昵称</text>
            <input class="me-modal-input" v-model="addPetForm.name" placeholder="请输入昵称" />
          </view>
          <view class="me-modal-field">
            <text class="me-modal-label">宠物品种</text>
            <input class="me-modal-input" v-model="addPetForm.breed" placeholder="请选择品种" />
          </view>
          <view class="me-modal-field">
            <text class="me-modal-label">体重 (kg)</text>
            <input class="me-modal-input" type="digit" v-model="addPetForm.weight" placeholder="输入体重" />
          </view>
          <view class="me-modal-field">
            <text class="me-modal-label">性别</text>
            <picker :range="genderLabels" :value="genderIndex" @change="onGenderChange">
              <view class="me-modal-select">
                <text v-if="genderIndex >= 0">{{ genderLabels[genderIndex] }}</text>
                <text v-else class="me-modal-placeholder">请选择</text>
              </view>
            </picker>
          </view>
          <view class="me-modal-field">
            <text class="me-modal-label">生日</text>
            <picker mode="date" :value="addPetForm.birthday" @change="onBirthdayChange">
              <view class="me-modal-select">
                <text v-if="addPetForm.birthday">{{ addPetForm.birthday }}</text>
                <text v-else class="me-modal-placeholder">请选择</text>
              </view>
            </picker>
          </view>
        </view>

        <view class="me-modal-actions">
          <button class="me-modal-btn me-modal-btn-cancel" @tap="closeAddPetModal">取消</button>
          <button class="me-modal-btn me-modal-btn-save" type="primary" @tap="submitAddPet(addPetForm)">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import UserTopBar from '@/components/UserTopBar.vue'
import { uploadImage } from '@/api/pet'
import { checkLogin } from '@/utils/index'

export default {
  components: {
    UserTopBar
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 70,
      scrollHeight: 0,
      avatarUrl: "https://ai-public.mastergo.com/ai/img_res/1774537096721a3K9mP2xQ7vN4rT8wY.jpg",
      defaultPetAvatar: "https://ai-public.mastergo.com/ai/img_res/1774575365924b4L8nQ3xR6vM9wP2yZ.jpg",
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
        avatar: ""
      }
    };
  },
  computed: {
    genderLabels() {
      return ["❓ 未知", "♂ 弟弟", "♀ 妹妹"];
    },
    genderIndex() {
      if (this.addPetForm.gender === 1) return 1;
      if (this.addPetForm.gender === 2) return 2;
      return 0;
    }
  },
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
    uni.$off('loginSuccess')
  },
  onUnload() {
    uni.$off('loginSuccess')
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
    // 加载用户信息
    async loadUserInfo() {
      // 先从本地缓存读取
      const userInfo = uni.getStorageSync('userInfo');
      if (userInfo && userInfo.avatar) {
        this.avatarUrl = userInfo.avatar;
        this.userName = userInfo.nickname || '宠物管家';
      }
      // 如果有 token，从后端获取最新数据
      const token = uni.getStorageSync('token');
      if (token) {
        try {
          const res = await uni.$request.get('/api/users/profile');
          if (res && res.success) {
            const userData = res.data;
            this.avatarUrl = userData.avatar || this.avatarUrl;
            this.userName = userData.nickname || this.userName;
            // 更新缓存
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
      uni.showToast({ title: "通知未实现", icon: "none" });
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
        avatar: ""
      };
      this.showAddPetModal = true;
    },
    closeAddPetModal() {
      this.showAddPetModal = false;
    },
    onGenderChange(e) {
      const idx = Number(e.detail.value);
      this.addPetForm.gender = idx === 1 ? 1 : idx === 2 ? 2 : 0;
    },
    onBirthdayChange(e) {
      this.addPetForm.birthday = e.detail.value;
    },
    onPickAvatar() {
      uni.showActionSheet({
        itemList: ["从相册选择", "拍照"],
        success: (res) => {
          const sourceType = res.tapIndex === 1 ? ["camera"] : ["album"];
          uni.chooseImage({
            count: 1,
            sourceType,
            success: async (imgRes) => {
              const path = imgRes.tempFilePaths && imgRes.tempFilePaths[0];
              if (!path) return;
              uni.showLoading({ title: "上传中..." });
              try {
                const up = await uploadImage(path);
                if (up && up.success && up.data && up.data.url) {
                  this.addPetForm.avatar = up.data.url;
                  uni.showToast({ title: "头像已上传", icon: "success" });
                } else {
                  throw new Error((up && up.message) || "上传失败");
                }
              } catch (e) {
                uni.showToast({ title: (e && e.message) || "上传失败", icon: "none" });
              } finally {
                uni.hideLoading();
              }
            }
          });
        }
      });
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
      uni.showToast({ title: "喂食提醒未实现", icon: "none" });
    },
    goToHealth() {
      uni.navigateTo({ url: "/pages/health/index" });
    },
    goToShop() {
      uni.showToast({ title: "商城未实现", icon: "none" });
    },
    goToTraining() {
      uni.showToast({ title: "训练未实现", icon: "none" });
    },
    goToVet() {
      uni.showToast({ title: "兽医未实现", icon: "none" });
    },
    goToGallery() {
      uni.showToast({ title: "相册未实现", icon: "none" });
    }
  }
};
</script>

<style lang="scss" scoped>
.me-page {
  min-height: 100vh;
  background: var(--pt-bg);
}

.me-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 20;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 10rpx 30rpx rgba(17, 24, 39, 0.06);
  padding: 0 20rpx 14rpx;
}

.me-statusbar {
  width: 100%;
}

.me-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.me-nav-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.me-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #e5e7eb;
}

.me-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
}

.me-nav-right {
  width: 96rpx;
  display: flex;
  justify-content: flex-end;
}

.me-bell {
  font-size: 38rpx;
  color: #6b7280;
}

.me-scroll {
  // 高度由模板内联 style 决定
  height: auto;
}

.me-content {
  padding: 80rpx 20rpx 220rpx;
}

.card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 28rpx;
  box-shadow: var(--pt-shadow-soft);
}

.pet-card {
  padding: 36rpx 24rpx 28rpx;
  margin-bottom: 24rpx;
}

.pet-card-head {
  text-align: center;
  margin-bottom: 28rpx;
}

.pet-card-title {
  display: block;
  font-size: 34rpx;
  font-weight: 900;
  color: #111827;
  margin-bottom: 8rpx;
}

.pet-card-subtitle {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
}

.pet-avatars {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24rpx;
  flex-wrap: wrap;
  margin-bottom: 30rpx;
}

.pet-avatar-item {
  position: relative;
}

.pet-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 56rpx;
  overflow: hidden;
  background: #e5e7eb;
  border: 4rpx solid rgba(255, 255, 255, 0.98);
  box-shadow: 0 10rpx 24rpx rgba(17, 24, 39, 0.08);
}

.pet-avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
}

.pet-avatar-fallback-text {
  color: #fff;
  font-size: 40rpx;
  font-weight: 900;
}

.pet-index-badge {
  position: absolute;
  bottom: -6rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  background: #3b82f6;
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.98);
}

.pet-avatar-add {
  width: 112rpx;
  height: 112rpx;
  border-radius: 56rpx;
  border: 4rpx dashed rgba(107, 114, 128, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.7);
}

.pet-plus {
  font-size: 52rpx;
  color: rgba(107, 114, 128, 0.7);
  font-weight: 900;
}

.pet-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
}

.pet-action-btn {
  height: 92rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  color: #fff;
  font-weight: 900;
}

.pet-action-blue {
  background: linear-gradient(180deg, #60a5fa 0%, #3b82f6 100%);
}

.pet-action-green {
  background: linear-gradient(180deg, #34d399 0%, #10b981 100%);
}

.pet-action-icon {
  font-size: 30rpx;
}

.pet-action-text {
  font-size: 28rpx;
}

.feature-card {
  padding: 28rpx 22rpx 24rpx;
}

.feature-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  color: #111827;
  margin-bottom: 18rpx;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
}

.feature-item {
  padding: 14rpx 10rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.feature-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  font-size: 34rpx;
}

.feature-text {
  font-size: 24rpx;
  color: #6b7280;
}

.feature-icon-blue { background: #dbeafe; }
.feature-icon-green { background: #d1fae5; }
.feature-icon-orange { background: #ffedd5; }
.feature-icon-purple { background: #ede9fe; }
.feature-icon-red { background: #fee2e2; }
.feature-icon-pink { background: #fce7f3; }
.me-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50000;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
}

.me-modal-card {
  width: 100%;
  max-width: 680rpx;
  background: #fff;
  border-radius: 28rpx;
  padding: 26rpx 24rpx;
}

.me-modal-avatar {
  width: 180rpx;
  height: 180rpx;
  margin: 0 auto 18rpx;
  border-radius: 90rpx;
  border: 4rpx dashed rgba(59,130,246,.4);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.me-modal-avatar-empty { display: flex; flex-direction: column; align-items: center; }
.me-modal-avatar-icon { font-size: 40rpx; }
.me-modal-avatar-tip { font-size: 22rpx; color: #6b7280; }
.me-modal-avatar-img { width: 100%; height: 100%; }

.me-modal-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.me-modal-field { display: flex; flex-direction: column; }
.me-modal-field-full { grid-column: 1 / -1; }
.me-modal-label { font-size: 22rpx; color: #6b7280; margin-bottom: 8rpx; }
.me-modal-input, .me-modal-select {
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 16rpx;
  font-size: 26rpx;
}
.me-modal-placeholder { color: #9ca3af; }
.me-modal-actions { display: flex; gap: 14rpx; margin-top: 18rpx; }
.me-modal-btn { flex: 1; border-radius: 999rpx; font-size: 26rpx; font-weight: 700; }
.me-modal-btn-cancel { background: #f3f4f6; color: #374151; }
.me-modal-btn-save { background: #3b82f6; color: #fff; }
</style>

