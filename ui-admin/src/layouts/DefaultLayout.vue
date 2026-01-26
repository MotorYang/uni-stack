<template>
  <div 
    class="h-screen w-screen bg-transparent overflow-hidden flex relative font-sans transition-colors duration-500"
    :class="themeStore.isDark ? 'dark' : ''"
  >
    <div 
      class="fixed inset-0 z-0 transition-all duration-700"
      :class="themeStore.isDark ? 'bg-slate-950' : 'bg-slate-50'"
    >
      <div 
        class="absolute top-[-10%] left-[-10%] w-[45%] h-[45%] rounded-full blur-[120px] transition-all duration-1000"
        :class="themeStore.isDark ? 'bg-blue-600/10' : 'bg-blue-400/20'"
      ></div>
      <div 
        class="absolute bottom-[-10%] right-[-10%] w-[45%] h-[45%] rounded-full blur-[120px] transition-all duration-1000"
        :class="themeStore.isDark ? 'bg-purple-600/10' : 'bg-purple-400/20'"
      ></div>
    </div>

    <aside
      class="floating-sidebar flex flex-col glass-sidebar z-50 relative shrink-0"
      :style="{ width: collapsed ? '82px' : '260px' }"
    >
      <div class="h-20 flex items-center justify-center border-b border-white/10 mb-2 relative overflow-hidden shrink-0">
        <div class="absolute inset-0 bg-gradient-to-br from-blue-500/10 to-transparent"></div>
        <h1 class="text-xl font-bold flex items-center gap-3 relative z-10">
          <n-icon size="28" class="text-blue-500 drop-shadow-[0_0_8px_rgba(59,130,246,0.5)]">
            <Layers />
          </n-icon>
          <transition name="fade-slide">
            <span 
              v-show="!collapsed" 
              class="whitespace-nowrap bg-clip-text text-transparent bg-gradient-to-r transition-all duration-500"
              :class="themeStore.isDark ? 'from-white to-white/70' : 'from-blue-600 to-indigo-600'"
            >
              UniStack
            </span>
          </transition>
        </h1>
      </div>

      <div class="flex-1 overflow-y-auto px-3 py-2 menu-scroll">
        <n-menu
          :options="menuOptions"
          :value="activeKey"
          :collapsed="collapsed"
          :collapsed-width="56"
          :collapsed-icon-size="22"
          v-model:expanded-keys="expandedKeys"
          @update:value="handleUpdateValue"
          :indent="20"
          class="glass-menu"
        />
      </div>

      <div class="p-4 border-t border-white/10">
        <n-dropdown trigger="click" :options="userMenuOptions" @select="handleUserMenuSelect" placement="top-start">
          <div 
            class="glass-card-sm flex items-center gap-3 p-3 cursor-pointer group transition-all" 
            :class="{ 'justify-center !p-2': collapsed }"
          >
            <n-avatar
              round
              size="small"
              :src="userStore.userInfo?.avatar || 'https://ui-avatars.com/api/?name=User'"
              class="ring-2 ring-white/20 group-hover:ring-blue-400/50"
            />
            <transition name="fade-slide">
              <div class="flex flex-col min-w-0" v-show="!collapsed">
                <span 
                  class="text-sm font-semibold truncate transition-colors"
                  :class="themeStore.isDark ? 'text-white/90' : 'text-slate-800'"
                >
                  {{ userStore.userInfo?.nickname || '管理员' }}
                </span>
                <span 
                  class="text-xs truncate scale-90 origin-left transition-colors"
                  :class="themeStore.isDark ? 'text-white/40' : 'text-slate-500'"
                >
                  {{ userStore.userInfo?.email || 'admin@unstack.com' }}
                </span>
              </div>
            </transition>
          </div>
        </n-dropdown>
      </div>
    </aside>

    <div class="flex-1 flex flex-col h-full relative z-10 min-w-0">
      <header class="floating-header glass-header flex items-center px-6 justify-between shrink-0 transition-all duration-500">
        <div class="flex items-center gap-6">
          <n-button quaternary circle @click="collapsed = !collapsed" class="header-icon-btn">
            <template #icon><n-icon size="22"><MenuOutline /></n-icon></template>
          </n-button>
          
          <div class="hidden md:block w-72 lg:w-96">
            <n-input round placeholder="搜索功能 (Ctrl + K)" class="glass-input">
              <template #prefix><n-icon :component="Search" class="opacity-40" /></template>
              <template #suffix><kbd class="text-[10px] opacity-30 border border-current px-1.5 rounded">⌘ K</kbd></template>
            </n-input>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <n-dropdown
            v-if="deptOptions.length > 1"
            trigger="click"
            :options="deptOptions"
            :render-label="renderDeptLabel"
            @select="handleDeptSelect"
            placement="bottom-start"
          >
            <n-button quaternary class="header-icon-btn dept-switch-btn">
              <template #icon><n-icon size="18" :component="BusinessOutline" /></template>
              <div class="ml-1.5 hidden lg:flex flex-col items-start leading-tight">
                <span class="text-[10px] opacity-50">{{ currentDeptInfo.company }}</span>
                <span class="text-xs font-medium max-w-28 truncate">{{ currentDeptInfo.dept }}</span>
              </div>
            </n-button>
          </n-dropdown>

          <n-button circle quaternary class="header-icon-btn" @click="themeStore.toggleTheme">
            <template #icon><n-icon size="20" :component="themeStore.isDark ? MoonOutline : SunnyOutline" /></template>
          </n-button>

          <n-popover trigger="click" placement="bottom" :width="360" class="message-popover">
            <template #trigger>
              <n-badge :value="unreadCount" :max="99" :offset="[-4, 4]" :show="unreadCount > 0">
                <n-button circle quaternary class="header-icon-btn">
                  <template #icon><n-icon size="20" :component="NotificationsOutline" /></template>
                </n-button>
              </n-badge>
            </template>
            <div class="message-panel">
              <n-tabs v-model:value="messageTab" type="line" animated>
                <n-tab-pane name="unread" :tab="`未读 (${unreadCount})`">
                  <n-scrollbar style="max-height: 320px">
                    <div v-if="unreadMessages.length === 0" class="py-8">
                      <n-empty description="暂无未读消息" />
                    </div>
                    <div v-else class="message-list">
                      <div
                        v-for="msg in unreadMessages"
                        :key="msg.id"
                        class="message-item"
                        @click="handleReadMessage(msg)"
                      >
                        <div class="message-icon" :style="{ color: getMessageColor(msg.type) }">
                          <n-icon size="20" :component="getMessageIcon(msg.type)" />
                        </div>
                        <div class="message-content">
                          <div class="message-title">{{ msg.title }}</div>
                          <div class="message-desc">{{ msg.content }}</div>
                          <div class="message-time">{{ msg.time }}</div>
                        </div>
                      </div>
                    </div>
                  </n-scrollbar>
                  <div v-if="unreadMessages.length > 0" class="message-footer">
                    <n-button text type="primary" @click="handleReadAll">全部标为已读</n-button>
                  </div>
                </n-tab-pane>
                <n-tab-pane name="read" tab="已读">
                  <n-scrollbar style="max-height: 320px">
                    <div v-if="readMessages.length === 0" class="py-8">
                      <n-empty description="暂无已读消息" />
                    </div>
                    <div v-else class="message-list">
                      <div v-for="msg in readMessages" :key="msg.id" class="message-item read">
                        <div class="message-icon" :style="{ color: getMessageColor(msg.type) }">
                          <n-icon size="20" :component="getMessageIcon(msg.type)" />
                        </div>
                        <div class="message-content">
                          <div class="message-title">{{ msg.title }}</div>
                          <div class="message-desc">{{ msg.content }}</div>
                          <div class="message-time">{{ msg.time }}</div>
                        </div>
                      </div>
                    </div>
                  </n-scrollbar>
                </n-tab-pane>
                <n-tab-pane name="history" tab="历史">
                  <n-scrollbar style="max-height: 320px">
                    <div v-if="historyMessages.length === 0" class="py-8">
                      <n-empty description="暂无历史消息" />
                    </div>
                    <div v-else class="message-list">
                      <div v-for="msg in historyMessages" :key="msg.id" class="message-item" :class="{ read: msg.read }">
                        <div class="message-icon" :style="{ color: getMessageColor(msg.type) }">
                          <n-icon size="20" :component="getMessageIcon(msg.type)" />
                        </div>
                        <div class="message-content">
                          <div class="message-title">
                            {{ msg.title }}
                            <n-tag v-if="!msg.read" size="small" type="error" :bordered="false" class="ml-2">未读</n-tag>
                          </div>
                          <div class="message-desc">{{ msg.content }}</div>
                          <div class="message-time">{{ msg.time }}</div>
                        </div>
                        <n-button
                          quaternary
                          circle
                          size="small"
                          class="message-delete-btn"
                          @click.stop="handleDeleteMessage(msg)"
                        >
                          <template #icon><n-icon size="16" :component="TrashOutline" /></template>
                        </n-button>
                      </div>
                    </div>
                  </n-scrollbar>
                </n-tab-pane>
              </n-tabs>
            </div>
          </n-popover>

          <n-divider vertical class="mx-2 opacity-10" />

          <n-dropdown
            trigger="click"
            :options="userMenuOptions"
            @select="handleUserMenuSelect"
            placement="bottom-end"
          >
            <n-avatar
              round
              size="medium"
              :src="userStore.userInfo?.avatar || 'https://ui-avatars.com/api/?name=User'"
              class="cursor-pointer hover:scale-110 transition-transform ring-2 ring-white/10"
            />
          </n-dropdown>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-4 pt-0 custom-scrollbar">
        <div class="glass-content-wrapper min-h-full transition-all duration-500">
          <router-view v-slot="{ Component, route }">
            <div class="min-h-full relative">
              <transition name="page-stagger" mode="out-in">
                <component
                  v-if="isMenuLoaded || route.name !== 'NotFound'"
                  :is="Component"
                  :key="route.fullPath"
                />
              </transition>
              <div
                v-if="!isMenuLoaded && route.name === 'NotFound'"
                class="absolute inset-0 p-6 flex flex-col gap-4 animate-pulse pointer-events-none"
              >
                <div class="h-8 w-64 rounded-xl bg-slate-200/70 dark:bg-slate-700/70"></div>
                <div class="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-4">
                  <div class="rounded-2xl bg-slate-200/70 dark:bg-slate-800/70"></div>
                  <div class="rounded-2xl bg-slate-200/70 dark:bg-slate-800/70"></div>
                  <div class="rounded-2xl bg-slate-200/70 dark:bg-slate-800/70"></div>
                </div>
              </div>
            </div>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, type Component, watch, watchEffect, onMounted, computed } from 'vue'
