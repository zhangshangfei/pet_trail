<template>
  <view class="publish-page">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="nav-bar">
      <view class="nav-back" @click="onCancel">
        <view class="nav-back-arrow"></view>
      </view>
      <text class="nav-title">发布动态</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="publish-scroll" :style="{ top: navHeight + 'px', paddingBottom: bottomBarHeight + 'px' }">
      <view class="feed-list">
        <view class="card">
          <view class="card-label">
            <text class="card-label-icon">🐾</text>
            <text class="card-label-text">选择宠物</text>
          </view>
          <view class="pet-selector" @click="showPetPicker = true">
            <view v-if="selectedPet" class="selected-pet">
              <image class="pet-avatar" :src="selectedPet.avatar" mode="aspectFill" />
              <text class="pet-name">{{ selectedPet.name }}</text>
            </view>
            <view v-else class="placeholder-pet">
              <text class="placeholder-text">点击选择宠物（可选）</text>
            </view>
            <text class="selector-arrow">›</text>
          </view>
        </view>

        <view class="card">
          <view class="card-label">
            <text class="card-label-icon">✏️</text>
            <text class="card-label-text">动态内容</text>
          </view>
          <textarea
            class="content-input"
            v-model="content"
            placeholder="分享今日宠物瞬间..."
            maxlength="500"
            :auto-height="true"
          />
          <view class="input-counter">
            <text class="counter-text" :class="{ 'counter-warn': content.length > 450 }">{{ content.length }}/500</text>
          </view>
        </view>

        <view v-if="mediaList.length > 0" class="card">
          <view class="card-label">
            <text class="card-label-icon">📷</text>
            <text class="card-label-text">图片/视频</text>
          </view>
          <view class="media-grid">
            <view
              v-for="(media, index) in mediaList"
              :key="index"
              class="media-item"
            >
              <image
                v-if="media.type === 'image'"
                class="media-thumb"
                :src="media.path"
                mode="aspectFill"
                @click="previewImage(index)"
              />
              <view v-else class="video-thumb" @click="previewVideo(index)">
                <image v-if="media.thumb" class="video-thumb-img" :src="media.thumb" mode="aspectFill" />
                <view class="video-play-icon">▶</view>
              </view>
              <view class="remove-media" @click="removeMedia(index)">
                <text class="remove-media-icon">✕</text>
              </view>
            </view>
            <view v-if="mediaList.length < 9" class="media-add" @click="chooseMedia">
              <text class="media-add-icon">+</text>
              <text class="media-add-text">添加</text>
            </view>
          </view>
        </view>

        <view v-if="mediaList.length === 0" class="card">
          <view class="add-media-area" @click="chooseMedia">
            <text class="add-media-emoji">📷</text>
            <text class="add-media-text">添加图片/视频</text>
            <text class="add-media-hint">最多9个媒体文件</text>
          </view>
        </view>

        <view class="card">
          <view class="card-label">
            <text class="card-label-icon">✨</text>
            <text class="card-label-text">更多选项</text>
          </view>
          <view class="option-list">
            <view class="option-item" @click="showStickerPicker = true">
              <text class="option-icon">🎨</text>
              <text class="option-text">{{ selectedStickers.length > 0 ? `已选${selectedStickers.length}个贴纸` : '添加贴纸' }}</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item" @click="showBubbleEditor = true">
              <text class="option-icon">💬</text>
              <text class="option-text">{{ bubbleText ? '编辑文字气泡' : '添加文字气泡' }}</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item" @click="chooseLocation">
              <text class="option-icon">📍</text>
              <text class="option-text">{{ selectedLocation || '添加位置' }}</text>
              <text v-if="selectedLocation" class="option-clear" @click.stop="clearLocation">✕</text>
              <text v-else class="option-arrow">›</text>
            </view>
            <view class="option-item" @click="showChallengePicker = true">
              <text class="option-icon">🏆</text>
              <text class="option-text">{{ selectedChallenge || '添加挑战赛话题' }}</text>
              <text class="option-arrow">›</text>
            </view>
            <view class="option-item" @click="showTagPicker = true">
              <text class="option-icon">#️⃣</text>
              <text class="option-text">{{ selectedTags.length > 0 ? '已选' + selectedTags.length + '个标签' : '添加话题标签' }}</text>
              <text class="option-arrow">›</text>
            </view>
          </view>
        </view>

        <view v-if="selectedTags.length > 0" class="card">
          <view class="card-label">
            <text class="card-label-icon">#️⃣</text>
            <text class="card-label-text">已选标签</text>
          </view>
          <view class="selected-tags-wrap">
            <view v-for="(tag, idx) in selectedTags" :key="idx" class="selected-tag-chip">
              <text class="selected-tag-text">#{{ tag }}</text>
              <text class="selected-tag-remove" @click="removeTag(idx)">✕</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="bottom-bar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
      <text class="cancel-btn" @click="onCancel">取消</text>
      <text
        class="submit-btn"
        :class="{ disabled: !canSubmit || submitting }"
        @click="onSubmit"
      >
        {{ submitting ? '发布中...' : '发布' }}
      </text>
    </view>

    <view v-if="showPetPicker" class="modal-mask" @click="showPetPicker = false">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择宠物</text>
          <text class="modal-close" @click="showPetPicker = false">✕</text>
        </view>
        <view class="pet-list">
          <view
            v-for="pet in petList"
            :key="pet.id"
            class="pet-item"
            :class="{ selected: selectedPet && selectedPet.id === pet.id }"
            @click="selectPet(pet)"
          >
            <image class="pet-item-avatar" :src="pet.avatar" mode="aspectFill" />
            <text class="pet-item-name">{{ pet.name }}</text>
            <text v-if="selectedPet && selectedPet.id === pet.id" class="pet-item-check">✓</text>
          </view>
          <view class="pet-item add-pet" @click="addNewPet">
            <text class="add-pet-icon">+</text>
            <text class="pet-item-name">添加宠物</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="showChallengePicker" class="modal-mask" @click="showChallengePicker = false">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择挑战赛话题</text>
          <text class="modal-close" @click="showChallengePicker = false">✕</text>
        </view>
        <view class="challenge-list">
          <view
            v-for="challenge in challengeList"
            :key="challenge.id"
            class="challenge-item"
            :class="{ selected: selectedChallenge === challenge.name }"
            @click="selectChallenge(challenge)"
          >
            <text class="challenge-item-icon">🏆</text>
            <view class="challenge-item-info">
              <text class="challenge-item-name">{{ challenge.name }}</text>
              <text class="challenge-item-desc">{{ challenge.description }}</text>
            </view>
            <text v-if="selectedChallenge === challenge.name" class="challenge-item-check">✓</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="showStickerPicker" class="modal-mask" @click="showStickerPicker = false">
      <view class="modal-card sticker-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择贴纸</text>
          <text class="modal-close" @click="showStickerPicker = false">✕</text>
        </view>
        <view class="sticker-category">
          <view
            v-for="cat in stickerCategories"
            :key="cat.key"
            class="sticker-cat-item"
            :class="{ active: currentStickerCat === cat.key }"
            @click="currentStickerCat = cat.key"
          >
            <text class="sticker-cat-text">{{ cat.label }}</text>
          </view>
        </view>
        <view class="sticker-grid">
          <view
            v-for="sticker in currentStickers"
            :key="sticker"
            class="sticker-item"
            :class="{ selected: selectedStickers.includes(sticker) }"
            @click="toggleSticker(sticker)"
          >
            <text class="sticker-emoji">{{ sticker }}</text>
          </view>
        </view>
        <view v-if="selectedStickers.length > 0" class="sticker-selected-bar">
          <scroll-view scroll-x class="sticker-selected-scroll">
            <view class="sticker-selected-list">
              <view v-for="(s, i) in selectedStickers" :key="i" class="sticker-selected-item">
                <text class="sticker-selected-emoji">{{ s }}</text>
                <view class="sticker-remove" @click="removeSticker(i)">
                  <text class="sticker-remove-icon">✕</text>
                </view>
              </view>
            </view>
          </scroll-view>
          <text class="sticker-confirm-btn" @click="showStickerPicker = false">确定</text>
        </view>
      </view>
    </view>

    <view v-if="showBubbleEditor" class="modal-mask" @click="showBubbleEditor = false">
      <view class="modal-card bubble-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">文字气泡</text>
          <text class="modal-close" @click="showBubbleEditor = false">✕</text>
        </view>
        <view class="bubble-editor">
          <view class="bubble-preview" :style="{ background: bubbleColor }">
            <text class="bubble-preview-text" :style="{ color: bubbleTextColor }">{{ bubbleText || '输入文字预览...' }}</text>
          </view>
          <textarea
            class="bubble-input"
            v-model="bubbleText"
            placeholder="输入气泡文字..."
            maxlength="30"
          />
          <view class="bubble-color-row">
            <text class="bubble-color-label">气泡颜色</text>
            <view class="bubble-color-options">
              <view
                v-for="c in bubbleColors"
                :key="c.bg"
                class="bubble-color-dot"
                :class="{ active: bubbleColor === c.bg }"
                :style="{ background: c.bg }"
                @click="bubbleColor = c.bg; bubbleTextColor = c.text"
              />
            </view>
          </view>
          <view v-if="bubbleText" class="bubble-actions">
            <text class="bubble-clear-btn" @click="bubbleText = ''">清除</text>
            <text class="bubble-confirm-btn" @click="showBubbleEditor = false">确定</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="showTagPicker" class="modal-mask" @click="showTagPicker = false">
      <view class="modal-card" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择话题标签</text>
          <text class="modal-close" @click="showTagPicker = false">✕</text>
        </view>
        <view class="tag-search-bar">
          <input class="tag-search-input" v-model="tagSearchKeyword" placeholder="搜索标签..." @input="onTagSearch" />
        </view>
        <view class="tag-grid">
          <view
            v-for="tag in tagList"
            :key="tag.id || tag.name"
            class="tag-chip"
            :class="{ active: selectedTags.includes(tag.name) }"
            @click="toggleTag(tag.name)"
          >
            <text class="tag-chip-text">#{{ tag.name }}</text>
            <text v-if="tag.usageCount" class="tag-chip-count">{{ tag.usageCount }}</text>
          </view>
        </view>
        <view v-if="selectedTags.length > 0" class="tag-selected-bar">
          <scroll-view scroll-x class="tag-selected-scroll">
            <view class="tag-selected-list">
              <view v-for="(t, i) in selectedTags" :key="i" class="tag-selected-item">
                <text class="tag-selected-text">#{{ t }}</text>
                <view class="tag-remove" @click="removeTag(i)">
                  <text class="tag-remove-icon">✕</text>
                </view>
              </view>
            </view>
          </scroll-view>
          <text class="tag-confirm-btn" @click="showTagPicker = false">确定</text>
        </view>
      </view>
    </view>

    <view v-if="showVideoPlayer" class="video-player-mask" @click="closeVideoPlayer">
      <view class="video-player-container" @click.stop>
        <view class="video-player-close" @click="closeVideoPlayer">✕</view>
        <video
          class="video-player-video"
          :src="currentVideoUrl"
          :autoplay="true"
          :show-play-btn="true"
          :show-center-play-btn="true"
          :enable-progress-gesture="true"
          :show-fullscreen-btn="true"
          object-fit="contain"
        />
      </view>
    </view>
  </view>
