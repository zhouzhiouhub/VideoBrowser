/*
 * Shared video element lookup helpers.
 */
(function () {
  const tools = window.VideoBrowserVideoQueryTools || {};
  window.VideoBrowserVideoQueryTools = tools;

  tools.all = tools.all || function () {
    return Array.prototype.slice.call(document.querySelectorAll('video'));
  };

  tools.forEach = tools.forEach || function (callback) {
    tools.all().forEach(callback);
  };

  tools.some = tools.some || function (predicate) {
    return tools.all().some(predicate);
  };
})();
