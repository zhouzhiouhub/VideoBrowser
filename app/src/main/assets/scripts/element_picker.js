/*
 * Element picker module used by the common enhancer public API.
 */
(function () {
  const pickerModule = window.VideoBrowserElementPicker || {};
  const domActions = window.VideoBrowserDomActions || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  const nativeBridge = window.VideoBrowserNativeBridge || {};
  window.VideoBrowserElementPicker = pickerModule;

  pickerModule.start = function (state) {
    return startElementPicker(state);
  };

  pickerModule.stop = function (state) {
    stopElementPicker(state);
  };

  function startElementPicker(state) {
    stopElementPicker(state);
    if (!state || !document.documentElement || !document.body) return false;

    const overlay = document.createElement('div');
    overlay.id = '__videobrowser_element_picker_overlay__';
    overlay.style.cssText = [
      'position:fixed',
      'left:0',
      'top:0',
      'width:0',
      'height:0',
      'box-sizing:border-box',
      'border:2px solid #1D6BE3',
      'background:rgba(29,107,227,0.12)',
      'box-shadow:0 0 0 9999px rgba(17,24,39,0.08)',
      'z-index:2147483647',
      'pointer-events:none',
      'display:none'
    ].join(';');
    document.documentElement.appendChild(overlay);

    state.elementPicker = {
      overlay: overlay,
      selectedElement: null,
      waitingForNative: false,
      lastSelectAt: 0,
      listeners: []
    };

    addElementPickerListener(state, 'pointerdown', function (event) { handleElementPickerMove(state, event); });
    addElementPickerListener(state, 'pointermove', function (event) { handleElementPickerMove(state, event); });
    addElementPickerListener(state, 'pointerup', function (event) { handleElementPickerSelection(state, event); });
    addElementPickerListener(state, 'touchstart', function (event) { handleElementPickerMove(state, event); });
    addElementPickerListener(state, 'touchmove', function (event) { handleElementPickerMove(state, event); });
    addElementPickerListener(state, 'touchend', function (event) { handleElementPickerSelection(state, event); });
    addElementPickerListener(state, 'mousedown', function (event) { handleElementPickerMove(state, event); });
    addElementPickerListener(state, 'mousemove', function (event) { handleElementPickerMove(state, event); });
    addElementPickerListener(state, 'mouseup', function (event) { handleElementPickerSelection(state, event); });
    addElementPickerListener(state, 'click', function (event) { handleElementPickerSelection(state, event); });
    return true;
  }

  function addElementPickerListener(state, type, listener) {
    if (!state || !state.elementPicker) return;
    document.addEventListener(type, listener, true);
    state.elementPicker.listeners.push({
      type: type,
      listener: listener
    });
  }

  function handleElementPickerMove(state, event) {
    const picker = state && state.elementPicker;
    if (!picker) return;
    if (picker.waitingForNative) {
      preventElementPickerEvent(event);
      return;
    }
    const element = elementPickerTargetFromEvent(event);
    if (element) highlightPickedElement(state, element);
    preventElementPickerEvent(event);
  }

  function handleElementPickerSelection(state, event) {
    const picker = state && state.elementPicker;
    if (!picker) return;
    if (picker.waitingForNative) {
      preventElementPickerEvent(event);
      return;
    }

    preventElementPickerEvent(event);
    const now = Date.now();
    if (now - Number(picker.lastSelectAt || 0) < 350) return;
    picker.lastSelectAt = now;

    const element = elementPickerTargetFromEvent(event) || picker.selectedElement;
    if (!element) return;

    const selector = buildElementPickerSelector(element);
    if (!selector || !selectorTools.isSafeSelector(selector)) return;

    picker.waitingForNative = true;
    highlightPickedElement(state, element);

    if (
      typeof nativeBridge.requestElementBlock === 'function' &&
      nativeBridge.requestElementBlock(selector, describePickedElement(element))
    ) {
      return;
    }
    stopElementPicker(state);
  }

  function preventElementPickerEvent(event) {
    if (!event) return;
    try { event.preventDefault(); } catch (_) {}
    try { event.stopPropagation(); } catch (_) {}
    try { event.stopImmediatePropagation(); } catch (_) {}
  }

  function elementPickerTargetFromEvent(event) {
    const point = pointFromPickerEvent(event);
    if (!point) return null;
    let element = null;
    try {
      element = document.elementFromPoint(point.x, point.y);
    } catch (_) {
      element = event && event.target;
    }
    return normalizePickerTarget(element);
  }

  function pointFromPickerEvent(event) {
    if (!event) return null;
    const touch = event.changedTouches && event.changedTouches.length
      ? event.changedTouches[0]
      : event.touches && event.touches.length
        ? event.touches[0]
        : null;
    const source = touch || event;
    const x = Number(source.clientX);
    const y = Number(source.clientY);
    if (!Number.isFinite(x) || !Number.isFinite(y)) return null;
    return { x: x, y: y };
  }

  function normalizePickerTarget(element) {
    let current = element && element.nodeType === 1 ? element : null;
    for (let depth = 0; current && depth < 6; depth += 1, current = current.parentElement) {
      if (isElementPickerInternal(current)) return null;
      if (current === document.body || current === document.documentElement) return null;
      if (!domActions.isProtectedAppContainer(current)) return current;
    }
    return null;
  }

  function isElementPickerInternal(element) {
    return String(element && element.id || '') === '__videobrowser_element_picker_overlay__';
  }

  function highlightPickedElement(state, element) {
    const picker = state && state.elementPicker;
    if (!picker || !picker.overlay || !element || !element.getBoundingClientRect) return;
    const rect = element.getBoundingClientRect();
    picker.selectedElement = element;
    picker.overlay.style.display = rect.width > 0 && rect.height > 0 ? 'block' : 'none';
    picker.overlay.style.left = Math.max(0, rect.left) + 'px';
    picker.overlay.style.top = Math.max(0, rect.top) + 'px';
    picker.overlay.style.width = Math.max(0, rect.width) + 'px';
    picker.overlay.style.height = Math.max(0, rect.height) + 'px';
  }

  function buildElementPickerSelector(element) {
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
  }

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
    return Array.prototype.slice.call(element.classList || [])
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

  function describePickedElement(element) {
    const parts = [elementTagName(element)];
    const id = String(element.id || '').trim();
    if (id) parts.push('#' + id.slice(0, 48));
    const classes = stableClassTokens(element).slice(0, 3);
    if (classes.length) parts.push('.' + classes.join('.'));
    const text = selectorTools.normalizeText(element.innerText || element.textContent);
    if (text) parts.push(text.slice(0, 80));
    return parts.join(' ');
  }

  function detachElementPickerListeners(picker) {
    if (!picker || !Array.isArray(picker.listeners)) return;
    picker.listeners.forEach(function (entry) {
      document.removeEventListener(entry.type, entry.listener, true);
    });
    picker.listeners.length = 0;
  }

  function stopElementPicker(state) {
    const picker = state && state.elementPicker;
    if (!picker) return;
    detachElementPickerListeners(picker);
    if (picker.overlay && picker.overlay.parentNode) {
      picker.overlay.parentNode.removeChild(picker.overlay);
    }
    state.elementPicker = null;
  }
})();
