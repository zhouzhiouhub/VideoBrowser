package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortToastContractTest {
    @Test
    fun shortToastCreationIsSharedByMigratedCallers() {
        val appToast = projectFile(
            "src/main/java/com/example/videobrowser/utils/AppToast.kt"
        ).readText()
        val shortToast = projectFile(
            "src/main/java/com/example/videobrowser/utils/ShortToast.kt"
        ).readText()
        val longToast = projectFile(
            "src/main/java/com/example/videobrowser/utils/LongToast.kt"
        ).readText()
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/utils/ClipboardTextActions.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/ChooserIntentLauncher.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/PageUnavailableToast.kt"),
            projectFile("src/main/java/com/example/videobrowser/download/DownloadEnqueueController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/PageArchiveController.kt"),
            projectFile("src/main/java/com/example/videobrowser/localfiles/LocalFilesController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserBackNavigationController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserNavigationController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserExternalNavigator.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/FindInPageDialogController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/PageActionsController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/PagePrintController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/WebFileChooserController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/ClientCertificateController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/PrivateBrowsingSwitchController.kt"),
            projectFile("src/main/java/com/example/videobrowser/localfiles/LocalDocumentOperationController.kt"),
            projectFile("src/main/java/com/example/videobrowser/video/NativeTrackSelectionDialogController.kt"),
            projectFile("src/main/java/com/example/videobrowser/video/PlayerActivity.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/AdBlockLogPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDialogController.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/CurrentSitePermissionSection.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/FeatureToggleToast.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/DownloadedFileLauncher.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/UserWhitelistPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/UserManualRulesPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/SitePermissionsPage.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"),
            projectFile("src/main/java/com/example/videobrowser/element/ElementPickerController.kt"),
            projectFile("src/main/java/com/example/videobrowser/storage/BookmarkImportExportController.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/ValidatedTextInputDialog.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/SearchProviderDialogController.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/RuleSubscriptionPage.kt")
        ).map { file -> file.readText() }

        assertTrue(appToast.contains("object AppToast"))
        assertTrue(appToast.contains("Toast.makeText(context, message, duration).show()"))
        assertTrue(shortToast.contains("object ShortToast"))
        assertTrue(shortToast.contains("AppToast.show(context, messageResId, Toast.LENGTH_SHORT)"))
        assertTrue(shortToast.contains("AppToast.show(context, message, Toast.LENGTH_SHORT)"))
        assertTrue(longToast.contains("object LongToast"))
        assertTrue(longToast.contains("AppToast.show(context, messageResId, Toast.LENGTH_LONG)"))
        assertTrue(longToast.contains("AppToast.show(context, message, Toast.LENGTH_LONG)"))

        sources.forEach { source ->
            assertTrue(source.contains("ShortToast.show(") || source.contains("LongToast.show("))
            assertFalse(source.contains("Toast.makeText("))
            assertFalse(source.contains("Toast.LENGTH_SHORT"))
            assertFalse(source.contains("Toast.LENGTH_LONG"))
        }
    }
}
