Component({
  data: {
    selected: 0,
    hidden: false,
    list: [
      { pagePath: "/pages/home/index" },
      { pagePath: "/pages/dashboard/index" },
      { pagePath: "/pages/checkin/index" },
      { pagePath: "/pages/me/index" }
    ]
  },
  lifetimes: {
    attached() {
      this.syncStateByRoute();
    },
    ready() {
      this.syncStateByRoute();
    }
  },
  pageLifetimes: {
    show() {
      const self = this;
      // 使用 nextTick 确保 DOM 更新后再同步
      setTimeout(() => {
        self.syncStateByRoute();
      }, 100);
    }
  },
  methods: {
    syncStateByRoute() {
      try {
        const pages = getCurrentPages();
        const current = pages && pages.length ? pages[pages.length - 1] : null;
        const route = current && current.route ? current.route : "";
        const shouldHide = route === "pages/health/index";
        const normalizedRoute = route ? `/${route}` : "";
        const selectedIndex = this.data.list.findIndex((item) => item.pagePath === normalizedRoute);
        
        if (selectedIndex >= 0 && this.data.selected !== selectedIndex) {
          this.setData({ selected: selectedIndex });
        }
        if (this.data.hidden !== shouldHide) {
          this.setData({ hidden: shouldHide });
        }
      } catch (e) {
        // ignore route detection errors
      }
    },
    onSwitch(e) {
      const index = Number(e.currentTarget.dataset.index || 0);
      const item = this.data.list[index];
      if (!item) return;
      
      wx.switchTab({
        url: item.pagePath
      });
    }
  }
});

