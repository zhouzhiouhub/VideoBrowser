/*
 * Shared state holder for the page enhancer runtime.
 */
(function () {
  const manager = window.VideoBrowserEnhancerState || {};
  window.VideoBrowserEnhancerState = manager;

  manager.current = manager.current || function () {
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
    return manager.normalize(state);
  };

  manager.normalize = manager.normalize || function (state) {
    state.suppressMutationWork = false;
    state.lastCleanupAt = Number(state.lastCleanupAt || 0);
    if (!Number.isFinite(state.lastCleanupAt)) state.lastCleanupAt = 0;
    state.fullscreenHookedVideos = weakSetOrNew(state.fullscreenHookedVideos);
    state.speedHookedVideos = weakSetOrNew(state.speedHookedVideos);
    state.bestQualityAttempts = weakMapOrNew(state.bestQualityAttempts);
    state.nativeFullscreenVideo = state.nativeFullscreenVideo || null;
    state.documentFullscreenActive = Boolean(state.documentFullscreenActive);
    state.directionalPlayback = state.directionalPlayback || null;
    state.elementPicker = state.elementPicker || null;
    state.fullscreenPlaybackSpeed = Number(state.fullscreenPlaybackSpeed || 1);
    if (!Number.isFinite(state.fullscreenPlaybackSpeed) || state.fullscreenPlaybackSpeed <= 0) {
      state.fullscreenPlaybackSpeed = 1;
    }
    return state;
  };

  function weakSetOrNew(value) {
    return value && typeof value.add === 'function' ? value : new WeakSet();
  }

  function weakMapOrNew(value) {
    return value && typeof value.get === 'function' ? value : new WeakMap();
  }
})();
