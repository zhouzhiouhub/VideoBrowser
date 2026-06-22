/*
 * Bilibili-specific video capability bridge for the common broker.
 */
(function () {
  var tools = window.VideoBrowserBilibiliVideoCapabilities || {};
  window.VideoBrowserBilibiliVideoCapabilities = tools;

  tools.create = tools.create || function (adapterTools, playerApi, qualityTools, overlayCleanup) {
    var adapter = adapterTools || {};
    var api = playerApi || {};
    var quality = qualityTools || {};
    var overlay = overlayCleanup || {};

    return {
      supports: function (video) {
        return Boolean(video && video.isConnected);
      },
      canUse: function (action, video) {
        if (action === 'enableControls') return true;
        if (action === 'preferBestQuality') return true;
        return hasPlayerMethod(api, action, video);
      },
      enableControls: function (video) {
        if (typeof adapter.removeNativeVideoControls === 'function') {
          adapter.removeNativeVideoControls(video);
        }
        hideVideoPlayPauseOverlays(adapter, overlay);
        return Boolean(video);
      },
      togglePlayPause: function (video) {
        var methodNames = playerMethodsFor(api, 'togglePlayPause', video);
        var result = callPlayerMethod(api, methodNames, []);
        if (!result) return null;
        return video && (video.paused || video.ended);
      },
      seekBy: function (video, offsetSeconds) {
        var offset = Number(offsetSeconds);
        if (!Number.isFinite(offset)) return null;
        var target = currentVideoTime(api, video) + offset;
        return handledValue(
          api,
          callPlayerMethod(api, ['seek', 'seekTo', 'setCurrentTime'], [target]),
          true
        );
      },
      seekTo: function (video, targetSeconds) {
        var target = Number(targetSeconds);
        if (!Number.isFinite(target)) return null;
        return handledValue(
          api,
          callPlayerMethod(api, ['seek', 'seekTo', 'setCurrentTime'], [target]),
          true
        );
      },
      setPlaybackSpeed: function (video, speed) {
        var normalizedSpeed = Number(speed);
        if (!Number.isFinite(normalizedSpeed) || normalizedSpeed <= 0) return null;
        return handledValue(
          api,
          callPlayerMethod(api, ['setPlaybackRate', 'setPlaybackSpeed'], [normalizedSpeed]),
          true
        );
      },
      preferBestQuality: function () {
        return typeof quality.preferBestQuality === 'function' &&
          quality.preferBestQuality(adapter, api);
      }
    };
  };

  function hideVideoPlayPauseOverlays(adapterTools, overlayCleanup) {
    if (typeof overlayCleanup.hideVideoPlayPauseOverlays === 'function') {
      overlayCleanup.hideVideoPlayPauseOverlays(adapterTools);
    }
  }

  function playerMethodsFor(playerApi, action, video) {
    return typeof playerApi.methodsFor === 'function' ? playerApi.methodsFor(action, video) : [];
  }

  function hasPlayerMethod(playerApi, action, video) {
    return typeof playerApi.hasMethod === 'function' && playerApi.hasMethod(action, video);
  }

  function callPlayerMethod(playerApi, methodNames, args) {
    return typeof playerApi.call === 'function' ? playerApi.call(methodNames, args) : null;
  }

  function handledValue(playerApi, callResult, fallbackValue) {
    return typeof playerApi.handledValue === 'function'
      ? playerApi.handledValue(callResult, fallbackValue)
      : null;
  }

  function currentVideoTime(playerApi, video) {
    return typeof playerApi.currentVideoTime === 'function'
      ? playerApi.currentVideoTime(video)
      : Number(video && video.currentTime || 0);
  }
})();
