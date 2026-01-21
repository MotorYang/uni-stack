import request from './request'

export interface ResourceVO {
  id: string
  groupId: string
  resName: string
  resType: 'API' | 'BUTTON'
  resPath: string
  resCode: string
  resMethod: string
  description: string | null
  sort: number
  status: number  // 0=正常, 1=停用
  createTime: string
}

export interface ResourceQueryDTO {
  groupId?: string
  resName?: string
  resType?: string
  resCode?: string
  status?: number
  current?: number
  size?: number
}

export interface ResourceCreateDTO {
  groupId: string
  resName: string
  resType: 'API' | 'BUTTON'
  resPath: string
  resCode: string
  resMethod?: string
  description?: string
  sort?: number
}

export interface ResourceUpdateDTO {
  resName?: string
  resType?: 'API' | 'BUTTON'
  resPath?: string
  resMethod?: string
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
 * 分页查询资源列表
 */
export const getResourcePage = (params: ResourceQueryDTO): Promise<PageResult<ResourceVO>> => {
  return request.get<any, PageResult<ResourceVO>>('/system/resources', { params })
}

/**
 * 获取资源详情
 */
export const getResourceDetail = (id: string): Promise<ResourceVO> => {
  return request.get<any, ResourceVO>(`/system/resources/${id}`)
}

/**
 * 创建资源
 */
export const createResource = (data: ResourceCreateDTO): Promise<string> => {
  return request.post<any, string>('/system/resources', data)
}

/**
 * 更新资源
 */
export const updateResource = (id: string, data: ResourceUpdateDTO): Promise<void> => {
  return request.put<any, void>(`/system/resources/${id}`, data)
}

/**
 * 删除资源
 */
export const deleteResource = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/resources/${id}`)
}

/**
 * 批量删除资源
 */
export const deleteResourceBatch = (ids: string[]): Promise<void> => {
  return request.delete<any, void>('/system/resources/batch', { data: ids })
}
