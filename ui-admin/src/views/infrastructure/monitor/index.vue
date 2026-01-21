<template>
  <div class="p-8 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-text-main">服务实例监控
        <n-tag :type="getStatusType(healthData?.status)" size="large">
          {{ healthData?.status || 'UNKNOWN' }}
        </n-tag>
      </h1>
      <div class="flex items-center gap-4">
        <n-select
          v-model:value="selectedInstance"
          :options="instanceOptions"
          placeholder="选择服务实例"
          style="width: 300px"
          :loading="loadingInstances"
          @update:value="handleInstanceChange"
        />
        <n-input-group style="width: 200px">
          <n-input-group-label>刷新间隔</n-input-group-label>
          <n-select
            v-model:value="refreshInterval"
            :options="intervalOptions"
            style="width: 100px"
            @update:value="handleIntervalChange"
          />
        </n-input-group>
        <n-button quaternary :loading="loading" :disabled="!selectedInstance" @click="refreshData">
          <template #icon><n-icon><RefreshOutline /></n-icon></template>
          刷新
        </n-button>
      </div>
    </div>

    <n-alert v-if="!selectedInstance" type="info">
      请选择一个服务实例以查看详细监控信息
    </n-alert>

    <template v-if="selectedInstance && currentInstance">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 元数据 -->
        <div class="glass-card !p-6">
          <h2 class="text-lg font-bold text-text-main mb-4 flex items-center gap-2">
            <n-icon><InformationCircleOutline /></n-icon>
            元数据
          </h2>
          <n-descriptions :column="1" label-placement="left" bordered size="small">
            <n-descriptions-item label="服务名称">{{ currentInstance.registration.name }}</n-descriptions-item>
            <n-descriptions-item label="实例ID">{{ currentInstance.id }}</n-descriptions-item>
            <n-descriptions-item label="服务地址">{{ currentInstance.registration.serviceUrl }}</n-descriptions-item>
            <n-descriptions-item label="管理地址">{{ currentInstance.registration.managementUrl }}</n-descriptions-item>
            <n-descriptions-item label="注册来源">{{ currentInstance.registration.source }}</n-descriptions-item>
            <n-descriptions-item label="Nacos集群">{{ currentInstance.registration.metadata['nacos.cluster'] || '-' }}</n-descriptions-item>
            <n-descriptions-item label="状态时间">{{ formatTime(currentInstance.statusTimestamp) }}</n-descriptions-item>
          </n-descriptions>
        </div>

        <!-- 进程信息 & GC -->
        <div class="glass-card !p-6">
          <h2 class="text-lg font-bold text-text-main mb-4 flex items-center gap-2">
            <n-icon><TerminalOutline /></n-icon>
            进程信息
          </h2>
          <n-descriptions :column="1" label-placement="left" bordered size="small">
            <n-descriptions-item label="进程 CPU 使用率">
              <n-progress
                type="line"
                :percentage="processMetrics.cpuUsage"
                :indicator-placement="'inside'"
                :color="processMetrics.cpuUsage > 80 ? '#ef4444' : processMetrics.cpuUsage > 50 ? '#f97316' : '#22c55e'"
              />
            </n-descriptions-item>
            <n-descriptions-item label="系统 CPU 使用率">
              <n-progress
                type="line"
                :percentage="processMetrics.systemCpuUsage"
                :indicator-placement="'inside'"
                :color="processMetrics.systemCpuUsage > 80 ? '#ef4444' : processMetrics.systemCpuUsage > 50 ? '#f97316' : '#22c55e'"
              />
            </n-descriptions-item>
            <n-descriptions-item label="运行时间">{{ processMetrics.uptime }}</n-descriptions-item>
            <n-descriptions-item label="启动时间">{{ processMetrics.startTime }}</n-descriptions-item>
          </n-descriptions>

          <h3 class="text-base font-bold text-text-main mt-6 mb-3 flex items-center gap-2">
            <n-icon><TrashOutline /></n-icon>
            垃圾回收
          </h3>
          <div class="space-y-3">
            <div v-for="gc in gcMetrics" :key="gc.name" class="p-3 rounded-lg bg-gray-50 dark:bg-gray-800/50">
              <div class="font-medium text-sm mb-2 text-text-main">{{ gc.name }}</div>
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-gray-500">回收次数:</span>
                  <span class="ml-2 font-mono text-text-main">{{ gc.count }}</span>
                </div>
                <div>
                  <span class="text-gray-500">总耗时:</span>
                  <span class="ml-2 font-mono text-text-main">{{ gc.time }}</span>
                </div>
              </div>
            </div>
            <div v-if="gcMetrics.length === 0" class="text-gray-400 text-sm">暂无 GC 数据</div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 线程统计 -->
        <div class="glass-card !p-6">
          <h2 class="text-lg font-bold text-text-main mb-4 flex items-center gap-2">
            <n-icon><GitNetworkOutline /></n-icon>
            线程
          </h2>
          <div class="grid grid-cols-3 gap-4 mb-4 text-center">
            <div>
              <div class="text-gray-500 text-sm">活动线程</div>
              <div class="text-2xl font-bold font-mono text-text-main">{{ threadMetrics.live }}</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">守护线程</div>
              <div class="text-2xl font-bold font-mono text-text-main">{{ threadMetrics.daemon }}</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">线程峰值</div>
              <div class="text-2xl font-bold font-mono text-text-main">{{ threadMetrics.peak }}</div>
            </div>
          </div>
          <div ref="threadChartRef" class="w-full h-[260px]"></div>
        </div>

        <!-- Heap 内存图表 -->
        <div class="glass-card !p-6">
          <h2 class="text-lg font-bold text-text-main mb-4 flex items-center gap-2">
            <n-icon><ServerOutline /></n-icon>
            内存: Heap
          </h2>
          <div class="grid grid-cols-3 gap-4 mb-4 text-center">
            <div>
              <div class="text-gray-500 text-sm">已用</div>
              <div class="text-xl font-bold font-mono text-text-main">{{ formatBytes(memoryMetrics.heapUsed) }}</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">已提交</div>
              <div class="text-xl font-bold font-mono text-text-main">{{ formatBytes(memoryMetrics.heapCommitted) }}</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">最大</div>
              <div class="text-xl font-bold font-mono text-text-main">{{ formatBytes(memoryMetrics.heapMax) }}</div>
            </div>
          </div>
          <div ref="heapChartRef" class="w-full h-[260px]"></div>
        </div>

        <!-- Non-Heap 内存图表 -->
        <div class="glass-card !p-6">
          <h2 class="text-lg font-bold text-text-main mb-4 flex items-center gap-2">
            <n-icon><LayersOutline /></n-icon>
            内存: Non-Heap
          </h2>
          <div class="grid grid-cols-2 gap-4 mb-4 text-center">
            <div>
              <div class="text-gray-500 text-sm">已用</div>
              <div class="text-xl font-bold font-mono text-text-main">{{ formatBytes(memoryMetrics.nonHeapUsed) }}</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">已提交</div>
              <div class="text-xl font-bold font-mono text-text-main">{{ formatBytes(memoryMetrics.nonHeapCommitted) }}</div>
            </div>
          </div>
          <div ref="nonHeapChartRef" class="w-full h-[260px]"></div>
        </div>

        <!-- 线程状态分布 -->
        <div class="glass-card !p-6">
          <h2 class="text-lg font-bold text-text-main mb-4 flex items-center gap-2">
            <n-icon><StatsChartOutline /></n-icon>
            线程状态分布
          </h2>
          <div ref="threadStateChartRef" class="w-full h-[300px]"></div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import {
  NSelect,
  NButton,
  NIcon,
  NAlert,
  NDescriptions,
  NDescriptionsItem,
  NTag,
  NProgress,
  NInputGroup,
  NInputGroupLabel,
  useMessage,
  type SelectOption
} from 'naive-ui'
import {
  RefreshOutline,
  InformationCircleOutline,
  TerminalOutline,
  TrashOutline,
  GitNetworkOutline,
  ServerOutline,
  LayersOutline,
  StatsChartOutline
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { useThemeStore } from '@/stores/theme'
import {
  getInstances,
  getInstanceHealth,
  getInstanceMetric,
  type ServiceInstance,
  type HealthResponse
} from '@/api/monitor'

const MAX_DATA_POINTS = 20

const themeStore = useThemeStore()
const message = useMessage()

const instances = ref<ServiceInstance[]>([])
const selectedInstance = ref<string | null>(null)
const currentInstance = ref<ServiceInstance | null>(null)
const loadingInstances = ref(false)
const loading = ref(false)

const refreshInterval = ref(2000)
const intervalOptions: SelectOption[] = [
  { label: '2秒', value: 2000 },
  { label: '5秒', value: 5000 },
  { label: '10秒', value: 10000 },
  { label: '30秒', value: 30000 },
  { label: '60秒', value: 60000 }
]

let refreshTimer: ReturnType<typeof setInterval> | null = null

const healthData = ref<HealthResponse | null>(null)

const processMetrics = ref({
  cpuUsage: 0,
  systemCpuUsage: 0,
  uptime: '-',
  startTime: '-'
})

const gcMetrics = ref<Array<{ name: string; count: number; time: string }>>([])

const threadMetrics = ref({
  live: 0,
  daemon: 0,
  peak: 0,
  started: 0
})

const threadStateMetrics = ref<Array<{ state: string; count: number }>>([])

const memoryMetrics = ref({
  heapUsed: 0,
  heapCommitted: 0,
  heapMax: 0,
  nonHeapUsed: 0,
  nonHeapCommitted: 0
})

interface HistoryPoint {
  time: string
  value: number
}

const heapUsedHistory = ref<HistoryPoint[]>([])
const heapCommittedHistory = ref<HistoryPoint[]>([])
const nonHeapUsedHistory = ref<HistoryPoint[]>([])
const nonHeapCommittedHistory = ref<HistoryPoint[]>([])
const threadLiveHistory = ref<HistoryPoint[]>([])
const threadDaemonHistory = ref<HistoryPoint[]>([])
const threadStateHistory = ref<Map<string, HistoryPoint[]>>(new Map())

const threadChartRef = ref<HTMLElement | null>(null)
const heapChartRef = ref<HTMLElement | null>(null)
const nonHeapChartRef = ref<HTMLElement | null>(null)
const threadStateChartRef = ref<HTMLElement | null>(null)

let threadChart: echarts.ECharts | null = null
let heapChart: echarts.ECharts | null = null
let nonHeapChart: echarts.ECharts | null = null
let threadStateChart: echarts.ECharts | null = null

const instanceOptions = computed<SelectOption[]>(() => {
  return instances.value.map(inst => ({
    label: `${inst.registration.name} (${inst.registration.serviceUrl})`,
    value: inst.id,
    disabled: inst.statusInfo.status !== 'UP'
  }))
})

const getStatusType = (status?: string): 'success' | 'error' | 'warning' | 'default' => {
  switch (status) {
    case 'UP': return 'success'
    case 'DOWN':
    case 'OFFLINE': return 'error'
    case 'OUT_OF_SERVICE': return 'warning'
    default: return 'default'
  }
}

const formatTime = (timestamp: string): string => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatBytesToMB = (bytes: number): number => {
  return Math.round(bytes / 1024 / 1024)
}

const formatDuration = (seconds: number): string => {
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (days > 0) return `${days}天 ${hours}小时`
  if (hours > 0) return `${hours}小时 ${minutes}分钟`
  return `${minutes}分钟`
}

const getCurrentTimeStr = (): string => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
}

