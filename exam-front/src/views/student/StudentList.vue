<template>
  <div class="student-list">
    <div class="page-header">
      <h2>考生信息管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增考生
        </el-button>
        <el-button type="success" @click="downloadTemplate">
          <el-icon><Download /></el-icon>下载模板
        </el-button>
        <el-upload
          class="upload-demo"
          action=""
          :auto-upload="false"
          :show-file-list="false"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>导入考生
          </el-button>
        </el-upload>
      </div>
    </div>

    <el-card class="search-card" shadow="hover">
      <div class="search-form">
        <div class="form-row">
          <div class="form-item">
            <span class="label">姓名</span>
            <el-input
              v-model="queryParams.name"
              placeholder="请输入姓名"
              clearable
              size="default"
              class="search-input"
              @keyup.enter="handleQuery"
            >
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
          <div class="form-item">
            <span class="label">学号</span>
            <el-input
              v-model="queryParams.studentId"
              placeholder="请输入学号"
              clearable
              size="default"
              class="search-input"
              @keyup.enter="handleQuery"
            >
              <template #prefix><el-icon><Ticket /></el-icon></template>
            </el-input>
          </div>
          <div class="form-item">
            <span class="label">班级</span>
            <el-input
              v-model="queryParams.className"
              placeholder="请输入班级"
              clearable
              size="default"
              class="search-input"
              @keyup.enter="handleQuery"
            >
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
            </el-input>
          </div>
          <div class="form-item">
            <span class="label">性别</span>
            <el-select 
              v-model="queryParams.gender" 
              placeholder="请选择性别" 
              clearable
              size="default"
              class="search-input"
            >
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </div>
        </div>
        <div class="search-buttons">
          <el-button type="primary" @click="handleQuery" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="RefreshRight">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="hover">
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <span class="selected-count">已选择 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button type="danger" size="small" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
        <el-button type="warning" size="small" @click="handleBatchResetPassword">
          <el-icon><Key /></el-icon>批量重置密码
        </el-button>
      </div>

      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="姓名" width="120" :show-overflow-tooltip="true" />
        <el-table-column prop="studentId" label="学号" width="120" :show-overflow-tooltip="true" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? 'info' : 'danger'" size="small">
              {{ row.gender }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" :show-overflow-tooltip="true" min-width="150" />
        <el-table-column prop="phone" label="联系电话" :show-overflow-tooltip="true" min-width="120" />
        <el-table-column prop="email" label="邮箱" :show-overflow-tooltip="true" min-width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" :show-overflow-tooltip="true">
          <template #default="{ row }">
            {{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)" text>
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)" text>
              <el-icon><Delete /></el-icon>删除
            </el-button>
            <el-button size="small" type="warning" @click="handleResetPassword(row)" text>
              <el-icon><Key /></el-icon>重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增考生' : '编辑考生'"
      width="600px"
      destroy-on-close
      draggable
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentId">
              <el-input v-model="form.studentId" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级" prop="className">
              <el-input v-model="form.className" placeholder="请输入班级" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入预览对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入预览"
      width="85%"
      destroy-on-close
    >
      <el-alert
        v-if="importErrors.length > 0"
        type="error"
        :title="'存在 ' + importErrors.length + ' 条错误数据'"
        show-icon
      >
        <template #default>
          <div v-for="(error, index) in importErrors" :key="index" class="error-item">
            第{{ error.row }}行：{{ error.message }}
          </div>
        </template>
      </el-alert>

      <el-table :data="importData" border style="width: 100%; margin-top: 15px;" max-height="400px">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="studentId" label="学号" />
        <el-table-column prop="gender" label="性别" />
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="状态" width="100">
          <template #default="{ row, $index }">
            <el-tag 
              :type="getRowStatus($index).type" 
              effect="light"
              size="small"
            >
              {{ getRowStatus($index).message }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="import-summary" v-if="importData.length > 0">
        <div class="summary-item">
          <span class="label">总记录数：</span>
          <span class="value">{{ importData.length }}</span>
        </div>
        <div class="summary-item">
          <span class="label">有效记录：</span>
          <span class="value">{{ importData.length - importErrors.length }}</span>
        </div>
        <div class="summary-item">
          <span class="label">错误记录：</span>
          <span class="value error-value">{{ importErrors.length }}</span>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleImportSubmit"
            :disabled="importErrors.length > 0"
            :loading="importLoading"
          >
            确认导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, addStudent, updateStudent, deleteStudent, resetPassword, batchImportStudents } from '@/api/student'
import dayjs from 'dayjs'
import * as XLSX from 'xlsx'
import { Plus, Download, Upload, Edit, Delete, Search, RefreshRight, Key, User, Ticket, OfficeBuilding } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const loading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const selectedRows = ref([])

// 导入相关
const importDialogVisible = ref(false)
const importData = ref([])
const importErrors = ref([])

const queryParams = reactive({
  name: '',
  studentId: '',
  className: '',
  phone: '',
  email: '',
  gender: '',
  pageNum: 1,
  pageSize: 10
})

const handleQuery = () => {
  queryParams.pageNum = 1
  loadData()
}

const resetQuery = () => {
  queryParams.name = ''
  queryParams.studentId = ''
  queryParams.className = ''
  queryParams.phone = ''
  queryParams.email = ''
  queryParams.gender = ''
  handleQuery()
}

const form = ref({
  name: '',
  studentId: '',
  gender: '',
  className: '',
  phone: '',
  email: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const { data: { records, total: totalCount} } = await getStudentList({
      page: queryParams.pageNum,
      size: queryParams.pageSize,
      name: queryParams.name || undefined,
      studentId: queryParams.studentId || undefined,
      className: queryParams.className || undefined,
      phone: queryParams.phone || undefined,
      email: queryParams.email || undefined,
      gender: queryParams.gender || undefined
    })
    tableData.value = records
    total.value = totalCount
  } catch (error) {
    console.log(error)
    ElMessage.error('获取考生列表失败')
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

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 名考生信息吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedRows.value.map(row => row.id)
      // 假设这里需要实现批量删除API
      for (const id of ids) {
        await deleteStudent(id)
      }
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

const handleBatchResetPassword = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }

  ElMessageBox.confirm(`确认重置选中的 ${selectedRows.value.length} 名考生密码吗？重置后密码将变为：123456`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedRows.value.map(row => row.id)
      // 假设这里需要实现批量重置密码API
      for (const id of ids) {
        await resetPassword(id)
      }
      ElMessage.success('批量重置密码成功')
    } catch (error) {
      ElMessage.error('批量重置密码失败')
    }
  })
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    name: '',
    studentId: '',
    gender: '',
    className: '',
    phone: '',
    email: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该考生信息吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteStudent(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleResetPassword = (row) => {
  ElMessageBox.confirm('确认重置该考生的密码吗？重置后密码将变为：123456', '提示', {
    type: 'warning'
  }).then(async () => {
    await resetPassword(row.id)
    ElMessage.success('密码重置成功')
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await addStudent(form.value)
          ElMessage.success('添加成功')
        } else {
          await updateStudent(form.value.id, form.value)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.log(error)
        ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 下载模板
const downloadTemplate = () => {
  // 创建工作簿
  const wb = XLSX.utils.book_new()
  
  // 模板数据
  const templateData = [
    ['姓名', '学号', '性别', '班级', '联系电话', '邮箱'],
    ['张三', '2021001', '男', '计算机2101', '13800138000', 'zhangsan@example.com'],
    ['李四', '2021002', '女', '计算机2101', '13800138001', 'lisi@example.com']
  ]
  
  // 创建工作表
  const ws = XLSX.utils.aoa_to_sheet(templateData)
  
  // 将工作表添加到工作簿
  XLSX.utils.book_append_sheet(wb, ws, '考生信息模板')
  
  // 下载文件
  XLSX.writeFile(wb, '考生信息导入模板.xlsx')
}

// 验证导入数据
const validateImportData = (data) => {
  const errors = []
  const phoneRegex = /^1[3-9]\d{9}$/
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  
  data.forEach((row, index) => {
    // 跳过表头
    if (index === 0) return
    
    // 验证必填字段
    if (!row.name) {
      errors.push({ row: index + 1, message: '姓名不能为空' })
    }
    if (!row.studentId) {
      errors.push({ row: index + 1, message: '学号不能为空' })
    }
    if (!row.gender) {
      errors.push({ row: index + 1, message: '性别不能为空' })
    }
    if (!row.className) {
      errors.push({ row: index + 1, message: '班级不能为空' })
    }
    
    // 验证格式
    if (row.phone && !phoneRegex.test(row.phone)) {
      errors.push({ row: index + 1, message: '手机号格式不正确' })
    }
    if (row.email && !emailRegex.test(row.email)) {
      errors.push({ row: index + 1, message: '邮箱格式不正确' })
    }
    if (row.gender && !['男', '女'].includes(row.gender)) {
      errors.push({ row: index + 1, message: '性别只能是男或女' })
    }
  })
  
  return errors
}

// 获取导入行的状态
const getRowStatus = (index) => {
  const rowErrors = importErrors.value.filter(e => e.row === index + 1)
  if (rowErrors.length > 0) {
    return { type: 'danger', message: '有错误' }
  }
  return { type: 'success', message: '有效' }
}

// 处理文件选择
const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      
      // 获取第一个工作表
      const worksheet = workbook.Sheets[workbook.SheetNames[0]]
      
      // 转换为数组
      const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: ['name', 'studentId', 'gender', 'className', 'phone', 'email'] })
      
      // 验证数据
      const errors = validateImportData(jsonData)
      importErrors.value = errors
      
      // 移除表头
      importData.value = jsonData.slice(1)
      
      // 显示预览对话框
      importDialogVisible.value = true
    } catch (error) {
      ElMessage.error('文件解析失败，请确保文件格式正确')
    }
  }
  reader.readAsArrayBuffer(file.raw)
}

