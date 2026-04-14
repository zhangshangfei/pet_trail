<template>
  <view class="community-page">
    <user-top-bar
      :status-bar-height="statusBarHeight"
      :avatar="userAvatar"
      :name="userName"
      right-icon="🔔"
      @rightTap="onBellTap"
    />

    <!-- 动态列表 -->
    <scroll-view scroll-y class="post-scroll" :style="{ height: scrollHeight + 'px', paddingTop: headerHeight + 'px' }">
      <view class="post-list">
        <!-- 动态列表 -->
        <view v-for="(post, index) in postList" :key="post.id" class="post-card">
          <view class="post-header">
            <image class="post-avatar" :src="post.userAvatar || defaultAvatar" mode="aspectFill" />
            <view class="post-meta">
              <text class="post-name">{{ post.userName || '未知用户' }}</text>
              <text class="post-time">{{ formatTime(post.createTime) }}</text>
            </view>
          </view>

          <text class="post-content">{{ post.content }}</text>

          <view v-if="post.images && post.images !== 'null' && Array.isArray(post.images) && post.images.length" class="post-images">
            <image
              v-for="(img, idx) in post.images"
              :key="'img-' + post.id + '-' + idx"
              class="post-image"
              :class="post.images.length === 2 ? 'post-image--2' : post.images.length === 3 ? 'post-image--3' : 'post-image--1'"
              :src="img"
              mode="widthFix"
              @click="previewImage(post.images, idx)"
            />
          </view>

          <view class="post-actions">
            <view class="post-actions-left">
              <view class="post-action" @click="toggleLike(post)">
                <text class="post-action-icon">{{ post.liked ? '❤️' : '🤍' }}</text>
                <text class="post-action-count">{{ post.likeCount || 0 }}</text>
              </view>
              <view class="post-action" @click="showComments(post)">
                <text class="post-action-icon">💬</text>
                <text class="post-action-count">{{ post.commentCount || 0 }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loading" class="loading-text">加载中...</view>
        <view v-if="!hasMore && postList.length > 0" class="no-more-text">没有更多了</view>
        
        <!-- 空状态 -->
        <view v-if="isEmpty" class="empty-state">
          <text class="empty-state-text">暂无动态</text>
        </view>
      </view>
    </scroll-view>

    <!-- 悬浮发布按钮 -->
    <view class="fab-button" @click="showCreateModal = true">
      <text class="fab-icon">＋</text>
    </view>

    <!-- 发布动态弹窗 -->
    <view v-if="showCreateModal" class="modal-mask" @click="showCreateModal = false">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">发布动态</text>
          <text class="modal-close" @click="showCreateModal = false">✕</text>
        </view>

        <view class="modal-body">
          <textarea
            class="content-input"
            v-model="newPostContent"
            placeholder="分享你和宠物的美好时光..."
            maxlength="500"
          />

          <!-- 已选图片 -->
          <view v-if="selectedImages.length > 0" class="selected-images">
            <view
              v-for="(img, index) in selectedImages"
              :key="'selected-img-' + index"
              class="selected-image-item"
            >
              <image class="selected-image" :src="img" mode="aspectFill" />
              <text class="remove-image" @click="removeImage(index)">✕</text>
            </view>
          </view>

          <!-- 添加图片按钮 -->
          <view class="add-image-btn" @click="chooseImage">
            <text class="add-icon">📷</text>
            <text class="add-text">添加图片</text>
          </view>
        </view>

        <view class="modal-footer">
          <button class="cancel-btn" @click="showCreateModal = false">取消</button>
          <button class="submit-btn" :loading="submitting" @click="submitPost">发布</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import * as postApi from '@/api/post'
import * as petApi from '@/api/pet'
import UserTopBar from '@/components/UserTopBar.vue'
import { compressImages } from '@/utils/imageCompress'

const defaultAvatar = 'https://ai-public.mastergo.com/ai/img_res/1774537096721a3K9mP2xQ7vN4rT8wY.jpg'

export default {
  components: {
    UserTopBar
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 0,
      scrollHeight: 0,
      defaultAvatar,
      userAvatar: defaultAvatar,
      userName: '萌宠主人',
      postList: [],
      page: 1,
      size: 10,
      loading: false,
      hasMore: true,
      showCreateModal: false,
      newPostContent: '',
      selectedImages: [],
      submitting: false
    }
  },
  computed: {
    isEmpty() {
      return !this.loading && this.postList.length === 0
    }
  },
  onShow() {
    this.loadUserInfo()
    this.loadPosts()
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      const headerHeight = this.statusBarHeight + 92
      // scroll-view 高度 = 窗口高度 - 顶部导航高度
      this.scrollHeight = (sys && sys.windowHeight) ? sys.windowHeight : 0
      this.headerHeight = headerHeight
    } catch (e) {
      this.statusBarHeight = 20
      this.headerHeight = 112
      this.scrollHeight = 0
    }
    this.loadUserInfo()
  },
  methods: {
    // 加载用户信息
    loadUserInfo() {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo && userInfo.avatar) {
        this.userAvatar = userInfo.avatar
      }
      if (userInfo && userInfo.nickname) {
        this.userName = userInfo.nickname
      }
    },
    onBellTap() {
      uni.showToast({ title: '通知未实现', icon: 'none' })
    },

    // 加载动态列表
    async loadPosts() {
      if (this.loading || !this.hasMore) return

      // 检查是否登录
      const token = uni.getStorageSync('token')
      if (!token) {
        this.loading = false
        return
      }

      this.loading = true
      try {
        const res = await postApi.getFeed(this.page, this.size)
        if (res.success && Array.isArray(res.data)) {
          const newPosts = res.data
          // 初始化每条动态的点赞状态
          newPosts.forEach(post => {
            if (post.liked === undefined || post.liked === null) {
              post.liked = false
            }
            if (post.likeCount === undefined || post.likeCount === null) {
              post.likeCount = 0
            }
          })
          if (newPosts.length < this.size) {
            this.hasMore = false
          }
          this.postList = [...this.postList, ...newPosts]
          this.page++
        }
      } catch (error) {
        if (error.statusCode !== 401) {
          uni.showToast({ title: '加载失败', icon: 'none' })
        }
      } finally {
        this.loading = false
      }
    },

    // 格式化时间
    formatTime(timestamp) {
      if (!timestamp) return ''
      const now = Date.now()
      const diff = now - timestamp
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      
      const date = new Date(timestamp)
      return `${date.getMonth() + 1}/${date.getDate()}`
    },

    // 点赞/取消点赞
    async toggleLike(post) {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showModal({
          title: '提示',
          content: '请先登录',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/login/index' })
            }
          }
        })
        return
      }

      try {
        const res = await postApi.toggleLike(post.id)
        if (res.success) {
          // 使用 $set 确保微信小程序能正确更新视图
          this.$set(post, 'liked', res.data.liked)
          const newLikeCount = res.data.liked
            ? (post.likeCount || 0) + 1
            : Math.max(0, (post.likeCount || 0) - 1)
          this.$set(post, 'likeCount', newLikeCount)
        }
      } catch (error) {
        console.error('点赞失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    // 显示评论（暂未实现）
    showComments(post) {
      uni.showToast({ title: '评论功能开发中', icon: 'none' })
    },

    // 预览图片
    previewImage(images, index) {
      // 确保 images 是数组
      const imageList = Array.isArray(images) ? images : [images]
      uni.previewImage({
        urls: imageList,
        current: index
      })
    },

    // 选择图片
    async chooseImage() {
      if (this.selectedImages.length >= 9) {
        uni.showToast({ title: '最多上传 9 张图片', icon: 'none' })
        return
      }
      
      uni.showLoading({ title: '选择图片...', mask: true })
      
      try {
        const res = await uni.chooseImage({
          count: 9 - this.selectedImages.length,
          sourceType: ['album', 'camera']
        })
        
        uni.showLoading({ title: '处理图片...', mask: true })
        
        // 压缩图片
        const compressedPaths = await compressImages(res.tempFilePaths, {
          quality: 75,
          maxWidth: 1920,
          maxHeight: 1920
        })
        
        this.selectedImages = [...this.selectedImages, ...compressedPaths]
        uni.hideLoading()
      } catch (error) {
        console.error('选择图片失败:', error)
        uni.hideLoading()
        uni.showToast({ title: '选择图片失败', icon: 'none' })
      }
    },

    // 移除图片
    removeImage(index) {
      this.selectedImages.splice(index, 1)
    },

    // 发布动态
    async submitPost() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showModal({
          title: '提示',
          content: '请先登录',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/login/index' })
            }
          }
        })
        return
      }

      if (!this.newPostContent.trim()) {
        uni.showToast({ title: '请输入内容', icon: 'none' })
        return
      }

      this.submitting = true
      uni.showLoading({ title: '发布中...', mask: true })

      try {
        // 先上传图片
        const uploadedUrls = []
        
        for (let i = 0; i < this.selectedImages.length; i++) {
          uni.showLoading({ 
            title: `上传图片 ${i + 1}/${this.selectedImages.length}...`, 
            mask: true 
          })
          
          try {
            const uploadRes = await petApi.uploadImage(this.selectedImages[i])
            if (uploadRes.success && uploadRes.data && uploadRes.data.url) {
              uploadedUrls.push(uploadRes.data.url)
            } else {
              throw new Error(uploadRes.message || '上传失败')
            }
          } catch (uploadError) {
            console.error(`图片 ${i + 1} 上传失败:`, uploadError)
            uni.hideLoading()
            uni.showToast({
              title: `图片 ${i + 1} 上传失败`,
              icon: 'none'
            })
            this.submitting = false
            return
          }
        }

        // 发布动态
        uni.showLoading({ title: '发布中...', mask: true })
        
        const res = await postApi.createPost({
          content: this.newPostContent,
          images: uploadedUrls.length > 0 ? uploadedUrls : undefined
        })

        uni.hideLoading()

        if (res.success) {
          uni.showToast({ title: '发布成功', icon: 'success' })

          // 添加到列表顶部
          const newPost = res.data
          newPost.liked = false
          newPost.likeCount = 0
          newPost.userAvatar = this.userAvatar
          newPost.userName = '我'
          this.postList.unshift(newPost)

          // 重置表单
          this.newPostContent = ''
          this.selectedImages = []
          this.showCreateModal = false
        }
      } catch (error) {
        uni.hideLoading()
        console.error('发布失败:', error)
        uni.showToast({ title: '发布失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.community-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.community-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 10rpx 30rpx rgba(17, 24, 39, 0.06);
  padding: 0 20rpx 14rpx;
}

.community-header-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.community-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #e5e7eb;
}

.community-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
}

