import axios from 'axios'

const service = axios.create({
  // Use same-origin in production so browser requests current deployed domain.
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 5000
})

// Request interceptor
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code && res.code !== 200) {
      const businessError = new Error(res.message || 'Error')
      businessError.response = { data: res, status: 200 }
      return Promise.reject(businessError)
    } else {
      return res
    }
  },
  error => {
    const url = error.config ? error.config.url : ''
    const method = error.config ? error.config.method : ''
    console.warn(`[请求失败] ${method?.toUpperCase()} ${url}`)
    return Promise.reject(error)
  }
)

export default service
