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
  state.lastGeneratedAdCleanupAt = Number(state.lastGeneratedAdCleanupAt || 0);
  if (!Number.isFinite(state.lastGeneratedAdCleanupAt)) state.lastGeneratedAdCleanupAt = 0;
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
  const nativeBridge = window.VideoBrowserNativeBridge;

  const styleId = '__videobrowser_css_filter__';
  const normalCleanupIntervalMs = 3000;
  const activeVideoCleanupIntervalMs = 15000;
  const generatedAdCleanupIntervalMs = 100;
  const bestQualityAttemptIntervalMs = 30000;
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

  /**
   * 函数 `shouldBlockUrl`：封装 `should Block Url` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function shouldBlockUrl(value) {
    return shouldBlockUrlAgainstKeywords(
      value,
      blockedKeywords.concat(externalBlockedKeywords())
    );
  }

  /**
   * 函数 `shouldBlockUrlAgainstKeywords`：封装 `should Block Url Against Keywords` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} keywords 表示函数执行 `keywords` 相关逻辑时需要读取或处理的输入。
   */
  function shouldBlockUrlAgainstKeywords(value, keywords) {
    const url = String(value || '').toLowerCase();
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param keyword 表示当前回调正在处理的名称、键或文本值。
     */
    return keywords.some(function (keyword) {
      return url.indexOf(keyword) !== -1;
    });
  }

  /**
   * 函数 `externalBlockedKeywords`：封装 `external Blocked Keywords` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function externalBlockedKeywords() {
    return configKeywordList('blockedUrlKeywords');
  }

  /**
   * 函数 `scriptletWindowOpenBlockedKeywords`：封装 `scriptlet Window Open Blocked Keywords` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function scriptletWindowOpenBlockedKeywords() {
    return configKeywordList('scriptletWindowOpenBlockedKeywords');
  }

  /**
   * 函数 `scriptletFetchBlockedKeywords`：封装 `scriptlet Fetch Blocked Keywords` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function scriptletFetchBlockedKeywords() {
    return configKeywordList('scriptletFetchBlockedKeywords');
  }

  /**
   * 函数 `configKeywordList`：封装 `config Keyword List` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} fieldName 表示函数执行 `fieldName` 相关逻辑时需要读取或处理的输入。
   */
  function configKeywordList(fieldName) {
    const values = state.config && state.config[fieldName];
    if (!Array.isArray(values)) return [];
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param keyword 表示当前回调正在处理的名称、键或文本值。
     */
    return values.map(function (keyword) {
      return String(keyword || '').trim().toLowerCase();
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param keyword 表示当前回调正在处理的名称、键或文本值。
     */
    }).filter(function (keyword) {
      return keyword.length >= 3;
    });
  }

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
    if (!Array.isArray(value)) return [];
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     */
    return value.map(function (selector) {
      return String(selector || '').trim();
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     */
    }).filter(function (selector) {
      return isSafeSelector(selector);
    });
  }

  /**
   * 函数 `isSafeSelector`：封装 `is Safe Selector` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} selector 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function isSafeSelector(selector) {
    if (!selector || selector.length > 200) return false;
    if (/[{};<>]/.test(selector)) return false;
    return !/:has\(|:contains\(|:matches\(|:xpath\(|javascript:|expression\(/i.test(selector);
  }

  /**
   * 函数 `querySelectorAllSafe`：封装 `query Selector All Safe` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} selector 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function querySelectorAllSafe(selector) {
    return domTools.queryAll(selector);
  }

  /**
   * 函数 `injectStyle`：封装 `inject Style` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} includeGenericSelectors 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   * @param {*} includeRuleSelectors 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function injectStyle(includeGenericSelectors, includeRuleSelectors) {
    const selectors = (includeGenericSelectors ? adSelectors.concat(accountSelectors, cleanupSelectors) : [])
      .concat(includeRuleSelectors ? externalCssSelectors() : [])
      .concat(userCssSelectors());
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     * @param index 表示排序、比较或去重时当前回调收到的位置或比较对象。
     */
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

  /**
   * 函数 `removeStyle`：封装 `remove Style` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeStyle() {
    const style = document.getElementById(styleId);
    if (style) style.remove();
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
      dismissSitePrompts();
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
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
       */
      adSelectors.concat(accountSelectors, cleanupSelectors).forEach(function (selector) {
        /*
         * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
         * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
         * @param element 表示当前回调正在检查或操作的页面元素。
         */
        document.querySelectorAll(selector).forEach(function (element) {
          hideElement(element, 'generic-cleanup');
        });
      });
      removeConfiguredDomElements();
      removeGenericAdOverlays();
      removeTopAccountBars();
      removeTopNoiseBlocks();
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
    if (!document.body) return;

    const roots = [];
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param candidate 表示当前回调正在检查或操作的页面元素。
     */
    collectGenericAdOverlayCandidates().forEach(function (candidate) {
      const root = findGenericAdOverlayRoot(candidate);
      if (root && roots.indexOf(root) === -1) roots.push(root);
    });

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param root 表示当前回调正在检查或操作的页面元素。
     */
    roots.forEach(function (root) {
      hideElement(root, 'generic-ad-overlay');
      hideGenericOverlayBackdrops(root);
      clearOverlayScrollLocks();
    });

    runGeneratedAdScaffoldCleanup(Date.now(), true);
  }

  /**
   * 函数 `runGeneratedAdScaffoldCleanup`：封装 `run Generated Ad Scaffold Cleanup` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} now 表示函数执行 `now` 相关逻辑时需要读取或处理的输入。
   * @param {*} force 表示函数执行 `force` 相关逻辑时需要读取或处理的输入。
   */
  function runGeneratedAdScaffoldCleanup(now, force) {
    const timestamp = Number(now || Date.now());
    if (!force && timestamp - Number(state.lastGeneratedAdCleanupAt || 0) < generatedAdCleanupIntervalMs) {
      return;
    }
    state.lastGeneratedAdCleanupAt = timestamp;
    removeGeneratedAdScaffolds();
  }

  /**
   * 函数 `removeGeneratedAdScaffolds`：封装 `remove Generated Ad Scaffolds` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeGeneratedAdScaffolds() {
    const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
    const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
    if (!viewportWidth || !viewportHeight) return;

    const imageSlices = [];
    const clickGridCells = [];
    const adjunctControls = [];
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
    querySelectorAllSafe('body *').forEach(function (element) {
      if (!element || isProtectedAppContainer(element)) return;

      const style = getComputedStyle(element);
      if (style.position !== 'fixed') return;

      const rect = element.getBoundingClientRect();
      if (!isVisibleRectInViewport(rect, viewportWidth, viewportHeight)) return;

      if (isGeneratedImageSlice(element, style, rect, viewportWidth, viewportHeight)) {
        imageSlices.push(element);
        return;
      }
      if (isGeneratedClickGridCell(element, style, rect, viewportWidth, viewportHeight)) {
        clickGridCells.push(element);
        return;
      }
      if (isGeneratedAdAdjunctControl(element, style, rect)) {
        adjunctControls.push(element);
      }
    });

    if (imageSlices.length >= 12) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param element 表示当前回调正在检查或操作的页面元素。
       */
      imageSlices.forEach(function (element) {
        hideElement(element, 'generated-sliced-ad');
      });
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param element 表示当前回调正在检查或操作的页面元素。
       */
      adjunctControls.forEach(function (element) {
        hideElement(element, 'generated-sliced-ad');
      });
    }

    if (clickGridCells.length >= 20) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param element 表示当前回调正在检查或操作的页面元素。
       */
      clickGridCells.forEach(function (element) {
        hideElement(element, 'generated-click-grid');
      });
    }
  }

  /**
   * 函数 `isVisibleRectInViewport`：封装 `is Visible Rect In Viewport` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} rect 表示参与几何计算、播放控制或列表定位的数值。
   * @param {*} viewportWidth 表示函数执行 `viewportWidth` 相关逻辑时需要读取或处理的输入。
   * @param {*} viewportHeight 表示函数执行 `viewportHeight` 相关逻辑时需要读取或处理的输入。
   */
  function isVisibleRectInViewport(rect, viewportWidth, viewportHeight) {
    return Boolean(
      rect &&
      rect.width > 0 &&
      rect.height > 0 &&
      rect.bottom > 0 &&
      rect.right > 0 &&
      rect.top < viewportHeight &&
      rect.left < viewportWidth
    );
  }

  /**
   * 函数 `isGeneratedImageSlice`：封装 `is Generated Image Slice` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} style 表示函数执行 `style` 相关逻辑时需要读取或处理的输入。
   * @param {*} rect 表示参与几何计算、播放控制或列表定位的数值。
   * @param {*} viewportWidth 表示函数执行 `viewportWidth` 相关逻辑时需要读取或处理的输入。
   * @param {*} viewportHeight 表示函数执行 `viewportHeight` 相关逻辑时需要读取或处理的输入。
   */
  function isGeneratedImageSlice(element, style, rect, viewportWidth, viewportHeight) {
    if (parseZIndex(style.zIndex) < 1000000) return false;
    if (!/^url\(["']?data:image\//i.test(String(style.backgroundImage || ''))) return false;
    if (normalizeText(element.innerText || element.textContent).length > 40) return false;
    if (element.querySelector('video,form,input,textarea,select')) return false;
    if (rect.width < Math.max(8, viewportWidth * 0.035)) return false;
    if (rect.width > Math.min(140, viewportWidth * 0.34)) return false;
    if (rect.height < 8 || rect.height > Math.min(180, viewportHeight * 0.3)) return false;
    return true;
  }

  /**
   * 函数 `isGeneratedClickGridCell`：封装 `is Generated Click Grid Cell` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} style 表示函数执行 `style` 相关逻辑时需要读取或处理的输入。
   * @param {*} rect 表示参与几何计算、播放控制或列表定位的数值。
   * @param {*} viewportWidth 表示函数执行 `viewportWidth` 相关逻辑时需要读取或处理的输入。
   * @param {*} viewportHeight 表示函数执行 `viewportHeight` 相关逻辑时需要读取或处理的输入。
   */
  function isGeneratedClickGridCell(element, style, rect, viewportWidth, viewportHeight) {
    const opacity = Number.parseFloat(style.opacity);
    if (!Number.isFinite(opacity) || opacity > 0.02) return false;
    const zIndex = parseZIndex(style.zIndex);
    if (zIndex < 10 || zIndex > 999999) return false;
    if (normalizeText(element.innerText || element.textContent).length > 0) return false;
    if (element.querySelector('video,form,input,textarea,select,img,svg,canvas')) return false;
    if (rect.width < Math.max(16, viewportWidth * 0.035)) return false;
    if (rect.width > Math.min(120, viewportWidth * 0.3)) return false;
    if (rect.height < 16 || rect.height > Math.min(120, viewportHeight * 0.22)) return false;
    return true;
  }

  /**
   * 函数 `isGeneratedAdAdjunctControl`：封装 `is Generated Ad Adjunct Control` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} style 表示函数执行 `style` 相关逻辑时需要读取或处理的输入。
   * @param {*} rect 表示参与几何计算、播放控制或列表定位的数值。
   */
  function isGeneratedAdAdjunctControl(element, style, rect) {
    if (parseZIndex(style.zIndex) < 1000000) return false;
    if (normalizeText(element.innerText || element.textContent).length > 20) return false;
    if (rect.width > 56 || rect.height > 56) return false;
    const tagName = String(element.tagName || '').toLowerCase();
    const descriptor = elementDescriptor(element);
    return /^[a-z]{5,10}$/.test(tagName) ||
      (!descriptor.trim() && !String(style.backgroundImage || '').match(/^url\(/i));
  }

  /**
   * 函数 `collectGenericAdOverlayCandidates`：封装 `collect Generic Ad Overlay Candidates` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function collectGenericAdOverlayCandidates() {
    const candidates = [];
    /**
     * 函数 `addCandidate`：封装 `add Candidate` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
     */
    function addCandidate(element) {
      if (element && candidates.indexOf(element) === -1) candidates.push(element);
    }

    [
      '[role="dialog"]',
      '[aria-modal="true"]',
      '[class*="modal"]',
      '[class*="Modal"]',
      '[class*="popup"]',
      '[class*="Popup"]',
      '[class*="pop"]',
      '[class*="Pop"]',
      '[class*="mask"]',
      '[class*="Mask"]',
      '[class*="overlay"]',
      '[class*="Overlay"]',
      '[class*="dialog"]',
      '[class*="Dialog"]',
      '[class*="layer"]',
      '[class*="Layer"]',
      '[class*="float"]',
      '[class*="Float"]',
      '[class*="promotion"]',
      '[class*="advert"]',
      '[style*="position: fixed"]',
      '[style*="position:fixed"]',
      '[style*="position: absolute"]',
      '[style*="position:absolute"]'
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     */
    ].forEach(function (selector) {
      querySelectorAllSafe(selector).forEach(addCandidate);
    });

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
    querySelectorAllSafe('button,a,i,[role="button"],[aria-label],[title]').forEach(function (element) {
      if (isCloseLikeControl(element)) addCandidate(element);
    });
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
    querySelectorAllSafe('img,picture,svg').forEach(function (element) {
      const rect = element.getBoundingClientRect();
      const source = mediaSourceValue(element);
      if (
        (rect.width >= 32 && rect.height >= 32) ||
        /^data:image\//i.test(source) ||
        /ad|ads|adv|advert|banner|promo|promotion|taojianghu|sf-express|alicdn|gif/i.test(source)
      ) {
        addCandidate(element);
      }
    });

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
    querySelectorAllSafe('body *').forEach(function (element) {
      if (!element || isProtectedAppContainer(element) || element.querySelector('video')) return;

      const style = getComputedStyle(element);
      if (!/fixed|absolute|sticky/i.test(style.position)) return;

      const rect = element.getBoundingClientRect();
      const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
      const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
      if (!rect.width || !rect.height || !viewportWidth || !viewportHeight) return;
      if (rect.bottom <= 0 || rect.right <= 0 || rect.top >= viewportHeight || rect.left >= viewportWidth) return;
      if (rect.width < 24 || rect.height < 24) return;
      if (rect.width > Math.min(viewportWidth * 0.9, 360)) return;
      if (rect.height > Math.min(viewportHeight * 0.55, 420)) return;

      const descriptor = elementDescriptor(element);
      const text = normalizeText(element.innerText || element.textContent);
      const promoText = /官方推荐|APP亲测无毒|狼友多下载|防丢失|招嫖约炮|情趣用品|注册即送|超高爆率|连麦|操控|同城约炮|超级巨奖|财富放水|PG娱乐|PG电子|PG游戏|私人订制|抢庄牛牛|附近上门|上门服务|学生空降|学生兼职|外围兼职|1元拉爆|新葡京|开户送钱|送\d+元|提款秒到|充提秒到/i
        .test(text);
      const compactPromo = rect.width >= viewportWidth * 0.5 &&
        rect.height <= Math.min(viewportHeight * 0.42, 320);
      const widePromoGrid = rect.width >= viewportWidth * 0.82 &&
        rect.height <= Math.max(viewportHeight * 1.8, 1400);
      if (promoText && (compactPromo || widePromoGrid)) {
        addCandidate(element);
      }

      const adSignal = /广告|廣告|推广|推廣|赞助|贊助|立即下载|立即安装|打开APP|下载APP|福利|红包|领取|客服|加微信|棋牌|彩票|博彩|download|install|openapp|adult|casino|sponsor|promotion/i
        .test(text + ' ' + descriptor);
      const nameSignal = /ad|ads|adv|advert|sponsor|promo|promotion|download|openapp|banner|float|popup|modal|layer|icon-close/i
        .test(descriptor);
      const hasMedia = Boolean(element.querySelector('img,picture,svg'));
      if (hasCloseLikeDescendant(element) || adSignal || (hasMedia && nameSignal)) {
        addCandidate(element);
      }
    });

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
    querySelectorAllSafe('body *').forEach(function (element) {
      if (!element || isProtectedAppContainer(element) || element.querySelector('video,form,input,textarea,select')) {
        return;
      }

      const text = normalizeText(element.innerText || element.textContent);
      if (!/官方推荐|APP亲测无毒|狼友多下载|防丢失|招嫖约炮|情趣用品|注册即送|超高爆率|连麦|操控|同城约炮|超级巨奖|财富放水|PG娱乐|PG电子|PG游戏|私人订制|抢庄牛牛|附近上门|上门服务|学生空降|学生兼职|外围兼职|1元拉爆|新葡京|开户送钱|送\d+元|提款秒到|充提秒到/i.test(text)) {
        return;
      }

      const rect = element.getBoundingClientRect();
      const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
      const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
      if (!rect.width || !rect.height || !viewportWidth || !viewportHeight) {
        addCandidate(element);
        return;
      }
      const compactPromo = rect.width >= viewportWidth * 0.5 &&
        rect.height <= Math.min(viewportHeight * 0.42, 320);
      const widePromoGrid = rect.width >= viewportWidth * 0.82 &&
        rect.height <= Math.max(viewportHeight * 1.8, 1400);
      if (compactPromo || widePromoGrid) {
        addCandidate(element);
      }
    });

    return candidates;
  }

  /**
   * 函数 `findGenericAdOverlayRoot`：封装 `find Generic Ad Overlay Root` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function findGenericAdOverlayRoot(element) {
    let current = element;
    let matchedRoot = null;
    for (let depth = 0; current && depth < 9; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (isProtectedAppContainer(current)) break;
      if (isLikelyGenericAdOverlay(current) && shouldUseGenericAdOverlayRoot(matchedRoot, current)) {
        matchedRoot = current;
      }
    }
    return matchedRoot;
  }

  /**
   * 函数 `shouldUseGenericAdOverlayRoot`：封装 `should Use Generic Ad Overlay Root` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} currentRoot 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} candidateRoot 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function shouldUseGenericAdOverlayRoot(currentRoot, candidateRoot) {
    if (!currentRoot) return true;

    const style = getComputedStyle(candidateRoot);
    const descriptor = elementDescriptor(candidateRoot);
    const canPromoteLayer = /fixed|absolute|sticky/i.test(style.position) &&
      (
        parseZIndex(style.zIndex) >= 10 ||
        /modal|popup|pop|mask|overlay|dialog|layer/i.test(descriptor)
      );
    if (canPromoteLayer) return true;

    const currentRect = currentRoot.getBoundingClientRect();
    const candidateRect = candidateRoot.getBoundingClientRect();
    const currentArea = currentRect.width * currentRect.height;
    const candidateArea = candidateRect.width * candidateRect.height;
    if (!currentArea || !candidateArea) return false;
    return candidateArea <= currentArea * 2.8;
  }

  /**
   * 函数 `isLikelyGenericAdOverlay`：封装 `is Likely Generic Ad Overlay` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isLikelyGenericAdOverlay(element) {
    if (!element || isProtectedAppContainer(element)) return false;
    if (element.querySelector('video')) return false;

    const descriptor = elementDescriptor(element);
    const text = normalizeText(element.innerText || element.textContent);
    const promoTextLike = /官方推荐|APP亲测无毒|狼友多下载|防丢失|招嫖约炮|情趣用品|注册即送|超高爆率|连麦|操控|同城约炮|超级巨奖|财富放水|PG娱乐|PG电子|PG游戏|私人订制|抢庄牛牛|附近上门|上门服务|学生空降|学生兼职|外围兼职|1元拉爆|新葡京|开户送钱|送\d+元|提款秒到|充提秒到/i
      .test(text);
    const adTextLike = /广告|廣告|推广|推廣|赞助|贊助|立即下载|立即安装|打开APP|下载APP|福利|红包|领取|客服|加微信|棋牌|彩票|博彩|adult|casino|sponsor|promotion/i
      .test(text);
    const adNameLike = /(^|[-_\s])(ad|ads|adv|advert|sponsor|promo|promotion|download|openapp|banner)([-_\s]|$)/i
      .test(descriptor);
    const hasClose = hasCloseLikeDescendant(element);
    const hasMediaOrAction = Boolean(
      element.querySelector('img,picture,svg,a[href],button,i,[role="button"],[onclick],[class*="icon-"]')
    );
    const hasMedia = Boolean(element.querySelector('img,picture,svg'));
    const formHeavy = element.querySelectorAll('input,textarea,select').length > 0 &&
      !adNameLike &&
      !adTextLike;
    if (formHeavy) return false;

    const rect = element.getBoundingClientRect();
    const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
    const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
    if (!rect.width || !rect.height || !viewportWidth || !viewportHeight) {
      const zeroLayoutAdNameLike = /modal|popup|pop|mask|overlay|dialog|layer|float|ad|ads|adv|advert|promo|promotion|download|install|openapp|banner/i
        .test(descriptor);
      if (promoTextLike && text.length <= 260) {
        return true;
      }
      return (zeroLayoutAdNameLike || adNameLike || adTextLike) &&
        hasMediaOrAction &&
        (hasClose || hasMedia || adNameLike || adTextLike);
    }
    if (rect.bottom <= 0 || rect.right <= 0 || rect.top >= viewportHeight || rect.left >= viewportWidth) {
      return false;
    }

    const style = getComputedStyle(element);
    const positioned = /fixed|absolute|sticky/i.test(style.position);
    const zIndex = parseZIndex(style.zIndex);
    const layerNameLike = /modal|popup|pop|mask|overlay|dialog|layer|float/i.test(descriptor);
    const highLayer = zIndex >= 10 || layerNameLike;
    const imageOnlyWideBanner = !positioned &&
      rect.width >= viewportWidth * 0.82 &&
      rect.height >= 48 &&
      rect.height <= Math.max(viewportHeight * 1.1, 360) &&
      text.length <= 20 &&
      hasMedia &&
      !element.querySelector('video,form,input,textarea,select') &&
      mediaSourceLooksLikeAd(element);
    const inlinePromoBlock = rect.width >= viewportWidth * 0.82 &&
      rect.height >= 8 &&
      rect.height <= Math.max(viewportHeight * 1.8, 1400) &&
      text.length <= 900 &&
      promoTextLike &&
      !element.querySelector('video,form,input,textarea,select');
    if (!positioned && !highLayer && !imageOnlyWideBanner && !inlinePromoBlock) return false;

    const fullOverlay = rect.width >= viewportWidth * 0.86 &&
      rect.height >= viewportHeight * 0.48 &&
      rect.left <= viewportWidth * 0.12 &&
      rect.top <= viewportHeight * 0.22;
    const centeredFloat = highLayer &&
      rect.width >= Math.min(viewportWidth * 0.42, 260) &&
      rect.height >= Math.min(viewportHeight * 0.14, 180) &&
      rect.left < viewportWidth * 0.92 &&
      rect.right > viewportWidth * 0.08 &&
      rect.top < viewportHeight * 0.92 &&
      rect.bottom > viewportHeight * 0.08;
    const edgeFloat = positioned &&
      rect.width >= 32 &&
      rect.width <= Math.min(viewportWidth * 0.46, 260) &&
      rect.height >= 32 &&
      rect.height <= Math.min(viewportHeight * 0.34, 280) &&
      (
        rect.right >= viewportWidth - 16 ||
        rect.left <= 16 ||
        rect.bottom >= viewportHeight - 16 ||
        rect.top <= 16
      );
    const bottomActionBar = positioned &&
      rect.width >= viewportWidth * 0.52 &&
      rect.height >= 36 &&
      rect.height <= Math.min(viewportHeight * 0.22, 150) &&
      (rect.bottom >= viewportHeight - 220 || rect.top >= viewportHeight * 0.62);
    const inlineMediaTile = !positioned &&
      rect.width >= 60 &&
      rect.width <= Math.min(viewportWidth * 0.46, 260) &&
      rect.height >= 60 &&
      rect.height <= Math.min(viewportHeight * 0.34, 280) &&
      hasClose &&
      hasMedia &&
      text.length <= 80;
    const inlineActionBar = !positioned &&
      rect.width >= viewportWidth * 0.52 &&
      rect.height >= 36 &&
      rect.height <= Math.min(viewportHeight * 0.22, 150) &&
      hasMediaOrAction &&
      /下载|安裝|安装|APP|app|桌面|download|install|openapp/i.test(text + ' ' + descriptor);

    if (
      !fullOverlay &&
      !centeredFloat &&
      !edgeFloat &&
      !bottomActionBar &&
      !inlineMediaTile &&
      !inlineActionBar &&
      !imageOnlyWideBanner &&
      !inlinePromoBlock
    ) {
      return false;
    }

    if (inlinePromoBlock) {
      return true;
    }
    if (imageOnlyWideBanner) {
      return true;
    }
    if (fullOverlay && (
      adNameLike ||
      adTextLike ||
      (hasClose && (hasMedia || text.length <= 80))
    )) {
      return true;
    }
    if (centeredFloat && hasMediaOrAction && (adNameLike || adTextLike || (hasClose && hasMedia))) {
      return true;
    }
    if (bottomActionBar && (hasMediaOrAction || adTextLike) && (
      adTextLike ||
      /下载|安裝|安装|APP|app|桌面|download|install|openapp/i.test(text + ' ' + descriptor)
    )) {
      return true;
    }
    if (inlineMediaTile || inlineActionBar) {
      return true;
    }
    return edgeFloat && hasMediaOrAction && (
      hasClose ||
      adNameLike ||
      adTextLike ||
      (hasMedia && zIndex >= 10 && text.length <= 20)
    );
  }

  /**
   * 函数 `mediaSourceLooksLikeAd`：封装 `media Source Looks Like Ad` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function mediaSourceLooksLikeAd(element) {
    return Array.prototype.some.call(
      element.querySelectorAll('img,source'),
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param media 表示当前回调正在检查或操作的页面元素。
       */
      function (media) {
        const value = mediaSourceValue(media);
        return /^data:image\//i.test(value) ||
          /ad|ads|adv|advert|banner|promo|promotion|taojianghu|sf-express|alicdn|gif/i.test(value);
      }
    );
  }

  /**
   * 函数 `mediaSourceValue`：封装 `media Source Value` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} media 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function mediaSourceValue(media) {
    return String(
      media &&
      (
        media.currentSrc ||
        media.src ||
        media.getAttribute('src') ||
        media.getAttribute('srcset') ||
        ''
      )
    );
  }

  /**
   * 函数 `hasCloseLikeDescendant`：封装 `has Close Like Descendant` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function hasCloseLikeDescendant(element) {
    if (!element) return false;
    if (isCloseLikeControl(element)) return true;
    return Array.prototype.some.call(
      element.querySelectorAll(
        'button,a,i,[role="button"],[aria-label],[title],' +
        '[class*="close"],[class*="Close"],[class*="icon-close"]'
      ),
      isCloseLikeControl
    );
  }

  /**
   * 函数 `isCloseLikeControl`：封装 `is Close Like Control` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isCloseLikeControl(element) {
    if (!element) return false;
    const rect = element.getBoundingClientRect();
    const text = normalizeText(
      element.innerText ||
      element.textContent ||
      element.getAttribute('aria-label') ||
      element.getAttribute('title') ||
      element.getAttribute('alt')
    );
    const descriptor = elementDescriptor(element);
    const compactText = text.replace(/\s+/g, '');
    if (/^(×|x|X|✕|✖|关闭|關閉|取消|跳过|跳過|稍后|稍後|不再提示|close|skip|dismiss)$/i.test(compactText)) {
      return true;
    }
    return rect.width <= 72 &&
      rect.height <= 72 &&
      /close|dismiss|cancel|skip|关闭|關閉|跳过|跳過/i.test(descriptor + ' ' + text);
  }

  /**
   * 函数 `hideGenericOverlayBackdrops`：封装 `hide Generic Overlay Backdrops` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} root 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function hideGenericOverlayBackdrops(root) {
    if (!root || !document.body) return;
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
    querySelectorAllSafe('body *').forEach(function (element) {
      if (!element || element === root || element.contains(root) || root.contains(element)) return;
      if (isProtectedAppContainer(element)) return;

      const rect = element.getBoundingClientRect();
      if (!rect.width || !rect.height) return;

      const style = getComputedStyle(element);
      if (!/fixed|absolute|sticky/i.test(style.position)) return;
      const descriptor = elementDescriptor(element);
      const fullScreenLike = rect.width >= window.innerWidth * 0.86 &&
        rect.height >= window.innerHeight * 0.48 &&
        rect.left <= window.innerWidth * 0.12 &&
        rect.top <= window.innerHeight * 0.22;
      const overlayNameLike = /mask|overlay|modal|popup|dialog|shade|shadow|backdrop|layer/i.test(descriptor);
      const text = normalizeText(element.textContent);
      if (fullScreenLike && overlayNameLike && text.length <= 80) {
        hideElement(element, 'generic-ad-backdrop');
      }
    });
  }

  /**
   * 函数 `clearOverlayScrollLocks`：封装 `clear Overlay Scroll Locks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function clearOverlayScrollLocks() {
    unlockScrollContainer(document.documentElement);
    unlockScrollContainer(document.body);
  }

  /**
   * 函数 `unlockScrollContainer`：封装 `unlock Scroll Container` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function unlockScrollContainer(element) {
    if (!element) return;
    [
      'overflow',
      'overflow-x',
      'overflow-y',
      'position',
      'height',
      'touch-action',
      'overscroll-behavior'
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param property 表示当前回调正在处理的名称、键或文本值。
     */
    ].forEach(function (property) {
      element.style.removeProperty(property);
    });

    if (!element.classList) return;
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param className 表示当前回调正在处理的名称、键或文本值。
     */
    Array.prototype.slice.call(element.classList).forEach(function (className) {
      if (isScrollLockClass(className)) {
        element.classList.remove(className);
      }
    });
  }

  /**
   * 函数 `isScrollLockClass`：封装 `is Scroll Lock Class` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} className 表示函数执行 `className` 相关逻辑时需要读取或处理的输入。
   */
  function isScrollLockClass(className) {
    return /(^|[-_])(overflow-hidden|no-scroll|noscroll|scroll-lock|lock-scroll)([-_]|$)/i
      .test(String(className || '')) ||
      /^adm-overflow-hidden$/i.test(String(className || ''));
  }

  /**
   * 函数 `elementDescriptor`：封装 `element Descriptor` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function elementDescriptor(element) {
    return (
      String(element.id || '') + ' ' +
      String(element.className || '') + ' ' +
      String(element.getAttribute('role') || '') + ' ' +
      String(element.getAttribute('aria-label') || '') + ' ' +
      String(element.getAttribute('title') || '')
    );
  }

  /**
   * 函数 `parseZIndex`：封装 `parse ZIndex` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function parseZIndex(value) {
    const parsed = Number.parseInt(value, 10);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  /**
   * 函数 `dismissSitePrompts`：封装 `dismiss Site Prompts` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function dismissSitePrompts() {
    dismissBilibiliBrowserChoicePrompts();
  }

  /**
   * 函数 `shouldSkipGenericCleanup`：封装 `should Skip Generic Cleanup` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function shouldSkipGenericCleanup() {
    return isBilibiliHost() || isSearchProviderResultPage();
  }

  /**
   * 函数 `isSearchProviderResultPage`：封装 `is Search Provider Result Page` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function isSearchProviderResultPage() {
    const host = String(location.hostname || '').toLowerCase();
    const path = String(location.pathname || '/').replace(/\/+$/, '') || '/';
    const query = location.search || '';

    if (/^(m|www)\.baidu\.com$/i.test(host)) {
      return (path === '/s' && /[?&](word|wd)=/i.test(query)) ||
        (path === '/baidu' && /[?&](word|wd)=/i.test(query));
    }
    if (/^(m\.)?sogou\.com$/i.test(host) || host === 'www.sogou.com') {
      return (path === '/web' || path === '/s') && /[?&](query|keyword)=/i.test(query);
    }
    if (/^(m\.)?so\.com$/i.test(host) || host === 'www.so.com') {
      return path === '/s' && /[?&]q=/i.test(query);
    }
    if (host === 'quark.sm.cn' || host === 'so.m.sm.cn') {
      return path === '/s' && /[?&]q=/i.test(query);
    }
    return false;
  }

  /**
   * 函数 `removeSearchResultAds`：封装 `remove Search Result Ads` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeSearchResultAds() {
    if (!isSearchProviderResultPage()) return;

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     */
    runWithMutationSuppressed(function () {
      hideKnownSearchAdContainers();
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param marker 表示当前回调正在检查或操作的页面元素。
       */
      findSearchAdDisclosureMarkers().forEach(function (marker) {
        const root = findSearchResultRoot(marker);
        if (root) hideElement(root, 'search-result-ad');
      });
    });
  }

  /**
   * 函数 `hideKnownSearchAdContainers`：封装 `hide Known Search Ad Containers` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
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

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     */
    selectors.forEach(function (selector) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param element 表示当前回调正在检查或操作的页面元素。
       */
      document.querySelectorAll(selector).forEach(function (element) {
        const root = findSearchResultRoot(element) || element;
        hideElement(root, 'search-result-ad-container');
      });
    });
  }

  /**
   * 函数 `findSearchAdDisclosureMarkers`：封装 `find Search Ad Disclosure Markers` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function findSearchAdDisclosureMarkers() {
    const markers = [];
    document.querySelectorAll(
      'span,i,em,b,a,button,[role="button"],[aria-label],[title],[class*="ad"],[class*="adv"]'
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
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

  /**
   * 函数 `isSearchAdDisclosure`：封装 `is Search Ad Disclosure` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} text 表示要判断、转换或传给播放器/规则逻辑的输入值。
   * @param {*} descriptor 表示函数执行 `descriptor` 相关逻辑时需要读取或处理的输入。
   */
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

  /**
   * 函数 `findSearchResultRoot`：封装 `find Search Result Root` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} marker 表示函数执行 `marker` 相关逻辑时需要读取或处理的输入。
   */
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

  /**
   * 函数 `isSearchControlContainer`：封装 `is Search Control Container` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `dismissBilibiliBrowserChoicePrompts`：封装 `dismiss Bilibili Browser Choice Prompts` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
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

  /**
   * 函数 `normalizeText`：封装 `normalize Text` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function normalizeText(value) {
    return String(value || '').replace(/\s+/g, ' ').trim();
  }

  /**
   * 函数 `findBilibiliPromptRoot`：封装 `find Bilibili Prompt Root` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `isBilibiliContentContainer`：封装 `is Bilibili Content Container` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isBilibiliContentContainer(element) {
    const descriptor = (String(element.id || '') + ' ' + String(element.className || '')).toLowerCase();
    return /\bm-home\b|\bm-video\b|video-normal|player|recommend|feed-list|video-list|v-card/.test(descriptor);
  }

  /**
   * 函数 `hideBilibiliPromptBackdrops`：封装 `hide Bilibili Prompt Backdrops` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} promptRoot 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function hideBilibiliPromptBackdrops(promptRoot) {
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
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

  /**
   * 函数 `hideElement`：封装 `hide Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} reason 表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
   */
  function hideElement(element, reason) {
    if (!element || element === document.body || element === document.documentElement) return;
    if (isProtectedAppContainer(element)) return;
    element.setAttribute('data-videobrowser-dismissed', reason || 'cleanup');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  }

  /**
   * 函数 `removeElement`：封装 `remove Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} reason 表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
   */
  function removeElement(element, reason) {
    if (!element || element === document.body || element === document.documentElement) return;
    if (isProtectedAppContainer(element)) return;
    element.setAttribute('data-videobrowser-dismissed', reason || 'remove');
    element.remove();
  }

  /**
   * 函数 `isProtectedAppContainer`：封装 `is Protected App Container` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isProtectedAppContainer(element) {
    const id = String(element && element.id || '').toLowerCase();
    return id === 'app' || id === 'root' || id === '__next' || id === 'nuxt';
  }

  /**
   * 函数 `startElementPicker`：封装 `start Element Picker` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function startElementPicker() {
    stopElementPicker();
    if (!document.documentElement || !document.body) return false;

    const overlay = document.createElement('div');
    overlay.id = '__videobrowser_element_picker_overlay__';
    overlay.style.cssText = [
      'position:fixed',
      'left:0',
      'top:0',
      'width:0',
      'height:0',
      'box-sizing:border-box',
      'border:2px solid #1D6BE3',
      'background:rgba(29,107,227,0.12)',
      'box-shadow:0 0 0 9999px rgba(17,24,39,0.08)',
      'z-index:2147483647',
      'pointer-events:none',
      'display:none'
    ].join(';');
    document.documentElement.appendChild(overlay);

    state.elementPicker = {
      overlay: overlay,
      selectedElement: null,
      waitingForNative: false,
      lastSelectAt: 0,
      listeners: []
    };

    addElementPickerListener('pointerdown', handleElementPickerMove);
    addElementPickerListener('pointermove', handleElementPickerMove);
    addElementPickerListener('pointerup', handleElementPickerSelection);
    addElementPickerListener('touchstart', handleElementPickerMove);
    addElementPickerListener('touchmove', handleElementPickerMove);
    addElementPickerListener('touchend', handleElementPickerSelection);
    addElementPickerListener('mousedown', handleElementPickerMove);
    addElementPickerListener('mousemove', handleElementPickerMove);
    addElementPickerListener('mouseup', handleElementPickerSelection);
    addElementPickerListener('click', handleElementPickerSelection);
    return true;
  }

  /**
   * 函数 `addElementPickerListener`：封装 `add Element Picker Listener` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} type 表示函数执行 `type` 相关逻辑时需要读取或处理的输入。
   * @param {*} listener 表示函数执行 `listener` 相关逻辑时需要读取或处理的输入。
   */
  function addElementPickerListener(type, listener) {
    if (!state.elementPicker) return;
    document.addEventListener(type, listener, true);
    state.elementPicker.listeners.push({
      type: type,
      listener: listener
    });
  }

  /**
   * 函数 `handleElementPickerMove`：封装 `handle Element Picker Move` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} event 表示浏览器事件或事件名称，用来区分触发来源。
   */
  function handleElementPickerMove(event) {
    const picker = state.elementPicker;
    if (!picker) return;
    if (picker.waitingForNative) {
      preventElementPickerEvent(event);
      return;
    }
    const element = elementPickerTargetFromEvent(event);
    if (element) highlightPickedElement(element);
    preventElementPickerEvent(event);
  }

  /**
   * 函数 `handleElementPickerSelection`：封装 `handle Element Picker Selection` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} event 表示浏览器事件或事件名称，用来区分触发来源。
   */
  function handleElementPickerSelection(event) {
    const picker = state.elementPicker;
    if (!picker) return;
    if (picker.waitingForNative) {
      preventElementPickerEvent(event);
      return;
    }

    preventElementPickerEvent(event);
    const now = Date.now();
    if (now - Number(picker.lastSelectAt || 0) < 350) return;
    picker.lastSelectAt = now;

    const element = elementPickerTargetFromEvent(event) || picker.selectedElement;
    if (!element) return;

    const selector = buildElementPickerSelector(element);
    if (!selector || !isSafeSelector(selector)) return;

    picker.waitingForNative = true;
    highlightPickedElement(element);

    const bridge = window.VideoBrowserNative;
    if (bridge && typeof bridge.requestElementBlock === 'function') {
      try {
        bridge.requestElementBlock(selector, describePickedElement(element));
        return;
      } catch (_) {}
    }
    stopElementPicker();
  }

  /**
   * 函数 `preventElementPickerEvent`：封装 `prevent Element Picker Event` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} event 表示浏览器事件或事件名称，用来区分触发来源。
   */
  function preventElementPickerEvent(event) {
    if (!event) return;
    try { event.preventDefault(); } catch (_) {}
    try { event.stopPropagation(); } catch (_) {}
    try { event.stopImmediatePropagation(); } catch (_) {}
  }

  /**
   * 函数 `elementPickerTargetFromEvent`：封装 `element Picker Target From Event` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} event 表示浏览器事件或事件名称，用来区分触发来源。
   */
  function elementPickerTargetFromEvent(event) {
    const point = pointFromPickerEvent(event);
    if (!point) return null;
    let element = null;
    try {
      element = document.elementFromPoint(point.x, point.y);
    } catch (_) {
      element = event && event.target;
    }
    return normalizePickerTarget(element);
  }

  /**
   * 函数 `pointFromPickerEvent`：封装 `point From Picker Event` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} event 表示浏览器事件或事件名称，用来区分触发来源。
   */
  function pointFromPickerEvent(event) {
    if (!event) return null;
    const touch = event.changedTouches && event.changedTouches.length
      ? event.changedTouches[0]
      : event.touches && event.touches.length
        ? event.touches[0]
        : null;
    const source = touch || event;
    const x = Number(source.clientX);
    const y = Number(source.clientY);
    if (!Number.isFinite(x) || !Number.isFinite(y)) return null;
    return { x: x, y: y };
  }

  /**
   * 函数 `normalizePickerTarget`：封装 `normalize Picker Target` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function normalizePickerTarget(element) {
    let current = element && element.nodeType === 1 ? element : null;
    for (let depth = 0; current && depth < 6; depth += 1, current = current.parentElement) {
      if (isElementPickerInternal(current)) return null;
      if (current === document.body || current === document.documentElement) return null;
      if (!isProtectedAppContainer(current)) return current;
    }
    return null;
  }

  /**
   * 函数 `isElementPickerInternal`：封装 `is Element Picker Internal` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isElementPickerInternal(element) {
    return String(element && element.id || '') === '__videobrowser_element_picker_overlay__';
  }

  /**
   * 函数 `highlightPickedElement`：封装 `highlight Picked Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function highlightPickedElement(element) {
    const picker = state.elementPicker;
    if (!picker || !picker.overlay || !element || !element.getBoundingClientRect) return;
    const rect = element.getBoundingClientRect();
    picker.selectedElement = element;
    picker.overlay.style.display = rect.width > 0 && rect.height > 0 ? 'block' : 'none';
    picker.overlay.style.left = Math.max(0, rect.left) + 'px';
    picker.overlay.style.top = Math.max(0, rect.top) + 'px';
    picker.overlay.style.width = Math.max(0, rect.width) + 'px';
    picker.overlay.style.height = Math.max(0, rect.height) + 'px';
  }

  /**
   * 函数 `buildElementPickerSelector`：封装 `build Element Picker Selector` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function buildElementPickerSelector(element) {
    if (!element || element === document.body || element === document.documentElement) return null;

    const idSelector = selectorFromElementId(element);
    if (idSelector && isUniqueSelector(idSelector)) return idSelector;

    const classSelector = selectorFromElementClasses(element);
    if (classSelector && isUniqueSelector(classSelector)) return classSelector;

    const segments = [];
    let current = element;
    for (let depth = 0; current && depth < 5; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      segments.unshift(selectorSegmentForElement(current));
      const candidate = segments.join(' ');
      if (isSafeSelector(candidate) && isUniqueSelector(candidate)) {
        return candidate;
      }
    }

    const fallback = segments.join(' ');
    return isSafeSelector(fallback) ? fallback : null;
  }

  /**
   * 函数 `selectorFromElementId`：封装 `selector From Element Id` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function selectorFromElementId(element) {
    const id = String(element.id || '').trim();
    if (!isStableSelectorToken(id)) return null;
    return '#' + cssIdentifier(id);
  }

  /**
   * 函数 `selectorFromElementClasses`：封装 `selector From Element Classes` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function selectorFromElementClasses(element) {
    const tagName = elementTagName(element);
    const classes = stableClassTokens(element).slice(0, 3);
    if (!tagName || !classes.length) return null;
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param className 表示当前回调正在处理的名称、键或文本值。
     */
    return tagName + classes.map(function (className) {
      return '.' + cssIdentifier(className);
    }).join('');
  }

  /**
   * 函数 `selectorSegmentForElement`：封装 `selector Segment For Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function selectorSegmentForElement(element) {
    const tagName = elementTagName(element);
    const idSelector = selectorFromElementId(element);
    if (idSelector) return tagName + idSelector;

    let segment = tagName;
    const classes = stableClassTokens(element).slice(0, 2);
    if (classes.length) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param className 表示当前回调正在处理的名称、键或文本值。
       */
      segment += classes.map(function (className) {
        return '.' + cssIdentifier(className);
      }).join('');
    }
    if (shouldAppendNthOfType(element, segment)) {
      segment += ':nth-of-type(' + nthOfType(element) + ')';
    }
    return segment;
  }

  /**
   * 函数 `shouldAppendNthOfType`：封装 `should Append Nth Of Type` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} segment 表示函数执行 `segment` 相关逻辑时需要读取或处理的输入。
   */
  function shouldAppendNthOfType(element, segment) {
    const parent = element.parentElement;
    if (!parent) return false;
    let matches = 0;
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param sibling 表示当前回调正在检查或操作的页面元素。
     */
    Array.prototype.forEach.call(parent.children, function (sibling) {
      try {
        if (sibling.matches(segment)) matches += 1;
      } catch (_) {}
    });
    return matches > 1;
  }

  /**
   * 函数 `nthOfType`：封装 `nth Of Type` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function nthOfType(element) {
    let index = 1;
    let sibling = element.previousElementSibling;
    const tagName = elementTagName(element);
    while (sibling) {
      if (elementTagName(sibling) === tagName) index += 1;
      sibling = sibling.previousElementSibling;
    }
    return index;
  }

  /**
   * 函数 `elementTagName`：封装 `element Tag Name` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function elementTagName(element) {
    return String(element && element.tagName || '').toLowerCase();
  }

  /**
   * 函数 `stableClassTokens`：封装 `stable Class Tokens` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function stableClassTokens(element) {
    return Array.prototype.slice.call(element.classList || [])
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param value 表示当前回调正在处理的名称、键或文本值。
       */
      .map(function (value) { return String(value || '').trim(); })
      .filter(isStableSelectorToken)
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param value 表示当前回调正在处理的名称、键或文本值。
       * @param index 表示排序、比较或去重时当前回调收到的位置或比较对象。
       * @param values 表示当前回调正在处理的名称、键或文本值。
       */
      .filter(function (value, index, values) {
        return values.indexOf(value) === index;
      });
  }

  /**
   * 函数 `isStableSelectorToken`：封装 `is Stable Selector Token` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function isStableSelectorToken(value) {
    const token = String(value || '').trim();
    if (token.length < 2 || token.length > 80) return false;
    if (!/^[A-Za-z_][A-Za-z0-9_-]*$/.test(token)) return false;
    if (/^[A-Fa-f0-9]{8,}$/.test(token)) return false;
    if (/\d{6,}/.test(token)) return false;
    return !/^(active|current|selected|open|close|show|hide|visible|hidden|loaded)$/i.test(token);
  }

  /**
   * 函数 `cssIdentifier`：封装 `css Identifier` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} value 表示要判断、转换或传给播放器/规则逻辑的输入值。
   */
  function cssIdentifier(value) {
    if (window.CSS && typeof window.CSS.escape === 'function') {
      return window.CSS.escape(String(value));
    }
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param character 表示当前回调收到的 `character` 参数。
     */
    return String(value).replace(/[^A-Za-z0-9_-]/g, function (character) {
      return '\\' + character.charCodeAt(0).toString(16) + ' ';
    });
  }

  /**
   * 函数 `isUniqueSelector`：封装 `is Unique Selector` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} selector 表示 CSS 选择器或查询条件，用来定位页面里的目标元素。
   */
  function isUniqueSelector(selector) {
    const matches = querySelectorAllSafe(selector);
    return matches.length === 1;
  }

  /**
   * 函数 `describePickedElement`：封装 `describe Picked Element` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function describePickedElement(element) {
    const parts = [elementTagName(element)];
    const id = String(element.id || '').trim();
    if (id) parts.push('#' + id.slice(0, 48));
    const classes = stableClassTokens(element).slice(0, 3);
    if (classes.length) parts.push('.' + classes.join('.'));
    const text = normalizeText(element.innerText || element.textContent);
    if (text) parts.push(text.slice(0, 80));
    return parts.join(' ');
  }

  /**
   * 函数 `detachElementPickerListeners`：封装 `detach Element Picker Listeners` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} picker 表示函数执行 `picker` 相关逻辑时需要读取或处理的输入。
   */
  function detachElementPickerListeners(picker) {
    if (!picker || !Array.isArray(picker.listeners)) return;
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param entry 表示当前回调收到的 `entry` 参数。
     */
    picker.listeners.forEach(function (entry) {
      document.removeEventListener(entry.type, entry.listener, true);
    });
    picker.listeners.length = 0;
  }

  /**
   * 函数 `stopElementPicker`：封装 `stop Element Picker` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function stopElementPicker() {
    const picker = state.elementPicker;
    if (!picker) return;
    detachElementPickerListeners(picker);
    if (picker.overlay && picker.overlay.parentNode) {
      picker.overlay.parentNode.removeChild(picker.overlay);
    }
    state.elementPicker = null;
  }

  /**
   * 函数 `removeTopAccountBars`：封装 `remove Top Account Bars` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeTopAccountBars() {
    if (!/(\.|^)baidu\.com$/i.test(location.hostname)) return;
    const candidates = [];
    /**
     * 函数 `addCandidate`：封装 `add Candidate` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} element 表示当前正在检查或操作的 DOM/媒体元素。
     */
    function addCandidate(element) {
      if (element && candidates.indexOf(element) === -1) candidates.push(element);
    }
    document.querySelectorAll(
      'header,[role="banner"],[id*="top"],[class*="top"],[id*="head"],[class*="head"],body>div,body>div>div'
    ).forEach(addCandidate);

    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
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

  /**
   * 函数 `removeTopNoiseBlocks`：封装 `remove Top Noise Blocks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function removeTopNoiseBlocks() {
    if (!isSearchProviderHomePage()) return;

    document.querySelectorAll(
      'body>div,body>section,header,[role="banner"],[id*="top"],[class*="top"],[id*="banner"],[class*="banner"]'
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param element 表示当前回调正在检查或操作的页面元素。
     */
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

  /**
   * 函数 `isSearchProviderHomePage`：封装 `is Search Provider Home Page` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function isSearchProviderHomePage() {
    const host = String(location.hostname || '').toLowerCase();
    const path = String(location.pathname || '/').replace(/\/+$/, '') || '/';
    return path === '/' && /^(m\.baidu\.com|m\.sogou\.com|m\.so\.com|quark\.sm\.cn|so\.m\.sm\.cn|www\.bing\.com)$/i.test(host);
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
    document.querySelectorAll('video').forEach(function (video) {
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
    if (!video.controls) video.controls = true;
    if (video.getAttribute('controls') !== 'controls') {
      video.setAttribute('controls', 'controls');
    }
    if (video.hasAttribute('playsinline')) video.removeAttribute('playsinline');
    if (video.hasAttribute('webkit-playsinline')) video.removeAttribute('webkit-playsinline');
    if (video.style.maxWidth !== '100%') video.style.maxWidth = '100%';
  }

  /**
   * 函数 `removeNativeVideoControls`：封装 `remove Native Video Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} reason 表示函数执行 `reason` 相关逻辑时需要读取或处理的输入。
   */
  function removeNativeVideoControls(video, reason) {
    if (!video) return false;
    const hadNativeControls = Boolean(video.controls || video.hasAttribute('controls'));
    try { video.controls = false; } catch (_) {}
    try { video.removeAttribute('controls'); } catch (_) {}
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
    return domTools.queryAllWithin(root, selector);
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

  /**
   * 函数 `reportPlaybackTimeline`：封装 `report Playback Timeline` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
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

  /**
   * 函数 `seekVideoTo`：封装 `seek Video To` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} targetSeconds 表示参与几何计算、播放控制或列表定位的数值。
   */
  function seekVideoTo(video, targetSeconds) {
    if (!video || !Number.isFinite(targetSeconds)) return;
    const siteResult = invokeSiteVideoCapability(video, 'seekTo', [targetSeconds]);
    if (siteResult.handled) {
      reportPlaybackTimeline(video);
      return;
    }
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

  /**
   * 函数 `seekVideoBy`：封装 `seek Video By` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   * @param {*} offsetSeconds 表示参与几何计算、播放控制或列表定位的数值。
   */
  function seekVideoBy(video, offsetSeconds) {
    if (!video || !Number.isFinite(offsetSeconds)) return;
    const siteResult = invokeSiteVideoCapability(video, 'seekBy', [offsetSeconds]);
    if (siteResult.handled) {
      reportPlaybackTimeline(video);
      return;
    }
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
    const videos = Array.prototype.slice.call(document.querySelectorAll('video'));
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
    document.querySelectorAll('video').forEach(applyVideoSpeed);
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
    const bridge = window.VideoBrowserNative;
    if (bridge && typeof bridge.enterFullscreen === 'function') {
      try { bridge.enterFullscreen(); } catch (_) {}
    }
  }

  /**
   * 函数 `exitNativeFullscreen`：封装 `exit Native Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function exitNativeFullscreen() {
    const bridge = window.VideoBrowserNative;
    if (bridge && typeof bridge.exitFullscreen === 'function') {
      try { bridge.exitFullscreen(); } catch (_) {}
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param selector 表示本次遍历拿到的选择器字符串，用来继续查找页面元素。
     */
    skipSelectors.forEach(function (selector) {
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       * @param button 表示当前回调正在检查或操作的页面元素。
       */
      document.querySelectorAll(selector).forEach(function (button) {
        const text = String(button.innerText || button.textContent || button.getAttribute('aria-label') || '');
        const looksLikeSkip = /skip|跳过|关闭|close/i.test(text) || selector.indexOf('skip') !== -1;
        if (looksLikeSkip && typeof button.click === 'function') button.click();
      });
    });
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
    return Array.prototype.some.call(document.querySelectorAll('video'), function (video) {
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
      document.querySelectorAll('video').forEach(applyVideoSpeed);
      enterNativeFullscreen();
      return;
    }

    if (!state.documentFullscreenActive &&
      state.nativeFullscreenVideo &&
      state.nativeFullscreenVideo.isConnected
    ) {
      reportPlaybackTimeline(state.nativeFullscreenVideo);
      document.querySelectorAll('video').forEach(applyVideoSpeed);
      return;
    }

    state.documentFullscreenActive = false;
    stopDirectionalPlayback();
    state.nativeFullscreenVideo = null;
    state.fullscreenPlaybackSpeed = 1;
    document.querySelectorAll('video').forEach(applyVideoSpeed);
    exitNativeFullscreen();
  }

  /**
   * 函数 `installHooks`：封装 `install Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function installHooks() {
    if (state.hooked) return;
    state.hooked = true;

    const originalOpen = window.open;
    /**
     * 函数 `window.open`：封装 `open` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} url 表示要判断、转换或传给播放器/规则逻辑的输入值。
     */
    window.open = function (url) {
      if (
        (state.config.cleanupEnabled && shouldBlockUrl(url)) ||
        shouldBlockUrlAgainstKeywords(url, scriptletWindowOpenBlockedKeywords())
      ) {
        return null;
      }
      return originalOpen.apply(this, arguments);
    };

    const originalFetch = window.fetch;
    if (typeof originalFetch === 'function') {
      /**
       * 函数 `window.fetch`：封装 `fetch` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
       *
       * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
       */
      window.fetch = function () {
        if (arguments.length > 0) {
          const input = arguments[0];
          const url = typeof input === 'string' ? input : input && input.url;
          if (
            (state.config.cleanupEnabled && shouldBlockUrl(url)) ||
            shouldBlockUrlAgainstKeywords(url, scriptletFetchBlockedKeywords())
          ) {
            return Promise.reject(new Error('Blocked by VideoBrowser'));
          }
        }
        return originalFetch.apply(this, arguments);
      };
    }

    installFullscreenEventHooks();
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
        runGeneratedAdScaffoldCleanup(now, false);
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
    dismissSitePrompts();
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
    /*
     * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
     * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
     * @param video 表示当前回调正在检查或操作的页面元素。
     */
    document.querySelectorAll('video').forEach(function (video) {
      try { video.pause(); } catch (_) {}
    });
  }

  /**
   * 函数 `togglePlayPause`：封装 `toggle Play Pause` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function togglePlayPause() {
    const video = activeFullscreenVideo();
    if (!video) return false;
    const siteResult = invokeSiteVideoCapability(video, 'togglePlayPause', []);
    if (siteResult.handled) return siteResult.value;
    if (video.paused || video.ended) {
      try {
        if (video.ended) video.currentTime = 0;
      } catch (_) {}
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       */
      try { video.play().catch(function () {}); } catch (__) {}
      return true;
    }
    try { video.pause(); } catch (_) {}
    return false;
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
      /*
       * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
       * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
       */
      try { video.play().catch(function () {}); } catch (_) {}
      return;
    }

    try { video.pause(); } catch (_) {}
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
        try { video.pause(); } catch (_) {}
      } else {
        /*
         * 内联回调函数：这一行把函数作为参数交给数组遍历、事件监听、定时器或异步 API。
         * 初学者阅读提示：先看回调参数，再看回调体如何处理当前这一项数据。
         */
        try { video.play().catch(function () {}); } catch (__) {}
      }
    }
    document.querySelectorAll('video').forEach(applyVideoSpeed);
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
      document.querySelectorAll('video').forEach(applyVideoSpeed);
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
