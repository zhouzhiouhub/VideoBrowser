/*
 * Shared CSS hide rule injection for page cleanup modules.
 */
(function () {
  const styleManager = window.VideoBrowserStyleManager || {};
  window.VideoBrowserStyleManager = styleManager;

  styleManager.styleId = styleManager.styleId || '__videobrowser_css_filter__';

  styleManager.injectHideRules = styleManager.injectHideRules || function (selectors) {
    const values = Array.isArray(selectors) ? selectors : [];
    const uniqueSelectors = values.filter(function (selector, index) {
      return values.indexOf(selector) === index;
    });
    if (!uniqueSelectors.length) {
      styleManager.remove();
      return;
    }
    let style = document.getElementById(styleManager.styleId);
    if (!style) {
      style = document.createElement('style');
      style.id = styleManager.styleId;
      document.documentElement.appendChild(style);
    }
    style.textContent = uniqueSelectors.join(',') +
      '{display:none!important;visibility:hidden!important;opacity:0!important;pointer-events:none!important;}';
  };

  styleManager.remove = styleManager.remove || function () {
    const style = document.getElementById(styleManager.styleId);
    if (style) style.remove();
  };
})();
