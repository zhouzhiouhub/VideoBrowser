/*
 * Tencent Video site adapter.
 * Site-specific selectors live here; shared adapter plumbing lives in site_adapter_helpers.js.
 */
(function () {
  window.VideoBrowserSiteAdapterTools.registerBasicAdapter({
    adapterId: 'tencent',
    stateKey: '__videobrowserTencentState',
    clickSelector: 'button,a,[role="button"],.close,.skip,.txp_btn_skip,.txp_ad_skip',
    cleanupSelectors: [
      '.txp_ad',
      '.txp_ad_wrap',
      '.txp_ad_cover',
      '.txp_ad_player',
      '.mod_ad',
      '.site_pop',
      '.mod_vip_popup',
      '[class*="txp_ad"]',
      '[class*="ad_wrap"]',
      '[data-videobrowser-remove]'
    ],
    cleanupButtonPattern: /(\u5173\u95ed|\u53d6\u6d88|\u7a0d\u540e|close|cancel)/i,
    videoButtonPattern: /(\u8df3\u8fc7|\u5173\u95ed|skip|close)/i
  });
})();
