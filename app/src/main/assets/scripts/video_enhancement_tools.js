/*
 * Shared video playback speed and quality preference operations.
 */
(function () {
  const tools = window.VideoBrowserVideoEnhancementTools || {};
  const enhancerState = window.VideoBrowserEnhancerState;
  const siteVideoCapabilityBroker = window.VideoBrowserSiteVideoCapabilityBroker;
  window.VideoBrowserVideoEnhancementTools = tools;

  tools.installPlaybackSpeedHooks = tools.installPlaybackSpeedHooks || function (video, state, options) {
    const targetState = state || {};
    const hookedVideos = enhancerState.ensureWeakSet(targetState, 'speedHookedVideos');
    if (!video || hookedVideos.has(video)) return;
    hookedVideos.add(video);

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
    if (siteVideoCapabilityBroker.hasFromOptions(config, video, 'setPlaybackSpeed')) {
      if (!tools.isFullscreenPlaybackTarget(video, state, config)) return false;
      const siteResult = siteVideoCapabilityBroker.invokeFromOptions(config, video, 'setPlaybackSpeed', [speed]);
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
    if (!siteVideoCapabilityBroker.hasFromOptions(config, video, 'preferBestQuality')) return false;

    enhancerState.ensureWeakMap(targetState, 'bestQualityAttempts');

    const intervalMs = Number(config.bestQualityAttemptIntervalMs || 30000);
    const now = Date.now();
    const lastAttempt = targetState.bestQualityAttempts.get(video);
    if (lastAttempt && lastAttempt.success) return true;
    if (lastAttempt && now - Number(lastAttempt.at || 0) < intervalMs) return false;

    targetState.bestQualityAttempts.set(video, { at: now, success: false });
    logVideoDiagnostic('quality-prefer-start', video, {}, config);

    const siteResult = siteVideoCapabilityBroker.invokeFromOptions(config, video, 'preferBestQuality', []);
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

  tools.setPlaybackSpeed = tools.setPlaybackSpeed || function (speed, state, options) {
    const targetState = state || {};
    const config = options || {};
    call(config, 'stopDirectionalPlayback');

    const normalizedSpeed = Number(speed || 1);
    targetState.fullscreenPlaybackSpeed =
      Number.isFinite(normalizedSpeed) && normalizedSpeed > 0 ? normalizedSpeed : 1;

    const video = activeVideo(config);
    if (video && !(document.fullscreenElement || document.webkitFullscreenElement)) {
      targetState.nativeFullscreenVideo = video;
    }

    const siteResult = siteVideoCapabilityBroker.invokeFromOptions(
      config,
      video,
      'setPlaybackSpeed',
      [targetState.fullscreenPlaybackSpeed]
    );
    if (siteResult.handled) return true;

    forEachVideo(config, function (targetVideo) {
      applyVideoSpeed(targetVideo, targetState, config);
    });
    return true;
  };

  function activeVideo(options) {
    const config = options || {};
    return typeof config.activeFullscreenVideo === 'function'
      ? config.activeFullscreenVideo()
      : null;
  }

  function applyVideoSpeed(video, state, options) {
    const config = options || {};
    if (typeof config.applyVideoSpeed === 'function') {
      config.applyVideoSpeed(video);
      return;
    }
    tools.applySpeed(video, state, config);
  }

  function forEachVideo(options, callback) {
    const config = options || {};
    if (config.videoQueryTools && typeof config.videoQueryTools.forEach === 'function') {
      config.videoQueryTools.forEach(callback);
    }
  }

  function call(callbacks, name) {
    if (callbacks && typeof callbacks[name] === 'function') {
      callbacks[name]();
    }
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
