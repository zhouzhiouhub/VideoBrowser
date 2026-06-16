package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Java Script Dialog Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class JavaScriptDialogContractTest {
    @Test
    fun chromeClientHandlesJavaScriptDialogsNatively() {
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(chromeClient.contains("override fun onJsAlert("))
        assertTrue(chromeClient.contains("override fun onJsConfirm("))
        assertTrue(chromeClient.contains("override fun onJsPrompt("))
        assertTrue(chromeClient.contains("override fun onJsBeforeUnload("))
        assertTrue(chromeClient.contains("AlertDialog.Builder(activity)"))
        assertTrue(chromeClient.contains("jsResult.confirm(input.text?.toString().orEmpty())"))
        assertTrue(chromeClient.contains("R.string.action_leave_page"))
        assertTrue(chromeClient.contains("R.string.action_stay_on_page"))
        assertTrue(chromeClient.contains("private fun javascriptDialogOrigin(url: String?): String?"))
        assertTrue(chromeClient.contains("activity.isDestroyed"))

        assertTrue(strings.contains("title_javascript_dialog"))
        assertTrue(strings.contains("title_javascript_confirm"))
        assertTrue(strings.contains("title_javascript_prompt"))
        assertTrue(strings.contains("title_javascript_before_unload"))
        assertTrue(strings.contains("dialog_javascript_before_unload_message"))
        assertTrue(readme.contains("JavaScript alert、confirm、prompt 和离页确认弹窗"))
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
