<template>
    <div class="backup-container">
        <!-- 备份配置卡片 -->
        <a-card title="备份配置" class="mb-4">
            <a-form :model="backupConfig" label-col="{ span: 6 }" wrapper-col="{ span: 14 }">
                <a-form-item label="自动备份">
                    <a-switch v-model:checked="backupConfig.autoBackup" />
                </a-form-item>
                <a-form-item label="备份时间">
                    <a-time-picker v-model:value="backupConfig.backupTime" format="HH:mm" />
                </a-form-item>
                <a-form-item label="备份保留天数">
                    <a-input-number v-model:value="backupConfig.retentionDays" :min="1" :max="365" />
                </a-form-item>
                <a-form-item label="备份存储路径">
                    <a-input v-model:value="backupConfig.storagePath" />
                </a-form-item>
                <a-form-item wrapper-col="{ offset: 6 }">
                    <a-button type="primary" @click="handleSaveConfig">保存配置</a-button>
                </a-form-item>
            </a-form>
        </a-card>

        <!-- 备份列表卡片 -->
        <a-card title="备份管理" class="mb-4">
            <!-- 工具栏 -->
            <div class="table-toolbar mb-4">
                <a-space>
                    <a-button type="primary" @click="handleCreateBackup">
                        <template #icon>
                            <PlusOutlined />
                        </template>
                        创建备份
                    </a-button>
                    <a-upload :customRequest="handleUploadBackup" :showUploadList="false" accept=".sql,.zip">
                        <a-button>
                            <template #icon>
                                <UploadOutlined />
                            </template>
                            上传备份
                        </a-button>
                    </a-upload>
                </a-space>
            </div>

            <!-- 备份列表 -->
            <a-table :columns="columns" :data-source="backupList" :loading="loading" :pagination="pagination"
                @change="handleTableChange">
                <template #bodyCell="{ column, record }">
                    <!-- 备份大小 -->
                    <template v-if="column.key === 'size'">
                        {{ formatFileSize(record.size) }}
                    </template>

                    <!-- 备份类型 -->
                    <template v-if="column.key === 'type'">
                        <a-tag :color="record.type === 'AUTO' ? 'blue' : 'green'">
                            {{ record.type === 'AUTO' ? '自动' : '手动' }}
                        </a-tag>
                    </template>

                    <!-- 操作列 -->
                    <template v-if="column.key === 'action'">
                        <a-space>
                            <a-button type="link" @click="handleDownload(record)">
                                <template #icon>
                                    <DownloadOutlined />
                                </template>
                                下载
                            </a-button>
                            <a-button type="link" @click="handleRestore(record)">
                                <template #icon>
                                    <UndoOutlined />
                                </template>
                                恢复
                            </a-button>
                            <a-popconfirm title="确定要删除这个备份吗？" @confirm="handleDelete(record)">
                                <a-button type="link" danger>
                                    <template #icon>
                                        <DeleteOutlined />
                                    </template>
                                    删除
                                </a-button>
                            </a-popconfirm>
                        </a-space>
                    </template>
                </template>
            </a-table>
        </a-card>

        <!-- 恢复确认对话框 -->
        <a-modal v-model:visible="restoreModal.visible" title="恢复确认" @ok="handleRestoreConfirm"
            :confirmLoading="restoreModal.loading">
            <p>确定要恢复到此备份吗？恢复过程中系统将暂停服务，请谨慎操作！</p>
            <a-alert message="警告" description="恢复操作将覆盖当前所有数据，建议先创建一个新的备份。" type="warning" show-icon class="mb-4" />
        </a-modal>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
    PlusOutlined,
    UploadOutlined,
    DownloadOutlined,
    UndoOutlined,
    DeleteOutlined
} from '@ant-design/icons-vue'
import {
    getBackupList,
    createBackup,
    restoreBackup,
    deleteBackup,
    downloadBackup,
    uploadBackup,
    getBackupConfig,
    updateBackupConfig
} from '@/api/backup'
import { downloadFile } from '@/utils/download'

// 加载状态
const loading = ref(false)

// 备份配置
const backupConfig = reactive({
    autoBackup: false,
    backupTime: null,
    retentionDays: 30,
    storagePath: ''
})

// 备份列表
const backupList = ref([])

