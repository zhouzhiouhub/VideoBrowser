(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;
  adapters.bilibili = adapters.bilibili || {
    apply: function (config) {
      this.lastConfig = config || {};
    }
  };
})();
