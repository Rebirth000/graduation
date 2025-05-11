import request from '@/utils/request'

// 获取讨论列表
export function getDiscussionList(params) {
  return request({
    url: '/discussions',
    method: 'get',
    params
  })
}

// 获取讨论详情
export function getDiscussionById(id) {
  return request({
    url: `/discussions/${id}`,
    method: 'get'
  })
}

// 创建讨论
export function createDiscussion(data) {
  return request({
    url: '/discussions',
    method: 'post',
    data
  })
}

// 更新讨论
export function updateDiscussion(id, data) {
  return request({
    url: `/discussions/${id}`,
    method: 'put',
    data
  })
}

// 删除讨论
export function deleteDiscussion(id) {
  return request({
    url: `/discussions/${id}`,
    method: 'delete'
  })
}

// 获取讨论回复列表
export function getDiscussionReplies(discussionId) {
  return request({
    url: `/discussions/${discussionId}/replies`,
    method: 'get'
  })
}

// 添加回复
export function addReply(discussionId, data) {
  return request({
    url: `/discussions/${discussionId}/replies`,
    method: 'post',
    data
  })
}

// 更新回复
export function updateReply(id, data) {
  return request({
    url: `/discussions/replies/${id}`,
    method: 'put',
    data
  })
}

// 删除回复
export function deleteReply(id) {
  return request({
    url: `/discussions/replies/${id}`,
    method: 'delete'
  })
} 