</template>

<script>
import * as postApi from '@/api/post'
import * as petApi from '@/api/pet'
import * as tagApi from '@/api/tag'
import { checkLogin } from '@/utils/index'

export default {
  data() {
    return {
      statusBarHeight: 20,
      navHeight: 64,
      bottomBarHeight: 100,
      safeAreaBottom: 0,

      content: '',
      selectedPet: null,
      mediaList: [],
      selectedChallenge: '',
      selectedStickers: [],
      bubbleText: '',
      bubbleColor: '#ff6a3d',
      bubbleTextColor: '#ffffff',
      selectedLocation: '',

      showPetPicker: false,
      showChallengePicker: false,
      showStickerPicker: false,
      showBubbleEditor: false,
      showTagPicker: false,
      currentStickerCat: 'pet',

      selectedTags: [],
      tagSearchKeyword: '',
      tagList: [],

      stickerCategories: [
        { key: 'pet', label: '🐾 宠物' },
        { key: 'mood', label: '😊 心情' },
        { key: 'food', label: '🍖 美食' },
        { key: 'nature', label: '🌸 自然' },
        { key: 'deco', label: '✨ 装饰' }
      ],

      stickerMap: {
        pet: ['🐱', '🐶', '🐰', '🐹', '🐦', '🐟', '🐢', '🦎', '🐍', '🐸', '🦜', '🐩', '🐈', '🦮', '🐕‍🦺', '🐈‍⬛'],
        mood: ['😊', '😂', '🥰', '😍', '😎', '🤩', '😜', '🥳', '😴', '🤗', '😭', '😤', '🥺', '😱', '🤯', '😇'],
        food: ['🍖', '🦴', '🥩', '🍗', '🥓', '🍔', '🍕', '🌭', '🥕', '🥦', '🍎', '🍌', '🥛', '🍼', '☕', '🧃'],
        nature: ['🌸', '🌺', '🌻', '🌹', '🌷', '🍀', '🌿', '🌴', '🌈', '☀️', '🌙', '⭐', '❄️', '🌊', '🍂', '🌾'],
        deco: ['✨', '💫', '⭐', '🌟', '💖', '💝', '🎀', '🎉', '🎊', '🎈', '🎁', '🏆', '👑', '💎', '🦋', '🐝']
      },

      bubbleColors: [
        { bg: '#ff6a3d', text: '#ffffff' },
        { bg: '#10b981', text: '#ffffff' },
        { bg: '#3b82f6', text: '#ffffff' },
        { bg: '#8b5cf6', text: '#ffffff' },
        { bg: '#f59e0b', text: '#ffffff' },
        { bg: '#ec4899', text: '#ffffff' },
        { bg: '#1f2937', text: '#ffffff' },
        { bg: '#ffffff', text: '#111827' }
      ],

      petList: [],
      challengeList: [
        { id: 1, name: '#萌宠日常', description: '分享你和宠物的日常瞬间' },
        { id: 2, name: '#宠物才艺', description: '展示宠物的特长和技能' },
        { id: 3, name: '#宠物穿搭', description: '宠物的时尚搭配' },
        { id: 4, name: '#宠物旅行', description: '带宠物去看世界' }
      ],

      submitting: false,
      showVideoPlayer: false,
      currentVideoUrl: ''
    }
  },
  computed: {
    canSubmit() {
      return this.content.trim().length > 0
    },
    currentStickers() {
      return this.stickerMap[this.currentStickerCat] || []
    }
  },
  onLoad() {
    try {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = (sys && sys.statusBarHeight) || 20
      this.navHeight = this.statusBarHeight + 44
      this.safeAreaBottom = sys.safeAreaInsets ? sys.safeAreaInsets.bottom : 0
    } catch (e) {
      this.navHeight = 64
      this.safeAreaBottom = 0
    }

    this.loadPetList()
    this.loadHotTags()
  },
  methods: {
    async loadPetList() {
      const token = uni.getStorageSync('token')
      if (!token) return

      try {
        const res = await petApi.getPetList()
        if (res.success && Array.isArray(res.data)) {
          this.petList = res.data
        }
      } catch (error) {
        console.error('加载宠物列表失败:', error)
      }
    },

    onCancel() {
      if (!this.content.trim() && this.mediaList.length === 0) {
        uni.navigateBack()
        return
      }
      uni.showModal({
        title: '提示',
        content: '确定要放弃发布吗？',
        success: (res) => {
          if (res.confirm) {
            uni.navigateBack()
          }
        }
      })
    },

    async onSubmit() {
      if (!this.canSubmit || this.submitting) return

      const loggedIn = await checkLogin('请先登录后再发布动态')
      if (!loggedIn) return

      this.submitting = true
      uni.showLoading({ title: '发布中...', mask: true })

      try {
        const imageFiles = this.mediaList.filter(m => m.type === 'image')
        const videoFiles = this.mediaList.filter(m => m.type === 'video')
        const totalFiles = imageFiles.length + videoFiles.length
        let uploadedCount = 0

        const uploadedImageUrls = []
        for (let i = 0; i < imageFiles.length; i++) {
          const media = imageFiles[i]
          uploadedCount++
          uni.showLoading({
            title: `压缩并上传 ${uploadedCount}/${totalFiles}...`,
            mask: true
          })

          try {
            const compressedPath = await petApi.compressImage(media.path, 80)
            const uploadRes = await petApi.uploadFile(compressedPath)
            if (uploadRes.success && uploadRes.data && uploadRes.data.url) {
              uploadedImageUrls.push(uploadRes.data.url)
            } else {
              throw new Error(uploadRes.message || '上传失败')
            }
          } catch (uploadError) {
            console.error(`图片 ${i + 1} 上传失败:`, uploadError)
            uni.hideLoading()
            uni.showToast({ title: `图片 ${i + 1} 上传失败`, icon: 'none' })
            this.submitting = false
            return
          }
        }

        const uploadedVideoUrls = []
        for (let i = 0; i < videoFiles.length; i++) {
          const media = videoFiles[i]
          uploadedCount++
          uni.showLoading({
            title: `上传视频 ${uploadedCount}/${totalFiles}...`,
            mask: true
          })

          try {
            const uploadRes = await petApi.uploadFile(media.path)
            if (uploadRes.success && uploadRes.data && uploadRes.data.url) {
              uploadedVideoUrls.push(uploadRes.data.url)
            } else {
              throw new Error(uploadRes.message || '上传失败')
            }
          } catch (uploadError) {
            console.error(`视频 ${i + 1} 上传失败:`, uploadError)
            uni.hideLoading()
            uni.showToast({ title: `视频 ${i + 1} 上传失败`, icon: 'none' })
            this.submitting = false
            return
          }
        }

        uni.showLoading({ title: '发布中...', mask: true })

        const postData = {
          content: this.content,
          petId: this.selectedPet ? this.selectedPet.id : undefined,
          images: uploadedImageUrls.length > 0 ? uploadedImageUrls : undefined,
          videos: uploadedVideoUrls.length > 0 ? uploadedVideoUrls : undefined,
          challengeTag: this.selectedChallenge || undefined,
          tags: this.selectedTags.length > 0 ? this.selectedTags : undefined,
          stickers: this.selectedStickers.length > 0 ? this.selectedStickers : undefined,
          bubble: this.bubbleText ? { text: this.bubbleText, bgColor: this.bubbleColor, textColor: this.bubbleTextColor } : undefined,
          location: this.selectedLocation || undefined
        }

        const res = await postApi.createPost(postData)

        uni.hideLoading()

        if (res.success) {
          uni.showToast({ title: '发布成功', icon: 'success' })
          this.content = ''
          this.selectedPet = null
          this.mediaList = []
          this.selectedChallenge = ''
          this.selectedStickers = []
          this.selectedTags = []
          this.bubbleText = ''
          this.bubbleColor = '#ff6a3d'
          this.bubbleTextColor = '#ffffff'
          this.selectedLocation = ''
          setTimeout(() => {
            uni.switchTab({ url: '/pages/home/index' })
          }, 1500)
        }
      } catch (error) {
        uni.hideLoading()
        console.error('发布失败:', error)
        uni.showToast({ title: error.message || '发布失败，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },

    selectPet(pet) {
      this.selectedPet = pet
      this.showPetPicker = false
    },

    addNewPet() {
      this.showPetPicker = false
      uni.navigateTo({ url: '/pages/pets/add' })
    },

    selectChallenge(challenge) {
      this.selectedChallenge = challenge.name
      this.showChallengePicker = false
    },

    chooseMedia() {
      const remaining = 9 - this.mediaList.length
      if (remaining <= 0) {
        uni.showToast({ title: '最多添加9个媒体文件', icon: 'none' })
        return
      }

      uni.chooseMedia({
        count: remaining,
        mediaType: ['image', 'video'],
        sourceType: ['album', 'camera'],
        sizeType: ['compressed'],
        success: (res) => {
          res.tempFiles.forEach(file => {
            const item = {
              type: file.fileType,
              path: file.tempFilePath
            }
            if (file.fileType === 'video' && file.thumbTempFilePath) {
              item.thumb = file.thumbTempFilePath
            }
            this.mediaList.push(item)
          })
        }
      })
    },

    previewImage(index) {
      const imagePaths = this.mediaList
        .filter(m => m.type === 'image')
        .map(m => m.path)
      if (imagePaths.length > 0) {
        uni.previewImage({ urls: imagePaths, current: index })
      }
    },

    previewVideo(index) {
      const video = this.mediaList[index]
      if (video && video.path) {
        this.currentVideoUrl = video.path
        this.showVideoPlayer = true
      }
    },

    closeVideoPlayer() {
      this.showVideoPlayer = false
      this.currentVideoUrl = ''
    },

    removeMedia(index) {
      this.mediaList.splice(index, 1)
    },

    toggleSticker(sticker) {
      const idx = this.selectedStickers.indexOf(sticker)
      if (idx >= 0) {
        this.selectedStickers.splice(idx, 1)
      } else if (this.selectedStickers.length < 10) {
        this.selectedStickers.push(sticker)
      } else {
        uni.showToast({ title: '最多选择10个贴纸', icon: 'none' })
      }
    },

    removeSticker(index) {
      this.selectedStickers.splice(index, 1)
    },

    chooseLocation() {
      uni.chooseLocation({
        success: (res) => {
          this.selectedLocation = res.name || res.address
        },
        fail: (err) => {
          if (err.errMsg && err.errMsg.indexOf('auth deny') !== -1) {
            uni.showModal({
              title: '位置权限',
              content: '需要获取您的位置信息，请在设置中开启',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  uni.openSetting()
                }
              }
            })
          }
        }
      })
    },

    clearLocation() {
      this.selectedLocation = ''
    },

    async loadHotTags() {
      try {
        const res = await tagApi.getHotTags(30)
        if (res && res.success && Array.isArray(res.data)) {
          this.tagList = res.data
        }
      } catch (e) {
        console.error('加载热门标签失败:', e)
      }
    },

    async onTagSearch() {
      try {
        const keyword = this.tagSearchKeyword.trim()
        if (!keyword) {
          await this.loadHotTags()
          return
        }
        const res = await tagApi.searchTags(keyword, 20)
        if (res && res.success && Array.isArray(res.data)) {
          this.tagList = res.data
        }
      } catch (e) {
        console.error('搜索标签失败:', e)
      }
    },

    toggleTag(name) {
      const idx = this.selectedTags.indexOf(name)
      if (idx >= 0) {
        this.selectedTags.splice(idx, 1)
      } else if (this.selectedTags.length < 5) {
        this.selectedTags.push(name)
      } else {
        uni.showToast({ title: '最多选择5个标签', icon: 'none' })
      }
    },

    removeTag(index) {
      this.selectedTags.splice(index, 1)
    },
  }
}
</script>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  width: 100%;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
}

.nav-bar {
  height: 92rpx;
  background: linear-gradient(180deg, #ff7a3d 0%, #ff4d4f 100%);
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
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

.nav-placeholder {
  width: 64rpx;
}

.publish-scroll {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
}

.feed-list {
  padding: 8rpx 20rpx;
  padding-bottom: 40rpx;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.card-label {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-label-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.card-label-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.pet-selector {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f9fafb;
  border-radius: 16rpx;
}

.selected-pet,
.placeholder-pet {
  display: flex;
  align-items: center;
  flex: 1;
}

.pet-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  background: #e5e7eb;
}

.pet-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.placeholder-text {
  font-size: 28rpx;
  color: #9ca3af;
}

.selector-arrow {
  font-size: 40rpx;
  color: #9ca3af;
}

.content-input {
  width: 100%;
  min-height: 200rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  line-height: 1.6;
  color: #374151;
  box-sizing: border-box;
}

.input-counter {
  display: flex;
  justify-content: flex-end;
  margin-top: 12rpx;
}

.counter-text {
  font-size: 22rpx;
  color: #9ca3af;
}

.counter-warn {
  color: #ff4d4f;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.media-item {
  position: relative;
  width: 100%;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f3f4f6;
}

.media-thumb {
  width: 100%;
  height: 100%;
}

.video-thumb {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5e7eb;
  position: relative;
}

.video-thumb-img {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.video-play-icon {
  position: absolute;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  z-index: 1;
}

.remove-media {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 44rpx;
  height: 44rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-media-icon {
  font-size: 22rpx;
  color: #fff;
}

.media-add {
  width: 100%;
  height: 200rpx;
  border: 2rpx dashed #d1d5db;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.media-add-icon {
  font-size: 48rpx;
  color: #9ca3af;
  margin-bottom: 8rpx;
}

.media-add-text {
  font-size: 22rpx;
  color: #9ca3af;
}

.add-media-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  border: 2rpx dashed #d1d5db;
}

.add-media-emoji {
  font-size: 60rpx;
  margin-bottom: 12rpx;
}

.add-media-text {
  font-size: 28rpx;
  color: #6b7280;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.add-media-hint {
  font-size: 22rpx;
  color: #9ca3af;
}

.option-list {
  border-radius: 16rpx;
  overflow: hidden;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 24rpx 16rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.option-item:last-child {
  border-bottom: none;
}

.option-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.option-text {
  flex: 1;
  font-size: 28rpx;
  color: #374151;
}

.option-arrow {
  font-size: 32rpx;
  color: #d1d5db;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 24rpx;
}

.cancel-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 999rpx;
}

.submit-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 999rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 106, 61, 0.3);

  &:active:not(.disabled) {
    transform: scale(0.98);
  }

  &.disabled {
    color: #9ca3af;
    background: #e5e7eb;
    box-shadow: none;
  }
}

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
  max-height: 70vh;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.modal-close {
  font-size: 32rpx;
  color: #9ca3af;
  padding: 8rpx;
}

.pet-list {
  padding: 16rpx;
  max-height: 50vh;
  overflow-y: auto;
}

.pet-item {
  display: flex;
  align-items: center;
  padding: 20rpx 16rpx;
  border-radius: 16rpx;
  margin-bottom: 8rpx;

  &.selected {
    background: #fff5f0;
  }
}

.pet-item-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  background: #e5e7eb;
}

.pet-item-name {
  flex: 1;
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
}

.pet-item-check {
  font-size: 28rpx;
  color: #ff6a3d;
  font-weight: 700;
}

.add-pet {
  justify-content: center;
  border: 2rpx dashed #d1d5db;
  border-radius: 16rpx;
  padding: 24rpx;
}

.add-pet-icon {
  font-size: 36rpx;
  color: #9ca3af;
  margin-right: 12rpx;
}

.challenge-list {
  padding: 16rpx;
  max-height: 50vh;
  overflow-y: auto;
}

.challenge-item {
  display: flex;
  align-items: center;
  padding: 20rpx 16rpx;
  border-radius: 16rpx;
  margin-bottom: 8rpx;
  background: #f9fafb;

  &.selected {
    background: #fff5f0;
  }
}

.challenge-item-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.challenge-item-info {
  flex: 1;
}

.challenge-item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4rpx;
}

.challenge-item-desc {
  font-size: 22rpx;
  color: #9ca3af;
}

.challenge-item-check {
  font-size: 28rpx;
  color: #ff6a3d;
  font-weight: 700;
}

.video-player-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player-container {
  position: relative;
  width: 100%;
}

.video-player-close {
  position: absolute;
  top: -80rpx;
  right: 20rpx;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.video-player-video {
  width: 100%;
  height: 420rpx;
}

.sticker-modal {
  max-height: 80vh;
}

.sticker-category {
  display: flex;
  padding: 16rpx 24rpx;
  gap: 16rpx;
  border-bottom: 1rpx solid #f3f4f6;
  overflow-x: auto;
}

.sticker-cat-item {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #f3f4f6;

  &.active {
    background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  }
}

.sticker-cat-text {
  font-size: 24rpx;
  color: #374151;

  .sticker-cat-item.active & {
    color: #fff;
  }
}

.sticker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  padding: 20rpx 24rpx;
  max-height: 40vh;
  overflow-y: auto;
}

.sticker-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  border-radius: 16rpx;
  background: #f9fafb;
  border: 2rpx solid transparent;

  &.selected {
    border-color: #ff6a3d;
    background: #fff5f0;
  }
}

