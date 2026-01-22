import request from './request'
import type { PageResult } from './user'

export interface RoleVO {
  id: string
  roleName: string
  roleKey: string
  sort: number
  status: number
  remark: string
  createTime: string
}

export interface RoleDetailVO {
  id: string
  roleName: string
  roleKey: string
  sort: number
  status: number
  remark: string
  menuIds: string[]
  createTime: string
  updateTime: string
}

export interface RoleQueryDTO {
  roleName?: string
  roleKey?: string
  status?: number
  current?: number
  size?: number
}

export interface RoleCreateDTO {
  roleName: string
  roleKey: string
  sort?: number
  status?: number
  remark?: string
  menuIds?: string[]
}

export interface RoleUpdateDTO {
  roleName?: string
  roleKey?: string
  sort?: number
  status?: number
  remark?: string
  menuIds?: string[]
}

export const getRolePage = (params: RoleQueryDTO): Promise<PageResult<RoleVO>> => {
  return request.get<any, PageResult<RoleVO>>('/system/roles', { params })
}

export const getRoleDetail = (id: string): Promise<RoleDetailVO> => {
  return request.get<any, RoleDetailVO>(`/system/roles/${id}`)
}

export const createRole = (data: RoleCreateDTO): Promise<RoleVO> => {
  return request.post<any, RoleVO>('/system/roles', data)
}

export const updateRole = (id: string, data: RoleUpdateDTO): Promise<RoleVO> => {
  return request.put<any, RoleVO>(`/system/roles/${id}`, data)
}

export const deleteRole = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/roles/${id}`)
}

export const getAllRoles = (): Promise<RoleVO[]> => {
  return request.get<any, RoleVO[]>('/system/roles/list')
}

// 角色用户相关接口
export interface RoleUserVO {
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  createTime: string
}

export interface RoleUserQueryDTO {
  roleId: string
  username?: string
  nickname?: string
  current?: number
  size?: number
}

// 获取角色下的用户列表
export const getRoleUsers = (params: RoleUserQueryDTO): Promise<PageResult<RoleUserVO>> => {
  return request.get<any, PageResult<RoleUserVO>>(`/system/roles/${params.roleId}/users`, { params })
}

// 获取未分配该角色的用户列表
export const getUnassignedUsers = (params: RoleUserQueryDTO): Promise<PageResult<RoleUserVO>> => {
  return request.get<any, PageResult<RoleUserVO>>(`/system/roles/${params.roleId}/users/unassigned`, { params })
}

// 添加用户到角色
export const addUsersToRole = (roleId: string, userIds: string[]): Promise<void> => {
  return request.post<any, void>(`/system/roles/${roleId}/users`, userIds)
}

// 从角色移除用户
export const removeUserFromRole = (roleId: string, userId: string): Promise<void> => {
  return request.delete<any, void>(`/system/roles/${roleId}/users/${userId}`)
}

// 批量从角色移除用户
export const removeUsersFromRole = (roleId: string, userIds: string[]): Promise<void> => {
  return request.delete<any, void>(`/system/roles/${roleId}/users`, { data: userIds })
}

// 权限相关类型
export interface PermissionVO {
  id: string
  permName: string
  permCode: string
  description: string
  sort: number
  status: number
  createTime: string
  resourceIds: string[]
}

// 获取角色关联的权限列表
export const getRolePermissions = (roleId: string): Promise<PermissionVO[]> => {
  return request.get<any, PermissionVO[]>(`/system/roles/${roleId}/permissions`)
}

// 批量添加权限到角色
export const addPermissionsToRole = (roleId: string, permissionIds: string[]): Promise<void> => {
  return request.post<any, void>(`/system/roles/${roleId}/permissions`, permissionIds)
}

// 从角色移除权限
export const removePermissionFromRole = (roleId: string, permissionId: string): Promise<void> => {
  return request.delete<any, void>(`/system/roles/${roleId}/permissions/${permissionId}`)
}

// 获取所有权限列表（用于权限选择器）
export const getAllPermissions = (): Promise<PermissionVO[]> => {
  return request.get<any, PermissionVO[]>('/system/permissions/list')
}