.community-header-right {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 96rpx;
  display: flex;
  justify-content: flex-end;
}

.community-icon {
  font-size: 38rpx;
  color: #6b7280;
}

.post-scroll {
  /* padding-top 改为动态设置 */
}

.post-list {
  padding: 20rpx;
  padding-bottom: 160rpx;
}

/* 悬浮发布按钮 */
.fab-button {
  position: fixed;
  right: 40rpx;
  bottom: calc(env(safe-area-inset-bottom) + 180rpx);
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 40rpx rgba(102, 126, 234, 0.4);
  z-index: 100;
  transition: transform 0.2s;
}

.fab-button:active {
  transform: scale(0.9);
}

.fab-icon {
  font-size: 60rpx;
  color: #fff;
  font-weight: 300;
}

/* 空状态 */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-state-text {
  font-size: 28rpx;
  color: #999;
}

/* 动态卡片 - 与首页一致 */
.post-card {
  background-color: #ffffff;
  border-radius: 22rpx;
  padding: 22rpx 18rpx;
  margin-bottom: 18rpx;
  border: 2rpx solid #f3f4f6;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 14rpx;
}

.post-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  background-color: #e5e7eb;
}

.post-meta {
  display: flex;
  flex-direction: column;
}

.post-name {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
  line-height: 36rpx;
}

