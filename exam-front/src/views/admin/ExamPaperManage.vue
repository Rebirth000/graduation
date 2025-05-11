<template>
  <div class="exam-paper-manage">
    <div class="page-header">
      <div class="header-title">
        <h2>试卷管理</h2>
        <span class="subtitle">管理系统中的所有试卷</span>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增试卷
        </el-button>
      </div>
    </div>

    <div class="filter-container">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索试卷标题或描述"
        class="search-input"
        clearable
        @clear="fetchExamPapers"
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
        @change="fetchExamPapers"
      >
        <el-option
          v-for="course in courseList"
          :key="course.id"
          :label="course.name"
          :value="course.id"
        />
      </el-select>

      <el-button type="primary" @click="fetchExamPapers">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetFilters">
        <el-icon><RefreshLeft /></el-icon> 重置
      </el-button>
    </div>

    <el-card class="exam-paper-table" shadow="hover">
      <el-table 
        :data="filteredPapers" 
        style="width: 100%" 
        border 
        stripe
        v-loading="loading"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
      >
        <el-table-column prop="title" label="试卷标题" min-width="150" />
        <el-table-column prop="description" label="试卷描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" min-width="120">
          <template #default="{ row }">
            {{ row.courseName || '未关联课程' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
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
            <el-button link type="primary" @click="handleManageQuestions(row)">
              <el-icon><Document /></el-icon> 管理题目
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 试卷表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑试卷' : '新增试卷'"
      width="550px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="试卷标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入试卷标题" />
        </el-form-item>
        
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="试卷描述" prop="description">
          <el-input
            type="textarea"
            v-model="form.description"
            placeholder="请输入试卷描述"
            :rows="4"
          />
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
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 使用共享组件预览试卷 -->
    <exam-paper-preview
      v-model="previewDialogVisible"
      :paper="currentPaper"
    />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ElMessage, 
  ElMessageBox 
} from 'element-plus'
import { 
  getExamPaperList, 
  getExamPaperById, 
  createExamPaper, 
  updateExamPaper, 
  deleteExamPaper, 
  getQuestionList 
} from '@/api/exam'
import { getAllCourses } from '@/api/course'
import dayjs from 'dayjs'
import { Plus, Search, View, Edit, Document, Delete, RefreshLeft } from '@element-plus/icons-vue'
// 导入题型相关API
import { getQuestionTypeList, getPaperQuestionTypes, updatePaperQuestionTypes } from '@/api/questionType'
// 导入共享组件
import ExamPaperPreview from '@/components/ExamPaperPreview.vue'

export default {
  name: 'ExamPaperManage',
  components: {
    Plus,
    Search,
    View,
    Edit,
    Document,
    Delete,
    RefreshLeft,
    ExamPaperPreview
  },
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const dialogVisible = ref(false)
    const previewDialogVisible = ref(false)
    const examPapers = ref([])
    const courseList = ref([])
    const searchKeyword = ref('')
    const courseFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const currentPaper = ref(null)
    const questionTypes = ref([]) // 所有题型列表

    const form = reactive({
      id: null,
      title: '',
      description: '',
      courseId: null,
      questionTypeIds: []
    })

    const rules = {
      title: [
        { required: true, message: '请输入试卷标题', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      courseId: [
        { required: true, message: '请选择所属课程', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入试卷描述', trigger: 'blur' }
      ]
    }

    // 过滤后的试卷列表
    const filteredPapers = computed(() => {
      let filtered = examPapers.value
      
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        filtered = filtered.filter(paper => 
          paper.title.toLowerCase().includes(keyword) || 
          paper.description.toLowerCase().includes(keyword)
        )
      }
      
      if (courseFilter.value) {
        filtered = filtered.filter(paper => paper.courseId === courseFilter.value)
      }
      
      return filtered
    })

    // 获取试卷列表
    const fetchExamPapers = async () => {
      loading.value = true
      try {
        const { data } = await getExamPaperList({
          page: currentPage.value,
          size: pageSize.value,
          keyword: searchKeyword.value,
          courseId: courseFilter.value || undefined
        })
        examPapers.value = data.records
        total.value = data.total
      } catch (error) {
        ElMessage.error('获取试卷列表失败')
      } finally {
        loading.value = false
      }
    }

    // 获取课程列表
    const fetchCourseList = async () => {
      try {
        const { data } = await getAllCourses()
        courseList.value = data
      } catch (error) {
        ElMessage.error('获取课程列表失败')
      }
    }

    // 获取题型列表
    const loadQuestionTypes = async () => {
      try {
        const { data } = await getQuestionTypeList({ page: 1, size: 100 })
        questionTypes.value = data.records
      } catch (error) {
        console.error('获取题型列表失败:', error)
        ElMessage.error('获取题型列表失败')
      }
    }

    // 重置筛选条件
    const resetFilters = () => {
      searchKeyword.value = ''
      courseFilter.value = ''
      fetchExamPapers()
    }

    // 新增试卷
    const handleAdd = () => {
      form.id = null
      form.title = ''
      form.description = ''
      form.courseId = null
      // 默认选择所有系统默认题型
      form.questionTypeIds = questionTypes.value
        .filter(type => type.isSystemDefault)
        .map(type => type.id)
      dialogVisible.value = true
    }

    // 编辑试卷
    const handleEdit = async (row) => {
      form.id = row.id
      form.title = row.title
      form.description = row.description
      form.courseId = row.courseId || null
      
      // 获取试卷已选择的题型
      try {
        const { data } = await getPaperQuestionTypes(row.id)
        form.questionTypeIds = data.map(type => type.id)
      } catch (error) {
        console.error('获取试卷题型失败:', error)
        form.questionTypeIds = questionTypes.value
          .filter(type => type.isSystemDefault)
          .map(type => type.id)
      }
      
      dialogVisible.value = true
    }

    // 管理题目
    const handleManageQuestions = (row) => {
      router.push(`/exam/paper/${row.id}`)
    }

    // 预览试卷
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

    // 删除试卷
    const handleDelete = (row) => {
      ElMessageBox.confirm('确认删除该试卷吗？删除后相关题目也将被删除', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteExamPaper(row.id)
          ElMessage.success('删除成功')
          fetchExamPapers()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
    }

    // 提交表单
    const submitForm = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          try {
            let paperId = form.id
            
            if (form.id) {
              await updateExamPaper(form.id, {
                title: form.title,
                description: form.description,
                courseId: form.courseId
              })
              ElMessage.success('更新试卷成功')
            } else {
              // 创建新试卷
              const response = await createExamPaper({
                title: form.title,
                description: form.description,
                courseId: form.courseId
              })
              paperId = response.data.id
              ElMessage.success('创建试卷成功')
            }
            
            // 保存试卷题型选择
            if (paperId) {
              try {
                await updatePaperQuestionTypes(paperId, form.questionTypeIds)
              } catch (error) {
                console.error('保存试卷题型失败:', error)
                ElMessage.warning('试卷创建成功，但题型保存失败')
              }
            }
            
            dialogVisible.value = false
            fetchExamPapers()
          } catch (error) {
            ElMessage.error(form.id ? '更新试卷失败' : '创建试卷失败')
          }
        }
      })
    }

    // 处理页码改变
    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchExamPapers()
    }

    // 处理每页条数改变
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
      fetchExamPapers()
    }

    // 格式化日期
    const formatDate = (dateString) => {
      return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
    }

    // 处理移除标签（防止系统默认题型被移除）
    const handleRemoveTag = (tagValue) => {
      // 查找被移除的题型
      const removedType = questionTypes.value.find(type => type.id === tagValue)
      
      // 如果是系统默认题型，则阻止移除
      if (removedType && removedType.isSystemDefault) {
        // 重新添加回来
        form.questionTypeIds.push(tagValue)
        
        // 显示提示
        ElMessage.warning('系统默认题型不可取消选择')
      }
    }

    onMounted(() => {
      fetchExamPapers()
      fetchCourseList()
      loadQuestionTypes()
    })

    return {
      formRef,
      loading,
      dialogVisible,
      previewDialogVisible,
      examPapers,
      courseList,
      filteredPapers,
      searchKeyword,
      courseFilter,
      currentPage,
      pageSize,
      total,
      form,
      rules,
      currentPaper,
      questionTypes,
      resetFilters,
      handleAdd,
      handleEdit,
      handleManageQuestions,
      handlePreview,
      handleDelete,
      submitForm,
      handleCurrentChange,
      handleSizeChange,
      fetchExamPapers,
      formatDate,
      handleRemoveTag
    }
  }
}
</script>

<style scoped>
.exam-paper-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-title {
  display: flex;
  flex-direction: column;
}

.header-title h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.filter-container {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.course-filter {
  width: 200px;
}

.exam-paper-table {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 