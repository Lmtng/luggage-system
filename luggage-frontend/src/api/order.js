import api from './index'

export const orderApi = {
    // 按规格查询当前可用柜格
    getAvailableCells(sizeType) {
        return api.get('/orders/locker-cells/available', {
            params: { sizeType }
        })
    },

    // 创建订单
    create(data) {
        return api.post('/orders', data)
    },

    // 查询个人订单列表
    getMyOrders(page, size) {
        return api.get('/orders/my', { params: { page, size } })
    },

    // 查询订单详情
    getDetail(id) {
        return api.get(`/orders/${id}`)
    },

    // 验证取件码
    verifyPickup(id, pickupCode) {
        return api.post(`/orders/${id}/pickup/verify`, { pickupCode })
    },

    // 完成取件
    complete(id) {
        return api.post(`/orders/${id}/complete`)
    }
}