// 分页配置
const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`
})

// 恢复确认模态框
const restoreModal = reactive({
    visible: false,
    loading: false,
    record: null
})

// 表格列定义
const columns = [
    {
        title: '备份名称',
        dataIndex: 'name',
        key: 'name'
    },
    {
        title: '备份类型',
        dataIndex: 'type',
        key: 'type',
        width: 100
    },
    {
        title: '文件大小',
        dataIndex: 'size',
        key: 'size',
        width: 120
    },
    {
        title: '创建时间',
        dataIndex: 'createTime',
        key: 'createTime',
        width: 180
    },
    {
        title: '操作',
        key: 'action',
        width: 250,
        fixed: 'right'
    }
]

// 格式化文件大小
const formatFileSize = (size) => {
    if (size < 1024) {
        return size + ' B'
    } else if (size < 1024 * 1024) {
        return (size / 1024).toFixed(2) + ' KB'
    } else if (size < 1024 * 1024 * 1024) {
        return (size / (1024 * 1024)).toFixed(2) + ' MB'
    } else {
        return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
    }
}

// 获取备份列表
const fetchBackupList = async () => {
    loading.value = true
    try {
        const res = await getBackupList({
            page: pagination.current,
            size: pagination.pageSize
        })
        backupList.value = res.data.records
        pagination.total = res.data.total
    } catch (error) {
        console.error('获取备份列表失败:', error)
        message.error('获取备份列表失败')
    } finally {
        loading.value = false
    }
}

// 获取备份配置
const fetchBackupConfig = async () => {
    try {
        const res = await getBackupConfig()
        Object.assign(backupConfig, res.data)
    } catch (error) {
        console.error('获取备份配置失败:', error)
        message.error('获取备份配置失败')
    }
}

// 保存配置
const handleSaveConfig = async () => {
    try {
        await updateBackupConfig(backupConfig)
        message.success('保存配置成功')
    } catch (error) {
        console.error('保存配置失败:', error)
        message.error('保存配置失败')
    }
}

// 创建备份
const handleCreateBackup = async () => {
    try {
        await createBackup()
        message.success('创建备份成功')
        fetchBackupList()
    } catch (error) {
        console.error('创建备份失败:', error)
        message.error('创建备份失败')
    }
}

// 上传备份
const handleUploadBackup = async ({ file }) => {
    try {
        await uploadBackup(file)
        message.success('上传备份成功')
        fetchBackupList()
    } catch (error) {
        console.error('上传备份失败:', error)
        message.error('上传备份失败')
    }
}

// 下载备份
const handleDownload = async (record) => {
    try {
        const res = await downloadBackup(record.id)
        downloadFile(res, record.name)
        message.success('下载成功')
    } catch (error) {
        console.error('下载备份失败:', error)
        message.error('下载备份失败')
    }
}

// 恢复备份
const handleRestore = (record) => {
    restoreModal.record = record
    restoreModal.visible = true
}

// 确认恢复
const handleRestoreConfirm = async () => {
    restoreModal.loading = true
    try {
        await restoreBackup(restoreModal.record.id)
        message.success('恢复成功，系统将在5秒后刷新')
        restoreModal.visible = false
        setTimeout(() => {
            window.location.reload()
        }, 5000)
    } catch (error) {
        console.error('恢复备份失败:', error)
        message.error('恢复备份失败')
    } finally {
        restoreModal.loading = false
    }
}

// 删除备份
const handleDelete = async (record) => {
    try {
        await deleteBackup(record.id)
        message.success('删除成功')
        fetchBackupList()
    } catch (error) {
        console.error('删除备份失败:', error)
        message.error('删除备份失败')
    }
}

// 处理表格变化
const handleTableChange = (pag) => {
    pagination.current = pag.current
    pagination.pageSize = pag.pageSize
    fetchBackupList()
}

// 初始化
onMounted(() => {
    fetchBackupConfig()
    fetchBackupList()
})
</script>

<style scoped>
.backup-container {
    max-width: 1200px;
    margin: 0 auto;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    min-height: 100vh;
    padding: 24px;
}

.mb-4 {
    margin-bottom: 24px;
}

.table-toolbar {
    display: flex;
    justify-content: flex-end;
}
</style>