import { useRouter, useRoute, type RouteRecordRaw } from 'vue-router'
import { NIcon, type MenuOption, type DropdownOption, NMenu, NAvatar, NInput, NButton, NBadge, NDropdown, NPopover, NTabs, NTabPane, NEmpty, NTag, NScrollbar, useMessage, NDivider } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { getUserMenuTree, type MenuVO } from '@/api/menu'
import { getUserIdFromToken } from '@/utils/jwt'
import * as Ionicons from '@vicons/ionicons5'
import {
  Layers,
  Search,
  NotificationsOutline,
  SunnyOutline,
  MoonOutline,
  MenuOutline,
  BusinessOutline,
  CheckmarkCircleOutline,
  AlertCircleOutline,
  InformationCircleOutline,
  WarningOutline,
  TrashOutline
} from '@vicons/ionicons5'

const themeStore = useThemeStore()
const userStore = useUserStore()
const collapsed = ref(false)
const message = useMessage()
const isMenuLoaded = ref(false)

// 消息功能
interface Message {
  id: string
  title: string
  content: string
  type: 'info' | 'success' | 'warning' | 'error'
  time: string
  read: boolean
}

const messageTab = ref('unread')

const mockMessages: Message[] = [
  { id: '1', title: '系统更新通知', content: '系统将于今晚 22:00 进行维护升级，预计持续 2 小时', type: 'warning', time: '5 分钟前', read: false },
  { id: '2', title: '新用户注册', content: '用户 张三 已完成注册，请及时审核', type: 'info', time: '15 分钟前', read: false },
  { id: '3', title: '订单支付成功', content: '订单 #20240126001 支付成功，金额 ¥1,280.00', type: 'success', time: '30 分钟前', read: false },
  { id: '4', title: '库存预警', content: '商品「无线蓝牙耳机」库存不足 10 件，请及时补货', type: 'error', time: '1 小时前', read: true },
  { id: '5', title: '审批通过', content: '您提交的请假申请已通过审批', type: 'success', time: '2 小时前', read: true },
  { id: '6', title: '新评论', content: '您的文章收到了一条新评论', type: 'info', time: '3 小时前', read: true },
  { id: '7', title: '安全提醒', content: '检测到您的账号在新设备上登录', type: 'warning', time: '昨天 18:30', read: true },
  { id: '8', title: '任务完成', content: '定时任务「数据备份」执行成功', type: 'success', time: '昨天 12:00', read: true },
]

