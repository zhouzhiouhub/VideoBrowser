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
