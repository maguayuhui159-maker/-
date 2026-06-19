import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/agreement',
    name: 'Agreement',
    component: () => import('../views/Agreement.vue')
  },
  {
    path: '/',
    component: () => import('../layout/PublicLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('../views/UserManage.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'courses',
        name: 'CourseManage',
        component: () => import('../views/CourseManage.vue')
      },
      {
        path: 'store',
        name: 'WorkManage',
        component: () => import('../views/WorkManage.vue')
      },
      {
        path: 'ai',
        name: 'AiService',
        component: () => import('../views/AiService.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'homework',
        name: 'HomeworkManage',
        component: () => import('../views/HomeworkManage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'ops',
        name: 'OpsCenter',
        component: () => import('../views/OpsCenter.vue'),
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard with guest mode
// 游客白名单，/ 下由于直接挂载了 /home，不再需要校验
const whiteList = ['/login', '/agreement', '/courses', '/store', '/home']

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const isWhiteListed = whiteList.includes(to.path) || to.path === '/'
  const requiresAuth = Boolean(to.meta?.requiresAuth)
  const allowedRoles = to.meta?.roles || []
  
  if (!token && (requiresAuth || !isWhiteListed)) {
    next({ name: 'Login' })
  } else if (token && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
