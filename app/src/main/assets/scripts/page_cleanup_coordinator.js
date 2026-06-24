/*
 * Shared page cleanup orchestration for common page work.
 */
(function () {
  const coordinator = window.VideoBrowserPageCleanupCoordinator || {};
  const genericCleanupSelectors = window.VideoBrowserGenericCleanupSelectors || {};
  const generatedAdCleanup = window.VideoBrowserGeneratedAdCleanup || {};
  const genericAdOverlayCleanup = window.VideoBrowserGenericAdOverlayCleanup || {};
  const configuredCleanup = window.VideoBrowserConfiguredCleanup || {};
  const topPageCleanup = window.VideoBrowserTopPageCleanup || {};
  const searchResultCleanup = window.VideoBrowserSearchResultCleanup || {};
  const embeddedSearchShellCleanup = window.VideoBrowserEmbeddedSearchShellCleanup || {};
  const pageLifecycleTools = window.VideoBrowserPageLifecycleTools;
  window.VideoBrowserPageCleanupCoordinator = coordinator;

  coordinator.run = coordinator.run || function (state, options) {
    if (!state || !state.config || !state.config.cleanupEnabled || !document.documentElement) {
      return false;
    }
    const config = options || {};
    if (coordinator.shouldSkipGenericCleanup(config)) {
      configuredCleanup.injectStyle(state, {
        includeGenericSelectors: false,
        includeRuleSelectors: true
      });
      coordinator.removeSearchResultAds(config);
      configuredCleanup.removeDomElements(state);
      if (!isBilibiliHost(config)) coordinator.removeGenericAdOverlays(state);
      return true;
    }

    pageLifecycleTools.runWithOptionalMutationSuppression(config, function () {
      configuredCleanup.injectStyle(state, {
        includeGenericSelectors: true,
        includeRuleSelectors: true
      });
      genericCleanupSelectors.hideDefaultElements();
      configuredCleanup.removeDomElements(state);
      coordinator.removeGenericAdOverlays(state);
      topPageCleanup.removeAccountBars();
      topPageCleanup.removeNoiseBlocks();
      coordinator.removeSearchResultAds(config);
    });
    return true;
  };

  coordinator.runGenerated = coordinator.runGenerated || function (state, options) {
    const config = options || {};
    if (isBilibiliHost(config)) return false;
    if (generatedAdCleanup && typeof generatedAdCleanup.run === 'function') {
      generatedAdCleanup.run(state, {
        now: Number(config.now || Date.now()),
        force: false
      });
      return true;
    }
    return false;
  };

  coordinator.applyDisabledState = coordinator.applyDisabledState || function (state) {
    if (configuredCleanup.hasUserCssSelectors(state)) {
      configuredCleanup.injectStyle(state, {
        includeGenericSelectors: false,
        includeRuleSelectors: false
      });
    } else {
      configuredCleanup.removeStyle();
    }
  };

  coordinator.shouldSkipGenericCleanup = coordinator.shouldSkipGenericCleanup || function (options) {
    return isBilibiliHost(options) || searchResultCleanup.isResultPage();
  };

  coordinator.removeSearchResultAds = coordinator.removeSearchResultAds || function (options) {
    const config = options || {};
    searchResultCleanup.removeAds({
      runWithMutationSuppressed: config.runWithMutationSuppressed
    });
  };

  coordinator.applyEmbeddedSearchShell = coordinator.applyEmbeddedSearchShell || function (state, options) {
    const config = options || {};
    embeddedSearchShellCleanup.apply({
      state: state,
      runWithMutationSuppressed: config.runWithMutationSuppressed
    });
  };

  coordinator.removeGenericAdOverlays = coordinator.removeGenericAdOverlays || function (state) {
    genericAdOverlayCleanup.run(state);
  };

  function isBilibiliHost(options) {
    return Boolean(
      options &&
      typeof options.isBilibiliHost === 'function' &&
      options.isBilibiliHost()
    );
  }
})();
