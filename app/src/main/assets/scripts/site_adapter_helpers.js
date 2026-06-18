/*
 * Shared helpers for site adapter scripts.
 *
 * Site adapters keep site-specific selectors and player API wiring in their own
 * files. This module owns the repeated DOM, logging, and native-control helpers.
 */
(function () {
  var tools = window.VideoBrowserSiteAdapterTools || {};
  window.VideoBrowserSiteAdapterTools = tools;

  tools.query = function (selector) {
    try {
      return document.querySelectorAll(selector);
    } catch (_) {
      return [];
    }
  };

  tools.textOf = function (element) {
    return String(
      element && (
        element.innerText ||
        element.textContent ||
        element.getAttribute('aria-label') ||
        element.getAttribute('title')
      ) ||
      ''
    );
  };

  tools.hideElement = function (element, reason, defaultReason, protectedIds) {
    if (!element || element === document.body || element === document.documentElement) return;
    var ids = protectedIds || [];
    if (ids.indexOf(String(element.id || '').toLowerCase()) !== -1) return;
    element.setAttribute('data-videobrowser-site-dismissed', reason || defaultReason || 'site-adapter');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
  };

  tools.hideSelectors = function (selectors, reason, defaultReason, protectedIds) {
    selectors.forEach(function (selector) {
      tools.query(selector).forEach(function (element) {
        tools.hideElement(element, reason, defaultReason, protectedIds);
      });
    });
  };

  tools.clickTextButtons = function (selector, pattern) {
    tools.query(selector).forEach(function (element) {
      if (pattern.test(tools.textOf(element)) && typeof element.click === 'function') {
        element.click();
      }
    });
  };

  tools.clickSelectors = function (selectors) {
    selectors.forEach(function (selector) {
      tools.query(selector).forEach(function (element) {
        if (typeof element.click === 'function') {
          element.click();
        }
      });
    });
  };

  tools.logVideoDiagnostic = function (adapterId, event, details) {
    var bridge = window.VideoBrowserNative;
    var message = 'event=' + event + ' adapter=' + adapterId + ' host=' + location.hostname + ' ' + (details || '');
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

  tools.videoSource = function (video) {
    return String(video && (video.currentSrc || video.src || video.getAttribute('src')) || '').slice(0, 180);
  };

  tools.removeNativeVideoControls = function (video, adapterId) {
    if (!video) return;
    var hadNativeControls = Boolean(video.controls || video.hasAttribute('controls'));
    try { video.controls = false; } catch (_) {}
    try { video.removeAttribute('controls'); } catch (_) {}
    if (hadNativeControls) {
      tools.logVideoDiagnostic(adapterId, 'remove-native-controls', 'src=' + tools.videoSource(video));
    }
  };

  tools.adapterState = function (stateKey) {
    var state = window[stateKey] || {
      intervalId: null,
      config: {}
    };
    window[stateKey] = state;
    return state;
  };

  tools.registerBasicAdapter = function (options) {
    var adapters = window.VideoBrowserSiteAdapters || {};
    window.VideoBrowserSiteAdapters = adapters;

    var adapterId = options.adapterId;
    var state = tools.adapterState(options.stateKey);
    var clickSelector = options.clickSelector || 'button,a,[role="button"],.close,.skip';
    var intervalMs = options.intervalMs || 1800;

    function hideSelectors(selectors) {
      tools.hideSelectors(selectors || [], adapterId + '-ad', adapterId, options.protectedIds);
    }

    function clickTextButtons(pattern) {
      tools.clickTextButtons(clickSelector, pattern);
    }

    function removeNativeVideoControls(video) {
      tools.removeNativeVideoControls(video, adapterId);
    }

    function run(config) {
      if (!document.documentElement) return;
      if (config && config.cleanupEnabled) {
        hideSelectors(options.cleanupSelectors);
        if (options.cleanupButtonPattern) {
          clickTextButtons(options.cleanupButtonPattern);
        }
      }
      if (config && config.videoEnabled) {
        tools.query('video').forEach(removeNativeVideoControls);
        if (options.videoButtonPattern) {
          clickTextButtons(options.videoButtonPattern);
        }
        if (options.videoClickSelectors) {
          tools.clickSelectors(options.videoClickSelectors);
        }
        if (typeof options.afterVideo === 'function') {
          options.afterVideo(config);
        }
      }
      if (typeof options.afterRun === 'function') {
        options.afterRun(config);
      }
    }

    function startWorker() {
      if (state.intervalId) return;
      state.intervalId = window.setInterval(function () {
        run(state.config || {});
      }, intervalMs);
    }

    adapters[adapterId] = adapters[adapterId] || {};
    adapters[adapterId].videoCapabilities = {
      supports: function (video) {
        return Boolean(video && video.isConnected);
      },
      canUse: function (action) {
        return action === 'enableControls';
      },
      enableControls: function (video) {
        removeNativeVideoControls(video);
        return Boolean(video);
      }
    };
    adapters[adapterId].apply = function (config) {
      this.lastConfig = config || {};
      state.config = this.lastConfig;
      run(state.config);
      startWorker();
    };

    return adapters[adapterId];
  };
})();
