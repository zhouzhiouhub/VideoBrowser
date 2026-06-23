/*
 * Shared generic ad overlay candidate and root detection.
 */
(function () {
  const detector = window.VideoBrowserGenericAdOverlayDetector || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  const geometry = window.VideoBrowserGeometry || {};
  const overlaySignals = window.VideoBrowserGenericAdOverlaySignals || {};
  window.VideoBrowserGenericAdOverlayDetector = detector;

  detector.collectCandidates = detector.collectCandidates || function () {
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
      domTools.queryAll(selector).forEach(addCandidate);
    });

    domTools.queryAll('button,a,i,[role="button"],[aria-label],[title]').forEach(function (element) {
      if (overlaySignals.isCloseLikeControl(element)) addCandidate(element);
    });
    domTools.queryAll('img,picture,svg').forEach(function (element) {
      const rect = geometry.safeRect(element);
      const source = overlaySignals.mediaSourceValue(element);
      if (
        (rect && rect.width >= 32 && rect.height >= 32) ||
        overlaySignals.mediaSourceLikeAd(source)
      ) {
        addCandidate(element);
      }
    });

    domTools.queryAll('body *').forEach(function (element) {
      if (!element || domActions.isProtectedAppContainer(element) || element.querySelector('video')) return;

      const style = getComputedStyle(element);
      if (!/fixed|absolute|sticky/i.test(style.position)) return;

      const rect = geometry.safeRect(element);
      const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
      const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
      if (!rect || !viewportWidth || !viewportHeight) return;
      if (!geometry.visibleRectInViewport(rect, viewportWidth, viewportHeight)) return;
      if (rect.width < 24 || rect.height < 24) return;
      if (rect.width > Math.min(viewportWidth * 0.9, 360)) return;
      if (rect.height > Math.min(viewportHeight * 0.55, 420)) return;

      const descriptor = domTools.elementDescriptor(element);
      const text = selectorTools.normalizeText(element.innerText || element.textContent);
      const promoText = overlaySignals.promoTextLike(text);
      const compactPromo = rect.width >= viewportWidth * 0.5 &&
        rect.height <= Math.min(viewportHeight * 0.42, 320);
      const widePromoGrid = rect.width >= viewportWidth * 0.82 &&
        rect.height <= Math.max(viewportHeight * 1.8, 1400);
      if (promoText && (compactPromo || widePromoGrid)) {
        addCandidate(element);
      }

      const adSignal = overlaySignals.adTextLike(text + ' ' + descriptor);
      const nameSignal = overlaySignals.nameSignalLike(descriptor);
      const hasMedia = Boolean(element.querySelector('img,picture,svg'));
      if (overlaySignals.hasCloseLikeDescendant(element) || adSignal || (hasMedia && nameSignal)) {
        addCandidate(element);
      }
    });

    domTools.queryAll('body *').forEach(function (element) {
      if (!element || domActions.isProtectedAppContainer(element) || element.querySelector('video,form,input,textarea,select')) {
        return;
      }

      const text = selectorTools.normalizeText(element.innerText || element.textContent);
      if (!overlaySignals.promoTextLike(text)) {
        return;
      }

      const rect = geometry.safeRect(element);
      const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
      const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
      if (!rect || !viewportWidth || !viewportHeight) {
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
  };

  detector.findRoot = detector.findRoot || function (element) {
    let current = element;
    let matchedRoot = null;
    for (let depth = 0; current && depth < 9; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (domActions.isProtectedAppContainer(current)) break;
      if (isLikelyGenericAdOverlay(current) && shouldUseGenericAdOverlayRoot(matchedRoot, current)) {
        matchedRoot = current;
      }
    }
    return matchedRoot;
  };

  detector.isLikelyOverlay = detector.isLikelyOverlay || function (element) {
    return isLikelyGenericAdOverlay(element);
  };

  detector.shouldUseRoot = detector.shouldUseRoot || function (currentRoot, candidateRoot) {
    return shouldUseGenericAdOverlayRoot(currentRoot, candidateRoot);
  };

  function shouldUseGenericAdOverlayRoot(currentRoot, candidateRoot) {
    if (!currentRoot) return true;

    const style = getComputedStyle(candidateRoot);
    const descriptor = domTools.elementDescriptor(candidateRoot);
    const canPromoteLayer = /fixed|absolute|sticky/i.test(style.position) &&
      (
        domTools.parseZIndex(style.zIndex) >= 10 ||
        /modal|popup|pop|mask|overlay|dialog|layer/i.test(descriptor)
      );
    if (canPromoteLayer) return true;

    const currentRect = geometry.safeRect(currentRoot);
    const candidateRect = geometry.safeRect(candidateRoot);
    if (!currentRect || !candidateRect) return false;
    const currentArea = currentRect.width * currentRect.height;
    const candidateArea = candidateRect.width * candidateRect.height;
    if (!currentArea || !candidateArea) return false;
    return candidateArea <= currentArea * 2.8;
  }

  function isLikelyGenericAdOverlay(element) {
    if (!element || domActions.isProtectedAppContainer(element)) return false;
    if (element.querySelector('video')) return false;

    const descriptor = domTools.elementDescriptor(element);
    const text = selectorTools.normalizeText(element.innerText || element.textContent);
    const promoTextLike = overlaySignals.promoTextLike(text);
    const adTextLike = overlaySignals.adTextLike(text);
    const adNameLike = overlaySignals.adNameLike(descriptor);
    const hasClose = overlaySignals.hasCloseLikeDescendant(element);
    const hasMediaOrAction = Boolean(
      element.querySelector('img,picture,svg,a[href],button,i,[role="button"],[onclick],[class*="icon-"]')
    );
    const hasMedia = Boolean(element.querySelector('img,picture,svg'));
    const formHeavy = domTools.queryAllWithin(element, 'input,textarea,select').length > 0 &&
      !adNameLike &&
      !adTextLike;
    if (formHeavy) return false;

    const rect = geometry.safeRect(element);
    const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
    const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
    if (!rect || !viewportWidth || !viewportHeight) {
      const zeroLayoutAdNameLike = overlaySignals.zeroLayoutNameLike(descriptor);
      if (promoTextLike && text.length <= 260) {
        return true;
      }
      return (zeroLayoutAdNameLike || adNameLike || adTextLike) &&
        hasMediaOrAction &&
        (hasClose || hasMedia || adNameLike || adTextLike);
    }
    if (!geometry.visibleRectInViewport(rect, viewportWidth, viewportHeight)) {
      return false;
    }

    const style = getComputedStyle(element);
    const positioned = /fixed|absolute|sticky/i.test(style.position);
    const zIndex = domTools.parseZIndex(style.zIndex);
    const layerNameLike = overlaySignals.layerNameLike(descriptor);
    const highLayer = zIndex >= 10 || layerNameLike;
    const imageOnlyWideBanner = !positioned &&
      rect.width >= viewportWidth * 0.82 &&
      rect.height >= 48 &&
      rect.height <= Math.max(viewportHeight * 1.1, 360) &&
      text.length <= 20 &&
      hasMedia &&
      !element.querySelector('video,form,input,textarea,select') &&
      overlaySignals.mediaSourceLooksLikeAd(element);
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
      overlaySignals.downloadActionLike(text + ' ' + descriptor);

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
      overlaySignals.downloadActionLike(text + ' ' + descriptor)
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

})();
