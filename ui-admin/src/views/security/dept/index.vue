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
          </n-collapse-transition>
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
          <n-button type="primary" :loading="savingDept" @click="handleSaveDept">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="showLeaderPicker"
      title="选择负责人"
      preset="card"
      style="width: 720px"
      :mask-closable="false"
    >
      <div class="space-y-4">
        <n-form inline :model="leaderQueryParams" label-placement="left" class="flex flex-wrap gap-4">
          <n-form-item label="用户名" :show-feedback="false">
            <n-input v-model:value="leaderQueryParams.username" placeholder="请输入用户名" clearable class="!w-40" />
          </n-form-item>
          <n-form-item label="昵称" :show-feedback="false">
            <n-input v-model:value="leaderQueryParams.nickname" placeholder="请输入昵称" clearable class="!w-40" />
          </n-form-item>
          <n-form-item :show-feedback="false">
            <n-space>
              <n-button type="primary" size="small" @click="handleSearchLeaders">
                <template #icon><n-icon><SearchOutline /></n-icon></template>
                搜索
              </n-button>
              <n-button size="small" @click="handleResetLeaders">
                <template #icon><n-icon><RefreshOutline /></n-icon></template>
                重置
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>

        <n-data-table
          :columns="leaderColumns"
          :data="leaderList"
          :loading="loadingLeaders"
          :pagination="leaderPagination"
          :row-key="(row: UserVO) => row.id"
          :single-line="false"
          :bordered="false"
          :checked-row-keys="leaderSelectedUserIds"
          checkable
          @update:page="handleLeaderPageChange"
          @update:page-size="handleLeaderPageSizeChange"
          @update:checked-row-keys="handleLeaderSelectionChange"
        />
      </div>
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
      v-model:show="showUserPicker"
      title="选择用户"
      preset="card"
      style="width: 800px"
      :mask-closable="false"
    >
      <UserPicker
        v-model:value="selectedUserIds"
        :existing-user-ids="existingUserIds"
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showUserPicker = false">取消</n-button>
          <n-button
            type="primary"
            :loading="savingUsers"
            :disabled="selectedUserIds.length === 0"
            @click="handleConfirmAddUsers"
          >
            确定 ({{ selectedUserIds.length }})
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
  NTreeSelect,
  NInputNumber,
  NPopconfirm,
  NIcon,
  NText,
  NTabs,
  NTabPane,
  NDescriptions,
  NDescriptionsItem,
  NGrid,
  NGi,
  NRadioGroup,
  NRadio,
  NSpin,
  NAvatar,
  useMessage,
  type FormInst,
  type FormRules,
  type TreeOption,
  type TreeSelectOption,
  type DataTableColumns, NCollapseTransition
} from 'naive-ui'
import {SearchOutline, RefreshOutline, ChevronDownOutline} from '@vicons/ionicons5'
import { getDeptTree, createDept, updateDept, deleteDept, getDeptUsers, assignUsersToDept, removeUsersFromDept, type DeptVO, type DeptCreateDTO, type DeptUpdateDTO, type DeptUserQueryDTO } from '@/api/dept'
import { getUserPage, type UserVO, type UserQueryDTO, type PageResult } from '@/api/user'
import { setPosition, type SetPositionDTO } from '@/api/dept'
import UserPicker from '@/components/UserPicker.vue'

const message = useMessage()

void NGrid
void NGi
void NSpin

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '停用', value: 1 }
]

const showSearchForm = ref(false)

const queryParams = reactive({
  deptName: '',
  status: undefined as number | undefined
})

const deptTree = ref<DeptVO[]>([])
const deptTreeData = ref<TreeOption[]>([])
const selectedDeptKeys = ref<string[]>([])
const currentDept = ref<DeptVO | null>(null)
const deptCount = ref(0)
const activeTab = ref('basic')

const buildTreeData = (nodes: DeptVO[]): TreeOption[] => {
  return nodes.map(node => ({
    key: node.id,
    label: node.deptName,
    children: node.children ? buildTreeData(node.children) : undefined
  }))
}

const loadDeptTree = async () => {
  try {
    const res = await getDeptTree({
      deptName: queryParams.deptName || undefined,
      status: queryParams.status
    })
    deptTree.value = res
    deptTreeData.value = buildTreeData(res)
    deptCount.value = res.length
    if (currentDept.value) {
      const exists = findDeptById(res, currentDept.value.id)
      if (!exists) {
        currentDept.value = null
        selectedDeptKeys.value = []
      }
    }
  } catch (error) {
    message.error('加载组织机构失败')
  }
}

const findDeptById = (list: DeptVO[], id: string): DeptVO | null => {
  for (const item of list) {
    if (item.id === id) return item
    if (item.children) {
      const found = findDeptById(item.children, id)
      if (found) return found
    }
  }
  return null
}

