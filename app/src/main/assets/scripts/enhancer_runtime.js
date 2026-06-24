/*
 * Shared runtime callbacks for the common enhancer API.
 *
 * This module owns page work scheduling, cleanup coordination, picker lifecycle,
 * and suspend/dispose behavior. common.js wires video-specific callbacks into it.
 */
(function () {
  const runtime = window.VideoBrowserEnhancerRuntime || {};
  const callbackTools = window.VideoBrowserCallbackTools || {};
  window.VideoBrowserEnhancerRuntime = runtime;

  runtime.create = runtime.create || function (options) {
    const config = options || {};
    const state = config.state || {};
    const pageCleanupCoordinator = config.pageCleanupCoordinator || {};
    const skipButtonTools = config.skipButtonTools || {};
    const pageLifecycleTools = config.pageLifecycleTools || {};
    const elementPicker = config.elementPicker || {};
    const videoQueryTools = config.videoQueryTools || {};
    const videoPlaybackTools = config.videoPlaybackTools || {};
    const videoControlCoordinator = config.videoControlCoordinator || {};
    const callbacks = config.callbacks || {};
    const normalCleanupIntervalMs = Number(config.normalCleanupIntervalMs || 3000);
    const activeVideoCleanupIntervalMs = Number(config.activeVideoCleanupIntervalMs || 15000);

    return {
      cleanupLegacyVideoOverlays: cleanupLegacyVideoOverlays,
      runWithMutationSuppressed: runWithMutationSuppressed,
      startElementPicker: startElementPicker,
      stopElementPicker: stopElementPicker,
      clickSkipButtons: clickSkipButtons,
      hasActiveVideo: hasActiveVideo,
      installFullscreenEventHooks: installFullscreenEventHooks,
      runPageWork: runPageWork,
      schedulePageWork: schedulePageWork,
      pausePageVideos: pausePageVideos,
      suspendPageFeatures: suspendPageFeatures,
      disposePageFeatures: disposePageFeatures,
      startWorkers: startWorkers
    };

    function cleanupLegacyVideoOverlays() {
      videoControlCoordinator.cleanupLegacyOverlays(state, {
        runWithMutationSuppressed: runWithMutationSuppressed
      });
    }

    function runWithMutationSuppressed(work) {
      return pageLifecycleTools.runWithMutationSuppressed(state, work);
    }

    function startElementPicker() {
      return elementPicker.start(state);
    }

    function stopElementPicker() {
      elementPicker.stop(state);
    }

    function clickSkipButtons() {
      if (!state.config.videoEnabled && !state.config.scriptletSkipButtonsEnabled) return;
      skipButtonTools.click();
    }

    function hasActiveVideo() {
      return videoQueryTools.hasActive();
    }

    function isBilibiliHost() {
      return /(\.|^)bilibili\.com$/i.test(location.hostname);
    }

    function installFullscreenEventHooks() {
      return pageLifecycleTools.installFullscreenEventHooks(state, {
        syncDocumentFullscreenState: syncDocumentFullscreenState,
        suspendPageFeatures: suspendPageFeatures,
        startWorkers: startWorkers,
        schedulePageWork: schedulePageWork
      });
    }

    function syncDocumentFullscreenState() {
      return callbackTools.call(callbacks, 'syncDocumentFullscreenState');
    }

    function runPageWork() {
      state.pendingWork = false;
      if (state.disposed) return;

      const now = Date.now();
      const cleanupInterval = hasActiveVideo() ? activeVideoCleanupIntervalMs : normalCleanupIntervalMs;
      state.lastWorkAt = now;
      if (state.config.builtInSearchResultPage) {
        pageCleanupCoordinator.applyEmbeddedSearchShell(state, {
          runWithMutationSuppressed: runWithMutationSuppressed
        });
      }
      if (state.config.cleanupEnabled) {
        pageCleanupCoordinator.runGenerated(state, {
          now: now,
          isBilibiliHost: isBilibiliHost
        });
        if (now - Number(state.lastCleanupAt || 0) >= cleanupInterval) {
          state.lastCleanupAt = now;
          pageCleanupCoordinator.run(state, {
            isBilibiliHost: isBilibiliHost,
            runWithMutationSuppressed: runWithMutationSuppressed
          });
        }
      } else {
        pageCleanupCoordinator.applyDisabledState(state);
      }
      clickSkipButtons();
      callbackTools.call(callbacks, 'enhanceVideos');
    }

    function schedulePageWork() {
      return pageLifecycleTools.schedulePageWork(state, {
        hasActiveVideo: hasActiveVideo,
        runPageWork: runPageWork
      });
    }

    function pausePageVideos() {
      videoPlaybackTools.pauseAll(videoQueryTools);
    }

    function suspendPageFeatures(options) {
      callbackTools.call(callbacks, 'stopDirectionalPlayback');
      stopElementPicker();
      if (options && options.pauseVideos) {
        pausePageVideos();
      }
      callbackTools.call(callbacks, 'exitVideoFullscreen');
      cleanupLegacyVideoOverlays();
    }

    function disposePageFeatures(options) {
      return pageLifecycleTools.disposePageFeatures(state, options, {
        suspendPageFeatures: suspendPageFeatures
      });
    }

    function startWorkers() {
      return pageLifecycleTools.startWorkers(state, {
        shouldDisableObserver: isBilibiliHost,
        schedulePageWork: schedulePageWork
      });
    }
  };
})();
