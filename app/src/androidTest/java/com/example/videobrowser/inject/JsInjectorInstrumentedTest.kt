package com.example.videobrowser.inject

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

    @Before
    fun setUp() {
        instrumentation.runOnMainSync {
            webView = WebView(instrumentation.targetContext).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        }
    }

    @After
    fun tearDown() {
        instrumentation.runOnMainSync {
            webView.destroy()
        }
    }

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

    private fun loadHtml(html: String, baseUrl: String? = null) {
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            webView.webViewClient = object : WebViewClient() {
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

    private fun injectPageFeatures(
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
                PageFeatureConfig(
                    cleanupEnabled = true,
                    videoEnabled = true
                ),
                pageUrl = pageUrl
            )
        }
        assertTrue("Timed out waiting for script injection.", latch.await(5, TimeUnit.SECONDS))
    }

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
    }
}
