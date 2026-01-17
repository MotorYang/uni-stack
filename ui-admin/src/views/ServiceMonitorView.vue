<template>
  <div class="p-8 space-y-8">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <n-card class="glass-card" size="small" :bordered="false">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">服务总数</p>
            <h3 class="text-2xl font-bold">{{ metrics.totalServices }}</h3>
            <n-tag size="small" round type="success" class="mt-2">
              在线 {{ metrics.onlineServices }} 个
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
            <p class="text-sm mb-1">平均响应时间</p>
            <h3 class="text-2xl font-bold">{{ metrics.avgLatency }} ms</h3>
            <n-tag size="small" round :type="metrics.avgLatencyTrend > 0 ? 'warning' : 'success'" class="mt-2">
              {{ metrics.avgLatencyTrend > 0 ? '+' : '' }}{{ metrics.avgLatencyTrend }}% vs 昨日
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24">
              <PulseOutline />
            </n-icon>
          </div>
        </div>
      </n-card>

      <n-card class="glass-card" size="small" :bordered="false">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">错误率</p>
            <h3 class="text-2xl font-bold">{{ metrics.errorRate }}%</h3>
            <n-tag size="small" round :type="metrics.errorRate > 1 ? 'error' : 'success'" class="mt-2">
              {{ metrics.errorRate > 1 ? '需要关注' : '运行稳定' }}
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24">
              <WarningOutline />
            </n-icon>
          </div>
        </div>
      </n-card>

      <n-card class="glass-card" size="small" :bordered="false">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">今日请求量</p>
            <h3 class="text-2xl font-bold">{{ metrics.todayRequests.toLocaleString() }}</h3>
            <n-tag size="small" round type="success" class="mt-2">
              峰值 {{ metrics.peakQps }} QPS
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24">
              <TrendingUpOutline />
            </n-icon>
          </div>
        </div>
      </n-card>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 glass-card !p-6 animate-enter delay-200">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-text-main">服务请求趋势</h2>
          <n-button quaternary size="small">最近 24 小时</n-button>
        </div>
        <div ref="requestChartRef" class="w-full h-[320px]"></div>
      </div>

      <div class="glass-card !p-6 animate-enter delay-300">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-text-main">服务错误率</h2>
        </div>
        <div ref="errorChartRef" class="w-full h-[320px]"></div>
      </div>
    </div>

    <div class="glass-card !p-0 overflow-hidden animate-enter delay-300">
      <div class="p-6 border-b border-white/40 flex justify-between items-center">
        <h2 class="text-xl font-bold text-text-main">Spring Cloud 服务实例</h2>
      </div>
      <n-data-table
        :columns="columns"
        :data="services"
        :bordered="false"
        class="glass-table"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, h, watch } from 'vue'
