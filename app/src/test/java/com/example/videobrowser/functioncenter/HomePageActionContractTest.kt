package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Home Page Action Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test

class HomePageActionContractTest {
    /**
     * 测试函数 `pageToolsCanOpenConfiguredHomePage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `page Tools Can Open Configured Home Page` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun pageToolsCanOpenConfiguredHomePage() {
        val catalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val rootActionSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionSection.kt"
        ).readText()
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        )
            .readText()
        val launchController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserLaunchController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(catalog.contains("HOME"))
        assertTrue(catalog.contains("FunctionCenterRootAction.HOME.takeIf { hasPage }"))
        assertTrue(pages.contains("openHomePage: () -> Unit"))
        assertTrue(pages.contains("openHomePage = openHomePage"))
        assertTrue(rootActionSection.contains("FunctionCenterRootAction.HOME"))
        assertTrue(rootActionSection.contains("R.drawable.ic_home_24"))
        assertTrue(rootActionSection.contains("runPageAction(openHomePage)"))
        assertTrue(functionCenterAssembly.contains("openHomePage = browserLaunchController::openHomePage"))
        assertTrue(launchController.contains("fun openHomePage()"))
        assertTrue(strings.contains("action_open_home_page_summary"))
        assertTrue(readme.contains("页面工具可直接回到已配置主页"))
    }

}
