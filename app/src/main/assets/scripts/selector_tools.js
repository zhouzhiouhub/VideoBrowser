/*
 * Shared selector validation, safe querying, and text normalization helpers.
 */
(function () {
  const selectorTools = window.VideoBrowserSelectorTools || {};
  const domTools = window.VideoBrowserDomTools || {};
  window.VideoBrowserSelectorTools = selectorTools;

  selectorTools.isSafeSelector = selectorTools.isSafeSelector || function (selector) {
    if (!selector || selector.length > 200) return false;
    if (/[{};<>]/.test(selector)) return false;
    return !/:has\(|:contains\(|:matches\(|:xpath\(|javascript:|expression\(/i.test(selector);
  };

  selectorTools.safeSelectorList = selectorTools.safeSelectorList || function (value) {
    if (!Array.isArray(value)) return [];
    return value.map(function (selector) {
      return String(selector || '').trim();
    }).filter(function (selector) {
      return selectorTools.isSafeSelector(selector);
    });
  };

  selectorTools.queryAll = selectorTools.queryAll || function (selector) {
    return domTools.queryAll(selector);
  };

  selectorTools.queryAllWithin = selectorTools.queryAllWithin || function (root, selector) {
    return domTools.queryAllWithin(root, selector);
  };

  selectorTools.normalizeText = selectorTools.normalizeText || function (value) {
    return String(value || '').replace(/\s+/g, ' ').trim();
  };

  selectorTools.compactText = selectorTools.compactText || function (value) {
    return String(value || '').replace(/\s+/g, '');
  };

  selectorTools.cssIdentifier = selectorTools.cssIdentifier || function (value) {
    if (window.CSS && typeof window.CSS.escape === 'function') {
      return window.CSS.escape(String(value));
    }
    return String(value).replace(/[^A-Za-z0-9_-]/g, function (character) {
      return '\\' + character.charCodeAt(0).toString(16) + ' ';
    });
  };
})();
