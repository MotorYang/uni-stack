<template>
  <div class="flex flex-col items-center justify-center h-full text-center p-8">
    <n-icon size="64" class="text-gray-300 mb-4">
      <ConstructOutline />
    </n-icon>
    <h2 class="text-2xl font-bold text-gray-700 dark:text-gray-200 mb-2">页面建设中</h2>
    <p class="text-gray-500 dark:text-gray-400 mb-6 max-w-md">
      当前路径并没有配置对应的页面组件。
    </p>
    <div class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-700 rounded-lg p-4 mb-6">
      <p class="text-yellow-700 dark:text-yellow-400 font-medium">
        请检查 “安全治理 / 菜单配置”
      </p>
    </div>
    <n-button class="glass-btn" @click="$router.push('/')">
      返回首页
    </n-button>
  </div>
</template>

<script setup lang="ts">
import { NIcon, NButton } from 'naive-ui'
import { ConstructOutline } from '@vicons/ionicons5'
</script>
