<template>
  <div class="p-6 animate-enter">
    <div class="flex gap-6 h-[calc(100vh-140px)]">
      <div class="glass-card !p-5 w-80 flex-shrink-0 flex flex-col">
        <div class="mb-4">
          <div
              class="flex items-center justify-between cursor-pointer select-none mb-2"
              @click="showSearchForm = !showSearchForm"
          >
            <span class="text-sm text-gray-500">搜索条件</span>
            <n-icon :class="['transition-transform', showSearchForm ? 'rotate-180' : '']">
              <ChevronDownOutline />
            </n-icon>
          </div>
          <n-collapse-transition :show="showSearchForm">
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
          </n-collapse-transition>
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
          <n-tabs
            v-model:value="activeTab"
            type="line"
            animated
            class="flex-1 flex flex-col overflow-hidden"
            pane-wrapper-style="flex: 1; overflow: hidden; display: flex; flex-direction: column;"
            pane-style="flex: 1; display: flex; flex-direction: column; overflow: hidden;"
          >
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
            <n-tab-pane name="permissions" tab="权限列表">
              <div class="flex flex-col h-full pt-2">
                <div class="flex items-center justify-between mb-3 flex-shrink-0">
                  <div class="flex items-center gap-2">
                    <h4 class="font-semibold text-sm text-text-main">角色权限</h4>
                    <n-tag size="tiny" round :bordered="false" type="info">{{ rolePermissionList.length }} 个</n-tag>
                  </div>
                  <n-button type="primary" size="tiny" quaternary @click="handleOpenPermissionSelector">
                    <template #icon><n-icon><AddOutline /></n-icon></template>
                    添加权限
                  </n-button>
                </div>
                <div class="flex-1 overflow-y-auto">
                  <n-spin :show="loadingRolePermissions">
                    <div v-if="rolePermissionList.length === 0" class="flex items-center justify-center h-40 text-gray-400">
                      暂无权限，点击「添加权限」按钮进行添加
                    </div>
                    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 pr-1">
                    <div
                      v-for="perm in rolePermissionList"
                      :key="perm.id"
                      class="group relative p-4 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:shadow-md transition-shadow"
                    >
                      <n-popconfirm
                        @positive-click="handleRemovePermission(perm.id)"
                      >
                        <template #trigger>
                          <n-button
                            class="!absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
                            size="tiny"
                            quaternary
                            circle
                            type="error"
                          >
                            <template #icon><n-icon><CloseOutline /></n-icon></template>
                          </n-button>
                        </template>
                        确定要移除该权限吗？
                      </n-popconfirm>
                      <div class="flex items-start justify-between mb-2 pr-6">
                        <div class="flex items-center gap-2">
                          <n-icon size="18" :color="perm.status === 0 ? '#18a058' : '#d03050'">
                            <ShieldCheckmarkOutline />
                          </n-icon>
                          <span class="font-medium text-sm text-text-main">{{ perm.permName }}</span>
                        </div>
                        <n-tag size="tiny" :type="perm.status === 0 ? 'success' : 'error'">
                          {{ perm.status === 0 ? '正常' : '禁用' }}
                        </n-tag>
                      </div>
                      <div class="mb-2">
                        <n-tag size="tiny" :bordered="false" type="info">
                          {{ perm.permCode }}
                        </n-tag>
                      </div>
                      <div v-if="perm.description" class="text-xs text-gray-500 line-clamp-2">
                        {{ perm.description }}
                      </div>
                      <div v-if="perm.resourceIds && perm.resourceIds.length > 0" class="mt-2 pt-2 border-t border-gray-100 dark:border-gray-700">
                        <span class="text-xs text-gray-400">关联资源: {{ perm.resourceIds.length }} 个</span>
                      </div>
                    </div>
                  </div>
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
          <n-button type="primary" :loading="savingRole" @click="handleSave">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 权限选择器弹窗 -->
    <n-modal
      v-model:show="showPermissionSelector"
      title="选择权限"
      preset="card"
      style="width: 700px"
      :mask-closable="false"
    >
      <div class="space-y-4">
        <n-input
          v-model:value="permissionSearchKeyword"
          placeholder="搜索权限名称或编码..."
          clearable
        >
          <template #prefix>
            <n-icon><SearchOutline /></n-icon>
          </template>
        </n-input>

        <n-spin :show="loadingAllPermissions">
          <div v-if="availablePermissions.length === 0" class="flex items-center justify-center h-40 text-gray-400">
            {{ permissionSearchKeyword ? '没有匹配的权限' : '没有可添加的权限' }}
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3 max-h-[400px] overflow-y-auto pr-1">
            <div
              v-for="perm in availablePermissions"
              :key="perm.id"
              class="flex items-start gap-3 p-3 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-primary cursor-pointer transition-colors"
              :class="{ 'border-primary bg-primary/5': selectedPermissionIds.includes(perm.id) }"
              @click="handlePermissionSelect(perm.id, !selectedPermissionIds.includes(perm.id))"
            >
              <n-checkbox
                :checked="selectedPermissionIds.includes(perm.id)"
                @update:checked="(checked: boolean) => handlePermissionSelect(perm.id, checked)"
                @click.stop
              />
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <n-icon size="16" :color="perm.status === 0 ? '#18a058' : '#d03050'">
                    <ShieldCheckmarkOutline />
                  </n-icon>
                  <span class="font-medium text-sm truncate">{{ perm.permName }}</span>
                </div>
                <n-tag size="tiny" :bordered="false" type="info">
                  {{ perm.permCode }}
                </n-tag>
                <div v-if="perm.description" class="mt-1 text-xs text-gray-500 line-clamp-1">
                  {{ perm.description }}
                </div>
              </div>
            </div>
          </div>
        </n-spin>
      </div>
      <template #footer>
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500">已选择 {{ selectedPermissionIds.length }} 个权限</span>
          <n-space>
            <n-button @click="showPermissionSelector = false">取消</n-button>
            <n-button
              type="primary"
              :loading="savingPermissions"
              :disabled="selectedPermissionIds.length === 0"
              @click="handleConfirmAddPermissions"
            >
              确定添加
            </n-button>
          </n-space>
        </div>
      </template>
    </n-modal>

    <n-modal
      v-model:show="showAddUsersModal"
      title="添加角色用户"
      preset="card"
      style="width: 800px"
      :mask-closable="false"
    >
      <div class="space-y-4">
        <n-form inline :model="addUserQueryParams" label-placement="left" class="flex flex-wrap gap-4">
          <n-form-item label="用户名" :show-feedback="false">
            <n-input v-model:value="addUserQueryParams.username" placeholder="用户名" clearable class="!w-40" />
          </n-form-item>
          <n-form-item label="昵称" :show-feedback="false">
            <n-input v-model:value="addUserQueryParams.nickname" placeholder="昵称" clearable class="!w-40" />
          </n-form-item>
          <n-form-item :show-feedback="false">
            <n-space>
              <n-button type="primary" size="small" @click="handleSearchAddUsers">
                <template #icon><n-icon><SearchOutline /></n-icon></template>
                搜索
              </n-button>
              <n-button size="small" @click="handleResetAddUsers">
                <template #icon><n-icon><RefreshOutline /></n-icon></template>
                重置
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>

        <n-data-table
          :columns="addUserColumns"
          :data="addUserList"
          :loading="loadingAddUsers"
          :pagination="addUserPagination"
          :row-key="(row: RoleUserVO) => row.id"
          :bordered="false"
          :single-line="false"
          :checked-row-keys="addUserSelectedIds"
          checkable
          @update:page="handleAddUserPageChange"
          @update:page-size="handleAddUserPageSizeChange"
          @update:checked-row-keys="handleAddUserSelectionChange"
        />
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showAddUsersModal = false">取消</n-button>
          <n-button
            type="primary"
            :loading="savingAddUsers"
            :disabled="addUserSelectedIds.length === 0"
            @click="handleConfirmAddUsers"
          >
            确定
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
  NButton,
  NSpace,
  NSelect,
  NTree,
  NTag,
  NDataTable,
  NModal,
  NInputNumber,
  NIcon,
  NText,
  NTabs,
  NTabPane,
  NDescriptions,
  NDescriptionsItem,
  NAvatar,
  NSpin,
  NCheckbox,
  NPopconfirm,
  useMessage,
  type FormInst,
  type FormRules,
  type TreeOption,
  type DataTableColumns,
  type DataTableRowKey, NCollapseTransition
} from 'naive-ui'
import {
  SearchOutline, RefreshOutline, AddOutline, PersonAddOutline, ShieldCheckmarkOutline, CloseOutline,
  ChevronDownOutline
} from '@vicons/ionicons5'
import {
  getRolePage,
  getRoleDetail,
  createRole,
  updateRole,
  getRoleUsers,
  getUnassignedUsers,
  addUsersToRole,
  removeUsersFromRole,
  getRolePermissions,
  addPermissionsToRole,
  removePermissionFromRole,
  getAllPermissions,
  type RoleVO,
  type RoleDetailVO,
  type RoleQueryDTO,
  type RoleCreateDTO,
  type RoleUpdateDTO,
  type RoleUserVO,
  type RoleUserQueryDTO,
  type PermissionVO
} from '@/api/role'
import { getMenuTree, type MenuVO } from '@/api/menu'

