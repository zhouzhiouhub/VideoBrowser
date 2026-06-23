/*
 * Shared generic ad overlay signal helpers.
 */
(function () {
  const signals = window.VideoBrowserGenericAdOverlaySignals || {};
  const domTools = window.VideoBrowserDomTools || {};
  const selectorTools = window.VideoBrowserSelectorTools || {};
  const geometry = window.VideoBrowserGeometry || {};
  window.VideoBrowserGenericAdOverlaySignals = signals;

  signals.promoTextLike = signals.promoTextLike || function (value) {
    return /官方推荐|APP亲测无毒|狼友多下载|防丢失|招嫖约炮|情趣用品|注册即送|超高爆率|连麦|操控|同城约炮|超级巨奖|财富放水|PG娱乐|PG电子|PG游戏|私人订制|抢庄牛牛|附近上门|上门服务|学生空降|学生兼职|外围兼职|1元拉爆|新葡京|开户送钱|送\d+元|提款秒到|充提秒到/i
      .test(String(value || ''));
  };

  signals.adTextLike = signals.adTextLike || function (value) {
    return /广告|廣告|推广|推廣|赞助|贊助|立即下载|立即安装|打开APP|下载APP|福利|红包|领取|客服|加微信|棋牌|彩票|博彩|adult|casino|sponsor|promotion|download|install|openapp/i
      .test(String(value || ''));
  };

  signals.adNameLike = signals.adNameLike || function (value) {
    return /(^|[-_\s])(ad|ads|adv|advert|sponsor|promo|promotion|download|openapp|banner)([-_\s]|$)/i
      .test(String(value || ''));
  };

  signals.layerNameLike = signals.layerNameLike || function (value) {
    return /modal|popup|pop|mask|overlay|dialog|layer|float/i.test(String(value || ''));
  };

  signals.nameSignalLike = signals.nameSignalLike || function (value) {
    return /ad|ads|adv|advert|sponsor|promo|promotion|download|openapp|banner|float|popup|modal|layer|icon-close/i
      .test(String(value || ''));
  };

  signals.zeroLayoutNameLike = signals.zeroLayoutNameLike || function (value) {
    return /modal|popup|pop|mask|overlay|dialog|layer|float|ad|ads|adv|advert|promo|promotion|download|install|openapp|banner/i
      .test(String(value || ''));
  };

  signals.downloadActionLike = signals.downloadActionLike || function (value) {
    return /下载|安裝|安装|APP|app|桌面|download|install|openapp/i.test(String(value || ''));
  };

  signals.mediaSourceLooksLikeAd = signals.mediaSourceLooksLikeAd || function (element) {
    if (!element || !element.querySelectorAll) return false;
    return Array.prototype.some.call(
      domTools.queryAllWithin(element, 'img,source'),
      function (media) {
        return signals.mediaSourceLikeAd(signals.mediaSourceValue(media));
      }
    );
  };

  signals.mediaSourceLikeAd = signals.mediaSourceLikeAd || function (value) {
    const source = String(value || '');
    return /^data:image\//i.test(source) ||
      /ad|ads|adv|advert|banner|promo|promotion|taojianghu|sf-express|alicdn|gif/i.test(source);
  };

  signals.mediaSourceValue = signals.mediaSourceValue || function (media) {
    return String(
      media &&
      (
        media.currentSrc ||
        media.src ||
        media.getAttribute('src') ||
        media.getAttribute('srcset') ||
        ''
      )
    );
  };

  signals.hasCloseLikeDescendant = signals.hasCloseLikeDescendant || function (element) {
    if (!element) return false;
    if (signals.isCloseLikeControl(element)) return true;
    if (!element.querySelectorAll) return false;
    return Array.prototype.some.call(
      domTools.queryAllWithin(
        element,
        'button,a,i,[role="button"],[aria-label],[title],' +
        '[class*="close"],[class*="Close"],[class*="icon-close"]'
      ),
      signals.isCloseLikeControl
    );
  };

  signals.isCloseLikeControl = signals.isCloseLikeControl || function (element) {
    if (!element) return false;
    const rect = geometry.safeRect(element);
    const text = selectorTools.normalizeText(
      element.innerText ||
      element.textContent ||
      element.getAttribute('aria-label') ||
      element.getAttribute('title') ||
      element.getAttribute('alt')
    );
    const descriptor = domTools.elementDescriptor(element);
    const compactText = text.replace(/\s+/g, '');
    if (/^(×|x|X|✕|✖|关闭|關閉|取消|跳过|跳過|稍后|稍後|不再提示|close|skip|dismiss)$/i.test(compactText)) {
      return true;
    }
    return Boolean(rect) &&
      rect.width <= 72 &&
      rect.height <= 72 &&
      /close|dismiss|cancel|skip|关闭|關閉|跳过|跳過/i.test(descriptor + ' ' + text);
  };

  signals.clearScrollLocks = signals.clearScrollLocks || function () {
    unlockScrollContainer(document.documentElement);
    unlockScrollContainer(document.body);
  };

  signals.isScrollLockClass = signals.isScrollLockClass || function (className) {
    return /(^|[-_])(overflow-hidden|no-scroll|noscroll|scroll-lock|lock-scroll)([-_]|$)/i
      .test(String(className || '')) ||
      /^adm-overflow-hidden$/i.test(String(className || ''));
  };

  function unlockScrollContainer(element) {
    if (!element) return;
    [
      'overflow',
      'overflow-x',
      'overflow-y',
      'position',
      'height',
      'touch-action',
      'overscroll-behavior'
    ].forEach(function (property) {
      element.style.removeProperty(property);
    });

    if (!element.classList) return;
    Array.prototype.slice.call(element.classList).forEach(function (className) {
      if (signals.isScrollLockClass(className)) {
        element.classList.remove(className);
      }
    });
  }
})();
