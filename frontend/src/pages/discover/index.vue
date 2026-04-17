<template>
  <view class="discover-page">
    <view class="nav-fixed">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <view class="nav-back-arrow"></view>
        </view>
        <text class="nav-title">发现用户</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <view class="search-bar" :style="{ top: navHeight + 'px' }">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索用户昵称"
          placeholder-class="search-placeholder"
          confirm-type="search"
          @confirm="onSearch"
          @input="onInputChange"
        />
        <text v-if="keyword" class="search-clear" @tap="clearSearch">✕</text>
      </view>
    </view>

    <view class="tab-bar" :style="{ top: navHeight + searchHeight + 'px' }">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'recommend' }"
        @tap="switchTab('recommend')"
      >
        <text class="tab-text">推荐</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'hot' }"
        @tap="switchTab('hot')"
      >
        <text class="tab-text">热门</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'new' }"
        @tap="switchTab('new')"
      >
        <text class="tab-text">新人</text>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="user-scroll"
      :style="{ paddingTop: navHeight + searchHeight + tabHeight + 'px' }"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && userList.length === 0" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="userList.length === 0" class="empty-state">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">{{ keyword ? '未找到相关用户' : '暂无推荐用户' }}</text>
      </view>

      <view v-else class="user-list">
        <view
          v-for="user in userList"
          :key="user.id"
          class="user-card"
          @tap="goUserDetail(user)"
        >
          <image class="user-avatar" :src="getUserAvatar(user.id, user.avatar)" mode="aspectFill" />
          <view class="user-info">
            <view class="user-name-row">
              <text class="user-nickname">{{ user.nickname || '萌宠主人' }}</text>
              <text v-if="user.gender === 1" class="gender-icon male">♂</text>
              <text v-else-if="user.gender === 2" class="gender-icon female">♀</text>
            </view>
            <view class="user-stats-row">
              <text class="user-stat">{{ user.followerCount || 0 }} 粉丝</text>
              <text class="stat-dot">·</text>
              <text class="user-stat">{{ user.postCount || 0 }} 动态</text>
            </view>
            <view v-if="user.recommendReason && currentTab === 'recommend'" class="user-reason-row">
              <text class="reason-tag" :class="'reason-tag--' + user.recommendReason">{{ reasonText(user.recommendReason) }}</text>
            </view>
          </view>
          <view
            class="follow-btn"
            :class="{ 'follow-btn--following': user.isFollowing }"
            @tap.stop="onToggleFollow(user)"
          >
            <text class="follow-btn-text">{{ user.isFollowing ? '已关注' : '+ 关注' }}</text>
          </view>
        </view>
      </view>

      <view v-if="loading && userList.length > 0" class="loading-more">
        <text class="loading-more-text">加载中...</text>
      </view>
      <view v-if="!hasMore && userList.length > 0" class="no-more">
        <text class="no-more-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { discoverUsers } from '@/api/auth'
import { toggleFollow } from '@/api/post'
import { checkLogin, getUserAvatar, DEFAULT_USER_AVATAR } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navHeight: 0,
      searchHeight: 56,
      tabHeight: 44,
      currentTab: 'recommend',
      keyword: '',
      userList: [],
      page: 1,
      size: 20,
      loading: false,
      hasMore: true,
      followLoadingMap: {}
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync()
    this.statusBarHeight = sysInfo.statusBarHeight || 0
    this.navHeight = this.statusBarHeight + 46
    this.loadUsers()
  },
  onPullDownRefresh() {
    this.page = 1
    this.userList = []
    this.hasMore = true
    this.loadUsers().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    getUserAvatar,
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    switchTab(tab) {
      if (this.currentTab === tab) return
      this.currentTab = tab
      this.page = 1
      this.userList = []
      this.hasMore = true
      this.loadUsers()
    },
    onSearch() {
      this.page = 1
      this.userList = []
      this.hasMore = true
      this.loadUsers()
    },
    onInputChange(e) {
      this.keyword = e.detail.value
    },
    clearSearch() {
      this.keyword = ''
      this.page = 1
      this.userList = []
      this.hasMore = true
      this.loadUsers()
    },
    async loadUsers() {
      if (this.loading) return
      this.loading = true
      try {
        const params = {
          type: this.currentTab,
          page: this.page,
          size: this.size
        }
        if (this.keyword.trim()) {
          params.keyword = this.keyword.trim()
        }
        const res = await discoverUsers(params)
        if (res && res.success) {
          const list = res.data || []
          if (this.page === 1) {
            this.userList = list
          } else {
            this.userList = [...this.userList, ...list]
          }
          this.hasMore = list.length >= this.size
        }
      } catch (e) {
        console.error('加载用户失败:', e)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadUsers()
    },
    async onToggleFollow(user) {
      const loggedIn = await checkLogin('请先登录后再关注')
      if (!loggedIn) return

      if (this.followLoadingMap[user.id]) return
      this.followLoadingMap[user.id] = true

      try {
        const res = await toggleFollow(user.id)
        if (res && res.success) {
          user.isFollowing = res.data.following
          user.followerCount = res.data.followerCount || user.followerCount
          uni.showToast({
            title: user.isFollowing ? '关注成功' : '已取消关注',
            icon: 'none'
          })
        }
      } catch (e) {
        console.error('关注操作失败:', e)
      } finally {
        this.followLoadingMap[user.id] = false
      }
    },
    goUserDetail(user) {
      uni.navigateTo({ url: `/pages/user/detail?id=${user.id}` })
    },
    reasonText(reason) {
      const map = {
        same_breed: '同品种',
        same_category: '同类型',
        social: '好友关注',
        interact: '互动过',
        activity: '活跃用户',
        hot: '热门用户',
        new_user: '新用户'
      }
      return map[reason] || '推荐'
    }
  }
}
</script>

