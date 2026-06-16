/*
 * 初学者阅读提示：
 * 这是 iqiyi 站点的适配脚本。
 * 它只处理该站点特有的播放器结构、遮挡元素和视频控制桥接，通用逻辑仍在 common.js 中。
 */
(function () {
  var adapters = window.VideoBrowserSiteAdapters || {};
  window.VideoBrowserSiteAdapters = adapters;

  var state = window.__videobrowserIqiyiState || {
    intervalId: null,
    config: {}
  };
  window.__videobrowserIqiyiState = state;

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
    element.setAttribute('data-videobrowser-site-dismissed', reason || 'iqiyi');
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
        hideElement(element, 'iqiyi-ad');
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
    query('button,a,[role="button"],.close,.skip,.qy-player-vippay-close').forEach(function (element) {
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
    var message = 'event=' + event + ' adapter=iqiyi host=' + location.hostname + ' ' + (details || '');
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
   * 函数 `run`：封装 `run` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} config 表示本次脚本运行的配置或上下文数据。
   */
  function run(config) {
    if (!document.documentElement) return;
    if (config && config.cleanupEnabled) {
      hideSelectors([
        '.cupid',
        '.qy-player-ad',
        '.qy-player-vippay',
        '.qy-player-focus-ad',
        '.qy-player-side-ad',
        '.qy-mod-ad',
        '.m-box-items-ad',
        '[class*="cupid"]',
        '[class*="ad-wrapper"]',
        '[data-videobrowser-remove]'
      ]);
      clickTextButtons(/(\u5173\u95ed|\u53d6\u6d88|\u7a0d\u540e|close|cancel)/i);
    }
    if (config && config.videoEnabled) {
      query('video').forEach(removeNativeVideoControls);
      clickTextButtons(/(\u8df3\u8fc7|\u5173\u95ed|skip|close)/i);
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

  adapters.iqiyi = adapters.iqiyi || {};
  adapters.iqiyi.videoCapabilities = {
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
     */
    canUse: function (action) {
      return action === 'enableControls';
    },
    /**
     * 函数 `enableControls`：封装 `enable Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
     * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
     */
    enableControls: function (video) {
      removeNativeVideoControls(video);
      return Boolean(video);
    }
  };
  /**
   * 函数 `adapters.iqiyi.apply`：封装 `apply` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} config 表示本次脚本运行的配置或上下文数据。
   */
  adapters.iqiyi.apply = function (config) {
    this.lastConfig = config || {};
    state.config = this.lastConfig;
    run(state.config);
    startWorker();
  };
})();
