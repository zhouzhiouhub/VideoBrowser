/*
 * Public API factory for window.VideoBrowserEnhancer.
 *
 * enhancer_runtime.js owns page orchestration; this file owns the stable method
 * names called from Kotlin and injected page commands.
 */
(function () {
  const api = window.VideoBrowserEnhancerApi || {};
  window.VideoBrowserEnhancerApi = api;

  api.create = function (callbacks) {
    const state = callbacks.state;
    const videoEnhancementTools = callbacks.videoEnhancementTools;

    return {
      apply: function (config) {
        state.disposed = false;
        state.config = config || {};
        state.lastCleanupAt = 0;
        if (!state.config.builtInSearchResultPage) {
          callbacks.cleanupLegacyVideoOverlays();
          callbacks.installHooks();
        }
        callbacks.runPageWork();
        callbacks.startWorkers();
      },
      exitFullscreen: function () {
        callbacks.exitVideoFullscreen();
      },
      seekBy: function (offsetSeconds) {
        callbacks.seekVideoBy(callbacks.activeFullscreenVideo(), Number(offsetSeconds || 0));
      },
      seekTo: function (targetSeconds) {
        callbacks.seekVideoTo(callbacks.activeFullscreenVideo(), Number(targetSeconds || 0));
      },
      reportPlaybackTimeline: function () {
        callbacks.reportPlaybackTimeline(callbacks.activeFullscreenVideo());
      },
      togglePlayPause: function () {
        return callbacks.togglePlayPause();
      },
      wakeControls: function () {
        return callbacks.wakeVideoControls(callbacks.activeFullscreenVideo());
      },
      setPlaybackSpeed: function (speed) {
        videoEnhancementTools.setPlaybackSpeed(speed, state, {
          stopDirectionalPlayback: callbacks.stopDirectionalPlayback,
          activeFullscreenVideo: callbacks.activeFullscreenVideo,
          invokeSiteVideoCapability: callbacks.invokeSiteVideoCapability,
          videoQueryTools: callbacks.videoQueryTools,
          applyVideoSpeed: callbacks.applyVideoSpeed
        });
      },
      startDirectionalPlayback: function (direction) {
        callbacks.startDirectionalPlayback(direction);
      },
      stopDirectionalPlayback: function () {
        callbacks.stopDirectionalPlayback();
      },
      startElementPicker: function () {
        return callbacks.startElementPicker();
      },
      cancelElementPicker: function () {
        callbacks.stopElementPicker();
      },
      finishElementPicker: function () {
        callbacks.stopElementPicker();
      },
      suspend: function (options) {
        callbacks.suspendPageFeatures(options || {});
      },
      dispose: function (options) {
        callbacks.disposePageFeatures(options || {});
      }
    };
  };
})();
