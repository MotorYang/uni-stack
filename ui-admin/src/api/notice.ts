import request from './request'

export interface NoticeVO {
  id: string
  title: string
  type: string
  priority: string
  status: string
  target: string
  timeOption: string
  noticeTime: string | null
  createBy: string
  createTime: string
}

export interface NoticeTargetVO {
  id: string
  noticeType: string
  targetId: string
  targetName: string | null
}

export interface NoticeDetailVO {
  id: string
  title: string
  content: string
  type: string
  priority: string
  status: string
  target: string
  timeOption: string
  noticeTime: string | null
  targets: NoticeTargetVO[]
  createBy: string
  createTime: string
  updateTime: string
}

export interface NoticeQueryDTO {
  title?: string
  type?: string
  status?: string
  priority?: string
  current?: number
  size?: number
}

export interface NoticeCreateDTO {
  title: string
  content: string
  type: string
  priority?: string
  target: string
  timeOption?: string
  noticeTime?: string | null
  targetIds?: string[]
}

export interface NoticeUpdateDTO {
  title?: string
  content?: string
  type?: string
  priority?: string
  target?: string
  timeOption?: string
  noticeTime?: string | null
  targetIds?: string[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getNoticePage = (params: NoticeQueryDTO): Promise<PageResult<NoticeVO>> => {
  return request.get<any, PageResult<NoticeVO>>('/system/notices', { params })
}

export const getNoticeDetail = (id: string): Promise<NoticeDetailVO> => {
  return request.get<any, NoticeDetailVO>(`/system/notices/${id}`)
}

export const createNotice = (data: NoticeCreateDTO): Promise<NoticeVO> => {
  return request.post<any, NoticeVO>('/system/notices', data)
}

export const updateNotice = (id: string, data: NoticeUpdateDTO): Promise<NoticeVO> => {
  return request.put<any, NoticeVO>(`/system/notices/${id}`, data)
}

export const deleteNotice = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/notices/${id}`)
}

export const publishNotice = (id: string): Promise<void> => {
  return request.put<any, void>(`/system/notices/${id}/publish`)
}

export const revokeNotice = (id: string): Promise<void> => {
  return request.put<any, void>(`/system/notices/${id}/revoke`)
}
