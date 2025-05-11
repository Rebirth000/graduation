<template>
  <div class="app-container">
    <!-- 考试信息 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>考试信息</span>
        </div>
      </template>
      <div v-if="exam" class="exam-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="考试名称">{{ exam.name }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(exam.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(exam.endTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 已分配考场列表 -->
    <el-card class="box-card mt-4">
      <template #header>
        <div class="card-header">
          <span>已分配考场</span>
          <el-button type="primary" @click="handleAssign">分配考场</el-button>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="assignedRooms" border style="width: 100%">
        <el-table-column prop="roomName" label="考场名称" />
        <el-table-column prop="roomLocation" label="考场位置" />
        <el-table-column prop="roomCapacity" label="考场容量" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="handleUnassign(scope.row)"
            >
              取消分配
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分配考场对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="分配考场"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择考场" prop="examRoomId">
          <el-select v-model="form.examRoomId" placeholder="请选择考场" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="room.name"
              :value="room.id"
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
import { getExamById } from '@/api/exam'
import { getExamRoomList } from '@/api/examRoom'
import { getRoomsByExamId, addExamRoomExam, deleteExamRoomExam } from '@/api/examRoomExam'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const examId = parseInt(route.params.id)

// 数据
const loading = ref(false)
const exam = ref(null)
const assignedRooms = ref([])
const availableRooms = ref([])
const dialogVisible = ref(false)
const form = ref({
  examRoomId: null
})

// 表单验证规则
const rules = {
  examRoomId: [
    { required: true, message: '请选择考场', trigger: 'change' }
  ]
}

// 获取考试信息
const loadExam = async () => {
  try {
    const { data }  = await getExamById(examId)
    exam.value = data
  } catch (error) {
    ElMessage.error('获取考试信息失败')
  }
}

// 获取已分配的考场列表
const loadAssignedRooms = async () => {
  loading.value = true
  try {
    const { data } = await getRoomsByExamId(examId)
    assignedRooms.value = data
  } catch (error) {
    ElMessage.error('获取已分配考场列表失败')
  } finally {
    loading.value = false
  }
}

// 获取可选的考场列表
const loadAvailableRooms = async () => {
  try {
    const { data } = await getExamRoomList()
    // 过滤掉已分配的考场
    const assignedRoomIds = assignedRooms.value.map(room => room.examRoomId)
    availableRooms.value = data.filter(room => !assignedRoomIds.includes(room.id))
  } catch (error) {
    console.log(error)
    ElMessage.error('获取可选考场列表失败')
  }
}

// 分配考场按钮点击
const handleAssign = () => {
  form.value.examRoomId = null
  loadAvailableRooms()
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    const data = {
      examId,
      examRoomId: form.value.examRoomId
    }
    await addExamRoomExam(data)
    ElMessage.success('分配成功')
    dialogVisible.value = false
    loadAssignedRooms()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

// 取消分配
const handleUnassign = (room) => {
  ElMessageBox.confirm(
    '确定要取消该考场的分配吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteExamRoomExam(room.id)
      ElMessage.success('取消分配成功')
      loadAssignedRooms()
    } catch (error) {
      ElMessage.error('取消分配失败')
    }
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadExam()
  loadAssignedRooms()
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

.exam-info {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 