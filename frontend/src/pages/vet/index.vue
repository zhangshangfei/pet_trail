<template>
  <view class="vet-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="header-title">宠物医院</text>
        <view class="header-right" @tap="goMyAppointments">
          <text class="my-btn">我的预约</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="vet-scroll" :style="{ top: headerHeight + 'px' }" @scrolltolower="loadMore">
      <view class="vet-content">
        <view class="clinic-list" v-if="clinics.length > 0">
          <view
            v-for="item in clinics"
            :key="item.id"
            class="clinic-card"
            @tap="goClinicDetail(item)"
          >
            <view class="clinic-img-wrap">
              <image v-if="item.coverImage" :src="item.coverImage" class="clinic-img" mode="aspectFill" />
              <view v-else class="img-placeholder">
                <text class="placeholder-icon">🏥</text>
              </view>
              <view class="partner-badge" v-if="item.isPartner">
                <text class="partner-text">合作医院</text>
              </view>
            </view>
            <view class="clinic-body">
              <view class="clinic-name-row">
                <text class="clinic-name">{{ item.name }}</text>
                <view class="rating-badge">
                  <text class="rating-text">⭐ {{ item.rating || '5.0' }}</text>
                </view>
              </view>
              <text class="clinic-address">{{ item.address || '暂无地址信息' }}</text>
              <view class="clinic-specialties" v-if="item.specialties">
                <text
                  v-for="(spec, idx) in parseSpecialties(item.specialties)"
                  :key="idx"
                  class="spec-tag"
                >{{ spec }}</text>
              </view>
              <view class="clinic-footer">
                <text class="hours-text">{{ item.businessHours || '24小时' }}</text>
                <view class="book-btn" @tap.stop="onBook(item)">
                  <text class="book-btn-text">预约</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="empty-tip" v-if="!loading && clinics.length === 0">
          <text class="empty-icon">🏥</text>
          <text class="empty-text">暂无医院信息</text>
        </view>

        <view class="loading-tip" v-if="loading">
          <text class="loading-text">加载中...</text>
        </view>
      </view>
    </scroll-view>

    <view class="book-popup" v-if="showBookPopup" @tap="closeBookPopup">
      <view class="popup-content" @tap.stop>
        <text class="popup-title">预约挂号</text>
        <text class="popup-clinic">{{ bookingClinic.name }}</text>

        <view class="form-group">
          <text class="form-label">选择宠物</text>
          <view class="pet-selector" v-if="myPets.length > 0">
            <view
              v-for="pet in myPets"
              :key="pet.id"
              class="pet-item"
              :class="{ active: selectedPetId === pet.id }"
              @tap="selectedPetId = pet.id"
            >
              <image v-if="pet.avatar" :src="pet.avatar" class="pet-avatar" mode="aspectFill" />
              <view v-else class="pet-avatar-placeholder">🐾</view>
              <text class="pet-name">{{ pet.name }}</text>
            </view>
          </view>
          <view v-else class="no-pet-tip">
            <text class="no-pet-text">暂无宠物，请先添加宠物</text>
          </view>
        </view>

        <view class="form-group">
          <text class="form-label">选择日期</text>
          <picker mode="date" :value="appointmentDate" :start="todayStr" @change="onDateChange">
            <view class="picker-value">{{ appointmentDate || '请选择日期' }}</view>
          </picker>
        </view>

        <view class="form-group">
          <text class="form-label">选择时段</text>
          <view class="time-slots">
            <view
              v-for="slot in timeSlots"
              :key="slot"
              class="time-slot"
              :class="{ active: appointmentTime === slot, disabled: isSlotPast(slot) }"
              @tap="onSelectSlot(slot)"
            >
              <text class="slot-text">{{ slot }}</text>
            </view>
          </view>
        </view>

        <view class="form-group">
          <text class="form-label">症状描述</text>
          <textarea
            v-model="symptom"
            class="symptom-input"
            placeholder="请描述宠物症状（选填）"
            maxlength="200"
          />
        </view>

        <view class="popup-btns">
          <view class="popup-cancel" @tap="closeBookPopup">
            <text class="cancel-text">取消</text>
          </view>
          <view class="popup-confirm" @tap="submitBooking">
            <text class="confirm-text">确认预约</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as vetApi from '@/api/vet'
