<template>
  <div class="discussion-list">
    <div class="page-header">
      <div class="header-title">
        <el-icon style="margin-right:8px;"><i class="el-icon-chat-dot-square"></i></el-icon>
        <span>讨论管理</span>
      </div>
      <el-button type="primary" @click="handleAdd" v-if="store.getUserRole !== 'STUDENT'" round icon="el-icon-plus">新建讨论</el-button>
    </div>

    <el-card class="search-bar-card">
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题"
            clearable
            @keyup.enter="handleQuery"
              class="search-input"
          />
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="handleQuery" round>查询</el-button>
            <el-button @click="resetQuery" round>重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    </el-card>

    <el-card class="discussion-table-card">
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
        :row-class-name="()=>'table-row'"
    >
      <el-table-column label="标题" prop="title" min-width="200">
        <template #default="{ row }">
            <router-link :to="'/discussion/detail/'+row.id" class="link-type">{{ row.title }}</router-link>
        </template>
      </el-table-column>
      <el-table-column label="创建人" prop="creatorName" />
      <el-table-column label="截止时间">
        <template #default="{ row }">
          {{ dayjs(row.deadline).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间">
        <template #default="{ row }">
          {{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column v-if="store.getUserRole !== 'STUDENT'" label="操作" fixed="right">
        <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" round>编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)" round>删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </el-card>

    <el-card class="pagination-card">
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 30, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新建讨论' : '编辑讨论'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入讨论内容"
          />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiscussionList, createDiscussion, updateDiscussion, deleteDiscussion } from '@/api/discussion'
import dayjs from 'dayjs'
import { useAuthStore } from '../../stores/authStore';

const store = useAuthStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()

const queryParams = reactive({
  title: '',
  pageNum: 1,
  pageSize: 10
})

const form = ref({
  title: '',
  content: '',
  deadline: null
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const { data: { records, total: totalCount } } = await getDiscussionList({
      page: queryParams.pageNum,
      size: queryParams.pageSize,
      title: queryParams.title || undefined
    })
    tableData.value = records
    total.value = totalCount
  } catch (error) {
    console.error('获取讨论列表失败:', error)
    ElMessage.error('获取讨论列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadData()
}

const resetQuery = () => {
  queryParams.title = ''
  handleQuery()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadData()
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    title: '',
    content: '',
    deadline: null
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    deadline: row.deadline
  }
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该讨论吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDiscussion(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const submitData = {
          ...form.value,
          deadline: form.value.deadline.replace(' ', 'T'),
          creatorRole: store.getUserRole === 'STUDENT' ? 'STUDENT' : 'ADMIN'
        }
        
        if (dialogType.value === 'add') {
          await createDiscussion(submitData)
          ElMessage.success('创建成功')
        } else {
          await updateDiscussion(form.value.id, submitData)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.discussion-list {
  padding: 20px;
  background: #f6f8fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding: 12px 0 8px 0;
}

.header-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
}

.search-bar-card {
  border-radius: 10px;
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.08);
  margin-bottom: 18px;
  background: #fff;
}

.search-bar {
  margin-bottom: 0;
  background-color: transparent;
  padding: 10px 0 0 0;
  border-radius: 0;
  box-shadow: none;
}

.search-input >>> input {
  border-radius: 8px;
  font-size: 1.08em;
}

.discussion-table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(64,158,255,0.08);
  margin-bottom: 24px;
  padding: 0 0 8px 0;
}

/deep/ .el-table {
  border-radius: 10px;
  overflow: hidden;
}

/deep/ .el-table__header th {
  background: #f5f7fa !important;
  color: #606266 !important;
  font-weight: bold;
}

.table-row:hover {
  background: #f0f7ff !important;
}

.link-type {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  transition: text-decoration 0.2s;
}

.link-type:hover {
  text-decoration: underline;
}

.pagination-card {
  border-radius: 10px;
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.06);
  margin-top: 10px;
  padding: 8px 0;
  background: #fff;
}

.el-pagination {
  margin-top: 0;
  justify-content: flex-end;
  padding: 0 12px;
}
</style> 