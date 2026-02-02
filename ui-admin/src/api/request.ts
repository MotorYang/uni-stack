import axios from 'axios'
import type {AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse} from 'axios'
import {createDiscreteApi} from 'naive-ui'
import {useUserStore} from '@/stores/user'
import {refresh} from '@/api/auth'

const {message, notification} = createDiscreteApi(['message', 'notification'])

// 标记：是否正在刷新 Token
let isRefreshing = false
// 存储：刷新期间被挂起的请求队列
let requestsQueue: ((token: string) => void)[] = []

// 扩展 AxiosRequestConfig 接口，增加重试标记
interface CustomRequestConfig extends InternalAxiosRequestConfig {
    _retry?: boolean;
}

const service: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 10000,
})

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    (error: AxiosError) => Promise.reject(error)
)

// 彻底退出并清理（仅本地清理，不调用服务端 logout API，避免无限循环）
function handleUnauthorized(msg: string) {
    const userStore = useUserStore()
    // 直接清理本地状态，不调用 logout API（token 已失效，调用也会 401）
    userStore.clearToken()
    userStore.userInfo = null
    if (window.location.pathname !== '/login') {
        message.error(msg || '会话已过期，请重新登录')
        window.location.href = '/login'
    }
}

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data
        // 如果后端返回的是标准封装格式
        if (res && Object.prototype.hasOwnProperty.call(res, 'code')) {
            if (res.code === 200) return res.data

            // 如果业务码是 401，走刷新逻辑或跳转登录
            if (res.code === 401) {
                return handleTokenRefresh(response.config)
            }

            message.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || 'Error'))
        }
        return res
    },
    async (error: AxiosError<{ code: number; message: string }>) => {
        const {response, config} = error

        if (response && config) {
            switch (response.status) {
                case 401:
                    // HTTP 401 拦截
                    return handleTokenRefresh(config)
                case 403:
                    message.warning('权限不足')
                    break
                case 500:
                    notification.error({title: '系统异常', content: response.data?.message || '请稍后重试'})
                    break
                default:
                    message.error(response.data?.message || '网络繁忙')
            }
        } else {
            message.error('网络连接异常')
        }
        return Promise.reject(error)
    }
)

/**
 * 核心逻辑：处理 Token 刷新
 */
async function handleTokenRefresh(config: CustomRequestConfig) {

    // 如果该请求已经重试过一次了，直接报错，防止无限循环
    if (config._retry) {
        handleUnauthorized('身份验证失败')
        return Promise.reject(new Error('Auth Retry Failed'))
    }
    config._retry = true

    const userStore = useUserStore()

    if (!isRefreshing) {
        isRefreshing = true
        try {
            if (!userStore.refreshToken) {
                throw new Error('续签失败，请重新登录')
            }
            const { accessToken, refreshToken } = await refresh({refreshToken: userStore.refreshToken})
            userStore.setTokens(accessToken, refreshToken)
            // 执行队列中的请求
            requestsQueue.forEach((callback) => callback(accessToken))
            requestsQueue = []
            config.headers.Authorization = `Bearer ${accessToken}`
            return service(config)
        } catch (refreshError) {
            // 刷新失败（如 Refresh Token 也过期了），彻底清除状态并跳登录
            requestsQueue = []
            handleUnauthorized('登录过期，请重新登录')
            return Promise.reject(refreshError)
        } finally {
            isRefreshing = false
        }
    } else {
        // 如果正在刷新中，返回一个 Promise，将 resolve 存入队列
        return new Promise((resolve) => {
            requestsQueue.push((token: string) => {
                config.headers.Authorization = `Bearer ${token}`
                resolve(service(config))
            })
        })
    }
}

export default service