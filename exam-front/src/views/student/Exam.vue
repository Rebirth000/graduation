<template>
  <div class="exam-container">
    <div class="exam-header">
      <h2>{{ examInfo.name }}</h2>
      <div class="remaining-time" :class="{ 'warning': remainingTime <= 300 }">
        剩余时间：{{ formatTime(remainingTime) }}
      </div>
    </div>
    
    <div class="questions-list">
      <div v-for="(question, questionIndex) in questions" :key="question.id" class="question-item">
        <div class="question-header">
          <span class="question-number">第 {{ questionIndex + 1 }} 题</span>
          <span class="question-type blue-tag">{{ getQuestionType(question.type) }}</span>
          <span class="question-score">({{ question.score }}分)</span>
        </div>
        <div class="question-title">
          <template v-if="question.type === 'FILL_BLANK'">
            <div class="fill-blank-content">
              <div v-if="isMultiBlanks(question)" class="multi-blanks-container">
                <div v-html="renderMultiBlanks(question)" class="multi-blanks-text"></div>
                <div class="multi-blanks-inputs">
                  <div v-for="(blank, index) in getBlankCount(question)" :key="index" class="blank-item">
                    <span class="blank-label">空{{index + 1}}:</span>
                    <el-input
                      v-model="getMultiBlankAnswers(question.id)[index]"
                      class="fill-blank-input"
                      :disabled="submitted"
                      :placeholder="`请填写第${index + 1}空答案`"
                    />
                    
                    <!-- 显示提示文本 -->
                    <div v-if="getBlankTip(question, index)" class="blank-tip">
                      提示: {{ getBlankTip(question, index) }}
                    </div>
                    
                    <!-- 显示题目提供的图片 -->
                    <div v-if="getBlankImage(question, index)" class="blank-question-image">
                      <img :src="getBlankImage(question, index)" alt="题目图片" class="blank-img" />
                    </div>
                    
                    <!-- 为每个空添加图片上传功能 -->
                    <div class="blank-image-upload">
                      <el-upload
                        class="image-upload"
                        :show-file-list="false"
                        :before-upload="beforeUpload"
                        :http-request="(options) => handleBlankImageUpload(options, question.id, index)"
                        :disabled="submitted"
                      >
                        <el-button type="primary" size="small" :disabled="submitted">
                          <el-icon><Upload /></el-icon>
                          上传图片
                        </el-button>
                      </el-upload>
                      
                      <!-- 预览区域 -->
                      <div v-if="getBlankImages(question.id, index)?.length > 0" class="blank-image-preview">
                        <div v-for="(imageUrl, imgIndex) in getBlankImages(question.id, index)" 
                             :key="imgIndex" 
                             class="blank-image-item">
                          <img 
                            :src="imageUrl" 
                            class="preview-image" 
                            @click="handlePreviewImage(imageUrl)"
                          />
                          <div class="image-actions">
                            <el-button 
                              type="danger" 
                              link 
                              size="small"
                              @click="handleRemoveBlankImage(question.id, index, imgIndex)" 
                              :disabled="submitted"
                            >
                              删除
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="fill-blank-text">
                <math-display :content="JSON.parse(question.title).prefix" />
                <span class="el-tag el-tag--small blank-tag">填空处</span>
                <el-input
                  v-model="answers[question.id]"
                  class="fill-blank-input"
                  :disabled="submitted"
                  placeholder="请填写答案"
                />
                <math-display :content="JSON.parse(question.title).suffix" />
              </div>
            </div>
          </template>
          <template v-else>
            <math-display :content="question.title" />
          </template>
        </div>
        
        <!-- 显示题目图片 -->
        <div v-if="question.imageUrl" class="question-image">
          <img :src="question.imageUrl" alt="题目图片" class="question-img" />
        </div>
        
        <!-- 选择题 -->
        <template v-if="question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE'">
          <el-checkbox-group 
            v-if="question.type === 'MULTIPLE_CHOICE'"
            v-model="answers[question.id]"
            :disabled="submitted"
            class="option-list"
          >
            <el-checkbox 
              v-for="(option, index) in JSON.parse(question.options)" 
              :key="index"
              :label="option"
              class="option-item"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}. </span>
              <math-display :content="option" />
            </el-checkbox>
          </el-checkbox-group>
          
          <el-radio-group 
            v-else
            v-model="answers[question.id]"
            :disabled="submitted"
            class="option-list"
          >
            <el-radio 
              v-for="(option, index) in JSON.parse(question.options)" 
              :key="index"
              :label="option"
              class="option-item"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}. </span>
              <math-display :content="option" />
            </el-radio>
          </el-radio-group>
        </template>

        <!-- 判断题 -->
        <template v-else-if="question.type === 'TRUE_FALSE'">
          <el-radio-group 
            v-model="answers[question.id]"
            :disabled="submitted"
            class="option-list"
          >
            <el-radio label="true" class="option-item">
              <span class="option-label">A. </span>正确
            </el-radio>
            <el-radio label="false" class="option-item">
              <span class="option-label">B. </span>错误
            </el-radio>
          </el-radio-group>
        </template>
        
        <!-- 填空题(非多空)、简答题、分析题 -->
        <template v-else-if="question.type !== 'FILL_BLANK' || (question.type === 'FILL_BLANK' && !isMultiBlanks(question))">
          <div class="answer-input-container">
          <el-input
            v-model="answers[question.id]"
            type="textarea"
            :rows="4"
            :disabled="submitted"
            placeholder="请输入答案"
          />
            <div class="answer-image-section">
              <el-upload
                class="image-upload"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :http-request="(options) => handleUpload(options, question.id)"
                :disabled="submitted"
              >
                <el-button type="primary" :disabled="submitted">
                  <el-icon><Upload /></el-icon>
                  上传手写答案
                </el-button>
                <template #tip>
                  <div class="upload-tip">可上传多张图片，支持 JPG/PNG 格式</div>
                </template>
              </el-upload>
              
              <!-- 图片预览区域 -->
              <div v-if="answerImages[question.id]?.length > 0" class="image-preview-section">
                <div class="image-grid">
                  <div v-for="(imageUrl, index) in answerImages[question.id]" 
                       :key="index" 
                       class="image-item">
                    <img 
                      :src="imageUrl" 
                      class="preview-image" 
                      @click="handlePreviewImage(imageUrl)"
                    />
                    <div class="image-actions">
                      <el-button 
                        type="danger" 
                        link 
                        @click="handleRemoveImage(question.id, index)" 
                        :disabled="submitted"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <div class="exam-footer">
      <el-button 
        type="primary" 
        @click="handleSubmit" 
        :disabled="submitted || submitting"
        :loading="submitting"
      >
        提交答案
      </el-button>
    </div>

    <!-- 添加图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="手写答案预览" width="800px" append-to-body>
      <img :src="previewImageUrl" style="width: 100%; max-height: 70vh; object-fit: contain;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamDetail, submitExamAnswers } from '@/api/exam'
