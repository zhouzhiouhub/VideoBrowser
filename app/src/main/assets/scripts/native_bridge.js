/*
 * Shared accessors for the Android JavaScript bridge and diagnostic logging.
 */
(function () {
  const bridgeTools = window.VideoBrowserNativeBridge || {};
  window.VideoBrowserNativeBridge = bridgeTools;

  bridgeTools.nativeBridge = bridgeTools.nativeBridge || function () {
    return window.VideoBrowserNative;
  };

  bridgeTools.callNative = bridgeTools.callNative || function (methodName, args) {
    const bridge = bridgeTools.nativeBridge();
    if (bridge && typeof bridge[methodName] === 'function') {
      try {
        bridge[methodName].apply(bridge, args || []);
        return true;
      } catch (_) {}
    }
    return false;
  };

  bridgeTools.safeLogValue = bridgeTools.safeLogValue || function (value) {
    return String(value === null || typeof value === 'undefined' ? '' : value)
      .replace(/\s+/g, ' ')
      .replace(/[|]/g, '/')
      .slice(0, 180);
  };

  bridgeTools.logVideoMessage = bridgeTools.logVideoMessage || function (message) {
    const bridge = bridgeTools.nativeBridge();
    if (bridge && typeof bridge.logVideoEvent === 'function') {
      try {
        bridge.logVideoEvent(message);
        return;
      } catch (_) {}
    }
    try {
      if (window.console && typeof window.console.log === 'function') {
        window.console.log('[VideoBrowserVideo] ' + message);
      }
    } catch (_) {}
  };

  bridgeTools.logVideoDiagnostic = bridgeTools.logVideoDiagnostic || function (event, details, options) {
    const config = options || {};
    const values = details && typeof details === 'object' ? details : {};
    const parts = [
      'event=' + bridgeTools.safeLogValue(event)
    ];
    if (config.adapterId) {
      parts.push('adapter=' + bridgeTools.safeLogValue(config.adapterId));
    }
    parts.push('host=' + bridgeTools.safeLogValue(location.hostname || ''));
    if (config.includePath) {
      parts.push('path=' + bridgeTools.safeLogValue(location.pathname || '/'));
    }
    if (typeof details === 'string') {
      if (details) parts.push(details);
    } else {
      Object.keys(values).forEach(function (key) {
        parts.push(bridgeTools.safeLogValue(key) + '=' + bridgeTools.safeLogValue(values[key]));
      });
    }
    bridgeTools.logVideoMessage(parts.join(' '));
  };

  bridgeTools.videoSource = bridgeTools.videoSource || function (video) {
    return String(video && (video.currentSrc || video.src || video.getAttribute('src')) || '').slice(0, 180);
  };

  bridgeTools.updatePlaybackTimeline = bridgeTools.updatePlaybackTimeline || function (positionMs, durationMs) {
    return bridgeTools.callNative('updatePlaybackTimeline', [positionMs, durationMs]);
  };

  bridgeTools.enterFullscreen = bridgeTools.enterFullscreen || function () {
    return bridgeTools.callNative('enterFullscreen');
  };

  bridgeTools.exitFullscreen = bridgeTools.exitFullscreen || function () {
    return bridgeTools.callNative('exitFullscreen');
  };

  bridgeTools.requestElementBlock = bridgeTools.requestElementBlock || function (selector, description) {
    return bridgeTools.callNative('requestElementBlock', [selector, description]);
  };
})();
