package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Print Page Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PrintPageWiringContractTest {
    /**
     * 测试函数 `functionCenterHasPrintPageAction`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Center Has Print Page Action` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun functionCenterHasPrintPageAction() {
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val rootActionCatalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val printIcon = projectFile("src/main/res/drawable/ic_print_24.xml").readText()

        assertTrue(rootActionCatalog.contains("PRINT_PAGE"))
        assertTrue(functionCenterPages.contains("printCurrentPage: () -> Unit"))
        assertTrue(functionCenterPages.contains("R.string.action_print_page"))
        assertTrue(functionCenterPages.contains("R.drawable.ic_print_24"))
        assertTrue(functionCenterPages.contains("runPageAction(printCurrentPage)"))
        assertTrue(strings.contains("action_print_page"))
        assertTrue(strings.contains("action_print_page_summary"))
        assertTrue(printIcon.contains("<vector"))
    }

    /**
     * 测试函数 `mainActivityPrintsCurrentWebViewThroughAndroidPrintFramework`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Prints Current Web View Through Android Print Framework` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityPrintsCurrentWebViewThroughAndroidPrintFramework() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        ).readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val pageToolEntryController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageToolEntryController.kt"
        ).readText()
        val pagePrintController = projectFile("src/main/java/com/example/videobrowser/browser/PagePrintController.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("private lateinit var browserCoreFeatures: BrowserCoreFeatureComponents"))
        assertTrue(startupFeatureAssembly.contains("browserPageToolEntryController = pageActions.browserPageToolEntryController"))
        assertTrue(pageToolEntryController.contains("pagePrintController.printCurrentPage()"))
        assertTrue(pagePrintController.contains("PrintManager"))
        assertTrue(pagePrintController.contains("PrintAttributes.Builder().build()"))
        assertTrue(pagePrintController.contains("createPrintDocumentAdapter(jobName)"))
        assertTrue(pagePrintController.contains("printManager.print(jobName, printAdapter"))
        assertTrue(functionCenterAssembly.contains("printCurrentPage = browserPageToolEntryController::printCurrentPage"))
        assertTrue(pagePrintController.contains("R.string.toast_print_page_unavailable"))
        assertTrue(strings.contains("print_job_name"))
        assertTrue(readme.contains("打印或另存为 PDF"))
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
