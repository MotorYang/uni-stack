import request from './request'

export interface Menu {
  label: string
  key: string
  path?: string
  icon?: string
  children?: Menu[]
}

export const getMenuList = (): Promise<Menu[]> => {
  return request.get<any, Menu[]>('/menu/list')
}
