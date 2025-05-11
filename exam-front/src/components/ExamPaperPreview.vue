<template>
  <el-dialog
    :title="'预览试卷 - ' + (paper?.title || '')"
    v-model="visible"
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
    
    <div class="paper-preview" v-if="paper">
      <!-- 试卷头部信息 -->
      <div class="paper-header">
        <h1 class="paper-title">{{ paper.title }}</h1>
        <div class="paper-meta">
          <span class="course-name">课程：{{ paper.courseName || '未关联课程' }}</span>
          <span class="total-score">总分：{{ getTotalScore() }} 分</span>
        </div>
        <div class="paper-description">{{ paper.description }}</div>
      </div>

      <!-- 试题列表 -->
      <div class="questions-container">
        <template v-if="paper.questions && paper.questions.length > 0">
          <div v-for="(question, index) in paper.questions" :key="question.id" class="question-item">
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
</template>

<script setup>
import { ref, computed } from 'vue'
import { MathJax } from 'mathjax-vue3'
import 'katex/dist/katex.min.css'
import katex from 'katex'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  paper: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const getTotalScore = () => {
  if (!props.paper || !props.paper.questions) return 0
  return props.paper.questions.reduce((total, question) => total + question.score, 0)
}

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

const parseOptions = (options) => {
  try {
    return JSON.parse(options || '[]')
  } catch (e) {
    console.error('解析选项失败:', e)
    return []
  }
}
</script>

<style scoped>
.paper-preview-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.paper-preview {
  padding: 20px;
}

.paper-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.paper-title {
  margin: 0 0 12px 0;
  font-size: 24px;
  text-align: center;
}

.paper-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #606266;
}

.paper-description {
  color: #606266;
  line-height: 1.6;
}

.questions-container {
  margin-top: 24px;
}

.question-item {
  margin-bottom: 24px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.question-header {
  margin-bottom: 12px;
  display: flex;
  align-items: baseline;
}

.question-number {
  font-weight: bold;
  margin-right: 8px;
}

.question-title, .option-content {
  flex: 1;
  line-height: 1.8;
}

.question-score {
  color: #f56c6c;
  margin-left: 8px;
}

.question-type {
  margin-bottom: 12px;
  color: #909399;
  font-size: 14px;
}

.options-list {
  margin-top: 12px;
}

.option-item {
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
}

.option-label {
  margin-right: 8px;
  font-weight: bold;
}

.question-images {
  margin-top: 16px;
}

.question-image {
  max-width: 100%;
  max-height: 300px;
}

.no-questions {
  padding: 32px 0;
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
</style> 