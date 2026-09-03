<template>
  <div class="pickup-page">
    <h2>🔑 取件</h2>

    <el-card>
      <el-form label-width="100px">
        <el-form-item label="取件码">
          <el-input
              v-model="pickupCode"
              placeholder="请输入6位取件码"
              maxlength="6"
              style="width: 300px"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleVerify" :loading="verifying">
            验证取件码
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 验证结果 -->
      <div v-if="verifyResult" class="result">
        <el-divider />
        <h3>📊 费用详情</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ verifyResult.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="柜格">{{ verifyResult.cellNo }}</el-descriptions-item>
          <el-descriptions-item label="寄存时长">{{ verifyResult.actualMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="免费时长">{{ verifyResult.freeMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="收费时长">{{ verifyResult.chargeableMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="计费单位">{{ verifyResult.unitMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ verifyResult.unitPrice }} / 单位</el-descriptions-item>
          <el-descriptions-item label="封顶">¥{{ verifyResult.capAmount || '无' }}</el-descriptions-item>
          <el-descriptions-item label="费用" :span="2">
            <span class="amount">¥{{ verifyResult.amount }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="actions">
          <el-button type="success" @click="handleComplete" :loading="completing" size="large">
            确认支付并取件
          </el-button>
          <el-button @click="reset">重新输入</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '../api/order'

const pickupCode = ref('')
const verifying = ref(false)
const completing = ref(false)
const verifyResult = ref(null)

// 验证取件码
const handleVerify = async () => {
  if (!pickupCode.value || pickupCode.value.length !== 6) {
    ElMessage.warning('请输入6位取件码')
    return
  }

  verifying.value = true
  try {
    // TODO: 需要用户选择或输入订单ID，目前简化处理
    // 实际应该先查询用户订单列表，然后选择
    // 这里简化：让用户输入取件码的同时输入订单号，或直接通过取件码查询
    ElMessage.info('请先在我的订单中点击"去取件"，或输入订单号')
    // 暂时模拟
    verifyResult.value = {
      orderNo: 'ST202609010001',
      cellNo: 'A-01',
      actualMinutes: 120,
      freeMinutes: 30,
      chargeableMinutes: 90,
      unitMinutes: 60,
      unitPrice: 2,
      capAmount: 20,
      amount: 4
    }
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    verifying.value = false
  }
}

// 完成取件
const handleComplete = async () => {
  completing.value = true
  try {
    await orderApi.complete(1) // TODO: 使用实际订单ID
    ElMessage.success('取件成功！')
    reset()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    completing.value = false
  }
}

const reset = () => {
  pickupCode.value = ''
  verifyResult.value = null
}
</script>

<style scoped>
.pickup-page {
  padding: 0;
}
.result {
  margin-top: 20px;
}
.amount {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
}
.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>