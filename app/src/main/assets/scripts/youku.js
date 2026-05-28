(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;
  adapters.youku = adapters.youku || {
    apply: function (config) {
      this.lastConfig = config || {};
    }
  };
})();
