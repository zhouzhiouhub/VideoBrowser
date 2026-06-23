package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageUrlActionsContractTest {
    @Test
    fun pageUrlClipboardAndSharingActionsAreShared() {
        val actions = projectFile(
            "src/main/java/com/example/videobrowser/utils/PageUrlActions.kt"
        ).readText()
        val clipboardActions = projectFile(
            "src/main/java/com/example/videobrowser/utils/ClipboardTextActions.kt"
        ).readText()
        val chooserLauncher = projectFile(
            "src/main/java/com/example/videobrowser/utils/ChooserIntentLauncher.kt"
        ).readText()
        val chooserFactory = projectFile(
            "src/main/java/com/example/videobrowser/utils/ChooserIntentFactory.kt"
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
        assertTrue(actions.contains("ClipboardTextActions.copyPlainText("))
        assertTrue(actions.contains("labelResId = R.string.clipboard_page_url"))
        assertTrue(actions.contains("toastResId = R.string.toast_link_copied"))
        assertTrue(clipboardActions.contains("object ClipboardTextActions"))
        assertTrue(clipboardActions.contains("Context.CLIPBOARD_SERVICE"))
        assertTrue(clipboardActions.contains("ClipData.newPlainText(activity.getString(labelResId), text)"))
        assertTrue(clipboardActions.contains("Toast.makeText(activity, toastResId, Toast.LENGTH_SHORT).show()"))
        assertTrue(actions.contains("fun sharePageUrl(activity: AppCompatActivity, url: String)"))
        assertTrue(actions.contains("fun shareLinkUrl(activity: AppCompatActivity, url: String)"))
        assertTrue(actions.contains("fun shareImageLinkUrl(activity: AppCompatActivity, url: String)"))
        assertTrue(actions.contains("private fun shareTextUrl(activity: AppCompatActivity, url: String, chooserTitleRes: Int)"))
        assertTrue(actions.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(actions.contains("putExtra(Intent.EXTRA_TEXT, url)"))
        assertTrue(actions.contains("ChooserIntentLauncher.start("))
        assertFalse(actions.contains("Intent.createChooser("))
        assertTrue(chooserLauncher.contains("ChooserIntentFactory.create(activity, intent, chooserTitleRes)"))
        assertFalse(chooserLauncher.contains("Intent.createChooser("))
        assertTrue(chooserFactory.contains("Intent.createChooser(intent, context.getString(chooserTitleRes))"))

        assertTrue(pageActionsController.contains("PageUrlActions.sharePageUrl(activity, url)"))
        assertTrue(savedPageLinkActions.contains("PageUrlActions.sharePageUrl(activity, page.url)"))
        assertTrue(browserTabsPage.contains("PageUrlActions.sharePageUrl(activity, url)"))
        assertTrue(linkContextMenuController.contains("PageUrlActions.shareLinkUrl(activity, url)"))
        assertTrue(linkContextMenuController.contains("PageUrlActions.shareImageLinkUrl(activity, url)"))

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
            assertFalse(source.contains("Intent(Intent.ACTION_SEND)"))
            assertFalse(source.contains("putExtra(Intent.EXTRA_TEXT"))
        }
    }
}
