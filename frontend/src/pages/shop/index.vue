<template>
  <view class="shop-page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="header-title">宠物商城</text>
        <view class="header-right"></view>
      </view>
    </view>

    <scroll-view scroll-y class="shop-scroll" :style="{ top: headerHeight + 'px' }" @scrolltolower="loadMore">
      <view class="shop-content">
        <view class="category-bar">
          <view
            v-for="cat in categories"
            :key="cat.key"
            class="category-item"
            :class="{ active: activeCategory === cat.key }"
            @tap="switchCategory(cat.key)"
          >
            <text class="cat-icon">{{ cat.icon }}</text>
            <text class="cat-text">{{ cat.label }}</text>
          </view>
        </view>

        <view class="product-grid" v-if="products.length > 0">
          <view
            v-for="item in products"
            :key="item.id"
            class="product-card"
            @tap="goDetail(item)"
          >
            <view class="product-img-wrap">
              <image v-if="item.coverImage" :src="item.coverImage" class="product-img" mode="aspectFill" />
              <view v-else class="img-placeholder">
                <text class="placeholder-icon">🛒</text>
              </view>
              <view class="price-tag" v-if="item.originalPrice && item.originalPrice > item.price">
                <text class="discount-text">省¥{{ (item.originalPrice - item.price).toFixed(0) }}</text>
              </view>
            </view>
            <view class="product-info">
              <text class="product-name">{{ item.name }}</text>
              <view class="product-price-row">
                <text class="product-price">¥{{ item.price }}</text>
                <text class="product-original" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
              </view>
              <text class="product-sales">{{ item.salesCount || 0 }}人已购</text>
            </view>
          </view>
        </view>

        <view class="empty-tip" v-if="!loading && products.length === 0">
          <text class="empty-icon">🛍️</text>
          <text class="empty-text">暂无商品</text>
        </view>

        <view class="loading-tip" v-if="loading">
          <text class="loading-text">加载中...</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import * as productApi from '@/api/product'

export default {
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 74,
      products: [],
      loading: false,
      page: 1,
      size: 20,
      hasMore: true,
      activeCategory: '',
      categories: [
        { key: '', icon: '🔥', label: '推荐' },
        { key: 'food', icon: '🍖', label: '粮食' },
        { key: 'toy', icon: '🎾', label: '玩具' },
        { key: 'health', icon: '💊', label: '保健' },
        { key: 'supplies', icon: '🏠', label: '用品' }
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
    this.headerHeight = this.statusBarHeight + 54
  },
  onShow() {
    this.page = 1
    this.products = []
    this.hasMore = true
    this.loadProducts()
  },
  methods: {
    async loadProducts() {
      if (this.loading || !this.hasMore) return
      this.loading = true
      try {
        const res = await productApi.getProducts({
          category: this.activeCategory || undefined,
          page: this.page,
          size: this.size
        })
        if (res.success && res.data) {
          const records = res.data.records || []
          if (this.page === 1) {
            this.products = records
          } else {
            this.products = [...this.products, ...records]
          }
          this.hasMore = records.length >= this.size
        }
      } catch (e) {
        console.error('加载商品失败:', e)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.page++
        this.loadProducts()
      }
    },
    switchCategory(key) {
      this.activeCategory = key
      this.page = 1
      this.products = []
      this.hasMore = true
      this.loadProducts()
    },
    goDetail(item) {
      uni.navigateTo({ url: `/pages/shop/detail?id=${item.id}` })
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.shop-page {
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
.header-right {
  width: 36px;
}
.shop-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}
.shop-content {
  padding: 12px 16px;
}
.category-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  overflow-x: auto;
  white-space: nowrap;
}
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 14px;
  background: #fff;
  border-radius: 12px;
  min-width: 60px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}
.category-item.active {
  background: linear-gradient(135deg, #ff6a3d, #ff8f6b);
}
.category-item.active .cat-text {
  color: #fff;
}
.cat-icon {
  font-size: 20px;
  margin-bottom: 2px;
}
.cat-text {
  font-size: 12px;
  color: #666;
}
.product-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.product-card {
  width: calc(50% - 6px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.product-img-wrap {
  position: relative;
  height: 160px;
}
.product-img {
  width: 100%;
  height: 100%;
}
.img-placeholder {
  width: 100%;
  height: 100%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-icon {
  font-size: 36px;
}
.price-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 68, 68, 0.9);
  padding: 2px 8px;
  border-radius: 10px;
}
.discount-text {
  font-size: 11px;
  color: #fff;
}
.product-info {
  padding: 10px 12px;
}
.product-name {
  font-size: 14px;
  color: #1a1a1a;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}
.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 4px;
}
.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
}
.product-original {
  font-size: 12px;
  color: #bbb;
  text-decoration: line-through;
}
.product-sales {
  font-size: 12px;
  color: #999;
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
</style>
