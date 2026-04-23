<template>
    <div class="dashboard-container">
        <!-- 欢迎信息 -->
        <div class="welcome-message" @click="handleWelcomeClick">
            <span class="welcome-text">你好{{ userStore.userInfo.username }}，欢迎登录</span>
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

            <!-- 管理员：已归还未缴纳罚款人数；普通用户：打卡天数 -->
            <a-col v-if="userStore.isAdmin" :span="6">
              <a-card>
                <statistic-card title="已归还未缴纳罚款人数" :value="statistics.unpaidFineCount" icon="warning" color="#722ed1" />
              </a-card>
            </a-col>
            <a-col v-else :span="6">
              <a-card>
                <div class="check-in-card">
                  <div class="check-in-content">
                    <div class="icon-wrapper" style="background-color: #722ed115;">
                      <CalendarOutlined style="color: #722ed1; font-size: 24px;" />
                    </div>
                    <div class="info">
                      <div class="title">打卡天数</div>
                      <div class="value" style="color: #722ed1;">{{ statistics.checkInDays }}</div>
                    </div>
                  </div>
                  <a-button
                    :type="checkedInToday ? 'default' : 'primary'"
                    :disabled="checkedInToday"
                    :loading="checkInLoading"
                    class="check-in-btn"
                    @click="handleCheckIn"
                  >
                    {{ checkedInToday ? '今日已打卡' : '打卡' }}
                  </a-button>
                </div>
              </a-card>
            </a-col>

            <!-- 管理员：新用户注册数量；普通用户：账号注册时长 -->
            <a-col v-if="userStore.isAdmin" :span="6">
              <a-card>
                <statistic-card title="新用户注册数量" :value="statistics.newUserCount" icon="user" color="#fa8c16" />
              </a-card>
            </a-col>
            <a-col v-else :span="6">
              <a-card>
                <statistic-card title="账号注册时长" :value="statistics.registrationTime" icon="clock-circle" color="#fa8c16" />
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
                <div class="horizontal-scroll-container" :loading="loading">
                    <div class="horizontal-scroll-content">
                        <div v-for="(item, index) in recommendedBooks" :key="item.id" class="horizontal-scroll-item">
                            <a-card :hoverable="true" @click="showBookDetail(item)" class="book-card">
                                <div class="book-card-content">
                                    <div v-if="item.coverUrl" class="book-image" :style="{ backgroundImage: `url('${item.coverUrl}')` }">
                                    </div>
                                    <div v-else class="book-image" :style="{ backgroundColor: getRandomColor(item) }">
                                        <span class="book-image-letter">{{ getFirstLetter(item) }}</span>
                                    </div>
                                    <div class="book-info">
                                        <h3 class="book-title">{{ item.title }}</h3>
                                        <p class="book-author">作者: {{ item.author }}</p>
                                        <p class="book-category">分类: {{ item.category }}</p>
                                        <a-tag :color="item.status === 1 ? 'green' : 'red'" class="book-status">
                                            {{ item.status === 1 ? '可借阅' : '已借出' }}
                                        </a-tag>
                                    </div>
                                </div>
                            </a-card>
                        </div>
                    </div>
                </div>
            </a-collapse-panel>

            <!-- 热门图书 -->
            <a-collapse-panel key="2" header="热门图书" :extra="`${hotBooks.length}本图书`">
                <div class="horizontal-scroll-container" :loading="loading">
                    <div class="horizontal-scroll-content">
                        <div v-for="(item, index) in hotBooks" :key="item.id" class="horizontal-scroll-item">
                            <a-card :hoverable="true" @click="showBookDetail(item)" class="book-card">
                                <div class="book-card-content">
                                    <div v-if="item.coverUrl" class="book-image" :style="{ backgroundImage: `url('${item.coverUrl}')` }">
                                    </div>
                                    <div v-else class="book-image" :style="{ backgroundColor: getRandomColor(item) }">
                                        <span class="book-image-letter">{{ getFirstLetter(item) }}</span>
                                    </div>
                                    <div class="book-info">
                                        <h3 class="book-title">{{ item.title }}</h3>
                                        <p class="book-author">作者: {{ item.author }}</p>
                                        <p class="book-count">借阅次数: {{ item.borrowCount }}</p>
                                        <a-tag :color="item.status === 1 ? 'green' : 'red'" class="book-status">
                                            {{ item.status === 1 ? '可借阅' : '已借出' }}
                                        </a-tag>
                                    </div>
                                </div>
                            </a-card>
                        </div>
                    </div>
                </div>
            </a-collapse-panel>

            <!-- 新书推荐 -->
            <a-collapse-panel key="3" header="新书推荐" :extra="`${newBooks.length}本图书`">
                <div class="horizontal-scroll-container" :loading="loading">
                    <div class="horizontal-scroll-content">
                        <div v-for="(item, index) in newBooks" :key="item.id" class="horizontal-scroll-item">
                            <a-card :hoverable="true" @click="showBookDetail(item)" class="book-card">
                                <div class="book-card-content">
                                    <div v-if="item.coverUrl" class="book-image" :style="{ backgroundImage: `url('${item.coverUrl}')` }">
                                    </div>
                                    <div v-else class="book-image" :style="{ backgroundColor: getRandomColor(item) }">
                                        <span class="book-image-letter">{{ getFirstLetter(item) }}</span>
                                    </div>
                                    <div class="book-info">
                                        <h3 class="book-title">{{ item.title }}</h3>
                                        <p class="book-author">作者: {{ item.author }}</p>
                                        <p class="book-date">入库时间: {{ item.stockDate ? item.stockDate.substring(0, 10) : '-' }}</p>
                                        <a-tag :color="item.status === 1 ? 'green' : 'red'" class="book-status">
                                            {{ item.status === 1 ? '可借阅' : '已借出' }}
                                        </a-tag>
                                    </div>
                                </div>
                            </a-card>
                        </div>
                    </div>
                </div>
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
            width="700px"
            @ok="handleBookDetailOk"
        >
            <div v-if="selectedBook" class="book-detail">
                <div class="book-detail-header">
                    <div class="book-cover">
                        <div v-if="selectedBook.coverUrl" class="book-cover-image" :style="{ backgroundImage: `url('${selectedBook.coverUrl}')` }"></div>
                        <div v-else class="book-cover-image default-cover" :style="{ backgroundColor: getRandomColor(selectedBook) }">
                            <span class="cover-letter">{{ getFirstLetter(selectedBook) }}</span>
                        </div>
                    </div>
                    <div class="book-basic-info">
                        <h2 class="book-title">{{ selectedBook.title }}</h2>
                        <p class="book-author">作者: {{ selectedBook.author }}</p>
                        <div class="book-tags">
                            <a-tag color="blue">{{ selectedBook.category }}</a-tag>
                            <a-tag :color="selectedBook.status === 1 ? 'green' : 'red'">
                                {{ selectedBook.status === 1 ? '可借阅' : '已借出' }}
                            </a-tag>
                        </div>
                    </div>
                </div>
                <div class="book-detail-body">
                    <a-descriptions bordered column="2" size="middle">
                        <a-descriptions-item label="ISBN">{{ selectedBook.isbn || '-' }}</a-descriptions-item>
                        <a-descriptions-item label="出版社">{{ selectedBook.publisher || '-' }}</a-descriptions-item>
                        <a-descriptions-item label="出版日期">{{ selectedBook.publishDate ? selectedBook.publishDate.substring(0, 10) : '-' }}</a-descriptions-item>
                        <a-descriptions-item label="入库时间">{{ selectedBook.stockDate ? selectedBook.stockDate.substring(0, 10) : '-' }}</a-descriptions-item>
                        <a-descriptions-item label="借阅次数" :span="2">{{ selectedBook.borrowCount || 0 }}</a-descriptions-item>
                        <a-descriptions-item label="简介" :span="2">{{ selectedBook.description || '暂无简介' }}</a-descriptions-item>
                    </a-descriptions>
                </div>
            </div>
            <div v-else>
                <div class="loading-container">
                    <a-spin tip="加载中..." />
                </div>
            </div>
        </a-modal>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'
