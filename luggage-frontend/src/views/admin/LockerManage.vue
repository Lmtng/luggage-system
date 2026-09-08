<template>
  <div class="locker-manage">
    <div class="page-header">
      <div>
        <h2>寄存柜与柜格管理</h2>
        <p>创建寄存柜、维护设备状态并管理柜格</p>
      </div>
      <el-button type="primary" @click="lockerDialogVisible = true">
        新增寄存柜
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>寄存柜列表</span>
              <el-button text type="primary" :loading="loadingLockers" @click="loadLockers">
                刷新
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="loadingLockers"
            :data="lockers"
            highlight-current-row
            empty-text="暂无寄存柜"
            @current-change="selectLocker"
          >
            <el-table-column prop="lockerCode" label="编号" min-width="100" />
            <el-table-column prop="name" label="名称" min-width="110" />
            <el-table-column prop="location" label="位置" min-width="130" />
            <el-table-column label="状态" width="85" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
                  {{ row.status === 'ENABLED' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="{ row }">
                <el-button
                  text
                  :type="row.status === 'ENABLED' ? 'danger' : 'success'"
                  @click.stop="toggleLockerStatus(row)"
                >
                  {{ row.status === 'ENABLED' ? '停用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>
                柜格列表
                <small v-if="selectedLocker">— {{ selectedLocker.name }}</small>
              </span>
              <el-button
                type="primary"
                size="small"
                :disabled="!selectedLocker"
                @click="cellDialogVisible = true"
              >
                新增柜格
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="loadingCells"
            :data="cells"
            :empty-text="selectedLocker ? '该寄存柜暂无柜格' : '请先选择左侧寄存柜'"
          >
            <el-table-column prop="cellNo" label="柜格编号" min-width="110" />
            <el-table-column label="规格" width="90" align="center">
              <template #default="{ row }">{{ sizeLabel(row.sizeType) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="cellStatusType(row.status)">
                  {{ cellStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="版本" prop="version" width="70" align="center" />
            <el-table-column label="操作" min-width="130" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.status !== 'OCCUPIED'"
                  size="small"
                  :type="row.status === 'AVAILABLE' ? 'danger' : 'success'"
                  @click="toggleCellStatus(row)"
                >
                  {{ row.status === 'AVAILABLE' ? '停用' : '恢复可用' }}
                </el-button>
                <span v-else class="muted">使用中不可修改</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="lockerDialogVisible" title="新增寄存柜" width="480px">
      <el-form :model="lockerForm" label-width="100px">
        <el-form-item label="寄存柜编号" required>
          <el-input v-model.trim="lockerForm.lockerCode" maxlength="30" />
        </el-form-item>
        <el-form-item label="寄存柜名称" required>
          <el-input v-model.trim="lockerForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="放置位置" required>
          <el-input v-model.trim="lockerForm.location" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lockerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingLocker" @click="createLocker">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cellDialogVisible" title="新增柜格" width="460px">
      <el-form :model="cellForm" label-width="100px">
        <el-form-item label="所属寄存柜">
          <el-input :model-value="selectedLocker?.name" disabled />
        </el-form-item>
        <el-form-item label="柜格编号" required>
          <el-input v-model.trim="cellForm.cellNo" maxlength="20" placeholder="例如 A-01" />
        </el-form-item>
        <el-form-item label="柜格规格" required>
          <el-select v-model="cellForm.sizeType" style="width: 100%">
            <el-option label="小柜" value="SMALL" />
            <el-option label="中柜" value="MEDIUM" />
            <el-option label="大柜" value="LARGE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cellDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingCell" @click="createCell">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/admin'

const lockers = ref([])
const cells = ref([])
const selectedLocker = ref(null)
const loadingLockers = ref(false)
const loadingCells = ref(false)
const lockerDialogVisible = ref(false)
const cellDialogVisible = ref(false)
const savingLocker = ref(false)
const savingCell = ref(false)

const lockerForm = reactive({ lockerCode: '', name: '', location: '' })
const cellForm = reactive({ cellNo: '', sizeType: 'SMALL' })

const sizeLabel = size => ({ SMALL: '小柜', MEDIUM: '中柜', LARGE: '大柜' }[size] || size)
const cellStatusLabel = status => ({ AVAILABLE: '空闲', OCCUPIED: '占用中', DISABLED: '已停用' }[status] || status)
const cellStatusType = status => ({ AVAILABLE: 'success', OCCUPIED: 'warning', DISABLED: 'info' }[status] || 'info')

const loadLockers = async () => {
  loadingLockers.value = true
  try {
    const res = await adminApi.getLockers()
    lockers.value = res.data || []

    if (selectedLocker.value) {
      selectedLocker.value = lockers.value.find(
        item => item.id === selectedLocker.value.id
      ) || null
    }

    if (!selectedLocker.value && lockers.value.length > 0) {
      await selectLocker(lockers.value[0])
    } else if (!selectedLocker.value) {
      cells.value = []
    }
  } catch (error) {
    lockers.value = []
    cells.value = []
  } finally {
    loadingLockers.value = false
  }
}

const selectLocker = async locker => {
  selectedLocker.value = locker || null
  cells.value = []
  if (!locker) return

  loadingCells.value = true
  try {
    const res = await adminApi.getLockerCells(locker.id)
    cells.value = res.data || []
  } catch (error) {
    cells.value = []
  } finally {
    loadingCells.value = false
  }
}

const createLocker = async () => {
  if (!lockerForm.lockerCode || !lockerForm.name || !lockerForm.location) {
    ElMessage.warning('请完整填写寄存柜编号、名称和位置')
    return
  }

  savingLocker.value = true
  try {
    await adminApi.createLocker({ ...lockerForm, status: 'ENABLED' })
    ElMessage.success('寄存柜创建成功')
    Object.assign(lockerForm, { lockerCode: '', name: '', location: '' })
    lockerDialogVisible.value = false
    await loadLockers()
  } catch (error) {
    // 错误提示由拦截器统一显示
  } finally {
    savingLocker.value = false
  }
}

const createCell = async () => {
  if (!selectedLocker.value || !cellForm.cellNo || !cellForm.sizeType) {
    ElMessage.warning('请填写柜格编号并选择规格')
    return
  }

  savingCell.value = true
  try {
    await adminApi.createLockerCell({
      lockerId: selectedLocker.value.id,
      cellNo: cellForm.cellNo,
      sizeType: cellForm.sizeType,
      status: 'AVAILABLE'
    })
    ElMessage.success('柜格创建成功')
    Object.assign(cellForm, { cellNo: '', sizeType: 'SMALL' })
    cellDialogVisible.value = false
    await selectLocker(selectedLocker.value)
  } catch (error) {
    // 错误提示由拦截器统一显示
  } finally {
    savingCell.value = false
  }
}

const toggleLockerStatus = async locker => {
  const status = locker.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = status === 'ENABLED' ? '启用' : '停用'

  try {
    await ElMessageBox.confirm(`确定要${action}寄存柜“${locker.name}”吗？`, '状态确认')
    await adminApi.changeLockerStatus(locker.id, status)
    ElMessage.success(`寄存柜已${action}`)
    await loadLockers()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      // 接口错误已经统一提示
    }
  }
}

const toggleCellStatus = async cell => {
  const status = cell.status === 'AVAILABLE' ? 'DISABLED' : 'AVAILABLE'
  const action = status === 'AVAILABLE' ? '恢复' : '停用'

  try {
    await ElMessageBox.confirm(`确定要${action}柜格“${cell.cellNo}”吗？`, '状态确认')
    await adminApi.changeLockerCellStatus(cell.id, status)
    ElMessage.success(`柜格已${action}`)
    await selectLocker(selectedLocker.value)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      // 接口错误已经统一提示
    }
  }
}

onMounted(loadLockers)
</script>

<style scoped>
.page-header, .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header small { color: #909399; font-weight: normal; }
.muted { color: #909399; font-size: 12px; }
</style>
