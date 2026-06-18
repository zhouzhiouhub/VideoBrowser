/*
 * Shared cleanup for top account bars and search-provider top noise blocks.
 */
(function () {
  const cleanup = window.VideoBrowserTopPageCleanup || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  window.VideoBrowserTopPageCleanup = cleanup;

  cleanup.removeAccountBars = cleanup.removeAccountBars || function () {
    if (!/(\.|^)baidu\.com$/i.test(location.hostname)) return false;
    const candidates = [];
    function addCandidate(element) {
      if (element && candidates.indexOf(element) === -1) candidates.push(element);
    }
    queryAll(
      'header,[role="banner"],[id*="top"],[class*="top"],[id*="head"],[class*="head"],body>div,body>div>div'
    ).forEach(addCandidate);

    candidates.forEach(function (element) {
      if (!element || element.querySelector('input,textarea,form')) return;
      const rect = element.getBoundingClientRect();
      if (rect.top < 0 || rect.top > 220 || rect.height <= 0 || rect.height > 72) return;

      const text = String(element.innerText || element.textContent || '');
      const html = String(element.innerHTML || '');
      const accountLike = /登录|账号|账户|我的|用户|passport|login|signin|user|profile/i.test(text + html);
      const iconBarLike = element.querySelectorAll('a,button,[role="button"],svg,i').length >= 1 &&
        /menu|grid|app|user|profile|account|more|更多|应用/i.test(html);
      if (rect.width < Math.min(window.innerWidth * 0.45, 180) && !accountLike && !iconBarLike) return;
      if (accountLike || iconBarLike) hideElement(element, 'top-account-bar');
    });
    return true;
  };

  cleanup.removeNoiseBlocks = cleanup.removeNoiseBlocks || function () {
    if (!cleanup.isSearchProviderHomePage()) return false;

    queryAll(
      'body>div,body>section,header,[role="banner"],[id*="top"],[class*="top"],[id*="banner"],[class*="banner"]'
    ).forEach(function (element) {
      if (!element || element.querySelector('input,textarea,select,form,video,canvas')) return;

      const rect = element.getBoundingClientRect();
      if (rect.top < 0 || rect.top > 180 || rect.height < 32 || rect.height > 150) return;
      if (rect.width < window.innerWidth * 0.58) return;

      const text = String(element.innerText || element.textContent || '').replace(/\s+/g, '');
      const html = String(element.innerHTML || '');
      const descriptor = String(element.id || '') + ' ' + String(element.className || '') + ' ' + html;
      const brandLogoLike = /logo|search-logo|bdlogo|sogoulogo/i.test(descriptor) ||
        /百度|搜狗搜索|搜狗|360搜索|必应|Bing/i.test(text);
      const adLike = /广告|推广|赞助|商业合作|无图|太平洋|下载APP|打开APP|app/i.test(text + descriptor) ||
        /ad|ads|advert|banner|promo|promotion|sponsor|slot|download|openapp/i.test(descriptor);
      const sparseTopSlot = /(\.|^)sogou\.com$/i.test(location.hostname) &&
        rect.top < 120 &&
        rect.height >= 48 &&
        rect.width > window.innerWidth * 0.82 &&
        text.length <= 18 &&
        element.querySelectorAll('a,button,img,svg').length <= 2;

      if (!brandLogoLike && (adLike || sparseTopSlot)) hideElement(element, 'top-noise-block');
    });
    return true;
  };

  cleanup.isSearchProviderHomePage = cleanup.isSearchProviderHomePage || function () {
    const host = String(location.hostname || '').toLowerCase();
    const path = String(location.pathname || '/').replace(/\/+$/, '') || '/';
    return path === '/' && /^(m\.baidu\.com|m\.sogou\.com|m\.so\.com|quark\.sm\.cn|so\.m\.sm\.cn|www\.bing\.com)$/i.test(host);
  };

  function hideElement(element, reason) {
    domActions.hideElement(element, {
      reason: reason || 'top-page-cleanup',
      protectAppContainers: true
    });
  }

  function queryAll(selector) {
    return domTools.queryAll(selector);
  }
})();
