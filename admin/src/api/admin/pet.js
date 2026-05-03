import request from '../../utils/request'

export const getPetList = (params) => request.get('/api/admin/pets', { params })
export const getPetDetail = (id) => request.get(`/api/admin/pets/${id}`)
export const updatePet = (id, data) => request.put(`/api/admin/pets/${id}`, data)
export const deletePet = (id) => request.delete(`/api/admin/pets/${id}`)
