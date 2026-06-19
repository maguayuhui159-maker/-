import request from '../utils/request'

export function fetchList(query) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params: query
  })
}

export function updateUser(id, data) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'delete'
  })
}
