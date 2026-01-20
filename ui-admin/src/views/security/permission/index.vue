<template>
  <div class="p-6 animate-enter">
    <div class="flex gap-6 h-[calc(100vh-140px)]">
      <!-- Left Panel: Search & Permission List -->
      <div class="glass-card !p-5 w-96 flex-shrink-0 flex flex-col">
        <div class="mb-4">
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
                    'p-3 rounded-lg cursor-pointer transition-all border',
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
                  <div class="mt-1 text-xs text-gray-500">
                    <n-text code>{{ perm.permCode }}</n-text>
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
          <div class="flex justify-between items-start mb-6">
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

          <!-- Detail Info -->
          <div class="flex-1 overflow-y-auto">
            <n-descriptions :column="1" label-placement="left" bordered size="small">
              <n-descriptions-item label="权限ID">
                <n-text code>{{ selectedPermission.id }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item label="权限名称">
                {{ selectedPermission.permName }}
              </n-descriptions-item>
              <n-descriptions-item label="权限编码">
                <n-text code>{{ selectedPermission.permCode }}</n-text>
              </n-descriptions-item>
              <n-descriptions-item label="描述">
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
              <n-descriptions-item label="创建时间">
                {{ selectedPermission.createTime?.replace('T', ' ').substring(0, 19) || '-' }}
              </n-descriptions-item>
            </n-descriptions>

            <!-- Usage Tips -->
            <div class="mt-6 p-4 rounded-lg bg-blue-500/10 border border-blue-500/20">
              <h4 class="font-semibold text-blue-400 mb-2">使用说明</h4>
              <ul class="text-sm text-gray-400 space-y-1">
                <li>权限编码格式建议：<n-text code>模块:操作</n-text>，如 <n-text code>user:read</n-text></li>
                <li>在角色管理中为角色分配权限</li>
                <li>后端接口可通过 <n-text code>@PreAuthorize</n-text> 注解进行权限控制</li>
              </ul>
            </div>
          </div>
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
  useMessage,
  type FormInst,
  type FormRules
} from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  CreateOutline,
  TrashOutline,
  KeyOutline
} from '@vicons/ionicons5'
import {
  getPermissionPage,
  createPermission,
  updatePermission,
  deletePermission,
  type PermissionVO,
  type PermissionQueryDTO,
  type PermissionCreateDTO,
  type PermissionUpdateDTO
} from '@/api/permission'

const message = useMessage()

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '停用', value: 1 }
]

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
    { min: 2, max: 100, message: '长度在 2-100 个字符', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9]*:[a-z][a-z0-9]*$/, message: '格式为 模块:操作，如 user:read', trigger: 'blur' }
  ]
}

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

const handleSelectPermission = (perm: PermissionVO) => {
  selectedPermission.value = perm
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
