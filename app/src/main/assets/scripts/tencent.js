(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;
  adapters.tencent = adapters.tencent || {
    apply: function (config) {
      this.lastConfig = config || {};
    }
  };
})();
