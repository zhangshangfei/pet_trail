import { defineStore } from 'pinia'
import { pet as petApi } from '@/api'

export const usePetStore = defineStore('pet', {
  state: () => ({
    pets: [],
    currentPetId: null
  }),

  getters: {
    petList: (state) => {
      return state.pets || []
    },
    currentPet: (state) => {
      return state.pets.find(p => p.id === state.currentPetId) || null
    },
    hasPets: (state) => {
      return state.pets.length > 0
    }
  },

  actions: {
    // 加载宠物列表
    async loadPets() {
      try {
        const res = await petApi.getList()
        if (res.success && Array.isArray(res.data)) {
          this.pets = res.data
          return { success: true }
        }
        this.pets = []
        return { success: false }
      } catch (error) {
        console.error('加载宠物列表失败:', error)
        this.pets = []
        return { success: false, error }
      }
    },

    // 添加宠物
    async addPet(petData) {
      try {
        const res = await petApi.add(petData)
        if (res.success) {
          this.pets.push(res.data)
          return { success: true, data: res.data }
        }
        throw new Error(res.message || '添加失败')
      } catch (error) {
        console.error('添加宠物失败:', error)
        return { success: false, message: error.message }
      }
    },

    // 更新宠物
    async updatePet(petId, petData) {
      try {
        const res = await petApi.update(petId, petData)
        if (res.success) {
          const index = this.pets.findIndex(p => p.id === petId)
          if (index !== -1) {
            this.pets[index] = { ...this.pets[index], ...petData }
          }
          return { success: true }
        }
        throw new Error(res.message || '更新失败')
      } catch (error) {
        console.error('更新宠物失败:', error)
        return { success: false, message: error.message }
      }
    },

    // 删除宠物
    async deletePet(petId) {
      try {
        const res = await petApi.delete(petId)
        if (res.success) {
          this.pets = this.pets.filter(p => p.id !== petId)
          if (this.currentPetId === petId) {
            this.currentPetId = null
          }
          return { success: true }
        }
        throw new Error(res.message || '删除失败')
      } catch (error) {
        console.error('删除宠物失败:', error)
        return { success: false, message: error.message }
      }
    },

    // 设置当前宠物
    setCurrentPet(petId) {
      this.currentPetId = petId
    }
  }
})
