package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HomePageActionContractTest {
    @Test
    fun pageToolsCanOpenConfiguredHomePage() {
        val catalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(catalog.contains("HOME"))
        assertTrue(catalog.contains("FunctionCenterRootAction.HOME.takeIf { hasPage }"))
        assertTrue(pages.contains("openHomePage: () -> Unit"))
        assertTrue(pages.contains("FunctionCenterRootAction.HOME"))
        assertTrue(pages.contains("R.drawable.ic_home_24"))
        assertTrue(pages.contains("runPageAction(openHomePage)"))
        assertTrue(mainActivity.contains("openHomePage = ::openHomePage"))
        assertTrue(mainActivity.contains("private fun openHomePage()"))
        assertTrue(strings.contains("action_open_home_page_summary"))
        assertTrue(readme.contains("页面工具可直接回到已配置主页"))
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
