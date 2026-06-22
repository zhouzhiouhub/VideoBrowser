/*
 * Shared callback invocation helpers.
 */
(function () {
  const tools = window.VideoBrowserCallbackTools || {};
  window.VideoBrowserCallbackTools = tools;

  tools.call = tools.call || function (callbacks, name, value) {
    if (callbacks && typeof callbacks[name] === 'function') {
      return callbacks[name](value);
    }
    return undefined;
  };
})();
