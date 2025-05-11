<template>
  <div class="announcements-container">
    <!-- 背景图片层 -->
    <div class="background-layer"></div>
    
    <!-- 内容层 -->
    <div class="content-layer">
      <div class="announcements">
        <div class="announcement-carousel">
          <el-carousel 
            :interval="4000" 
            type="card" 
            height="400px"
            :autoplay="true"
            trigger="click"
          >
            <el-carousel-item v-for="(item, index) in sortedAnnouncements" :key="item.id">
              <el-card class="announcement-card" shadow="hover">
                <div class="corner-tag" v-if="index === 0">
                  <el-tag type="danger" effect="dark" size="small">最新</el-tag>
                </div>
                <template #header>
                  <div class="card-header">
                    <div class="title-wrapper">
                      <el-icon class="title-icon"><Notification /></el-icon>
                      <h3>{{ item.title }}</h3>
                    </div>
                    <div class="time">
                      <el-icon><Clock /></el-icon>
                      <span>{{ dayjs(item.createdAt).format('YYYY-MM-DD HH:mm:ss') }}</span>
                    </div>
                  </div>
                </template>
                <div class="card-content">
                  <p>{{ item.content }}</p>
                </div>
              </el-card>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="announcement-list">
          <el-card shadow="hover" class="list-card">
            <template #header>
              <div class="list-header">
                <span class="list-title">公告列表</span>
                <el-tag type="info" size="small">共 {{ announcements.length }} 条公告</el-tag>
              </div>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in announcements"
                :key="item.id"
                :timestamp="dayjs(item.createdAt).format('YYYY-MM-DD HH:mm:ss')"
                placement="top"
                :type="index < 3 ? 'primary' : ''"
                :size="index < 3 ? 'large' : 'normal'"
              >
                <el-card shadow="hover" @click="scrollToCarousel(index)">
                  <h4>{{ item.title }}</h4>
                  <p class="list-content">{{ item.content }}</p>
                  <div class="view-more" v-if="item.content.length > 100">
                    <el-button type="text" size="small">查看详情</el-button>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAllAnnouncements } from '@/api/announcement'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Notification, Clock } from '@element-plus/icons-vue'

const announcements = ref([])

// 计算属性：按创建时间降序排序的公告列表
const sortedAnnouncements = computed(() => {
  return [...announcements.value].sort((a, b) => {
    return new Date(b.createdAt) - new Date(a.createdAt)
  })
})

const loadAnnouncements = async () => {
  try {
    const { data } = await getAllAnnouncements()
    announcements.value = data.records
  } catch (error) {
    console.error(error)
    ElMessage.error('获取公告失败')
  }
}

const scrollToCarousel = (index) => {
  // 这里可以添加滚动到对应轮播项的逻辑
  console.log('跳转到公告:', index)
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
}

.background-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('https://img.freepik.com/free-vector/hand-painted-watercolor-pastel-sky-background_23-2148902771.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  opacity: 0.3;
  z-index: -1;
}

.content-layer {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.announcement-carousel {
  margin-bottom: 40px;
  padding: 20px 0;
}

.announcement-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(245,245,245,0.95) 100%);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.announcement-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.announcement-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #67C23A, #E6A23C, #F56C6C);
}

.corner-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.title-wrapper {
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 10px;
  color: #409EFF;
  font-size: 20px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.time {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.time .el-icon {
  margin-right: 5px;
}

.card-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  color: #606266;
  line-height: 1.8;
}

.card-content p {
  margin: 0;
  font-size: 15px;
  white-space: pre-wrap;
}

.list-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}

.list-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.list-content {
  margin: 10px 0 0;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
}

.view-more {
  margin-top: 10px;
  text-align: right;
}

/* 轮播图样式优化 */
:deep(.el-carousel__item) {
  border-radius: 12px;
  padding: 10px;
  box-sizing: border-box;
}

:deep(.el-carousel__item.is-active) {
  transform: scale(1.05);
  transition: transform 0.3s ease;
}

:deep(.el-carousel__arrow) {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.9);
  color: #409EFF;
  font-size: 18px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

:deep(.el-carousel__arrow:hover) {
  background-color: #409EFF;
  color: white;
  transform: scale(1.1);
}

:deep(.el-carousel__indicators) {
  bottom: 15px;
}

:deep(.el-carousel__indicator button) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #c0c4cc;
}

:deep(.el-carousel__indicator.is-active button) {
  background-color: #409EFF;
  width: 12px;
  height: 12px;
}

/* 时间线样式优化 */
:deep(.el-timeline-item__content) {
  width: 100%;
  cursor: pointer;
}

:deep(.el-timeline-item__node) {
  background-color: #409EFF;
  box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
}

:deep(.el-timeline-item__timestamp) {
  color: #666;
  font-size: 14px;
}

/* 卡片悬停效果 */
:deep(.el-card):hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}
</style>