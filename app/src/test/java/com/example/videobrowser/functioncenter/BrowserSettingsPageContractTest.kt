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

    @Test
    fun browserSettingsPageCanEditDefaultSearchEngine() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val searchProviderController = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("SearchProviders.defaults"))
        assertTrue(page.contains("private fun showSearchEngineDialog()"))
        assertTrue(page.contains("currentSearchProviderName()"))
        assertTrue(page.contains("selectSearchProvider(provider.id)"))
        assertTrue(searchProviderController.contains("fun selectDefaultSearchProvider(providerId: String): Boolean"))
        assertTrue(searchProviderController.contains("settingsManager.setSearchEngineId(provider.id)"))
        assertTrue(
            selectDefaultSearchProviderBody(searchProviderController)
                .contains("settingsManager.setSearchEngineId(provider.id)")
        )
        assertTrue(
            !selectDefaultSearchProviderBody(searchProviderController)
                .contains("settingsManager.setHomeUrl")
        )
        assertTrue(functionCenterPages.contains("currentSearchProviderName: () -> String"))
        assertTrue(functionCenterPages.contains("selectSearchProvider: (String) -> Boolean"))
        assertTrue(mainActivity.contains("currentSearchProviderName = { searchProviderController.selectedProvider.name }"))
        assertTrue(mainActivity.contains("selectSearchProvider = searchProviderController::selectDefaultSearchProvider"))
        assertTrue(strings.contains("setting_search_engine"))
        assertTrue(strings.contains("toast_search_engine_updated"))
    }

    private fun selectDefaultSearchProviderBody(source: String): String {
        val signature = "fun selectDefaultSearchProvider(providerId: String): Boolean"
        val start = source.indexOf(signature)
        assertTrue(start >= 0)
        val bodyStart = source.indexOf('{', start)
        assertTrue(bodyStart >= 0)
        var depth = 0
        for (index in bodyStart until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return source.substring(bodyStart, index + 1)
                    }
                }
            }
        }
        error("Unclosed selectDefaultSearchProvider body")
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
