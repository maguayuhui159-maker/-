import request from '../utils/request'

export function submitProfileChange(data) {
  return request({
    url: '/api/profile-change/submit',
    method: 'post',
    data
  })
}

export function fetchMyProfileChanges(username) {
  return request({
    url: '/api/profile-change/my',
    method: 'get',
    params: { username }
  })
}

export function fetchCurrentProfile(username) {
  return request({
    url: '/api/profile-change/current',
    method: 'get',
    params: { username }
  })
}

export function fetchProfileChangeRequests(status) {
  return request({
    url: '/api/admin/profile-change-requests',
    method: 'get',
    params: status ? { status } : {}
  })
}

export function reviewProfileChangeRequest(id, data) {
  return request({
    url: `/api/admin/profile-change-requests/${id}/review`,
    method: 'put',
    data
  })
}

