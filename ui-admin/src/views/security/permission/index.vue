<template>
  <div class="p-6 animate-enter">
    <div class="flex gap-6 h-[calc(100vh-140px)]">
      <!-- Left Panel: Search & Permission List -->
      <div class="glass-card !p-5 w-96 flex-shrink-0 flex flex-col">
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
              <n-form-item label="权限名称" :show-feedback="false">
                <n-input v-model:value="queryParams.permName" placeholder="请输入权限名称" clearable class="!w-36" />
              </n-form-item>
              <n-form-item label="权限编码" :show-feedback="false">
                <n-input v-model:value="queryParams.permCode" placeholder="请输入权限编码" clearable class="!w-36" />
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

        <div class="flex-1 flex flex-col overflow-hidden">
          <div class="flex justify-between items-center mb-3">
            <div class="flex items-center gap-2">
              <h3 class="text-lg font-semibold text-text-main">权限列表</h3>
              <n-tag size="small" round :bordered="false" type="info">{{ total }} 条</n-tag>
            </div>
            <n-button type="primary" size="small" @click="handleAdd">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              新增权限
            </n-button>
          </div>
          <div class="flex-1 overflow-y-auto permission-list-scroll">
            <n-spin :show="loading">
              <div class="space-y-2">
                <div
                  v-for="perm in permissionList"
                  :key="perm.id"
                  :class="[
                    'p-3 rounded-lg cursor-pointer transition-all border group',
                    selectedPermission?.id === perm.id
                      ? 'bg-blue-500/10 border-blue-400/50'
                      : 'bg-white/5 border-transparent hover:bg-white/10'
                  ]"
                  @click="handleSelectPermission(perm)"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <n-icon size="16" class="text-blue-500"><KeyOutline /></n-icon>
                      <span class="font-medium text-text-main">{{ perm.permName }}</span>
                    </div>
                    <n-tag :type="perm.status === 0 ? 'success' : 'error'" size="tiny">
                      {{ perm.status === 0 ? '正常' : '停用' }}
                    </n-tag>
                  </div>
                  <div class="flex items-center justify-between mt-1">
                    <n-text code class="text-xs text-gray-500">{{ perm.permCode }}</n-text>
                    <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop>
                      <n-button text size="tiny" type="primary" @click="handleEdit(perm)">
                        <template #icon><n-icon :size="14"><CreateOutline /></n-icon></template>
                      </n-button>
                      <n-popconfirm @positive-click="handleDelete(perm)">
                        <template #trigger>
                          <n-button text size="tiny" type="error">
                            <template #icon><n-icon :size="14"><TrashOutline /></n-icon></template>
                          </n-button>
                        </template>
                        确定删除权限 "{{ perm.permName }}" 吗？
                      </n-popconfirm>
                    </div>
                  </div>
                </div>
                <div v-if="permissionList.length === 0 && !loading" class="text-center py-8 text-gray-400">
                  暂无数据
                </div>
              </div>
            </n-spin>
          </div>
          <!-- Pagination -->
          <div class="mt-3 pt-3 border-t border-gray-200/10">
            <n-pagination
              v-model:page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              :page-count="Math.ceil(total / queryParams.pageSize)"
              :page-sizes="[10, 20, 50]"
              size="small"
              show-size-picker
              @update:page="loadPermissions"
              @update:page-size="handlePageSizeChange"
            />
          </div>
        </div>
      </div>

      <!-- Right Panel: Permission Details -->
      <div class="glass-card !p-6 flex-1 flex flex-col overflow-hidden">
        <template v-if="selectedPermission">
          <!-- Header -->
          <div class="flex justify-between items-start mb-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-green-500/20 to-blue-500/20 flex items-center justify-center">
                <n-icon :size="24" class="text-green-500"><KeyOutline /></n-icon>
              </div>
              <div>
                <h2 class="text-xl font-bold text-text-main">{{ selectedPermission.permName }}</h2>
                <div class="flex items-center gap-2 mt-1">
                  <n-text code>{{ selectedPermission.permCode }}</n-text>
                  <n-tag :type="selectedPermission.status === 0 ? 'success' : 'error'" size="small">
                    {{ selectedPermission.status === 0 ? '正常' : '停用' }}
                  </n-tag>
                </div>
              </div>
            </div>
            <n-space>
              <n-button type="primary" size="small" @click="handleEdit(selectedPermission)">
                <template #icon><n-icon><CreateOutline /></n-icon></template>
                编辑
              </n-button>
              <n-popconfirm @positive-click="handleDelete(selectedPermission)">
                <template #trigger>
                  <n-button type="error" size="small">
                    <template #icon><n-icon><TrashOutline /></n-icon></template>
                    删除
                  </n-button>
                </template>
                确定要删除权限 "{{ selectedPermission.permName }}" 吗？
              </n-popconfirm>
            </n-space>
          </div>

          <!-- Tabs -->
          <n-tabs
            v-model:value="activeTab"
            type="line"
            animated
            class="flex-1 flex flex-col overflow-hidden"
            pane-wrapper-style="flex: 1; overflow: hidden; display: flex; flex-direction: column;"
            pane-style="flex: 1; display: flex; flex-direction: column; overflow: hidden;"
          >
            <n-tab-pane name="basic" tab="基本信息">
              <div class="flex-1 overflow-y-auto pt-2">
                <n-descriptions :column="2" label-placement="left" bordered size="small">
                  <n-descriptions-item label="权限ID" :span="2">
                    <n-text code>{{ selectedPermission.id }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="权限名称" :span="2">
                    {{ selectedPermission.permName }}
                  </n-descriptions-item>
                  <n-descriptions-item label="权限编码" :span="2">
                    <n-text code>{{ selectedPermission.permCode }}</n-text>
                  </n-descriptions-item>
                  <n-descriptions-item label="描述" :span="2">
                    {{ selectedPermission.description || '-' }}
                  </n-descriptions-item>
                  <n-descriptions-item label="排序">
                    {{ selectedPermission.sort }}
                  </n-descriptions-item>
                  <n-descriptions-item label="状态">
                    <n-tag :type="selectedPermission.status === 0 ? 'success' : 'error'" size="small">
                      {{ selectedPermission.status === 0 ? '正常' : '停用' }}
                    </n-tag>
                  </n-descriptions-item>
                  <n-descriptions-item label="创建时间" :span="2">
                    {{ selectedPermission.createTime?.replace('T', ' ').substring(0, 19) || '-' }}
                  </n-descriptions-item>
                </n-descriptions>

                <!-- Usage Tips -->
                <div class="mt-6 p-4 rounded-lg bg-blue-500/10 border border-blue-500/20">
                  <h4 class="font-semibold text-blue-400 mb-2">使用说明</h4>
                  <ul class="text-sm text-gray-400 space-y-1">
                    <li>在<n-text code> 角色管理 </n-text>中为角色分配权限</li>
                    <li>在<n-text code> 关联资源 </n-text>页签中为权限分配资源</li>
                  </ul>
                </div>
              </div>
            </n-tab-pane>

            <n-tab-pane name="resources" tab="关联资源">
              <div class="flex-1 overflow-y-auto pt-2">
                <div class="flex justify-between items-center mb-4">
                  <div class="flex items-center gap-2">
                    <span class="text-sm text-gray-500">已关联</span>
                    <n-tag size="small" round :bordered="false" type="info">{{ assignedResources.length }} 个资源</n-tag>
                  </div>
                  <n-button size="small" type="primary" @click="handleAssignResources">
                    <template #icon><n-icon><LinkOutline /></n-icon></template>
                    分配资源
                  </n-button>
                </div>
                <div v-if="assignedResources.length > 0" class="space-y-2">
                  <div v-for="res in assignedResources" :key="res.id" class="flex items-center justify-between p-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors border border-gray-200/10 group">
                    <div class="flex items-center gap-2 flex-1 min-w-0">
                      <n-tag :type="res.resType === 'API' ? 'info' : 'warning'" size="small">
                        {{ res.resType }}
                      </n-tag>
                      <span class="text-sm text-text-main font-medium">{{ res.resName }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <n-text code class="text-xs text-gray-500">{{ res.resPath }}</n-text>
                      <n-tag v-if="res.resType === 'API'" :type="getMethodType(res.resMethod)" size="tiny">
                        {{ res.resMethod }}
                      </n-tag>
                      <n-popconfirm @positive-click="handleRemoveResource(res.id)">
                        <template #trigger>
                          <n-button
                            text
                            size="tiny"
                            type="error"
                            class="opacity-0 group-hover:opacity-100 transition-opacity ml-2"
                          >
                            <template #icon><n-icon :size="14"><CloseOutline /></n-icon></template>
                          </n-button>
                        </template>
                        确定移除该资源吗？
                      </n-popconfirm>
                    </div>
                  </div>
                </div>
                <div v-else class="flex flex-col items-center justify-center py-12 text-gray-400">
                  <n-icon size="48" class="mb-3 opacity-30"><LinkOutline /></n-icon>
                  <p>暂无关联资源</p>
                  <p class="text-xs mt-1">点击「分配资源」按钮添加资源</p>
                </div>
              </div>
            </n-tab-pane>
          </n-tabs>
        </template>

        <!-- Empty State -->
        <template v-else>
          <div class="flex-1 flex flex-col items-center justify-center text-gray-400">
            <n-icon size="64" class="mb-4 opacity-30">
              <KeyOutline />
            </n-icon>
            <p class="text-lg">请从左侧选择一个权限</p>
            <p class="text-sm mt-2">或点击「新增权限」按钮创建权限</p>
          </div>
        </template>
      </div>
    </div>

    <!-- Add/Edit Modal -->
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
        <n-form-item label="权限名称" path="permName">
          <n-input v-model:value="formData.permName" placeholder="请输入权限名称，如 用户查询" />
        </n-form-item>
        <n-form-item label="权限编码" path="permCode">
          <n-input v-model:value="formData.permCode" placeholder="请输入权限编码，如 user:read" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input
            v-model:value="formData.description"
            type="textarea"
            placeholder="请输入权限描述"
            :rows="2"
          />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" :max="9999" class="!w-full" />
        </n-form-item>
        <n-form-item v-if="isEdit" label="状态" path="status">
          <n-radio-group v-model:value="formData.status">
            <n-space>
              <n-radio :value="0">正常</n-radio>
              <n-radio :value="1">停用</n-radio>
            </n-space>
          </n-radio-group>
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

    <!-- Resource Assignment Modal -->
    <n-modal
      v-model:show="showResourceModal"
      title="分配资源"
      preset="card"
      style="width: 800px; height: 600px;"
      :mask-closable="false"
      class="flex flex-col"
    >
      <div class="flex h-full gap-4">
        <!-- Left: Resource Groups -->
        <div class="w-1/3 border-r pr-4 flex flex-col">
          <n-input v-model:value="resourceGroupSearch" placeholder="搜索资源组" class="mb-2" clearable>
            <template #prefix><n-icon><SearchOutline /></n-icon></template>
          </n-input>
          <div class="flex-1 overflow-y-auto">
            <n-spin :show="loadingGroups">
              <div class="space-y-1">
                <div
                  v-for="group in filteredResourceGroups"
                  :key="group.id"
                  :class="[
                    'p-2 rounded cursor-pointer text-sm flex justify-between items-center',
                    selectedResourceGroup?.id === group.id ? 'bg-blue-500/10 text-blue-500' : 'hover:bg-gray-100 dark:hover:bg-white/5'
                  ]"
                  @click="handleSelectResourceGroup(group)"
                >
                  <span>{{ group.resGroupName }}</span>
                  <n-tag size="tiny" :bordered="false">{{ group.resourceCount }}</n-tag>
                </div>
              </div>
            </n-spin>
          </div>
        </div>

        <!-- Right: Resources -->
        <div class="flex-1 flex flex-col">
          <div class="mb-2 flex justify-between items-center">
             <n-input v-model:value="resourceSearch" placeholder="搜索资源" class="w-48" size="small" clearable>
               <template #prefix><n-icon><SearchOutline /></n-icon></template>
             </n-input>
             <div class="text-xs text-gray-500">
               已选 {{ checkedResourceIds.length }} 个资源
             </div>
          </div>
          <div class="flex-1 overflow-y-auto border rounded p-2">
             <n-spin :show="loadingResources">
               <n-checkbox-group v-model:value="checkedResourceIds">
                 <div class="space-y-2">
                   <div v-if="!selectedResourceGroup" class="text-center text-gray-400 py-10">
                     请选择左侧资源组
                   </div>
                   <div v-else-if="filteredResources.length === 0" class="text-center text-gray-400 py-10">
                     暂无资源
                   </div>
                   <div v-else v-for="res in filteredResources" :key="res.id" class="flex items-start gap-2 p-2 hover:bg-gray-50 dark:hover:bg-white/5 rounded">
                     <n-checkbox :value="res.id" class="mt-1" />
                     <div class="flex-1">
                       <div class="flex items-center gap-2">
                         <span class="font-medium">{{ res.resName }}</span>
                         <n-tag :type="res.resType === 'API' ? 'info' : 'warning'" size="tiny" :bordered="false">
                           {{ res.resType }}
                         </n-tag>
                         <n-tag v-if="res.resType === 'API'" :type="getMethodType(res.resMethod)" size="tiny" :bordered="false">
                           {{ res.resMethod }}
                         </n-tag>
                       </div>
                       <div class="text-xs text-gray-500 mt-1 font-mono break-all">
                         {{ res.resPath }}
                       </div>
                     </div>
                   </div>
                 </div>
               </n-checkbox-group>
             </n-spin>
          </div>
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showResourceModal = false">取消</n-button>
          <n-button type="primary" :loading="submittingResources" @click="handleSubmitResources">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import {
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NButton,
  NSpace,
  NSelect,
  NIcon,
  NTag,
  NText,
  NModal,
  NRadio,
  NRadioGroup,
  NDescriptions,
  NDescriptionsItem,
  NSpin,
  NPagination,
  NPopconfirm,
  NCheckbox,
  NCheckboxGroup,
  NCollapseTransition,
  NTabs,
  NTabPane,
  useMessage,
  type FormInst,
  type FormRules,
  type TagProps
} from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  CreateOutline,
  TrashOutline,
  KeyOutline,
  LinkOutline,
  ChevronDownOutline,
  CloseOutline
} from '@vicons/ionicons5'
import {
  getPermissionPage,
  createPermission,
  updatePermission,
  deletePermission,
  getPermissionResourceIds,
  assignResourcesToPermission,
  type PermissionVO,
  type PermissionQueryDTO,
  type PermissionCreateDTO,
  type PermissionUpdateDTO
} from '@/api/permission'
import {
  getResourceGroupList,
  type ResourceGroupVO
} from '@/api/resource-group'
import {
  getResourcePage,
  getResourceDetail,
  type ResourceVO
} from '@/api/resource'

