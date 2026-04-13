<template>
  <view class="apm-mask" @tap="onMaskTap" @touchmove.stop.prevent>
    <view class="apm-sheet" @tap.stop>
      <!-- Avatar uploader -->
      <view class="apm-avatar-row">
        <view class="apm-avatar-wrap" @tap="openCameraModal">
          <view v-if="!localForm.avatar" class="apm-avatar-empty">
            <text class="apm-avatar-icon">📷</text>
            <text class="apm-avatar-tip">点击上传</text>
          </view>
          <image v-else class="apm-avatar-img" :src="localForm.avatar" mode="aspectFill" />
        </view>
        <view v-if="localForm.avatar" class="apm-avatar-edit" @tap="openCameraModal">
          <text class="apm-avatar-edit-text">✎</text>
        </view>
      </view>

      <!-- Form -->
      <scroll-view class="apm-body" scroll-y="true" @tap.stop>
        <view class="apm-grid">
          <view class="apm-field apm-field-full">
            <text class="apm-label">宠物昵称</text>
            <input class="apm-input" v-model="localForm.name" placeholder="请输入昵称" />
          </view>

          <view class="apm-field">
            <text class="apm-label">宠物品种</text>
            <input class="apm-input" v-model="localForm.breed" placeholder="请选择品种" />
          </view>

          <view class="apm-field">
            <text class="apm-label">体重 (kg)</text>
            <input class="apm-input" type="digit" v-model="localForm.weight" placeholder="输入体重" />
          </view>

          <view class="apm-field">
            <text class="apm-label">性别</text>
            <picker :range="genderOptions" range-key="label" :value="genderIndex" @change="onGenderPick">
              <view class="apm-select">
                <text v-if="genderIndex >= 0">{{ genderOptions[genderIndex].label }}</text>
                <text v-else class="apm-placeholder">请选择</text>
                <text class="apm-chevron">⌄</text>
              </view>
            </picker>
          </view>

          <view class="apm-field">
            <text class="apm-label">生日</text>
            <picker mode="date" :value="localForm.birthday" @change="onBirthdayPick">
              <view class="apm-select">
                <text v-if="localForm.birthday">{{ localForm.birthday }}</text>
                <text v-else class="apm-placeholder">请选择</text>
                <text class="apm-chevron">⌄</text>
              </view>
            </picker>
          </view>

          <view class="apm-field">
            <text class="apm-label">毛色</text>
            <input class="apm-input" v-model="localForm.color" placeholder="请输入毛色" />
          </view>

          <view class="apm-field">
            <text class="apm-label">宠物类别</text>
            <picker :range="categoryOptions" range-key="label" :value="categoryIndex" @change="onCategoryPick">
              <view class="apm-select">
                <text v-if="categoryIndex >= 0">{{ categoryOptions[categoryIndex].label }}</text>
                <text v-else class="apm-placeholder">请选择</text>
                <text class="apm-chevron">⌄</text>
              </view>
            </picker>
          </view>

          <view class="apm-field">
            <text class="apm-label">是否绝育</text>
            <picker :range="sterilizedOptions" range-key="label" :value="sterilizedIndex" @change="onSterilizedPick">
              <view class="apm-select">
                <text v-if="sterilizedIndex >= 0">{{ sterilizedOptions[sterilizedIndex].label }}</text>
                <text v-else class="apm-placeholder">请选择</text>
                <text class="apm-chevron">⌄</text>
              </view>
            </picker>
          </view>
        </view>

        <view class="apm-actions">
          <button class="apm-btn apm-btn-cancel" @tap="emitClose">取消</button>
          <button class="apm-btn apm-btn-save" type="primary" @tap="emitSave">保存</button>
        </view>
      </scroll-view>
    </view>

    <!-- Camera modal -->
    <view v-if="showCameraModal" class="apm-camera-mask" @tap="onCameraMaskTap" @touchmove.stop.prevent>
      <view class="apm-camera-sheet" @tap.stop>
        <view class="apm-camera-head">
          <text class="apm-camera-title">选择头像</text>
          <text class="apm-camera-close" @tap="closeCameraModal">✕</text>
        </view>
        <view class="apm-camera-list">
          <view class="apm-camera-item" @tap="chooseFromGallery">
            <text class="apm-camera-item-icon">🖼️</text>
            <text class="apm-camera-item-text">从相册选择</text>
          </view>
          <view class="apm-camera-item" @tap="takePhoto">
            <text class="apm-camera-item-icon">📷</text>
            <text class="apm-camera-item-text">拍照</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '@/api'

