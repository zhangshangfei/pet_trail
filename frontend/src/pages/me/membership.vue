<template>
  <view class="membership-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">宠迹Pro</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content">

        <view class="hero-card">
          <view class="hero-bg"></view>
          <view class="hero-body">
            <text class="hero-badge">{{ isPro ? '✨ Pro会员' : '🐾 免费版' }}</text>
            <text v-if="isPro && expireDate" class="hero-expire">有效期至 {{ expireDate }}</text>
            <text v-if="!isPro" class="hero-desc">升级Pro，解锁全部高级功能</text>
          </view>
        </view>

        <view class="features-section">
          <text class="section-title">Pro专属权益</text>
          <view class="features-grid">
            <view v-for="feature in features" :key="feature.title" class="feature-card">
              <text class="feature-icon">{{ feature.icon }}</text>
              <text class="feature-title">{{ feature.title }}</text>
              <text class="feature-desc">{{ feature.desc }}</text>
            </view>
          </view>
        </view>

        <view v-if="!isPro" class="plans-section">
          <text class="section-title">选择方案</text>
          <view class="plans">
            <view class="plan-card" :class="{ active: selectedPlan === 'monthly' }" @tap="selectedPlan = 'monthly'">
              <text class="plan-name">月度会员</text>
              <text class="plan-price">¥9.9</text>
              <text class="plan-unit">/月</text>
              <view v-if="selectedPlan === 'monthly'" class="plan-check">✓</view>
            </view>
            <view class="plan-card plan-card--recommend" :class="{ active: selectedPlan === 'yearly' }" @tap="selectedPlan = 'yearly'">
              <view class="plan-badge">推荐</view>
              <text class="plan-name">年度会员</text>
              <text class="plan-price">¥99</text>
              <text class="plan-unit">/年</text>
              <text class="plan-save">省¥19.8</text>
              <view v-if="selectedPlan === 'yearly'" class="plan-check">✓</view>
            </view>
          </view>
        </view>

        <view v-if="isPro" class="pro-status">
          <text class="pro-status-text">您已是Pro会员，享受全部权益</text>
          <view class="cancel-btn" @tap="onCancelSubscription">
            <text class="cancel-btn-text">取消自动续费</text>
          </view>
        </view>

        <view class="compare-section">
          <text class="section-title">功能对比</text>
          <view class="compare-table">
            <view class="compare-row compare-header">
              <text class="compare-col">功能</text>
              <text class="compare-col">免费</text>
              <text class="compare-col compare-pro">Pro</text>
            </view>
            <view class="compare-row" v-for="row in compareData" :key="row.feature">
              <text class="compare-col">{{ row.feature }}</text>
              <text class="compare-col">{{ row.free }}</text>
              <text class="compare-col compare-pro">{{ row.pro }}</text>
            </view>
          </view>
        </view>

      </view>
    </scroll-view>

    <view v-if="!isPro" class="bottom-bar">
      <view class="bottom-bar-inner" :style="{ paddingBottom: 'max(16rpx, env(safe-area-inset-bottom))' }">
        <view class="subscribe-btn" @tap="onSubscribe">
          <text class="subscribe-btn-text">立即开通 {{ selectedPlan === 'yearly' ? '¥99/年' : '¥9.9/月' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 20,
      isPro: false,
      expireDate: '',
      selectedPlan: 'yearly',
      features: [],
      compareData: [
        { feature: '成长相册', free: '20张', pro: '无限' },
        { feature: '体重趋势', free: '7天', pro: '90天' },
        { feature: '健康报告', free: '基础', pro: '详细' },
        { feature: '数据导出', free: '✕', pro: '✓' },
        { feature: '主题皮肤', free: '✕', pro: '✓' },
        { feature: '医院预约', free: '✕', pro: '优先' }
      ]
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
    } catch (e) {
      this.statusBarHeight = 20
    }
    this.loadMembershipInfo()
  },
  methods: {
    goBack() { uni.navigateBack({ delta: 1 }) },

    async loadMembershipInfo() {
      try {
        const res = await membershipApi.getMembershipInfo()
        if (res && res.success && res.data) {
          this.isPro = res.data.isPro || false
          if (res.data.expireDate) {
            const d = new Date(res.data.expireDate)
            this.expireDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
          }
          if (res.data.features) {
            this.features = res.data.features
          }
        }
      } catch (e) {
        console.error('加载会员信息失败:', e)
      }
    },

    onSubscribe() {
      const self = this
      uni.showModal({
        title: '确认开通',
        content: `确定开通宠迹Pro${self.selectedPlan === 'yearly' ? '年度' : '月度'}会员吗？`,
        confirmColor: '#ff6a3d',
        async success(res) {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: '创建订单...' })
            const orderRes = await membershipApi.createMembershipOrder( {
              plan: self.selectedPlan
            })
            if (!orderRes || !orderRes.success || !orderRes.data) {
              uni.hideLoading()
              uni.showToast({ title: (orderRes && orderRes.message) || '创建订单失败', icon: 'none' })
              return
            }

            uni.showLoading({ title: '支付中...' })
            const payRes = await membershipApi.payMembershipOrder(orderRes.data.id)
            uni.hideLoading()

            if (payRes && payRes.success) {
              uni.showToast({ title: '开通成功！', icon: 'success' })
              self.isPro = true
              self.loadMembershipInfo()
            } else {
              uni.showToast({ title: (payRes && payRes.message) || '支付失败', icon: 'none' })
            }
          } catch (e) {
            uni.hideLoading()
            console.error('订阅失败:', e)
            uni.showToast({ title: '网络错误', icon: 'none' })
          }
        }
      })
    },

    onCancelSubscription() {
      const self = this
      uni.showModal({
        title: '取消续费',
        content: '取消后将不再自动续费，当前权益到期后失效',
        confirmColor: '#ff4d4f',
        async success(res) {
          if (!res.confirm) return
          try {
            const result = await membershipApi.cancelMembership()
            if (result && result.success) {
              uni.showToast({ title: '已取消续费', icon: 'none' })
            }
          } catch (e) {
            uni.showToast({ title: '操作失败', icon: 'none' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
$primary: #ff6a3d;
$gold: #f5a623;
$bg: #f5f5f5;
$card-bg: #ffffff;
$text-primary: #1a1a1a;
$text-secondary: #666666;
$text-light: #999999;

.membership-page { min-height: 100vh; background: $bg; }
.nav-fixed { position: fixed; top: 0; left: 0; right: 0; z-index: 30; background: #fff; }
.status-bar { width: 100%; }
.nav-bar { height: 92rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; border-bottom: 1rpx solid #f0f0f0; }
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-back-arrow { width: 20rpx; height: 20rpx; border-left: 4rpx solid $text-primary; border-bottom: 4rpx solid $text-primary; transform: rotate(45deg); }
.nav-title { font-size: 32rpx; font-weight: 700; color: $text-primary; }
.nav-placeholder { width: 60rpx; }
.page-scroll { height: 100vh; }
.page-content { padding: 24rpx; }

.hero-card {
  position: relative; border-radius: 24rpx; overflow: hidden;
  padding: 48rpx 32rpx; margin-bottom: 24rpx;
}
.hero-bg {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, #ff6a3d 0%, #f5a623 100%);
}
.hero-body { position: relative; z-index: 1; }
.hero-badge {
  display: inline-block; font-size: 36rpx; font-weight: 700; color: #fff;
  background: rgba(255,255,255,0.2); padding: 8rpx 24rpx; border-radius: 20rpx;
  margin-bottom: 12rpx;
}
.hero-expire { display: block; font-size: 26rpx; color: rgba(255,255,255,0.9); margin-top: 8rpx; }
.hero-desc { display: block; font-size: 28rpx; color: rgba(255,255,255,0.9); margin-top: 8rpx; }

.section-title { display: block; font-size: 30rpx; font-weight: 700; color: $text-primary; margin-bottom: 20rpx; }

.features-section { margin-bottom: 32rpx; }
.features-grid { display: flex; flex-wrap: wrap; justify-content: space-between; }
.feature-card {
  width: 48%; background: $card-bg; border-radius: 16rpx;
  padding: 20rpx 16rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  margin-bottom: 16rpx; box-sizing: border-box;
}
.feature-icon { display: block; font-size: 36rpx; margin-bottom: 6rpx; }
.feature-title { display: block; font-size: 24rpx; font-weight: 600; color: $text-primary; margin-bottom: 2rpx; }
.feature-desc { display: block; font-size: 20rpx; color: $text-light; }

.plans-section { margin-bottom: 32rpx; }
.plans { display: flex; gap: 16rpx; }
.plan-card {
  flex: 1; background: $card-bg; border-radius: 20rpx; padding: 32rpx 24rpx;
  text-align: center; border: 3rpx solid transparent; position: relative;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); transition: all 0.2s;
}
.plan-card.active { border-color: $primary; }
.plan-card--recommend { background: linear-gradient(180deg, #fff8f0, #fff); }
.plan-badge {
  position: absolute; top: -12rpx; right: 20rpx;
  background: $primary; color: #fff; font-size: 20rpx; font-weight: 600;
  padding: 4rpx 16rpx; border-radius: 12rpx;
}
.plan-name { display: block; font-size: 28rpx; font-weight: 600; color: $text-primary; margin-bottom: 12rpx; }
.plan-price { font-size: 48rpx; font-weight: 700; color: $primary; }
.plan-unit { font-size: 24rpx; color: $text-light; }
.plan-save { display: block; font-size: 22rpx; color: #52c41a; margin-top: 8rpx; }
.plan-check {
  position: absolute; bottom: 12rpx; right: 12rpx;
  width: 36rpx; height: 36rpx; border-radius: 50%;
  background: $primary; color: #fff; font-size: 22rpx;
  display: flex; align-items: center; justify-content: center;
}

.pro-status {
  background: $card-bg; border-radius: 20rpx; padding: 32rpx;
  text-align: center; margin-bottom: 32rpx;
}
.pro-status-text { display: block; font-size: 28rpx; color: $primary; font-weight: 600; margin-bottom: 16rpx; }
.cancel-btn { display: inline-block; padding: 12rpx 32rpx; border-radius: 24rpx; background: #f5f5f5; }
.cancel-btn-text { font-size: 24rpx; color: $text-light; }

.compare-section { margin-bottom: 160rpx; }
.compare-table { background: $card-bg; border-radius: 20rpx; overflow: hidden; }
.compare-row {
  display: flex; padding: 20rpx 24rpx; border-bottom: 1rpx solid #f5f5f5;
}
.compare-row:last-child { border-bottom: none; }
.compare-header { background: #fafafa; }
.compare-col { flex: 1; font-size: 24rpx; color: $text-secondary; text-align: center; }
.compare-header .compare-col { font-weight: 600; color: $text-primary; }
.compare-pro { color: $primary; font-weight: 600; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 30; background: #fff; border-top: 1rpx solid #f0f0f0; }
.bottom-bar-inner { padding: 16rpx 32rpx; }
.subscribe-btn {
  height: 96rpx; border-radius: 48rpx; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #ff6a3d, #f5a623);
  box-shadow: 0 8rpx 24rpx rgba(255,106,61,0.3);
}
.subscribe-btn:active { opacity: 0.9; }
.subscribe-btn-text { font-size: 30rpx; font-weight: 600; color: #fff; }
</style>
