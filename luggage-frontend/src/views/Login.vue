<template>
  <div class="login-container">
    <div class="login-shell">
      <section class="intro-panel">
        <p class="system-label">SMART STORAGE SYSTEM</p>
        <h1>行李寄存系统</h1>
        <p class="intro-lead">让寄存流程更清晰，让每一次出行更从容。</p>

        <div class="intro-list">
          <div class="intro-item">
            <span>01</span>
            <div>
              <strong>便捷寄存</strong>
              <p>在线选择空闲柜格，快速创建寄存订单。</p>
            </div>
          </div>
          <div class="intro-item">
            <span>02</span>
            <div>
              <strong>安全取件</strong>
              <p>通过专属取件码完成身份验证与取件。</p>
            </div>
          </div>
          <div class="intro-item">
            <span>03</span>
            <div>
              <strong>信息清晰</strong>
              <p>订单状态、寄存时长与费用明细一目了然。</p>
            </div>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-content">
          <p class="form-label">USER ACCESS</p>
          <h2>账户登录</h2>
          <p class="form-description">请输入账户信息进入行李寄存系统</p>

          <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                  v-model="form.username"
                  placeholder="请输入用户名"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item class="submit-item">
              <el-button type="primary" @click="handleLogin" :loading="loading">
                登录
              </el-button>
            </el-form-item>

            <div class="register-link">
              还没有账号？<router-link to="/register">立即注册</router-link>
            </div>
          </el-form>
        </div>
      </section>
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
  display: grid;
  height: 100vh;
  min-height: 700px;
  padding: 56px;
  place-items: center;
  background: var(--brand-cream);
}

.login-shell {
  display: grid;
  width: min(1080px, calc(100vw - 112px));
  min-height: 590px;
  grid-template-columns: 1.08fr 0.92fr;
  border: 1px solid var(--brand-blue);
  background: var(--brand-paper);
  box-shadow: 18px 18px 0 rgba(24, 77, 151, 0.12);
}

.intro-panel {
  padding: 64px 66px;
  color: #ffffff;
  background:
    linear-gradient(90deg, transparent 49.7%, rgba(255, 255, 255, 0.07) 50%, transparent 50.3%),
    linear-gradient(var(--brand-blue), var(--brand-blue));
}

.system-label,
.form-label {
  margin: 0;
  font-family: "Times New Roman", serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.22em;
}

.system-label {
  color: var(--brand-sun);
}

.intro-panel h1 {
  margin: 20px 0 13px;
  font-size: 42px;
  letter-spacing: 0.09em;
}

.intro-lead {
  margin: 0;
  color: var(--brand-cream);
  font-size: 15px;
  letter-spacing: 0.06em;
}

.intro-list {
  margin-top: 66px;
}

.intro-item {
  display: grid;
  margin-top: 29px;
  grid-template-columns: 42px 1fr;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.24);
}

.intro-item > span {
  color: var(--brand-sun);
  font-family: "Times New Roman", serif;
  font-weight: 600;
}

.intro-item strong {
  font-size: 16px;
  letter-spacing: 0.08em;
}

.intro-item p {
  margin: 7px 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
  line-height: 1.7;
}

.form-panel {
  display: grid;
  padding: 62px 60px;
  place-items: center;
  background: var(--brand-paper);
}

.form-content {
  width: 100%;
  max-width: 350px;
}

.form-label {
  color: var(--brand-cyan);
}

.form-content h2 {
  margin: 14px 0 8px;
  color: var(--brand-ink);
  font-size: 30px;
  letter-spacing: 0.08em;
}

.form-description {
  margin: 0 0 38px;
  color: var(--brand-muted);
  font-size: 13px;
}

.form-content :deep(.el-form-item) {
  margin-bottom: 23px;
}

.form-content :deep(.el-form-item__label) {
  color: var(--brand-ink);
  font-weight: 700;
}

.form-content :deep(.el-input__wrapper) {
  min-height: 44px;
}

.submit-item {
  margin-top: 34px;
}

.submit-item :deep(.el-button) {
  width: 100%;
  height: 46px;
}

.register-link {
  text-align: center;
  color: var(--brand-muted);
  font-size: 14px;
}

.register-link a {
  color: var(--brand-blue);
  font-weight: 700;
  text-decoration: none;
}

.register-link a:hover {
  color: var(--brand-cyan);
}
</style>
