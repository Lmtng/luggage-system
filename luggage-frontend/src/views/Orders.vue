<template>
  <div class="orders-page">
    <div class="page-header">
      <div>
        <h2>我的订单</h2>
        <p>查看个人寄存订单及当前状态</p>
      </div>

      <el-button
        type="primary"
        :loading="loading"
        @click="loadOrders"
      >
        刷新
      </el-button>
    </div>

    <el-card>
      <el-table
        v-loading="loading"
        :data="orders"
        style="width: 100%"
        empty-text="暂无订单"
      >
        <el-table-column
          prop="orderNo"
          label="订单号"
          min-width="175"
        />

        <el-table-column
          prop="cellId"
          label="柜格ID"
          width="90"
          align="center"
        />

        <el-table-column
          label="订单状态"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="支付状态"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="
                row.paymentStatus === 'PAID'
                  ? 'success'
                  : 'warning'
              "
            >
              {{
                row.paymentStatus === 'PAID'
                  ? '已支付'
                  : '未支付'
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="费用"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>

        <el-table-column
          label="寄存时间"
          min-width="175"
        >
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>

        <el-table-column
          label="完成时间"
          min-width="175"
        >
          <template #default="{ row }">
            {{
              row.endTime
                ? formatTime(row.endTime)
                : '尚未完成'
            }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="180"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              plain
              @click="viewDetail(row.id)"
            >
              查看
            </el-button>

            <el-button
              v-if="row.status === 'STORED'"
              size="small"
              type="success"
              @click="goToPickup(row.id)"
            >
              去取件
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadOrders"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '../api/order'

const router = useRouter()

const loading = ref(false)
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusLabel = (status) => {
  const statusMap = {
    STORED: '寄存中',
    PENDING_PAYMENT: '待支付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    EXCEPTION: '异常'
  }

  return statusMap[status] || status || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    STORED: 'primary',
    PENDING_PAYMENT: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    EXCEPTION: 'danger'
  }

  return typeMap[status] || 'info'
}

const formatTime = (time) => {
  if (!time) {
    return '-'
  }

  const date = new Date(time)

  if (Number.isNaN(date.getTime())) {
    return time
  }

  return date.toLocaleString('zh-CN')
}

const formatAmount = (amount) => {
  const value = Number(amount ?? 0)

  if (Number.isNaN(value)) {
    return '0.00'
  }

  return value.toFixed(2)
}

const loadOrders = async () => {
  loading.value = true

  try {
    const response = await orderApi.getMyOrders(
      pageNum.value,
      pageSize.value
    )

    const pageData = response.data || {}

    orders.value = pageData.records || []
    total.value = Number(pageData.total || 0)
  } catch (error) {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  pageNum.value = 1
  loadOrders()
}

const viewDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

const goToPickup = (orderId) => {
  router.push({
    path: '/pickup',
    query: {
      orderId
    }
  })
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .page-header {
    align-items: flex-start;
  }

  .pagination {
    justify-content: center;
    overflow-x: auto;
  }
}
</style>
