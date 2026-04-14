import { defineStore } from 'pinia'

export const useTabBarStore = defineStore('tabbar', {
  state: () => ({
    selected: 0,
    routeMap: {
      'pages/home/index': 0,
      'pages/dashboard/index': 1,
      'pages/checkin/index': 2,
      'pages/me/index': 3
    }
  }),

  actions: {
    setSelected(index) {
      this.selected = index
    },

    syncFromRoute(route) {
      const index = this.routeMap[route]
      if (typeof index === 'number') {
        this.selected = index
      }
    }
  }
})