export default {
  name: "AddPetModal",
  props: {
    initialForm: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      showCameraModal: false,
      uploading: false,
      localForm: this.normalizeForm(this.initialForm)
    };
  },
  computed: {
    genderOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "♂ 公", value: 1 },
        { label: "♀ 母", value: 2 },
        { label: "❓ 未知", value: 0 }
      ];
    },
    categoryOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "🐱 猫", value: 1 },
        { label: "🐶 狗", value: 2 },
        { label: "🐾 其他", value: 0 }
      ];
    },
    sterilizedOptions() {
      return [
        { label: "请选择", value: -1 },
        { label: "✅ 已绝育", value: 1 },
        { label: "❌ 未绝育", value: 0 }
      ];
    },
    genderIndex() {
      const idx = this.genderOptions.findIndex((x) => x.value === this.localForm.gender);
      return idx === -1 ? -1 : idx;
    },
    categoryIndex() {
      const idx = this.categoryOptions.findIndex((x) => x.value === this.localForm.category);
      return idx === -1 ? -1 : idx;
    },
    sterilizedIndex() {
      const idx = this.sterilizedOptions.findIndex((x) => x.value === this.localForm.sterilized);
      return idx === -1 ? -1 : idx;
    }
  },
  watch: {
    initialForm: {
      deep: true,
      handler(v) {
        this.localForm = this.normalizeForm(v);
      }
    }
  },
  methods: {
    onMaskTap(e) {
      // Deliberately do nothing to avoid "instant close" issues.
    },
    onCameraMaskTap(e) {
      if (e && e.target && e.currentTarget && e.target !== e.currentTarget) return;
      this.closeCameraModal();
    },
    normalizeForm(v) {
      const src = v || {};
      return {
        name: src.name || "",
        breed: src.breed || "",
        gender: typeof src.gender === "number" ? src.gender : 0,
        birthday: src.birthday || "",
        weight: src.weight || "",
        color: src.color || "",
        avatar: src.avatar || "",
        category: typeof src.category === "number" ? src.category : 0,
        sterilized: typeof src.sterilized === "number" ? src.sterilized : 0
      };
    },
    emitClose() {
      this.$emit("close");
    },
    emitSave() {
      if (!this.localForm.name) {
        uni.showToast({ title: "请输入宠物昵称", icon: "none" });
        return;
      }
      console.log('[AddPetModal] emitSave 触发表单数据:', this.localForm);
      this.$emit("save", { ...this.localForm });
    },
    openCameraModal() {
      this.showCameraModal = true;
    },
    closeCameraModal() {
      this.showCameraModal = false;
    },
    onGenderPick(e) {
      const idx = Number(e.detail.value);
      const opt = this.genderOptions[idx];
      if (!opt || opt.value === -1) return;
      this.localForm.gender = opt.value;
    },
    onBirthdayPick(e) {
      this.localForm.birthday = e.detail.value;
    },
    onCategoryPick(e) {
      const idx = Number(e.detail.value);
      const opt = this.categoryOptions[idx];
      if (!opt || opt.value === -1) return;
      this.localForm.category = opt.value;
    },
    onSterilizedPick(e) {
      const idx = Number(e.detail.value);
      const opt = this.sterilizedOptions[idx];
      if (!opt || opt.value === -1) return;
      this.localForm.sterilized = opt.value;
    },
    chooseFromGallery() {
      uni.chooseImage({
        count: 1,
        sourceType: ["album"],
        success: (res) => {
          const file = res.tempFilePaths && res.tempFilePaths[0];
          if (file) {
            this.uploadImageToServer(file);
          }
          this.showCameraModal = false;
        },
        fail: () => {
          this.showCameraModal = false;
        }
      });
    },
    takePhoto() {
      uni.chooseImage({
        count: 1,
        sourceType: ["camera"],
        success: (res) => {
          const file = res.tempFilePaths && res.tempFilePaths[0];
          if (file) {
            this.uploadImageToServer(file);
          }
          this.showCameraModal = false;
        },
        fail: () => {
          this.showCameraModal = false;
        }
      });
    },
    async uploadImageToServer(filePath) {
      this.uploading = true;
      uni.showLoading({ title: '上传中...' });
      try {
        console.log('[AddPetModal] 开始上传图片:', filePath);
        const res = await api.pet.uploadImage(filePath);
        console.log('[AddPetModal] 上传响应:', res);
        if (res.success && res.data && res.data.url) {
          const newAvatar = res.data.url;
          console.log('[AddPetModal] 准备设置头像 URL:', newAvatar);
          console.log('[AddPetModal] 设置前 localForm.avatar:', this.localForm.avatar);
          
          // 使用 $set 确保响应式更新（Vue 3 中直接赋值也可以，但为了兼容性问题）
          this.localForm.avatar = newAvatar;
          
          console.log('[AddPetModal] 设置后 localForm.avatar:', this.localForm.avatar);
          console.log('[AddPetModal] 完整 localForm:', JSON.parse(JSON.stringify(this.localForm)));
          
          uni.showToast({ title: '上传成功', icon: 'success' });
        } else {
          throw new Error(res.message || '上传失败');
        }
      } catch (error) {
        console.error('[AddPetModal] 图片上传失败:', error);
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      } finally {
        this.uploading = false;
        uni.hideLoading();
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.apm-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 20000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40rpx 28rpx;
  overflow-y: auto;
}

.apm-sheet {
  width: 100%;
  max-width: 680rpx;
  max-height: 85vh;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 24rpx 60rpx rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
}

.apm-avatar-row {
  padding: 20rpx 0 10rpx;
  display: flex;
  justify-content: center;
  position: relative;
}

.apm-avatar-wrap {
  width: 140rpx;
  height: 140rpx;
  border-radius: 70rpx;
  border: 3rpx dashed rgba(59, 130, 246, 0.35);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.apm-avatar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.apm-avatar-icon {
  font-size: 36rpx;
  margin-bottom: 4rpx;
  color: #94a3b8;
}

.apm-avatar-tip {
  font-size: 20rpx;
  color: #6b7280;
}

.apm-avatar-img {
  width: 100%;
  height: 100%;
}

.apm-avatar-edit {
  position: absolute;
  right: 220rpx;
  bottom: 20rpx;
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 6rpx solid rgba(255, 255, 255, 0.98);
}

.apm-avatar-edit-text {
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
}

.apm-body {
  flex: 1;
  padding: 16rpx 26rpx 24rpx;
  overflow: hidden;
}

.apm-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
}

.apm-field {
  display: flex;
  flex-direction: column;
}

.apm-field-full {
  grid-column: 1 / -1;
}

.apm-label {
  font-size: 22rpx;
  color: #6b7280;
  margin-bottom: 8rpx;
}

.apm-input {
  background: #fff;
  border: 2rpx solid rgba(209, 213, 219, 0.85);
  border-radius: 18rpx;
  padding: 14rpx 16rpx;
  font-size: 26rpx;
  color: #111827;
}

.apm-select {
  background: #fff;
  border: 2rpx solid rgba(209, 213, 219, 0.85);
  border-radius: 18rpx;
  padding: 14rpx 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #111827;
  font-size: 26rpx;
}

.apm-placeholder {
  color: #9ca3af;
}

.apm-chevron {
  color: #9ca3af;
  font-size: 26rpx;
}

.apm-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 14rpx;
}

.apm-btn {
  flex: 1;
  border-radius: 999rpx;
  padding: 18rpx 0;
  font-size: 26rpx;
  font-weight: 800;
}

.apm-btn-cancel {
  background: #fff;
  color: #6b7280;
  border: 2rpx solid rgba(209, 213, 219, 0.9);
}

.apm-btn-save {
  background: #3b82f6;
  color: #fff;
}

.apm-camera-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 20010;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.apm-camera-sheet {
  width: 100%;
  background: rgba(255, 255, 255, 0.98);
  border-top-left-radius: 28rpx;
  border-top-right-radius: 28rpx;
  padding: 22rpx 22rpx 34rpx;
}

.apm-camera-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18rpx;
}

.apm-camera-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
}

.apm-camera-close {
  font-size: 34rpx;
  color: #6b7280;
  padding: 8rpx 12rpx;
}

.apm-camera-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.apm-camera-item {
  display: flex;
  align-items: center;
  padding: 18rpx 14rpx;
  border-radius: 18rpx;
  background: rgba(249, 250, 251, 1);
}

.apm-camera-item-icon {
  font-size: 30rpx;
  margin-right: 12rpx;
}

.apm-camera-item-text {
  font-size: 26rpx;
  color: #111827;
  font-weight: 700;
}
</style>

