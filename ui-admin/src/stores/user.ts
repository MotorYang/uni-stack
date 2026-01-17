import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  login as loginApi,
  getUserInfo as getUserInfoApi,
  updateUserInfo as updateUserInfoApi,
  type LoginData,
  type UserInfo,
  type UpdateUserInfoPayload
} from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = null
    localStorage.removeItem('token')
  }

  const login = async (loginForm: LoginData) => {
    try {
      const res = await loginApi(loginForm)
      setToken(res.token)
      userInfo.value = res.user
      return true
    } catch (error) {
      throw error
    }
  }

  const getUserInfo = async () => {
    try {
      const res = await getUserInfoApi()
      userInfo.value = res
      return res
    } catch (error) {
      throw error
    }
  }

  const updateUserInfo = async (payload: UpdateUserInfoPayload) => {
    try {
      const res = await updateUserInfoApi(payload)
      userInfo.value = res
      return res
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    clearToken()
    userInfo.value = null
  }

  return {
    token,
    userInfo,
    setToken,
    clearToken,
    login,
    getUserInfo,
    logout,
    updateUserInfo
  }
})
