package com.example.videobrowser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Main Activity Layout Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class MainActivityLayoutContractTest {
    /**
     * 测试函数 `addressBarUsesSelectedProviderBadgeStyle`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `address Bar Uses Selected Provider Badge Style` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addressBarUsesSelectedProviderBadgeStyle() {
        val layout = activityMainLayout()
        val providerBadge = layout.elementById("addressProviderBadge")
        val addressInput = layout.elementById("addressInput")

        assertEquals("TextView", providerBadge.tagName)
        assertEquals("24dp", providerBadge.androidAttribute("layout_width"))
        assertEquals("24dp", providerBadge.androidAttribute("layout_height"))
        assertEquals("center", providerBadge.androidAttribute("gravity"))
        assertEquals("bold", providerBadge.androidAttribute("textStyle"))
        assertFalse(providerBadge.hasAndroidAttribute("src"))
        assertEquals("@string/hint_address_bar", addressInput.androidAttribute("hint"))
    }

    /**
     * 测试函数 `addressBarProviderBadgeFollowsSelectedSearchProvider`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `address Bar Provider Badge Follows Selected Search Provider` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addressBarProviderBadgeFollowsSelectedSearchProvider() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()
        val viewBinding = projectFile("src/main/java/com/example/videobrowser/MainActivityViews.kt")
            .readText()

        assertTrue(viewBinding.contains("val addressProviderBadge: TextView"))
        assertTrue(viewBinding.contains("R.id.addressProviderBadge"))
        assertTrue(controller.contains("addressProviderBadge.text = selectedProvider.badge"))
        assertTrue(controller.contains("createProviderBadgeBackground("))
        assertTrue(controller.contains("selectedProvider"))
        assertTrue(controller.contains("selected = true"))
        assertTrue(controller.contains("addressProviderBadge.setTextColor(Color.WHITE)"))
    }

    /**
     * 测试函数 `addressBarContainsSiteSecurityIndicator`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `address Bar Contains Site Security Indicator` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addressBarContainsSiteSecurityIndicator() {
        val layout = activityMainLayout()
        val securityIcon = layout.elementById("siteSecurityIcon")
        val viewBinding = projectFile("src/main/java/com/example/videobrowser/MainActivityViews.kt")
            .readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val coreFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()
        val addressBarStateController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserAddressBarStateController.kt"
        ).readText()
        val browserShellUiController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserShellUiController.kt"
        ).readText()
        val siteSecurityController = projectFile(
            "src/main/java/com/example/videobrowser/browser/SiteSecurityController.kt"
        ).readText()
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertEquals("ImageView", securityIcon.tagName)
        assertEquals("20dp", securityIcon.androidAttribute("layout_width"))
        assertEquals("20dp", securityIcon.androidAttribute("layout_height"))
        assertEquals("gone", securityIcon.androidAttribute("visibility"))
        assertTrue(viewBinding.contains("val siteSecurityIcon: ImageView"))
        assertTrue(viewBinding.contains("R.id.siteSecurityIcon"))
        assertTrue(mainActivity.contains("private lateinit var browserStartupFeatures: BrowserStartupFeatureComponents"))
        assertTrue(startupFeatureAssembly.contains("siteSecurityController = BrowserSiteSecurityAssemblyController"))
        assertTrue(mainActivity.contains("private lateinit var browserCoreFeatures: BrowserCoreFeatureComponents"))
        assertTrue(coreFeatureAssembly.contains("browserShellUiController = browserShell.browserShellUiController"))
        assertTrue(browserShellUiController.contains("siteSecurityController()?.setup()"))
        assertTrue(coreFeatureAssembly.contains("browserAddressBarStateController = browserSearch.browserAddressBarStateController"))
        assertTrue(addressBarStateController.contains("siteSecurityController()?.updateStatus(url)"))
        assertTrue(siteSecurityController.contains("fun updateStatus(url: String?)"))
        assertTrue(siteSecurityController.contains("SiteSecurityStatus.fromUrl(url)"))
        assertTrue(siteSecurityController.contains("R.drawable.ic_lock_24"))
        assertTrue(siteSecurityController.contains("R.drawable.ic_warning_24"))
        assertTrue(siteSecurityController.contains("siteSecurityIcon.setOnClickListener"))
        assertTrue(siteSecurityController.contains("fun showInfoDialog()"))
        assertTrue(siteSecurityController.contains("R.string.site_security_icon_description"))
        assertTrue(siteSecurityController.contains("R.string.title_site_security_info"))
        assertTrue(siteSecurityController.contains("R.string.site_security_secure_message"))
        assertTrue(siteSecurityController.contains("R.string.site_security_not_secure_message"))
        assertTrue(siteSecurityController.contains("R.string.site_security_protocol"))
        assertTrue(siteSecurityController.contains("status.protocolDisplayName()"))
        assertTrue(siteSecurityController.contains("private fun certificateSummary(status: SiteSecurityStatus): String"))
        assertTrue(siteSecurityController.contains("R.string.site_security_certificate_validated"))
        assertTrue(siteSecurityController.contains("R.string.site_security_certificate_not_used"))
        assertTrue(siteSecurityController.contains("private fun mixedContentSummary(status: SiteSecurityStatus): String"))
        assertTrue(siteSecurityController.contains("settingsManager.isMixedContentBlocked()"))
        assertTrue(siteSecurityController.contains("R.string.site_security_mixed_content_blocked"))
        assertTrue(siteSecurityController.contains("R.string.site_security_mixed_content_compatibility"))
        assertTrue(siteSecurityController.contains("showCurrentSiteSettingsPage()"))
        assertTrue(functionCenterPages.contains("fun showCurrentSiteSettingsPage()"))
        assertTrue(strings.contains("title_site_security_info"))
        assertTrue(strings.contains("site_security_icon_description"))
        assertTrue(strings.contains("site_security_certificate_validated"))
        assertTrue(strings.contains("site_security_mixed_content_blocked"))
        assertTrue(readme.contains("证书验证边界和混合内容策略"))
    }

    /**
     * 测试函数 `addressBarHintDoesNotIncludeSelectedSearchProviderName`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `address Bar Hint Does Not Include Selected Search Provider Name` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addressBarHintDoesNotIncludeSelectedSearchProviderName() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()

        assertFalse(controller.contains("R.string.hint_search_with_provider"))
        assertTrue(controller.contains("R.string.hint_address_bar"))
    }

    /**
     * 测试函数 `addressBarDoesNotExposeUnimplementedVoiceOrCameraEntries`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `address Bar Does Not Expose Unimplemented Voice Or Camera Entries` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addressBarDoesNotExposeUnimplementedVoiceOrCameraEntries() {
        val idNames = R.id::class.java.declaredFields.map { it.name }
        val layout = projectFile("src/main/res/layout/activity_main.xml").readText()

        assertFalse(idNames.contains("voiceIcon"))
        assertFalse(layout.contains("@drawable/ic_camera_24"))
    }

    /**
     * 测试函数 `addressSuggestionPanelSitsBetweenProgressAndSearchProviders`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `address Suggestion Panel Sits Between Progress And Search Providers` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addressSuggestionPanelSitsBetweenProgressAndSearchProviders() {
        val layout = activityMainLayout()
        val suggestionPanel = layout.elementById("addressSuggestionPanel")
        val providerScroll = layout.elementById("searchProviderScroll")
        val webViewContainer = layout.elementById("webViewContainer")

        assertEquals("LinearLayout", suggestionPanel.tagName)
        assertEquals("gone", suggestionPanel.androidAttribute("visibility"))
        assertEquals("@id/pageProgress", suggestionPanel.appAttribute("layout_constraintTop_toBottomOf"))
        assertEquals("@id/addressSuggestionPanel", providerScroll.appAttribute("layout_constraintTop_toBottomOf"))
        assertEquals("@id/searchProviderScroll", webViewContainer.appAttribute("layout_constraintTop_toBottomOf"))
    }

    /**
     * 测试函数 `addressSuggestionControllerIsWiredToBookmarksHistoryAndRemoteSuggestions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `address Suggestion Controller Is Wired To Bookmarks History And Remote Suggestions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addressSuggestionControllerIsWiredToBookmarksHistoryAndRemoteSuggestions() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/AddressSuggestionController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val lifecycleController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityLifecycleController.kt"
        ).readText()
        val startupController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupController.kt"
        ).readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val coreFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()
        val keyboardController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserKeyboardController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(controller.contains("addTextChangedListener"))
        assertTrue(controller.contains("savedPageRepository.bookmarks()"))
        assertTrue(controller.contains("savedPageRepository.history()"))
        assertTrue(controller.contains("suggestionClient.fetch"))
        assertTrue(controller.contains("AddressSuggestionRanker.build"))
        assertTrue(controller.contains("AddressSuggestion.Bookmark"))
        assertTrue(controller.contains("val includePrivateSources = !isPrivateBrowsingEnabled()"))
        assertTrue(controller.contains("fun dispose()"))
        assertTrue(controller.contains("disposed = true"))
        assertTrue(controller.contains("!disposed &&"))
        assertTrue(controller.contains("suggestionClient.dispose()"))
        assertTrue(mainActivity.contains("browserActivityLifecycleController.handleDestroy()"))
        assertTrue(lifecycleController.contains("addressSuggestionController()?.dispose()"))
        assertTrue(mainActivity.contains("private lateinit var browserCoreFeatures: BrowserCoreFeatureComponents"))
        assertTrue(coreFeatureAssembly.contains("addressSuggestionController = browserSearch.addressSuggestionController"))
        assertTrue(mainActivity.contains("BrowserStartupFeatureAssemblyController"))
        assertTrue(startupFeatureAssembly.contains("BrowserStartupControllerAssembly"))
        assertTrue(startupController.contains("addressSuggestionController.setup()"))
        assertTrue(coreFeatureAssembly.contains("hideKeyboard = browserShell.browserKeyboardController::hideKeyboard"))
        assertTrue(keyboardController.contains("addressSuggestionController()?.hide()"))
        assertTrue(strings.contains("address_suggestion_bookmark"))
        assertTrue(readme.contains("收藏夹匹配"))
        assertTrue(readme.contains("Activity 销毁时会关闭远程建议线程"))
    }

    /**
     * 测试函数 `historyRecordingSkipsProviderAndConfiguredHomeUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `history Recording Skips Provider And Configured Home Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun historyRecordingSkipsProviderAndConfiguredHomeUrls() {
        val pageActions = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val searchAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BrowserSearchAssemblyController.kt"
        ).readText()
        val pageActionAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageActionAssemblyController.kt"
        ).readText()
        val coreFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserCoreFeatureAssemblyController.kt"
        ).readText()

        assertTrue(pageActions.contains("private val shouldRecordHistoryUrl: (String?) -> Boolean"))
        assertTrue(pageActions.contains("if (!shouldRecordHistoryUrl(page.url))"))
        assertTrue(mainActivity.contains("private lateinit var browserCoreFeatures: BrowserCoreFeatureComponents"))
        assertTrue(coreFeatureAssembly.contains("historyRecordPolicy = browserSearch.historyRecordPolicy"))
        assertTrue(pageActionAssembly.contains("shouldRecordHistoryUrl = historyRecordPolicy::shouldRecord"))
        assertTrue(searchAssembly.contains("SearchProviders.defaults.map { provider -> provider.homeUrl }"))
        assertTrue(searchAssembly.contains("settingsManager.homeUrlOr(searchProviderController.selectedProvider.homeUrl)"))
    }

    /**
     * 测试函数 `bottomBarActionsUseIntrinsicWidthsInsteadOfFillingAvailableSpace`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bottom Bar Actions Use Intrinsic Widths Instead Of Filling Available Space` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun bottomBarActionsUseIntrinsicWidthsInsteadOfFillingAvailableSpace() {
        val layout = activityMainLayout()
        val bottomBarActionIds = listOf(
            "backButton",
            "pageToolsButton",
            "wenxinButton",
            "profileButton"
        )

        bottomBarActionIds.forEach { id ->
            val action = layout.elementById(id)

            assertFalse("$id should not reserve weighted space", action.hasAndroidAttribute("layout_weight"))
            assertFalse("$id should not fill remaining width", action.androidAttribute("layout_width") == "0dp")
        }
    }

    /**
     * 测试函数 `bottomBarActionsKeepCompactVisualScale`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bottom Bar Actions Keep Compact Visual Scale` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun bottomBarActionsKeepCompactVisualScale() {
        val layout = activityMainLayout()
        val iconActionIds = listOf(
            "backButton",
            "pageToolsButton",
            "profileButton",
            "refreshButton"
        )
        val iconWidth = layout.elementById(iconActionIds.first()).dpAndroidAttribute("layout_width")
        val wenxinWidth = layout.elementById("wenxinButton").dpAndroidAttribute("layout_width")

        iconActionIds.forEach { id ->
            assertEquals("$id should match the other icon action widths", iconWidth, layout.elementById(id).dpAndroidAttribute("layout_width"))
        }
        assertTrue(
            "wenxinButton should stay visually close to the icon buttons",
            wenxinWidth <= iconWidth * 2.25
        )
    }

    /**
     * 测试函数 `wenxinActionUsesSameIconButtonStyleAsBottomBarActions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `wenxin Action Uses Same Icon Button Style As Bottom Bar Actions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun wenxinActionUsesSameIconButtonStyleAsBottomBarActions() {
        val layout = activityMainLayout()
        val wenxinAction = layout.elementById("wenxinButton")
        val profileAction = layout.elementById("profileButton")

        assertEquals("ImageButton", wenxinAction.tagName)
        assertEquals(profileAction.androidAttribute("layout_width"), wenxinAction.androidAttribute("layout_width"))
        assertEquals(profileAction.androidAttribute("layout_height"), wenxinAction.androidAttribute("layout_height"))
        assertEquals(profileAction.androidAttribute("background"), wenxinAction.androidAttribute("background"))
        assertEquals(profileAction.androidAttribute("tint"), wenxinAction.androidAttribute("tint"))
        assertEquals("@drawable/ic_wenxin_wave_24", wenxinAction.androidAttribute("src"))
        assertFalse(wenxinAction.hasAndroidAttribute("text"))
    }

    /**
     * 测试函数 `bottomBarActionsUseOneFullWidthSpreadChain`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bottom Bar Actions Use One Full Width Spread Chain` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun bottomBarActionsUseOneFullWidthSpreadChain() {
        val layout = activityMainLayout()
        val bottomBar = layout.elementById("bottomBar")
        val backAction = layout.elementById("backButton")
        val pageToolsAction = layout.elementById("pageToolsButton")
        val refreshAction = layout.elementById("refreshButton")
        val wenxinAction = layout.elementById("wenxinButton")
        val profileAction = layout.elementById("profileButton")

        assertEquals("androidx.constraintlayout.widget.ConstraintLayout", bottomBar.tagName)
        assertFalse(layout.hasElementById("bottomBarCenterGuide"))
        assertEquals("parent", backAction.appAttribute("layout_constraintStart_toStartOf"))
        assertEquals("@id/pageToolsButton", backAction.appAttribute("layout_constraintEnd_toStartOf"))
        assertEquals("spread", backAction.appAttribute("layout_constraintHorizontal_chainStyle"))
        assertEquals("@id/refreshButton", pageToolsAction.appAttribute("layout_constraintEnd_toStartOf"))
        assertEquals("@id/wenxinButton", refreshAction.appAttribute("layout_constraintEnd_toStartOf"))
        assertEquals("@id/profileButton", wenxinAction.appAttribute("layout_constraintEnd_toStartOf"))
        assertEquals("@id/wenxinButton", profileAction.appAttribute("layout_constraintStart_toEndOf"))
        assertEquals("parent", profileAction.appAttribute("layout_constraintEnd_toEndOf"))
    }

    /**
     * 测试函数 `activityMainLayout`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `activity Main Layout` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun activityMainLayout(): Document {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(projectFile("src/main/res/layout/activity_main.xml"))
    }

    /**
     * 测试函数 `elementById`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element By Id` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Document.elementById(id: String): Element {
        return findElementById(id) ?: error("Missing view with id $id")
    }

    /**
     * 测试函数 `hasElementById`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `has Element By Id` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Document.hasElementById(id: String): Boolean {
        return findElementById(id) != null
    }

    /**
     * 测试函数 `findElementById`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `find Element By Id` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Document.findElementById(id: String): Element? {
        val nodes = getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            val androidId = element.androidAttribute("id")
            if (androidId == "@+id/$id" || androidId == "@id/$id") {
                return element
            }
        }

        return null
    }

    /**
     * 测试函数 `hasAndroidAttribute`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `has Android Attribute` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.hasAndroidAttribute(name: String): Boolean {
        return hasAttributeNS(ANDROID_NAMESPACE, name)
    }

    /**
     * 测试函数 `androidAttribute`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `android Attribute` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.androidAttribute(name: String): String {
        return getAttributeNS(ANDROID_NAMESPACE, name)
    }

    /**
     * 测试函数 `appAttribute`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `app Attribute` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.appAttribute(name: String): String {
        return getAttributeNS(APP_NAMESPACE, name)
    }

    /**
     * 测试函数 `dpAndroidAttribute`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `dp Android Attribute` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.dpAndroidAttribute(name: String): Float {
        return androidAttribute(name).removeSuffix("dp").toFloat()
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

    private companion object {
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        private const val APP_NAMESPACE = "http://schemas.android.com/apk/res-auto"
    }
}
