<template>
  <div class="order-manage">
    <h2>📋 订单管理</h2>

    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="loadOrders">
          <el-option label="全部状态" value="" />
          <el-option label="寄存中" value="STORED" />
          <el-option label="待支付" value="PENDING_PAYMENT" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
          <el-option label="异常" value="EXCEPTION" />
        </el-select>
        <el-button type="primary" @click="loadOrders">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </div>

      <!-- 订单表格 -->
      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="cellId" label="柜格ID" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="费用" width="100">
          <template #default="{ row }">
            ¥{{ row.amount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row.id)">查看</el-button>
            <el-button
                v-if="row.status === 'EXCEPTION'"
                size="small"
                type="warning"
                @click="showFixDialog(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:page-size="pageSize"
            v-model:current-page="pageNum"
            :total="total"
            :page-sizes="[10, 20, 50]"
            @size-change="loadOrders"
            @current-change="loadOrders"
            layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>

    <!-- 处理异常订单弹窗 -->
    <el-dialog v-model="fixDialogVisible" title="处理异常订单" width="400px">
      <el-form>
        <el-form-item label="订单号">
          <span>{{ fixOrder?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag type="danger">异常</el-tag>
        </el-form-item>
        <el-form-item label="目标状态">
          <el-radio-group v-model="fixTargetStatus">
            <el-radio value="STORED">恢复寄存中</el-radio>
            <el-radio value="COMPLETED">标记已完成</el-radio>
            <el-radio value="CANCELLED">取消订单</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fixDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFixOrder" :loading="fixing">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/admin'

const router = useRouter()

const loading = ref(false)
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')

// 处理异常订单
const fixDialogVisible = ref(false)
const fixOrder = ref(null)
const fixTargetStatus = ref('COMPLETED')
const fixing = ref(false)

// 状态映射
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

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const res = await adminApi.getOrders(pageNum.value, pageSize.value, filterStatus.value)
    orders.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterStatus.value = ''
  pageNum.value = 1
  loadOrders()
}

// 查看订单详情
const viewDetail = (id) => {
  router.push(`/order/${id}`)
}

// 显示处理异常弹窗
const showFixDialog = (row) => {
  fixOrder.value = row
  fixTargetStatus.value = 'COMPLETED'
  fixDialogVisible.value = true
}

// 处理异常订单
const handleFixOrder = async () => {
  if (!fixTargetStatus.value) {
    ElMessage.warning('请选择目标状态')
    return
  }

  fixing.value = true
  try {
    await adminApi.fixOrder(fixOrder.value.id, fixTargetStatus.value)
    ElMessage.success('异常订单处理成功')
    fixDialogVisible.value = false
    loadOrders()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    fixing.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-manage {
  padding: 0;
}
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>