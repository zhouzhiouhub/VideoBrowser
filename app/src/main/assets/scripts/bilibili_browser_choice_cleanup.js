/*
 * Bilibili-specific browser choice prompt cleanup.
 */
(function () {
  var tools = window.VideoBrowserBilibiliBrowserChoiceCleanup || {};
  var adapterDefaults = window.VideoBrowserSiteAdapterTools || {};
  window.VideoBrowserBilibiliBrowserChoiceCleanup = tools;

  tools.dismissPrompts = tools.dismissPrompts || function (adapterTools) {
    var helpers = promptHelpers(adapterTools);
    if (!document.body) return;

    var pageText = helpers.normalizeText(document.body.innerText || document.body.textContent);
    var hasBrowserChoiceTitle = /浏览方式|browse mode/i.test(pageText) &&
      /推荐使用|recommended/i.test(pageText);
    if (!hasBrowserChoiceTitle || !/哔哩哔哩|bilibili|b站/i.test(pageText)) return;

    helpers.query(
      'div,section,aside,[role="dialog"],[class*="dialog"],[class*="modal"],' +
      '[class*="popup"],[class*="mask"],[class*="overlay"],[class*="sheet"]'
    ).forEach(function (element) {
      if (String(element.id || '').toLowerCase() === 'app') return;
      if (isBilibiliContentContainer(element)) return;

      var text = helpers.normalizeText(element.innerText || element.textContent);
      if (!(/浏览方式|browse mode/i.test(text) && /推荐使用|recommended/i.test(text))) return;
      if (!/哔哩哔哩|bilibili|b站/i.test(text)) return;

      var root = findBrowserChoicePromptRoot(element, helpers);
      if (!root) return;
      helpers.hideElement(root, 'bilibili-browser-choice');
      hideBrowserChoiceBackdrops(root, helpers);
      document.documentElement.style.overflow = '';
      if (document.body) document.body.style.overflow = '';
    });
  };

  function promptHelpers(adapterTools) {
    var tools = adapterTools || {};
    return {
      query: typeof tools.query === 'function' ? tools.query : adapterDefaults.emptyQuery,
      hideElement: typeof tools.hideElement === 'function' ? tools.hideElement : adapterDefaults.noop,
      normalizeText: typeof tools.normalizeText === 'function' ? tools.normalizeText : adapterDefaults.normalizeText,
      safeRect: typeof tools.safeRect === 'function' ? tools.safeRect : adapterDefaults.nullResult
    };
  }

  function findBrowserChoicePromptRoot(element, helpers) {
    var current = element;
    for (var depth = 0; current && depth < 8; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (isBilibiliContentContainer(current)) break;
      var rect = helpers.safeRect(current);
      if (!rect) continue;

      var style = getComputedStyle(current);
      var positioned = /fixed|absolute|sticky/i.test(style.position);
      var bottomSheetLike = rect.width >= window.innerWidth * 0.82 &&
        rect.height >= 96 &&
        rect.height <= window.innerHeight * 0.72 &&
        rect.bottom >= window.innerHeight - 6 &&
        rect.top >= window.innerHeight * 0.25;
      var fullOverlayLike = positioned &&
        rect.width >= window.innerWidth * 0.94 &&
        rect.height >= window.innerHeight * 0.82 &&
        String(current.id || '').toLowerCase() !== 'app';
      if (bottomSheetLike || fullOverlayLike) return current;
    }
    return null;
  }

  function isBilibiliContentContainer(element) {
    var descriptor = (String(element && element.id || '') + ' ' + String(element && element.className || ''))
      .toLowerCase();
    return /\bm-home\b|\bm-video\b|video-normal|player|recommend|feed-list|video-list|v-card/.test(descriptor);
  }

  function hideBrowserChoiceBackdrops(promptRoot, helpers) {
    helpers.query('body *').forEach(function (element) {
      if (!element || element === promptRoot || element.contains(promptRoot) || promptRoot.contains(element)) {
        return;
      }
      var rect = helpers.safeRect(element);
      if (!rect) return;

      var style = getComputedStyle(element);
      if (!/fixed|absolute|sticky/i.test(style.position)) return;
      var className = String(element.className || '');
      var overlayNameLike = /mask|overlay|modal|popup|dialog|shade|shadow/i.test(className);
      var fullScreenLike = rect.width >= window.innerWidth * 0.94 &&
        rect.height >= window.innerHeight * 0.82 &&
        rect.left <= 4 &&
        rect.top <= 4;
      if (!overlayNameLike && !fullScreenLike) return;

      var text = helpers.normalizeText(element.textContent);
      if (text.length > 40) return;
      helpers.hideElement(element, 'bilibili-browser-choice-backdrop');
    });
  }
})();
