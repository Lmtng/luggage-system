<template>
  <el-container>
    <!-- 顶栏 -->
    <el-header>
      <div class="header-left">
        <h2>🧳 行李寄存系统</h2>
      </div>
      <div class="header-right">
        <span class="user-name">欢迎，{{ userStore.nickname || userStore.username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <el-menu
            :default-active="$route.path"
            router
            class="menu"
        >
          <!-- 普通用户菜单 -->
          <template v-if="!userStore.isAdmin">
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/orders">
              <el-icon><List /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="/pickup">
              <el-icon><Check /></el-icon>
              <span>取件</span>
            </el-menu-item>
          </template>

          <!-- 管理员菜单 -->
          <template v-else>
            <el-menu-item index="/admin">
              <el-icon><House /></el-icon>
              <span>管理首页</span>
            </el-menu-item>
            <el-menu-item index="/admin/orders">
              <el-icon><List /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/price-rules">
              <el-icon><Setting /></el-icon>
              <span>计费规则</span>
            </el-menu-item>
            <el-menu-item index="/admin/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>统计数据</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.el-header {
  background: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.el-header h2 {
  margin: 0;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
.user-name {
  color: white;
}
.el-aside {
  background: #f5f7fa;
  height: calc(100vh - 60px);
}
.menu {
  height: 100%;
  border-right: none;
}
.el-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>