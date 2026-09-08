<template>
  <div class="pickup-page">
    <div class="page-header">
      <div>
        <h2>取件</h2>
        <p>选择寄存订单，验证取件码并完成模拟支付</p>
      </div>
      <el-button @click="$router.push('/orders')">返回订单列表</el-button>
    </div>

    <el-card v-loading="loadingOrders">
      <el-form label-width="100px">
        <el-form-item label="寄存订单">
          <el-select
            v-model="selectedOrderId"
            placeholder="请选择要取件的订单"
            style="width: 360px"
            :disabled="Boolean(verifyResult)"
          >
            <el-option
              v-for="order in activeOrders"
              :key="order.id"
              :label="`${order.orderNo}（柜格 ${order.cellId}）`"
              :value="order.id"
            />
          </el-select>
          <el-button
            text
            type="primary"
            :disabled="Boolean(verifyResult)"
            @click="loadActiveOrders"
          >
            刷新
          </el-button>
        </el-form-item>

        <el-form-item label="取件码">
          <el-input
            v-model="pickupCode"
            placeholder="请输入创建订单时获得的6位取件码"
            maxlength="6"
            style="width: 360px"
            :disabled="Boolean(verifyResult)"
            @keyup.enter="handleVerify"
          />
        </el-form-item>

        <el-form-item v-if="!verifyResult">
          <el-button
            type="primary"
            :loading="verifying"
            @click="handleVerify"
          >
            验证取件码
          </el-button>
        </el-form-item>
      </el-form>

      <div
        v-if="!loadingOrders && activeOrders.length === 0 && !verifyResult"
        class="empty-state"
      >
        暂无可以取件的订单
      </div>

      <div v-if="verifyResult" class="result">
        <el-divider />
        <el-alert
          title="取件码验证成功，请确认费用后完成模拟支付"
          type="success"
          :closable="false"
        />

        <div class="fee-heading">
          <div>
            <span>CHARGE DETAILS</span>
            <h3>费用详情</h3>
          </div>
          <p>费用由当前有效计费规则自动计算</p>
        </div>

        <div class="fee-table-wrap">
          <table class="fee-table">
            <thead>
              <tr>
                <th colspan="4">寄存订单计费明细</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <th>订单号</th>
                <td>{{ verifyResult.orderNo }}</td>
                <th>柜格编号</th>
                <td>{{ verifyResult.cellNo }}</td>
              </tr>
              <tr>
                <th>实际时长</th>
                <td>{{ verifyResult.actualMinutes }} 分钟</td>
                <th>免费时长</th>
                <td>{{ verifyResult.freeMinutes }} 分钟</td>
              </tr>
              <tr>
                <th>收费时长</th>
                <td>{{ verifyResult.chargeableMinutes }} 分钟</td>
                <th>计费单位</th>
                <td>{{ verifyResult.unitMinutes }} 分钟</td>
              </tr>
              <tr>
                <th>计费单价</th>
                <td>¥{{ formatAmount(verifyResult.unitPrice) }} / 单位</td>
                <th>封顶金额</th>
                <td>{{ verifyResult.capAmount == null ? '无' : `¥${formatAmount(verifyResult.capAmount)}` }}</td>
              </tr>
            </tbody>
            <tfoot>
              <tr>
                <th colspan="3">本次应付费用</th>
                <td class="amount">¥{{ formatAmount(verifyResult.amount) }}</td>
              </tr>
            </tfoot>
          </table>
        </div>

        <div class="actions">
          <el-button
            type="success"
            size="large"
            :loading="completing"
            @click="handleComplete"
          >
            确认支付并取件
          </el-button>
          <el-button @click="reset">返回修改</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '../api/order'

const route = useRoute()
const router = useRouter()

const pickupCode = ref('')
const selectedOrderId = ref(null)
const activeOrders = ref([])
const loadingOrders = ref(false)
const verifying = ref(false)
const completing = ref(false)
const verifyResult = ref(null)

