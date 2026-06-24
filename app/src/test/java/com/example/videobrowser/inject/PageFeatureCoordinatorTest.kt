package com.example.videobrowser.inject

/**
 * 测试阅读提示：
 * 这个测试文件验证“Page Feature Coordinator Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.adblock.AdBlockDecisionReason
import com.example.videobrowser.adblock.AdBlockRequestPolicy
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageFeatureCoordinatorTest {
    /**
     * 测试函数 `injectPageFeatures_skipsJavascriptWhenCurrentSiteDisablesInjection`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject Page Features skips Javascript When Current Site Disables Injection` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun injectPageFeatures_skipsJavascriptWhenCurrentSiteDisablesInjection() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setJsInjectionDisabledForSite(" Video.Example.COM. ", true)
        val evaluatedScripts = mutableListOf<String>()
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        coordinator.injectPageFeatures()

        assertTrue(evaluatedScripts.isEmpty())
    }

    /**
     * 测试函数 `currentSiteSwitches_disableAdBlockCleanupVideoAndWhitelistSeparately`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `current Site Switches disable Ad Block Cleanup Video And Whitelist Separately` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun currentSiteSwitches_disableAdBlockCleanupVideoAndWhitelistSeparately() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setAdBlockDisabledForSite("video.example.com", true)
        settings.setJsInjectionDisabledForSite("video.example.com", true)
        settings.setDomAdBlockDisabledForSite("video.example.com", true)
        settings.setVideoEnhancementDisabledForSite("video.example.com", true)
        settings.setSmartNoImageDisabledForSite("video.example.com", true)
        settings.setUserWhitelistedSite("ads.example.com", true)
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = mutableListOf(),
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        assertTrue(coordinator.isCurrentSiteAdBlockDisabled())
        assertTrue(coordinator.isCurrentSiteJsInjectionDisabled())
        assertTrue(coordinator.isCurrentSitePageCleanupDisabled())
        assertTrue(coordinator.isCurrentSiteVideoEnhancementDisabled())
        assertTrue(coordinator.isCurrentSiteSmartNoImageDisabled())

        val decision = AdBlockRequestPolicy.evaluate(
            enabled = settings.isAdBlockEnabled(),
            siteAdBlockDisabled = coordinator.isCurrentSiteAdBlockDisabled(),
            userWhitelisted = settings.isUserWhitelistedSite("ads.example.com"),
            url = "https://ads.example.com/banner.js",
            host = "ads.example.com",
            scheme = "https",
            pageHost = "video.example.com",
            isForMainFrame = false,
            ruleEngine = RuleEngine(listOf(Rule.blockDomainContains("ads.example.com")))
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.USER_WHITELISTED, decision.reason)
    }

    /**
     * 测试函数 `injectPageFeatures_passesCurrentSiteUserSelectorsIntoFeatureConfig`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject Page Features passes Current Site User Selectors Into Feature Config` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun injectPageFeatures_passesCurrentSiteUserSelectorsIntoFeatureConfig() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.addUserElementHideSelectorForSite("video.example.com", ".picked-ad")
        settings.addUserElementHideSelectorForSite("other.example.com", ".other-ad")
        val evaluatedScripts = mutableListOf<String>()
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        coordinator.injectPageFeatures()

        val script = evaluatedScripts.single()
        assertTrue(script.contains("\"userCssSelectors\":[\".picked-ad\"]"))
        assertTrue(script.contains("\"cleanupEnabled\":true"))
        assertTrue(script.contains("\"videoEnabled\":true"))
    }

    /**
     * 测试函数 `injectPageFeatures_appliesCurrentSiteCleanupAndVideoSwitches`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject Page Features applies Current Site Cleanup And Video Switches` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun injectPageFeatures_appliesCurrentSiteCleanupAndVideoSwitches() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setDomAdBlockDisabledForSite("video.example.com", true)
        settings.setVideoEnhancementDisabledForSite("video.example.com", true)
        settings.addUserElementHideSelectorForSite("video.example.com", ".picked-ad")
        val evaluatedScripts = mutableListOf<String>()
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        coordinator.injectPageFeatures()

        val script = evaluatedScripts.single()
        assertFalse(script.contains("\"jsInjectionEnabled\""))
        assertTrue(script.contains("\"cleanupEnabled\":false"))
        assertTrue(script.contains("\"videoEnabled\":false"))
        assertTrue(script.contains("\"userCssSelectors\":[\".picked-ad\"]"))
    }

    @Test
    fun injectPageFeatures_marksBuiltInSearchResultPages() {
        val evaluatedScripts = mutableListOf<String>()
        val coordinator = coordinatorFor(
            settings = SettingsManager(InMemoryPreferenceStore()),
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "m.baidu.com",
            currentPageUrl = "https://m.baidu.com/s?ie=utf-8&word=%E4%BD%A0%E5%A5%BD",
            isBuiltInSearchResultPage = { url ->
                url == "https://m.baidu.com/s?ie=utf-8&word=%E4%BD%A0%E5%A5%BD"
            }
        )

        coordinator.injectPageFeatures()

        assertTrue(evaluatedScripts.single().contains("\"builtInSearchResultPage\":true"))
    }

    /**
     * 测试函数 `injectPageFeatures_doesNotRequestBrowserManagerWhenCurrentPageUrlIsSupplied`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject Page Features does Not Request Browser Manager When Current Page Url Is Supplied` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun injectPageFeatures_doesNotRequestBrowserManagerWhenCurrentPageUrlIsSupplied() {
        val evaluatedScripts = mutableListOf<String>()
        var browserManagerRequested = false
        val coordinator = PageFeatureCoordinator(
            settingsManager = SettingsManager(InMemoryPreferenceStore()),
            browserManager = {
                browserManagerRequested = true
                error("BrowserManager should not be instantiated in this JVM regression test.")
            },
            jsInjector = jsInjectorFor(evaluatedScripts),
            currentSiteHost = { "video.example.com" },
            currentPageUrl = { "https://video.example.com/watch" }
        )

        coordinator.injectPageFeatures()

        assertFalse(browserManagerRequested)
        assertTrue(evaluatedScripts.isNotEmpty())
    }

    @Test
    fun injectPageFeatures_forwardsCompletionCallback() {
        val evaluatedScripts = mutableListOf<String>()
        var callbackInvoked = false
        val coordinator = coordinatorFor(
            settings = SettingsManager(InMemoryPreferenceStore()),
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        coordinator.injectPageFeatures {
            callbackInvoked = true
        }

        assertTrue(callbackInvoked)
    }

    /**
     * 测试函数 `coordinatorFor`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `coordinator For` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param settings 参数类型为 `SettingsManager`，表示本次操作的配置集合，函数会按这些开关和参数调整行为。
     * @param evaluatedScripts 参数类型为 `MutableList<String>`，表示函数执行 `evaluatedScripts` 相关逻辑时需要读取或处理的输入。
     * @param currentSiteHost 参数类型为 `String?`，表示函数执行 `currentSiteHost` 相关逻辑时需要读取或处理的输入。
     * @param currentPageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun coordinatorFor(
        settings: SettingsManager,
        evaluatedScripts: MutableList<String>,
        currentSiteHost: String?,
        currentPageUrl: String?,
        isBuiltInSearchResultPage: (String?) -> Boolean = { false }
    ): PageFeatureCoordinator {
        return PageFeatureCoordinator(
            settingsManager = settings,
            browserManager = { unusedBrowserManager() },
            jsInjector = jsInjectorFor(evaluatedScripts),
            currentSiteHost = { currentSiteHost },
            currentPageUrl = { currentPageUrl },
            isBuiltInSearchResultPage = isBuiltInSearchResultPage
        )
    }

    /**
     * 测试函数 `jsInjectorFor`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `js Injector For` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param evaluatedScripts 参数类型为 `MutableList<String>`，表示函数执行 `evaluatedScripts` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun jsInjectorFor(evaluatedScripts: MutableList<String>): JsInjector {
        return JsInjector(
            scriptLoader = ScriptLoader {
                ByteArrayInputStream(COMMON_SCRIPT.toByteArray(Charsets.UTF_8))
            },
            evaluateJavascript = { script -> evaluatedScripts += script }
        )
    }

    /**
     * 测试函数 `unusedBrowserManager`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `unused Browser Manager` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun unusedBrowserManager(): BrowserManager {
        error("Current page URL should be supplied by the test.")
    }



    private companion object {
        private const val COMMON_SCRIPT =
            "window.VideoBrowserEnhancer={apply:function(config){window.__applied=config;}};"
    }
}