import { getPetList } from '@/api/pet'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 74,
      clinics: [],
      loading: false,
      page: 1,
      size: 20,
      hasMore: true,
      showBookPopup: false,
      bookingClinic: {},
      myPets: [],
      selectedPetId: null,
      appointmentDate: '',
      appointmentTime: '',
      symptom: '',
      timeSlots: ['09:00-10:00', '10:00-11:00', '11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']
    }
  },
  computed: {
    todayStr() {
      const d = new Date()
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    this.headerHeight = this.statusBarHeight + 54
  },
  onShow() {
    this.page = 1
    this.clinics = []
    this.hasMore = true
    this.loadClinics()
  },
  methods: {
    async loadClinics() {
      if (this.loading || !this.hasMore) return
      this.loading = true
      try {
        const res = await vetApi.getClinics({ page: this.page, size: this.size })
        if (res.success) {
          const list = res.data || []
          if (this.page === 1) {
            this.clinics = list
          } else {
            this.clinics = [...this.clinics, ...list]
          }
          this.hasMore = list.length >= this.size
        }
      } catch (e) {
        console.error('加载医院列表失败:', e)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.page++
        this.loadClinics()
      }
    },
    onBook(clinic) {
      this.bookingClinic = clinic
      this.selectedPetId = null
      this.appointmentDate = ''
      this.appointmentTime = ''
      this.symptom = ''
      this.showBookPopup = true
      this.loadMyPets()
    },
    onDateChange(e) {
      this.appointmentDate = e.detail.value
      this.appointmentTime = ''
    },
    isSlotPast(slot) {
      if (!this.appointmentDate) return false
      const today = this.todayStr
      if (this.appointmentDate > today) return false
      if (this.appointmentDate < today) return true
      const now = new Date()
      const slotEnd = slot.split('-')[1] || slot
      const [h, m] = slotEnd.split(':').map(Number)
      return now.getHours() > h || (now.getHours() === h && now.getMinutes() >= m)
    },
    onSelectSlot(slot) {
      if (this.isSlotPast(slot)) {
        uni.showToast({ title: '该时段已过', icon: 'none' })
        return
      }
      this.appointmentTime = slot
    },
    async submitBooking() {
      if (!this.selectedPetId) {
        uni.showToast({ title: '请选择宠物', icon: 'none' })
        return
      }
      if (!this.appointmentDate) {
        uni.showToast({ title: '请选择日期', icon: 'none' })
        return
      }
      if (!this.appointmentTime) {
        uni.showToast({ title: '请选择时段', icon: 'none' })
        return
      }
      try {
        const res = await vetApi.createAppointment({
          clinicId: this.bookingClinic.id,
          petId: this.selectedPetId,
          appointmentDate: this.appointmentDate,
          appointmentTime: this.appointmentTime,
          symptom: this.symptom || undefined
        })
        if (res.success) {
          uni.showToast({ title: '预约成功！', icon: 'success' })
          this.showBookPopup = false
        }
      } catch (e) {
        uni.showToast({ title: e.message || '预约失败', icon: 'none' })
      }
    },
    closeBookPopup() {
      this.showBookPopup = false
    },
    async loadMyPets() {
      try {
        const res = await getPetList()
        if (res.success) {
          this.myPets = res.data || []
          if (this.myPets.length > 0 && !this.selectedPetId) {
            this.selectedPetId = this.myPets[0].id
          }
        }
      } catch (e) {
        console.error('加载宠物列表失败:', e)
      }
    },
    goClinicDetail(item) {
      uni.navigateTo({ url: `/pages/vet/detail?id=${item.id}` })
    },
    goMyAppointments() {
      uni.navigateTo({ url: '/pages/vet/appointments' })
    },
    goBack() {
      uni.navigateBack()
    },
    parseSpecialties(str) {
      if (!str) return []
      return str.split(',').slice(0, 3)
    }
  }
}
</script>

