<template>
  <div class="borrows-container">
    <a-card class="mb-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-bold">借阅管理</h2>
        <div>
          <a-radio-group v-model:value="viewMode" button-style="solid" class="mr-4">
            <a-radio-button v-if="!userStore.isAdmin" value="my">我的借阅</a-radio-button>
            <a-radio-button v-if="userStore.isAdmin" value="all">全部借阅</a-radio-button>
          </a-radio-group>
        </div>
      </div>

      <!-- 搜索区域 -->
      <div class="search-area mb-4">
        <div class="search-card">
          <a-form layout="inline" :model="searchForm" class="search-form">
            <a-form-item label="图书名称" class="search-item">
              <a-input v-model:value="searchForm.bookTitle" placeholder="请输入图书名称" allowClear/>
            </a-form-item>
            <a-form-item v-if="viewMode === 'all'" label="用户名" class="search-item">
              <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allowClear/>
            </a-form-item>
            <a-form-item label="状态" class="search-item">
              <a-select v-model:value="searchForm.status" placeholder="请选择状态" style="width: 120px" allowClear>
                <a-select-option v-for="option in statusOptions" :value="option.value" :key="option.value">
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item class="search-actions">
              <a-button type="primary" @click="handleSearch" class="search-button">
                <template #icon>
                  <SearchOutlined/>
                </template>
                查询
              </a-button>
              <a-button class="reset-button" @click="resetSearch">
                <template #icon>
                  <ReloadOutlined/>
                </template>
                重置
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 借阅列表 -->
      <a-table :columns="columns" :scroll="{x:1500}" :data-source="borrows" :pagination="pagination" :loading="loading"
               @change="handleTableChange" row-key="id" :bordered="true">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getBorrowStatusColor(record.status)">
              {{ getBorrowStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'fine'">
            <span v-if="record.fine > 0" class="text-red-500">
              ¥{{ record.fine.toFixed(2) }}
              <a-tag v-if="record.fineStatus === 1" color="red" size="small" class="ml-1">未缴</a-tag>
              <a-tag v-else-if="record.fineStatus === 2" color="green" size="small" class="ml-1">已缴</a-tag>
            </span>
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button v-if="record.status === 0" type="primary" size="small"
                        @click="handleRenew(record.id)">
                续借
              </a-button>
              <a-button v-if="record.status === 0 || record.status === 2" type="default" size="small"
                        @click="handleReturn(record.id)">
                归还
              </a-button>
              <a-button v-if="(record.status === 2 || record.status === 3) && record.fine > 0 && record.fineStatus === 1" type="danger" size="small"
                        @click="handlePayFine(record.id)">
                缴纳罚款
              </a-button>
              <a-button type="link" size="small" @click="handleViewDetail(record.id)">
                详情
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 借阅详情对话框 -->
    <a-modal title="借阅详情" :visible="borrowDetailVisible" @cancel="borrowDetailVisible = false" :footer=null
             width="600px">
      <div v-if="currentBorrow" class="borrow-detail">
        <h3 class="text-xl font-bold mb-4">{{ currentBorrow.bookTitle }}</h3>

        <div class="grid grid-cols-2 gap-4 mb-4">
          <div class="detail-item">
            <span class="text-gray-500">借阅人:</span>
            <span class="ml-2">{{ currentBorrow.username }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">借阅日期:</span>
            <span class="ml-2">{{
                currentBorrow.borrowTime ?
                    dayjs(currentBorrow.borrowTime).format('YYYY-MM-DD') :
                    '-'
              }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">应还日期:</span>
            <span class="ml-2">{{
                currentBorrow.returnTime ?
                    dayjs(currentBorrow.returnTime).format('YYYY-MM-DD') :
                    '-'
              }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">实际归还日期:</span>
            <span class="ml-2">{{
                currentBorrow.actualReturnTime ?
                    dayjs(currentBorrow.actualReturnTime).format('YYYY-MM-DD') : '-'
              }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">续借次数:</span>
            <span class="ml-2">{{ currentBorrow.renewCount || 0 }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">状态:</span>
            <a-tag :color="getBorrowStatusColor(currentBorrow.status)" class="ml-2">
              {{ getBorrowStatusText(currentBorrow.status) }}
            </a-tag>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">罚款:</span>
            <span v-if="currentBorrow.fine > 0" class="ml-2 text-red-500">
                ¥{{ currentBorrow.fine.toFixed(2) }}
                <a-tag v-if="currentBorrow.fineStatus === 1" color="red" class="ml-1">未缴纳</a-tag>
                <a-tag v-else-if="currentBorrow.fineStatus === 2" color="green" class="ml-1">已缴纳</a-tag>
            </span>
            <span v-else class="ml-2">-</span>
          </div>
        </div>

        <div class="detail-item mb-4" v-if="currentBorrow.renewDate">
          <span class="text-gray-500">续借日期:</span>
          <span class="ml-2">{{ currentBorrow.renewDate }}</span>
        </div>

        <div class="detail-item mb-4" v-if="currentBorrow.remarks">
          <span class="text-gray-500">备注:</span>
          <div class="mt-2 text-gray-700">{{ currentBorrow.remarks }}</div>
        </div>

        <div class="flex justify-end">
          <a-button v-if="currentBorrow.status === 0" type="primary" @click="handleRenew(currentBorrow.id)"
                    class="mr-2">
            续借
          </a-button>
          <a-button v-if="currentBorrow.status === 0 || currentBorrow.status === 2" type="default" @click="handleReturn(currentBorrow.id)"
                    class="mr-2">
            归还
          </a-button>
          <a-button v-if="(currentBorrow.status === 2 || currentBorrow.status === 3) && currentBorrow.fine > 0 && currentBorrow.fineStatus === 1" type="danger"
                    @click="handlePayFine(currentBorrow.id)" class="mr-2">
            缴纳罚款
          </a-button>
          <a-button @click="borrowDetailVisible = false">
            关闭
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, watch, computed} from 'vue'
import {useUserStore} from '@/store/user'
import {message} from 'ant-design-vue'
import {SearchOutlined, ReloadOutlined} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getMyBorrowList,
  getAllBorrowList,
  getBorrowDetail,
  returnBook,
  renewBook,
  payFine
} from '@/api/borrow'

const userStore = useUserStore()
const loading = ref(false)
const borrows = ref([])
const viewMode = ref(userStore.isAdmin ? 'all' : 'my')
const borrowDetailVisible = ref(false)
const currentBorrow = ref(null)

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
  bookTitle: '',
  username: '',
  status: undefined
})

// 表格列定义
const columns = computed(() => {
  const baseColumns = [
    {
      title: '图书名称',
      dataIndex: 'bookTitle',
      key: 'bookTitle',
      ellipsis: true
    },
    {
      title: '借阅日期',
      dataIndex: 'borrowTime',
      key: 'borrowTime',
      width: 120,
      customRender: ({text}) => text ? dayjs(text).format('YYYY-MM-DD') : '-'
    },
    {
      title: '应还日期',
      dataIndex: 'returnTime',
      key: 'returnTime',
      width: 120,
      customRender: ({text}) => text ? dayjs(text).format('YYYY-MM-DD') : '-'
    },
    {
      title: '实际归还日期',
      dataIndex: 'actualReturnTime',
      key: 'actualReturnTime',
      width: 120,
      customRender: ({text}) => text ? dayjs(text).format('YYYY-MM-DD') : '-'
    },
    {
      title: '续借次数',
      dataIndex: 'renewCount',
      key: 'renewCount',
      width: 100
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100
    },
    {
      title: '罚款',
      dataIndex: 'fine',
      key: 'fine',
      width: 100
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed:'right'
    }
  ]

  // 如果是管理员查看全部借阅，添加用户名列
  if (viewMode.value === 'all') {
    return [
      {
        title: '用户名',
        dataIndex: 'username',
        key: 'username',
        width: 120
      },
      ...baseColumns
    ]
  }

  return baseColumns
})

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
  return statusTexts[status] || '未知状态'
}

// 获取借阅列表
const fetchBorrows = async () => {
  loading.value = true
  try {
    // 准备请求参数
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      status: searchForm.status
    }
    
    let res
    if (viewMode.value === 'my') {
      // 我的借阅只需要状态参数
      res = await getMyBorrowList(params)
    } else {
      // 管理员查询 - 添加用户名和图书名为模糊搜索参数
      if (searchForm.username && searchForm.username.trim() !== '') {
        params.username = searchForm.username.trim();
      }
      
      if (searchForm.bookTitle && searchForm.bookTitle.trim() !== '') {
        params.bookTitle = searchForm.bookTitle.trim();
      }
      
      res = await getAllBorrowList(params)
    }

    borrows.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取借阅列表失败:', error)
    message.error(error.response?.data?.message || '获取借阅列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchBorrows()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'status' ? undefined : ''
  })
  pagination.current = 1
  fetchBorrows()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchBorrows()
}

// 查看借阅详情
const handleViewDetail = async (id) => {
  try {
    const res = await getBorrowDetail(id)
    currentBorrow.value = res.data
    borrowDetailVisible.value = true
  } catch (error) {
    console.error('获取借阅详情失败:', error)
    message.error('获取借阅详情失败，请稍后重试')
  }
}

// 续借
const handleRenew = async (id) => {
  try {
    await renewBook(id)
    message.success('续借成功')
    fetchBorrows()

    // 如果是在详情页续借，更新详情
    if (borrowDetailVisible.value && currentBorrow.value?.id === id) {
      const res = await getBorrowDetail(id)
      currentBorrow.value = res.data
    }
  } catch (error) {
    console.error('续借失败:', error)
    message.error(error.response?.data?.message || '续借失败，请稍后重试')
  }
}

// 归还
const handleReturn = async (id) => {
  try {
    await returnBook(id)
    message.success('归还成功')
    fetchBorrows()

    // 如果是在详情页归还，更新详情
    if (borrowDetailVisible.value && currentBorrow.value?.id === id) {
      const res = await getBorrowDetail(id)
      currentBorrow.value = res.data
    }
  } catch (error) {
    console.error('归还失败:', error)
    message.error(error.response?.data?.message || '归还失败，请稍后重试')
  }
}

// 缴纳罚款
const handlePayFine = async (id) => {
  try {
    await payFine(id)
    message.success('罚款缴纳成功')
    fetchBorrows()

    // 如果是在详情页缴纳罚款，更新详情
    if (borrowDetailVisible.value && currentBorrow.value?.id === id) {
      const res = await getBorrowDetail(id)
      currentBorrow.value = res.data
    }
  } catch (error) {
    console.error('缴纳罚款失败:', error)
    message.error(error.response?.data?.message || '缴纳罚款失败，请稍后重试')
  }
}

// 监听视图模式变化
watch(viewMode, () => {
  // 切换视图模式时重置搜索条件和分页
  resetSearch()
})

onMounted(() => {
  fetchBorrows()
})

// 在模板中修改状态选择器的选项
const statusOptions = [
  {value: 0, label: '借阅中'},
  {value: 1, label: '已归还'},
  {value: 2, label: '已逾期未还'},
  {value: 3, label: '已逾期已还'}
]
</script>

<style scoped>
.borrows-container {
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
}

.borrow-detail .detail-item {
  margin-bottom: 8px;
}

.borrows-container :deep(.ant-table-bordered .ant-table-cell) {
  border-color: #b4d3f7;
}

.borrows-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
  border-color: #b4d3f7;
}

.borrows-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
  border-color: #b4d3f7;
}

/* 搜索区域样式 */
.search-card {
  background: #f0f8ff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-item {
  margin-bottom: 0;
}

.search-actions {
  margin-bottom: 0;
  display: flex;
  gap: 8px;
}

.search-button {
  background-color: #1890ff;
  border-color: #1890ff;
  transition: all 0.3s ease;
}

.search-button:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

.reset-button {
  transition: all 0.3s ease;
}

.reset-button:hover {
  border-color: #1890ff;
  color: #1890ff;
}

/* 表格样式 */
.borrows-container :deep(.ant-table) {
  border-radius: 8px;
  overflow: hidden;
}

.borrows-container :deep(.ant-table-thead > tr > th) {
  background-color: #f0f8ff;
  font-weight: 600;
}

.borrows-container :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f0f8ff;
}

.borrows-container :deep(.ant-table-tbody > tr > td) {
  transition: background-color 0.3s ease;
}
</style>