const message = useMessage()

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '停用', value: 1 }
]

// Search form visibility (default collapsed)
const showSearchForm = ref(false)

// Query params
const queryParams = reactive({
  permName: undefined as string | undefined,
  permCode: undefined as string | undefined,
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

// List state
const loading = ref(false)
const total = ref(0)
const permissionList = ref<PermissionVO[]>([])
const selectedPermission = ref<PermissionVO | null>(null)
const activeTab = ref('basic')

// Modal state
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = computed(() => isEdit.value ? '编辑权限' : '新增权限')
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const editingId = ref<string>('')

const formData = reactive<PermissionCreateDTO & PermissionUpdateDTO>({
  permName: '',
  permCode: '',
  description: '',
  sort: 0,
  status: 0
})

const formRules: FormRules = {
  permName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2-50 个字符', trigger: 'blur' }
  ],
  permCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2-100 个字符', trigger: 'blur' }
  ]
}

// Resource Assignment State
const showResourceModal = ref(false)
const loadingGroups = ref(false)
const resourceGroups = ref<ResourceGroupVO[]>([])
const resourceGroupSearch = ref('')
const selectedResourceGroup = ref<ResourceGroupVO | null>(null)
const loadingResources = ref(false)
const groupResources = ref<ResourceVO[]>([])
const resourceSearch = ref('')
const checkedResourceIds = ref<string[]>([])
const submittingResources = ref(false)
const assignedResources = ref<ResourceVO[]>([]) // To display in details panel

