(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;

  var state = window.__videobrowserYoutubeState || {
    intervalId: null,
    config: {}
  };
  window.__videobrowserYoutubeState = state;

  function query(selector) {
    try {
      return document.querySelectorAll(selector);
    } catch (_) {
      return [];
    }
  }

  function hideElement(element, reason) {
    if (!element || element === document.body || element === document.documentElement) return;
    element.setAttribute('data-videobrowser-site-dismissed', reason || 'youtube');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  }

  function hideSelectors(selectors) {
    selectors.forEach(function (selector) {
      query(selector).forEach(function (element) {
        hideElement(element, 'youtube-ad');
      });
    });
  }

  function clickSkipButtons() {
    [
      '.ytp-ad-skip-button',
      '.ytp-ad-skip-button-modern',
      '.ytp-ad-overlay-close-button',
      'button[class*="skip"]',
      'button[aria-label*="Skip"]',
      'button[title*="Skip"]'
    ].forEach(function (selector) {
      query(selector).forEach(function (button) {
        if (typeof button.click === 'function') button.click();
      });
    });
  }

  function run(config) {
    if (!document.documentElement) return;
    if (config && config.cleanupEnabled) {
      hideSelectors([
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
      ]);
    }
    if (config && config.videoEnabled) {
      clickSkipButtons();
    }
  }

  function startWorker() {
    if (state.intervalId) return;
    state.intervalId = window.setInterval(function () {
      run(state.config || {});
    }, 1500);
  }

  adapters.youtube = adapters.youtube || {};
  adapters.youtube.videoCapabilities = {
    supports: function (video) {
      return Boolean(video && video.isConnected);
    },
    canUse: function (action) {
      return action === 'enableControls';
    },
    enableControls: function (video) {
      return Boolean(video);
    }
  };
  adapters.youtube.apply = function (config) {
    this.lastConfig = config || {};
    state.config = this.lastConfig;
    run(state.config);
    startWorker();
  };
})();
