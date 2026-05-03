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

export { uploadImage, uploadFile, compressImage } from './upload'
