package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Session Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabSessionWiringContractTest {
    /**
     * 测试函数 `mainActivityOwnsBrowserTabsAndSessionBinding`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Owns Browser Tabs And Session Binding` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityOwnsBrowserTabsAndSessionBinding() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val standardTabSessionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStandardTabSessionController.kt"
        ).readText()
        val sessionStateController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionStateController.kt"
        ).readText()
        val sessionStateAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionStateAssemblyController.kt"
        ).readText()
        val tabStateAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabStateAssemblyController.kt"
        ).readText()
        val startupController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupController.kt"
        ).readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val persistenceAssembly = projectFile(
            "src/main/java/com/example/videobrowser/storage/BrowserPersistenceAssemblyController.kt"
        ).readText()

        assertTrue(mainActivity.contains("BrowserTabStateAssemblyController"))
        assertTrue(tabStateAssembly.contains("BrowserTabStore()"))
        assertTrue(tabStateAssembly.contains("BrowserTabSessionBinding(standardTabStore)"))
        assertTrue(tabStateAssembly.contains("BrowserTabSessionBinding(privateTabStore)"))
        assertTrue(mainActivity.contains("BrowserPersistenceComponents"))
        assertTrue(persistenceAssembly.contains("BrowserTabSessionRepository(preferenceStore)"))
        assertTrue(persistenceAssembly.contains("BrowserStandardTabSessionController("))
        assertTrue(mainActivity.contains("BrowserSessionStateAssemblyController"))
        assertTrue(mainActivity.contains("browserSessionStateController.currentSessionController()"))
        assertTrue(sessionStateAssembly.contains("BrowserSessionStateController("))
        assertTrue(sessionStateAssembly.contains("isPrivateBrowsingActive = isPrivateBrowsingActive"))
        assertTrue(sessionStateAssembly.contains("standardSessionController = standardSessionController"))
        assertTrue(sessionStateAssembly.contains("privateSessionController = privateSessionController"))
        assertTrue(sessionStateController.contains("fun currentSessionController(): BrowserSessionController"))
        assertTrue(sessionStateController.contains("fun areBrowserSessionsInitialized(): Boolean"))
        assertTrue(sessionStateController.contains("if (isPrivateBrowsingActive())"))
        assertTrue(mainActivity.contains("browserTabState.standardTabSessionBinding"))
        assertTrue(persistenceAssembly.contains("browserStandardTabSessionController.restoreStandardTabSession()"))
        assertTrue(mainActivity.contains("browserPersistence.browserStandardTabSessionController::saveStandardTabSession"))
        assertTrue(standardTabSessionController.contains("standardTabStore.restore(session.tabs, session.activeTabId)"))
        assertTrue(standardTabSessionController.contains("repository.save("))
        assertTrue(mainActivity.contains("BrowserStartupFeatureAssemblyController"))
        assertTrue(startupFeatureAssembly.contains("BrowserStartupControllerAssembly"))
        assertTrue(startupController.contains("browserLaunchController.openInitialStandardPage()"))
        assertTrue(mainActivity.contains("browserTabState.standardTabSessionBinding.handlePageMetadataChanged(url, title)"))
        assertTrue(mainActivity.contains("browserPersistence.browserStandardTabSessionController.saveStandardTabSession()"))
    }

    /**
     * 测试函数 `tabSessionRepositoryRestoresOnlyWebUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `tab Session Repository Restores Only Web Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun tabSessionRepositoryRestoresOnlyWebUrls() {
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabSessionRepository.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(repository.contains("private fun normalizeRestorableWebUrl(url: String?): String?"))
        assertTrue(repository.contains("scheme != \"http\" && scheme != \"https\""))
        assertTrue(repository.contains("uri.host.isNullOrBlank()"))
        assertTrue(readme.contains("标准标签页会话只恢复带主机名的 HTTP/HTTPS 页面 URL"))
    }

    /**
     * 测试函数 `sessionControllerExposesPageMetadataCallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `session Controller Exposes Page Metadata Callback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun sessionControllerExposesPageMetadataCallback() {
        val sessionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionController.kt"
        ).readText()

        assertTrue(sessionController.contains("onPageMetadataChanged: (String?, String?) -> Unit"))
        assertTrue(sessionController.contains("onPageMetadataChanged(currentPageUrl, currentPageTitle)"))
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
