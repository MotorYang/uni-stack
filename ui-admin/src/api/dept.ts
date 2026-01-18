import request from './request'

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
  createTime: string
  children?: DeptVO[] | null
}

export interface DeptQueryDTO {
  deptName?: string
  status?: number
}

export interface DeptCreateDTO {
  parentId?: string
  deptName: string
  sort?: number
  leader?: string
  phone?: string
  email?: string
}

export interface DeptUpdateDTO {
  parentId?: string
  deptName?: string
  sort?: number
  leader?: string
  phone?: string
  email?: string
  status?: number
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

