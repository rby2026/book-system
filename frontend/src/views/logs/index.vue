<template>
    <div class="logs-container">
        <a-card>
            <!-- 搜索区域 -->
            <div class="search-area mb-4">
                <a-form layout="inline">
                    <a-form-item label="用户名">
                        <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allowClear />
                    </a-form-item>
                    <a-form-item label="操作类型">
                        <a-input v-model:value="searchForm.operation" placeholder="请输入操作类型" allowClear />
                    </a-form-item>
                    <a-form-item label="状态">
                        <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
                            <a-select-option :value="1">成功</a-select-option>
                            <a-select-option :value="0">失败</a-select-option>
                        </a-select>
                    </a-form-item>
                    <a-form-item label="操作时间">
                        <a-range-picker v-model:value="dateRange" show-time format="YYYY-MM-DD HH:mm:ss" />
                    </a-form-item>
                    <a-form-item>
                        <a-button type="primary" @click="handleSearch">
                            <template #icon>
                                <SearchOutlined />
                            </template>
                            查询
                        </a-button>
                        <a-button class="ml-2" @click="handleReset">
                            <template #icon>
                                <ReloadOutlined />
                            </template>
                            重置
                        </a-button>
                    </a-form-item>
                </a-form>
            </div>

            <!-- 工具栏 -->
            <div class="table-toolbar mb-4">
                <a-space>
                    <a-button danger type="primary" @click="handleBatchDelete" :disabled="!selectedLogIds.length">
                        <template #icon>
                            <DeleteOutlined />
                        </template>
                        批量删除
                    </a-button>
                </a-space>
            </div>
            <!-- 日志表格 -->
            <a-table :columns="columns" :data-source="logs" :loading="loading" :pagination="pagination" :row-selection="{
                selectedRowKeys: selectedLogIds,
                onChange: (selectedRowKeys) => selectedLogIds = selectedRowKeys
            }" :row-key="record => record.id" @change="handleTableChange" :scroll="{ x: 1300 }" :bordered="true">
                <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'status'">
                        <a-tag :color="record.status === 1 ? 'success' : 'error'">
                            {{ record.status === 1 ? '成功' : '失败' }}
                        </a-tag>
                    </template>
                    <template v-if="column.key === 'action'">
                        <a-space>
                            <a-button type="link" @click="handleViewDetails(record)">详情</a-button>
                            <a-button type="link" danger @click="handleDelete(record.id)">删除</a-button>
                        </a-space>
                    </template>
                </template>
            </a-table>

            <!-- 日志详情抽屉 -->
            <a-drawer title="日志详情" :width="600" placement="right" :visible="drawerVisible" @close="closeDrawer">
                <a-descriptions v-if="selectedLog" bordered :column="1">
                    <a-descriptions-item label="用户名">{{ selectedLog.username || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="操作">{{ selectedLog.operation || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="请求方法">{{ selectedLog.method || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="IP地址">{{ selectedLog.ip || '-' }}</a-descriptions-item>
                    <a-descriptions-item label="操作时间">
                        {{ selectedLog.operationTime ? dayjs(selectedLog.operationTime).format('YYYY-MM-DD HH:mm:ss') :
                            '-' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="执行时长">{{ selectedLog.executionTime || '-' }} ms</a-descriptions-item>
                    <a-descriptions-item label="状态">
                        <a-tag :color="selectedLog.status === 1 ? 'success' : 'error'">
                            {{ selectedLog.status === 1 ? '成功' : '失败' }}
                        </a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="请求参数">
                        <pre class="params-pre">{{ formatParams(selectedLog.params) }}</pre>
                    </a-descriptions-item>
                </a-descriptions>
            </a-drawer>
        </a-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
    SearchOutlined,
    ReloadOutlined,
    DeleteOutlined
} from '@ant-design/icons-vue'
import { getLogList, deleteLog, batchDeleteLogs } from '@/api/logs'
import dayjs from 'dayjs'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
    username: '',
    operation: '',
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

// 日志数据
const logs = ref([])

// 选中的日志ID
const selectedLogIds = ref([])

// 选中的日志详情
const selectedLog = ref(null)

// 抽屉可见状态
const drawerVisible = ref(false)

// 日志表格列
const columns = [
    {
        title: '序号',
        dataIndex: 'index',
        width: 70,
        customRender: ({ index }) => pagination.pageSize * (pagination.current - 1) + index + 1
    },
    {
        title: '用户名',
        dataIndex: 'username',
        width: 100
    },
    {
        title: '操作',
        dataIndex: 'operation',
        width: 100
    },
    {
        title: '请求方法',
        dataIndex: 'method',
        width: 150,
        ellipsis: true
    },
    {
        title: '请求参数',
        dataIndex: 'params',
        width: 200,
        ellipsis: true
    },
    {
        title: 'IP地址',
        dataIndex: 'ip',
        width: 120
    },
    {
        title: '操作时间',
        dataIndex: 'operationTime',
        width: 160,
        customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : '-'
    },
    {
        title: '执行时长(ms)',
        dataIndex: 'executionTime',
        width: 100
    },
    {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 80
    },
    {
        title: '操作',
        key: 'action',
        width: 200,
        fixed: 'right'
    }
]

// 获取日志列表
const fetchLogs = async () => {
    loading.value = true
    try {
        const params = {
            current: pagination.current,
            size: pagination.pageSize,
            username: searchForm.username || undefined,
            operation: searchForm.operation || undefined,
            status: searchForm.status,
            startTime: searchForm.startTime ? dayjs(searchForm.startTime).format('YYYY-MM-DD HH:mm:ss') : undefined,
            endTime: searchForm.endTime ? dayjs(searchForm.endTime).format('YYYY-MM-DD HH:mm:ss') : undefined
        }

        const res = await getLogList(params)
        logs.value = res.data.records
        pagination.total = res.data.total
    } catch (error) {
        console.error('获取日志列表失败:', error)
        message.error('获取日志列表失败')
    } finally {
        loading.value = false
    }
}

// 处理查询
const handleSearch = () => {
    pagination.current = 1
    fetchLogs()
}

// 处理重置
const handleReset = () => {
    searchForm.username = ''
    searchForm.operation = ''
    searchForm.status = undefined
    searchForm.startTime = null
    searchForm.endTime = null
    handleSearch()
}

// 处理表格变化
const handleTableChange = (pag) => {
    pagination.current = pag.current
    pagination.pageSize = pag.pageSize
    fetchLogs()
}

// 处理删除
const handleDelete = async (id) => {
    try {
        await deleteLog(id)
        message.success('删除成功')
        if (logs.value.length === 1 && pagination.current > 1) {
            pagination.current -= 1
        }
        fetchLogs()
    } catch (error) {
        console.error('删除日志失败:', error)
        message.error('删除日志失败')
    }
}

// 处理批量删除
const handleBatchDelete = async () => {
    if (!selectedLogIds.value.length) {
        message.warning('请选择要删除的日志')
        return
    }

    try {
        await batchDeleteLogs(selectedLogIds.value)
        message.success('批量删除成功')
        selectedLogIds.value = []
        if (logs.value.length === selectedLogIds.value.length && pagination.current > 1) {
            pagination.current -= 1
        }
        fetchLogs()
    } catch (error) {
        console.error('批量删除日志失败:', error)
        message.error('批量删除日志失败')
    }
}

// 处理查看详情
const handleViewDetails = (record) => {
    selectedLog.value = record
    drawerVisible.value = true
}

// 关闭抽屉
const closeDrawer = () => {
    drawerVisible.value = false
}

// 格式化参数
const formatParams = (params) => {
    if (!params) return '-'
    try {
        // 尝试解析JSON字符串
        const parsed = typeof params === 'string' ? JSON.parse(params) : params
        return JSON.stringify(parsed, null, 2)
    } catch (e) {
        // 如果解析失败，直接返回原始字符串
        return params
    }
}

// 在 script setup 中添加
const dateRange = computed({
    get: () => [searchForm.startTime, searchForm.endTime],
    set: (val) => {
        searchForm.startTime = val?.[0] || null
        searchForm.endTime = val?.[1] || null
    }
})

// 初始化
fetchLogs()
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
    justify-content: flex-end;
}

/* 确保表格不会溢出容器 */
:deep(.ant-table-wrapper) {
    width: 100%;
    overflow-x: auto;
}

.log-details {
    margin-top: 16px;
}

.params-pre {
    max-height: 200px;
    overflow: auto;
    background-color: #f5f5f5;
    padding: 8px;
    border-radius: 4px;
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
}

.logs-container :deep(.ant-table-bordered .ant-table-cell) {
    border-color: #b4d3f7;
}

.logs-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
    border-color: #b4d3f7;
}

.logs-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
    border-color: #b4d3f7;
}
</style>