<style lang="scss" scoped>
.discover-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 30;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.status-bar {
  width: 100%;
}

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
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: background 0.2s, transform 0.15s;
}

.nav-back:active {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(0.92);
}

.nav-back-arrow {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg) translate(2rpx, -2rpx);
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.nav-placeholder {
  width: 64rpx;
}

.search-bar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 29;
  background: linear-gradient(180deg, #ff4d4f 0%, #ff4d4f 100%);
  padding: 0 24rpx 16rpx;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 36rpx;
  padding: 0 24rpx;
  height: 68rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #fff;
  height: 68rpx;
}

.search-placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.search-clear {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.6);
  padding: 8rpx;
}

.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 28;
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  position: relative;
}

.tab-item.active .tab-text {
  color: #ff6a3d;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 6rpx;
  background: #ff6a3d;
  border-radius: 3rpx;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
}

.user-scroll {
  min-height: 100vh;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.user-list {
  padding: 16rpx 24rpx;
}

.user-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.user-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  margin-left: 20rpx;
  overflow: hidden;
}

.user-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.user-nickname {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a1a;
  max-width: 300rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gender-icon {
  font-size: 26rpx;
  margin-left: 8rpx;
}

.gender-icon.male {
  color: #3b82f6;
}

.gender-icon.female {
  color: #ec4899;
}

.user-stats-row {
  display: flex;
  align-items: center;
}

.user-reason-row {
  margin-top: 8rpx;
}

.reason-tag {
  display: inline-block;
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
  line-height: 28rpx;
}

.reason-tag--same_breed {
  color: #10b981;
  background: #ecfdf5;
}

.reason-tag--same_category {
  color: #3b82f6;
  background: #eff6ff;
}

.reason-tag--social {
  color: #8b5cf6;
  background: #f5f3ff;
}

.reason-tag--interact {
  color: #f59e0b;
  background: #fffbeb;
}

.reason-tag--activity {
  color: #ff6a3d;
  background: #fff7ed;
}

.reason-tag--hot {
  color: #ef4444;
  background: #fef2f2;
}

.reason-tag--new_user {
  color: #06b6d4;
  background: #ecfeff;
}

.user-stat {
  font-size: 24rpx;
  color: #999;
}

.stat-dot {
  font-size: 24rpx;
  color: #ccc;
  margin: 0 8rpx;
}

.follow-btn {
  flex-shrink: 0;
  padding: 12rpx 28rpx;
  border-radius: 32rpx;
  background: #ff6a3d;
  margin-left: 16rpx;
}

.follow-btn--following {
  background: #f0f0f0;
}

.follow-btn-text {
  font-size: 24rpx;
  color: #fff;
}

.follow-btn--following .follow-btn-text {
  color: #999;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0;
}

.loading-more-text {
  font-size: 24rpx;
  color: #999;
}

.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0 48rpx;
}

.no-more-text {
  font-size: 24rpx;
  color: #ccc;
}
</style>
