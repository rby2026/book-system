<template>
    <div class="dashboard-container">
        <!-- 欢迎信息 -->
        <div class="welcome-message">
            <span class="welcome-text">你好，欢迎登录</span>
        </div>
        
        <a-row :gutter="16" class="mb-4">
            <!-- 统计卡片 -->
            <a-col :span="6">
                <a-card>
                    <statistic-card title="当前借阅数" :value="statistics.currentBorrowCount" icon="book" color="#1890ff" />
                </a-card>
            </a-col>

            <a-col :span="6">
                <a-card>
                    <statistic-card title="总借阅数" :value="statistics.totalBorrowCount" icon="swap" color="#52c41a" />
                </a-card>
            </a-col>

            <a-col :span="6">
                <a-card>
                    <statistic-card title="逾期数量" :value="statistics.overdueCount" icon="warning" color="#fa8c16" />
                </a-card>
            </a-col>

            <a-col :span="6">
                <a-card>
                    <statistic-card title="总罚款金额" :value="statistics.totalFine ? `¥${statistics.totalFine}` : '¥0'"
                        icon="dollar" color="#722ed1" />
                </a-card>
            </a-col>

            <a-col :span="6">
              <a-card>
                <statistic-card title="已归还未缴纳罚款人数" :value="statistics.unpaidFineCount " icon="warning"  color="#722ed1" />
              </a-card>
            </a-col>

            <a-col :span="6">
              <a-card>
                <statistic-card title="新用户注册数量" :value="statistics.newUserCount" icon="user" color="#fa8c16" />
              </a-card>
            </a-col>

            <a-col :span="6">
              <a-card>
                <statistic-card title="图书馆书本总数" :value="statistics.totalBookCount" icon="book" color="#52c41a" />
              </a-card>
            </a-col>

            <a-col :span="6">
              <a-card>
                <statistic-card title="系统时间" :value="formatDateTime(currentTime)" icon="clock-circle" color="#1890ff" />
              </a-card>
            </a-col>
        </a-row>

        <!-- 智能推荐 -->
        <a-collapse class="mb-4" :active-key="activeKey" @change="handleCollapseChange">
            <a-collapse-panel key="1" header="智能推荐" :extra="`${recommendedBooks.length}本图书`">
                <a-row :gutter="16" :loading="loading">
                    <a-col :span="8" v-for="(item, index) in recommendedBooks" :key="item.id" class="mb-4">
                        <a-card :hoverable="true" @click="showBookDetail(item)" class="book-card">
                            <div class="book-avatar" :style="{ backgroundColor: getRandomColor(item) }">
                                {{ getFirstLetter(item) }}
                            </div>
                            <div class="book-info">
                                <h3 class="book-title">{{ item.title }}</h3>
                                <p class="book-author">作者: {{ item.author }}</p>
                                <p class="book-category">分类: {{ item.category }}</p>
                                <a-tag :color="item.status === 1 ? 'green' : 'red'" class="book-status">
                                    {{ item.status === 1 ? '可借阅' : '已借出' }}
                                </a-tag>
                            </div>
                        </a-card>
                    </a-col>
                </a-row>
            </a-collapse-panel>

            <!-- 热门图书 -->
            <a-collapse-panel key="2" header="热门图书" :extra="`${hotBooks.length}本图书`">
                <a-row :gutter="16" :loading="loading">
                    <a-col :span="8" v-for="(item, index) in hotBooks" :key="item.id" class="mb-4">
                        <a-card :hoverable="true" @click="showBookDetail(item)" class="book-card">
                            <div class="book-avatar" :style="{ backgroundColor: getRandomColor(item) }">
                                {{ getFirstLetter(item) }}
                            </div>
                            <div class="book-info">
                                <h3 class="book-title">{{ item.title }}</h3>
                                <p class="book-author">作者: {{ item.author }}</p>
                                <p class="book-count">借阅次数: {{ item.borrowCount }}</p>
                                <a-tag :color="item.status === 1 ? 'green' : 'red'" class="book-status">
                                    {{ item.status === 1 ? '可借阅' : '已借出' }}
                                </a-tag>
                            </div>
                        </a-card>
                    </a-col>
                </a-row>
            </a-collapse-panel>

            <!-- 新书推荐 -->
            <a-collapse-panel key="3" header="新书推荐" :extra="`${newBooks.length}本图书`">
                <a-row :gutter="16" :loading="loading">
                    <a-col :span="8" v-for="(item, index) in newBooks" :key="item.id" class="mb-4">
                        <a-card :hoverable="true" @click="showBookDetail(item)" class="book-card">
                            <div class="book-avatar" :style="{ backgroundColor: getRandomColor(item) }">
                                {{ getFirstLetter(item) }}
                            </div>
                            <div class="book-info">
                                <h3 class="book-title">{{ item.title }}</h3>
                                <p class="book-author">作者: {{ item.author }}</p>
                                <p class="book-date">入库时间: {{ item.stockDate ? item.stockDate.substring(0, 10) : '-' }}</p>
                                <a-tag :color="item.status === 1 ? 'green' : 'red'" class="book-status">
                                    {{ item.status === 1 ? '可借阅' : '已借出' }}
                                </a-tag>
                            </div>
                        </a-card>
                    </a-col>
                </a-row>
            </a-collapse-panel>

            <!-- 最近借阅 -->
            <a-collapse-panel key="4" header="最近借阅" :extra="`${recentBorrows.length}条记录`">
                <a-table :columns="borrowColumns" :data-source="recentBorrows" :pagination="false" size="small">
                    <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'status'">
                            <a-tag :color="getBorrowStatusColor(record.status)">
                                {{ getBorrowStatusText(record.status) }}
                            </a-tag>
                        </template>
                        <template v-else-if="column.key === 'bookTitle'">
                            <a @click="showBookDetailByTitle(record.bookTitle)">{{ record.bookTitle }}</a>
                        </template>
                    </template>
                </a-table>
            </a-collapse-panel>
        </a-collapse>

        <!-- 图书详情模态框 -->
        <a-modal
            v-model:visible="bookDetailVisible"
            :title="selectedBook.title || '图书详情'"
            width="600px"
        >
            <div v-if="selectedBook" class="book-detail">
                <a-descriptions bordered column="1">
                    <a-descriptions-item label="书名">{{ selectedBook.title }}</a-descriptions-item>
                    <a-descriptions-item label="作者">{{ selectedBook.author }}</a-descriptions-item>
                    <a-descriptions-item label="分类">{{ selectedBook.category }}</a-descriptions-item>
                    <a-descriptions-item label="ISBN">{{ selectedBook.isbn }}</a-descriptions-item>
                    <a-descriptions-item label="出版社">{{ selectedBook.publisher }}</a-descriptions-item>
                    <a-descriptions-item label="出版日期">{{ selectedBook.publishDate ? selectedBook.publishDate.substring(0, 10) : '-' }}</a-descriptions-item>
                    <a-descriptions-item label="入库时间">{{ selectedBook.stockDate ? selectedBook.stockDate.substring(0, 10) : '-' }}</a-descriptions-item>
                    <a-descriptions-item label="状态">{{ selectedBook.status === 1 ? '可借阅' : '已借出' }}</a-descriptions-item>
                    <a-descriptions-item label="简介">{{ selectedBook.description || '暂无简介' }}</a-descriptions-item>
                </a-descriptions>
            </div>
            <div v-else>
                加载中...
            </div>
        </a-modal>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'
