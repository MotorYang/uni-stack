<template>
  <div class="p-6 animate-enter">
    <div class="flex gap-6 h-[calc(100vh-140px)]">
      <div class="glass-card !p-5 w-80 flex-shrink-0 flex flex-col">
        <div class="mb-4">
          <n-form inline :model="queryParams" label-placement="left" class="flex flex-wrap gap-4">
            <n-form-item label="名称" :show-feedback="false">
              <n-input v-model:value="queryParams.deptName" placeholder="请输入名称" clearable class="!w-40" />
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
              <h3 class="text-lg font-semibold text-text-main">组织机构</h3>
              <n-tag size="small" round :bordered="false" type="info">{{ deptCount }} 个</n-tag>
            </div>
            <n-space size="small">
              <n-button type="primary" size="small" @click="handleAddDeptRoot">
                新增
              </n-button>
              <n-button size="small" @click="handleRefreshDeptTree">
                <template #icon><n-icon><RefreshOutline /></n-icon></template>
                刷新
              </n-button>
            </n-space>
          </div>
          <div class="flex-1 overflow-y-auto">
            <n-tree
              :data="deptTreeData"
              :selected-keys="selectedDeptKeys"
              block-line
              :default-expand-all="true"
              @update:selected-keys="handleDeptTreeSelect"
            />
          </div>
        </div>
      </div>

      <div class="glass-card !p-6 flex-1 flex flex-col overflow-hidden">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-semibold text-text-main">
              {{ currentDept ? currentDept.deptName : '请选择组织机构' }}
            </h3>
            <div v-if="currentDept" class="mt-1 text-xs text-gray-500">
              部门ID：{{ currentDept.id }}
            </div>
          </div>
          <div v-if="currentDept" class="flex items-center gap-3">
            <n-tag size="small" :type="currentDept.status === 0 ? 'success' : 'error'">
              {{ currentDept.status === 0 ? '正常' : '停用' }}
            </n-tag>
            <n-space size="small">
              <n-button type="info" size="small" @click="handleAddChildDept">
                新增子部门
              </n-button>
              <n-button type="primary" size="small" @click="handleEditDept">
                编辑
              </n-button>
              <n-popconfirm @positive-click="handleDeleteDept">
                <template #trigger>
                  <n-button type="error" size="small">
                    删除
                  </n-button>
                </template>
                确定要删除该组织机构吗？
              </n-popconfirm>
            </n-space>
          </div>
        </div>

        <div v-if="currentDept" class="flex-1 flex flex-col overflow-hidden">
          <n-tabs v-model:value="activeTab" type="line" animated>
            <n-tab-pane name="basic" tab="基本信息">
              <div class="pt-2">
                <n-descriptions :column="2" label-placement="left" bordered size="small">
                  <n-descriptions-item label="部门名称">
                    <n-text>{{ currentDept.deptName }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="上级部门">
                    <n-text>{{ parentDeptName }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="负责人">
                    <n-text>{{ currentDept.leader || '-' }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="联系电话">
                    <n-text>{{ currentDept.phone || '-' }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="邮箱">
                    <n-text>{{ currentDept.email || '-' }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="排序">
                    <n-text>{{ currentDept.sort }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="状态">
                    <n-text>{{ currentDept.status === 0 ? '正常' : '停用' }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="创建时间">
                    <n-text>{{ currentDept.createTime?.replace('T', ' ').substring(0, 19) || '-' }}</n-text>
                  </n-descriptions-item>
                </n-descriptions>
              </div>
            </n-tab-pane>
            <n-tab-pane name="users" tab="组织机构用户">
              <div class="flex flex-col h-full pt-2">
                <div class="flex justify-between items-center mb-2">
                  <div class="flex items-center gap-2">
                    <h4 class="font-semibold text-sm text-text-main">用户列表</h4>
                    <n-tag size="tiny" round :bordered="false" type="info">{{ userTotal }} 人</n-tag>
                  </div>
                <div class="flex items-center gap-2">
                  <n-button type="primary" size="tiny" quaternary @click="handleAddUser">
                    新增用户
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
                      <n-input
                        v-model:value="userQueryParams.phone"
                        placeholder="手机号"
                        clearable
                        class="!w-32"
                      />
                    </n-form-item>
                    <n-form-item :show-feedback="false">
                      <n-select
                        v-model:value="userQueryParams.status"
                        :options="statusOptions"
                        placeholder="状态"
                        clearable
                        class="!w-28"
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
                  :data="userList"
                  :loading="loadingUsers"
                  :pagination="userPagination"
                  :row-key="(row: UserVO) => row.id"
                  :bordered="false"
                  :single-line="false"
                  size="small"
                  style="min-height: 260px"
                  @update:page="handleUserPageChange"
                  @update:page-size="handleUserPageSizeChange"
                />
              </div>
            </n-tab-pane>
          </n-tabs>
        </div>

        <div v-else class="flex-1 flex items-center justify-center text-gray-400">
          请在左侧选择一个组织机构查看详情
        </div>
      </div>
    </div>

    <n-modal
      v-model:show="showDeptModal"
      :title="deptModalTitle"
      preset="card"
      style="width: 520px"
      :mask-closable="false"
    >
      <n-form
        ref="deptFormRef"
        :model="deptFormData"
        :rules="deptFormRules"
        label-placement="left"
        label-width="90"
      >
        <n-form-item label="上级部门" path="parentId">
          <n-tree-select
            v-model:value="deptFormData.parentId"
            :options="deptTreeOptions"
            placeholder="请选择上级部门"
            clearable
            default-expand-all
          />
        </n-form-item>
        <n-form-item label="部门名称" path="deptName">
          <n-input v-model:value="deptFormData.deptName" placeholder="请输入部门名称" />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="deptFormData.sort" :min="0" class="!w-full" />
        </n-form-item>
        <n-form-item label="负责人" path="leader">
          <div class="flex gap-2 w-full">
            <n-input v-model:value="deptFormData.leader" placeholder="请选择负责人" disabled />
            <n-button size="small" @click="openLeaderPicker">
              选择
            </n-button>
            <n-button size="small" quaternary @click="clearLeader">
              清空
            </n-button>
          </div>
        </n-form-item>
        <n-form-item label="联系电话" path="phone">
          <n-input v-model:value="deptFormData.phone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="deptFormData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item v-if="isEditDept" label="状态" path="status">
          <n-radio-group v-model:value="deptFormData.status">
            <n-space>
              <n-radio :value="0">正常</n-radio>
              <n-radio :value="1">停用</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDeptModal = false">取消</n-button>
          <n-button type="primary" :loading="deptSubmitting" @click="handleSubmitDept">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="showLeaderPicker"
      title="选择负责人"
      preset="card"
      style="width: 800px"
      :mask-closable="false"
    >
      <UserPicker v-model:value="leaderSelectedUserIds" />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showLeaderPicker = false">取消</n-button>
          <n-button
            type="primary"
            :loading="pickingLeader"
            :disabled="leaderSelectedUserIds.length === 0"
            @click="handleConfirmLeader"
          >
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="showUserModal"
      :title="userModalTitle"
      preset="card"
      style="width: 600px"
      :mask-closable="false"
    >
      <n-form
        ref="userFormRef"
        :model="userFormData"
        :rules="userFormRules"
        label-placement="left"
        label-width="100"
      >
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="userFormData.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item v-if="!isEditUser" label="密码" path="password">
          <n-input v-model:value="userFormData.password" type="password" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="userFormData.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="userFormData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="userFormData.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="性别" path="gender">
          <n-radio-group v-model:value="userFormData.gender">
            <n-space>
              <n-radio :value="0">保密</n-radio>
              <n-radio :value="1">男</n-radio>
              <n-radio :value="2">女</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-radio-group v-model:value="userFormData.status">
            <n-space>
              <n-radio :value="0">正常</n-radio>
              <n-radio :value="1">禁用</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showUserModal = false">取消</n-button>
          <n-button type="primary" :loading="userSubmitting" @click="handleSubmitUser">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="showAssignUsersModal"
      title="添加用户到组织机构"
      preset="card"
      style="width: 800px"
      :mask-closable="false"
    >
      <UserPicker v-model:value="selectedAssignUserIds" />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showAssignUsersModal = false">取消</n-button>
          <n-button
            type="primary"
            :loading="assigningUsers"
            :disabled="selectedAssignUserIds.length === 0"
            @click="handleAssignUsersToDept"
          >
            确定添加 ({{ selectedAssignUserIds.length }})
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
  NSelect,
  NButton,
  NSpace,
  NDataTable,
  NTag,
  NIcon,
  NAvatar,
  NTree,
  NTabs,
  NTabPane,
  NDescriptions,
  NDescriptionsItem,
  NText,
  NModal,
  NInputNumber,
  NRadio,
  NRadioGroup,
  NPopconfirm,
  NTreeSelect,
  useMessage,
  type DataTableColumns,
  type TreeOption,
  type FormInst,
  type FormRules,
  type TreeSelectOption
} from 'naive-ui'
import { SearchOutline, RefreshOutline } from '@vicons/ionicons5'
import {
  getDeptTree,
  getDeptDetail,
  createDept,
  updateDept,
  deleteDept,
  type DeptVO,
  type DeptQueryDTO,
  type DeptCreateDTO,
  type DeptUpdateDTO
} from '@/api/dept'
import {
  getUserPage,
  getUserDetail,
  createUser,
  updateUser,
  deleteUser,
  type UserVO,
  type UserQueryDTO,
  type UserCreateDTO,
  type UserUpdateDTO
} from '@/api/user'
import UserPicker from '@/components/UserPicker.vue'

const message = useMessage()

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '禁用', value: 1 }
]

const queryParams = reactive<DeptQueryDTO>({
  deptName: undefined,
  status: undefined
})

const deptTree = ref<DeptVO[]>([])
const selectedDeptKeys = ref<string[]>([])
const currentDept = ref<DeptVO | null>(null)

const deptTreeData = computed<TreeOption[]>(() => {
  return transformDeptToTree(deptTree.value)
})

const deptCount = computed(() => {
  const flatten = (nodes: DeptVO[]): DeptVO[] => {
    return nodes.flatMap((n) => [n, ...(n.children ? flatten(n.children) : [])])
  }
  return flatten(deptTree.value).length
})

const activeTab = ref('basic')

const showDeptModal = ref(false)
const isEditDept = ref(false)
const deptModalTitle = computed(() => (isEditDept.value ? '编辑组织机构' : '新增组织机构'))
const deptFormRef = ref<FormInst | null>(null)
const deptSubmitting = ref(false)
const editingDeptId = ref<string>('')

const deptFormData = reactive<DeptCreateDTO & DeptUpdateDTO>({
  parentId: '0',
  deptName: '',
  sort: 0,
  leader: '',
  phone: '',
  email: '',
  status: 0
})

const showLeaderPicker = ref(false)
const leaderSelectedUserIds = ref<string[]>([])
const pickingLeader = ref(false)

const deptFormRules: FormRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 1, max: 50, message: '部门名称长度为 1-50 个字符', trigger: 'blur' }
  ],
  email: [
    {
      validator: (_rule, value) => {
        if (!value) return true
        const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!reg.test(value)) {
          return new Error('邮箱格式不正确')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

const deptTreeOptions = computed<TreeSelectOption[]>(() => {
  const options: TreeSelectOption[] = [{ key: '0', label: '根节点' }]
  const buildOptions = (nodes: DeptVO[]): TreeSelectOption[] => {
    return nodes.map((node) => ({
      key: node.id,
      label: node.deptName,
      children: node.children ? buildOptions(node.children) : undefined
    }))
  }
  options.push(...buildOptions(deptTree.value))
  return options
})

const userList = ref<UserVO[]>([])
const loadingUsers = ref(false)
const userTotal = ref(0)

const userQueryParams = reactive<UserQueryDTO>({
  username: undefined,
  nickname: undefined,
  phone: undefined,
  status: undefined,
  deptId: '',
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

const userColumns: DataTableColumns<UserVO> = [
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
    render: (row) =>
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
    render: (row) => row.email || '-'
  },
  {
    title: '手机号',
    key: 'phone',
    width: 120,
    render: (row) => row.phone || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) =>
      h(
        NTag,
        {
          type: row.status === 0 ? 'success' : 'error',
          size: 'small'
        },
        { default: () => (row.status === 0 ? '正常' : '禁用') }
      )
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: (row) => row.createTime?.replace('T', ' ').substring(0, 19) || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    fixed: 'right',
    render: (row) =>
      h(
        NSpace,
        null,
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                quaternary: true,
                type: 'primary',
                onClick: () => handleEditUser(row)
              },
              { default: () => '编辑' }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => handleDeleteUser(row)
              },
              {
                trigger: () =>
                  h(
                    NButton,
                    {
                      size: 'small',
                      quaternary: true,
                      type: 'error'
                    },
                    { default: () => '删除' }
                  ),
                default: () => `确定要删除用户 "${row.username}" 吗？`
              }
            )
          ]
        }
      )
  }
]

