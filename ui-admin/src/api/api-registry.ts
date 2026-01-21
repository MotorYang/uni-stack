import request from './request'

export interface ApiInfo {
  path: string
  method: string
  summary: string
  description: string
  tags: string[]
}

/**
 * 获取所有已注册的服务名
 */
export const getRegisteredServices = (): Promise<string[]> => {
  return request.get<any, string[]>('/system/api-registry/services')
}

/**
 * 获取指定服务的API列表
 */
export const getServiceApis = (serviceName: string): Promise<ApiInfo[]> => {
  return request.get<any, ApiInfo[]>('/system/api-registry/apis', {
    params: { serviceName }
  })
}
