<template>
  <div class="users-container">
    <a-card class="mb-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-bold">用户管理</h2>
        <a-button type="primary" @click="handleAdd">
          <plus-outlined/>
          新增用户
        </a-button>
      </div>

      <!-- 搜索区域 -->
      <div class="search-area mb-4">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="用户名">
            <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allowClear/>
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="searchForm.status" placeholder="请选择状态" style="width: 120px" allowClear>
              <a-select-option :value="1">正常</a-select-option>
              <a-select-option :value="0">禁用</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleSearch">
              <search-outlined/>
              搜索
            </a-button>
            <a-button class="ml-2" @click="resetSearch">
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 用户列表 -->
      <a-table :columns="columns" :scroll="{x:1500}" :data-source="users" :pagination="pagination" :loading="loading"
               @change="handleTableChange" row-key="id" :bordered="true">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'role'">
            <a-tag :color="record.role === 'ADMIN' ? 'blue' : 'default'">
              {{ record.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="primary" size="small" @click="handleEdit(record)" ghost>
                编辑
              </a-button>
              <a-button type="primary" size="small" @click="handleViewUser(record.id)" ghost>
                查看
              </a-button>
              <a-button v-if="record.status === 1" type="danger" size="small"
                        @click="handleLockUser(record.id)" ghost>
                禁用
              </a-button>
              <a-button v-else type="primary" size="small" @click="handleUnlockUser(record.id)">
                启用
              </a-button>
              <a-popconfirm title="确定要删除该用户吗？" @confirm="handleDelete(record.id)" ok-text="确定"
                            cancel-text="取消">
                <a-button type="danger" size="small" ghost>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 用户详情对话框 -->
    <a-modal title="用户详情" :visible="userDetailVisible" @cancel="userDetailVisible = false" :footer=null
             width="600px">
      <div v-if="currentUser" class="user-detail">
        <h3 class="text-xl font-bold mb-4">{{ currentUser.username }}</h3>

        <div class="grid grid-cols-2 gap-4 mb-4">
          <div class="detail-item">
            <span class="text-gray-500">用户ID:</span>
            <span class="ml-2">{{ currentUser.id }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">邮箱:</span>
            <span class="ml-2">{{ currentUser.email }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">手机号:</span>
            <span class="ml-2">{{ currentUser.phone }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">角色:</span>
            <a-tag :color="currentUser.role === 'ADMIN' ? 'blue' : 'default'" class="ml-2">
              {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </a-tag>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">状态:</span>
            <a-tag :color="currentUser.status === 1 ? 'green' : 'red'" class="ml-2">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">注册时间:</span>
            <span class="ml-2">{{ currentUser.createTime }}</span>
          </div>
        </div>

        <div class="flex justify-end">
          <a-button v-if="currentUser.status === 1" type="danger" @click="handleLockUser(currentUser.id)"
                    class="mr-2">
            禁用用户
          </a-button>
          <a-button v-else type="primary" @click="handleUnlockUser(currentUser.id)" class="mr-2">
            启用用户
          </a-button>
          <a-button @click="userDetailVisible = false">
            关闭
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 新增/编辑用户对话框 -->
    <a-modal :title="userForm.id ? '编辑用户' : '新增用户'" :visible="userFormVisible" @ok="handleSubmitUser"
             @cancel="userFormVisible = false" :confirmLoading="submitLoading">
      <a-form ref="userFormRef" :model="userForm" :rules="userFormRules" :label-col="{ span: 6 }"
              :wrapper-col="{ span: 16 }">
        <a-form-item name="username" label="用户名">
          <a-input v-model:value="userForm.username" placeholder="请输入用户名" :disabled="!!userForm.id"/>
        </a-form-item>

        <a-form-item name="password" label="密码" v-if="!userForm.id">
          <a-input-password v-model:value="userForm.password" placeholder="请输入密码"/>
        </a-form-item>

        <a-form-item name="realName" label="真实姓名">
          <a-input v-model:value="userForm.realName" placeholder="请输入真实姓名"/>
        </a-form-item>

        <a-form-item name="phone" label="手机号">
          <a-input v-model:value="userForm.phone" placeholder="请输入手机号"/>
        </a-form-item>

        <a-form-item name="email" label="邮箱">
          <a-input v-model:value="userForm.email" placeholder="请输入邮箱"/>
        </a-form-item>

        <a-form-item name="role" label="角色">
          <a-select v-model:value="userForm.role" placeholder="请选择角色">
            <a-select-option :value="0">普通用户</a-select-option>
            <a-select-option :value="1">管理员</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item name="status" label="状态">
          <a-select v-model:value="userForm.status" placeholder="请选择状态">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted} from 'vue'
import {useUserStore} from '@/store/user'
import {message} from 'ant-design-vue'
import {SearchOutlined, PlusOutlined} from '@ant-design/icons-vue'
import {getUserList, getUserById, updateUserStatus, createUser, updateUser, deleteUser} from '@/api/user'

const userStore = useUserStore()
const loading = ref(false)
const users = ref([])
const userDetailVisible = ref(false)
const currentUser = ref(null)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

// 搜索表单
const searchForm = reactive({
  username: '',
  status: undefined
})

// 表格列定义
const columns = [
  {
    title: '用户ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username'
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    ellipsis: true
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone'
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    width: 100
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80
  },
  {
    title: '注册时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 300,
    fixed: 'right'
  }
]

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res.data || []

    // 过滤搜索条件
    if (searchForm.username) {
      users.value = users.value.filter(user =>
          user.username.toLowerCase().includes(searchForm.username.toLowerCase())
      )
    }

    if (searchForm.status) {
      users.value = users.value.filter(user => user.status === searchForm.status)
    }

    pagination.total = users.value.length

    // 手动分页
    const start = (pagination.current - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    users.value = users.value.slice(start, end)
  } catch (error) {
    console.error('获取用户列表失败:', error)
    message.error('获取用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

// 重置搜索
const resetSearch = () => {
  searchForm.username = ''
  searchForm.status = undefined
  pagination.current = 1
  fetchUsers()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUsers()
}

// 查看用户详情
const handleViewUser = async (id) => {
  try {
    const res = await getUserById(id)
    currentUser.value = res.data
    userDetailVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
    message.error('获取用户详情失败，请稍后重试')
  }
}

// 锁定用户
const handleLockUser = async (id) => {
  try {
    await updateUserStatus(id, 0)
    message.success('用户禁用成功')
    fetchUsers()

    // 如果是在详情页锁定，更新详情
    if (userDetailVisible.value && currentUser.value?.id === id) {
      const res = await getUserById(id)
      currentUser.value = res.data
    }
  } catch (error) {
    console.error('禁用用户失败:', error)
    message.error('禁用用户失败，请稍后重试')
  }
}

// 解锁用户
const handleUnlockUser = async (id) => {
  try {
    await updateUserStatus(id, 1)
    message.success('用户启用成功')
    fetchUsers()

    // 如果是在详情页解锁，更新详情
    if (userDetailVisible.value && currentUser.value?.id === id) {
      const res = await getUserById(id)
      currentUser.value = res.data
    }
  } catch (error) {
    console.error('启用用户失败:', error)
    message.error('启用用户失败，请稍后重试')
  }
}

// 用户表单对话框
const userFormVisible = ref(false)
const submitLoading = ref(false)
const userFormRef = ref()

// 用户表单数据
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 0,
  status: 1
})

// 表单校验规则
const userFormRules = {
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur'}
  ],
  email: [
    {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
  ],
  phone: [
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur'}
  ]
}

// 新增用户
const handleAdd = () => {
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    role: 0,
    status: 1
  })
  userFormVisible.value = true
}

// 编辑用户
const handleEdit = (record) => {
  Object.assign(userForm, {
    id: record.id,
    username: record.username,
    realName: record.realName,
    phone: record.phone,
    email: record.email,
    role: record.role,
    status: record.status
  })
  userFormVisible.value = true
}

// 提交用户表单
const handleSubmitUser = () => {
  userFormRef.value.validate().then(async () => {
    submitLoading.value = true
    try {
      if (userForm.id) {
        // 编辑用户
        await updateUser(userForm)
        message.success('编辑用户成功')
      } else {
        // 新增用户
        await createUser(userForm)
        message.success('新增用户成功')
      }
      userFormVisible.value = false
      fetchUsers()
    } catch (error) {
      console.error('保存用户失败:', error)
      message.error('保存用户失败：' + error.message)
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除用户
const handleDelete = async (id) => {
  try {
    await deleteUser(id)
    message.success('删除用户成功')
    fetchUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
    message.error('删除用户失败：' + error.message)
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-container {
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
}

.user-detail .detail-item {
  margin-bottom: 8px;
}

.users-container :deep(.ant-table-bordered .ant-table-cell) {
  border-color: #b4d3f7;
}

.users-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
  border-color: #b4d3f7;
}

.users-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
  border-color: #b4d3f7;
}
</style>