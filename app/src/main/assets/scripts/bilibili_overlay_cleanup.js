/*
 * Bilibili-specific video overlay cleanup.
 */
(function () {
  var tools = window.VideoBrowserBilibiliOverlayCleanup || {};
  window.VideoBrowserBilibiliOverlayCleanup = tools;

  var playbackOverlaySelectors = [
    '.mplayer-play-icon',
    '.mplayer-pause-icon',
    '.mplayer-icon-play',
    '.mplayer-icon-pause',
    '.mplayer-state-play',
    '.mplayer-state-pause',
    '.bpx-player-state-wrap',
    '.bpx-player-state-play',
    '.bpx-player-state-pause',
    '.bilibili-player-video-state',
    '[class*="player-state"]',
    '[class*="state-play"]',
    '[class*="state-pause"]',
    '[class*="mplayer"][class*="play"]',
    '[class*="mplayer"][class*="pause"]'
  ];

  tools.hideVideoPlayPauseOverlays = tools.hideVideoPlayPauseOverlays || function (adapterTools) {
    var helpers = overlayHelpers(adapterTools);
    var videos = Array.prototype.slice.call(helpers.query('video')).filter(function (video) {
      return video && video.isConnected && !video.paused && !video.ended && video.readyState > 1;
    });
    if (!videos.length) return;

    playbackOverlaySelectors.forEach(function (selector) {
      helpers.query(selector).forEach(function (element) {
        var video = matchingVideoForOverlay(element, videos, helpers);
        if (!video || !isLikelyCenterPlaybackOverlay(element, video, helpers)) return;
        helpers.hideElement(playbackOverlayRoot(element, video, helpers), 'bilibili-video-play-overlay');
      });
    });
  };

  function overlayHelpers(adapterTools) {
    var tools = adapterTools || {};
    return {
      query: typeof tools.query === 'function' ? tools.query : emptyQuery,
      hideElement: typeof tools.hideElement === 'function' ? tools.hideElement : noop,
      safeRect: tools.safeRect,
      expandedRect: tools.expandedRect,
      rectsOverlap: tools.rectsOverlap,
      rectCenterX: tools.rectCenterX,
      rectCenterY: tools.rectCenterY,
      centerDistance: tools.centerDistance
    };
  }

  function matchingVideoForOverlay(element, videos, helpers) {
    var elementRect = helpers.safeRect(element);
    if (!elementRect) return null;

    var bestVideo = null;
    var bestDistance = Infinity;
    videos.forEach(function (video) {
      var videoRect = helpers.safeRect(video);
      if (!videoRect) return;
      if (!helpers.rectsOverlap(elementRect, helpers.expandedRect(videoRect, 16))) return;

      var distance = helpers.centerDistance(elementRect, videoRect);
      if (distance < bestDistance) {
        bestDistance = distance;
        bestVideo = video;
      }
    });
    return bestVideo;
  }

  function isLikelyCenterPlaybackOverlay(element, video, helpers) {
    if (!element || !video || element.querySelector('video')) return false;

    var descriptor = (String(element.id || '') + ' ' + String(element.className || '')).toLowerCase();
    if (!/(^|[-_\s])(play|pause|state|mplayer|bpx-player-state)([-_\s]|$)|player-state|state-play|state-pause/.test(descriptor)) {
      return false;
    }

    var elementRect = helpers.safeRect(element);
    var videoRect = helpers.safeRect(video);
    if (!elementRect || !videoRect) return false;
    if (!helpers.rectsOverlap(elementRect, helpers.expandedRect(videoRect, 16))) return false;

    var centerLimitX = Math.max(72, videoRect.width * 0.28);
    var centerLimitY = Math.max(54, videoRect.height * 0.32);
    var centerAligned =
      Math.abs(helpers.rectCenterX(elementRect) - helpers.rectCenterX(videoRect)) <= centerLimitX &&
      Math.abs(helpers.rectCenterY(elementRect) - helpers.rectCenterY(videoRect)) <= centerLimitY;
    if (!centerAligned) return false;

    var compactControl =
      elementRect.width <= Math.max(144, videoRect.width * 0.72) &&
      elementRect.height <= Math.max(144, videoRect.height * 0.72);
    var knownStateLayer = /mplayer|bpx-player-state|player-state|video-state|state-play|state-pause/.test(descriptor);
    return compactControl || knownStateLayer;
  }

  function playbackOverlayRoot(element, video, helpers) {
    var root = element;
    for (var depth = 0; depth < 3 && root.parentElement; depth += 1) {
      var parent = root.parentElement;
      if (parent === document.body || parent === document.documentElement) break;
      if (parent.querySelector('video')) break;
      if (!isLikelyCenterPlaybackOverlay(parent, video, helpers)) break;
      root = parent;
    }
    return root;
  }

  function emptyQuery() {
    return [];
  }

  function noop() {}
})();