import { CalendarOutlined } from '@ant-design/icons-vue'
import StatisticCard from '@/components/StatisticCard.vue'
import { getMyBorrowList, getAllBorrowList } from '@/api/borrow'
import { getBorrowStatistics } from '@/api/borrow'
import { getRecommendedBooks, getHotBooks, getNewBooks } from '@/api/book'
import { doCheckIn, getTodayCheckIn } from '@/api/checkin'

const userStore = useUserStore()
const loading = ref(false)
const checkedInToday = ref(false)
const checkInLoading = ref(false)

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
    totalBookCount: 0,
    registrationTime: '0天',
    checkInDays: 0
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

// 处理欢迎信息点击
const handleWelcomeClick = () => {
    message.info('欢迎回来！')
}

// 处理图书详情模态框 OK 按钮点击
const handleBookDetailOk = () => {
    bookDetailVisible.value = false
}

// 去重函数
const removeDuplicates = (books) => {
    const uniqueBooks = []
    const seenTitles = new Set()
    
    for (const book of books) {
        if (!seenTitles.has(book.title)) {
            seenTitles.add(book.title)
            uniqueBooks.push(book)
        }
    }
    
    return uniqueBooks
}

// 获取热门图书
const fetchHotBooks = async () => {
    try {
        const res = await getHotBooks()
        if (res.code === 200) {
            hotBooks.value = removeDuplicates(res.data || [])
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
            recommendedBooks.value = removeDuplicates(res.data || [])
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
            newBooks.value = removeDuplicates(res.data || [])
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
            res = await getMyBorrowList({ page: 1, size: 100 })
        }

        if (res.code === 200) {
            recentBorrows.value = (res.data.records || []).slice(0, 5)

            // 计算账号注册时长
            if (!userStore.isAdmin && userStore.userInfo) {
                const userInfo = userStore.userInfo
                console.log('用户信息:', userInfo)
                if (userInfo.createTime) {
                    try {
                        const createTimeStr = userInfo.createTime
                        console.log('原始注册时间:', createTimeStr)
                        
                        // 确保日期格式正确
                        let formattedCreateTimeStr = createTimeStr
                        if (typeof formattedCreateTimeStr === 'string') {
                            if (formattedCreateTimeStr.includes(' ')) {
                                formattedCreateTimeStr = formattedCreateTimeStr.replace(' ', 'T')
                            }
                            console.log('格式化后的注册时间:', formattedCreateTimeStr)
                        }
                        
                        const createDate = new Date(formattedCreateTimeStr)
                        console.log('解析后的注册时间:', createDate)
                        console.log('注册时间是否有效:', !isNaN(createDate.getTime()))
                        
                        if (!isNaN(createDate.getTime())) {
                            const now = new Date()
                            const diffMs = now - createDate
                            const days = Math.ceil(diffMs / (1000 * 60 * 60 * 24))
                            console.log('注册时长(天):', days)
                            statistics.registrationTime = days > 0 ? `${days}天` : '1天'
                        } else {
                            console.warn('无效的注册时间:', createDate)
                            statistics.registrationTime = '0天'
                        }
                    } catch (e) {
                        console.warn('注册时间解析失败:', userInfo.createTime, e)
                        statistics.registrationTime = '0天'
                    }
                } else {
                    console.warn('用户信息中没有注册时间:', userInfo)
                    statistics.registrationTime = '0天'
                }
            }
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
    fetchCheckInStatus()
    updateCurrentTime()
})

// 打卡
const handleCheckIn = async () => {
    checkInLoading.value = true
    try {
        const res = await doCheckIn()
        if (res.code === 200) {
            message.success('打卡成功')
            checkedInToday.value = true
            statistics.checkInDays++
        }
    } catch (error) {
        console.error('打卡失败:', error)
    } finally {
        checkInLoading.value = false
    }
}

// 获取打卡状态
const fetchCheckInStatus = async () => {
    if (userStore.isAdmin) return
    try {
        const res = await getTodayCheckIn()
        if (res.code === 200) {
            checkedInToday.value = res.data.checkedIn || false
            statistics.checkInDays = res.data.count || 0
        }
    } catch (error) {
        console.error('获取打卡状态失败:', error)
    }
}
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
    cursor: pointer;
    transition: all 0.3s ease;
}

