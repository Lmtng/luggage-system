<template>
  <div class="profile-page">
    <div class="page-header">
      <div>
        <h2>个人信息</h2>
        <p>查看当前登录账号的基本信息与权限状态</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadProfile">刷新</el-button>
    </div>

    <el-card v-loading="loading" class="profile-card">
      <el-descriptions v-if="profile" :column="1" border>
        <el-descriptions-item label="用户ID">{{ profile.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ profile.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'primary'">
            {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="profile.status === 'NORMAL' ? 'success' : 'info'">
            {{ profile.status === 'NORMAL' ? '正常' : '已停用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { authApi } from '../api/auth'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const profile = ref(null)
const loading = ref(false)

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await authApi.getMe()
    profile.value = res.data
    userStore.setUser(res.data)
  } catch (error) {
    profile.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0 0 8px; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.profile-card { max-width: 720px; }
</style>
