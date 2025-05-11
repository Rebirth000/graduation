<template>
  <div class="admin-list">
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">新增老师</el-button>
    </div>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="ID"/>
      <el-table-column prop="username" label="用户名"/>
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'success'">
            {{ row.role === 'SUPER_ADMIN' ? '管理员' : '老师' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间">
        <template #default="{ row }">
          {{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right">
        <template #default="{ row }">
          <el-button 
            v-if="row.role !== 'SUPER_ADMIN'"
            link 
            type="primary" 
            @click="handleResetPassword(row)"
          >
            重置密码
          </el-button>
          <el-button 
            v-if="row.role !== 'SUPER_ADMIN'"
            link 
            type="danger" 
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="新增老师"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminList, addAdmin, deleteAdmin, resetPassword } from '@/api/admin'
import dayjs from 'dayjs'

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const page = ref(1)
const size = ref(10)
const total = ref(0)

const form = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
  } else {
    if (form.value.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能小于3位', trigger: 'blur' }
  ],
  password: [{ validator: validatePass, trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const { data } = await getAdminList({
      page: page.value,
      size: size.value
    })
    tableData.value = data.records
    total.value = data.total
  } catch (error) {
    console.log(error)
  }
}

const handleAdd = () => {
  form.value = {
    username: '',
    password: '',
    confirmPassword: ''
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该老师账号吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAdmin(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleResetPassword = (row) => {
  ElMessageBox.confirm('确认重置该老师密码吗？重置后密码将变为：123456', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await resetPassword(row.id)
      ElMessage.success('密码重置成功')
    } catch (error) {
      ElMessage.error('密码重置失败')
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await addAdmin({
          username: form.value.username,
          password: form.value.password
        })
        ElMessage.success('添加成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.log(error)
      }
    }
  })
}

const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  loadData()
}

const handleCurrentChange = (val) => {
  page.value = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.admin-list {
  padding: 20px;
}

.operation-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 