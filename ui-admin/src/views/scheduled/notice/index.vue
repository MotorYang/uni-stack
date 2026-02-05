<template>
  <div class="p-6 space-y-6 animate-enter">
    <div class="glass-card !p-5 mb-4">
      <n-form inline :model="queryParams" label-placement="left" class="flex flex-wrap gap-4">
        <n-form-item label="标题" :show-feedback="false">
          <n-input v-model:value="queryParams.title" placeholder="请输入标题" clearable class="!w-44" />
        </n-form-item>
        <n-form-item label="类型" :show-feedback="false">
          <n-select
            v-model:value="queryParams.type"
            :options="typeOptions"
            placeholder="请选择"
            clearable
            class="!w-28"
          />
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
        <n-form-item label="优先级" :show-feedback="false">
          <n-select
            v-model:value="queryParams.priority"
            :options="priorityOptions"
            placeholder="请选择"
            clearable
            class="!w-28"
          />
        </n-form-item>
        <n-form-item :show-feedback="false">
          <n-space>
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon><SearchOutline /></n-icon></template>
              搜索
            </n-button>
            <n-button @click="handleReset">
              <template #icon><n-icon><RefreshOutline /></n-icon></template>
              重置
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>

    <div class="glass-card !p-5">
      <div class="flex justify-between items-center mb-4">
        <div class="flex items-center gap-2">
          <h3 class="text-lg font-semibold text-text-main">通知列表</h3>
          <n-tag size="small" round :bordered="false" type="info">{{ total }} 条</n-tag>
        </div>
        <n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增通知
          </n-button>
        </n-space>
      </div>

      <div class="glass-table">
        <n-data-table
          :columns="columns"
          :data="noticeList"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: NoticeVO) => row.id"
          :bordered="false"
          :single-line="false"
          :scroll-x="1100"
          flex-height
          style="min-height: 400px"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- 新增 / 编辑 Modal -->
    <n-modal
      v-model:show="showModal"
      :title="modalTitle"
      preset="card"
      style="width: 680px"
      :mask-closable="false"
      display-directive="show"
      :auto-focus="false"
      :trap-focus="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="90"
      >
        <n-form-item label="通知标题" path="title">
          <n-input v-model:value="formData.title" placeholder="请输入通知标题" maxlength="200" show-count />
        </n-form-item>
        <n-form-item label="通知类型" path="type">
          <n-select v-model:value="formData.type" :options="typeOptions" placeholder="请选择通知类型" />
        </n-form-item>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="优先级" path="priority">
              <n-select v-model:value="formData.priority" :options="priorityOptions" placeholder="请选择" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="通知目标" path="target">
              <n-select v-model:value="formData.target" :options="targetOptions" placeholder="请选择" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="发送时间" path="timeOption">
              <n-select v-model:value="formData.timeOption" :options="timeOptionOptions" placeholder="请选择" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item v-if="formData.timeOption === 'TIME'" label="定时时间" path="noticeTime">
              <n-date-picker
                v-model:formatted-value="formData.noticeTime"
                type="datetime"
                clearable
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
              />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item label="通知内容" path="content">
          <n-input
            v-model:value="formData.content"
            type="textarea"
            placeholder="请输入通知内容"
            :rows="6"
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

    <!-- 查看详情 Modal -->
    <n-modal
      v-model:show="showDetailModal"
      title="通知详情"
      preset="card"
      style="width: 680px"
      :mask-closable="true"
      :auto-focus="false"
      :trap-focus="false"
    >
      <div v-if="detailData" class="space-y-4">
        <div class="flex items-center gap-3 mb-2">
          <n-tag :type="typeTagType(detailData.type)" size="small">{{ typeLabel(detailData.type) }}</n-tag>
          <n-tag :type="priorityTagType(detailData.priority)" size="small">{{ priorityLabel(detailData.priority) }}</n-tag>
          <n-tag :type="statusTagType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</n-tag>
        </div>
        <h3 class="text-lg font-semibold">{{ detailData.title }}</h3>
        <n-divider style="margin: 8px 0" />
        <div class="text-sm text-gray-500 whitespace-pre-wrap leading-relaxed">{{ detailData.content }}</div>
        <n-divider style="margin: 8px 0" />
        <div class="flex justify-between text-xs text-gray-400">
          <span>目标: {{ targetLabel(detailData.target) }}</span>
          <span>{{ detailData.createTime?.replace('T', ' ').substring(0, 19) }}</span>
        </div>
      </div>
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
  NDataTable,
  NModal,
  NIcon,
  NTag,
  NPopconfirm,
  NTooltip,
  NGrid,
  NGi,
  NDatePicker,
  NDivider,
  useMessage,
  type FormInst,
  type FormRules,
  type DataTableColumns
} from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  CreateOutline,
  TrashOutline,
  EyeOutline,
  SendOutline,
  ArrowUndoOutline
} from '@vicons/ionicons5'
import {
  getNoticePage,
  getNoticeDetail,
  createNotice,
  updateNotice,
  deleteNotice,
  publishNotice,
  revokeNotice,
  type NoticeVO,
  type NoticeDetailVO,
  type NoticeQueryDTO,
  type NoticeCreateDTO,
  type NoticeUpdateDTO
} from '@/api/notice'

