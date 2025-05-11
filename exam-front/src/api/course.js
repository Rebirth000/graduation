import request from '@/utils/request'

// 获取课程列表（分页）
export function getCourseList(params) {
  return request({
    url: '/course/list',
    method: 'get',
    params
  })
}

// 获取所有课程
export function getAllCourses() {
  return request({
    url: '/course',
    method: 'get'
  })
}

// 获取课程详情
export function getCourseById(id) {
  return request({
    url: `/course/${id}`,
    method: 'get'
  })
}

// 创建课程
export function createCourse(data) {
  return request({
    url: '/course',
    method: 'post',
    data
  })
}

// 更新课程
export function updateCourse(id, data) {
  return request({
    url: `/course/${id}`,
    method: 'put',
    data
  })
}

// 删除课程
export function deleteCourse(id) {
  return request({
    url: `/course/${id}`,
    method: 'delete'
  })
}

// 获取课程关联的试卷
export function getAssociatedPapers(courseId) {
  return request({
    url: `/course/${courseId}/exam-papers`,
    method: 'get'
  })
}

// 添加试卷到课程
export function associatePaper(courseId, paperId) {
  return request({
    url: `/course/${courseId}/exam-paper/${paperId}`,
    method: 'post'
  })
}

// 从课程移除试卷
export function dissociatePaper(courseId, paperId) {
  return request({
    url: `/course/${courseId}/exam-paper/${paperId}`,
    method: 'delete'
  })
} 