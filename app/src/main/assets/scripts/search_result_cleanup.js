/*
 * Search result page cleanup strategy shared by the common enhancer.
 */
(function () {
  const cleanup = window.VideoBrowserSearchResultCleanup || {};
  const geometry = window.VideoBrowserGeometry || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  window.VideoBrowserSearchResultCleanup = cleanup;

  cleanup.isResultPage = cleanup.isResultPage || function () {
    const host = String(location.hostname || '').toLowerCase();
    const path = String(location.pathname || '/').replace(/\/+$/, '') || '/';
    const query = location.search || '';

    if (/^(m|www)\.baidu\.com$/i.test(host)) {
      return (pathMatchesSearchPath(path, '/s') && /[?&](word|wd)=/i.test(query)) ||
        (pathMatchesSearchPath(path, '/baidu') && /[?&](word|wd)=/i.test(query));
    }
    if (/^(m\.)?sogou\.com$/i.test(host) || host === 'www.sogou.com') {
      return (pathMatchesSearchPath(path, '/web') || pathMatchesSearchPath(path, '/s')) &&
        /[?&](query|keyword)=/i.test(query);
    }
    if (/^(m\.)?so\.com$/i.test(host) || host === 'www.so.com') {
      return pathMatchesSearchPath(path, '/s') && /[?&]q=/i.test(query);
    }
    if (host === 'quark.sm.cn' || host === 'so.m.sm.cn') {
      return pathMatchesSearchPath(path, '/s') && /[?&]q=/i.test(query);
    }
    return false;
  };

  function pathMatchesSearchPath(path, searchPath) {
    return path === searchPath || (searchPath !== '/' && path.endsWith(searchPath));
  }

  cleanup.removeAds = cleanup.removeAds || function (options) {
    if (!cleanup.isResultPage()) return false;

    const config = options || {};
    const work = function () {
      hideKnownSearchAdContainers();
      findSearchAdDisclosureMarkers().forEach(function (marker) {
        const root = findSearchResultRoot(marker);
        if (root) {
          domActions.hideElement(root, {
            reason: 'search-result-ad',
            protectAppContainers: true
          });
        }
      });
    };
    if (typeof config.runWithMutationSuppressed === 'function') {
      config.runWithMutationSuppressed(work);
    } else {
      work();
    }
    return true;
  };

  function hideKnownSearchAdContainers() {
    [
      '[tpl^="ad"]',
      '[tpl*="-ad"]',
      '[tpl*="_ad"]',
      '[tpl*="adv"]',
      '[data-tuiguang]',
      '[data-log*="-ad"]',
      '[data-log*="_ad"]',
      '[class*="ad-result"]',
      '[class*="ec-ad"]',
      '[class*="ec_ad"]',
      '[class*="ec-tuiguang"]',
      '[class*="ec_tuiguang"]',
      '[class*="wise-ad"]',
      '[class*="wise_ad"]'
    ].forEach(function (selector) {
      domTools.queryAll(selector).forEach(function (element) {
        const root = findSearchResultRoot(element) || element;
        domActions.hideElement(root, {
          reason: 'search-result-ad-container',
          protectAppContainers: true
        });
      });
    });
  }

  function findSearchAdDisclosureMarkers() {
    const markers = [];
    domTools.queryAll('span,i,em,b,a,button,[role="button"],[aria-label],[title],[class*="ad"],[class*="adv"]')
      .forEach(function (element) {
        const text = selectorTools.normalizeText(
          element.innerText ||
          element.textContent ||
          element.getAttribute('aria-label') ||
          element.getAttribute('title')
        );
        const descriptor = domTools.elementDescriptor(element);
        if (isSearchAdDisclosure(text, descriptor)) markers.push(element);
      });
    return markers;
  }

  function isSearchAdDisclosure(text, descriptor) {
    const compactText = selectorTools.compactText(text);
    const compactDescriptor = selectorTools.compactText(descriptor);
    if (/^(广告|廣告|推广|推廣|商业推广|商業推廣|赞助|贊助|sponsored|ad)$/i.test(compactText)) {
      return true;
    }
    if (/^(广告|廣告|推广|推廣|赞助|贊助)[:：]?$/.test(compactText)) {
      return true;
    }
    if (compactText.length <= 10 && /广告|廣告|推广|推廣|赞助|贊助|sponsored/i.test(compactText)) {
      return true;
    }
    return /(^|[-_\s])(ad|ads|adv|sponsored|tuiguang|promotion)([-_\s]|$)/i.test(compactDescriptor) &&
      compactText.length <= 24;
  }

  function findSearchResultRoot(marker) {
    if (!marker || !document.body) return null;

    let current = marker;
    let candidate = null;
    for (let depth = 0; current && depth < 9; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (isSearchControlContainer(current)) break;

      const rect = geometry.safeRect(current);
      if (!rect) continue;
      if (rect.width < window.innerWidth * 0.45) continue;
      if (rect.height < 36) continue;
      if (rect.height > Math.min(window.innerHeight * 0.72, 520)) continue;

      const text = selectorTools.normalizeText(current.innerText || current.textContent);
      if (text.length < 2 || text.length > 1200) continue;
      const hasContent = domTools.queryAllWithin(current, 'a,img,h1,h2,h3,[role="heading"]').length > 0 ||
        text.length >= 8;
      if (hasContent) candidate = current;
    }

    return candidate;
  }

  function isSearchControlContainer(element) {
    if (!element || typeof element.querySelector !== 'function') return false;
    if (element.querySelector('input,textarea,select,form')) return true;

    const rect = geometry.safeRect(element);
    const text = selectorTools.normalizeText(element.innerText || element.textContent);
    const descriptor = domTools.elementDescriptor(element);
    const topChromeLike = rect && rect.top < 120 &&
      /综合|资讯|视频|图片|知道|文库|贴吧|地图|更多|搜索|百度一下|网页|问答/.test(text);
    return topChromeLike || /searchbox|search-box|searchbar|search-bar|tab|tabs|navbar|nav-bar/i.test(descriptor);
  }
})();
