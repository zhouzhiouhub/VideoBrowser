/*
 * 初学者阅读提示：
 * 这是 bilibili 站点的适配脚本。
 * 它只处理该站点特有的播放器结构、遮挡元素和视频控制桥接，通用逻辑仍在 common.js 中。
 */
(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;
  var siteTools = window.VideoBrowserSiteAdapterTools;
  var state = siteTools.adapterState('__videobrowserBilibiliState');
  var adapterTools = siteTools.scopedAdapterTools('bilibili', {
    protectedIds: ['app'],
    clickSelector: 'button,a,[role="button"],.close,.cancel,.skip'
  });
  var query = adapterTools.query;
  var textOf = adapterTools.textOf;
  var hideSelectors = adapterTools.hideSelectors;
  var clickTextButtons = adapterTools.clickTextButtons;
  var logVideoDiagnostic = adapterTools.logVideoDiagnostic;
  var removeNativeVideoControls = adapterTools.removeNativeVideoControls;
  var overlayCleanup = window.VideoBrowserBilibiliOverlayCleanup || {};
  var browserChoiceCleanup = window.VideoBrowserBilibiliBrowserChoiceCleanup || {};
  var playerApi = window.VideoBrowserBilibiliPlayerApi || {};

  /**
   * 函数 `hideVideoPlayPauseOverlays`：封装 `hide Video Play Pause Overlays` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function hideVideoPlayPauseOverlays() {
    if (typeof overlayCleanup.hideVideoPlayPauseOverlays === 'function') {
      overlayCleanup.hideVideoPlayPauseOverlays(adapterTools);
    }
  }

  function dismissBrowserChoicePrompts() {
    if (typeof browserChoiceCleanup.dismissPrompts === 'function') {
      browserChoiceCleanup.dismissPrompts(adapterTools);
    }
  }

  /**
   * 函数 `playerMethodsFor`：封装 `player Methods For` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function playerMethodsFor(action, video) {
    return typeof playerApi.methodsFor === 'function' ? playerApi.methodsFor(action, video) : [];
  }

  /**
   * 函数 `hasPlayerMethod`：封装 `has Player Method` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function hasPlayerMethod(action, video) {
    return typeof playerApi.hasMethod === 'function' && playerApi.hasMethod(action, video);
  }

  /**
   * 函数 `callPlayerMethod`：封装 `call Player Method` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} methodNames 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} args 表示稍后执行的回调、清理函数或调用参数。
   */
  function callPlayerMethod(methodNames, args) {
    return typeof playerApi.call === 'function' ? playerApi.call(methodNames, args) : null;
  }

  /**
   * 函数 `readPlayerMethod`：封装 `read Player Method` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} methodNames 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function readPlayerMethod(methodNames) {
    return typeof playerApi.read === 'function' ? playerApi.read(methodNames) : null;
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param candidate 表示当前回调正在检查或操作的页面元素。
     */
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param root 表示当前回调正在检查或操作的页面元素。
     */
    query('[class*="quality"],[class*="Quality"],[aria-label*="\u753b\u8d28"],[title*="\u753b\u8d28"]').forEach(function (root) {
      if (!root || root.querySelector('video')) return;
      var elements = Array.prototype.slice.call(root.querySelectorAll(
        'button,a,li,span,div,[role="button"],[role="menuitem"]'
      ));
      if (root.matches && root.matches('button,a,li,[role="button"],[role="menuitem"]')) {
        elements.unshift(root);
      }
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param element 表示当前回调正在检查或操作的页面元素。
       */
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param first 表示排序、比较或去重时当前回调收到的位置或比较对象。
     * @param second 表示排序、比较或去重时当前回调收到的位置或比较对象。
     */
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
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
    return typeof playerApi.handledValue === 'function'
      ? playerApi.handledValue(callResult, fallbackValue)
      : null;
  }

  /**
   * 函数 `currentVideoTime`：封装 `current Video Time` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function currentVideoTime(video) {
    return typeof playerApi.currentVideoTime === 'function'
      ? playerApi.currentVideoTime(video)
      : Number(video && video.currentTime || 0);
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
    dismissBrowserChoicePrompts();
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
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
