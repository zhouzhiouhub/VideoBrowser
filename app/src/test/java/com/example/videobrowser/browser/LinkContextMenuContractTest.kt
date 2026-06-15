package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkContextMenuContractTest {
    @Test
    fun webViewLongPressShowsLinkActions() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt").readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("configureLinkContextMenu(standardWebView)"))
        assertTrue(mainActivity.contains("configureLinkContextMenu(activeWebView)"))
        assertTrue(mainActivity.contains("targetWebView.setOnLongClickListener"))
        assertTrue(mainActivity.contains("WebView.HitTestResult.SRC_ANCHOR_TYPE"))
        assertTrue(mainActivity.contains("WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE"))
        assertTrue(mainActivity.contains("WebView.HitTestResult.IMAGE_TYPE"))
        assertTrue(mainActivity.contains("private fun showLinkContextMenu(url: String)"))
        assertTrue(mainActivity.contains("R.string.action_open_link_new_tab"))
        assertTrue(mainActivity.contains("R.string.action_download_link"))
        assertTrue(mainActivity.contains("openUrlInNewTab(url)"))
        assertTrue(mainActivity.contains("downloadLinkUrl(url)"))
        assertTrue(mainActivity.contains("copyLinkUrl(url)"))
        assertTrue(mainActivity.contains("shareLinkUrl(url)"))
        assertFalse(mainActivity.contains("openExternalUrl(url)"))
        assertFalse(mainActivity.contains("R.string.action_open_external"))
        assertTrue(mainActivity.contains("private fun downloadLinkUrl(url: String)"))
        assertTrue(mainActivity.contains("downloadController.enqueue("))
        assertTrue(mainActivity.contains("userAgent = currentBrowserManager().userAgentString()"))
        assertTrue(mainActivity.contains("ClipData.newPlainText(getString(R.string.clipboard_page_url), url)"))
        assertTrue(mainActivity.contains("Intent.createChooser(intent, getString(R.string.action_share_link))"))
        assertTrue(mainActivity.contains("private fun showImageContextMenu(url: String)"))
        assertTrue(mainActivity.contains("R.string.action_open_image_new_tab"))
        assertTrue(mainActivity.contains("R.string.action_download_image"))
        assertTrue(mainActivity.contains("R.string.action_copy_image_link"))
        assertTrue(mainActivity.contains("R.string.action_share_image_link"))
        assertTrue(mainActivity.contains("downloadImageUrl(url)"))
        assertTrue(mainActivity.contains("copyImageUrl(url)"))
        assertTrue(mainActivity.contains("shareImageUrl(url)"))
        assertTrue(mainActivity.contains("Intent.createChooser(intent, getString(R.string.action_share_image_link))"))
        assertTrue(strings.contains("title_link_context_menu"))
        assertTrue(strings.contains("title_image_context_menu"))
        assertTrue(strings.contains("action_open_link_new_tab"))
        assertTrue(strings.contains("action_download_link"))
        assertTrue(strings.contains("action_open_image_new_tab"))
        assertTrue(strings.contains("action_download_image"))
        assertTrue(strings.contains("action_copy_image_link"))
        assertTrue(strings.contains("action_share_image_link"))
        assertTrue(strings.contains("action_share_link"))
        assertTrue(readme.contains("长按网页链接或图片"))
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
