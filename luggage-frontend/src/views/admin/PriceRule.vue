<template>
  <div class="price-rule">
    <h2>⚙️ 计费规则管理</h2>

    <el-card>
      <el-table :data="rules" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="规格" width="100">
          <template #default="{ row }">
            <el-tag :type="getSizeTypeTag(row.sizeType)">
              {{ getSizeLabel(row.sizeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unitMinutes" label="计费单位（分钟）" width="150" />
        <el-table-column prop="unitPrice" label="单价（元）" width="120">
          <template #default="{ row }">
            ¥{{ row.unitPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="freeMinutes" label="免费时长（分钟）" width="150" />
        <el-table-column prop="capAmount" label="封顶金额（元）" width="140">
          <template #default="{ row }">
            {{ row.capAmount ? '¥' + row.capAmount : '无' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="showEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑计费规则弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑计费规则" width="500px">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="规格">
          <el-tag>{{ getSizeLabel(editForm.sizeType) }}</el-tag>
        </el-form-item>
        <el-form-item label="计费单位（分钟）">
          <el-input-number v-model="editForm.unitMinutes" :min="1" :max="1440" />
        </el-form-item>
        <el-form-item label="单价（元）">
          <el-input-number v-model="editForm.unitPrice" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="免费时长（分钟）">
          <el-input-number v-model="editForm.freeMinutes" :min="0" :max="1440" />
        </el-form-item>
        <el-form-item label="封顶金额（元）">
          <el-input-number v-model="editForm.capAmount" :min="0" :precision="2" :step="5" placeholder="0表示不封顶" />
          <span style="color: #909399; font-size: 12px; margin-left: 10px;">0表示不封顶</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateRule" :loading="updating">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api/admin'

const loading = ref(false)
const rules = ref([])
const editDialogVisible = ref(false)
const updating = ref(false)

const editForm = reactive({
  id: null,
  sizeType: '',
  unitMinutes: 60,
  unitPrice: 2,
  freeMinutes: 30,
  capAmount: 20,
  enabled: 1
})

// 规格映射
const getSizeLabel = (size) => {
  const map = { SMALL: '小柜', MEDIUM: '中柜', LARGE: '大柜' }
  return map[size] || size
}

const getSizeTypeTag = (size) => {
  const map = { SMALL: 'success', MEDIUM: 'warning', LARGE: 'danger' }
  return map[size] || 'info'
}

// 加载计费规则
const loadRules = async () => {
  loading.value = true
  try {
    const res = await adminApi.getPriceRules()
    rules.value = res.data || []
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 显示编辑弹窗
const showEditDialog = (row) => {
  Object.assign(editForm, {
    id: row.id,
    sizeType: row.sizeType,
    unitMinutes: row.unitMinutes,
    unitPrice: row.unitPrice,
    freeMinutes: row.freeMinutes,
    capAmount: row.capAmount || 0,
    enabled: row.enabled
  })
  editDialogVisible.value = true
}

// 更新计费规则
const handleUpdateRule = async () => {
  updating.value = true
  try {
    // 如果 capAmount 为 0，表示不封顶
    const data = { ...editForm }
    if (data.capAmount === 0) {
      data.capAmount = null
    }

    await adminApi.updatePriceRule(editForm.id, data)
    ElMessage.success('计费规则修改成功')
    editDialogVisible.value = false
    loadRules()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.price-rule {
  padding: 0;
}
</style>