const message = useMessage()

// ============ 选项数据 ============

const typeOptions = [
  { label: '通知', value: 'NOTICE' },
  { label: '公告', value: 'ANNOUNCE' },
  { label: '待办', value: 'TASK' }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已撤销', value: 'REVOKED' }
]

const priorityOptions = [
  { label: '低', value: 'L' },
  { label: '中', value: 'M' },
  { label: '高', value: 'H' }
]

const targetOptions = [
  { label: '所有用户', value: 'ALL' },
  { label: '指定角色', value: 'ROLE' },
  { label: '指定用户', value: 'USER' }
]

const timeOptionOptions = [
  { label: '即时发送', value: 'NOW' },
  { label: '定时发送', value: 'TIME' }
]

// ============ 标签辅助函数 ============

const typeLabel = (v: string) => typeOptions.find(o => o.value === v)?.label ?? v
const statusLabel = (v: string) => statusOptions.find(o => o.value === v)?.label ?? v
const priorityLabel = (v: string) => priorityOptions.find(o => o.value === v)?.label ?? v
const targetLabel = (v: string) => targetOptions.find(o => o.value === v)?.label ?? v

const typeTagType = (v: string) => ({ NOTICE: 'info', ANNOUNCE: 'warning', TASK: 'success' }[v] ?? 'default') as any
const statusTagType = (v: string) => ({ DRAFT: 'default', PUBLISHED: 'success', REVOKED: 'error' }[v] ?? 'default') as any
const priorityTagType = (v: string) => ({ L: 'default', M: 'warning', H: 'error' }[v] ?? 'default') as any

// ============ 查询 ============

const queryParams = reactive<NoticeQueryDTO>({
  title: undefined,
  type: undefined,
  status: undefined,
  priority: undefined,
  current: 1,
  size: 10
})

const noticeList = ref<NoticeVO[]>([])
const loading = ref(false)
const total = ref(0)

const pagination = computed(() => ({
  page: queryParams.current,
  pageSize: queryParams.size,
  pageCount: Math.ceil(total.value / (queryParams.size || 10)),
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
}))

// ============ 表格列 ============

const columns: DataTableColumns<NoticeVO> = [
  {
    title: '标题',
    key: 'title',
    minWidth: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '类型',
    key: 'type',
    width: 90,
    render: row => h(NTag, { type: typeTagType(row.type), size: 'small' }, { default: () => typeLabel(row.type) })
  },
  {
    title: '优先级',
    key: 'priority',
    width: 80,
    render: row => h(NTag, { type: priorityTagType(row.priority), size: 'small' }, { default: () => priorityLabel(row.priority) })
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: row => h(NTag, { type: statusTagType(row.status), size: 'small' }, { default: () => statusLabel(row.status) })
  },
  {
    title: '通知目标',
    key: 'target',
    width: 100,
    render: row => targetLabel(row.target)
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 170,
    render: row => row.createTime?.replace('T', ' ').substring(0, 19) || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    fixed: 'right',
    render: row => {
      const actions = []

      // 查看详情
      actions.push(
        h(NTooltip, null, {
          trigger: () =>
            h(NButton, { size: 'small', quaternary: true, onClick: () => handleView(row) },
              { icon: () => h(NIcon, null, { default: () => h(EyeOutline) }) }),
          default: () => '查看'
        })
      )

      // 编辑（仅草稿可编辑）
      if (row.status === 'DRAFT') {
        actions.push(
          h(NTooltip, null, {
            trigger: () =>
              h(NButton, { size: 'small', quaternary: true, onClick: () => handleEdit(row) },
                { icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) }),
            default: () => '编辑'
          })
        )
      }

      // 发布（仅草稿可发布）
      if (row.status === 'DRAFT') {
        actions.push(
          h(NPopconfirm, { onPositiveClick: () => handlePublish(row) }, {
            trigger: () =>
              h(NTooltip, null, {
                trigger: () =>
                  h(NButton, { size: 'small', quaternary: true, type: 'success' },
                    { icon: () => h(NIcon, null, { default: () => h(SendOutline) }) }),
                default: () => '发布'
              }),
            default: () => '确认发布该通知吗？'
          })
        )
      }

      // 撤销（仅已发布可撤销）
      if (row.status === 'PUBLISHED') {
        actions.push(
          h(NPopconfirm, { onPositiveClick: () => handleRevoke(row) }, {
            trigger: () =>
              h(NTooltip, null, {
                trigger: () =>
                  h(NButton, { size: 'small', quaternary: true, type: 'warning' },
                    { icon: () => h(NIcon, null, { default: () => h(ArrowUndoOutline) }) }),
                default: () => '撤销'
              }),
            default: () => '确认撤销该通知吗？'
          })
        )
      }

      // 删除
      actions.push(
        h(NPopconfirm, { onPositiveClick: () => handleDelete(row) }, {
          trigger: () =>
            h(NTooltip, null, {
              trigger: () =>
                h(NButton, { size: 'small', quaternary: true, type: 'error' },
                  { icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) }),
              default: () => '删除'
            }),
          default: () => '确认删除该通知吗？'
        })
      )

      return h(NSpace, { size: 'small', wrap: false }, { default: () => actions })
    }
  }
]

