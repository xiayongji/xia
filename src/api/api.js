import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let isRefreshing = false
let failedQueue = []
let redirectTimer = null

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

function forceRedirectToLogin() {
  if (redirectTimer) return
  const path = window.location.pathname
  if (path === '/login' || path === '/register') return
  if (path.startsWith('/chrome-error')) return
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  redirectTimer = setTimeout(() => {
    redirectTimer = null
    window.location.replace('/login')
  }, 100)
}

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    return response.data
  },
  async error => {
    const originalRequest = error.config

    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch(err => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const response = await axios.post('/api/user/auth/refresh', { refreshToken })
          const newToken = response.data?.accessToken || response.data?.token
          if (newToken) {
            localStorage.setItem('token', newToken)
            api.defaults.headers.common.Authorization = `Bearer ${newToken}`
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            processQueue(null, newToken)
            isRefreshing = false
            return api(originalRequest)
          }
        } catch {
          processQueue(new Error('Token refresh failed'))
          isRefreshing = false
          forceRedirectToLogin()
          return Promise.reject(error)
        }
      }

      processQueue(new Error('No refresh token'))
      isRefreshing = false
      forceRedirectToLogin()
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export default api