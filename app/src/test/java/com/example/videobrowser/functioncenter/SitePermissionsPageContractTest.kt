package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SitePermissionsPageContractTest {
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

    @Test
    fun sitePermissionsPageCanRemoveAndClearSavedDecisions() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SitePermissionsPage.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(settings.contains("data class SitePermissionRecord"))
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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
