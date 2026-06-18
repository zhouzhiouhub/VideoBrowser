/*
 * iQIYI site adapter.
 * Site-specific selectors live here; shared adapter plumbing lives in site_adapter_helpers.js.
 */
(function () {
  window.VideoBrowserSiteAdapterTools.registerBasicAdapter({
    adapterId: 'iqiyi',
    stateKey: '__videobrowserIqiyiState',
    clickSelector: 'button,a,[role="button"],.close,.skip,.qy-player-vippay-close',
    cleanupSelectors: [
      '.cupid',
      '.qy-player-ad',
      '.qy-player-vippay',
      '.qy-player-focus-ad',
      '.qy-player-side-ad',
      '.qy-mod-ad',
      '.m-box-items-ad',
      '[class*="cupid"]',
      '[class*="ad-wrapper"]',
      '[data-videobrowser-remove]'
    ],
    cleanupButtonPattern: /(\u5173\u95ed|\u53d6\u6d88|\u7a0d\u540e|close|cancel)/i,
    videoButtonPattern: /(\u8df3\u8fc7|\u5173\u95ed|skip|close)/i
  });
})();
