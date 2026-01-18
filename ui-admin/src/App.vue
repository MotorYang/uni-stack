<script setup lang="ts">
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, darkTheme, zhCN, type GlobalThemeOverrides } from 'naive-ui'
import { RouterView } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { watch, computed } from 'vue'

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
const glassThemeOverrides = computed<GlobalThemeOverrides>(() => {
  const isDark = themeStore.isDark

  // 基础颜色配置
  const glassBg = isDark ? 'rgba(30, 30, 40, 0.75)' : 'rgba(255, 255, 255, 0.7)'
  const glassBgHover = isDark ? 'rgba(40, 40, 55, 0.85)' : 'rgba(255, 255, 255, 0.85)'
  const glassBorder = isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)'
  const textColor = isDark ? 'rgba(255, 255, 255, 0.9)' : 'rgba(0, 0, 0, 0.85)'
  const textColorSecondary = isDark ? 'rgba(255, 255, 255, 0.6)' : 'rgba(0, 0, 0, 0.55)'
  const primaryColor = isDark ? '#60a5fa' : '#3b82f6'
  const primaryColorHover = isDark ? '#93c5fd' : '#60a5fa'
  const primaryColorPressed = isDark ? '#3b82f6' : '#2563eb'

  return {
    common: {
      primaryColor,
      primaryColorHover,
      primaryColorPressed,
      primaryColorSuppl: primaryColor,
      bodyColor: isDark ? 'rgba(15, 15, 25, 0.95)' : 'rgba(245, 247, 250, 0.95)',
      cardColor: glassBg,
      modalColor: glassBg,
      popoverColor: glassBg,
      tableColor: glassBg,
      inputColor: isDark ? 'rgba(40, 40, 55, 0.6)' : 'rgba(255, 255, 255, 0.5)',
      actionColor: isDark ? 'rgba(40, 40, 55, 0.5)' : 'rgba(250, 250, 252, 0.6)',
      hoverColor: glassBgHover,
      borderColor: glassBorder,
      dividerColor: glassBorder,
      borderRadius: '12px',
      borderRadiusSmall: '8px',
      boxShadow1: isDark
        ? '0 4px 24px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05)'
        : '0 4px 24px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.8)',
      boxShadow2: isDark
        ? '0 8px 32px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.05)'
        : '0 8px 32px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.8)',
      boxShadow3: isDark
        ? '0 12px 48px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.05)'
        : '0 12px 48px rgba(0, 0, 0, 0.15), inset 0 1px 0 rgba(255, 255, 255, 0.8)',
    },
    Card: {
      color: glassBg,
      colorModal: glassBg,
      colorPopover: glassBg,
      colorEmbedded: isDark ? 'rgba(25, 25, 35, 0.6)' : 'rgba(250, 250, 252, 0.6)',
      borderColor: glassBorder,
      borderRadius: '16px',
    },
    Button: {
      colorSecondary: isDark ? 'rgba(60, 60, 80, 0.5)' : 'rgba(255, 255, 255, 0.6)',
      colorSecondaryHover: isDark ? 'rgba(70, 70, 95, 0.6)' : 'rgba(255, 255, 255, 0.8)',
      colorSecondaryPressed: isDark ? 'rgba(50, 50, 70, 0.7)' : 'rgba(240, 240, 245, 0.9)',
      borderRadiusMedium: '10px',
      borderRadiusSmall: '8px',
      borderRadiusLarge: '12px',
    },
    Input: {
      color: isDark ? 'rgba(40, 40, 55, 0.5)' : 'rgba(255, 255, 255, 0.5)',
      colorFocus: isDark ? 'rgba(45, 45, 65, 0.7)' : 'rgba(255, 255, 255, 0.75)',
      borderRadius: '10px',
      border: `1px solid ${glassBorder}`,
      borderHover: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.15)'}`,
      borderFocus: `1px solid ${primaryColor}`,
    },
    Menu: {
      color: 'transparent',
      itemColorHover: isDark ? 'rgba(60, 60, 80, 0.4)' : 'rgba(0, 0, 0, 0.04)',
      itemColorActive: isDark ? 'rgba(96, 165, 250, 0.15)' : 'rgba(59, 130, 246, 0.1)',
      itemColorActiveHover: isDark ? 'rgba(96, 165, 250, 0.2)' : 'rgba(59, 130, 246, 0.15)',
      borderRadius: '10px',
    },
    DataTable: {
      thColor: isDark ? 'rgba(35, 35, 50, 0.8)' : 'rgba(250, 250, 252, 0.8)',
      tdColor: 'transparent',
      tdColorHover: isDark ? 'rgba(50, 50, 70, 0.4)' : 'rgba(0, 0, 0, 0.02)',
      tdColorStriped: isDark ? 'rgba(40, 40, 55, 0.3)' : 'rgba(0, 0, 0, 0.015)',
      borderColor: glassBorder,
      borderRadius: '12px',
    },
    Modal: {
      color: glassBg,
      borderRadius: '20px',
    },
    Dialog: {
      color: glassBg,
      borderRadius: '20px',
    },
    Drawer: {
      color: glassBg,
      borderRadius: '0',
    },
    Dropdown: {
      color: glassBg,
      borderRadius: '12px',
      optionColorHover: isDark ? 'rgba(60, 60, 80, 0.5)' : 'rgba(0, 0, 0, 0.04)',
    },
    Popover: {
      color: glassBg,
      borderRadius: '12px',
    },
    Select: {
      peers: {
        InternalSelection: {
          color: isDark ? 'rgba(40, 40, 55, 0.5)' : 'rgba(255, 255, 255, 0.5)',
          borderRadius: '10px',
        }
      }
    },
    Tag: {
      borderRadius: '8px',
      color: isDark ? 'rgba(60, 60, 80, 0.5)' : 'rgba(0, 0, 0, 0.05)',
    },
    Message: {
      borderRadius: '12px',
    },
    Notification: {
      borderRadius: '16px',
    },
    Tabs: {
      tabBorderRadius: '10px',
      paneTextColor: textColor,
    },
    Pagination: {
      itemBorderRadius: '8px',
    },
    Form: {
      labelTextColor: textColorSecondary,
    },
    Layout: {
      color: 'transparent',
      siderColor: glassBg,
      headerColor: glassBg,
    },
  }
})
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
