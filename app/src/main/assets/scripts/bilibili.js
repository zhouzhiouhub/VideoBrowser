(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;

  var state = window.__videobrowserBilibiliState || {
    intervalId: null,
    config: {}
  };
  window.__videobrowserBilibiliState = state;

  var playbackOverlaySelectors = [
    '.mplayer-play-icon',
    '.mplayer-pause-icon',
    '.mplayer-icon-play',
    '.mplayer-icon-pause',
    '.mplayer-state-play',
    '.mplayer-state-pause',
    '.bpx-player-state-wrap',
    '.bpx-player-state-play',
    '.bpx-player-state-pause',
    '.bilibili-player-video-state',
    '[class*="player-state"]',
    '[class*="state-play"]',
    '[class*="state-pause"]',
    '[class*="mplayer"][class*="play"]',
    '[class*="mplayer"][class*="pause"]'
  ];

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
    if (String(element.id || '').toLowerCase() === 'app') return;
    element.setAttribute('data-videobrowser-site-dismissed', reason || 'bilibili');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  }

  function hideSelectors(selectors) {
    selectors.forEach(function (selector) {
      query(selector).forEach(function (element) {
        hideElement(element, 'bilibili-ad');
      });
    });
  }

  function clickTextButtons(pattern) {
    query('button,a,[role="button"],.close,.cancel,.skip').forEach(function (element) {
      if (pattern.test(textOf(element)) && typeof element.click === 'function') {
        element.click();
      }
    });
  }

  function logVideoDiagnostic(event, details) {
    var bridge = window.VideoBrowserNative;
    var message = 'event=' + event + ' adapter=bilibili host=' + location.hostname + ' ' + (details || '');
    if (bridge && typeof bridge.logVideoEvent === 'function') {
      try {
        bridge.logVideoEvent(message);
        return;
      } catch (_) {}
    }
    try {
      if (window.console && typeof window.console.log === 'function') {
        window.console.log('[VideoBrowserVideo] ' + message);
      }
    } catch (_) {}
  }

  function videoSource(video) {
    return String(video && (video.currentSrc || video.src || video.getAttribute('src')) || '').slice(0, 180);
  }

  function removeNativeVideoControls(video) {
    if (!video) return;
    var hadNativeControls = Boolean(video.controls || video.hasAttribute('controls'));
    try { video.controls = false; } catch (_) {}
    try { video.removeAttribute('controls'); } catch (_) {}
    if (hadNativeControls) {
      logVideoDiagnostic('remove-native-controls', 'src=' + videoSource(video));
    }
  }

  function hideVideoPlayPauseOverlays() {
    var videos = Array.prototype.slice.call(query('video')).filter(function (video) {
      return video && video.isConnected && !video.paused && !video.ended && video.readyState > 1;
    });
    if (!videos.length) return;

    playbackOverlaySelectors.forEach(function (selector) {
      query(selector).forEach(function (element) {
        var video = matchingVideoForOverlay(element, videos);
        if (!video || !isLikelyCenterPlaybackOverlay(element, video)) return;
        hideElement(playbackOverlayRoot(element, video), 'bilibili-video-play-overlay');
      });
    });
  }

  function matchingVideoForOverlay(element, videos) {
    var elementRect = safeRect(element);
    if (!elementRect) return null;

    var bestVideo = null;
    var bestDistance = Infinity;
    videos.forEach(function (video) {
      var videoRect = safeRect(video);
      if (!videoRect) return;
      if (!rectsOverlap(elementRect, expandedRect(videoRect, 16))) return;

      var distance = centerDistance(elementRect, videoRect);
      if (distance < bestDistance) {
        bestDistance = distance;
        bestVideo = video;
      }
    });
    return bestVideo;
  }

  function isLikelyCenterPlaybackOverlay(element, video) {
    if (!element || !video || element.querySelector('video')) return false;

    var descriptor = (String(element.id || '') + ' ' + String(element.className || '')).toLowerCase();
    if (!/(^|[-_\s])(play|pause|state|mplayer|bpx-player-state)([-_\s]|$)|player-state|state-play|state-pause/.test(descriptor)) {
      return false;
    }

    var elementRect = safeRect(element);
    var videoRect = safeRect(video);
    if (!elementRect || !videoRect) return false;
    if (!rectsOverlap(elementRect, expandedRect(videoRect, 16))) return false;

    var centerLimitX = Math.max(72, videoRect.width * 0.28);
    var centerLimitY = Math.max(54, videoRect.height * 0.32);
    var centerAligned =
      Math.abs(rectCenterX(elementRect) - rectCenterX(videoRect)) <= centerLimitX &&
      Math.abs(rectCenterY(elementRect) - rectCenterY(videoRect)) <= centerLimitY;
    if (!centerAligned) return false;

    var compactControl =
      elementRect.width <= Math.max(144, videoRect.width * 0.72) &&
      elementRect.height <= Math.max(144, videoRect.height * 0.72);
    var knownStateLayer = /mplayer|bpx-player-state|player-state|video-state|state-play|state-pause/.test(descriptor);
    return compactControl || knownStateLayer;
  }

  function playbackOverlayRoot(element, video) {
    var root = element;
    for (var depth = 0; depth < 3 && root.parentElement; depth += 1) {
      var parent = root.parentElement;
      if (parent === document.body || parent === document.documentElement) break;
      if (parent.querySelector('video')) break;
      if (!isLikelyCenterPlaybackOverlay(parent, video)) break;
      root = parent;
    }
    return root;
  }

  function safeRect(element) {
    if (!element || typeof element.getBoundingClientRect !== 'function') return null;
    var rect = element.getBoundingClientRect();
    if (!rect || rect.width <= 0 || rect.height <= 0) return null;
    return rect;
  }

  function expandedRect(rect, amount) {
    return {
      left: rect.left - amount,
      right: rect.right + amount,
      top: rect.top - amount,
      bottom: rect.bottom + amount,
      width: rect.width + amount * 2,
      height: rect.height + amount * 2
    };
  }

  function rectsOverlap(first, second) {
    return first.left < second.right &&
      first.right > second.left &&
      first.top < second.bottom &&
      first.bottom > second.top;
  }

  function rectCenterX(rect) {
    return rect.left + rect.width / 2;
  }

  function rectCenterY(rect) {
    return rect.top + rect.height / 2;
  }

  function centerDistance(first, second) {
    var dx = rectCenterX(first) - rectCenterX(second);
    var dy = rectCenterY(first) - rectCenterY(second);
    return Math.sqrt(dx * dx + dy * dy);
  }

  function findBilibiliPlayerApi() {
    var candidates = [
      window.player,
      window.bilibiliPlayer,
      window.__bilibiliPlayer,
      window.__PLAYER__,
      window.$player
    ];

    for (var index = 0; index < candidates.length; index += 1) {
      var candidate = candidates[index];
      if (candidate && typeof candidate === 'object') return candidate;
    }
    return null;
  }

  function playerMethodsFor(action, video) {
    if (action === 'togglePlayPause') {
      return video && (video.paused || video.ended)
        ? ['play']
        : ['pause'];
    }
    if (action === 'seekBy' || action === 'seekTo') {
      return ['seek', 'seekTo', 'setCurrentTime'];
    }
    if (action === 'setPlaybackSpeed') {
      return ['setPlaybackRate', 'setPlaybackSpeed'];
    }
    return [];
  }

  function hasPlayerMethod(action, video) {
    var api = findBilibiliPlayerApi();
    if (!api) return false;
    return playerMethodsFor(action, video).some(function (methodName) {
      return typeof api[methodName] === 'function';
    });
  }

  function callPlayerMethod(methodNames, args) {
    var api = findBilibiliPlayerApi();
    if (!api) return null;

    for (var index = 0; index < methodNames.length; index += 1) {
      var methodName = methodNames[index];
      var method = api[methodName];
      if (typeof method !== 'function') continue;
      try {
        return {
          handled: true,
          value: method.apply(api, args || [])
        };
      } catch (_) {}
    }
    return null;
  }

  function handledValue(callResult, fallbackValue) {
    if (!callResult || !callResult.handled) return null;
    return typeof callResult.value === 'undefined' ? fallbackValue : callResult.value;
  }

  function currentVideoTime(video) {
    var api = findBilibiliPlayerApi();
    if (api) {
      var getterNames = ['getCurrentTime', 'currentTime'];
      for (var index = 0; index < getterNames.length; index += 1) {
        var getter = api[getterNames[index]];
        try {
          var value = typeof getter === 'function' ? getter.call(api) : getter;
          var numericValue = Number(value);
          if (Number.isFinite(numericValue) && numericValue >= 0) return numericValue;
        } catch (_) {}
      }
    }
    return Number(video && video.currentTime || 0);
  }

  function run(config) {
    if (!document.documentElement) return;
    if (config && config.cleanupEnabled) {
      hideSelectors([
        '.ad-report',
        '.banner-card',
        '.m-ad',
        '.open-app-btn',
        '.launch-app-btn',
        '.download-app',
        '[class*="ad-card"]',
        '[class*="ad-floor"]',
        '[class*="adbanner"]',
        '[data-videobrowser-remove]'
      ]);
      clickTextButtons(/(\u5173\u95ed|\u53d6\u6d88|\u7a0d\u540e|\u7ee7\u7eed\u6d4f\u89c8|close|cancel)/i);
    }
    if (config && config.videoEnabled) {
      query('video').forEach(removeNativeVideoControls);
      clickTextButtons(/(\u8df3\u8fc7|\u5173\u95ed|skip|close)/i);
      hideVideoPlayPauseOverlays();
    }
  }

  function startWorker() {
    if (state.intervalId) return;
    state.intervalId = window.setInterval(function () {
      run(state.config || {});
    }, 1800);
  }

  adapters.bilibili = adapters.bilibili || {};
  adapters.bilibili.videoCapabilities = {
    supports: function (video) {
      return Boolean(video && video.isConnected);
    },
    canUse: function (action, video) {
      if (action === 'enableControls') return true;
      return hasPlayerMethod(action, video);
    },
    enableControls: function (video) {
      removeNativeVideoControls(video);
      hideVideoPlayPauseOverlays();
      return Boolean(video);
    },
    togglePlayPause: function (video) {
      var methodNames = playerMethodsFor('togglePlayPause', video);
      var result = callPlayerMethod(methodNames, []);
      if (!result) return null;
      return video && (video.paused || video.ended);
    },
    seekBy: function (video, offsetSeconds) {
      var offset = Number(offsetSeconds);
      if (!Number.isFinite(offset)) return null;
      var target = currentVideoTime(video) + offset;
      return handledValue(callPlayerMethod(['seek', 'seekTo', 'setCurrentTime'], [target]), true);
    },
    seekTo: function (video, targetSeconds) {
      var target = Number(targetSeconds);
      if (!Number.isFinite(target)) return null;
      return handledValue(callPlayerMethod(['seek', 'seekTo', 'setCurrentTime'], [target]), true);
    },
    setPlaybackSpeed: function (video, speed) {
      var normalizedSpeed = Number(speed);
      if (!Number.isFinite(normalizedSpeed) || normalizedSpeed <= 0) return null;
      return handledValue(callPlayerMethod(['setPlaybackRate', 'setPlaybackSpeed'], [normalizedSpeed]), true);
    }
  };
  adapters.bilibili.apply = function (config) {
    this.lastConfig = config || {};
    state.config = this.lastConfig;
    run(state.config);
    startWorker();
  };
})();
