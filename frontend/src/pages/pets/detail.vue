<template>
  <view class="pet-detail-page">
    <view class="pd-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="pd-nav-inner">
        <view class="pd-back" @tap="goBack">
          <view class="pd-back-arrow"></view>
        </view>
        <text class="pd-nav-title">宠物详情</text>
      </view>
    </view>

    <scroll-view scroll-y class="pd-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view v-if="pet" class="pd-content">
        <view class="pd-hero">
          <view class="pd-hero-bg"></view>
          <view class="pd-hero-body">
            <view class="pd-avatar-wrap">
              <image v-if="pet.avatar" class="pd-avatar" :src="pet.avatar" mode="aspectFill" />
              <view v-else class="pd-avatar-empty">
                <text class="pd-avatar-letter">{{ pet.name ? pet.name[0] : '宠' }}</text>
              </view>
              <view class="pd-avatar-edit" @tap="onEditTap">
                <text class="pd-avatar-edit-icon">✎</text>
              </view>
            </view>
            <text class="pd-name">{{ pet.name }}</text>
            <text class="pd-breed">{{ pet.breed || '未填写品种' }}</text>
          </view>
        </view>

        <view class="pd-card">
          <view class="pd-info-grid">
            <view class="pd-info-item">
              <text class="pd-info-label">性别</text>
              <text class="pd-info-value">{{ getGenderText(pet.gender) }}</text>
            </view>
            <view class="pd-info-item">
              <text class="pd-info-label">类别</text>
              <text class="pd-info-value">{{ getCategoryText(pet.category) }}</text>
            </view>
            <view class="pd-info-item">
              <text class="pd-info-label">体重</text>
              <text class="pd-info-value">{{ pet.weight ? pet.weight + ' kg' : '-' }}</text>
            </view>
            <view class="pd-info-item">
              <text class="pd-info-label">生日</text>
              <text class="pd-info-value">{{ formatDate(pet.birthday) }}</text>
            </view>
            <view class="pd-info-item">
              <text class="pd-info-label">毛色</text>
              <text class="pd-info-value">{{ pet.color || '-' }}</text>
            </view>
            <view class="pd-info-item">
              <text class="pd-info-label">绝育</text>
              <text class="pd-info-value">{{ pet.sterilized === 1 ? '已绝育' : '未绝育' }}</text>
            </view>
          </view>
        </view>

        <view class="pd-shortcuts">
          <view class="pd-shortcut-item" @tap="goAlbum">
            <text class="pd-shortcut-icon">📷</text>
            <text class="pd-shortcut-label">成长相册</text>
          </view>
          <view class="pd-shortcut-item" @tap="goCalendar">
            <text class="pd-shortcut-icon">📅</text>
            <text class="pd-shortcut-label">宠物日历</text>
          </view>
          <view class="pd-shortcut-item" @tap="goWeightRecords">
            <text class="pd-shortcut-icon">⚖️</text>
            <text class="pd-shortcut-label">体重记录</text>
          </view>
          <view class="pd-shortcut-item" @tap="goVaccineList">
            <text class="pd-shortcut-icon">💉</text>
            <text class="pd-shortcut-label">疫苗记录</text>
          </view>
        </view>

        <view class="pd-danger-zone">
          <button class="pd-delete-btn" @tap="confirmDelete">删除宠物</button>
        </view>
      </view>

      <view v-else class="pd-loading">
        <text class="pd-loading-text">加载中...</text>
      </view>
    </scroll-view>

    <AddPetModal
      v-if="showEditModal"
      :initialForm="editForm"
      @close="closeEditModal"
      @save="submitEdit"
    />

    <view v-if="showDeleteConfirm" class="pd-confirm-mask" @tap="cancelDelete" @touchmove.stop.prevent>
      <view class="pd-confirm-sheet" @tap.stop>
        <view class="pd-confirm-body">
          <text class="pd-confirm-title">确认删除</text>
          <text class="pd-confirm-desc">确定要删除宠物「{{ pet ? pet.name : '' }}」吗？删除后不可恢复。</text>
        </view>
        <view class="pd-confirm-actions">
          <button class="pd-confirm-btn pd-confirm-cancel" @tap="cancelDelete">取消</button>
          <button class="pd-confirm-btn pd-confirm-danger" @tap="doDelete">删除</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import AddPetModal from '@/components/AddPetModal.vue'
import { checkLogin } from '@/utils/index'

