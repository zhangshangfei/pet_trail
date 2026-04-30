<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '230px'" class="admin-aside">
      <div class="logo">
        <span v-if="!isCollapse" class="logo-text">🐾 宠迹管理</span>
        <span v-else class="logo-icon">🐾</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#1d1e1f"
        text-color="#bfcbd9"
        active-text-color="#ff6a3d"
      >
        <template v-for="menu in menuList" :key="menu.id">
          <el-menu-item v-if="!menu.children || menu.children.length === 0" :index="menu.path" :class="{ 'menu-disabled': Number(menu.status ?? 1) !== 1 }">
            <el-icon><component :is="menu.icon" /></el-icon>
            <template #title>{{ menu.name }}<span v-if="Number(menu.status ?? 1) !== 1" class="menu-disabled-tag">（已禁用）</span></template>
          </el-menu-item>
          <el-sub-menu v-else :index="'menu-' + menu.id" :class="{ 'menu-disabled': Number(menu.status ?? 1) !== 1 }">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}<span v-if="Number(menu.status ?? 1) !== 1" class="menu-disabled-tag">（已禁用）</span></span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path" :class="{ 'menu-disabled': Number(child.status ?? 1) !== 1 }">
              <el-icon><component :is="child.icon" /></el-icon>
              <template #title>{{ child.name }}<span v-if="Number(child.status ?? 1) !== 1" class="menu-disabled-tag">（已禁用）</span></template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="admin-name">{{ adminStore.adminInfo?.nickname || adminStore.adminInfo?.username || '管理员' }}</span>
              <el-tag :type="roleTagType" size="small" style="margin-left: 6px;">{{ roleLabel }}</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog v-model="showChangePwd" title="修改密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePwd = false">取消</el-button>
        <el-button type="primary" @click="submitChangePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { changePassword } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const isCollapse = ref(false)
const showChangePwd = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const activeMenu = computed(() => route.path)
const menuList = computed(() => {
  const filterMenus = (menus) => {
    return menus
      .filter(m => {
        if (m.children && m.children.length > 0) {
          m.children = filterMenus(m.children)
          return true
        }
        return m.path && m.path.trim() !== ''
      })
      .map(m => ({ ...m }))
  }
  return filterMenus(adminStore.menus || [])
})

const roleLabel = computed(() => {
  return adminStore.roleName || '管理员'
})

const roleTagType = computed(() => {
  const map = { SUPER_ADMIN: 'danger', ADMIN: 'warning', MERCHANT_ADMIN: 'warning', MERCHANT_STAFF: 'info' }
  return map[adminStore.roleCode] || 'info'
})

const handleCommand = (command) => {
  if (command === 'logout') {
    adminStore.logout()
    router.push('/login')
  } else if (command === 'changePassword') {
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    showChangePwd.value = true
  }
}

const submitChangePwd = async () => {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  if (pwdForm.value.newPassword.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  try {
    await changePassword(pwdForm.value)
    ElMessage.success('密码修改成功，请重新登录')
    showChangePwd.value = false
    adminStore.logout()
    router.push('/login')
  } catch (e) {}
}

onMounted(async () => {
  await adminStore.fetchProfile()
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.admin-aside {
  background: #1d1e1f;
  transition: width 0.3s;
  overflow: hidden;
}
.admin-aside :deep(.el-sub-menu__title:hover) {
  background-color: #2d2e2f !important;
}
.admin-aside :deep(.el-sub-menu .el-menu-item) {
  background-color: #1a1a1a !important;
  padding-left: 52px !important;
}
.admin-aside :deep(.el-sub-menu .el-menu-item:hover) {
  background-color: #2d2e2f !important;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #333;
}
.logo-text {
  color: #ff6a3d;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}
.logo-icon {
  font-size: 24px;
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}
.collapse-btn:hover {
  color: #ff6a3d;
}
.header-right {
  display: flex;
  align-items: center;
}
.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.admin-name {
  font-size: 14px;
  color: #333;
}
.admin-main {
  background: #f5f5f5;
  padding: 20px;
}

.menu-disabled {
  opacity: 0.5;
}

.menu-disabled-tag {
  font-size: 11px;
  color: #f56c6c;
  margin-left: 4px;
}
</style>
