<template>
  <div class="p-8 space-y-8">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <n-card
        v-for="stat in stats"
        :key="stat.title"
        class="glass-card"
        size="small"
        :bordered="false"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm mb-1">{{ stat.title }}</p>
            <h3 class="text-2xl font-bold">{{ stat.value }}</h3>
            <n-tag
              size="small"
              round
              :type="stat.trend > 0 ? 'success' : stat.trend < 0 ? 'error' : 'default'"
              class="mt-2"
            >
              {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}% from last month
            </n-tag>
          </div>
          <div class="w-12 h-12 rounded-xl flex items-center justify-center bg-white/40 dark:bg-white/5 backdrop-blur-sm">
            <n-icon size="24">
              <component :is="stat.icon" />
            </n-icon>
          </div>
        </div>
      </n-card>
    </div>

    <div class="glass-card !p-6 animate-enter delay-200">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-text-main">服务请求数量</h2>
        <div class="flex gap-2">
          <n-button size="small" quaternary class="hover:bg-white/50">Weekly</n-button>
          <n-button size="small" type="primary" secondary class="shadow-sm">Monthly</n-button>
        </div>
      </div>
      <div ref="chartRef" class="w-full h-[350px]"></div>
    </div>

    <div class="glass-card !p-0 overflow-hidden animate-enter delay-300">
      <div class="p-6 border-b border-white/40 flex justify-between items-center">
        <h2 class="text-xl font-bold text-text-main">定时任务日志</h2>
        <n-button size="small" quaternary class="text-primary font-medium hover:bg-white/50">View All</n-button>
      </div>
      <n-data-table
        :columns="columns"
        :data="tableData"
        :bordered="false"
        class="glass-table"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, h, watch } from 'vue'
import { NIcon, NButton, NDataTable, NTag, NCard, type DataTableColumns } from 'naive-ui'
import {
  CardOutline,
  PeopleOutline,
  PersonOutline,
  ServerOutline,
  PulseOutline
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { useThemeStore } from '@/stores/theme'
import { getDashboardStats, type DashboardStat, type DashboardTransaction } from '@/api/dashboard'

const themeStore = useThemeStore()

interface ViewStat extends DashboardStat {
  icon: any
}

const statIconMap: Record<string, any> = {
  userTotal: PeopleOutline,
  onlineUsers: PersonOutline,
  servicesOnline: ServerOutline,
  jobPending: PulseOutline
}

const stats = ref<ViewStat[]>([])

const chartDates = ref<string[]>([])
const chartActiveUsers = ref<number[]>([])

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (chartRef.value) {
    if (chartInstance) {
      chartInstance.dispose()
    }
    chartInstance = echarts.init(chartRef.value)

    const isDark = themeStore.isDark
    const textColor = isDark ? '#9CA3AF' : '#6B7280'
    const splitLineColor = isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)'

    const xData = chartDates.value.length ? chartDates.value : ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    const yData = chartActiveUsers.value.length ? chartActiveUsers.value : [820, 932, 901, 934, 1290, 1330, 1320]

    const option = {
      grid: {
        top: '10%',
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: isDark ? 'rgba(30, 41, 59, 0.9)' : 'rgba(255, 255, 255, 0.9)',
        borderColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)',
        textStyle: {
          color: isDark ? '#F3F4F6' : '#111827'
        }
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xData,
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
          name: '服务请求数',
          type: 'line',
          smooth: true,
          symbol: 'none',
          lineStyle: {
            color: '#4F46E5',
            width: 3
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(79, 70, 229, 0.2)' },
              { offset: 1, color: 'rgba(79, 70, 229, 0)' }
            ])
          },
          data: yData
        }
      ]
    }
    chartInstance.setOption(option)
  }
}

watch(
  () => themeStore.isDark,
  () => {
    initChart()
  }
)

const tableData = ref<DashboardTransaction[]>([])

const columns: DataTableColumns<DashboardTransaction> = [
  { title: 'Transaction ID', key: 'id' },
  {
    title: 'User',
    key: 'user',
    render: row => h('div', { class: 'font-medium text-text-main' }, row.user)
  },
  { title: 'Amount', key: 'amount' },
  { title: 'Date', key: 'date' },
  {
    title: 'Status',
    key: 'status',
    render: row => {
      const type =
        row.status === 'completed' ? 'success' : row.status === 'pending' ? 'warning' : 'error'
      return h(
        NTag,
        { type, bordered: false, size: 'small', round: true },
        { default: () => row.status }
      )
    }
  }
]

const loadDashboard = async () => {
  try {
    const data = await getDashboardStats()
    stats.value = data.stats.map(item => ({
      ...item,
      icon: statIconMap[item.key] || CardOutline
    }))
    chartDates.value = data.chart.dates
    chartActiveUsers.value = data.chart.activeUsers
    tableData.value = data.table
  } catch (error) {
    console.error('Failed to load dashboard stats', error)
  }
}

onMounted(async () => {
  await loadDashboard()
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

const handleResize = () => {
  chartInstance?.resize()
}
</script>