const messages = ref<Message[]>(mockMessages)

const unreadMessages = computed(() => messages.value.filter(m => !m.read))
const readMessages = computed(() => messages.value.filter(m => m.read).slice(0, 5))
const historyMessages = computed(() => messages.value)
const unreadCount = computed(() => unreadMessages.value.length)

function getMessageIcon(type: Message['type']) {
  const icons = {
    info: InformationCircleOutline,
    success: CheckmarkCircleOutline,
    warning: WarningOutline,
    error: AlertCircleOutline
  }
  return icons[type]
}

function getMessageColor(type: Message['type']) {
  const colors = {
    info: '#2080f0',
    success: '#18a058',
    warning: '#f0a020',
    error: '#d03050'
  }
  return colors[type]
}

function handleReadMessage(msg: Message) {
  msg.read = true
  message.success('已标记为已读')
}

function handleReadAll() {
  messages.value.forEach(m => m.read = true)
  message.success('已全部标记为已读')
}

function handleDeleteMessage(msg: Message) {
  const index = messages.value.findIndex(m => m.id === msg.id)
  if (index > -1) {
    messages.value.splice(index, 1)
    message.success('已删除')
  }
}

// 同步主题到 document，让全局下拉菜单也能感知主题
watchEffect(() => {
  if (themeStore.isDark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})

const deptOptions = computed<DropdownOption[]>(() => {
  const depts = userStore.userInfo?.depts || []
  return depts.map(dept => {
    const position = dept.position || '成员'
    const isCompany = dept.deptType === 'C'
    const companyLabel = isCompany ? '公司' : (dept.companyName || '未知公司')

    return {
      key: dept.deptId,
      label: `${dept.deptName} - ${position}`,
      companyLabel,
      deptLabel: `${dept.deptName} - ${position}`
    } as DropdownOption & { companyLabel: string; deptLabel: string }
  })
})

function renderDeptLabel(option: DropdownOption) {
  const opt = option as DropdownOption & { companyLabel: string; deptLabel: string }
  return h('div', { class: 'dept-option' }, [
    h('div', { class: 'dept-option-company' }, opt.companyLabel || ''),
    h('div', { class: 'dept-option-name' }, opt.deptLabel || '')
  ])
}

const currentDeptId = computed(() => userStore.userInfo?.deptId || '')

const currentDeptInfo = computed(() => {
  const depts = userStore.userInfo?.depts || []
  const currentDept = depts.find(d => d.deptId === currentDeptId.value)
  if (!currentDept) {
    return {
      company: '',
      dept: userStore.userInfo?.deptName || ''
    }
  }

  if (currentDept.deptType === 'C') {
    return {
      company: '公司',
      dept: currentDept.deptName
    }
  }
  return {
    company: currentDept.companyName || '',
    dept: currentDept.deptName
  }
})

function handleDeptSelect(key: string) {
  const depts = userStore.userInfo?.depts || []
  const selected = depts.find(d => d.deptId === key)
  if (selected) {
    userStore.setCurrentDept(key, selected.deptName)
  }
}


function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

function getIconByName(name: string): Component | null {
  const icon = (Ionicons as Record<string, Component | undefined>)[name]
  return icon || null
}

const menuOptions = ref<MenuOption[]>([])
const expandedKeys = ref<string[]>([])
const userMenuOptions: DropdownOption[] = [
  {
    label: '个人信息',
    key: 'profile'
  },
  {
    label: '退出登录',
    key: 'logout'
  }
]

const viewModules = import.meta.glob('../views/**/*.vue')
let dynamicRoutesAdded = false

/**
 * 动态视图组件加载器
 */
function resolveViewComponent(component: string | null) {
  const fallback = viewModules['../views/MissingComponent.vue']
  if (!component) {
    return fallback as () => Promise<Component>
  }
  const normalized = component.replace(/^\/+|\/+$/g, '')
  const key = `../views/${normalized}/index.vue`
  const loader = viewModules[key]
  
  if (!loader) {
    console.warn(`[ViewResolver] 未找到组件文件: ${key}`)
    return fallback as () => Promise<Component>
  }

  return loader as () => Promise<Component>
}

function transformMenuToRoutes(menus: MenuVO[], existingPaths: Set<string>): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []

  const traverse = (items: MenuVO[]) => {
    items.forEach(item => {
      if (
        item.menuType === 'C' &&
        item.visible === 0 &&
        item.status === 0 &&
        item.path &&
        !existingPaths.has(item.path)
      ) {
        routes.push({
          path: item.path,
          name: item.path,
          component: resolveViewComponent(item.component),
          meta: {
            title: item.menuName,
            icon: item.icon,
          }
        })
      }
      if (item.children && item.children.length > 0) {
        traverse(item.children)
      }
    })
  }

  traverse(menus)
  return routes
}

