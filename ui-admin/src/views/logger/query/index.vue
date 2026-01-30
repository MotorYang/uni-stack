<template>
  <div class="p-8 space-y-6">
    <div class="glass-card !p-6">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-text-main">日志查询</h2>
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
            :loading="loadingFiles"
            :disabled="!selectedInstance"
            @click="loadLogFiles"
          >
            <template #icon>
              <n-icon><RefreshOutline /></n-icon>
            </template>
            刷新
          </n-button>
        </div>
      </div>

      <!-- File Selection & Controls -->
      <div class="flex flex-wrap items-center gap-4 mb-4">
        <n-select
          v-model:value="selectedFile"
          :options="fileOptions"
          placeholder="选择日志文件"
          style="width: 320px"
          :disabled="!selectedInstance || loadingFiles"
          @update:value="handleFileChange"
        />
        <n-button
          type="primary"
          :disabled="!selectedFile"
          :loading="loadingContent"
          @click="handleLoadFromEnd"
        >
          <template #icon>
            <n-icon><ArrowDownOutline /></n-icon>
          </template>
          查看最新
        </n-button>
        <n-button
          :disabled="!selectedFile"
          :loading="loadingContent"
          @click="handleLoadFromStart"
        >
          <template #icon>
            <n-icon><PlayOutline /></n-icon>
          </template>
          从头查看
        </n-button>
        <n-button
          :disabled="!selectedFile"
          :loading="downloading"
          @click="handleDownload"
        >
          <template #icon>
            <n-icon><DownloadOutline /></n-icon>
          </template>
          下载
        </n-button>
      </div>

      <!-- File Info Alert -->
      <n-alert v-if="!selectedInstance" type="info" class="mb-4">
        请先选择一个服务实例以查看日志文件
      </n-alert>

      <n-alert v-else-if="!selectedFile && logFiles.length === 0 && !loadingFiles" type="warning" class="mb-4">
        该服务实例没有可用的日志文件
      </n-alert>

      <n-alert v-else-if="selectedFile && currentFileInfo" type="default" class="mb-4">
        <div class="flex items-center justify-between">
          <span>
            <strong>{{ currentFileInfo.fileName }}</strong> -
            大小: {{ formatFileSize(currentFileInfo.size) }} |
            最后修改: {{ formatDateTime(currentFileInfo.lastModified) }}
          </span>
          <span v-if="totalSize > 0">
            已加载: {{ formatFileSize(loadedStartPointer) }} - {{ formatFileSize(loadedEndPointer) }}
            / {{ formatFileSize(totalSize) }}
          </span>
        </div>
      </n-alert>

      <!-- Log Content Display -->
      <div v-if="logLines.length > 0" class="log-content-container">
        <div class="log-toolbar">
          <n-checkbox v-model:checked="wrapLines">自动换行</n-checkbox>
          <div class="flex items-center gap-2">
            <n-checkbox v-model:checked="autoRefresh">自动刷新</n-checkbox>
            <n-select
              v-if="autoRefresh"
              v-model:value="refreshInterval"
              :options="refreshIntervalOptions"
              size="small"
              style="width: 100px"
            />
          </div>
          <div class="flex-1"></div>
          <n-tag v-if="isLoadingMore" type="info" size="small">
            <template #icon>
              <n-icon><SyncOutline /></n-icon>
            </template>
            加载中...
          </n-tag>
          <n-tag v-if="autoRefresh && isRefreshing" type="success" size="small">
            <template #icon>
              <n-icon><SyncOutline /></n-icon>
            </template>
            刷新中...
          </n-tag>
          <span class="text-sm opacity-60">{{ logLines.length }} 行</span>
          <n-button size="small" quaternary @click="copyContent">
            <template #icon>
              <n-icon><CopyOutline /></n-icon>
            </template>
            复制
          </n-button>
          <n-button size="small" quaternary @click="clearContent">
            <template #icon>
              <n-icon><TrashOutline /></n-icon>
            </template>
            清空
          </n-button>
        </div>

        <!-- Scroll Container with dynamic loading -->
        <div
          ref="scrollContainerRef"
          class="log-scroll-container"
          :style="{ height: 'calc(100vh - 420px)' }"
          @scroll="handleScroll"
        >
          <!-- Top loading indicator -->
          <div v-if="loadedStartPointer > 0" class="load-more-hint">
            <span v-if="isLoadingMore">加载中...</span>
            <span v-else>↑ 向上滚动加载更多</span>
          </div>

          <!-- Log lines -->
          <div class="log-lines" :class="{ 'wrap-lines': wrapLines }">
            <div
              v-for="(line, index) in logLines"
              :key="index"
              class="log-line"
              :class="getLineClass(line)"
            >
              <span class="line-number">{{ index + 1 }}</span>
              <span class="line-content">{{ line }}</span>
            </div>
          </div>

          <!-- Bottom loading indicator -->
          <div v-if="loadedEndPointer < totalSize" class="load-more-hint">
            <span v-if="isLoadingMore">加载中...</span>
            <span v-else>↓ 向下滚动加载更多</span>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <n-empty
        v-else-if="selectedFile && !loadingContent"
        description="点击 '查看最新' 或 '从头查看' 加载日志内容"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import {
  NSelect,
  NButton,
  NIcon,
  NAlert,
  NEmpty,
  NCheckbox,
  NTag,
  useMessage,
  type SelectOption
} from 'naive-ui'
import {
  RefreshOutline,
  ArrowDownOutline,
  PlayOutline,
  CopyOutline,
  TrashOutline,
  DownloadOutline,
  SyncOutline
} from '@vicons/ionicons5'
import {
  getInstances,
  type ServiceInstance
} from '@/api/monitor'
import {
  getLogFiles,
  readLogContent,
  downloadLogFile,
  type LogFileInfo
} from '@/api/logfile'

