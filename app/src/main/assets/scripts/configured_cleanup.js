/*
 * Shared cleanup driven by configured CSS and DOM selectors.
 */
(function () {
  const cleanup = window.VideoBrowserConfiguredCleanup || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  const genericCleanupSelectors = window.VideoBrowserGenericCleanupSelectors || {};
  const domActions = window.VideoBrowserDomActions || {};
  const styleManager = window.VideoBrowserStyleManager || {};
  window.VideoBrowserConfiguredCleanup = cleanup;

  cleanup.cssSelectors = cleanup.cssSelectors || function (state) {
    return selectorList(state && state.config && state.config.cssSelectors);
  };

  cleanup.userCssSelectors = cleanup.userCssSelectors || function (state) {
    return selectorList(state && state.config && state.config.userCssSelectors);
  };

  cleanup.domSelectors = cleanup.domSelectors || function (state) {
    return selectorList(state && state.config && state.config.domSelectors);
  };

  cleanup.searchPageHideCss = cleanup.searchPageHideCss || function (state) {
    return selectorList(state && state.config && state.config.searchPageHideCss);
  };

  cleanup.hasUserCssSelectors = cleanup.hasUserCssSelectors || function (state) {
    return cleanup.userCssSelectors(state).length > 0;
  };

  cleanup.injectStyle = cleanup.injectStyle || function (state, options) {
    const config = options || {};
    const selectors = (config.includeGenericSelectors ? genericCleanupSelectors.defaultSelectors() : [])
      .concat(config.includeRuleSelectors ? cleanup.cssSelectors(state) : [])
      .concat(cleanup.searchPageHideCss(state))
      .concat(cleanup.userCssSelectors(state));
    styleManager.injectHideRules(selectors);
  };

  cleanup.removeStyle = cleanup.removeStyle || function () {
    styleManager.remove();
  };

  cleanup.removeDomElements = cleanup.removeDomElements || function (state) {
    cleanup.domSelectors(state).forEach(function (selector) {
      selectorTools.queryAll(selector).forEach(function (element) {
        removeElement(element, 'rule-dom-remove');
      });
    });
  };

  function selectorList(value) {
    return selectorTools.safeSelectorList(value);
  }

  function removeElement(element, reason) {
    domActions.removeElement(element, {
      reason: reason || 'remove',
      protectAppContainers: true
    });
  }
})();
