<template>
  <div class="course-manage">
    <!-- 顶部操作和搜索栏 -->
    <div class="top-bar">
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入课程名称搜索"
          class="search-input"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加课程
      </el-button>
    </div>

    <!-- 课程列表 -->
    <el-card class="course-table-card">
      <el-table 
        :data="filteredCourses" 
        style="width: 100%" 
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
        border
        stripe
        highlight-current-row
      >
        <el-table-column prop="name" label="课程名称" min-width="150">
          <template #default="scope">
            <span class="course-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="课程描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="success" @click="handleManagePapers(scope.row)">
              <el-icon><Document /></el-icon> 管理试卷
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      destroy-on-close
    >
      <el-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input type="textarea" v-model="courseForm.description" placeholder="请输入课程描述" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 试卷管理对话框 -->
    <el-dialog
      :title="'管理试卷 - ' + (currentCourse?.name || '')"
      v-model="paperDialogVisible"
      width="800px"
      destroy-on-close
    >
      <div class="paper-management">
        <!-- 添加搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchQuery"
            placeholder="请输入试卷标题搜索"
            clearable
            @input="handleSearch"
            style="margin-bottom: 20px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div v-if="filteredPapers.length > 0">
          <el-table 
            :data="filteredPapers" 
            style="width: 100%" 
            border
            stripe
            :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
          >
            <el-table-column prop="title" label="试卷标题" min-width="150" />
            <el-table-column prop="description" label="试卷描述" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handlePreviewPaper(scope.row)">
                  <el-icon><View /></el-icon> 预览
                </el-button>
                <el-button size="small" type="danger" @click="handleRemovePaper(scope.row)">
                  <el-icon><Delete /></el-icon> 移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else class="no-data">
          <el-empty description="暂无关联试卷" />
        </div>
      </div>
    </el-dialog>

    <!-- 试卷预览对话框 -->
    <el-dialog
      :title="'预览试卷 - ' + (currentPaper?.title || '')"
      v-model="previewDialogVisible"
      width="80%"
      class="paper-preview-dialog"
    >
      <!-- 为MathJax添加全局配置 -->
      <MathJax :config="{
        tex: {
          inlineMath: [['$', '$'], ['\\(', '\\)']],
          displayMath: [['$$', '$$'], ['\\[', '\\]']],
          processEscapes: true
        }
      }" />
      
      <div class="paper-preview" v-if="currentPaper">
        <!-- 试卷头部信息 -->
        <div class="paper-header">
          <h1 class="paper-title">{{ currentPaper.title }}</h1>
          <div class="paper-meta">
            <span class="course-name">课程：{{ currentCourse?.name || '未关联课程' }}</span>
            <span class="total-score">总分：{{ getTotalScore() }} 分</span>
          </div>
          <div class="paper-description" v-if="shouldShowDescription(currentPaper.description)">
            {{ currentPaper.description }}
          </div>
        </div>

        <!-- 试题列表 -->
        <div class="questions-container">
          <template v-if="currentPaper.questions && currentPaper.questions.length > 0">
          <div v-for="(question, index) in currentPaper.questions" :key="question.id" class="question-item">
            <div class="question-header">
              <span class="question-number">{{ index + 1 }}.</span>
              <!-- 使用v-html渲染LaTeX公式 -->
              <span class="question-title" v-html="renderQuestionTitle(question)"></span>
              <span class="question-score">（{{ question.score }}分）</span>
            </div>
            
            <div class="question-type">
              题型：{{ getQuestionType(question.type) }}
            </div>

            <!-- 选择题选项 -->
            <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(question.type)" class="options-list">
              <div v-for="(option, optionIndex) in parseOptions(question.options)" :key="optionIndex" class="option-item">
                <span class="option-label">{{ String.fromCharCode(65 + optionIndex) }}.</span>
                <!-- 使用v-html渲染选项中的LaTeX公式 -->
                <span class="option-content" v-html="renderLatex(option)"></span>
              </div>
            </div>

            <!-- 题目图片 -->
            <div v-if="question.imageUrl" class="question-images">
              <el-image
                :src="question.imageUrl"
                :preview-src-list="[question.imageUrl]"
                fit="contain"
                class="question-image"
              />
            </div>
            </div>
          </template>
          <div v-else class="no-questions">
            <el-empty description="该试卷暂无题目" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList, createCourse, updateCourse, deleteCourse } from '@/api/course'
