import request from '@/utils/request'

export const getPetList = () => {
  return request.get('/api/pets')
}

export const createPet = (data) => {
  return request.post('/api/pets', data)
}

export const getPetDetail = (petId) => {
  return request.get(`/api/pets/${petId}`)
}

export const updatePet = (petId, data) => {
  return request.put(`/api/pets/${petId}`, data)
}

export const deletePet = (petId) => {
  return request.delete(`/api/pets/${petId}`)
}

export const updatePetWeight = (petId, weight) => {
  return request.put(`/api/pets/${petId}/weight`, { weight })
}

export const getPetAlbum = (petId, params) => {
  return request.get(`/api/pets/${petId}/album`, params)
}

export const addPetAlbumPhoto = (petId, data) => {
  return request.post(`/api/pets/${petId}/album`, data)
}

export const deletePetAlbumPhoto = (petId, photoId) => {
  return request.delete(`/api/pets/${petId}/album/${photoId}`)
}

export const getVaccineReminders = (petId) => {
  return request.get(`/api/pets/${petId}/vaccine-reminders`)
}

export const createVaccineReminder = (petId, data) => {
  return request.post(`/api/pets/${petId}/vaccine-reminders`, data)
}

export const updateVaccineReminder = (petId, reminderId, data) => {
  return request.put(`/api/pets/${petId}/vaccine-reminders/${reminderId}`, data)
}

export const updateVaccineReminderStatus = (petId, reminderId, data) => {
  return request.put(`/api/pets/${petId}/vaccine-reminders/${reminderId}/status`, data)
}

export const deleteVaccineReminder = (petId, reminderId) => {
  return request.delete(`/api/pets/${petId}/vaccine-reminders/${reminderId}`)
}

export const getParasiteReminders = (petId) => {
  return request.get(`/api/pets/${petId}/parasite-reminders`)
}

export const createParasiteReminder = (petId, data) => {
  return request.post(`/api/pets/${petId}/parasite-reminders`, data)
}

export const updateParasiteReminder = (petId, reminderId, data) => {
  return request.put(`/api/pets/${petId}/parasite-reminders/${reminderId}`, data)
}

export const updateParasiteReminderStatus = (petId, reminderId, data) => {
  return request.put(`/api/pets/${petId}/parasite-reminders/${reminderId}/status`, data)
}

export const deleteParasiteReminder = (petId, reminderId) => {
  return request.delete(`/api/pets/${petId}/parasite-reminders/${reminderId}`)
}

export { uploadImage, uploadFile, compressImage } from './upload'
