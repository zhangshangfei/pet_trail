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
        <el-menu-item v-if="hasPermission('dashboard')" index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>

        <el-sub-menu v-if="hasAnyPermission(['user:view','pet:view'])" index="group-user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户与宠物</span>
          </template>
          <el-menu-item v-if="hasPermission('user:view')" index="/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('pet:view')" index="/pets">
            <el-icon><Guide /></el-icon>
            <template #title>宠物管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="hasAnyPermission(['post:view','comment:view'])" index="group-content">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item v-if="hasPermission('post:view')" index="/posts">
            <el-icon><Document /></el-icon>
            <template #title>动态管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('comment:view')" index="/comments">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>评论管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="hasAnyPermission(['report:view','notification:view','feedback:view','challenge:view'])" index="group-operation">
          <template #title>
            <el-icon><Bell /></el-icon>
            <span>运营与互动</span>
          </template>
          <el-menu-item v-if="hasPermission('report:view')" index="/reports">
            <el-icon><Warning /></el-icon>
            <template #title>举报管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('notification:view')" index="/notifications">
            <el-icon><Bell /></el-icon>
            <template #title>通知管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('feedback:view')" index="/feedbacks">
            <el-icon><ChatLineSquare /></el-icon>
            <template #title>反馈管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('challenge:view')" index="/challenges">
            <el-icon><Trophy /></el-icon>
            <template #title>挑战赛配置</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="hasAnyPermission(['product:view','vet-clinic:view','merchant:manage'])" index="group-business">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>商业服务</span>
          </template>
          <el-menu-item v-if="hasPermission('vet-clinic:view')" index="/vet-clinics">
            <el-icon><FirstAidKit /></el-icon>
            <template #title>医院管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('product:view')" index="/products">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>商品管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('merchant:manage')" index="/merchants">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>商户管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="hasAnyPermission(['admin:manage','log:view','setting:manage','config:manage','ai-model:view'])" index="group-system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="hasPermission('admin:manage')" index="/admins">
            <el-icon><UserFilled /></el-icon>
            <template #title>管理员管理</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('log:view')" index="/logs">
            <el-icon><List /></el-icon>
            <template #title>操作日志</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('setting:manage')" index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('config:manage')" index="/config">
            <el-icon><Tools /></el-icon>
            <template #title>系统配置</template>
          </el-menu-item>
          <el-menu-item v-if="hasPermission('ai-model:view')" index="/ai-models">
            <el-icon><Cpu /></el-icon>
            <template #title>AI模型管理</template>
          </el-menu-item>
        </el-sub-menu>
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
import { getProfile, changePassword } from '../api/admin'
import { useAdminStore } from '@/store/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const isCollapse = ref(false)
const adminName = ref('管理员')
const adminRole = ref('')
const showChangePwd = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const activeMenu = computed(() => route.path)

const roleLabel = computed(() => {
  const map = { SUPER_ADMIN: '超管', ADMIN: '管理员', MERCHANT_ADMIN: '商户管理', MERCHANT_STAFF: '商户员工' }
  return map[adminRole.value] || '管理员'
})

const roleTagType = computed(() => {
  const map = { SUPER_ADMIN: 'danger', ADMIN: '', MERCHANT_ADMIN: 'warning', MERCHANT_STAFF: 'info' }
  return map[adminRole.value] || ''
})

function hasPermission(code) {
  return adminStore.hasPermission(code)
}

function hasAnyPermission(codes) {
  return adminStore.hasAnyPermission(codes)
}

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
      adminStore.adminInfo = res.data
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
</style>
