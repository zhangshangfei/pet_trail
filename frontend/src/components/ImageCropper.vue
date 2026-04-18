<template>
  <view v-if="visible" class="cropper-mask" @tap="onCancel">
    <view class="cropper-container" @tap.stop>
      <view class="cropper-header" :style="headerStyle">
        <text class="cropper-title">裁剪头像</text>
      </view>

      <view class="cropper-body">
        <view
          class="cropper-canvas-wrap"
          @touchstart="onTouchStart"
          @touchmove.stop.prevent="onTouchMove"
          @touchend="onTouchEnd"
        >
          <image
            v-if="imageSrc"
            :src="imageSrc"
            class="cropper-image"
            :style="imageStyle"
            mode="scaleToFill"
            @load="onImageLoad"
          />
          <view class="cropper-overlay">
            <view class="cropper-cut-area" :style="cutAreaStyle">
              <view class="cropper-cut-border"></view>
              <view class="cropper-grid-h cropper-grid-h1"></view>
              <view class="cropper-grid-h cropper-grid-h2"></view>
              <view class="cropper-grid-v cropper-grid-v1"></view>
              <view class="cropper-grid-v cropper-grid-v2"></view>
            </view>
          </view>
        </view>
      </view>

      <view class="cropper-footer" :style="{ paddingBottom: 'calc(24rpx + env(safe-area-inset-bottom))' }">
        <view class="cropper-footer-inner">
          <view class="cropper-action-btn cropper-action-cancel" @tap="onCancel">
            <text class="cropper-action-text cropper-action-cancel-text">取消</text>
          </view>
          <text class="cropper-tip-text">拖动和缩放调整裁剪区域</text>
          <view class="cropper-action-btn cropper-action-confirm" @tap="onConfirm">
            <text class="cropper-action-text cropper-action-confirm-text">完成</text>
          </view>
        </view>
      </view>

      <canvas
        canvas-id="avatarCropper"
        class="cropper-hidden-canvas"
        :style="{ width: canvasSize + 'px', height: canvasSize + 'px' }"
      />
    </view>
  </view>
</template>

