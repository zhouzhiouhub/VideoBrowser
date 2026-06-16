package com.example.videobrowser.regression

/**
 * 测试阅读提示：
 * 这个测试文件验证“Video Playback Regression Instrumented Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureConfig
import com.example.videobrowser.inject.ScriptLoader
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
class VideoPlaybackRegressionInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var webView: WebView

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

    @After
    fun tearDown() {
        instrumentation.runOnMainSync {
            webView.destroy()
        }
    }

    @Test
    fun videoRegression_keepsControlsSpeedAndFullscreenStateWorking() {
        loadHtml(VIDEO_HTML)
        injectPageFeatures(
            PageFeatureConfig(
                cleanupEnabled = false,
                videoEnabled = true
            )
        )

        val activeResult = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  window.VideoBrowserEnhancer.setPlaybackSpeed(1.25);
                  video.dispatchEvent(new Event('webkitbeginfullscreen'));
                  return [
                    video.controls === true,
                    video.hasAttribute('controls'),
                    video.playbackRate,
                    window.__videobrowserState.nativeFullscreenVideo === video,
                    window.__videobrowserState.fullscreenPlaybackSpeed
                  ];
                })();
            """.trimIndent()
        )

        assertTrue(activeResult.toString(), activeResult.getBoolean(0))
        assertTrue(activeResult.toString(), activeResult.getBoolean(1))
        assertEquals(1.25, activeResult.getDouble(2), 0.01)
        assertTrue(activeResult.toString(), activeResult.getBoolean(3))
        assertEquals(1.25, activeResult.getDouble(4), 0.01)

        val exitResult = evaluateJsonArray(
            """
                (function () {
                  var video = document.getElementById('video');
                  video.dispatchEvent(new Event('webkitendfullscreen'));
                  return [
                    window.__videobrowserState.nativeFullscreenVideo === null,
                    window.__videobrowserState.fullscreenPlaybackSpeed
                  ];
                })();
            """.trimIndent()
        )

        assertTrue(exitResult.toString(), exitResult.getBoolean(0))
        assertEquals(1.0, exitResult.getDouble(1), 0.01)
    }

    private fun loadHtml(html: String) {
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    latch.countDown()
                }
            }
            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        }
        assertTrue("Timed out waiting for test page to load.", latch.await(5, TimeUnit.SECONDS))
    }

    private fun injectPageFeatures(config: PageFeatureConfig) {
        val latch = CountDownLatch(1)
        instrumentation.runOnMainSync {
            JsInjector(
                scriptLoader = ScriptLoader(instrumentation.targetContext.assets),
                evaluateJavascript = { script ->
                    webView.evaluateJavascript(script) {
                        latch.countDown()
                    }
                }
            ).inject(config)
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
        private const val VIDEO_HTML = """
            <!doctype html>
            <html>
              <body>
                <video id="video"></video>
              </body>
            </html>
        """
    }
}
