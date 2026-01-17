import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<ThemeMode>(localStorage.getItem('theme') as ThemeMode || 'auto')
  const systemIsDark = ref(window.matchMedia('(prefers-color-scheme: dark)').matches)

  // Update system preference listener
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    systemIsDark.value = e.matches
  })

  const isDark = computed(() => {
    if (theme.value === 'auto') {
      return systemIsDark.value
    }
    return theme.value === 'dark'
  })

  const setTheme = (mode: ThemeMode) => {
    theme.value = mode
    localStorage.setItem('theme', mode)
  }

  const toggleTheme = () => {
    if (isDark.value) {
      setTheme('light')
    } else {
      setTheme('dark')
    }
  }

  return {
    theme,
    isDark,
    setTheme,
    toggleTheme
  }
})
