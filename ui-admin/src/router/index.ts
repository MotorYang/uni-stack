import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Root',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
      },
      {
        path: ':pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/MissingComponent.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      {
        path: '',
        name: 'Login',
        component: () => import('@/views/LoginView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：检查是否需要登录
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  // 需要认证的页面，没有 token 则跳转登录页
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 已登录用户访问登录页，跳转到首页
  if (to.path === '/login' && token) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
