import axios from 'axios'
import type { AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { createDiscreteApi } from 'naive-ui'

const { message, notification } = createDiscreteApi(['message', 'notification'])

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

import { useUserStore } from '@/stores/user'

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // Check if the response follows the standard { code, data, message } format
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200) {
        return res.data
      } else {
        // Business logic error
        message.error(res.message || 'Request failed')
        return Promise.reject(new Error(res.message || 'Request failed'))
      }
    }
    return res
  },
  (error: AxiosError) => {
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401:
            // 凭证失效，跳转认证中心
            // Assuming sys-auth is an external URL or a route
            // For now, we'll log it. In a real app, we might redirect.
            console.warn('401 Unauthorized - Redirecting to sys-auth')
            // window.location.href = '/sys-auth' 
            break
        case 403:
            // 权限不足，Naive UI 弹出警告通知
            message.warning('权限不足')
            break
        case 500:
            // 系统异常
            notification.error({ title: '系统异常', content: '请稍后重试' })
            break
        default:
            message.error(response.statusText || '请求失败')
      }
    } else {
        message.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
