import request from './request'

export interface MenuVO {
  id: string
  parentId: string
  menuName: string
  menuType: string  // 'D' 目录, 'M' 菜单, 'B' 按钮
  path: string
  component: string
  perms: string
  icon: string
  sort: number
  visible: number
  status: number
  createTime: string
  children?: MenuVO[]
}

export interface MenuQueryDTO {
  menuName?: string
  menuType?: string
  status?: number
}

export interface MenuCreateDTO {
  parentId?: string
  menuName: string
  menuType: string
  path?: string
  component?: string
  perms?: string
  icon?: string
  sort?: number
}

export interface MenuUpdateDTO {
  parentId?: string
  menuName?: string
  menuType?: string
  path?: string
  component?: string
  perms?: string
  icon?: string
  sort?: number
  visible?: number
  status?: number
}

/**
 * 查询菜单列表（树形）
 */
export const getMenuTree = (params?: MenuQueryDTO): Promise<MenuVO[]> => {
  return request.get<any, MenuVO[]>('/system/menus', { params })
}

/**
 * 获取用户菜单树
 */
export const getUserMenuTree = (userId: string): Promise<MenuVO[]> => {
  return request.get<any, MenuVO[]>(`/system/menus/user/${userId}`)
}

/**
 * 获取菜单详情
 */
export const getMenuDetail = (id: string): Promise<MenuVO> => {
  return request.get<any, MenuVO>(`/system/menus/${id}`)
}

/**
 * 创建菜单
 */
export const createMenu = (data: MenuCreateDTO): Promise<MenuVO> => {
  return request.post<any, MenuVO>('/system/menus', data)
}

/**
 * 更新菜单
 */
export const updateMenu = (id: string, data: MenuUpdateDTO): Promise<MenuVO> => {
  return request.put<any, MenuVO>(`/system/menus/${id}`, data)
}

/**
 * 删除菜单
 */
export const deleteMenu = (id: string): Promise<void> => {
  return request.delete<any, void>(`/system/menus/${id}`)
}

/**
 * 获取角色菜单树
 */
export const getRoleMenuTree = (roleId: string): Promise<MenuVO[]> => {
  return request.get<any, MenuVO[]>(`/system/menus/role/${roleId}`)
}
