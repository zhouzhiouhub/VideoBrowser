/*
 * Shared skip/close button discovery and click helpers.
 */
(function () {
  const tools = window.VideoBrowserSkipButtonTools || {};
  const domTools = window.VideoBrowserDomTools || {};
  window.VideoBrowserSkipButtonTools = tools;

  tools.defaultSelectors = tools.defaultSelectors || [
    '.skip',
    '.skip-button',
    '.ad-skip',
    '.ytp-ad-skip-button',
    '.ytp-ad-skip-button-modern',
    'button[class*="skip"]',
    'button[id*="skip"]',
    'button[aria-label*="Skip"]',
    'button[aria-label*="skip"]',
    'button[aria-label*="跳过"]',
    'button[title*="跳过"]'
  ];

  tools.click = tools.click || function (selectors) {
    (selectors || tools.defaultSelectors).forEach(function (selector) {
      queryAll(selector).forEach(function (button) {
        const text = String(button.innerText || button.textContent || button.getAttribute('aria-label') || '');
        const looksLikeSkip = /skip|跳过|关闭|close/i.test(text) || selector.indexOf('skip') !== -1;
        if (looksLikeSkip && typeof button.click === 'function') {
          try { button.click(); } catch (_) {}
        }
      });
    });
  };

  function queryAll(selector) {
    if (typeof domTools.queryAll === 'function') return domTools.queryAll(selector);
    try {
      return Array.prototype.slice.call(document.querySelectorAll(selector));
    } catch (_) {
      return [];
    }
  }
})();
