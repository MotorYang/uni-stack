<template>
  <div>
    <!-- Header -->
    <div class="glass-card !p-5 mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <n-button quaternary @click="$emit('back')">
            <template #icon>
              <n-icon>
                <ArrowBackOutline/>
              </n-icon>
            </template>
            返回
          </n-button>
          <n-divider vertical/>
          <div class="flex items-center gap-3">
            <div
                class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500/20 to-purple-500/20 flex items-center justify-center">
              <n-icon :size="20" class="text-blue-500">
                <FolderOutline/>
              </n-icon>
            </div>
            <div>
              <h2 class="font-semibold text-text-main">{{ group.resGroupName }}</h2>
              <div class="flex items-center gap-2 text-sm">
                <n-text code>{{ group.resGroupCode }}</n-text>
                <n-tag v-if="group.serviceName" size="small" type="info">{{ group.serviceName }}</n-tag>
              </div>
            </div>
          </div>
        </div>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <n-icon>
              <AddOutline/>
            </n-icon>
          </template>
          新增资源
        </n-button>
      </div>
    </div>

    <!-- Search Bar -->
    <div class="glass-card !p-4 mb-4">
      <n-form inline :model="queryParams" label-placement="left">
        <n-form-item label="资源名称" :show-feedback="false">
          <n-input
              v-model:value="queryParams.resName"
              placeholder="请输入资源名称"
              clearable
              style="width: 160px"
          />
        </n-form-item>
        <n-form-item label="资源类型" :show-feedback="false">
          <n-select
              v-model:value="queryParams.resType"
              :options="typeOptions"
              placeholder="请选择"
              clearable
              style="width: 120px"
          />
        </n-form-item>
        <n-form-item label="资源编码" :show-feedback="false">
          <n-input
              v-model:value="queryParams.resCode"
              placeholder="请输入资源编码"
              clearable
              style="width: 160px"
          />
        </n-form-item>
        <n-form-item label="状态" :show-feedback="false">
          <n-select
              v-model:value="queryParams.status"
              :options="statusOptions"
              placeholder="请选择"
              clearable
              style="width: 100px"
          />
        </n-form-item>
        <n-form-item :show-feedback="false">
          <n-space>
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <n-icon>
                  <SearchOutline/>
                </n-icon>
              </template>
              搜索
            </n-button>
            <n-button @click="handleReset">
              <template #icon>
                <n-icon>
                  <RefreshOutline/>
                </n-icon>
              </template>
              重置
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>

    <!-- Table -->
    <div class="glass-card !p-4">
      <n-data-table
          :columns="columns"
          :data="resourceList"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: ResourceVO) => row.id"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- Add/Edit Modal -->
    <n-modal
        v-model:show="showModal"
        :title="isEdit ? '编辑资源' : '新增资源'"
        preset="card"
        style="width: 600px"
        :mask-closable="false"
    >
      <n-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-placement="left"
          label-width="80"
      >
        <n-form-item label="资源名称" path="resName">
          <n-input v-model:value="formData.resName" placeholder="请输入资源名称"/>
        </n-form-item>
        <n-form-item label="资源类型" path="resType">
          <n-radio-group v-model:value="formData.resType" @update:value="handleTypeChange">
            <n-space>
              <n-radio value="API">API接口</n-radio>
              <n-radio value="BUTTON">按钮权限</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="资源路径" path="resPath">
          <n-input-group>
            <n-input
                v-model:value="formData.resPath"
                placeholder="请输入资源路径"
                :style="{ width: formData.resType === 'API' ? 'calc(100% - 100px)' : '100%' }"
            />
            <n-button
                v-if="formData.resType === 'API' && group.serviceName"
                type="primary"
                ghost
                @click="showApiSelector = true"
            >
              选择API
            </n-button>
          </n-input-group>
        </n-form-item>
        <n-form-item v-if="formData.resType === 'API'" label="请求方式" path="resMethod">
          <n-select
              v-model:value="formData.resMethod"
              :options="methodOptions"
              placeholder="请选择请求方式"
              style="width: 200px"
          />
        </n-form-item>
        <n-form-item label="资源编码" path="resCode">
          <n-input v-model:value="formData.resCode" placeholder="请输入资源编码"/>
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input
              v-model:value="formData.description"
              type="textarea"
              placeholder="请输入描述"
              :rows="2"
          />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" :max="9999" class="!w-full"/>
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

    <!-- API Selector Modal -->
    <n-modal
        v-model:show="showApiSelector"
        title="选择 API"
        preset="card"
        style="width: 900px"
        :mask-closable="false"
    >
      <div class="mb-4">
        <n-input
            v-model:value="apiSearchKeyword"
            placeholder="搜索 API 路径或描述"
            clearable
        >
          <template #prefix>
            <n-icon>
              <SearchOutline/>
            </n-icon>
          </template>
        </n-input>
      </div>
      <n-data-table
          :columns="apiColumns"
          :data="filteredApis"
          :loading="loadingApis"
          :max-height="400"
          :row-key="(row: ApiInfo) => row.path + '|' + row.method"
          :row-class-name="(row: ApiInfo) => selectedApi?.path === row.path && selectedApi?.method === row.method ? 'bg-blue-500/10' : ''"
          @update:checked-row-keys="handleApiSelect"
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showApiSelector = false">取消</n-button>
          <n-button type="primary" :disabled="!selectedApi" @click="confirmApiSelection">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, computed, onMounted, h, watch} from 'vue'