import StatisticCard from '@/components/StatisticCard.vue'
import { getMyBorrowList, getAllBorrowList } from '@/api/borrow'
import { getBorrowStatistics } from '@/api/borrow'
import { getRecommendedBooks, getHotBooks, getNewBooks } from '@/api/book'

const userStore = useUserStore()
const loading = ref(false)

// 折叠面板状态
const activeKey = ref(['1'])

// 图书详情模态框
const bookDetailVisible = ref(false)
const selectedBook = ref({})

// 统计数据
const statistics = reactive({
    currentBorrowCount: 0,
    totalBorrowCount: 0,
    overdueCount: 0,
    totalFine: 0,
    unpaidFineCount: 0,
    newUserCount: 0,
    totalBookCount: 0
})

// 系统时间
const currentTime = ref(new Date())

// 热门图书
const hotBooks = ref([])

// 推荐图书
const recommendedBooks = ref([])

// 新书
const newBooks = ref([])

// 最近借阅
const recentBorrows = ref([])

// 借阅表格列
const borrowColumns = [
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
        width: 100,
        customRender: ({ text }) => text ? text.substring(0, 10) : '-'
    },
    {
        title: '应还日期',
        dataIndex: 'returnTime',
        key: 'returnTime',
        width: 100,
        customRender: ({ text }) => text ? text.substring(0, 10) : '-'
    },
    {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 80
    }
]

// 处理折叠面板变化
const handleCollapseChange = (key) => {
    activeKey.value = key
}

// 显示图书详情
const showBookDetail = (book) => {
    selectedBook.value = book
    bookDetailVisible.value = true
}