// 提交导入
const handleImportSubmit = async () => {
  importLoading.value = true
  try {
    await batchImportStudents(importData.value)
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.student-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  padding: 20px;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.form-item {
  flex: 1;
  min-width: 200px;
  margin-bottom: 10px;
}

.label {
  display: inline-block;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
}

.search-input {
  width: 100%;
}

.search-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

@media (max-width: 768px) {
  .form-item {
    flex: 100%;
  }
  
  .search-buttons {
    justify-content: flex-end;
  }
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.batch-actions {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  background-color: #f0f9eb;
  padding: 10px 15px;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
}

.selected-count {
  margin-right: auto;
  color: #606266;
}

.error-item {
  margin-top: 5px;
  color: #f56c6c;
  font-size: 14px;
}

.import-summary {
  display: flex;
  margin-top: 15px;
  padding: 15px;
  background-color: #f4f4f5;
  border-radius: 4px;
}

.summary-item {
  margin-right: 20px;
  display: flex;
  align-items: center;
}

.summary-item .label {
  color: #606266;
  margin-right: 5px;
}

.summary-item .value {
  font-weight: bold;
  color: #303133;
}

.summary-item .error-value {
  color: #f56c6c;
}

:deep(.el-table .el-table__header-wrapper th) {
  font-weight: bold;
}

:deep(.el-card__body) {
  padding: 15px;
}
</style> 