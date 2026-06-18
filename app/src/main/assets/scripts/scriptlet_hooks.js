/*
 * Shared scriptlet hooks for popup and request blocking.
 */
(function () {
  const hooks = window.VideoBrowserScriptletHooks || {};
  window.VideoBrowserScriptletHooks = hooks;

  hooks.defaultBlockedKeywords = hooks.defaultBlockedKeywords || [
    'doubleclick',
    'googleads',
    'googlesyndication',
    '/pagead/',
    '/adservice/',
    '/adserver/',
    '/advert/',
    '/ads/',
    'vast',
    'vmap',
    'preroll',
    'midroll'
  ];

  hooks.configKeywordList = hooks.configKeywordList || function (config, fieldName) {
    const values = config && config[fieldName];
    if (!Array.isArray(values)) return [];
    return values.map(function (keyword) {
      return String(keyword || '').trim().toLowerCase();
    }).filter(function (keyword) {
      return keyword.length >= 3;
    });
  };

  hooks.shouldBlockUrlAgainstKeywords = hooks.shouldBlockUrlAgainstKeywords || function (value, keywords) {
    const url = String(value || '').toLowerCase();
    return (keywords || []).some(function (keyword) {
      return url.indexOf(keyword) !== -1;
    });
  };

  hooks.shouldBlockUrl = hooks.shouldBlockUrl || function (value, config) {
    return hooks.shouldBlockUrlAgainstKeywords(
      value,
      hooks.defaultBlockedKeywords.concat(hooks.configKeywordList(config, 'blockedUrlKeywords'))
    );
  };

  hooks.windowOpenBlockedKeywords = hooks.windowOpenBlockedKeywords || function (config) {
    return hooks.configKeywordList(config, 'scriptletWindowOpenBlockedKeywords');
  };

  hooks.fetchBlockedKeywords = hooks.fetchBlockedKeywords || function (config) {
    return hooks.configKeywordList(config, 'scriptletFetchBlockedKeywords');
  };

  hooks.install = hooks.install || function (state, callbacks) {
    if (!state || state.hooked) return;
    state.hooked = true;

    const originalOpen = window.open;
    window.open = function (url) {
      const config = state.config || {};
      if (
        (config.cleanupEnabled && hooks.shouldBlockUrl(url, config)) ||
        hooks.shouldBlockUrlAgainstKeywords(url, hooks.windowOpenBlockedKeywords(config))
      ) {
        return null;
      }
      return originalOpen.apply(this, arguments);
    };

    const originalFetch = window.fetch;
    if (typeof originalFetch === 'function') {
      window.fetch = function () {
        if (arguments.length > 0) {
          const config = state.config || {};
          const input = arguments[0];
          const url = typeof input === 'string' ? input : input && input.url;
          if (
            (config.cleanupEnabled && hooks.shouldBlockUrl(url, config)) ||
            hooks.shouldBlockUrlAgainstKeywords(url, hooks.fetchBlockedKeywords(config))
          ) {
            return Promise.reject(new Error('Blocked by VideoBrowser'));
          }
        }
        return originalFetch.apply(this, arguments);
      };
    }

    if (callbacks && typeof callbacks.installFullscreenEventHooks === 'function') {
      callbacks.installFullscreenEventHooks();
    }
  };
})();
