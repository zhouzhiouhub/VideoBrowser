package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserClientContractTest {
    @Test
    fun browserClientRoutesMainFrameLoadFailuresToErrorCallback() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()

        assertTrue(browserClient.contains("pageLoadFailed: (BrowserPageError) -> Unit"))
        assertTrue(browserClient.contains("override fun onReceivedError"))
        assertTrue(browserClient.contains("request?.isForMainFrame != true"))
        assertTrue(browserClient.contains("BrowserPageError.Network"))
        assertTrue(browserClient.contains("override fun onReceivedHttpError"))
        assertTrue(browserClient.contains("BrowserPageError.Http"))
    }

    @Test
    fun browserClientCancelsSslErrorsBeforeShowingErrorPage() {
        val browserClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserClient.kt"
        ).readText()

        assertTrue(browserClient.contains("override fun onReceivedSslError"))
        assertTrue(browserClient.contains("handler?.cancel()"))
        assertTrue(browserClient.contains("BrowserPageError.Ssl"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
