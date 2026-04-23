<template>
  <div class="profile-container">
    <a-row :gutter="16">
      <!-- 左侧个人信息卡片 -->
      <a-col :span="8">
        <a-card class="mb-4">
          <div class="text-center mb-4">
            <a-avatar :size="80" class="mb-2" :style="{ background: userStore.isAdmin ? '#1890ff' : '#52c41a' }">
              {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
            </a-avatar>
            <h2 class="text-xl font-bold">{{ userStore.userInfo?.username }}</h2>
            <p class="text-gray-500">
              <a-tag :color="userStore.isAdmin ? 'blue' : 'default'">
                {{ userStore.isAdmin ? '管理员' : '普通用户' }}
              </a-tag>
            </p>
            <p v-if="!userStore.isAdmin" class="text-gray-500">
              <a-tag :color="userStore.userInfo?.userType === 1 ? 'purple' : 'green'">
                {{ userStore.userInfo?.userType === 1 ? '教师' : '学生' }}
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
            <p v-if="userStore.isAdmin" class="mb-2">
              <user-outlined class="mr-2"/>
              <span class="text-gray-600">角色权限：</span>
              <span>系统管理员</span>
            </p>
          </div>
        </a-card>

        <!-- 统计卡片 -->
        <a-card v-if="!userStore.isAdmin" title="借阅统计" :loading="loading">
          <a-statistic title="当前借阅" :value="borrowStats.currentCount" class="mb-4"/>
          <a-statistic title="历史借阅" :value="borrowStats.historyCount" class="mb-4"/>
          <a-statistic title="逾期次数" :value="borrowStats.overdueCount" class="mb-4"/>
          <a-statistic title="待缴罚款" :value="borrowStats.fineAmount" :precision="2" prefix="¥"
                       :value-style="{ color: borrowStats.fineAmount > 0 ? '#cf1322' : '#3f8600' }"/>
        </a-card>

        <!-- 管理员统计卡片 -->
        <a-card v-if="userStore.isAdmin" title="系统统计" :loading="adminStatsLoading">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-statistic title="用户总数" :value="adminStats.userCount" class="mb-4"/>
              <a-statistic title="图书总数" :value="adminStats.bookCount" class="mb-4"/>
            </a-col>
            <a-col :span="12">
              <a-statistic title="当前借阅" :value="adminStats.borrowCount" class="mb-4"/>
              <a-statistic title="今日访问" :value="adminStats.todayVisit"/>
            </a-col>
          </a-row>
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

                <a-form-item name="realName" label="真实姓名">
                  <a-input v-model:value="userForm.realName" placeholder="请输入真实姓名"/>
                </a-form-item>

                <a-form-item name="email" label="邮箱">
                  <a-input v-model:value="userForm.email" placeholder="请输入邮箱"/>
                </a-form-item>

                <a-form-item name="phone" label="手机号">
                  <a-input v-model:value="userForm.phone" placeholder="请输入手机号"/>
                </a-form-item>

                <a-form-item name="department" :label="userStore.isAdmin ? '单位' : '学院'">
                  <a-input v-model:value="userForm.department" :placeholder="userStore.isAdmin ? '请输入单位' : '请输入学院'"/>
                </a-form-item>

                <a-form-item name="position" :label="userStore.isAdmin ? '职位' : '专业'">
                  <a-input v-model:value="userForm.position" :placeholder="userStore.isAdmin ? '请输入职位' : '请输入专业'"/>
                </a-form-item>

                <a-form-item name="studentId" label="学号/工号">
                  <a-input v-model:value="userForm.studentId" placeholder="请输入学号或工号"/>
                </a-form-item>

                <a-form-item name="address" label="地址">
                  <a-input v-model:value="userForm.address" placeholder="请输入地址"/>
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

            <!-- 管理员专属标签页 -->
            <a-tab-pane v-if="userStore.isAdmin" key="system" tab="系统管理">
              <div class="system-management">
                <a-card class="mb-4">
                  <template #title>
                    <div class="flex items-center">
                      <setting-outlined class="mr-2"/>
                      <span>系统设置</span>
                    </div>
                  </template>
                  <div class="system-settings">
                    <a-row :gutter="16">
                      <a-col :span="8">
                        <a-card hoverable @click="navigateTo('books')" class="setting-card">
                          <template #cover>
                            <img alt="图书管理" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=library%20books%20management%20system%20interface&image_size=square_hd" />
                          </template>
                          <a-card-meta title="图书管理" description="管理图书信息、分类、库存" />
                        </a-card>
                      </a-col>
                      <a-col :span="8">
                        <a-card hoverable @click="navigateTo('users')" class="setting-card">
                          <template #cover>
                            <img alt="用户管理" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20management%20system%20interface&image_size=square_hd" />
                          </template>
                          <a-card-meta title="用户管理" description="管理用户账号、权限" />
                        </a-card>
                      </a-col>
                      <a-col :span="8">
                        <a-card hoverable @click="navigateTo('borrows')" class="setting-card">
                          <template #cover>
                            <img alt="借阅管理" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=book%20borrowing%20management%20system&image_size=square_hd" />
                          </template>
                          <a-card-meta title="借阅管理" description="管理借阅记录、归还、罚款" />
                        </a-card>
                      </a-col>
                    </a-row>
                    <a-row :gutter="16" class="mt-4">
                      <a-col :span="8">
                        <a-card hoverable @click="navigateTo('logs')" class="setting-card">
                          <template #cover>
                            <img alt="日志管理" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=system%20logs%20management%20interface&image_size=square_hd" />
                          </template>
                          <a-card-meta title="日志管理" description="查看系统操作日志" />
                        </a-card>
                      </a-col>
                      <a-col :span="8">
                        <a-card hoverable class="setting-card">
                          <template #cover>
                            <img alt="数据备份" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=data%20backup%20system%20interface&image_size=square_hd" />
                          </template>
                          <a-card-meta title="数据备份" description="备份和恢复系统数据" />
                        </a-card>
                      </a-col>
                      <a-col :span="8">
                        <a-card hoverable class="setting-card">
                          <template #cover>
                            <img alt="系统设置" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=system%20settings%20dashboard&image_size=square_hd" />
                          </template>
                          <a-card-meta title="系统设置" description="配置系统参数和选项" />
                        </a-card>
                      </a-col>
                    </a-row>
                  </div>
                </a-card>

                <a-card>
                  <template #title>
                    <div class="flex items-center">
                      <dashboard-outlined class="mr-2"/>
                      <span>系统状态</span>
                    </div>
                  </template>
                  <div class="system-status">
                    <a-row :gutter="16">
                      <a-col :span="12">
                        <a-descriptions bordered :column="1">
                          <a-descriptions-item label="系统版本">1.0.0</a-descriptions-item>
                          <a-descriptions-item label="运行状态">正常</a-descriptions-item>
                          <a-descriptions-item label="数据库状态">连接正常</a-descriptions-item>
                          <a-descriptions-item label="最后更新">2026-04-22</a-descriptions-item>
                        </a-descriptions>
                      </a-col>
                      <a-col :span="12">
                        <a-descriptions bordered :column="1">
                          <a-descriptions-item label="服务器IP">127.0.0.1</a-descriptions-item>
                          <a-descriptions-item label="服务器端口">8086</a-descriptions-item>
                          <a-descriptions-item label="运行时间">72小时</a-descriptions-item>
                          <a-descriptions-item label="内存使用">256MB / 1GB</a-descriptions-item>
                        </a-descriptions>
                      </a-col>
                    </a-row>
                  </div>
                </a-card>
              </div>
            </a-tab-pane>


          </a-tabs>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/store/user'
import {message} from 'ant-design-vue'
import {
  MailOutlined,
  PhoneOutlined,
  CalendarOutlined,
  UserOutlined,
  SettingOutlined,
  DashboardOutlined
} from '@ant-design/icons-vue'
import {updateUserInfo, updatePassword} from '@/api/user'
import {getMyBorrowList, returnBook, renewBook, payFine} from '@/api/borrow'

const userStore = useUserStore()
const router = useRouter()
const activeTabKey = ref('info')
const loading = ref(false)
const updateLoading = ref(false)
const passwordLoading = ref(false)
const historyLoading = ref(false)
const adminStatsLoading = ref(false)
const userFormRef = ref(null)
const passwordFormRef = ref(null)

// 借阅统计
const borrowStats = reactive({
  currentCount: 0,
  historyCount: 0,
  overdueCount: 0,
  fineAmount: 0
})

// 管理员统计
const adminStats = reactive({
  userCount: 0,
  bookCount: 0,
  borrowCount: 0,
  todayVisit: 0
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
  realName: userStore.userInfo?.realName || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || '',
  department: userStore.userInfo?.department || '',
  position: userStore.userInfo?.position || '',
  studentId: userStore.userInfo?.studentId || '',
  address: userStore.userInfo?.address || ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  realName: [
    {required: true, message: '请输入真实姓名'}
  ],
  email: [
    {required: true, message: '请输入邮箱'},
    {type: 'email', message: '请输入有效的邮箱地址'}
  ],
  phone: [
    {required: true, message: '请输入手机号'}
  ],
  department: [
    {required: true, message: '请输入部门'}
  ],
  position: [
    {required: true, message: '请输入职位'}
  ],
  studentId: [
    {required: true, message: '请输入学号或工号'}
  ],
  address: [
    {required: true, message: '请输入地址'}
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
  if (userStore.isAdmin) return
  
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

// 获取管理员统计数据
const fetchAdminStats = async () => {
  if (!userStore.isAdmin) return
  
  adminStatsLoading.value = true
  try {
    // 模拟数据，实际项目中应该从API获取
    adminStats.userCount = 100
    adminStats.bookCount = 500
    adminStats.borrowCount = 50
    adminStats.todayVisit = 200
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    adminStatsLoading.value = false
  }
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchBorrowHistory()
}

// 导航到指定页面
const navigateTo = (path) => {
  router.push(`/${path}`)
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
  userForm.realName = userStore.userInfo?.realName || ''
  userForm.email = userStore.userInfo?.email || ''
  userForm.phone = userStore.userInfo?.phone || ''
  userForm.department = userStore.userInfo?.department || ''
  userForm.position = userStore.userInfo?.position || ''
  userForm.studentId = userStore.userInfo?.studentId || ''
  userForm.address = userStore.userInfo?.address || ''

  // 获取数据
  if (userStore.isAdmin) {
    fetchAdminStats()
  } else {
    fetchBorrowHistory()
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  background: #f0f2f5;
  min-height: 100vh;
  padding: 30px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 卡片样式 */
.ant-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.ant-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 个人信息卡片 */
.mb-4:first-child {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

/* 头像样式 */
.ant-avatar {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.ant-avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 标题样式 */
h2.text-xl {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 12px 0 8px;
}

/* 用户信息样式 */
.user-info p {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.user-info p:hover {
  background: rgba(0, 0, 0, 0.02);
  padding-left: 10px;
  border-radius: 4px;
}

/* 统计卡片 */
.ant-card-title {
  font-weight: 600;
  color: #333;
}

/* 系统管理样式 */
.system-management {
  padding: 16px 0;
}

.setting-card {
  transition: all 0.3s ease;
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
}

.setting-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.setting-card .ant-card-cover {
  height: 160px;
  overflow: hidden;
  position: relative;
}

.setting-card .ant-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.setting-card:hover .ant-card-cover img {
  transform: scale(1.1);
}

.setting-card .ant-card-meta-title {
  font-weight: 600;
  color: #333;
}

.setting-card .ant-card-meta-description {
  color: #666;
}

/* 表单样式 */
.ant-form-item {
  margin-bottom: 16px;
}

.ant-form-item-label label {
  font-weight: 500;
  color: #333;
}

.ant-input {
  border-radius: 8px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.ant-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 按钮样式 */
.ant-btn {
  border-radius: 8px;
  padding: 8px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.ant-btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.ant-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 表格样式 */
.ant-table {
  border-radius: 8px;
  overflow: hidden;
}

.ant-table-tbody > tr:hover > td {
  background: rgba(0, 0, 0, 0.02);
}

/* 标签样式 */
.ant-tag {
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
}

/* 系统状态样式 */
.system-status {
  margin-top: 16px;
}

.ant-descriptions-bordered .ant-descriptions-item-label {
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-container {
    padding: 15px;
  }
  
  .setting-card .ant-card-cover {
    height: 120px;
  }
  
  .system-settings .ant-row {
    flex-direction: column;
  }
  
  .system-settings .ant-col {
    width: 100% !important;
    margin-bottom: 16px;
  }
  
  .ant-row {
    flex-direction: column;
  }
  
  .ant-col {
    width: 100% !important;
    margin-bottom: 16px;
  }
}

/* 加载动画 */
.ant-spin {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 标签页样式 */
.ant-tabs {
  border-radius: 8px;
  overflow: hidden;
}

.ant-tabs-nav {
  margin: 0;
  background: #f5f5f5;
  padding: 0 16px;
}

.ant-tabs-tab {
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.ant-tabs-tab:hover {
  color: #1890ff;
}

.ant-tabs-tab-active .ant-tabs-tab-btn {
  color: #1890ff;
  font-weight: 600;
}

.ant-tabs-ink-bar {
  background: #1890ff;
  height: 3px;
  border-radius: 3px;
}
</style>