import MathDisplay from '@/components/MathDisplay.vue'
import ImageUploader from '@/components/ImageUploader.vue'
import { Upload } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'

const route = useRoute()
const router = useRouter()
const registrationId = route.params.id

const examInfo = ref({})
const questions = ref([])
const answers = ref({})
const multiBlankAnswers = ref({})
const answerImages = ref({})
const blankImages = ref({})
const remainingTime = ref(0)
const submitted = ref(false)
const submitting = ref(false)
let timer = null

// 添加预览相关的响应式变量
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 判断是否为多填空题
const isMultiBlanks = (question) => {
  try {
    const parsed = JSON.parse(question.title)
    return parsed.type === 'multi-blanks'
  } catch (e) {
    return false
  }
}

// 获取填空题的填空数量
const getBlankCount = (question) => {
  try {
    const parsed = JSON.parse(question.title)
    return parsed.blanks || 1
  } catch (e) {
    return 1
  }
}

// 渲染多填空题的题干文本（将[blank]替换为填空标记）
const renderMultiBlanks = (question) => {
  try {
    const parsed = JSON.parse(question.title)
    let content = parsed.content || ''
    // 替换[blank]为填空样式，使用el-tag样式
    return content.replace(/\[blank\]/g, () => {
      return `<span class="el-tag el-tag--small blank-tag">填空处</span>`
    })
  } catch (e) {
    return '解析题目内容失败'
  }
}

// 获取多填空题的答案数组
const getMultiBlankAnswers = (questionId) => {
  if (!multiBlankAnswers.value[questionId]) {
    const question = questions.value.find(q => q.id === questionId)
    const blankCount = getBlankCount(question)
    multiBlankAnswers.value[questionId] = Array(blankCount).fill('')
  }
  return multiBlankAnswers.value[questionId]
}

// 获取多空填空题的图片
const getBlankImages = (questionId, blankIndex) => {
  if (!blankImages.value[questionId]) {
    blankImages.value[questionId] = {}
  }
  if (!blankImages.value[questionId][blankIndex]) {
    blankImages.value[questionId][blankIndex] = []
  }
  return blankImages.value[questionId][blankIndex]
}

// 获取多填空题的填空内容提示
const getBlankTip = (question, index) => {
  try {
    const parsed = JSON.parse(question.title)
    if (parsed.blanksConfig && parsed.blanksConfig[index]) {
      return parsed.blanksConfig[index].tip
    }
    return ''
  } catch (e) {
    return ''
  }
}

