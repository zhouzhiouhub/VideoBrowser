package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserSettingsPageContractTest {
    @Test
    fun browserSettingsPageCanEditHomePageUrl() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("addBrowserBasicsSection(content)"))
        assertTrue(page.contains("private fun showHomeUrlDialog()"))
        assertTrue(page.contains("settingsManager.homeUrl()"))
        assertTrue(page.contains("settingsManager.isValidHomeUrl(homeUrl)"))
        assertTrue(page.contains("settingsManager.setHomeUrl(homeUrl)"))
        assertTrue(strings.contains("setting_home_page"))
        assertTrue(strings.contains("hint_home_page_url"))
        assertTrue(strings.contains("toast_home_page_updated"))
        assertTrue(strings.contains("toast_home_page_invalid"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
