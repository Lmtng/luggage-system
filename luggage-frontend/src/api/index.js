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

    // 订单、认证接口返回统一的 Result；部分早期管理接口直接返回实体或数组。
    // 在这里将旧格式统一包装，避免正常响应被误判为失败。
    if (
      result !== null &&
      typeof result === 'object' &&
      Object.prototype.hasOwnProperty.call(result, 'code')
    ) {
      if (result.code !== 200) {
        const message = result.message || '请求失败'
        ElMessage.error(message)
        return Promise.reject(new Error(message))
      }

      return result
    }

    return {
      code: 200,
      message: '操作成功',
      data: result
    }
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