const message = useMessage()

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '禁用', value: 1 }
]

const showSearchForm = ref(false)

const queryParams = reactive<RoleQueryDTO>({
  roleName: undefined,
  roleKey: undefined,
  status: undefined,
  current: 1,
  size: 50
})

const total = ref(0)
const roleTreeData = ref<TreeOption[]>([])
const selectedRoleKeys = ref<string[]>([])
const currentRole = ref<RoleDetailVO | null>(null)
const activeTab = ref('basic')

const buildRoleTreeData = (roles: RoleVO[]): TreeOption[] => {
  return roles.map(role => ({
    key: role.id,
    label: role.roleName
  }))
}

const loadRoleTree = async () => {
  try {
    const res = await getRolePage(queryParams)
    total.value = res.total
    roleTreeData.value = buildRoleTreeData(res.records)
    if (currentRole.value) {
      const exists = res.records.find(item => item.id === currentRole.value?.id)
      if (!exists) {
        currentRole.value = null
        selectedRoleKeys.value = []
      }
    }
  } catch (error) {
    message.error('加载角色列表失败')
  }
}

const handleSearch = () => {
  loadRoleTree()
}

const handleReset = () => {
  queryParams.roleName = undefined
  queryParams.roleKey = undefined
  queryParams.status = undefined
  loadRoleTree()
}

