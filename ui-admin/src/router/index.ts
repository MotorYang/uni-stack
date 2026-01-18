import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Root',
    component: () => import('@/layouts/DefaultLayout.vue'),
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

export default router