function findFirstMenuPath(menus: MenuVO[]): string | null {
  for (const item of menus) {
    if (
      item.menuType === 'C' &&
      item.visible === 0 &&
      item.status === 0 &&
      item.path
    ) {
      return item.path
    }
    if (item.children && item.children.length > 0) {
      const childPath = findFirstMenuPath(item.children)
      if (childPath) {
        return childPath
      }
    }
  }
  return null
}

function transformMenu(menus: MenuVO[]): MenuOption[] {
  return menus
    .filter(item => item.visible === 0 && item.status === 0)
    .map(item => {
      const option: MenuOption = {
        label: item.menuName,
        key: item.path || item.id,
        path: item.path
      }
      if (item.icon) {
        const iconComp = getIconByName(item.icon)
        if (iconComp) {
          option.icon = renderIcon(iconComp)
        }
      }
      if (item.children && item.children.length > 0) {
        option.children = transformMenu(item.children)
      }
      return option
    })
}

onMounted(async () => {
  // Fetch user info if not available
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('Failed to load user info', error)
    }
  }

  // Load user menu
  const token = userStore.token
  if (!token) {
    console.error('No token available')
    return
  }
  const userId = getUserIdFromToken(token)
  if (!userId) {
    console.error('Failed to get userId from token')
    return
  }

  try {
    const res = await getUserMenuTree(userId)
    menuOptions.value = transformMenu(res)

    if (!dynamicRoutesAdded) {
      const existingPaths = new Set(router.getRoutes().map(r => r.path))
      const dynamicRoutes = transformMenuToRoutes(res, existingPaths)
      dynamicRoutes.forEach(routeRecord => {
        router.addRoute('Root', routeRecord)
      })
      dynamicRoutesAdded = true

      if (route.path === '/' || route.path === '') {
        const firstPath = findFirstMenuPath(res)
        if (firstPath) {
          router.replace(firstPath)
        }
      }

      if (route.name === 'NotFound' && route.path && route.path !== '/' && route.path !== '') {
        const targetExists = router
          .getRoutes()
          .some(r => r.path === route.path && r.name !== 'NotFound')

        if (targetExists) {
          router.replace(route.fullPath)
        }
      }
    }

    const currentPath = route.path
    const keysToExpand: string[] = []

    function findParentKeys(options: MenuOption[], parentKey?: string) {
      for (const item of options) {
        if (item.key === currentPath) {
          if (parentKey) {
            keysToExpand.push(parentKey)
          }
          return true
        }
        if (item.children && item.children.length > 0) {
          const found = findParentKeys(item.children, item.key as string)
          if (found && parentKey) {
            keysToExpand.push(parentKey)
            return true
          }
          if (found) return true
        }
      }
      return false
    }

    findParentKeys(menuOptions.value)
    if (keysToExpand.length > 0) {
      expandedKeys.value = keysToExpand
    }
  } catch (error) {
    console.error('Failed to load menu', error)
  } finally {
    isMenuLoaded.value = true
  }
})