.post-time {
  font-size: 22rpx;
  color: #6b7280;
  margin-top: 6rpx;
}

.post-content {
  display: block;
  font-size: 26rpx;
  color: #374151;
  line-height: 36rpx;
  margin-bottom: 14rpx;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 10rpx;
}

.post-image {
  border-radius: 16rpx;
  background-color: #f3f4f6;
  width: 100%;
  display: block;
}

.post-image--2 {
  width: calc(50% - 6rpx);
}

.post-image--3 {
  width: calc(33.333% - 8rpx);
}

.post-image--1 {
  width: 100%;
}

.post-actions {
  border-top: 1rpx solid #f3f4f6;
  padding-top: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-actions-left {
  display: flex;
  gap: 22rpx;
}

.post-action {
  display: flex;
  align-items: center;
  color: #6b7280;
}

.post-action-icon {
  font-size: 30rpx;
}

.post-action-count {
  margin-left: 8rpx;
  font-size: 22rpx;
  font-weight: 800;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 26rpx;
}

/* 弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.modal-card {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
}

.modal-close {
  font-size: 40rpx;
  color: #999;
  padding: 8rpx;
}

.modal-body {
  padding: 24rpx;
}

.content-input {
  width: 100%;
  min-height: 200rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

.selected-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-top: 20rpx;
}

.selected-image-item {
  position: relative;
  aspect-ratio: 1;
}

.selected-image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  overflow: hidden;
}

.remove-image {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.add-image-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  border: 2rpx dashed #e0e0e0;
  border-radius: 12rpx;
  margin-top: 20rpx;
}

.add-icon {
  font-size: 60rpx;
  margin-bottom: 12rpx;
}

.add-text {
  color: #999;
  font-size: 26rpx;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.submit-btn {
  background: linear-gradient(180deg, #8b9cf7 0%, #667eea 100%);
  color: #fff;
}
</style>
