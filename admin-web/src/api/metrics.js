import request from '../utils/request'

export function fetchAdminOverviewMetrics() {
  return request({
    url: '/api/admin/metrics/overview',
    method: 'get'
  })
}
