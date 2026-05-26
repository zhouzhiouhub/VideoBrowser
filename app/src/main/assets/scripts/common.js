(function () {
  const state = window.__videobrowserState || {
    observer: null,
    intervalId: null,
    hooked: false,
    config: {}
  };
  window.__videobrowserState = state;
  state.videoOverlays = state.videoOverlays || new WeakMap();

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
        enableVideoControls(video);
        video.playbackRate = 1;
        video.defaultPlaybackRate = 1;
      });
      return;
    }
    const speed = Number(state.config.videoSpeed || 1);
    document.querySelectorAll('video').forEach(function (video) {
      enableVideoControls(video);
      if (Number.isFinite(speed) && speed > 0 && video.playbackRate !== speed) {
        video.playbackRate = speed;
      }
      video.defaultPlaybackRate = speed;
    });
  }

  function enableVideoControls(video) {
    video.controls = true;
    video.setAttribute('controls', 'controls');
    video.setAttribute('playsinline', 'playsinline');
    video.setAttribute('webkit-playsinline', 'webkit-playsinline');
    video.style.maxWidth = '100%';
    installVideoOverlay(video);
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
      'touch-action:none'
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
    seek.style.cssText = 'flex:1;min-width:90px;accent-color:#3d8bfd';

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

    const controls = { overlay, play, seek, time, quality, full, seeking: false };
    state.videoOverlays.set(video, controls);

    ['touchstart', 'touchmove', 'touchend', 'mousedown', 'mousemove', 'mouseup', 'click'].forEach(function (eventName) {
      overlay.addEventListener(eventName, function (event) {
        event.stopPropagation();
      }, true);
    });

    play.addEventListener('click', function () {
      if (video.paused) {
        video.play().catch(function () {});
      } else {
        video.pause();
      }
    });

    seek.addEventListener('input', function () {
      controls.seeking = true;
      seekVideo(video, seek.value);
    });
    seek.addEventListener('change', function () {
      seekVideo(video, seek.value);
      controls.seeking = false;
    });

    full.addEventListener('click', function () {
      requestVideoFullscreen(video);
    });

    quality.addEventListener('change', function () {
      switchVideoSource(video, quality.value);
    });

    ['loadedmetadata', 'durationchange', 'timeupdate', 'play', 'pause', 'ratechange'].forEach(function (eventName) {
      video.addEventListener(eventName, function () {
        updateVideoOverlay(video, controls);
      });
    });
    window.addEventListener('scroll', function () {
      updateVideoOverlay(video, controls);
    }, true);
    window.addEventListener('resize', function () {
      updateVideoOverlay(video, controls);
    });

    updateVideoOverlay(video, controls);
  }

  function controlButtonStyle() {
    return 'height:26px;min-width:42px;padding:0 8px;border:1px solid rgba(255,255,255,.35);border-radius:4px;background:#1d1d1d;color:#fff;font:12px sans-serif';
  }

  function updateVideoOverlay(video, controls) {
    if (!video || !video.isConnected) {
      if (controls && controls.overlay) controls.overlay.remove();
      return;
    }

    const rect = video.getBoundingClientRect();
    const visible = rect.width >= 120 && rect.height >= 80 && rect.bottom > 0 &&
      rect.right > 0 && rect.top < window.innerHeight && rect.left < window.innerWidth;
    controls.overlay.style.display = visible ? 'flex' : 'none';
    if (!visible) return;

    controls.overlay.style.left = Math.max(0, rect.left) + 'px';
    controls.overlay.style.top = Math.max(0, rect.bottom - 42) + 'px';
    controls.overlay.style.width = Math.min(rect.width, window.innerWidth - Math.max(0, rect.left)) + 'px';
    controls.play.textContent = video.paused ? 'Play' : 'Pause';

    const duration = video.duration;
    const canSeek = Number.isFinite(duration) && duration > 0;
    controls.seek.disabled = !canSeek;
    if (canSeek && !controls.seeking) {
      controls.seek.value = String(Math.max(0, Math.min(1000, Math.round((video.currentTime / duration) * 1000))));
    }
    controls.time.textContent = formatTime(video.currentTime) + ' / ' + (canSeek ? formatTime(duration) : '--:--');

    updateQualityOptions(video, controls.quality);
  }

  function seekVideo(video, sliderValue) {
    const duration = video.duration;
    if (!Number.isFinite(duration) || duration <= 0) return;
    video.currentTime = (Number(sliderValue) / 1000) * duration;
  }

  function requestVideoFullscreen(video) {
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