const getMetricValue = (metric: any, statistic: string = 'VALUE'): number => {
  const m = metric?.measurements?.find((m: any) => m.statistic === statistic)
  return m?.value ?? 0
}

const addHistoryPoint = (history: HistoryPoint[], value: number) => {
  const time = getCurrentTimeStr()
  history.push({ time, value })
  if (history.length > MAX_DATA_POINTS) {
    history.shift()
  }
}

const getChartColors = () => {
  const isDark = themeStore.isDark
  return {
    textColor: isDark ? '#9CA3AF' : '#6B7280',
    splitLineColor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.05)',
    tooltipBg: isDark ? 'rgba(30, 41, 59, 0.9)' : 'rgba(255, 255, 255, 0.9)',
    tooltipBorder: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)',
    tooltipText: isDark ? '#F3F4F6' : '#111827'
  }
}

const initThreadChart = () => {
  if (!threadChartRef.value) return
  if (threadChartRef.value.clientWidth === 0 || threadChartRef.value.clientHeight === 0) return

  // 只在图表不存在时初始化
  if (!threadChart) {
    threadChart = echarts.init(threadChartRef.value)
  }

  const colors = getChartColors()
  const times = threadLiveHistory.value.map(p => p.time)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.tooltipText }
    },
    legend: {
      data: ['活动线程', '守护线程'],
      textStyle: { color: colors.textColor },
      top: 0
    },
    grid: { top: 30, right: 10, bottom: 50, left: 40 },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: { color: colors.textColor, fontSize: 10, rotate: 45 },
      axisLine: { lineStyle: { color: colors.splitLineColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: colors.textColor },
      splitLine: { lineStyle: { color: colors.splitLineColor } }
    },
    series: [
      {
        name: '活动线程',
        type: 'line',
        smooth: false,
        symbol: 'none',
        lineStyle: { color: '#EAB308', width: 2 },
        data: threadLiveHistory.value.map(p => p.value)
      },
      {
        name: '守护线程',
        type: 'line',
        smooth: false,
        symbol: 'none',
        lineStyle: { color: '#3B82F6', width: 2 },
        data: threadDaemonHistory.value.map(p => p.value)
      }
    ]
  }
  threadChart.setOption(option)
}

