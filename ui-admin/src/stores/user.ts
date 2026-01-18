import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  login as loginApi,
  refresh as refreshApi,
  logout as logoutApi,
  getUserInfo as getUserInfoApi,
  updateUserInfo as updateUserInfoApi,
  type LoginData,
  type RefreshTokenData,
  type UserInfo,
  type UpdateUserInfoPayload
} from '@/api/auth'
import { getUserIdFromToken } from '@/utils/jwt'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userInfo = ref<UserInfo | null>(null)

  const setTokens = (accessToken: string, newRefreshToken: string) => {
    token.value = accessToken
    refreshToken.value = newRefreshToken
    localStorage.setItem('token', accessToken)
    localStorage.setItem('refreshToken', newRefreshToken)
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = null
    refreshToken.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  const login = async (loginForm: LoginData) => {
    try {
      const res = await loginApi(loginForm)
      setTokens(res.accessToken, res.refreshToken)
      return true
    } catch (error) {
      throw error
    }
  }

  const refresh = async () => {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }
    try {
      const data: RefreshTokenData = { refreshToken: refreshToken.value }
      const res = await refreshApi(data)
      setTokens(res.accessToken, res.refreshToken)
      return true
    } catch (error) {
      clearToken()
      throw error
    }
  }

  const getUserInfo = async () => {
    if (!token.value) {
      throw new Error('No token available')
    }
    const userId = getUserIdFromToken(token.value)
    if (!userId) {
      throw new Error('Invalid token: no userId')
    }
    try {
      const res = await getUserInfoApi(userId)
      userInfo.value = res
      return res
    } catch (error) {
      throw error
    }
  }

  const updateUserInfo = async (payload: UpdateUserInfoPayload) => {
    if (!token.value) {
      throw new Error('No token available')
    }
    const userId = getUserIdFromToken(token.value)
    if (!userId) {
      throw new Error('Invalid token: no userId')
    }
    try {
      const res = await updateUserInfoApi(userId, payload)
      userInfo.value = res
      return res
    } catch (error) {
      throw error
    }
  }

  const logout = async () => {
    try {
      await logoutApi()
    } finally {
      clearToken()
      userInfo.value = null
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    setToken,
    setTokens,
    clearToken,
    login,
    refresh,
    getUserInfo,
    logout,
    updateUserInfo
  }
})
