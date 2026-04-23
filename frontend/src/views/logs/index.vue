<template>
    <div class="logs-container">
        <a-card>
            <!-- 搜索区域 -->
            <div class="search-area mb-4">
                <div class="search-card">
                    <a-form layout="inline" :model="searchForm" class="search-form">
                        <a-form-item label="图书名称" class="search-item">
                            <a-input v-model:value="searchForm.bookTitle" placeholder="请输入图书名称" allowClear />
                        </a-form-item>
                        <a-form-item label="用户名" class="search-item">
                            <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allowClear />
                        </a-form-item>
                        <a-form-item label="状态" class="search-item">
                            <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
                                <a-select-option :value="0">借阅中</a-select-option>
                                <a-select-option :value="1">已归还</a-select-option>
                                <a-select-option :value="2">已逾期未还</a-select-option>
                                <a-select-option :value="3">已逾期已还</a-select-option>
                            </a-select>
                        </a-form-item>
                        <a-form-item label="借阅时间" class="search-item">
                            <a-range-picker v-model:value="dateRange" show-time format="YYYY-MM-DD HH:mm:ss" />
                        </a-form-item>
                        <a-form-item class="search-actions">
                            <a-button type="primary" @click="handleSearch" class="search-button">
                                <template #icon>
                                    <SearchOutlined />
                                </template>
                                查询
                            </a-button>
                            <a-button class="reset-button" @click="handleReset">
                                <template #icon>
                                    <ReloadOutlined />
                                </template>
                                重置
                            </a-button>
                        </a-form-item>
                    </a-form>
                </div>
            </div>

            <!-- 工具栏 -->
            <div class="table-toolbar mb-4">
                <h2 class="text-xl font-bold">最近借阅</h2>
                <span class="text-gray-500 ml-2">{{ borrows.length }}条记录</span>
            </div>
            <!-- 借阅表格 -->
            <a-table :columns="columns" :data-source="borrows" :loading="loading" :pagination="pagination" :row-key="record => record.id" @change="handleTableChange" :scroll="{ x: 1000 }" :bordered="true">
                <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'status'">
                        <a-tag :color="getBorrowStatusColor(record.status)">
                            {{ getBorrowStatusText(record.status) }}
                        </a-tag>
                    </template>
                    <template v-if="column.key === 'action'">
                        <a-space>
                            <a-button v-if="record.status === 0" type="primary" size="small" @click="handleRenew(record.id)">
                                续借
                            </a-button>
                            <a-button v-if="record.status === 0 || record.status === 2" type="default" size="small" @click="handleReturn(record.id)">
                                归还
                            </a-button>
                            <a-button v-if="(record.status === 2 || record.status === 3) && record.fine > 0 && record.fineStatus === 1" type="danger" size="small" @click="handlePayFine(record.id)">
                                缴纳罚款
                            </a-button>
                            <a-button type="link" size="small" @click="handleViewDetail(record.id)">
                                详情
                            </a-button>
                        </a-space>
                    </template>
                </template>
            </a-table>

            <!-- 借阅详情对话框 -->
            <a-modal title="借阅详情" :visible="borrowDetailVisible" @cancel="borrowDetailVisible = false" :footer=null width="600px">
                <div v-if="currentBorrow" class="borrow-detail">
                    <h3 class="text-xl font-bold mb-4">{{ currentBorrow.bookTitle }}</h3>

                    <div class="grid grid-cols-2 gap-4 mb-4">
                        <div class="detail-item">
                            <span class="text-gray-500">借阅人:</span>
                            <span class="ml-2">{{ currentBorrow.username }}</span>
                        </div>

                        <div class="detail-item">
                            <span class="text-gray-500">借阅日期:</span>
                            <span class="ml-2">{{ currentBorrow.borrowTime ? dayjs(currentBorrow.borrowTime).format('YYYY-MM-DD') : '-' }}</span>
                        </div>

                        <div class="detail-item">
                            <span class="text-gray-500">应还日期:</span>
                            <span class="ml-2">{{ currentBorrow.returnTime ? dayjs(currentBorrow.returnTime).format('YYYY-MM-DD') : '-' }}</span>
                        </div>

                        <div class="detail-item">
                            <span class="text-gray-500">实际归还日期:</span>
                            <span class="ml-2">{{ currentBorrow.actualReturnTime ? dayjs(currentBorrow.actualReturnTime).format('YYYY-MM-DD') : '-' }}</span>
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

                    <div class="flex justify-end">
                        <a-button v-if="currentBorrow.status === 0" type="primary" @click="handleRenew(currentBorrow.id)" class="mr-2">
                            续借
                        </a-button>
                        <a-button v-if="currentBorrow.status === 0 || currentBorrow.status === 2" type="default" @click="handleReturn(currentBorrow.id)" class="mr-2">
                            归还
                        </a-button>
                        <a-button v-if="(currentBorrow.status === 2 || currentBorrow.status === 3) && currentBorrow.fine > 0 && currentBorrow.fineStatus === 1" type="danger" @click="handlePayFine(currentBorrow.id)" class="mr-2">
                            缴纳罚款
                        </a-button>
                        <a-button @click="borrowDetailVisible = false">
                            关闭
                        </a-button>
                    </div>
                </div>
            </a-modal>
        </a-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
    SearchOutlined,
    ReloadOutlined
} from '@ant-design/icons-vue'
import { getMyBorrowList, getAllBorrowList, getBorrowDetail, returnBook, renewBook, payFine } from '@/api/borrow'
import dayjs from 'dayjs'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
    bookTitle: '',
    username: '',
    status: undefined,
    startTime: null,
    endTime: null
})

