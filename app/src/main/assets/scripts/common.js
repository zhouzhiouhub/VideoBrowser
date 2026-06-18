/*
 * 初学者阅读提示：
 * 这是所有网页都会注入的通用增强脚本。
 * 它负责读取 Kotlin 侧传入的配置，执行页面清理、元素屏蔽、视频手势桥接和诊断日志。
 */
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
  if (!state.bestQualityAttempts || typeof state.bestQualityAttempts.get !== 'function') {
    state.bestQualityAttempts = new WeakMap();
  }
  state.nativeFullscreenVideo = state.nativeFullscreenVideo || null;
  state.documentFullscreenActive = Boolean(state.documentFullscreenActive);
  state.directionalPlayback = state.directionalPlayback || null;
  state.elementPicker = state.elementPicker || null;
  state.fullscreenPlaybackSpeed = Number(state.fullscreenPlaybackSpeed || 1);
  if (!Number.isFinite(state.fullscreenPlaybackSpeed) || state.fullscreenPlaybackSpeed <= 0) {
    state.fullscreenPlaybackSpeed = 1;
  }

  const geometry = window.VideoBrowserGeometry;
  const expandedRect = geometry.expandedRect;
  const rectsOverlap = geometry.rectsOverlap;

  const domTools = window.VideoBrowserDomTools;
  const elementDescriptor = domTools.elementDescriptor;
  const domActions = window.VideoBrowserDomActions;
  const selectorTools = window.VideoBrowserSelectorTools;
  const genericCleanupSelectors = window.VideoBrowserGenericCleanupSelectors;
  const generatedAdCleanup = window.VideoBrowserGeneratedAdCleanup;
  const genericAdOverlayCleanup = window.VideoBrowserGenericAdOverlayCleanup;
  const topPageCleanup = window.VideoBrowserTopPageCleanup;
  const searchResultCleanup = window.VideoBrowserSearchResultCleanup;
  const skipButtonTools = window.VideoBrowserSkipButtonTools;
  const nativeBridge = window.VideoBrowserNativeBridge;
  const videoControlTools = window.VideoBrowserVideoControlTools;
  const videoQueryTools = window.VideoBrowserVideoQueryTools;
  const videoPlaybackTools = window.VideoBrowserVideoPlaybackTools;
  const elementPicker = window.VideoBrowserElementPicker;
  const scriptletHooks = window.VideoBrowserScriptletHooks;
  const styleManager = window.VideoBrowserStyleManager;

  const normalCleanupIntervalMs = 3000;
  const activeVideoCleanupIntervalMs = 15000;
  const bestQualityAttemptIntervalMs = 30000;
  const normalWorkDelayMs = 250;
  const activeVideoWorkDelayMs = 750;
  /**
   * 函数 `externalCssSelectors`：封装 `external Css Selectors` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function externalCssSelectors() {
    return safeSelectorList(state.config && state.config.cssSelectors);
  }

  /**
   * 函数 `userCssSelectors`：封装 `user Css Selectors` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function userCssSelectors() {
    return safeSelectorList(state.config && state.config.userCssSelectors);
  }

  /**
   * 函数 `externalDomSelectors`：封装 `external Dom Selectors` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function externalDomSelectors() {
    return safeSelectorList(state.config && state.config.domSelectors);
  }

  /**
   * 函数 `safeSelectorList`：封装 `safe Selector List` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function safeSelectorList(value) {
    return selectorTools.safeSelectorList(value);
  }

  /**
   * 函数 `isSafeSelector`：封装 `is Safe Selector` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} selector 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function isSafeSelector(selector) {
    return selectorTools.isSafeSelector(selector);
  }

  /**
   * 函数 `querySelectorAllSafe`：封装 `query Selector All Safe` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} selector 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function querySelectorAllSafe(selector) {
    return selectorTools.queryAll(selector);
  }

  /**
   * 函数 `injectStyle`：封装 `inject Style` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} includeGenericSelectors 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   * @param {*} includeRuleSelectors 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function injectStyle(includeGenericSelectors, includeRuleSelectors) {
    const selectors = (includeGenericSelectors ? genericCleanupSelectors.defaultSelectors() : [])
      .concat(includeRuleSelectors ? externalCssSelectors() : [])
      .concat(userCssSelectors());
    styleManager.injectHideRules(selectors);
  }

  /**
   * 函数 `removeStyle`：封装 `remove Style` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeStyle() {
    styleManager.remove();
  }

  /**
   * 函数 `logVideoDiagnostic`：封装 `log Video Diagnostic` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} event 表示浏览器事件或事件名称，用来区分触发来源。
   * @param {*} details 表示本次脚本运行的配置或上下文数据。
   */
  function logVideoDiagnostic(event, details) {
    nativeBridge.logVideoDiagnostic(event, details, {
      includePath: true
    });
  }

  /**
   * 函数 `videoLogDetails`：封装 `video Log Details` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} extra 表示本次脚本运行的配置或上下文数据。
   */
  function videoLogDetails(video, extra) {
    const values = extra && typeof extra === 'object' ? extra : {};
    const className = video && String(video.className || '');
    return Object.assign({
      tag: video && video.tagName ? video.tagName.toLowerCase() : '',
      controls: video ? Boolean(video.controls) : false,
      controlsAttr: video ? video.hasAttribute('controls') : false,
      paused: video ? Boolean(video.paused) : false,
      readyState: video ? Number(video.readyState || 0) : 0,
      className: className,
      src: nativeBridge.videoSource(video)
    }, values);
  }

  /**
   * 函数 `cleanupLegacyVideoOverlays`：封装 `cleanup Legacy Video Overlays` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function cleanupLegacyVideoOverlays() {
    if (state.videoOverlays && typeof state.videoOverlays.forEach === 'function') {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param controls 表示当前回调收到的 `controls` 参数。
       */
      state.videoOverlays.forEach(function (controls) {
        if (controls && Array.isArray(controls.disposers)) {
          /*
           * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
           * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
           * @param dispose 表示当前回调需要执行的函数或请求来源。
           */
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    runWithMutationSuppressed(function () {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param overlay 表示当前回调正在检查或操作的页面元素。
       */
      document.querySelectorAll('.__videobrowser_video_controls__').forEach(function (overlay) {
        overlay.remove();
      });
    });
  }

  /**
   * 函数 `runWithMutationSuppressed`：封装 `run With Mutation Suppressed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} work 表示稍后执行的回调、清理函数或调用参数。
   */
  function runWithMutationSuppressed(work) {
    state.suppressMutationWork = true;
    try {
      return work();
    } finally {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       */
      window.setTimeout(function () {
        state.suppressMutationWork = false;
      }, 0);
    }
  }

  /**
   * 函数 `removeAds`：封装 `remove Ads` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeAds() {
    if (!state.config.cleanupEnabled || !document.documentElement) return;
    if (shouldSkipGenericCleanup()) {
      injectStyle(false, true);
      removeSearchResultAds();
      removeConfiguredDomElements();
      if (!isBilibiliHost()) removeGenericAdOverlays();
      return;
    }

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    runWithMutationSuppressed(function () {
      injectStyle(true, true);
      genericCleanupSelectors.hideDefaultElements();
      removeConfiguredDomElements();
      removeGenericAdOverlays();
      topPageCleanup.removeAccountBars();
      topPageCleanup.removeNoiseBlocks();
      removeSearchResultAds();
    });
  }

  /**
   * 函数 `removeConfiguredDomElements`：封装 `remove Configured Dom Elements` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeConfiguredDomElements() {
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     */
    externalDomSelectors().forEach(function (selector) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param element 表示当前回调正在检查或操作的页面元素。
       */
      querySelectorAllSafe(selector).forEach(function (element) {
        removeElement(element, 'rule-dom-remove');
      });
    });
  }

  /**
   * 函数 `removeGenericAdOverlays`：封装 `remove Generic Ad Overlays` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeGenericAdOverlays() {
    genericAdOverlayCleanup.run(state);
  }

  /**
   * 函数 `shouldSkipGenericCleanup`：封装 `should Skip Generic Cleanup` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function shouldSkipGenericCleanup() {
    return isBilibiliHost() || searchResultCleanup.isResultPage();
  }

  /**
   * 函数 `removeSearchResultAds`：封装 `remove Search Result Ads` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeSearchResultAds() {
    searchResultCleanup.removeAds({
      runWithMutationSuppressed: runWithMutationSuppressed
    });
  }

  /**
   * 函数 `normalizeText`：封装 `normalize Text` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function normalizeText(value) {
    return selectorTools.normalizeText(value);
  }

  /**
   * 函数 `removeElement`：封装 `remove Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} reason 表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
   */
  function removeElement(element, reason) {
    domActions.removeElement(element, {
      reason: reason || 'remove',
      protectAppContainers: true
    });
  }

  /**
   * 函数 `startElementPicker`：启动元素选择器模块。
   */
  function startElementPicker() {
    return elementPicker.start(state);
  }

  /**
   * 函数 `stopElementPicker`：停止元素选择器模块。
   */
  function stopElementPicker() {
    elementPicker.stop(state);
  }

  /**
   * 函数 `enhanceVideos`：封装 `enhance Videos` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function enhanceVideos() {
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param video 表示当前回调正在检查或操作的页面元素。
     */
    videoQueryTools.forEach(function (video) {
      if (state.config.videoEnabled || state.config.scriptletVideoControlsEnabled) {
        enableVideoControls(video);
      }
      if (!state.config.videoEnabled) return;
      preferBestVideoQuality(video);
      installVideoFullscreenHooks(video);
      installPlaybackSpeedHooks(video);
      applyVideoSpeed(video);
    });
  }

  const customPlayerControlSelectors = [
    '.xgplayer-controls',
    '.xgplayer-progress',
    '.xgplayer-start',
    '.dplayer-controller',
    '.dplayer-icons',
    '.art-controls',
    '.art-control',
    '.vjs-control-bar',
    '.jw-controls',
    '.plyr__controls',
    '.ckplayer-control',
    '.ckplayer-controls',
    '.prism-controlbar',
    '.mejs__controls',
    '[class*="player-control"]',
    '[class*="player_control"]',
    '[class*="video-control"]',
    '[class*="video_control"]',
    '[class*="control-bar"]',
    '[class*="controlbar"]'
  ];

  /**
   * 函数 `enableNativeVideoControls`：封装 `enable Native Video Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function enableNativeVideoControls(video) {
    videoControlTools.enableNativeControls(video);
  }

  /**
   * 函数 `removeNativeVideoControls`：封装 `remove Native Video Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} reason 表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
   */
  function removeNativeVideoControls(video, reason) {
    const hadNativeControls = videoControlTools.removeNativeControls(video);
    if (hadNativeControls) {
      logVideoDiagnostic('remove-native-controls-generic', videoLogDetails(video, {
        reason: reason || 'custom-player'
      }));
    }
    return hadNativeControls;
  }

  /**
   * 函数 `hasLikelyCustomPlayerControls`：封装 `has Likely Custom Player Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function hasLikelyCustomPlayerControls(video) {
    const root = customPlayerRootFor(video);
    if (!root) return false;
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     */
    const controls = customPlayerControlSelectors.some(function (selector) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param element 表示当前回调正在检查或操作的页面元素。
       */
      return querySelectorAllSafeWithin(root, selector).some(function (element) {
        return isLikelyCustomControlElement(element, video);
      });
    });
    if (controls) return true;

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
    return querySelectorAllSafeWithin(root, 'button,[role="button"],input[type="range"]').some(function (element) {
      return isLikelyMediaControlElement(element, video);
    });
  }

  /**
   * 函数 `customPlayerRootFor`：封装 `custom Player Root For` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function customPlayerRootFor(video) {
    if (!video || !video.isConnected) return null;
    let current = video.parentElement || video;
    for (let depth = 0; current && depth < 8; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (!current.contains(video)) continue;
      if (isLikelyCustomPlayerRoot(current)) return current;
    }
    return video.parentElement || video;
  }

  /**
   * 函数 `isLikelyCustomPlayerRoot`：封装 `is Likely Custom Player Root` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isLikelyCustomPlayerRoot(element) {
    const descriptor = elementDescriptor(element).toLowerCase();
    return /xgplayer|dplayer|artplayer|jwplayer|video-js|vjs-|plyr|ckplayer|prism-player|mejs|hls-player|video-player|player-container|player-wrap|player_box|playerbox|video-container|video-wrap|video_box|videobox/i
      .test(descriptor);
  }

  /**
   * 函数 `querySelectorAllSafeWithin`：封装 `query Selector All Safe Within` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} root 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} selector 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function querySelectorAllSafeWithin(root, selector) {
    return selectorTools.queryAllWithin(root, selector);
  }

  /**
   * 函数 `isLikelyCustomControlElement`：封装 `is Likely Custom Control Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isLikelyCustomControlElement(element, video) {
    if (!element || element === video || element.querySelector('video')) return false;
    if (element.getAttribute('data-videobrowser-dismissed')) return false;

    const rect = element.getBoundingClientRect();
    const videoRect = video && typeof video.getBoundingClientRect === 'function'
      ? video.getBoundingClientRect()
      : null;
    if (!rect || rect.width <= 0 || rect.height <= 0) {
      return true;
    }
    if (!videoRect || videoRect.width <= 0 || videoRect.height <= 0) {
      return true;
    }
    return rectsOverlap(rect, expandedRect(videoRect, 12));
  }

  /**
   * 函数 `isLikelyMediaControlElement`：封装 `is Likely Media Control Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isLikelyMediaControlElement(element, video) {
    if (!isLikelyCustomControlElement(element, video)) return false;
    const descriptor = elementDescriptor(element).toLowerCase();
    const text = normalizeText(
      element.innerText ||
      element.textContent ||
      element.getAttribute('aria-label') ||
      element.getAttribute('title')
    ).toLowerCase();
    if (element.matches && element.matches('input[type="range"]')) return true;
    return /play|pause|seek|progress|volume|fullscreen|screenfull|control|播放|暂停|进度|音量|全屏/i
      .test(descriptor + ' ' + text);
  }

  /**
   * 函数 `installVideoFullscreenHooks`：封装 `install Video Fullscreen Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function installVideoFullscreenHooks(video) {
    if (!video || state.fullscreenHookedVideos.has(video)) return;
    state.fullscreenHookedVideos.add(video);
    /**
     * 函数 `timelineReporter`：封装 `timeline Reporter` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    const timelineReporter = function () {
      const position = Number(video.currentTime || 0);
      if (isVideoFullscreen(video) || state.nativeFullscreenVideo === video || !video.paused || position > 0) {
        reportPlaybackTimeline(video);
      }
    };
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    video.addEventListener('webkitbeginfullscreen', function () {
      state.nativeFullscreenVideo = video;
      applyVideoSpeed(video);
      reportPlaybackTimeline(video);
      enterNativeFullscreen();
    });
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    video.addEventListener('webkitendfullscreen', function () {
      stopDirectionalPlayback();
      state.nativeFullscreenVideo = null;
      state.documentFullscreenActive = false;
      state.fullscreenPlaybackSpeed = 1;
      applyVideoSpeed(video);
      exitNativeFullscreen();
    });
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    video.addEventListener('dblclick', function () {
      requestVideoFullscreen(video);
    });
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param eventName 表示当前回调正在处理的名称、键或文本值。
     */
    ['loadedmetadata', 'durationchange', 'timeupdate', 'seeked', 'play', 'playing'].forEach(function (eventName) {
      video.addEventListener(eventName, timelineReporter);
    });
  }

  /**
   * 函数 `installPlaybackSpeedHooks`：封装 `install Playback Speed Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function installPlaybackSpeedHooks(video) {
    if (!video || state.speedHookedVideos.has(video)) return;
    state.speedHookedVideos.add(video);
    /**
     * 函数 `enforceSpeed`：封装 `enforce Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    const enforceSpeed = function () {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       */
      window.setTimeout(function () {
        applyVideoSpeed(video);
      }, 0);
    };
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param eventName 表示当前回调正在处理的名称、键或文本值。
     */
    ['loadedmetadata', 'play', 'playing', 'ratechange', 'webkitbeginfullscreen'].forEach(function (eventName) {
      video.addEventListener(eventName, enforceSpeed);
    });
  }

  /**
   * 函数 `desiredVideoSpeed`：封装 `desired Video Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function desiredVideoSpeed(video) {
    const speed = currentFullscreenPlaybackSpeed();
    if (!state.config.videoEnabled || !isFullscreenPlaybackTarget(video)) return 1;
    return Number.isFinite(speed) && speed > 0 ? speed : 1;
  }

  /**
   * 函数 `currentFullscreenPlaybackSpeed`：封装 `current Fullscreen Playback Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function currentFullscreenPlaybackSpeed() {
    const speed = Number(state.fullscreenPlaybackSpeed || 1);
    return Number.isFinite(speed) && speed > 0 ? speed : 1;
  }

  /**
   * 函数 `isFullscreenPlaybackTarget`：封装 `is Fullscreen Playback Target` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isFullscreenPlaybackTarget(video) {
    return Boolean(video && (isVideoFullscreen(video) || state.nativeFullscreenVideo === video));
  }

  /**
   * 函数 `siteVideoCapabilitiesFor`：封装 `site Video Capabilities For` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function siteVideoCapabilitiesFor(video, action) {
    const adapters = window.VideoBrowserSiteAdapters || {};
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param key 表示当前回调正在处理的名称、键或文本值。
     */
    return Object.keys(adapters).map(function (key) {
      const adapter = adapters[key];
      return adapter && adapter.videoCapabilities;
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param capabilities 表示当前回调收到的 `capabilities` 参数。
     */
    }).filter(function (capabilities) {
      if (!capabilities || typeof capabilities !== 'object') return false;
      if (action && typeof capabilities[action] !== 'function') return false;
      if (typeof capabilities.supports === 'function') {
        try {
          if (!capabilities.supports(video)) return false;
        } catch (_) {
          return false;
        }
      }
      if (action && typeof capabilities.canUse === 'function') {
        try {
          if (!capabilities.canUse(action, video)) return false;
        } catch (_) {
          return false;
        }
      }
      return true;
    });
  }

  /**
   * 函数 `hasSiteVideoCapability`：封装 `has Site Video Capability` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function hasSiteVideoCapability(video, action) {
    return siteVideoCapabilitiesFor(video, action).length > 0;
  }

  /**
   * 函数 `invokeSiteVideoCapability`：封装 `invoke Site Video Capability` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} action 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} args 表示稍后执行的回调、清理函数或调用参数。
   */
  function invokeSiteVideoCapability(video, action, args) {
    const capabilitiesList = siteVideoCapabilitiesFor(video, action);
    for (let index = 0; index < capabilitiesList.length; index += 1) {
      const capabilities = capabilitiesList[index];
      const method = capabilities && capabilities[action];
      if (typeof method !== 'function') continue;
      try {
        const result = method.apply(capabilities, [video].concat(args || []));
        if (result !== null && typeof result !== 'undefined') {
          return { handled: true, value: result };
        }
      } catch (_) {}
    }
    return { handled: false, value: undefined };
  }

  /**
   * 函数 `applyVideoSpeed`：封装 `apply Video Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function applyVideoSpeed(video) {
    const speed = desiredVideoSpeed(video);
    if (hasSiteVideoCapability(video, 'setPlaybackSpeed')) {
      if (!isFullscreenPlaybackTarget(video)) return;
      const siteResult = invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed]);
      if (siteResult.handled) return;
    }
    try {
      if (Math.abs(Number(video.playbackRate || 1) - speed) > 0.01) {
        video.playbackRate = speed;
      }
      video.defaultPlaybackRate = speed;
    } catch (_) {}
  }

  /**
   * 函数 `preferBestVideoQuality`：封装 `prefer Best Video Quality` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function preferBestVideoQuality(video) {
    if (!state.config.videoEnabled || !video || !video.isConnected) return;
    if (!hasSiteVideoCapability(video, 'preferBestQuality')) return;

    const now = Date.now();
    const lastAttempt = state.bestQualityAttempts.get(video);
    if (lastAttempt && lastAttempt.success) return;
    if (lastAttempt && now - Number(lastAttempt.at || 0) < bestQualityAttemptIntervalMs) return;

    state.bestQualityAttempts.set(video, { at: now, success: false });
    logVideoDiagnostic('quality-prefer-start', videoLogDetails(video, {}));

    const siteResult = invokeSiteVideoCapability(video, 'preferBestQuality', []);
    if (siteResult.handled) {
      const success = siteResult.value !== false;
      state.bestQualityAttempts.set(video, { at: now, success: success });
      logVideoDiagnostic(
        success ? 'quality-prefer-success' : 'quality-prefer-unavailable',
        videoLogDetails(video, { result: siteResult.value })
      );
      return;
    }

    logVideoDiagnostic('quality-prefer-unavailable', videoLogDetails(video, {
      result: 'no-site-handler'
    }));
  }

  /**
   * 函数 `enableVideoControls`：封装 `enable Video Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function enableVideoControls(video) {
    const siteResult = invokeSiteVideoCapability(video, 'enableControls', []);
    if (siteResult.handled) {
      logVideoDiagnostic('enable-controls-site', videoLogDetails(video, {
        handled: true,
        result: siteResult.value
      }));
      return;
    }
    if (hasLikelyCustomPlayerControls(video)) {
      removeNativeVideoControls(video, 'custom-player');
      logVideoDiagnostic('enable-controls-custom-player', videoLogDetails(video, {
        handled: true
      }));
      return;
    }
    enableNativeVideoControls(video);
    logVideoDiagnostic('enable-controls-native', videoLogDetails(video, {
      handled: false
    }));
  }

  /**
   * 函数 `wakeVideoControls`：封装 `wake Video Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function wakeVideoControls(video) {
    const target = video || activeFullscreenVideo();
    if (!target) return false;

    enableVideoControls(target);
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

  /**
   * 函数 `dispatchControlWakeEvent`：封装 `dispatch Control Wake Event` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} target 表示函数执行 `target` 相关逻辑时需要读取或处理的输入。
   * @param {*} type 表示函数执行 `type` 相关逻辑时需要读取或处理的输入。
   * @param {*} clientX 表示函数执行 `clientX` 相关逻辑时需要读取或处理的输入。
   * @param {*} clientY 表示函数执行 `clientY` 相关逻辑时需要读取或处理的输入。
   */
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

  /**
   * 函数 `dispatchPointerWakeEvent`：封装 `dispatch Pointer Wake Event` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} target 表示函数执行 `target` 相关逻辑时需要读取或处理的输入。
   * @param {*} clientX 表示函数执行 `clientX` 相关逻辑时需要读取或处理的输入。
   * @param {*} clientY 表示函数执行 `clientY` 相关逻辑时需要读取或处理的输入。
   */
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

  /**
   * 函数 `videoTimeline`：封装 `video Timeline` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function videoTimeline(video) {
    return videoPlaybackTools.timeline(video);
  }

  /**
   * 函数 `reportPlaybackTimeline`：封装 `report Playback Timeline` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function reportPlaybackTimeline(video) {
    const target = video || activeFullscreenVideo();
    videoPlaybackTools.reportTimeline(target);
  }

  /**
   * 函数 `isBilibiliHost`：封装 `is Bilibili Host` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function isBilibiliHost() {
    return /(\.|^)bilibili\.com$/i.test(location.hostname);
  }

  /**
   * 函数 `seekVideo`：封装 `seek Video` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} sliderValue 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function seekVideo(video, sliderValue) {
    videoPlaybackTools.seek(video, sliderValue, {
      reportPlaybackTimeline: reportPlaybackTimeline
    });
  }

  /**
   * 函数 `seekVideoTo`：封装 `seek Video To` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} targetSeconds 表示参与几何计算、播放控制或列表定位的数值。
   */
  function seekVideoTo(video, targetSeconds) {
    videoPlaybackTools.seekTo(video, targetSeconds, {
      invokeSiteVideoCapability: invokeSiteVideoCapability,
      reportPlaybackTimeline: reportPlaybackTimeline
    });
  }

  /**
   * 函数 `seekVideoBy`：封装 `seek Video By` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} offsetSeconds 表示参与几何计算、播放控制或列表定位的数值。
   */
  function seekVideoBy(video, offsetSeconds) {
    videoPlaybackTools.seekBy(video, offsetSeconds, {
      invokeSiteVideoCapability: invokeSiteVideoCapability,
      reportPlaybackTimeline: reportPlaybackTimeline
    });
  }

  /**
   * 函数 `activeFullscreenVideo`：封装 `active Fullscreen Video` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
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
    const videos = videoQueryTools.all();
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param video 表示当前回调正在检查或操作的页面元素。
     */
    return videos.find(function (video) {
      return video && video.isConnected && !video.paused && !video.ended;
    }) || videos[0] || null;
  }

  /**
   * 函数 `requestVideoFullscreen`：封装 `request Video Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param requester 表示当前回调需要执行的函数或请求来源。
     */
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

  /**
   * 函数 `exitVideoFullscreen`：封装 `exit Video Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function exitVideoFullscreen() {
    stopDirectionalPlayback();
    state.nativeFullscreenVideo = null;
    state.documentFullscreenActive = false;
    state.fullscreenPlaybackSpeed = 1;
    videoQueryTools.forEach(applyVideoSpeed);
    if (document.fullscreenElement && typeof document.exitFullscreen === 'function') {
      try { document.exitFullscreen(); } catch (_) {}
    } else if (document.webkitFullscreenElement && typeof document.webkitExitFullscreen === 'function') {
      try { document.webkitExitFullscreen(); } catch (__) {}
    }
    exitNativeFullscreen();
  }

  /**
   * 函数 `isVideoFullscreen`：封装 `is Video Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isVideoFullscreen(video) {
    return video && (
      document.fullscreenElement === video ||
      document.fullscreenElement === video.parentElement ||
      document.webkitFullscreenElement === video ||
      document.webkitFullscreenElement === video.parentElement
    );
  }

  /**
   * 函数 `enterNativeFullscreen`：封装 `enter Native Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function enterNativeFullscreen() {
    if (nativeBridge && typeof nativeBridge.enterFullscreen === 'function') {
      nativeBridge.enterFullscreen();
    }
  }

  /**
   * 函数 `exitNativeFullscreen`：封装 `exit Native Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function exitNativeFullscreen() {
    if (nativeBridge && typeof nativeBridge.exitFullscreen === 'function') {
      nativeBridge.exitFullscreen();
    }
  }

  /**
   * 函数 `formatTime`：封装 `format Time` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function formatTime(value) {
    if (!Number.isFinite(value) || value < 0) return '00:00';
    const total = Math.floor(value);
    const minutes = Math.floor(total / 60);
    const seconds = total % 60;
    return String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
  }

  /**
   * 函数 `clickSkipButtons`：封装 `click Skip Buttons` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function clickSkipButtons() {
    if (!state.config.videoEnabled && !state.config.scriptletSkipButtonsEnabled) return;
    skipButtonTools.click();
  }

  /**
   * 函数 `hasActiveVideo`：封装 `has Active Video` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function hasActiveVideo() {
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param video 表示当前回调正在检查或操作的页面元素。
     */
    return videoQueryTools.some(function (video) {
      return video && video.isConnected && !video.paused && !video.ended && video.readyState > 1;
    });
  }

  /**
   * 函数 `installFullscreenEventHooks`：封装 `install Fullscreen Event Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function installFullscreenEventHooks() {
    document.addEventListener('fullscreenchange', syncDocumentFullscreenState);
    document.addEventListener('webkitfullscreenchange', syncDocumentFullscreenState);
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    window.addEventListener('pagehide', function () {
      suspendPageFeatures({ pauseVideos: true });
    });
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    window.addEventListener('pageshow', function () {
      state.disposed = false;
      startWorkers();
      schedulePageWork();
    });
  }

  /**
   * 函数 `syncDocumentFullscreenState`：封装 `sync Document Fullscreen State` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function syncDocumentFullscreenState() {
    const hasDocumentFullscreen = Boolean(document.fullscreenElement || document.webkitFullscreenElement);
    if (hasDocumentFullscreen) {
      state.documentFullscreenActive = true;
      state.nativeFullscreenVideo = activeFullscreenVideo();
      reportPlaybackTimeline(state.nativeFullscreenVideo);
      videoQueryTools.forEach(applyVideoSpeed);
      enterNativeFullscreen();
      return;
    }

    if (!state.documentFullscreenActive &&
      state.nativeFullscreenVideo &&
      state.nativeFullscreenVideo.isConnected
    ) {
      reportPlaybackTimeline(state.nativeFullscreenVideo);
      videoQueryTools.forEach(applyVideoSpeed);
      return;
    }

    state.documentFullscreenActive = false;
    stopDirectionalPlayback();
    state.nativeFullscreenVideo = null;
    state.fullscreenPlaybackSpeed = 1;
    videoQueryTools.forEach(applyVideoSpeed);
    exitNativeFullscreen();
  }

  /**
   * 函数 `installHooks`：封装 `install Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function installHooks() {
    scriptletHooks.install(state, {
      installFullscreenEventHooks: installFullscreenEventHooks
    });
  }

  /**
   * 函数 `runPageWork`：封装 `run Page Work` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function runPageWork() {
    state.pendingWork = false;
    if (state.disposed) return;

    const now = Date.now();
    const cleanupInterval = hasActiveVideo() ? activeVideoCleanupIntervalMs : normalCleanupIntervalMs;
    state.lastWorkAt = now;
    if (state.config.cleanupEnabled) {
      if (!isBilibiliHost()) {
        generatedAdCleanup.run(state, { now: now, force: false });
      }
      if (now - Number(state.lastCleanupAt || 0) >= cleanupInterval) {
        state.lastCleanupAt = now;
        removeAds();
      }
    } else if (userCssSelectors().length) {
      injectStyle(false, false);
    } else {
      removeStyle();
    }
    clickSkipButtons();
    enhanceVideos();
  }

  /**
   * 函数 `schedulePageWork`：封装 `schedule Page Work` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function schedulePageWork() {
    if (state.disposed || state.pendingWork) return;

    const elapsed = Date.now() - Number(state.lastWorkAt || 0);
    const workDelay = hasActiveVideo() ? activeVideoWorkDelayMs : normalWorkDelayMs;
    const delay = Math.max(60, workDelay - elapsed);
    state.pendingWork = true;
    window.setTimeout(runPageWork, delay);
  }

  /**
   * 函数 `pausePageVideos`：封装 `pause Page Videos` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function pausePageVideos() {
    videoPlaybackTools.pauseAll(videoQueryTools);
  }

  /**
   * 函数 `togglePlayPause`：封装 `toggle Play Pause` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function togglePlayPause() {
    const video = activeFullscreenVideo();
    return videoPlaybackTools.togglePlayPause(video, {
      invokeSiteVideoCapability: invokeSiteVideoCapability
    });
  }

  /**
   * 函数 `startDirectionalPlayback`：封装 `start Directional Playback` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} direction 表示参与几何计算、播放控制或列表定位的数值。
   */
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
      videoPlaybackTools.play(video);
      return;
    }

    videoPlaybackTools.pause(video);
    seekVideoBy(video, -0.5);
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    state.directionalPlayback.intervalId = window.setInterval(function () {
      const scan = state.directionalPlayback;
      if (!scan || scan.direction >= 0 || !scan.video || !scan.video.isConnected) {
        stopDirectionalPlayback();
        return;
      }
      seekVideoBy(scan.video, -0.5);
    }, 250);
  }

  /**
   * 函数 `stopDirectionalPlayback`：封装 `stop Directional Playback` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
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
        videoPlaybackTools.pause(video);
      } else {
        videoPlaybackTools.play(video);
      }
    }
    videoQueryTools.forEach(applyVideoSpeed);
  }

  /**
   * 函数 `suspendPageFeatures`：封装 `suspend Page Features` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} options 表示函数执行 `options` 相关逻辑时需要读取或处理的输入。
   */
  function suspendPageFeatures(options) {
    stopDirectionalPlayback();
    stopElementPicker();
    if (options && options.pauseVideos) {
      pausePageVideos();
    }
    exitVideoFullscreen();
    cleanupLegacyVideoOverlays();
  }

  /**
   * 函数 `disposePageFeatures`：封装 `dispose Page Features` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} options 表示函数执行 `options` 相关逻辑时需要读取或处理的输入。
   */
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

  /**
   * 函数 `startWorkers`：封装 `start Workers` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function startWorkers() {
    if (state.disposed) return;

    if (isBilibiliHost() && state.observer) {
      state.observer.disconnect();
      state.observer = null;
    }

    if (!state.observer && document.documentElement && !isBilibiliHost()) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       */
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
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       */
      state.intervalId = window.setInterval(function () {
        schedulePageWork();
      }, 1500);
    }
  }

  window.VideoBrowserEnhancer = {
    /**
     * 函数 `apply`：封装 `apply` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} config 表示本次脚本运行的配置或上下文数据。
     */
    apply: function (config) {
      state.disposed = false;
      state.config = config || {};
      state.lastCleanupAt = 0;
      cleanupLegacyVideoOverlays();
      installHooks();
      runPageWork();
      startWorkers();
    },
    /**
     * 函数 `exitFullscreen`：封装 `exit Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    exitFullscreen: function () {
      exitVideoFullscreen();
    },
    /**
     * 函数 `seekBy`：封装 `seek By` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} offsetSeconds 表示参与几何计算、播放控制或列表定位的数值。
     */
    seekBy: function (offsetSeconds) {
      seekVideoBy(activeFullscreenVideo(), Number(offsetSeconds || 0));
    },
    /**
     * 函数 `seekTo`：封装 `seek To` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} targetSeconds 表示参与几何计算、播放控制或列表定位的数值。
     */
    seekTo: function (targetSeconds) {
      seekVideoTo(activeFullscreenVideo(), Number(targetSeconds || 0));
    },
    /**
     * 函数 `reportPlaybackTimeline`：封装 `report Playback Timeline` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    reportPlaybackTimeline: function () {
      reportPlaybackTimeline(activeFullscreenVideo());
    },
    /**
     * 函数 `togglePlayPause`：封装 `toggle Play Pause` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    togglePlayPause: function () {
      return togglePlayPause();
    },
    /**
     * 函数 `wakeControls`：封装 `wake Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    wakeControls: function () {
      return wakeVideoControls(activeFullscreenVideo());
    },
    /**
     * 函数 `setPlaybackSpeed`：封装 `set Playback Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} speed 表示参与几何计算、播放控制或列表定位的数值。
     */
    setPlaybackSpeed: function (speed) {
      stopDirectionalPlayback();
      const normalizedSpeed = Number(speed || 1);
      state.fullscreenPlaybackSpeed =
        Number.isFinite(normalizedSpeed) && normalizedSpeed > 0 ? normalizedSpeed : 1;
      const video = activeFullscreenVideo();
      if (video && !(document.fullscreenElement || document.webkitFullscreenElement)) {
        state.nativeFullscreenVideo = video;
      }
      const siteResult = invokeSiteVideoCapability(video, 'setPlaybackSpeed', [state.fullscreenPlaybackSpeed]);
      if (siteResult.handled) return;
      videoQueryTools.forEach(applyVideoSpeed);
    },
    /**
     * 函数 `startDirectionalPlayback`：封装 `start Directional Playback` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} direction 表示参与几何计算、播放控制或列表定位的数值。
     */
    startDirectionalPlayback: function (direction) {
      startDirectionalPlayback(direction);
    },
    /**
     * 函数 `stopDirectionalPlayback`：封装 `stop Directional Playback` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    stopDirectionalPlayback: function () {
      stopDirectionalPlayback();
    },
    /**
     * 函数 `startElementPicker`：封装 `start Element Picker` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    startElementPicker: function () {
      return startElementPicker();
    },
    /**
     * 函数 `cancelElementPicker`：封装 `cancel Element Picker` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    cancelElementPicker: function () {
      stopElementPicker();
    },
    /**
     * 函数 `finishElementPicker`：封装 `finish Element Picker` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     */
    finishElementPicker: function () {
      stopElementPicker();
    },
    /**
     * 函数 `suspend`：封装 `suspend` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} options 表示函数执行 `options` 相关逻辑时需要读取或处理的输入。
     */
    suspend: function (options) {
      suspendPageFeatures(options || {});
    },
    /**
     * 函数 `dispose`：封装 `dispose` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} options 表示函数执行 `options` 相关逻辑时需要读取或处理的输入。
     */
    dispose: function (options) {
      disposePageFeatures(options || {});
    }
  };
})();
