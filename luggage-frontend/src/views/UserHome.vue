<template>
  <div class="user-home">
    <h2>🏠 欢迎使用行李寄存系统</h2>

    <el-row :gutter="20">
      <!-- 左侧：选择柜格 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>📦 选择柜格</span>
          </template>

          <el-form label-width="100px">
            <el-form-item label="柜格规格">
              <el-radio-group v-model="sizeType">
                <el-radio-button value="SMALL">小柜</el-radio-button>
                <el-radio-button value="MEDIUM">中柜</el-radio-button>
                <el-radio-button value="LARGE">大柜</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="queryAvailableCells" :loading="loading">
                查询空闲柜格
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 柜格列表 -->
          <div v-if="availableCells.length > 0" class="cell-grid">
            <div
                v-for="cell in availableCells"
                :key="cell.id"
                class="cell-item"
                :class="{ selected: selectedCellId === cell.id }"
                @click="selectCell(cell.id)"
            >
              <div class="cell-no">{{ cell.cellNo }}</div>
              <div class="cell-size">{{ cell.sizeType }}</div>
              <div class="cell-status available">空闲</div>
            </div>
          </div>
          <el-empty v-else description="暂无空闲柜格" />

          <div v-if="selectedCellId" class="selected-info">
            <el-tag type="success">已选择柜格：{{ getSelectedCellNo() }}</el-tag>
            <el-button type="primary" @click="createOrder" :loading="creating">
              确认寄存
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：快捷操作 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>⚡ 快捷操作</span>
          </template>

          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/orders')" block>
              查看我的订单
            </el-button>
            <el-button type="success" @click="$router.push('/pickup')" block>
              取件
            </el-button>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>📋 今日统计</span>
          </template>
          <div class="stat-item">
            <span>总订单数</span>
            <span class="stat-value">{{ statistics.totalOrders || 0 }}</span>
          </div>
          <div class="stat-item">
            <span>寄存中</span>
            <span class="stat-value">{{ statistics.status_STORED || 0 }}</span>
          </div>
          <div class="stat-item">
            <span>已完成</span>
            <span class="stat-value">{{ statistics.status_COMPLETED || 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建订单成功弹窗 -->
    <el-dialog v-model="showOrderDialog" title="✅ 寄存成功" width="400px">
      <div class="order-result">
        <p><strong>订单号：</strong>{{ newOrder?.orderNo }}</p>
        <p><strong>取件码：</strong><span class="pickup-code">{{ newOrder?.pickupCode }}</span></p>
        <p><strong>柜格：</strong>{{ newOrder?.cellNo }}</p>
        <p><strong>开始时间：</strong>{{ newOrder?.startTime }}</p>
        <el-alert type="warning" :closable="false">
          请妥善保管取件码！取件时需要输入。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="showOrderDialog = false">关闭</el-button>
        <el-button type="primary" @click="goToOrderDetail">查看订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '../api/order'

const router = useRouter()

const sizeType = ref('SMALL')
const loading = ref(false)
const creating = ref(false)
const availableCells = ref([])
const selectedCellId = ref(null)
const showOrderDialog = ref(false)
const newOrder = ref(null)

// 统计数据（简单模拟）
const statistics = reactive({
  totalOrders: 0,
  status_STORED: 0,
  status_COMPLETED: 0
})

// 查询空闲柜格
const queryAvailableCells = async () => {
  loading.value = true
  try {
    // TODO: 调用成员A的柜格查询接口
    // 目前模拟数据
    availableCells.value = [
      { id: 1, cellNo: 'A-01', sizeType: 'SMALL', status: 'AVAILABLE' },
      { id: 2, cellNo: 'A-02', sizeType: 'MEDIUM', status: 'AVAILABLE' },
      { id: 3, cellNo: 'A-03', sizeType: 'LARGE', status: 'AVAILABLE' }
    ].filter(c => c.sizeType === sizeType.value)

    if (availableCells.value.length === 0) {
      ElMessage.info('暂无空闲柜格')
    }
    selectedCellId.value = null
  } finally {
    loading.value = false
  }
}

// 选择柜格
const selectCell = (id) => {
  selectedCellId.value = id
}

// 获取选中柜格编号
const getSelectedCellNo = () => {
  const cell = availableCells.value.find(c => c.id === selectedCellId.value)
  return cell?.cellNo || ''
}

// 创建订单
const createOrder = async () => {
  if (!selectedCellId.value) {
    ElMessage.warning('请先选择一个柜格')
    return
  }

  creating.value = true
  try {
    const res = await orderApi.create({ cellId: selectedCellId.value })
    newOrder.value = res.data
    showOrderDialog.value = true

    // 刷新柜格列表
    await queryAvailableCells()
    ElMessage.success('寄存成功！')
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    creating.value = false
  }
}

// 查看订单详情
const goToOrderDetail = () => {
  showOrderDialog.value = false
  if (newOrder.value) {
    router.push(`/order/${newOrder.value.orderId}`)
  }
}

// 页面加载时查询柜格
queryAvailableCells()
</script>

<style scoped>
.user-home {
  padding: 20px;
}
.cell-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin: 15px 0;
}
.cell-item {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}
.cell-item:hover {
  border-color: #409EFF;
}
.cell-item.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.cell-no {
  font-size: 18px;
  font-weight: bold;
}
.cell-size {
  font-size: 12px;
  color: #909399;
}
.cell-status {
  font-size: 12px;
  margin-top: 4px;
}
.cell-status.available {
  color: #67c23a;
}
.selected-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 15px;
}
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.stat-value {
  font-weight: bold;
  color: #409EFF;
}
.pickup-code {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
  letter-spacing: 4px;
}
</style>