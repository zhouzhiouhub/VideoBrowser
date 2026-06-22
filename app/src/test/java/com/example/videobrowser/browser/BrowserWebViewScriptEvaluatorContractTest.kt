package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserWebViewScriptEvaluatorContractTest {
    @Test
    fun browserWebViewJavascriptExecutionUsesSharedEvaluator() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val lifecycleScriptController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageLifecycleScriptController.kt"
        ).readText()
        val scriptEvaluator = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewScriptEvaluator.kt"
        ).readText()

        assertTrue(scriptEvaluator.contains("internal object BrowserWebViewScriptEvaluator"))
        assertTrue(scriptEvaluator.contains("targetWebView.evaluateJavascript(script, null)"))

        assertTrue(browserManager.contains("BrowserWebViewScriptEvaluator.evaluate(webView, script)"))
        assertFalse(browserManager.contains("webView.evaluateJavascript(script, null)"))

        assertTrue(
            lifecycleScriptController.contains(
                "BrowserWebViewScriptEvaluator.evaluate(webView, PAGE_SUSPEND_SCRIPT)"
            )
        )
        assertTrue(
            lifecycleScriptController.contains(
                "BrowserWebViewScriptEvaluator.evaluate(webView, PAGE_DISPOSE_SCRIPT)"
            )
        )
        assertFalse(lifecycleScriptController.contains("webView.evaluateJavascript(PAGE_SUSPEND_SCRIPT, null)"))
        assertFalse(lifecycleScriptController.contains("webView.evaluateJavascript(PAGE_DISPOSE_SCRIPT, null)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
