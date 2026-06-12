package com.example.videobrowser

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class MainActivityLayoutContractTest {
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

    @Test
    fun addressBarContainsSiteSecurityIndicator() {
        val layout = activityMainLayout()
        val securityIcon = layout.elementById("siteSecurityIcon")
        val viewBinding = projectFile("src/main/java/com/example/videobrowser/MainActivityViews.kt")
            .readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertEquals("ImageView", securityIcon.tagName)
        assertEquals("20dp", securityIcon.androidAttribute("layout_width"))
        assertEquals("20dp", securityIcon.androidAttribute("layout_height"))
        assertEquals("gone", securityIcon.androidAttribute("visibility"))
        assertTrue(viewBinding.contains("val siteSecurityIcon: ImageView"))
        assertTrue(viewBinding.contains("R.id.siteSecurityIcon"))
        assertTrue(mainActivity.contains("private fun updateSiteSecurityStatus"))
        assertTrue(mainActivity.contains("SiteSecurityStatus.fromUrl(url)"))
        assertTrue(mainActivity.contains("R.drawable.ic_lock_24"))
        assertTrue(mainActivity.contains("R.drawable.ic_warning_24"))
    }

    @Test
    fun addressBarHintDoesNotIncludeSelectedSearchProviderName() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()

        assertFalse(controller.contains("R.string.hint_search_with_provider"))
        assertTrue(controller.contains("R.string.hint_address_bar"))
    }

    @Test
    fun addressBarDoesNotExposeUnimplementedVoiceOrCameraEntries() {
        val idNames = R.id::class.java.declaredFields.map { it.name }
        val layout = projectFile("src/main/res/layout/activity_main.xml").readText()

        assertFalse(idNames.contains("voiceIcon"))
        assertFalse(layout.contains("@drawable/ic_camera_24"))
    }

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

    @Test
    fun addressSuggestionControllerIsWiredToBookmarksHistoryAndRemoteSuggestions() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/AddressSuggestionController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(controller.contains("addTextChangedListener"))
        assertTrue(controller.contains("savedPageRepository.bookmarks()"))
        assertTrue(controller.contains("savedPageRepository.history()"))
        assertTrue(controller.contains("suggestionClient.fetch"))
        assertTrue(controller.contains("AddressSuggestionRanker.build"))
        assertTrue(controller.contains("AddressSuggestion.Bookmark"))
        assertTrue(controller.contains("val includePrivateSources = !isPrivateBrowsingEnabled()"))
        assertTrue(mainActivity.contains("private lateinit var addressSuggestionController: AddressSuggestionController"))
        assertTrue(mainActivity.contains("addressSuggestionController.setup()"))
        assertTrue(mainActivity.contains("addressSuggestionController.hide()"))
        assertTrue(strings.contains("address_suggestion_bookmark"))
        assertTrue(readme.contains("收藏夹匹配"))
    }

    @Test
    fun historyRecordingSkipsProviderAndConfiguredHomeUrls() {
        val pageActions = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(pageActions.contains("private val shouldRecordHistoryUrl: (String?) -> Boolean"))
        assertTrue(pageActions.contains("if (!shouldRecordHistoryUrl(page.url))"))
        assertTrue(mainActivity.contains("private lateinit var historyRecordPolicy: HistoryRecordPolicy"))
        assertTrue(mainActivity.contains("shouldRecordHistoryUrl = historyRecordPolicy::shouldRecord"))
        assertTrue(mainActivity.contains("SearchProviders.defaults.map { provider -> provider.homeUrl }"))
        assertTrue(mainActivity.contains("settingsManager.homeUrlOr(searchProviderController.selectedProvider.homeUrl)"))
    }

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

    @Test
    fun homeBottomBarActionsCenterInSeparateHalves() {
        val layout = activityMainLayout()
        val bottomBar = layout.elementById("bottomBar")
        val centerGuide = layout.elementById("bottomBarCenterGuide")
        val wenxinAction = layout.elementById("wenxinButton")
        val profileAction = layout.elementById("profileButton")

        assertEquals("androidx.constraintlayout.widget.ConstraintLayout", bottomBar.tagName)
        assertEquals("vertical", centerGuide.androidAttribute("orientation"))
        assertEquals("0.5", centerGuide.appAttribute("layout_constraintGuide_percent"))
        assertEquals("@id/bottomBarCenterGuide", wenxinAction.appAttribute("layout_constraintEnd_toStartOf"))
        assertEquals("@id/bottomBarCenterGuide", profileAction.appAttribute("layout_constraintStart_toEndOf"))
        assertEquals("parent", profileAction.appAttribute("layout_constraintEnd_toEndOf"))
    }

    private fun activityMainLayout(): Document {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(projectFile("src/main/res/layout/activity_main.xml"))
    }

    private fun Document.elementById(id: String): Element {
        val nodes = getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            val androidId = element.androidAttribute("id")
            if (androidId == "@+id/$id" || androidId == "@id/$id") {
                return element
            }
        }

        error("Missing view with id $id")
    }

    private fun Element.hasAndroidAttribute(name: String): Boolean {
        return hasAttributeNS(ANDROID_NAMESPACE, name)
    }

    private fun Element.androidAttribute(name: String): String {
        return getAttributeNS(ANDROID_NAMESPACE, name)
    }

    private fun Element.appAttribute(name: String): String {
        return getAttributeNS(APP_NAMESPACE, name)
    }

    private fun Element.dpAndroidAttribute(name: String): Float {
        return androidAttribute(name).removeSuffix("dp").toFloat()
    }

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
