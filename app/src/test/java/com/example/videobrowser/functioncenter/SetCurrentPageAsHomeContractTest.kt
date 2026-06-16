package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Set Current Page As Home Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SetCurrentPageAsHomeContractTest {
    @Test
    fun pageToolsCanSetCurrentPageAsHomePage() {
        val pageActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(pageActionsController.contains("fun setCurrentPageAsHomePage()"))
        assertTrue(pageActionsController.contains("settingsManager.isValidHomeUrl(url)"))
        assertTrue(pageActionsController.contains("settingsManager.setHomeUrl(url)"))
        assertTrue(pageActionsController.contains("R.string.toast_home_page_updated"))
        assertTrue(pageActionsController.contains("R.string.toast_home_page_invalid"))
        assertTrue(functionCenterPages.contains("setCurrentPageAsHomePage: () -> Unit"))
        assertTrue(functionCenterPages.contains("R.string.action_set_current_page_as_home"))
        assertTrue(functionCenterPages.contains("R.string.action_set_current_page_as_home_summary"))
        assertTrue(functionCenterPages.contains("R.drawable.ic_home_24"))
        assertTrue(functionCenterPages.contains("runPageAction(setCurrentPageAsHomePage)"))
        assertTrue(mainActivity.contains("setCurrentPageAsHomePage = pageActionsController::setCurrentPageAsHomePage"))
        assertTrue(strings.contains("action_set_current_page_as_home"))
        assertTrue(strings.contains("action_set_current_page_as_home_summary"))
        assertTrue(readme.contains("也可将当前页面设为主页"))
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
