<template>
  <div class="order-detail">
    <el-card>
      <template #header>
        <div class="header">
          <div>
            <span class="header-label">ORDER DETAILS</span>
            <h2>订单详情</h2>
          </div>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <div v-if="order" class="detail">
        <table class="detail-table">
          <thead>
            <tr>
              <th colspan="4">寄存订单信息</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <th>订单号</th>
              <td>{{ order.orderNo }}</td>
              <th>订单状态</th>
              <td>
                <el-tag :type="getStatusType(order.status)">
                  {{ getStatusLabel(order.status) }}
                </el-tag>
              </td>
            </tr>
            <tr>
              <th>柜格编号</th>
              <td>{{ order.cellId }}</td>
              <th>支付状态</th>
              <td>
                <el-tag :type="order.paymentStatus === 'PAID' ? 'success' : 'warning'">
                  {{ order.paymentStatus === 'PAID' ? '已支付' : '未支付' }}
                </el-tag>
              </td>
            </tr>
            <tr>
              <th>开始时间</th>
              <td>{{ formatTime(order.startTime) }}</td>
              <th>结束时间</th>
              <td>{{ order.endTime ? formatTime(order.endTime) : '-' }}</td>
            </tr>
            <tr>
              <th>创建时间</th>
              <td>{{ formatTime(order.createdAt) }}</td>
              <th>订单费用</th>
              <td class="detail-amount">¥{{ order.amount || 0 }}</td>
            </tr>
          </tbody>
        </table>

        <!-- 取件按钮（仅当订单状态为 STORED 时显示） -->
        <div v-if="order.status === 'STORED'" class="actions">
          <el-button type="success" @click="goToPickup">去取件</el-button>
        </div>

        <!-- 已完成信息 -->
        <div v-if="order.status === 'COMPLETED'" class="actions">
          <el-alert type="success" :closable="false">
            取件已完成！感谢使用行李寄存服务。
          </el-alert>
        </div>
      </div>

      <div v-else class="empty-state">订单不存在</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '../api/order'

const route = useRoute()
const router = useRouter()
const order = ref(null)

const getStatusLabel = (status) => {
  const map = {
    STORED: '寄存中',
    PENDING_PAYMENT: '待支付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    EXCEPTION: '异常'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    STORED: 'primary',
    PENDING_PAYMENT: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    EXCEPTION: 'danger'
  }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const goToPickup = () => {
  router.push({
    path: '/pickup',
    query: { orderId: order.value.id }
  })
}

const loadDetail = async () => {
  try {
    const id = route.params.id
    const res = await orderApi.getDetail(id)
    order.value = res.data
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.order-detail {
  padding: 0;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-label {
  color: var(--brand-cyan);
  font-family: "Times New Roman", serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
}

.header h2 {
  margin: 5px 0 0;
  color: var(--brand-ink);
  font-size: 24px;
}

.detail-table {
  width: 100%;
  border: 1px solid var(--brand-blue);
  border-collapse: collapse;
  table-layout: fixed;
  box-shadow: 8px 8px 0 rgba(24, 77, 151, 0.08);
}

.detail-table th,
.detail-table td {
  padding: 16px 18px;
  border-right: 1px solid var(--brand-line);
  border-bottom: 1px solid var(--brand-line);
  text-align: left;
}

.detail-table tr > *:last-child {
  border-right: 0;
}

.detail-table tbody tr:last-child > * {
  border-bottom: 0;
}

.detail-table thead th {
  color: #fff;
  border-color: var(--brand-blue);
  background: var(--brand-blue);
  letter-spacing: 0.12em;
}

.detail-table tbody th {
  width: 18%;
  color: var(--brand-ink);
  background: #dfeff1;
}

.detail-table tbody td {
  width: 32%;
  background: var(--brand-paper);
}

.detail-table tbody tr:nth-child(even) td {
  background: #f5faf9;
}

.detail-amount {
  color: var(--brand-blue);
  font-family: "Times New Roman", serif;
  font-size: 24px;
  font-weight: 700;
}

.empty-state {
  padding: 58px 20px;
  color: var(--brand-muted);
  text-align: center;
  border: 1px dashed var(--brand-line);
  background: #f4f9f8;
}

.actions {
  margin-top: 20px;
  text-align: center;
}
</style>
