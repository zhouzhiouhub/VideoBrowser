/*
 * Shared safe DOM query helpers for common and site adapter scripts.
 */
(function () {
  const domTools = window.VideoBrowserDomTools || {};
  window.VideoBrowserDomTools = domTools;

  domTools.queryAll = domTools.queryAll || function (selector) {
    try {
      return document.querySelectorAll(selector);
    } catch (_) {
      return [];
    }
  };

  domTools.queryAllWithin = domTools.queryAllWithin || function (root, selector) {
    if (!root || typeof root.querySelectorAll !== 'function') return [];
    try {
      return Array.prototype.slice.call(root.querySelectorAll(selector));
    } catch (_) {
      return [];
    }
  };
})();
