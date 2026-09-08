<template>
  <div class="admin-home">
    <div class="page-intro">
      <div>
        <span class="eyebrow">OPERATION OVERVIEW</span>
        <h2>管理仪表盘</h2>
      </div>
      <p>掌握寄存服务运行状态与核心业务数据</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total">
          <div class="stat-number">{{ stats.totalOrders || 0 }}</div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stored">
          <div class="stat-number">{{ stats.status_STORED || 0 }}</div>
          <div class="stat-label">寄存中</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card completed">
          <div class="stat-number">{{ stats.status_COMPLETED || 0 }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card revenue">
          <div class="stat-number">¥{{ stats.totalRevenue || 0 }}</div>
          <div class="stat-label">总收入</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/orders')">
          <div class="quick-code">ORDER MANAGEMENT</div>
          <div class="quick-title">订单管理</div>
          <div class="quick-desc">查看和管理所有订单</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/price-rules')">
          <div class="quick-code">PRICING RULES</div>
          <div class="quick-title">计费规则</div>
          <div class="quick-desc">配置寄存费用规则</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/statistics')">
          <div class="quick-code">DATA REPORT</div>
          <div class="quick-title">统计数据</div>
          <div class="quick-desc">查看详细运营数据</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/users')">
          <div class="quick-code">USER MANAGEMENT</div>
          <div class="quick-title">用户管理</div>
          <div class="quick-desc">查看、停用与恢复用户账号</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/lockers')">
          <div class="quick-code">LOCKER MANAGEMENT</div>
          <div class="quick-title">柜体与柜格</div>
          <div class="quick-desc">维护寄存柜及柜格状态</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/operation-logs')">
          <div class="quick-code">OPERATION LOG</div>
          <div class="quick-title">操作日志</div>
          <div class="quick-desc">追踪管理员重要操作</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../../api/admin'

const stats = reactive({
  totalOrders: 0,
  status_STORED: 0,
  status_PENDING_PAYMENT: 0,
  status_COMPLETED: 0,
  status_CANCELLED: 0,
  status_EXCEPTION: 0,
  totalRevenue: 0
})

const loadStatistics = async () => {
  try {
    const res = await adminApi.getStatistics()
    Object.assign(stats, res.data)
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.admin-home {
  padding: 0;
}

.page-intro {
  display: flex;
  margin-bottom: 24px;
  align-items: flex-end;
  justify-content: space-between;
}

.page-intro .eyebrow {
  color: var(--brand-gold);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
}

.page-intro h2 {
  margin: 7px 0 0;
  color: var(--brand-ink);
  font: 700 28px/1.2 "Times New Roman", "SimSun", "宋体", serif;
}

.page-intro p {
  margin: 0 0 3px;
  color: #7b858a;
  font-size: 13px;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  text-align: center;
  padding: 14px 0 12px;
  overflow: hidden;
  cursor: default;
}

.stat-card::before {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 4px;
  content: '';
  background: #405563;
}

.stat-card.stored::before {
  background: #607c88;
}

.stat-card.completed::before {
  background: #748e82;
}

.stat-card.revenue::before {
  background: #b9824a;
}

.stat-number {
  color: var(--brand-ink);
  font: 700 32px/1.1 "Times New Roman", "SimSun", "宋体", serif;
}

.stat-card.stored .stat-number {
  color: #496776;
}

.stat-card.completed .stat-number {
  color: #617b70;
}

.stat-card.revenue .stat-number {
  color: #9a6738;
}

.stat-label {
  font-size: 14px;
  color: #818b90;
  margin-top: 9px;
}

.quick-card {
  text-align: center;
  padding: 24px 0;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.quick-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 35px rgba(23, 38, 51, 0.1);
}

.quick-code {
  color: var(--brand-cyan);
  font-family: "Times New Roman", serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.quick-title {
  color: var(--brand-ink);
  font-size: 18px;
  font-weight: 600;
  margin-top: 16px;
}

.quick-desc {
  font-size: 13px;
  color: #818b90;
  margin-top: 7px;
}
</style>
