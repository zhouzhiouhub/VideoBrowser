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
  const accountSelectors = [
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
  const cleanupSelectors = [
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
    style.textContent = adSelectors.concat(accountSelectors, cleanupSelectors).join(',') +
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
    adSelectors.concat(accountSelectors, cleanupSelectors).forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (element) {
        if (element && element.parentNode) element.remove();
      });
    });
    removeTopAccountBars();
    removeTopNoiseBlocks();
  }

  function removeTopAccountBars() {
    if (!/(\.|^)baidu\.com$/i.test(location.hostname)) return;
    const candidates = [];
    function addCandidate(element) {
      if (element && candidates.indexOf(element) === -1) candidates.push(element);
    }
    document.querySelectorAll(
      'header,[role="banner"],[id*="top"],[class*="top"],[id*="head"],[class*="head"],body>div,body>div>div'
    ).forEach(addCandidate);

    candidates.forEach(function (element) {
      if (!element || element.querySelector('input,textarea,form')) return;
      const rect = element.getBoundingClientRect();
      if (rect.top < 0 || rect.top > 220 || rect.height <= 0 || rect.height > 72) return;

      const text = String(element.innerText || element.textContent || '');
      const html = String(element.innerHTML || '');
      const accountLike = /登录|账号|账户|我的|用户|passport|login|signin|user|profile/i.test(text + html);
      const iconBarLike = element.querySelectorAll('a,button,[role="button"],svg,i').length >= 1 &&
        /menu|grid|app|user|profile|account|more|更多|应用/i.test(html);
      if (rect.width < Math.min(window.innerWidth * 0.45, 180) && !accountLike && !iconBarLike) return;
      if (accountLike || iconBarLike) element.remove();
    });
  }

  function removeTopNoiseBlocks() {
    const searchHomeLike = /(\.|^)(baidu|sogou|so|sm)\.com$/i.test(location.hostname);
    if (!searchHomeLike) return;

    document.querySelectorAll(
      'body>div,body>section,header,[role="banner"],[id*="top"],[class*="top"],[id*="banner"],[class*="banner"]'
    ).forEach(function (element) {
      if (!element || element.querySelector('input,textarea,select,form,video,canvas')) return;

      const rect = element.getBoundingClientRect();
      if (rect.top < 0 || rect.top > 180 || rect.height < 32 || rect.height > 150) return;
      if (rect.width < window.innerWidth * 0.58) return;

      const text = String(element.innerText || element.textContent || '').replace(/\s+/g, '');
      const html = String(element.innerHTML || '');
      const descriptor = String(element.id || '') + ' ' + String(element.className || '') + ' ' + html;
      const brandLogoLike = /logo|search-logo|bdlogo|sogoulogo/i.test(descriptor) ||
        /百度|搜狗搜索|搜狗|360搜索|必应|Bing/i.test(text);
      const adLike = /广告|推广|赞助|商业合作|无图|太平洋|下载APP|打开APP|app/i.test(text + descriptor) ||
        /ad|ads|advert|banner|promo|promotion|sponsor|slot|download|openapp/i.test(descriptor);
      const sparseTopSlot = /(\.|^)sogou\.com$/i.test(location.hostname) &&
        rect.top < 120 &&
        rect.height >= 48 &&
        rect.width > window.innerWidth * 0.82 &&
        text.length <= 18 &&
        element.querySelectorAll('a,button,img,svg').length <= 2;

      if (!brandLogoLike && (adLike || sparseTopSlot)) element.remove();
    });
  }

  function enhanceVideos() {
    if (!state.config.videoEnabled) {
      document.querySelectorAll('video').forEach(function (video) {
        video.playbackRate = 1;
        video.defaultPlaybackRate = 1;
      });
      return;
    }
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