export default {
  components: { AddPetModal },
  data() {
    return {
      petId: null,
      pet: null,
      statusBarHeight: 20,
      showEditModal: false,
      showDeleteConfirm: false,
      editForm: {}
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    if (options && options.id) {
      this.petId = options.id
      this.loadPetDetail(options.id)
    }
  },
  onShow() {
    if (this.petId) {
      this.loadPetDetail(this.petId)
    }
  },
  methods: {
    async loadPetDetail(petId) {
      try {
        const res = await uni.$request.get(`/api/pets/${petId}`)
        if (res && res.success) {
          this.pet = res.data
        } else {
          uni.showToast({ title: (res && res.message) || '加载失败', icon: 'none' })
        }
      } catch (error) {
        uni.showToast({ title: '网络错误', icon: 'none' })
      }
    },
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    getGenderText(gender) {
      const map = { 0: '未知', 1: '♂ 公', 2: '♀ 母' }
      return map[gender] || '未知'
    },
    getCategoryText(category) {
      const map = { 0: '其他', 1: '猫咪', 2: '狗狗' }
      return map[category] || '其他'
    },
    goBack() {
      uni.navigateBack()
    },
    goAlbum() {
      if (!this.pet || !this.pet.id) return
      uni.navigateTo({ url: `/pages/pets/album?petId=${this.pet.id}` })
    },
    goCalendar() {
      if (!this.pet || !this.pet.id) return
      uni.navigateTo({ url: `/pages/pets/calendar?petId=${this.pet.id}` })
    },
    goWeightRecords() {
      if (!this.pet || !this.pet.id) return
      uni.navigateTo({ url: `/pages/health/index?petId=${this.pet.id}` })
    },
    goVaccineList() {
      if (!this.pet || !this.pet.id) return
      uni.navigateTo({ url: `/pages/health/vaccine-list?petId=${this.pet.id}` })
    },
    goParasiteList() {
      if (!this.pet || !this.pet.id) return
      uni.navigateTo({ url: `/pages/health/parasite-list?petId=${this.pet.id}` })
    },
    async onEditTap() {
      const loggedIn = await checkLogin('请先登录后再编辑')
      if (!loggedIn) return
      if (!this.pet) return
      this.editForm = {
        id: this.pet.id,
        name: this.pet.name || '',
        breed: this.pet.breed || '',
        gender: typeof this.pet.gender === 'number' ? this.pet.gender : 0,
        birthday: this.pet.birthday || '',
        weight: this.pet.weight || '',
        color: this.pet.color || '',
        avatar: this.pet.avatar || '',
        category: typeof this.pet.category === 'number' ? this.pet.category : 0,
        sterilized: typeof this.pet.sterilized === 'number' ? this.pet.sterilized : 0
      }
      this.showEditModal = true
    },
    closeEditModal() {
      this.showEditModal = false
    },
    async submitEdit(payload) {
      const loggedIn = await checkLogin('请先登录后再编辑')
      if (!loggedIn) return
      try {
        const res = await uni.$request.put(`/api/pets/${this.pet.id}`, payload)
        if (res && res.success) {
          uni.showToast({ title: '保存成功', icon: 'success' })
          this.showEditModal = false
          this.loadPetDetail(this.pet.id)
        } else {
          uni.showToast({ title: (res && res.message) || '保存失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络错误', icon: 'none' })
      }
    },
    async confirmDelete() {
      const loggedIn = await checkLogin('请先登录后再删除')
      if (!loggedIn) return
      this.showDeleteConfirm = true
    },
    cancelDelete() {
      this.showDeleteConfirm = false
    },
    async doDelete() {
      try {
        const res = await uni.$request.delete(`/api/pets/${this.pet.id}`)
        if (res && res.success) {
          uni.showToast({ title: '删除成功', icon: 'success' })
          this.showDeleteConfirm = false
          setTimeout(() => { uni.navigateBack() }, 1000)
        } else {
          uni.showToast({ title: (res && res.message) || '删除失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络错误', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.pet-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.pd-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.pd-nav-inner {
  display: flex;
  align-items: center;
  height: 46px;
  padding: 0 16px;
  position: relative;
}

.pd-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pd-back-arrow {
  width: 10px;
  height: 10px;
  border-left: 2.5px solid #333;
  border-bottom: 2.5px solid #333;
  transform: rotate(45deg);
}

.pd-nav-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.pd-scroll {
  height: 100vh;
  box-sizing: border-box;
}

.pd-content {
  padding: 12px 16px 40px;
}

.pd-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 12px;
}

.pd-hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  opacity: 0.9;
}

.pd-hero-body {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 20px 24px;
}

.pd-avatar-wrap {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 40px;
  border: 3px solid rgba(255, 255, 255, 0.6);
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.2);
}

.pd-avatar {
  width: 100%;
  height: 100%;
  border-radius: 40px;
}

.pd-avatar-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40px;
  overflow: hidden;
}

.pd-avatar-edit {
  position: absolute;
  right: -4px;
  top: -4px;
  width: 24px;
  height: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
}

.pd-avatar-edit-icon {
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.pd-avatar-letter {
  font-size: 36px;
  color: #fff;
  font-weight: 700;
}

.pd-name {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}

.pd-breed {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.pd-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.pd-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px 12px;
}

.pd-info-item {
  display: flex;
  flex-direction: column;
}

.pd-info-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.pd-info-value {
  font-size: 15px;
  color: #111827;
  font-weight: 600;
}

.pd-danger-zone {
  margin-top: 24px;
  padding: 0 20px;
}

.pd-shortcuts {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
  padding: 0 32rpx;
}
.pd-shortcut-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 8rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.pd-shortcut-icon { font-size: 36rpx; margin-bottom: 8rpx; }
.pd-shortcut-label { font-size: 22rpx; color: #666; }

.pd-delete-btn {
  width: 100%;
  height: 44px;
  border-radius: 999px;
  background: #fff;
  border: 1.5px solid #fca5a5;
  color: #ef4444;
  font-size: 15px;
  font-weight: 600;
  line-height: 44px;
}

.pd-delete-btn::after {
  border: none;
}

.pd-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.pd-loading-text {
  font-size: 14px;
  color: #9ca3af;
}

.pd-confirm-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 20000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.pd-confirm-sheet {
  width: 100%;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
}

.pd-confirm-body {
  padding: 28px 24px 20px;
}

.pd-confirm-title {
  font-size: 17px;
  font-weight: 700;
  color: #111827;
  display: block;
  margin-bottom: 12px;
}

.pd-confirm-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.pd-confirm-actions {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px;
}

.pd-confirm-btn {
  flex: 1;
  height: 44px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 700;
  line-height: 44px;
  border: none;
}

.pd-confirm-btn::after {
  border: none;
}

.pd-confirm-cancel {
  background: #f3f4f6;
  color: #6b7280;
}

.pd-confirm-danger {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}
</style>
