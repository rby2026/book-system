<template>
  <div class="announcements-container">
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold">公告管理</h1>
      <a-button type="primary" @click="showAddModal">
        <template #icon>
          <PlusOutlined />
        </template>
        添加公告
      </a-button>
    </div>

    <a-card>
      <a-form :model="searchForm" layout="inline" @submit.prevent="handleSearch">
        <a-form-item label="标题">
          <a-input v-model:value="searchForm.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="0">草稿</a-select-option>
            <a-select-option value="1">已发布</a-select-option>
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
        :data-source="announcements"
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
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'blue'">
              {{ record.status === 1 ? '已发布' : '草稿' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'publishTime'">
            {{ record.publishTime ? formatDate(record.publishTime) : '-' }}
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑公告模态框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item label="标题" :required="true">
          <a-input v-model:value="form.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="内容" :required="true">
          <a-textarea v-model:value="form.content" placeholder="请输入公告内容" rows="6" />
        </a-form-item>
        <a-form-item label="发布者">
          <a-input v-model:value="form.publisher" placeholder="请输入发布者" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option value="0">草稿</a-select-option>
            <a-select-option value="1">已发布</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import * as announcementApi from '@/api/announcement'

// 表格数据
const announcements = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = ref({
  title: '',
  status: ''
})

// 模态框
const modalVisible = ref(false)
const modalTitle = ref('添加公告')
const form = ref({
  id: '',
  title: '',
  content: '',
  publisher: '',
  status: 1
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
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    ellipsis: true
  },
  {
    title: '发布者',
    dataIndex: 'publisher',
    key: 'publisher',
    width: 120
  },
  {
    title: '发布时间',
    dataIndex: 'publishTime',
    key: 'publishTime',
    width: 180
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
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 获取公告列表
const getAnnouncementList = async () => {
  try {
    const response = await announcementApi.getAnnouncementList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: searchForm.value.title,
      status: searchForm.value.status
    })
    announcements.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    message.error('获取公告列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  getAnnouncementList()
}

// 重置
const resetForm = () => {
  searchForm.value = {
    title: '',
    status: ''
  }
  pageNum.value = 1
  getAnnouncementList()
}

// 表格变化
const handleTableChange = (pagination) => {
  pageNum.value = pagination.current
  pageSize.value = pagination.pageSize
  getAnnouncementList()
}

// 显示添加模态框
const showAddModal = () => {
  form.value = {
    id: '',
    title: '',
    content: '',
    publisher: '',
    status: 1
  }
  modalTitle.value = '添加公告'
  modalVisible.value = true
}

// 显示编辑模态框
const showEditModal = (record) => {
  form.value = { ...record }
  modalTitle.value = '编辑公告'
  modalVisible.value = true
}

// 处理确定
const handleOk = async () => {
  try {
    if (form.value.id) {
      // 编辑
      await announcementApi.updateAnnouncement(form.value)
      message.success('编辑公告成功')
    } else {
      // 添加
      await announcementApi.addAnnouncement(form.value)
      message.success('添加公告成功')
    }
    modalVisible.value = false
    getAnnouncementList()
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
    content: '确定要删除这条公告吗？',
    onOk: async () => {
      try {
        await announcementApi.deleteAnnouncement(id)
        message.success('删除公告成功')
        getAnnouncementList()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

// 初始化
onMounted(() => {
  getAnnouncementList()
})
</script>

<style scoped>
.announcements-container {
  max-width: 1200px;
  margin: 0 auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
}

.announcements-container :deep(.ant-table-bordered .ant-table-cell) {
  border-color: #b4d3f7;
}

.announcements-container :deep(.ant-table-bordered .ant-table-tbody > tr > td) {
  border-color: #b4d3f7;
}

.announcements-container :deep(.ant-table-bordered .ant-table-thead > tr > th) {
  border-color: #b4d3f7;
}
</style>