import {
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NInputGroup,
  NButton,
  NSpace,
  NSelect,
  NIcon,
  NTag,
  NText,
  NModal,
  NRadio,
  NRadioGroup,
  NDivider,
  NDataTable,
  NPopconfirm,
  useMessage,
  type FormInst,
  type FormRules,
  type SelectOption,
  type DataTableColumns,
  type DataTableRowKey,
  type TagProps,
} from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  CreateOutline,
  TrashOutline,
  ArrowBackOutline,
  FolderOutline
} from '@vicons/ionicons5'
import {
  getResourcePage,
  createResource,
  updateResource,
  deleteResource,
  type ResourceVO,
  type ResourceCreateDTO,
  type ResourceUpdateDTO
} from '@/api/resource'
import {getServiceApis, type ApiInfo} from '@/api/api-registry'
import type {ResourceGroupVO} from '@/api/resource-group'

const props = defineProps<{
  group: ResourceGroupVO
}>()

defineEmits<{
  back: []
}>()

const message = useMessage()

const statusOptions: SelectOption[] = [
  {label: '正常', value: 0},
  {label: '停用', value: 1}
]

const typeOptions: SelectOption[] = [
  {label: 'API接口', value: 'API'},
  {label: '按钮权限', value: 'BUTTON'}
]

const methodOptions: SelectOption[] = [
  {label: '全部 (*)', value: '*'},
  {label: 'GET', value: 'GET'},
  {label: 'POST', value: 'POST'},
  {label: 'PUT', value: 'PUT'},
  {label: 'DELETE', value: 'DELETE'}
]

// Query params
const queryParams = reactive({
  resName: undefined as string | undefined,
  resType: undefined as string | undefined,
  resCode: undefined as string | undefined,
  status: undefined as number | undefined,
  current: 1,
  size: 10
})

// List state
const loading = ref(false)
const total = ref(0)
const resourceList = ref<ResourceVO[]>([])

const pagination = computed(() => ({
  page: queryParams.current,
  pageSize: queryParams.size,
  pageCount: Math.ceil(total.value / queryParams.size),
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
}))

