<template>
  <div class="min-h-screen flex items-center justify-center p-4 login-container">
    <n-card style="max-width: 420px; width: 100%" class="glass-card" title="用户登录">
      <n-form
        ref="formRef"
        :model="model"
        :rules="rules"
        size="large"
      >
        <n-form-item path="username" label="账号">
          <n-input 
            v-model:value="model.username" 
            placeholder="请输入账号"
          >
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-input>
        </n-form-item>
        
        <n-form-item path="password" label="密码">
          <n-input
            v-model:value="model.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>
        
        <div class="flex justify-between items-center mb-6">
          <n-checkbox v-model:checked="model.rememberMe">记住我</n-checkbox>
          <a href="#" class="text-sm">忘记密码?</a>
        </div>

        <n-button type="primary" text-color="white" block size="large" class="mb-4" @click="handleLogin" :loading="loading">
          登录
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
    message: '请输入用户名',
    trigger: ['blur', 'input']
  },
  password: {
    required: true,
    message: '请输入密码！',
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
        message.success('登录成功！')
        router.push('/')
      } catch (error) {
        message.error('登录失败！')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 1. 容器背景：深色渐变 + 科技网格 + 动态光晕 */
.login-container {
  background-color: #0a0f1d;
  /* 组合多个背景层：光晕渐变 + 细网格 + 粗网格 */
  background-image: 
    radial-gradient(circle at 50% 50%, rgba(18, 52, 86, 1) 0%, transparent 80%),
    linear-gradient(rgba(18, 107, 206, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 107, 206, 0.1) 1px, transparent 1px),
    linear-gradient(rgba(18, 107, 206, 0.05) 2px, transparent 2px),
    linear-gradient(90deg, rgba(18, 107, 206, 0.05) 2px, transparent 2px);
  background-size: 100% 100%, 20px 20px, 20px 20px, 100px 100px, 100px 100px;
  background-attachment: fixed;
  position: relative;
  overflow: hidden;
}

/* 2. 装饰性动画光点（伪元素实现） */
.login-container::before {
  content: "";
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: radial-gradient(circle at var(--x, 50%) var(--y, 50%), rgba(0, 221, 255, 0.08) 0%, transparent 30%);
  pointer-events: none;
}

/* 3. 增强版毛玻璃卡片 */
.glass-card {
  background: rgba(15, 25, 45, 0.75) !important; /* 深色半透明 */
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

/* 4. 修改 Naive UI 内部文字颜色以适应深色背景 */
:deep(.n-card-header__main) {
  color: #00d9ff !important;
  font-weight: bold;
  letter-spacing: 2px;
  text-shadow: 0 0 10px rgba(0, 217, 255, 0.5);
}

:deep(.n-form-item-label) {
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.n-input) {
  background-color: rgba(0, 0, 0, 0.2) !important;
}

/* 5. 登录按钮科技感微调 */
.n-button {
  background: linear-gradient(90deg, #00c6ff 0%, #0072ff 100%) !important;
  border: none;
  transition: all 0.3s ease;
}

.n-button:hover {
  box-shadow: 0 0 20px rgba(0, 198, 255, 0.6);
  transform: translateY(-1px);
}
</style>