<style scoped>
.vet-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #ff6a3d 0%, #ff8f6b 15%, #f5f5f5 30%);
}
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 54px;
  padding: 0 16px;
}
.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.back-icon {
  font-size: 28px;
  color: #fff;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}
.my-btn {
  font-size: 14px;
  color: #fff;
}
.vet-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}
.vet-content {
  padding: 12px 16px;
}
.clinic-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.clinic-img-wrap {
  position: relative;
  height: 140px;
}
.clinic-img {
  width: 100%;
  height: 100%;
}
.img-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-icon {
  font-size: 48px;
}
.partner-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(255, 106, 61, 0.9);
  padding: 3px 10px;
  border-radius: 10px;
}
.partner-text {
  font-size: 12px;
  color: #fff;
}
.clinic-body {
  padding: 14px 16px;
}
.clinic-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.clinic-name {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}
.rating-badge {
  padding: 2px 8px;
  background: #fffbe6;
  border-radius: 10px;
}
.rating-text {
  font-size: 13px;
  color: #faad14;
}
.clinic-address {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}
.clinic-specialties {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.spec-tag {
  font-size: 12px;
  color: #ff6a3d;
  background: #fff7f0;
  padding: 2px 8px;
  border-radius: 10px;
}
.clinic-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hours-text {
  font-size: 13px;
  color: #999;
}
.book-btn {
  padding: 6px 20px;
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
  border-radius: 20px;
}
.book-btn-text {
  font-size: 14px;
  color: #fff;
}
.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.empty-text {
  font-size: 16px;
  color: #999;
}
.loading-tip {
  text-align: center;
  padding: 20px 0;
}
.loading-text {
  font-size: 14px;
  color: #999;
}
.book-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}
.popup-content {
  width: 100%;
  background: #fff;
  border-radius: 20px 20px 0 0;
  padding: 24px 20px;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));
}
.popup-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}
.popup-clinic {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}
.form-group {
  margin-bottom: 16px;
}
.form-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  display: block;
}
.picker-value {
  padding: 10px 14px;
  background: #f5f5f5;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
}
.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.time-slot {
  padding: 8px 14px;
  background: #f5f5f5;
  border-radius: 10px;
}
.time-slot.active {
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
}
.time-slot.active .slot-text {
  color: #fff;
}
.time-slot.disabled {
  background: #f0f0f0;
  opacity: 0.5;
}
.time-slot.disabled .slot-text {
  color: #bbb;
}
.slot-text {
  font-size: 13px;
  color: #333;
}
.symptom-input {
  width: 100%;
  height: 80px;
  padding: 10px 14px;
  background: #f5f5f5;
  border-radius: 10px;
  font-size: 14px;
  box-sizing: border-box;
}
.popup-btns {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}
.popup-cancel {
  flex: 1;
  padding: 12px 0;
  text-align: center;
  background: #f5f5f5;
  border-radius: 12px;
}
.cancel-text {
  font-size: 16px;
  color: #666;
}
.popup-confirm {
  flex: 2;
  padding: 12px 0;
  text-align: center;
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
  border-radius: 12px;
}
.confirm-text {
  font-size: 16px;
  color: #fff;
  font-weight: 500;
}
.pet-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.pet-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 12px;
  min-width: 70px;
}
.pet-item.active {
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
}
.pet-item.active .pet-name {
  color: #fff;
}
.pet-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-bottom: 4px;
}
.pet-avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
  font-size: 18px;
}
.pet-name {
  font-size: 12px;
  color: #333;
}
.no-pet-tip {
  padding: 10px 0;
}
.no-pet-text {
  font-size: 14px;
  color: #999;
}
</style>
