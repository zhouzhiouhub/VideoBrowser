package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Link Context Menu Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkContextMenuContractTest {
    /**
     * 测试函数 `webViewLongPressShowsLinkActions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `web View Long Press Shows Link Actions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun webViewLongPressShowsLinkActions() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt").readText()
        val linkContextMenuController = projectFile(
            "src/main/java/com/example/videobrowser/browser/LinkContextMenuController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("configureLinkContextMenu(standardWebView)"))
        assertTrue(mainActivity.contains("configureLinkContextMenu(activeWebView)"))
        assertTrue(mainActivity.contains("linkContextMenuController.configure(targetWebView)"))
        assertTrue(mainActivity.contains("private lateinit var linkContextMenuController: LinkContextMenuController"))
        assertTrue(linkContextMenuController.contains("targetWebView.setOnLongClickListener"))
        assertTrue(linkContextMenuController.contains("WebView.HitTestResult.SRC_ANCHOR_TYPE"))
        assertTrue(linkContextMenuController.contains("WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE"))
        assertTrue(linkContextMenuController.contains("WebView.HitTestResult.IMAGE_TYPE"))
        assertTrue(linkContextMenuController.contains("private fun showLinkContextMenu(url: String)"))
        assertTrue(linkContextMenuController.contains("R.string.action_open_link_new_tab"))
        assertTrue(linkContextMenuController.contains("R.string.action_download_link"))
        assertTrue(linkContextMenuController.contains("openUrlInNewTab(url)"))
        assertTrue(linkContextMenuController.contains("downloadLinkUrl(url)"))
        assertTrue(linkContextMenuController.contains("copyLinkUrl(url)"))
        assertTrue(linkContextMenuController.contains("shareLinkUrl(url)"))
        assertFalse(mainActivity.contains("openExternalUrl(url)"))
        assertFalse(linkContextMenuController.contains("R.string.action_open_external"))
        assertTrue(linkContextMenuController.contains("private fun downloadLinkUrl(url: String)"))
        assertTrue(linkContextMenuController.contains("downloadUrl(url, currentUserAgent())"))
        assertTrue(mainActivity.contains("downloadController.enqueue("))
        assertTrue(mainActivity.contains("currentUserAgent = { currentBrowserManager().userAgentString() }"))
        assertTrue(linkContextMenuController.contains("ClipData.newPlainText(activity.getString(R.string.clipboard_page_url), url)"))
        assertTrue(linkContextMenuController.contains("Intent.createChooser(intent, activity.getString(R.string.action_share_link))"))
        assertTrue(linkContextMenuController.contains("private fun showImageContextMenu(url: String)"))
        assertTrue(linkContextMenuController.contains("R.string.action_open_image_new_tab"))
        assertTrue(linkContextMenuController.contains("R.string.action_download_image"))
        assertTrue(linkContextMenuController.contains("R.string.action_copy_image_link"))
        assertTrue(linkContextMenuController.contains("R.string.action_share_image_link"))
        assertTrue(linkContextMenuController.contains("downloadImageUrl(url)"))
        assertTrue(linkContextMenuController.contains("copyImageUrl(url)"))
        assertTrue(linkContextMenuController.contains("shareImageUrl(url)"))
        assertTrue(linkContextMenuController.contains("Intent.createChooser(intent, activity.getString(R.string.action_share_image_link))"))
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

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
