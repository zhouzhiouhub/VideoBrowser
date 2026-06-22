/*
 * Shared video playback timeline, seeking, and play/pause operations.
 */
(function () {
  const tools = window.VideoBrowserVideoPlaybackTools || {};
  const nativeBridge = window.VideoBrowserNativeBridge || {};
  const siteVideoCapabilityBroker = window.VideoBrowserSiteVideoCapabilityBroker;
  const callbackTools = window.VideoBrowserCallbackTools;
  window.VideoBrowserVideoPlaybackTools = tools;

  tools.timeline = tools.timeline || function (video) {
    if (!video) return { canSeek: false, start: 0, end: 0 };
    const duration = video.duration;
    if (Number.isFinite(duration) && duration > 0) {
      return { canSeek: true, start: 0, end: duration };
    }

    const seekable = video.seekable;
    if (!seekable || !seekable.length) {
      return { canSeek: false, start: 0, end: 0 };
    }

    const start = seekable.start(0);
    const end = seekable.end(seekable.length - 1);
    if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
      return { canSeek: false, start: 0, end: 0 };
    }
    return { canSeek: true, start: start, end: end };
  };

  tools.reportTimeline = tools.reportTimeline || function (video) {
    if (!video) return;
    const timeline = tools.timeline(video);
    const position = Number(video.currentTime || 0);
    const duration = timeline.canSeek ? timeline.end : Number(video.duration || -1);
    if (typeof nativeBridge.updatePlaybackTimeline === 'function') {
      nativeBridge.updatePlaybackTimeline(
        Number.isFinite(position) && position >= 0 ? position * 1000 : -1,
        Number.isFinite(duration) && duration > 0 ? duration * 1000 : -1
      );
    }
  };

  tools.play = tools.play || function (video) {
    if (!video || typeof video.play !== 'function') return false;
    try {
      const result = video.play();
      if (result && typeof result.catch === 'function') {
        result.catch(function () {});
      }
      return true;
    } catch (_) {
      return false;
    }
  };

  tools.pause = tools.pause || function (video) {
    if (!video || typeof video.pause !== 'function') return false;
    try {
      video.pause();
      return true;
    } catch (_) {
      return false;
    }
  };

  tools.pauseAll = tools.pauseAll || function (videoQueryTools) {
    if (!videoQueryTools || typeof videoQueryTools.forEach !== 'function') return;
    videoQueryTools.forEach(function (video) {
      tools.pause(video);
    });
  };

  tools.seek = tools.seek || function (video, sliderValue, options) {
    if (!video) return false;
    const timeline = tools.timeline(video);
    if (!timeline.canSeek) return false;
    const ratio = Math.max(0, Math.min(1, Number(sliderValue) / 1000));
    const targetTime = timeline.start + ratio * (timeline.end - timeline.start);
    setVideoTime(video, targetTime);
    reportPlaybackTimeline(video, options);
    return true;
  };

  tools.seekTo = tools.seekTo || function (video, targetSeconds, options) {
    if (!video || !Number.isFinite(targetSeconds)) return false;
    const siteResult = siteVideoCapabilityBroker.invokeFromOptions(options, video, 'seekTo', [targetSeconds]);
    if (siteResult.handled) {
      reportPlaybackTimeline(video, options);
      return true;
    }

    const timeline = tools.timeline(video);
    const targetTime = timeline.canSeek
      ? Math.max(timeline.start, Math.min(timeline.end, targetSeconds))
      : Math.max(0, targetSeconds);
    setVideoTime(video, targetTime);
    reportPlaybackTimeline(video, options);
    return true;
  };

  tools.seekBy = tools.seekBy || function (video, offsetSeconds, options) {
    if (!video || !Number.isFinite(offsetSeconds)) return false;
    const siteResult = siteVideoCapabilityBroker.invokeFromOptions(options, video, 'seekBy', [offsetSeconds]);
    if (siteResult.handled) {
      reportPlaybackTimeline(video, options);
      return true;
    }

    const timeline = tools.timeline(video);
    const currentTime = Number(video.currentTime || 0);
    const targetTime = timeline.canSeek
      ? Math.max(timeline.start, Math.min(timeline.end, currentTime + offsetSeconds))
      : Math.max(0, currentTime + offsetSeconds);
    setVideoTime(video, targetTime);
    reportPlaybackTimeline(video, options);
    return true;
  };

  tools.togglePlayPause = tools.togglePlayPause || function (video, options) {
    if (!video) return false;
    const siteResult = siteVideoCapabilityBroker.invokeFromOptions(options, video, 'togglePlayPause', []);
    if (siteResult.handled) return siteResult.value;
    if (video.paused || video.ended) {
      try {
        if (video.ended) video.currentTime = 0;
      } catch (_) {}
      tools.play(video);
      return true;
    }
    tools.pause(video);
    return false;
  };

  tools.startDirectional = tools.startDirectional || function (direction, state, options) {
    const targetState = state || {};
    const callbacks = options || {};
    const video = activeVideo(callbacks);
    if (!video) return false;
    tools.stopDirectional(targetState, callbacks);

    const normalizedDirection = Number(direction) < 0 ? -1 : 1;
    const previousSpeed = currentPlaybackSpeed(targetState, callbacks);
    targetState.directionalPlayback = {
      video: video,
      direction: normalizedDirection,
      previousSpeed: previousSpeed,
      wasPaused: Boolean(video.paused),
      intervalId: null
    };

    if (normalizedDirection > 0) {
      targetState.fullscreenPlaybackSpeed = 2;
      callbackTools.call(callbacks, 'applyVideoSpeed', video);
      tools.play(video);
      return true;
    }

    tools.pause(video);
    seekBy(video, -0.5, callbacks);
    targetState.directionalPlayback.intervalId = window.setInterval(function () {
      const scan = targetState.directionalPlayback;
      if (!scan || scan.direction >= 0 || !scan.video || !scan.video.isConnected) {
        tools.stopDirectional(targetState, callbacks);
        return;
      }
      seekBy(scan.video, -0.5, callbacks);
    }, 250);
    return true;
  };

  tools.stopDirectional = tools.stopDirectional || function (state, options) {
    const targetState = state || {};
    const callbacks = options || {};
    const scan = targetState.directionalPlayback;
    if (!scan) return false;
    if (scan.intervalId) {
      window.clearInterval(scan.intervalId);
    }
    targetState.directionalPlayback = null;
    targetState.fullscreenPlaybackSpeed = Number.isFinite(Number(scan.previousSpeed)) && Number(scan.previousSpeed) > 0
      ? Number(scan.previousSpeed)
      : 1;

    const video = scan.video && scan.video.isConnected ? scan.video : activeVideo(callbacks);
    if (video) {
      callbackTools.call(callbacks, 'applyVideoSpeed', video);
      if (scan.wasPaused) {
        tools.pause(video);
      } else {
        tools.play(video);
      }
    }
    if (callbacks.videoQueryTools && typeof callbacks.videoQueryTools.forEach === 'function') {
      callbacks.videoQueryTools.forEach(callbacks.applyVideoSpeed);
    }
    return true;
  };

  function reportPlaybackTimeline(video, options) {
    const config = options || {};
    if (typeof config.reportPlaybackTimeline === 'function') {
      config.reportPlaybackTimeline(video);
      return;
    }
    tools.reportTimeline(video);
  }

  function setVideoTime(video, targetTime) {
    try {
      if (typeof video.fastSeek === 'function') {
        video.fastSeek(targetTime);
      } else {
        video.currentTime = targetTime;
      }
    } catch (_) {
      try { video.currentTime = targetTime; } catch (__) {}
    }
  }

  function activeVideo(options) {
    const config = options || {};
    return typeof config.activeFullscreenVideo === 'function'
      ? config.activeFullscreenVideo()
      : null;
  }

  function currentPlaybackSpeed(state, options) {
    const config = options || {};
    if (typeof config.currentFullscreenPlaybackSpeed === 'function') {
      return config.currentFullscreenPlaybackSpeed();
    }
    const speed = Number((state && state.fullscreenPlaybackSpeed) || 1);
    return Number.isFinite(speed) && speed > 0 ? speed : 1;
  }

  function seekBy(video, offsetSeconds, options) {
    const config = options || {};
    if (typeof config.seekVideoBy === 'function') {
      config.seekVideoBy(video, offsetSeconds);
      return true;
    }
    return tools.seekBy(video, offsetSeconds, config);
  }
})();
