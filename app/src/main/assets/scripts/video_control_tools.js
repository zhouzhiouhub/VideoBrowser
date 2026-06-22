/*
 * Shared native video control toggles for common and site adapter scripts.
 */
(function () {
  const tools = window.VideoBrowserVideoControlTools || {};
  const domTools = window.VideoBrowserDomTools || {};
  const pageLifecycleTools = window.VideoBrowserPageLifecycleTools;
  window.VideoBrowserVideoControlTools = tools;

  tools.enableNativeControls = tools.enableNativeControls || function (video) {
    if (!video) return false;
    if (!video.controls) video.controls = true;
    if (video.getAttribute('controls') !== 'controls') {
      video.setAttribute('controls', 'controls');
    }
    if (video.hasAttribute('playsinline')) video.removeAttribute('playsinline');
    if (video.hasAttribute('webkit-playsinline')) video.removeAttribute('webkit-playsinline');
    if (video.style.maxWidth !== '100%') video.style.maxWidth = '100%';
    return true;
  };

  tools.removeNativeControls = tools.removeNativeControls || function (video) {
    if (!video) return false;
    const hadNativeControls = Boolean(video.controls || video.hasAttribute('controls'));
    try { video.controls = false; } catch (_) {}
    try { video.removeAttribute('controls'); } catch (_) {}
    return hadNativeControls;
  };

  tools.cleanupLegacyOverlays = tools.cleanupLegacyOverlays || function (state, options) {
    const targetState = state || {};
    const config = options || {};
    if (targetState.videoOverlays && typeof targetState.videoOverlays.forEach === 'function') {
      targetState.videoOverlays.forEach(function (controls) {
        if (controls && Array.isArray(controls.disposers)) {
          controls.disposers.forEach(function (dispose) {
            try { dispose(); } catch (_) {}
          });
          controls.disposers.length = 0;
        }
      });
      if (typeof targetState.videoOverlays.clear === 'function') {
        targetState.videoOverlays.clear();
      }
    }
    targetState.videoOverlays = null;

    pageLifecycleTools.runWithOptionalMutationSuppression(config, function () {
      domTools.queryAll('.__videobrowser_video_controls__').forEach(function (overlay) {
        overlay.remove();
      });
    });
  };
})();
