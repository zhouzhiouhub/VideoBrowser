/*
 * Shared selector construction helpers for picked elements.
 */
(function () {
  const tools = window.VideoBrowserElementPickerSelectorTools || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  window.VideoBrowserElementPickerSelectorTools = tools;

  tools.buildSelector = tools.buildSelector || function (element) {
    if (!element || element === document.body || element === document.documentElement) return null;

    const idSelector = selectorFromElementId(element);
    if (idSelector && isUniqueSelector(idSelector)) return idSelector;

    const classSelector = selectorFromElementClasses(element);
    if (classSelector && isUniqueSelector(classSelector)) return classSelector;

    const segments = [];
    let current = element;
    for (let depth = 0; current && depth < 5; depth += 1, current = current.parentElement) {
      if (current === document.body || current === document.documentElement) break;
      segments.unshift(selectorSegmentForElement(current));
      const candidate = segments.join(' ');
      if (selectorTools.isSafeSelector(candidate) && isUniqueSelector(candidate)) {
        return candidate;
      }
    }

    const fallback = segments.join(' ');
    return selectorTools.isSafeSelector(fallback) ? fallback : null;
  };

  tools.describeElement = tools.describeElement || function (element) {
    const parts = [elementTagName(element)];
    const id = String(element && element.id || '').trim();
    if (id) parts.push('#' + id.slice(0, 48));
    const classes = stableClassTokens(element).slice(0, 3);
    if (classes.length) parts.push('.' + classes.join('.'));
    const text = selectorTools.normalizeText(element && (element.innerText || element.textContent));
    if (text) parts.push(text.slice(0, 80));
    return parts.join(' ');
  };

  tools.stableClassTokens = tools.stableClassTokens || function (element) {
    return stableClassTokens(element);
  };

  tools.isStableSelectorToken = tools.isStableSelectorToken || function (value) {
    return isStableSelectorToken(value);
  };

  function selectorFromElementId(element) {
    const id = String(element.id || '').trim();
    if (!isStableSelectorToken(id)) return null;
    return '#' + selectorTools.cssIdentifier(id);
  }

  function selectorFromElementClasses(element) {
    const tagName = elementTagName(element);
    const classes = stableClassTokens(element).slice(0, 3);
    if (!tagName || !classes.length) return null;
    return tagName + classes.map(function (className) {
      return '.' + selectorTools.cssIdentifier(className);
    }).join('');
  }

  function selectorSegmentForElement(element) {
    const tagName = elementTagName(element);
    const idSelector = selectorFromElementId(element);
    if (idSelector) return tagName + idSelector;

    let segment = tagName;
    const classes = stableClassTokens(element).slice(0, 2);
    if (classes.length) {
      segment += classes.map(function (className) {
        return '.' + selectorTools.cssIdentifier(className);
      }).join('');
    }
    if (shouldAppendNthOfType(element, segment)) {
      segment += ':nth-of-type(' + nthOfType(element) + ')';
    }
    return segment;
  }

  function shouldAppendNthOfType(element, segment) {
    const parent = element.parentElement;
    if (!parent) return false;
    let matches = 0;
    Array.prototype.forEach.call(parent.children, function (sibling) {
      try {
        if (sibling.matches(segment)) matches += 1;
      } catch (_) {}
    });
    return matches > 1;
  }

  function nthOfType(element) {
    let index = 1;
    let sibling = element.previousElementSibling;
    const tagName = elementTagName(element);
    while (sibling) {
      if (elementTagName(sibling) === tagName) index += 1;
      sibling = sibling.previousElementSibling;
    }
    return index;
  }

  function elementTagName(element) {
    return String(element && element.tagName || '').toLowerCase();
  }

  function stableClassTokens(element) {
    return Array.prototype.slice.call(element && element.classList || [])
      .map(function (value) { return String(value || '').trim(); })
      .filter(isStableSelectorToken)
      .filter(function (value, index, values) {
        return values.indexOf(value) === index;
      });
  }

  function isStableSelectorToken(value) {
    const token = String(value || '').trim();
    if (token.length < 2 || token.length > 80) return false;
    if (!/^[A-Za-z_][A-Za-z0-9_-]*$/.test(token)) return false;
    if (/^[A-Fa-f0-9]{8,}$/.test(token)) return false;
    if (/\d{6,}/.test(token)) return false;
    return !/^(active|current|selected|open|close|show|hide|visible|hidden|loaded)$/i.test(token);
  }

  function isUniqueSelector(selector) {
    const matches = selectorTools.queryAll(selector);
    return matches.length === 1;
  }
})();
