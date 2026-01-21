import request from './request'

export interface PermissionVO {
  id: string
  permName: string
  permCode: string
  description: string | null
  sort: number
  status: number  // 0=正常, 1=停用
  createTime: string
}

export interface PermissionQueryDTO {
  permName?: string
  permCode?: string
  status?: number
}

export interface PermissionCreateDTO {
  permName: string
  permCode: string
  description?: string
  sort?: number
}

export interface PermissionUpdateDTO {
  permName: string
  permCode: string
  description?: string
  sort?: number
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

/**
 * 分页查询权限列表
 */
export const getPermissionPage = (
  pageNum: number = 1,
  pageSize: number = 10,
  params?: PermissionQueryDTO
): Promise<PageResult<PermissionVO>> => {
  return request.get<any, PageResult<PermissionVO>>('/system/permissions', {
    params: { pageNum, pageSize, ...params }
  })
}

/**
 * 查询所有权限列表
 */
export const getPermissionList = (params?: PermissionQueryDTO): Promise<PermissionVO[]> => {
  return request.get<any, PermissionVO[]>('/system/permissions/list', { params })
}

/**
 * 获取权限详情
 */
export const getPermissionDetail = (id: string): Promise<PermissionVO> => {
  return request.get<any, PermissionVO>(`/system/permissions/${id}`)
}

/**
 * 创建权限
 */
export const createPermission = (data: PermissionCreateDTO): Promise<string> => {
  return request.post<any, string>('/system/permissions', data)
}

/**
 * 更新权限
 */
export const updatePermission = (id: string, data: PermissionUpdateDTO): Promise<void> => {
  return request.put<any, void>(`/system/permissions/${id}`, data)
}

/**
 * 删除权限
 */
export const deletePermission = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/permissions/${id}`)
}

/**
 * 批量删除权限
 */
export const deletePermissionBatch = (ids: string[]): Promise<void> => {
  return request.delete<any, void>('/system/permissions/batch', { data: ids })
}

/**
 * 获取权限关联的资源ID列表
 */
export const getPermissionResourceIds = (permissionId: string): Promise<string[]> => {
  return request.get<any, string[]>(`/system/permissions/${permissionId}/resources`)
}

/**
 * 分配资源给权限
 */
export const assignResourcesToPermission = (permissionId: string, resourceIds: string[]): Promise<void> => {
  return request.post<any, void>(`/system/permissions/${permissionId}/resources`, resourceIds)
}
