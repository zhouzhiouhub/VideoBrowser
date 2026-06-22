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
  var videoCapabilities = window.VideoBrowserBilibiliVideoCapabilities || {};

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
  adapters.bilibili.videoCapabilities = typeof videoCapabilities.create === 'function'
    ? videoCapabilities.create(adapterTools, playerApi, qualityTools, overlayCleanup)
    : {};
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
