package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Settings Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserSettingsPageContractTest {
    /**
     * 测试函数 `browserSettingsPageCanEditHomePageUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Can Edit Home Page Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsPageCanEditHomePageUrl() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDialogController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("addBrowserBasicsSection(content)"))
        assertTrue(dialogs.contains("fun showHomeUrlDialog()"))
        assertTrue(page.contains("settingsManager.homeUrl()"))
        assertTrue(dialogs.contains("settingsManager.isValidHomeUrl(homeUrl)"))
        assertTrue(dialogs.contains("settingsManager.setHomeUrl(homeUrl)"))
        assertTrue(strings.contains("setting_home_page"))
        assertTrue(strings.contains("hint_home_page_url"))
        assertTrue(strings.contains("toast_home_page_updated"))
        assertTrue(strings.contains("toast_home_page_invalid"))
    }

    /**
     * 测试函数 `browserSettingsPageCanEditDefaultSearchEngine`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Can Edit Default Search Engine` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsPageCanEditDefaultSearchEngine() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDialogController.kt"
        ).readText()
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        )
            .readText()
        val searchProviderController = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(dialogs.contains("SearchProviders.defaults"))
        assertTrue(dialogs.contains("fun showSearchEngineDialog()"))
        assertTrue(page.contains("currentSearchProviderName()"))
        assertTrue(dialogs.contains("selectSearchProvider(provider.id)"))
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
        assertTrue(functionCenterAssembly.contains("currentSearchProviderName = { searchProviderController.selectedProvider.name }"))
        assertTrue(functionCenterAssembly.contains("selectSearchProvider = searchProviderController::selectDefaultSearchProvider"))
        assertTrue(strings.contains("setting_search_engine"))
        assertTrue(strings.contains("toast_search_engine_updated"))
    }

    /**
     * 测试函数 `browserSettingsPageCanControlThirdPartyCookies`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Can Control Third Party Cookies` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsPageCanControlThirdPartyCookies() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val startupController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupController.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(settings.contains("fun areThirdPartyCookiesEnabled(): Boolean"))
        assertTrue(settings.contains("fun setThirdPartyCookiesEnabled(enabled: Boolean)"))
        assertTrue(page.contains("R.string.setting_third_party_cookies"))
        assertTrue(page.contains("settingsManager.areThirdPartyCookiesEnabled()"))
        assertTrue(page.contains("settingsManager.setThirdPartyCookiesEnabled(enabled)"))
        assertTrue(page.contains("browserManager().setThirdPartyCookiesEnabled(enabled)"))
        assertTrue(browserManager.contains("fun setThirdPartyCookiesEnabled(enabled: Boolean)"))
        assertTrue(browserManager.contains("setAcceptThirdPartyCookies("))
        assertTrue(browserManager.contains("!privateBrowsingEnabled && thirdPartyCookiesEnabled"))
        assertTrue(mainActivity.contains("BrowserActivityFeatureAssemblyController"))
        assertTrue(startupController.contains("setThirdPartyCookiesEnabled(settingsManager.areThirdPartyCookiesEnabled())"))
        assertTrue(strings.contains("setting_third_party_cookies"))
        assertTrue(strings.contains("setting_third_party_cookies_summary"))
    }

    /**
     * 测试函数 `browserSettingsPageCanControlMixedContentBlocking`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Can Control Mixed Content Blocking` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsPageCanControlMixedContentBlocking() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val startupController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupController.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val settingsKeys = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsPreferenceKeys.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(settings.contains("fun isMixedContentBlocked(): Boolean"))
        assertTrue(settings.contains("fun setMixedContentBlocked(blocked: Boolean)"))
        assertTrue(settingsKeys.contains("DEFAULT_MIXED_CONTENT_BLOCKED = true"))
        assertTrue(settingsKeys.contains("KEY_MIXED_CONTENT_BLOCKED = \"mixed_content_blocked\""))
        assertTrue(page.contains("R.string.setting_mixed_content_blocking"))
        assertTrue(page.contains("settingsManager.isMixedContentBlocked()"))
        assertTrue(page.contains("settingsManager.setMixedContentBlocked(blocked)"))
        assertTrue(page.contains("browserManager().setMixedContentBlocked(blocked)"))
        assertTrue(browserManager.contains("fun setMixedContentBlocked(blocked: Boolean)"))
        assertTrue(browserManager.contains("WebSettings.MIXED_CONTENT_NEVER_ALLOW"))
        assertTrue(browserManager.contains("WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE"))
        assertTrue(mainActivity.contains("BrowserActivityFeatureAssemblyController"))
        assertTrue(startupController.contains("setMixedContentBlocked(settingsManager.isMixedContentBlocked())"))
        assertTrue(strings.contains("setting_mixed_content_blocking"))
        assertTrue(strings.contains("setting_mixed_content_blocking_summary"))
        assertTrue(readme.contains("混合内容"))
    }

    /**
     * 测试函数 `browserSettingsPageCanControlWebPageTextZoom`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Can Control Web Page Text Zoom` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsPageCanControlWebPageTextZoom() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDialogController.kt"
        ).readText()
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val startupController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupController.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val settingsKeys = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsPreferenceKeys.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(settings.contains("fun textZoomPercent(): Int"))
        assertTrue(settings.contains("fun setTextZoomPercent(percent: Int)"))
        assertTrue(settings.contains("DEFAULT_TEXT_ZOOM_PERCENT = 100"))
        assertTrue(settings.contains("TEXT_ZOOM_OPTIONS = listOf(75, 100, 125, 150, 200)"))
        assertTrue(settingsKeys.contains("KEY_TEXT_ZOOM_PERCENT = \"text_zoom_percent\""))
        assertTrue(dialogs.contains("fun showTextZoomDialog()"))
        assertTrue(page.contains("R.string.setting_text_zoom"))
        assertTrue(page.contains("settingsManager.textZoomPercent()"))
        assertTrue(dialogs.contains("settingsManager.setTextZoomPercent(percent)"))
        assertTrue(dialogs.contains("browserManager().setTextZoomPercent(percent)"))
        assertTrue(browserManager.contains("fun setTextZoomPercent(percent: Int)"))
        assertTrue(browserManager.contains("textZoom = textZoomPercent"))
        assertTrue(mainActivity.contains("BrowserActivityFeatureAssemblyController"))
        assertTrue(startupController.contains("setTextZoomPercent(settingsManager.textZoomPercent())"))
        assertTrue(strings.contains("setting_text_zoom"))
        assertTrue(strings.contains("setting_text_zoom_summary"))
        assertTrue(strings.contains("toast_text_zoom_updated"))
        assertTrue(readme.contains("网页文字大小"))
    }

    /**
     * 测试函数 `selectDefaultSearchProviderBody`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `select Default Search Provider Body` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
