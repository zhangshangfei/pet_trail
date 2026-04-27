import request from '@/utils/request'

export const getClinics = (params) => {
  return request.get('/api/vet/clinics', params)
}

export const getClinicDetail = (id) => {
  return request.get(`/api/vet/clinics/${id}`)
}

export const createAppointment = (data) => {
  return request.post('/api/vet/appointments', data)
}

export const getMyAppointments = () => {
  return request.get('/api/vet/appointments')
}

export const cancelAppointment = (id, reason) => {
  const url = reason ? `/api/vet/appointments/${id}?reason=${encodeURIComponent(reason)}` : `/api/vet/appointments/${id}`
  return request.delete(url)
}