// 获取多填空题的填空内容图片
const getBlankImage = (question, index) => {
  try {
    const parsed = JSON.parse(question.title)
    if (parsed.blanksConfig && parsed.blanksConfig[index]) {
      return parsed.blanksConfig[index].imageUrl
    }
    return ''
  } catch (e) {
    return ''
  }
}

// 处理多空填空题的图片上传
const handleBlankImageUpload = async (options, questionId, blankIndex) => {
  try {
    const { data: imageUrl } = await uploadImage(options.file)
    const images = getBlankImages(questionId, blankIndex)
    images.push(imageUrl)
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

// 删除多空填空题的图片
const handleRemoveBlankImage = (questionId, blankIndex, imgIndex) => {
  ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const images = getBlankImages(questionId, blankIndex)
    images.splice(imgIndex, 1)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 获取考试信息和试题
const fetchExamDetail = async () => {
  try {
    const res = await getExamDetail(registrationId)
    examInfo.value = res.data.examInfo
    
    // 确保题目按照顺序号排序
    if (res.data.questions && Array.isArray(res.data.questions)) {
      // 如果有orderNum字段，按orderNum排序
      if (res.data.questions.some(q => q.orderNum !== undefined)) {
        questions.value = res.data.questions.sort((a, b) => {
          return (a.orderNum || 0) - (b.orderNum || 0)
        })
      } else {
        // 如果没有orderNum，尝试按题型排序，保持常见顺序：单选 -> 多选 -> 判断 -> 填空 -> 简答 -> 分析
        const typeOrder = {
          'SINGLE_CHOICE': 1,
          'MULTIPLE_CHOICE': 2,
          'TRUE_FALSE': 3,
          'FILL_BLANK': 4,
          'SHORT_ANSWER': 5,
          'ANALYSIS': 6
        }
        questions.value = res.data.questions.sort((a, b) => {
          return (typeOrder[a.type] || 99) - (typeOrder[b.type] || 99)
        })
      }
    } else {
      questions.value = res.data.questions || []
    }
    
    // 初始化答案对象和图片对象
    questions.value.forEach(question => {
      if (question.type === 'MULTIPLE_CHOICE') {
        answers.value[question.id] = []
      } else if (question.type === 'FILL_BLANK' && isMultiBlanks(question)) {
        const blankCount = getBlankCount(question)
        multiBlankAnswers.value[question.id] = Array(blankCount).fill('')
        answers.value[question.id] = '' // 保留兼容性
      } else {
        answers.value[question.id] = ''
        // 为需要手动输入的题型初始化图片数组
        if (['FILL_BLANK', 'SHORT_ANSWER', 'ANALYSIS'].includes(question.type)) {
          answerImages.value[question.id] = []
        }
      }
    })
    
    // 计算剩余时间
    const endTime = new Date(examInfo.value.endTime).getTime()
    const updateRemainingTime = () => {
      const now = new Date().getTime()
      remainingTime.value = Math.max(0, Math.floor((endTime - now) / 1000))
      
      if (remainingTime.value <= 0) {
        clearInterval(timer)
        if (!submitted.value) {
          handleSubmit()
        }
      }
    }
    
    updateRemainingTime()
    timer = setInterval(updateRemainingTime, 1000)
  } catch (error) {
    router.push('/exam/my')
  }
}

// 格式化剩余时间
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 获取题目类型显示文本
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

// 提交答案
const handleSubmit = async () => {
  try {
    // 处理多填空题答案格式
    const processedAnswers = { ...answers.value }
    for (const questionId in multiBlankAnswers.value) {
      if (multiBlankAnswers.value[questionId]?.length > 0) {
        processedAnswers[questionId] = multiBlankAnswers.value[questionId].join('|')
      }
    }
    
    // 合并所有图片数据
    const processedImages = { ...answerImages.value }
    
    // 处理多空填空题的图片
    for (const questionId in blankImages.value) {
      if (!processedImages[questionId]) {
        processedImages[questionId] = []
      }
      
      // 遍历每个空的图片并添加到提交数据中
      const blanksWithImages = Object.keys(blankImages.value[questionId])
      for (const blankIndex of blanksWithImages) {
        const images = blankImages.value[questionId][blankIndex] || []
        processedImages[questionId].push(...images)
      }
    }
    
    // 检查是否所有题目都已作答
    const unansweredQuestions = questions.value.filter(question => {
      if (question.type === 'FILL_BLANK' && isMultiBlanks(question)) {
        const blanksAnswers = multiBlankAnswers.value[question.id] || []
        return blanksAnswers.some(ans => !ans)
      } else {
      const answer = answers.value[question.id]
      return !answer || (Array.isArray(answer) && answer.length === 0)
      }
    })
    
    if (unansweredQuestions.length > 0) {
      const confirm = await ElMessageBox.confirm(
        `还有 ${unansweredQuestions.length} 道题目未作答，确定要提交吗？`,
        '提示',
        {
          confirmButtonText: '确定提交',
          cancelButtonText: '继续答题',
          type: 'warning'
        }
      )
      
      if (!confirm) {
        return
      }
    }
    
    submitting.value = true
    await submitExamAnswers(registrationId, {
      answers: processedAnswers,
      answerImages: processedImages
    })
    
    submitted.value = true
    ElMessage.success('提交成功')
    setTimeout(() => {
      router.push('/exam/my')
    }, 1500)
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 添加图片上传前的验证函数
const beforeUpload = (file) => {
  const isImage = /^image\/(jpeg|png|jpg)$/.test(file.type)
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 添加图片上传处理函数
const handleUpload = async (options, questionId) => {
  try {
    const { data: imageUrl } = await uploadImage(options.file)
    if (!Array.isArray(answerImages.value[questionId])) {
      answerImages.value[questionId] = []
    }
    answerImages.value[questionId].push(imageUrl)
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

// 添加图片预览函数
const handlePreviewImage = (url) => {
  previewImageUrl.value = url
  previewVisible.value = true
}

// 添加图片删除函数
const handleRemoveImage = (questionId, index) => {
  ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    answerImages.value[questionId].splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 页面加载时获取考试信息
onMounted(() => {
  fetchExamDetail()
})

// 页面销毁时清除定时器
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.exam-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.remaining-time {
  font-size: 18px;
  font-weight: bold;
}

.remaining-time.warning {
  color: #f56c6c;
}

.question-item {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 4px;
  background: #fff;
}

.question-header {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.question-number {
  font-weight: bold;
  margin-right: 10px;
  color: #303133;
}

.question-type {
  background-color: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
  margin-right: 8px;
}

.question-score {
  margin-left: 10px;
  color: #666;
}

.question-title {
  margin: 10px 0;
  font-size: 16px;
  line-height: 1.6;
}

.option-list {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex !important;
  align-items: flex-start;
  margin-left: 0 !important;
  padding: 8px 15px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.option-item:hover {
  background-color: #f5f7fa;
}

.option-label {
  margin-right: 8px;
  color: #666;
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

.exam-footer {
  margin-top: 30px;
  text-align: center;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

:deep(.el-textarea) {
  width: 100%;
}

/* 添加题目图片样式 */
.question-image {
  margin: 15px 0;
  text-align: center;
}

.question-img {
  max-width: 400px;
  max-height: 200px;
  object-fit: contain;
  border: 1px solid #eee;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.fill-blank-content {
  margin: 10px 0;
}

.fill-blank-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fill-blank-input {
  width: 120px;
  :deep(.el-input__inner) {
    text-align: center;
  }
}

/* 多填空题样式 */
.multi-blanks-container {
  margin: 15px 0;
}

.multi-blanks-text {
  margin-bottom: 15px;
  font-size: 16px;
  line-height: 1.6;
}

.blank-tag {
  background-color: #409eff !important;
  color: white !important;
  display: inline-block;
  margin: 0 4px;
  vertical-align: middle;
}

.multi-blanks-inputs {
  margin-top: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
}

.blank-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
  padding-bottom: 15px;
}

.blank-item:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.blank-label {
  width: 100%;
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
}

.fill-blank-input {
  width: 100%;
  margin-bottom: 10px;
}

.blank-image-upload {
  margin-top: 10px;
}

.blank-image-preview {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.blank-image-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 5px;
  background-color: #f8f9fa;
}

.blank-image-item .preview-image {
  width: 100%;
  height: 80px;
  object-fit: contain;
  cursor: pointer;
  border-radius: 4px;
}

.answer-input-container {
  margin: 10px 0;
}

.answer-image-section {
  margin-top: 15px;
}

.upload-tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.image-preview-section {
  margin-top: 15px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 10px;
}

.image-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 8px;
  background-color: #f8f9fa;
  position: relative;
}

.preview-image {
  width: 100%;
  height: 150px;
  object-fit: contain;
  cursor: pointer;
  border-radius: 4px;
  transition: transform 0.2s;
}

.preview-image:hover {
  transform: scale(1.02);
}

.image-actions {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

:deep(.el-upload) {
  width: auto;
}

:deep(.el-button .el-icon) {
  margin-right: 4px;
}

/* 预览弹窗样式优化 */
:deep(.el-dialog__body) {
  text-align: center;
  padding: 20px;
  
  img {
    max-width: 100%;
    max-height: 70vh;
    object-fit: contain;
  }
}

.blue-tag {
  background-color: #409eff;
  color: white;
}

.blank-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 13px;
  font-style: italic;
}

.blank-question-image {
  margin: 10px 0;
  text-align: center;
}

.blank-img {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style> 