<template>
  <div class="order-detail">
    <el-card>
      <template #header>
        <div class="header">
          <span>📄 订单详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <div v-if="order" class="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(order.status)">
              {{ getStatusLabel(order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="柜格ID">{{ order.cellId }}</el-descriptions-item>
          <el-descriptions-item label="费用">¥{{ order.amount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(order.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ order.endTime ? formatTime(order.endTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="order.paymentStatus === 'PAID' ? 'success' : 'warning'">
              {{ order.paymentStatus === 'PAID' ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(order.createdAt) }}</el-descriptions-item>
        </el-descriptions>

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

      <el-empty v-else description="订单不存在" />
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
  router.push('/pickup')
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
.actions {
  margin-top: 20px;
  text-align: center;
}
</style>