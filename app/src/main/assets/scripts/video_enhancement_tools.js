/*
 * Shared video playback speed and quality preference operations.
 */
(function () {
  const tools = window.VideoBrowserVideoEnhancementTools || {};
  window.VideoBrowserVideoEnhancementTools = tools;

  tools.installPlaybackSpeedHooks = tools.installPlaybackSpeedHooks || function (video, state, options) {
    const targetState = state || {};
    if (!targetState.speedHookedVideos || typeof targetState.speedHookedVideos.add !== 'function') {
      targetState.speedHookedVideos = new WeakSet();
    }
    if (!video || targetState.speedHookedVideos.has(video)) return;
    targetState.speedHookedVideos.add(video);

    const callbacks = options || {};
    const enforceSpeed = function () {
      window.setTimeout(function () {
        if (typeof callbacks.applyVideoSpeed === 'function') {
          callbacks.applyVideoSpeed(video);
        } else {
          tools.applySpeed(video, targetState, callbacks);
        }
      }, 0);
    };
    ['loadedmetadata', 'play', 'playing', 'ratechange', 'webkitbeginfullscreen'].forEach(function (eventName) {
      video.addEventListener(eventName, enforceSpeed);
    });
  };

  tools.currentFullscreenPlaybackSpeed = tools.currentFullscreenPlaybackSpeed || function (state) {
    const targetState = state || {};
    const speed = Number(targetState.fullscreenPlaybackSpeed || 1);
    return Number.isFinite(speed) && speed > 0 ? speed : 1;
  };

  tools.isFullscreenPlaybackTarget = tools.isFullscreenPlaybackTarget || function (video, state, options) {
    const targetState = state || {};
    const callbacks = options || {};
    const isFullscreen = typeof callbacks.isVideoFullscreen === 'function'
      ? callbacks.isVideoFullscreen(video)
      : false;
    return Boolean(video && (isFullscreen || targetState.nativeFullscreenVideo === video));
  };

  tools.desiredSpeed = tools.desiredSpeed || function (video, state, options) {
    const targetState = state || {};
    const speed = tools.currentFullscreenPlaybackSpeed(targetState);
    if (!targetState.config || !targetState.config.videoEnabled) return 1;
    if (!tools.isFullscreenPlaybackTarget(video, targetState, options)) return 1;
    return Number.isFinite(speed) && speed > 0 ? speed : 1;
  };

  tools.applySpeed = tools.applySpeed || function (video, state, options) {
    const config = options || {};
    const speed = tools.desiredSpeed(video, state, config);
    if (hasSiteVideoCapability(video, 'setPlaybackSpeed', config)) {
      if (!tools.isFullscreenPlaybackTarget(video, state, config)) return false;
      const siteResult = invokeSiteVideoCapability(video, 'setPlaybackSpeed', [speed], config);
      if (siteResult.handled) return true;
    }
    try {
      if (Math.abs(Number(video.playbackRate || 1) - speed) > 0.01) {
        video.playbackRate = speed;
      }
      video.defaultPlaybackRate = speed;
      return true;
    } catch (_) {
      return false;
    }
  };

  tools.preferBestQuality = tools.preferBestQuality || function (video, state, options) {
    const targetState = state || {};
    const config = options || {};
    if (!targetState.config || !targetState.config.videoEnabled || !video || !video.isConnected) return false;
    if (!hasSiteVideoCapability(video, 'preferBestQuality', config)) return false;

    if (!targetState.bestQualityAttempts || typeof targetState.bestQualityAttempts.get !== 'function') {
      targetState.bestQualityAttempts = new WeakMap();
    }

    const intervalMs = Number(config.bestQualityAttemptIntervalMs || 30000);
    const now = Date.now();
    const lastAttempt = targetState.bestQualityAttempts.get(video);
    if (lastAttempt && lastAttempt.success) return true;
    if (lastAttempt && now - Number(lastAttempt.at || 0) < intervalMs) return false;

    targetState.bestQualityAttempts.set(video, { at: now, success: false });
    logVideoDiagnostic('quality-prefer-start', video, {}, config);

    const siteResult = invokeSiteVideoCapability(video, 'preferBestQuality', [], config);
    if (siteResult.handled) {
      const success = siteResult.value !== false;
      targetState.bestQualityAttempts.set(video, { at: now, success: success });
      logVideoDiagnostic(
        success ? 'quality-prefer-success' : 'quality-prefer-unavailable',
        video,
        { result: siteResult.value },
        config
      );
      return success;
    }

    logVideoDiagnostic('quality-prefer-unavailable', video, {
      result: 'no-site-handler'
    }, config);
    return false;
  };

  function hasSiteVideoCapability(video, action, options) {
    const config = options || {};
    return typeof config.hasSiteVideoCapability === 'function'
      ? config.hasSiteVideoCapability(video, action)
      : false;
  }

  function invokeSiteVideoCapability(video, action, args, options) {
    const config = options || {};
    if (typeof config.invokeSiteVideoCapability === 'function') {
      return config.invokeSiteVideoCapability(video, action, args);
    }
    return { handled: false, value: undefined };
  }

  function logVideoDiagnostic(event, video, extra, options) {
    const config = options || {};
    if (typeof config.logVideoDiagnostic !== 'function') return;
    const details = typeof config.videoLogDetails === 'function'
      ? config.videoLogDetails(video, extra)
      : (extra || {});
    config.logVideoDiagnostic(event, details);
  }
})();
