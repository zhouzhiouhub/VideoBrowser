/*
 * Shared detector for custom HTML5 video player controls.
 */
(function () {
  const detector = window.VideoBrowserVideoCustomControlDetector || {};
  const geometry = window.VideoBrowserGeometry || {};
  const domTools = window.VideoBrowserDomTools || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  window.VideoBrowserVideoCustomControlDetector = detector;

  const controlSelectors = [
    '.xgplayer-controls',
    '.xgplayer-progress',
    '.xgplayer-start',
    '.dplayer-controller',
    '.dplayer-icons',
    '.art-controls',
    '.art-control',
    '.vjs-control-bar',
    '.jw-controls',
    '.plyr__controls',
    '.ckplayer-control',
    '.ckplayer-controls',
    '.prism-controlbar',
    '.mejs__controls',
    '[class*="player-control"]',
    '[class*="player_control"]',
    '[class*="video-control"]',
    '[class*="video_control"]',
    '[class*="control-bar"]',
    '[class*="controlbar"]'
  ];

  detector.hasControls = detector.hasControls || function (video) {
    const root = detector.rootFor(video);
    if (!root) return false;
    const controls = controlSelectors.some(function (selector) {
      return queryAllWithin(root, selector).some(function (element) {
        return isLikelyControlElement(element, video);
      });
    });
    if (controls) return true;

    return queryAllWithin(root, 'button,[role="button"],input[type="range"]').some(function (element) {
      return isLikelyMediaControlElement(element, video);
    });
  };

  detector.rootFor = detector.rootFor || function (video) {
    if (!video || !video.isConnected) return null;
    let current = video.parentElement || video;
    for (let depth = 0; current && depth < 8; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (!current.contains(video)) continue;
      if (isLikelyPlayerRoot(current)) return current;
    }
    return video.parentElement || video;
  };

  function isLikelyPlayerRoot(element) {
    const descriptor = elementDescriptor(element).toLowerCase();
    return /xgplayer|dplayer|artplayer|jwplayer|video-js|vjs-|plyr|ckplayer|prism-player|mejs|hls-player|video-player|player-container|player-wrap|player_box|playerbox|video-container|video-wrap|video_box|videobox/i
      .test(descriptor);
  }

  function isLikelyControlElement(element, video) {
    if (!element || element === video || element.querySelector('video')) return false;
    if (element.getAttribute('data-videobrowser-dismissed')) return false;

    const rect = element.getBoundingClientRect();
    const videoRect = video && typeof video.getBoundingClientRect === 'function'
      ? video.getBoundingClientRect()
      : null;
    if (!rect || rect.width <= 0 || rect.height <= 0) {
      return true;
    }
    if (!videoRect || videoRect.width <= 0 || videoRect.height <= 0) {
      return true;
    }
    return geometry.rectsOverlap(rect, geometry.expandedRect(videoRect, 12));
  }

  function isLikelyMediaControlElement(element, video) {
    if (!isLikelyControlElement(element, video)) return false;
    const descriptor = elementDescriptor(element).toLowerCase();
    const text = normalizeText(
      element.innerText ||
      element.textContent ||
      element.getAttribute('aria-label') ||
      element.getAttribute('title')
    ).toLowerCase();
    if (element.matches && element.matches('input[type="range"]')) return true;
    return /play|pause|seek|progress|volume|fullscreen|screenfull|control|播放|暂停|进度|音量|全屏/i
      .test(descriptor + ' ' + text);
  }

  function normalizeText(value) {
    return selectorTools.normalizeText(value);
  }

  function elementDescriptor(element) {
    return domTools.elementDescriptor(element);
  }

  function queryAllWithin(root, selector) {
    return selectorTools.queryAllWithin(root, selector);
  }
})();
