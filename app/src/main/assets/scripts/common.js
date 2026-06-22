/*
 * 初学者阅读提示：
 * 这是所有网页都会注入的通用增强脚本。
 * 它负责读取 Kotlin 侧传入的配置，并把页面运行时、视频增强 callbacks 和公开 API 装配起来。
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
  const videoEnhancerCallbacksFactory = window.VideoBrowserVideoEnhancerCallbacks;

  const videoCallbacks = videoEnhancerCallbacksFactory.create({
    state: state,
    videoControlCoordinator: videoControlCoordinator,
    videoQueryTools: videoQueryTools,
    videoPlaybackTools: videoPlaybackTools,
    videoFullscreenTools: videoFullscreenTools,
    videoWakeTools: videoWakeTools,
    videoEnhancementTools: videoEnhancementTools,
    hasSiteVideoCapability: hasSiteVideoCapability,
    invokeSiteVideoCapability: invokeSiteVideoCapability,
    logVideoDiagnostic: logVideoDiagnostic,
    videoLogDetails: videoLogDetails,
    nativeBridge: nativeBridge,
    scriptletHooks: scriptletHooks
  });

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
      enhanceVideos: videoCallbacks.enhanceVideos,
      syncDocumentFullscreenState: videoCallbacks.syncDocumentFullscreenState,
      stopDirectionalPlayback: videoCallbacks.stopDirectionalPlayback,
      exitVideoFullscreen: videoCallbacks.exitVideoFullscreen
    }
  });
  videoCallbacks.setPageRuntime(pageRuntime);

  window.VideoBrowserEnhancer = enhancerApi.create({
    state: state,
    videoEnhancementTools: videoEnhancementTools,
    videoQueryTools: videoQueryTools,
    invokeSiteVideoCapability: invokeSiteVideoCapability,
    cleanupLegacyVideoOverlays: pageRuntime.cleanupLegacyVideoOverlays,
    installHooks: videoCallbacks.installHooks,
    runPageWork: pageRuntime.runPageWork,
    startWorkers: pageRuntime.startWorkers,
    exitVideoFullscreen: videoCallbacks.exitVideoFullscreen,
    seekVideoBy: videoCallbacks.seekVideoBy,
    seekVideoTo: videoCallbacks.seekVideoTo,
    reportPlaybackTimeline: videoCallbacks.reportPlaybackTimeline,
    togglePlayPause: videoCallbacks.togglePlayPause,
    wakeVideoControls: videoCallbacks.wakeVideoControls,
    activeFullscreenVideo: videoCallbacks.activeFullscreenVideo,
    applyVideoSpeed: videoCallbacks.applyVideoSpeed,
    startDirectionalPlayback: videoCallbacks.startDirectionalPlayback,
    stopDirectionalPlayback: videoCallbacks.stopDirectionalPlayback,
    startElementPicker: pageRuntime.startElementPicker,
    stopElementPicker: pageRuntime.stopElementPicker,
    suspendPageFeatures: pageRuntime.suspendPageFeatures,
    disposePageFeatures: pageRuntime.disposePageFeatures
  });
})();
