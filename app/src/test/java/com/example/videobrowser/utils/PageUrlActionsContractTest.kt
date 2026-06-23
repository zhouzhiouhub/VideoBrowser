package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageUrlActionsContractTest {
    @Test
    fun pageUrlClipboardCopyingIsShared() {
        val actions = projectFile(
            "src/main/java/com/example/videobrowser/utils/PageUrlActions.kt"
        ).readText()
        val pageActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val linkContextMenuController = projectFile(
            "src/main/java/com/example/videobrowser/browser/LinkContextMenuController.kt"
        ).readText()
        val savedPageLinkActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageLinkActions.kt"
        ).readText()
        val browserTabsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserTabsPage.kt"
        ).readText()

        assertTrue(actions.contains("object PageUrlActions"))
        assertTrue(actions.contains("fun copyPageUrl(activity: AppCompatActivity, url: String)"))
        assertTrue(actions.contains("Context.CLIPBOARD_SERVICE"))
        assertTrue(actions.contains("ClipData.newPlainText(activity.getString(R.string.clipboard_page_url), url)"))
        assertTrue(actions.contains("Toast.makeText(activity, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()"))

        listOf(
            pageActionsController,
            linkContextMenuController,
            savedPageLinkActions,
            browserTabsPage
        ).forEach { source ->
            assertTrue(source.contains("PageUrlActions.copyPageUrl("))
            assertFalse(source.contains("Context.CLIPBOARD_SERVICE"))
            assertFalse(source.contains("ClipData.newPlainText"))
            assertFalse(source.contains("R.string.toast_link_copied"))
        }
    }
}
