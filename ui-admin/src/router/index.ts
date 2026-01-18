import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/workbench/dashboard'
      },
      {
        path: '/workbench/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/HomeView.vue'),
      },
      {
        path: '/workbench/monitor',
        name: 'ServiceMonitor',
        component: () => import('@/views/ServiceMonitorView.vue'),
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/ProfileView.vue'),
      },
      // 安全治理
      {
        path: '/security/user',
        name: 'UserManage',
        component: () => import('@/views/security/UserManageView.vue'),
      },
      // Catch-all route for undefined paths within the layout
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

export default router
