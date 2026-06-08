(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;

  var state = window.__videobrowserTencentState || {
    intervalId: null,
    config: {}
  };
  window.__videobrowserTencentState = state;

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
    element.setAttribute('data-videobrowser-site-dismissed', reason || 'tencent');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  }

  function hideSelectors(selectors) {
    selectors.forEach(function (selector) {
      query(selector).forEach(function (element) {
        hideElement(element, 'tencent-ad');
      });
    });
  }

  function clickTextButtons(pattern) {
    query('button,a,[role="button"],.close,.skip,.txp_btn_skip,.txp_ad_skip').forEach(function (element) {
      if (pattern.test(textOf(element)) && typeof element.click === 'function') {
        element.click();
      }
    });
  }

  function run(config) {
    if (!document.documentElement) return;
    if (config && config.cleanupEnabled) {
      hideSelectors([
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
      ]);
      clickTextButtons(/(\u5173\u95ed|\u53d6\u6d88|\u7a0d\u540e|close|cancel)/i);
    }
    if (config && config.videoEnabled) {
      clickTextButtons(/(\u8df3\u8fc7|\u5173\u95ed|skip|close)/i);
    }
  }

  function startWorker() {
    if (state.intervalId) return;
    state.intervalId = window.setInterval(function () {
      run(state.config || {});
    }, 1800);
  }

  adapters.tencent = adapters.tencent || {};
  adapters.tencent.videoCapabilities = {
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
  adapters.tencent.apply = function (config) {
    this.lastConfig = config || {};
    state.config = this.lastConfig;
    run(state.config);
    startWorker();
  };
})();
