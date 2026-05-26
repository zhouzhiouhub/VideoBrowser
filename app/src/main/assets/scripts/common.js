(function () {
  const state = window.__videobrowserState || {
    observer: null,
    intervalId: null,
    hooked: false,
    config: {},
    pendingWork: false,
    lastWorkAt: 0,
    disposed: false
  };
  window.__videobrowserState = state;
  if (!state.videoOverlays || typeof state.videoOverlays.forEach !== 'function') {
    state.videoOverlays = new Map();
  }
  if (!state.fullscreenHookedVideos || typeof state.fullscreenHookedVideos.add !== 'function') {
    state.fullscreenHookedVideos = new WeakSet();
  }
  state.pageFullscreenVideo = state.pageFullscreenVideo || null;
  state.previousDocumentOverflow = state.previousDocumentOverflow || '';

  const styleId = '__videobrowser_css_filter__';
  const fullscreenStyleId = '__videobrowser_video_fullscreen_css__';
  const pageFullscreenClass = '__videobrowser_page_fullscreen_video__';
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
    if (shouldSkipGenericCleanup()) {
      removeStyle();
      dismissSitePrompts();
      return;
    }
    injectStyle();
    adSelectors.concat(accountSelectors, cleanupSelectors).forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (element) {
        if (element && element.parentNode) element.remove();
      });
    });
    removeTopAccountBars();
    removeTopNoiseBlocks();
  }

  function dismissSitePrompts() {
    dismissBilibiliBrowserChoicePrompts();
  }

  function shouldSkipGenericCleanup() {
    return isBilibiliHost();
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
      document.documentElement.style.overflow = state.previousDocumentOverflow || '';
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

  function hideElement(element) {
    if (!element || element === document.body || element === document.documentElement) return;
    if (String(element.id || '').toLowerCase() === 'app') return;
    element.setAttribute('data-videobrowser-dismissed', 'bilibili-browser-choice');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
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
    const speed = state.config.videoEnabled ? Number(state.config.videoSpeed || 1) : 1;
    document.querySelectorAll('video').forEach(function (video) {
      enableNativeVideoControls(video);
      installVideoFullscreenHooks(video);
      if (Number.isFinite(speed) && speed > 0 && video.playbackRate !== speed) {
        video.playbackRate = speed;
      }
      video.defaultPlaybackRate = speed;
    });
  }

  function enableNativeVideoControls(video) {
    video.controls = true;
    video.setAttribute('controls', 'controls');
    video.removeAttribute('playsinline');
    video.removeAttribute('webkit-playsinline');
    video.style.maxWidth = '100%';
  }

  function installVideoFullscreenHooks(video) {
    if (!video || state.fullscreenHookedVideos.has(video)) return;
    state.fullscreenHookedVideos.add(video);
    video.addEventListener('webkitbeginfullscreen', enterNativeFullscreen);
    video.addEventListener('webkitendfullscreen', exitNativeFullscreen);
    video.addEventListener('dblclick', function () {
      requestVideoFullscreen(video);
    });
  }

  function enableVideoControls(video) {
    enableNativeVideoControls(video);
  }

  function addManagedListener(target, eventName, handler, options, disposers) {
    if (!target || typeof target.addEventListener !== 'function') return;
    target.addEventListener(eventName, handler, options);
    disposers.push(function () {
      try {
        target.removeEventListener(eventName, handler, options);
      } catch (_) {}
    });
  }

  function cleanupDisconnectedVideoOverlays() {
    const entries = [];
    state.videoOverlays.forEach(function (controls, video) {
      if (!video || !video.isConnected) entries.push([video, controls]);
    });
    entries.forEach(function (entry) {
      removeVideoOverlay(entry[0], entry[1]);
    });
  }

  function removeVideoOverlay(video, controls) {
    if (!controls || controls.disposed) return;
    controls.disposed = true;
    if (Array.isArray(controls.disposers)) {
      controls.disposers.forEach(function (dispose) {
        try { dispose(); } catch (_) {}
      });
      controls.disposers.length = 0;
    }
    if (controls.overlay) controls.overlay.remove();
    if (video && state.pageFullscreenVideo === video) {
      video.classList.remove(pageFullscreenClass);
      state.pageFullscreenVideo = null;
      document.documentElement.style.overflow = state.previousDocumentOverflow || '';
    }
    if (video) state.videoOverlays.delete(video);
  }

  function installVideoOverlay(video) {
    if (state.videoOverlays.has(video)) {
      updateVideoOverlay(video, state.videoOverlays.get(video));
      return;
    }

    const overlay = document.createElement('div');
    overlay.className = '__videobrowser_video_controls__';
    overlay.style.cssText = [
      'position:fixed',
      'left:0',
      'top:0',
      'z-index:2147483647',
      'display:none',
      'align-items:center',
      'gap:6px',
      'height:38px',
      'padding:5px 7px',
      'box-sizing:border-box',
      'background:rgba(0,0,0,.72)',
      'color:#fff',
      'font:12px sans-serif',
      'pointer-events:auto',
      'touch-action:manipulation'
    ].join(';');

    const play = document.createElement('button');
    play.type = 'button';
    play.textContent = 'Play';
    play.style.cssText = controlButtonStyle();

    const seek = document.createElement('input');
    seek.type = 'range';
    seek.min = '0';
    seek.max = '1000';
    seek.value = '0';
    seek.step = '1';
    seek.style.cssText = 'flex:1;min-width:90px;accent-color:#3d8bfd;touch-action:none';

    const time = document.createElement('span');
    time.textContent = '00:00 / 00:00';
    time.style.cssText = 'white-space:nowrap;min-width:82px;text-align:center';

    const quality = document.createElement('select');
    quality.style.cssText = 'max-width:76px;height:26px;background:#111;color:#fff;border:1px solid rgba(255,255,255,.35);border-radius:4px';

    const full = document.createElement('button');
    full.type = 'button';
    full.textContent = 'Full';
    full.style.cssText = controlButtonStyle();

    overlay.appendChild(play);
    overlay.appendChild(seek);
    overlay.appendChild(time);
    overlay.appendChild(quality);
    overlay.appendChild(full);
    document.documentElement.appendChild(overlay);

    const controls = {
      overlay,
      play,
      seek,
      time,
      quality,
      full,
      seeking: false,
      dragging: false,
      disposed: false,
      disposers: []
    };
    state.videoOverlays.set(video, controls);

    const stopOverlayEvent = function (event) {
      event.stopPropagation();
    };
    ['touchstart', 'touchmove', 'touchend', 'mousedown', 'mousemove', 'mouseup', 'click'].forEach(function (eventName) {
      addManagedListener(overlay, eventName, stopOverlayEvent, null, controls.disposers);
    });

    addManagedListener(play, 'click', function () {
      if (video.paused) {
        video.play().catch(function () {});
      } else {
        video.pause();
      }
    }, null, controls.disposers);

    addManagedListener(seek, 'input', function () {
      controls.seeking = true;
      seekVideo(video, seek.value);
    }, null, controls.disposers);
    addManagedListener(seek, 'change', function () {
      seekVideo(video, seek.value);
      controls.seeking = false;
    }, null, controls.disposers);
    installSeekDragHandlers(video, controls);

    addManagedListener(full, 'click', function () {
      requestVideoFullscreen(video);
    }, null, controls.disposers);

    addManagedListener(quality, 'change', function () {
      switchVideoSource(video, quality.value);
    }, null, controls.disposers);

    const refreshOverlay = function () {
      updateVideoOverlay(video, controls);
    };
    ['loadedmetadata', 'durationchange', 'timeupdate', 'play', 'pause', 'ratechange'].forEach(function (eventName) {
      addManagedListener(video, eventName, refreshOverlay, null, controls.disposers);
    });
    addManagedListener(window, 'scroll', refreshOverlay, true, controls.disposers);
    addManagedListener(window, 'resize', refreshOverlay, null, controls.disposers);

    updateVideoOverlay(video, controls);
  }

  function installSeekDragHandlers(video, controls) {
    function updateFromClientX(clientX) {
      const rect = controls.seek.getBoundingClientRect();
      if (!rect.width) return;
      const ratio = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
      controls.seek.value = String(Math.round(ratio * 1000));
      seekVideo(video, controls.seek.value);
      updateVideoOverlay(video, controls);
    }

    function clientXFromEvent(event) {
      const touch = event.touches && event.touches[0] ||
        event.changedTouches && event.changedTouches[0];
      return touch ? touch.clientX : event.clientX;
    }

    function beginDrag(event) {
      if (controls.seek.disabled) return;
      controls.dragging = true;
      controls.seeking = true;
      updateFromClientX(clientXFromEvent(event));
      if (typeof controls.seek.setPointerCapture === 'function' && event.pointerId != null) {
        try { controls.seek.setPointerCapture(event.pointerId); } catch (_) {}
      }
      event.preventDefault();
      event.stopPropagation();
    }

    function moveDrag(event) {
      if (!controls.dragging) return;
      updateFromClientX(clientXFromEvent(event));
      event.preventDefault();
      event.stopPropagation();
    }

    function endDrag(event) {
      if (!controls.dragging) return;
      updateFromClientX(clientXFromEvent(event));
      controls.dragging = false;
      controls.seeking = false;
      event.preventDefault();
      event.stopPropagation();
    }

    addManagedListener(controls.seek, 'pointerdown', beginDrag, null, controls.disposers);
    addManagedListener(controls.seek, 'pointermove', moveDrag, null, controls.disposers);
    addManagedListener(controls.seek, 'pointerup', endDrag, null, controls.disposers);
    addManagedListener(controls.seek, 'pointercancel', endDrag, null, controls.disposers);
    addManagedListener(controls.seek, 'touchstart', beginDrag, { passive: false }, controls.disposers);
    addManagedListener(controls.seek, 'touchmove', moveDrag, { passive: false }, controls.disposers);
    addManagedListener(controls.seek, 'touchend', endDrag, { passive: false }, controls.disposers);
    addManagedListener(controls.seek, 'touchcancel', endDrag, { passive: false }, controls.disposers);
    addManagedListener(controls.seek, 'mousedown', beginDrag, null, controls.disposers);
    addManagedListener(window, 'mousemove', moveDrag, true, controls.disposers);
    addManagedListener(window, 'mouseup', endDrag, true, controls.disposers);
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

  function isBilibiliHost() {
    return /(\.|^)bilibili\.com$/i.test(location.hostname);
  }

  function controlButtonStyle() {
    return 'height:26px;min-width:42px;padding:0 8px;border:1px solid rgba(255,255,255,.35);border-radius:4px;background:#1d1d1d;color:#fff;font:12px sans-serif';
  }

  function updateVideoOverlay(video, controls) {
    if (!video || !video.isConnected) {
      removeVideoOverlay(video, controls);
      return;
    }
    if (!controls || controls.disposed) return;

    const rect = video.getBoundingClientRect();
    const visible = rect.width >= 120 && rect.height >= 80 && rect.bottom > 0 &&
      rect.right > 0 && rect.top < window.innerHeight && rect.left < window.innerWidth;
    controls.overlay.style.display = visible ? 'flex' : 'none';
    if (!visible) return;

    const overlayTop = isBilibiliHost() ? rect.top + 4 : rect.bottom - 42;
    controls.overlay.style.left = Math.max(0, rect.left) + 'px';
    controls.overlay.style.top = Math.max(0, Math.min(window.innerHeight - 42, overlayTop)) + 'px';
    controls.overlay.style.width = Math.min(rect.width, window.innerWidth - Math.max(0, rect.left)) + 'px';
    controls.play.textContent = video.paused ? 'Play' : 'Pause';
    controls.full.textContent = isVideoFullscreen(video) ? 'Exit' : 'Full';

    const timeline = videoTimeline(video);
    controls.seek.disabled = !timeline.canSeek;
    if (timeline.canSeek && !controls.seeking) {
      controls.seek.value = String(Math.max(0, Math.min(1000, Math.round(
        ((video.currentTime - timeline.start) / (timeline.end - timeline.start)) * 1000
      ))));
    }
    controls.time.textContent = formatTime(video.currentTime) + ' / ' +
      (timeline.canSeek ? formatTime(timeline.end) : '--:--');

    updateQualityOptions(video, controls.quality);
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
  }

  function requestVideoFullscreen(video) {
    if (isVideoFullscreen(video)) {
      exitVideoFullscreen();
      return;
    }

    applyPageVideoFullscreen(video);
    enterNativeFullscreen();

    const target = video.parentElement || video;
    const request = target.requestFullscreen ||
      target.webkitRequestFullscreen ||
      video.requestFullscreen ||
      video.webkitRequestFullscreen ||
      video.webkitEnterFullscreen;
    if (typeof request === 'function') {
      try {
        request.call(target);
      } catch (_) {
        try { request.call(video); } catch (__) {}
      }
    }
  }

  function applyPageVideoFullscreen(video) {
    if (!video) return;
    injectFullscreenStyle();
    if (state.pageFullscreenVideo && state.pageFullscreenVideo !== video) {
      state.pageFullscreenVideo.classList.remove(pageFullscreenClass);
    }
    state.pageFullscreenVideo = video;
    state.previousDocumentOverflow = document.documentElement.style.overflow || '';
    document.documentElement.style.overflow = 'hidden';
    video.classList.add(pageFullscreenClass);
    updateVideoOverlay(video, state.videoOverlays.get(video));
  }

  function exitVideoFullscreen() {
    const video = state.pageFullscreenVideo;
    if (video) {
      video.classList.remove(pageFullscreenClass);
      updateVideoOverlay(video, state.videoOverlays.get(video));
    }
    state.pageFullscreenVideo = null;
    document.documentElement.style.overflow = state.previousDocumentOverflow || '';
    if (document.fullscreenElement && typeof document.exitFullscreen === 'function') {
      try { document.exitFullscreen(); } catch (_) {}
    } else if (document.webkitFullscreenElement && typeof document.webkitExitFullscreen === 'function') {
      try { document.webkitExitFullscreen(); } catch (__) {}
    }
    exitNativeFullscreen();
  }

  function isVideoFullscreen(video) {
    return video && (
      state.pageFullscreenVideo === video ||
      document.fullscreenElement === video ||
      document.fullscreenElement === video.parentElement ||
      document.webkitFullscreenElement === video ||
      document.webkitFullscreenElement === video.parentElement
    );
  }

  function injectFullscreenStyle() {
    if (document.getElementById(fullscreenStyleId)) return;
    const style = document.createElement('style');
    style.id = fullscreenStyleId;
    style.textContent = '.' + pageFullscreenClass + '{' +
      'position:fixed!important;' +
      'left:0!important;' +
      'top:0!important;' +
      'right:auto!important;' +
      'bottom:auto!important;' +
      'width:100vw!important;' +
      'height:100vh!important;' +
      'max-width:none!important;' +
      'max-height:none!important;' +
      'z-index:2147483646!important;' +
      'background:#000!important;' +
      'object-fit:contain!important;' +
      '}';
    document.documentElement.appendChild(style);
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

  function updateQualityOptions(video, select) {
    const sources = availableVideoSources(video);
    select.style.display = sources.length > 1 ? 'block' : 'none';
    if (select.dataset.sourceList === sources.map(function (source) { return source.src; }).join('|')) return;

    select.dataset.sourceList = sources.map(function (source) { return source.src; }).join('|');
    select.innerHTML = '';
    sources.forEach(function (source) {
      const option = document.createElement('option');
      option.value = source.src;
      option.textContent = source.label;
      option.selected = source.src === video.currentSrc || source.src === video.src;
      select.appendChild(option);
    });
  }

  function availableVideoSources(video) {
    const sources = [];
    function addSource(src, label) {
      if (!src || sources.some(function (source) { return source.src === src; })) return;
      sources.push({ src: src, label: label || guessQualityLabel(src) });
    }

    video.querySelectorAll('source[src]').forEach(function (source) {
      addSource(
        source.src,
        source.getAttribute('label') ||
          source.getAttribute('data-quality') ||
          source.getAttribute('res') ||
          source.getAttribute('size')
      );
    });
    addSource(video.currentSrc || video.src, 'Auto');
    return sources;
  }

  function guessQualityLabel(src) {
    const value = String(src || '');
    const match = value.match(/(?:^|[^0-9])(2160|1440|1080|720|480|360|240)p?(?:[^0-9]|$)/i);
    return match ? match[1] + 'p' : 'Auto';
  }

  function switchVideoSource(video, src) {
    if (!src || src === video.currentSrc || src === video.src) return;
    const currentTime = video.currentTime;
    const wasPaused = video.paused;
    const rate = video.playbackRate;
    video.src = src;
    video.load();
    video.addEventListener('loadedmetadata', function restorePlayback() {
      video.removeEventListener('loadedmetadata', restorePlayback);
      if (Number.isFinite(currentTime)) {
        video.currentTime = Math.min(currentTime, Number.isFinite(video.duration) ? video.duration : currentTime);
      }
      video.playbackRate = rate;
      if (!wasPaused) video.play().catch(function () {});
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

  function installFullscreenEventHooks() {
    document.addEventListener('fullscreenchange', syncDocumentFullscreenState);
    document.addEventListener('webkitfullscreenchange', syncDocumentFullscreenState);
    window.addEventListener('pagehide', function () {
      disposePageFeatures({ pauseVideos: true });
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
      enterNativeFullscreen();
      return;
    }

    if (state.pageFullscreenVideo) {
      const video = state.pageFullscreenVideo;
      video.classList.remove(pageFullscreenClass);
      state.pageFullscreenVideo = null;
      document.documentElement.style.overflow = state.previousDocumentOverflow || '';
      updateVideoOverlay(video, state.videoOverlays.get(video));
    }
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

    state.lastWorkAt = Date.now();
    if (state.config.cleanupEnabled) {
      removeAds();
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
    const delay = Math.max(60, 250 - elapsed);
    state.pendingWork = true;
    window.setTimeout(runPageWork, delay);
  }

  function pausePageVideos() {
    document.querySelectorAll('video').forEach(function (video) {
      try { video.pause(); } catch (_) {}
    });
  }

  function disposePageFeatures(options) {
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
    if (options && options.pauseVideos) {
      pausePageVideos();
    }
    exitVideoFullscreen();
    document.querySelectorAll('.__videobrowser_video_controls__').forEach(function (overlay) {
      overlay.remove();
    });
  }

  function startWorkers() {
    if (state.disposed) return;

    if (isBilibiliHost() && state.observer) {
      state.observer.disconnect();
      state.observer = null;
    }

    if (!state.observer && document.documentElement && !isBilibiliHost()) {
      state.observer = new MutationObserver(function () {
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
      installHooks();
      runPageWork();
      startWorkers();
    },
    exitFullscreen: function () {
      exitVideoFullscreen();
    },
    dispose: function (options) {
      disposePageFeatures(options || {});
    }
  };
})();
