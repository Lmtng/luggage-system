<template>
  <el-container class="app-shell">
    <el-aside width="240px" class="side-panel">
      <div class="brand">
        <div>
          <div class="brand-name">LUGGAGE</div>
          <div class="brand-subtitle">行李寄存管理系统</div>
        </div>
      </div>

      <div class="account-card">
        <div class="account-copy">
          <strong>{{ userStore.nickname || userStore.username }}</strong>
          <span>{{ userStore.isAdmin ? '系统管理员' : '寄存用户' }}</span>
        </div>
      </div>

      <div class="menu-caption">功能导航</div>
      <el-menu
        :default-active="$route.path"
        router
        class="side-menu"
      >
        <template v-if="!userStore.isAdmin">
          <el-menu-item index="/">
            <span>首页概览</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="/pickup">
            <span>自助取件</span>
          </el-menu-item>
        </template>

        <template v-else>
          <el-menu-item index="/admin">
            <span>管理概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/lockers">
            <span>柜体与柜格</span>
          </el-menu-item>
          <el-menu-item index="/admin/price-rules">
            <span>计费规则</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics">
            <span>运营统计</span>
          </el-menu-item>
          <el-menu-item index="/admin/operation-logs">
            <span>操作日志</span>
          </el-menu-item>
        </template>

        <el-menu-item index="/profile">
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>

      <div class="side-footer">
        <div>
          <strong>当前会话安全</strong>
          <span>数据由本地服务保护</span>
        </div>
      </div>
    </el-aside>

    <el-container class="workspace">
      <el-header class="topbar">
        <div class="topbar-copy">
          <span class="eyebrow">LUGGAGE MANAGEMENT</span>
          <h1>{{ currentPageTitle }}</h1>
        </div>

        <div class="topbar-actions">
          <div class="service-status">
            服务状态：运行中
          </div>
          <el-button class="logout-button" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="view-frame">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import { authApi } from '../api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentPageTitle = computed(() => {
  const titles = {
    UserHome: '首页概览',
    Orders: '我的订单',
    OrderDetail: '订单详情',
    Pickup: '自助取件',
    Profile: '个人信息',
    AdminHome: '管理概览',
    OrderManage: '订单管理',
    UserManage: '用户管理',
    LockerManage: '柜体与柜格',
    PriceRule: '计费规则',
    Statistics: '运营统计',
    OperationLogs: '操作日志'
  }

  return titles[route.name] || '行李寄存系统'
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前账号吗？', '退出登录', {
    confirmButtonText: '确认退出',
    cancelButtonText: '继续使用',
    type: 'warning'
  }).then(async () => {
    try {
      await authApi.logout()
    } catch (error) {
      // 即使后端暂时不可用，也清除浏览器中的登录状态。
    }

    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--brand-mist);
}

.side-panel {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  width: 240px !important;
  padding: 30px 20px 22px;
  color: #ffffff;
  border-right: 6px solid var(--brand-cyan);
  background: var(--brand-blue);
  box-shadow: none;
}

.brand {
  padding: 0 10px 27px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.25);
}

.brand-name {
  font: 700 21px/1.1 "Times New Roman", serif;
  letter-spacing: 0.2em;
}

.brand-subtitle {
  margin-top: 8px;
  color: var(--brand-cream);
  font-size: 12px;
  letter-spacing: 0.14em;
}

.account-card {
  display: flex;
  align-items: center;
  margin: 20px 0 30px;
  padding: 15px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.07);
}

.account-copy {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.account-copy strong {
  overflow: hidden;
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-copy span {
  margin-top: 4px;
  color: var(--brand-mint);
  font-size: 11px;
}

.menu-caption {
  padding: 0 14px 9px;
  color: var(--brand-sun);
  font-size: 10px;
  letter-spacing: 0.18em;
}

.side-menu {
  flex: 1;
  border-right: 0 !important;
  background: transparent !important;
}

.side-menu :deep(.el-menu-item) {
  height: 48px;
  margin: 2px 0;
  padding-left: 15px !important;
  color: rgba(255, 255, 255, 0.78);
  border-radius: 0;
  font-size: 14px;
}

.side-menu :deep(.el-menu-item:hover) {
  color: white;
  background: rgba(52, 184, 197, 0.22) !important;
}

.side-menu :deep(.el-menu-item.is-active) {
  color: var(--brand-ink-deep);
  background: var(--brand-cream) !important;
  box-shadow: inset 5px 0 0 var(--brand-aqua);
}

.side-footer {
  padding: 15px 10px 2px;
  border-top: 1px solid rgba(255, 255, 255, 0.24);
}

.side-footer div:last-child {
  display: flex;
  flex-direction: column;
}

.side-footer strong {
  font-size: 11px;
  font-weight: 600;
}

.side-footer span {
  margin-top: 3px;
  color: var(--brand-mint);
  font-size: 9px;
}

.workspace {
  flex: 0 0 calc(100% - 240px);
  width: calc(100% - 240px);
  min-height: 100vh;
  margin-left: 240px;
}

.topbar {
  display: flex;
  height: 88px;
  padding: 0 34px;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--brand-line);
  background: var(--brand-cream);
}

.topbar-copy .eyebrow {
  color: var(--brand-blue);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.2em;
}

.topbar-copy h1 {
  margin: 5px 0 0;
  color: var(--brand-ink);
  font: 700 24px/1.1 "Times New Roman", "SimSun", "宋体", serif;
  letter-spacing: 0.01em;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.service-status {
  color: var(--brand-slate);
  font-size: 12px;
}

.logout-button {
  color: var(--brand-blue);
  border-color: var(--brand-blue);
  background: transparent;
}

.main-content {
  min-height: calc(100vh - 88px);
  padding: 30px 34px 42px;
  background: var(--brand-mist);
}

.view-frame {
  width: 100%;
  max-width: 1500px;
  margin: 0 auto;
}

@media (max-width: 1200px) {
  .side-panel {
    width: 220px !important;
  }

  .workspace {
    flex-basis: calc(100% - 220px);
    width: calc(100% - 220px);
    margin-left: 220px;
  }

  .main-content,
  .topbar {
    padding-right: 24px;
    padding-left: 24px;
  }
}
</style>
