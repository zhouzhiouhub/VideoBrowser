/*
 * Embedded search shell cleanup for built-in search result pages.
 */
(function () {
  const cleanup = window.VideoBrowserEmbeddedSearchShellCleanup || {};
  const geometry = window.VideoBrowserGeometry || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  window.VideoBrowserEmbeddedSearchShellCleanup = cleanup;

  cleanup.apply = cleanup.apply || function (options) {
    const config = options || {};
    const state = config.state || {};
    if (!state.config || !state.config.builtInSearchResultPage) return false;

    const work = function () {
      hideEmbeddedSearchChrome();
    };
    if (typeof config.runWithMutationSuppressed === 'function') {
      config.runWithMutationSuppressed(work);
    } else {
      work();
    }
    return true;
  };

  function hideEmbeddedSearchChrome() {
    const roots = [];
    collectEmbeddedSearchChromeCandidates().forEach(function (candidate) {
      const root = findEmbeddedSearchChromeRoot(candidate);
      if (root && roots.indexOf(root) === -1) roots.push(root);
    });
    roots.forEach(function (root) {
      domActions.hideElement(root, {
        reason: 'embedded-search-shell',
        protectAppContainers: true
      });
    });
  }

  function collectEmbeddedSearchChromeCandidates() {
    const selectors = [
      'form',
      '[role="search"]',
      'input',
      'textarea',
      '[id*="search"]',
      '[class*="search"]',
      '[id*="logo"]',
      '[class*="logo"]',
      '[id*="brand"]',
      '[class*="brand"]',
      '[id*="login"]',
      '[class*="login"]',
      '[id*="account"]',
      '[class*="account"]',
      '[id*="profile"]',
      '[class*="profile"]',
      '[aria-label*="登录"]',
      '[title*="登录"]',
      'a[href*="login"]',
      'a[href*="passport"]'
    ];
    const candidates = [];
    selectors.forEach(function (selector) {
      domTools.queryAll(selector).forEach(function (element) {
        if (
          candidates.indexOf(element) === -1 &&
          isEmbeddedSearchChromeCandidate(element)
        ) {
          candidates.push(element);
        }
      });
    });
    return candidates;
  }

  function isEmbeddedSearchChromeCandidate(element) {
    if (!isInEmbeddedSearchChromeZone(element)) return false;
    if (isLikelyResultContent(element)) return false;

    const descriptor = domTools.elementDescriptor(element);
    const text = selectorTools.normalizeText(element.innerText || element.textContent);
    const hasSearchControl = matchesSafely(element, 'form,[role="search"],input,textarea') ||
      domTools.queryAllWithin(element, 'input,textarea,[role="search"]').length > 0;
    const hasAccountSignal = /login|signin|sign-in|account|profile|passport|user/i.test(descriptor) ||
      /登录|登入|账号|账户|个人|头像/.test(text);
    const hasBrandSignal = /logo|brand/i.test(descriptor);

    return hasSearchControl || hasAccountSignal || hasBrandSignal;
  }

  function findEmbeddedSearchChromeRoot(element) {
    if (!element || !document.body) return null;

    let current = element;
    let candidate = element;
    for (let depth = 0; current && depth < 6; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      const rect = geometry.safeRect(current);
      if (!rect || !isRectInTopChromeZone(rect)) break;
      if (isLikelyResultContent(current)) break;

      const parent = current.parentElement;
      const parentRect = geometry.safeRect(parent);
      candidate = current;
      if (
        !parent ||
        parent === document.body ||
        parent === document.documentElement ||
        !parentRect ||
        !isRectInTopChromeZone(parentRect) ||
        parentRect.height > Math.max(220, window.innerHeight * 0.32) ||
        isLikelyResultContent(parent)
      ) {
        break;
      }
    }
    return candidate;
  }

  function isInEmbeddedSearchChromeZone(element) {
    const rect = geometry.safeRect(element);
    return isRectInTopChromeZone(rect);
  }

  function isRectInTopChromeZone(rect) {
    return Boolean(
      rect &&
      rect.bottom > 0 &&
      rect.top < Math.min(260, window.innerHeight * 0.38) &&
      rect.width >= Math.min(24, window.innerWidth * 0.08) &&
      rect.height >= 10 &&
      rect.height <= Math.max(220, window.innerHeight * 0.32)
    );
  }

  function isLikelyResultContent(element) {
    if (!element || typeof element.querySelector !== 'function') return false;
    const rect = geometry.safeRect(element);
    const text = selectorTools.normalizeText(element.innerText || element.textContent);
    const descriptor = domTools.elementDescriptor(element);
    const hasDenseResultContent =
      domTools.queryAllWithin(element, 'article,h1,h2,h3,[role="heading"],img,video').length >= 2 &&
      text.length > 80;
    const hasSearchResultDescriptor =
      /result|content|container|article|list|feed|card/i.test(descriptor) &&
      text.length > 80;
    return Boolean(
      rect &&
      rect.height > 80 &&
      (hasDenseResultContent || hasSearchResultDescriptor)
    );
  }

  function matchesSafely(element, selector) {
    if (!element || typeof element.matches !== 'function') return false;
    try {
      return element.matches(selector);
    } catch (_) {
      return false;
    }
  }
})();
