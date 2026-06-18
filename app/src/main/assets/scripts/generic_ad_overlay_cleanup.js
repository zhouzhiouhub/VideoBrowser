/*
 * Shared generic ad overlay cleanup strategy.
 */
(function () {
  const cleanup = window.VideoBrowserGenericAdOverlayCleanup || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  const generatedAdCleanup = window.VideoBrowserGeneratedAdCleanup || {};
  window.VideoBrowserGenericAdOverlayCleanup = cleanup;

  cleanup.run = cleanup.run || function (state, options) {
    if (!document.body) return false;

    const roots = [];
    collectGenericAdOverlayCandidates().forEach(function (candidate) {
      const root = findGenericAdOverlayRoot(candidate);
      if (root && roots.indexOf(root) === -1) roots.push(root);
    });

    roots.forEach(function (root) {
      hideElement(root, 'generic-ad-overlay');
      hideGenericOverlayBackdrops(root);
      clearOverlayScrollLocks();
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

  function collectGenericAdOverlayCandidates() {
    const candidates = [];
    function addCandidate(element) {
      if (element && candidates.indexOf(element) === -1) candidates.push(element);
    }

    [
      '[role="dialog"]',
      '[aria-modal="true"]',
      '[class*="modal"]',
      '[class*="Modal"]',
      '[class*="popup"]',
      '[class*="Popup"]',
      '[class*="pop"]',
      '[class*="Pop"]',
      '[class*="mask"]',
      '[class*="Mask"]',
      '[class*="overlay"]',
      '[class*="Overlay"]',
      '[class*="dialog"]',
      '[class*="Dialog"]',
      '[class*="layer"]',
      '[class*="Layer"]',
      '[class*="float"]',
      '[class*="Float"]',
      '[class*="promotion"]',
      '[class*="advert"]',
      '[style*="position: fixed"]',
      '[style*="position:fixed"]',
      '[style*="position: absolute"]',
      '[style*="position:absolute"]'
    ].forEach(function (selector) {
      queryAll(selector).forEach(addCandidate);
    });

    queryAll('button,a,i,[role="button"],[aria-label],[title]').forEach(function (element) {
      if (isCloseLikeControl(element)) addCandidate(element);
    });
    queryAll('img,picture,svg').forEach(function (element) {
      const rect = element.getBoundingClientRect();
      const source = mediaSourceValue(element);
      if (
        (rect.width >= 32 && rect.height >= 32) ||
        /^data:image\//i.test(source) ||
        /ad|ads|adv|advert|banner|promo|promotion|taojianghu|sf-express|alicdn|gif/i.test(source)
      ) {
        addCandidate(element);
      }
    });

    queryAll('body *').forEach(function (element) {
      if (!element || isProtectedAppContainer(element) || element.querySelector('video')) return;

      const style = getComputedStyle(element);
      if (!/fixed|absolute|sticky/i.test(style.position)) return;

      const rect = element.getBoundingClientRect();
      const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
      const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
      if (!rect.width || !rect.height || !viewportWidth || !viewportHeight) return;
      if (rect.bottom <= 0 || rect.right <= 0 || rect.top >= viewportHeight || rect.left >= viewportWidth) return;
      if (rect.width < 24 || rect.height < 24) return;
      if (rect.width > Math.min(viewportWidth * 0.9, 360)) return;
      if (rect.height > Math.min(viewportHeight * 0.55, 420)) return;

      const descriptor = elementDescriptor(element);
      const text = normalizeText(element.innerText || element.textContent);
      const promoText = /官方推荐|APP亲测无毒|狼友多下载|防丢失|招嫖约炮|情趣用品|注册即送|超高爆率|连麦|操控|同城约炮|超级巨奖|财富放水|PG娱乐|PG电子|PG游戏|私人订制|抢庄牛牛|附近上门|上门服务|学生空降|学生兼职|外围兼职|1元拉爆|新葡京|开户送钱|送\d+元|提款秒到|充提秒到/i
        .test(text);
      const compactPromo = rect.width >= viewportWidth * 0.5 &&
        rect.height <= Math.min(viewportHeight * 0.42, 320);
      const widePromoGrid = rect.width >= viewportWidth * 0.82 &&
        rect.height <= Math.max(viewportHeight * 1.8, 1400);
      if (promoText && (compactPromo || widePromoGrid)) {
        addCandidate(element);
      }

      const adSignal = /广告|廣告|推广|推廣|赞助|贊助|立即下载|立即安装|打开APP|下载APP|福利|红包|领取|客服|加微信|棋牌|彩票|博彩|download|install|openapp|adult|casino|sponsor|promotion/i
        .test(text + ' ' + descriptor);
      const nameSignal = /ad|ads|adv|advert|sponsor|promo|promotion|download|openapp|banner|float|popup|modal|layer|icon-close/i
        .test(descriptor);
      const hasMedia = Boolean(element.querySelector('img,picture,svg'));
      if (hasCloseLikeDescendant(element) || adSignal || (hasMedia && nameSignal)) {
        addCandidate(element);
      }
    });

    queryAll('body *').forEach(function (element) {
      if (!element || isProtectedAppContainer(element) || element.querySelector('video,form,input,textarea,select')) {
        return;
      }

      const text = normalizeText(element.innerText || element.textContent);
      if (!/官方推荐|APP亲测无毒|狼友多下载|防丢失|招嫖约炮|情趣用品|注册即送|超高爆率|连麦|操控|同城约炮|超级巨奖|财富放水|PG娱乐|PG电子|PG游戏|私人订制|抢庄牛牛|附近上门|上门服务|学生空降|学生兼职|外围兼职|1元拉爆|新葡京|开户送钱|送\d+元|提款秒到|充提秒到/i.test(text)) {
        return;
      }

      const rect = element.getBoundingClientRect();
      const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
      const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
      if (!rect.width || !rect.height || !viewportWidth || !viewportHeight) {
        addCandidate(element);
        return;
      }
      const compactPromo = rect.width >= viewportWidth * 0.5 &&
        rect.height <= Math.min(viewportHeight * 0.42, 320);
      const widePromoGrid = rect.width >= viewportWidth * 0.82 &&
        rect.height <= Math.max(viewportHeight * 1.8, 1400);
      if (compactPromo || widePromoGrid) {
        addCandidate(element);
      }
    });

    return candidates;
  }

  function findGenericAdOverlayRoot(element) {
    let current = element;
    let matchedRoot = null;
    for (let depth = 0; current && depth < 9; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (isProtectedAppContainer(current)) break;
      if (isLikelyGenericAdOverlay(current) && shouldUseGenericAdOverlayRoot(matchedRoot, current)) {
        matchedRoot = current;
      }
    }
    return matchedRoot;
  }

  function shouldUseGenericAdOverlayRoot(currentRoot, candidateRoot) {
    if (!currentRoot) return true;

    const style = getComputedStyle(candidateRoot);
    const descriptor = elementDescriptor(candidateRoot);
    const canPromoteLayer = /fixed|absolute|sticky/i.test(style.position) &&
      (
        parseZIndex(style.zIndex) >= 10 ||
        /modal|popup|pop|mask|overlay|dialog|layer/i.test(descriptor)
      );
    if (canPromoteLayer) return true;

    const currentRect = currentRoot.getBoundingClientRect();
    const candidateRect = candidateRoot.getBoundingClientRect();
    const currentArea = currentRect.width * currentRect.height;
    const candidateArea = candidateRect.width * candidateRect.height;
    if (!currentArea || !candidateArea) return false;
    return candidateArea <= currentArea * 2.8;
  }

  function isLikelyGenericAdOverlay(element) {
    if (!element || isProtectedAppContainer(element)) return false;
    if (element.querySelector('video')) return false;

    const descriptor = elementDescriptor(element);
    const text = normalizeText(element.innerText || element.textContent);
    const promoTextLike = /官方推荐|APP亲测无毒|狼友多下载|防丢失|招嫖约炮|情趣用品|注册即送|超高爆率|连麦|操控|同城约炮|超级巨奖|财富放水|PG娱乐|PG电子|PG游戏|私人订制|抢庄牛牛|附近上门|上门服务|学生空降|学生兼职|外围兼职|1元拉爆|新葡京|开户送钱|送\d+元|提款秒到|充提秒到/i
      .test(text);
    const adTextLike = /广告|廣告|推广|推廣|赞助|贊助|立即下载|立即安装|打开APP|下载APP|福利|红包|领取|客服|加微信|棋牌|彩票|博彩|adult|casino|sponsor|promotion/i
      .test(text);
    const adNameLike = /(^|[-_\s])(ad|ads|adv|advert|sponsor|promo|promotion|download|openapp|banner)([-_\s]|$)/i
      .test(descriptor);
    const hasClose = hasCloseLikeDescendant(element);
    const hasMediaOrAction = Boolean(
      element.querySelector('img,picture,svg,a[href],button,i,[role="button"],[onclick],[class*="icon-"]')
    );
    const hasMedia = Boolean(element.querySelector('img,picture,svg'));
    const formHeavy = element.querySelectorAll('input,textarea,select').length > 0 &&
      !adNameLike &&
      !adTextLike;
    if (formHeavy) return false;

    const rect = element.getBoundingClientRect();
    const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
    const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
    if (!rect.width || !rect.height || !viewportWidth || !viewportHeight) {
      const zeroLayoutAdNameLike = /modal|popup|pop|mask|overlay|dialog|layer|float|ad|ads|adv|advert|promo|promotion|download|install|openapp|banner/i
        .test(descriptor);
      if (promoTextLike && text.length <= 260) {
        return true;
      }
      return (zeroLayoutAdNameLike || adNameLike || adTextLike) &&
        hasMediaOrAction &&
        (hasClose || hasMedia || adNameLike || adTextLike);
    }
    if (rect.bottom <= 0 || rect.right <= 0 || rect.top >= viewportHeight || rect.left >= viewportWidth) {
      return false;
    }

    const style = getComputedStyle(element);
    const positioned = /fixed|absolute|sticky/i.test(style.position);
    const zIndex = parseZIndex(style.zIndex);
    const layerNameLike = /modal|popup|pop|mask|overlay|dialog|layer|float/i.test(descriptor);
    const highLayer = zIndex >= 10 || layerNameLike;
    const imageOnlyWideBanner = !positioned &&
      rect.width >= viewportWidth * 0.82 &&
      rect.height >= 48 &&
      rect.height <= Math.max(viewportHeight * 1.1, 360) &&
      text.length <= 20 &&
      hasMedia &&
      !element.querySelector('video,form,input,textarea,select') &&
      mediaSourceLooksLikeAd(element);
    const inlinePromoBlock = rect.width >= viewportWidth * 0.82 &&
      rect.height >= 8 &&
      rect.height <= Math.max(viewportHeight * 1.8, 1400) &&
      text.length <= 900 &&
      promoTextLike &&
      !element.querySelector('video,form,input,textarea,select');
    if (!positioned && !highLayer && !imageOnlyWideBanner && !inlinePromoBlock) return false;

    const fullOverlay = rect.width >= viewportWidth * 0.86 &&
      rect.height >= viewportHeight * 0.48 &&
      rect.left <= viewportWidth * 0.12 &&
      rect.top <= viewportHeight * 0.22;
    const centeredFloat = highLayer &&
      rect.width >= Math.min(viewportWidth * 0.42, 260) &&
      rect.height >= Math.min(viewportHeight * 0.14, 180) &&
      rect.left < viewportWidth * 0.92 &&
      rect.right > viewportWidth * 0.08 &&
      rect.top < viewportHeight * 0.92 &&
      rect.bottom > viewportHeight * 0.08;
    const edgeFloat = positioned &&
      rect.width >= 32 &&
      rect.width <= Math.min(viewportWidth * 0.46, 260) &&
      rect.height >= 32 &&
      rect.height <= Math.min(viewportHeight * 0.34, 280) &&
      (
        rect.right >= viewportWidth - 16 ||
        rect.left <= 16 ||
        rect.bottom >= viewportHeight - 16 ||
        rect.top <= 16
      );
    const bottomActionBar = positioned &&
      rect.width >= viewportWidth * 0.52 &&
      rect.height >= 36 &&
      rect.height <= Math.min(viewportHeight * 0.22, 150) &&
      (rect.bottom >= viewportHeight - 220 || rect.top >= viewportHeight * 0.62);
    const inlineMediaTile = !positioned &&
      rect.width >= 60 &&
      rect.width <= Math.min(viewportWidth * 0.46, 260) &&
      rect.height >= 60 &&
      rect.height <= Math.min(viewportHeight * 0.34, 280) &&
      hasClose &&
      hasMedia &&
      text.length <= 80;
    const inlineActionBar = !positioned &&
      rect.width >= viewportWidth * 0.52 &&
      rect.height >= 36 &&
      rect.height <= Math.min(viewportHeight * 0.22, 150) &&
      hasMediaOrAction &&
      /下载|安裝|安装|APP|app|桌面|download|install|openapp/i.test(text + ' ' + descriptor);

    if (
      !fullOverlay &&
      !centeredFloat &&
      !edgeFloat &&
      !bottomActionBar &&
      !inlineMediaTile &&
      !inlineActionBar &&
      !imageOnlyWideBanner &&
      !inlinePromoBlock
    ) {
      return false;
    }

    if (inlinePromoBlock) {
      return true;
    }
    if (imageOnlyWideBanner) {
      return true;
    }
    if (fullOverlay && (
      adNameLike ||
      adTextLike ||
      (hasClose && (hasMedia || text.length <= 80))
    )) {
      return true;
    }
    if (centeredFloat && hasMediaOrAction && (adNameLike || adTextLike || (hasClose && hasMedia))) {
      return true;
    }
    if (bottomActionBar && (hasMediaOrAction || adTextLike) && (
      adTextLike ||
      /下载|安裝|安装|APP|app|桌面|download|install|openapp/i.test(text + ' ' + descriptor)
    )) {
      return true;
    }
    if (inlineMediaTile || inlineActionBar) {
      return true;
    }
    return edgeFloat && hasMediaOrAction && (
      hasClose ||
      adNameLike ||
      adTextLike ||
      (hasMedia && zIndex >= 10 && text.length <= 20)
    );
  }

  function mediaSourceLooksLikeAd(element) {
    return Array.prototype.some.call(
      element.querySelectorAll('img,source'),
      function (media) {
        const value = mediaSourceValue(media);
        return /^data:image\//i.test(value) ||
          /ad|ads|adv|advert|banner|promo|promotion|taojianghu|sf-express|alicdn|gif/i.test(value);
      }
    );
  }

  function mediaSourceValue(media) {
    return String(
      media &&
      (
        media.currentSrc ||
        media.src ||
        media.getAttribute('src') ||
        media.getAttribute('srcset') ||
        ''
      )
    );
  }

  function hasCloseLikeDescendant(element) {
    if (!element) return false;
    if (isCloseLikeControl(element)) return true;
    return Array.prototype.some.call(
      element.querySelectorAll(
        'button,a,i,[role="button"],[aria-label],[title],' +
        '[class*="close"],[class*="Close"],[class*="icon-close"]'
      ),
      isCloseLikeControl
    );
  }

  function isCloseLikeControl(element) {
    if (!element) return false;
    const rect = element.getBoundingClientRect();
    const text = normalizeText(
      element.innerText ||
      element.textContent ||
      element.getAttribute('aria-label') ||
      element.getAttribute('title') ||
      element.getAttribute('alt')
    );
    const descriptor = elementDescriptor(element);
    const compactText = text.replace(/\s+/g, '');
    if (/^(×|x|X|✕|✖|关闭|關閉|取消|跳过|跳過|稍后|稍後|不再提示|close|skip|dismiss)$/i.test(compactText)) {
      return true;
    }
    return rect.width <= 72 &&
      rect.height <= 72 &&
      /close|dismiss|cancel|skip|关闭|關閉|跳过|跳過/i.test(descriptor + ' ' + text);
  }

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

  function clearOverlayScrollLocks() {
    unlockScrollContainer(document.documentElement);
    unlockScrollContainer(document.body);
  }

  function unlockScrollContainer(element) {
    if (!element) return;
    [
      'overflow',
      'overflow-x',
      'overflow-y',
      'position',
      'height',
      'touch-action',
      'overscroll-behavior'
    ].forEach(function (property) {
      element.style.removeProperty(property);
    });

    if (!element.classList) return;
    Array.prototype.slice.call(element.classList).forEach(function (className) {
      if (isScrollLockClass(className)) {
        element.classList.remove(className);
      }
    });
  }

  function isScrollLockClass(className) {
    return /(^|[-_])(overflow-hidden|no-scroll|noscroll|scroll-lock|lock-scroll)([-_]|$)/i
      .test(String(className || '')) ||
      /^adm-overflow-hidden$/i.test(String(className || ''));
  }

  function normalizeText(value) {
    return selectorTools.normalizeText(value);
  }

  function elementDescriptor(element) {
    return domTools.elementDescriptor(element);
  }

  function parseZIndex(value) {
    return domTools.parseZIndex(value);
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
