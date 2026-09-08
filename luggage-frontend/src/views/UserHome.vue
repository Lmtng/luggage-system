<template>
  <div class="user-home">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="hero-eyebrow">TRAVEL LIGHT · STORE SMART</span>
        <h2>安心寄存，从容出行</h2>
        <p>安全、便捷的智能行李寄存服务，让每一次抵达都更轻松。</p>
        <div class="hero-actions">
          <el-button type="primary" @click="$router.push('/orders')">查看我的订单</el-button>
          <el-button class="hero-secondary" @click="$router.push('/pickup')">立即取件</el-button>
        </div>
      </div>
    </section>

    <el-row :gutter="22" class="main-grid">
      <!-- 左侧：选择柜格 -->
      <el-col :span="16">
        <el-card class="selection-card">
          <template #header>
            <span>选择柜格</span>
          </template>

          <el-form label-width="100px">
            <el-form-item label="柜格规格">
              <el-radio-group v-model="sizeType" @change="queryAvailableCells">
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
              <div class="cell-size">{{ sizeLabel(cell.sizeType) }}</div>
              <div class="cell-status available">空闲</div>
            </div>
          </div>
          <div v-else class="empty-state">暂无空闲柜格</div>

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
        <el-card class="quick-card">
          <template #header>
            <span>快捷操作</span>
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

        <el-card class="statistics-card">
          <template #header>
            <span>我的订单统计</span>
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
    <el-dialog v-model="showOrderDialog" title="寄存成功" width="400px">
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
import { onMounted, reactive, ref } from 'vue'
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

const statistics = reactive({
  totalOrders: 0,
  status_STORED: 0,
  status_COMPLETED: 0
})

// 查询空闲柜格
const queryAvailableCells = async () => {
  loading.value = true
  try {
    const res = await orderApi.getAvailableCells(sizeType.value)
    availableCells.value = res.data || []

    if (availableCells.value.length === 0) {
      ElMessage.info('暂无空闲柜格')
    }
    selectedCellId.value = null
  } catch (error) {
    availableCells.value = []
    selectedCellId.value = null
  } finally {
    loading.value = false
  }
}

// 根据个人订单计算首页统计，不再显示固定的0。
const loadStatistics = async () => {
  try {
    const res = await orderApi.getMyOrders(1, 100)
    const pageData = res.data || {}
    const records = pageData.records || []

    statistics.totalOrders = Number(pageData.total || records.length)
    statistics.status_STORED = records.filter(
      order => order.status === 'STORED'
    ).length
    statistics.status_COMPLETED = records.filter(
      order => order.status === 'COMPLETED'
    ).length
  } catch (error) {
    statistics.totalOrders = 0
    statistics.status_STORED = 0
    statistics.status_COMPLETED = 0
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
    await Promise.all([
      queryAvailableCells(),
      loadStatistics()
    ])
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

onMounted(() => {
  Promise.all([
    queryAvailableCells(),
    loadStatistics()
  ])
})

const sizeLabel = size => ({
  SMALL: '小型柜格',
  MEDIUM: '中型柜格',
  LARGE: '大型柜格'
}[size] || size)
</script>

<style scoped>
.user-home {
  padding: 0;
}

.hero-panel {
  position: relative;
  display: flex;
  min-height: 285px;
  margin-bottom: 24px;
  padding: 48px 54px;
  align-items: center;
  color: #f8f5ef;
  border: 1px solid var(--brand-blue);
  border-left: 10px solid var(--brand-aqua);
  border-radius: 0;
  background: linear-gradient(105deg, var(--brand-blue) 0%, var(--brand-blue) 70%, var(--brand-cyan) 100%);
  box-shadow: 10px 10px 0 rgba(24, 77, 151, 0.11);
}

.hero-panel::before {
  position: absolute;
  top: 0;
  right: 8%;
  width: 1px;
  height: 100%;
  content: "";
  background: rgba(255, 255, 255, 0.18);
}

.hero-copy {
  position: relative;
  z-index: 2;
  max-width: 650px;
}

.hero-eyebrow {
  color: var(--brand-sun);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.24em;
}

.hero-copy h2 {
  margin: 13px 0 14px;
  color: #fffaf3;
  font: 700 38px/1.15 "Times New Roman", "SimSun", "宋体", serif;
  letter-spacing: 0.03em;
}

.hero-copy p {
  max-width: 520px;
  margin: 0;
  color: rgba(255, 255, 255, 0.67);
  font-size: 15px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 26px;
}

.hero-secondary {
  color: #fffaf3 !important;
  border-color: rgba(255, 255, 255, 0.32) !important;
  background: rgba(255, 255, 255, 0.06) !important;
}

.main-grid {
  margin-top: 0;
}

.selection-card,
.quick-card,
.statistics-card {
  height: auto;
}

.statistics-card {
  margin-top: 20px;
}

.cell-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(105px, 1fr));
  gap: 12px;
  margin: 20px 0;
}

.cell-item {
  position: relative;
  padding: 18px 12px;
  text-align: center;
  cursor: pointer;
  border: 1px solid var(--brand-line);
  border-radius: 0;
  background: var(--brand-paper);
  transition: all 0.25s ease;
}

.cell-item:hover {
  border-color: var(--brand-cyan);
  box-shadow: 6px 6px 0 rgba(24, 136, 191, 0.1);
  transform: translateY(-2px);
}

.cell-item.selected {
  color: #fff;
  border-color: var(--brand-blue);
  background: var(--brand-blue);
  box-shadow: 6px 6px 0 rgba(24, 136, 191, 0.18);
}

.cell-no {
  font-size: 18px;
  font-weight: 700;
}

.cell-size {
  margin-top: 5px;
  font-size: 12px;
  color: #849099;
}

.cell-item.selected .cell-size {
  color: rgba(255, 255, 255, 0.58);
}

.cell-status {
  font-size: 12px;
  margin-top: 7px;
}

.cell-status.available {
  color: #238f94;
}

.cell-item.selected .cell-status.available {
  color: var(--brand-sun);
}

.selected-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
  padding: 15px;
  border: 1px solid var(--brand-line);
  border-radius: 0;
  background: #e7f3f2;
}

.empty-state {
  margin: 20px 0;
  padding: 45px 20px;
  color: var(--brand-muted);
  text-align: center;
  border: 1px dashed var(--brand-line);
  background: #f4f9f8;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-actions :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 13px 0;
  color: #677680;
  border-bottom: 1px solid #eeeae3;
}

.stat-item:last-child {
  border-bottom: 0;
}

.stat-value {
  color: var(--brand-blue);
  font: 700 20px/1 "Times New Roman", "SimSun", "宋体", serif;
}

.pickup-code {
  font-size: 24px;
  font-weight: bold;
  color: var(--brand-blue);
  letter-spacing: 4px;
}

@media (max-width: 1200px) {
  .hero-copy { max-width: 520px; }
}
</style>
