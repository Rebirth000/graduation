  <template>
  <div class="exam-history">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="考试名称">
          <el-input
            v-model="queryParams.examName"
            placeholder="请输入考试名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="考试时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考试记录列表 -->
    <el-table
      v-loading="loading"
      :data="examList"
      style="width: 100%"
    >
      <el-table-column prop="examName" label="考试名称" min-width="180" />
      <el-table-column label="考试时间" min-width="300">
        <template #default="{ row }">
          {{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="examRoomName" label="考场" align="center" width="100" />
      <el-table-column prop="examRoomLocation" label="考场位置" align="center" width="100" />
      <el-table-column prop="seatNumber" label="座位号" align="center" width="100" />
      <el-table-column prop="status" label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120">
        <template #default="{ row }">
          <el-button 
            link 
            type="primary" 
            @click="viewDetail(row)"
            :disabled="row.status === '未开始' || row.status === '进行中' || row.status === '未参加'"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 答题详情对话框 -->
    <el-dialog
      style="--el-dialog-margin-top: 8vh"
      v-model="detailVisible"
      title="答题详情"
      width="800px"
      destroy-on-close
      class="exam-detail-dialog"
    >
      <div v-loading="detailLoading" class="dialog-content">
        <div v-for="question in questionList" :key="question.questionId" class="question-item">
          <div class="question-header">
            <span class="question-number">第 {{ questionList.indexOf(question) + 1 }} 题</span>
            <span class="question-type blue-tag">{{ getQuestionType(question.type) }}</span>
            <span class="question-score">({{ question.questionScore }}分)</span>
          </div>
          
          <div class="question-content">
            <!-- 题目内容 -->
            <template v-if="question.type === 'FILL_BLANK'">
              <div class="question-title">
                <template v-if="isMultiBlanks(question)">
                  <!-- 多空填空题 -->
                  <div v-html="renderMultiBlanks(question)" class="multi-blanks-text"></div>
                </template>
                <template v-else>
                  <!-- 单空填空题 -->
                  <div class="fill-blank-text">
                    <LatexRenderer :content="JSON.parse(question.title).prefix" />
                    <span class="el-tag el-tag--small blank-tag">填空处</span>
                    <LatexRenderer :content="JSON.parse(question.title).suffix" />
                  </div>
                </template>
              </div>
            </template>
            <template v-else>
              <div class="question-title">
                <LatexRenderer :content="question.title" />
              </div>
            </template>
            
            <!-- 选择题选项 -->
            <template v-if="question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE'">
              <div 
                v-for="option in question.optionList" 
                :key="option.code" 
                class="question-option"
                :class="{ 'selected': isOptionSelected(question, option.code) }"
              >
                {{ option.code }}. <LatexRenderer :content="option.content" />
              </div>
            </template>
            
            <!-- 答案 -->
            <div class="answer-section">
              <div class="answer-label">你的答案：</div>
              <div class="answer-content">
                <template v-if="question.type === 'SINGLE_CHOICE'">
                  <LatexRenderer :content="getOptionByCode(question, JSON.parse(question.answer))" />
                </template>
                <template v-else-if="question.type === 'MULTIPLE_CHOICE'">
                  <LatexRenderer :content="JSON.parse(question.answer).map(code => getOptionByCode(question, code)).join('、')" />
                </template>
                <template v-else-if="question.type === 'TRUE_FALSE'">
                  {{ JSON.parse(question.answer) === 'true' ? '正确' : '错误' }}
                </template>
                <template v-else-if="question.type === 'FILL_BLANK' && isMultiBlanks(question)">
                  <!-- 多空填空题答案 -->
                  <div class="multi-blanks-answers">
                    <div v-for="(answer, index) in getMultiBlankAnswers(question)" 
                         :key="index" 
                         class="blank-answer-item">
                      <span class="blank-index">第{{index + 1}}空：</span>
                      <span class="blank-answer">{{ answer || '未作答' }}</span>
                      
                      <!-- 显示题目提示 -->
                      <div v-if="getBlankTip(question, index)" class="blank-tip">
                        提示：{{ getBlankTip(question, index) }}
                      </div>
                      
                      <!-- 显示题目图片 -->
                      <div v-if="getBlankImage(question, index)" class="blank-question-image">
                        <img 
                          :src="getBlankImage(question, index)"
                          alt="题目图片"
                          class="blank-img"
                          @click="handlePreviewImage(getBlankImage(question, index))"
                          style="cursor: pointer;"
                        />
                      </div>
                    </div>
                  </div>

                  <!-- 显示学生上传的所有答案图片 -->
                  <div v-if="question.answerImages && question.answerImages.length > 0" class="student-multi-answer-images">
                    <div class="answer-images-label">所有填空处上传的答案图片：</div>
                    <div class="blank-answer-images">
                      <div
                        v-for="(image, imgIndex) in question.answerImages" 
                        :key="imgIndex"
                        class="answer-img-container"
                      >
                        <img 
                          :src="image"
                          class="answer-img"
                          @click="handlePreviewImage(image)"
                          style="cursor: pointer;"
                        />
                      </div>
                    </div>
                  </div>
                </template>
                <template v-else>
                  <LatexRenderer :content="question.answer || '未作答'" />
                  <div v-if="question.answerImages && question.answerImages.length > 0" class="answer-images">
                    <div
                      v-for="(image, index) in question.answerImages" 
                      :key="index"
                      class="answer-img-container"
                    >
                      <img 
                        :src="image"
                        class="answer-img"
                        @click="handlePreviewImage(image)"
                        style="cursor: pointer;"
                      />
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="800px" append-to-body destroy-on-close>
      <div class="preview-image-container">
        <img :src="previewImageUrl" style="max-width: 100%; max-height: 70vh; object-fit: contain;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExamHistory, getExamHistoryDetail } from '@/api/exam'
import { ElMessage } from 'element-plus'
import LatexRenderer from '@/components/LatexRenderer.vue'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  startTime: null,
  endTime: null,
  examName: null  // 添加考试名称参数
})

