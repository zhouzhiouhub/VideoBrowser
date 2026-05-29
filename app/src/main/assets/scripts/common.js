(function () {
  const state = window.__videobrowserState || {
    observer: null,
    intervalId: null,
    hooked: false,
    config: {},
    pendingWork: false,
    lastWorkAt: 0,
    lastCleanupAt: 0,
    suppressMutationWork: false,
    disposed: false
  };
  window.__videobrowserState = state;
  state.suppressMutationWork = false;
  state.lastCleanupAt = Number(state.lastCleanupAt || 0);
  if (!Number.isFinite(state.lastCleanupAt)) state.lastCleanupAt = 0;
  if (!state.fullscreenHookedVideos || typeof state.fullscreenHookedVideos.add !== 'function') {
    state.fullscreenHookedVideos = new WeakSet();
  }
  if (!state.speedHookedVideos || typeof state.speedHookedVideos.add !== 'function') {
    state.speedHookedVideos = new WeakSet();
  }
  state.nativeFullscreenVideo = state.nativeFullscreenVideo || null;
  state.directionalPlayback = state.directionalPlayback || null;
  state.fullscreenPlaybackSpeed = Number(state.fullscreenPlaybackSpeed || 1);
  if (!Number.isFinite(state.fullscreenPlaybackSpeed) || state.fullscreenPlaybackSpeed <= 0) {
    state.fullscreenPlaybackSpeed = 1;
  }

  const styleId = '__videobrowser_css_filter__';
  const normalCleanupIntervalMs = 3000;
  const activeVideoCleanupIntervalMs = 15000;
  const normalWorkDelayMs = 250;
  const activeVideoWorkDelayMs = 750;
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
    return blockedKeywords.concat(externalBlockedKeywords()).some(function (keyword) {
      return url.indexOf(keyword) !== -1;
    });
  }

  function externalBlockedKeywords() {
    const values = state.config && state.config.blockedUrlKeywords;
    if (!Array.isArray(values)) return [];
    return values.map(function (keyword) {
      return String(keyword || '').trim().toLowerCase();
    }).filter(function (keyword) {
      return keyword.length >= 3;
    });
  }

  function externalCssSelectors() {
    return safeSelectorList(state.config && state.config.cssSelectors);
  }

  function externalDomSelectors() {
    return safeSelectorList(state.config && state.config.domSelectors);
  }

  function safeSelectorList(value) {
    if (!Array.isArray(value)) return [];
    return value.map(function (selector) {
      return String(selector || '').trim();
    }).filter(function (selector) {
      return isSafeSelector(selector);
    });
  }

  function isSafeSelector(selector) {
    if (!selector || selector.length > 200) return false;
    if (/[{};<>]/.test(selector)) return false;
    return !/:has\(|:contains\(|:matches\(|:xpath\(|javascript:|expression\(/i.test(selector);
  }

  function querySelectorAllSafe(selector) {
    try {
      return document.querySelectorAll(selector);
    } catch (_) {
      return [];
    }
  }

  function injectStyle(includeGenericSelectors) {
    const selectors = (includeGenericSelectors ? adSelectors.concat(accountSelectors, cleanupSelectors) : [])
      .concat(externalCssSelectors());
    const uniqueSelectors = selectors.filter(function (selector, index) {
      return selectors.indexOf(selector) === index;
    });
    if (!uniqueSelectors.length) {
      removeStyle();
      return;
    }
    let style = document.getElementById(styleId);
    if (!style) {
      style = document.createElement('style');
      style.id = styleId;
      document.documentElement.appendChild(style);
    }
    style.textContent = uniqueSelectors.join(',') +
      '{display:none!important;visibility:hidden!important;opacity:0!important;pointer-events:none!important;}';
  }

  function removeStyle() {
    const style = document.getElementById(styleId);
    if (style) style.remove();
  }

  function cleanupLegacyVideoOverlays() {
    if (state.videoOverlays && typeof state.videoOverlays.forEach === 'function') {
      state.videoOverlays.forEach(function (controls) {
        if (controls && Array.isArray(controls.disposers)) {
          controls.disposers.forEach(function (dispose) {
            try { dispose(); } catch (_) {}
          });
          controls.disposers.length = 0;
        }
      });
      if (typeof state.videoOverlays.clear === 'function') {
        state.videoOverlays.clear();
      }
    }
    state.videoOverlays = null;
    runWithMutationSuppressed(function () {
      document.querySelectorAll('.__videobrowser_video_controls__').forEach(function (overlay) {
        overlay.remove();
      });
    });
  }

  function runWithMutationSuppressed(work) {
    state.suppressMutationWork = true;
    try {
      return work();
    } finally {
      window.setTimeout(function () {
        state.suppressMutationWork = false;
      }, 0);
    }
  }

  function removeAds() {
    if (!state.config.cleanupEnabled || !document.documentElement) return;
    if (shouldSkipGenericCleanup()) {
      injectStyle(false);
      dismissSitePrompts();
      removeSearchResultAds();
      removeConfiguredDomElements();
      return;
    }

    runWithMutationSuppressed(function () {
      injectStyle(true);
      adSelectors.concat(accountSelectors, cleanupSelectors).forEach(function (selector) {
        document.querySelectorAll(selector).forEach(function (element) {
          hideElement(element, 'generic-cleanup');
        });
      });
      removeConfiguredDomElements();
      removeTopAccountBars();
      removeTopNoiseBlocks();
      removeSearchResultAds();
    });
  }

  function removeConfiguredDomElements() {
    externalDomSelectors().forEach(function (selector) {
      querySelectorAllSafe(selector).forEach(function (element) {
        removeElement(element, 'rule-dom-remove');
      });
    });
  }

  function dismissSitePrompts() {
    dismissBilibiliBrowserChoicePrompts();
  }

  function shouldSkipGenericCleanup() {
    return isBilibiliHost() || isSearchProviderResultPage();
  }

  function isSearchProviderResultPage() {
    const host = String(location.hostname || '');
    const path = String(location.pathname || '/').replace(/\/+$/, '') || '/';
    if (!/(\.|^)(baidu|sogou|so|sm)\.com$/i.test(host)) return false;
    return path !== '/';
  }

  function removeSearchResultAds() {
    if (!isSearchProviderResultPage()) return;

    runWithMutationSuppressed(function () {
      hideKnownSearchAdContainers();
      findSearchAdDisclosureMarkers().forEach(function (marker) {
        const root = findSearchResultRoot(marker);
        if (root) hideElement(root, 'search-result-ad');
      });
    });
  }

  function hideKnownSearchAdContainers() {
    const selectors = [
      '[tpl^="ad"]',
      '[tpl*="-ad"]',
      '[tpl*="_ad"]',
      '[tpl*="adv"]',
      '[data-tuiguang]',
      '[data-log*="-ad"]',
      '[data-log*="_ad"]',
      '[class*="ad-result"]',
      '[class*="ec-ad"]',
      '[class*="ec_ad"]',
      '[class*="ec-tuiguang"]',
      '[class*="ec_tuiguang"]',
      '[class*="wise-ad"]',
      '[class*="wise_ad"]'
    ];

    selectors.forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (element) {
        const root = findSearchResultRoot(element) || element;
        hideElement(root, 'search-result-ad-container');
      });
    });
  }

  function findSearchAdDisclosureMarkers() {
    const markers = [];
    document.querySelectorAll(
      'span,i,em,b,a,button,[role="button"],[aria-label],[title],[class*="ad"],[class*="adv"]'
    ).forEach(function (element) {
      const text = normalizeText(
        element.innerText ||
        element.textContent ||
        element.getAttribute('aria-label') ||
        element.getAttribute('title')
      );
      const descriptor = String(element.id || '') + ' ' + String(element.className || '') + ' ' +
        String(element.getAttribute('aria-label') || '') + ' ' +
        String(element.getAttribute('title') || '');
      if (isSearchAdDisclosure(text, descriptor)) markers.push(element);
    });
    return markers;
  }

  function isSearchAdDisclosure(text, descriptor) {
    const compactText = String(text || '').replace(/\s+/g, '');
    const compactDescriptor = String(descriptor || '').replace(/\s+/g, '');
    if (/^(广告|廣告|推广|推廣|商业推广|商業推廣|赞助|贊助|sponsored|ad)$/i.test(compactText)) {
      return true;
    }
    if (/^(广告|廣告|推广|推廣|赞助|贊助)[:：]?$/.test(compactText)) {
      return true;
    }
    if (compactText.length <= 10 && /广告|廣告|推广|推廣|赞助|贊助|sponsored/i.test(compactText)) {
      return true;
    }
    return /(^|[-_\s])(ad|ads|adv|sponsored|tuiguang|promotion)([-_\s]|$)/i.test(compactDescriptor) &&
      compactText.length <= 24;
  }

  function findSearchResultRoot(marker) {
    if (!marker || !document.body) return null;

    let current = marker;
    let candidate = null;
    for (let depth = 0; current && depth < 9; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (isSearchControlContainer(current)) break;

      const rect = current.getBoundingClientRect();
      if (!rect.width || !rect.height) continue;
      if (rect.width < window.innerWidth * 0.45) continue;
      if (rect.height < 36) continue;
      if (rect.height > Math.min(window.innerHeight * 0.72, 520)) continue;

      const text = normalizeText(current.innerText || current.textContent);
      if (text.length < 2 || text.length > 1200) continue;
      const hasContent = current.querySelectorAll('a,img,h1,h2,h3,[role="heading"]').length > 0 ||
        text.length >= 8;
      if (hasContent) candidate = current;
    }

    return candidate;
  }

  function isSearchControlContainer(element) {
    if (!element || typeof element.querySelector !== 'function') return false;
    if (element.querySelector('input,textarea,select,form')) return true;

    const rect = element.getBoundingClientRect();
    const text = normalizeText(element.innerText || element.textContent);
    const descriptor = String(element.id || '') + ' ' + String(element.className || '');
    const topChromeLike = rect.top < 120 &&
      /综合|资讯|视频|图片|知道|文库|贴吧|地图|更多|搜索|百度一下|网页|问答/.test(text);
    return topChromeLike || /searchbox|search-box|searchbar|search-bar|tab|tabs|navbar|nav-bar/i.test(descriptor);
  }

  function dismissBilibiliBrowserChoicePrompts() {
    if (!isBilibiliHost() || !document.body) return;

    const pageText = normalizeText(document.body.innerText || document.body.textContent);
    const hasBrowserChoiceTitle = /浏览方式|browse mode/i.test(pageText) &&
      /推荐使用|recommended/i.test(pageText);
    if (!hasBrowserChoiceTitle || !/哔哩哔哩|bilibili|b站/i.test(pageText)) return;

    const candidates = document.querySelectorAll(
      'div,section,aside,[role="dialog"],[class*="dialog"],[class*="modal"],' +
      '[class*="popup"],[class*="mask"],[class*="overlay"],[class*="sheet"]'
    );
    candidates.forEach(function (element) {
      if (String(element.id || '').toLowerCase() === 'app') return;
      if (isBilibiliContentContainer(element)) return;

      const text = normalizeText(element.innerText || element.textContent);
      if (!(/浏览方式|browse mode/i.test(text) && /推荐使用|recommended/i.test(text))) return;
      if (!/哔哩哔哩|bilibili|b站/i.test(text)) return;

      const root = findBilibiliPromptRoot(element);
      if (!root) return;
      hideElement(root);
      hideBilibiliPromptBackdrops(root);
      document.documentElement.style.overflow = '';
      if (document.body) document.body.style.overflow = '';
    });
  }

  function normalizeText(value) {
    return String(value || '').replace(/\s+/g, ' ').trim();
  }

  function findBilibiliPromptRoot(element) {
    let current = element;
    for (let depth = 0; current && depth < 8; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (isBilibiliContentContainer(current)) break;
      const rect = current.getBoundingClientRect();
      if (!rect.width || !rect.height) continue;

      const style = getComputedStyle(current);
      const positioned = /fixed|absolute|sticky/i.test(style.position);
      const bottomSheetLike = rect.width >= window.innerWidth * 0.82 &&
        rect.height >= 96 &&
        rect.height <= window.innerHeight * 0.72 &&
        rect.bottom >= window.innerHeight - 6 &&
        rect.top >= window.innerHeight * 0.25;
      const fullOverlayLike = positioned &&
        rect.width >= window.innerWidth * 0.94 &&
        rect.height >= window.innerHeight * 0.82 &&
        String(current.id || '').toLowerCase() !== 'app';
      if (bottomSheetLike || fullOverlayLike) return current;
    }
    return null;
  }

  function isBilibiliContentContainer(element) {
    const descriptor = (String(element.id || '') + ' ' + String(element.className || '')).toLowerCase();
    return /\bm-home\b|\bm-video\b|video-normal|player|recommend|feed-list|video-list|v-card/.test(descriptor);
  }

  function hideBilibiliPromptBackdrops(promptRoot) {
    document.querySelectorAll('body *').forEach(function (element) {
      if (!element || element === promptRoot || element.contains(promptRoot) || promptRoot.contains(element)) {
        return;
      }
      const rect = element.getBoundingClientRect();
      if (!rect.width || !rect.height) return;

      const style = getComputedStyle(element);
      if (!/fixed|absolute|sticky/i.test(style.position)) return;
      const className = String(element.className || '');
      const overlayNameLike = /mask|overlay|modal|popup|dialog|shade|shadow/i.test(className);
      const fullScreenLike = rect.width >= window.innerWidth * 0.94 &&
        rect.height >= window.innerHeight * 0.82 &&
        rect.left <= 4 &&
        rect.top <= 4;
      if (!overlayNameLike && !fullScreenLike) return;

      const text = normalizeText(element.textContent);
      if (text.length > 40) return;
      hideElement(element);
    });
  }

  function hideElement(element, reason) {
    if (!element || element === document.body || element === document.documentElement) return;
    if (String(element.id || '').toLowerCase() === 'app') return;
    element.setAttribute('data-videobrowser-dismissed', reason || 'cleanup');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  }

  function removeElement(element, reason) {
    if (!element || element === document.body || element === document.documentElement) return;
    if (String(element.id || '').toLowerCase() === 'app') return;
    element.setAttribute('data-videobrowser-dismissed', reason || 'remove');
    element.remove();
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
      if (accountLike || iconBarLike) hideElement(element, 'top-account-bar');
    });
  }

  function removeTopNoiseBlocks() {
    if (!isSearchProviderHomePage()) return;

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

      if (!brandLogoLike && (adLike || sparseTopSlot)) hideElement(element, 'top-noise-block');
    });
  }

  function isSearchProviderHomePage() {
    const host = String(location.hostname || '');
    const path = String(location.pathname || '/').replace(/\/+$/, '') || '/';
    if (!/(\.|^)(baidu|sogou|so|sm)\.com$/i.test(host)) return false;
    return path === '/';
  }

  function enhanceVideos() {
    document.querySelectorAll('video').forEach(function (video) {
      enableNativeVideoControls(video);
      installVideoFullscreenHooks(video);
      installPlaybackSpeedHooks(video);
      applyVideoSpeed(video);
    });
  }

  function enableNativeVideoControls(video) {
    if (!video.controls) video.controls = true;
    if (video.getAttribute('controls') !== 'controls') {
      video.setAttribute('controls', 'controls');
    }
    if (video.hasAttribute('playsinline')) video.removeAttribute('playsinline');
    if (video.hasAttribute('webkit-playsinline')) video.removeAttribute('webkit-playsinline');
    if (video.style.maxWidth !== '100%') video.style.maxWidth = '100%';
  }

  function installVideoFullscreenHooks(video) {
    if (!video || state.fullscreenHookedVideos.has(video)) return;
    state.fullscreenHookedVideos.add(video);
    const timelineReporter = function () {
      if (isVideoFullscreen(video) || state.nativeFullscreenVideo === video) {
        reportPlaybackTimeline(video);
      }
    };
    video.addEventListener('webkitbeginfullscreen', function () {
      state.nativeFullscreenVideo = video;
      applyVideoSpeed(video);
      reportPlaybackTimeline(video);
      enterNativeFullscreen();
    });
    video.addEventListener('webkitendfullscreen', function () {
      stopDirectionalPlayback();
      state.nativeFullscreenVideo = null;
      state.fullscreenPlaybackSpeed = 1;
      applyVideoSpeed(video);
      exitNativeFullscreen();
    });
    video.addEventListener('dblclick', function () {
      requestVideoFullscreen(video);
    });
    ['loadedmetadata', 'durationchange', 'timeupdate', 'seeked', 'play', 'playing'].forEach(function (eventName) {
      video.addEventListener(eventName, timelineReporter);
    });
  }

  function installPlaybackSpeedHooks(video) {
    if (!video || state.speedHookedVideos.has(video)) return;
    state.speedHookedVideos.add(video);
    const enforceSpeed = function () {
      window.setTimeout(function () {
        applyVideoSpeed(video);
      }, 0);
    };
    ['loadedmetadata', 'play', 'playing', 'ratechange', 'webkitbeginfullscreen'].forEach(function (eventName) {
      video.addEventListener(eventName, enforceSpeed);
    });
  }

  function desiredVideoSpeed(video) {
    const speed = currentFullscreenPlaybackSpeed();
    const activeFullscreen = video && (isVideoFullscreen(video) || state.nativeFullscreenVideo === video);
    if (!state.config.videoEnabled || !activeFullscreen) return 1;
    return Number.isFinite(speed) && speed > 0 ? speed : 1;
  }

  function currentFullscreenPlaybackSpeed() {
    const speed = Number(state.fullscreenPlaybackSpeed || 1);
    return Number.isFinite(speed) && speed > 0 ? speed : 1;
  }

  function applyVideoSpeed(video) {
    const speed = desiredVideoSpeed(video);
    try {
      if (Math.abs(Number(video.playbackRate || 1) - speed) > 0.01) {
        video.playbackRate = speed;
      }
      video.defaultPlaybackRate = speed;
    } catch (_) {}
  }

  function enableVideoControls(video) {
    enableNativeVideoControls(video);
  }

  function wakeVideoControls(video) {
    const target = video || activeFullscreenVideo();
    if (!target) return false;

    enableNativeVideoControls(target);
    reportPlaybackTimeline(target);

    const fullscreenElement = document.fullscreenElement || document.webkitFullscreenElement;
    const root = fullscreenElement || target.parentElement || target;
    const rect = root && typeof root.getBoundingClientRect === 'function'
      ? root.getBoundingClientRect()
      : null;
    const clientX = rect && Number.isFinite(rect.left + rect.width / 2)
      ? rect.left + rect.width / 2
      : Math.max(1, window.innerWidth / 2);
    const clientY = rect && Number.isFinite(rect.top + rect.height / 2)
      ? rect.top + rect.height / 2
      : Math.max(1, window.innerHeight / 2);

    dispatchControlWakeEvent(root, 'mousemove', clientX, clientY);
    dispatchControlWakeEvent(target, 'mousemove', clientX, clientY);
    dispatchPointerWakeEvent(root, clientX, clientY);
    dispatchPointerWakeEvent(target, clientX, clientY);
    try { if (typeof target.focus === 'function') target.focus({ preventScroll: true }); } catch (_) {}
    return true;
  }

  function dispatchControlWakeEvent(target, type, clientX, clientY) {
    if (!target || typeof target.dispatchEvent !== 'function') return;
    try {
      target.dispatchEvent(new MouseEvent(type, {
        bubbles: true,
        cancelable: true,
        view: window,
        clientX: clientX,
        clientY: clientY
      }));
    } catch (_) {}
  }

  function dispatchPointerWakeEvent(target, clientX, clientY) {
    if (!target || typeof target.dispatchEvent !== 'function' || typeof PointerEvent !== 'function') {
      return;
    }
    try {
      target.dispatchEvent(new PointerEvent('pointermove', {
        bubbles: true,
        cancelable: true,
        view: window,
        pointerType: 'touch',
        clientX: clientX,
        clientY: clientY
      }));
    } catch (_) {}
  }

  function videoTimeline(video) {
    const duration = video.duration;
    if (Number.isFinite(duration) && duration > 0) {
      return { canSeek: true, start: 0, end: duration };
    }

    const seekable = video.seekable;
    if (!seekable || !seekable.length) {
      return { canSeek: false, start: 0, end: 0 };
    }

    const start = seekable.start(0);
    const end = seekable.end(seekable.length - 1);
    if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
      return { canSeek: false, start: 0, end: 0 };
    }
    return { canSeek: true, start: start, end: end };
  }

  function reportPlaybackTimeline(video) {
    const target = video || activeFullscreenVideo();
    if (!target) return;

    const timeline = videoTimeline(target);
    const position = Number(target.currentTime || 0);
    const duration = timeline.canSeek ? timeline.end : Number(target.duration || -1);
    const bridge = window.VideoBrowserNative;
    if (bridge && typeof bridge.updatePlaybackTimeline === 'function') {
      try {
        bridge.updatePlaybackTimeline(
          Number.isFinite(position) && position >= 0 ? position * 1000 : -1,
          Number.isFinite(duration) && duration > 0 ? duration * 1000 : -1
        );
      } catch (_) {}
    }
  }

  function isBilibiliHost() {
    return /(\.|^)bilibili\.com$/i.test(location.hostname);
  }

  function seekVideo(video, sliderValue) {
    const timeline = videoTimeline(video);
    if (!timeline.canSeek) return;
    const ratio = Math.max(0, Math.min(1, Number(sliderValue) / 1000));
    const targetTime = timeline.start + ratio * (timeline.end - timeline.start);
    try {
      if (typeof video.fastSeek === 'function') {
        video.fastSeek(targetTime);
      } else {
        video.currentTime = targetTime;
      }
    } catch (_) {
      try { video.currentTime = targetTime; } catch (__) {}
    }
    reportPlaybackTimeline(video);
  }

  function seekVideoTo(video, targetSeconds) {
    if (!video || !Number.isFinite(targetSeconds)) return;
    const timeline = videoTimeline(video);
    let targetTime = targetSeconds;
    if (timeline.canSeek) {
      targetTime = Math.max(timeline.start, Math.min(timeline.end, targetTime));
    } else {
      targetTime = Math.max(0, targetTime);
    }
    try {
      if (typeof video.fastSeek === 'function') {
        video.fastSeek(targetTime);
      } else {
        video.currentTime = targetTime;
      }
    } catch (_) {
      try { video.currentTime = targetTime; } catch (__) {}
    }
    reportPlaybackTimeline(video);
  }

  function seekVideoBy(video, offsetSeconds) {
    if (!video || !Number.isFinite(offsetSeconds)) return;
    const timeline = videoTimeline(video);
    let targetTime = Number(video.currentTime || 0) + offsetSeconds;
    if (timeline.canSeek) {
      targetTime = Math.max(timeline.start, Math.min(timeline.end, targetTime));
    } else {
      targetTime = Math.max(0, targetTime);
    }
    try {
      if (typeof video.fastSeek === 'function') {
        video.fastSeek(targetTime);
      } else {
        video.currentTime = targetTime;
      }
    } catch (_) {
      try { video.currentTime = targetTime; } catch (__) {}
    }
    reportPlaybackTimeline(video);
  }

  function activeFullscreenVideo() {
    if (state.nativeFullscreenVideo && state.nativeFullscreenVideo.isConnected) {
      return state.nativeFullscreenVideo;
    }
    const fullscreenElement = document.fullscreenElement || document.webkitFullscreenElement;
    if (fullscreenElement) {
      if (fullscreenElement.tagName && fullscreenElement.tagName.toLowerCase() === 'video') {
        return fullscreenElement;
      }
      if (typeof fullscreenElement.querySelector === 'function') {
        const video = fullscreenElement.querySelector('video');
        if (video) return video;
      }
    }
    const videos = Array.prototype.slice.call(document.querySelectorAll('video'));
    return videos.find(function (video) {
      return video && video.isConnected && !video.paused && !video.ended;
    }) || videos[0] || null;
  }

  function requestVideoFullscreen(video) {
    if (isVideoFullscreen(video)) {
      exitVideoFullscreen();
      return;
    }

    reportPlaybackTimeline(video);

    const target = video.parentElement || video;
    const requesters = [
      { element: target, request: target.requestFullscreen || target.webkitRequestFullscreen },
      { element: video, request: video.requestFullscreen || video.webkitRequestFullscreen || video.webkitEnterFullscreen }
    ];
    const requested = requesters.some(function (requester) {
      if (typeof requester.request !== 'function') return false;
      try {
        requester.request.call(requester.element);
        return true;
      } catch (_) {
        return false;
      }
    });
    if (requested) {
      enterNativeFullscreen();
    }
  }

  function exitVideoFullscreen() {
    stopDirectionalPlayback();
    state.nativeFullscreenVideo = null;
    state.fullscreenPlaybackSpeed = 1;
    document.querySelectorAll('video').forEach(applyVideoSpeed);
    if (document.fullscreenElement && typeof document.exitFullscreen === 'function') {
      try { document.exitFullscreen(); } catch (_) {}
    } else if (document.webkitFullscreenElement && typeof document.webkitExitFullscreen === 'function') {
      try { document.webkitExitFullscreen(); } catch (__) {}
    }
    exitNativeFullscreen();
  }

  function isVideoFullscreen(video) {
    return video && (
      document.fullscreenElement === video ||
      document.fullscreenElement === video.parentElement ||
      document.webkitFullscreenElement === video ||
      document.webkitFullscreenElement === video.parentElement
    );
  }

  function enterNativeFullscreen() {
    const bridge = window.VideoBrowserNative;
    if (bridge && typeof bridge.enterFullscreen === 'function') {
      try { bridge.enterFullscreen(); } catch (_) {}
    }
  }

  function exitNativeFullscreen() {
    const bridge = window.VideoBrowserNative;
    if (bridge && typeof bridge.exitFullscreen === 'function') {
      try { bridge.exitFullscreen(); } catch (_) {}
    }
  }

  function formatTime(value) {
    if (!Number.isFinite(value) || value < 0) return '00:00';
    const total = Math.floor(value);
    const minutes = Math.floor(total / 60);
    const seconds = total % 60;
    return String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
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

  function hasActiveVideo() {
    return Array.prototype.some.call(document.querySelectorAll('video'), function (video) {
      return video && video.isConnected && !video.paused && !video.ended && video.readyState > 1;
    });
  }

  function installFullscreenEventHooks() {
    document.addEventListener('fullscreenchange', syncDocumentFullscreenState);
    document.addEventListener('webkitfullscreenchange', syncDocumentFullscreenState);
    window.addEventListener('pagehide', function () {
      suspendPageFeatures({ pauseVideos: true });
    });
    window.addEventListener('pageshow', function () {
      state.disposed = false;
      startWorkers();
      schedulePageWork();
    });
  }

  function syncDocumentFullscreenState() {
    const hasDocumentFullscreen = Boolean(document.fullscreenElement || document.webkitFullscreenElement);
    if (hasDocumentFullscreen) {
      state.nativeFullscreenVideo = activeFullscreenVideo();
      reportPlaybackTimeline(state.nativeFullscreenVideo);
      document.querySelectorAll('video').forEach(applyVideoSpeed);
      enterNativeFullscreen();
      return;
    }

    stopDirectionalPlayback();
    state.nativeFullscreenVideo = null;
    state.fullscreenPlaybackSpeed = 1;
    document.querySelectorAll('video').forEach(applyVideoSpeed);
    exitNativeFullscreen();
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

    installFullscreenEventHooks();
  }

  function runPageWork() {
    state.pendingWork = false;
    if (state.disposed) return;

    const now = Date.now();
    const cleanupInterval = hasActiveVideo() ? activeVideoCleanupIntervalMs : normalCleanupIntervalMs;
    state.lastWorkAt = now;
    if (state.config.cleanupEnabled) {
      if (now - Number(state.lastCleanupAt || 0) >= cleanupInterval) {
        state.lastCleanupAt = now;
        removeAds();
      }
    } else {
      removeStyle();
    }
    dismissSitePrompts();
    clickSkipButtons();
    enhanceVideos();
  }

  function schedulePageWork() {
    if (state.disposed || state.pendingWork) return;

    const elapsed = Date.now() - Number(state.lastWorkAt || 0);
    const workDelay = hasActiveVideo() ? activeVideoWorkDelayMs : normalWorkDelayMs;
    const delay = Math.max(60, workDelay - elapsed);
    state.pendingWork = true;
    window.setTimeout(runPageWork, delay);
  }

  function pausePageVideos() {
    document.querySelectorAll('video').forEach(function (video) {
      try { video.pause(); } catch (_) {}
    });
  }

  function togglePlayPause() {
    const video = activeFullscreenVideo();
    if (!video) return false;
    if (video.paused || video.ended) {
      try {
        if (video.ended) video.currentTime = 0;
      } catch (_) {}
      try { video.play().catch(function () {}); } catch (__) {}
      return true;
    }
    try { video.pause(); } catch (_) {}
    return false;
  }

  function startDirectionalPlayback(direction) {
    const video = activeFullscreenVideo();
    if (!video) return;
    stopDirectionalPlayback();

    const normalizedDirection = Number(direction) < 0 ? -1 : 1;
    const previousSpeed = currentFullscreenPlaybackSpeed();
    state.directionalPlayback = {
      video: video,
      direction: normalizedDirection,
      previousSpeed: previousSpeed,
      wasPaused: Boolean(video.paused),
      intervalId: null
    };

    if (normalizedDirection > 0) {
      state.fullscreenPlaybackSpeed = 2;
      applyVideoSpeed(video);
      try { video.play().catch(function () {}); } catch (_) {}
      return;
    }

    try { video.pause(); } catch (_) {}
    seekVideoBy(video, -0.5);
    state.directionalPlayback.intervalId = window.setInterval(function () {
      const scan = state.directionalPlayback;
      if (!scan || scan.direction >= 0 || !scan.video || !scan.video.isConnected) {
        stopDirectionalPlayback();
        return;
      }
      seekVideoBy(scan.video, -0.5);
    }, 250);
  }

  function stopDirectionalPlayback() {
    const scan = state.directionalPlayback;
    if (!scan) return;
    if (scan.intervalId) {
      window.clearInterval(scan.intervalId);
    }
    state.directionalPlayback = null;
    state.fullscreenPlaybackSpeed = Number.isFinite(Number(scan.previousSpeed)) && Number(scan.previousSpeed) > 0
      ? Number(scan.previousSpeed)
      : 1;

    const video = scan.video && scan.video.isConnected ? scan.video : activeFullscreenVideo();
    if (video) {
      applyVideoSpeed(video);
      if (scan.wasPaused) {
        try { video.pause(); } catch (_) {}
      } else {
        try { video.play().catch(function () {}); } catch (__) {}
      }
    }
    document.querySelectorAll('video').forEach(applyVideoSpeed);
  }

  function suspendPageFeatures(options) {
    stopDirectionalPlayback();
    if (options && options.pauseVideos) {
      pausePageVideos();
    }
    exitVideoFullscreen();
    cleanupLegacyVideoOverlays();
  }

  function disposePageFeatures(options) {
    suspendPageFeatures(options);
    state.disposed = true;
    state.pendingWork = false;

    if (state.observer) {
      state.observer.disconnect();
      state.observer = null;
    }
    if (state.intervalId) {
      window.clearInterval(state.intervalId);
      state.intervalId = null;
    }
  }

  function startWorkers() {
    if (state.disposed) return;

    if (isBilibiliHost() && state.observer) {
      state.observer.disconnect();
      state.observer = null;
    }

    if (!state.observer && document.documentElement && !isBilibiliHost()) {
      state.observer = new MutationObserver(function () {
        if (state.suppressMutationWork) return;
        schedulePageWork();
      });
      state.observer.observe(document.documentElement, {
        childList: true,
        subtree: true
      });
    }

    if (!state.intervalId) {
      state.intervalId = window.setInterval(function () {
        schedulePageWork();
      }, 1500);
    }
  }

  window.VideoBrowserEnhancer = {
    apply: function (config) {
      state.disposed = false;
      state.config = config || {};
      state.fullscreenPlaybackSpeed = 1;
      cleanupLegacyVideoOverlays();
      installHooks();
      runPageWork();
      startWorkers();
    },
    exitFullscreen: function () {
      exitVideoFullscreen();
    },
    seekBy: function (offsetSeconds) {
      seekVideoBy(activeFullscreenVideo(), Number(offsetSeconds || 0));
    },
    seekTo: function (targetSeconds) {
      seekVideoTo(activeFullscreenVideo(), Number(targetSeconds || 0));
    },
    reportPlaybackTimeline: function () {
      reportPlaybackTimeline(activeFullscreenVideo());
    },
    togglePlayPause: function () {
      return togglePlayPause();
    },
    wakeControls: function () {
      return wakeVideoControls(activeFullscreenVideo());
    },
    setPlaybackSpeed: function (speed) {
      stopDirectionalPlayback();
      const normalizedSpeed = Number(speed || 1);
      state.fullscreenPlaybackSpeed =
        Number.isFinite(normalizedSpeed) && normalizedSpeed > 0 ? normalizedSpeed : 1;
      document.querySelectorAll('video').forEach(applyVideoSpeed);
    },
    startDirectionalPlayback: function (direction) {
      startDirectionalPlayback(direction);
    },
    stopDirectionalPlayback: function () {
      stopDirectionalPlayback();
    },
    suspend: function (options) {
      suspendPageFeatures(options || {});
    },
    dispose: function (options) {
      disposePageFeatures(options || {});
    }
  };
})();
