<template>
  <div class="question-type-manage">
    <div class="page-header">
      <div class="header-title">
        <el-icon style="margin-right:8px;"><i class="el-icon-document-checked"></i></el-icon>
        <span>题型管理</span>
      </div>
      <el-button type="primary" @click="handleAdd" round icon="el-icon-plus">新增题型</el-button>
    </div>

    <el-card class="search-card">
      <div class="search-bar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="题型名称">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入题型名称"
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

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="typeList"
        border
        style="width: 100%"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
        :row-class-name="()=>'table-row'"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="题型名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column label="类型标识" min-width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"
              :disabled="row.isSystemDefault"
              round>编辑</el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)" 
              :disabled="row.isSystemDefault"
              round
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑题型对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增题型' : '编辑题型'"
      width="500px"
      class="type-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入题型名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入题型描述"
          />
        </el-form-item>
        <el-form-item label="类型标识" prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="请输入题型标识，如SINGLE_CHOICE"
            :disabled="dialogType === 'edit' && form.isSystemDefault"
          />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch
            v-model="form.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" round>取消</el-button>
          <el-button type="primary" @click="handleSubmit" round>确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestionTypeList, createQuestionType, updateQuestionType, deleteQuestionType } from '@/api/questionType'

const loading = ref(false)
const typeList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()

const queryParams = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10
})

const form = ref({
  name: '',
  description: '',
  code: '',
  sortOrder: 1,
  enabled: true,
  isSystemDefault: false
})

const rules = {
  name: [{ required: true, message: '请输入题型名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入题型标识', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getQuestionTypeList({
      name: queryParams.name,
      page: queryParams.pageNum,
      size: queryParams.pageSize
    })
    typeList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取题型列表失败:', error)
    ElMessage.error('获取题型列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadData()
}

const resetQuery = () => {
  queryParams.name = ''
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
    name: '',
    description: '',
    code: '',
    sortOrder: 1,
    enabled: true,
    isSystemDefault: false
  }
  dialogVisible.value = true
  setTimeout(() => {
    formRef.value?.resetFields()
  }, 0)
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  if (row.isSystemDefault) {
    ElMessage.warning('系统默认题型不能删除')
    return
  }
  
  ElMessageBox.confirm(`确认删除题型"${row.name}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteQuestionType(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除题型失败:', error)
      ElMessage.error('删除题型失败')
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await createQuestionType(form.value)
          ElMessage.success('新增题型成功')
        } else {
          await updateQuestionType(form.value.id, form.value)
          ElMessage.success('更新题型成功')
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
.question-type-manage {
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

.search-card {
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

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(64,158,255,0.08);
  margin-bottom: 24px;
  padding: 0 0 20px 0;
}

:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.el-table__header th) {
  background: #f5f7fa !important;
  color: #606266 !important;
  font-weight: bold;
}

.table-row:hover {
  background: #f0f7ff !important;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
  padding-right: 20px;
}

.type-dialog :deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.type-dialog :deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style> 