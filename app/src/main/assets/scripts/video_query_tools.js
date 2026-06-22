/*
 * Shared video element lookup helpers.
 */
(function () {
  const tools = window.VideoBrowserVideoQueryTools || {};
  const domTools = window.VideoBrowserDomTools || {};
  window.VideoBrowserVideoQueryTools = tools;

  tools.all = tools.all || function () {
    return Array.prototype.slice.call(domTools.queryAll('video'));
  };

  tools.forEach = tools.forEach || function (callback) {
    tools.all().forEach(callback);
  };

  tools.some = tools.some || function (predicate) {
    return tools.all().some(predicate);
  };

  tools.isActive = tools.isActive || function (video) {
    return Boolean(video && video.isConnected && !video.paused && !video.ended && video.readyState > 1);
  };

  tools.hasActive = tools.hasActive || function () {
    return tools.some(tools.isActive);
  };
})();
