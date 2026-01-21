<template>
  <div class="p-8 space-y-8">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <n-card class="glass-card" size="small" :bordered="false">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">服务总数</p>
            <h3 class="text-2xl font-bold">{{ stats.total }}</h3>
            <n-tag size="small" round type="info" class="mt-2">
              {{ uniqueServiceNames.length }} 个服务
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24">
              <ServerOutline />
            </n-icon>
          </div>
        </div>
      </n-card>

      <n-card class="glass-card" size="small" :bordered="false">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">在线实例</p>
            <h3 class="text-2xl font-bold text-green-500">{{ stats.up }}</h3>
            <n-tag size="small" round type="success" class="mt-2">
              运行正常
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24" color="#22c55e">
              <CheckmarkCircleOutline />
            </n-icon>
          </div>
        </div>
      </n-card>

      <n-card class="glass-card" size="small" :bordered="false">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">离线实例</p>
            <h3 class="text-2xl font-bold" :class="stats.down > 0 ? 'text-red-500' : ''">{{ stats.down }}</h3>
            <n-tag size="small" round :type="stats.down > 0 ? 'error' : 'success'" class="mt-2">
              {{ stats.down > 0 ? '需要关注' : '全部在线' }}
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24" :color="stats.down > 0 ? '#ef4444' : '#9ca3af'">
              <CloseCircleOutline />
            </n-icon>
          </div>
        </div>
      </n-card>

      <n-card class="glass-card" size="small" :bordered="false">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">注册中心</p>
            <h3 class="text-2xl font-bold">Nacos</h3>
            <n-tag size="small" round type="success" class="mt-2">
              已连接
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24">
              <CloudOutline />
            </n-icon>
          </div>
        </div>
      </n-card>
    </div>

    <div class="glass-card !p-0 overflow-hidden animate-enter delay-200">
      <div class="p-6 border-b border-white/40 flex justify-between items-center">
        <h2 class="text-xl font-bold text-text-main">服务实例列表</h2>
        <n-button size="small" quaternary @click="loadInstances" :loading="loading">
          <template #icon>
            <n-icon><RefreshOutline /></n-icon>
          </template>
          刷新
        </n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="instances"
        :bordered="false"
        :loading="loading"
        :row-key="(row: ServiceInstance) => row.id"
        class="glass-table"
      />
    </div>

    <n-modal v-model:show="showDetail" preset="card" style="width: 700px;" :title="detailInstance?.registration.name">
      <template v-if="detailInstance">
        <n-descriptions :column="2" label-placement="left" bordered>
          <n-descriptions-item label="实例ID">{{ detailInstance.id }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="getStatusType(detailInstance.statusInfo.status)" size="small">
              {{ detailInstance.statusInfo.status }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="服务地址">{{ detailInstance.registration.serviceUrl }}</n-descriptions-item>
          <n-descriptions-item label="管理地址">{{ detailInstance.registration.managementUrl }}</n-descriptions-item>
          <n-descriptions-item label="注册来源">{{ detailInstance.registration.source }}</n-descriptions-item>
          <n-descriptions-item label="Nacos集群">{{ detailInstance.registration.metadata['nacos.cluster'] }}</n-descriptions-item>
        </n-descriptions>

        <n-divider>健康检查组件</n-divider>
        <div class="grid grid-cols-2 gap-4">
          <n-card
            v-for="(component, name) in detailInstance.statusInfo.details"
            :key="name"
            size="small"
            :bordered="true"
          >
            <div class="flex items-center justify-between">
              <span class="font-medium">{{ name }}</span>
              <n-tag :type="getStatusType(component.status)" size="small">{{ component.status }}</n-tag>
            </div>
            <div v-if="component.details" class="mt-2 text-xs text-gray-500 space-y-1">
              <div v-for="(value, key) in getDisplayDetails(component.details)" :key="key">
                <span class="font-medium">{{ key }}:</span> {{ value }}
              </div>
            </div>
          </n-card>
        </div>

        <n-divider>Actuator 端点</n-divider>
        <n-space wrap>
          <n-tag
            v-for="endpoint in detailInstance.endpoints"
            :key="endpoint.id"
            type="info"
            size="small"
            round
          >
            {{ endpoint.id }}
          </n-tag>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import {
  NCard,
  NTag,
  NIcon,
  NButton,
  NDataTable,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NDivider,
  NSpace,
  type DataTableColumns
} from 'naive-ui'
import {
  ServerOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline,
  CloudOutline,
  RefreshOutline,
  EyeOutline
} from '@vicons/ionicons5'
import { getInstances, type ServiceInstance, type StatusInfo } from '@/api/monitor'

const loading = ref(false)
const instances = ref<ServiceInstance[]>([])
const showDetail = ref(false)
const detailInstance = ref<ServiceInstance | null>(null)

const stats = computed(() => {
  const total = instances.value.length
  const up = instances.value.filter(i => i.statusInfo.status === 'UP').length
  const down = total - up
  return { total, up, down }
})

const uniqueServiceNames = computed(() => {
  const names = new Set(instances.value.map(i => i.registration.name))
  return Array.from(names)
})

const getStatusType = (status: StatusInfo['status']): 'success' | 'error' | 'warning' | 'default' => {
  switch (status) {
    case 'UP':
      return 'success'
    case 'DOWN':
    case 'OFFLINE':
      return 'error'
    case 'OUT_OF_SERVICE':
      return 'warning'
    default:
      return 'default'
  }
}

const getDisplayDetails = (details: Record<string, any>): Record<string, string> => {
  const result: Record<string, string> = {}
  for (const [key, value] of Object.entries(details)) {
    if (key === 'total' || key === 'free') {
      result[key] = formatBytes(value as number)
    } else if (typeof value === 'object' && value !== null) {
      continue
    } else {
      result[key] = String(value)
    }
  }
  return result
}

const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (timestamp: string): string => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

const openDetail = (instance: ServiceInstance) => {
  detailInstance.value = instance
  showDetail.value = true
}

const columns: DataTableColumns<ServiceInstance> = [
  {
    title: '服务名称',
    key: 'name',
    render: row =>
      h('div', { class: 'flex flex-col' }, [
        h('span', { class: 'font-medium text-text-main' }, row.registration.name),
        h('span', { class: 'text-xs text-text-sec' }, row.id.substring(0, 12))
      ])
  },
  {
    title: '服务地址',
    key: 'serviceUrl',
    render: row => h('span', { class: 'text-sm' }, row.registration.serviceUrl)
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row => {
      const status = row.statusInfo.status
      return h(
        NTag,
        {
          type: getStatusType(status),
          size: 'small',
          round: true,
          bordered: false
        },
        { default: () => status }
      )
    }
  },
  {
    title: '健康组件',
    key: 'components',
    render: row => {
      const details = row.statusInfo.details || {}
      const componentNames = Object.keys(details).slice(0, 3)
      const more = Object.keys(details).length - 3
      return h('div', { class: 'flex flex-wrap gap-1' }, [
        ...componentNames.map(name =>
          h(
            NTag,
            {
              type: getStatusType(details[name].status),
              size: 'tiny',
              bordered: false
            },
            { default: () => name }
          )
        ),
        more > 0 ? h('span', { class: 'text-xs text-gray-400' }, `+${more}`) : null
      ])
    }
  },
  {
    title: '更新时间',
    key: 'statusTimestamp',
    width: 180,
    render: row => h('span', { class: 'text-sm text-text-sec' }, formatTime(row.statusTimestamp))
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: row =>
      h(
        NButton,
        {
          size: 'small',
          quaternary: true,
          onClick: () => openDetail(row)
        },
        {
          icon: () => h(NIcon, null, { default: () => h(EyeOutline) }),
          default: () => '详情'
        }
      )
  }
]

const loadInstances = async () => {
  loading.value = true
  try {
    instances.value = await getInstances()
  } catch (error) {
    console.error('Failed to load instances', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadInstances()
})
</script>
