<template>
  <n-input :value="modelValue" :placeholder="placeholder" @update:value="handleInput">
    <template #prefix>
      <n-icon v-if="currentIcon" :component="currentIcon" />
    </template>
    <template #suffix>
      <n-popover trigger="click" placement="bottom-start">
        <template #trigger>
          <n-button quaternary size="tiny">选择</n-button>
        </template>
        <div class="w-80">
          <n-input
            v-model:value="searchKeyword"
            size="small"
            placeholder="搜索图标名称..."
            class="mb-2"
          />
          <div class="max-h-64 overflow-y-auto">
            <n-grid :cols="6" :x-gap="8" :y-gap="8">
              <n-gi v-for="name in filteredIconNames" :key="name">
                <div
                  class="flex flex-col items-center gap-1 p-1 cursor-pointer rounded hover:bg-gray-100 dark:hover:bg-gray-700"
                  @click="handleSelect(name)"
                >
                  <n-icon :component="getIconComponent(name)" />
                  <span class="text-[10px] truncate w-full text-center">{{ name }}</span>
                </div>
              </n-gi>
            </n-grid>
          </div>
        </div>
      </n-popover>
    </template>
  </n-input>
</template>

<script setup lang="ts">
import { computed, ref, type Component } from 'vue'
import { NInput, NIcon, NPopover, NGrid, NGi, NButton } from 'naive-ui'
import * as Ionicons from '@vicons/ionicons5'
import { HelpCircleOutline } from '@vicons/ionicons5'

const props = defineProps<{
  modelValue?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | undefined): void
}>()

const searchKeyword = ref('')

const allIconNames = Object.keys(Ionicons).filter((name) => /^[A-Z].+Outline$/.test(name))

const filteredIconNames = computed(() => {
  if (!searchKeyword.value) return allIconNames.slice(0, 300)
  const keyword = searchKeyword.value.toLowerCase()
  return allIconNames.filter((name) => name.toLowerCase().includes(keyword)).slice(0, 300)
})

const getIconComponent = (name: string): Component => {
  const icon = (Ionicons as Record<string, Component | undefined>)[name]
  return icon || HelpCircleOutline
}

const currentIcon = computed<Component | null>(() => {
  if (!props.modelValue) return null
  const icon = (Ionicons as Record<string, Component | undefined>)[props.modelValue]
  if (icon) return icon
  return null
})

const handleSelect = (name: string) => {
  emit('update:modelValue', name)
}

const handleInput = (value: string) => {
  emit('update:modelValue', value || undefined)
}
</script>
