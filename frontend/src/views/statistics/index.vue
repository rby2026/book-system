<template>
    <div class="statistics-container">
        <!-- 时间范围选择 -->
        <div class="mb-4">
            <a-radio-group v-model:value="timeRange" button-style="solid">
                <a-radio-button value="week">最近一周</a-radio-button>
                <a-radio-button value="month">最近一月</a-radio-button>
                <a-radio-button value="year">最近一年</a-radio-button>
                        <a-radio-button value="custom">自定义</a-radio-button>
            </a-radio-group>

            <a-range-picker v-if="timeRange === 'custom'" v-model:value="customDateRange" class="ml-4"
                      @change="handleDateRangeChange"/>
        </div>

        <a-row :gutter="16">
            <!-- 借阅趋势分析 -->
            <a-col :span="24" class="mb-4">
                <a-card title="借阅趋势分析">
                    <div id="borrowTrendChart" style="width: 100%; height: 400px;"></div>
                </a-card>
            </a-col>

            <!-- 热门图书分类分析 -->
            <a-col :span="12" class="mb-4">
                <a-card title="热门图书分类">
                    <div id="categoryChart" style="width: 100%; height: 400px;"></div>
                </a-card>
            </a-col>

            <!-- 读者借阅时段分析 -->
            <a-col :span="12" class="mb-4">
                <a-card title="读者借阅时段分析">
                    <div id="timeSlotChart" style="width: 100%; height: 400px;"></div>
                </a-card>
            </a-col>

            <!-- 采购建议 -->
            <a-col :span="24" class="mb-4">
                <a-card title="采购建议">
                    <a-table :columns="purchaseColumns" :data-source="purchaseSuggestions" :pagination="false"
                        size="small" :bordered="true">
                        <template #bodyCell="{ column, record }">
                            <template v-if="column.key === 'demand'">
                                <a-progress :percent="record.demand" :stroke-color="getDemandColor(record.demand)" />
                            </template>
                        </template>
                    </a-table>
                </a-card>
            </a-col>

            <!-- 藏书结构分析 -->
            <a-col :span="24" class="mb-4">
                <a-card title="藏书结构分析">
                    <div id="structureChart" style="width: 100%; height: 400px;"></div>
                </a-card>
            </a-col>
        </a-row>

        <!-- 详细数据表格 -->
        <a-card title="详细数据" class="mb-4">
            <a-tabs v-model:activeKey="activeTab">
                <a-tab-pane key="books" tab="图书借阅排行">
                    <a-table :columns="bookColumns" :data-source="bookRankings" :pagination="pagination"
                        @change="handleTableChange">
                        <template #bodyCell="{ column, record }">
                            <template v-if="column.key === 'trend'">
                                <span :style="{ color: record.trend > 0 ? '#52c41a' : '#f5222d' }">
                                    {{ record.trend > 0 ? '+' : '' }}{{ record.trend }}%
                                    <component :is="record.trend > 0 ? RiseOutlined : FallOutlined" />
                                </span>
                            </template>
                        </template>
                    </a-table>
                </a-tab-pane>

                <a-tab-pane key="readers" tab="读者活跃度排行">
                    <a-table :columns="readerColumns" :data-source="readerRankings" :pagination="pagination"
                        @change="handleTableChange" />
                </a-tab-pane>
            </a-tabs>
        </a-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { RiseOutlined, FallOutlined } from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import {
    getBorrowTrend,
    getCategoryAnalysis,
    getTimeSlotAnalysis,
    getPurchaseSuggestions,
    getBookStructure,
    getBookRankings,
    getReaderRankings
} from '@/api/statistics'

// 图表实例
let charts = {
    trend: null,
    category: null,
    timeSlot: null,
    structure: null
}

// 加载状态
const loading = reactive({
    trend: false,
    category: false,
    timeSlot: false,
    purchase: false,
    structure: false
})