<script>
export default {
  name: 'ImageCropper',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    imageSrc: {
      type: String,
      default: ''
    },
    circular: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      statusBarHeight: 20,
      headerHeight: 44,
      canvasSize: 300,
      imgWidth: 0,
      imgHeight: 0,
      imgScale: 1,
      imgX: 0,
      imgY: 0,
      cutLeft: 0,
      cutTop: 0,
      cutSize: 280,
      touchStartX: 0,
      touchStartY: 0,
      touchStartImgX: 0,
      touchStartImgY: 0,
      touchStartScale: 1,
      touchStartDistance: 0,
      isTouching: false,
      isTwoFinger: false,
      canvasWrapWidth: 375,
      canvasWrapHeight: 375,
      initialized: false
    }
  },
  computed: {
    headerStyle() {
      return {
        paddingTop: this.statusBarHeight + 'px',
        height: (this.statusBarHeight + this.headerHeight) + 'px'
      }
    },
    imageStyle() {
      return {
        width: this.imgWidth * this.imgScale + 'px',
        height: this.imgHeight * this.imgScale + 'px',
        transform: `translate(${this.imgX}px, ${this.imgY}px)`
      }
    },
    cutAreaStyle() {
      return {
        left: this.cutLeft + 'px',
        top: this.cutTop + 'px',
        width: this.cutSize + 'px',
        height: this.cutSize + 'px',
        borderRadius: this.circular ? '50%' : '0'
      }
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.initLayout()
      }
    }
  },
  methods: {
    initLayout() {
      try {
        const sys = uni.getSystemInfoSync()
        this.statusBarHeight = (sys && sys.statusBarHeight) || 20
        this.canvasWrapWidth = sys.windowWidth

        let menuBottom = this.statusBarHeight + 44
        try {
          const menuRect = wx.getMenuButtonBoundingClientRect()
          if (menuRect && menuRect.bottom) {
            menuBottom = menuRect.bottom + 8
          }
        } catch (e) {}

        this.headerHeight = menuBottom - this.statusBarHeight

        const footerH = 100
        this.canvasWrapHeight = sys.windowHeight - menuBottom - footerH
      } catch (e) {
        this.canvasWrapHeight = 375
      }

      this.cutSize = Math.min(
        Math.floor(this.canvasWrapWidth * 0.82),
        Math.floor(this.canvasWrapHeight * 0.75)
      )
      this.cutLeft = (this.canvasWrapWidth - this.cutSize) / 2
      this.cutTop = (this.canvasWrapHeight - this.cutSize) / 2
      this.initialized = false
    },
    onImageLoad(e) {
      const { width, height } = e.detail
      this.imgWidth = width
      this.imgHeight = height

      const scaleX = this.canvasWrapWidth / width
      const scaleY = this.canvasWrapHeight / height
      this.imgScale = Math.max(scaleX, scaleY)

      const scaledW = width * this.imgScale
      const scaledH = height * this.imgScale
      this.imgX = (this.canvasWrapWidth - scaledW) / 2
      this.imgY = (this.canvasWrapHeight - scaledH) / 2

      this.initialized = true
    },
    onTouchStart(e) {
      if (!this.initialized) return
      const touches = e.touches
      if (touches.length === 1) {
        this.isTwoFinger = false
        this.touchStartX = touches[0].clientX
        this.touchStartY = touches[0].clientY
        this.touchStartImgX = this.imgX
        this.touchStartImgY = this.imgY
      } else if (touches.length === 2) {
        this.isTwoFinger = true
        this.touchStartDistance = this.getDistance(touches[0], touches[1])
        this.touchStartScale = this.imgScale
      }
      this.isTouching = true
    },
    onTouchMove(e) {
      if (!this.isTouching || !this.initialized) return
      const touches = e.touches
      if (this.isTwoFinger && touches.length === 2) {
        const distance = this.getDistance(touches[0], touches[1])
        const scale = this.touchStartScale * (distance / this.touchStartDistance)
        const minScale = Math.max(
          this.cutSize / this.imgWidth,
          this.cutSize / this.imgHeight
        )
        this.imgScale = Math.max(minScale, Math.min(scale, minScale * 3))
      } else if (touches.length === 1) {
        const dx = touches[0].clientX - this.touchStartX
        const dy = touches[0].clientY - this.touchStartY
        this.imgX = this.touchStartImgX + dx
        this.imgY = this.touchStartImgY + dy
      }
    },
    onTouchEnd() {
      this.isTouching = false
      this.isTwoFinger = false
      this.adjustPosition()
    },
    adjustPosition() {
      const scaledW = this.imgWidth * this.imgScale
      const scaledH = this.imgHeight * this.imgScale
      const cutRight = this.cutLeft + this.cutSize
      const cutBottom = this.cutTop + this.cutSize

      if (scaledW > this.cutSize) {
        if (this.imgX > this.cutLeft) this.imgX = this.cutLeft
        if (this.imgX + scaledW < cutRight) this.imgX = cutRight - scaledW
      } else {
        this.imgX = this.cutLeft + (this.cutSize - scaledW) / 2
      }

      if (scaledH > this.cutSize) {
        if (this.imgY > this.cutTop) this.imgY = this.cutTop
        if (this.imgY + scaledH < cutBottom) this.imgY = cutBottom - scaledH
      } else {
        this.imgY = this.cutTop + (this.cutSize - scaledH) / 2
      }
    },
    getDistance(t1, t2) {
      const x = t1.clientX - t2.clientX
      const y = t1.clientY - t2.clientY
      return Math.sqrt(x * x + y * y)
    },
    onCancel() {
      this.$emit('cancel')
    },
    onConfirm() {
      this.cropImage()
    },
    cropImage() {
      uni.showLoading({ title: '裁剪中...', mask: true })

      const outputSize = 800
      this.canvasSize = outputSize

      const scaleRatio = outputSize / this.cutSize
      const drawX = (this.imgX - this.cutLeft) * scaleRatio
      const drawY = (this.imgY - this.cutTop) * scaleRatio
      const drawW = this.imgWidth * this.imgScale * scaleRatio
      const drawH = this.imgHeight * this.imgScale * scaleRatio

      const ctx = uni.createCanvasContext('avatarCropper', this)
      ctx.clearRect(0, 0, outputSize, outputSize)

      if (this.circular) {
        ctx.save()
        ctx.beginPath()
        ctx.arc(outputSize / 2, outputSize / 2, outputSize / 2, 0, Math.PI * 2)
        ctx.clip()
      }

      ctx.drawImage(this.imageSrc, drawX, drawY, drawW, drawH)

      if (this.circular) {
        ctx.restore()
      }

      ctx.draw(false, () => {
        setTimeout(() => {
          uni.canvasToTempFilePath({
            canvasId: 'avatarCropper',
            destWidth: outputSize,
            destHeight: outputSize,
            quality: 0.9,
            fileType: 'jpg',
            success: (res) => {
              uni.hideLoading()
              this.$emit('confirm', res.tempFilePath)
            },
            fail: (err) => {
              console.error('canvasToTempFilePath 失败:', err)
              uni.hideLoading()
              uni.showToast({ title: '裁剪失败，请重试', icon: 'none' })
            }
          }, this)
        }, 300)
      })
    }
  }
}
</script>