const loadRoleDetail = async (id: string) => {
  try {
    const res = await getRoleDetail(id)
    currentRole.value = res
    await Promise.all([
      loadRoleMenus(id),
      loadRoleUsers(id),
      loadRolePermissions(id)
    ])
  } catch (error) {
    message.error('加载角色详情失败')
  }
}

const handleRoleTreeSelect = (keys: string[]) => {
  selectedRoleKeys.value = keys
  if (keys.length === 0) {
    currentRole.value = null
    return
  }
  const id = keys[0]
  if (!id) {
    currentRole.value = null
    return
  }
  loadRoleDetail(id)
}

const showModal = ref(false)
const modalTitle = ref('新增角色')
const formRef = ref<FormInst | null>(null)
const savingRole = ref(false)
const isEdit = ref(false)
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
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  roleKey: [
    { required: true, message: '请输入权限标识', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增角色'
  Object.assign(formData, {
    roleName: '',
    roleKey: '',
    sort: 0,
    status: 0,
    remark: ''
  })
  showModal.value = true
}

const handleEdit = (role: RoleDetailVO) => {
  isEdit.value = true
  modalTitle.value = '编辑角色'
  Object.assign(formData, {
    roleName: role.roleName,
    roleKey: role.roleKey,
    sort: role.sort,
    status: role.status,
    remark: role.remark
  })
  showModal.value = true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  savingRole.value = true
  try {
    if (isEdit.value && currentRole.value) {
      const payload: RoleUpdateDTO = {
        roleName: formData.roleName,
        roleKey: formData.roleKey,
        sort: formData.sort,
        status: formData.status,
        remark: formData.remark
      }
      await updateRole(currentRole.value.id, payload)
      message.success('更新成功')
    } else {
      const payload: RoleCreateDTO = {
        roleName: formData.roleName,
        roleKey: formData.roleKey,
        sort: formData.sort,
        status: formData.status,
        remark: formData.remark
      }
      await createRole(payload)
      message.success('创建成功')
    }
    showModal.value = false
    loadRoleTree()
  } catch (error) {
    message.error('保存失败')
  } finally {
    savingRole.value = false
  }
}

const roleMenuTreeData = ref<TreeOption[]>([])
const checkedMenuIds = ref<string[]>([])
const loadingRoleMenus = ref(false)
const savingRoleMenus = ref(false)

const buildMenuTree = (menus: MenuVO[]): TreeOption[] => {
  return menus.map(menu => ({
    key: menu.id,
    label: menu.menuName,
    children: menu.children ? buildMenuTree(menu.children) : undefined
  }))
}

const loadRoleMenus = async (roleId: string) => {
  loadingRoleMenus.value = true
  try {
    const [allMenus, roleDetail] = await Promise.all([
      getMenuTree(),
      getRoleDetail(roleId)
    ])
    roleMenuTreeData.value = buildMenuTree(allMenus)
    checkedMenuIds.value = roleDetail.menuIds || []
  } catch (error) {
    message.error('加载角色菜单失败')
  } finally {
    loadingRoleMenus.value = false
  }
}

const handleMenuCheckedChange = (keys: (string | number)[]) => {
  checkedMenuIds.value = keys as string[]
}

const handleResetRoleMenus = async () => {
  if (!currentRole.value) return
  await loadRoleMenus(currentRole.value.id)
}

const handleSaveRoleMenus = async () => {
  if (!currentRole.value) return
  savingRoleMenus.value = true
  try {
    await updateRole(currentRole.value.id, {
      menuIds: checkedMenuIds.value
    })
    message.success('菜单权限已保存')
  } catch (error) {
    message.error('保存菜单权限失败')
  } finally {
    savingRoleMenus.value = false
  }
}

// 角色权限相关
const rolePermissionList = ref<PermissionVO[]>([])
const loadingRolePermissions = ref(false)

const loadRolePermissions = async (roleId: string) => {
  loadingRolePermissions.value = true
  try {
    rolePermissionList.value = await getRolePermissions(roleId)
  } catch (error) {
    message.error('加载角色权限失败')
  } finally {
    loadingRolePermissions.value = false
  }
}

// 权限选择器相关
const showPermissionSelector = ref(false)
const allPermissionList = ref<PermissionVO[]>([])
const loadingAllPermissions = ref(false)
const selectedPermissionIds = ref<string[]>([])
const savingPermissions = ref(false)
const permissionSearchKeyword = ref('')

const filteredPermissions = computed(() => {
  const keyword = permissionSearchKeyword.value.toLowerCase()
  if (!keyword) return allPermissionList.value
  return allPermissionList.value.filter(
    p => p.permName.toLowerCase().includes(keyword) || p.permCode.toLowerCase().includes(keyword)
  )
})

const availablePermissions = computed(() => {
  const existingIds = new Set(rolePermissionList.value.map(p => p.id))
  return filteredPermissions.value.filter(p => !existingIds.has(p.id))
})

const loadAllPermissions = async () => {
  loadingAllPermissions.value = true
  try {
    allPermissionList.value = await getAllPermissions()
  } catch (error) {
    message.error('加载权限列表失败')
  } finally {
    loadingAllPermissions.value = false
  }
}

const handleOpenPermissionSelector = async () => {
  if (!currentRole.value) return
  showPermissionSelector.value = true
  selectedPermissionIds.value = []
  permissionSearchKeyword.value = ''
  await loadAllPermissions()
}

const handlePermissionSelect = (permId: string, checked: boolean) => {
  if (checked) {
    selectedPermissionIds.value.push(permId)
  } else {
    selectedPermissionIds.value = selectedPermissionIds.value.filter(id => id !== permId)
  }
}

const handleConfirmAddPermissions = async () => {
  if (!currentRole.value || selectedPermissionIds.value.length === 0) return
  savingPermissions.value = true
  try {
    await addPermissionsToRole(currentRole.value.id, selectedPermissionIds.value)
    message.success('添加权限成功')
    showPermissionSelector.value = false
    await loadRolePermissions(currentRole.value.id)
  } catch (error) {
    message.error('添加权限失败')
  } finally {
    savingPermissions.value = false
  }
}

const handleRemovePermission = async (permissionId: string) => {
  if (!currentRole.value) return
  try {
    await removePermissionFromRole(currentRole.value.id, permissionId)
    message.success('移除权限成功')
    await loadRolePermissions(currentRole.value.id)
  } catch (error) {
    message.error('移除权限失败')
  }
}

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
    render: row => row.nickname || '-'
  },
  {
    title: '头像',
    key: 'avatar',
    width: 70,
    render: row =>
      h(NAvatar, {
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
    render: row => row.email || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(
        NTag,
        {
          type: row.status === 0 ? 'success' : 'error',
          size: 'small'
        },
        { default: () => (row.status === 0 ? '正常' : '禁用') }
      )
  }
]

const loadRoleUsers = async (roleId: string) => {
  loadingUsers.value = true
  try {
    userQueryParams.roleId = roleId
    const res = await getRoleUsers(userQueryParams)
    roleUserList.value = res.records
    userTotal.value = res.total
    selectedUserIds.value = []
  } catch (error) {
    message.error('加载角色用户失败')
  } finally {
    loadingUsers.value = false
  }
}

const handleUserPageChange = (page: number) => {
  userQueryParams.current = page
  if (currentRole.value) {
    loadRoleUsers(currentRole.value.id)
  }
}

const handleUserPageSizeChange = (size: number) => {
  userQueryParams.size = size
  userQueryParams.current = 1
  if (currentRole.value) {
    loadRoleUsers(currentRole.value.id)
  }
}

const handleSearchUsers = () => {
  userQueryParams.current = 1
  if (currentRole.value) {
    loadRoleUsers(currentRole.value.id)
  }
}

const handleUserSelectionChange = (keys: DataTableRowKey[]) => {
  selectedUserIds.value = keys
}

const handleBatchRemoveUsers = async () => {
  if (!currentRole.value || selectedUserIds.value.length === 0) return
  try {
    await removeUsersFromRole(currentRole.value.id, selectedUserIds.value as string[])
    message.success('移除成功')
    loadRoleUsers(currentRole.value.id)
  } catch (error) {
    message.error('移除失败')
  }
}

const showAddUsersModal = ref(false)
const addUserQueryParams = reactive<RoleUserQueryDTO>({
  roleId: '',
  username: undefined,
  nickname: undefined,
  current: 1,
  size: 10
})

const addUserList = ref<RoleUserVO[]>([])
const loadingAddUsers = ref(false)
const addUserTotal = ref(0)
const addUserSelectedIds = ref<DataTableRowKey[]>([])
const savingAddUsers = ref(false)

const addUserPagination = computed(() => ({
  page: addUserQueryParams.current,
  pageSize: addUserQueryParams.size,
  pageCount: Math.ceil(addUserTotal.value / (addUserQueryParams.size || 10)),
  itemCount: addUserTotal.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
}))

const addUserColumns: DataTableColumns<RoleUserVO> = [
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
    render: row => row.nickname || '-'
  },
  {
    title: '邮箱',
    key: 'email',
    width: 180,
    ellipsis: { tooltip: true },
    render: row => row.email || '-'
  }
]

