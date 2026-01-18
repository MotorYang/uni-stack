<template>
  <div class="h-screen w-screen bg-transparent overflow-hidden flex relative">
    <!-- Sidebar -->
    <aside
      class="floating-sidebar flex flex-col glass-sidebar z-50"
      :style="{ width: collapsed ? '80px' : '240px' }"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-white/20 mb-2 relative overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-r from-blue-500/5 to-purple-500/5"></div>
        <h1 class="text-xl font-bold text-primary flex items-center gap-2 relative z-10">
          <n-icon size="26" class="text-blue-500">
            <Layers />
          </n-icon>
          <transition name="fade-slide">
            <span v-show="!collapsed" class="whitespace-nowrap">UniStack</span>
          </transition>
        </h1>
      </div>

      <!-- Menu -->
      <div class="flex-1 overflow-y-auto px-3 py-2 menu-scroll">
        <n-menu
          :options="menuOptions"
          :value="activeKey"
          :collapsed="collapsed"
          :collapsed-width="56"
          :collapsed-icon-size="22"
          v-model:expanded-keys="expandedKeys"
          @update:value="handleUpdateValue"
          :indent="18"
          class="glass-menu"
        />
      </div>

      <!-- User Card -->
      <div class="p-3 border-t border-white/20">
        <n-dropdown
          trigger="click"
          :options="userMenuOptions"
          @select="handleUserMenuSelect"
          placement="top-start"
        >
          <div
            class="glass-card-sm flex items-center gap-3 p-3 cursor-pointer"
            :class="{ 'justify-center !p-2': collapsed }"
          >
            <n-avatar
              round
              size="small"
              :src="userStore.userInfo?.avatar || 'https://ui-avatars.com/api/?name=User&background=random'"
              class="ring-2 ring-white/20 ring-offset-1 ring-offset-transparent"
            />
            <transition name="fade-slide">
              <div class="flex flex-col min-w-0" v-show="!collapsed">
                <span class="user-name text-sm font-semibold truncate">
                  {{ userStore.userInfo?.nickname || 'User' }}
                </span>
                <span class="user-email text-xs truncate">
                  {{ userStore.userInfo?.email || 'email@example.com' }}
                </span>
              </div>
            </transition>
          </div>
        </n-dropdown>
      </div>
    </aside>

    <!-- Main Content Wrapper -->
    <div class="flex-1 flex flex-col h-full relative z-10">
      <!-- Header -->
      <header class="floating-header glass-header flex items-center px-6 justify-between">
        <div class="flex items-center gap-4">
          <!-- Toggle Button -->
          <n-button quaternary circle @click="collapsed = !collapsed" class="header-btn">
            <template #icon>
              <n-icon size="22">
                <MenuOutline />
              </n-icon>
            </template>
          </n-button>

          <!-- Search -->
          <div class="w-80 lg:w-96">
            <n-input round placeholder="搜索菜单、功能..." class="glass-input">
              <template #prefix>
                <n-icon :component="Search" class="text-gray-400" />
              </template>
            </n-input>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <!-- Theme Toggle -->
          <n-button circle quaternary class="header-btn" @click="themeStore.toggleTheme">
            <template #icon>
              <n-icon size="20" :component="themeStore.isDark ? MoonOutline : SunnyOutline" />
            </template>
          </n-button>

          <!-- Notifications -->
          <n-badge dot type="error" :offset="[-4, 4]">
            <n-button circle quaternary class="header-btn">
              <template #icon>
                <n-icon size="20" :component="NotificationsOutline" />
              </template>
            </n-button>
          </n-badge>

          <!-- Settings -->
          <n-button circle quaternary class="header-btn">
            <template #icon>
              <n-icon size="20" :component="SettingsOutline" />
            </template>
          </n-button>

          <!-- User Avatar -->
          <n-dropdown
            trigger="click"
            :options="userMenuOptions"
            @select="handleUserMenuSelect"
            placement="bottom-end"
          >
            <n-avatar
              round
              size="medium"
              :src="userStore.userInfo?.avatar || 'https://ui-avatars.com/api/?name=User&background=random'"
              class="cursor-pointer ml-2 ring-2 ring-white/30 hover:ring-blue-400/50"
            />
          </n-dropdown>
        </div>
      </header>

      <!-- Content Area -->
      <main class="flex-1 overflow-y-auto p-4 pt-0 pr-4">
        <div class="min-h-full">
          <router-view v-slot="{ Component, route }">
            <transition name="page-fade" mode="out-in">
              <component :is="Component" :key="route.fullPath" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, h, type Component, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon, type MenuOption, type DropdownOption, NMenu, NAvatar, NInput, NButton, NBadge, NDropdown, useMessage } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { getUserMenuTree, type MenuVO } from '@/api/menu'
import { getUserIdFromToken } from '@/utils/jwt'
import * as Ionicons from '@vicons/ionicons5'
import { 
  Layers, 
  Search, 
  NotificationsOutline, 
  SettingsOutline,
  SunnyOutline,
  MoonOutline,
  MenuOutline
} from '@vicons/ionicons5'

const themeStore = useThemeStore()
const userStore = useUserStore()
const collapsed = ref(false)
const message = useMessage()


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