const parentDeptName = computed(() => {
  if (!currentDept.value) return '-'
  if (currentDept.value.parentId === '0') return '根节点'
  const parent = findDeptById(deptTree.value, currentDept.value.parentId)
  return parent ? parent.deptName : '-'
})

const handleSearch = () => {
  loadDeptTree()
}

const handleReset = () => {
  queryParams.deptName = ''
  queryParams.status = undefined
  loadDeptTree()
}

const handleDeptTreeSelect = (keys: string[]) => {
  selectedDeptKeys.value = keys
  if (keys.length > 0) {
    const key = keys[0]
    if (!key) {
      currentDept.value = null
      return
    }
    const dept = findDeptById(deptTree.value, key)
    currentDept.value = dept
    activeTab.value = 'basic'
    if (dept) {
      currentDeptId.value = dept.id
      handleSearchUsers()
    }
  } else {
    currentDept.value = null
  }
}

const handleRefreshDeptTree = () => {
  loadDeptTree()
}

const showDeptModal = ref(false)
const deptModalTitle = ref('新增部门')
const deptFormRef = ref<FormInst | null>(null)
const savingDept = ref(false)
const isEditDept = ref(false)
const editingDeptId = ref<string | null>(null)

const deptFormData = reactive<DeptCreateDTO & DeptUpdateDTO>({
  parentId: '0',
  deptName: '',
  sort: 0,
  leader: '',
  phone: '',
  email: '',
  status: 0
})

const deptFormRules: FormRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const deptTreeOptions = computed<TreeSelectOption[]>(() => {
  const options: TreeSelectOption[] = [{ key: '0', label: '根节点' }]
  const buildOptions = (nodes: DeptVO[]): TreeSelectOption[] =>
    nodes.map(node => ({
      key: node.id,
      label: node.deptName,
      children: node.children ? buildOptions(node.children) : undefined
    }))
  options.push(...buildOptions(deptTree.value))
  return options
})