const loadAddUsers = async () => {
  if (!currentRole.value) return
  loadingAddUsers.value = true
  try {
    addUserQueryParams.roleId = currentRole.value.id
    const res = await getUnassignedUsers(addUserQueryParams)
    addUserList.value = res.records
    addUserTotal.value = res.total
  } catch (error) {
    message.error('加载可添加用户失败')
  } finally {
    loadingAddUsers.value = false
  }
}

const handleOpenAddUsers = () => {
  if (!currentRole.value) return
  showAddUsersModal.value = true
  addUserSelectedIds.value = []
  loadAddUsers()
}

const handleAddUserPageChange = (page: number) => {
  addUserQueryParams.current = page
  loadAddUsers()
}

const handleAddUserPageSizeChange = (size: number) => {
  addUserQueryParams.size = size
  addUserQueryParams.current = 1
  loadAddUsers()
}

const handleSearchAddUsers = () => {
  addUserQueryParams.current = 1
  loadAddUsers()
}

const handleResetAddUsers = () => {
  addUserQueryParams.username = undefined
  addUserQueryParams.nickname = undefined
  addUserQueryParams.current = 1
  loadAddUsers()
}

const handleAddUserSelectionChange = (keys: DataTableRowKey[]) => {
  addUserSelectedIds.value = keys
}

const handleConfirmAddUsers = async () => {
  if (!currentRole.value || addUserSelectedIds.value.length === 0) return
  savingAddUsers.value = true
  try {
    await addUsersToRole(currentRole.value.id, addUserSelectedIds.value as string[])
    message.success('添加成功')
    showAddUsersModal.value = false
    loadRoleUsers(currentRole.value.id)
  } catch (error) {
    message.error('添加失败')
  } finally {
    savingAddUsers.value = false
  }
}

onMounted(() => {
  loadRoleTree()
})
</script>

