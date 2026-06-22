/*
 * Shared generic ad overlay cleanup strategy.
 */
(function () {
  const cleanup = window.VideoBrowserGenericAdOverlayCleanup || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  const generatedAdCleanup = window.VideoBrowserGeneratedAdCleanup || {};
  const overlaySignals = window.VideoBrowserGenericAdOverlaySignals || {};
  const overlayDetector = window.VideoBrowserGenericAdOverlayDetector || {};
  window.VideoBrowserGenericAdOverlayCleanup = cleanup;

  cleanup.run = cleanup.run || function (state, options) {
    if (!document.body) return false;

    const roots = [];
    const candidates = typeof overlayDetector.collectCandidates === 'function'
      ? overlayDetector.collectCandidates()
      : [];
    candidates.forEach(function (candidate) {
      const root = typeof overlayDetector.findRoot === 'function'
        ? overlayDetector.findRoot(candidate)
        : null;
      if (root && roots.indexOf(root) === -1) roots.push(root);
    });

    roots.forEach(function (root) {
      hideElement(root, 'generic-ad-overlay');
      hideGenericOverlayBackdrops(root);
      overlaySignals.clearScrollLocks();
    });

    if (generatedAdCleanup && typeof generatedAdCleanup.run === 'function') {
      const config = options || {};
      generatedAdCleanup.run(state, {
        now: Number(config.now || Date.now()),
        force: true
      });
    }
    return true;
  };

  function hideGenericOverlayBackdrops(root) {
    if (!root || !document.body) return;
    queryAll('body *').forEach(function (element) {
      if (!element || element === root || element.contains(root) || root.contains(element)) return;
      if (isProtectedAppContainer(element)) return;

      const rect = element.getBoundingClientRect();
      if (!rect.width || !rect.height) return;

      const style = getComputedStyle(element);
      if (!/fixed|absolute|sticky/i.test(style.position)) return;
      const descriptor = elementDescriptor(element);
      const fullScreenLike = rect.width >= window.innerWidth * 0.86 &&
        rect.height >= window.innerHeight * 0.48 &&
        rect.left <= window.innerWidth * 0.12 &&
        rect.top <= window.innerHeight * 0.22;
      const overlayNameLike = /mask|overlay|modal|popup|dialog|shade|shadow|backdrop|layer/i.test(descriptor);
      const text = normalizeText(element.textContent);
      if (fullScreenLike && overlayNameLike && text.length <= 80) {
        hideElement(element, 'generic-ad-backdrop');
      }
    });
  }

  function normalizeText(value) {
    return selectorTools.normalizeText(value);
  }

  function elementDescriptor(element) {
    return domTools.elementDescriptor(element);
  }

  function hideElement(element, reason) {
    domActions.hideElement(element, {
      reason: reason || 'generic-ad-overlay',
      protectAppContainers: true
    });
  }

  function isProtectedAppContainer(element) {
    return domActions.isProtectedAppContainer(element);
  }

  function queryAll(selector) {
    return domTools.queryAll(selector);
  }
})();
