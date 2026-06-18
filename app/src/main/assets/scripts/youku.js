/*
 * Youku site adapter.
 * Site-specific selectors live here; shared adapter plumbing lives in site_adapter_helpers.js.
 */
(function () {
  window.VideoBrowserSiteAdapterTools.registerBasicAdapter({
    adapterId: 'youku',
    stateKey: '__videobrowserYoukuState',
    clickSelector: 'button,a,[role="button"],.close,.skip,.youku-ad-skip,.h5player-skip',
    cleanupSelectors: [
      '.yk-ad',
      '.youku-ad',
      '.h5-detail-app-btn',
      '.app-download',
      '.open-app',
      '.yk-player-ad',
      '[class*="ad-wrap"]',
      '[class*="ad_container"]',
      '[class*="ad-container"]',
      '[data-videobrowser-remove]'
    ],
    cleanupButtonPattern: /(\u5173\u95ed|\u53d6\u6d88|\u7a0d\u540e|close|cancel)/i,
    videoButtonPattern: /(\u8df3\u8fc7|\u5173\u95ed|skip|close)/i
  });
})();
