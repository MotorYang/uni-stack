<template>
  <div class="p-8 space-y-6">
    <div class="glass-card !p-6">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-text-main">日志配置</h2>
        <div class="flex items-center gap-4">
          <n-select
            v-model:value="selectedInstance"
            :options="instanceOptions"
            placeholder="选择服务实例"
            style="width: 280px"
            :loading="loadingInstances"
            @update:value="handleInstanceChange"
          />
          <n-button
            size="small"
            quaternary
            :loading="loadingLoggers"
            :disabled="!selectedInstance"
            @click="loadLoggers"
          >
            <template #icon>
              <n-icon><RefreshOutline /></n-icon>
            </template>
            刷新
          </n-button>
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-4 mb-4">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索 Logger 名称..."
          clearable
          style="width: 280px"
        >
          <template #prefix>
            <n-icon><SearchOutline /></n-icon>
          </template>
        </n-input>
        <n-select
          v-model:value="filterConfiguredLevel"
          :options="levelFilterOptions"
          placeholder="配置级别"
          clearable
          style="width: 140px"
        />
        <n-select
          v-model:value="filterEffectiveLevel"
          :options="levelFilterOptions"
          placeholder="生效级别"
          clearable
          style="width: 140px"
        />
        <n-checkbox v-model:checked="showConfiguredOnly">
          仅显示已配置
        </n-checkbox>
        <n-checkbox v-model:checked="showPackageOnly">
          仅显示包级别
        </n-checkbox>
      </div>

      <n-alert v-if="!selectedInstance" type="info" class="mb-4">
        请先选择一个服务实例以查看和配置日志级别
      </n-alert>

      <n-data-table
        v-if="selectedInstance"
        :columns="columns"
        :data="filteredLoggers"
        :bordered="false"
        :loading="loadingLoggers"
        :pagination="pagination"
        :row-key="(row: LoggerItem) => row.name"
        max-height="calc(100vh - 380px)"
        virtual-scroll
        class="glass-table"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import {
  NSelect,
  NInput,
  NButton,
  NIcon,
  NDataTable,
  NTag,
  NAlert,
  NCheckbox,
  NPopselect,
  useMessage,
  type DataTableColumns,
  type SelectOption
} from 'naive-ui'
import { RefreshOutline, SearchOutline } from '@vicons/ionicons5'
import {
  getInstances,
  getInstanceLoggers,
  setInstanceLoggerLevel,
  type ServiceInstance,
  type LogLevel,
  type LoggerConfig
} from '@/api/monitor'

interface LoggerItem {
  name: string
  configuredLevel: LogLevel
  effectiveLevel: LogLevel
}

const message = useMessage()

const instances = ref<ServiceInstance[]>([])
const selectedInstance = ref<string | null>(null)
const loadingInstances = ref(false)
const loadingLoggers = ref(false)

const loggers = ref<LoggerItem[]>([])
const availableLevels = ref<string[]>([])
const searchKeyword = ref('')
const showConfiguredOnly = ref(false)
const showPackageOnly = ref(false)
const filterConfiguredLevel = ref<string | null>(null)
const filterEffectiveLevel = ref<string | null>(null)

const pagination = {
  pageSize: 50
}

const instanceOptions = computed<SelectOption[]>(() => {
  return instances.value.map(inst => ({
    label: `${inst.registration.name} (${inst.registration.serviceUrl})`,
    value: inst.id,
    disabled: inst.statusInfo.status !== 'UP'
  }))
})

const levelFilterOptions = computed<SelectOption[]>(() => {
  return availableLevels.value.map(level => ({
    label: level,
    value: level
  }))
})

const filteredLoggers = computed(() => {
  let result = loggers.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(l => l.name.toLowerCase().includes(keyword))
  }

  if (showConfiguredOnly.value) {
    result = result.filter(l => l.configuredLevel !== null)
  }

  if (showPackageOnly.value) {
    result = result.filter(l => !l.name.includes('$') && l.name.includes('.'))
  }

  if (filterConfiguredLevel.value) {
    result = result.filter(l => l.configuredLevel === filterConfiguredLevel.value)
  }

  if (filterEffectiveLevel.value) {
    result = result.filter(l => l.effectiveLevel === filterEffectiveLevel.value)
  }

  return result
})

