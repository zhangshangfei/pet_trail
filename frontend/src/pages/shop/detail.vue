<template>
  <view class="detail-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack"><view class="nav-back-arrow"></view></view>
        <text class="nav-title">商品详情</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: (statusBarHeight + 46) + 'px' }">
      <view class="page-content" v-if="product">
        <view class="swiper-section">
          <swiper
            class="product-swiper"
            :indicator-dots="imageList.length > 1"
            indicator-color="rgba(255,255,255,0.4)"
            indicator-active-color="#ffffff"
            :autoplay="imageList.length > 1"
            :circular="true"
            @change="onSwiperChange"
          >
            <swiper-item v-for="(img, idx) in imageList" :key="idx">
              <image :src="img" class="swiper-img" mode="aspectFill" @tap="previewImage(idx)" />
            </swiper-item>
          </swiper>
          <view class="swiper-counter" v-if="imageList.length > 1">
            <text class="counter-text">{{ currentSwiperIndex + 1 }}/{{ imageList.length }}</text>
          </view>
        </view>

        <view class="price-card">
          <view class="price-row">
            <text class="price-symbol">¥</text>
            <text class="price-value">{{ formatPrice(product.price) }}</text>
            <text class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
              ¥{{ formatPrice(product.originalPrice) }}
            </text>
            <view class="discount-tag" v-if="product.originalPrice && product.originalPrice > product.price">
              <text class="discount-text">{{ getDiscount() }}折</text>
            </view>
          </view>
          <view class="sales-row">
            <text class="sales-text">{{ product.salesCount || 0 }}人已购</text>
            <view class="rating-badge" v-if="product.rating">
              <text class="rating-text">⭐ {{ product.rating }}</text>
            </view>
          </view>
        </view>

        <view class="info-card">
          <text class="product-name">{{ product.name }}</text>
          <view class="meta-row">
            <view class="meta-tag" v-if="product.brand">
              <text class="meta-tag-text">{{ product.brand }}</text>
            </view>
            <view class="meta-tag" v-if="product.category">
              <text class="meta-tag-text">{{ getCategoryLabel(product.category) }}</text>
            </view>
            <view class="meta-tag pet-type" v-if="product.petType">
              <text class="meta-tag-text">{{ getPetTypeLabel(product.petType) }}</text>
            </view>
          </view>
        </view>

        <view class="detail-card" v-if="product.description">
          <view class="card-header">
            <text class="card-header-icon">📝</text>
            <text class="card-header-text">商品详情</text>
          </view>
          <text class="desc-text">{{ product.description }}</text>
        </view>

        <view class="source-card" v-if="product.sourcePlatform">
          <view class="card-header">
            <text class="card-header-icon">🔗</text>
            <text class="card-header-text">商品来源</text>
          </view>
          <view class="source-info">
            <text class="source-platform">{{ getSourceLabel(product.sourcePlatform) }}</text>
            <text class="source-hint">点击下方按钮前往购买</text>
          </view>
        </view>

        <view class="recommend-section" v-if="recommendProducts.length > 0">
          <view class="card-header">
            <text class="card-header-icon">💡</text>
            <text class="card-header-text">猜你喜欢</text>
          </view>
          <scroll-view scroll-x class="recommend-scroll">
            <view class="recommend-list">
              <view
                v-for="item in recommendProducts"
                :key="item.id"
                class="recommend-item"
                @tap="goToProduct(item)"
              >
                <image v-if="item.coverImage" :src="item.coverImage" class="recommend-img" mode="aspectFill" />
                <view v-else class="recommend-img-placeholder">
                  <text class="recommend-placeholder-icon">🛒</text>
                </view>
                <text class="recommend-name">{{ item.name }}</text>
                <text class="recommend-price">¥{{ formatPrice(item.price) }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view class="bottom-space"></view>
      </view>

      <view class="loading-state" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <view class="empty-state" v-if="!loading && !product">
        <text class="empty-emoji">🛍️</text>
        <text class="empty-text">商品不存在或已下架</text>
      </view>
    </scroll-view>

    <view class="bottom-bar" v-if="product">
      <view class="bar-left">
        <view class="bar-icon-btn" @tap="goShopIndex">
          <text class="bar-icon">🏪</text>
          <text class="bar-icon-label">商城</text>
        </view>
        <view class="bar-icon-btn" @tap="shareProduct">
          <text class="bar-icon">📤</text>
          <text class="bar-icon-label">分享</text>
        </view>
      </view>
      <view class="bar-right">
        <view class="buy-btn" @tap="onBuy">
          <text class="buy-btn-text">立即购买</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as productApi from '@/api/product'

export default {
  data() {
    return {
      statusBarHeight: 20,
      productId: null,
      product: null,
      loading: false,
      currentSwiperIndex: 0,
      recommendProducts: []
    }
  },
  computed: {
    imageList() {
      if (!this.product) return []
      const images = []
      if (this.product.coverImage) {
        images.push(this.product.coverImage)
      }
      if (this.product.images) {
        try {
          const parsed = JSON.parse(this.product.images)
          if (Array.isArray(parsed)) {
            parsed.forEach(img => {
              if (img && !images.includes(img)) images.push(img)
            })
          }
        } catch (e) {
          if (typeof this.product.images === 'string' && this.product.images.startsWith('http') && !images.includes(this.product.images)) {
            images.push(this.product.images)
          }
        }
      }
      return images.length > 0 ? images : []
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
      this.productId = options.id
      this.loadProductDetail()
      this.loadRecommendProducts()
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
    async loadProductDetail() {
      if (!this.productId) return
      this.loading = true
      try {
        const res = await productApi.getProductDetail(this.productId)
        if (res.success) {
          this.product = res.data
        }
      } catch (e) {
        console.error('加载商品详情失败:', e)
      } finally {
        this.loading = false
      }
    },
    async loadRecommendProducts() {
      try {
        const res = await productApi.getRecommendProducts(6)
        if (res.success) {
          this.recommendProducts = (res.data || []).filter(item => String(item.id) !== String(this.productId))
        }
      } catch (e) {
        console.error('加载推荐商品失败:', e)
      }
    },
    onSwiperChange(e) {
      this.currentSwiperIndex = e.detail.current
    },
    previewImage(idx) {
      uni.previewImage({
        current: this.imageList[idx],
        urls: this.imageList
      })
    },
    formatPrice(price) {
      if (!price) return '0.00'
      return Number(price).toFixed(2)
    },
    getDiscount() {
      if (!this.product || !this.product.originalPrice || !this.product.price) return ''
      const discount = (Number(this.product.price) / Number(this.product.originalPrice) * 10).toFixed(1)
      return discount
    },
    getCategoryLabel(category) {
      const map = { food: '粮食', toy: '玩具', health: '保健', supplies: '用品' }
      return map[category] || category
    },
    getPetTypeLabel(petType) {
      const map = { 0: '通用', 1: '猫咪', 2: '狗狗' }
      return map[petType] || '通用'
    },
    getSourceLabel(platform) {
      const map = { taobao: '淘宝', jd: '京东', pdd: '拼多多', tmall: '天猫' }
      return map[platform] || platform
    },
    onBuy() {
      if (this.product && this.product.sourceUrl) {
        uni.setClipboardData({
          data: this.product.sourceUrl,
          success: () => {
            uni.showToast({ title: '链接已复制，请在浏览器中打开', icon: 'none', duration: 2000 })
          }
        })
      } else {
        uni.showToast({ title: '暂无购买链接', icon: 'none' })
      }
    },
    shareProduct() {
      if (this.product) {
        uni.showToast({ title: '分享功能开发中', icon: 'none' })
      }
    },
    goShopIndex() {
      const pages = getCurrentPages()
      const shopPage = pages.find(p => p.route === 'pages/shop/index')
      if (shopPage) {
        uni.navigateBack()
      } else {
        uni.navigateTo({ url: '/pages/shop/index' })
      }
    },
    goToProduct(item) {
      uni.redirectTo({ url: `/pages/shop/detail?id=${item.id}` })
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

.detail-page { min-height: 100vh; background: $bg; }

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
.page-content { padding: 0 0 180rpx; }

.swiper-section { position: relative; width: 100%; height: 600rpx; }

.product-swiper { width: 100%; height: 100%; }

.swiper-img { width: 100%; height: 100%; }

.swiper-counter {
  position: absolute;
  bottom: 20rpx;
  right: 20rpx;
  background: rgba(0, 0, 0, 0.5);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.counter-text { font-size: 22rpx; color: #fff; }

.price-card {
  background: $card-bg;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 12rpx;
}

.price-symbol { font-size: 28rpx; font-weight: 700; color: #ff4d4f; }
.price-value { font-size: 48rpx; font-weight: 700; color: #ff4d4f; margin-right: 12rpx; }

.original-price {
  font-size: 26rpx;
  color: #bbb;
  text-decoration: line-through;
  margin-right: 12rpx;
}

.discount-tag {
  padding: 4rpx 12rpx;
  background: #fff0f0;
  border-radius: 8rpx;
}

.discount-text { font-size: 22rpx; color: #ff4d4f; font-weight: 600; }

.sales-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.sales-text { font-size: 24rpx; color: $text-light; }

.rating-badge {
  padding: 4rpx 12rpx;
  background: #fffbe6;
  border-radius: 12rpx;
}

.rating-text { font-size: 22rpx; color: #faad14; }

.info-card {
  background: $card-bg;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
}

.product-name {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.meta-tag {
  padding: 6rpx 16rpx;
  background: #f3f4f6;
  border-radius: 16rpx;
}

.meta-tag-text { font-size: 22rpx; color: $text-secondary; }

.meta-tag.pet-type { background: #fff7f0; }
.meta-tag.pet-type .meta-tag-text { color: $primary; }

.detail-card {
  background: $card-bg;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
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

.source-card {
  background: $card-bg;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
}

.source-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.source-platform {
  font-size: 28rpx;
  color: $primary;
  font-weight: 600;
}

.source-hint { font-size: 24rpx; color: $text-light; }

.recommend-section {
  background: $card-bg;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
}

.recommend-scroll { white-space: nowrap; margin-top: 16rpx; }

.recommend-list {
  display: inline-flex;
  gap: 16rpx;
}

.recommend-item {
  display: inline-flex;
  flex-direction: column;
  width: 200rpx;
  flex-shrink: 0;
}

.recommend-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  background: #f3f4f6;
}

.recommend-img-placeholder {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recommend-placeholder-icon { font-size: 40rpx; }

.recommend-name {
  font-size: 24rpx;
  color: $text-primary;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-price {
  font-size: 26rpx;
  color: #ff4d4f;
  font-weight: 600;
  margin-top: 4rpx;
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
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 30;
}

.bar-left {
  display: flex;
  gap: 24rpx;
  margin-right: 24rpx;
}

.bar-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 72rpx;
}

.bar-icon { font-size: 36rpx; }
.bar-icon-label { font-size: 20rpx; color: $text-light; margin-top: 4rpx; }

.bar-right { flex: 1; }

.buy-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.3);
}

.buy-btn:active { opacity: 0.85; }

.buy-btn-text { font-size: 30rpx; font-weight: 600; color: #fff; }
</style>
