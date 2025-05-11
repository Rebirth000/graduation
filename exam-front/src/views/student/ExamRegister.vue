<template>
  <div class="exam-register">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="搜索">
          <el-input
            v-model="queryParams.examName"
            placeholder="请输入考试名称或课程名称"
            clearable
            style="width: 240px;"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考试卡片列表 -->
    <div v-loading="loading" class="card-container">
      <el-empty v-if="examList.length === 0" description="暂无可报名考试" />
      
      <el-row :gutter="20">
        <el-col v-for="exam in examList" :key="exam.id" :xs="24" :sm="12" :md="8" :lg="8" :xl="6">
          <el-card class="exam-card" shadow="hover">
            <div class="card-header">
              <el-icon><Calendar /></el-icon>
              <span class="exam-name">{{ exam.name }}</span>
            </div>
            
            <div class="card-content">
              <div class="info-item">
                <span class="label">所属课程：</span>
                <span class="value">{{ exam.courseName }}</span>
              </div>
              
              <div class="info-item">
                <span class="label">试卷名称：</span>
                <span class="value">{{ exam.paperName }}</span>
              </div>
              
              <div class="info-item">
                <span class="label">考试时间：</span>
                <span class="value time-value">
                  {{ formatDateTime(exam.startTime) }} 至 {{ formatDateTime(exam.endTime) }}
                </span>
              </div>
              
              <div class="status-actions">
                <div class="status-tag" :class="getStatusClass(exam)">
                  {{ getStatusText(exam) }}
                </div>
                
                <div class="actions">
          <el-button 
                    v-if="!exam.registrationStatus" 
            type="primary" 
                    size="small"
                    @click="handleRegister(exam)"
          >
                    报名考试
          </el-button>
                  
          <el-button 
                    v-else-if="exam.registrationStatus === 'PENDING'" 
            type="warning" 
                    size="small"
            disabled
          >
            待分配考场
          </el-button>
                  
          <el-button 
                    v-else-if="exam.registrationStatus === 'ASSIGNED'" 
                    type="danger" 
                    size="small"
                    @click="handleCancelRegistration(exam)"
                  >
                    取消报名
          </el-button>
                  
          <el-button 
                    v-else-if="exam.registrationStatus === 'CANCELLED'" 
            type="primary" 
                    size="small"
                    @click="handleRegister(exam)"
          >
            重新报名
          </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { formatDateTime } from '../../utils/format'
import { cancelRegistration } from '@/api/exam'

const loading = ref(false)
const examList = ref([])
const total = ref(0)
const registrationIds = ref({}) // Map to store registration IDs by exam ID

// 获取状态显示文本
const getStatusText = (exam) => {
  if (!exam.registrationStatus) return '未报名'
  switch (exam.registrationStatus) {
    case 'PENDING': return '待分配考场'
    case 'ASSIGNED': return '已报名'
    case 'CANCELLED': return '已取消'
    default: return '未知状态'
  }
}

// 获取状态CSS类
const getStatusClass = (exam) => {
  if (!exam.registrationStatus) return 'status-none'
  switch (exam.registrationStatus) {
    case 'PENDING': return 'status-pending'
    case 'ASSIGNED': return 'status-assigned'
    case 'CANCELLED': return 'status-cancelled'
    default: return 'status-none'
  }
}

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  examName: null
})

// 获取可报名考试列表
const getExamList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/exam/registration/available',
      method: 'get',
      params: {
        page: queryParams.pageNum,
        size: queryParams.pageSize,
        keyword: queryParams.examName
      }
    })
    console.log('考试列表完整响应:', res)
    console.log('考试列表数据:', res.data.records)
    
    // 同时获取所有已报名考试的记录
    const myRes = await request({
      url: '/exam/registration/my',
      method: 'get',
      params: {
        page: 1,
        size: 100 // 假设不会超过100条记录
      }
    })
    
    // 建立考试ID到注册ID的映射
    const regMap = {}
    if (myRes?.data?.records) {
      myRes.data.records.forEach(reg => {
        regMap[reg.examId] = reg.id
      })
    }
    console.log('报名ID映射:', regMap)
    
    registrationIds.value = regMap
    
    examList.value = res.data.records.map(exam => {
      console.log(`考试 ${exam.name} 的报名ID:`, regMap[exam.id])
      return exam
    })
    total.value = res.data.total
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// 报名考试
const handleRegister = (exam) => {
  ElMessageBox.confirm(`确认报名考试：${exam.name}？`, '提示', {
    type: 'info'
  }).then(async () => {
    await request({
      url: `/exam/registration/${exam.id}`,
      method: 'post'
    })
    ElMessage.success('报名成功')
    getExamList()
  })
}

// 取消报名
const handleCancelRegistration = (exam) => {
  console.log('取消报名的考试信息:', exam)
  
  if (!exam.id) {
    console.error('考试ID不存在:', exam)
    ElMessage.error('无法获取考试信息')
    return
  }
  
  const registrationId = registrationIds.value[exam.id]
  if (!registrationId) {
    console.error('考试ID对应的报名记录未找到:', exam.id)
    ElMessage.error('无法找到对应的报名记录')
    return
  }

  ElMessageBox.confirm(`确认取消报名考试：${exam.name}？\n取消后将释放已分配的考场座位。`, '提示', {
    type: 'warning',
    confirmButtonText: '确认取消',
    cancelButtonText: '继续报名',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      console.log('准备取消报名，报名ID:', registrationId)
      await cancelRegistration(registrationId)
      ElMessage.success('取消报名成功')
      getExamList()
    } catch (error) {
      console.error('取消报名失败:', error)
      ElMessage.error(error.response?.data?.message || '取消报名失败')
    }
  })
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getExamList()
}

// 重置查询
const resetQuery = () => {
  queryParams.examName = null
  queryParams.pageNum = 1
  getExamList()
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  queryParams.pageNum = 1
  getExamList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getExamList()
}

onMounted(() => {
  getExamList()
})
</script>

<style scoped>
.exam-register {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 18px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.card-container {
  margin-bottom: 20px;
}

.exam-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  height: 100%;
}

.exam-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.card-header .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.exam-name {
  font-size: 16px;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-content {
  color: #606266;
}

.info-item {
  margin-bottom: 8px;
  display: flex;
}

.info-item .label {
  min-width: 80px;
  color: #909399;
}

.info-item .value {
  flex: 1;
  word-break: break-all;
}

.time-value {
  font-size: 13px;
  color: #67c23a;
}

.status-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ebeef5;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-none {
  background-color: #f4f4f5;
  color: #909399;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-assigned {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-cancelled {
  background-color: #fef0f0;
  color: #f56c6c;
}

.actions {
  text-align: right;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 