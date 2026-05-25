(function () {
  const state = window.__videobrowserState || {
    observer: null,
    intervalId: null,
    hooked: false,
    config: {}
  };
  window.__videobrowserState = state;

  const styleId = '__videobrowser_css_filter__';
  const adSelectors = [
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
  const skipSelectors = [
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
  const blockedKeywords = [
    'doubleclick',
    'googleads',
    'googlesyndication',
    '/pagead/',
    '/adservice/',
    '/adserver/',
    '/advert/',
    '/ads/',
    'vast',
    'vmap',
    'preroll',
    'midroll'
  ];

  function shouldBlockUrl(value) {
    const url = String(value || '').toLowerCase();
    return blockedKeywords.some(function (keyword) {
      return url.indexOf(keyword) !== -1;
    });
  }

  function injectStyle() {
    if (document.getElementById(styleId)) return;
    const style = document.createElement('style');
    style.id = styleId;
    style.textContent = adSelectors.join(',') +
      '{display:none!important;visibility:hidden!important;opacity:0!important;pointer-events:none!important;}';
    document.documentElement.appendChild(style);
  }

  function removeStyle() {
    const style = document.getElementById(styleId);
    if (style) style.remove();
  }

  function removeAds() {
    if (!state.config.cleanupEnabled || !document.documentElement) return;
    injectStyle();
    adSelectors.forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (element) {
        if (element && element.parentNode) element.remove();
      });
    });
  }

  function enhanceVideos() {
    if (!state.config.videoEnabled) return;
    const speed = Number(state.config.videoSpeed || 1);
    document.querySelectorAll('video').forEach(function (video) {
      if (Number.isFinite(speed) && speed > 0 && video.playbackRate !== speed) {
        video.playbackRate = speed;
      }
      video.defaultPlaybackRate = speed;
    });
  }

  function clickSkipButtons() {
    if (!state.config.videoEnabled) return;
    skipSelectors.forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (button) {
        const text = String(button.innerText || button.textContent || button.getAttribute('aria-label') || '');
        const looksLikeSkip = /skip|跳过|关闭|close/i.test(text) || selector.indexOf('skip') !== -1;
        if (looksLikeSkip && typeof button.click === 'function') button.click();
      });
    });
  }

  function installHooks() {
    if (state.hooked) return;
    state.hooked = true;

    const originalOpen = window.open;
    window.open = function (url) {
      if (state.config.cleanupEnabled && shouldBlockUrl(url)) return null;
      return originalOpen.apply(this, arguments);
    };

    const originalFetch = window.fetch;
    if (typeof originalFetch === 'function') {
      window.fetch = function () {
        if (state.config.cleanupEnabled && arguments.length > 0) {
          const input = arguments[0];
          const url = typeof input === 'string' ? input : input && input.url;
          if (shouldBlockUrl(url)) {
            return Promise.reject(new Error('Blocked by VideoBrowser'));
          }
        }
        return originalFetch.apply(this, arguments);
      };
    }
  }

  function startWorkers() {
    if (!state.observer && document.documentElement) {
      state.observer = new MutationObserver(function () {
        removeAds();
        clickSkipButtons();
        enhanceVideos();
      });
      state.observer.observe(document.documentElement, {
        childList: true,
        subtree: true
      });
    }

    if (!state.intervalId) {
      state.intervalId = window.setInterval(function () {
        removeAds();
        clickSkipButtons();
        enhanceVideos();
      }, 1500);
    }
  }

  window.VideoBrowserEnhancer = {
    apply: function (config) {
      state.config = config || {};
      installHooks();
      if (state.config.cleanupEnabled) {
        removeAds();
      } else {
        removeStyle();
      }
      clickSkipButtons();
      enhanceVideos();
      startWorkers();
    }
  };
})();
