/*
 * Element picker module used by the common enhancer public API.
 */
(function () {
  const pickerModule = window.VideoBrowserElementPicker || {};
  const domActions = window.VideoBrowserDomActions || {};
  const pickerSelectorTools = window.VideoBrowserElementPickerSelectorTools || {};
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

    const selector = typeof pickerSelectorTools.buildSelector === 'function'
      ? pickerSelectorTools.buildSelector(element)
      : null;
    if (!selector) return;

    picker.waitingForNative = true;
    highlightPickedElement(state, element);
    const description = typeof pickerSelectorTools.describeElement === 'function'
      ? pickerSelectorTools.describeElement(element)
      : '';

    if (
      typeof nativeBridge.requestElementBlock === 'function' &&
      nativeBridge.requestElementBlock(selector, description)
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
