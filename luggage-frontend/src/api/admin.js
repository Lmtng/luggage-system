import api from './index'

export const adminApi = {
    // 查询所有订单
    getOrders(page, size, status) {
        return api.get('/admin/orders', { params: { page, size, status } })
    },

    // 获取所有计费规则
    getPriceRules() {
        return api.get('/admin/price-rules')
    },

    // 修改计费规则
    updatePriceRule(id, data) {
        return api.put(`/admin/price-rules/${id}`, data)
    },

    // 处理异常订单
    fixOrder(id, targetStatus) {
        return api.put(`/admin/orders/${id}/status`, { targetStatus })
    },

    // 获取统计数据
    getStatistics() {
        return api.get('/admin/statistics')
    },

    // 用户管理
    getUsers() {
        return api.get('/admin/users')
    },

    changeUserStatus(userId, status) {
        return api.put(`/admin/users/${userId}/status`, null, {
            params: { status }
        })
    },

    // 寄存柜管理
    getLockers() {
        return api.get('/admin/lockers')
    },

    createLocker(data) {
        return api.post('/admin/lockers', data)
    },

    changeLockerStatus(lockerId, status) {
        return api.put(`/admin/lockers/${lockerId}/status`, null, {
            params: { status }
        })
    },

    // 柜格管理
    getLockerCells(lockerId) {
        return api.get('/admin/locker-cells', {
            params: { lockerId }
        })
    },

    createLockerCell(data) {
        return api.post('/admin/locker-cells', data)
    },

    changeLockerCellStatus(cellId, status) {
        return api.put(`/admin/locker-cells/${cellId}/status`, null, {
            params: { status }
        })
    },

    // 管理员操作日志
    getOperationLogs(page, size, operationType) {
        return api.get('/admin/operation-logs', {
            params: { page, size, operationType }
        })
    }
}
