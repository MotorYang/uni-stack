import request from './request'
import type { PageResult, UserVO } from './user'

export interface DeptVO {
  id: string
  parentId: string
  ancestors: string
  deptName: string
  sort: number
  leader: string | null
  phone: string | null
  email: string | null
  status: number
  deptType: string
  createTime: string
  children?: DeptVO[] | null
}

export interface DeptQueryDTO {
  deptName?: string
  status?: number
  deptType?: string
}

export interface DeptCreateDTO {
  parentId?: string
  deptName: string
  sort?: number
  leader?: string
  phone?: string
  email?: string
  deptType?: string
}

export interface DeptUpdateDTO {
  parentId?: string
  deptName?: string
  sort?: number
  leader?: string
  phone?: string
  email?: string
  status?: number
  deptType?: string
}

export interface DeptUserQueryDTO {
  username?: string
  nickname?: string
  status?: number
  current?: number
  size?: number
}

export interface DeptAssignUsersDTO {
  userIds: string[]
  deptId: string
  position?: string
}

export interface DeptRemoveUsersDTO {
  userIds: string[]
}

export interface SetPrimaryDeptDTO {
  userId: string
  deptId: string
}

export interface SetPositionDTO {
  userId: string
  deptId: string
  position?: string
}

export const getDeptTree = (params?: DeptQueryDTO): Promise<DeptVO[]> => {
  return request.get<any, DeptVO[]>('/system/depts', { params })
}

export const getDeptDetail = (id: string): Promise<DeptVO> => {
  return request.get<any, DeptVO>(`/system/depts/${id}`)
}

export const createDept = (data: DeptCreateDTO): Promise<DeptVO> => {
  return request.post<any, DeptVO>('/system/depts', data)
}

export const updateDept = (id: string, data: DeptUpdateDTO): Promise<DeptVO> => {
  return request.put<any, DeptVO>(`/system/depts/${id}`, data)
}

export const deleteDept = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/depts/${id}`)
}

export const getDeptTreeSelect = (): Promise<DeptVO[]> => {
  return request.get<any, DeptVO[]>('/system/depts/tree-select')
}

// 部门用户相关 API

export const getDeptUsers = (deptId: string, params?: DeptUserQueryDTO): Promise<PageResult<UserVO>> => {
  return request.get<any, PageResult<UserVO>>(`/system/depts/${deptId}/users`, { params })
}

export const getDeptUnassignedUsers = (deptId: string, params?: DeptUserQueryDTO): Promise<PageResult<UserVO>> => {
  return request.get<any, PageResult<UserVO>>(`/system/depts/${deptId}/unassigned-users`, { params })
}

export const assignUsersToDept = (data: DeptAssignUsersDTO): Promise<void> => {
  return request.post<any, void>(`/system/depts/assign/users`, data)
}

export const removeUsersFromDept = (deptId: string, data: DeptRemoveUsersDTO): Promise<void> => {
  return request.delete<any, void>(`/system/depts/${deptId}/users`, { data })
}

export const setPrimaryDept = (data: SetPrimaryDeptDTO): Promise<void> => {
  return request.put<any, void>('/system/depts/users/primary', data)
}

export const setPosition = (data: SetPositionDTO): Promise<void> => {
  return request.put<any, void>('/system/depts/users/position', data)
}
