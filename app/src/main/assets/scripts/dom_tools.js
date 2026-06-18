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

  domTools.elementDescriptor = domTools.elementDescriptor || function (element) {
    return (
      String(element && element.id || '') + ' ' +
      String(element && element.className || '') + ' ' +
      String(element && element.getAttribute && element.getAttribute('role') || '') + ' ' +
      String(element && element.getAttribute && element.getAttribute('aria-label') || '') + ' ' +
      String(element && element.getAttribute && element.getAttribute('title') || '')
    );
  };

  domTools.parseZIndex = domTools.parseZIndex || function (value) {
    const parsed = Number.parseInt(value, 10);
    return Number.isFinite(parsed) ? parsed : 0;
  };
})();
