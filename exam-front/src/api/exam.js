import request from '@/utils/request'

// 获取试卷列表
export function getExamPaperList(params) {
  return request({
    url: '/exam-paper/list',
    method: 'get',
    params
  })
}

// 获取所有试卷（不分页）
export function getAllExamPapers() {
  return request({
    url: '/exam-paper',
    method: 'get'
  })
}

// 获取试卷详情
export function getExamPaperById(id) {
  return request({
    url: `/exam-paper/${id}`,
    method: 'get'
  })
}

// 创建试卷
export function createExamPaper(data) {
  return request({
    url: '/exam-paper',
    method: 'post',
    data
  })
}

// 更新试卷
export function updateExamPaper(id, data) {
  return request({
    url: `/exam-paper/${id}`,
    method: 'put',
    data
  })
}

// 删除试卷
export function deleteExamPaper(id) {
  return request({
    url: `/exam-paper/${id}`,
    method: 'delete'
  })
}

// 获取试卷题目列表
export function getQuestionList(paperId) {
  return request({
    url: `/exam-paper/${paperId}/questions`,
    method: 'get'
  })
}

// 创建题目
export function createQuestion(paperId, data) {
  return request({
    url: `/exam-paper/${paperId}/questions`,
    method: 'post',
    data
  })
}

// 更新题目
export function updateQuestion(id, data) {
  return request({
    url: `/exam-paper/questions/${id}`,
    method: 'put',
    data
  })
}

// 删除题目
export function deleteQuestion(id) {
  return request({
    url: `/exam-paper/questions/${id}`,
    method: 'delete'
  })
}

// 更新题目顺序
export function updateQuestionOrder(paperId, questionIds) {
  return request({
    url: `/exam-paper/${paperId}/questions/order`,
    method: 'put',
    data: questionIds
  })
}

// 导入试卷题目
export function importExamPaperQuestions(id, formData) {
  return request({
    url: `/exam-paper/${id}/import`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载试卷题目模板
export function downloadQuestionsTemplate() {
  return request({
    url: '/exam-paper/template',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取考试列表
 */
export function getExamList() {
  return request({
    url: '/exam',
    method: 'get'
  })
}

/**
 * 获取考试详情
 * @param {number} id - 考试ID
 */
export function getExamById(id) {
  return request({
    url: `/exam/${id}`,
    method: 'get'
  })
}

/**
 * 根据试卷ID获取考试列表
 * @param {number} paperId - 试卷ID
 */
export function getExamsByPaperId(paperId) {
  return request({
    url: `/exam/paper/${paperId}`,
    method: 'get'
  })
}

/**
 * 添加考试
 * @param {Object} data - 考试信息
 */
export function addExam(data) {
  return request({
    url: '/exam',
    method: 'post',
    data
  })
}

/**
 * 更新考试
 * @param {number} id - 考试ID
 * @param {Object} data - 考试信息
 */
export function updateExam(id, data) {
  return request({
    url: `/exam/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除考试
 * @param {number} id - 考试ID
 */
export function deleteExam(id) {
  return request({
    url: `/exam/${id}`,
    method: 'delete'
  })
}

// 获取我的考试列表
export function getMyExams(params) {
  return request({
    url: '/exam/registration/my',
    method: 'get',
    params
  })
}

// 进入考试
export function enterExam(id) {
  return request({
    url: `/exam/registration/${id}/enter`,
    method: 'post'
  })
}

// 获取考试信息和试题
export function getExamDetail(registrationId) {
  return request({
    url: `/exam/registration/${registrationId}/detail`,
    method: 'get'
  })
}

// 提交考试答案
export function submitExamAnswers(registrationId, answers) {
  return request({
    url: `/exam/registration/${registrationId}/submit`,
    method: 'post',
    data: answers
  })
}

// 获取考试历史记录
export function getExamHistory(params) {
  return request({
    url: '/student/exam-history',
    method: 'get',
    params
  })
}

export function getExamHistory2(params) {
  return request({
    url: '/student/exam-history/admin',
    method: 'get',
    params
  })
}

// 获取考试答题详情
export function getExamHistoryDetail(examId) {
  return request({
    url: `/student/exam-history/${examId}`,
    method: 'get'
  })
} 

export function getExamHistoryDetail2(examId, studentId) {
  return request({
    url: `/student/exam-history/detail/admin/${examId}/${studentId}`,
    method: 'get'
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

// 取消报名
export function cancelRegistration(id) {
  return request({
    url: `/exam/registration/${id}`,
    method: 'delete'
  })
}

// 导出答卷 - Excel格式
export function exportExamAnswersExcel(data) {
  return request({
    url: '/student/exam-history/export/excel',
    method: 'post',
    data,
    responseType: 'blob'
  })
} 