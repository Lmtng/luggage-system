<template>
  <div class="user-manage">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>查看用户账号，并停用或恢复普通用户</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadUsers">
        刷新
      </el-button>
    </div>

    <el-card>
      <el-table v-loading="loading" :data="users" empty-text="暂无用户">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="140">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'info'">
              {{ row.status === 'NORMAL' ? '正常' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'ADMIN'"
              size="small"
              :type="row.status === 'NORMAL' ? 'danger' : 'success'"
              :loading="changingId === row.id"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'NORMAL' ? '停用账号' : '恢复账号' }}
            </el-button>
            <span v-else class="muted">管理员账号</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/admin'

const users = ref([])
const loading = ref(false)
const changingId = ref(null)

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUsers()
    users.value = res.data || []
  } catch (error) {
    users.value = []
  } finally {
    loading.value = false
  }
}

const toggleStatus = async user => {
  const targetStatus = user.status === 'NORMAL' ? 'DISABLED' : 'NORMAL'
  const action = targetStatus === 'DISABLED' ? '停用' : '恢复'

  try {
    await ElMessageBox.confirm(
      `确定要${action}用户“${user.username}”吗？`,
      '账号状态确认',
      { type: 'warning' }
    )
  } catch (error) {
    return
  }

  changingId.value = user.id
  try {
    await adminApi.changeUserStatus(user.id, targetStatus)
    ElMessage.success(`账号已${action}`)
    await loadUsers()
  } catch (error) {
    // 错误提示由拦截器统一显示
  } finally {
    changingId.value = null
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0 0 8px; }
.page-header p, .muted { margin: 0; color: #909399; font-size: 14px; }
</style>
