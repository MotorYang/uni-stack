import request from './request'

export interface LoginData {
  username: string
  password: string
}

export interface UserInfo {
  username: string
  nickName: string
  avatar: string
  email: string
  phone?: string
  department?: string
  position?: string
  github?: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export type UpdateUserInfoPayload = Partial<UserInfo>

export const login = (data: LoginData): Promise<LoginResponse> => {
  return request.post<any, LoginResponse>('/auth/login', data)
}

export const getUserInfo = (): Promise<UserInfo> => {
  return request.get<any, UserInfo>('/user/info')
}

export const updateUserInfo = (data: UpdateUserInfoPayload): Promise<UserInfo> => {
  return request.post<any, UserInfo>('/user/update', data)
}

export const logout = () => {
  return request.post('/auth/logout')
}
