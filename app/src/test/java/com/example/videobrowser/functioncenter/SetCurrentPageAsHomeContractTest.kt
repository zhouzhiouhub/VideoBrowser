package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Set Current Page As Home Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetCurrentPageAsHomeContractTest {
    /**
     * 测试函数 `pageToolsCanSetCurrentPageAsHomePage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `page Tools Can Set Current Page As Home Page` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun pageActionControllerCanSetCurrentPageAsHomePageWithoutUnusedFunctionCenterWiring() {
        val pageActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        )
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(pageActionsController.contains("fun setCurrentPageAsHomePage()"))
        assertTrue(pageActionsController.contains("settingsManager.isValidHomeUrl(url)"))
        assertTrue(pageActionsController.contains("settingsManager.setHomeUrl(url)"))
        assertTrue(pageActionsController.contains("R.string.toast_home_page_updated"))
        assertTrue(pageActionsController.contains("R.string.toast_home_page_invalid"))
        assertFalse(functionCenterPages.contains("setCurrentPageAsHomePage: () -> Unit"))
        assertFalse(functionCenterPages.contains("R.string.action_set_current_page_as_home"))
        assertFalse(functionCenterPages.contains("runPageAction(setCurrentPageAsHomePage)"))
        assertFalse(functionCenterAssembly.contains("setCurrentPageAsHomePage = pageActionsController::setCurrentPageAsHomePage"))
        assertTrue(strings.contains("action_set_current_page_as_home"))
        assertTrue(strings.contains("action_set_current_page_as_home_summary"))
    }

}