// ============ 表单 ============

const showModal = ref(false)
const modalTitle = ref('新增通知')
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const editingId = ref<string | null>(null)

const formData = reactive<NoticeCreateDTO & NoticeUpdateDTO>({
  title: '',
  content: '',
  type: 'NOTICE',
  priority: 'M',
  target: 'ALL',
  timeOption: 'NOW',
  noticeTime: null,
  targetIds: []
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { max: 200, message: '标题不能超过200个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  target: [{ required: true, message: '请选择通知目标', trigger: 'change' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
  noticeTime: [{
    validator: (_rule, value) => {
      if (formData.timeOption === 'TIME' && !value) {
        return new Error('定时发送需要选择时间')
      }
      return true
    },
    trigger: 'change'
  }]
}

// ============ 详情 ============

const showDetailModal = ref(false)
const detailData = ref<NoticeDetailVO | null>(null)

// ============ 方法 ============

const resetForm = () => {
  Object.assign(formData, {
    title: '',
    content: '',
    type: 'NOTICE',
    priority: 'M',
    target: 'ALL',
    timeOption: 'NOW',
    noticeTime: null,
    targetIds: []
  })
}

const handleSearch = () => {
  queryParams.current = 1
  loadNotices()
}

const handleReset = () => {
  queryParams.title = undefined
  queryParams.type = undefined
  queryParams.status = undefined
  queryParams.priority = undefined
  queryParams.current = 1
  loadNotices()
}

const handlePageChange = (page: number) => {
  queryParams.current = page
  loadNotices()
}

const handlePageSizeChange = (pageSize: number) => {
  queryParams.size = pageSize
  queryParams.current = 1
  loadNotices()
}

const loadNotices = async () => {
  loading.value = true
  try {
    const res = await getNoticePage(queryParams)
    noticeList.value = res.records
    total.value = res.total
  } catch {
    message.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增通知'
  editingId.value = null
  resetForm()
  showModal.value = true
}

const handleEdit = async (row: NoticeVO) => {
  isEdit.value = true
  modalTitle.value = '编辑通知'
  editingId.value = row.id
  try {
    const detail = await getNoticeDetail(row.id)
    Object.assign(formData, {
      title: detail.title,
      content: detail.content,
      type: detail.type,
      priority: detail.priority,
      target: detail.target,
      timeOption: detail.timeOption,
      noticeTime: detail.noticeTime?.replace('T', ' ').substring(0, 19) || null,
      targetIds: detail.targets?.map(t => t.targetId) || []
    })
    showModal.value = true
  } catch {
    message.error('获取通知详情失败')
  }
}

const handleView = async (row: NoticeVO) => {
  try {
    detailData.value = await getNoticeDetail(row.id)
    showDetailModal.value = true
  } catch {
    message.error('获取通知详情失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      const payload: NoticeUpdateDTO = {
        title: formData.title,
        content: formData.content,
        type: formData.type,
        priority: formData.priority,
        target: formData.target,
        timeOption: formData.timeOption,
        noticeTime: formData.timeOption === 'TIME' ? formData.noticeTime : null,
        targetIds: formData.target !== 'ALL' ? formData.targetIds : []
      }
      await updateNotice(editingId.value, payload)
      message.success('更新成功')
    } else {
      const payload: NoticeCreateDTO = {
        title: formData.title,
        content: formData.content,
        type: formData.type,
        priority: formData.priority,
        target: formData.target,
        timeOption: formData.timeOption,
        noticeTime: formData.timeOption === 'TIME' ? formData.noticeTime : null,
        targetIds: formData.target !== 'ALL' ? formData.targetIds : []
      }
      await createNotice(payload)
      message.success('创建成功')
    }
    showModal.value = false
    loadNotices()
  } catch {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: NoticeVO) => {
  try {
    await deleteNotice(row.id)
    message.success('删除成功')
    loadNotices()
  } catch {
    message.error('删除失败')
  }
}

const handlePublish = async (row: NoticeVO) => {
  try {
    await publishNotice(row.id)
    message.success('发布成功')
    loadNotices()
  } catch {
    message.error('发布失败')
  }
}

const handleRevoke = async (row: NoticeVO) => {
  try {
    await revokeNotice(row.id)
    message.success('撤销成功')
    loadNotices()
  } catch {
    message.error('撤销失败')
  }
}

onMounted(() => {
  loadNotices()
})
</script>
