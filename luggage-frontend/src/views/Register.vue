<template>
  <div class="register-container">
    <div class="register-shell">
      <section class="intro-panel">
        <p class="system-label">SMART STORAGE SYSTEM</p>
        <h1>创建寄存账户</h1>
        <p class="intro-lead">建立个人账户，开始使用清晰可靠的行李寄存服务。</p>

        <div class="intro-list">
          <div class="intro-item">
            <span>01</span>
            <div>
              <strong>管理个人订单</strong>
              <p>集中查看寄存记录、订单状态与费用信息。</p>
            </div>
          </div>
          <div class="intro-item">
            <span>02</span>
            <div>
              <strong>选择适合柜格</strong>
              <p>根据行李尺寸查询并选择当前空闲柜格。</p>
            </div>
          </div>
          <div class="intro-item">
            <span>03</span>
            <div>
              <strong>完成自助取件</strong>
              <p>核对费用后完成模拟支付并释放柜格。</p>
            </div>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-content">
          <p class="form-label">CREATE ACCOUNT</p>
          <h2>用户注册</h2>
          <p class="form-description">请填写以下信息完成账户创建</p>

          <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
          >
            <div class="form-row">
              <el-form-item label="用户名" prop="username">
                <el-input
                    v-model="form.username"
                    placeholder="3-30位字母、数字或下划线"
                />
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input
                    v-model="form.nickname"
                    placeholder="可选"
                />
              </el-form-item>
            </div>

            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入6-20位密码"
                  show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  show-password
                  @keyup.enter="handleRegister"
              />
            </el-form-item>

            <el-form-item class="submit-item">
              <el-button type="primary" @click="handleRegister" :loading="loading">
                注册
              </el-button>
            </el-form-item>

            <div class="login-link">
              已有账号？<router-link to="/login">立即登录</router-link>
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
import { authApi } from '../api/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,30}$/, message: '3-30位字母、数字或下划线', trigger: 'blur' }
  ],
  nickname: [
    { max: 30, message: '昵称不能超过30个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      nickname: form.nickname || form.username,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: grid;
  height: 100vh;
  min-height: 720px;
  padding: 44px;
  place-items: center;
  background: var(--brand-cream);
}

.register-shell {
  display: grid;
  width: min(1120px, calc(100vw - 88px));
  min-height: 640px;
  grid-template-columns: 0.92fr 1.08fr;
  border: 1px solid var(--brand-blue);
  background: var(--brand-paper);
  box-shadow: 18px 18px 0 rgba(24, 77, 151, 0.12);
}

.intro-panel {
  padding: 58px 56px;
  color: #ffffff;
  background: var(--brand-blue);
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
  font-size: 38px;
  letter-spacing: 0.08em;
}

.intro-lead {
  margin: 0;
  color: var(--brand-cream);
  font-size: 14px;
  line-height: 1.8;
}

.intro-list {
  margin-top: 56px;
}

.intro-item {
  display: grid;
  margin-top: 27px;
  grid-template-columns: 40px 1fr;
  gap: 14px;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.24);
}

.intro-item > span {
  color: var(--brand-sun);
  font-family: "Times New Roman", serif;
  font-weight: 700;
}

.intro-item strong {
  font-size: 16px;
  letter-spacing: 0.06em;
}

.intro-item p {
  margin: 7px 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
  line-height: 1.7;
}

.form-panel {
  display: grid;
  padding: 42px 58px;
  place-items: center;
  background: var(--brand-paper);
}

.form-content {
  width: 100%;
  max-width: 480px;
}

.form-label {
  color: var(--brand-cyan);
}

.form-content h2 {
  margin: 13px 0 7px;
  color: var(--brand-ink);
  font-size: 28px;
  letter-spacing: 0.08em;
}

.form-description {
  margin: 0 0 27px;
  color: var(--brand-muted);
  font-size: 13px;
}

.form-row {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 15px;
}

.form-content :deep(.el-form-item) {
  margin-bottom: 19px;
}

.form-content :deep(.el-form-item__label) {
  color: var(--brand-ink);
  font-weight: 700;
}

.form-content :deep(.el-input__wrapper) {
  min-height: 43px;
}

.submit-item {
  margin-top: 25px;
}

.submit-item :deep(.el-button) {
  width: 100%;
  height: 46px;
}

.login-link {
  text-align: center;
  color: var(--brand-muted);
  font-size: 14px;
}

.login-link a {
  color: var(--brand-blue);
  font-weight: 700;
  text-decoration: none;
}

.login-link a:hover {
  color: var(--brand-cyan);
}
</style>
