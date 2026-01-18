<script setup lang="ts">
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, darkTheme, zhCN, type GlobalThemeOverrides } from 'naive-ui'
import { RouterView } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { watch, computed } from 'vue'
import { getGlassThemeOverrides } from './config/theme'

const themeStore = useThemeStore()

watch(
  () => themeStore.isDark,
  (isDark) => {
    if (isDark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  },
  { immediate: true }
)

// 毛玻璃主题覆盖配置
const glassThemeOverrides = computed<GlobalThemeOverrides>(() => getGlassThemeOverrides(themeStore.isDark));
</script>

<template>
  <n-config-provider
    :theme="themeStore.isDark ? darkTheme : null"
    :theme-overrides="glassThemeOverrides"
    :locale="zhCN"
  >
    <n-message-provider>
      <n-notification-provider>
        <n-dialog-provider>
           <RouterView />
        </n-dialog-provider>
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<style>
/* Global styles are imported in main.ts via style.css */
</style>
