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

          <n-badge dot type="error" :offset="[-4, 4]" processing>
            <n-button circle quaternary class="header-icon-btn">
              <template #icon><n-icon size="20" :component="NotificationsOutline" /></template>
            </n-button>
          </n-badge>

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
import { NIcon, type MenuOption, type DropdownOption, NMenu, NAvatar, NInput, NButton, NBadge, NDropdown, useMessage, NDivider } from 'naive-ui'
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
  BusinessOutline
} from '@vicons/ionicons5'

const themeStore = useThemeStore()
const userStore = useUserStore()
const collapsed = ref(false)
const message = useMessage()
const isMenuLoaded = ref(false)

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
</style>