const initHeapChart = () => {
  if (!heapChartRef.value) return
  if (heapChartRef.value.clientWidth === 0 || heapChartRef.value.clientHeight === 0) return

  // 只在图表不存在时初始化
  if (!heapChart) {
    heapChart = echarts.init(heapChartRef.value)
  }

  const colors = getChartColors()
  const times = heapUsedHistory.value.map(p => p.time)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.tooltipText },
      formatter: (params: any) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach((p: any) => {
          result += `${p.marker} ${p.seriesName}: ${p.value} MB<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['已用', 'Committed'],
      textStyle: { color: colors.textColor },
      top: 0
    },
    grid: { top: 30, right: 10, bottom: 50, left: 50 },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: { color: colors.textColor, fontSize: 10, rotate: 45 },
      axisLine: { lineStyle: { color: colors.splitLineColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: colors.textColor, formatter: '{value} MB' },
      splitLine: { lineStyle: { color: colors.splitLineColor } }
    },
    series: [
      {
        name: '已用',
        type: 'line',
        smooth: false,
        symbol: 'none',
        lineStyle: { color: '#EAB308', width: 2 },
        data: heapUsedHistory.value.map(p => p.value)
      },
      {
        name: 'Committed',
        type: 'line',
        smooth: false,
        symbol: 'none',
        lineStyle: { color: '#14B8A6', width: 2 },
        data: heapCommittedHistory.value.map(p => p.value)
      }
    ]
  }
  heapChart.setOption(option)
}

const initNonHeapChart = () => {
  if (!nonHeapChartRef.value) return
  if (nonHeapChartRef.value.clientWidth === 0 || nonHeapChartRef.value.clientHeight === 0) return

  // 只在图表不存在时初始化
  if (!nonHeapChart) {
    nonHeapChart = echarts.init(nonHeapChartRef.value)
  }

  const colors = getChartColors()
  const times = nonHeapUsedHistory.value.map(p => p.time)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.tooltipText },
      formatter: (params: any) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach((p: any) => {
          result += `${p.marker} ${p.seriesName}: ${p.value} MB<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['已用', 'Committed'],
      textStyle: { color: colors.textColor },
      top: 0
    },
    grid: { top: 30, right: 10, bottom: 50, left: 50 },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: { color: colors.textColor, fontSize: 10, rotate: 45 },
      axisLine: { lineStyle: { color: colors.splitLineColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: colors.textColor, formatter: '{value} MB' },
      splitLine: { lineStyle: { color: colors.splitLineColor } }
    },
    series: [
      {
        name: '已用',
        type: 'line',
        smooth: false,
        symbol: 'none',
        lineStyle: { color: '#EAB308', width: 2 },
        data: nonHeapUsedHistory.value.map(p => p.value)
      },
      {
        name: 'Committed',
        type: 'line',
        smooth: false,
        symbol: 'none',
        lineStyle: { color: '#14B8A6', width: 2 },
        data: nonHeapCommittedHistory.value.map(p => p.value)
      }
    ]
  }
  nonHeapChart.setOption(option)
}

