<template>
  <div class="p-6 space-y-6 animate-enter">
    <div class="glass-card !p-5 mb-4">
      <n-form inline :model="queryParams" label-placement="left" class="flex flex-wrap gap-4">
        <n-form-item label="用户名" :show-feedback="false">
          <n-input v-model:value="queryParams.username" placeholder="请输入用户名" clearable class="!w-40" />
        </n-form-item>
        <n-form-item label="昵称" :show-feedback="false">
          <n-input v-model:value="queryParams.nickname" placeholder="请输入昵称" clearable class="!w-40" />
        </n-form-item>
        <n-form-item label="手机号" :show-feedback="false">
          <n-input v-model:value="queryParams.phone" placeholder="请输入手机号" clearable class="!w-40" />
        </n-form-item>
        <n-form-item label="状态" :show-feedback="false">
          <n-select
            v-model:value="queryParams.status"
            :options="statusOptions"
            placeholder="请选择"
            clearable
            class="!w-28"
          />
        </n-form-item>
        <n-form-item :show-feedback="false">
          <n-space>
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon><SearchOutline /></n-icon></template>
              搜索
            </n-button>
            <n-button @click="handleReset">
              <template #icon><n-icon><RefreshOutline /></n-icon></template>
              重置
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>

    <div class="glass-card !p-5">
      <div class="flex justify-between items-center mb-4 ">
        <div class="flex items-center gap-2">
          <h3 class="text-lg font-semibold text-text-main">用户列表</h3>
          <n-tag size="small" round :bordered="false" type="info">{{ total }} 条</n-tag>
        </div>
        <n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增用户
          </n-button>
        </n-space>
      </div>

      <div class="glass-table">
        <n-data-table
          :columns="columns"
          :data="userList"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: UserVO) => row.id"
          :bordered="false"
          :single-line="false"
          :scroll-x="1200"
          flex-height
          style="min-height: 400px"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </div>

    <n-modal
      v-model:show="showModal"
      :title="modalTitle"
      preset="card"
      style="width: 600px"
      :mask-closable="false"
      display-directive="show"
      :auto-focus="false"
      :trap-focus="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="80"
      >
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </n-form-item>
        <n-form-item v-if="!isEdit" label="密码" path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="formData.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="头像">
          <div class="flex items-center gap-4">
            <n-avatar
              round
              :size="40"
              :src="formData.avatar || undefined"
              :fallback-src="'https://ui-avatars.com/api/?name=' + (formData.nickname || formData.username)"
            />
            <div>
              <n-button size="small" @click="triggerAvatarUpload">上传头像</n-button>
              <input
                ref="avatarInputRef"
                type="file"
                accept="image/*"
                class="hidden"
                @change="handleAvatarChange"
              />
            </div>
          </div>
        </n-form-item>
        <n-form-item label="性别" path="gender">
          <n-radio-group v-model:value="formData.gender">
            <n-space>
              <n-radio :value="1">男</n-radio>
              <n-radio :value="2">女</n-radio>
              <n-radio :value="0">未知</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item v-if="isEdit" label="状态" path="status">
          <n-radio-group v-model:value="formData.status">
            <n-space>
              <n-radio :value="0">正常</n-radio>
              <n-radio :value="1">禁用</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="showResetPwdModal"
      title="重置密码"
      preset="card"
      style="width: 400px"
      :mask-closable="false"
      display-directive="show"
      :auto-focus="false"
      :trap-focus="false"
    >
      <n-form
        ref="resetPwdFormRef"
        :model="resetPwdData"
        :rules="resetPwdRules"
        label-placement="left"
        label-width="80"
      >
        <n-form-item label="新密码" path="newPassword">
          <n-input
            v-model:value="resetPwdData.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input
            v-model:value="resetPwdData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password-on="click"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showResetPwdModal = false">取消</n-button>
          <n-button type="primary" :loading="resettingPwd" @click="handleResetPassword">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import {
  NForm,
  NFormItem,
  NInput,
  NButton,
  NSpace,
  NSelect,
  NDataTable,
  NModal,
  NRadio,
  NRadioGroup,
  NIcon,
  NTag,
  NAvatar,
  NPopconfirm,
  NTooltip,
  useMessage,
  type FormInst,
  type FormRules,
  type DataTableColumns
} from 'naive-ui'
import { SearchOutline, RefreshOutline, AddOutline, CreateOutline, KeyOutline, TrashOutline } from '@vicons/ionicons5'
import {
  getUserPage,
  createUser,
  updateUser,
  deleteUser,
  resetPassword,
  type UserVO,
  type UserQueryDTO,
  type UserCreateDTO,
  type UserUpdateDTO
} from '@/api/user'
import { uploadAvatar } from '@/api/storage'

const message = useMessage()

const statusOptions = [
  { label: '正常', value: 0 },
  { label: '禁用', value: 1 }
]

const queryParams = reactive<UserQueryDTO>({
  username: undefined,
  nickname: undefined,
  phone: undefined,
  status: undefined,
  current: 1,
  size: 10
})

const userList = ref<UserVO[]>([])
const loading = ref(false)
const total = ref(0)

const pagination = computed(() => ({
  page: queryParams.current,
  pageSize: queryParams.size,
  pageCount: Math.ceil(total.value / (queryParams.size || 10)),
  itemCount: total.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
}))

