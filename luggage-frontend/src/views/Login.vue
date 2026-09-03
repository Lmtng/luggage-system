<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">🧳 行李寄存系统</h1>
      <h3 class="subtitle">用户登录</h3>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" block>
            登录
          </el-button>
        </el-form-item>

        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { authApi } from '../api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await authApi.login(form)
    // 保存用户信息
    userStore.login(res.data)
    ElMessage.success('登录成功')

    // 跳转到对应首页
    if (userStore.isAdmin) {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
.title {
  text-align: center;
  margin-bottom: 10px;
}
.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
  font-weight: normal;
}
.register-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}
.register-link a {
  color: #409EFF;
  text-decoration: none;
}
</style>