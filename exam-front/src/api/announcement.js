import request from '@/utils/request'

export function getAnnouncementList(params) {
  return request({
    url: '/announcement/list',
    method: 'get',
    params
  })
}

export function addAnnouncement(data) {
  return request({
    url: '/announcement',
    method: 'post',
    data
  })
}

export function updateAnnouncement(id, data) {
  return request({
    url: `/announcement/${id}`,
    method: 'put',
    data
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'delete'
  })
}

export function getAnnouncementById(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'get'
  })
}

export function getAllAnnouncements() {
  return request({
    url: '/announcement/all',
    method: 'get'
  })
} 