import { NCard, NTag, NIcon, NButton, NDataTable, type DataTableColumns } from 'naive-ui'
import { 
  ServerOutline,
  PulseOutline,
  WarningOutline,
  TrendingUpOutline,
  CheckmarkCircleOutline,
  AlertCircleOutline,
  TimeOutline
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { useThemeStore } from '@/stores/theme'
import { getServiceRequestTrend, getServiceErrorRate, type ServiceRequestTrendPoint, type ServiceErrorRatePoint } from '@/api/dashboard'

const themeStore = useThemeStore()

interface ServiceMetric {
  name: string
  group: string
  instances: number
  qps: number
  avgLatency: number
  errorRate: number
  status: 'healthy' | 'warning' | 'down'
}

const metrics = ref({
  totalServices: 12,
  onlineServices: 11,
  avgLatency: 42,
  avgLatencyTrend: -3.2,
  errorRate: 0.8,
  todayRequests: 182340,
  peakQps: 860
})

const services = ref<ServiceMetric[]>([
  {
    name: 'auth-service',
    group: '认证中心',
    instances: 3,
    qps: 320,
    avgLatency: 35,
    errorRate: 0.4,
    status: 'healthy'
  },
  {
    name: 'gateway',
    group: '网关集群',
    instances: 4,
    qps: 520,
    avgLatency: 28,
    errorRate: 1.1,
    status: 'warning'
  },
  {
    name: 'order-service',
    group: '业务服务',
    instances: 2,
    qps: 210,
    avgLatency: 52,
    errorRate: 0.9,
    status: 'healthy'
  },
  {
    name: 'job-scheduler',
    group: '任务调度',
    instances: 1,
    qps: 32,
    avgLatency: 120,
    errorRate: 2.3,
    status: 'warning'
  },
  {
    name: 'log-service',
    group: '基础设施',
    instances: 2,
    qps: 64,
    avgLatency: 80,
    errorRate: 0.3,
    status: 'healthy'
  }
])

const requestTrendData = ref<ServiceRequestTrendPoint[]>([])
const errorRateData = ref<ServiceErrorRatePoint[]>([])

const columns: DataTableColumns<ServiceMetric> = [
  {
    title: '服务名称',
    key: 'name',
    render: (row) =>
      h('div', { class: 'flex flex-col' }, [
        h('span', { class: 'font-medium text-text-main' }, row.name),
        h('span', { class: 'text-xs text-text-sec' }, row.group)
      ])
  },
  {
    title: '实例数',
    key: 'instances'
  },
  {
    title: 'QPS',
    key: 'qps'
  },
  {
    title: '平均延迟(ms)',
    key: 'avgLatency'
  },
  {
    title: '错误率(%)',
    key: 'errorRate'
  },
  {
    title: '状态',
    key: 'status',
    render: (row) => {
      const type = row.status === 'healthy' ? 'success' : row.status === 'warning' ? 'warning' : 'error'
      const icon =
        row.status === 'healthy' ? CheckmarkCircleOutline : row.status === 'warning' ? AlertCircleOutline : TimeOutline
      const label =
        row.status === 'healthy' ? '健康' : row.status === 'warning' ? '需关注' : '异常'
      return h(
        NTag,
        { type, size: 'small', round: true, bordered: false, icon: () => h(NIcon, null, { default: () => h(icon) }) },
        { default: () => label }
      )
    }
  }
]

const requestChartRef = ref<HTMLElement | null>(null)
const errorChartRef = ref<HTMLElement | null>(null)

let requestChart: echarts.ECharts | null = null
let errorChart: echarts.ECharts | null = null

const initRequestChart = () => {
  if (!requestChartRef.value) return
  if (requestChart) requestChart.dispose()
  requestChart = echarts.init(requestChartRef.value)

  const isDark = themeStore.isDark
  const textColor = isDark ? '#9CA3AF' : '#6B7280'
  const splitLineColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.05)'

  const data = requestTrendData.value.length
    ? requestTrendData.value
    : Array.from({ length: 24 }, (_, i) => ({
        time: `${i}:00`,
        count: 100 + Math.round(Math.random() * 200)
      }))

  const hours = data.map((item) => item.time)
  const values = data.map((item) => item.count)

  const option = {
    grid: {
      top: '10%',
      left: '3%',
      right: '4%',
      bottom: '6%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: isDark ? 'rgba(15, 23, 42, 0.9)' : 'rgba(255, 255, 255, 0.9)',
      borderColor: isDark ? 'rgba(148, 163, 184, 0.3)' : 'rgba(0, 0, 0, 0.08)',
      textStyle: {
        color: isDark ? '#F9FAFB' : '#111827'
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: hours,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: textColor }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: {
          color: splitLineColor
        }
      },
      axisLabel: { color: textColor }
    },
    series: [
      {
        name: '请求数',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          color: '#4F46E5',
          width: 3
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79, 70, 229, 0.3)' },
            { offset: 1, color: 'rgba(79, 70, 229, 0)' }
          ])
        },
        data: values
      }
    ]
  }

  requestChart.setOption(option)
}

const initErrorChart = () => {
  if (!errorChartRef.value) return
  if (errorChart) errorChart.dispose()
  errorChart = echarts.init(errorChartRef.value)

  const isDark = themeStore.isDark
  const textColor = isDark ? '#9CA3AF' : '#6B7280'
  const splitLineColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.05)'

  const hasApiErrorData = errorRateData.value.length > 0
  const names = hasApiErrorData ? errorRateData.value.map((item) => item.serviceName) : services.value.map((s) => s.name)
  const errorRates = hasApiErrorData ? errorRateData.value.map((item) => item.errorRate) : services.value.map((s) => s.errorRate)

  const option = {
    grid: {
      top: '10%',
      left: '3%',
      right: '4%',
      bottom: '12%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: isDark ? 'rgba(15, 23, 42, 0.9)' : 'rgba(255, 255, 255, 0.9)',
      borderColor: isDark ? 'rgba(148, 163, 184, 0.3)' : 'rgba(0, 0, 0, 0.08)',
      textStyle: {
        color: isDark ? '#F9FAFB' : '#111827'
      }
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        color: textColor,
        rotate: 20
      },
      axisTick: { show: false },
      axisLine: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: textColor,
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          color: splitLineColor
        }
      }
    },
    series: [
      {
        name: '错误率',
        type: 'bar',
        data: errorRates,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(248, 113, 113, 0.95)' },
            { offset: 1, color: 'rgba(248, 113, 113, 0.35)' }
          ])
        },
        barWidth: '50%'
      }
    ]
  }

  errorChart.setOption(option)
}

const loadMonitorData = async () => {
  try {
    const [trendRes, errorRes] = await Promise.all([
      getServiceRequestTrend(),
      getServiceErrorRate()
    ])
    requestTrendData.value = trendRes.points
    errorRateData.value = errorRes.points
  } catch (error) {
    console.error('Failed to load service monitor data', error)
  } finally {
    initRequestChart()
    initErrorChart()
  }
}

watch(
  () => themeStore.isDark,
  () => {
    initRequestChart()
    initErrorChart()
  }
)

const handleResize = () => {
  requestChart?.resize()
  errorChart?.resize()
}

onMounted(() => {
  loadMonitorData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  requestChart?.dispose()
  errorChart?.dispose()
})
</script>
