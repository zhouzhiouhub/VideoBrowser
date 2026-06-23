package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageActionsControllerUnavailablePageContractTest {
    @Test
    fun unavailableCurrentPageToastIsSharedByPageActionsAndSiteTools() {
        val pageActions = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val elementPicker = projectFile(
            "src/main/java/com/example/videobrowser/element/ElementPickerController.kt"
        ).readText()
        val currentSiteSettings = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt"
        ).readText()
        val unavailableToast = projectFile(
            "src/main/java/com/example/videobrowser/utils/PageUnavailableToast.kt"
        ).readText()

        assertTrue(pageActions.contains("private fun currentShareableUrlOrShowUnavailable(): String?"))
        assertTrue(pageActions.contains("private fun currentSavedPageOrShowUnavailable(): SavedPage?"))
        assertTrue(pageActions.contains("val url = currentShareableUrlOrShowUnavailable() ?: return"))
        assertTrue(pageActions.contains("val page = currentSavedPageOrShowUnavailable() ?: return"))
        assertFalse(pageActions.contains("private fun showNoPageUrlToast()"))
        assertTrue(unavailableToast.contains("object PageUnavailableToast"))
        assertTrue(unavailableToast.contains("R.string.toast_no_page_url"))

        listOf(pageActions, elementPicker, currentSiteSettings).forEach { source ->
            assertTrue(source.contains("PageUnavailableToast.showNoPageUrl(activity)"))
            assertFalse(source.contains("R.string.toast_no_page_url"))
        }
    }
}
