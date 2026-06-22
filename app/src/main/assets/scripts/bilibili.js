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
  var hideSelectors = adapterTools.hideSelectors;
  var clickTextButtons = adapterTools.clickTextButtons;
  var removeNativeVideoControls = adapterTools.removeNativeVideoControls;
  var overlayCleanup = window.VideoBrowserBilibiliOverlayCleanup || {};
  var browserChoiceCleanup = window.VideoBrowserBilibiliBrowserChoiceCleanup || {};
  var playerApi = window.VideoBrowserBilibiliPlayerApi || {};
  var qualityTools = window.VideoBrowserBilibiliQualityTools || {};

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

  function preferBestQuality() {
    return typeof qualityTools.preferBestQuality === 'function' &&
      qualityTools.preferBestQuality(adapterTools, playerApi);
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
      return playerApi.handledValue(callPlayerMethod(['seek', 'seekTo', 'setCurrentTime'], [target]), true);
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
      return playerApi.handledValue(callPlayerMethod(['seek', 'seekTo', 'setCurrentTime'], [target]), true);
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
      return playerApi.handledValue(callPlayerMethod(['setPlaybackRate', 'setPlaybackSpeed'], [normalizedSpeed]), true);
    },
    /**
     * 函数 `preferBestQuality`：封装 `prefer Best Quality` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     */
    preferBestQuality: function (video) {
      return preferBestQuality();
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
