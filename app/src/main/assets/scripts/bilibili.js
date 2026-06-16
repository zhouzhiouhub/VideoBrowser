/*
 * 初学者阅读提示：
 * 这是 bilibili 站点的适配脚本。
 * 它只处理该站点特有的播放器结构、遮挡元素和视频控制桥接，通用逻辑仍在 common.js 中。
 */
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

  /**
   * 函数 `query`：封装 `query` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} selector 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function query(selector) {
    try {
      return document.querySelectorAll(selector);
    } catch (_) {
      return [];
    }
  }

  /**
   * 函数 `textOf`：封装 `text Of` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function textOf(element) {
    return String(
      element.innerText ||
      element.textContent ||
      element.getAttribute('aria-label') ||
      element.getAttribute('title') ||
      ''
    );
  }

  /**
   * 函数 `hideElement`：封装 `hide Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} reason 表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
   */
  function hideElement(element, reason) {
    if (!element || element === document.body || element === document.documentElement) return;
    if (String(element.id || '').toLowerCase() === 'app') return;
    element.setAttribute('data-videobrowser-site-dismissed', reason || 'bilibili');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  }

  /**
   * 函数 `hideSelectors`：封装 `hide Selectors` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} selectors 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function hideSelectors(selectors) {
    selectors.forEach(function (selector) {
      query(selector).forEach(function (element) {
        hideElement(element, 'bilibili-ad');
      });
    });
  }

  /**
   * 函数 `clickTextButtons`：封装 `click Text Buttons` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} pattern 表示函数执行 `pattern` 相关逻辑时需要读取或处理的输入。
   */
  function clickTextButtons(pattern) {
    query('button,a,[role="button"],.close,.cancel,.skip').forEach(function (element) {
      if (pattern.test(textOf(element)) && typeof element.click === 'function') {
        element.click();
      }
    });
  }

  /**
   * 函数 `logVideoDiagnostic`：封装 `log Video Diagnostic` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} event 表示浏览器事件或事件名称，用来区分触发来源。
   * @param {*} details 表示本次脚本运行的配置或上下文数据。
   */
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

  /**
   * 函数 `videoSource`：封装 `video Source` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function videoSource(video) {
    return String(video && (video.currentSrc || video.src || video.getAttribute('src')) || '').slice(0, 180);
  }

  /**
   * 函数 `removeNativeVideoControls`：封装 `remove Native Video Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function removeNativeVideoControls(video) {
    if (!video) return;
    var hadNativeControls = Boolean(video.controls || video.hasAttribute('controls'));
    try { video.controls = false; } catch (_) {}
    try { video.removeAttribute('controls'); } catch (_) {}
    if (hadNativeControls) {
      logVideoDiagnostic('remove-native-controls', 'src=' + videoSource(video));
    }
  }

  /**
   * 函数 `hideVideoPlayPauseOverlays`：封装 `hide Video Play Pause Overlays` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
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

  /**
   * 函数 `matchingVideoForOverlay`：封装 `matching Video For Overlay` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} videos 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `isLikelyCenterPlaybackOverlay`：封装 `is Likely Center Playback Overlay` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `playbackOverlayRoot`：封装 `playback Overlay Root` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `safeRect`：封装 `safe Rect` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function safeRect(element) {
    if (!element || typeof element.getBoundingClientRect !== 'function') return null;
    var rect = element.getBoundingClientRect();
    if (!rect || rect.width <= 0 || rect.height <= 0) return null;
    return rect;
  }

  /**
   * 函数 `expandedRect`：封装 `expanded Rect` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} rect 表示参与几何计算、播放控制或列表定位的数值。
   * @param {*} amount 表示参与几何计算、播放控制或列表定位的数值。
   */
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

  /**
   * 函数 `rectsOverlap`：封装 `rects Overlap` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} first 表示参与几何计算、播放控制或列表定位的数值。
   * @param {*} second 表示参与几何计算、播放控制或列表定位的数值。
   */
  function rectsOverlap(first, second) {
    return first.left < second.right &&
      first.right > second.left &&
      first.top < second.bottom &&
      first.bottom > second.top;
  }

  /**
   * 函数 `rectCenterX`：封装 `rect Center X` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} rect 表示参与几何计算、播放控制或列表定位的数值。
   */
  function rectCenterX(rect) {
    return rect.left + rect.width / 2;
  }

  /**
   * 函数 `rectCenterY`：封装 `rect Center Y` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} rect 表示参与几何计算、播放控制或列表定位的数值。
   */
  function rectCenterY(rect) {
    return rect.top + rect.height / 2;
  }

  /**
   * 函数 `centerDistance`：封装 `center Distance` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} first 表示参与几何计算、播放控制或列表定位的数值。
   * @param {*} second 表示参与几何计算、播放控制或列表定位的数值。
   */
  function centerDistance(first, second) {
    var dx = rectCenterX(first) - rectCenterX(second);
    var dy = rectCenterY(first) - rectCenterY(second);
    return Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * 函数 `findBilibiliPlayerApi`：封装 `find Bilibili Player Api` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
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

  /**
   * 函数 `playerMethodsFor`：封装 `player Methods For` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `hasPlayerMethod`：封装 `has Player Method` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function hasPlayerMethod(action, video) {
    var api = findBilibiliPlayerApi();
    if (!api) return false;
    return playerMethodsFor(action, video).some(function (methodName) {
      return typeof api[methodName] === 'function';
    });
  }

  /**
   * 函数 `callPlayerMethod`：封装 `call Player Method` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} methodNames 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} args 表示稍后执行的回调、清理函数或调用参数。
   */
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

  /**
   * 函数 `readPlayerMethod`：封装 `read Player Method` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} methodNames 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function readPlayerMethod(methodNames) {
    var api = findBilibiliPlayerApi();
    if (!api) return null;

    for (var index = 0; index < methodNames.length; index += 1) {
      var methodName = methodNames[index];
      var value = api[methodName];
      if (typeof value === 'undefined' || value === null) continue;
      try {
        var result = typeof value === 'function' ? value.call(api) : value;
        if (typeof result !== 'undefined' && result !== null) return result;
      } catch (_) {}
    }
    return null;
  }

  /**
   * 函数 `qualityValueOf`：封装 `quality Value Of` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} candidate 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function qualityValueOf(candidate) {
    if (typeof candidate === 'number') return candidate;
    if (typeof candidate === 'string') {
      var parsed = parseInt(candidate, 10);
      return Number.isFinite(parsed) ? parsed : null;
    }
    if (!candidate || typeof candidate !== 'object') return null;
    var keys = ['qn', 'quality', 'value', 'id', 'code'];
    for (var index = 0; index < keys.length; index += 1) {
      var value = candidate[keys[index]];
      var numericValue = typeof value === 'number' ? value : parseInt(String(value || ''), 10);
      if (Number.isFinite(numericValue)) return numericValue;
    }
    return null;
  }

  /**
   * 函数 `bestApiQualityValue`：封装 `best Api Quality Value` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function bestApiQualityValue() {
    var qualityList = readPlayerMethod([
      'getSupportedQuality',
      'getSupportedQualities',
      'getAvailableQuality',
      'getAvailableQualities',
      'getQualityList',
      'getVideoQualityList',
      'qualityList'
    ]);
    if (!Array.isArray(qualityList) || !qualityList.length) return null;

    var best = null;
    qualityList.forEach(function (candidate) {
      var value = qualityValueOf(candidate);
      if (!Number.isFinite(value)) return;
      if (best === null || value > best) best = value;
    });
    return best;
  }

  /**
   * 函数 `preferBestQualityByApi`：封装 `prefer Best Quality By Api` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function preferBestQualityByApi() {
    var quality = bestApiQualityValue();
    if (!Number.isFinite(quality)) return null;
    return handledValue(
      callPlayerMethod(['setQuality', 'setVideoQuality', 'switchQuality', 'changeQuality'], [quality]),
      true
    );
  }

  /**
   * 函数 `qualityScore`：封装 `quality Score` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} text 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function qualityScore(text) {
    var value = String(text || '').replace(/\s+/g, ' ').trim();
    if (!value) return 0;
    if (/8k/i.test(value)) return 8000;
    if (/杜比|dolby/i.test(value)) return 7600;
    if (/hdr|真彩/i.test(value)) return 7400;
    if (/4k|2160/i.test(value)) return 7000;
    if (/1080\s*p?\s*\+|1080p?\s*60|60\s*帧/i.test(value)) return 6200;
    if (/1080/i.test(value)) return 6000;
    if (/720|高清/i.test(value)) return 5000;
    if (/480/i.test(value)) return 4000;
    if (/360|流畅/i.test(value)) return 3000;
    return 0;
  }

  /**
   * 函数 `visibleElement`：封装 `visible Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function visibleElement(element) {
    var rect = element && typeof element.getBoundingClientRect === 'function'
      ? element.getBoundingClientRect()
      : null;
    return Boolean(rect && rect.width > 0 && rect.height > 0);
  }

  /**
   * 函数 `clickableQualityElement`：封装 `clickable Quality Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function clickableQualityElement(element) {
    var current = element;
    for (var depth = 0; current && depth < 4; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (current.matches && current.matches('button,a,li,[role="button"],[role="menuitem"]')) {
        return current;
      }
    }
    return element;
  }

  /**
   * 函数 `bestVisibleQualityOption`：封装 `best Visible Quality Option` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function bestVisibleQualityOption() {
    var candidates = [];
    query('[class*="quality"],[class*="Quality"],[aria-label*="\u753b\u8d28"],[title*="\u753b\u8d28"]').forEach(function (root) {
      if (!root || root.querySelector('video')) return;
      var elements = Array.prototype.slice.call(root.querySelectorAll(
        'button,a,li,span,div,[role="button"],[role="menuitem"]'
      ));
      if (root.matches && root.matches('button,a,li,[role="button"],[role="menuitem"]')) {
        elements.unshift(root);
      }
      elements.forEach(function (element) {
        if (!visibleElement(element)) return;
        var text = textOf(element);
        var score = qualityScore(text);
        if (score <= 0) return;
        var clickable = clickableQualityElement(element);
        if (!clickable || typeof clickable.click !== 'function') return;
        candidates.push({
          element: clickable,
          score: score,
          text: text
        });
      });
    });
    candidates.sort(function (first, second) {
      return second.score - first.score;
    });
    return candidates[0] || null;
  }

  /**
   * 函数 `clickQualityMenuControl`：封装 `click Quality Menu Control` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function clickQualityMenuControl() {
    var controls = Array.prototype.slice.call(query(
      '.bpx-player-ctrl-quality,.bilibili-player-video-quality,[class*="quality"],[aria-label*="\u753b\u8d28"],[title*="\u753b\u8d28"]'
    ));
    for (var index = 0; index < controls.length; index += 1) {
      var control = controls[index];
      if (!visibleElement(control) || control.querySelector('video')) continue;
      var text = textOf(control);
      if (!/\u753b\u8d28|quality|清晰|720|1080|4k|8k/i.test(text + ' ' + String(control.className || ''))) continue;
      try {
        control.click();
        return true;
      } catch (_) {}
    }
    return false;
  }

  /**
   * 函数 `preferBestQualityByMenu`：封装 `prefer Best Quality By Menu` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function preferBestQualityByMenu() {
    var option = bestVisibleQualityOption();
    if (option) {
      try {
        option.element.click();
        logVideoDiagnostic('quality-menu-select', 'text=' + option.text);
        return true;
      } catch (_) {
        return false;
      }
    }

    if (!clickQualityMenuControl()) return false;
    window.setTimeout(function () {
      var delayedOption = bestVisibleQualityOption();
      if (!delayedOption) return;
      try {
        delayedOption.element.click();
        logVideoDiagnostic('quality-menu-select', 'text=' + delayedOption.text);
      } catch (_) {}
    }, 0);
    return true;
  }

  /**
   * 函数 `handledValue`：封装 `handled Value` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} callResult 表示函数执行 `callResult` 相关逻辑时需要读取或处理的输入。
   * @param {*} fallbackValue 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function handledValue(callResult, fallbackValue) {
    if (!callResult || !callResult.handled) return null;
    return typeof callResult.value === 'undefined' ? fallbackValue : callResult.value;
  }

  /**
   * 函数 `currentVideoTime`：封装 `current Video Time` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `run`：封装 `run` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} config 表示本次脚本运行的配置或上下文数据。
   */
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

  /**
   * 函数 `startWorker`：封装 `start Worker` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function startWorker() {
    if (state.intervalId) return;
    state.intervalId = window.setInterval(function () {
      run(state.config || {});
    }, 1800);
  }

  adapters.bilibili = adapters.bilibili || {};
  adapters.bilibili.videoCapabilities = {
    /**
     * 函数 `supports`：封装 `supports` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     */
    supports: function (video) {
      return Boolean(video && video.isConnected);
    },
    /**
     * 函数 `canUse`：封装 `can Use` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     */
    canUse: function (action, video) {
      if (action === 'enableControls') return true;
      if (action === 'preferBestQuality') return true;
      return hasPlayerMethod(action, video);
    },
    /**
     * 函数 `enableControls`：封装 `enable Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     */
    enableControls: function (video) {
      removeNativeVideoControls(video);
      hideVideoPlayPauseOverlays();
      return Boolean(video);
    },
    /**
     * 函数 `togglePlayPause`：封装 `toggle Play Pause` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     */
    togglePlayPause: function (video) {
      var methodNames = playerMethodsFor('togglePlayPause', video);
      var result = callPlayerMethod(methodNames, []);
      if (!result) return null;
      return video && (video.paused || video.ended);
    },
    /**
     * 函数 `seekBy`：封装 `seek By` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     * @param {*} offsetSeconds 表示参与几何计算、播放控制或列表定位的数值。
     */
    seekBy: function (video, offsetSeconds) {
      var offset = Number(offsetSeconds);
      if (!Number.isFinite(offset)) return null;
      var target = currentVideoTime(video) + offset;
      return handledValue(callPlayerMethod(['seek', 'seekTo', 'setCurrentTime'], [target]), true);
    },
    /**
     * 函数 `seekTo`：封装 `seek To` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     * @param {*} targetSeconds 表示参与几何计算、播放控制或列表定位的数值。
     */
    seekTo: function (video, targetSeconds) {
      var target = Number(targetSeconds);
      if (!Number.isFinite(target)) return null;
      return handledValue(callPlayerMethod(['seek', 'seekTo', 'setCurrentTime'], [target]), true);
    },
    /**
     * 函数 `setPlaybackSpeed`：封装 `set Playback Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     * @param {*} speed 表示参与几何计算、播放控制或列表定位的数值。
     */
    setPlaybackSpeed: function (video, speed) {
      var normalizedSpeed = Number(speed);
      if (!Number.isFinite(normalizedSpeed) || normalizedSpeed <= 0) return null;
      return handledValue(callPlayerMethod(['setPlaybackRate', 'setPlaybackSpeed'], [normalizedSpeed]), true);
    },
    /**
     * 函数 `preferBestQuality`：封装 `prefer Best Quality` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     */
    preferBestQuality: function (video) {
      return preferBestQualityByApi() || preferBestQualityByMenu();
    }
  };
  /**
   * 函数 `adapters.bilibili.apply`：封装 `apply` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} config 表示本次脚本运行的配置或上下文数据。
   */
  adapters.bilibili.apply = function (config) {
    this.lastConfig = config || {};
    state.config = this.lastConfig;
    run(state.config);
    startWorker();
  };
})();
