import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

api.interceptors.response.use(
  response => {
    const result = response.data

    if (result.code !== 200) {
      const message = result.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }

    return result
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const message =
        error.response.data?.message || '服务器错误'

      if (status === 401) {
        ElMessage.error('请先登录')

        const userStore = useUserStore()
        userStore.logout()

        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('权限不足')
      } else if (status === 404) {
        ElMessage.error('接口不存在')
      } else {
        ElMessage.error(message)
      }
    } else {
      ElMessage.error('网络连接失败，请检查后端是否启动')
    }

    return Promise.reject(error)
  }
)

export default api