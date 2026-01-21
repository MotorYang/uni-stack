<template>
  <div class="p-6 animate-enter">
    <!-- Resource Groups View -->
    <template v-if="!selectedGroup">
      <!-- Header -->
      <div class="glass-card !p-5 mb-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <n-input
              v-model:value="queryParams.resGroupName"
              placeholder="搜索资源组名称"
              clearable
              style="width: 200px"
              @keyup.enter="loadGroups"
            >
              <template #prefix>
                <n-icon><SearchOutline /></n-icon>
              </template>
            </n-input>
            <n-select
              v-model:value="queryParams.serviceName"
              :options="serviceOptions"
              placeholder="选择服务"
              clearable
              style="width: 160px"
            />
            <n-select
              v-model:value="queryParams.status"
              :options="statusOptions"
              placeholder="状态"
              clearable
              style="width: 100px"
            />
            <n-button type="primary" @click="loadGroups">
              <template #icon><n-icon><SearchOutline /></n-icon></template>
              搜索
            </n-button>
            <n-button @click="handleReset">
              <template #icon><n-icon><RefreshOutline /></n-icon></template>
              重置
            </n-button>
          </div>
          <n-button type="primary" @click="handleAddGroup">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增资源组
          </n-button>
        </div>
      </div>

      <!-- Cards Grid -->
      <n-spin :show="loading">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="group in groupList"
            :key="group.id"
            class="glass-card !p-5 cursor-pointer hover:shadow-lg transition-all group"
            @click="handleSelectGroup(group)"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500/20 to-purple-500/20 flex items-center justify-center">
                  <n-icon :size="20" class="text-blue-500"><FolderOutline /></n-icon>
                </div>
                <div>
                  <h3 class="font-semibold text-text-main">{{ group.resGroupName }}</h3>
                  <n-text code class="text-xs">{{ group.resGroupCode }}</n-text>
                </div>
              </div>
              <n-tag :type="group.status === 0 ? 'success' : 'error'" size="small">
                {{ group.status === 0 ? '正常' : '停用' }}
              </n-tag>
            </div>

            <p class="text-sm text-gray-500 mb-3 line-clamp-2 h-10">
              {{ group.description || '暂无描述' }}
            </p>

            <div class="flex items-center justify-between text-sm">
              <div class="flex items-center gap-2">
                <n-tag v-if="group.serviceName" size="small" type="info" round>
                  {{ group.serviceName }}
                </n-tag>
                <n-tag size="small" round :bordered="false">
                  {{ group.resourceCount }} 个资源
                </n-tag>
              </div>
              <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop>
                <n-button text size="small" @click="handleEditGroup(group)">
                  <template #icon><n-icon><CreateOutline /></n-icon></template>
                </n-button>
                <n-popconfirm @positive-click="handleDeleteGroup(group)">
                  <template #trigger>
                    <n-button text size="small" type="error">
                      <template #icon><n-icon><TrashOutline /></n-icon></template>
                    </n-button>
                  </template>
                  确定要删除资源组 "{{ group.resGroupName }}" 吗？
                </n-popconfirm>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="groupList.length === 0 && !loading" class="col-span-full">
            <div class="glass-card !p-12 text-center">
              <n-icon size="64" class="text-gray-400 mb-4"><FolderOutline /></n-icon>
              <p class="text-gray-400">暂无资源组数据</p>
              <n-button type="primary" class="mt-4" @click="handleAddGroup">
                <template #icon><n-icon><AddOutline /></n-icon></template>
                创建第一个资源组
              </n-button>
            </div>
          </div>
        </div>
      </n-spin>
    </template>

    <!-- Resources List View -->
    <ResourceList
      v-else
      :group="selectedGroup"
      @back="selectedGroup = null"
    />

    <!-- Add/Edit Group Modal -->
    <n-modal
      v-model:show="showGroupModal"
      :title="isEditGroup ? '编辑资源组' : '新增资源组'"
      preset="card"
      style="width: 550px"
      :mask-closable="false"
    >
      <n-form
        ref="groupFormRef"
        :model="groupFormData"
        :rules="groupFormRules"
        label-placement="left"
        label-width="80"
      >
        <n-form-item label="组名称" path="resGroupName">
          <n-input v-model:value="groupFormData.resGroupName" placeholder="请输入资源组名称" />
        </n-form-item>
        <n-form-item label="组编码" path="resGroupCode">
          <n-input
            v-model:value="groupFormData.resGroupCode"
            placeholder="请输入资源组编码"
            :disabled="isEditGroup"
          />
        </n-form-item>
        <n-form-item label="所属服务" path="serviceName">
          <n-select
            v-model:value="groupFormData.serviceName"
            :options="serviceOptions"
            placeholder="请选择关联的微服务"
            clearable
          />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input
            v-model:value="groupFormData.description"
            type="textarea"
            placeholder="请输入描述"
            :rows="2"
          />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="groupFormData.sort" :min="0" :max="9999" class="!w-full" />
        </n-form-item>
        <n-form-item v-if="isEditGroup" label="状态" path="status">
          <n-radio-group v-model:value="groupFormData.status">
            <n-space>
              <n-radio :value="0">正常</n-radio>
              <n-radio :value="1">停用</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showGroupModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmitGroup">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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
  NSpin,
  NPopconfirm,
  useMessage,
  type FormInst,
  type FormRules,
  type SelectOption
} from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  CreateOutline,
  TrashOutline,
  FolderOutline
} from '@vicons/ionicons5'
import ResourceList from './ResourceList.vue'
import {
  getResourceGroupList,
  createResourceGroup,
  updateResourceGroup,
  deleteResourceGroup,
  type ResourceGroupVO,
  type ResourceGroupCreateDTO,
  type ResourceGroupUpdateDTO
} from '@/api/resource-group'
import { getRegisteredServices } from '@/api/api-registry'

