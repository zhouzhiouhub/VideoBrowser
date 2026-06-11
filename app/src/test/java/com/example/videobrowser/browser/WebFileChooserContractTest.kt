package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class WebFileChooserContractTest {
    @Test
    fun chromeClientForwardsWebFileChooserRequests() {
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()

        assertTrue(chromeClient.contains("ValueCallback<Array<Uri>>"))
        assertTrue(chromeClient.contains("FileChooserParams"))
        assertTrue(chromeClient.contains("fileChooserRequested"))
        assertTrue(chromeClient.contains("override fun onShowFileChooser"))
        assertTrue(chromeClient.contains("return fileChooserRequested(filePathCallback, fileChooserParams)"))
    }

    @Test
    fun mainActivityLaunchesSystemPickerAndReturnsChosenFilesToWebView() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("ActivityResultContracts.StartActivityForResult()"))
        assertTrue(mainActivity.contains("pendingFileChooserCallback: ValueCallback<Array<Uri>>?"))
        assertTrue(mainActivity.contains("FileChooserParams.parseResult"))
        assertTrue(mainActivity.contains("pendingFileChooserCallback?.onReceiveValue"))
        assertTrue(mainActivity.contains("showWebFileChooser"))
        assertTrue(mainActivity.contains("fileChooserRequested = ::showWebFileChooser"))
        assertTrue(mainActivity.contains("cancelPendingWebFileChooser()"))
        assertTrue(mainActivity.contains("R.string.toast_file_chooser_unavailable"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