const router = useRouter()
const route = useRoute()
const activeKey = ref<string>(route.path || '/dashboard')

// 监听路由变化，更新菜单选中状态
watch(() => route.path, (newPath) => {
  if (newPath) {
    activeKey.value = newPath
  }
}, { immediate: true })

function handleUpdateValue(key: string, item: MenuOption) {
  activeKey.value = key
  if (item.path) {
    router.push(item.path as string)
  } else {
    router.push({ name: key })
  }
}

async function handleUserMenuSelect(key: string) {
  if (key === 'profile') {
    await router.push('/profile')
    return
  }
  if (key === 'logout') {
    await userStore.logout()
    message.success('已退出登录')
    await router.push('/login')
  }
}
</script>

<style scoped>
/* 侧边栏浮动 */
.floating-sidebar {
  overflow: hidden;
  height: calc(100vh - 32px);
  margin: 16px;
  border-radius: 24px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1), background 0.5s ease;
}

/* 头部浮动 */
.floating-header {
  height: 64px;
  margin: 16px 16px 16px 0;
  border-radius: 20px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
}

/* 内容区容器 */
.glass-content-wrapper {
  background: var(--glass-bg-light);
  backdrop-filter: blur(10px);
  border: 1px solid var(--glass-border-subtle);
  border-radius: 24px;
  box-shadow: var(--glass-inset);
}

