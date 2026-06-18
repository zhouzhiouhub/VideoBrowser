/*
 * Shared accessors for the Android JavaScript bridge and diagnostic logging.
 */
(function () {
  const bridgeTools = window.VideoBrowserNativeBridge || {};
  window.VideoBrowserNativeBridge = bridgeTools;

  bridgeTools.safeLogValue = bridgeTools.safeLogValue || function (value) {
    return String(value === null || typeof value === 'undefined' ? '' : value)
      .replace(/\s+/g, ' ')
      .replace(/[|]/g, '/')
      .slice(0, 180);
  };

  bridgeTools.logVideoMessage = bridgeTools.logVideoMessage || function (message) {
    const bridge = window.VideoBrowserNative;
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
})();