// 通过书名显示图书详情
const showBookDetailByTitle = (bookTitle) => {
    // 从所有图书中查找匹配的图书
    const allBooks = [...recommendedBooks.value, ...hotBooks.value, ...newBooks.value]
    const book = allBooks.find(b => b.title === bookTitle)
    if (book) {
        showBookDetail(book)
    } else {
        message.info('未找到该图书的详细信息')
    }
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

// 获取随机颜色和首字母
const getRandomColor = (book) => {
    const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#f5222d', '#13c2c2', '#faad14']
    // 使用书名的首字母作为索引
    const firstChar = book.title ? book.title.charAt(0).toUpperCase() : '?'
    const index = firstChar.charCodeAt(0) % colors.length
    return colors[index]
}

// 获取书名首字母
const getFirstLetter = (book) => {
    return book.title ? book.title.charAt(0).toUpperCase() : '?'
}

// 格式化日期时间
const formatDateTime = (date) => {
    if (!date) return ''
    const d = new Date(date)
    return d.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    })
}

// 获取统计数据
const fetchStatistics = async () => {
    loading.value = true
    try {
        const res = await getBorrowStatistics()
        if (res.code === 200) {
            statistics.currentBorrowCount = res.data.currentBorrowCount || 0
            statistics.totalBorrowCount = res.data.totalBorrowCount || 0
            statistics.overdueCount = res.data.overdueCount || 0
            statistics.totalFine = res.data.totalFine || 0
            statistics.unpaidFineCount = res.data.unpaidFineCount || 0
            statistics.newUserCount = res.data.newUserCount || 5
            statistics.totalBookCount = res.data.totalBookCount || 128
        }
    } catch (error) {
        console.error('获取统计数据失败:', error)
        message.error('获取统计数据失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

// 更新系统时间
const updateCurrentTime = () => {
    setInterval(() => {
        currentTime.value = new Date()
    }, 1000)
}

// 获取热门图书
const fetchHotBooks = async () => {
    try {
        const res = await getHotBooks()
        if (res.code === 200) {
            hotBooks.value = res.data || []
        }
    } catch (error) {
        console.error('获取热门图书失败:', error)
        message.error('获取热门图书失败，请稍后重试')
    }
}

// 获取推荐图书
const fetchRecommendedBooks = async () => {
    try {
        const res = await getRecommendedBooks()
        if (res.code === 200) {
            recommendedBooks.value = res.data || []
        }
    } catch (error) {
        console.error('获取推荐图书失败:', error)
        message.error('获取推荐图书失败，请稍后重试')
    }
}

// 获取新书
const fetchNewBooks = async () => {
    try {
        const res = await getNewBooks()
        if (res.code === 200) {
            newBooks.value = res.data || []
        }
    } catch (error) {
        console.error('获取新书失败:', error)
        message.error('获取新书失败，请稍后重试')
    }
}

// 获取最近借阅
const fetchRecentBorrows = async () => {
    try {
        let res
        if (userStore.isAdmin) {
            res = await getAllBorrowList({ page: 1, size: 5 })
        } else {
            res = await getMyBorrowList({ page: 1, size: 5 })
        }

        if (res.code === 200) {
            recentBorrows.value = res.data.records || []
        }
    } catch (error) {
        console.error('获取最近借阅失败:', error)
        message.error('获取最近借阅失败，请稍后重试')
    }
}

onMounted(() => {
    fetchStatistics()
    fetchHotBooks()
    fetchRecommendedBooks()
    fetchNewBooks()
    fetchRecentBorrows()
    updateCurrentTime()
})
</script>

<style scoped>
.dashboard-container {
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    min-height: 100vh;
    padding: 20px 12px;
}

.welcome-message {
    background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 20px;
    text-align: center;
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.welcome-text {
    color: white;
    font-size: 18px;
    font-weight: bold;
}

.mb-4 {
    margin-bottom: 24px;
}

.book-card {
    cursor: pointer;
    transition: all 0.3s ease;
    height: 100%;
    padding: 12px;
}

.book-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.book-avatar {
    width: 48px;
    height: 48px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: bold;
    color: white;
    margin-bottom: 12px;
}

.book-info {
    padding: 0 4px;
}

.book-title {
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 6px;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.book-author,
.book-category,
.book-count,
.book-date {
    font-size: 12px;
    color: #666;
    margin-bottom: 3px;
}

.book-status {
    margin-top: 8px;
}

.book-detail {
    padding: 8px 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .book-card {
        margin-bottom: 16px;
    }
}
</style>
