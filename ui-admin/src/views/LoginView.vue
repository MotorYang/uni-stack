<template>
  <div class="min-h-screen flex items-center justify-center p-4">
    <n-card style="max-width: 420px; width: 100%" class="glass-card" title="Sign In">
      <n-form
        ref="formRef"
        :model="model"
        :rules="rules"
        size="large"
      >
        <n-form-item path="username" label="Username">
          <n-input 
            v-model:value="model.username" 
            placeholder="Enter your username"
          >
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-input>
        </n-form-item>
        
        <n-form-item path="password" label="Password">
          <n-input
            v-model:value="model.password"
            type="password"
            show-password-on="click"
            placeholder="Enter your password"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>
        
        <div class="flex justify-between items-center mb-6">
          <n-checkbox v-model:checked="model.rememberMe">Remember me</n-checkbox>
          <a href="#" class="text-sm">Forgot password?</a>
        </div>

        <n-button type="primary" block size="large" class="mb-4" @click="handleLogin" :loading="loading">
          Sign In
        </n-button>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  NForm, NFormItem, NInput, NButton, NCheckbox, NIcon, NCard, useMessage,
  type FormInst
} from 'naive-ui'
import { 
  PersonOutline, 
  LockClosedOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const model = ref({
  username: '',
  password: '',
  rememberMe: false
})

const rules = {
  username: {
    required: true,
    message: 'Please enter your username',
    trigger: ['blur', 'input']
  },
  password: {
    required: true,
    message: 'Please enter your password',
    trigger: ['blur', 'input']
  }
}

function handleLogin(e: MouseEvent) {
  e.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true
      try {
        await userStore.login(model.value)
        message.success('Login successful')
        router.push('/')
      } catch (error) {
        message.error('Login failed')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