// Table columns
const columns: DataTableColumns<ResourceVO> = [
  {
    title: '资源名称',
    key: 'resName',
    width: 150
  },
  {
    title: '资源类型',
    key: 'resType',
    width: 100,
    render: (row) => h(NTag, {type: row.resType === 'API' ? 'info' : 'warning', size: 'small'}, () => row.resType)
  },
  {
    title: '资源路径',
    key: 'resPath',
    ellipsis: {tooltip: true},
    render: (row) => h(NText, {code: true}, () => row.resPath)
  },
  {
    title: '请求方式',
    key: 'resMethod',
    width: 100,
    render: (row) => {
      if (row.resType !== 'API') return '-'
      const colorMap: Record<string, TagProps['type']> = {
        'GET': 'success',
        'POST': 'info',
        'PUT': 'warning',
        'DELETE': 'error',
        '*': 'default'
      }
      return h(NTag, {type: colorMap[row.resMethod] || 'default', size: 'small'}, () => row.resMethod)
    }
  },
  {
    title: '资源编码',
    key: 'resCode',
    width: 180,
    ellipsis: {tooltip: true},
    render: (row) => h(NText, {code: true}, () => row.resCode)
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => h(NTag, {
      type: row.status === 0 ? 'success' : 'error',
      size: 'small'
    }, () => row.status === 0 ? '正常' : '停用')
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row) => h(NSpace, {size: 'small'}, () => [
      h(NButton, {
        text: true,
        type: 'primary',
        size: 'small',
        onClick: () => handleEdit(row)
      }, {icon: () => h(NIcon, null, () => h(CreateOutline))}),
      h(NPopconfirm, {onPositiveClick: () => handleDelete(row)}, {
        trigger: () => h(NButton, {
          text: true,
          type: 'error',
          size: 'small'
        }, {icon: () => h(NIcon, null, () => h(TrashOutline))}),
        default: () => `确定要删除资源 "${row.resName}" 吗？`
      })
    ])
  }
]

// Modal state
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const editingId = ref<string>('')

const formData = reactive<ResourceCreateDTO & ResourceUpdateDTO>({
  groupId: '',
  resName: '',
  resType: 'API',
  resPath: '',
  resCode: '',
  resMethod: '*',
  description: '',
  sort: 0,
  status: 0
})

const formRules: FormRules = {
  resName: [
    {required: true, message: '请输入资源名称', trigger: 'blur'},
    {min: 2, max: 50, message: '长度在 2-50 个字符', trigger: 'blur'}
  ],
  resType: [
    {required: true, message: '请选择资源类型', trigger: 'change'}
  ],
  resPath: [
    {required: true, message: '请输入资源路径', trigger: 'blur'}
  ],
  resCode: [
    {required: true, message: '请输入资源编码', trigger: 'blur'},
    {min: 2, max: 100, message: '长度在 2-100 个字符', trigger: 'blur'}
  ]
}

// API Selector state
const showApiSelector = ref(false)
const loadingApis = ref(false)
const apiList = ref<ApiInfo[]>([])
const apiSearchKeyword = ref('')
const selectedApi = ref<ApiInfo | null>(null)

const filteredApis = computed(() => {
  if (!apiSearchKeyword.value) return apiList.value
  const keyword = apiSearchKeyword.value.toLowerCase()
  return apiList.value.filter(api =>
      api.path.toLowerCase().includes(keyword) ||
      api.summary.toLowerCase().includes(keyword) ||
      api.description.toLowerCase().includes(keyword)
  )
})

const apiColumns: DataTableColumns<ApiInfo> = [
  {
    type: 'selection',
    multiple: false
  },
  {
    title: '路径',
    key: 'path',
    ellipsis: {tooltip: true},
    render: (row) => h(NText, {code: true}, () => row.path)
  },
  {
    title: '方法',
    key: 'method',
    width: 100,
    render: (row) => {
      const methods = row.method.split(',')
      return h(NSpace, {size: 'small'}, () =>
          methods.map(m => {
            const colorMap: Record<string, TagProps['type']> = {
              'GET': 'success',
              'POST': 'info',
              'PUT': 'warning',
              'DELETE': 'error',
              '*': 'default'
            }
            return h(NTag, {type: colorMap[m] || 'default', size: 'small'}, () => m)
          })
      )
    }
  },
  {
    title: '描述',
    key: 'summary',
    ellipsis: {tooltip: true}
  },
  {
    title: '标签',
    key: 'tags',
    width: 120,
    render: (row) => row.tags?.length ? h(NTag, {size: 'small'}, () => row.tags[0]) : '-'
  }
]

