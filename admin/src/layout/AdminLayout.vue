<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
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
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/pets">
          <el-icon><Guide /></el-icon>
          <template #title>宠物管理</template>
        </el-menu-item>
        <el-menu-item index="/posts">
          <el-icon><Document /></el-icon>
          <template #title>动态管理</template>
        </el-menu-item>
        <el-menu-item index="/comments">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>评论管理</template>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><Warning /></el-icon>
          <template #title>举报管理</template>
        </el-menu-item>
        <el-menu-item index="/notifications">
          <el-icon><Bell /></el-icon>
          <template #title>通知管理</template>
        </el-menu-item>
        <el-menu-item index="/feedbacks">
          <el-icon><ChatLineSquare /></el-icon>
          <template #title>反馈管理</template>
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="/admins">
          <el-icon><UserFilled /></el-icon>
          <template #title>管理员管理</template>
        </el-menu-item>
        <el-menu-item index="/logs">
          <el-icon><List /></el-icon>
          <template #title>操作日志</template>
        </el-menu-item>
        <el-menu-item v-if="isSuperAdmin" index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
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
              <span class="admin-name">{{ adminName }}</span>
              <el-tag v-if="isSuperAdmin" type="danger" size="small" style="margin-left: 6px;">超管</el-tag>
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
import { getProfile, changePassword } from '../api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const adminName = ref('管理员')
const adminRole = ref('')
const showChangePwd = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const activeMenu = computed(() => route.path)
const isSuperAdmin = computed(() => adminRole.value === 'SUPER_ADMIN')

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
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
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
    router.push('/login')
  } catch (e) {}
}

onMounted(async () => {
  const cached = localStorage.getItem('admin_info')
  if (cached) {
    try {
      const info = JSON.parse(cached)
      adminName.value = info.nickname || info.username || '管理员'
      adminRole.value = info.role || ''
    } catch (e) {}
  }
  try {
    const res = await getProfile()
    if (res.data) {
      adminName.value = res.data.nickname || res.data.username || '管理员'
      adminRole.value = res.data.role || ''
      localStorage.setItem('admin_info', JSON.stringify(res.data))
    }
  } catch (e) {}
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
</style>
