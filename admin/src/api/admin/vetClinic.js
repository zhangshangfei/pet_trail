import request from '../../utils/request'

export const getClinicList = (params) => request.get('/api/admin/vet-clinics', { params })
export const getClinicDetail = (id) => request.get(`/api/admin/vet-clinics/${id}`)
export const createClinic = (data) => request.post('/api/admin/vet-clinics', data)
export const updateClinic = (id, data) => request.put(`/api/admin/vet-clinics/${id}`, data)
export const deleteClinic = (id) => request.delete(`/api/admin/vet-clinics/${id}`)
export const updateClinicStatus = (id, status) => request.put(`/api/admin/vet-clinics/${id}/status`, { status })
export const setClinicPartner = (id, isPartner) => request.put(`/api/admin/vet-clinics/${id}/partner`, { isPartner })
export const getClinicStats = () => request.get('/api/admin/vet-clinics/stats')
export const getAppointmentList = (params) => request.get('/api/admin/vet-clinics/appointments', { params })
export const updateAppointmentStatus = (id, status) => request.put(`/api/admin/vet-clinics/appointments/${id}/status`, { status })