.welcome-message:hover {
    box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
    transform: translateY(-2px);
}

.welcome-message:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.welcome-text {
    color: white;
    font-size: 18px;
    font-weight: bold;
}

.mb-4 {
    margin-bottom: 24px;
}

/* 统计卡片悬浮效果 */
.dashboard-container :deep(.ant-card) {
    cursor: pointer;
    transition: all 0.3s ease;
}

.dashboard-container :deep(.ant-card:hover) {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.dashboard-container :deep(.ant-card:active) {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 水平滚动容器 */
.horizontal-scroll-container {
    width: 100%;
    overflow: hidden;
    position: relative;
}

.horizontal-scroll-content {
    display: flex;
    flex-direction: row;
    overflow-x: auto;
    padding: 8px 0;
    scroll-behavior: smooth;
    -webkit-overflow-scrolling: touch;
}

.horizontal-scroll-content::-webkit-scrollbar {
    height: 6px;
}

.horizontal-scroll-content::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.horizontal-scroll-content::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.horizontal-scroll-content::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

.horizontal-scroll-item {
    flex: 0 0 350px;
    margin-right: 16px;
}

/* 图书详情模态框样式 */
.book-detail {
    padding: 10px 0;
}

.book-detail-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #f0f0f0;
}

.book-cover {
    margin-right: 20px;
}

.book-cover-image {
    width: 120px;
    height: 160px;
    border-radius: 4px;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
}

.default-cover {
    background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
}

.cover-letter {
    font-size: 48px;
    font-weight: bold;
    color: white;
}

.book-basic-info {
    flex: 1;
}

.book-basic-info .book-title {
    margin: 0 0 10px 0;
    font-size: 20px;
    font-weight: bold;
}

.book-basic-info .book-author {
    margin: 0 0 15px 0;
    font-size: 14px;
    color: #666;
}

.book-tags {
    display: flex;
    gap: 10px;
}

.book-detail-body {
    margin-top: 20px;
}

.loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
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

.book-card-content {
    display: flex;
    height: 140px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.book-image {
    width: 50%;
    background-size: cover;
    background-position: center;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
}

.book-image-letter {
    font-size: 32px;
    font-weight: bold;
    color: white;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.book-info {
    width: 50%;
    padding: 16px;
    background: white;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.book-title {
    font-size: 15px;
    font-weight: bold;
    margin-bottom: 6px;
    color: #333;
    line-height: 1.4;
    white-space: normal;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.book-author,
.book-category,
.book-count,
.book-date {
    font-size: 13px;
    color: #333;
    margin-bottom: 4px;
    line-height: 1.3;
}

.book-status {
    margin-top: 4px;
    align-self: flex-start;
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

.check-in-card {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.check-in-content {
    display: flex;
    align-items: center;
    gap: 12px;
}

.check-in-card .icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 8px;
    margin-right: 4px;
}

.check-in-card .info {
    flex: 1;
}

.check-in-card .title {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
    margin-bottom: 4px;
}

.check-in-card .value {
    font-size: 16px;
    font-weight: 600;
}

.check-in-btn {
    width: 100%;
}
</style>