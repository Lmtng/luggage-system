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
    }
}