const showUserModal = ref(false)
const isEditUser = ref(false)
const userModalTitle = computed(() => (isEditUser.value ? '编辑用户' : '新增用户'))
const userFormRef = ref<FormInst | null>(null)
const userSubmitting = ref(false)
const editingUserId = ref<string>('')

const showAssignUsersModal = ref(false)
const selectedAssignUserIds = ref<string[]>([])
const assigningUsers = ref(false)

const userFormData = reactive<UserCreateDTO & UserUpdateDTO>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  status: 0,
  deptId: ''
})

const userFormRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为 2-50 个字符', trigger: 'blur' }
  ],
  password: [
    {
      validator: (_rule, value) => {
        if (!isEditUser.value && !value) {
          return new Error('请输入密码')
        }
        if (value && (value.length < 6 || value.length > 20)) {
          return new Error('密码长度为 6-20 个字符')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_rule, value) => {
        if (!value) return true
        const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!reg.test(value)) {
          return new Error('邮箱格式不正确')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

const parentDeptName = computed(() => {
  if (!currentDept.value) return '-'
  if (!currentDept.value.parentId || currentDept.value.parentId === '0') return '根节点'
  const findDept = (nodes: DeptVO[], id: string): DeptVO | null => {
    for (const node of nodes) {
      if (node.id === id) return node
      if (node.children) {
        const found = findDept(node.children, id)
        if (found) return found
      }
    }
    return null
  }
  const parent = findDept(deptTree.value, currentDept.value.parentId)
  return parent?.deptName || '-'
})

function transformDeptToTree(nodes: DeptVO[]): TreeOption[] {
  return nodes.map((node) => ({
    key: node.id,
    label: node.deptName,
    children: node.children ? transformDeptToTree(node.children) : undefined
  }))
}

const loadDeptTree = async (selectedId?: string) => {
  try {
    const res = await getDeptTree(queryParams)
    deptTree.value = res
    if (res.length > 0) {
      const targetId = selectedId || res[0]?.id
      if (targetId) {
        handleDeptTreeSelect([targetId])
      }
    } else {
      selectedDeptKeys.value = []
      currentDept.value = null
      userList.value = []
      userTotal.value = 0
    }
  } catch (error) {
    message.error('加载组织机构失败')
  }
}

const handleSearch = () => {
  loadDeptTree()
}

const handleReset = () => {
  queryParams.deptName = undefined
  queryParams.status = undefined
  loadDeptTree()
}

const handleRefreshDeptTree = () => {
  loadDeptTree()
}

const handleDeptTreeSelect = (keys: Array<string | number>) => {
  selectedDeptKeys.value = keys as string[]
  const first = keys[0]
  if (!first) return
  const id = String(first)
  loadDeptDetail(id)
  loadDeptUsers(id, true)
}

const openLeaderPicker = () => {
  leaderSelectedUserIds.value = []
  showLeaderPicker.value = true
}

const clearLeader = () => {
  deptFormData.leader = ''
  leaderSelectedUserIds.value = []
}

const handleConfirmLeader = async () => {
  if (leaderSelectedUserIds.value.length === 0) {
    message.warning('请选择负责人')
    return
  }
  const userId = String(leaderSelectedUserIds.value[0])
  pickingLeader.value = true
  try {
    const detail = await getUserDetail(userId)
    deptFormData.leader = detail.nickname || detail.username
    showLeaderPicker.value = false
  } catch (error) {
    message.error('获取用户信息失败')
  } finally {
    pickingLeader.value = false
  }
}

const handleAddDeptRoot = () => {
  isEditDept.value = false
  editingDeptId.value = ''
  Object.assign(deptFormData, {
    parentId: '0',
    deptName: '',
    sort: 0,
    leader: '',
    phone: '',
    email: '',
    status: 0
  })
  showDeptModal.value = true
}

const handleAddChildDept = () => {
  if (!currentDept.value) {
    message.warning('请先选择组织机构')
    return
  }
  isEditDept.value = false
  editingDeptId.value = ''
  Object.assign(deptFormData, {
    parentId: currentDept.value.id,
    deptName: '',
    sort: currentDept.value.sort ?? 0,
    leader: '',
    phone: '',
    email: '',
    status: 0
  })
  showDeptModal.value = true
}

const handleEditDept = () => {
  if (!currentDept.value) return
  isEditDept.value = true
  editingDeptId.value = currentDept.value.id
  Object.assign(deptFormData, {
    parentId: currentDept.value.parentId,
    deptName: currentDept.value.deptName,
    sort: currentDept.value.sort,
    leader: currentDept.value.leader || '',
    phone: currentDept.value.phone || '',
    email: currentDept.value.email || '',
    status: currentDept.value.status
  })
  showDeptModal.value = true
}

const handleSubmitDept = async () => {
  try {
    await deptFormRef.value?.validate()
  } catch {
    return
  }

  deptSubmitting.value = true
  try {
    if (isEditDept.value) {
      const updateData: DeptUpdateDTO = {
        parentId: deptFormData.parentId,
        deptName: deptFormData.deptName,
        sort: deptFormData.sort,
        leader: deptFormData.leader,
        phone: deptFormData.phone,
        email: deptFormData.email,
        status: deptFormData.status
      }
      const updated = await updateDept(editingDeptId.value, updateData)
      message.success('更新成功')
      showDeptModal.value = false
      await loadDeptTree(updated.id)
    } else {
      const createData: DeptCreateDTO = {
        parentId: deptFormData.parentId,
        deptName: deptFormData.deptName!,
        sort: deptFormData.sort,
        leader: deptFormData.leader,
        phone: deptFormData.phone,
        email: deptFormData.email
      }
      const created = await createDept(createData)
      message.success('创建成功')
      showDeptModal.value = false
      await loadDeptTree(created.id)
    }
  } catch (error) {
    message.error(isEditDept.value ? '更新失败' : '创建失败')
  } finally {
    deptSubmitting.value = false
  }
}

const handleDeleteDept = async () => {
  if (!currentDept.value) return
  const targetId = currentDept.value.id

  const findDept = (nodes: DeptVO[], id: string): DeptVO | null => {
    for (const node of nodes) {
      if (node.id === id) return node
      if (node.children) {
        const found = findDept(node.children, id)
        if (found) return found
      }
    }
    return null
  }

  const node = findDept(deptTree.value, targetId)
  if (node && node.children && node.children.length > 0) {
    message.warning('请先删除子部门')
    return
  }

  try {
    await deleteDept(targetId)
    message.success('删除成功')
    currentDept.value = null
    selectedDeptKeys.value = []
    await loadDeptTree()
  } catch (error) {
    message.error('删除失败')
  }
}

const loadDeptDetail = async (id: string) => {
  try {
    const res = await getDeptDetail(id)
    currentDept.value = res
    activeTab.value = 'basic'
  } catch (error) {
    message.error('加载组织机构详情失败')
  }
}

const loadDeptUsers = async (deptId: string, resetPage = false) => {
  if (resetPage) {
    userQueryParams.current = 1
  }
  userQueryParams.deptId = deptId
  loadingUsers.value = true
  try {
    const res = await getUserPage(userQueryParams)
    userList.value = res.records
    userTotal.value = res.total
  } catch (error) {
    message.error('加载用户列表失败')
  } finally {
    loadingUsers.value = false
  }
}

const handleSearchUsers = () => {
  if (!currentDept.value) return
  loadDeptUsers(currentDept.value.id, true)
}

const handleUserPageChange = (page: number) => {
  userQueryParams.current = page
  if (!currentDept.value) return
  loadDeptUsers(currentDept.value.id)
}

const handleUserPageSizeChange = (pageSize: number) => {
  userQueryParams.size = pageSize
  userQueryParams.current = 1
  if (!currentDept.value) return
  loadDeptUsers(currentDept.value.id)
}

const handleAddUser = () => {
  if (!currentDept.value) {
    message.warning('请先选择组织机构')
    return
  }
  selectedAssignUserIds.value = []
  showAssignUsersModal.value = true
}

const handleEditUser = (row: UserVO) => {
  isEditUser.value = true
  editingUserId.value = row.id
  Object.assign(userFormData, {
    username: row.username,
    password: '',
    nickname: row.nickname || '',
    email: row.email || '',
    phone: row.phone || '',
    gender: row.gender,
    status: row.status,
    deptId: row.deptId
  })
  showUserModal.value = true
}

const handleSubmitUser = async () => {
  if (!currentDept.value) {
    message.warning('请先选择组织机构')
    return
  }

  try {
    await userFormRef.value?.validate()
  } catch {
    return
  }

  userSubmitting.value = true
  try {
    if (isEditUser.value) {
      const updateData: UserUpdateDTO = {
        username: userFormData.username,
        nickname: userFormData.nickname,
        email: userFormData.email,
        phone: userFormData.phone,
        gender: userFormData.gender,
        status: userFormData.status
      }
      await updateUser(editingUserId.value, updateData)
      message.success('更新成功')
    } else {
      const createData: UserCreateDTO = {
        username: userFormData.username!,
        password: userFormData.password!,
        nickname: userFormData.nickname,
        email: userFormData.email,
        phone: userFormData.phone,
        gender: userFormData.gender,
        deptId: currentDept.value.id
      }
      await createUser(createData)
      message.success('创建成功')
    }
    showUserModal.value = false
    await loadDeptUsers(currentDept.value.id, true)
  } catch (error) {
    message.error(isEditUser.value ? '更新失败' : '创建失败')
  } finally {
    userSubmitting.value = false
  }
}

const handleDeleteUser = async (row: UserVO) => {
  try {
    await deleteUser(row.id)
    message.success('删除成功')
    if (currentDept.value) {
      await loadDeptUsers(currentDept.value.id, true)
    }
  } catch (error) {
    message.error('删除失败')
  }
}

const handleAssignUsersToDept = async () => {
  if (!currentDept.value) {
    message.warning('请先选择组织机构')
    return
  }
  if (selectedAssignUserIds.value.length === 0) {
    message.warning('请选择用户')
    return
  }
  assigningUsers.value = true
  try {
    const deptId = currentDept.value.id
    const tasks = selectedAssignUserIds.value.map((userId) =>
      updateUser(userId, { deptId } as UserUpdateDTO)
    )
    await Promise.all(tasks)
    message.success('添加成功')
    showAssignUsersModal.value = false
    await loadDeptUsers(deptId, true)
  } catch (error) {
    message.error('添加失败')
  } finally {
    assigningUsers.value = false
  }
}

onMounted(() => {
  loadDeptTree()
})
</script>
