/*
 * Shared page lifecycle, scheduling, and worker orchestration.
 */
(function () {
  const tools = window.VideoBrowserPageLifecycleTools || {};
  const callbackTools = window.VideoBrowserCallbackTools;
  window.VideoBrowserPageLifecycleTools = tools;

  tools.runWithMutationSuppressed = tools.runWithMutationSuppressed || function (state, work) {
    const targetState = state || {};
    targetState.suppressMutationWork = true;
    try {
      return typeof work === 'function' ? work() : undefined;
    } finally {
      window.setTimeout(function () {
        targetState.suppressMutationWork = false;
      }, 0);
    }
  };

  tools.runWithOptionalMutationSuppression = tools.runWithOptionalMutationSuppression || function (options, work) {
    const config = options || {};
    if (typeof config.runWithMutationSuppressed === 'function') {
      return config.runWithMutationSuppressed(work);
    }
    return typeof work === 'function' ? work() : undefined;
  };

  tools.installFullscreenEventHooks = tools.installFullscreenEventHooks || function (state, options) {
    const targetState = state || {};
    const callbacks = options || {};
    if (typeof callbacks.syncDocumentFullscreenState === 'function') {
      document.addEventListener('fullscreenchange', callbacks.syncDocumentFullscreenState);
      document.addEventListener('webkitfullscreenchange', callbacks.syncDocumentFullscreenState);
    }
    window.addEventListener('pagehide', function () {
      callbackTools.call(callbacks, 'suspendPageFeatures', { pauseVideos: true });
    });
    window.addEventListener('pageshow', function () {
      targetState.disposed = false;
      callbackTools.call(callbacks, 'startWorkers');
      callbackTools.call(callbacks, 'schedulePageWork');
    });
  };

  tools.schedulePageWork = tools.schedulePageWork || function (state, options) {
    const targetState = state || {};
    const callbacks = options || {};
    if (targetState.disposed || targetState.pendingWork || typeof callbacks.runPageWork !== 'function') {
      return false;
    }

    const elapsed = Date.now() - Number(targetState.lastWorkAt || 0);
    const active = typeof callbacks.hasActiveVideo === 'function' && callbacks.hasActiveVideo();
    const workDelay = active
      ? Number(callbacks.activeVideoWorkDelayMs || 750)
      : Number(callbacks.normalWorkDelayMs || 250);
    const minDelay = Number(callbacks.minDelayMs || 60);
    const delay = Math.max(minDelay, workDelay - elapsed);
    targetState.pendingWork = true;
    window.setTimeout(callbacks.runPageWork, delay);
    return true;
  };

  tools.disposePageFeatures = tools.disposePageFeatures || function (state, options, callbacks) {
    const targetState = state || {};
    const config = callbacks || {};
    callbackTools.call(config, 'suspendPageFeatures', options);
    targetState.disposed = true;
    targetState.pendingWork = false;

    if (targetState.observer) {
      targetState.observer.disconnect();
      targetState.observer = null;
    }
    if (targetState.intervalId) {
      window.clearInterval(targetState.intervalId);
      targetState.intervalId = null;
    }
    return true;
  };

  tools.startWorkers = tools.startWorkers || function (state, options) {
    const targetState = state || {};
    const callbacks = options || {};
    if (targetState.disposed) return false;

    const disableObserver = typeof callbacks.shouldDisableObserver === 'function' &&
      callbacks.shouldDisableObserver();
    if (disableObserver && targetState.observer) {
      targetState.observer.disconnect();
      targetState.observer = null;
    }

    if (!targetState.observer && document.documentElement && !disableObserver) {
      targetState.observer = new MutationObserver(function () {
        if (targetState.suppressMutationWork) return;
        callbackTools.call(callbacks, 'schedulePageWork');
      });
      targetState.observer.observe(document.documentElement, {
        childList: true,
        subtree: true
      });
    }

    if (!targetState.intervalId) {
      targetState.intervalId = window.setInterval(function () {
        callbackTools.call(callbacks, 'schedulePageWork');
      }, Number(callbacks.intervalMs || 1500));
    }
    return true;
  };
})();
