<template>
  <div class="profile-container">
    <a-row :gutter="16">
      <!-- 左侧个人信息卡片 -->
      <a-col :span="8">
        <a-card class="mb-4">
          <div class="text-center mb-4">
            <a-avatar :size="80" class="mb-2">
              {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
            </a-avatar>
            <h2 class="text-xl font-bold">{{ userStore.userInfo?.username }}</h2>
            <p class="text-gray-500">
              <a-tag :color="userStore.isAdmin ? 'blue' : 'default'">
                {{ userStore.isAdmin ? '管理员' : '普通用户' }}
              </a-tag>
            </p>
          </div>

          <a-divider/>

          <div class="user-info">
            <p class="mb-2">
              <mail-outlined class="mr-2"/>
              <span class="text-gray-600">邮箱：</span>
              <span>{{ userStore.userInfo?.email }}</span>
            </p>
            <p class="mb-2">
              <phone-outlined class="mr-2"/>
              <span class="text-gray-600">手机：</span>
              <span>{{ userStore.userInfo?.phone }}</span>
            </p>
            <p class="mb-2">
              <calendar-outlined class="mr-2"/>
              <span class="text-gray-600">注册时间：</span>
              <span>{{ userStore.userInfo?.createTime }}</span>
            </p>
          </div>
        </a-card>

        <!-- 借阅统计卡片 -->
        <a-card title="借阅统计" :loading="loading">
          <a-statistic title="当前借阅" :value="borrowStats.currentCount" class="mb-4"/>
          <a-statistic title="历史借阅" :value="borrowStats.historyCount" class="mb-4"/>
          <a-statistic title="逾期次数" :value="borrowStats.overdueCount" class="mb-4"/>
          <a-statistic title="待缴罚款" :value="borrowStats.fineAmount" :precision="2" prefix="¥"
                       :value-style="{ color: borrowStats.fineAmount > 0 ? '#cf1322' : '#3f8600' }"/>
        </a-card>
      </a-col>

      <!-- 右侧内容区 -->
      <a-col :span="16">
        <a-card class="mb-4">
          <a-tabs v-model:activeKey="activeTabKey">
            <a-tab-pane key="info" tab="个人信息">
              <a-form :model="userForm" :rules="rules" ref="userFormRef" layout="vertical">
                <a-form-item name="username" label="用户名">
                  <a-input v-model:value="userForm.username" placeholder="请输入用户名" disabled/>
                </a-form-item>

                <a-form-item name="email" label="邮箱">
                  <a-input v-model:value="userForm.email" placeholder="请输入邮箱"/>
                </a-form-item>

                <a-form-item name="phone" label="手机号">
                  <a-input v-model:value="userForm.phone" placeholder="请输入手机号"/>
                </a-form-item>

                <a-form-item>
                  <a-button type="primary" @click="handleUpdateInfo" :loading="updateLoading">
                    保存修改
                  </a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>

            <a-tab-pane key="password" tab="修改密码">
              <a-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef"
                      layout="vertical">
                <a-form-item name="oldPassword" label="当前密码">
                  <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入当前密码"/>
                </a-form-item>

                <a-form-item name="newPassword" label="新密码">
                  <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码"/>
                </a-form-item>

                <a-form-item name="confirmPassword" label="确认新密码">
                  <a-input-password v-model:value="passwordForm.confirmPassword"
                                    placeholder="请确认新密码"/>
                </a-form-item>

                <a-form-item>
                  <a-button type="primary" @click="handleUpdatePassword" :loading="passwordLoading">
                    修改密码
                  </a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>

            <a-tab-pane key="history" tab="借阅历史">
              <a-table :columns="borrowColumns" :scroll="{x:1500}" :data-source="borrowHistory" :pagination="pagination"
                       :loading="historyLoading" @change="handleTableChange" row-key="id">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="getBorrowStatusColor(record.status)">
                      {{ getBorrowStatusText(record.status) }}
                    </a-tag>
                  </template>
                  <template v-if="column.key === 'action'">
                    <a-space>
                      <a-button v-if="record.status === 0" type="primary" size="small"
                                @click="handleRenew(record.id)">
                        续借
                      </a-button>
                      <a-button v-if="record.status === 0" type="default" size="small"
                                @click="handleReturn(record.id)">
                        归还
                      </a-button>
                      <a-button
                          v-if="record.status === 2 && record.fine > 0 && record.fineStatus === 1"
                          type="danger" size="small" @click="handlePayFine(record.id)">
                        缴纳罚款
                      </a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import {useUserStore} from '@/store/user'
import {message} from 'ant-design-vue'
import {
  MailOutlined,
  PhoneOutlined,
  CalendarOutlined
} from '@ant-design/icons-vue'
import {updateUserInfo, updatePassword} from '@/api/user'
import {getMyBorrowList, returnBook, renewBook, payFine} from '@/api/borrow'

const userStore = useUserStore()
const activeTabKey = ref('info')
const loading = ref(false)
const updateLoading = ref(false)
const passwordLoading = ref(false)
const historyLoading = ref(false)
const userFormRef = ref(null)
const passwordFormRef = ref(null)

// 借阅统计
const borrowStats = reactive({
  currentCount: 0,
  historyCount: 0,
  overdueCount: 0,
  fineAmount: 0
})

// 借阅历史
const borrowHistory = ref([])

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

// 用户表单
const userForm = reactive({
  username: userStore.userInfo?.username || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  email: [
    {required: true, message: '请输入邮箱'},
    {type: 'email', message: '请输入有效的邮箱地址'}
  ],
  phone: [
    {required: true, message: '请输入手机号'}
  ]
}

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    {required: true, message: '请输入当前密码'}
  ],
  newPassword: [
    {required: true, message: '请输入新密码'},
    {min: 6, message: '密码长度不能少于6个字符'}
  ],
  confirmPassword: [
    {required: true, message: '请确认新密码'},
    {validator: validateConfirmPassword}
  ]
}

