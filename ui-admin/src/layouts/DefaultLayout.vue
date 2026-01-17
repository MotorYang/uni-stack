<template>
  <div class="h-screen w-screen bg-transparent overflow-hidden flex relative">
    <!-- Sidebar -->
    <aside class="floating-sidebar flex flex-col glass-sidebar z-50 transition-all duration-300 ease-in-out" :style="{ width: collapsed ? '80px' : '230px' }">
      <div class="h-16 flex items-center justify-center border-b border-white/20 mb-2">
        <h1 class="text-xl font-bold text-primary flex items-center gap-2">
          <n-icon size="24">
            <Layers />
          </n-icon>
          <span class="text-text-main transition-colors" v-show="!collapsed">UniStack</span>
        </h1>
      </div>
      
      <div class="flex-1 overflow-y-auto px-2 py-2 menu-scroll">
        <n-menu
          :options="menuOptions"
          :value="activeKey"
          :collapsed="collapsed"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          v-model:expanded-keys="expandedKeys"
          @update:value="handleUpdateValue"
          :indent="18"
          class="glass-menu"
        />
      </div>
      
      <div class="p-4 border-t border-white/20">
        <n-dropdown
          trigger="click"
          :options="userMenuOptions"
          @select="handleUserMenuSelect"
        >
          <div class="glass-card-sm flex items-center gap-3 p-3 cursor-pointer hover:bg-white/40 dark:hover:bg-white/10 transition-colors duration-300" :class="{ 'justify-center !p-2': collapsed }">
            <n-avatar round size="small" :src="userStore.userInfo?.avatar || 'https://ui-avatars.com/api/?name=User&background=random'" />
            <div class="flex flex-col" v-show="!collapsed">
              <span class="text-sm font-semibold text-text-main">{{ userStore.userInfo?.nickName || 'User' }}</span>
              <span class="text-xs text-text-sec">{{ userStore.userInfo?.email || 'email@example.com' }}</span>
            </div>
          </div>
        </n-dropdown>
      </div>
    </aside>

    <!-- Main Content Wrapper -->
    <div class="flex-1 flex flex-col h-full relative z-10 transition-all duration-300">
      <!-- Header -->
      <header class="floating-header glass-header flex items-center px-6 justify-between transition-all duration-300 ease-in-out">
        <div class="flex items-center gap-4">
          <n-button quaternary circle @click="collapsed = !collapsed">
            <template #icon>
              <n-icon size="22">
                <MenuOutline />
              </n-icon>
            </template>
          </n-button>
          <div class="w-96">
            <n-input round placeholder="搜索..." class="glass-input transition-all duration-300 focus:shadow-md">
              <template #prefix>
                <n-icon :component="Search" />
              </template>
            </n-input>
          </div>
        </div>
        
        <div class="flex items-center gap-4">
          <!-- Theme Toggle -->
          <n-button circle quaternary class="hover:bg-white/50 dark:hover:bg-white/10 transition-colors" @click="themeStore.toggleTheme">
            <template #icon>
              <n-icon size="20" :component="themeStore.isDark ? MoonOutline : SunnyOutline" />
            </template>
          </n-button>

          <n-badge dot type="error">
            <n-button circle quaternary class="hover:bg-white/50 dark:hover:bg-white/10 transition-colors">
              <template #icon>
                <n-icon size="20" :component="NotificationsOutline" />
              </template>
            </n-button>
          </n-badge>
          <n-button circle quaternary class="hover:bg-white/50 dark:hover:bg-white/10 transition-colors">
             <template #icon>
                <n-icon size="20" :component="SettingsOutline" />
             </template>
          </n-button>
          <n-dropdown
            trigger="click"
            :options="userMenuOptions"
            @select="handleUserMenuSelect"
          >
            <n-avatar
              round
              size="medium"
              :src="userStore.userInfo?.avatar || 'https://ui-avatars.com/api/?name=User&background=random'"
              class="hover:scale-105 transition-transform duration-300 cursor-pointer"
            />
          </n-dropdown>
        </div>
      </header>

      <!-- Content Area -->
      <main class="flex-1 overflow-y-auto p-4 pt-0 pr-4">
         <!-- Add a wrapper for consistency -->
         <div class="min-h-full">
            <router-view :key="$route.fullPath" />
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
import { getMenuList, type Menu } from '@/api/menu'
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

function transformMenu(menus: Menu[]): MenuOption[] {
  return menus.map(item => {
    const option: MenuOption = {
      label: item.label,
      key: item.key,
      path: item.path // Add path to option
    }
    if (item.icon) {
      const iconComp = getIconByName(item.icon)
      if (iconComp) {
        option.icon = renderIcon(iconComp)
      }
    }
    if (item.children) {
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

  try {
    const res = await getMenuList()
    menuOptions.value = transformMenu(res)
    
    // Default expand the first menu item if it has children
    if (menuOptions.value.length > 0) {
      const firstItem = menuOptions.value[0]
      if (firstItem && firstItem.children && firstItem.children.length > 0) {
        expandedKeys.value = [firstItem.key as string]
      }
    }
  } catch (error) {
    console.error('Failed to load menu', error)
  }
})

const router = useRouter()
const route = useRoute()
const activeKey = ref<string>((route.name as string) || 'Dashboard')

watch(() => route.name, (newName) => {
  if (newName) {
    activeKey.value = newName as string
  }
})

function handleUpdateValue(key: string, item: MenuOption) {
  activeKey.value = key
  if (item.path) {
    router.push(item.path as string)
  } else {
    router.push({ name: key })
  }
}

function handleUserMenuSelect(key: string) {
  if (key === 'profile') {
    router.push('/profile')
    return
  }
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
    message.success('已退出登录')
  }
}
</script>

<style scoped>
.floating-sidebar {
  height: calc(100vh - 32px);
  margin: 16px;
  border-radius: 20px;
  flex-shrink: 0;
}

.floating-header {
  height: 64px;
  margin: 16px 16px 16px 0;
  border-radius: 20px;
  flex-shrink: 0;
}
</style>
