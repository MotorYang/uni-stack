import request from './request'

export interface LoginData {
  username: string
  password: string
}

export interface RefreshTokenData {
  refreshToken: string
}

export interface TokenVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface UserInfo {
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  gender: number
  status: number
  deptId: string
  deptName: string
  roleIds: string[]
  roles: string[]
  permissions: string[]
  createTime: string
  updateTime: string
}

export interface UpdateUserInfoPayload {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  gender?: number
}

export const login = (data: LoginData): Promise<TokenVO> => {
  return request.post<any, TokenVO>('/auth/login', data)
}

export const refresh = (data: RefreshTokenData): Promise<TokenVO> => {
  return request.post<any, TokenVO>('/auth/refresh', data)
}

export const logout = () => {
  return request.post('/auth/logout')
}

export const getUserInfo = (userId: string): Promise<UserInfo> => {
  return request.get<any, UserInfo>(`/system/users/${userId}`)
}

export const updateUserInfo = (userId: string, data: UpdateUserInfoPayload): Promise<UserInfo> => {
  return request.put<any, UserInfo>(`/system/users/${userId}`, data)
}