function transformMenu(menus: MenuVO[]): MenuOption[] {
  return menus
    .filter(item => item.menuType !== 'B' && item.visible === 0 && item.status === 0)
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

    // 展开包含当前路由的父菜单
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
    router.push('/profile')
    return
  }
  if (key === 'logout') {
    await userStore.logout()
    router.push('/login')
    message.success('已退出登录')
  }
}
</script>

<style scoped>
/* ============================================
   浮动侧边栏 - 毛玻璃效果
   ============================================ */
.floating-sidebar {
  height: calc(100vh - 32px);
  margin: 16px;
  border-radius: 24px;
  flex-shrink: 0;
}

.glass-sidebar {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  border: 1px solid var(--glass-border);
  box-shadow:
    var(--glass-shadow),
    var(--glass-inset),
    0 0 40px rgba(59, 130, 246, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-sidebar:hover {
  box-shadow:
    var(--glass-shadow-hover),
    var(--glass-inset),
    0 0 60px rgba(59, 130, 246, 0.08);
}

/* ============================================
   浮动头部 - 毛玻璃效果
   ============================================ */
.floating-header {
  height: 64px;
  margin: 16px 16px 16px 0;
  border-radius: 20px;
  flex-shrink: 0;
}

.glass-header {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  border: 1px solid var(--glass-border);
  box-shadow:
    var(--glass-shadow),
    var(--glass-inset);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ============================================
   毛玻璃菜单样式
   ============================================ */
.glass-menu :deep(.n-menu) {
  background: transparent !important;
}

.glass-menu :deep(.n-menu-item) {
  margin: 4px 0;
  border-radius: 12px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-menu :deep(.n-menu-item:hover) {
  background: var(--glass-bg-light) !important;
  transform: translateX(4px);
}

.glass-menu :deep(.n-menu-item-content--selected),
.glass-menu :deep(.n-menu-item-content--child-active) {
  background: linear-gradient(135deg,
    rgba(59, 130, 246, 0.2) 0%,
    rgba(147, 197, 253, 0.15) 100%) !important;
  border-left: 3px solid #3b82f6;
}

.dark .glass-menu :deep(.n-menu-item-content--selected),
.dark .glass-menu :deep(.n-menu-item-content--child-active) {
  background: linear-gradient(135deg,
    rgba(96, 165, 250, 0.25) 0%,
    rgba(59, 130, 246, 0.15) 100%) !important;
}

.glass-menu :deep(.n-submenu-children) {
  background: transparent !important;
  border-radius: 8px;
  margin: 4px 8px;
  padding: 4px;
}

/* ============================================
   毛玻璃输入框样式
   ============================================ */
.glass-input :deep(.n-input) {
  background: var(--glass-bg-light) !important;
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  border: 1px solid var(--glass-border-subtle) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-input :deep(.n-input:hover) {
  background: var(--glass-bg) !important;
  border-color: var(--glass-border) !important;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.glass-input :deep(.n-input:focus-within) {
  background: var(--glass-bg) !important;
  border-color: #3b82f6 !important;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15), var(--glass-inset);
}

/* ============================================
   毛玻璃小卡片样式
   ============================================ */
.glass-card-sm {
  background: var(--glass-bg-light);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--glass-border-subtle);
  border-radius: 14px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-card-sm:hover {
  background: var(--glass-bg);
  border-color: var(--glass-border);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.dark .glass-card-sm:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

/* 用户卡片文字颜色 - 使用 CSS 变量自动适应主题 */
.glass-card-sm .user-name {
  color: var(--text-main);
}

.glass-card-sm .user-email {
  color: var(--text-secondary);
}

/* ============================================
   隐藏滚动条但保留滚动功能
   ============================================ */
.menu-scroll {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.menu-scroll::-webkit-scrollbar {
  display: none; /* Chrome/Safari/Opera */
}

/* ============================================
   主内容区域样式
   ============================================ */
main {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

main::-webkit-scrollbar {
  display: none; /* Chrome/Safari/Opera */
}

/* ============================================
   按钮悬停效果增强
   ============================================ */
:deep(.n-button.n-button--quaternary-type) {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.n-button.n-button--quaternary-type:hover) {
  background: var(--glass-bg-light) !important;
  transform: scale(1.05);
}

:deep(.n-button.n-button--quaternary-type:active) {
  transform: scale(0.98);
}

/* ============================================
   头像悬停效果
   ============================================ */
:deep(.n-avatar) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.n-avatar:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* ============================================
   徽章动画
   ============================================ */
:deep(.n-badge-sup) {
  animation: badge-pulse 2s infinite;
}

@keyframes badge-pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

/* ============================================
   Logo 文字渐变效果
   ============================================ */
.text-primary {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ============================================
   边框颜色自定义
   ============================================ */
.border-white\/20 {
  border-color: var(--glass-border) !important;
}

/* ============================================
   头部按钮统一样式
   ============================================ */
.header-btn {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.header-btn:hover {
  background: var(--glass-bg-light) !important;
  transform: scale(1.08);
}

.header-btn:active {
  transform: scale(0.95);
}

/* ============================================
   头像环形效果
   ============================================ */
.ring-white\/20 {
  --tw-ring-color: var(--glass-border);
}

.ring-white\/30 {
  --tw-ring-color: var(--glass-border);
}

.hover\:ring-blue-400\/50:hover {
  --tw-ring-color: rgba(96, 165, 250, 0.5);
}

/* ============================================
   过渡动画定义
   ============================================ */

/* 淡入滑动动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

/* 页面切换动画 */
.page-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
