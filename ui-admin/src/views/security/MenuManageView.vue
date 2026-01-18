<template>
  <div class="p-6 animate-enter">
    <div class="flex gap-6 h-[calc(100vh-140px)]">
      <!-- 左侧菜单树 -->
      <div class="glass-card !p-4 w-80 flex-shrink-0 flex flex-col">
        <!-- 搜索框 -->
        <div class="mb-4">
          <n-input
            v-model:value="searchKeyword"
            placeholder="搜索菜单..."
            clearable
          >
            <template #prefix>
              <n-icon><SearchOutline /></n-icon>
            </template>
          </n-input>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-2 mb-4">
          <n-button type="primary" size="small" class="flex-1" @click="handleAdd(null)">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增
          </n-button>
          <n-button size="small" @click="handleExpandAll">
            {{ isExpandAll ? '折叠' : '展开' }}
          </n-button>
          <n-button size="small" @click="loadMenuList">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
          </n-button>
        </div>

        <!-- 菜单树 -->
        <div class="flex-1 overflow-y-auto menu-tree-scroll">
          <n-spin :show="loading">
            <n-tree
              :data="filteredTreeData"
              :pattern="searchKeyword"
              :default-expand-all="isExpandAll"
              :node-props="nodeProps"
              :render-label="renderLabel"
              :render-suffix="renderSuffix"
              block-line
              selectable
              :selected-keys="selectedKeys"
              @update:selected-keys="handleSelectMenu"
            />
          </n-spin>
        </div>
      </div>

      <!-- 右侧详情区域 -->
      <div class="glass-card !p-6 flex-1 flex flex-col overflow-hidden">
        <template v-if="selectedMenu">
          <!-- 头部信息 -->
          <div class="flex justify-between items-start mb-6">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500/20 to-purple-500/20 flex items-center justify-center">
                <n-icon :size="24" :component="getIconComponent(selectedMenu.icon)" class="text-blue-500" />
              </div>
              <div>
                <h2 class="text-xl font-bold text-text-main">{{ selectedMenu.menuName }}</h2>
                <div class="flex items-center gap-2 mt-1">
                  <n-tag :type="menuTypeConfig[selectedMenu.menuType].type" size="small">
                    {{ menuTypeConfig[selectedMenu.menuType].label }}
                  </n-tag>
                  <n-tag :type="selectedMenu.status === 0 ? 'success' : 'error'" size="small">
                    {{ selectedMenu.status === 0 ? '正常' : '禁用' }}
                  </n-tag>
                  <n-tag :type="selectedMenu.visible === 0 ? 'success' : 'default'" size="small">
                    {{ selectedMenu.visible === 0 ? '显示' : '隐藏' }}
                  </n-tag>
                </div>
              </div>
            </div>
            <n-space>
              <n-button v-if="selectedMenu.menuType !== 'B'" type="info" size="small" @click="handleAdd(selectedMenu)">
                <template #icon><n-icon><AddOutline /></n-icon></template>
                新增子级
              </n-button>
              <n-button type="primary" size="small" @click="handleEdit(selectedMenu)">
                <template #icon><n-icon><CreateOutline /></n-icon></template>
                编辑
              </n-button>
              <n-popconfirm @positive-click="handleDelete(selectedMenu)">
                <template #trigger>
                  <n-button type="error" size="small">
                    <template #icon><n-icon><TrashOutline /></n-icon></template>
                    删除
                  </n-button>
                </template>
                确定要删除菜单 "{{ selectedMenu.menuName }}" 吗？
              </n-popconfirm>
            </n-space>
          </div>

          <!-- 详细信息 -->
          <div class="flex-1 overflow-y-auto">
            <n-descriptions :column="2" label-placement="left" bordered size="small">
              <n-descriptions-item label="菜单ID">
                <n-text code>{{ selectedMenu.id }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item label="上级菜单">
                {{ getParentMenuName(selectedMenu.parentId) }}
              </n-descriptions-item>
              <n-descriptions-item label="菜单名称">
                {{ selectedMenu.menuName }}
              </n-descriptions-item>
              <n-descriptions-item label="菜单类型">
                <n-tag :type="menuTypeConfig[selectedMenu.menuType].type" size="small">
                  {{ menuTypeConfig[selectedMenu.menuType].label }}
                </n-tag>
              </n-descriptions-item>
              <n-descriptions-item label="图标">
                <div class="flex items-center gap-2" v-if="selectedMenu.icon">
                  <n-icon :component="getIconComponent(selectedMenu.icon)" />
                  <n-text code>{{ selectedMenu.icon }}</n-text>
                </div>
                <span v-else class="text-gray-400">-</span>
              </n-descriptions-item>
              <n-descriptions-item label="排序">
                {{ selectedMenu.sort }}
              </n-descriptions-item>
              <n-descriptions-item label="路由地址" :span="2">
                <n-text code v-if="selectedMenu.path">{{ selectedMenu.path }}</n-text>
                <span v-else class="text-gray-400">-</span>
              </n-descriptions-item>
              <n-descriptions-item label="组件路径" :span="2">
                <n-text code v-if="selectedMenu.component">{{ selectedMenu.component }}</n-text>
                <span v-else class="text-gray-400">-</span>
              </n-descriptions-item>
              <n-descriptions-item label="权限标识" :span="2">
                <n-text code v-if="selectedMenu.perms">{{ selectedMenu.perms }}</n-text>
                <span v-else class="text-gray-400">-</span>
              </n-descriptions-item>
              <n-descriptions-item label="显示状态">
                <n-tag :type="selectedMenu.visible === 0 ? 'success' : 'default'" size="small">
                  {{ selectedMenu.visible === 0 ? '显示' : '隐藏' }}
                </n-tag>
              </n-descriptions-item>
              <n-descriptions-item label="菜单状态">
                <n-tag :type="selectedMenu.status === 0 ? 'success' : 'error'" size="small">
                  {{ selectedMenu.status === 0 ? '正常' : '禁用' }}
                </n-tag>
              </n-descriptions-item>
              <n-descriptions-item label="创建时间" :span="2">
                {{ selectedMenu.createTime?.replace('T', ' ').substring(0, 19) || '-' }}
              </n-descriptions-item>
            </n-descriptions>

            <!-- 子菜单列表 -->
            <div v-if="selectedMenu.children && selectedMenu.children.length > 0" class="mt-6">
              <h3 class="text-base font-semibold text-text-main mb-3">子菜单 ({{ selectedMenu.children.length }})</h3>
              <div class="grid grid-cols-2 gap-3">
                <div
                  v-for="child in selectedMenu.children"
                  :key="child.id"
                  class="glass-card-sm !p-3 cursor-pointer hover:border-blue-400/50 transition-all"
                  @click="handleSelectMenu([child.id])"
                >
                  <div class="flex items-center gap-2">
                    <n-icon :component="getIconComponent(child.icon)" class="text-gray-500" />
                    <span class="font-medium text-text-main">{{ child.menuName }}</span>
                    <n-tag :type="menuTypeConfig[child.menuType].type" size="tiny">
                      {{ menuTypeConfig[child.menuType].label }}
                    </n-tag>
                  </div>
                  <div class="text-xs text-gray-500 mt-1 truncate">
                    {{ child.path || child.perms || '-' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 未选择状态 -->
        <template v-else>
          <div class="flex-1 flex flex-col items-center justify-center text-gray-400">
            <n-icon size="64" class="mb-4 opacity-30">
              <MenuOutline />
            </n-icon>
            <p class="text-lg">请从左侧选择一个菜单</p>
            <p class="text-sm mt-2">或点击「新增」按钮创建菜单</p>
          </div>
        </template>
      </div>
    </div>

    <!-- 新增/编辑菜单弹窗 -->
    <n-modal
      v-model:show="showModal"
      :title="modalTitle"
      preset="card"
      style="width: 650px"
      :mask-closable="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100"
      >
        <n-form-item label="上级菜单" path="parentId">
          <n-tree-select
            v-model:value="formData.parentId"
            :options="menuTreeOptions"
            placeholder="请选择上级菜单"
            clearable
            default-expand-all
          />
        </n-form-item>
        <n-form-item label="菜单类型" path="menuType">
          <n-radio-group v-model:value="formData.menuType">
            <n-space>
              <n-radio value="M">目录</n-radio>
              <n-radio value="C">菜单</n-radio>
              <n-radio value="B">按钮</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="菜单名称" path="menuName">
          <n-input v-model:value="formData.menuName" placeholder="请输入菜单名称" />
        </n-form-item>
        <n-form-item v-if="formData.menuType !== 'B'" label="菜单图标" path="icon">
          <IconPicker v-model="formData.icon" placeholder="请输入图标名称，如 HomeOutline" />
        </n-form-item>
        <n-form-item v-if="formData.menuType !== 'B'" label="路由地址" path="path">
          <n-input v-model:value="formData.path" placeholder="请输入路由地址，如 /security/user" />
        </n-form-item>
        <n-form-item v-if="formData.menuType === 'C'" label="组件路径" path="component">
          <n-input v-model:value="formData.component" placeholder="请输入组件路径，如 security/user/index" />
        </n-form-item>
        <n-form-item v-if="formData.menuType !== 'M'" label="权限标识" path="perms">
          <n-input v-model:value="formData.perms" placeholder="请输入权限标识，如 security:user:view" />
        </n-form-item>
        <n-form-item label="显示排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" :max="999" class="!w-full" />
        </n-form-item>
        <n-grid :cols="2" :x-gap="24">
          <n-gi>
            <n-form-item label="显示状态" path="visible">
              <n-radio-group v-model:value="formData.visible">
                <n-space>
                  <n-radio :value="0">显示</n-radio>
                  <n-radio :value="1">隐藏</n-radio>
                </n-space>
              </n-radio-group>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="菜单状态" path="status">
              <n-radio-group v-model:value="formData.status">
                <n-space>
                  <n-radio :value="0">正常</n-radio>
                  <n-radio :value="1">禁用</n-radio>
                </n-space>
              </n-radio-group>
            </n-form-item>
          </n-gi>
        </n-grid>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h, type Component, type VNodeChild } from 'vue'
import {
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NButton,
  NSpace,
  NModal,
  NRadio,
  NRadioGroup,
  NIcon,
  NTag,
  NTree,
  NTreeSelect,
  NGrid,
  NGi,
  NDescriptions,
  NDescriptionsItem,
  NText,
  NSpin,
  NPopconfirm,
  useMessage,
  type FormInst,
  type FormRules,
  type TreeOption,
  type TreeSelectOption
} from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  CreateOutline,
  TrashOutline,
  MenuOutline,
  EllipseOutline
} from '@vicons/ionicons5'
import IconPicker from '@/components/IconPicker.vue'
import * as Ionicons from '@vicons/ionicons5'
import {
  getMenuTree,
  createMenu,
  updateMenu,
  deleteMenu,
  type MenuVO,
  type MenuCreateDTO,
  type MenuUpdateDTO
} from '@/api/menu'

const message = useMessage()

// 菜单类型配置
const menuTypeConfig: Record<MenuVO['menuType'], { label: string; type: 'info' | 'success' | 'warning' }> = {
  'M': { label: '目录', type: 'info' },
  'C': { label: '菜单', type: 'success' },
  'B': { label: '按钮', type: 'warning' }
}

// 搜索关键字
const searchKeyword = ref('')
const isExpandAll = ref(true)
const loading = ref(false)

// 菜单数据
const menuList = ref<MenuVO[]>([])
const selectedKeys = ref<string[]>([])
const selectedMenu = ref<MenuVO | null>(null)

// 将菜单数据转换为树形结构
const treeData = computed<TreeOption[]>(() => {
  return transformToTreeData(menuList.value)
})

// 过滤后的树数据
const filteredTreeData = computed<TreeOption[]>(() => {
  if (!searchKeyword.value) return treeData.value
  return filterTree(treeData.value, searchKeyword.value.toLowerCase())
})

// 递归过滤树
function filterTree(nodes: TreeOption[], keyword: string): TreeOption[] {
  return nodes.reduce((acc: TreeOption[], node) => {
    const label = (node.label as string)?.toLowerCase() || ''
    const matchSelf = label.includes(keyword)
    let children: TreeOption[] | undefined

    if (node.children) {
      children = filterTree(node.children, keyword)
    }

    if (matchSelf || (children && children.length > 0)) {
      acc.push({
        ...node,
        children: children && children.length > 0 ? children : node.children
      })
    }
    return acc
  }, [])
}

// 转换菜单数据为树形结构
function transformToTreeData(menus: MenuVO[]): TreeOption[] {
  return menus.map(menu => ({
    key: menu.id,
    label: menu.menuName,
    children: menu.children ? transformToTreeData(menu.children) : undefined,
    menuData: menu
  }))
}

// 菜单树选择器选项
const menuTreeOptions = computed<TreeSelectOption[]>(() => {
  const options: TreeSelectOption[] = [
    { key: '0', label: '根目录' }
  ]
  const buildOptions = (menus: MenuVO[]): TreeSelectOption[] => {
    return menus
      .filter(menu => menu.menuType !== 'B')
      .map(menu => ({
        key: menu.id,
        label: menu.menuName,
        children: menu.children ? buildOptions(menu.children) : undefined
      }))
  }
  options.push(...buildOptions(menuList.value))
  return options
})

// 获取图标组件
function getIconComponent(iconName: string | null): Component {
  if (!iconName) return EllipseOutline
  const icon = (Ionicons as Record<string, Component | undefined>)[iconName]
  if (icon) return icon
  return EllipseOutline
}

// 获取父菜单名称
function getParentMenuName(parentId: string): string {
  if (parentId === '0') return '根目录'
  const findMenu = (menus: MenuVO[]): MenuVO | null => {
    for (const menu of menus) {
      if (menu.id === parentId) return menu
      if (menu.children) {
        const found = findMenu(menu.children)
        if (found) return found
      }
    }
    return null
  }
  const parent = findMenu(menuList.value)
  return parent?.menuName || '-'
}

// 查找菜单
function findMenuById(menus: MenuVO[], id?: string): MenuVO | null {
  if (!id) return null
  for (const menu of menus) {
    if (menu.id === id) return menu
    if (menu.children) {
      const found = findMenuById(menu.children, id)
      if (found) return found
    }
  }
  return null
}

// 树节点属性
const nodeProps = () => {
  return {
    class: 'menu-tree-node'
  }
}

// 渲染树节点标签
const renderLabel = ({ option }: { option: TreeOption }): VNodeChild => {
  const menu = (option as any).menuData as MenuVO
  return h('div', { class: 'flex items-center gap-2 py-1' }, [
    h(NIcon, {
      size: 16,
      component: getIconComponent(menu?.icon),
      class: 'text-gray-500'
    }),
    h('span', { class: 'truncate' }, option.label as string)
  ])
}

// 渲染树节点后缀（徽标）
const renderSuffix = ({ option }: { option: TreeOption }): VNodeChild => {
  const menu = (option as any).menuData as MenuVO
  if (!menu) return null

  const badges: VNodeChild[] = []

  // 类型徽标
  badges.push(h(NTag, {
    size: 'tiny',
    type: menuTypeConfig[menu.menuType]?.type || 'default',
    bordered: false,
    round: true
  }, { default: () => menuTypeConfig[menu.menuType]?.label || menu.menuType }))

  // 状态徽标
  if (menu.status === 1) {
    badges.push(h(NTag, {
      size: 'tiny',
      type: 'error',
      bordered: false,
      round: true
    }, { default: () => '禁用' }))
  }

  // 隐藏徽标
  if (menu.visible === 1) {
    badges.push(h(NTag, {
      size: 'tiny',
      type: 'default',
      bordered: false,
      round: true
    }, { default: () => '隐藏' }))
  }

  return h('div', { class: 'flex items-center gap-1' }, badges)
}

// 选择菜单
const handleSelectMenu = (keys: string[]) => {
  selectedKeys.value = keys
  if (keys.length > 0) {
    selectedMenu.value = findMenuById(menuList.value, keys[0])
  } else {
    selectedMenu.value = null
  }
}

// 加载菜单列表
const loadMenuList = async () => {
  loading.value = true
  try {
    menuList.value = await getMenuTree()
    // 如果有选中的菜单，刷新选中状态
    if (selectedKeys.value.length > 0) {
      selectedMenu.value = findMenuById(menuList.value, selectedKeys.value[0])
    }
  } catch (error) {
    message.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 展开/折叠全部
const handleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
}

// 弹窗相关
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const editingMenuId = ref<string>('')

const formData = reactive<MenuCreateDTO & MenuUpdateDTO>({
  parentId: '0',
  menuName: '',
  menuType: 'M',
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 0,
  visible: 0,
  status: 0
})

const formRules: FormRules = {
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 50, message: '菜单名称长度为 2-50 个字符', trigger: 'blur' }
  ],
  menuType: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
  ],
  path: [
    {
      validator: (_rule, value) => {
        if (formData.menuType !== 'B' && !value) {
          return new Error('请输入路由地址')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

// 新增菜单
const handleAdd = (parent: MenuVO | null) => {
  isEdit.value = false
  editingMenuId.value = ''
  Object.assign(formData, {
    parentId: parent?.id || '0',
    menuName: '',
    menuType: parent ? 'C' : 'M',
    path: '',
    component: '',
    perms: '',
    icon: '',
    sort: 0,
    visible: 0,
    status: 0
  })
  showModal.value = true
}

// 编辑菜单
const handleEdit = (menu: MenuVO) => {
  isEdit.value = true
  editingMenuId.value = menu.id
  Object.assign(formData, {
    parentId: menu.parentId,
    menuName: menu.menuName,
    menuType: menu.menuType,
    path: menu.path || '',
    component: menu.component || '',
    perms: menu.perms || '',
    icon: menu.icon || '',
    sort: menu.sort,
    visible: menu.visible,
    status: menu.status
  })
  showModal.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      const updateData: MenuUpdateDTO = {
        parentId: formData.parentId,
        menuName: formData.menuName,
        menuType: formData.menuType,
        path: formData.path || undefined,
        component: formData.component || undefined,
        perms: formData.perms || undefined,
        icon: formData.icon || undefined,
        sort: formData.sort,
        visible: formData.visible,
        status: formData.status
      }
      await updateMenu(editingMenuId.value, updateData)
      message.success('更新成功')
    } else {
      const createData: MenuCreateDTO = {
        parentId: formData.parentId!,
        menuName: formData.menuName!,
        menuType: formData.menuType as 'M' | 'C' | 'B',
        path: formData.path || undefined,
        component: formData.component || undefined,
        perms: formData.perms || undefined,
        icon: formData.icon || undefined,
        sort: formData.sort,
        visible: formData.visible,
        status: formData.status
      }
      await createMenu(createData)
      message.success('创建成功')
    }
    showModal.value = false
    loadMenuList()
  } catch (error) {
    message.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 删除菜单
const handleDelete = async (menu: MenuVO) => {
  if (menu.children && menu.children.length > 0) {
    message.warning('请先删除子菜单')
    return
  }
  try {
    await deleteMenu(menu.id)
    message.success('删除成功')
    selectedMenu.value = null
    selectedKeys.value = []
    loadMenuList()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  loadMenuList()
})
</script>

<style scoped>
.menu-tree-scroll {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.menu-tree-scroll::-webkit-scrollbar {
  display: none;
}

/* 毛玻璃小卡片 */
.glass-card-sm {
  background: var(--glass-bg-light);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--glass-border-subtle);
  border-radius: 12px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card-sm:hover {
  background: var(--glass-bg);
  border-color: var(--glass-border);
}

/* 树节点样式 */
:deep(.n-tree-node) {
  border-radius: 8px;
  margin: 2px 0;
}

:deep(.n-tree-node:hover) {
  background: var(--glass-bg-light);
}

:deep(.n-tree-node--selected) {
  background: rgba(59, 130, 246, 0.15) !important;
}

:deep(.n-tree-node-content) {
  padding: 4px 8px !important;
}

:deep(.n-tree-node-switcher) {
  width: 20px !important;
}
</style>
