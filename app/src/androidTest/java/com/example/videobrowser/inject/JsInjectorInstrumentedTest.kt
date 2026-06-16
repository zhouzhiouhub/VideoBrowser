package com.example.videobrowser.inject

/**
 * 测试阅读提示：
 * 这个测试文件验证“Js Injector Instrumented Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.videobrowser.site.SiteAdapter
import com.example.videobrowser.site.SiteAdapterRegistry
import com.example.videobrowser.site.SiteProfile
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsInjectorInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var webView: WebView

    /**
     * 测试函数 `setUp`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `set Up` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Before
    fun setUp() {
        instrumentation.runOnMainSync {
            webView = WebView(instrumentation.targetContext).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
                measure(widthSpec, heightSpec)
                layout(0, 0, 1080, 1920)
            }
        }
    }

    /**
     * 测试函数 `tearDown`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `tear Down` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @After
    fun tearDown() {
        instrumentation.runOnMainSync {
            webView.destroy()
        }
    }

    /**
     * 测试函数 `inject_appliesCleanupSkipAndVideoSpeedFeatures`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject applies Cleanup Skip And Video Speed Features` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_appliesCleanupSkipAndVideoSpeedFeatures() {
        loadHtml(TEST_HTML)
        injectPageFeatures()

        val result = evaluateJsonArray(
            """
                (function () {
                  var ad = document.getElementById('ad');
                  var video = document.getElementById('video');
                  window.VideoBrowserEnhancer.setPlaybackSpeed(1.5);
                  video.dispatchEvent(new Event('webkitbeginfullscreen'));
                  return [
                    window.__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__ === true,
                    window.getComputedStyle(ad).display,
                    window.__skipClicked === true,
                    video.playbackRate
                  ];
                })();
            """.trimIndent()
        )

        assertTrue(result.getBoolean(0))
        assertEquals("none", result.getString(1))
        assertTrue(result.getBoolean(2))
        assertEquals(1.5, result.getDouble(3), 0.01)
    }

    /**
     * 测试函数 `scriptletWindowOpenAndFetchKeywordsWorkWhenCleanupDisabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `scriptlet Window Open And Fetch Keywords Work When Cleanup Disabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun scriptletWindowOpenAndFetchKeywordsWorkWhenCleanupDisabled() {
        loadHtml(SCRIPTLET_HOOK_HTML)
        injectPageFeatures(
            config = PageFeatureConfig(
                cleanupEnabled = false,
                videoEnabled = false,
                scriptletWindowOpenBlockedKeywords = listOf("/popup-ad/"),
                scriptletFetchBlockedKeywords = listOf("/fetch-ad/")
            )
        )

        val openResult = evaluateJsonArray(
            """
                (function () {
                  window.__fetchBlocked = false;
                  window.__fetchPassed = false;
                  var opened = window.open('/popup-ad/landing');
                  window.fetch('/fetch-ad/pixel')
                    .then(function () { window.__fetchPassed = true; })
                    .catch(function () { window.__fetchBlocked = true; });
                  return [
                    opened === null,
                    window.__openCalls.length
                  ];
                })();
            """.trimIndent()
        )

        Thread.sleep(100)

        val fetchResult = evaluateJsonArray(
            """
                (function () {
                  return [
                    window.__fetchBlocked === true,
                    window.__fetchPassed === true,
                    window.__fetchCalls.length
                  ];
                })();
            """.trimIndent()
        )

        assertTrue(openResult.toString(), openResult.getBoolean(0))
        assertEquals(0, openResult.getInt(1))
        assertTrue(fetchResult.toString(), fetchResult.getBoolean(0))
        assertFalse(fetchResult.toString(), fetchResult.getBoolean(1))
        assertEquals(0, fetchResult.getInt(2))
    }

    /**
     * 测试函数 `scriptletSkipButtonsCanRunWhenVideoEnhancementDisabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `scriptlet Skip Buttons Can Run When Video Enhancement Disabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun scriptletSkipButtonsCanRunWhenVideoEnhancementDisabled() {
        loadHtml(TEST_HTML)
        injectPageFeatures(
            config = PageFeatureConfig(
                cleanupEnabled = false,
                videoEnabled = false,
                scriptletSkipButtonsEnabled = true
            )
        )

        val result = evaluateJsonArray("[(window.__skipClicked === true)]")

        assertTrue(result.toString(), result.getBoolean(0))
    }

    /**
     * 测试函数 `scriptletVideoControlsCanRunWhenVideoEnhancementDisabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `scriptlet Video Controls Can Run When Video Enhancement Disabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun scriptletVideoControlsCanRunWhenVideoEnhancementDisabled() {
        loadHtml(TEST_HTML)
        injectPageFeatures(
            config = PageFeatureConfig(
                cleanupEnabled = false,
                videoEnabled = false,
                scriptletVideoControlsEnabled = true
            )
        )

        val result = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  return [
                    video.controls === true,
                    video.hasAttribute('controls')
                  ];
                })();
            """.trimIndent()
        )

        assertTrue(result.toString(), result.getBoolean(0))
        assertTrue(result.toString(), result.getBoolean(1))
    }

    /**
     * 测试函数 `videoControlsStayDisabledWithoutVideoEnhancementOrScriptletHook`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `video Controls Stay Disabled Without Video Enhancement Or Scriptlet Hook` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun videoControlsStayDisabledWithoutVideoEnhancementOrScriptletHook() {
        loadHtml(TEST_HTML)
        injectPageFeatures(
            config = PageFeatureConfig(
                cleanupEnabled = false,
                videoEnabled = false
            )
        )

        val result = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  return [
                    video.controls === true,
                    video.hasAttribute('controls')
                  ];
                })();
            """.trimIndent()
        )

        assertFalse(result.toString(), result.getBoolean(0))
        assertFalse(result.toString(), result.getBoolean(1))
    }

    /**
     * 测试函数 `setPlaybackSpeed_appliesToActiveVideoWhenAndroidCustomViewFullscreenHasNoDocumentFullscreen`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `set Playback Speed applies To Active Video When Android Custom View Fullscreen Has No Document Fullscreen` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun setPlaybackSpeed_appliesToActiveVideoWhenAndroidCustomViewFullscreenHasNoDocumentFullscreen() {
        loadHtml(TEST_HTML)
        injectPageFeatures()

        val result = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  window.VideoBrowserEnhancer.setPlaybackSpeed(1.5);
                  return [
                    Boolean(document.fullscreenElement || document.webkitFullscreenElement),
                    video.playbackRate
                  ];
                })();
            """.trimIndent()
        )

        assertFalse(result.getBoolean(0))
        assertEquals(1.5, result.getDouble(1), 0.01)
    }

    /**
     * 测试函数 `setPlaybackSpeed_survivesDocumentFullscreenSyncWhileAndroidCustomViewFullscreenIsActive`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `set Playback Speed survives Document Fullscreen Sync While Android Custom View Fullscreen Is Active` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun setPlaybackSpeed_survivesDocumentFullscreenSyncWhileAndroidCustomViewFullscreenIsActive() {
        loadHtml(TEST_HTML)
        injectPageFeatures()

        val setup = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  window.VideoBrowserEnhancer.setPlaybackSpeed(1.5);
                  document.dispatchEvent(new Event('fullscreenchange'));
                  video.playbackRate = 1;
                  video.dispatchEvent(new Event('ratechange'));
                  return [
                    Boolean(document.fullscreenElement || document.webkitFullscreenElement),
                    window.__videobrowserState.fullscreenPlaybackSpeed
                  ];
                })();
            """.trimIndent()
        )

        assertFalse(setup.getBoolean(0))
        assertEquals(1.5, setup.getDouble(1), 0.01)

        Thread.sleep(100)

        val result = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  return [
                    window.__videobrowserState.fullscreenPlaybackSpeed,
                    video.playbackRate
                  ];
                })();
            """.trimIndent()
        )

        assertEquals(1.5, result.getDouble(0), 0.01)
        assertEquals(1.5, result.getDouble(1), 0.01)
    }

    /**
     * 测试函数 `setPlaybackSpeed_survivesFeatureReapplyWhileAndroidCustomViewFullscreenIsActive`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `set Playback Speed survives Feature Reapply While Android Custom View Fullscreen Is Active` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun setPlaybackSpeed_survivesFeatureReapplyWhileAndroidCustomViewFullscreenIsActive() {
        loadHtml(TEST_HTML)
        injectPageFeatures()

        val selected = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  window.VideoBrowserEnhancer.setPlaybackSpeed(1.5);
                  return [
                    video.playbackRate,
                    window.__videobrowserState.fullscreenPlaybackSpeed,
                    window.__videobrowserState.nativeFullscreenVideo === video
                  ];
                })();
            """.trimIndent()
        )

        assertEquals(1.5, selected.getDouble(0), 0.01)
        assertEquals(1.5, selected.getDouble(1), 0.01)
        assertTrue(selected.toString(), selected.getBoolean(2))

        injectPageFeatures()

        val reapplied = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  return [
                    video.playbackRate,
                    window.__videobrowserState.fullscreenPlaybackSpeed,
                    window.__videobrowserState.nativeFullscreenVideo === video
                  ];
                })();
            """.trimIndent()
        )

        assertEquals(1.5, reapplied.getDouble(0), 0.01)
        assertEquals(1.5, reapplied.getDouble(1), 0.01)
        assertTrue(reapplied.toString(), reapplied.getBoolean(2))
    }

    /**
     * 测试函数 `inject_loadsOnlyMatchingSiteAdapterInWebView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject loads Only Matching Site Adapter In Web View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_loadsOnlyMatchingSiteAdapterInWebView() {
        loadHtml(TEST_HTML)
        injectPageFeatures(
            pageUrl = MATCHING_TEST_PAGE,
            siteAdapterRegistry = SiteAdapterRegistry(
                listOf(
                    testSiteAdapter(
                        id = "youtube",
                        scriptAssetPath = "scripts/youtube.js",
                        matchesPage = true
                    ),
                    testSiteAdapter(
                        id = "bilibili",
                        scriptAssetPath = "scripts/bilibili.js",
                        matchesPage = false
                    )
                )
            )
        )

        val result = evaluateJsonArray(
            """
                (function () {
                  return [
                    Boolean(window.VideoBrowserSiteAdapters && window.VideoBrowserSiteAdapters.youtube),
                    Boolean(window.VideoBrowserSiteAdapters && window.VideoBrowserSiteAdapters.bilibili),
                    window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__ &&
                      window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__['scripts/youtube.js'] === true,
                    window.VideoBrowserSiteAdapters.youtube.lastConfig.cleanupEnabled === true
                  ];
                })();
            """.trimIndent()
        )

        assertTrue(result.getBoolean(0))
        assertFalse(result.getBoolean(1))
        assertTrue(result.getBoolean(2))
        assertTrue(result.getBoolean(3))
    }

    /**
     * 测试函数 `inject_removesGenericImageInterstitialOverlay`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject removes Generic Image Interstitial Overlay` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_removesGenericImageInterstitialOverlay() {
        loadHtml(IMAGE_INTERSTITIAL_HTML)
        injectPageFeatures()

        val result = evaluateJsonArray(
            """
                (function () {
                  var overlay = document.getElementById('promo-overlay');
                  var root = document.getElementById('root');
                  var bodyStyle = window.getComputedStyle(document.body);
                  return [
                    window.getComputedStyle(overlay).display,
                    overlay.getAttribute('data-videobrowser-dismissed'),
                    window.getComputedStyle(root).display,
                    document.body.classList.contains('adm-overflow-hidden'),
                    document.body.classList.contains('no-scroll'),
                    bodyStyle.overflowY,
                    bodyStyle.position,
                    document.body.style.overflow,
                    document.body.style.position
                  ];
                })();
            """.trimIndent()
        )

        assertEquals("none", result.getString(0))
        assertEquals("generic-ad-overlay", result.getString(1))
        assertFalse("none" == result.getString(2))
        assertFalse(result.getBoolean(3))
        assertFalse(result.getBoolean(4))
        assertFalse("hidden" == result.getString(5))
        assertFalse("fixed" == result.getString(6))
        assertEquals("", result.getString(7))
        assertEquals("", result.getString(8))
    }

    /**
     * 测试函数 `inject_removesGenericFloatingImageAdsAndDownloadBar`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject removes Generic Floating Image Ads And Download Bar` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_removesGenericFloatingImageAdsAndDownloadBar() {
        loadHtml(FLOATING_AD_HTML)
        injectPageFeatures()

        val result = evaluateJsonArray(
            """
                (function () {
                  var edgeAd = document.getElementById('edge-ad');
                  var appBar = document.getElementById('app-bar');
                  var aiBubble = document.getElementById('ai-bubble');
                  var inlineBanner = document.getElementById('inline-banner');
                  var promoGrid = document.getElementById('promo-grid');
                  var root = document.getElementById('root');
                  return [
                    window.getComputedStyle(edgeAd).display,
                    edgeAd.getAttribute('data-videobrowser-dismissed'),
                    window.getComputedStyle(appBar).display,
                    appBar.getAttribute('data-videobrowser-dismissed'),
                    window.getComputedStyle(aiBubble).display,
                    aiBubble.getAttribute('data-videobrowser-dismissed'),
                    window.getComputedStyle(inlineBanner).display,
                    inlineBanner.getAttribute('data-videobrowser-dismissed'),
                    window.getComputedStyle(promoGrid).display,
                    promoGrid.getAttribute('data-videobrowser-dismissed'),
                    window.getComputedStyle(root).display
                  ];
                })();
            """.trimIndent()
        )

        assertEquals("none", result.getString(0))
        assertEquals("generic-ad-overlay", result.getString(1))
        assertEquals("none", result.getString(2))
        assertEquals("generic-ad-overlay", result.getString(3))
        assertEquals("none", result.getString(4))
        assertEquals("generic-ad-overlay", result.getString(5))
        assertEquals("none", result.getString(6))
        assertEquals("generic-ad-overlay", result.getString(7))
        assertEquals("none", result.getString(8))
        assertEquals("generic-ad-overlay", result.getString(9))
        assertFalse("none" == result.getString(10))
    }

    /**
     * 测试函数 `inject_removesGeneratedSlicedImageAdsWithoutDomainOrClassRules`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject removes Generated Sliced Image Ads Without Domain Or Class Rules` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_removesGeneratedSlicedImageAdsWithoutDomainOrClassRules() {
        loadHtml(GENERATED_SLICED_AD_HTML)
        injectPageFeatures()

        val result = evaluateJsonArray(
            """
                (function () {
                  var topTiles = document.querySelectorAll('.randomTopTile');
                  var bottomTiles = document.querySelectorAll('xrandad');
                  var clickGrid = document.querySelectorAll('.transparent-click-grid');
                  var normalNav = document.getElementById('normal-bottom-nav');
                  var normalHero = document.getElementById('normal-hero');
                  return [
                    window.__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__ === true,
                    Boolean(window.VideoBrowserEnhancer),
                    topTiles.length,
                    bottomTiles.length,
                    clickGrid.length,
                    Array.prototype.every.call(topTiles, function (tile) {
                      return window.getComputedStyle(tile).display === 'none' &&
                        tile.getAttribute('data-videobrowser-dismissed') === 'generated-sliced-ad';
                    }),
                    Array.prototype.every.call(bottomTiles, function (tile) {
                      return window.getComputedStyle(tile).display === 'none' &&
                        tile.getAttribute('data-videobrowser-dismissed') === 'generated-sliced-ad';
                    }),
                    Array.prototype.every.call(clickGrid, function (tile) {
                      return window.getComputedStyle(tile).display === 'none' &&
                        tile.getAttribute('data-videobrowser-dismissed') === 'generated-click-grid';
                    }),
                    window.getComputedStyle(normalNav).display,
                    normalNav.getAttribute('data-videobrowser-dismissed'),
                    window.getComputedStyle(normalHero).display,
                    normalHero.getAttribute('data-videobrowser-dismissed')
                  ];
                })();
            """.trimIndent()
        )

        assertTrue(result.toString(), result.getBoolean(0))
        assertTrue(result.toString(), result.getBoolean(1))
        assertEquals(40, result.getInt(2))
        assertEquals(40, result.getInt(3))
        assertEquals(80, result.getInt(4))
        assertTrue(result.toString(), result.getBoolean(5))
        assertTrue(result.toString(), result.getBoolean(6))
        assertTrue(result.toString(), result.getBoolean(7))
        assertFalse("none" == result.getString(8))
        assertTrue(result.isNull(9))
        assertFalse("none" == result.getString(10))
        assertTrue(result.isNull(11))
    }

    /**
     * 测试函数 `inject_removesRegeneratedSlicedImageAdsBeforeGenericCleanupInterval`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject removes Regenerated Sliced Image Ads Before Generic Cleanup Interval` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_removesRegeneratedSlicedImageAdsBeforeGenericCleanupInterval() {
        loadHtml(GENERATED_SLICED_AD_HTML)
        injectPageFeatures()

        Thread.sleep(100)

        val inserted = evaluateJsonArray(
            """
                (function () {
                  for (var row = 0; row < 4; row += 1) {
                    for (var col = 0; col < 10; col += 1) {
                      var tile = document.createElement('div');
                      tile.className = 'lateGeneratedTile';
                      tile.style.cssText = [
                        'position:fixed',
                        'top:' + (row * 32) + 'px',
                        'left:' + (col * 40) + 'px',
                        'z-index:2147483646',
                        'display:block',
                        'width:40px',
                        'height:32px',
                        'background-image:url("data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==")',
                        'background-size:400px 128px'
                      ].join(';');
                      document.body.appendChild(tile);
                    }
                  }
                  return [document.querySelectorAll('.lateGeneratedTile').length];
                })();
            """.trimIndent()
        )
        assertEquals(40, inserted.getInt(0))

        Thread.sleep(1200)

        val result = evaluateJsonArray(
            """
                (function () {
                  var lateTiles = document.querySelectorAll('.lateGeneratedTile');
                  return [
                    lateTiles.length,
                    Array.prototype.every.call(lateTiles, function (tile) {
                      return window.getComputedStyle(tile).display === 'none' &&
                        tile.getAttribute('data-videobrowser-dismissed') === 'generated-sliced-ad';
                    }),
                    Date.now() - Number((window.__videobrowserState && window.__videobrowserState.lastCleanupAt) || 0)
                  ];
                })();
            """.trimIndent()
        )

        assertEquals(40, result.getInt(0))
        assertTrue(result.toString(), result.getBoolean(1))
        assertTrue(
            "Regenerated ad cleanup should not wait for the generic cleanup interval: $result",
            result.getDouble(2) < 3000.0
        )
    }

    /**
     * 测试函数 `loadHtml`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Html` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param html 参数类型为 `String`，表示函数执行 `html` 相关逻辑时需要读取或处理的输入。
     * @param baseUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun loadHtml(html: String, baseUrl: String? = null) {
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            webView.webViewClient = object : WebViewClient() {
                /**
                 * 测试函数 `onPageFinished`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `on Page Finished` 这条行为是否成立。
                 *
                 * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
                 * @param view 参数类型为 `WebView?`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
                 * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
                 */
                override fun onPageFinished(view: WebView?, url: String?) {
                    latch.countDown()
                }
            }
            webView.loadDataWithBaseURL(
                baseUrl,
                html,
                "text/html",
                "utf-8",
                null
            )
        }
        assertTrue("Timed out waiting for test page to load.", latch.await(5, TimeUnit.SECONDS))
    }

    /**
     * 测试函数 `injectPageFeatures`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject Page Features` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param config 参数类型为 `PageFeatureConfig`，表示本次操作的配置集合，函数会按这些开关和参数调整行为。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param siteAdapterRegistry 参数类型为 `SiteAdapterRegistry`，表示函数执行 `siteAdapterRegistry` 相关逻辑时需要读取或处理的输入。
     */
    private fun injectPageFeatures(
        config: PageFeatureConfig = PageFeatureConfig(
            cleanupEnabled = true,
            videoEnabled = true
        ),
        pageUrl: String? = null,
        siteAdapterRegistry: SiteAdapterRegistry = SiteAdapterRegistry.default()
    ) {
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            JsInjector(
                scriptLoader = ScriptLoader(instrumentation.targetContext.assets),
                siteAdapterRegistry = siteAdapterRegistry,
                evaluateJavascript = { script ->
                    webView.evaluateJavascript(script) {
                        latch.countDown()
                    }
                }
            ).inject(
                config,
                pageUrl = pageUrl
            )
        }
        assertTrue("Timed out waiting for script injection.", latch.await(5, TimeUnit.SECONDS))
    }

    /**
     * 测试函数 `evaluateJsonArray`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `evaluate Json Array` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param script 参数类型为 `String`，表示函数执行 `script` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun evaluateJsonArray(script: String): JSONArray {
        val latch = CountDownLatch(1)
        var rawResult: String? = null
        instrumentation.runOnMainSync {
            webView.evaluateJavascript(script) { result ->
                rawResult = result
                latch.countDown()
            }
        }
        assertTrue("Timed out waiting for JavaScript result.", latch.await(5, TimeUnit.SECONDS))
        return JSONArray(requireNotNull(rawResult))
    }

    /**
     * 测试函数 `testSiteAdapter`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `test Site Adapter` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param scriptAssetPath 参数类型为 `String`，表示函数执行 `scriptAssetPath` 相关逻辑时需要读取或处理的输入。
     * @param matchesPage 参数类型为 `Boolean`，表示函数执行 `matchesPage` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun testSiteAdapter(
        id: String,
        scriptAssetPath: String,
        matchesPage: Boolean
    ): SiteAdapter {
        return object : SiteAdapter {
            override val profile = SiteProfile(
                id = id,
                displayName = id,
                domains = setOf("test.invalid"),
                scriptAssetPaths = listOf(scriptAssetPath)
            )

            /**
             * 测试函数 `matches`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `matches` 这条行为是否成立。
             *
             * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
             * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
             * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
             */
            override fun matches(url: String): Boolean {
                return matchesPage && url == MATCHING_TEST_PAGE
            }
        }
    }

    private companion object {
        private const val MATCHING_TEST_PAGE = "matching-test-page"

        private const val TEST_HTML = """
            <!doctype html>
            <html>
              <body>
                <div id="ad" class="ad-banner">ad</div>
                <button id="skip" class="skip-button" onclick="window.__skipClicked=true">Skip ad</button>
                <video id="video"></video>
                <script>window.__skipClicked=false;</script>
              </body>
            </html>
        """

        private const val SCRIPTLET_HOOK_HTML = """
            <!doctype html>
            <html>
              <body>
                <script>
                  window.__openCalls = [];
                  window.__fetchCalls = [];
                  window.open = function (url) {
                    window.__openCalls.push(url);
                    return { url: url };
                  };
                  window.fetch = function (url) {
                    window.__fetchCalls.push(url);
                    return Promise.resolve({ ok: true });
                  };
                </script>
              </body>
            </html>
        """

        private const val IMAGE_INTERSTITIAL_HTML = """
            <!doctype html>
            <html>
              <head>
                <style>
                  .adm-overflow-hidden,
                  .no-scroll {
                    overflow: hidden !important;
                    position: fixed !important;
                    height: 100% !important;
                    touch-action: none !important;
                  }
                </style>
              </head>
              <body class="adm-overflow-hidden no-scroll" style="overflow:hidden;position:fixed;height:100%;touch-action:none">
                <div id="root">page content</div>
                <div
                  id="promo-overlay"
                  class="adm-mask adm-center-popup"
                  style="position:fixed;left:0;top:0;width:100vw;height:100vh;z-index:9999"
                >
                  <div style="position:absolute;left:10vw;top:20vh;width:80vw;height:45vh">
                    <button aria-label="关闭">×</button>
                    <img alt="promotion" style="width:100%;height:100%" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" />
                  </div>
                </div>
              </body>
            </html>
        """

        private const val FLOATING_AD_HTML = """
            <!doctype html>
            <html>
              <body>
                <div id="root">
                  <button>关闭菜单</button>
                  <p>page content</p>
                </div>
                <div
                  id="edge-ad"
                  class="floating-slot"
                  style="position:fixed;left:10px;bottom:180px;width:63px;height:63px;z-index:999"
                >
                  <i class="icon-close iconfont" style="position:absolute;right:-8px;top:-8px;width:16px;height:16px"></i>
                  <img alt="promotion" style="width:63px;height:63px" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" />
                </div>
                <div
                  id="app-bar"
                  class="desktop-install"
                  style="position:fixed;left:86px;bottom:60px;width:240px;height:40px;z-index:999;display:flex"
                >
                  <span><i class="icon-Union iconfont"></i> 添加到桌面</span>
                  <span><i class="icon-shoujiapp iconfont"></i> 下载APP</span>
                  <span>关闭</span>
                </div>
                <div
                  id="ai-bubble"
                  class="floating-ai"
                  style="position:fixed;right:10px;bottom:150px;width:80px;height:74px;z-index:10"
                >
                  <img alt="ai promotion" style="width:80px;height:74px" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" />
                </div>
                <div id="inline-banner" style="width:100vw;height:120px">
                  <img alt="banner" style="width:100%;height:120px" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==" />
                </div>
                <div id="promo-grid" style="width:100vw;height:154px">
                  注册即送 超高爆率 同城约炮 PG电子 开户送钱 提款秒到
                </div>
              </body>
            </html>
        """

        private const val GENERATED_SLICED_AD_HTML = """
            <!doctype html>
            <html>
              <head>
                <style>
                  body { margin: 0; min-height: 1200px; }
                  #normal-hero {
                    width: 100vw;
                    height: 160px;
                    background-image: url("data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==");
                    background-size: cover;
                  }
                  #normal-bottom-nav {
                    position: fixed;
                    left: 0;
                    bottom: 0;
                    width: 100vw;
                    height: 56px;
                    z-index: 999999994;
                    background: #111;
                    color: #fff;
                  }
                  .randomTopTile,
                  xrandad {
                    position: fixed;
                    z-index: 2147483646;
                    display: block;
                    width: 40px;
                    height: 32px;
                    background-image: url("data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==");
                    background-size: 400px 128px !important;
                  }
                </style>
              </head>
              <body>
                <div id="normal-hero"></div>
                <p>page content</p>
                <div id="normal-bottom-nav">Home Video Profile</div>
                <script>
                  (function () {
                    for (var row = 0; row < 4; row += 1) {
                      for (var col = 0; col < 10; col += 1) {
                        var topTile = document.createElement('div');
                        topTile.className = 'randomTopTile';
                        topTile.style.top = (row * 32) + 'px';
                        topTile.style.left = (col * 40) + 'px';
                        topTile.style.backgroundPosition = '-' + (col * 40) + 'px -' + (row * 32) + 'px';
                        document.body.appendChild(topTile);

                        var bottomTile = document.createElement('xrandad');
                        bottomTile.style.bottom = (row * 32 + 56) + 'px';
                        bottomTile.style.left = (col * 40) + 'px';
                        bottomTile.style.backgroundPosition = '-' + (col * 40) + 'px -' + (row * 32) + 'px';
                        document.body.appendChild(bottomTile);
                      }
                    }
                    for (var hitRow = 0; hitRow < 8; hitRow += 1) {
                      for (var hitCol = 0; hitCol < 10; hitCol += 1) {
                        var hit = document.createElement('div');
                        hit.className = 'transparent-click-grid';
                        hit.style.cssText = [
                          'position:fixed',
                          'top:' + (hitRow * 32) + 'px',
                          'left:' + (hitCol * 40) + 'px',
                          'z-index:100',
                          'display:block',
                          'width:38px',
                          'height:30px',
                          'background:#000',
                          'opacity:0.01'
                        ].join(';');
                        document.body.appendChild(hit);
                      }
                    }
                  })();
                </script>
              </body>
            </html>
        """
    }
}