import { getExamPaperList, getAssociatedPapers, associatePaper, dissociatePaper, getExamPaperById, getQuestionList } from '@/api/exam'
import { Search, Edit, Delete, Document, Plus, View } from '@element-plus/icons-vue'
import katex from 'katex'
import 'katex/dist/katex.min.css'
import dayjs from 'dayjs'
import MathJax from '@/components/MathJax.vue'
import { MathJax as MathJaxVue } from 'mathjax-vue3'

export default {
  name: 'CourseManage',
  components: {
    Search,
    Edit,
    Delete,
    Document,
    Plus,
    View,
    MathJax,
    MathJaxVue
  },
  setup() {
    const courseList = ref([])
    const dialogVisible = ref(false)
    const paperDialogVisible = ref(false)
    const previewDialogVisible = ref(false)
    const dialogTitle = ref('')
    const courseFormRef = ref(null)
    const activeTab = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const searchKeyword = ref('')
    
    const courseForm = reactive({
      id: '',
      name: '',
      description: ''
    })

    const availablePapers = ref([])
    const associatedPapers = ref([])
    const currentCourse = ref(null)
    const currentPaper = ref(null)

    const rules = {
      name: [
        { required: true, message: '请输入课程名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入课程描述', trigger: 'blur' }
      ]
    }

    const searchQuery = ref('')
    const filteredPapers = ref([])

    // 格式化日期
    const formatDate = (dateString) => {
      return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
    }

    // 基于搜索关键词过滤课程列表
    const filteredCourses = computed(() => {
      if (!searchKeyword.value) {
        return courseList.value
      }
      return courseList.value.filter(course => 
        course.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        course.description.toLowerCase().includes(searchKeyword.value.toLowerCase())
      )
    })

    // 搜索处理
    const handleSearch = () => {
      // 对于试卷搜索
      if (!searchQuery.value) {
        filteredPapers.value = associatedPapers.value
      } else {
        filteredPapers.value = associatedPapers.value.filter(paper => 
          paper.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          paper.description.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
      }
    }

    // 获取课程列表
    const fetchCourseList = async () => {
      try {
        const response = await getCourseList({ 
          page: currentPage.value,
          size: pageSize.value
        })
        courseList.value = response.data.records
        total.value = response.data.total
      } catch (error) {
        ElMessage.error('获取课程列表失败')
      }
    }

    // 获取试卷列表
    const fetchPapers = async () => {
      try {
        const response = await getExamPaperList()
        availablePapers.value = response.data.records.filter(paper => 
          !associatedPapers.value.some(ap => ap.id === paper.id)
        )
      } catch (error) {
        ElMessage.error('获取试卷列表失败')
      }
    }

    // 显示添加对话框
    const showAddDialog = () => {
      dialogTitle.value = '添加课程'
      courseForm.id = ''
      courseForm.name = ''
      courseForm.description = ''
      dialogVisible.value = true
    }

    // 显示编辑对话框
    const handleEdit = (row) => {
      dialogTitle.value = '编辑课程'
      courseForm.id = row.id
      courseForm.name = row.name
      courseForm.description = row.description
      dialogVisible.value = true
    }

    // 监听搜索框的变化
    watch(searchQuery, () => {
      handleSearch()
    })

    // 监听关联试卷列表的变化
    watch(associatedPapers, () => {
      handleSearch()
    })

    // 管理试卷
    const handleManagePapers = async (row) => {
      currentCourse.value = row
      searchQuery.value = ''
      paperDialogVisible.value = true
      
      try {
        const response = await getAssociatedPapers(row.id)
        associatedPapers.value = response.data
        filteredPapers.value = response.data
      } catch (error) {
        ElMessage.error('获取关联试卷失败')
      }
    }

    // 预览试卷
    const handlePreviewPaper = async (paper) => {
      try {
        // 获取试卷基本信息
        const paperResponse = await getExamPaperById(paper.id)
        currentPaper.value = paperResponse.data
        
        // 获取试卷题目并排序
        const questionsResponse = await getQuestionList(paper.id)
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
      }
    }

    // 从课程移除试卷
    const handleRemovePaper = (paper) => {
      ElMessageBox.confirm('确认从此课程移除该试卷吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await dissociatePaper(currentCourse.value.id, paper.id)
          ElMessage.success('移除成功')
          
          // 刷新关联试卷列表
          const response = await getAssociatedPapers(currentCourse.value.id)
          associatedPapers.value = response.data
          filteredPapers.value = response.data
        } catch (error) {
          ElMessage.error('移除失败')
        }
      })
    }

    // 删除课程
    const handleDelete = (row) => {
      ElMessageBox.confirm('确认删除该课程吗？此操作将永久删除该课程，是否继续？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        draggable: true,
      }).then(async () => {
        try {
          await deleteCourse(row.id)
          ElMessage.success('删除成功')
          fetchCourseList()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
    }

    // 提交表单
    const handleSubmit = async () => {
      if (!courseFormRef.value) return
      
      await courseFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (courseForm.id) {
              await updateCourse(courseForm.id, courseForm)
              ElMessage.success('更新成功')
            } else {
              await createCourse(courseForm)
              ElMessage.success('添加成功')
            }
            dialogVisible.value = false
            fetchCourseList()
          } catch (error) {
            ElMessage.error(courseForm.id ? '更新失败' : '添加失败')
          }
        }
      })
    }

    // 处理页码改变
    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchCourseList()
    }

    // 处理每页条数改变
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
      fetchCourseList()
    }

    // 添加试卷到课程
    const handleAddPaper = async (paper) => {
      try {
        await associatePaper(currentCourse.value.id, paper.id)
        ElMessage.success('添加成功')
        
        // 刷新关联试卷列表
        const response = await getAssociatedPapers(currentCourse.value.id)
        associatedPapers.value = response.data
        filteredPapers.value = response.data
        
        // 刷新可用试卷列表
        await fetchPapers()
      } catch (error) {
        ElMessage.error('添加失败')
      }
    }

    // 保留原有函数
    const getQuestionType = (type) => {
      const typeMap = {
        'SINGLE_CHOICE': '单选题',
        'MULTIPLE_CHOICE': '多选题',
        'FILL_BLANK': '填空题',
        'TRUE_FALSE': '判断题',
        'SHORT_ANSWER': '简答题',
        'ANALYSIS': '分析题'
      }
      return typeMap[type] || type
    }

    const parseOptions = (options) => {
      try {
        return JSON.parse(options || '[]')
      } catch (e) {
        console.error('解析选项失败:', e)
        return []
      }
    }

    const getTotalScore = () => {
      if (!currentPaper.value?.questions) return 0
      return currentPaper.value.questions.reduce((total, q) => total + q.score, 0)
    }

    const renderQuestionTitle = (question) => {
      if (question.type === 'FILL_BLANK') {
        try {
          const title = JSON.parse(question.title)
          if (title.content) {
            // 多空填空题
            return renderLatex(title.content.replace(/\[blank\]/g, '____'))
        } else {
            // 单空填空题
            return renderLatex((title.prefix || '') + '____' + (title.suffix || ''))
        }
        } catch (e) {
          return renderLatex(question.title)
        }
      } else {
        // 其他题型直接返回标题，应用LaTeX渲染
        return renderLatex(question.title)
      }
    }

    const renderLatex = (text) => {
      if (!text) return '';
      try {
        // 检查是否包含LaTeX公式，如果包含则使用KaTeX渲染
        if (text.includes('$')) {
          // 匹配行内LaTeX公式 $...$ 和行间公式 $$..$$ 
          return text.replace(/\$\$(.*?)\$\$/g, (match, formula) => {
          try {
              return katex.renderToString(formula, { displayMode: true });
            } catch (e) {
              console.error('KaTeX渲染错误:', e);
              return match;
            }
          }).replace(/\$(.*?)\$/g, (match, formula) => {
            try {
              return katex.renderToString(formula, { displayMode: false });
          } catch (e) {
              console.error('KaTeX渲染错误:', e);
              return match;
          }
          });
        }
        return text;
      } catch (e) {
        console.error('LaTeX渲染错误:', e);
        return text;
      }
    }

    // 判断试卷描述是否需要显示
    const shouldShowDescription = (desc) => {
      if (!desc) return false;
      const trimmed = desc.trim();
      // 只包含$$、$ $或空内容时不显示
      if (trimmed === '$$' || trimmed === '$ $' || trimmed === '') return false;
      // 只包含空的LaTeX块时不显示
      if (/^\$\$\s*\$\$$/.test(trimmed) || /^\$\s*\$/.test(trimmed)) return false;
      return true;
    }

    onMounted(() => {
      fetchCourseList()
    })

    return {
      courseList,
      filteredCourses,
      dialogVisible,
      paperDialogVisible,
      previewDialogVisible,
      dialogTitle,
      courseForm,
      courseFormRef,
      availablePapers,
      associatedPapers,
      activeTab,
      rules,
      showAddDialog,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleManagePapers,
      handlePreviewPaper,
      handleAddPaper,
      handleRemovePaper,
      getQuestionType,
      currentPaper,
      searchQuery,
      filteredPapers,
      handleSearch,
      currentPage,
      pageSize,
      total,
      handleCurrentChange,
      handleSizeChange,
      parseOptions,
      getTotalScore,
      renderQuestionTitle,
      renderLatex,
      formatDate,
      searchKeyword,
      currentCourse,
      shouldShowDescription
    }
  }
}
</script>

