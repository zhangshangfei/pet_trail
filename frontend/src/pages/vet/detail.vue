<template>
  <view class="detail-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">医院详情</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content" v-if="clinic">
        <view class="cover-section">
          <image v-if="clinic.coverImage" :src="clinic.coverImage" class="cover-img" mode="aspectFill" />
          <view v-else class="cover-placeholder">
            <text class="cover-placeholder-icon">🏥</text>
          </view>
          <view class="partner-badge" v-if="clinic.isPartner">
            <text class="partner-text">合作医院</text>
          </view>
        </view>

        <view class="info-card">
          <view class="name-row">
            <text class="clinic-name">{{ clinic.name }}</text>
            <view class="rating-badge">
              <text class="rating-text">⭐ {{ clinic.rating || '5.0' }}</text>
            </view>
          </view>
          <view class="specialties-row" v-if="clinic.specialties">
            <text
              v-for="(spec, idx) in parseSpecialties(clinic.specialties)"
              :key="idx"
              class="spec-tag"
            >{{ spec }}</text>
          </view>
        </view>

        <view class="detail-card">
          <view class="detail-item">
            <view class="detail-icon-wrap">
              <text class="detail-emoji">📍</text>
            </view>
            <view class="detail-content">
              <text class="detail-label">地址</text>
              <text class="detail-value">{{ clinic.address || '暂无地址信息' }}</text>
            </view>
          </view>
          <view class="detail-item">
            <view class="detail-icon-wrap">
              <text class="detail-emoji">📞</text>
            </view>
            <view class="detail-content">
              <text class="detail-label">电话</text>
              <text class="detail-value phone-value" @tap="callPhone">{{ clinic.phone || '暂无电话' }}</text>
            </view>
          </view>
          <view class="detail-item">
            <view class="detail-icon-wrap">
              <text class="detail-emoji">🕐</text>
            </view>
            <view class="detail-content">
              <text class="detail-label">营业时间</text>
              <text class="detail-value">{{ clinic.businessHours || '24小时' }}</text>
            </view>
          </view>
          <view class="detail-item">
            <view class="detail-icon-wrap">
              <text class="detail-emoji">📋</text>
            </view>
            <view class="detail-content">
              <text class="detail-label">状态</text>
              <text class="detail-value" :class="clinic.status === 1 ? 'status-open' : 'status-closed'">
                {{ clinic.status === 1 ? '营业中' : '已关闭' }}
              </text>
            </view>
          </view>
        </view>

        <view class="desc-card" v-if="clinic.description">
          <view class="card-header">
            <text class="card-header-icon">📝</text>
            <text class="card-header-text">医院简介</text>
          </view>
          <text class="desc-text">{{ clinic.description }}</text>
        </view>

        <view class="bottom-space"></view>
      </view>

      <view class="loading-state" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <view class="empty-state" v-if="!loading && !clinic">
        <text class="empty-emoji">🏥</text>
        <text class="empty-text">医院信息不存在</text>
      </view>
    </scroll-view>

    <view class="bottom-bar" v-if="clinic && clinic.status === 1">
      <view class="book-btn" @tap="goBook">
        <text class="book-btn-text">立即预约</text>
      </view>
    </view>
  </view>
</template>

<script>
import * as vetApi from '@/api/vet'

export default {
  data() {
    return {
      statusBarHeight: 20,
      clinicId: null,
      clinic: null,
      loading: false
    }
  },
  onLoad(options) {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    if (options.id) {
      this.clinicId = options.id
      this.loadClinicDetail()
    }
  },
  methods: {
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.switchTab({ url: '/pages/home/index' })
      }
    },
    async loadClinicDetail() {
      if (!this.clinicId) return
      this.loading = true
      try {
        const res = await vetApi.getClinicDetail(this.clinicId)
        if (res.success) {
          this.clinic = res.data
        }
      } catch (e) {
        console.error('加载医院详情失败:', e)
      } finally {
        this.loading = false
      }
    },
    parseSpecialties(str) {
      if (!str) return []
      return str.split(',')
    },
    callPhone() {
      if (this.clinic && this.clinic.phone) {
        uni.makePhoneCall({ phoneNumber: this.clinic.phone })
      }
    },
    goBook() {
      uni.navigateBack({
        success: () => {
          const pages = getCurrentPages()
          const vetPage = pages.find(p => p.route === 'pages/vet/index')
          if (vetPage) {
            vetPage.$vm.onBook(this.clinic)
          }
        },
        fail: () => {
          uni.navigateTo({ url: '/pages/vet/index' })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$primary-light: #ff8f6b;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.detail-page {
  min-height: 100vh;
  background: $bg;
}

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.status-bar { width: 100%; }

.nav-bar {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back:active { background: rgba(255, 255, 255, 0.35); }

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title { font-size: 34rpx; font-weight: 700; color: #fff; }
.nav-placeholder { width: 64rpx; }

.page-scroll { height: 100vh; box-sizing: border-box; }
.page-content { padding: 20rpx 24rpx 180rpx; }

.cover-section {
  position: relative;
  width: 100%;
  height: 360rpx;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder-icon { font-size: 80rpx; }

.partner-badge {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  background: rgba(255, 106, 61, 0.9);
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
}

.partner-text { font-size: 24rpx; color: #fff; font-weight: 500; }

.info-card {
  background: $card-bg;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.clinic-name {
  font-size: 36rpx;
  font-weight: 700;
  color: $text-primary;
  flex: 1;
}

.rating-badge {
  padding: 6rpx 16rpx;
  background: #fffbe6;
  border-radius: 20rpx;
}

.rating-text { font-size: 26rpx; color: #faad14; font-weight: 500; }

.specialties-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.spec-tag {
  font-size: 24rpx;
  color: $primary;
  background: #fff7f0;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.detail-card {
  background: $card-bg;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.detail-item {
  display: flex;
  align-items: flex-start;
  padding: 16rpx 0;
}

.detail-item + .detail-item {
  border-top: 1rpx solid #f3f4f6;
}

.detail-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: rgba(255, 122, 61, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.detail-emoji { font-size: 28rpx; }

.detail-content {
  flex: 1;
  min-width: 0;
}

.detail-label {
  font-size: 24rpx;
  color: $text-light;
  display: block;
  margin-bottom: 4rpx;
}

.detail-value {
  font-size: 28rpx;
  color: $text-primary;
  display: block;
}

.phone-value { color: $primary; }

.status-open { color: #10b981; font-weight: 600; }
.status-closed { color: #ef4444; font-weight: 600; }

.desc-card {
  background: $card-bg;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.card-header-icon { font-size: 28rpx; margin-right: 8rpx; }
.card-header-text { font-size: 28rpx; font-weight: 700; color: $text-primary; }

.desc-text {
  font-size: 28rpx;
  color: $text-secondary;
  line-height: 1.8;
}

.bottom-space { height: 40rpx; }

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;
}

.loading-text { font-size: 28rpx; color: $text-light; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;
}

.empty-emoji { font-size: 80rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 30rpx; color: $text-light; }

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 30;
}

.book-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, $primary, $primary-light);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(255, 106, 61, 0.3);
}

.book-btn:active { opacity: 0.85; }

.book-btn-text { font-size: 32rpx; font-weight: 600; color: #fff; }
</style>
