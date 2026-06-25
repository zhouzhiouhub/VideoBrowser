package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Settings Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserSettingsPageContractTest {
    /**
     * 测试函数 `browserSettingsPageDoesNotRenderBrowserBasicsSection`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Does Not Render Browser Basics Section` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsPageDoesNotRenderBrowserBasicsSection() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDialogController.kt"
        ).readText()

        assertFalse(page.contains("addBrowserBasicsSection("))
        assertFalse(page.contains("R.string.function_center_section_browser_basics"))
        assertFalse(page.contains("settingsManager.homeUrl()"))
        assertFalse(dialogs.contains("fun showHomeUrlDialog()"))
        assertFalse(dialogs.contains("fun showSearchEngineDialog()"))
    }

    /**
     * 测试函数 `browserSettingsPageCanEditDefaultSearchEngine`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Can Edit Default Search Engine` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserSettingsPageCanEditDefaultSearchEngine() {
        val searchEnginePage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SearchEngineSettingsPage.kt"
        ).readText()
        val customSearchEnginePage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CustomSearchEngineSettingsPage.kt"
        ).readText()
        val profileCatalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt"
        ).readText()
        val profileSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileShortcutSection.kt"
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
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(profileCatalog.contains("SEARCH_ENGINE"))
        assertTrue(profileSection.contains("showSearchEngines"))
        assertTrue(profileSection.contains("R.string.action_search_engine_short"))
        assertTrue(functionCenterPages.contains("private val searchEngineSettingsPage = SearchEngineSettingsPage("))
        assertTrue(functionCenterPages.contains("showSearchEngines = { searchEngineSettingsPage.show() }"))
        assertTrue(searchEnginePage.contains("host.showPage("))
        assertTrue(searchEnginePage.contains("availableSearchProviders()"))
        assertTrue(searchEnginePage.contains("currentSearchProviderId()"))
        assertTrue(searchEnginePage.contains("selectSearchProvider(provider.id)"))
        assertTrue(searchEnginePage.contains("settingsManager.customSearchEngines().associateBy"))
        assertTrue(searchEnginePage.contains("onLongClick = { showRemoveSearchProviderDialog(provider, customEngine) }"))
        assertTrue(searchEnginePage.contains("settingsManager.removeBuiltInSearchProvider(provider.id)"))
        assertTrue(searchEnginePage.contains("availableSearchProviders().size <= 1"))
        assertTrue(searchEnginePage.contains("customSearchEngineSettingsPage.show(customEngine)"))
        assertTrue(searchEnginePage.contains("host.gridFactory.addActionGrid("))
        assertTrue(searchEnginePage.contains("TwoTextInputDialog.show("))
        assertTrue(searchEnginePage.contains("CustomSearchEngineDialogSession(activity = activity)"))
        assertTrue(searchEnginePage.contains("session.submit("))
        assertTrue(searchEnginePage.contains("isDialogActive = { dialog?.isShowing == true }"))
        assertTrue(searchEnginePage.contains("dismissDialog = { dialog?.dismiss() }"))
        assertTrue(searchEnginePage.contains("private fun addCustomSearchEngine(name: String, config: SearchEngineConfig): Boolean"))
        assertFalse(searchEnginePage.contains("SingleChoiceDialog.show("))
        assertTrue(customSearchEnginePage.contains("host.showPage("))
        assertTrue(customSearchEnginePage.contains("TwoTextInputDialog.show("))
        assertTrue(customSearchEnginePage.contains("ConfirmationDialog.show("))
        assertTrue(customSearchEnginePage.contains("CustomSearchEngineDialogSession(activity = activity)"))
        assertTrue(customSearchEnginePage.contains("session.submit("))
        assertTrue(customSearchEnginePage.contains("isDialogActive = { dialog?.isShowing == true }"))
        assertTrue(customSearchEnginePage.contains("refreshAfterCustomSearchEngineUpdated(engine)"))
        assertTrue(customSearchEnginePage.contains("private fun updateCustomSearchEngine("))
        assertTrue(customSearchEnginePage.contains("settingsManager.removeCustomSearchEngine(engine)"))
        assertTrue(customSearchEnginePage.contains("selectSearchProvider(updatedEngine.id)"))
        assertTrue(customSearchEnginePage.contains("availableSearchProviderCount() <= 1"))
        assertTrue(customSearchEnginePage.contains("fallbackSearchProviderId()?.let(selectSearchProvider)"))
        assertFalse(customSearchEnginePage.contains("SingleChoiceDialog.show("))
        assertTrue(settings.contains("fun customSearchEngines(): List<CustomSearchEngine>"))
        assertTrue(settings.contains("fun removedSearchProviderIds(): Set<String>"))
        assertTrue(settings.contains("fun removeBuiltInSearchProvider(id: String): Boolean"))
        assertTrue(settings.contains("fun addCustomSearchEngine(name: String, searchUrlPrefix: String): Boolean"))
        assertTrue(settings.contains("fun updateCustomSearchEngine("))
        assertTrue(settings.contains("fun removeCustomSearchEngine(engine: CustomSearchEngine): Boolean"))
        assertTrue(searchProviderController.contains("fun selectDefaultSearchProvider(providerId: String): Boolean"))
        assertTrue(searchProviderController.contains("fun availableProviders(): List<SearchProvider>"))
        assertFalse(searchProviderController.contains("settingsManager.setSearchEngineId(provider.id)"))
        assertFalse(
            selectDefaultSearchProviderBody(searchProviderController)
                .contains("settingsManager.setSearchEngineId")
        )
        assertTrue(
            !selectDefaultSearchProviderBody(searchProviderController)
                .contains("settingsManager.setHomeUrl")
        )
        assertTrue(functionCenterPages.contains("availableSearchProviders: () -> List<SearchProvider>"))
        assertTrue(functionCenterPages.contains("currentSearchProviderId: () -> String"))
        assertTrue(functionCenterPages.contains("selectSearchProvider: (String) -> Boolean"))
        assertTrue(functionCenterAssembly.contains("availableSearchProviders = searchProviderController::availableProviders"))
        assertTrue(functionCenterAssembly.contains("currentSearchProviderId = { searchProviderController.selectedProvider.id }"))
        assertTrue(functionCenterAssembly.contains("selectSearchProvider = searchProviderController::selectDefaultSearchProvider"))
        assertTrue(strings.contains("setting_search_engine"))
        assertTrue(strings.contains("action_search_engine_short"))
        assertTrue(strings.contains("title_add_custom_search_engine"))
        assertTrue(strings.contains("title_edit_custom_search_engine"))
        assertTrue(strings.contains("title_remove_custom_search_engine"))
        assertTrue(strings.contains("toast_search_engine_updated"))
        assertTrue(strings.contains("toast_search_engine_remove_last"))
        assertTrue(strings.contains("toast_custom_search_engine_added"))
        assertTrue(strings.contains("toast_custom_search_engine_updated"))
        assertTrue(strings.contains("toast_custom_search_engine_removed"))
        assertTrue(strings.contains("toast_custom_search_engine_probe_started"))
        assertTrue(strings.contains("toast_custom_search_engine_probe_failed"))
    }

    @Test
    fun customSearchEngineDialogSessionOwnsAutoProbeFlow() {
        val session = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CustomSearchEngineDialogSession.kt"
        ).readText()
        val analyzer = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/CustomSearchEngineInputAnalyzer.kt"
        ).readText()
        val searchEnginePage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SearchEngineSettingsPage.kt"
        ).readText()
        val customSearchEnginePage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CustomSearchEngineSettingsPage.kt"
        ).readText()

        assertTrue(analyzer.contains("CustomSearchEngineInputResolver.resolve(input, knownProviders)"))
        assertTrue(analyzer.contains("SearchEngineTemplateProber.normalizeProbeUrl(input)"))
        assertTrue(session.contains("CustomSearchEngineInputAnalyzer.analyze(values.second)"))
        assertTrue(session.contains("SearchEngineTemplateProber()"))
        assertTrue(session.contains("templateProber.probe(homeUrl = homeUrl, name = name)"))
        assertTrue(session.contains("if (isDialogActive())"))
        assertTrue(session.contains("Executors.newSingleThreadExecutor()"))
        assertTrue(session.contains("Handler(Looper.getMainLooper()).post"))
        assertTrue(session.contains("toast_custom_search_engine_probe_failed"))
        assertFalse(searchEnginePage.contains("SearchEngineTemplateProber()"))
        assertFalse(customSearchEnginePage.contains("SearchEngineTemplateProber()"))
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
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
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
        assertTrue(browserManager.contains("webViewSettings.setThirdPartyCookiesEnabled(enabled)"))
        assertTrue(settingsController.contains("setAcceptThirdPartyCookies("))
        assertTrue(settingsController.contains("!privateBrowsingEnabled && thirdPartyCookiesEnabled"))
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
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
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
        assertTrue(browserManager.contains("webViewSettings.setMixedContentBlocked(blocked)"))
        assertTrue(settingsController.contains("WebSettings.MIXED_CONTENT_NEVER_ALLOW"))
        assertTrue(settingsController.contains("WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE"))
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
        val settingsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserWebViewSettingsController.kt"
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
        assertTrue(browserManager.contains("webViewSettings.setTextZoomPercent(percent)"))
        assertTrue(settingsController.contains("textZoom = textZoomPercent"))
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

}
