package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Java Script Dialog Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JavaScriptDialogContractTest {
    /**
     * 测试函数 `chromeClientHandlesJavaScriptDialogsNatively`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `chrome Client Handles Java Script Dialogs Natively` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun chromeClientHandlesJavaScriptDialogsNatively() {
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeJavaScriptDialogController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(chromeClient.contains("ChromeJavaScriptDialogController(activity)"))
        assertTrue(chromeClient.contains("override fun onJsAlert("))
        assertTrue(chromeClient.contains("override fun onJsConfirm("))
        assertTrue(chromeClient.contains("override fun onJsPrompt("))
        assertTrue(chromeClient.contains("override fun onJsBeforeUnload("))
        assertTrue(chromeClient.contains("javaScriptDialogs.showAlert(view, url, message, result)"))
        assertTrue(chromeClient.contains("javaScriptDialogs.showConfirm(view, url, message, result)"))
        assertTrue(
            chromeClient.contains(
                "javaScriptDialogs.showPrompt(view, url, message, defaultValue, result)"
            )
        )
        assertTrue(chromeClient.contains("javaScriptDialogs.showBeforeUnload(view, url, message, result)"))
        assertTrue(dialogController.contains("private fun showJavaScriptDialog("))
        assertTrue(dialogController.contains("private fun <T : JsResult> activeJavaScriptResult(result: T?): T?"))
        assertEquals(1, Regex("AppDialog\\.builder\\(activity\\)").findAll(dialogController).count())
        assertTrue(dialogController.contains("jsResult.confirm(input.text?.toString().orEmpty())"))
        assertTrue(dialogController.contains("R.string.action_leave_page"))
        assertTrue(dialogController.contains("R.string.action_stay_on_page"))
        assertTrue(dialogController.contains("private fun javascriptDialogOrigin(url: String?): String?"))
        assertTrue(dialogController.contains("activity.isDestroyed"))

        assertTrue(strings.contains("title_javascript_dialog"))
        assertTrue(strings.contains("title_javascript_confirm"))
        assertTrue(strings.contains("title_javascript_prompt"))
        assertTrue(strings.contains("title_javascript_before_unload"))
        assertTrue(strings.contains("dialog_javascript_before_unload_message"))
        assertTrue(readme.contains("JavaScript alert、confirm、prompt 和离页确认弹窗"))
    }

}