const message = useMessage()

// Constants
const CHUNK_SIZE = 102400 // 100KB per chunk
const LOAD_THRESHOLD = 300 // pixels from edge to trigger load

// Instance selection
const instances = ref<ServiceInstance[]>([])
const selectedInstance = ref<string | null>(null)
const loadingInstances = ref(false)

// File selection
const logFiles = ref<LogFileInfo[]>([])
const selectedFile = ref<string | null>(null)
const loadingFiles = ref(false)

// Content
const logLines = ref<string[]>([])
const loadingContent = ref(false)
const isLoadingMore = ref(false)
const totalSize = ref(0)
const loadedStartPointer = ref(0)
const loadedEndPointer = ref(0)

// Download
const downloading = ref(false)

// Options
const wrapLines = ref(false)

// Auto refresh
const autoRefresh = ref(false)
const refreshInterval = ref(3000)
const isRefreshing = ref(false)
let refreshTimer: ReturnType<typeof setInterval> | null = null

const refreshIntervalOptions: SelectOption[] = [
  { label: '1秒', value: 1000 },
  { label: '3秒', value: 3000 },
  { label: '5秒', value: 5000 },
  { label: '10秒', value: 10000 }
]

// Scroll container ref
const scrollContainerRef = ref<HTMLDivElement | null>(null)

// Computed
const instanceOptions = computed<SelectOption[]>(() => {
  const serviceMap = new Map<string, ServiceInstance>()
  instances.value.forEach(inst => {
    const serviceName = inst.registration.name
    const existing = serviceMap.get(serviceName)
    if (!existing || (inst.statusInfo.status === 'UP' && existing.statusInfo.status !== 'UP')) {
      serviceMap.set(serviceName, inst)
    }
  })

  return Array.from(serviceMap.entries()).map(([serviceName, inst]) => ({
    label: `${serviceName} (${inst.registration.serviceUrl})`,
    value: serviceName,
    disabled: inst.statusInfo.status !== 'UP'
  }))
})

const fileOptions = computed<SelectOption[]>(() => {
  return logFiles.value.map(file => ({
    label: `${file.fileName} (${formatFileSize(file.size)})`,
    value: file.fileName
  }))
})

const currentFileInfo = computed(() => {
  if (!selectedFile.value) return null
  return logFiles.value.find(f => f.fileName === selectedFile.value)
})

// Helpers
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDateTime = (dateStr: string): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getLineClass = (content: string): Record<string, boolean> => {
  const upperContent = content.toUpperCase()
  return {
    'line-error': upperContent.includes(' ERROR ') || upperContent.includes('[ERROR]'),
    'line-warn': upperContent.includes(' WARN ') || upperContent.includes('[WARN]'),
    'line-info': upperContent.includes(' INFO ') || upperContent.includes('[INFO]'),
    'line-debug': upperContent.includes(' DEBUG ') || upperContent.includes('[DEBUG]')
  }
}

// API calls
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

const loadLogFiles = async () => {
  if (!selectedInstance.value) return

  loadingFiles.value = true
  try {
    logFiles.value = await getLogFiles(selectedInstance.value)
    if (logFiles.value.length > 0 && !selectedFile.value) {
      selectedFile.value = logFiles.value[0].fileName
    }
  } catch (error) {
    message.error('加载日志文件列表失败')
    console.error('Failed to load log files', error)
  } finally {
    loadingFiles.value = false
  }
}

