<template>
  <div class="flex flex-col">
    <div class="flex justify-between items-center mb-4">
      <n-form inline :model="queryParams" label-placement="left" class="flex flex-wrap gap-2">
        <n-form-item :show-feedback="false">
          <n-input
            v-model:value="queryParams.username"
            placeholder="用户名"
            clearable
            class="!w-32"
          />
        </n-form-item>
        <n-form-item :show-feedback="false">
          <n-input
            v-model:value="queryParams.nickname"
            placeholder="昵称"
            clearable
            class="!w-32"
          />
        </n-form-item>
        <n-form-item :show-feedback="false">
          <n-button type="primary" size="small" @click="handleSearch">
            <template #icon><n-icon><SearchOutline /></n-icon></template>
            搜索
          </n-button>
        </n-form-item>
      </n-form>
      <n-tag v-if="internalSelectedIds.length > 0" type="info">
        已选择 {{ internalSelectedIds.length }} 个用户
      </n-tag>
    </div>

    <n-data-table
      :columns="columns"
      :data="userList"
      :loading="loading"
      :pagination="pagination"
      :row-key="(row: UserVO) => row.id"
      :checked-row-keys="allSelectedIds"
      :bordered="false"
      :single-line="false"
      size="small"
      style="min-height: 300px"
      @update:page="handlePageChange"
      @update:page-size="handlePageSizeChange"
      @update:checked-row-keys="handleSelectionChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch, h } from 'vue'
import {
  NForm,
  NFormItem,
  NInput,
  NButton,
  NTag,
  NDataTable,
  NIcon,
  NAvatar,
  type DataTableColumns,
  type DataTableRowKey
} from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { getUserPage, type UserVO, type UserQueryDTO } from '@/api/user'

const props = defineProps<{
  value: string[]
  roleId?: string
  existingUserIds?: string[]
}>()

const emit = defineEmits<{
  'update:value': [string[]]
}>()

const userList = ref<UserVO[]>([])
const loading = ref(false)
const total = ref(0)
const internalSelectedIds = ref<DataTableRowKey[]>([])

const allSelectedIds = computed<DataTableRowKey[]>(() => {
  const fixed = props.existingUserIds ?? []
  const dynamic = internalSelectedIds.value as string[]
  const set = new Set<string>()
  fixed.forEach((id) => set.add(id))
  dynamic.forEach((id) => {
    if (!fixed.includes(id)) {
      set.add(id)
    }
  })
  return Array.from(set)
})

const queryParams = reactive<UserQueryDTO>({
  username: undefined,
  nickname: undefined,
  phone: undefined,
  status: undefined,
  current: 1,
  size: 10
})

const pagination = computed(() => ({
  page: queryParams.current,
  pageSize: queryParams.size,
  pageCount: Math.ceil(total.value / (queryParams.size || 10)),
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
}))

const columns: DataTableColumns<UserVO> = [
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
  }
]

const loadUserList = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadUserList()
}

const handlePageChange = (page: number) => {
  queryParams.current = page
  loadUserList()
}

const handlePageSizeChange = (pageSize: number) => {
  queryParams.size = pageSize
  queryParams.current = 1
  loadUserList()
}

const handleSelectionChange = (keys: DataTableRowKey[]) => {
  const fixed = props.existingUserIds ?? []
  const selected = (keys as string[]).filter((id) => !fixed.includes(id))
  internalSelectedIds.value = selected
  emit('update:value', selected)
}

watch(
  () => props.value,
  (val) => {
    internalSelectedIds.value = val as DataTableRowKey[]
  },
  { immediate: true }
)

watch(
  () => props.roleId,
  () => {
    queryParams.current = 1
    loadUserList()
  },
  { immediate: true }
)
</script>