// 日期范围
const dateRange = ref([])

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

const loading = ref(false)
const total = ref(0)
const examList = ref([])

// 获取考试记录列表
const getList = async () => {
  loading.value = true
  try {
    // 处理时间范围
    if (dateRange.value && dateRange.value.length === 2) {
      // 确保时间格式正确
      const formatDate = (date) => {
        const d = new Date(date)
        const year = d.getFullYear()
        const month = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        const hour = String(d.getHours()).padStart(2, '0')
        const minute = String(d.getMinutes()).padStart(2, '0')
        const second = String(d.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hour}:${minute}:${second}`
      }
      queryParams.startTime = formatDate(dateRange.value[0])
      queryParams.endTime = formatDate(dateRange.value[1])
    }
    
    const res = await getExamHistory(queryParams)
    examList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取考试记录失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  dateRange.value = []
  queryParams.pageNum = 1
  queryParams.startTime = null
  queryParams.endTime = null
  queryParams.examName = null  // 重置考试名称
  getList()
}

// 每页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '未开始': 'info',
    '进行中': 'warning',
    '已完成': 'success',
    '未参加': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
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

// 获取题目类型排序权重
const getQuestionTypeWeight = (type) => {
  const weightMap = {
    'SINGLE_CHOICE': 1,
    'MULTIPLE_CHOICE': 2,
    'FILL_BLANK': 3,
    'TRUE_FALSE': 4,
    'SHORT_ANSWER': 5,
    'ANALYSIS': 6
  }
  return weightMap[type] || 999
}

// 答题详情相关
const detailVisible = ref(false)
const detailLoading = ref(false)
const questionList = ref([])

// 查看详情
const viewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getExamHistoryDetail(row.examId)
    // 对题目进行排序
    questionList.value = res.data.sort((a, b) => 
      getQuestionTypeWeight(a.type) - getQuestionTypeWeight(b.type)
    )
  } catch (error) {
    ElMessage.error('获取答题详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 根据选项编号获取完整选项
const getOptionByCode = (question, code) => {
  const option = question.optionList?.find(opt => opt.code === code)
  return option ? `${option.code}. ${option.content}` : code
}

// 检查选项是否被选中
const isOptionSelected = (question, code) => {
  if (!question.answer) return false
  if (question.type === 'SINGLE_CHOICE') {
    return JSON.parse(question.answer) === code
  } else if (question.type === 'MULTIPLE_CHOICE') {
    return JSON.parse(question.answer).includes(code)
  }
  return false
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
const getMultiBlankAnswers = (question) => {
  try {
    if (!question.answer) return ['未作答']
    return question.answer.split('|')
  } catch (e) {
    return [question.answer || '未作答']
  }
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

// 图片预览相关
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 显示图片预览
const handlePreviewImage = (url) => {
  previewImageUrl.value = url
  previewVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.exam-history {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 18px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.question-item {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
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
}

.question-score {
  margin-left: 10px;
  color: #666;
}

.question-content {
  font-size: 14px;
}

.question-title {
  margin-bottom: 10px;
  line-height: 1.6;
}

.question-option {
  margin-bottom: 5px;
  padding: 8px 20px;
  border-radius: 4px;
}

.question-option.selected {
  background-color: #ecf5ff;
  color: #409eff;
}

.answer-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}

.answer-label {
  color: #666;
  margin-bottom: 5px;
}

.answer-content {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  min-height: 40px;
}

.exam-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: calc(80vh - 120px);
}

.dialog-content {
  padding: 20px;
  max-height: calc(90vh - 120px);
  overflow-y: auto;
}

.dialog-content::-webkit-scrollbar {
  width: 6px;
}

.dialog-content::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

.answer-images {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.answer-img {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
}

.answer-img:hover {
  border-color: #409eff;
}

/* 多填空题样式 */
.multi-blanks-text {
  margin-bottom: 10px;
}

.blank-tag {
  background-color: #409eff !important;
  color: white !important;
  display: inline-block;
  margin: 0 4px;
  vertical-align: middle;
}

.multi-blanks-answers {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.blank-answer-item {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.blank-index {
  font-weight: bold;
  margin-right: 10px;
}

.blank-answer {
  color: #e6a23c;
}

.blank-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 13px;
  font-style: italic;
}

.blank-question-image {
  margin-top: 10px;
  text-align: center;
}

.blank-img {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.student-multi-answer-images {
  margin-top: 15px;
  border-top: 1px dashed #ebeef5;
  padding-top: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 12px;
}

.answer-images-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
  font-weight: bold;
}

.blank-answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.answer-img-container {
  width: 150px;
  height: 150px;
  background-color: white;
  padding: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.answer-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.answer-img-container:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.preview-image-container {
  text-align: center;
}

.fill-blank-text {
  margin-bottom: 10px;
}
</style> 