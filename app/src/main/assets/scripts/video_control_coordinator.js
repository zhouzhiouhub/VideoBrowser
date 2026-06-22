/*
 * Shared video control enablement coordinator.
 */
(function () {
  const coordinator = window.VideoBrowserVideoControlCoordinator || {};
  const nativeBridge = window.VideoBrowserNativeBridge || {};
  const videoControlTools = window.VideoBrowserVideoControlTools || {};
  const siteVideoCapabilityBroker = window.VideoBrowserSiteVideoCapabilityBroker || {};
  const customControlDetector = window.VideoBrowserVideoCustomControlDetector || {};
  window.VideoBrowserVideoControlCoordinator = coordinator;

  coordinator.cleanupLegacyOverlays = coordinator.cleanupLegacyOverlays || function (state, options) {
    videoControlTools.cleanupLegacyOverlays(state, options);
  };

  coordinator.enableControls = coordinator.enableControls || function (video) {
    const siteResult = siteVideoCapabilityBroker.invoke(video, 'enableControls', []);
    if (siteResult.handled) {
      logVideoDiagnostic('enable-controls-site', videoLogDetails(video, {
        handled: true,
        result: siteResult.value
      }));
      return true;
    }

    if (customControlDetector.hasControls(video)) {
      coordinator.removeNativeControls(video, 'custom-player');
      logVideoDiagnostic('enable-controls-custom-player', videoLogDetails(video, {
        handled: true
      }));
      return true;
    }

    videoControlTools.enableNativeControls(video);
    logVideoDiagnostic('enable-controls-native', videoLogDetails(video, {
      handled: false
    }));
    return true;
  };

  coordinator.removeNativeControls = coordinator.removeNativeControls || function (video, reason) {
    const hadNativeControls = videoControlTools.removeNativeControls(video);
    if (hadNativeControls) {
      logVideoDiagnostic('remove-native-controls-generic', videoLogDetails(video, {
        reason: reason || 'custom-player'
      }));
    }
    return hadNativeControls;
  };

  function logVideoDiagnostic(event, details) {
    if (typeof nativeBridge.logPageVideoDiagnostic === 'function') {
      nativeBridge.logPageVideoDiagnostic(event, details);
    }
  }

  function videoLogDetails(video, extra) {
    return typeof nativeBridge.videoLogDetails === 'function'
      ? nativeBridge.videoLogDetails(video, extra)
      : extra || {};
  }
})();