.sticker-emoji {
  font-size: 48rpx;
}

.sticker-selected-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid #f3f4f6;
  gap: 16rpx;
}

.sticker-selected-scroll {
  flex: 1;
  white-space: nowrap;
}

.sticker-selected-list {
  display: inline-flex;
  gap: 12rpx;
}

.sticker-selected-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  background: #f9fafb;
  border-radius: 12rpx;
}

.sticker-selected-emoji {
  font-size: 36rpx;
}

.sticker-remove {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 28rpx;
  height: 28rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sticker-remove-icon {
  font-size: 16rpx;
  color: #fff;
}

.sticker-confirm-btn {
  flex-shrink: 0;
  padding: 12rpx 32rpx;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 999rpx;
}

.bubble-modal {
  max-height: 80vh;
}

.bubble-editor {
  padding: 24rpx;
}

.bubble-preview {
  padding: 24rpx 32rpx;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  min-height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bubble-preview-text {
  font-size: 30rpx;
  font-weight: 600;
  text-align: center;
}

.bubble-input {
  width: 100%;
  height: 120rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 20rpx;
}

.bubble-color-row {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.bubble-color-label {
  font-size: 26rpx;
  color: #6b7280;
  margin-right: 20rpx;
}

.bubble-color-options {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.bubble-color-dot {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 4rpx solid transparent;

  &.active {
    border-color: #111827;
    transform: scale(1.15);
  }
}

.bubble-actions {
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
}

.bubble-clear-btn {
  padding: 16rpx 32rpx;
  font-size: 26rpx;
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 999rpx;
}

.bubble-confirm-btn {
  padding: 16rpx 32rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  border-radius: 999rpx;
}

.option-clear {
  font-size: 24rpx;
  color: #9ca3af;
  padding: 4rpx 8rpx;
}

.selected-tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 8rpx 0;
}

.selected-tag-chip {
  display: flex;
  align-items: center;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  background: #fff0ea;
}

.selected-tag-text {
  font-size: 24rpx;
  color: #ff6a3d;
  font-weight: 500;
}

.selected-tag-remove {
  font-size: 20rpx;
  color: #ff6a3d;
  margin-left: 8rpx;
}

.tag-search-bar {
  padding: 16rpx 24rpx;
}

.tag-search-input {
  width: 100%;
  height: 64rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding: 0 24rpx;
  font-size: 26rpx;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 8rpx 24rpx;
  max-height: 400rpx;
  overflow-y: auto;
}

.tag-chip {
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  border-radius: 24rpx;
  background: #f5f5f5;
  transition: all 0.2s;
}

.tag-chip.active {
  background: #fff0ea;
}

.tag-chip-text {
  font-size: 24rpx;
  color: #374151;
  font-weight: 500;
}

.tag-chip.active .tag-chip-text {
  color: #ff6a3d;
}

.tag-chip-count {
  font-size: 20rpx;
  color: #9ca3af;
  margin-left: 8rpx;
}

.tag-selected-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.tag-selected-scroll {
  flex: 1;
  white-space: nowrap;
}

.tag-selected-list {
  display: inline-flex;
  gap: 12rpx;
}

.tag-selected-item {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  background: #fff0ea;
}

.tag-selected-text {
  font-size: 24rpx;
  color: #ff6a3d;
  font-weight: 500;
}

.tag-remove {
  margin-left: 8rpx;
}

.tag-remove-icon {
  font-size: 20rpx;
  color: #ff6a3d;
}

.tag-confirm-btn {
  padding: 12rpx 32rpx;
  border-radius: 24rpx;
  background: #ff6a3d;
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
  margin-left: 16rpx;
  flex-shrink: 0;
}
</style>
