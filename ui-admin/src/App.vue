<script setup lang="ts">
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import { RouterView } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { computed, watch } from 'vue'

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

const themeOverrides = computed<GlobalThemeOverrides>(() => ({
  common: {
    primaryColor: '#4F46E5',
    primaryColorHover: '#6366F1',
    primaryColorPressed: '#4338CA',
    primaryColorSuppl: '#4F46E5',
    borderRadius: '12px',
    borderRadiusSmall: '999px',
    fontFamily:
      'system-ui, -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI", sans-serif'
  },
  Button: {
    borderRadius: '999px',
  },
  Card: {
    borderRadius: '20px'
  },
  Input: {
    borderRadius: '999px'
  },
  Tag: {
    borderRadius: '999px'
  }
}))
</script>

<template>
  <n-config-provider
    :theme="themeStore.isDark ? darkTheme : null"
    :theme-overrides="themeOverrides"
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
