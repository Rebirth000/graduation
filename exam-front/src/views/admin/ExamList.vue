<el-table
  v-loading="loading"
  :data="examList"
  border
  style="width: 100%"
>
  <el-table-column prop="name" label="考试名称" />
  <el-table-column prop="courseName" label="所属课程" />
  <el-table-column prop="paperName" label="试卷名称" />
  <el-table-column label="开始时间">
    <template #default="scope">
      {{ formatDateTime(scope.row.startTime) }}
    </template>
  </el-table-column>
  <el-table-column label="结束时间">
    <template #default="scope">
      {{ formatDateTime(scope.row.endTime) }}
    </template>
  </el-table-column>
  <el-table-column label="操作" width="250">
    <template #default="scope">
      <el-button
        type="primary"
        size="small"
        @click="handleAssignRoom(scope.row)"
      >
        分配考场
      </el-button>
      <el-button
        type="warning"
        size="small"
        @click="handleEdit(scope.row)"
      >
        编辑
      </el-button>
      <el-button
        type="danger"
        size="small"
        @click="handleDelete(scope.row)"
      >
        删除
      </el-button>
    </template>
  </el-table-column>
</el-table> 