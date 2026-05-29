(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;

  var state = window.__videobrowserIqiyiState || {
    intervalId: null,
    config: {}
  };
  window.__videobrowserIqiyiState = state;

  function query(selector) {
    try {
      return document.querySelectorAll(selector);
    } catch (_) {
      return [];
    }
  }

  function textOf(element) {
    return String(
      element.innerText ||
      element.textContent ||
      element.getAttribute('aria-label') ||
      element.getAttribute('title') ||
      ''
    );
  }

  function hideElement(element, reason) {
    if (!element || element === document.body || element === document.documentElement) return;
    element.setAttribute('data-videobrowser-site-dismissed', reason || 'iqiyi');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  }

  function hideSelectors(selectors) {
    selectors.forEach(function (selector) {
      query(selector).forEach(function (element) {
        hideElement(element, 'iqiyi-ad');
      });
    });
  }

  function clickTextButtons(pattern) {
    query('button,a,[role="button"],.close,.skip,.qy-player-vippay-close').forEach(function (element) {
      if (pattern.test(textOf(element)) && typeof element.click === 'function') {
        element.click();
      }
    });
  }

  function enableVideoControls() {
    query('video').forEach(function (video) {
      if (!video.controls) video.controls = true;
      video.setAttribute('controls', 'controls');
    });
  }

  function run(config) {
    if (!document.documentElement) return;
    if (config && config.cleanupEnabled) {
      hideSelectors([
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
      ]);
      clickTextButtons(/(\u5173\u95ed|\u53d6\u6d88|\u7a0d\u540e|close|cancel)/i);
    }
    if (config && config.videoEnabled) {
      clickTextButtons(/(\u8df3\u8fc7|\u5173\u95ed|skip|close)/i);
      enableVideoControls();
    }
  }

  function startWorker() {
    if (state.intervalId) return;
    state.intervalId = window.setInterval(function () {
      run(state.config || {});
    }, 1800);
  }

  adapters.iqiyi = adapters.iqiyi || {};
  adapters.iqiyi.apply = function (config) {
    this.lastConfig = config || {};
    state.config = this.lastConfig;
    run(state.config);
    startWorker();
  };
})();
