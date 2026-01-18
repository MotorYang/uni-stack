<template>
  <div class="p-8 space-y-8">
    <div class="glass-card !p-6 max-w-4xl mx-auto animate-enter space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6 border-b border-white/40 pb-6 mb-6">
        <div class="flex items-center gap-4">
          <div class="relative group cursor-pointer" @click="triggerAvatarUpload">
            <n-avatar
              round
              :size="64"
              :src="userStore.userInfo?.avatar || defaultAvatar"
            />
            <div class="absolute inset-0 bg-black/50 rounded-full opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
              <n-icon :size="20" color="white"><CameraOutline /></n-icon>
            </div>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleAvatarChange"
            />
          </div>
          <div>
            <h2 class="text-2xl font-bold text-text-main mb-1">
              {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '未命名用户' }}
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
            <span class="font-medium text-text-main">{{ userStore.userInfo?.nickname || '-' }}</span>
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
            <span class="text-text-sec">性别</span>
            <span class="font-medium text-text-main">{{ genderText }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">部门</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.deptName || '-' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-text-sec">角色</span>
            <span class="font-medium text-text-main">{{ userStore.userInfo?.roles?.join(', ') || '-' }}</span>
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
          <n-input v-model:value="formModel.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱">
          <n-input v-model:value="formModel.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="手机号">
          <n-input v-model:value="formModel.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="性别">
          <n-radio-group v-model:value="formModel.gender">
            <n-space>
              <n-radio :value="1">男</n-radio>
              <n-radio :value="2">女</n-radio>
              <n-radio :value="0">未知</n-radio>
            </n-space>
          </n-radio-group>
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
import { NAvatar, NButton, NCard, NForm, NFormItem, NInput, NModal, NRadio, NRadioGroup, NSpace, NIcon, useMessage } from 'naive-ui'
import { CameraOutline } from '@vicons/ionicons5'
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { uploadAvatar } from '@/api/storage'

const userStore = useUserStore()
const defaultAvatar =
  'https://ui-avatars.com/api/?name=User&background=4F46E5&color=fff'

const showEdit = ref(false)
const saving = ref(false)
const avatarInputRef = ref<HTMLInputElement | null>(null)
const formModel = ref({
  nickname: '',
  email: '',
  phone: '',
  gender: 0
})

const message = useMessage()

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }

  // 验证文件大小 (2MB)
  if (file.size > 2 * 1024 * 1024) {
    message.error('图片大小不能超过 2MB')
    return
  }

  try {
    message.loading('正在上传头像...')
    const result = await uploadAvatar(file)
    await userStore.updateUserInfo({ avatar: result.url })
    message.success('头像更新成功')
  } catch (error) {
    message.error('头像上传失败')
  } finally {
    // 清空 input，允许重复选择同一文件
    input.value = ''
  }
}

const genderText = computed(() => {
  const gender = userStore.userInfo?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
})

const openEdit = () => {
  if (userStore.userInfo) {
    formModel.value = {
      nickname: userStore.userInfo.nickname || '',
      email: userStore.userInfo.email || '',
      phone: userStore.userInfo.phone || '',
      gender: userStore.userInfo.gender ?? 0
    }
  }
  showEdit.value = true
}

const handleSave = async () => {
  if (!userStore.userInfo) return
  try {
    saving.value = true
    await userStore.updateUserInfo({
      nickname: formModel.value.nickname,
      email: formModel.value.email,
      phone: formModel.value.phone,
      gender: formModel.value.gender
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