// Load permissions
const loadPermissions = async () => {
  loading.value = true
  try {
    const params: PermissionQueryDTO = {
      permName: queryParams.permName,
      permCode: queryParams.permCode,
      status: queryParams.status
    }
    const res = await getPermissionPage(queryParams.pageNum, queryParams.pageSize, params)
    permissionList.value = res.records
    total.value = res.total

    // If selected permission is not in list, clear selection
    if (selectedPermission.value) {
      const exists = res.records.find(p => p.id === selectedPermission.value?.id)
      if (!exists) {
        selectedPermission.value = null
      }
    }
  } catch (error) {
    message.error('加载权限列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadPermissions()
}

const handleReset = () => {
  queryParams.permName = undefined
  queryParams.permCode = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  loadPermissions()
}

const handlePageSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadPermissions()
}

const handleSelectPermission = async (perm: PermissionVO) => {
  selectedPermission.value = perm
  await loadAssignedResources(perm.id)
}

// Add permission
const handleAdd = () => {
  isEdit.value = false
  editingId.value = ''
  Object.assign(formData, {
    permName: '',
    permCode: '',
    description: '',
    sort: 0,
    status: 0
  })
  showModal.value = true
}

// Edit permission
const handleEdit = (perm: PermissionVO) => {
  isEdit.value = true
  editingId.value = perm.id
  Object.assign(formData, {
    permName: perm.permName,
    permCode: perm.permCode,
    description: perm.description || '',
    sort: perm.sort,
    status: perm.status
  })
  showModal.value = true
}

// Submit form
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      const data: PermissionUpdateDTO = {
        permName: formData.permName,
        permCode: formData.permCode,
        description: formData.description || undefined,
        sort: formData.sort,
        status: formData.status
      }
      await updatePermission(editingId.value, data)
      message.success('更新成功')
    } else {
      const data: PermissionCreateDTO = {
        permName: formData.permName,
        permCode: formData.permCode,
        description: formData.description || undefined,
        sort: formData.sort
      }
      await createPermission(data)
      message.success('创建成功')
    }
    showModal.value = false
    loadPermissions()
  } catch (error) {
    message.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// Delete permission
const handleDelete = async (perm: PermissionVO) => {
  try {
    await deletePermission(perm.id)
    message.success('删除成功')
    if (selectedPermission.value?.id === perm.id) {
      selectedPermission.value = null
    }
    loadPermissions()
  } catch (error) {
    message.error('删除失败')
  }
}

// Resource Assignment Logic
const loadAssignedResources = async (permId: string) => {
  try {
    const ids = await getPermissionResourceIds(permId)
    // Since we only get IDs, we might need to fetch details or just store IDs if we don't want to fetch all details
    // For display purposes in the card, ideally we should have resource details.
    // However, fetching details for all IDs one by one is inefficient.
    // A better approach would be an API that returns List<ResourceVO> for a permission.
    // Assuming we only have getPermissionResourceIds for now, we can try to fetch details if the list is small,
    // or maybe we need to rely on what we have.
    // For now, let's just store IDs and maybe we can't display full details in the card without extra API calls.
    // Wait, I can use getResourcePage to search by IDs if the API supported it, but it doesn't seem to.
    // Let's assume for now we just want to show the count or we need to fetch them.
    // Actually, let's try to fetch details for the IDs.
    // Optimization: If the list is long, this will be slow.
    // Let's just fetch the IDs for the modal state.
    // For the display card, we might need a new API endpoint or just show the count.
    // But the requirement says "add a card panel to add resources", implying we should see them.
    // Let's try to fetch details for a few of them or all if not too many.

    // NOTE: In a real scenario, I would ask backend to provide `getPermissionResources(permId)`.
    // Since I cannot change backend easily (I am assuming), I will try to fetch details for the IDs.
    // But wait, I can only call `getResourceDetail`.
    // Let's just fetch the IDs for now and maybe not show full details in the card unless I have them.
    // Or, I can load all resources when opening the modal.

    // Let's implement `handleAssignResources` to open the modal and load necessary data.
    // For the card display, I will leave it empty or just show a count if I can't get details easily.
    // Actually, I can try to fetch details for the assigned resources if the number is small.

    // Let's just fetch IDs for now.
    // assignedResources.value = [] // Reset
    // const detailsPromises = ids.map(id => getResourceDetail(id))
    // const details = await Promise.all(detailsPromises)
    // assignedResources.value = details

    // To avoid too many requests, let's just fetch the IDs and when the user clicks "Assign Resources",
    // we load the groups and resources.

    // However, to show the list in the card, I really need the details.
    // I will try to fetch them.
    if (ids.length > 0) {
       const detailsPromises = ids.map(id => getResourceDetail(id))
       assignedResources.value = await Promise.all(detailsPromises)
    } else {
       assignedResources.value = []
    }

  } catch (error) {
    console.error('Failed to load assigned resources', error)
    assignedResources.value = []
  }
}

const handleAssignResources = async () => {
  if (!selectedPermission.value) return
  showResourceModal.value = true

  // Load current assignments
  try {
    const ids = await getPermissionResourceIds(selectedPermission.value.id)
    checkedResourceIds.value = ids
  } catch (error) {
    message.error('加载已分配资源失败')
  }

  // Load resource groups if not loaded
  if (resourceGroups.value.length === 0) {
    loadResourceGroups()
  }
}

const loadResourceGroups = async () => {
  loadingGroups.value = true
  try {
    resourceGroups.value = await getResourceGroupList()
  } catch (error) {
    message.error('加载资源组失败')
  } finally {
    loadingGroups.value = false
  }
}

const filteredResourceGroups = computed(() => {
  if (!resourceGroupSearch.value) return resourceGroups.value
  const keyword = resourceGroupSearch.value.toLowerCase()
  return resourceGroups.value.filter(g => g.resGroupName.toLowerCase().includes(keyword))
})

const handleSelectResourceGroup = async (group: ResourceGroupVO) => {
  selectedResourceGroup.value = group
  loadingResources.value = true
  try {
    // Fetch all resources for this group
    // We use a large page size to get all
    const res = await getResourcePage({
      groupId: group.id,
      size: 1000
    })
    groupResources.value = res.records
  } catch (error) {
    message.error('加载资源失败')
  } finally {
    loadingResources.value = false
  }
}

const filteredResources = computed(() => {
  if (!resourceSearch.value) return groupResources.value
  const keyword = resourceSearch.value.toLowerCase()
  return groupResources.value.filter(r =>
    r.resName.toLowerCase().includes(keyword) ||
    r.resPath.toLowerCase().includes(keyword)
  )
})

const handleSubmitResources = async () => {
  if (!selectedPermission.value) return
  submittingResources.value = true
  try {
    await assignResourcesToPermission(selectedPermission.value.id, checkedResourceIds.value)
    message.success('分配资源成功')
    showResourceModal.value = false
    // Reload assigned resources for display
    await loadAssignedResources(selectedPermission.value.id)
  } catch (error) {
    message.error('分配资源失败')
  } finally {
    submittingResources.value = false
  }
}

// Remove a single resource from permission
const handleRemoveResource = async (resourceId: string) => {
  if (!selectedPermission.value) return
  try {
    const newResourceIds = assignedResources.value
      .filter(r => r.id !== resourceId)
      .map(r => r.id)
    await assignResourcesToPermission(selectedPermission.value.id, newResourceIds)
    message.success('移除成功')
    await loadAssignedResources(selectedPermission.value.id)
  } catch (error) {
    message.error('移除失败')
  }
}

const getMethodType = (method: string): TagProps['type'] => {
  const map: Record<string, TagProps['type']> = {
    'GET': 'success',
    'POST': 'info',
    'PUT': 'warning',
    'DELETE': 'error',
    '*': 'default'
  }
  return map[method] || 'default'
}

onMounted(() => {
  loadPermissions()
})
</script>

<style scoped>
.permission-list-scroll {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.permission-list-scroll::-webkit-scrollbar {
  display: none;
}
</style>