/* 侧边栏折叠后，菜单图标居中 */
.glass-menu.n-menu--collapsed :deep(.n-menu-item-content) {
  padding-left: 0 !important;
  display: flex;
  justify-content: center;
  flex-direction: row-reverse;
}
.glass-menu.n-menu--collapsed :deep(.n-menu-item-content__icon) {
  margin-right: 0 !important;
}

/* 页面切换动画 */
.page-stagger-enter-active { transition: all 0.4s cubic-bezier(0.25, 1, 0.5, 1); }
.page-stagger-leave-active { transition: all 0.2s cubic-bezier(0.4, 0, 1, 1); }
.page-stagger-enter-from { opacity: 0; transform: translateY(10px); filter: blur(4px); }
.page-stagger-leave-to { opacity: 0; transform: translateY(-10px); filter: blur(4px); }

/* 隐藏滚动条 */
.menu-scroll::-webkit-scrollbar,
.custom-scrollbar::-webkit-scrollbar {
  display: none;
}

/* 部门切换按钮 */
.dept-switch-btn {
  padding: 4px 12px !important;
  height: auto !important;
  min-height: 36px !important;
  border-radius: 12px !important;
}
</style>

<style>
/* 部门下拉选项 */
.dept-option {
  padding: 2px 0;
}

.dept-option-company {
  font-size: 12px;
  opacity: 0.5;
  line-height: 1.3;
}

.dept-option-name {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}

/* 深色主题 */
html.dark .dept-option {
  color: #e2e8f0;
}

/* 消息面板 */
.message-panel {
  margin: -12px -16px;
}

.message-panel .n-tabs-nav {
  padding: 0 16px;
}

.message-list {
  padding: 8px 0;
}

.message-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.message-item:hover {
  background: var(--glass-bg-light);
}

.message-item.read {
  opacity: 0.6;
}

.message-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--glass-bg-light);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-main);
  display: flex;
  align-items: center;
}

.message-desc {
  font-size: 13px;
  color: var(--text-sec);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  font-size: 12px;
  color: var(--text-sec);
  opacity: 0.7;
  margin-top: 4px;
}

.message-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--glass-border);
  text-align: center;
}

.message-delete-btn {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-item:hover .message-delete-btn {
  opacity: 0.6;
}

.message-delete-btn:hover {
  opacity: 1 !important;
  color: #d03050 !important;
}
</style>
