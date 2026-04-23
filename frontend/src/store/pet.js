import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const DEFAULT_PET_AVATAR = '/static/images/default-pet.png'

export const usePetStore = defineStore('pet', () => {
  const petList = ref([])
  const currentPetId = ref(null)
  const loaded = ref(false)

  const currentPet = computed(() => {
    if (!currentPetId.value || petList.value.length === 0) return null
    return petList.value.find(p => p.id === currentPetId.value) || petList.value[0] || null
  })

  const currentPetAvatar = computed(() => {
    const pet = currentPet.value
    if (!pet) return DEFAULT_PET_AVATAR
    return pet.avatar || DEFAULT_PET_AVATAR
  })

  async function loadPets() {
    try {
      const res = await uni.$request.get('/api/pets/my')
      if (res.success && res.data) {
        petList.value = res.data
        loaded.value = true
        if (!currentPetId.value && res.data.length > 0) {
          currentPetId.value = res.data[0].id
        }
      }
    } catch (e) {
      console.error('加载宠物列表失败:', e)
    }
  }

  function setCurrentPet(petId) {
    currentPetId.value = petId
  }

  function getPetAvatar(pet) {
    if (!pet) return DEFAULT_PET_AVATAR
    return pet.avatar || DEFAULT_PET_AVATAR
  }

  function clearPets() {
    petList.value = []
    currentPetId.value = null
    loaded.value = false
  }

  return {
    petList,
    currentPetId,
    currentPet,
    currentPetAvatar,
    loaded,
    loadPets,
    setCurrentPet,
    getPetAvatar,
    clearPets
  }
})