// Load content from the end (latest logs)
const handleLoadFromEnd = async () => {
  if (!selectedInstance.value || !selectedFile.value) return

  loadingContent.value = true
  clearContent()

  try {
    // First get file size
    const sizeCheck = await readLogContent(selectedInstance.value, selectedFile.value, 0, 1)
    totalSize.value = sizeCheck.totalSize

    // Calculate start position for last chunk
    const startPos = Math.max(0, totalSize.value - CHUNK_SIZE)
    const response = await readLogContent(
      selectedInstance.value,
      selectedFile.value,
      startPos,
      CHUNK_SIZE
    )

    processContent(response.content)
    loadedStartPointer.value = startPos
    loadedEndPointer.value = response.nextPointer
    totalSize.value = response.totalSize

    // Scroll to bottom
    await nextTick()
    scrollToBottom()
  } catch (error) {
    message.error('读取日志失败')
    console.error('Failed to load log', error)
  } finally {
    loadingContent.value = false
  }
}

// Load content from start
const handleLoadFromStart = async () => {
  if (!selectedInstance.value || !selectedFile.value) return

  loadingContent.value = true
  clearContent()

  try {
    const response = await readLogContent(
      selectedInstance.value,
      selectedFile.value,
      0,
      CHUNK_SIZE
    )

    processContent(response.content)
    loadedStartPointer.value = 0
    loadedEndPointer.value = response.nextPointer
    totalSize.value = response.totalSize

    // Scroll to top
    await nextTick()
    if (scrollContainerRef.value) {
      scrollContainerRef.value.scrollTop = 0
    }
  } catch (error) {
    message.error('读取日志失败')
    console.error('Failed to load log', error)
  } finally {
    loadingContent.value = false
  }
}

// Process content into lines
const processContent = (content: string) => {
  const lines = content.split('\n')
  if (lines.length > 0 && lines[lines.length - 1] === '') {
    lines.pop()
  }
  logLines.value = lines
}

// Load more content (prepend)
const loadMoreBefore = async () => {
  if (!selectedInstance.value || !selectedFile.value) return
  if (loadedStartPointer.value <= 0) return
  if (isLoadingMore.value) return

  isLoadingMore.value = true
  try {
    const newStart = Math.max(0, loadedStartPointer.value - CHUNK_SIZE)
    const readSize = loadedStartPointer.value - newStart

    const response = await readLogContent(
      selectedInstance.value,
      selectedFile.value,
      newStart,
      readSize
    )

    const lines = response.content.split('\n')
    if (lines.length > 0 && lines[lines.length - 1] === '') {
      lines.pop()
    }

    // Remember scroll position relative to content
    const container = scrollContainerRef.value
    const oldScrollHeight = container?.scrollHeight || 0

    // Prepend lines
    logLines.value = [...lines, ...logLines.value]
    loadedStartPointer.value = newStart

    // Restore scroll position
    await nextTick()
    if (container) {
      const newScrollHeight = container.scrollHeight
      container.scrollTop += newScrollHeight - oldScrollHeight
    }
  } catch (error) {
    console.error('Failed to load more content', error)
  } finally {
    isLoadingMore.value = false
  }
}

// Load more content (append)
const loadMoreAfter = async () => {
  if (!selectedInstance.value || !selectedFile.value) return
  if (loadedEndPointer.value >= totalSize.value) return
  if (isLoadingMore.value) return

  isLoadingMore.value = true
  try {
    const response = await readLogContent(
      selectedInstance.value,
      selectedFile.value,
      loadedEndPointer.value,
      CHUNK_SIZE
    )

    const lines = response.content.split('\n')
    if (lines.length > 0 && lines[lines.length - 1] === '') {
      lines.pop()
    }

    logLines.value = [...logLines.value, ...lines]
    loadedEndPointer.value = response.nextPointer
    totalSize.value = response.totalSize
  } catch (error) {
    console.error('Failed to load more content', error)
  } finally {
    isLoadingMore.value = false
  }
}

// Scroll handler
const handleScroll = (event: Event) => {
  const target = event.target as HTMLDivElement

  // Check if near top - load previous content
  if (target.scrollTop < LOAD_THRESHOLD && loadedStartPointer.value > 0) {
    loadMoreBefore()
  }

  // Check if near bottom - load next content
  const bottomDistance = target.scrollHeight - target.scrollTop - target.clientHeight
  if (bottomDistance < LOAD_THRESHOLD && loadedEndPointer.value < totalSize.value) {
    loadMoreAfter()
  }
}

const scrollToBottom = () => {
  if (scrollContainerRef.value) {
    scrollContainerRef.value.scrollTop = scrollContainerRef.value.scrollHeight
  }
}

