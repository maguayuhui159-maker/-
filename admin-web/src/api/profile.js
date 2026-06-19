import request from '../utils/request'

export function fetchProfileSummary() {
  return request({
    url: '/api/profile/summary',
    method: 'get'
  })
}