// 分页配置
const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`
})

// 借阅数据
const borrows = ref([])

// 借阅详情
const currentBorrow = ref(null)
const borrowDetailVisible = ref(false)

// 借阅表格列
const columns = [
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
        customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD') : '-'
    },
    {
        title: '应还日期',
        dataIndex: 'returnTime',
        key: 'returnTime',
        width: 120,
        customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD') : '-'
    },
    {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 100
    },
    {
        title: '操作',
        key: 'action',
        width: 200,
        fixed: 'right'
    }
]

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
        const params = {
            current: pagination.current,
            size: pagination.pageSize,
            status: searchForm.status
        }
        
        if (searchForm.bookTitle) {
            params.bookTitle = searchForm.bookTitle
        }
        
        if (searchForm.username) {
            params.username = searchForm.username
        }
        
        if (searchForm.startTime) {
            params.startTime = dayjs(searchForm.startTime).format('YYYY-MM-DD HH:mm:ss')
        }
        
        if (searchForm.endTime) {
            params.endTime = dayjs(searchForm.endTime).format('YYYY-MM-DD HH:mm:ss')
        }

        const res = await getAllBorrowList(params)
        borrows.value = res.data.records
        pagination.total = res.data.total
    } catch (error) {
        console.error('获取借阅列表失败:', error)
        message.error('获取借阅列表失败')
    } finally {
        loading.value = false
    }
}

// 处理查询
const handleSearch = () => {
    pagination.current = 1
    fetchBorrows()
}

// 处理重置
const handleReset = () => {
    searchForm.bookTitle = ''
    searchForm.username = ''
    searchForm.status = undefined
    searchForm.startTime = null
    searchForm.endTime = null
    handleSearch()
}

// 处理表格变化
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
        message.error('获取借阅详情失败')
    }
}

// 续借
const handleRenew = async (id) => {
    try {
        await renewBook(id)
        message.success('续借成功')
        fetchBorrows()
    } catch (error) {
        console.error('续借失败:', error)
        message.error('续借失败')
    }
}

// 归还
const handleReturn = async (id) => {
    try {
        await returnBook(id)
        message.success('归还成功')
        fetchBorrows()
    } catch (error) {
        console.error('归还失败:', error)
        message.error('归还失败')
    }
}

// 缴纳罚款
const handlePayFine = async (id) => {
    try {
        await payFine(id)
        message.success('罚款缴纳成功')
        fetchBorrows()
    } catch (error) {
        console.error('缴纳罚款失败:', error)
        message.error('缴纳罚款失败')
    }
}

// 日期范围
const dateRange = computed({
    get: () => [searchForm.startTime, searchForm.endTime],
    set: (val) => {
        searchForm.startTime = val?.[0] || null
        searchForm.endTime = val?.[1] || null
    }
})

// 初始化
fetchBorrows()
</script>

<style scoped>
.logs-container {
    max-width: 1200px;
    margin: 0 auto;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    min-height: 100vh;
    padding: 24px;
    overflow-x: hidden;
}

.mb-4 {
    margin-bottom: 24px;
}

.ml-2 {
    margin-left: 8px;
}

.table-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

/* 搜索区域美化 */
.search-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
}

.search-card:hover {
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
}

.search-form {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16px;
}

.search-item {
    margin-bottom: 0;
}

.search-actions {
    margin-bottom: 0;
    display: flex;
    align-items: center;
    gap: 12px;
}

.search-button {
    background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
    border: none;
    border-radius: 8px;
    padding: 0 24px;
    height: 40px;
    font-weight: 500;
    transition: all 0.3s ease;
}

.search-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.reset-button {
    border-radius: 8px;
    padding: 0 24px;
    height: 40px;
    transition: all 0.3s ease;
}

.reset-button:hover {
    border-color: #1890ff;
    color: #1890ff;
    transform: translateY(-1px);
}

/* 确保表格不会溢出容器 */
:deep(.ant-table-wrapper) {
    width: 100%;
    overflow-x: auto;
}

.borrow-detail .detail-item {
    margin-bottom: 8px;
}

.logs-container :deep(.ant-table-bordered .ant-table-cell) {
    border-color: #b4d3f7;
}

.logs-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
    border-color: #b4d3f7;
}

.logs-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
    border-color: #b4d3f7;
    background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
}

/* 表格行悬停效果 */
.logs-container :deep(.ant-table-tbody > tr:hover > td) {
    background-color: #f0f9ff;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .search-form {
        flex-direction: column;
        align-items: stretch;
    }
    
    .search-item {
        width: 100%;
    }
    
    .search-actions {
        justify-content: flex-end;
    }
    
    .table-toolbar {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }
}
</style>