const loadActiveOrders = async () => {
  loadingOrders.value = true

  try {
    const res = await orderApi.getMyOrders(1, 100)
    const records = res.data?.records || []

    activeOrders.value = records.filter(order => order.status === 'STORED')

    const routeOrderId = Number(route.query.orderId)

    if (
      Number.isInteger(routeOrderId) &&
      activeOrders.value.some(order => order.id === routeOrderId)
    ) {
      selectedOrderId.value = routeOrderId
    } else if (
      !activeOrders.value.some(order => order.id === selectedOrderId.value)
    ) {
      selectedOrderId.value = activeOrders.value[0]?.id ?? null
    }
  } catch (error) {
    activeOrders.value = []
    selectedOrderId.value = null
  } finally {
    loadingOrders.value = false
  }
}

const handleVerify = async () => {
  if (!selectedOrderId.value) {
    ElMessage.warning('请先选择要取件的订单')
    return
  }

  if (!/^\d{6}$/.test(pickupCode.value)) {
    ElMessage.warning('请输入6位数字取件码')
    return
  }

  verifying.value = true

  try {
    const res = await orderApi.verifyPickup(
      selectedOrderId.value,
      pickupCode.value
    )
    verifyResult.value = res.data
    ElMessage.success('取件码验证成功')
  } catch (error) {
    verifyResult.value = null
  } finally {
    verifying.value = false
  }
}

const handleComplete = async () => {
  const orderId = verifyResult.value?.orderId || selectedOrderId.value

  if (!orderId) {
    ElMessage.warning('订单信息不存在，请重新验证')
    return
  }

  completing.value = true

  try {
    await orderApi.complete(orderId)
    ElMessage.success('支付与取件已完成，柜格已经释放')
    router.push('/orders')
  } catch (error) {
    // 错误提示由响应拦截器统一显示
  } finally {
    completing.value = false
  }
}

const reset = () => {
  pickupCode.value = ''
  verifyResult.value = null
}

const formatAmount = value => {
  const number = Number(value ?? 0)
  return Number.isNaN(number) ? '0.00' : number.toFixed(2)
}

onMounted(() => {
  loadActiveOrders()
})
</script>

<style scoped>
.pickup-page {
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
}

.page-header p {
  margin: 0;
  color: var(--brand-muted);
  font-size: 14px;
}

.result {
  margin-top: 20px;
}

.result h3 {
  margin: 6px 0 0;
  color: var(--brand-ink);
  font-size: 24px;
}

.empty-state {
  margin-top: 22px;
  padding: 48px 20px;
  color: var(--brand-muted);
  text-align: center;
  border: 1px dashed var(--brand-line);
  background: #f4f9f8;
}

.fee-heading {
  display: flex;
  margin: 28px 0 14px;
  align-items: flex-end;
  justify-content: space-between;
}

.fee-heading span {
  color: var(--brand-cyan);
  font-family: "Times New Roman", serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
}

.fee-heading p {
  margin: 0 0 3px;
  color: var(--brand-muted);
  font-size: 12px;
}

.fee-table-wrap {
  border: 1px solid var(--brand-blue);
  box-shadow: 8px 8px 0 rgba(24, 77, 151, 0.08);
}

.fee-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.fee-table th,
.fee-table td {
  padding: 16px 18px;
  border-right: 1px solid var(--brand-line);
  border-bottom: 1px solid var(--brand-line);
  text-align: left;
}

.fee-table tr > *:last-child {
  border-right: 0;
}

.fee-table thead th {
  color: #ffffff;
  border-color: var(--brand-blue);
  background: var(--brand-blue);
  font-size: 15px;
  letter-spacing: 0.12em;
}

.fee-table tbody th {
  width: 18%;
  color: var(--brand-ink);
  background: #dfeff1;
  font-weight: 700;
}

.fee-table tbody td {
  width: 32%;
  color: var(--brand-text);
  background: var(--brand-paper);
}

.fee-table tbody tr:nth-child(even) td {
  background: #f5faf9;
}

.fee-table tfoot th,
.fee-table tfoot td {
  border-bottom: 0;
  background: var(--brand-sun);
}

.fee-table tfoot th {
  color: var(--brand-ink-deep);
  text-align: right;
  letter-spacing: 0.08em;
}

.fee-table .amount {
  color: var(--brand-blue);
  font-family: "Times New Roman", serif;
  font-size: 28px;
  font-weight: 700;
  white-space: nowrap;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
