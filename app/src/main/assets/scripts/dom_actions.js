/*
 * Shared DOM mutation helpers for hiding and removing page elements.
 */
(function () {
  const actions = window.VideoBrowserDomActions || {};
  window.VideoBrowserDomActions = actions;

  actions.isPageRoot = actions.isPageRoot || function (element) {
    return !element || element === document.body || element === document.documentElement;
  };

  actions.isProtectedAppContainer = actions.isProtectedAppContainer || function (element) {
    const id = String(element && element.id || '').toLowerCase();
    return id === 'app' || id === 'root' || id === '__next' || id === 'nuxt';
  };

  actions.hasProtectedId = actions.hasProtectedId || function (element, protectedIds) {
    const ids = protectedIds || [];
    return ids.indexOf(String(element && element.id || '').toLowerCase()) !== -1;
  };

  actions.hideElement = actions.hideElement || function (element, options) {
    const config = options || {};
    if (actions.isPageRoot(element)) return false;
    if (config.protectAppContainers && actions.isProtectedAppContainer(element)) return false;
    if (actions.hasProtectedId(element, config.protectedIds)) return false;
    element.setAttribute(config.attributeName || 'data-videobrowser-dismissed', config.reason || 'cleanup');
    element.style.setProperty('display', 'none', 'important');
    element.style.setProperty('visibility', 'hidden', 'important');
    element.style.setProperty('pointer-events', 'none', 'important');
    return true;
  };

  actions.removeElement = actions.removeElement || function (element, options) {
    const config = options || {};
    if (actions.isPageRoot(element)) return false;
    if (config.protectAppContainers && actions.isProtectedAppContainer(element)) return false;
    if (actions.hasProtectedId(element, config.protectedIds)) return false;
    element.setAttribute(config.attributeName || 'data-videobrowser-dismissed', config.reason || 'remove');
    element.remove();
    return true;
  };
})();
