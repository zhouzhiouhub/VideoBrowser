/*
 * 初学者阅读提示：
 * 这是所有网页都会注入的通用增强脚本。
 * 它负责读取 Kotlin 侧传入的配置，执行页面清理、元素屏蔽、视频手势桥接和诊断日志。
 */
(function () {
  const enhancerState = window.VideoBrowserEnhancerState;
  const state = enhancerState.current();
  const pageCleanupCoordinator = window.VideoBrowserPageCleanupCoordinator;
  const skipButtonTools = window.VideoBrowserSkipButtonTools;
  const nativeBridge = window.VideoBrowserNativeBridge;
  const logVideoDiagnostic = nativeBridge.logPageVideoDiagnostic;
  const videoLogDetails = nativeBridge.videoLogDetails;
  const videoControlCoordinator = window.VideoBrowserVideoControlCoordinator;
  const videoQueryTools = window.VideoBrowserVideoQueryTools;
  const videoPlaybackTools = window.VideoBrowserVideoPlaybackTools;
  const videoFullscreenTools = window.VideoBrowserVideoFullscreenTools;
  const videoWakeTools = window.VideoBrowserVideoWakeTools;
  const videoEnhancementTools = window.VideoBrowserVideoEnhancementTools;
  const siteVideoCapabilityBroker = window.VideoBrowserSiteVideoCapabilityBroker;
  const hasSiteVideoCapability = siteVideoCapabilityBroker.has;
  const invokeSiteVideoCapability = siteVideoCapabilityBroker.invoke;
  const elementPicker = window.VideoBrowserElementPicker;
  const scriptletHooks = window.VideoBrowserScriptletHooks;
  const pageLifecycleTools = window.VideoBrowserPageLifecycleTools;
  const enhancerRuntime = window.VideoBrowserEnhancerRuntime;
  const enhancerApi = window.VideoBrowserEnhancerApi;

  const pageRuntime = enhancerRuntime.create({
    state: state,
    pageCleanupCoordinator: pageCleanupCoordinator,
    skipButtonTools: skipButtonTools,
    pageLifecycleTools: pageLifecycleTools,
    elementPicker: elementPicker,
    videoQueryTools: videoQueryTools,
    videoPlaybackTools: videoPlaybackTools,
    videoControlCoordinator: videoControlCoordinator,
    callbacks: {
      enhanceVideos: enhanceVideos,
      syncDocumentFullscreenState: syncDocumentFullscreenState,
      stopDirectionalPlayback: stopDirectionalPlayback,
      exitVideoFullscreen: exitVideoFullscreen
    }
  });

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
        videoControlCoordinator.enableControls(video);
      }
      if (!state.config.videoEnabled) return;
      preferBestVideoQuality(video);
      installVideoFullscreenHooks(video);
      installPlaybackSpeedHooks(video);
      applyVideoSpeed(video);
    });
  }

  /**
   * 函数 `installVideoFullscreenHooks`：封装 `install Video Fullscreen Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function installVideoFullscreenHooks(video) {
    return videoFullscreenTools.installVideoHooks(video, state, {
      isVideoFullscreen: isVideoFullscreen,
      applyVideoSpeed: applyVideoSpeed,
      reportPlaybackTimeline: reportPlaybackTimeline,
      enterNativeFullscreen: enterNativeFullscreen,
      stopDirectionalPlayback: stopDirectionalPlayback,
      exitNativeFullscreen: exitNativeFullscreen,
      requestVideoFullscreen: requestVideoFullscreen
    });
  }

  /**
   * 函数 `installPlaybackSpeedHooks`：封装 `install Playback Speed Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function installPlaybackSpeedHooks(video) {
    return videoEnhancementTools.installPlaybackSpeedHooks(video, state, {
      applyVideoSpeed: applyVideoSpeed
    });
  }

  /**
   * 函数 `desiredVideoSpeed`：封装 `desired Video Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function desiredVideoSpeed(video) {
    return videoEnhancementTools.desiredSpeed(video, state, {
      isVideoFullscreen: isVideoFullscreen
    });
  }

  /**
   * 函数 `currentFullscreenPlaybackSpeed`：封装 `current Fullscreen Playback Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function currentFullscreenPlaybackSpeed() {
    return videoEnhancementTools.currentFullscreenPlaybackSpeed(state);
  }

  /**
   * 函数 `isFullscreenPlaybackTarget`：封装 `is Fullscreen Playback Target` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isFullscreenPlaybackTarget(video) {
    return videoEnhancementTools.isFullscreenPlaybackTarget(video, state, {
      isVideoFullscreen: isVideoFullscreen
    });
  }

  /**
   * 函数 `applyVideoSpeed`：封装 `apply Video Speed` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function applyVideoSpeed(video) {
    return videoEnhancementTools.applySpeed(video, state, {
      hasSiteVideoCapability: hasSiteVideoCapability,
      invokeSiteVideoCapability: invokeSiteVideoCapability,
      isVideoFullscreen: isVideoFullscreen
    });
  }

  /**
   * 函数 `preferBestVideoQuality`：封装 `prefer Best Video Quality` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function preferBestVideoQuality(video) {
    return videoEnhancementTools.preferBestQuality(video, state, {
      hasSiteVideoCapability: hasSiteVideoCapability,
      invokeSiteVideoCapability: invokeSiteVideoCapability,
      logVideoDiagnostic: logVideoDiagnostic,
      videoLogDetails: videoLogDetails
    });
  }

  /**
   * 函数 `wakeVideoControls`：封装 `wake Video Controls` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function wakeVideoControls(video) {
    return videoWakeTools.wake(video, {
      activeFullscreenVideo: activeFullscreenVideo,
      enableVideoControls: videoControlCoordinator.enableControls,
      reportPlaybackTimeline: reportPlaybackTimeline
    });
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
    return videoFullscreenTools.activeVideo(state, videoQueryTools);
  }

  /**
   * 函数 `requestVideoFullscreen`：封装 `request Video Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function requestVideoFullscreen(video) {
    videoFullscreenTools.request(video, {
      isVideoFullscreen: isVideoFullscreen,
      exitVideoFullscreen: exitVideoFullscreen,
      reportPlaybackTimeline: reportPlaybackTimeline,
      enterNativeFullscreen: enterNativeFullscreen
    });
  }

  /**
   * 函数 `exitVideoFullscreen`：封装 `exit Video Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function exitVideoFullscreen() {
    videoFullscreenTools.exit(state, {
      stopDirectionalPlayback: stopDirectionalPlayback,
      videoQueryTools: videoQueryTools,
      applyVideoSpeed: applyVideoSpeed,
      exitNativeFullscreen: exitNativeFullscreen
    });
  }

  /**
   * 函数 `isVideoFullscreen`：封装 `is Video Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   * @param {*} video 表示当前正在检查或操作的 DOM/媒体元素。
   */
  function isVideoFullscreen(video) {
    return videoFullscreenTools.isVideoFullscreen(video);
  }

  /**
   * 函数 `enterNativeFullscreen`：封装 `enter Native Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function enterNativeFullscreen() {
    videoFullscreenTools.enterNative(nativeBridge);
  }

  /**
   * 函数 `exitNativeFullscreen`：封装 `exit Native Fullscreen` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function exitNativeFullscreen() {
    videoFullscreenTools.exitNative(nativeBridge);
  }

  /**
   * 函数 `syncDocumentFullscreenState`：封装 `sync Document Fullscreen State` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function syncDocumentFullscreenState() {
    videoFullscreenTools.syncDocumentState(state, {
      activeFullscreenVideo: activeFullscreenVideo,
      reportPlaybackTimeline: reportPlaybackTimeline,
      videoQueryTools: videoQueryTools,
      applyVideoSpeed: applyVideoSpeed,
      stopDirectionalPlayback: stopDirectionalPlayback,
      enterNativeFullscreen: enterNativeFullscreen,
      exitNativeFullscreen: exitNativeFullscreen
    });
  }

  /**
   * 函数 `installHooks`：封装 `install Hooks` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function installHooks() {
    scriptletHooks.install(state, {
      installFullscreenEventHooks: pageRuntime.installFullscreenEventHooks
    });
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
    return videoPlaybackTools.startDirectional(direction, state, {
      activeFullscreenVideo: activeFullscreenVideo,
      currentFullscreenPlaybackSpeed: currentFullscreenPlaybackSpeed,
      applyVideoSpeed: applyVideoSpeed,
      seekVideoBy: seekVideoBy,
      videoQueryTools: videoQueryTools
    });
  }

  /**
   * 函数 `stopDirectionalPlayback`：封装 `stop Directional Playback` 这一段网页脚本逻辑，让调用方不用关心内部 DOM 查询、状态判断或桥接细节。
   *
   * 初学者阅读提示：先看参数说明，再看函数体如何读取页面元素、脚本状态或原生桥接对象。
   */
  function stopDirectionalPlayback() {
    return videoPlaybackTools.stopDirectional(state, {
      activeFullscreenVideo: activeFullscreenVideo,
      applyVideoSpeed: applyVideoSpeed,
      videoQueryTools: videoQueryTools
    });
  }

  window.VideoBrowserEnhancer = enhancerApi.create({
    state: state,
    videoEnhancementTools: videoEnhancementTools,
    videoQueryTools: videoQueryTools,
    invokeSiteVideoCapability: invokeSiteVideoCapability,
    cleanupLegacyVideoOverlays: pageRuntime.cleanupLegacyVideoOverlays,
    installHooks: installHooks,
    runPageWork: pageRuntime.runPageWork,
    startWorkers: pageRuntime.startWorkers,
    exitVideoFullscreen: exitVideoFullscreen,
    seekVideoBy: seekVideoBy,
    seekVideoTo: seekVideoTo,
    reportPlaybackTimeline: reportPlaybackTimeline,
    togglePlayPause: togglePlayPause,
    wakeVideoControls: wakeVideoControls,
    activeFullscreenVideo: activeFullscreenVideo,
    applyVideoSpeed: applyVideoSpeed,
    startDirectionalPlayback: startDirectionalPlayback,
    stopDirectionalPlayback: stopDirectionalPlayback,
    startElementPicker: pageRuntime.startElementPicker,
    stopElementPicker: pageRuntime.stopElementPicker,
    suspendPageFeatures: pageRuntime.suspendPageFeatures,
    disposePageFeatures: pageRuntime.disposePageFeatures
  });
})();
