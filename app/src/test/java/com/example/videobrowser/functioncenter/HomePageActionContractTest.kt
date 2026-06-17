package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Home Page Action Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
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
        assertTrue(pages.contains("FunctionCenterRootAction.HOME"))
        assertTrue(pages.contains("R.drawable.ic_home_24"))
        assertTrue(pages.contains("runPageAction(openHomePage)"))
        assertTrue(functionCenterAssembly.contains("openHomePage = browserLaunchController::openHomePage"))
        assertTrue(launchController.contains("fun openHomePage()"))
        assertTrue(strings.contains("action_open_home_page_summary"))
        assertTrue(readme.contains("页面工具可直接回到已配置主页"))
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
