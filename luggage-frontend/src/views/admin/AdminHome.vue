<template>
  <div class="admin-home">
    <h2>📊 管理仪表盘</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">{{ stats.totalOrders || 0 }}</div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" style="border-left: 4px solid #409EFF;">
          <div class="stat-number" style="color: #409EFF;">{{ stats.status_STORED || 0 }}</div>
          <div class="stat-label">寄存中</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" style="border-left: 4px solid #67C23A;">
          <div class="stat-number" style="color: #67C23A;">{{ stats.status_COMPLETED || 0 }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" style="border-left: 4px solid #E6A23C;">
          <div class="stat-number" style="color: #E6A23C;">¥{{ stats.totalRevenue || 0 }}</div>
          <div class="stat-label">总收入</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/orders')">
          <div class="quick-icon">📋</div>
          <div class="quick-title">订单管理</div>
          <div class="quick-desc">查看和管理所有订单</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/price-rules')">
          <div class="quick-icon">⚙️</div>
          <div class="quick-title">计费规则</div>
          <div class="quick-desc">配置寄存费用规则</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="quick-card" @click="$router.push('/admin/statistics')">
          <div class="quick-icon">📊</div>
          <div class="quick-title">统计数据</div>
          <div class="quick-desc">查看详细运营数据</div>
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
.stats-cards {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
  padding: 10px 0;
  cursor: default;
}
.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}
.quick-card {
  text-align: center;
  padding: 20px 0;
  cursor: pointer;
  transition: all 0.3s;
}
.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}
.quick-icon {
  font-size: 48px;
}
.quick-title {
  font-size: 18px;
  font-weight: bold;
  margin-top: 10px;
}
.quick-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
}
</style>