// 分类名称映射（英文 -> 中文）
const categoryNameMap = {
    'LITERATURE': '文学',
    'SCIENCE': '科学',
    'TECHNOLOGY': '技术',
    'HISTORY': '历史',
    'PHILOSOPHY': '哲学',
    'ECONOMICS': '经济',
    'POLITICS': '政治',
    'ART': '艺术',
    'EDUCATION': '教育',
    'MEDICINE': '医学',
    'LAW': '法律',
    'MILITARY': '军事',
    'SPORTS': '体育',
    'ENTERTAINMENT': '娱乐',
    'FICTION': '小说',
    'BIOGRAPHY': '传记',
    'CHILDREN': '儿童读物',
    'COMICS': '漫画',
    'COOKING': '烹饪',
    'TRAVEL': '旅游',
    'RELIGION': '宗教',
    'REFERENCE': '参考书',
    'OTHER': '其他'
}

// 将英文分类名称转换为中文
const translateCategoryName = (name) => {
    return categoryNameMap[name] || name // 如果没有对应的映射，则返回原名称
}

// 时间范围选择
const timeRange = ref('month')
const customDateRange = ref([])

// 表格相关
const activeTab = ref('books')
const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`
})

// 数据
const purchaseSuggestions = ref([])
const bookRankings = ref([])
const readerRankings = ref([])

// 采购建议表格列
const purchaseColumns = [
    {
        title: '图书分类',
        dataIndex: 'category',
        key: 'category'
    },
    {
        title: '总馆藏量',
        dataIndex: 'currentCount',
        key: 'currentCount',
        width: 100
    },
    {
        title: '可借数量',
        dataIndex: 'availableCount',
        key: 'availableCount',
        width: 100
    },
    {
        title: '历史借阅次数',
        dataIndex: 'borrowCount',
        key: 'borrowCount',
        width: 120
    },
    {
        title: '建议采购',
        dataIndex: 'suggestedCount',
        key: 'suggestedCount',
        width: 100
    },
    {
        title: '需求度',
        dataIndex: 'demand',
        key: 'demand',
        width: 200
    }
]

// 图书排行表格列
const bookColumns = [
    {
        title: '排名',
        dataIndex: 'rank',
        key: 'rank',
        width: 80
    },
    {
        title: '图书名称',
        dataIndex: 'title',
        key: 'title'
    },
    {
        title: '作者',
        dataIndex: 'author',
        key: 'author',
        width: 150
    },
    {
        title: '借阅次数',
        dataIndex: 'borrowCount',
        key: 'borrowCount',
        width: 100
    },
    {
        title: '环比变化',
        dataIndex: 'trend',
        key: 'trend',
        width: 120
    }
]

// 读者排行表格列
const readerColumns = [
    {
        title: '排名',
        dataIndex: 'rank',
        key: 'rank',
        width: 80
    },
    {
        title: '用户名',
        dataIndex: 'username',
        key: 'username'
    },
    {
        title: '借阅总数',
        dataIndex: 'totalBorrows',
        key: 'totalBorrows',
        width: 100
    },
    {
        title: '当前借阅',
        dataIndex: 'currentBorrows',
        key: 'currentBorrows',
        width: 100
    },
    {
        title: '准时归还率',
        dataIndex: 'returnRate',
        key: 'returnRate',
        width: 120,
        customRender: ({ text }) => `${text}%`
    }
]

// 获取需求度颜色
const getDemandColor = (demand) => {
    if (demand >= 80) return '#f5222d'
    if (demand >= 60) return '#fa8c16'
    if (demand >= 40) return '#1890ff'
    return '#52c41a'
}

// 获取借阅趋势数据
const fetchBorrowTrend = async () => {
    if (!charts.trend) {
        console.error('借阅趋势图表未初始化');
        return;
    }

    loading.trend = true;
    try {
        const params = {
            timeRange: timeRange.value,
            startDate: customDateRange.value?.[0],
            endDate: customDateRange.value?.[1]
        };
        console.log('获取借阅趋势数据，参数:', params);

        const res = await getBorrowTrend(params);
        console.log('借阅趋势数据:', res.data);

        const { dates, counts } = res.data;
        if (!dates || !counts) {
            console.error('借阅趋势数据格式错误:', res.data);
            message.error('借阅趋势数据格式错误');
            return;
        }

        // 使用简化的配置
        const option = {
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            tooltip: {
                trigger: 'axis'
            },
            xAxis: {
                type: 'category',
                data: dates,
                axisLabel: {
                    rotate: 45,
                    interval: 'auto'
                }
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: '借阅数量',
                    type: 'line',
                    smooth: true,
                    data: counts,
                    areaStyle: {
                        opacity: 0.1
                    }
                }
            ]
        };

        // 清除之前的配置并设置新配置
        charts.trend.clear();
        charts.trend.setOption(option);
        console.log(option)
        console.log('借阅趋势图表渲染成功');
    } catch (error) {
        console.error('获取借阅趋势数据失败:', error);
        message.error('获取借阅趋势数据失败');
    } finally {
        loading.trend = false;
    }
}

// 获取分类分析数据
const fetchCategoryAnalysis = async () => {
    if (!charts.category) {
        console.error('分类分析图表未初始化');
        return;
    }

    loading.category = true;
    try {
        console.log('获取分类分析数据');

        const res = await getCategoryAnalysis();
        console.log('分类分析数据:', res.data);

        const { categories, values } = res.data;
        if (!categories || !values) {
            console.error('分类分析数据格式错误:', res.data);
            message.error('分类分析数据格式错误');
            return;
        }

        // 将英文分类名称转换为中文
        const translatedCategories = categories.map(category => translateCategoryName(category));
        console.log('转换后的分类名称:', translatedCategories);

        // 使用简化的配置
        const option = {
            tooltip: {
                trigger: 'item',
                formatter: '{a} <br/>{b}: {c} ({d}%)'
            },
            legend: {
                orient: 'vertical',
                right: 10,
                top: 'center',
                data: translatedCategories
            },
            series: [
                {
                    name: '图书分类',
                    type: 'pie',
                    radius: ['40%', '70%'],
                    avoidLabelOverlap: false,
                    label: {
                        show: false
                    },
                    emphasis: {
                        label: {
                            show: true,
                            formatter: '{b}: {c} ({d}%)'
                        }
                    },
                    data: categories.map((name, index) => ({
                        name: translateCategoryName(name),
                        value: values[index]
                    }))
                }
            ]
        };

        // 清除之前的配置并设置新配置
        charts.category.clear();
        charts.category.setOption(option);

        console.log('分类分析图表渲染成功');
    } catch (error) {
        console.error('获取分类分析数据失败:', error);
        message.error('获取分类分析数据失败');
    } finally {
        loading.category = false;
    }
}

// 获取时段分析数据
const fetchTimeSlotAnalysis = async () => {
    if (!charts.timeSlot) {
        console.error('时段分析图表未初始化');
        return;
    }

    loading.timeSlot = true;
    try {
        console.log('获取时段分析数据');

        const res = await getTimeSlotAnalysis();
        console.log('时段分析数据:', res.data);

        const { hours, counts } = res.data;
        if (!hours || !counts) {
            console.error('时段分析数据格式错误:', res.data);
            message.error('时段分析数据格式错误');
            return;
        }

        // 使用简化的配置
        const option = {
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            tooltip: {
                trigger: 'axis'
            },
            xAxis: {
                type: 'category',
                data: hours
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: '借阅次数',
                    type: 'bar',
                    data: counts,
                    itemStyle: {
                        color: '#1890ff'
                    }
                }
            ]
        };

        // 清除之前的配置并设置新配置
        charts.timeSlot.clear();
        charts.timeSlot.setOption(option);

        console.log('时段分析图表渲染成功');
    } catch (error) {
        console.error('获取时段分析数据失败:', error);
        message.error('获取时段分析数据失败');
    } finally {
        loading.timeSlot = false;
    }
}

// 获取采购建议
const fetchPurchaseSuggestions = async () => {
    loading.purchase = true
    try {
        const res = await getPurchaseSuggestions()
        // 转换分类名称为中文
        purchaseSuggestions.value = (res.data || []).map(item => ({
            ...item,
            category: translateCategoryName(item.category)
        }))
        console.log('采购建议数据(转换后):', purchaseSuggestions.value)
    } catch (error) {
        console.error('获取采购建议失败:', error)
        message.error('获取采购建议失败')
    } finally {
        loading.purchase = false
    }
}

// 获取藏书结构数据
const fetchBookStructure = async () => {
    if (!charts.structure) {
        console.error('藏书结构图表未初始化');
        return;
    }

    loading.structure = true;
    try {
        console.log('获取藏书结构数据');

        const res = await getBookStructure();
        console.log('藏书结构数据:', res.data);

        const { categories, borrowed, total } = res.data;
        if (!categories || !borrowed || !total) {
            console.error('藏书结构数据格式错误:', res.data);
            message.error('藏书结构数据格式错误');
            return;
        }

        // 将英文分类名称转换为中文
        const translatedCategories = categories.map(category => translateCategoryName(category));
        console.log('转换后的藏书结构分类名称:', translatedCategories);

        // 使用简化的配置
        const option = {
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                }
            },
            legend: {
                data: ['已借出', '馆藏总量']
            },
            xAxis: {
                type: 'value'
            },
            yAxis: {
                type: 'category',
                data: translatedCategories
            },
            series: [
                {
                    name: '已借出',
                    type: 'bar',
                    stack: 'total',
                    data: borrowed
                },
                {
                    name: '馆藏总量',
                    type: 'bar',
                    stack: 'total',
                    data: total
                }
            ]
        };

        // 清除之前的配置并设置新配置
        charts.structure.clear();
        charts.structure.setOption(option);

        console.log('藏书结构图表渲染成功');
    } catch (error) {
        console.error('获取藏书结构数据失败:', error);
        message.error('获取藏书结构数据失败');
    } finally {
        loading.structure = false;
    }
}

// 获取排行榜数据
const fetchRankings = async () => {
    try {
        if (activeTab.value === 'books') {
            const res = await getBookRankings({
                page: pagination.current,
                size: pagination.pageSize
            })
            bookRankings.value = res.data.records || []
            pagination.total = res.data.total
        } else {
            const res = await getReaderRankings({
                page: pagination.current,
                size: pagination.pageSize
            })
            readerRankings.value = res.data.records || []
            pagination.total = res.data.total
        }
    } catch (error) {
        console.error('获取排行榜数据失败:', error)
        message.error('获取排行榜数据失败')
    }
}

// 处理时间范围变化
const handleDateRangeChange = () => {
    fetchBorrowTrend()
}

// 处理表格变化
const handleTableChange = (pag) => {
    pagination.current = pag.current
    pagination.pageSize = pag.pageSize
    fetchRankings()
}

// 监听时间范围变化
watch(timeRange, () => {
    if (timeRange.value !== 'custom') {
        customDateRange.value = []
        fetchBorrowTrend()
    }
})

// 监听标签页变化
watch(activeTab, () => {
    pagination.current = 1
    fetchRankings()
})

// 初始化图表的最简单方法
const initCharts = () => {
    try {
        // 确保ECharts已加载
        console.log('ECharts版本:', echarts.version);

        // 借阅趋势图表
        const trendChartDom = document.getElementById('borrowTrendChart');
        if (trendChartDom) {
            // 确保容器有尺寸
            trendChartDom.style.width = '100%';
            trendChartDom.style.height = '400px';

            // 初始化图表
            charts.trend = echarts.init(trendChartDom);

            // 设置一个简单的配置，确保图表可见
            charts.trend.setOption({
                grid: { containLabel: true },
                xAxis: { type: 'category', data: ['加载中...'] },
                yAxis: { type: 'value' },
                series: [{ type: 'line', data: [0] }]
            });

            console.log('借阅趋势图表初始化成功');
        } else {
            console.error('找不到借阅趋势图表容器');
        }

        // 分类分析图表
        const categoryChartDom = document.getElementById('categoryChart');
        if (categoryChartDom) {
            categoryChartDom.style.width = '100%';
            categoryChartDom.style.height = '400px';

            charts.category = echarts.init(categoryChartDom);
            charts.category.setOption({
                series: [{
                    type: 'pie',
                    radius: '50%',
                    data: [{ name: '加载中...', value: 1 }]
                }]
            });

            console.log('分类分析图表初始化成功');
        } else {
            console.error('找不到分类分析图表容器');
        }

        // 时段分析图表
        const timeSlotChartDom = document.getElementById('timeSlotChart');
        if (timeSlotChartDom) {
            timeSlotChartDom.style.width = '100%';
            timeSlotChartDom.style.height = '400px';

            charts.timeSlot = echarts.init(timeSlotChartDom);
            charts.timeSlot.setOption({
                grid: { containLabel: true },
                xAxis: { type: 'category', data: ['加载中...'] },
                yAxis: { type: 'value' },
                series: [{ type: 'bar', data: [0] }]
            });

            console.log('时段分析图表初始化成功');
        } else {
            console.error('找不到时段分析图表容器');
        }

        // 藏书结构图表
        const structureChartDom = document.getElementById('structureChart');
        if (structureChartDom) {
            structureChartDom.style.width = '100%';
            structureChartDom.style.height = '400px';

            charts.structure = echarts.init(structureChartDom);
            charts.structure.setOption({
                grid: { containLabel: true },
                xAxis: { type: 'value' },
                yAxis: { type: 'category', data: ['加载中...'] },
                series: [{ type: 'bar', data: [0] }]
            });

            console.log('藏书结构图表初始化成功');
        } else {
            console.error('找不到藏书结构图表容器');
        }

        // 监听窗口大小变化
        window.addEventListener('resize', () => {
            Object.values(charts).forEach(chart => {
                if (chart) {
                    try {
                        chart.resize();
                    } catch (e) {
                        console.error('图表resize失败:', e);
                    }
                }
            });
        });

        console.log('图表初始化完成');
    } catch (error) {
        console.error('图表初始化失败:', error);
        message.error('图表初始化失败，请刷新页面重试');
    }
};

onMounted(() => {
    console.log('组件挂载完成');

    // 使用更长的延迟确保DOM已完全渲染
    setTimeout(() => {
        initCharts();

        // 获取数据
        if (charts.trend) fetchBorrowTrend();
        if (charts.category) fetchCategoryAnalysis();
        if (charts.timeSlot) fetchTimeSlotAnalysis();
        fetchPurchaseSuggestions();
        if (charts.structure) fetchBookStructure();
        fetchRankings();

        // 手动触发一次resize事件
        window.dispatchEvent(new Event('resize'));
        console.log('手动触发resize事件');
    }, 1000); // 增加延迟时间到1秒
})
</script>

<style scoped>
.statistics-container {
    padding: 24px;
    max-width: 1400px;
    margin: 0 auto;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    min-height: 100vh;
}

.mb-4 {
    margin-bottom: 24px;
}

.ml-4 {
    margin-left: 16px;
}

/* 添加图表容器样式 */
.chart-container {
    width: 100%;
    height: 350px;
    margin-bottom: 20px;
}

.statistics-container :deep(.ant-table-bordered .ant-table-cell) {
    border-color: #b4d3f7;
}

.statistics-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
    border-color: #b4d3f7;
}

.statistics-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
    border-color: #b4d3f7;
}
</style>