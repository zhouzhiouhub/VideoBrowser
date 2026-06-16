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

    @Test
    fun mainActivityPrintsCurrentWebViewThroughAndroidPrintFramework() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("PrintManager"))
        assertTrue(mainActivity.contains("PrintAttributes.Builder().build()"))
        assertTrue(mainActivity.contains("createPrintDocumentAdapter(jobName)"))
        assertTrue(mainActivity.contains("printManager.print(jobName, printAdapter"))
        assertTrue(mainActivity.contains("printCurrentPage = ::printCurrentPage"))
        assertTrue(mainActivity.contains("R.string.toast_print_page_unavailable"))
        assertTrue(strings.contains("print_job_name"))
        assertTrue(readme.contains("打印或另存为 PDF"))
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
