import request from './request'

export interface UserDeptVO {
  deptId: string
  deptName: string
  deptType: string
  companyId: string | null
  companyName: string | null
  isPrimary: number
  position: string
}

export interface UserVO {
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
  position: string
  createTime: string
}

export interface UserDetailVO {
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
  position: string
  depts: UserDeptVO[]
  roleIds: string[]
  roles: string[]
  permissions: string[]
  createTime: string
  updateTime: string
}

export interface UserQueryDTO {
  username?: string
  nickname?: string
  phone?: string
  status?: number
  deptId?: string
  current?: number
  size?: number
}

export interface UserCreateDTO {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  gender?: number
  avatar?: string
  roleIds?: string[]
}

export interface UserUpdateDTO {
  username?: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  gender?: number
  status?: number
  roleIds?: string[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getUserPage = (params: UserQueryDTO): Promise<PageResult<UserVO>> => {
  return request.get<any, PageResult<UserVO>>('/system/users', { params })
}

export const getUserDetail = (id: string): Promise<UserDetailVO> => {
  return request.get<any, UserDetailVO>(`/system/users/${id}`)
}

export const createUser = (data: UserCreateDTO): Promise<UserVO> => {
  return request.post<any, UserVO>('/system/users', data)
}

export const updateUser = (id: string, data: UserUpdateDTO): Promise<UserVO> => {
  return request.put<any, UserVO>(`/system/users/${id}`, data)
}

export const deleteUser = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/users/${id}`)
}

export const resetPassword = (id: string, newPassword: string): Promise<void> => {
  return request.put<any, void>(`/system/users/${id}/reset-password`, newPassword)
}
