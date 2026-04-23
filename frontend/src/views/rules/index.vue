<template>
    <div class="rules-container">
        <a-card class="mb-4">
            <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-bold">借阅规则管理</h2>
                <a-button type="primary" @click="showAddRuleModal">
                    <plus-outlined /> 添加规则
                </a-button>
            </div>

            <!-- 当前生效规则 -->
            <div class="current-rule mb-6">
                <h3 class="text-lg font-medium mb-3">当前生效规则</h3>
                <a-descriptions bordered :column="2" size="middle" v-if="currentRule">
                    <a-descriptions-item label="规则名称" :span="2">{{ currentRule.name }}</a-descriptions-item>
                    <a-descriptions-item label="借阅天数">{{ currentRule.borrowDays }} 天</a-descriptions-item>
                    <a-descriptions-item label="最大借阅数量">{{ currentRule.maxBorrowCount }} 本</a-descriptions-item>
                    <a-descriptions-item label="最大续借次数">{{ currentRule.maxRenewCount }} 次</a-descriptions-item>
                    <a-descriptions-item label="逾期罚款(元/天)">{{ currentRule.finePerDay }}
                        元</a-descriptions-item>
                    <a-descriptions-item label="创建时间">{{ currentRule.createTime }}</a-descriptions-item>
                    <a-descriptions-item label="状态">
                        <a-tag color="green">生效中</a-tag>
                    </a-descriptions-item>
                </a-descriptions>
                <a-empty v-else description="暂无生效规则" />
            </div>

            <!-- 规则列表 -->
            <div class="rule-list">
                <h3 class="text-lg font-medium mb-3">所有规则</h3>
                <a-table :columns="columns" :data-source="rules" :pagination="pagination" :loading="loading"
                    @change="handleTableChange" row-key="id" :bordered="true">
                    <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'status'">
                            <a-tag :color="record.status === 1 ? 'green' : 'default'">
                                {{ record.status === 1 ? '生效中' : '未生效' }}
                            </a-tag>
                        </template>
                        <template v-if="column.key === 'action'">
                            <a-space>
                                <a-button v-if="record.status === 0" type="primary" size="small"
                                    @click="handleEnableRule(record.id)">
                                    启用
                                </a-button>
                                <a-button v-if="record.status === 1" type="danger" size="small"
                                    @click="handleDisableRule(record.id)" ghost>
                                    禁用
                                </a-button>
                                <a-button type="default" size="small" @click="handleEditRule(record)">
                                    编辑
                                </a-button>
                            </a-space>
                        </template>
                    </template>
                </a-table>
            </div>
        </a-card>

        <!-- 添加/编辑规则对话框 -->
        <a-modal :title="isEdit ? '编辑规则' : '添加规则'" :visible="ruleModalVisible" :confirm-loading="confirmLoading"
            @ok="handleRuleModalOk" @cancel="handleRuleModalCancel" width="600px">
            <a-form :model="ruleForm" :rules="rules" ref="ruleFormRef" layout="vertical">
                <a-form-item name="name" label="规则名称">
                    <a-input v-model:value="ruleForm.name" placeholder="请输入规则名称" />
                </a-form-item>

                <a-form-item name="borrowDays" label="借阅天数">
                    <a-input-number v-model:value="ruleForm.borrowDays" :min="1" :max="365" style="width: 100%"
                        placeholder="请输入借阅天数" />
                </a-form-item>

                <a-form-item name="maxBorrowCount" label="最大借阅数量">
                    <a-input-number v-model:value="ruleForm.maxBorrowCount" :min="1" :max="100" style="width: 100%"
                        placeholder="请输入最大借阅数量" />
                </a-form-item>

                <a-form-item name="maxRenewCount" label="最大续借次数">
                    <a-input-number v-model:value="ruleForm.maxRenewCount" :min="0" :max="10" style="width: 100%"
                        placeholder="请输入最大续借次数" />
                </a-form-item>

                <a-form-item name="finePerDay" label="逾期罚款(元/天)">
                    <a-input-number v-model:value="ruleForm.finePerDay" :min="0" :max="100" :step="0.1" :precision="2"
                        style="width: 100%" placeholder="请输入逾期罚款金额" />
                </a-form-item>

                <a-form-item name="description" label="规则描述">
                    <a-textarea v-model:value="ruleForm.description" placeholder="请输入规则描述" :rows="4" />
                </a-form-item>
            </a-form>
        </a-modal>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import {
    getCurrentRule,
    getRuleList,
    addRule,
    updateRule,
    enableRule,
    disableRule
} from '@/api/rule'

const loading = ref(false)
const rules = ref([])
const currentRule = ref(null)
const ruleFormRef = ref(null)
const ruleModalVisible = ref(false)
const confirmLoading = ref(false)
const isEdit = ref(false)

