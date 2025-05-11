<template>
  <div class="announcement-list">
    <div class="page-header">
      <h2>公告管理</h2>
      <div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>发布公告
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题"
            clearable
            prefix-icon="Search"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-card shadow="hover">
        <el-table 
          :data="tableData" 
          border 
          style="width: 100%" 
          :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
          row-key="id"
          v-loading="loading"
        >
          <el-table-column prop="title" label="标题" width="300" show-overflow-tooltip />
          <el-table-column prop="content" label="内容摘要" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <div>{{ formatContent(row.content) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="isRecent(row.createdAt) ? 'success' : 'info'"
              >
                {{ isRecent(row.createdAt) ? '最新' : '常规' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发布时间" width="180" align="center">
            <template #default="{ row }">
              {{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm') }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 查看公告详情 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="公告详情"
      width="700px"
      destroy-on-close
    >
      <div class="announcement-detail">
        <h2 class="announcement-title">{{ currentAnnouncement.title }}</h2>
        <div class="announcement-meta">
          <span>发布时间：{{ dayjs(currentAnnouncement.createdAt).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </div>
        <div class="announcement-content" v-html="currentAnnouncement.content"></div>
      </div>
    </el-dialog>

    <!-- 编辑/新增公告 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '发布公告' : '编辑公告'"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <!-- 使用常规文本区域，可以替换为富文本编辑器组件 -->
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ dialogType === 'add' ? '发布' : '更新' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncementList, addAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'
import dayjs from 'dayjs'
import { Plus, Search } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const dateRange = ref([])
const currentAnnouncement = ref({})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  }
]

const queryParams = reactive({
  title: '',
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 10
})

const form = ref({
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2-50个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 5, max: 5000, message: '内容长度应在5-5000个字符之间', trigger: 'blur' }
  ]
}

// 判断是否为近期发布的公告（7天内）
const isRecent = (date) => {
  const now = dayjs()
  const createdDate = dayjs(date)
  return now.diff(createdDate, 'day') <= 7
}

// 格式化显示内容摘要
const formatContent = (content) => {
  // 移除HTML标签
  const plainText = content.replace(/<[^>]+>/g, '')
  // 返回前100个字符作为摘要
  return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
}

const handleQuery = () => {
  queryParams.pageNum = 1
  
  // 处理日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
    queryParams.endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
  
  loadData()
}

const resetQuery = () => {
  queryParams.title = ''
  dateRange.value = []
  queryParams.startDate = ''
  queryParams.endDate = ''
  handleQuery()
}

const loadData = async () => {
  loading.value = true
  try {
    const { data: { records, total: totalCount } } = await getAnnouncementList({
      page: queryParams.pageNum,
      size: queryParams.pageSize,
      title: queryParams.title || undefined,
      startDate: queryParams.startDate || undefined,
      endDate: queryParams.endDate || undefined
    })
    tableData.value = records
    total.value = totalCount
  } catch (error) {
    console.log(error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadData()
}

const handleView = (row) => {
  currentAnnouncement.value = { ...row }
  viewDialogVisible.value = true
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    title: '',
    content: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该公告吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAnnouncement(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await addAnnouncement(form.value)
          ElMessage.success('发布成功')
        } else {
          await updateAnnouncement(form.value.id, form.value)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.log(error)
        ElMessage.error(dialogType.value === 'add' ? '发布失败' : '更新失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.announcement-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.search-bar {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 18px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.announcement-detail {
  padding: 10px;
}

.announcement-title {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
}

.announcement-meta {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.announcement-content {
  line-height: 1.8;
  font-size: 15px;
  white-space: pre-wrap;
}

:deep(.el-card__body) {
  padding: 10px;
}

:deep(.el-table .el-table__header-wrapper th) {
  font-weight: bold;
}
</style> 