const initThreadStateChart = () => {
  if (!threadStateChartRef.value) return
  if (threadStateChartRef.value.clientWidth === 0 || threadStateChartRef.value.clientHeight === 0) return

  // 只在图表不存在时初始化
  if (!threadStateChart) {
    threadStateChart = echarts.init(threadStateChartRef.value)
  }

  const colors = getChartColors()
  const stateColors: Record<string, string> = {
    'runnable': '#22C55E',
    'blocked': '#EF4444',
    'waiting': '#F97316',
    'timed-waiting': '#8B5CF6',
    'new': '#06B6D4',
    'terminated': '#6B7280'
  }

  const states = Array.from(threadStateHistory.value.keys())
  const times = states.length > 0
    ? (threadStateHistory.value.get(states[0]) || []).map(p => p.time)
    : []

  const series = states.map(state => {
    const history = threadStateHistory.value.get(state) || []
    return {
      name: state,
      type: 'line',
      smooth: true,
      symbol: 'none',
      stack: 'Total',
      lineStyle: { width: 0 },
      areaStyle: { color: stateColors[state] || '#9CA3AF' },
      emphasis: { focus: 'series' },
      data: history.map(p => p.value)
    }
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.tooltipText },
      axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } }
    },
    legend: {
      data: states,
      textStyle: { color: colors.textColor },
      top: 0
    },
    grid: { top: 30, right: 10, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
      axisLabel: { color: colors.textColor, fontSize: 10 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: colors.textColor },
      splitLine: { lineStyle: { color: colors.splitLineColor } }
    },
    series
  }
  threadStateChart.setOption(option)
}

