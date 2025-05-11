<template>
  <div class="app-container">
    <div class="page-header">
      <div class="header-title">
        <el-icon style="margin-right:8px;"><i class="el-icon-office-building"></i></el-icon>
        <span>考场管理</span>
      </div>
      <el-button type="primary" @click="handleAdd" round icon="el-icon-plus">新增考场</el-button>
    </div>

    <el-card class="exam-room-table-card">
    <el-table
      v-loading="loading"
      :data="examRoomList"
      border
      style="width: 100%"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
        :row-class-name="()=>'table-row'"
    >
      <el-table-column prop="name" label="考场名称" />
      <el-table-column prop="location" label="考场位置" />
      <el-table-column prop="capacity" label="考场容量" />
      <el-table-column label="操作" width="250">
        <template #default="scope">
            <el-button type="primary" size="small" @click="handleDetail(scope.row)" round>考试管理</el-button>
            <el-button type="warning" size="small" @click="handleEdit(scope.row)" round>编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)" round>删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </el-card>

    <!-- 分页 -->
    <el-card class="pagination-card">
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    </el-card>

    <!-- 添加/编辑考场对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="考场名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入考场名称" />
        </el-form-item>
        <el-form-item label="考场位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入考场位置" />
        </el-form-item>
        <el-form-item label="考场容量" prop="capacity">
          <el-input-number
            v-model="form.capacity"
            :min="1"
            :max="1000"
            placeholder="请输入考场容量"
          />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, addExamRoom, updateExamRoom, deleteExamRoom } from '@/api/examRoom'

const router = useRouter()

// 数据
const loading = ref(false)
const examRoomList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const form = ref({
  id: null,
  name: '',
  location: '',
  capacity: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入考场名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入考场位置', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入考场容量', trigger: 'blur' },
    { type: 'number', min: 1, message: '容量必须大于0', trigger: 'blur' }
  ]
}

// 获取考场列表
const loadExamRoomList = async () => {
  loading.value = true
  try {
    const { data } = await getList(page.value, size.value)
    examRoomList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取考场列表失败')
  } finally {
    loading.value = false
  }
}

// 处理页码改变
const handleCurrentChange = (val) => {
  page.value = val
  loadExamRoomList()
}

// 处理每页条数改变
const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  loadExamRoomList()
}

// 新增考场
const handleAdd = () => {
  form.value = {
    id: null,
    name: '',
    location: '',
    capacity: 1
  }
  dialogTitle.value = '新增考场'
  dialogVisible.value = true
}

// 编辑考场
const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑考场'
  dialogVisible.value = true
}

// 查看考场详情
const handleDetail = (row) => {
  router.push(`/exam-room/detail/${row.id}`)
}

// 删除考场
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该考场吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteExamRoom(row.id)
      ElMessage.success('删除成功')
      loadExamRoomList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const submitForm = async () => {
  try {
    const data = { ...form.value }
    if (data.id) {
      await updateExamRoom(data.id, data)
      ElMessage.success('更新成功')
    } else {
      await addExamRoom(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadExamRoomList()
  } catch (error) {
    ElMessage.error(form.value.id ? '更新失败' : '添加失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadExamRoomList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: #f6f8fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding: 12px 0 8px 0;
}

.header-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
}

.exam-room-table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(64,158,255,0.08);
  margin-bottom: 24px;
  padding: 0 0 8px 0;
}

.el-table {
  border-radius: 10px;
  overflow: hidden;
}

 .el-table__header th {
  background: #f5f7fa !important;
  color: #606266 !important;
  font-weight: bold;
}

.table-row:hover {
  background: #f0f7ff !important;
}

.pagination-card {
  border-radius: 10px;
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.06);
  margin-top: 10px;
  padding: 8px 0;
  background: #fff;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 