<style scoped>
.cropper-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000;
  z-index: 9999;
}

.cropper-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.cropper-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 32rpx;
  background: rgba(0, 0, 0, 0.8);
  flex-shrink: 0;
}

.cropper-title {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}

.cropper-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cropper-canvas-wrap {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.cropper-image {
  position: absolute;
  top: 0;
  left: 0;
  transform-origin: 0 0;
}

.cropper-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.cropper-cut-area {
  position: absolute;
  z-index: 2;
  box-shadow: 0 0 0 2000px rgba(0, 0, 0, 0.55);
}

.cropper-cut-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2rpx solid rgba(255, 255, 255, 0.8);
  border-radius: inherit;
}

.cropper-grid-h,
.cropper-grid-v {
  position: absolute;
  background: rgba(255, 255, 255, 0.25);
}

.cropper-grid-h {
  left: 0;
  right: 0;
  height: 1rpx;
}

.cropper-grid-v {
  top: 0;
  bottom: 0;
  width: 1rpx;
}

.cropper-grid-h1 {
  top: 33.33%;
}

.cropper-grid-h2 {
  top: 66.66%;
}

.cropper-grid-v1 {
  left: 33.33%;
}

.cropper-grid-v2 {
  left: 66.66%;
}

.cropper-footer {
  padding: 20rpx 32rpx;
  background: rgba(0, 0, 0, 0.8);
  flex-shrink: 0;
}

.cropper-footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cropper-action-btn {
  min-width: 140rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 36rpx;
}

.cropper-action-cancel {
  background: rgba(255, 255, 255, 0.1);
}

.cropper-action-confirm {
  background: linear-gradient(135deg, #ff7a3d 0%, #ff4d4f 100%);
  box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.4);
}

.cropper-action-text {
  font-size: 28rpx;
  font-weight: 600;
}

.cropper-action-cancel-text {
  color: #ccc;
}

.cropper-action-confirm-text {
  color: #fff;
}

.cropper-tip-text {
  color: #999;
  font-size: 22rpx;
  flex: 1;
  text-align: center;
  padding: 0 16rpx;
}

.cropper-hidden-canvas {
  position: fixed;
  left: -9999px;
  top: -9999px;
}
</style>
