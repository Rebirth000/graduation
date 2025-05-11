<template>
  <div class="app-container">
    <!-- 顶部操作区 -->
    <el-card class="header-card">
      <div class="header-operations">
        <div class="left-section">
          <h2 class="page-title">考试管理</h2>
          <span class="subtitle">创建和管理所有考试</span>
        </div>
        <div class="right-section">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增考试
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="main-card">
      <el-table
        v-loading="loading"
        :data="examList"
        border
        stripe
        highlight-current-row
        style="width: 100%"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
      >
        <el-table-column prop="name" label="考试名称" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="exam-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="所属课程" min-width="120" show-overflow-tooltip />
        <el-table-column prop="paperName" label="试卷" min-width="120" show-overflow-tooltip />
        <el-table-column label="开始时间" width="170">
          <template #default="scope">
            <el-tag size="small" type="info">
              {{ formatDateTime(scope.row.startTime) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="scope">
            <el-tag size="small" type="info">
              {{ formatDateTime(scope.row.endTime) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getExamStatusType(scope.row)" size="small">
              {{ getExamStatus(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-tooltip content="分配考场" placement="top">
                <el-button circle type="primary" @click="handleAssignRoom(scope.row)" size="small">
                  <el-icon><Location /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button circle type="warning" @click="handleEdit(scope.row)" size="small">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button circle type="danger" @click="handleDelete(scope.row)" size="small">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 添加/编辑考试对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="550px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="exam-form"
      >
        <el-form-item label="考试名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select 
            v-model="form.courseId" 
            placeholder="请选择课程" 
            style="width: 100%"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="试卷" prop="paperId">
          <el-select 
            v-model="form.paperId" 
            placeholder="请选择试卷" 
            style="width: 100%"
            :disabled="!form.courseId"
          >
            <el-option
              v-for="paper in paperList"
              :key="paper.id"
              :label="paper.title"
              :value="paper.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllExamPapers, getExamList, addExam, updateExam, deleteExam, getAssociatedPapers } from '@/api/exam'
import { getAllCourses } from '@/api/course'
import { formatDateTime } from '@/utils/format'
import { Plus, Edit, Delete, Location } from '@element-plus/icons-vue'

const router = useRouter()

// 数据
const loading = ref(false)
const examList = ref([])
const paperList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const courseList = ref([])
const form = ref({
  id: null,
  name: '',
  courseId: null,
  paperId: null,
  startTime: null,
  endTime: null
})

// 计算考试状态
const getExamStatus = (exam) => {
  const now = new Date()
  const startTime = new Date(exam.startTime)
  const endTime = new Date(exam.endTime)
  
  if (now < startTime) {
    return '未开始'
  } else if (now >= startTime && now <= endTime) {
    return '进行中'
  } else {
    return '已结束'
  }
}

// 获取考试状态标签类型
const getExamStatusType = (exam) => {
  const status = getExamStatus(exam)
  switch (status) {
    case '未开始':
      return 'info'
    case '进行中':
      return 'success'
    case '已结束':
      return 'warning'
    default:
      return 'info'
  }
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入考试名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  paperId: [
    { required: true, message: '请选择试卷', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && form.value.startTime && value <= form.value.startTime) {
          callback(new Error('结束时间必须大于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 获取考试列表
const loadExamList = async () => {
  loading.value = true
  try {
    const { data: { records, total: totalCount} } = await getExamList({
      page: page.value,
      size: size.value
    })
    examList.value = records
    total.value = totalCount
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// 获取试卷列表
const loadPaperList = async () => {
  try {
    const { data } = await getAllExamPapers()
    paperList.value = data
  } catch (error) {
    ElMessage.error('获取试卷列表失败')
  }
}

// 获取课程列表
const loadCourseList = async () => {
  try {
    const { data } = await getAllCourses()
    courseList.value = data
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  }
}

// 课程选择改变时获取相关试卷
const handleCourseChange = async (courseId) => {
  if (!courseId) {
    paperList.value = []
    form.value.paperId = null
    return
  }
  
  try {
    const { data } = await getAssociatedPapers(courseId)
    paperList.value = data
    form.value.paperId = null // 重置试卷选择
  } catch (error) {
    ElMessage.error('获取课程试卷失败')
    paperList.value = []
  }
}

// 新增考试
const handleAdd = () => {
  form.value = {
    id: null,
    name: '',
    courseId: null,
    paperId: null,
    startTime: null,
    endTime: null
  }
  paperList.value = [] // 清空试卷列表
  dialogTitle.value = '新增考试'
  dialogVisible.value = true
  loadCourseList()
}

// 编辑考试
const handleEdit = async (row) => {
  dialogTitle.value = '编辑考试'
  // 先加载课程列表
  await loadCourseList()
  
  // 如果有课程ID，先加载相关试卷
  if (row.courseId) {
    try {
      const { data } = await getAssociatedPapers(row.courseId)
      paperList.value = data
    } catch (error) {
      ElMessage.error('获取课程试卷失败')
      paperList.value = []
    }
  } else {
    paperList.value = []
  }
  
  // 设置表单数据
  form.value = {
    ...row,
    startTime: formatDateTime(row.startTime),
    endTime: formatDateTime(row.endTime)
  }
  
  dialogVisible.value = true
}

// 分配考场
const handleAssignRoom = (row) => {
  router.push(`/exam/assign-room/${row.id}`)
}

// 删除考试
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该考试吗？删除后将无法恢复。',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true,
      icon: 'Warning'
    }
  ).then(async () => {
    try {
      await deleteExam(row.id)
      ElMessage.success('删除成功')
      loadExamList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const submitForm = async () => {
  try {
    const data = { ...form.value }
    // 转换时间格式
    data.startTime = data.startTime.replace(' ', 'T')
    data.endTime = data.endTime.replace(' ', 'T')
    
    if (data.id) {
      await updateExam(data.id, data)
      ElMessage.success('更新成功')
    } else {
      await addExam(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadExamList()
  } catch (error) {
    ElMessage.error(form.value.id ? '更新失败' : '添加失败')
  }
}

const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  loadExamList()
}

const handleCurrentChange = (val) => {
  page.value = val
  loadExamList()
}

// 页面加载时获取数据
onMounted(() => {
  loadExamList()
  loadCourseList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-operations {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-section {
  display: flex;
  flex-direction: column;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.right-section {
  display: flex;
  gap: 10px;
}

.main-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.exam-name {
  font-weight: 500;
  color: #409EFF;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.exam-form .el-date-editor.el-input {
  width: 100%;
}
</style> 