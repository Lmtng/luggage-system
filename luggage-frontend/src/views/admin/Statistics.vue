<template>
  <div class="statistics">
    <h2>📊 运营数据统计</h2>

    <el-row :gutter="20">
      <!-- 概览卡片 -->
      <el-col :span="6" v-for="(item, key) in overviewCards" :key="key">
        <el-card class="overview-card">
          <div class="overview-icon" :style="{ background: item.color }">
            {{ item.icon }}
          </div>
          <div class="overview-info">
            <div class="overview-number">{{ stats[item.key] || 0 }}</div>
            <div class="overview-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单状态分布 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>📊 订单状态分布</span>
          </template>
          <div class="status-distribution">
            <div
                v-for="(item, key) in statusList"
                :key="key"
                class="status-item"
            >
              <div class="status-label">
                <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
                <span>{{ stats['status_' + key] || 0 }}</span>
              </div>
              <el-progress
                  :percentage="getPercentage(stats['status_' + key] || 0)"
                  :color="item.color"
                  :stroke-width="12"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 收入统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>💰 收入统计</span>
          </template>
          <div class="revenue-stats">
            <div class="revenue-total">
              <span class="revenue-label">总收入</span>
              <span class="revenue-amount">¥{{ stats.totalRevenue || 0 }}</span>
            </div>
            <el-divider />
            <div class="revenue-detail">
              <div class="revenue-item">
                <span>已完成订单</span>
                <span>{{ stats.status_COMPLETED || 0 }} 单</span>
              </div>
              <div class="revenue-item">
                <span>平均单价</span>
                <span>¥{{ getAveragePrice() }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 时间信息 -->
    <el-card style="margin-top: 20px;">
      <div class="update-time">
        <el-icon><Clock /></el-icon>
        <span>数据更新时间：{{ currentTime }}</span>
        <el-button size="small" @click="loadStatistics">刷新数据</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
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

const currentTime = ref('')

// 概览卡片配置
const overviewCards = [
  { key: 'totalOrders', label: '总订单数', icon: '📋', color: '#409EFF' },
  { key: 'status_STORED', label: '寄存中', icon: '📦', color: '#67C23A' },
  { key: 'status_COMPLETED', label: '已完成', icon: '✅', color: '#409EFF' },
  { key: 'totalRevenue', label: '总收入', icon: '💰', color: '#E6A23C' }
]

// 状态列表
const statusList = {
  STORED: { label: '寄存中', type: 'primary', color: '#409EFF' },
  PENDING_PAYMENT: { label: '待支付', type: 'warning', color: '#E6A23C' },
  COMPLETED: { label: '已完成', type: 'success', color: '#67C23A' },
  CANCELLED: { label: '已取消', type: 'info', color: '#909399' },
  EXCEPTION: { label: '异常', type: 'danger', color: '#F56C6C' }
}

// 计算百分比
const getPercentage = (value) => {
  const total = stats.totalOrders || 1
  return Math.round((value / total) * 100)
}

// 计算平均单价
const getAveragePrice = () => {
  const completed = stats.status_COMPLETED || 0
  if (completed === 0) return '0.00'
  return (stats.totalRevenue / completed).toFixed(2)
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await adminApi.getStatistics()
    Object.assign(stats, res.data)
    currentTime.value = new Date().toLocaleString('zh-CN')
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics {
  padding: 0;
}
.overview-card {
  display: flex;
  align-items: center;
  padding: 15px;
}
.overview-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin-right: 15px;
  flex-shrink: 0;
}
.overview-number {
  font-size: 28px;
  font-weight: bold;
}
.overview-label {
  font-size: 14px;
  color: #909399;
}
.status-distribution {
  padding: 10px 0;
}
.status-item {
  margin-bottom: 15px;
}
.status-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 14px;
}
.revenue-total {
  text-align: center;
  padding: 20px 0;
}
.revenue-label {
  font-size: 16px;
  color: #909399;
}
.revenue-amount {
  font-size: 42px;
  font-weight: bold;
  color: #E6A23C;
  display: block;
  margin-top: 10px;
}
.revenue-detail {
  padding: 10px 0;
}
.revenue-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}
.revenue-item span:last-child {
  font-weight: bold;
}
.update-time {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #909399;
  font-size: 14px;
}
</style>