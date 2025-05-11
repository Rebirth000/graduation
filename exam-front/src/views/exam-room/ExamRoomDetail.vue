<template>
  <div class="app-container">
    <!-- 考场基本信息 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>考场信息</span>
        </div>
      </template>
      <div v-if="examRoom" class="exam-room-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="考场名称">{{ examRoom.name }}</el-descriptions-item>
          <el-descriptions-item label="考场位置">{{ examRoom.location }}</el-descriptions-item>
          <el-descriptions-item label="考场容量">{{ examRoom.capacity }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 关联考试列表 -->
    <el-card class="box-card mt-4">
      <template #header>
        <div class="card-header">
          <span>关联考试</span>
          <el-button type="primary" @click="handleAddExam">添加考试</el-button>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="examList" border style="width: 100%">
        <el-table-column prop="examName" label="考试名称" />
        <el-table-column prop="examStartTime" label="开始时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.examStartTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="examEndTime" label="结束时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.examEndTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleDeleteExam(scope.row)">
              解除关联
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加考试对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加考试"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择考试" prop="examId">
          <el-select v-model="form.examId" placeholder="请选择考试" style="width: 100%">
            <el-option
              v-for="exam in availableExams"
              :key="exam.id"
              :label="exam.name"
              :value="exam.id"
            />
          </el-select>
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamsByRoomId, addExamRoomExam, deleteExamRoomExam } from '@/api/examRoomExam'
import { getExamRoomById } from '@/api/examRoom'
import { getExamList } from '@/api/exam'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const examRoomId = parseInt(route.params.id)

// 数据
const loading = ref(false)
const examRoom = ref(null)
const examList = ref([])
const availableExams = ref([])
const dialogVisible = ref(false)
const form = ref({
  examId: null
})

// 表单验证规则
const rules = {
  examId: [
    { required: true, message: '请选择考试', trigger: 'change' }
  ]
}

// 获取考场信息
const loadExamRoom = async () => {
  try {
    const { data } = await getExamRoomById(examRoomId)
    examRoom.value = data
  } catch (error) {
    ElMessage.error('获取考场信息失败')
  }
}

// 获取关联的考试列表
const loadExamList = async () => {
  loading.value = true
  try {
    const { data } = await getExamsByRoomId(examRoomId)
    examList.value = data
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// 获取可选的考试列表
const loadAvailableExams = async () => {
  try {
    const { data: { records } } = await getExamList({
      page: 1,
      size: 1000
    })
    // 过滤掉已关联的考试
    const existingExamIds = examList.value.map(exam => exam.examId)
    availableExams.value = records.filter(exam => !existingExamIds.includes(exam.id))
  } catch (error) {
    ElMessage.error('获取可选考试列表失败')
  }
}

// 添加考试按钮点击
const handleAddExam = () => {
  form.value.examId = null
  loadAvailableExams()
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    const data = {
      examRoomId,
      examId: form.value.examId
    }
    await addExamRoomExam(data)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    loadExamList()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

// 删除考试关联
const handleDeleteExam = (exam) => {
  ElMessageBox.confirm(
    '确定要解除该考试的关联吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteExamRoomExam(exam.id)
      ElMessage.success('解除关联成功')
      loadExamList()
    } catch (error) {
      ElMessage.error('解除关联失败')
    }
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadExamRoom()
  loadExamList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mt-4 {
  margin-top: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-room-info {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 