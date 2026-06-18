/*
 * Shared rectangle helpers for common and site adapter scripts.
 */
(function () {
  const geometry = window.VideoBrowserGeometry || {};
  window.VideoBrowserGeometry = geometry;

  geometry.safeRect = geometry.safeRect || function (element) {
    if (!element || typeof element.getBoundingClientRect !== 'function') return null;
    const rect = element.getBoundingClientRect();
    if (!rect || rect.width <= 0 || rect.height <= 0) return null;
    return rect;
  };

  geometry.expandedRect = geometry.expandedRect || function (rect, amount) {
    return {
      left: rect.left - amount,
      right: rect.right + amount,
      top: rect.top - amount,
      bottom: rect.bottom + amount,
      width: rect.width + amount * 2,
      height: rect.height + amount * 2
    };
  };

  geometry.rectsOverlap = geometry.rectsOverlap || function (first, second) {
    return first.left < second.right &&
      first.right > second.left &&
      first.top < second.bottom &&
      first.bottom > second.top;
  };

  geometry.rectCenterX = geometry.rectCenterX || function (rect) {
    return rect.left + rect.width / 2;
  };

  geometry.rectCenterY = geometry.rectCenterY || function (rect) {
    return rect.top + rect.height / 2;
  };

  geometry.centerDistance = geometry.centerDistance || function (first, second) {
    const dx = geometry.rectCenterX(first) - geometry.rectCenterX(second);
    const dy = geometry.rectCenterY(first) - geometry.rectCenterY(second);
    return Math.sqrt(dx * dx + dy * dy);
  };
})();
