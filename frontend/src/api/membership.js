import request from '@/utils/request'

export const getMembershipInfo = () => {
  return request.get('/api/membership/info')
}

export const createMembershipOrder = (data) => {
  return request.post('/api/membership/orders', data)
}

export const payMembershipOrder = (orderId) => {
  return request.post(`/api/membership/orders/${orderId}/pay`)
}

export const cancelMembership = () => {
  return request.post('/api/membership/cancel')
}