const columns: DataTableColumns<UserVO> = [
  {
    title: '用户名',
    key: 'username',
    width: 120
  },
  {
    title: '昵称',
    key: 'nickname',
    width: 120,
    render: row => row.nickname || '-'
  },
  {
    title: '头像',
    key: 'avatar',
    width: 80,
    render: row =>
      h(NAvatar, {
        size: 'small',
        round: true,
        src: row.avatar || undefined,
        fallbackSrc: 'https://ui-avatars.com/api/?name=' + (row.nickname || row.username)
      })
  },
  {
    title: '邮箱',
    key: 'email',
    width: 180,
    ellipsis: { tooltip: true },
    render: row => row.email || '-'
  },
  {
    title: '手机号',
    key: 'phone',
    width: 120,
    render: row => row.phone || '-'
  },
  {
    title: '部门',
    key: 'deptName',
    width: 120,
    render: row => row.deptName || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(
        NTag,
        {
          type: row.status === 0 ? 'success' : 'error',
          size: 'small'
        },
        { default: () => (row.status === 0 ? '正常' : '禁用') }
      )
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: row => row.createTime?.replace('T', ' ').substring(0, 19) || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    fixed: 'right',
    render: row =>
      h(NSpace, { size: 'small', wrap: false }, {
        default: () => [
          h(
            NTooltip,
            null,
            {
              trigger: () =>
                h(
                  NButton,
                  {
                    size: 'small',
                    quaternary: true,
                    onClick: () => handleEdit(row)
                  },
                  { icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) }
                ),
              default: () => '编辑'
            }
          ),
          h(
            NTooltip,
            null,
            {
              trigger: () =>
                h(
                  NButton,
                  {
                    size: 'small',
                    quaternary: true,
                    type: 'warning',
                    onClick: () => handleResetPwd(row)
                  },
                  { icon: () => h(NIcon, null, { default: () => h(KeyOutline) }) }
                ),
              default: () => '重置密码'
            }
          ),
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row)
            },
            {
              trigger: () =>
                h(
                  NTooltip,
                  null,
                  {
                    trigger: () =>
                      h(
                        NButton,
                        {
                          size: 'small',
                          quaternary: true,
                          type: 'error'
                        },
                        { icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) }
                      ),
                    default: () => '删除'
                  }
                ),
              default: () => '确认删除该用户吗？'
            }
          )
        ]
      })
  }
]

const showModal = ref(false)
const modalTitle = ref('新增用户')
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const editingId = ref<string | null>(null)

const formData = reactive<UserCreateDTO & UserUpdateDTO>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  avatar: ''
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    {
      validator: (_rule, value) => {
        if (!isEdit.value && !value) {
          return new Error('请输入密码')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: ['blur', 'input']
    }
  ],
  phone: [
    {
      pattern: /^1\d{10}$/,
      message: '请输入正确的手机号',
      trigger: ['blur', 'input']
    }
  ]
}

const avatarInputRef = ref<HTMLInputElement | null>(null)

const resetForm = () => {
  Object.assign(formData, {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    gender: 0,
    avatar: '',
    status: 0
  })
}

const handleSearch = () => {
  queryParams.current = 1
  loadUsers()
}

const handleReset = () => {
  queryParams.username = undefined
  queryParams.nickname = undefined
  queryParams.phone = undefined
  queryParams.status = undefined
  queryParams.current = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  queryParams.current = page
  loadUsers()
}

const handlePageSizeChange = (pageSize: number) => {
  queryParams.size = pageSize
  queryParams.current = 1
  loadUsers()
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res.records
    total.value = res.total
  } catch (error) {
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增用户'
  editingId.value = null
  resetForm()
  showModal.value = true
}

const handleEdit = (row: UserVO) => {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  editingId.value = row.id
  Object.assign(formData, {
    username: row.username,
    nickname: row.nickname,
    email: row.email,
    phone: row.phone,
    gender: row.gender,
    avatar: row.avatar,
    status: row.status
  })
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      const payload: UserUpdateDTO = {
        nickname: formData.nickname,
        email: formData.email,
        phone: formData.phone,
        gender: formData.gender,
        avatar: formData.avatar,
        status: formData.status
      }
      await updateUser(editingId.value, payload)
      message.success('更新成功')
    } else {
      const payload: UserCreateDTO = {
        username: formData.username,
        password: formData.password || '',
        nickname: formData.nickname,
        email: formData.email,
        phone: formData.phone,
        gender: formData.gender,
        avatar: formData.avatar
      }
      await createUser(payload)
      message.success('创建成功')
    }
    showModal.value = false
    loadUsers()
  } catch (error) {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: UserVO) => {
  try {
    await deleteUser(row.id)
    message.success('删除成功')
    loadUsers()
  } catch (error) {
    message.error('删除失败')
  }
}

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    message.error('图片大小不能超过 2MB')
    return
  }

  try {
    const result = await uploadAvatar(file)
    formData.avatar = result.url
    message.success('头像上传成功')
  } catch (error) {
    message.error('头像上传失败')
  } finally {
    input.value = ''
  }
}

const showResetPwdModal = ref(false)
const resetPwdFormRef = ref<FormInst | null>(null)
const resettingPwd = ref(false)
const currentResetUserId = ref<string | null>(null)

const resetPwdData = reactive({
  newPassword: '',
  confirmPassword: ''
})

const resetPwdRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_rule, value) => {
        if (value !== resetPwdData.newPassword) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

const handleResetPwd = (row: UserVO) => {
  currentResetUserId.value = row.id
  resetPwdData.newPassword = ''
  resetPwdData.confirmPassword = ''
  showResetPwdModal.value = true
}

const handleResetPassword = async () => {
  try {
    await resetPwdFormRef.value?.validate()
  } catch {
    return
  }

  if (!currentResetUserId.value) return

  resettingPwd.value = true
  try {
    await resetPassword(currentResetUserId.value, resetPwdData.newPassword)
    message.success('密码重置成功')
    showResetPwdModal.value = false
  } catch (error) {
    message.error('密码重置失败')
  } finally {
    resettingPwd.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>
