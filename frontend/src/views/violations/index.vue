<template>
  <div class="violations-container">
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold">违章管理</h1>
      <a-button type="primary" @click="showAddModal">
        <template #icon>
          <PlusOutlined />
        </template>
        添加违章
      </a-button>
    </div>

    <a-card>
      <a-form :model="searchForm" layout="inline" @submit.prevent="handleSearch">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="违章类型">
          <a-select v-model:value="searchForm.violationType" placeholder="请选择违章类型">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="1">未按时归还</a-select-option>
            <a-select-option value="2">损坏图书</a-select-option>
            <a-select-option value="3">丢失图书</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="处理状态">
          <a-select v-model:value="searchForm.processStatus" placeholder="请选择处理状态">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="0">未处理</a-select-option>
            <a-select-option value="1">已处理</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">
            <template #icon>
              <SearchOutlined />
            </template>
            搜索
          </a-button>
        </a-form-item>
        <a-form-item>
          <a-button @click="resetForm">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置
          </a-button>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="violations"
        row-key="id"
        :pagination="{
          current: pageNum,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条记录`
        }"
        @change="handleTableChange"
        :bordered="true"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'actions'">
            <a-button size="small" @click="showEditModal(record)">
              <template #icon>
                <EditOutlined />
              </template>
              编辑
            </a-button>
            <a-button size="small" danger @click="handleDelete(record.id)">
              <template #icon>
                <DeleteOutlined />
              </template>
              删除
            </a-button>
          </template>
          <template v-else-if="column.key === 'violationType'">
            <span>
              {{ getViolationTypeText(record.violationType) }}
            </span>
          </template>
          <template v-else-if="column.key === 'processStatus'">
            <a-tag :color="record.processStatus === 1 ? 'green' : 'blue'">
              {{ record.processStatus === 1 ? '已处理' : '未处理' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'violationTime'">
            {{ formatDate(record.violationTime) }}
          </template>
          <template v-else-if="column.key === 'processTime'">
            {{ record.processTime ? formatDate(record.processTime) : '-' }}
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑违章模态框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item label="用户ID" :required="true">
          <a-input v-model:value="form.userId" placeholder="请输入用户ID" />
        </a-form-item>
        <a-form-item label="用户名" :required="true">
          <a-input v-model:value="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="违章类型" :required="true">
          <a-select v-model:value="form.violationType">
            <a-select-option value="1">未按时归还</a-select-option>
            <a-select-option value="2">损坏图书</a-select-option>
            <a-select-option value="3">丢失图书</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述" :required="true">
          <a-textarea v-model:value="form.description" placeholder="请输入违章描述" rows="4" />
        </a-form-item>
        <a-form-item label="图书ID">
          <a-input v-model:value="form.bookId" placeholder="请输入图书ID" />
        </a-form-item>
        <a-form-item label="图书标题">
          <a-input v-model:value="form.bookTitle" placeholder="请输入图书标题" />
        </a-form-item>
        <a-form-item label="罚款金额">
          <a-input-number v-model:value="form.fine" placeholder="请输入罚款金额" />
        </a-form-item>
        <a-form-item label="处理状态">
          <a-select v-model:value="form.processStatus">
            <a-select-option value="0">未处理</a-select-option>
            <a-select-option value="1">已处理</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import * as violationApi from '@/api/violation'

// 表格数据
const violations = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = ref({
  username: '',
  violationType: '',
  processStatus: ''
})

// 模态框
const modalVisible = ref(false)
const modalTitle = ref('添加违章')
const form = ref({
  id: '',
  userId: '',
  username: '',
  violationType: 1,
  description: '',
  bookId: '',
  bookTitle: '',
  fine: 0,
  processStatus: 0
})

// 表格列
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 100
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 120
  },
  {
    title: '违章类型',
    dataIndex: 'violationType',
    key: 'violationType',
    width: 120
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '图书标题',
    dataIndex: 'bookTitle',
    key: 'bookTitle',
    ellipsis: true
  },
  {
    title: '罚款金额',
    dataIndex: 'fine',
    key: 'fine',
    width: 100
  },
  {
    title: '处理状态',
    dataIndex: 'processStatus',
    key: 'processStatus',
    width: 100
  },
  {
    title: '违章时间',
    dataIndex: 'violationTime',
    key: 'violationTime',
    width: 180
  },
  {
    title: '处理时间',
    dataIndex: 'processTime',
    key: 'processTime',
    width: 180
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 获取违章类型文本
const getViolationTypeText = (type) => {
  const typeMap = {
    1: '未按时归还',
    2: '损坏图书',
    3: '丢失图书'
  }
  return typeMap[type] || '未知类型'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 获取违章信息列表
const getViolationList = async () => {
  try {
    const response = await violationApi.getViolationList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: searchForm.value.username,
      violationType: searchForm.value.violationType,
      processStatus: searchForm.value.processStatus
    })
    violations.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    message.error('获取违章信息列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  getViolationList()
}

// 重置
const resetForm = () => {
  searchForm.value = {
    username: '',
    violationType: '',
    processStatus: ''
  }
  pageNum.value = 1
  getViolationList()
}

// 表格变化
const handleTableChange = (pagination) => {
  pageNum.value = pagination.current
  pageSize.value = pagination.pageSize
  getViolationList()
}

// 显示添加模态框
const showAddModal = () => {
  form.value = {
    id: '',
    userId: '',
    username: '',
    violationType: 1,
    description: '',
    bookId: '',
    bookTitle: '',
    fine: 0,
    processStatus: 0
  }
  modalTitle.value = '添加违章'
  modalVisible.value = true
}

// 显示编辑模态框
const showEditModal = (record) => {
  form.value = { ...record }
  modalTitle.value = '编辑违章'
  modalVisible.value = true
}

// 处理确定
const handleOk = async () => {
  try {
    if (form.value.id) {
      // 编辑
      await violationApi.updateViolation(form.value)
      message.success('编辑违章成功')
    } else {
      // 添加
      await violationApi.addViolation(form.value)
      message.success('添加违章成功')
    }
    modalVisible.value = false
    getViolationList()
  } catch (error) {
    message.error('操作失败')
  }
}

// 处理取消
const handleCancel = () => {
  modalVisible.value = false
}

// 处理删除
const handleDelete = (id) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条违章信息吗？',
    onOk: async () => {
      try {
        await violationApi.deleteViolation(id)
        message.success('删除违章成功')
        getViolationList()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

// 初始化
onMounted(() => {
  getViolationList()
})
</script>

<style scoped>
.violations-container {
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
}

.violations-container :deep(.ant-table-bordered .ant-table-cell) {
  border-color: #b4d3f7;
}

.violations-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
  border-color: #b4d3f7;
}

.violations-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
  border-color: #b4d3f7;
}
</style>
