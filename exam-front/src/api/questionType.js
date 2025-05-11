import request from '@/utils/request'

// 获取题型分页列表
export function getQuestionTypeList(params) {
  return request({
    url: '/question-type/list',
    method: 'get',
    params
  })
}

// 获取题型详情
export function getQuestionTypeById(id) {
  return request({
    url: `/question-type/${id}`,
    method: 'get'
  })
}

// 创建题型
export function createQuestionType(data) {
  return request({
    url: '/question-type',
    method: 'post',
    data
  })
}

// 更新题型
export function updateQuestionType(id, data) {
  return request({
    url: `/question-type/${id}`,
    method: 'put',
    data
  })
}

// 删除题型
export function deleteQuestionType(id) {
  return request({
    url: `/question-type/${id}`,
    method: 'delete'
  })
}

// 获取试卷的题型
export function getPaperQuestionTypes(paperId) {
  return request({
    url: `/exam-paper/${paperId}/question-types`,
    method: 'get'
  })
}

// 更新试卷题型
export function updatePaperQuestionTypes(paperId, questionTypeIds) {
  // 将简单的ID数组转换为完整的对象数组
  const questionTypes = questionTypeIds.map(typeId => ({
    paperId: paperId,
    questionTypeId: typeId
  }));
  
  return request({
    url: `/exam-paper/${paperId}/question-types`,
    method: 'post',
    data: questionTypes
  })
}

// 下载指定题型的模板
export function downloadQuestionTypeTemplate(typeIds) {
  return request({
    url: '/question-type/template',
    method: 'post',
    data: typeIds ,
    responseType: 'blob'
  })
}

