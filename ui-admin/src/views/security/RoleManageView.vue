<template>
  <div class="p-6 animate-enter">
    <div class="flex gap-6 h-[calc(100vh-140px)]">
      <div class="glass-card !p-5 w-80 flex-shrink-0 flex flex-col">
        <div class="mb-4">
          <n-form inline :model="queryParams" label-placement="left" class="flex flex-wrap gap-4">
            <n-form-item label="角色名称" :show-feedback="false">
              <n-input v-model:value="queryParams.roleName" placeholder="请输入角色名称" clearable class="!w-40" />
            </n-form-item>
            <n-form-item label="权限标识" :show-feedback="false">
              <n-input v-model:value="queryParams.roleKey" placeholder="请输入权限标识" clearable class="!w-40" />
            </n-form-item>
            <n-form-item label="状态" :show-feedback="false">
              <n-select
                v-model:value="queryParams.status"
                :options="statusOptions"
                placeholder="请选择"
                clearable
                class="!w-28"
              />
            </n-form-item>
            <n-form-item :show-feedback="false">
              <n-space>
                <n-button type="primary" size="small" @click="handleSearch">
                  <template #icon><n-icon><SearchOutline /></n-icon></template>
                  搜索
                </n-button>
                <n-button size="small" @click="handleReset">
                  <template #icon><n-icon><RefreshOutline /></n-icon></template>
                  重置
                </n-button>
              </n-space>
            </n-form-item>
          </n-form>
        </div>

        <div class="flex-1 flex flex-col">
          <div class="flex justify-between items-center mb-3">
            <div class="flex items-center gap-2">
              <h3 class="text-lg font-semibold text-text-main">角色列表</h3>
              <n-tag size="small" round :bordered="false" type="info">{{ total }} 条</n-tag>
            </div>
            <n-button type="primary" size="small" @click="handleAdd">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              新增角色
            </n-button>
          </div>
          <div class="flex-1 overflow-y-auto">
            <n-tree
              :data="roleTreeData"
              :selected-keys="selectedRoleKeys"
              block-line
              :default-expand-all="true"
              @update:selected-keys="handleRoleTreeSelect"
            />
          </div>
        </div>
      </div>

      <div class="glass-card !p-6 flex-1 flex flex-col overflow-hidden">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-semibold text-text-main">
              {{ currentRole ? currentRole.roleName : '请选择角色' }}
            </h3>
            <div v-if="currentRole" class="mt-1 text-xs text-gray-500">
              权限标识：{{ currentRole.roleKey }}
            </div>
          </div>
          <div v-if="currentRole" class="flex items-center gap-2">
            <n-tag size="small" :type="currentRole.status === 0 ? 'success' : 'error'">
              {{ currentRole.status === 0 ? '正常' : '禁用' }}
            </n-tag>
            <n-button size="small" quaternary type="primary" @click="handleEdit(currentRole)">
              编辑角色
            </n-button>
          </div>
        </div>

        <div v-if="currentRole" class="flex-1 flex flex-col overflow-hidden">
          <n-tabs v-model:value="activeTab" type="line" animated>
            <n-tab-pane name="basic" tab="基本信息">
              <div class="pt-2">
                <n-descriptions
                  :column="1"
                  label-placement="left"
                  bordered
                  size="small"
                >
                  <n-descriptions-item label="角色名称">
                    <n-text>{{ currentRole.roleName }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="权限标识">
                    <n-text>{{ currentRole.roleKey }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="排序">
                    <n-text>{{ currentRole.sort }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="状态">
                    <n-text>{{ currentRole.status === 0 ? '正常' : '禁用' }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="创建时间">
                    <n-text>{{ currentRole.createTime?.replace('T', ' ').substring(0, 19) || '-' }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="备注">
                    <n-text>{{ currentRole.remark || '-' }}</n-text>
                  </n-descriptions-item>
                </n-descriptions>
              </div>
            </n-tab-pane>
            <n-tab-pane name="users" tab="用户列表">
              <div class="flex flex-col h-full pt-2">
                <div class="flex justify-between items-center mb-2">
                  <div class="flex items-center gap-2">
                    <h4 class="font-semibold text-sm text-text-main">角色用户</h4>
                    <n-tag size="tiny" round :bordered="false" type="info">{{ userTotal }} 人</n-tag>
                  </div>
                  <div class="flex items-center gap-2">
                    <n-button
                      type="error"
                      size="tiny"
                      quaternary
                      :disabled="selectedUserIds.length === 0"
                      @click="handleBatchRemoveUsers"
                    >
                      批量移除
                    </n-button>
                    <n-button type="primary" size="tiny" quaternary @click="handleOpenAddUsers">
                      <template #icon><n-icon><PersonAddOutline /></n-icon></template>
                      添加用户
                    </n-button>
                  </div>
                </div>

                <div class="mb-2">
                  <n-form inline :model="userQueryParams" label-placement="left" class="flex flex-wrap gap-2">
                    <n-form-item :show-feedback="false">
                      <n-input
                        v-model:value="userQueryParams.username"
                        placeholder="用户名"
                        clearable
                        class="!w-32"
                      />
                    </n-form-item>
                    <n-form-item :show-feedback="false">
                      <n-input
                        v-model:value="userQueryParams.nickname"
                        placeholder="昵称"
                        clearable
                        class="!w-32"
                      />
                    </n-form-item>
                    <n-form-item :show-feedback="false">
                      <n-button type="primary" size="small" @click="handleSearchUsers">
                        <template #icon><n-icon><SearchOutline /></n-icon></template>
                        搜索
                      </n-button>
                    </n-form-item>
                  </n-form>
                </div>

                <n-data-table
                  :columns="userColumns"
                  :data="roleUserList"
                  :loading="loadingUsers"
                  :pagination="userPagination"
                  :row-key="(row: RoleUserVO) => row.id"
                  :checked-row-keys="selectedUserIds"
                  :bordered="false"
                  :single-line="false"
                  size="small"
                  style="min-height: 260px"
                  @update:page="handleUserPageChange"
                  @update:page-size="handleUserPageSizeChange"
                  @update:checked-row-keys="handleUserSelectionChange"
                />
              </div>
            </n-tab-pane>
            <n-tab-pane name="menus" tab="菜单树">
              <div class="flex flex-col h-full pt-2">
                <div class="flex items-center justify-between mb-2">
                  <h4 class="font-semibold text-sm text-text-main">角色菜单</h4>
                  <div class="flex items-center gap-2">
                    <n-button size="tiny" @click="handleResetRoleMenus">
                      重置
                    </n-button>
                    <n-button type="primary" size="tiny" :loading="savingRoleMenus" @click="handleSaveRoleMenus">
                      保存
                    </n-button>
                  </div>
                </div>
                <div class="flex-1 overflow-y-auto">
                  <n-spin :show="loadingRoleMenus">
                    <n-tree
                      :data="roleMenuTreeData"
                      block-line
                      checkable
                      :checked-keys="checkedMenuIds"
                      :default-expand-all="true"
                      @update:checked-keys="handleMenuCheckedChange"
                    />
                  </n-spin>
                </div>
              </div>
            </n-tab-pane>
          </n-tabs>
        </div>

        <div v-else class="flex-1 flex items-center justify-center text-gray-400">
          请在左侧选择一个角色查看详情
        </div>
      </div>
    </div>

    <n-modal
      v-model:show="showModal"
      :title="modalTitle"
      preset="card"
      style="width: 550px"
      :mask-closable="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="80"
      >
        <n-form-item label="角色名称" path="roleName">
          <n-input v-model:value="formData.roleName" placeholder="请输入角色名称" />
        </n-form-item>
        <n-form-item label="权限标识" path="roleKey">
          <n-input
            v-model:value="formData.roleKey"
            placeholder="请输入权限标识，如 ADMIN"
            :disabled="isEdit"
          />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" :max="999" class="!w-full" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-radio-group v-model:value="formData.status">
            <n-space>
              <n-radio :value="0">正常</n-radio>
              <n-radio :value="1">禁用</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input
            v-model:value="formData.remark"
            type="textarea"
            placeholder="请输入备注"
            :rows="3"
          />
        </n-form-item>
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

    <n-modal
      v-model:show="showAddUsersModal"
      title="添加用户到角色"
      preset="card"
      style="width: 800px"
      :mask-closable="false"
    >
      <UserPicker
        v-if="currentRole"
        :role-id="currentRole.id"
        :existing-user-ids="roleUserList.map((u) => u.id)"
        v-model:value="selectedAddUserIds"
      />

      <template #footer>
        <n-space justify="end">
          <n-button @click="showAddUsersModal = false">取消</n-button>
          <n-button
            type="primary"
            :loading="addingUsers"
            :disabled="selectedAddUserIds.length === 0"
            @click="handleAddUsersToRole"
          >
            确定添加 ({{ selectedAddUserIds.length }})
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import {
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NButton,
  NSpace,
  NSelect,
  NDataTable,
  NModal,
  NRadio,
  NRadioGroup,
  NIcon,
  NTag,
  NAvatar,
  NPopconfirm,
  NSpin,
  NTree,
  NTabs,
  NTabPane,
  NDescriptions,
  NDescriptionsItem,
  NText,
  useMessage,
  type FormInst,
  type FormRules,
  type DataTableColumns,
  type DataTableRowKey,
  type TreeOption
} from 'naive-ui'
import { SearchOutline, RefreshOutline, AddOutline, PersonAddOutline } from '@vicons/ionicons5'
import {
  getRolePage,
  getRoleDetail,
  createRole,
  updateRole,
  getRoleUsers,
  addUsersToRole,
  removeUserFromRole,
  removeUsersFromRole,
  type RoleVO,
  type RoleDetailVO,
  type RoleQueryDTO,
  type RoleCreateDTO,
  type RoleUpdateDTO,
  type RoleUserVO,
  type RoleUserQueryDTO
} from '@/api/role'
import UserPicker from '@/components/UserPicker.vue'
import { getRoleMenuTree, type MenuVO } from '@/api/menu'

const message = useMessage()

// 状态选项
const statusOptions = [
  { label: '正常', value: 0 },
  { label: '禁用', value: 1 }
]

// 查询参数
const queryParams = reactive<RoleQueryDTO>({
  roleName: undefined,
  roleKey: undefined,
  status: undefined,
  current: 1,
  size: 10
})

// 列表数据
const roleList = ref<RoleVO[]>([])
const loading = ref(false)
const total = ref(0)
const currentRole = ref<RoleVO | null>(null)
const selectedRoleKeys = ref<string[]>([])

const roleTreeData = computed<TreeOption[]>(() => {
  return roleList.value.map((role) => ({
    key: role.id,
    label: role.roleName
  }))
})

const handleRoleTreeSelect = (keys: Array<string | number>) => {
  selectedRoleKeys.value = keys as string[]
  const first = keys[0]
  if (!first) return
  const target = roleList.value.find((item) => item.id === String(first))
  if (target) {
    handleViewUsers(target)
  }
}

// 弹窗相关
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const editingRoleId = ref<string>('')

const formData = reactive<RoleCreateDTO & RoleUpdateDTO>({
  roleName: '',
  roleKey: '',
  sort: 0,
  status: 0,
  remark: ''
})

const formRules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度为 2-50 个字符', trigger: 'blur' }
  ],
  roleKey: [
    { required: true, message: '请输入权限标识', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]*$/, message: '权限标识必须以大写字母开头，只能包含大写字母、数字和下划线', trigger: 'blur' }
  ]
}

const activeTab = ref('basic')

// ==================== 角色用户相关 ====================
const roleUserList = ref<RoleUserVO[]>([])
const loadingUsers = ref(false)
const userTotal = ref(0)
const selectedUserIds = ref<DataTableRowKey[]>([])

const userQueryParams = reactive<RoleUserQueryDTO>({
  roleId: '',
  username: undefined,
  nickname: undefined,
  current: 1,
  size: 10
})

const userPagination = computed(() => ({
  page: userQueryParams.current,
  pageSize: userQueryParams.size,
  pageCount: Math.ceil(userTotal.value / (userQueryParams.size || 10)),
  itemCount: userTotal.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
}))

// 角色用户表格列
const userColumns: DataTableColumns<RoleUserVO> = [
  { type: 'selection' },
  {
    title: '用户名',
    key: 'username',
    width: 120
  },
  {
    title: '昵称',
    key: 'nickname',
    width: 120,
    render: (row) => row.nickname || '-'
  },
  {
    title: '头像',
    key: 'avatar',
    width: 70,
    render: (row) => h(NAvatar, {
      size: 'small',
      round: true,
      src: row.avatar || undefined,
      fallbackSrc: 'https://ui-avatars.com/api/?name=' + (row.nickname || row.username)
    })
  },
  {
    title: '邮箱',
    key: 'email',
    width: 180,
    ellipsis: { tooltip: true },
    render: (row) => row.email || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => h(NTag, {
      type: row.status === 0 ? 'success' : 'error',
      size: 'small'
    }, { default: () => row.status === 0 ? '正常' : '禁用' })
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: (row) => h(NPopconfirm, {
      onPositiveClick: () => handleRemoveUser(row)
    }, {
      trigger: () => h(NButton, {
        size: 'small',
        quaternary: true,
        type: 'error'
      }, { default: () => '移除' }),
      default: () => `确定要将用户 "${row.username}" 从该角色移除吗？`
    })
  }
]

const roleMenus = ref<MenuVO[]>([])
const loadingRoleMenus = ref(false)
const checkedMenuIds = ref<string[]>([])
const savingRoleMenus = ref(false)

const roleMenuTreeData = computed<TreeOption[]>(() => {
  return transformRoleMenusToTree(roleMenus.value)
})

function transformRoleMenusToTree(menus: MenuVO[]): TreeOption[] {
  return menus.map((menu) => ({
    key: menu.id,
    label: menu.menuName,
    children: menu.children ? transformRoleMenusToTree(menu.children) : undefined
  }))
}

// ==================== 添加用户相关 ====================
const showAddUsersModal = ref(false)
const selectedAddUserIds = ref<string[]>([])
const addingUsers = ref(false)

// ==================== 方法 ====================

// 加载角色列表
const loadRoleList = async () => {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    roleList.value = res.records
    total.value = res.total
    let target: RoleVO | undefined
    if (currentRole.value) {
      target = res.records.find((item) => item.id === currentRole.value?.id)
    }
    if (!target && res.records.length > 0) {
      target = res.records[0]
    }
    if (target) {
      handleViewUsers(target)
    } else {
      currentRole.value = null
      roleUserList.value = []
      userTotal.value = 0
      selectedUserIds.value = []
      roleMenus.value = []
      selectedRoleKeys.value = []
    }
  } catch (error) {
    message.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.current = 1
  loadRoleList()
}

// 重置搜索
const handleReset = () => {
  queryParams.roleName = undefined
  queryParams.roleKey = undefined
  queryParams.status = undefined
  queryParams.current = 1
  loadRoleList()
}

// 新增角色
const handleAdd = () => {
  isEdit.value = false
  editingRoleId.value = ''
  Object.assign(formData, {
    roleName: '',
    roleKey: '',
    sort: 0,
    status: 0,
    remark: ''
  })
  showModal.value = true
}

// 编辑角色
const handleEdit = (row: RoleVO) => {
  isEdit.value = true
  editingRoleId.value = row.id
  Object.assign(formData, {
    roleName: row.roleName,
    roleKey: row.roleKey,
    sort: row.sort,
    status: row.status,
    remark: row.remark || ''
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
      const updateData: RoleUpdateDTO = {
        roleName: formData.roleName,
        roleKey: formData.roleKey,
        sort: formData.sort,
        status: formData.status,
        remark: formData.remark
      }
      await updateRole(editingRoleId.value, updateData)
      message.success('更新成功')
    } else {
      const createData: RoleCreateDTO = {
        roleName: formData.roleName!,
        roleKey: formData.roleKey!,
        sort: formData.sort,
        status: formData.status,
        remark: formData.remark
      }
      await createRole(createData)
      message.success('创建成功')
    }
    showModal.value = false
    loadRoleList()
  } catch (error) {
    message.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 角色用户相关方法 ====================

// 查看角色用户
const handleViewUsers = (row: RoleVO) => {
  currentRole.value = row
  activeTab.value = 'basic'
  selectedRoleKeys.value = [row.id]
  userQueryParams.roleId = row.id
  userQueryParams.username = undefined
  userQueryParams.nickname = undefined
  userQueryParams.current = 1
  selectedUserIds.value = []
  loadRoleUsers()
  loadRoleMenus()
}

// 加载角色用户列表
const loadRoleUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await getRoleUsers(userQueryParams)
    roleUserList.value = res.records
    userTotal.value = res.total
  } catch (error) {
    message.error('加载角色用户失败')
  } finally {
    loadingUsers.value = false
  }
}

const loadRoleMenus = async () => {
  if (!currentRole.value) return
  loadingRoleMenus.value = true
  try {
    const [menus, detail] = await Promise.all([
      getRoleMenuTree(currentRole.value.id),
      getRoleDetail(currentRole.value.id)
    ])
    roleMenus.value = menus
    checkedMenuIds.value = (detail as RoleDetailVO).menuIds || []
  } catch (error) {
    roleMenus.value = []
    checkedMenuIds.value = []
    message.error('加载角色菜单失败')
  } finally {
    loadingRoleMenus.value = false
  }
}

const handleMenuCheckedChange = (keys: Array<string | number>) => {
  checkedMenuIds.value = keys as string[]
}

const handleSaveRoleMenus = async () => {
  if (!currentRole.value) return
  savingRoleMenus.value = true
  try {
    await updateRole(currentRole.value.id, { menuIds: checkedMenuIds.value })
    message.success('保存成功')
    await loadRoleMenus()
  } catch (error) {
    message.error('保存失败')
  } finally {
    savingRoleMenus.value = false
  }
}

const handleResetRoleMenus = () => {
  loadRoleMenus()
}

// 搜索角色用户
const handleSearchUsers = () => {
  userQueryParams.current = 1
  loadRoleUsers()
}

// 用户分页变化
const handleUserPageChange = (page: number) => {
  userQueryParams.current = page
  loadRoleUsers()
}

const handleUserPageSizeChange = (pageSize: number) => {
  userQueryParams.size = pageSize
  userQueryParams.current = 1
  loadRoleUsers()
}

// 用户选择变化
const handleUserSelectionChange = (keys: DataTableRowKey[]) => {
  selectedUserIds.value = keys
}

// 移除单个用户
const handleRemoveUser = async (row: RoleUserVO) => {
  try {
    await removeUserFromRole(currentRole.value!.id, row.id)
    message.success('移除成功')
    loadRoleUsers()
  } catch (error) {
    message.error('移除失败')
  }
}

// 批量移除用户
const handleBatchRemoveUsers = async () => {
  if (selectedUserIds.value.length === 0) return
  try {
    await removeUsersFromRole(currentRole.value!.id, selectedUserIds.value as string[])
    message.success('批量移除成功')
    selectedUserIds.value = []
    loadRoleUsers()
  } catch (error) {
    message.error('批量移除失败')
  }
}

// ==================== 添加用户相关方法 ====================

// 打开添加用户弹窗
const handleOpenAddUsers = () => {
  if (!currentRole.value) return
  selectedAddUserIds.value = []
  showAddUsersModal.value = true
}

// 添加用户到角色
const handleAddUsersToRole = async () => {
  if (selectedAddUserIds.value.length === 0) return
  addingUsers.value = true
  try {
    await addUsersToRole(currentRole.value!.id, selectedAddUserIds.value)
    message.success('添加成功')
    showAddUsersModal.value = false
    loadRoleUsers() // 刷新角色用户列表
  } catch (error) {
    message.error('添加失败')
  } finally {
    addingUsers.value = false
  }
}

onMounted(() => {
  loadRoleList()
})
</script>
