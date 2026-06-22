/*
 * Video enhancer callback factory.
 *
 * common.js owns page-level wiring; this module owns the video callback functions
 * that bridge shared video tools, site capabilities and the native bridge.
 */
(function () {
  const factory = window.VideoBrowserVideoEnhancerCallbacks || {};
  window.VideoBrowserVideoEnhancerCallbacks = factory;

  factory.create = factory.create || function (dependencies) {
    const state = dependencies.state;
    const videoControlCoordinator = dependencies.videoControlCoordinator;
    const videoQueryTools = dependencies.videoQueryTools;
    const videoPlaybackTools = dependencies.videoPlaybackTools;
    const videoFullscreenTools = dependencies.videoFullscreenTools;
    const videoWakeTools = dependencies.videoWakeTools;
    const videoEnhancementTools = dependencies.videoEnhancementTools;
    const hasSiteVideoCapability = dependencies.hasSiteVideoCapability;
    const invokeSiteVideoCapability = dependencies.invokeSiteVideoCapability;
    const logVideoDiagnostic = dependencies.logVideoDiagnostic;
    const videoLogDetails = dependencies.videoLogDetails;
    const nativeBridge = dependencies.nativeBridge;
    const scriptletHooks = dependencies.scriptletHooks;
    let pageRuntime = dependencies.pageRuntime || null;

    function enhanceVideos() {
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

    function installPlaybackSpeedHooks(video) {
      return videoEnhancementTools.installPlaybackSpeedHooks(video, state, {
        applyVideoSpeed: applyVideoSpeed
      });
    }

    function desiredVideoSpeed(video) {
      return videoEnhancementTools.desiredSpeed(video, state, {
        isVideoFullscreen: isVideoFullscreen
      });
    }

    function currentFullscreenPlaybackSpeed() {
      return videoEnhancementTools.currentFullscreenPlaybackSpeed(state);
    }

    function isFullscreenPlaybackTarget(video) {
      return videoEnhancementTools.isFullscreenPlaybackTarget(video, state, {
        isVideoFullscreen: isVideoFullscreen
      });
    }

    function applyVideoSpeed(video) {
      return videoEnhancementTools.applySpeed(video, state, {
        hasSiteVideoCapability: hasSiteVideoCapability,
        invokeSiteVideoCapability: invokeSiteVideoCapability,
        isVideoFullscreen: isVideoFullscreen
      });
    }

    function preferBestVideoQuality(video) {
      return videoEnhancementTools.preferBestQuality(video, state, {
        hasSiteVideoCapability: hasSiteVideoCapability,
        invokeSiteVideoCapability: invokeSiteVideoCapability,
        logVideoDiagnostic: logVideoDiagnostic,
        videoLogDetails: videoLogDetails
      });
    }

    function wakeVideoControls(video) {
      return videoWakeTools.wake(video, {
        activeFullscreenVideo: activeFullscreenVideo,
        enableVideoControls: videoControlCoordinator.enableControls,
        reportPlaybackTimeline: reportPlaybackTimeline
      });
    }

    function videoTimeline(video) {
      return videoPlaybackTools.timeline(video);
    }

    function reportPlaybackTimeline(video) {
      const target = video || activeFullscreenVideo();
      videoPlaybackTools.reportTimeline(target);
    }

    function seekVideo(video, sliderValue) {
      videoPlaybackTools.seek(video, sliderValue, {
        reportPlaybackTimeline: reportPlaybackTimeline
      });
    }

    function seekVideoTo(video, targetSeconds) {
      videoPlaybackTools.seekTo(video, targetSeconds, {
        invokeSiteVideoCapability: invokeSiteVideoCapability,
        reportPlaybackTimeline: reportPlaybackTimeline
      });
    }

    function seekVideoBy(video, offsetSeconds) {
      videoPlaybackTools.seekBy(video, offsetSeconds, {
        invokeSiteVideoCapability: invokeSiteVideoCapability,
        reportPlaybackTimeline: reportPlaybackTimeline
      });
    }

    function activeFullscreenVideo() {
      return videoFullscreenTools.activeVideo(state, videoQueryTools);
    }

    function requestVideoFullscreen(video) {
      videoFullscreenTools.request(video, {
        isVideoFullscreen: isVideoFullscreen,
        exitVideoFullscreen: exitVideoFullscreen,
        reportPlaybackTimeline: reportPlaybackTimeline,
        enterNativeFullscreen: enterNativeFullscreen
      });
    }

    function exitVideoFullscreen() {
      videoFullscreenTools.exit(state, {
        stopDirectionalPlayback: stopDirectionalPlayback,
        videoQueryTools: videoQueryTools,
        applyVideoSpeed: applyVideoSpeed,
        exitNativeFullscreen: exitNativeFullscreen
      });
    }

    function isVideoFullscreen(video) {
      return videoFullscreenTools.isVideoFullscreen(video);
    }

    function enterNativeFullscreen() {
      videoFullscreenTools.enterNative(nativeBridge);
    }

    function exitNativeFullscreen() {
      videoFullscreenTools.exitNative(nativeBridge);
    }

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

    function installHooks() {
      if (!pageRuntime) return;
      scriptletHooks.install(state, {
        installFullscreenEventHooks: pageRuntime.installFullscreenEventHooks
      });
    }

    function togglePlayPause() {
      const video = activeFullscreenVideo();
      return videoPlaybackTools.togglePlayPause(video, {
        invokeSiteVideoCapability: invokeSiteVideoCapability
      });
    }

    function startDirectionalPlayback(direction) {
      return videoPlaybackTools.startDirectional(direction, state, {
        activeFullscreenVideo: activeFullscreenVideo,
        currentFullscreenPlaybackSpeed: currentFullscreenPlaybackSpeed,
        applyVideoSpeed: applyVideoSpeed,
        seekVideoBy: seekVideoBy,
        videoQueryTools: videoQueryTools
      });
    }

    function stopDirectionalPlayback() {
      return videoPlaybackTools.stopDirectional(state, {
        activeFullscreenVideo: activeFullscreenVideo,
        applyVideoSpeed: applyVideoSpeed,
        videoQueryTools: videoQueryTools
      });
    }

    return {
      setPageRuntime: function (runtime) {
        pageRuntime = runtime;
      },
      enhanceVideos: enhanceVideos,
      installVideoFullscreenHooks: installVideoFullscreenHooks,
      installPlaybackSpeedHooks: installPlaybackSpeedHooks,
      desiredVideoSpeed: desiredVideoSpeed,
      currentFullscreenPlaybackSpeed: currentFullscreenPlaybackSpeed,
      isFullscreenPlaybackTarget: isFullscreenPlaybackTarget,
      applyVideoSpeed: applyVideoSpeed,
      preferBestVideoQuality: preferBestVideoQuality,
      wakeVideoControls: wakeVideoControls,
      videoTimeline: videoTimeline,
      reportPlaybackTimeline: reportPlaybackTimeline,
      seekVideo: seekVideo,
      seekVideoTo: seekVideoTo,
      seekVideoBy: seekVideoBy,
      activeFullscreenVideo: activeFullscreenVideo,
      requestVideoFullscreen: requestVideoFullscreen,
      exitVideoFullscreen: exitVideoFullscreen,
      isVideoFullscreen: isVideoFullscreen,
      enterNativeFullscreen: enterNativeFullscreen,
      exitNativeFullscreen: exitNativeFullscreen,
      syncDocumentFullscreenState: syncDocumentFullscreenState,
      installHooks: installHooks,
      togglePlayPause: togglePlayPause,
      startDirectionalPlayback: startDirectionalPlayback,
      stopDirectionalPlayback: stopDirectionalPlayback
    };
  };
})();
