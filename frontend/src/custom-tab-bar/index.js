Component({
  data: {
    selected: 0,
    hidden: false,
    list: [
      { pagePath: "/pages/home/index" },
      { pagePath: "/pages/dashboard/index" },
      { pagePath: "/pages/community/index" },
      { pagePath: "/pages/me/index" }
    ]
  },
  pageLifetimes: {
    show() {
      this.syncStateByRoute();
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
        const nextSelected = selectedIndex >= 0 ? selectedIndex : this.data.selected;

        if (this.data.hidden !== shouldHide || this.data.selected !== nextSelected) {
          this.setData({ hidden: shouldHide, selected: nextSelected });
        }
      } catch (e) {
        // ignore route detection errors
      }
    },
    onSwitch(e) {
      const index = Number(e.currentTarget.dataset.index || 0);
      const item = this.data.list[index];
      if (!item) return;
      this._switchSeq = (this._switchSeq || 0) + 1;
      const seq = this._switchSeq;
      this.setData({ hidden: false, selected: index });
      wx.switchTab({
        url: item.pagePath,
        complete: () => {
          // Only the latest tap can correct state.
          setTimeout(() => {
            if (seq === this._switchSeq) this.syncStateByRoute();
          }, 80);
        }
      });
    }
  }
});