<style scoped>
.course-manage {
  padding: 20px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-container {
  width: 300px;
}

.search-input {
  width: 100%;
}

.course-table-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.paper-management {
  margin-top: 10px;
}

.no-data {
  text-align: center;
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.course-name {
  font-weight: 500;
  color: #409EFF;
}

/* 保留原有的样式并添加预览对话框所需的其他样式 */
.paper-preview-dialog :deep(.question-item) {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.paper-preview-dialog :deep(.question-title) {
  font-weight: bold;
  margin-bottom: 10px;
}

.paper-preview-dialog :deep(.question-options) {
  margin-left: 20px;
}

.paper-preview-dialog :deep(.question-images) {
  margin-top: 10px;
}

.paper-preview-dialog :deep(.question-image) {
  max-width: 100%;
  max-height: 300px;
}

.paper-preview {
  padding: 20px;
}

.paper-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #EBEEF5;
}

.paper-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.paper-meta {
  margin: 10px 0;
  color: #606266;
}

.paper-meta span {
  margin: 0 15px;
  font-size: 16px;
}

.paper-description {
  color: #606266;
  margin-bottom: 30px;
  text-align: center;
}

.questions-container {
  padding: 0 20px;
}

.question-item {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
}

.question-item h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.question-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
  line-height: 1.5;
  font-size: 16px;
}

.question-number {
  font-weight: bold;
  margin-right: 8px;
  color: #303133;
  min-width: 25px;
  text-align: right;
  line-height: inherit;
  font-size: inherit;
}

.question-title, .option-content {
  flex: 1;
  line-height: 1.8;
}

/* 添加LaTeX公式样式 */
.katex-display {
  margin: 12px 0;
  overflow-x: auto;
  overflow-y: hidden;
}

.katex {
  font-size: 1.1em;
}

/* 优化填空题的下划线样式 */
.question-title:deep(.____ ) {
  display: inline-block;
  min-width: 60px;
  border-bottom: 1px solid #606266;
  text-align: center;
  margin: 0 4px;
}

/* 数学公式块样式 */
:deep(.MathJax) {
  outline: 0;
}

.no-questions {
  padding: 32px 0;
}

.question-score {
  color: #F56C6C;
  font-weight: bold;
  margin-left: 8px;
  line-height: inherit;
  font-size: inherit;
}

.question-type {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.options-list {
  margin-top: 15px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  margin: 10px 0;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.option-label {
  margin-right: 10px;
  font-weight: bold;
  color: #303133;
}

.question-images {
  margin-top: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.blank-tag) {
  background-color: #409eff !important;
  color: white !important;
  display: inline-block;
  margin: 0 4px;
  vertical-align: middle;
}

/* 调整对话框内容区域高度，避免内容过多导致滚动问题 */
.paper-preview-dialog :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}
</style> 