const handleAddDeptRoot = () => {
  isEditDept.value = false
  deptModalTitle.value = '新增部门'
  editingDeptId.value = null
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
  if (!currentDept.value) return
  isEditDept.value = false
  deptModalTitle.value = '新增子部门'
  editingDeptId.value = null
  Object.assign(deptFormData, {
    parentId: currentDept.value.id,
    deptName: '',
    sort: currentDept.value.sort + 1,
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
  deptModalTitle.value = '编辑部门'
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

const handleSaveDept = async () => {
  try {
    await deptFormRef.value?.validate()
  } catch {
    return
  }

  savingDept.value = true
  try {
    if (isEditDept.value && editingDeptId.value) {
      const payload: DeptUpdateDTO = {
        parentId: deptFormData.parentId,
        deptName: deptFormData.deptName,
        sort: deptFormData.sort,
        leader: deptFormData.leader,
        phone: deptFormData.phone,
        email: deptFormData.email,
        status: deptFormData.status
      }
      await updateDept(editingDeptId.value, payload)
      message.success('更新成功')
    } else {
      const payload: DeptCreateDTO = {
        parentId: deptFormData.parentId,
        deptName: deptFormData.deptName,
        sort: deptFormData.sort,
        leader: deptFormData.leader || undefined,
        phone: deptFormData.phone || undefined,
        email: deptFormData.email || undefined
      }
      await createDept(payload)
      message.success('创建成功')
    }
    showDeptModal.value = false
    loadDeptTree()
  } catch (error) {
    message.error('保存失败')
  } finally {
    savingDept.value = false
  }
}

const handleDeleteDept = async () => {
  if (!currentDept.value) return
  try {
    await deleteDept(currentDept.value.id)
    message.success('删除成功')
    currentDept.value = null
    selectedDeptKeys.value = []
    loadDeptTree()
  } catch (error) {
    message.error('删除失败')
  }
}

const showLeaderPicker = ref(false)
const leaderQueryParams = reactive<UserQueryDTO>({
  roleId: '',
  username: undefined,
  nickname: undefined,
  current: 1,
  size: 10
} as any)

const leaderList = ref<UserVO[]>([])
const loadingLeaders = ref(false)
const leaderTotal = ref(0)
const leaderSelectedUserIds = ref<string[]>([])
const pickingLeader = ref(false)

const leaderPagination = computed(() => ({
  page: leaderQueryParams.current,
  pageSize: leaderQueryParams.size,
  pageCount: Math.ceil(leaderTotal.value / (leaderQueryParams.size || 10)),
  itemCount: leaderTotal.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
}))

const leaderColumns: DataTableColumns<UserVO> = [
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
    title: '手机号',
    key: 'phone',
    width: 120,
    render: row => row.phone || '-'
  }
]

const loadLeaders = async () => {
  loadingLeaders.value = true
  try {
    const res: PageResult<UserVO> = await getUserPage(leaderQueryParams)
    leaderList.value = res.records
    leaderTotal.value = res.total
  } catch (error) {
    message.error('加载用户失败')
  } finally {
    loadingLeaders.value = false
  }
}

const openLeaderPicker = () => {
  showLeaderPicker.value = true
  leaderSelectedUserIds.value = []
  loadLeaders()
}

const clearLeader = () => {
  deptFormData.leader = ''
}

const handleLeaderSelectionChange = (keys: Array<string | number>) => {
  leaderSelectedUserIds.value = keys.map(key => String(key))
}

const handleLeaderPageChange = (page: number) => {
  leaderQueryParams.current = page
  loadLeaders()
}

const handleLeaderPageSizeChange = (size: number) => {
  leaderQueryParams.size = size
  leaderQueryParams.current = 1
  loadLeaders()
}

const handleSearchLeaders = () => {
  leaderQueryParams.current = 1
  loadLeaders()
}

const handleResetLeaders = () => {
  leaderQueryParams.username = undefined
  leaderQueryParams.nickname = undefined
  leaderQueryParams.current = 1
  loadLeaders()
}

const handleConfirmLeader = async () => {
  if (!leaderSelectedUserIds.value.length) return
  const user = leaderList.value.find(item => item.id === leaderSelectedUserIds.value[0])
  if (!user) return
  deptFormData.leader = user.nickname || user.username
  showLeaderPicker.value = false
}

const userList = ref<UserVO[]>([])
const loadingUsers = ref(false)
const userTotal = ref(0)
const currentDeptId = ref('')

const userQueryParams = reactive<DeptUserQueryDTO>({
  username: undefined,
  nickname: undefined,
  status: undefined,
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
    width: 100
  },
  {
    title: '昵称',
    key: 'nickname',
    width: 100,
    render: row => row.nickname || '-'
  },
  {
    title: '头像',
    key: 'avatar',
    width: 60,
    render: row =>
      h(NAvatar, {
        size: 'small',
        round: true,
        src: row.avatar || undefined,
        fallbackSrc: 'https://ui-avatars.com/api/?name=' + (row.nickname || row.username)
      })
  },
  {
    title: '职务',
    key: 'position',
    width: 100,
    render(row, index) {
      return h(NInput, {
        value: row.position,
        onUpdateValue(v) {
          const user = userList.value[index]
          if (user) {
            user.position = v
            setPosition({
              userId: user.id,
              position: user.position,
              deptId: currentDeptId.value
            })
          }
        },
      })
    }
  },
  {
    title: '手机号',
    key: 'phone',
    width: 120,
    render: row => row.phone || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 70,
    render: row =>
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
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row) =>
        h(NSpace, { size: 'small' }, () => [
          h(
              NPopconfirm,
              {
                onPositiveClick: () => handleRemoveUser(row.id),
              },
              {
                trigger: () =>
                    h(
                        NButton,
                        {
                          size: 'tiny',
                          type: 'error',
                          quaternary: true,
                        },
                        { default: () => '移除' }
                    ),
                default: () => '确定要从该部门移除此用户吗？',
              }
          ),
        ]),
  },
]

const loadUsersByDept = async () => {
  if (!currentDeptId.value) {
    userList.value = []
    userTotal.value = 0
    return
  }
  loadingUsers.value = true
  try {
    const res: PageResult<UserVO> = await getDeptUsers(currentDeptId.value, userQueryParams)
    userList.value = res.records
    userTotal.value = res.total
  } catch (error) {
    message.error('加载用户失败')
  } finally {
    loadingUsers.value = false
  }
}

const handleSearchUsers = () => {
  userQueryParams.current = 1
  loadUsersByDept()
}

const handleUserPageChange = (page: number) => {
  userQueryParams.current = page
  loadUsersByDept()
}

const handleUserPageSizeChange = (size: number) => {
  userQueryParams.size = size
  userQueryParams.current = 1
  loadUsersByDept()
}

const showUserPicker = ref(false)
const selectedUserIds = ref<string[]>([])
const savingUsers = ref(false)

const existingUserIds = computed(() => userList.value.map(u => u.id))

const handleAddUser = () => {
  if (!currentDept.value) {
    message.warning('请先选择部门')
    return
  }
  selectedUserIds.value = []
  showUserPicker.value = true
}

const handleConfirmAddUsers = async () => {
  if (!currentDept.value || selectedUserIds.value.length === 0) return
  savingUsers.value = true
  try {
    await assignUsersToDept({
      userIds: selectedUserIds.value,
      deptId: currentDept.value.id
    })
    message.success('添加用户成功')
    showUserPicker.value = false
    selectedUserIds.value = []
    handleSearchUsers()
  } catch (error) {
    message.error('添加用户失败')
  } finally {
    savingUsers.value = false
  }
}

const handleRemoveUser = async (userId: string) => {
  if (!currentDept.value) return
  try {
    await removeUsersFromDept(currentDept.value.id, { userIds: [userId] })
    message.success('移除成功')
    handleSearchUsers()
  } catch (error) {
    message.error('移除失败')
  }
}

onMounted(() => {
  loadDeptTree()
})
</script>
