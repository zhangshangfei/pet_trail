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
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue'), meta: { title: '用户管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'pets', name: 'Pets', component: () => import('../views/Pets.vue'), meta: { title: '宠物管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'posts', name: 'Posts', component: () => import('../views/Posts.vue'), meta: { title: '动态管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'comments', name: 'Comments', component: () => import('../views/Comments.vue'), meta: { title: '评论管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'reports', name: 'Reports', component: () => import('../views/Reports.vue'), meta: { title: '举报管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { title: '通知管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'feedbacks', name: 'Feedbacks', component: () => import('../views/Feedbacks.vue'), meta: { title: '反馈管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'admins', name: 'Admins', component: () => import('../views/Admins.vue'), meta: { title: '管理员管理', roles: ['SUPER_ADMIN'] } },
      { path: 'logs', name: 'Logs', component: () => import('../views/Logs.vue'), meta: { title: '操作日志', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'settings', name: 'Settings', component: () => import('../views/Settings.vue'), meta: { title: '系统设置', roles: ['SUPER_ADMIN'] } },
      { path: 'config', name: 'Config', component: () => import('../views/Config.vue'), meta: { title: '系统配置管理', roles: ['SUPER_ADMIN'] } },
      { path: 'ai-models', name: 'AiModels', component: () => import('../views/AiModel.vue'), meta: { title: 'AI模型管理', roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue'), meta: { title: '页面不存在' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let profileChecked = false

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
        localStorage.setItem('admin_info', JSON.stringify(res.data))
        profileChecked = true

        const role = res.data.role
        const requiredRoles = to.meta?.roles
        if (requiredRoles && !requiredRoles.includes(role)) {
          next('/dashboard')
          return
        }
      }
      next()
    } catch (e) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
      profileChecked = false
      next('/login')
    }
    return
  }

  const adminInfo = JSON.parse(localStorage.getItem('admin_info') || '{}')
  const role = adminInfo.role
  const requiredRoles = to.meta?.roles
  if (requiredRoles && !requiredRoles.includes(role)) {
    next('/dashboard')
    return
  }

  next()
})

export default router
