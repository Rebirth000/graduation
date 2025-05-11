<template>
  <div class="exam-paper-detail">
    <div class="header">
      <h2 class="paper-title">{{ examPaper.title }}</h2>
      <p class="description">{{ examPaper.description }}</p>
      <div class="header-divider"></div>
    </div>

    <div class="operation-bar">
      <div class="operation-group">
        <el-button type="primary" @click="handleAddQuestion" round>
          添加题目
        </el-button>
        <el-button type="primary" @click="handleDownloadTemplate" round>
          下载题目模板
        </el-button>
        <el-upload
          class="upload"
          :show-file-list="false"
          :before-upload="handleUpload"
        >
          <el-button type="primary" round>导入题目</el-button>
        </el-upload>
      </div>
      <div>
        <el-button @click="router.push('/exam/paper')" round>返回列表</el-button>
      </div>
    </div>

    <div v-loading="loading">
      <!-- 全部题目 -->
      <div class="question-section">
        <h3>全部题目 ({{questions.length}})</h3>
        <el-table :data="questions" border style="width: 100%">
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column prop="type" label="题型" width="120">
            <template #default="{ row }">
              {{ getQuestionTypeName(row.type) }}
            </template>
          </el-table-column>
          <el-table-column label="题目内容">
            <template #default="{ row }">
              <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(row.type)">
                <div>
                  <math-display :content="row.title" />
                </div>
                <div v-if="row.imageUrl" class="question-image">
                  <img :src="row.imageUrl" alt="题目图片" class="question-img" />
                </div>
                <div v-for="(option, index) in parseOptions(row.options)" :key="index" class="option">
                  {{ String.fromCharCode(65 + index) }}. <math-display :content="option" />
                </div>
              </div>
              <div v-else-if="row.type === 'FILL_BLANK'">
                <!-- 多空填空题 -->
                <div v-if="parseFillBlankTitle(row.title).isMulti" 
                     class="multi-blank-content" 
                     v-html="renderMultiBlanks(parseFillBlankTitle(row.title).content)">
                </div>
                <!-- 单空填空题 -->
                <div v-else>
                  {{ parseFillBlankTitle(row.title).prefix }}<span class="blank-line">_</span>{{ parseFillBlankTitle(row.title).suffix }}
                </div>
                
                <div v-if="row.imageUrl" class="question-image">
                  <img :src="row.imageUrl" alt="题目图片" class="question-img" />
                </div>
              </div>
              <div v-else>
                <math-display :content="row.title" />
                <div v-if="row.imageUrl" class="question-image">
                  <img :src="row.imageUrl" alt="题目图片" class="question-img" />
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="分值" width="80" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEditQuestion(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteQuestion(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-if="questions.length === 0" description="该试卷暂无题目，请点击添加题目按钮开始创建题目" />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="questionForm.id ? '编辑题目' : '添加题目'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="questionForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="题目类型" prop="type">
          <el-select
            v-model="questionForm.type"
            placeholder="请选择题目类型"
            :loading="questionTypeLoading"
            @focus="fetchQuestionTypes"
          >
            <el-option
              v-for="type in getAvailableTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>

        <template v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(questionForm.type)">
          <el-form-item label="题目" prop="title">
            <math-editor v-model="questionForm.title" :rows="3" />
          </el-form-item>
          <el-form-item
            v-for="(option, index) in questionForm.optionList"
            :key="index"
            :label="`选项${String.fromCharCode(65 + index)}`"
            :prop="'optionList.' + index"
            :rules="{ required: true, message: '请输入选项内容', trigger: 'blur' }"
          >
            <math-editor v-model="questionForm.optionList[index]" :rows="1" />
          </el-form-item>
        </template>

        <template v-else-if="questionForm.type === 'TRUE_FALSE'">
          <el-form-item label="题目" prop="title">
            <math-editor v-model="questionForm.title" :rows="3" />
          </el-form-item>
        </template>

        <template v-else-if="questionForm.type === 'FILL_BLANK'">
          <el-form-item label="填空题类型">
            <el-radio-group v-model="questionForm.fillBlank.blankType" @change="handleBlankTypeChange">
              <el-radio label="single">单空填空</el-radio>
              <el-radio label="multi">多空填空</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 单填空 -->
          <template v-if="questionForm.fillBlank.blankType === 'single'">
          <el-form-item label="填空前文本">
            <math-editor v-model="questionForm.fillBlank.prefix" :rows="2" />
          </el-form-item>
          <el-form-item label="填空后文本">
            <math-editor v-model="questionForm.fillBlank.suffix" :rows="2" />
          </el-form-item>
        </template>

          <!-- 多填空 -->
          <template v-else>
            <el-form-item label="题目内容">
              <el-alert
                title="使用[blank]标记填空位置，例如：北京是[blank]的首都，[blank]是中国的首都。"
                type="info"
                :closable="false"
                style="margin-bottom: 10px;"
              />
              <math-editor v-model="questionForm.fillBlank.content" :rows="4" />
            </el-form-item>
            
            <el-form-item label="填空数量">
              <el-input-number 
                v-model="questionForm.fillBlank.blanks" 
                :min="1" 
                :max="10"
                @change="handleBlanksCountChange"
              />
            </el-form-item>
            
            <!-- 为每个空添加单独的配置 -->
            <div v-if="questionForm.fillBlank.blanks > 0" class="blanks-config">
              <!-- 这里移除了每个空的提示和图片上传功能 -->
            </div>
          </template>
        </template>
        
        <!-- 简答题和分析题 -->
        <template v-else>
          <el-form-item label="题目" prop="title">
            <math-editor v-model="questionForm.title" :rows="3" />
          </el-form-item>
        </template>

        <el-form-item label="题目图片">
          <image-uploader v-model="questionForm.imageUrl" />
        </el-form-item>

        <el-form-item label="分值" prop="score">
          <el-input-number v-model="questionForm.score" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitQuestion">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MathEditor from '@/components/MathEditor.vue'
import MathDisplay from '@/components/MathDisplay.vue'
import ImageUploader from '@/components/ImageUploader.vue'
import {
  getExamPaperById,
  getQuestionList,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  importExamPaperQuestions,
  downloadQuestionsTemplate
} from '@/api/exam'
import { getPaperQuestionTypes, downloadQuestionTypeTemplate, getQuestionTypeList } from '@/api/questionType'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const examPaper = ref({})
const questions = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const questionForm = ref({
  id: null,
  type: '',
  title: '',
  optionList: ['', '', '', ''],
  fillBlank: {
    blankType: 'single',
    prefix: '',
    suffix: '',
    content: '',
    blanks: 1,
    blanksConfig: []
  },
  imageUrl: '',
  score: 1
})

// 试卷可用的题型列表
const availableQuestionTypes = ref([])
const questionTypeLoading = ref(false)

const questionTypes = [
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '多选题', value: 'MULTIPLE_CHOICE' },
  { label: '判断题', value: 'TRUE_FALSE' },
  { label: '填空题', value: 'FILL_BLANK' },
  { label: '简答题', value: 'SHORT_ANSWER' },
  { label: '分析题', value: 'ANALYSIS' }
]

const rules = {
  type: [
    { required: true, message: '请选择题目类型', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  score: [
    { required: true, message: '请输入题目分数', trigger: 'blur' },
    { type: 'number', message: '分数必须为数字', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    loading.value = true
    console.log('加载试卷ID:', route.params.id)
    
    // 获取试卷信息
    const paperResponse = await getExamPaperById(route.params.id)
    if (paperResponse.code === 200) {
      examPaper.value = paperResponse.data
      console.log('试卷信息:', examPaper.value)
    }
    
    // 获取试卷题目列表
    const questionsResponse = await getQuestionList(route.params.id)
    if (questionsResponse.code === 200) {
      questions.value = questionsResponse.data || []
      console.log('题目列表:', questions.value)
    } else {
      console.error('获取题目失败:', questionsResponse)
      questions.value = []
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const getQuestionsByType = (typeCode) => {
  try {
    console.log(`查找题型 ${typeCode} 的题目，当前题目总数:`, questions.value.length)
    if (!questions.value || !Array.isArray(questions.value)) {
      console.warn('题目列表为空或不是数组')
      return []
    }
    const result = questions.value.filter(q => q.type === typeCode) || []
    console.log(`题型 ${typeCode} 的题目数量:`, result.length)
    return result
  } catch (error) {
    console.error('获取题型题目异常:', error)
    return []
  }
}

const resetQuestionForm = () => {
  questionForm.value = {
    id: null,
    type: '',
    title: '',
    optionList: ['', '', '', ''],
    fillBlank: {
      blankType: 'single',
      prefix: '',
      suffix: '',
      content: '',
      blanks: 1,
      blanksConfig: []
    },
    imageUrl: '',
    score: 1
  }
}

const handleAddQuestion = () => {
  resetQuestionForm()
  dialogVisible.value = true
  fetchQuestionTypes()
}

const handleEditQuestion = (row) => {
  const question = { ...row }
  if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(question.type)) {
    question.optionList = JSON.parse(question.options)
  } else if (question.type === 'FILL_BLANK') {
    try {
      const parsed = JSON.parse(question.title)
      if (parsed.type === 'multi-blanks') {
        question.fillBlank = {
          blankType: 'multi',
          content: parsed.content || '',
          blanks: parsed.blanks || 1,
          blanksConfig: parsed.blanksConfig || Array(parsed.blanks || 1).fill(null).map(() => ({ tip: '', imageUrl: '' }))
        }
      } else {
        question.fillBlank = {
          blankType: 'single',
          prefix: parsed.prefix || '',
          suffix: parsed.suffix || '',
          blanks: 1,
          blanksConfig: []
        }
      }
    } catch (e) {
      question.fillBlank = {
        blankType: 'single',
        prefix: '',
        suffix: '',
        blanks: 1,
        blanksConfig: []
      }
    }
  }
  questionForm.value = question
  dialogVisible.value = true
  fetchQuestionTypes()
}

const handleDeleteQuestion = (row) => {
  ElMessageBox.confirm('确认删除该题目吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteQuestion(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmitQuestion = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
        const data = {
          ...questionForm.value,
          paperId: route.params.id
        }

      try {
        if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(data.type)) {
          data.options = JSON.stringify(data.optionList)
        } else if (data.type === 'FILL_BLANK') {
          // 处理填空题数据
          if (data.fillBlank.blankType === 'single') {
            data.title = JSON.stringify({
              prefix: data.fillBlank.prefix,
              suffix: data.fillBlank.suffix
            })
          } else {
            // 多空填空题
            data.title = JSON.stringify({
              type: 'multi-blanks',
              content: data.fillBlank.content,
              blanks: data.fillBlank.blanks,
              blanksConfig: data.fillBlank.blanksConfig || []
            })
          }
        } else if (data.type === 'TRUE_FALSE') {
          data.options = JSON.stringify(['true', 'false'])
        }

        if (data.id) {
          await updateQuestion(data.id, data)
          ElMessage.success('更新成功')
        } else {
          await createQuestion(route.params.id, data)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(data.id ? '更新失败' : '添加失败')
      }
    }
  })
}

const handleDownloadTemplate = async () => {
  try {
    // 获取可用题型的ID列表
    const typeIds = availableQuestionTypes.value.map(type => type.id)
    
    if (typeIds.length === 0) {
      ElMessage.warning('当前试卷未配置可用题型，无法下载模板')
      return
    }
    
    // 使用可用题型下载模板
    const response = await downloadQuestionTypeTemplate(typeIds)
    
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${examPaper.value.title || '试卷'}_题目导入模板.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

const handleUpload = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    ElMessageBox.confirm('导入题目将覆盖当前试卷的所有题目，是否继续？', '提示', {
      type: 'warning'
    }).then(async () => {
      try {
        await importExamPaperQuestions(route.params.id, formData)
        ElMessage.success('导入题目成功')
        loadData()
      } catch (error) {
        ElMessage.error('导入题目失败')
      }
    }).catch(() => {
      // 用户取消导入
    })
  } catch (error) {
    ElMessage.error('导入失败')
  }
  return false
}

const getFullImageUrl = (url) => {
  if (!url) return '';
  return url;  // 直接返回URL，因为现在使用了代理
}

const handleBlankTypeChange = (type) => {
  if (type === 'multi' && !questionForm.value.fillBlank.content) {
    // 设置默认示例
    questionForm.value.fillBlank.content = '这是一个例子，[blank]需要填空的部分使用[blank]标记。'
    questionForm.value.fillBlank.blanks = 2
    questionForm.value.fillBlank.blanksConfig = [
      { tip: '', imageUrl: '' },
      { tip: '', imageUrl: '' }
    ]
  }
}

const handleBlanksCountChange = (count) => {
  // 调整填空配置数组大小，但所有字段都是空的
  questionForm.value.fillBlank.blanksConfig = Array(count).fill(null).map(() => ({ tip: '', imageUrl: '' }))
  questionForm.value.fillBlank.blanks = count
}

// 判断是否为多填空题
const isMultiBlanks = (question) => {
  try {
    const parsed = JSON.parse(question.title)
    return parsed.type === 'multi-blanks'
  } catch (e) {
    return false
  }
}

// 渲染多空填空题的题干文本（将[blank]替换为填空标记）
const renderMultiBlanks = (content) => {
  if (!content) return '';
  try {
    // 替换[blank]为与单空填空题相同样式的横线
    return content.replace(/\[blank\]/g, '<span style="display: inline-block; min-width: 80px; height: 20px; border-bottom: 1px solid #000; margin: 0 5px; position: relative; top: -4px; line-height: 5px; color: transparent;">_</span>');
  } catch (e) {
    console.error('渲染多空填空内容失败:', e);
    return content;
  }
}

// 获取可用的题型列表，直接使用后端返回的数据，并确保不为空
const getAvailableTypes = computed(() => {
  console.log('availableQuestionTypes:', availableQuestionTypes.value)
  // 确保返回一个有效的数组
  if (!availableQuestionTypes.value || !Array.isArray(availableQuestionTypes.value) || availableQuestionTypes.value.length === 0) {
    console.log('使用默认题型')
    return questionTypes.map((type, index) => ({
      id: index + 1,
      label: type.label,
      value: type.value
    }))
  }
  console.log('使用后端题型')
  return availableQuestionTypes.value
})

const fetchQuestionTypes = async () => {
  try {
    questionTypeLoading.value = true
    console.log('开始获取题型，试卷ID:', route.params.id)
    const response = await getPaperQuestionTypes(route.params.id)
    console.log('获取题型响应:', response)
    
    if (response.code === 200 && response.data && Array.isArray(response.data)) {
      console.log('获取到题型数据:', response.data)
      availableQuestionTypes.value = response.data.map(type => ({
        id: type.questionTypeId,
        label: type.name,
        value: type.code
      }))
    } else {
      console.log('使用系统题型')
      // 使用预定义的题型
      availableQuestionTypes.value = questionTypes.map((type, index) => ({
        id: index + 1,
        label: type.label,
        value: type.value
      }))
    }
  } catch (error) {
    console.error('获取题型列表失败:', error)
    ElMessage.error('获取题型列表失败，使用系统默认题型')
    
    // 出错时使用默认题型列表
    availableQuestionTypes.value = questionTypes.map((type, index) => ({
      id: index + 1,
      label: type.label,
      value: type.value
    }))
  } finally {
    questionTypeLoading.value = false
  }
}

const parseOptions = (options) => {
  try {
    if (typeof options === 'string') {
      return JSON.parse(options) || []
    } else if (Array.isArray(options)) {
      return options
    } else {
      console.warn('未知的选项格式:', options)
      return []
    }
  } catch (error) {
    console.error('解析选项失败:', error, '选项内容:', options)
    return []
  }
}

const getQuestionTypeName = (type) => {
  const typeInfo = questionTypes.find(t => t.value === type)
  return typeInfo ? typeInfo.label : '未知题型'
}

const parseFillBlankTitle = (title) => {
  try {
    if (typeof title === 'string') {
      const parsed = JSON.parse(title)
      // 判断多空填空题的标识
      if (parsed.content && parsed.content.includes('[blank]')) {
        return {
          isMulti: true,
          content: parsed.content,
          prefix: '',
          suffix: ''
        }
      }
      return {
        isMulti: false,
        prefix: parsed.prefix || '',
        suffix: parsed.suffix || '',
        content: ''
      }
    }
    return {
      isMulti: false,
      prefix: '',
      suffix: '',
      content: title || ''
    }
  } catch (error) {
    console.error('解析填空题标题失败:', error, '标题内容:', title)
    return {
      isMulti: false,
      prefix: '',
      suffix: '',
      content: title || ''
    }
  }
}

onMounted(async () => {
  try {
    await fetchQuestionTypes()
    await loadData()
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('初始化失败')
  }
})
</script>

<style scoped>
.exam-paper-detail {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.06);
  padding: 24px 32px 12px 32px;
  position: relative;
}

.paper-title {
  margin: 0 0 10px;
  font-size: 2rem;
  font-weight: bold;
  color: #303133;
  letter-spacing: 1px;
}

.description {
  color: #909399;
  margin: 0 0 8px 0;
  font-size: 1.1rem;
}

.header-divider {
  border-bottom: 2px solid #ebeef5;
  margin-top: 10px;
}

.operation-bar {
  margin-bottom: 24px;
  display: flex;
  gap: 16px;
  align-items: center;
}

.operation-group {
  display: flex;
  gap: 12px;
}

.question-section {
  margin-bottom: 30px;
}

.question-section h3 {
  margin: 0 0 15px;
}

.option {
  margin: 5px 0 5px 20px;
  color: #666;
  display: flex;
  align-items: flex-start;
  gap: 4px;
}

.option :deep(.math-display) {
  flex: 1;
  min-width: 0;
}

.question-image {
  margin: 10px 0;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.question-img {
  max-width: 400px;
  max-height: 200px;
  width: auto;
  height: auto;
  border: 1px solid #eee;
  border-radius: 4px;
  object-fit: contain;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.el-upload) {
  width: auto;
}

:deep(.el-button+.el-button) {
  margin: 0;
}

:deep(.math-display) {
  display: inline-block;
  vertical-align: middle;
}

:deep(.katex) {
  font-size: 1em;
}

:deep(.katex-display) {
  margin: 0.5em 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 2px;
}

.el-dialog :deep(.math-editor) {
  width: 100%;
}

/* 添加多填空题样式 */
.blanks-answer-section {
  margin-top: 10px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 5px;
  border: 1px solid #ebeef5;
}

.section-title {
  font-weight: bold;
  margin-bottom: 15px;
  color: #409eff;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.blank-answer-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.blank-index {
  width: 70px;
  font-weight: bold;
  color: #606266;
}

/* 多空填空题在列表中的样式 */
.multi-blanks-text {
  margin-bottom: 10px;
}

.blank-tag {
  display: inline-block;
  margin: 0 4px;
  vertical-align: middle;
  background-color: #409EFF !important;
  color: white !important;
}

.blanks-config {
  margin-top: 10px;
}

.blank-config-item {
  margin-bottom: 15px;
}

.blank-line {
  display: inline-block;
  min-width: 80px;
  max-width: 80px;
  height: 20px;
  border-bottom: 1px solid #000;
  margin: 0 5px;
  position: relative;
  top: -4px;
  line-height: 5px;
  color: transparent;
}

.multi-blank-content {
  line-height: 1.8;
}

.multi-blank-content :deep(.blank-line) {
  min-width: 60px;
  display: inline-block;
  vertical-align: middle;
}
</style>