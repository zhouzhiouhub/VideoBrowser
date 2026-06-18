package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Site Permissions Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SitePermissionsPageContractTest {
    /**
     * 测试函数 `browserSettingsRoutesToSitePermissionsManager`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Routes To Site Permissions Manager` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsRoutesToSitePermissionsManager() {
        val settingsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val catalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterDataManagementActionCatalog.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(catalog.contains("SITE_PERMISSIONS"))
        assertTrue(settingsPage.contains("showSitePermissionsManager: () -> Unit"))
        assertTrue(settingsPage.contains("R.string.action_manage_site_permissions"))
        assertTrue(settingsPage.contains("showSitePermissionsManager()"))
        assertTrue(pages.contains("private val sitePermissionsPage = SitePermissionsPage"))
        assertTrue(pages.contains("showSitePermissionsManager = { sitePermissionsPage.show() }"))
        assertTrue(strings.contains("action_manage_site_permissions"))
        assertTrue(strings.contains("action_manage_site_permissions_summary"))
        assertTrue(readme.contains("站点权限记录页可集中查看和清理已保存决策"))
    }

    /**
     * 测试函数 `sitePermissionsPageCanRemoveAndClearSavedDecisions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `site Permissions Page Can Remove And Clear Saved Decisions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun sitePermissionsPageCanRemoveAndClearSavedDecisions() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SitePermissionsPage.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val settingsModels = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsModels.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(settingsModels.contains("data class SitePermissionRecord"))
        assertTrue(settings.contains("fun sitePermissionRecords(): List<SitePermissionRecord>"))
        assertTrue(settings.contains("fun clearSitePermissionDecisions()"))
        assertTrue(page.contains("settingsManager.sitePermissionRecords()"))
        assertTrue(page.contains("settingsManager.setSitePermissionDecision("))
        assertTrue(page.contains("SitePermissionDecision.ASK"))
        assertTrue(page.contains("settingsManager.clearSitePermissionDecisions()"))
        assertTrue(page.contains("R.string.title_site_permissions"))
        assertTrue(page.contains("R.string.dialog_site_permissions_empty"))
        assertTrue(page.contains("R.string.title_remove_site_permission"))
        assertTrue(page.contains("R.string.dialog_clear_site_permissions_message"))
        assertTrue(strings.contains("title_site_permissions"))
        assertTrue(strings.contains("dialog_site_permissions_empty"))
        assertTrue(strings.contains("toast_site_permissions_cleared"))
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
