(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;
  adapters.youtube = adapters.youtube || {
    apply: function (config) {
      this.lastConfig = config || {};
    }
  };
})();
