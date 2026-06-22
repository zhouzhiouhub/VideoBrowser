/*
 * Shared broker for site-specific video capabilities.
 */
(function () {
  const broker = window.VideoBrowserSiteVideoCapabilityBroker || {};
  window.VideoBrowserSiteVideoCapabilityBroker = broker;

  broker.forVideo = broker.forVideo || function (video, action) {
    const adapters = window.VideoBrowserSiteAdapters || {};
    return Object.keys(adapters).map(function (key) {
      const adapter = adapters[key];
      return adapter && adapter.videoCapabilities;
    }).filter(function (capabilities) {
      if (!capabilities || typeof capabilities !== 'object') return false;
      if (action && typeof capabilities[action] !== 'function') return false;
      if (typeof capabilities.supports === 'function') {
        try {
          if (!capabilities.supports(video)) return false;
        } catch (_) {
          return false;
        }
      }
      if (action && typeof capabilities.canUse === 'function') {
        try {
          if (!capabilities.canUse(action, video)) return false;
        } catch (_) {
          return false;
        }
      }
      return true;
    });
  };

  broker.has = broker.has || function (video, action) {
    return broker.forVideo(video, action).length > 0;
  };

  broker.hasFromOptions = broker.hasFromOptions || function (options, video, action) {
    const config = options || {};
    return typeof config.hasSiteVideoCapability === 'function'
      ? config.hasSiteVideoCapability(video, action)
      : false;
  };

  broker.invoke = broker.invoke || function (video, action, args) {
    const capabilitiesList = broker.forVideo(video, action);
    for (let index = 0; index < capabilitiesList.length; index += 1) {
      const capabilities = capabilitiesList[index];
      const method = capabilities && capabilities[action];
      if (typeof method !== 'function') continue;
      try {
        const result = method.apply(capabilities, [video].concat(args || []));
        if (result !== null && typeof result !== 'undefined') {
          return { handled: true, value: result };
        }
      } catch (_) {}
    }
    return broker.unhandled();
  };

  broker.invokeFromOptions = broker.invokeFromOptions || function (options, video, action, args) {
    const config = options || {};
    if (typeof config.invokeSiteVideoCapability === 'function') {
      return config.invokeSiteVideoCapability(video, action, args);
    }
    return broker.unhandled();
  };

  broker.unhandled = broker.unhandled || function () {
    return { handled: false, value: undefined };
  };
})();
