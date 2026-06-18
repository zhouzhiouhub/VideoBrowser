/*
 * Shared generic cleanup selectors and DOM hide pass.
 */
(function () {
  const cleanup = window.VideoBrowserGenericCleanupSelectors || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  window.VideoBrowserGenericCleanupSelectors = cleanup;

  cleanup.adSelectors = cleanup.adSelectors || [
    '.ad',
    '.ads',
    '.ad-banner',
    '.ad-container',
    '.adsbygoogle',
    '.advertisement',
    '.popup-ad',
    '.video-ad',
    '[id^="ad_"]',
    '[id*="-ad-"]',
    '[class*="ad-banner"]',
    '[class*="advertisement"]',
    '[class*="popup-ad"]',
    '[data-ad]',
    '[data-ads]'
  ];

  cleanup.accountSelectors = cleanup.accountSelectors || [
    '[href*="passport.baidu.com"]',
    '[href*="wappass.baidu.com"]',
    '[href*="/login"]',
    '[href*="login?"]',
    '[id*="login"]',
    '[class*="login"]',
    '[id*="passport"]',
    '[class*="passport"]',
    '[id*="signin"]',
    '[class*="signin"]',
    '[aria-label*="登录"]',
    '[title*="登录"]',
    '[data-module*="login"]',
    '[data-module*="passport"]',
    '#userinfo-wrap',
    '#s-top-loginbtn',
    '.s-top-login-btn',
    '.user-login',
    '.login-area'
  ];

  cleanup.cleanupSelectors = cleanup.cleanupSelectors || [
    '[id*="top-ad"]',
    '[class*="top-ad"]',
    '[id*="topad"]',
    '[class*="topad"]',
    '[id*="ad-slot"]',
    '[class*="ad-slot"]',
    '[id*="ad-placeholder"]',
    '[class*="ad-placeholder"]',
    '[id*="banner-ad"]',
    '[class*="banner-ad"]',
    '[id*="top-banner"]',
    '[class*="top-banner"]',
    '[id*="promotion"]',
    '[class*="promotion"]',
    '[id*="open-app"]',
    '[class*="open-app"]',
    '[id*="download-app"]',
    '[class*="download-app"]',
    '[class*="app-download"]'
  ];

  cleanup.defaultSelectors = cleanup.defaultSelectors || function () {
    return cleanup.adSelectors.concat(cleanup.accountSelectors, cleanup.cleanupSelectors);
  };

  cleanup.hideDefaultElements = cleanup.hideDefaultElements || function () {
    cleanup.defaultSelectors().forEach(function (selector) {
      queryAll(selector).forEach(function (element) {
        domActions.hideElement(element, {
          reason: 'generic-cleanup',
          protectAppContainers: true
        });
      });
    });
  };

  function queryAll(selector) {
    return domTools.queryAll(selector);
  }
})();
