package com.example.videobrowser.inject

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
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

    private fun loadHtml(html: String) {
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    latch.countDown()
                }
            }
            webView.loadDataWithBaseURL(
                "https://example.com/",
                html,
                "text/html",
                "utf-8",
                null
            )
        }
        assertTrue("Timed out waiting for test page to load.", latch.await(5, TimeUnit.SECONDS))
    }

    private fun injectPageFeatures() {
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            JsInjector(
                scriptLoader = ScriptLoader(instrumentation.targetContext.assets),
                evaluateJavascript = { script ->
                    webView.evaluateJavascript(script) {
                        latch.countDown()
                    }
                }
            ).inject(
                PageFeatureConfig(
                    cleanupEnabled = true,
                    videoEnabled = true
                )
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

    private companion object {
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
    }
}
