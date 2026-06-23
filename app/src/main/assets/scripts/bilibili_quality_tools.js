/*
 * Bilibili-specific quality preference helpers.
 */
(function () {
  var tools = window.VideoBrowserBilibiliQualityTools || {};
  var adapterDefaults = window.VideoBrowserSiteAdapterTools || {};
  window.VideoBrowserBilibiliQualityTools = tools;

  tools.preferBestQuality = tools.preferBestQuality || function (adapterTools, playerApi) {
    var helpers = qualityHelpers(adapterTools, playerApi);
    return preferBestQualityByApi(helpers) || preferBestQualityByMenu(helpers);
  };

  function qualityHelpers(adapterTools, playerApi) {
    var adapter = adapterTools || {};
    var api = playerApi || {};
    return {
      query: typeof adapter.query === 'function' ? adapter.query : adapterDefaults.emptyQuery,
      queryWithin: typeof adapter.queryWithin === 'function' ? adapter.queryWithin : adapterDefaults.emptyQuery,
      textOf: typeof adapter.textOf === 'function' ? adapter.textOf : adapterDefaults.textOf,
      normalizeText: typeof adapter.normalizeText === 'function' ? adapter.normalizeText : adapterDefaults.normalizeText,
      visibleElement: typeof adapter.visibleElement === 'function' ? adapter.visibleElement : adapterDefaults.noop,
      logVideoDiagnostic: typeof adapter.logVideoDiagnostic === 'function' ? adapter.logVideoDiagnostic : adapterDefaults.noop,
      readPlayerMethod: typeof api.read === 'function' ? api.read : adapterDefaults.nullResult,
      callPlayerMethod: typeof api.call === 'function' ? api.call : adapterDefaults.nullResult,
      handledValue: typeof api.handledValue === 'function' ? api.handledValue : adapterDefaults.nullResult
    };
  }

  function qualityValueOf(candidate) {
    if (typeof candidate === 'number') return candidate;
    if (typeof candidate === 'string') {
      var parsed = parseInt(candidate, 10);
      return Number.isFinite(parsed) ? parsed : null;
    }
    if (!candidate || typeof candidate !== 'object') return null;
    var keys = ['qn', 'quality', 'value', 'id', 'code'];
    for (var index = 0; index < keys.length; index += 1) {
      var value = candidate[keys[index]];
      var numericValue = typeof value === 'number' ? value : parseInt(String(value || ''), 10);
      if (Number.isFinite(numericValue)) return numericValue;
    }
    return null;
  }

  function bestApiQualityValue(helpers) {
    var qualityList = helpers.readPlayerMethod([
      'getSupportedQuality',
      'getSupportedQualities',
      'getAvailableQuality',
      'getAvailableQualities',
      'getQualityList',
      'getVideoQualityList',
      'qualityList'
    ]);
    if (!Array.isArray(qualityList) || !qualityList.length) return null;

    var best = null;
    qualityList.forEach(function (candidate) {
      var value = qualityValueOf(candidate);
      if (!Number.isFinite(value)) return;
      if (best === null || value > best) best = value;
    });
    return best;
  }

  function preferBestQualityByApi(helpers) {
    var quality = bestApiQualityValue(helpers);
    if (!Number.isFinite(quality)) return null;
    return helpers.handledValue(
      helpers.callPlayerMethod(['setQuality', 'setVideoQuality', 'switchQuality', 'changeQuality'], [quality]),
      true
    );
  }

  function qualityScore(text, helpers) {
    var value = helpers.normalizeText(text);
    if (!value) return 0;
    if (/8k/i.test(value)) return 8000;
    if (/杜比|dolby/i.test(value)) return 7600;
    if (/hdr|真彩/i.test(value)) return 7400;
    if (/4k|2160/i.test(value)) return 7000;
    if (/1080\s*p?\s*\+|1080p?\s*60|60\s*帧/i.test(value)) return 6200;
    if (/1080/i.test(value)) return 6000;
    if (/720|高清/i.test(value)) return 5000;
    if (/480/i.test(value)) return 4000;
    if (/360|流畅/i.test(value)) return 3000;
    return 0;
  }

  function clickableQualityElement(element) {
    var current = element;
    for (var depth = 0; current && depth < 4; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      if (current.matches && current.matches('button,a,li,[role="button"],[role="menuitem"]')) {
        return current;
      }
    }
    return element;
  }

  function bestVisibleQualityOption(helpers) {
    var candidates = [];
    helpers.query('[class*="quality"],[class*="Quality"],[aria-label*="\u753b\u8d28"],[title*="\u753b\u8d28"]').forEach(function (root) {
      if (!root || root.querySelector('video')) return;
      var elements = helpers.queryWithin(root,
        'button,a,li,span,div,[role="button"],[role="menuitem"]'
      );
      if (root.matches && root.matches('button,a,li,[role="button"],[role="menuitem"]')) {
        elements.unshift(root);
      }
      elements.forEach(function (element) {
        if (!helpers.visibleElement(element)) return;
        var text = helpers.textOf(element);
        var score = qualityScore(text, helpers);
        if (score <= 0) return;
        var clickable = clickableQualityElement(element);
        if (!clickable || typeof clickable.click !== 'function') return;
        candidates.push({
          element: clickable,
          score: score,
          text: text
        });
      });
    });
    candidates.sort(function (first, second) {
      return second.score - first.score;
    });
    return candidates[0] || null;
  }

  function clickQualityMenuControl(helpers) {
    var controls = Array.prototype.slice.call(helpers.query(
      '.bpx-player-ctrl-quality,.bilibili-player-video-quality,[class*="quality"],[aria-label*="\u753b\u8d28"],[title*="\u753b\u8d28"]'
    ));
    for (var index = 0; index < controls.length; index += 1) {
      var control = controls[index];
      if (!helpers.visibleElement(control) || control.querySelector('video')) continue;
      var text = helpers.textOf(control);
      if (!/\u753b\u8d28|quality|清晰|720|1080|4k|8k/i.test(text + ' ' + String(control.className || ''))) continue;
      try {
        control.click();
        return true;
      } catch (_) {}
    }
    return false;
  }

  function preferBestQualityByMenu(helpers) {
    var option = bestVisibleQualityOption(helpers);
    if (option) {
      try {
        option.element.click();
        helpers.logVideoDiagnostic('quality-menu-select', 'text=' + option.text);
        return true;
      } catch (_) {
        return false;
      }
    }

    if (!clickQualityMenuControl(helpers)) return false;
    window.setTimeout(function () {
      var delayedOption = bestVisibleQualityOption(helpers);
      if (!delayedOption) return;
      try {
        delayedOption.element.click();
        helpers.logVideoDiagnostic('quality-menu-select', 'text=' + delayedOption.text);
      } catch (_) {}
    }, 0);
    return true;
  }
})();
