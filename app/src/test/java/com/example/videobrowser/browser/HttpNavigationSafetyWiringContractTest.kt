package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Http Navigation Safety Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpNavigationSafetyWiringContractTest {
    /**
     * 测试函数 `mainActivityConfirmsHttpsToHttpTopLevelNavigation`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Confirms Https To Http Top Level Navigation` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityConfirmsHttpsToHttpTopLevelNavigation() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt").readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation"))
        assertTrue(mainActivity.contains("private fun loadUrlInternal(url: String, allowInsecureNavigation: Boolean)"))
        assertTrue(mainActivity.contains("private fun loadUrlAfterInsecureNavigationConfirmation(url: String)"))
        assertTrue(mainActivity.contains("private fun showInsecureNavigationConfirmation(url: String)"))
        assertTrue(mainActivity.contains("view?.stopLoading()"))
        assertTrue(mainActivity.contains("R.string.title_confirm_insecure_navigation"))
        assertTrue(mainActivity.contains("R.string.dialog_confirm_insecure_navigation_message"))
        assertTrue(strings.contains("title_confirm_insecure_navigation"))
        assertTrue(strings.contains("dialog_confirm_insecure_navigation_message"))
        assertTrue(readme.contains("HTTPS 页面跳转到 HTTP 明文页面前会先确认"))
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
