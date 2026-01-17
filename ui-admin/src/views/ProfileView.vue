<template>
  <div class="p-8 space-y-8">
    <div class="glass-card !p-6 max-w-4xl mx-auto animate-enter space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6 border-b border-white/40 pb-6 mb-6">
        <div class="flex items-center gap-4">
          <n-avatar
            round
            :size="64"
            :src="userStore.userInfo?.avatar || defaultAvatar"
          />
          <div>
            <h2 class="text-2xl font-bold text-text-main mb-1">
              {{ userStore.userInfo?.nickName || userStore.userInfo?.username || '未命名用户' }}
            </h2>
            <p class="text-sm text-text-sec">
              {{ userStore.userInfo?.email || '未设置邮箱' }}
            </p>
          </div>
        </div>
        <n-button type="primary" text-color="white" strong round @click="openEdit">
          编辑资料
        </n-button>
      </div>

      <n-card class="glass-card !p-4" size="small" :bordered="false">
        <h3 class="text-sm font-semibold text-text-main mb-3">账号信息</h3>
        <div class="space-y-3 text-sm">
          <div class="flex items-center justify-between">
            <span class="text-text-sec">用户名</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.username || '-' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">昵称</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.nickName || '-' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">邮箱</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.email || '-' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">手机号</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.phone || '-' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">部门</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.department || '-' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">职位</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.position || '-' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">Github</span>
            <span class="font-medium text-text-main">
              <a
                v-if="userStore.userInfo?.github"
                :href="userStore.userInfo.github"
                target="_blank"
                rel="noopener noreferrer"
                class="text-primary underline decoration-dotted underline-offset-4"
              >
                {{ userStore.userInfo.github }}
              </a>
              <span v-else>-</span>
            </span>
          </div>
        </div>
      </n-card>

      <n-card class="glass-card !p-4" size="small" :bordered="false">
        <h3 class="text-sm font-semibold text-text-main mb-3">安全信息</h3>
        <div class="space-y-3 text-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-text-main font-medium">登录密码</p>
              <p class="text-xs text-text-sec">建议定期修改密码，保障账号安全</p>
            </div>
            <n-button size="small" quaternary>修改</n-button>
          </div>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-text-main font-medium">多因素认证</p>
              <p class="text-xs text-text-sec">未开启</p>
            </div>
            <n-button size="small" quaternary>去开启</n-button>
          </div>
        </div>
      </n-card>
    </div>

    <n-modal
      v-model:show="showEdit"
      preset="card"
      title="编辑资料"
      :style="{ maxWidth: '480px', width: '100%' }"
    >
      <n-form :model="formModel" label-placement="left" label-width="80">
        <n-form-item label="昵称">
          <n-input v-model:value="formModel.nickName" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱">
          <n-input v-model:value="formModel.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="手机号">
          <n-input v-model:value="formModel.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="部门">
          <n-input v-model:value="formModel.department" placeholder="请输入部门" />
        </n-form-item>
        <n-form-item label="职位">
          <n-input v-model:value="formModel.position" placeholder="请输入职位" />
        </n-form-item>
        <n-form-item label="Github">
          <n-input v-model:value="formModel.github" placeholder="请输入 Github 主页地址" />
        </n-form-item>
        <div class="flex justify-end gap-2 mt-4">
          <n-button quaternary @click="showEdit = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
        </div>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { NAvatar, NButton, NCard, NForm, NFormItem, NInput, NModal, useMessage } from 'naive-ui'
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const defaultAvatar =
  'https://ui-avatars.com/api/?name=User&background=4F46E5&color=fff'

const showEdit = ref(false)
const saving = ref(false)
const formModel = ref({
  nickName: '',
  email: '',
  phone: '',
  department: '',
  position: '',
  github: ''
})

const message = useMessage()

const openEdit = () => {
  if (userStore.userInfo) {
    formModel.value = {
      nickName: userStore.userInfo.nickName,
      email: userStore.userInfo.email,
      phone: userStore.userInfo.phone || '',
      department: userStore.userInfo.department || '',
      position: userStore.userInfo.position || '',
      github: userStore.userInfo.github || ''
    }
  }
  showEdit.value = true
}

const handleSave = async () => {
  if (!userStore.userInfo) return
  try {
    saving.value = true
    await userStore.updateUserInfo({
      nickName: formModel.value.nickName,
      email: formModel.value.email,
      phone: formModel.value.phone,
      department: formModel.value.department,
      position: formModel.value.position,
      github: formModel.value.github
    })
    message.success('资料已更新')
    showEdit.value = false
  } catch (error) {
    message.error('更新失败')
  } finally {
    saving.value = false
  }
}
</script>