// Load APIs when selector opens
watch(showApiSelector, async (show) => {
  if (show && props.group.serviceName) {
    loadingApis.value = true
    selectedApi.value = null
    apiSearchKeyword.value = ''
    try {
      apiList.value = await getServiceApis(props.group.serviceName)
    } catch (error) {
      message.error('加载 API 列表失败')
    } finally {
      loadingApis.value = false
    }
  }
})

const handleApiSelect = (keys: DataTableRowKey[]) => {
  if (keys.length > 0) {
    const firstKey = keys[0] as string;
    const [path, method] = firstKey.split('|')
    selectedApi.value = apiList.value.find(a => a.path === path && a.method === method) || null
  } else {
    selectedApi.value = null
  }
}

const confirmApiSelection = () => {
  if (selectedApi.value) {
    formData.resPath = selectedApi.value.path
    formData.resMethod = selectedApi.value.method === '*' ? '*' : selectedApi.value.method.split(',')[0]
    if (!formData.resName && selectedApi.value.summary) {
      formData.resName = selectedApi.value.summary
    }
    showApiSelector.value = false
  }
}

// Load resources
const loadResources = async () => {
  loading.value = true
  try {
    const res = await getResourcePage({
      groupId: props.group.id,
      resName: queryParams.resName,
      resType: queryParams.resType,
      resCode: queryParams.resCode,
      status: queryParams.status,
      current: queryParams.current,
      size: queryParams.size
    })
    resourceList.value = res.records
    total.value = res.total
  } catch (error) {
    message.error('加载资源列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadResources()
}

const handleReset = () => {
  queryParams.resName = undefined
  queryParams.resType = undefined
  queryParams.resCode = undefined
  queryParams.status = undefined
  queryParams.current = 1
  loadResources()
}

const handlePageChange = (page: number) => {
  queryParams.current = page
  loadResources()
}

const handlePageSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.current = 1
  loadResources()
}

// Add resource
const handleAdd = () => {
  isEdit.value = false
  editingId.value = ''
  Object.assign(formData, {
    groupId: props.group.id,
    resName: '',
    resType: 'API',
    resPath: '',
    resCode: '',
    resMethod: '*',
    description: '',
    sort: 0,
    status: 0
  })
  showModal.value = true
}

// Edit resource
const handleEdit = (resource: ResourceVO) => {
  isEdit.value = true
  editingId.value = resource.id
  Object.assign(formData, {
    groupId: resource.groupId,
    resName: resource.resName,
    resType: resource.resType,
    resPath: resource.resPath,
    resCode: resource.resCode,
    resMethod: resource.resMethod,
    description: resource.description || '',
    sort: resource.sort,
    status: resource.status
  })
  showModal.value = true
}

const handleTypeChange = (type: string) => {
  if (type === 'BUTTON') {
    formData.resMethod = '*'
  }
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
      const data: ResourceUpdateDTO = {
        resName: formData.resName,
        resType: formData.resType as 'API' | 'BUTTON',
        resPath: formData.resPath,
        resMethod: formData.resMethod,
        description: formData.description || undefined,
        sort: formData.sort,
        status: formData.status
      }
      await updateResource(editingId.value, data)
      message.success('更新成功')
    } else {
      const data: ResourceCreateDTO = {
        groupId: props.group.id,
        resName: formData.resName,
        resType: formData.resType as 'API' | 'BUTTON',
        resPath: formData.resPath,
        resCode: formData.resCode,
        resMethod: formData.resMethod,
        description: formData.description || undefined,
        sort: formData.sort
      }
      await createResource(data)
      message.success('创建成功')
    }
    showModal.value = false
    loadResources()
  } catch (error) {
    message.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// Delete resource
const handleDelete = async (resource: ResourceVO) => {
  try {
    await deleteResource(resource.id)
    message.success('删除成功')
    loadResources()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  loadResources()
})
</script>