const loadInstances = async () => {
  loadingInstances.value = true
  try {
    instances.value = await getInstances()
  } catch (error) {
    message.error('加载服务实例失败')
  } finally {
    loadingInstances.value = false
  }
}

const loadHealthData = async () => {
  if (!selectedInstance.value) return
  try {
    healthData.value = await getInstanceHealth(selectedInstance.value)
  } catch (error) {
    console.error('Failed to load health data', error)
  }
}

const loadProcessMetrics = async () => {
  if (!selectedInstance.value) return
  try {
    const [cpuUsage, systemCpu, uptime, startTime] = await Promise.all([
      getInstanceMetric(selectedInstance.value, 'process.cpu.usage').catch(() => null),
      getInstanceMetric(selectedInstance.value, 'system.cpu.usage').catch(() => null),
      getInstanceMetric(selectedInstance.value, 'process.uptime').catch(() => null),
      getInstanceMetric(selectedInstance.value, 'process.start.time').catch(() => null)
    ])

    processMetrics.value = {
      cpuUsage: Math.round(getMetricValue(cpuUsage) * 100),
      systemCpuUsage: Math.round(getMetricValue(systemCpu) * 100),
      uptime: uptime ? formatDuration(getMetricValue(uptime)) : '-',
      startTime: startTime ? new Date(getMetricValue(startTime) * 1000).toLocaleString('zh-CN') : '-'
    }
  } catch (error) {
    console.error('Failed to load process metrics', error)
  }
}

const loadGCMetrics = async () => {
  if (!selectedInstance.value) return
  try {
    const gcPause = await getInstanceMetric(selectedInstance.value, 'jvm.gc.pause').catch(() => null)
    const gcNames = gcPause?.availableTags?.find(t => t.tag === 'gc')?.values || []
    const results: Array<{ name: string; count: number; time: string }> = []

    for (const gc of gcNames) {
      try {
        const gcData = await getInstanceMetric(selectedInstance.value!, 'jvm.gc.pause', [`gc:${gc}`])
        const count = getMetricValue(gcData, 'COUNT')
        const totalTime = getMetricValue(gcData, 'TOTAL_TIME')
        results.push({
          name: gc,
          count,
          time: `${(totalTime * 1000).toFixed(2)} ms`
        })
      } catch (e) {
        // ignore
      }
    }

    gcMetrics.value = results
  } catch (error) {
    console.error('Failed to load GC metrics', error)
  }
}

const loadThreadMetrics = async () => {
  if (!selectedInstance.value) return
  try {
    const [live, daemon, peak, started, states] = await Promise.all([
      getInstanceMetric(selectedInstance.value, 'jvm.threads.live').catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.threads.daemon').catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.threads.peak').catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.threads.started').catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.threads.states').catch(() => null)
    ])

    const liveCount = getMetricValue(live)
    const daemonCount = getMetricValue(daemon)
    threadMetrics.value = {
      live: liveCount,
      daemon: daemonCount,
      peak: getMetricValue(peak),
      started: getMetricValue(started)
    }

    addHistoryPoint(threadLiveHistory.value, liveCount)
    addHistoryPoint(threadDaemonHistory.value, daemonCount)

    const stateTag = states?.availableTags?.find(t => t.tag === 'state')
    if (stateTag) {
      const stateResults: Array<{ state: string; count: number }> = []
      for (const state of stateTag.values) {
        try {
          const stateData = await getInstanceMetric(selectedInstance.value!, 'jvm.threads.states', [`state:${state}`])
          const count = getMetricValue(stateData)
          stateResults.push({ state, count })

          if (!threadStateHistory.value.has(state)) {
            threadStateHistory.value.set(state, [])
          }
          const stateHistory = threadStateHistory.value.get(state)!
          addHistoryPoint(stateHistory, count)
        } catch (e) {
          // ignore
        }
      }
      threadStateMetrics.value = stateResults
    }
  } catch (error) {
    console.error('Failed to load thread metrics', error)
  }
}

