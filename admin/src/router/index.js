import { createRouter, createWebHistory } from 'vue-router'
import { getProfile } from '../api/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘', permission: 'dashboard' } },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue'), meta: { title: '用户管理', permission: 'user:view' } },
      { path: 'pets', name: 'Pets', component: () => import('../views/Pets.vue'), meta: { title: '宠物管理', permission: 'pet:view' } },
      { path: 'posts', name: 'Posts', component: () => import('../views/Posts.vue'), meta: { title: '动态管理', permission: 'post:view' } },
      { path: 'comments', name: 'Comments', component: () => import('../views/Comments.vue'), meta: { title: '评论管理', permission: 'comment:view' } },
      { path: 'reports', name: 'Reports', component: () => import('../views/Reports.vue'), meta: { title: '举报管理', permission: 'report:view' } },
      { path: 'notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { title: '通知管理', permission: 'notification:view' } },
      { path: 'feedbacks', name: 'Feedbacks', component: () => import('../views/Feedbacks.vue'), meta: { title: '反馈管理', permission: 'feedback:view' } },
      { path: 'admins', name: 'Admins', component: () => import('../views/Admins.vue'), meta: { title: '管理员管理', permission: 'admin:manage' } },
      { path: 'logs', name: 'Logs', component: () => import('../views/Logs.vue'), meta: { title: '操作日志', permission: 'log:view' } },
      { path: 'settings', name: 'Settings', component: () => import('../views/Settings.vue'), meta: { title: '系统设置', permission: 'setting:manage' } },
      { path: 'config', name: 'Config', component: () => import('../views/Config.vue'), meta: { title: '系统配置管理', permission: 'config:manage' } },
      { path: 'ai-models', name: 'AiModels', component: () => import('../views/AiModel.vue'), meta: { title: 'AI模型管理', permission: 'ai-model:view' } },
      { path: 'challenges', name: 'Challenges', component: () => import('../views/Challenges.vue'), meta: { title: '挑战赛配置', permission: 'challenge:view' } },
      { path: 'products', name: 'Products', component: () => import('../views/Products.vue'), meta: { title: '商城管理', permission: 'product:view' } },
      { path: 'vet-clinics', name: 'VetClinics', component: () => import('../views/VetClinics.vue'), meta: { title: '医院信息管理', permission: 'vet-clinic:view' } },
      { path: 'merchants', name: 'Merchants', component: () => import('../views/Merchants.vue'), meta: { title: '商户管理', permission: 'merchant:manage' } },
      { path: 'roles', name: 'Roles', component: () => import('../views/Roles.vue'), meta: { title: '角色管理', permission: 'admin:manage' } },
      { path: 'sys-menus', name: 'SysMenus', component: () => import('../views/SysMenus.vue'), meta: { title: '菜单管理', permission: 'admin:manage' } },
      { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue'), meta: { title: '页面不存在' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let profileChecked = false

function checkPermission(roleCode, buttons, requiredPermission) {
  if (!requiredPermission) return true
  if (roleCode === 'SUPER_ADMIN') return true
  if (!buttons) return false
  return buttons.includes(requiredPermission)
}

function extractAdmin(data) {
  if (data.admin) return data.admin
  if (data.roleCode) return data
  return {}
}

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('admin_token')

  if (to.path === '/login') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (!profileChecked) {
    try {
      const res = await getProfile()
      if (res.data) {
        const admin = extractAdmin(res.data)
        localStorage.setItem('admin_info', JSON.stringify(admin))
        if (res.data.menus) {
          localStorage.setItem('admin_menus', JSON.stringify(res.data.menus))
        }
        profileChecked = true

        if (to.path === '/dashboard' || to.path === '/') {
          next()
          return
        }
        const requiredPermission = to.meta?.permission
        if (!checkPermission(admin.roleCode, admin.buttons, requiredPermission)) {
          next('/dashboard')
          return
        }
      }
      next()
    } catch (e) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
      localStorage.removeItem('admin_menus')
      profileChecked = false
      next('/login')
    }
    return
  }

  if (to.path === '/dashboard' || to.path === '/') {
    next()
    return
  }

  const adminInfo = JSON.parse(localStorage.getItem('admin_info') || '{}')
  const requiredPermission = to.meta?.permission
  if (!checkPermission(adminInfo.roleCode, adminInfo.buttons, requiredPermission)) {
    next('/dashboard')
    return
  }

  next()
})

export default router