const message = useMessage()

const statusOptions: SelectOption[] = [
  { label: '正常', value: 0 },
  { label: '停用', value: 1 }
]

// Service options from Redis
const serviceOptions = ref<SelectOption[]>([])

// Query params
const queryParams = reactive({
  resGroupName: undefined as string | undefined,
  serviceName: undefined as string | undefined,
  status: undefined as number | undefined
})

// List state
const loading = ref(false)
const groupList = ref<ResourceGroupVO[]>([])
const selectedGroup = ref<ResourceGroupVO | null>(null)

// Modal state
const showGroupModal = ref(false)
const isEditGroup = ref(false)
const groupFormRef = ref<FormInst | null>(null)
const submitting = ref(false)
const editingGroupId = ref<string>('')

const groupFormData = reactive<ResourceGroupCreateDTO & ResourceGroupUpdateDTO>({
  resGroupName: '',
  resGroupCode: '',
  description: '',
  serviceName: undefined,
  sort: 0,
  status: 0
})

const groupFormRules: FormRules = {
  resGroupName: [
    { required: true, message: '请输入资源组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2-50 个字符', trigger: 'blur' }
  ],
  resGroupCode: [
    { required: true, message: '请输入资源组编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2-50 个字符', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_-]*$/, message: '以小写字母开头，只能包含小写字母、数字、下划线、连字符', trigger: 'blur' }
  ]
}

// Load services from Redis
const loadServices = async () => {
  try {
    const services = await getRegisteredServices()
    serviceOptions.value = services.map(s => ({ label: s, value: s }))
  } catch (error) {
    console.error('Failed to load services', error)
  }
}

// Load groups
const loadGroups = async () => {
  loading.value = true
  try {
    groupList.value = await getResourceGroupList({
      resGroupName: queryParams.resGroupName,
      serviceName: queryParams.serviceName,
      status: queryParams.status
    })
  } catch (error) {
    message.error('加载资源组列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.resGroupName = undefined
  queryParams.serviceName = undefined
  queryParams.status = undefined
  loadGroups()
}

const handleSelectGroup = (group: ResourceGroupVO) => {
  selectedGroup.value = group
}

// Add group
const handleAddGroup = () => {
  isEditGroup.value = false
  editingGroupId.value = ''
  Object.assign(groupFormData, {
    resGroupName: '',
    resGroupCode: '',
    description: '',
    serviceName: undefined,
    sort: 0,
    status: 0
  })
  showGroupModal.value = true
}

// Edit group
const handleEditGroup = (group: ResourceGroupVO) => {
  isEditGroup.value = true
  editingGroupId.value = group.id
  Object.assign(groupFormData, {
    resGroupName: group.resGroupName,
    resGroupCode: group.resGroupCode,
    description: group.description || '',
    serviceName: group.serviceName || undefined,
    sort: group.sort,
    status: group.status
  })
  showGroupModal.value = true
}

// Submit group form
const handleSubmitGroup = async () => {
  try {
    await groupFormRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEditGroup.value) {
      const data: ResourceGroupUpdateDTO = {
        resGroupName: groupFormData.resGroupName,
        description: groupFormData.description || undefined,
        serviceName: groupFormData.serviceName || undefined,
        sort: groupFormData.sort,
        status: groupFormData.status
      }
      await updateResourceGroup(editingGroupId.value, data)
      message.success('更新成功')
    } else {
      const data: ResourceGroupCreateDTO = {
        resGroupName: groupFormData.resGroupName,
        resGroupCode: groupFormData.resGroupCode,
        description: groupFormData.description || undefined,
        serviceName: groupFormData.serviceName || undefined,
        sort: groupFormData.sort
      }
      await createResourceGroup(data)
      message.success('创建成功')
    }
    showGroupModal.value = false
    loadGroups()
  } catch (error) {
    message.error(isEditGroup.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// Delete group
const handleDeleteGroup = async (group: ResourceGroupVO) => {
  try {
    await deleteResourceGroup(group.id)
    message.success('删除成功')
    loadGroups()
  } catch (error) {
    message.error('删除失败，请确保该资源组下没有资源')
  }
}

onMounted(() => {
  loadServices()
  loadGroups()
})
</script>
