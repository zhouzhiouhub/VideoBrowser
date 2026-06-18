/*
 * YouTube site adapter.
 * Site-specific selectors live here; shared adapter plumbing lives in site_adapter_helpers.js.
 */
(function () {
  window.VideoBrowserSiteAdapterTools.registerBasicAdapter({
    adapterId: 'youtube',
    stateKey: '__videobrowserYoutubeState',
    intervalMs: 1500,
    cleanupSelectors: [
      '#player-ads',
      '#masthead-ad',
      '.ytp-ad-module',
      '.ytp-ad-overlay-container',
      '.ytp-paid-content-overlay',
      'ytd-ad-slot-renderer',
      'ytd-display-ad-renderer',
      'ytd-promoted-sparkles-web-renderer',
      'ytd-promoted-video-renderer',
      'ytd-in-feed-ad-layout-renderer',
      'ytd-compact-promoted-video-renderer'
    ],
    videoClickSelectors: [
      '.ytp-ad-skip-button',
      '.ytp-ad-skip-button-modern',
      '.ytp-ad-overlay-close-button',
      'button[class*="skip"]',
      'button[aria-label*="Skip"]',
      'button[title*="Skip"]'
    ]
  });
})();
