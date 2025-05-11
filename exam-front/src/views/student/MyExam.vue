<template>
  <div class="my-exam">
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
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考试列表 -->
    <el-table
      v-loading="loading"
      :data="registrations"
      style="width: 100%"
    >
      <el-table-column prop="examName" label="考试名称" min-width="180" />
      <el-table-column label="考试时间" min-width="300">
        <template #default="{ row }">
          {{ formatDateTime(row.examStartTime) }} 至 {{ formatDateTime(row.examEndTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="examRoomName" label="考场" align="center" width="100" />
      <el-table-column prop="examRoomLocation" label="考场位置" align="center" width="100" />
      <el-table-column prop="seatNumber" label="座位号" align="center" width="100" />
      <el-table-column label="操作" align="center" width="120">
        <template #default="{ row }">
          <el-button
            v-if="canEnterExam(row)"
            link
            type="primary"
            @click="handleEnterExam(row)"
          >
            进入考试
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyExams, enterExam } from '@/api/exam'
import { formatDateTime } from '@/utils/format'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const registrations = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  examName: null
})

// 获取报名记录列表
const fetchRegistrations = async () => {
  loading.value = true
  try {
    const { data } = await getMyExams({
      page: queryParams.pageNum,
      size: queryParams.pageSize,
      keyword: queryParams.examName
    })
    registrations.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  fetchRegistrations()
}

// 重置查询
const resetQuery = () => {
  queryParams.examName = null
  queryParams.pageNum = 1
  fetchRegistrations()
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  queryParams.pageNum = 1
  fetchRegistrations()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchRegistrations()
}

// 判断是否可以进入考试
const canEnterExam = (registration) => {
  if (registration.status !== 'ASSIGNED') {
    return false
  }
  const now = new Date()
  const startTime = new Date(registration.examStartTime)
  const endTime = new Date(registration.examEndTime)
  return now >= startTime && now <= endTime
}

// 进入考试
const handleEnterExam = async (registration) => {
  try {
    await enterExam(registration.id)
    router.push(`/exam/${registration.id}`)
  } catch (error) {
    ElMessage.error('进入考试失败')
  }
}

onMounted(() => {
  fetchRegistrations()
})
</script>

<style scoped>
.my-exam {
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

:deep(.el-table) {
  margin-top: 20px;
}
</style> 