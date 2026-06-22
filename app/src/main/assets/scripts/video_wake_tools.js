/*
 * Shared video control wake events.
 */
(function () {
  const tools = window.VideoBrowserVideoWakeTools || {};
  window.VideoBrowserVideoWakeTools = tools;

  tools.wake = tools.wake || function (video, options) {
    const callbacks = options || {};
    const target = video || (
      typeof callbacks.activeFullscreenVideo === 'function'
        ? callbacks.activeFullscreenVideo()
        : null
    );
    if (!target) return false;

    if (typeof callbacks.enableVideoControls === 'function') {
      callbacks.enableVideoControls(target);
    }
    if (typeof callbacks.reportPlaybackTimeline === 'function') {
      callbacks.reportPlaybackTimeline(target);
    }

    const fullscreenElement = document.fullscreenElement || document.webkitFullscreenElement;
    const root = fullscreenElement || target.parentElement || target;
    const rect = root && typeof root.getBoundingClientRect === 'function'
      ? root.getBoundingClientRect()
      : null;
    const clientX = rect && Number.isFinite(rect.left + rect.width / 2)
      ? rect.left + rect.width / 2
      : Math.max(1, window.innerWidth / 2);
    const clientY = rect && Number.isFinite(rect.top + rect.height / 2)
      ? rect.top + rect.height / 2
      : Math.max(1, window.innerHeight / 2);

    dispatchMouseWakeEvent(root, 'mousemove', clientX, clientY);
    dispatchMouseWakeEvent(target, 'mousemove', clientX, clientY);
    dispatchPointerWakeEvent(root, clientX, clientY);
    dispatchPointerWakeEvent(target, clientX, clientY);
    try { if (typeof target.focus === 'function') target.focus({ preventScroll: true }); } catch (_) {}
    return true;
  };

  function dispatchMouseWakeEvent(target, type, clientX, clientY) {
    if (!target || typeof target.dispatchEvent !== 'function') return;
    try {
      target.dispatchEvent(new MouseEvent(type, {
        bubbles: true,
        cancelable: true,
        view: window,
        clientX: clientX,
        clientY: clientY
      }));
    } catch (_) {}
  }

  function dispatchPointerWakeEvent(target, clientX, clientY) {
    if (!target || typeof target.dispatchEvent !== 'function' || typeof PointerEvent !== 'function') {
      return;
    }
    try {
      target.dispatchEvent(new PointerEvent('pointermove', {
        bubbles: true,
        cancelable: true,
        view: window,
        pointerType: 'touch',
        clientX: clientX,
        clientY: clientY
      }));
    } catch (_) {}
  }
})();