const loadMemoryMetrics = async () => {
  if (!selectedInstance.value) return
  try {
    const [heapUsed, heapCommitted, heapMax, nonHeapUsed, nonHeapCommitted] = await Promise.all([
      getInstanceMetric(selectedInstance.value, 'jvm.memory.used', ['area:heap']).catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.memory.committed', ['area:heap']).catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.memory.max', ['area:heap']).catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.memory.used', ['area:nonheap']).catch(() => null),
      getInstanceMetric(selectedInstance.value, 'jvm.memory.committed', ['area:nonheap']).catch(() => null)
    ])

    const heapUsedVal = getMetricValue(heapUsed)
    const heapCommittedVal = getMetricValue(heapCommitted)
    const nonHeapUsedVal = getMetricValue(nonHeapUsed)
    const nonHeapCommittedVal = getMetricValue(nonHeapCommitted)

    memoryMetrics.value = {
      heapUsed: heapUsedVal,
      heapCommitted: heapCommittedVal,
      heapMax: getMetricValue(heapMax),
      nonHeapUsed: nonHeapUsedVal,
      nonHeapCommitted: nonHeapCommittedVal
    }

    addHistoryPoint(heapUsedHistory.value, formatBytesToMB(heapUsedVal))
    addHistoryPoint(heapCommittedHistory.value, formatBytesToMB(heapCommittedVal))
    addHistoryPoint(nonHeapUsedHistory.value, formatBytesToMB(nonHeapUsedVal))
    addHistoryPoint(nonHeapCommittedHistory.value, formatBytesToMB(nonHeapCommittedVal))
  } catch (error) {
    console.error('Failed to load memory metrics', error)
  }
}

const disposeCharts = () => {
  threadChart?.dispose()
  heapChart?.dispose()
  nonHeapChart?.dispose()
  threadStateChart?.dispose()
  threadChart = null
  heapChart = null
  nonHeapChart = null
  threadStateChart = null
}

const clearHistory = () => {
  heapUsedHistory.value = []
  heapCommittedHistory.value = []
  nonHeapUsedHistory.value = []
  nonHeapCommittedHistory.value = []
  threadLiveHistory.value = []
  threadDaemonHistory.value = []
  threadStateHistory.value = new Map()
  // 切换实例时销毁旧图表，以便重新创建
  disposeCharts()
}

const refreshData = async (showLoading = true) => {
  if (!selectedInstance.value) return
  if (showLoading) loading.value = true
  try {
    currentInstance.value = instances.value.find(i => i.id === selectedInstance.value) || null
    await Promise.all([
      loadHealthData(),
      loadProcessMetrics(),
      loadGCMetrics(),
      loadThreadMetrics(),
      loadMemoryMetrics()
    ])
    await nextTick()
    initCharts()
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    if (showLoading) loading.value = false
  }
}

const initCharts = () => {
  nextTick(() => {
    // 确保 DOM 元素已经存在于文档中
    if (!threadChartRef.value || threadChartRef.value.clientWidth === 0) {
      // 如果 DOM 还没好，稍微延迟再试一次（针对 v-if 的过渡期）
      setTimeout(initCharts, 100);
      return;
    }

    initThreadChart();
    initHeapChart();
    initNonHeapChart();
    initThreadStateChart();
  });
}

const startAutoRefresh = () => {
  stopAutoRefresh()
  if (selectedInstance.value) {
    refreshTimer = setInterval(() => {
      refreshData(false)
    }, refreshInterval.value)
  }
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const handleInstanceChange = (value: string) => {
  selectedInstance.value = value
  clearHistory()
  if (value) {
    refreshData()
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const handleIntervalChange = () => {
  if (selectedInstance.value) {
    startAutoRefresh()
  }
}

const handleResize = () => {
  threadChart?.resize()
  heapChart?.resize()
  nonHeapChart?.resize()
  threadStateChart?.resize()
}

onMounted(() => {
  loadInstances()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  stopAutoRefresh()
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

watch(() => themeStore.isDark, () => {
  if (selectedInstance.value) {
    // 主题切换时销毁并重建图表以应用新配色
    disposeCharts()
    nextTick(() => {
      initCharts()
    })
  }
})
</script>
