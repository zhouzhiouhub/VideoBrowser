/*
 * Shared cleanup for generated fixed-position ad scaffolds.
 */
(function () {
  const cleanup = window.VideoBrowserGeneratedAdCleanup || {};
  const geometry = window.VideoBrowserGeometry || {};
  const domTools = window.VideoBrowserDomTools || {};
  const domActions = window.VideoBrowserDomActions || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  window.VideoBrowserGeneratedAdCleanup = cleanup;

  const generatedAdCleanupIntervalMs = 100;

  cleanup.run = cleanup.run || function (state, options) {
    const config = options || {};
    const timestamp = Number(config.now || Date.now());
    if (!config.force && timestamp - Number(state && state.lastGeneratedAdCleanupAt || 0) < generatedAdCleanupIntervalMs) {
      return false;
    }
    if (state) state.lastGeneratedAdCleanupAt = timestamp;
    cleanup.removeScaffolds();
    return true;
  };

  cleanup.removeScaffolds = cleanup.removeScaffolds || function () {
    const viewportWidth = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
    const viewportHeight = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
    if (!viewportWidth || !viewportHeight) return;

    const imageSlices = [];
    const clickGridCells = [];
    const adjunctControls = [];
    domTools.queryAll('body *').forEach(function (element) {
      if (!element || domActions.isProtectedAppContainer(element)) return;

      const style = getComputedStyle(element);
      if (style.position !== 'fixed') return;

      const rect = element.getBoundingClientRect();
      if (!geometry.visibleRectInViewport(rect, viewportWidth, viewportHeight)) return;

      if (isGeneratedImageSlice(element, style, rect, viewportWidth, viewportHeight)) {
        imageSlices.push(element);
        return;
      }
      if (isGeneratedClickGridCell(element, style, rect, viewportWidth, viewportHeight)) {
        clickGridCells.push(element);
        return;
      }
      if (isGeneratedAdAdjunctControl(element, style, rect)) {
        adjunctControls.push(element);
      }
    });

    if (imageSlices.length >= 12) {
      imageSlices.forEach(function (element) {
        domActions.hideElement(element, {
          reason: 'generated-sliced-ad',
          protectAppContainers: true
        });
      });
      adjunctControls.forEach(function (element) {
        domActions.hideElement(element, {
          reason: 'generated-sliced-ad',
          protectAppContainers: true
        });
      });
    }

    if (clickGridCells.length >= 20) {
      clickGridCells.forEach(function (element) {
        domActions.hideElement(element, {
          reason: 'generated-click-grid',
          protectAppContainers: true
        });
      });
    }
  };

  function isGeneratedImageSlice(element, style, rect, viewportWidth, viewportHeight) {
    if (domTools.parseZIndex(style.zIndex) < 1000000) return false;
    if (!/^url\(["']?data:image\//i.test(String(style.backgroundImage || ''))) return false;
    if (selectorTools.normalizeText(element.innerText || element.textContent).length > 40) return false;
    if (element.querySelector('video,form,input,textarea,select')) return false;
    if (rect.width < Math.max(8, viewportWidth * 0.035)) return false;
    if (rect.width > Math.min(140, viewportWidth * 0.34)) return false;
    if (rect.height < 8 || rect.height > Math.min(180, viewportHeight * 0.3)) return false;
    return true;
  }

  function isGeneratedClickGridCell(element, style, rect, viewportWidth, viewportHeight) {
    const opacity = Number.parseFloat(style.opacity);
    if (!Number.isFinite(opacity) || opacity > 0.02) return false;
    const zIndex = domTools.parseZIndex(style.zIndex);
    if (zIndex < 10 || zIndex > 999999) return false;
    if (selectorTools.normalizeText(element.innerText || element.textContent).length > 0) return false;
    if (element.querySelector('video,form,input,textarea,select,img,svg,canvas')) return false;
    if (rect.width < Math.max(16, viewportWidth * 0.035)) return false;
    if (rect.width > Math.min(120, viewportWidth * 0.3)) return false;
    if (rect.height < 16 || rect.height > Math.min(120, viewportHeight * 0.22)) return false;
    return true;
  }

  function isGeneratedAdAdjunctControl(element, style, rect) {
    if (domTools.parseZIndex(style.zIndex) < 1000000) return false;
    if (selectorTools.normalizeText(element.innerText || element.textContent).length > 20) return false;
    if (rect.width > 56 || rect.height > 56) return false;
    const tagName = String(element.tagName || '').toLowerCase();
    const descriptor = domTools.elementDescriptor(element);
    return /^[a-z]{5,10}$/.test(tagName) ||
      (!descriptor.trim() && !String(style.backgroundImage || '').match(/^url\(/i));
  }
})();
