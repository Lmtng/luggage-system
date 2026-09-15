<template>
  <div class="operation-logs">
    <div class="page-header">
      <div>
        <h2>操作日志</h2>
        <p>查看管理员修改计费规则和处理异常订单的记录</p>
      </div>
      <div class="filters">
        <el-select
          v-model="operationType"
          placeholder="全部操作"
          @change="handleFilterChange"
        >
          <el-option label="修改计费规则" value="UPDATE_PRICE_RULE" />
          <el-option label="处理异常订单" value="FIX_EXCEPTION_ORDER" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadLogs">刷新</el-button>
      </div>
    </div>

    <el-card>
      <el-table v-loading="loading" :data="logs" empty-text="暂无操作日志">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operatorId" label="管理员ID" width="110" />
        <el-table-column label="操作类型" width="180">
          <template #default="{ row }">
            {{ getOperationTypeLabel(row.operationType) }}
          </template>
        </el-table-column>
        <el-table-column label="操作对象" width="150">
          <template #default="{ row }">
            {{ getTargetTypeLabel(row.targetType) }} #{{ row.targetId }}
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详细说明" min-width="260" />
        <el-table-column label="操作时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadLogs"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '../../api/admin'

const logs = ref([])
const loading = ref(false)
const operationType = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 操作类型映射
const getOperationTypeLabel = (type) => {
  const map = {
    UPDATE_CELL: '修改柜格',
    UPDATE_PRICE_RULE: '修改计费规则',
    FIX_EXCEPTION_ORDER: '处理异常订单',
    CREATE_LOCKER: '新增寄存柜',
    DISABLE_CELL: '停用柜格',
    ENABLE_CELL: '启用柜格',
    OPEN_CELL: '释放柜格',
    CHANGE_USER_STATUS: '修改用户状态',
    RELEASE_CELL_BY_USER_DISABLE: '停用用户释放柜格'
  }
  return map[type] || type
}

// 操作对象映射
const getTargetTypeLabel = (type) => {
  const map = {
    CELL: '柜格',
    PRICE_RULE: '计费规则',
    ORDER: '订单',
    LOCKER: '寄存柜',
    USER: '用户'
  }
  return map[type] || type
}

const typeLabel = type => ({
  UPDATE_PRICE_RULE: '修改计费规则',
  FIX_EXCEPTION_ORDER: '处理异常订单'
}[type] || type)

const formatTime = time => time ? new Date(time).toLocaleString('zh-CN') : '-'

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await adminApi.getOperationLogs(
      pageNum.value,
      pageSize.value,
      operationType.value || undefined
    )
    logs.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } catch (error) {
    logs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pageNum.value = 1
  loadLogs()
}

const handleSizeChange = () => {
  pageNum.value = 1
  loadLogs()
}

onMounted(loadLogs)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0 0 8px; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.filters { display: flex; gap: 10px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
