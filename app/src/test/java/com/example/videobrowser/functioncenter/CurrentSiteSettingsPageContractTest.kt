package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentSiteSettingsPageContractTest {
    @Test
    fun currentSiteSettingsExposeSitePermissionDecisions() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("SitePermission.CAMERA"))
        assertTrue(page.contains("SitePermission.MICROPHONE"))
        assertTrue(page.contains("SitePermission.LOCATION"))
        assertTrue(page.contains("SitePermissionDecision.entries"))
        assertTrue(page.contains("settingsManager.sitePermissionDecision(hostName, permission)"))
        assertTrue(page.contains("settingsManager.setSitePermissionDecision(hostName, permission, decision)"))
        assertTrue(page.contains("fun showSitePermissionDialog"))
        assertTrue(page.contains("R.string.site_permission_ask"))
        assertTrue(page.contains("R.string.site_permission_allowed"))
        assertTrue(page.contains("R.string.site_permission_blocked"))
        assertTrue(strings.contains("setting_site_permission_camera"))
        assertTrue(strings.contains("setting_site_permission_microphone"))
        assertTrue(strings.contains("setting_site_permission_location"))
        assertTrue(strings.contains("toast_site_permission_updated"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
