/*
 * Bilibili-specific player API helpers.
 */
(function () {
  var tools = window.VideoBrowserBilibiliPlayerApi || {};
  window.VideoBrowserBilibiliPlayerApi = tools;

  tools.find = tools.find || function () {
    var candidates = [
      window.player,
      window.bilibiliPlayer,
      window.__bilibiliPlayer,
      window.__PLAYER__,
      window.$player
    ];

    for (var index = 0; index < candidates.length; index += 1) {
      var candidate = candidates[index];
      if (candidate && typeof candidate === 'object') return candidate;
    }
    return null;
  };

  tools.methodsFor = tools.methodsFor || function (action, video) {
    if (action === 'togglePlayPause') {
      return video && (video.paused || video.ended)
        ? ['play']
        : ['pause'];
    }
    if (action === 'seekBy' || action === 'seekTo') {
      return ['seek', 'seekTo', 'setCurrentTime'];
    }
    if (action === 'setPlaybackSpeed') {
      return ['setPlaybackRate', 'setPlaybackSpeed'];
    }
    return [];
  };

  tools.hasMethod = tools.hasMethod || function (action, video) {
    var api = tools.find();
    if (!api) return false;
    return tools.methodsFor(action, video).some(function (methodName) {
      return typeof api[methodName] === 'function';
    });
  };

  tools.call = tools.call || function (methodNames, args) {
    var api = tools.find();
    if (!api) return null;

    for (var index = 0; index < methodNames.length; index += 1) {
      var methodName = methodNames[index];
      var method = api[methodName];
      if (typeof method !== 'function') continue;
      try {
        return {
          handled: true,
          value: method.apply(api, args || [])
        };
      } catch (_) {}
    }
    return null;
  };

  tools.read = tools.read || function (methodNames) {
    var api = tools.find();
    if (!api) return null;

    for (var index = 0; index < methodNames.length; index += 1) {
      var methodName = methodNames[index];
      var value = api[methodName];
      if (typeof value === 'undefined' || value === null) continue;
      try {
        var result = typeof value === 'function' ? value.call(api) : value;
        if (typeof result !== 'undefined' && result !== null) return result;
      } catch (_) {}
    }
    return null;
  };

  tools.handledValue = tools.handledValue || function (callResult, fallbackValue) {
    if (!callResult || !callResult.handled) return null;
    return typeof callResult.value === 'undefined' ? fallbackValue : callResult.value;
  };

  tools.currentVideoTime = tools.currentVideoTime || function (video) {
    var api = tools.find();
    if (api) {
      var getterNames = ['getCurrentTime', 'currentTime'];
      for (var index = 0; index < getterNames.length; index += 1) {
        var getter = api[getterNames[index]];
        try {
          var value = typeof getter === 'function' ? getter.call(api) : getter;
          var numericValue = Number(value);
          if (Number.isFinite(numericValue) && numericValue >= 0) return numericValue;
        } catch (_) {}
      }
    }
    return Number(video && video.currentTime || 0);
  };
})();
