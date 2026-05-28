(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;
  adapters.iqiyi = adapters.iqiyi || {
    apply: function (config) {
      this.lastConfig = config || {};
    }
  };
})();
