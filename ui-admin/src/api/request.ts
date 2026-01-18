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

// 处理401未授权，退出登录并跳转到登录页
function handleUnauthorized(msg: string) {
  const userStore = useUserStore()
  userStore.logout()
  message.error(msg || '请先登录')
  // 使用 setTimeout 确保消息显示后再跳转
  setTimeout(() => {
    window.location.href = '/login'
  }, 300)
}

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // Check if the response follows the standard { code, data, message } format
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200) {
        return res.data
      } else if (res.code === 401) {
        // 业务层面的401，token过期或未登录
        handleUnauthorized(res.message)
        return Promise.reject(new Error(res.message || '请先登录'))
      } else {
        // Business logic error
        message.error(res.message || 'Request failed')
        return Promise.reject(new Error(res.message || 'Request failed'))
      }
    }
    return res
  },
  (error: AxiosError<{ code: number; message: string; data: unknown }>) => {
    const { response } = error
    if (response) {
      const resData = response.data
      switch (response.status) {
        case 401:
            // HTTP层面的401，凭证失效
            handleUnauthorized(resData?.message || '请先登录')
            break
        case 403:
            // 权限不足
            message.warning(resData?.message || '权限不足')
            break
        case 500:
            // 系统异常
            notification.error({ title: '系统异常', content: resData?.message || '请稍后重试' })
            break
        default:
            message.error(resData?.message || response.statusText || '请求失败')
      }
    } else {
        message.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