const levelOptions = computed(() => {
  return availableLevels.value.map(level => ({
    label: level,
    value: level
  }))
})

const getLevelType = (level: LogLevel): 'default' | 'info' | 'success' | 'warning' | 'error' => {
  switch (level) {
    case 'TRACE':
    case 'DEBUG':
      return 'default'
    case 'INFO':
      return 'info'
    case 'WARN':
      return 'warning'
    case 'ERROR':
      return 'error'
    case 'OFF':
      return 'default'
    default:
      return 'default'
  }
}

const handleLevelChange = async (loggerName: string, newLevel: string) => {
  if (!selectedInstance.value) return

  try {
    await setInstanceLoggerLevel(selectedInstance.value, loggerName, newLevel as LogLevel)
    message.success(`已将 ${loggerName} 的日志级别设置为 ${newLevel}`)

    const logger = loggers.value.find(l => l.name === loggerName)
    if (logger) {
      logger.configuredLevel = newLevel as LogLevel
      logger.effectiveLevel = newLevel as LogLevel
    }
  } catch (error) {
    message.error('设置日志级别失败')
    console.error('Failed to set logger level', error)
  }
}

const handleResetLevel = async (loggerName: string) => {
  if (!selectedInstance.value) return

  try {
    await setInstanceLoggerLevel(selectedInstance.value, loggerName, null)
    message.success(`已重置 ${loggerName} 的日志级别`)
    await loadLoggers()
  } catch (error) {
    message.error('重置日志级别失败')
    console.error('Failed to reset logger level', error)
  }
}

const columns: DataTableColumns<LoggerItem> = [
  {
    title: 'Logger 名称',
    key: 'name',
    ellipsis: {
      tooltip: true
    },
    render: row => h('code', { class: 'text-sm' }, row.name)
  },
  {
    title: '配置级别',
    key: 'configuredLevel',
    width: 140,
    render: row => {
      if (row.configuredLevel === null) {
        return h('span', { class: 'text-gray-400 text-sm' }, '未配置')
      }
      return h(
        NTag,
        {
          type: getLevelType(row.configuredLevel),
          size: 'small',
          bordered: false
        },
        { default: () => row.configuredLevel }
      )
    }
  },
  {
    title: '生效级别',
    key: 'effectiveLevel',
    width: 140,
    render: row =>
      h(
        NTag,
        {
          type: getLevelType(row.effectiveLevel),
          size: 'small'
        },
        { default: () => row.effectiveLevel }
      )
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: row =>
      h('div', { class: 'flex items-center gap-2' }, [
        h(
          NPopselect,
          {
            options: levelOptions.value,
            onUpdateValue: (value: string) => handleLevelChange(row.name, value),
            trigger: 'click'
          },
          {
            default: () =>
              h(
                NButton,
                { size: 'small', quaternary: true },
                { default: () => '修改级别' }
              )
          }
        ),
        row.configuredLevel !== null
          ? h(
              NButton,
              {
                size: 'small',
                quaternary: true,
                type: 'warning',
                onClick: () => handleResetLevel(row.name)
              },
              { default: () => '重置' }
            )
          : null
      ])
  }
]

const loadInstances = async () => {
  loadingInstances.value = true
  try {
    instances.value = await getInstances()
  } catch (error) {
    message.error('加载服务实例失败')
    console.error('Failed to load instances', error)
  } finally {
    loadingInstances.value = false
  }
}

const loadLoggers = async () => {
  if (!selectedInstance.value) return

  loadingLoggers.value = true
  try {
    const response = await getInstanceLoggers(selectedInstance.value)
    availableLevels.value = response.levels
    loggers.value = Object.entries(response.loggers).map(([name, config]: [string, LoggerConfig]) => ({
      name,
      configuredLevel: config.configuredLevel,
      effectiveLevel: config.effectiveLevel
    }))
  } catch (error) {
    message.error('加载日志配置失败')
    console.error('Failed to load loggers', error)
  } finally {
    loadingLoggers.value = false
  }
}

const handleInstanceChange = (value: string) => {
  selectedInstance.value = value
  if (value) {
    loadLoggers()
  } else {
    loggers.value = []
  }
}

onMounted(() => {
  loadInstances()
})
</script>