// Auto refresh
const doAutoRefresh = async () => {
  if (!selectedInstance.value || !selectedFile.value || logLines.value.length === 0) return
  if (isLoadingMore.value || loadingContent.value) return

  isRefreshing.value = true
  try {
    const response = await readLogContent(
      selectedInstance.value,
      selectedFile.value,
      loadedEndPointer.value,
      CHUNK_SIZE
    )

    if (response.nextPointer > loadedEndPointer.value) {
      const lines = response.content.split('\n')
      if (lines.length > 0 && lines[lines.length - 1] === '') {
        lines.pop()
      }

      const container = scrollContainerRef.value
      const wasAtBottom = container
        ? container.scrollHeight - container.scrollTop - container.clientHeight < 50
        : false

      logLines.value = [...logLines.value, ...lines]
      loadedEndPointer.value = response.nextPointer
      totalSize.value = response.totalSize

      // Auto scroll to bottom if was at bottom
      if (wasAtBottom) {
        await nextTick()
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('Auto refresh failed', error)
  } finally {
    isRefreshing.value = false
  }
}

const startAutoRefresh = () => {
  stopAutoRefresh()
  if (autoRefresh.value && logLines.value.length > 0) {
    refreshTimer = setInterval(doAutoRefresh, refreshInterval.value)
  }
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  isRefreshing.value = false
}

// Event handlers
const handleInstanceChange = (value: string) => {
  selectedInstance.value = value
  selectedFile.value = null
  logFiles.value = []
  clearContent()
  stopAutoRefresh()

  if (value) {
    loadLogFiles()
  }
}

const handleFileChange = () => {
  clearContent()
  stopAutoRefresh()
}

const handleDownload = async () => {
  if (!selectedInstance.value || !selectedFile.value) return

  downloading.value = true
  try {
    await downloadLogFile(selectedInstance.value, selectedFile.value)
    message.success('下载已开始')
  } catch (error) {
    message.error('下载失败')
    console.error('Download failed', error)
  } finally {
    downloading.value = false
  }
}

const copyContent = async () => {
  try {
    await navigator.clipboard.writeText(logLines.value.join('\n'))
    message.success('已复制到剪贴板')
  } catch (error) {
    message.error('复制失败')
  }
}

const clearContent = () => {
  logLines.value = []
  loadedStartPointer.value = 0
  loadedEndPointer.value = 0
  totalSize.value = 0
  stopAutoRefresh()
}

// Watchers
watch(autoRefresh, (value) => {
  if (value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
})

watch(refreshInterval, () => {
  if (autoRefresh.value) {
    startAutoRefresh()
  }
})

watch(logLines, () => {
  if (autoRefresh.value && !refreshTimer) {
    startAutoRefresh()
  }
})

// Lifecycle
onMounted(() => {
  loadInstances()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.log-content-container {
  border: 1px solid var(--n-border-color);
  border-radius: 8px;
  overflow: hidden;
}

.log-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  background: var(--n-color-modal);
  border-bottom: 1px solid var(--n-border-color);
}

.log-scroll-container {
  background: #1a1a2e;
  overflow: auto;
}

.load-more-hint {
  text-align: center;
  padding: 8px;
  color: #666;
  font-size: 12px;
}

.log-lines {
  padding: 8px 0;
}

.log-line {
  display: flex;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #e0e0e0;
  padding: 2px 16px;
}

.log-lines.wrap-lines .log-line {
  flex-wrap: wrap;
}

.log-lines.wrap-lines .line-content {
  white-space: pre-wrap;
  word-break: break-all;
}

.line-number {
  flex-shrink: 0;
  width: 50px;
  min-width: 50px;
  color: #666;
  text-align: right;
  padding-right: 12px;
  user-select: none;
}

.line-content {
  flex: 1;
  white-space: pre;
  overflow-x: auto;
}

/* Log level colors */
.line-error {
  background: rgba(255, 107, 107, 0.1);
}
.line-error .line-content {
  color: #ff6b6b;
}

.line-warn {
  background: rgba(254, 202, 87, 0.1);
}
.line-warn .line-content {
  color: #feca57;
}

.line-info .line-content {
  color: #54a0ff;
}

.line-debug .line-content {
  color: #a29bfe;
}

/* Scrollbar styling */
.log-scroll-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.log-scroll-container::-webkit-scrollbar-track {
  background: #1a1a2e;
}

.log-scroll-container::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 4px;
}

.log-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.log-scroll-container::-webkit-scrollbar-corner {
  background: #1a1a2e;
}
</style>
