/*
 * Shared video fullscreen state and browser fullscreen operations.
 */
(function () {
  const tools = window.VideoBrowserVideoFullscreenTools || {};
  const enhancerState = window.VideoBrowserEnhancerState;
  window.VideoBrowserVideoFullscreenTools = tools;

  tools.activeVideo = tools.activeVideo || function (state, videoQueryTools) {
    if (state && state.nativeFullscreenVideo && state.nativeFullscreenVideo.isConnected) {
      return state.nativeFullscreenVideo;
    }
    const fullscreenElement = document.fullscreenElement || document.webkitFullscreenElement;
    if (fullscreenElement) {
      if (fullscreenElement.tagName && fullscreenElement.tagName.toLowerCase() === 'video') {
        return fullscreenElement;
      }
      if (typeof fullscreenElement.querySelector === 'function') {
        const video = fullscreenElement.querySelector('video');
        if (video) return video;
      }
    }
    const videos = videoQueryTools && typeof videoQueryTools.all === 'function'
      ? videoQueryTools.all()
      : [];
    return videos.find(function (video) {
      return video && video.isConnected && !video.paused && !video.ended;
    }) || videos[0] || null;
  };

  tools.request = tools.request || function (video, options) {
    const callbacks = options || {};
    if (typeof callbacks.isVideoFullscreen === 'function' && callbacks.isVideoFullscreen(video)) {
      if (typeof callbacks.exitVideoFullscreen === 'function') callbacks.exitVideoFullscreen();
      return false;
    }

    if (typeof callbacks.reportPlaybackTimeline === 'function') {
      callbacks.reportPlaybackTimeline(video);
    }

    const target = video && (video.parentElement || video);
    if (!video || !target) return false;
    const requesters = [
      { element: target, request: target.requestFullscreen || target.webkitRequestFullscreen },
      { element: video, request: video.requestFullscreen || video.webkitRequestFullscreen || video.webkitEnterFullscreen }
    ];
    const requested = requesters.some(function (requester) {
      if (typeof requester.request !== 'function') return false;
      try {
        requester.request.call(requester.element);
        return true;
      } catch (_) {
        return false;
      }
    });
    if (requested && typeof callbacks.enterNativeFullscreen === 'function') {
      callbacks.enterNativeFullscreen();
    }
    return requested;
  };

  tools.exit = tools.exit || function (state, options) {
    const callbacks = options || {};
    if (typeof callbacks.stopDirectionalPlayback === 'function') {
      callbacks.stopDirectionalPlayback();
    }
    if (state) {
      state.nativeFullscreenVideo = null;
      state.documentFullscreenActive = false;
      state.fullscreenPlaybackSpeed = 1;
    }
    if (callbacks.videoQueryTools && typeof callbacks.videoQueryTools.forEach === 'function') {
      callbacks.videoQueryTools.forEach(callbacks.applyVideoSpeed);
    }
    if (document.fullscreenElement && typeof document.exitFullscreen === 'function') {
      try { document.exitFullscreen(); } catch (_) {}
    } else if (document.webkitFullscreenElement && typeof document.webkitExitFullscreen === 'function') {
      try { document.webkitExitFullscreen(); } catch (__) {}
    }
    if (typeof callbacks.exitNativeFullscreen === 'function') {
      callbacks.exitNativeFullscreen();
    }
  };

  tools.isVideoFullscreen = tools.isVideoFullscreen || function (video) {
    return video && (
      document.fullscreenElement === video ||
      document.fullscreenElement === video.parentElement ||
      document.webkitFullscreenElement === video ||
      document.webkitFullscreenElement === video.parentElement
    );
  };

  tools.enterNative = tools.enterNative || function (nativeBridge) {
    if (nativeBridge && typeof nativeBridge.enterFullscreen === 'function') {
      nativeBridge.enterFullscreen();
    }
  };

  tools.exitNative = tools.exitNative || function (nativeBridge) {
    if (nativeBridge && typeof nativeBridge.exitFullscreen === 'function') {
      nativeBridge.exitFullscreen();
    }
  };

  tools.installVideoHooks = tools.installVideoHooks || function (video, state, options) {
    const targetState = state || {};
    const callbacks = options || {};
    const hookedVideos = enhancerState.ensureWeakSet(targetState, 'fullscreenHookedVideos');
    if (!video || hookedVideos.has(video)) return;
    hookedVideos.add(video);

    const timelineReporter = function () {
      const position = Number(video.currentTime || 0);
      if (isVideoFullscreen(video, callbacks) ||
        targetState.nativeFullscreenVideo === video ||
        !video.paused ||
        position > 0
      ) {
        call(callbacks, 'reportPlaybackTimeline', video);
      }
    };

    video.addEventListener('webkitbeginfullscreen', function () {
      targetState.nativeFullscreenVideo = video;
      call(callbacks, 'applyVideoSpeed', video);
      call(callbacks, 'reportPlaybackTimeline', video);
      call(callbacks, 'enterNativeFullscreen');
    });

    video.addEventListener('webkitendfullscreen', function () {
      call(callbacks, 'stopDirectionalPlayback');
      targetState.nativeFullscreenVideo = null;
      targetState.documentFullscreenActive = false;
      targetState.fullscreenPlaybackSpeed = 1;
      call(callbacks, 'applyVideoSpeed', video);
      call(callbacks, 'exitNativeFullscreen');
    });

    video.addEventListener('dblclick', function () {
      call(callbacks, 'requestVideoFullscreen', video);
    });

    ['loadedmetadata', 'durationchange', 'timeupdate', 'seeked', 'play', 'playing'].forEach(function (eventName) {
      video.addEventListener(eventName, timelineReporter);
    });
  };

  tools.syncDocumentState = tools.syncDocumentState || function (state, options) {
    const callbacks = options || {};
    const hasDocumentFullscreen = Boolean(document.fullscreenElement || document.webkitFullscreenElement);
    if (hasDocumentFullscreen) {
      state.documentFullscreenActive = true;
      state.nativeFullscreenVideo = callbacks.activeFullscreenVideo();
      callbacks.reportPlaybackTimeline(state.nativeFullscreenVideo);
      callbacks.videoQueryTools.forEach(callbacks.applyVideoSpeed);
      callbacks.enterNativeFullscreen();
      return;
    }

    if (!state.documentFullscreenActive &&
      state.nativeFullscreenVideo &&
      state.nativeFullscreenVideo.isConnected
    ) {
      callbacks.reportPlaybackTimeline(state.nativeFullscreenVideo);
      callbacks.videoQueryTools.forEach(callbacks.applyVideoSpeed);
      return;
    }

    state.documentFullscreenActive = false;
    callbacks.stopDirectionalPlayback();
    state.nativeFullscreenVideo = null;
    state.fullscreenPlaybackSpeed = 1;
    callbacks.videoQueryTools.forEach(callbacks.applyVideoSpeed);
    callbacks.exitNativeFullscreen();
  };

  function isVideoFullscreen(video, callbacks) {
    return typeof callbacks.isVideoFullscreen === 'function'
      ? callbacks.isVideoFullscreen(video)
      : tools.isVideoFullscreen(video);
  }

  function call(callbacks, name, value) {
    if (typeof callbacks[name] === 'function') {
      callbacks[name](value);
    }
  }
})();
