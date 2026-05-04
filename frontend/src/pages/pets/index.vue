<template>
  <view class="pet-list">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">我的宠物</text>
      <button class="add-btn" type="primary" @click="showAddModal">+ 添加宠物</button>
    </view>

    <!-- 宠物列表 -->
    <view class="pet-list-container" v-if="pets.length > 0">
      <view class="pet-item" v-for="(pet, index) in pets" :key="pet.id" @click="goToDetail(pet.id)">
        <view class="pet-avatar">
          <image v-if="pet.avatar" :src="pet.avatar" mode="aspectFill"></image>
          <view v-else class="avatar-placeholder">
            <text class="placeholder-text">{{ pet.name ? pet.name[0] : '宠' }}</text>
          </view>
        </view>
        <view class="pet-info">
          <view class="pet-name">{{ pet.name }}</view>
          <view class="pet-breed">{{ pet.breed || '未填写品种' }}</view>
          <view class="pet-meta">
            <text class="pet-gender" v-if="pet.gender">
              {{ pet.gender === 1 ? '♂' : pet.gender === 2 ? '♀' : '' }}
            </text>
            <text class="pet-weight" v-if="pet.weight">
              {{ pet.weight }} kg
            </text>
            <text class="pet-birthday" v-if="pet.birthday">
              {{ formatDate(pet.birthday) }}
            </text>
          </view>
        </view>
        <view class="pet-action">
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <image class="empty-icon" src="/static/images/empty-pets.png" mode="aspectFit"></image>
      <text class="empty-text">还没有宠物哦</text>
      <button class="add-btn-large" type="primary" @click="showAddModal">添加第一只宠物</button>
    </view>

    <AddPetModal
      v-if="showModal"
      :initialForm="form"
      @close="hideAddModal"
      @save="submitForm"
    />
  </view>
</template>

<script>
import AddPetModal from "../../components/AddPetModal.vue";
import { checkLogin } from '@/utils/index'
import * as petApi from '@/api/pet'

export default {
  components: {
    AddPetModal
  },
  data() {
    return {
      pets: [],
      showModal: false,
      form: {
        name: '',
        category: 0,
        breed: '',
        gender: 0,
        sterilized: 0,
        birthday: '',
        weight: '',
        color: '',
        avatar: ''
      }
    };
  },
  onShow() {
    // 每次显示页面时重新加载宠物列表
    this.loadPets();
  },
  onLoad() {
    this.loadPets();
  },
  methods: {
    // 加载宠物列表
    async loadPets() {
      try {
        const res = await petApi.getPetList();
        console.log('[pet-list] 加载宠物列表响应:', res);
        if (res.success) {
          this.pets = res.data || [];
          console.log('[pet-list] 宠物列表数据:', this.pets);
          // 检查每个宠物的 avatar 字段
          this.pets.forEach((pet, index) => {
            console.log(`[pet-list] 宠物 ${index} - ID: ${pet.id}, 名称: ${pet.name}, avatar:`, pet.avatar);
          });
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('[pet-list] 加载宠物列表失败:', error);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    },

    // 显示添加弹窗
    async showAddModal() {
      const loggedIn = await checkLogin('请先登录后再添加宠物')
      if (!loggedIn) return

      this.showModal = true;
      this.form = {
        name: '',
        category: 0,
        breed: '',
        gender: 0,
        sterilized: 0,
        birthday: '',
        weight: '',
        color: '',
        avatar: ''
      };
    },

    // 隐藏添加弹窗
    hideAddModal() {
      this.showModal = false;
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },

    // 提交表单
    async submitForm(payload) {
      const loggedIn = await checkLogin('请先登录后再添加宠物')
      if (!loggedIn) return

      const data = payload || this.form;
      console.log('[pet-list] 提交表单数据:', data);
      this.form = data;
      if (!data.name) {
        uni.showToast({
          title: '请输入宠物名称',
          icon: 'none'
        });
        return;
      }

      try {
        const res = await petApi.createPet(data);
        console.log('[pet-list] 创建宠物响应:', res);

        if (res.success) {
          uni.showToast({
            title: '添加成功',
            icon: 'success'
          });
          this.hideAddModal();
          this.loadPets();
        } else {
          uni.showToast({
            title: res.message || '添加失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('[pet-list] 添加宠物失败:', error);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    },

    // 跳转到宠物详情
    goToDetail(petId) {
      uni.navigateTo({
        url: `/pages/pets/detail?id=${petId}`
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.pet-list {
  min-height: 100vh;
  background-color: var(--pt-bg);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 34rpx 30rpx 22rpx;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 55%, rgba(247, 243, 239, 0) 100%);
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
}

.add-btn {
  padding: 14rpx 28rpx;
  font-size: 28rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
  color: #ffffff;
}

.pet-list-container {
  padding: 20rpx;
}

.pet-item {
  display: flex;
  align-items: center;
  background-color: var(--pt-card);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: var(--pt-shadow-soft);
}

.pet-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 24rpx;
}

.pet-avatar image {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, var(--pt-primary-2) 0%, var(--pt-primary) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 48rpx;
  color: #ffffff;
  font-weight: bold;
}

.pet-info {
  flex: 1;
}

.pet-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8rpx;
}

.pet-breed {
  font-size: 26rpx;
  color: #999999;
  margin-bottom: 8rpx;
}

.pet-meta {
  display: flex;
  gap: 20rpx;
}

.pet-gender {
  font-size: 24rpx;
  color: var(--pt-primary);
  padding: 4rpx 12rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
}

.pet-weight {
  font-size: 24rpx;
  color: #666666;
}

.pet-birthday {
  font-size: 24rpx;
  color: #666666;
}

.pet-action {
  margin-left: 20rpx;
}

.arrow {
  font-size: 48rpx;
  color: #cccccc;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.empty-icon {
  width: 300rpx;
  height: 300rpx;
  margin-bottom: 40rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
  margin-bottom: 40rpx;
}

.add-btn-large {
  padding: 24rpx 80rpx;
  font-size: 32rpx;
  border-radius: 40rpx;
}
</style>