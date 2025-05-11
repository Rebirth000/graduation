<template>
  <div class="exam-paper-list">
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">新增试卷</el-button>
    </div>

    <div class="filter-container">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索试卷标题或描述"
        class="search-input"
        clearable
        @clear="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select 
        v-model="courseFilter" 
        placeholder="按课程筛选" 
        class="course-filter"
        clearable
        @change="loadData"
      >
        <el-option
          v-for="course in courseList"
          :key="course.id"
          :label="course.name"
          :value="course.id"
        />
      </el-select>

      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetFilters">
        <el-icon><RefreshLeft /></el-icon> 重置
      </el-button>
    </div>

    <el-card class="exam-paper-table" shadow="hover">
      <el-table :data="tableData" border style="width: 100%" v-loading="loading" stripe
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}">
        <el-table-column prop="title" label="试卷标题" min-width="150" />
        <el-table-column prop="description" label="试卷描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" min-width="120">
          <template #default="{ row }">
            {{ row.courseName || '未关联课程' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePreview(row)">
              <el-icon><View /></el-icon> 预览
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button link type="primary" @click="handleManage(row)">
              <el-icon><Document /></el-icon> 管理题目
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑试卷' : '新增试卷'"
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
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="可用题型" prop="questionTypeIds">
          <el-select 
            v-model="form.questionTypeIds" 
            multiple 
            placeholder="请选择试卷可用题型"
            style="width: 100%"
            @remove-tag="handleRemoveTag"
          >
            <el-option
              v-for="type in questionTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
              :disabled="type.isSystemDefault && !form.questionTypeIds.includes(type.id)"
            >
              <span>{{ type.name }}</span>
              <el-tag v-if="type.isSystemDefault" size="small" type="info" effect="plain">默认</el-tag>
            </el-option>
          </el-select>
          <div class="form-help-text">
            <el-alert
              type="info"
              :closable="false"
              size="small"
            >
              一张试卷可以包含多个题型，系统默认题型不可取消选择。
            </el-alert>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 使用共享组件预览试卷 -->
    <exam-paper-preview
      v-model="previewDialogVisible"
      :paper="currentPaper"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getExamPaperList, createExamPaper, updateExamPaper, deleteExamPaper, getExamPaperById, getQuestionList } from '@/api/exam'
import { getAllCourses } from '@/api/course'
import { getQuestionTypeList, getPaperQuestionTypes, updatePaperQuestionTypes } from '@/api/questionType'
import { Search, Edit, Delete, Document, View, RefreshLeft } from '@element-plus/icons-vue'
import ExamPaperPreview from '@/components/ExamPaperPreview.vue'

const router = useRouter()
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const page = ref(1)
const size = ref(10)
const total = ref(0)
const courseList = ref([])
const questionTypes = ref([])
const searchKeyword = ref('')
const courseFilter = ref(null)
const loading = ref(false)
const previewDialogVisible = ref(false)
const currentPaper = ref(null)

const form = ref({
  id: null,
  title: '',
  description: '',
  courseId: null,
  questionTypeIds: []
})

const rules = {
  title: [
    { required: true, message: '请输入试卷标题', trigger: 'blur' },
    { min: 2, message: '标题长度不能小于2个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入试卷描述', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    loading.value = true
    const { data } = await getExamPaperList({
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value,
      courseId: courseFilter.value
    })
    tableData.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取试卷列表失败')
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    const { data } = await getAllCourses()
    courseList.value = data
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  }
}

const loadQuestionTypes = async () => {
  try {
    const { data } = await getQuestionTypeList({ page: 1, size: 100 })
    questionTypes.value = data.records
  } catch (error) {
    ElMessage.error('获取题型列表失败')
  }
}

const handleAdd = () => {
  form.value = {
    id: null,
    title: '',
    description: '',
    courseId: null,
    questionTypeIds: questionTypes.value
      .filter(type => type.isSystemDefault)
      .map(type => type.id)
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  form.value = { 
    ...row,
    courseId: row.courseId || null,
    questionTypeIds: []
  }
  
  // 获取试卷已选择的题型
  try {
    const { data } = await getPaperQuestionTypes(row.id)
    form.value.questionTypeIds = data.map(type => type.id)
  } catch (error) {
    form.value.questionTypeIds = questionTypes.value
      .filter(type => type.isSystemDefault)
      .map(type => type.id)
  }
  
  dialogVisible.value = true
}

const handleManage = (row) => {
  router.push(`/exam/paper/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该试卷吗？删除后相关题目也将被删除', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExamPaper(row.id)
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
      try {
        let paperId = form.value.id
        
        if (form.value.id) {
          await updateExamPaper(form.value.id, {
            title: form.value.title,
            description: form.value.description,
            courseId: form.value.courseId
          })
          ElMessage.success('更新成功')
        } else {
          const response = await createExamPaper({
            title: form.value.title,
            description: form.value.description,
            courseId: form.value.courseId
          })
          paperId = response.data.id
          ElMessage.success('创建成功')
        }
        
        // 保存试卷题型选择
        if (paperId) {
          try {
            await updatePaperQuestionTypes(paperId, form.value.questionTypeIds)
          } catch (error) {
            ElMessage.warning('试卷创建成功，但题型保存失败')
          }
        }
        
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(form.value.id ? '更新失败' : '创建失败')
      }
    }
  })
}

const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  loadData()
}

const handleCurrentChange = (val) => {
  page.value = val
  loadData()
}

const handleRemoveTag = (tagValue) => {
  // 查找被移除的题型
  const removedType = questionTypes.value.find(type => type.id === tagValue)
  
  // 如果是系统默认题型，则阻止移除
  if (removedType && removedType.isSystemDefault) {
    // 重新添加回来
    form.value.questionTypeIds.push(tagValue)
    
    // 显示提示
    ElMessage.warning('系统默认题型不可取消选择')
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  courseFilter.value = null
  loadData()
}

const handlePreview = async (row) => {
  try {
    loading.value = true
    
    // 获取试卷基本信息
    const paperResponse = await getExamPaperById(row.id)
    currentPaper.value = paperResponse.data
    
    // 获取试卷题目并排序
    const questionsResponse = await getQuestionList(row.id)
    currentPaper.value.questions = questionsResponse.data.sort((a, b) => {
      // 按题型排序
      const typeOrder = {
        'SINGLE_CHOICE': 1,
        'MULTIPLE_CHOICE': 2,
        'TRUE_FALSE': 3,
        'FILL_BLANK': 4,
        'SHORT_ANSWER': 5,
        'ANALYSIS': 6
      }
      return (typeOrder[a.type] || 99) - (typeOrder[b.type] || 99)
    })
    
    previewDialogVisible.value = true
  } catch (error) {
    console.error('获取试卷信息失败:', error)
    ElMessage.error('获取试卷信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  loadCourses()
  loadQuestionTypes()
})
</script>

<style scoped>
.exam-paper-list {
  padding: 20px;
}

.operation-bar {
  margin-bottom: 20px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.search-input {
  width: 200px;
  margin-right: 10px;
}

.course-filter {
  width: 200px;
  margin-right: 10px;
}

.exam-paper-table {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-help-text {
  margin-top: 8px;
}

:deep(.el-tag) {
  margin-left: 6px;
}
</style> 