// 借阅历史表格列
const borrowColumns = [
  {
    title: '图书名称',
    dataIndex: 'bookTitle',
    key: 'bookTitle',
    ellipsis: true,
  },
  {
    title: '借阅日期',
    dataIndex: 'borrowTime',
    key: 'borrowTime',
    customRender: ({text}) => text ? text.substring(0, 10) : '-'
  },
  {
    title: '应还日期',
    dataIndex: 'returnTime',
    key: 'returnTime',
    customRender: ({text}) => text ? text.substring(0, 10) : '-'
  },
  {
    title: '实际归还日期',
    dataIndex: 'actualReturnTime',
    key: 'actualReturnTime',
    customRender: ({text}) => text ? text.substring(0, 10) : '-'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed:'right'
  }
]

// 验证确认密码
async function validateConfirmPassword(rule, value) {
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

// 获取借阅状态颜色
const getBorrowStatusColor = (status) => {
  const statusColors = {
    0: 'green',    // 借阅中
    1: 'blue',     // 已归还
    2: 'red',      // 已逾期未还
    3: 'orange'    // 已逾期已还
  }
  return statusColors[status] || 'default'
}

// 获取借阅状态文本
const getBorrowStatusText = (status) => {
  const statusTexts = {
    0: '借阅中',
    1: '已归还',
    2: '已逾期未还',
    3: '已逾期已还'
  }
  return statusTexts[status] || '未知'
}

// 获取借阅历史
const fetchBorrowHistory = async () => {
  historyLoading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    }

    const res = await getMyBorrowList(params)
    borrowHistory.value = res.data.records || []
    pagination.total = res.data.total || 0

    // 计算借阅统计
    calculateBorrowStats(res.data.records || [], res.data.total || 0)
  } catch (error) {
    console.error('获取借阅历史失败:', error)
    message.error('获取借阅历史失败，请稍后重试')
  } finally {
    historyLoading.value = false
  }
}

// 计算借阅统计
const calculateBorrowStats = (borrows, total) => {
  let currentCount = 0
  let overdueCount = 0
  let fineAmount = 0

  borrows.forEach(borrow => {
    if (borrow.status === 0) {
      currentCount++
    }

    if (borrow.status === 2 || borrow.status === 3) {
      overdueCount++
    }

    if ((borrow.status === 2 || borrow.status === 3) && borrow.fine > 0 && borrow.fineStatus === 0) {
      fineAmount += borrow.fine || 0
    }
  })

  borrowStats.currentCount = currentCount
  borrowStats.historyCount = total
  borrowStats.overdueCount = overdueCount
  borrowStats.fineAmount = fineAmount
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchBorrowHistory()
}

// 更新个人信息
const handleUpdateInfo = () => {
  userFormRef.value.validate().then(async () => {
    updateLoading.value = true
    try {
      await updateUserInfo(userForm)
      message.success('个人信息更新成功')

      // 更新本地存储的用户信息
      await userStore.getUserInfo()
    } catch (error) {
      console.error('更新个人信息失败:', error)
      message.error('更新个人信息失败，请稍后重试')
    } finally {
      updateLoading.value = false
    }
  })
}

// 更新密码
const handleUpdatePassword = () => {
  passwordFormRef.value.validate().then(async () => {
    passwordLoading.value = true
    try {
      await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
      message.success('密码修改成功，请重新登录')

      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''

      // 退出登录
      userStore.logout()
      router.push('/login')
    } catch (error) {
      console.error('修改密码失败:', error)
      message.error('修改密码失败，请检查当前密码是否正确')
    } finally {
      passwordLoading.value = false
    }
  })
}

// 续借
const handleRenew = async (id) => {
  try {
    await renewBook(id)
    message.success('续借成功')
    fetchBorrowHistory()
  } catch (error) {
    console.error('续借失败:', error)
    message.error('续借失败，请稍后重试')
  }
}

// 归还
const handleReturn = async (id) => {
  try {
    await returnBook(id)
    message.success('归还成功')
    fetchBorrowHistory()
  } catch (error) {
    console.error('归还失败:', error)
    message.error('归还失败，请稍后重试')
  }
}

// 缴纳罚款
const handlePayFine = async (id) => {
  try {
    await payFine(id)
    message.success('罚款缴纳成功')
    fetchBorrowHistory()
  } catch (error) {
    console.error('缴纳罚款失败:', error)
    message.error('缴纳罚款失败，请稍后重试')
  }
}

onMounted(() => {
  // 初始化表单数据
  userForm.username = userStore.userInfo?.username || ''
  userForm.email = userStore.userInfo?.email || ''
  userForm.phone = userStore.userInfo?.phone || ''

  // 获取借阅历史
  fetchBorrowHistory()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
}

.user-info p {
  display: flex;
  align-items: center;
}
</style>