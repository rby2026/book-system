<template>
  <div class="books-container">
    <a-card class="mb-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-bold">图书管理</h2>
        <a-button v-if="userStore.isAdmin" type="primary" @click="showAddBookModal">
          <plus-outlined />
          添加图书
        </a-button>
      </div>

      <!-- 搜索区域 -->
      <div class="search-area mb-4">
        <div class="search-card">
          <a-form layout="inline" :model="searchForm" class="search-form">
            <a-form-item label="书名" class="search-item">
              <a-input v-model:value="searchForm.title" placeholder="请输入书名" allowClear />
            </a-form-item>
            <a-form-item label="作者" class="search-item">
              <a-input v-model:value="searchForm.author" placeholder="请输入作者" allowClear />
            </a-form-item>
            <a-form-item label="ISBN" class="search-item">
              <a-input v-model:value="searchForm.isbn" placeholder="请输入ISBN" allowClear />
            </a-form-item>
            <a-form-item class="search-actions">
              <a-button type="primary" @click="handleSearch" class="search-button">
                <template #icon>
                  <SearchOutlined />
                </template>
                查询
              </a-button>
              <a-button class="reset-button" @click="resetSearch">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 在工具栏中添加扫描按钮 -->
      <div class="table-toolbar mb-4">
        <a-space>
          <a-button type="primary" @click="showScanner = true" v-if="userStore.isAdmin">
            <template #icon>
              <ScanOutlined/>
            </template>
            扫描录入
          </a-button>
        </a-space>
      </div>

      <!-- 图书列表 -->
      <a-table :columns="columns" :scroll="{x:1500}" :data-source="books" :pagination="pagination" :loading="loading"
        @change="handleTableChange" row-key="id" :bordered="true">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 && record.availableCount > 0 ? 'green' : 'red'">
              {{ record.status === 1 && record.availableCount > 0 ? '可借阅' : 
                 record.status === 1 && record.availableCount === 0 ? '已借完' : '不可借阅' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="primary" size="small" @click="handleViewBook(record)" ghost>
                查看
              </a-button>
              <a-button v-if="record.status === 1 && record.availableCount > 0 && !userStore.isAdmin" type="primary" size="small" @click="handleBorrowBook(record.id)">
                借阅
              </a-button>
              <a-button v-if="userStore.isAdmin" type="default" size="small" @click="handleBorrowInfo(record.id)">
                借阅信息
              </a-button>
              <a-button v-if="userStore.isAdmin" type="default" size="small" @click="handleEditBook(record)">
                编辑
              </a-button>
              <a-popconfirm v-if="userStore.isAdmin" title="确定要删除这本书吗?" ok-text="确定" cancel-text="取消"
                @confirm="handleDeleteBook(record.id)">
                <a-button type="danger" size="small" ghost>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑图书对话框 -->
    <a-modal :title="isEdit ? '编辑图书' : '添加图书'" :visible="bookModalVisible" :confirm-loading="confirmLoading"
      @ok="handleBookModalOk" @cancel="handleBookModalCancel" width="600px">
      <a-form :model="bookForm" :rules="rules" ref="bookFormRef" layout="vertical">
        <a-form-item name="title" label="书名">
          <a-input v-model:value="bookForm.title" placeholder="请输入书名" />
        </a-form-item>

        <a-form-item name="author" label="作者">
          <a-input v-model:value="bookForm.author" placeholder="请输入作者" />
        </a-form-item>

        <a-form-item name="isbn" label="ISBN" required>
          <a-input-search v-model:value="bookForm.isbn" placeholder="请输入ISBN" enter-button="查询"
            @search="handleIsbnSearch" />
        </a-form-item>

        <a-form-item name="publisher" label="出版社">
          <a-input v-model:value="bookForm.publisher" placeholder="请输入出版社" />
        </a-form-item>

        <a-form-item name="location" label="位置">
          <a-input v-model:value="bookForm.location" placeholder="请输入图书位置" />
        </a-form-item>

        <a-form-item name="category" label="分类">
          <a-select v-model:value="bookForm.category" placeholder="请选择分类">
            <a-select-option value="LITERATURE">文学</a-select-option>
            <a-select-option value="FICTION">小说</a-select-option>
            <a-select-option value="SCIENCE">科学</a-select-option>
            <a-select-option value="HISTORY">历史</a-select-option>
            <a-select-option value="TECHNOLOGY">技术</a-select-option>
            <a-select-option value="ART">艺术</a-select-option>
            <a-select-option value="PHILOSOPHY">哲学</a-select-option>
            <a-select-option value="ECONOMICS">经济</a-select-option>
            <a-select-option value="EDUCATION">教育</a-select-option>
            <a-select-option value="MEDICINE">医学</a-select-option>
            <a-select-option value="LAW">法律</a-select-option>
            <a-select-option value="POLITICS">政治</a-select-option>
            <a-select-option value="MILITARY">军事</a-select-option>
            <a-select-option value="SPORTS">体育</a-select-option>
            <a-select-option value="ENTERTAINMENT">娱乐</a-select-option>
            <a-select-option value="BIOGRAPHY">传记</a-select-option>
            <a-select-option value="CHILDREN">儿童读物</a-select-option>
            <a-select-option value="COMICS">漫画</a-select-option>
            <a-select-option value="COOKING">烹饪</a-select-option>
            <a-select-option value="TRAVEL">旅游</a-select-option>
            <a-select-option value="RELIGION">宗教</a-select-option>
            <a-select-option value="REFERENCE">参考书</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item name="totalCount" label="总数量">
          <a-input-number v-model:value="bookForm.totalCount" :min="1" placeholder="请输入总数量" style="width: 100%" />
        </a-form-item>

        <a-form-item name="description" label="描述">
          <a-textarea v-model:value="bookForm.description" placeholder="请输入图书描述" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 图书详情对话框 -->
    <a-modal title="图书详情" :visible="bookDetailVisible" @cancel="bookDetailVisible = false" :footer=null width="600px">
      <div v-if="currentBook" class="book-detail">
        <h3 class="text-xl font-bold mb-4">{{ currentBook.title }}</h3>

        <div class="grid grid-cols-2 gap-4 mb-4">
          <div class="detail-item">
            <span class="text-gray-500">作者:</span>
            <span class="ml-2">{{ currentBook.author }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">ISBN:</span>
            <span class="ml-2">{{ currentBook.isbn }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">出版社:</span>
            <span class="ml-2">{{ currentBook.publisher }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">位置:</span>
            <span class="ml-2">{{ currentBook.location || '未设置' }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">分类:</span>
            <span class="ml-2">{{ getCategoryText(currentBook.category) }}</span>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">状态:</span>
            <a-tag :color="currentBook.status === 1 && currentBook.availableCount > 0 ? 'green' : 'red'" class="ml-2">
              {{ currentBook.status === 1 && currentBook.availableCount > 0 ? '可借阅' : 
                 currentBook.status === 1 && currentBook.availableCount === 0 ? '已借完' : '不可借阅' }}
            </a-tag>
          </div>

          <div class="detail-item">
            <span class="text-gray-500">可借数量:</span>
            <span class="ml-2">{{ currentBook.availableCount }}/{{ currentBook.totalCount }}</span>
          </div>
        </div>

        <div class="detail-item mb-4">
          <span class="text-gray-500">描述:</span>
          <div class="mt-2 text-gray-700">{{ currentBook.description || '暂无描述' }}</div>
        </div>

        <div class="flex justify-end">
          <a-button v-if="currentBook.status === 1 && currentBook.availableCount > 0 && !userStore.isAdmin" type="primary" @click="handleBorrowBook(currentBook.id)">
            借阅
          </a-button>
          <a-button v-if="userStore.isAdmin" type="default" @click="handleBorrowInfo(currentBook.id)" class="ml-2">
            借阅信息
          </a-button>
          <a-button @click="bookDetailVisible = false" class="ml-2">
            关闭
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 扫描器组件 -->
    <barcode-scanner v-model:visible="showScanner" @scan="handleScan" />

    <!-- 借阅信息对话框 -->
    <a-modal title="借阅信息" :visible="borrowInfoVisible" @cancel="borrowInfoVisible = false" @ok="borrowInfoVisible = false" width="800px">
      <a-table :columns="borrowInfoColumns" :data-source="borrowInfos" :pagination="false" :loading="borrowInfoLoading" bordered>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 0 ? 'blue' : record.status === 1 ? 'green' : 'red'">
              {{ record.status === 0 ? '已借阅' : record.status === 1 ? '已归还' : '逾期' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'fineStatus'">
            <a-tag :color="record.fineStatus === 0 ? 'red' : 'green'">
              {{ record.fineStatus === 0 ? '未缴纳' : '已缴纳' }}
            </a-tag>
          </template>
        </template>
      </a-table>
      <div v-if="borrowInfos.length === 0" class="text-center py-4">
        暂无借阅记录
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ScanOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import {
  getBookList,
  addBook,
  updateBook,
  deleteBook,
  getBookDetail,
  getBookByIsbn
} from '@/api/book'
import { borrowBook, getBorrowListByBookId } from '@/api/borrow'
import dayjs from 'dayjs'
import BarcodeScanner from '@/components/BarcodeScanner.vue'
import { getBookInfoByIsbn } from '@/utils/book'

const userStore = useUserStore()
const loading = ref(false)
const books = ref([])
const bookFormRef = ref(null)
const bookModalVisible = ref(false)
const bookDetailVisible = ref(false)
const confirmLoading = ref(false)
const isEdit = ref(false)
const currentBook = ref(null)
const showScanner = ref(false)

// 借阅信息
const borrowInfoVisible = ref(false)
const borrowInfoLoading = ref(false)
const borrowInfos = ref([])

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
  title: '',
  author: '',
  isbn: ''
})

// 图书表单
const bookForm = reactive({
  id: null,
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  location: '',
  category: undefined,
  totalCount: 1,
  description: ''
})

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入书名' }],
  author: [{ required: true, message: '请输入作者' }],
  isbn: [{ required: true, message: '请输入ISBN' }],
  publisher: [{ required: true, message: '请输入出版社' }],
  location: [{ required: true, message: '请输入图书位置' }],
  category: [{ required: true, message: '请选择分类' }],
  totalCount: [{ required: true, message: '请输入总数量' }]
}

// 表格列定义
const columns = [
  {
    title: '书名',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '作者',
    dataIndex: 'author',
    key: 'author'
  },
  {
    title: 'ISBN',
    dataIndex: 'isbn',
    key: 'isbn'
  },
  {
    title: '出版社',
    dataIndex: 'publisher',
    key: 'publisher',
    ellipsis: true
  },
  {
    title: '位置',
    dataIndex: 'location',
    key: 'location'
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
    customRender: ({ text }) => getCategoryText(text)
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status'
  },
  {
    title: '操作',
    width: 300,
    key: 'action',
    fixed:'right'
  }
]

// 借阅信息表格列定义
const borrowInfoColumns = [
  {
    title: '借阅用户',
    dataIndex: 'username',
    key: 'username',
    customRender: ({ text, record }) => record.username || record.realName || ''
  },
  {
    title: '借阅时间',
    dataIndex: 'borrowTime',
    key: 'borrowTime',
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : ''
  },
  {
    title: '应还时间',
    dataIndex: 'returnTime',
    key: 'returnTime',
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : ''
  },
  {
    title: '实际还书时间',
    dataIndex: 'actualReturnTime',
    key: 'actualReturnTime',
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : '未归还'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status'
  },
  {
    title: '罚款状态',
    dataIndex: 'fineStatus',
    key: 'fineStatus'
  },
  {
    title: '罚款金额',
    dataIndex: 'fine',
    key: 'fine',
    customRender: ({ text }) => text ? `¥${text.toFixed(2)}` : '¥0.00'
  }
]

// 获取分类文本
const getCategoryText = (category) => {
  const categoryMap = {
    'LITERATURE': '文学',
    'FICTION': '小说',
    'SCIENCE': '科学',
    'HISTORY': '历史',
    'TECHNOLOGY': '技术',
    'ART': '艺术',
    'PHILOSOPHY': '哲学',
    'ECONOMICS': '经济',
    'EDUCATION': '教育',
    'MEDICINE': '医学',
    'LAW': '法律',
    'POLITICS': '政治',
    'MILITARY': '军事',
    'SPORTS': '体育',
    'ENTERTAINMENT': '娱乐',
    'BIOGRAPHY': '传记',
    'CHILDREN': '儿童读物',
    'COMICS': '漫画',
    'COOKING': '烹饪',
    'TRAVEL': '旅游',
    'RELIGION': '宗教',
    'REFERENCE': '参考书',
    'OTHER': '其他'
  }
  return categoryMap[category] || category
}

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    }

    const res = await getBookList(params)
    books.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取图书列表失败:', error)
    message.error('获取图书列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchBooks()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.current = 1
  fetchBooks()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchBooks()
}

// 显示添加图书对话框
const showAddBookModal = () => {
  isEdit.value = false
  resetBookForm()
  bookModalVisible.value = true
}

// 重置图书表单
const resetBookForm = () => {
  Object.keys(bookForm).forEach(key => {
    bookForm[key] = key === 'id' ? null : ''
  })
  bookForm.category = undefined

  if (bookFormRef.value) {
    bookFormRef.value.resetFields()
  }
}

// 处理图书对话框确认
const handleBookModalOk = () => {
  bookFormRef.value.validate().then(async () => {
    confirmLoading.value = true
    try {
      const formData = { ...bookForm }

      if (isEdit.value) {
        await updateBook(formData)
        message.success('更新图书成功')
      } else {
        await addBook(formData)
        message.success('添加图书成功')
      }

      bookModalVisible.value = false
      fetchBooks()
    } catch (error) {
      console.error(isEdit.value ? '更新图书失败:' : '添加图书失败:', error)
      message.error(isEdit.value ? '更新图书失败，请稍后重试' : '添加图书失败，请稍后重试')
    } finally {
      confirmLoading.value = false
    }
  })
}

// 处理图书对话框取消
const handleBookModalCancel = () => {
  bookModalVisible.value = false
  resetBookForm()
}

// 处理编辑图书
const handleEditBook = (record) => {
  isEdit.value = true

  // 填充表单数据
  Object.keys(bookForm).forEach(key => {
    bookForm[key] = record[key] || (key === 'totalCount' ? 1 : '')
  })

  bookModalVisible.value = true
}

// 处理查看图书
const handleViewBook = async (record) => {
  try {
    const res = await getBookDetail(record.id)
    currentBook.value = res.data
    bookDetailVisible.value = true
  } catch (error) {
    console.error('获取图书详情失败:', error)
    message.error('获取图书详情失败，请稍后重试')
  }
}

// 处理删除图书
const handleDeleteBook = async (id) => {
  try {
    await deleteBook(id)
    message.success('删除图书成功')
    fetchBooks()
  } catch (error) {
    console.error('删除图书失败:', error)
    message.error('删除图书失败，请稍后重试')
  }
}

// 处理借阅图书
const handleBorrowBook = async (id) => {
  try {
    // 先检查图书状态
    const bookDetailRes = await getBookDetail(id);
    const book = bookDetailRes.data;
    
    if (book.status !== 1) {
      message.error('该图书不可借阅');
      return;
    }
    
    if (book.availableCount <= 0) {
      message.error('该图书已全部借出');
      return;
    }
    
    // 执行借阅操作
    await borrowBook(id)
    message.success('借阅成功')
    fetchBooks()

    // 如果是在详情页借阅，关闭详情页
    if (bookDetailVisible.value) {
      bookDetailVisible.value = false
    }
  } catch (error) {
    console.error('借阅失败:', error)
    if (error.response && error.response.data && error.response.data.message) {
      message.error('借阅失败：' + error.response.data.message)
    } else {
      message.error('借阅失败，请稍后重试')
    }
  }
}

// 处理查看借阅信息
const handleBorrowInfo = async (bookId) => {
  try {
    borrowInfoLoading.value = true
    const res = await getBorrowListByBookId(bookId)
    borrowInfos.value = res.data.records || []
    // 关闭图书详情对话框，避免重叠
    bookDetailVisible.value = false
    borrowInfoVisible.value = true
  } catch (error) {
    console.error('获取借阅信息失败:', error)
    message.error('获取借阅信息失败，请稍后重试')
  } finally {
    borrowInfoLoading.value = false
  }
}

// 添加处理扫描结果的方法
const handleScan = async (isbn) => {
  try {
    // 查询ISBN是否已存在
    const res = await getBookByIsbn(isbn)
    if (res.data) {
      message.warning('该图书已存在')
      return
    }

    // 获取图书信息
    const bookInfo = await getBookInfoByIsbn(isbn)
    if (bookInfo) {
      Object.assign(bookForm, bookInfo)
      message.success('获取图书信息成功')
    } else {
      bookForm.isbn = isbn
      message.warning('获取图书信息失败，请手动填写')
    }

    showScanner.value = false
    showAddBookModal()
  } catch (error) {
    console.error('查询图书失败:', error)
    message.error('查询图书失败')
  }
}

// 添加handleIsbnSearch方法
const handleIsbnSearch = async (isbn) => {
  if (!isbn) {
    message.warning('请输入ISBN')
    return
  }

  try {
    // 获取图书信息
    const bookInfo = await getBookInfoByIsbn(isbn)
    if (bookInfo) {
      Object.assign(bookForm, bookInfo)
      message.success('获取图书信息成功')
    } else {
      message.warning('获取图书信息失败')
    }
  } catch (error) {
    console.error('获取图书信息失败:', error)
    message.error('获取图书信息失败')
  }
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.books-container {
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
}

.book-detail .detail-item {
  margin-bottom: 8px;
}

.books-container :deep(.ant-table-bordered .ant-table-cell) {
  border-color: #b4d3f7;
}

.books-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
  border-color: #b4d3f7;
}

.books-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
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
.books-container :deep(.ant-table) {
  border-radius: 8px;
  overflow: hidden;
}

.books-container :deep(.ant-table-thead > tr > th) {
  background-color: #f0f8ff;
  font-weight: 600;
}

.books-container :deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f0f8ff;
}

.books-container :deep(.ant-table-tbody > tr > td) {
  transition: background-color 0.3s ease;
}
</style>