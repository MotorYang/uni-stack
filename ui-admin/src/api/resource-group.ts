import request from './request'

export interface ResourceGroupVO {
  id: string
  resGroupName: string
  resGroupCode: string
  description: string | null
  serviceName: string | null
  sort: number
  status: number  // 0=正常, 1=停用
  createTime: string
  resourceCount: number
}

export interface ResourceGroupQueryDTO {
  resGroupName?: string
  resGroupCode?: string
  serviceName?: string
  status?: number
}

export interface ResourceGroupCreateDTO {
  resGroupName: string
  resGroupCode: string
  description?: string
  serviceName?: string
  sort?: number
}

export interface ResourceGroupUpdateDTO {
  resGroupName?: string
  description?: string
  serviceName?: string
  sort?: number
  status?: number
}

/**
 * 查询资源组列表
 */
export const getResourceGroupList = (params?: ResourceGroupQueryDTO): Promise<ResourceGroupVO[]> => {
  return request.get<any, ResourceGroupVO[]>('/system/resource-groups', { params })
}

/**
 * 获取资源组详情
 */
export const getResourceGroupDetail = (id: string): Promise<ResourceGroupVO> => {
  return request.get<any, ResourceGroupVO>(`/system/resource-groups/${id}`)
}

/**
 * 创建资源组
 */
export const createResourceGroup = (data: ResourceGroupCreateDTO): Promise<string> => {
  return request.post<any, string>('/system/resource-groups', data)
}

/**
 * 更新资源组
 */
export const updateResourceGroup = (id: string, data: ResourceGroupUpdateDTO): Promise<void> => {
  return request.put<any, void>(`/system/resource-groups/${id}`, data)
}

/**
 * 删除资源组
 */
export const deleteResourceGroup = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/resource-groups/${id}`)
}
