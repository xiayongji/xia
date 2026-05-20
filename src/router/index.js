import { createRouter, createWebHistory } from 'vue-router'
import { Role } from '../utils/permissions'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/AdminDashboard.vue'),
    meta: { requiresAuth: true, roles: [Role.ADMIN] }
  },
  {
    path: '/user',
    name: 'UserDashboard',
    component: () => import('../views/UserDashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    redirect: '/admin'
  },
  {
    path: '/admin-settings',
    redirect: '/admin'
  },
  {
    path: '/scenes',
    name: 'Scenes',
    component: () => import('../views/Scenes.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/energy',
    name: 'Energy',
    component: () => import('../views/Energy.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue'),
    meta: { requiresAuth: true, roles: [Role.ADMIN] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const hasToken = localStorage.getItem('token')
  const userRole = localStorage.getItem('role') || 'user'

  if (requiresAuth && !hasToken) {
    next('/login')
    return
  }

  const requiredRoles = to.matched.find(record => record.meta.roles)?.meta.roles
  if (requiredRoles && !requiredRoles.includes(userRole)) {
    next('/user')
    return
  }

  if (hasToken && (to.path === '/login' || to.path === '/register')) {
    next('/user')
    return
  }

  next()
})

export default router