// 分页
const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`
})

// 规则表单
const ruleForm = reactive({
    id: null,
    name: '',
    borrowDays: 30,
    maxBorrowCount: 5,
    maxRenewCount: 1,
    finePerDay: 0.5,
    description: ''
})

// 表单验证规则
const formRules = {
    name: [{ required: true, message: '请输入规则名称' }],
    borrowDays: [{ required: true, message: '请输入借阅天数' }],
    maxBorrowCount: [{ required: true, message: '请输入最大借阅数量' }],
    maxRenewCount: [{ required: true, message: '请输入最大续借次数' }],
    finePerDay: [{ required: true, message: '请输入逾期罚款金额' }]
}

// 表格列定义
const columns = [
    {
        title: '规则名称',
        dataIndex: 'name',
        key: 'name',
        ellipsis: true
    },
    {
        title: '借阅天数',
        dataIndex: 'borrowDays',
        key: 'borrowDays',
        width: 100
    },
    {
        title: '最大借阅数量',
        dataIndex: 'maxBorrowCount',
        key: 'maxBorrowCount',
        width: 120
    },
    {
        title: '最大续借次数',
        dataIndex: 'maxRenewCount',
        key: 'maxRenewCount',
        width: 120
    },
    {
        title: '逾期罚款(元/天)',
        dataIndex: 'finePerDay',
        key: 'finePerDay',
        width: 140
    },
    {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 100
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
        width: 180
    }
]

// 获取当前生效规则
const fetchCurrentRule = async () => {
    try {
        const res = await getCurrentRule()
        currentRule.value = res.data
    } catch (error) {
        console.error('获取当前生效规则失败:', error)
        message.error('获取当前生效规则失败，请稍后重试')
    }
}

// 获取规则列表
const fetchRules = async () => {
    loading.value = true
    try {
        const res = await getRuleList()
        rules.value = res.data || []

        // 确保状态字段正确
        rules.value.forEach(rule => {
            // 如果后端返回的是 enabled 字段而不是 status 字段，进行转换
            if (rule.enabled !== undefined && rule.status === undefined) {
                rule.status = rule.enabled ? 1 : 0
            }
        })

        pagination.total = rules.value.length

        // 手动分页
        const start = (pagination.current - 1) * pagination.pageSize
        const end = start + pagination.pageSize
        rules.value = rules.value.slice(start, end)
    } catch (error) {
        console.error('获取规则列表失败:', error)
        message.error('获取规则列表失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

// 表格变化
const handleTableChange = (pag) => {
    pagination.current = pag.current
    pagination.pageSize = pag.pageSize
    fetchRules()
}

// 显示添加规则对话框
const showAddRuleModal = () => {
    isEdit.value = false
    resetRuleForm()
    ruleModalVisible.value = true
}

// 重置规则表单
const resetRuleForm = () => {
    ruleForm.id = null
    ruleForm.name = ''
    ruleForm.borrowDays = 30
    ruleForm.maxBorrowCount = 5
    ruleForm.maxRenewCount = 1
    ruleForm.finePerDay = 0.5
    ruleForm.description = ''

    if (ruleFormRef.value) {
        ruleFormRef.value.resetFields()
    }
}

// 处理规则对话框确认
const handleRuleModalOk = () => {
    ruleFormRef.value.validate().then(async () => {
        confirmLoading.value = true
        try {
            if (isEdit.value) {
                await updateRule(ruleForm)
                message.success('更新规则成功')
            } else {
                await addRule(ruleForm)
                message.success('添加规则成功')
            }

            ruleModalVisible.value = false
            fetchRules()
            fetchCurrentRule()
        } catch (error) {
            console.error(isEdit.value ? '更新规则失败:' : '添加规则失败:', error)
            message.error(isEdit.value ? '更新规则失败，请稍后重试' : '添加规则失败，请稍后重试')
        } finally {
            confirmLoading.value = false
        }
    })
}

// 处理规则对话框取消
const handleRuleModalCancel = () => {
    ruleModalVisible.value = false
    resetRuleForm()
}

// 处理编辑规则
const handleEditRule = (record) => {
    isEdit.value = true

    // 填充表单数据
    Object.keys(ruleForm).forEach(key => {
        if (record[key] !== undefined) {
            ruleForm[key] = record[key]
        }
    })

    ruleModalVisible.value = true
}

// 启用规则
const handleEnableRule = async (id) => {
    try {
        await enableRule(id)
        message.success('规则启用成功')
        fetchRules()
        fetchCurrentRule()
    } catch (error) {
        console.error('启用规则失败:', error)
        message.error('启用规则失败，请稍后重试')
    }
}

// 禁用规则
const handleDisableRule = async (id) => {
    try {
        await disableRule(id)
        message.success('规则禁用成功')
        fetchRules()
        fetchCurrentRule()
    } catch (error) {
        console.error('禁用规则失败:', error)
        message.error('禁用规则失败，请稍后重试')
    }
}

onMounted(() => {
    fetchCurrentRule()
    fetchRules()
})
</script>

<style scoped>
.rules-container {
    max-width: 1200px;
    margin: 0 auto;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    min-height: 100vh;
    padding: 20px;
}

.rules-container :deep(.ant-table-bordered .ant-table-cell) {
    border-color: #b4d3f7;
}

.rules-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
    border-color: #b4d3f7;
}

.rules-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
    border-color: #b4d3f7;
}

/* 表格样式 */
.rules-container :deep(.ant-table) {
    border-radius: 8px;
    overflow: hidden;
}

.rules-container :deep(.ant-table-thead > tr > th) {
    background-color: #f0f8ff;
    font-weight: 600;
}

.rules-container :deep(.ant-table-tbody > tr:hover > td) {
    background-color: #f0f8ff;
}

.rules-container :deep(.ant-table-tbody > tr > td) {
    transition: background-color 0.3s ease;
}

/* 当前生效规则样式 */
.current-rule {
    background: #f0f8ff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 规则列表样式 */
.rule-list {
    background: #f0f8ff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>