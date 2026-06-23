package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortToastContractTest {
    @Test
    fun shortToastCreationIsSharedByMigratedCallers() {
        val shortToast = projectFile(
            "src/main/java/com/example/videobrowser/utils/ShortToast.kt"
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
            projectFile("src/main/java/com/example/videobrowser/video/PlayerActivity.kt")
        ).map { file -> file.readText() }

        assertTrue(shortToast.contains("object ShortToast"))
        assertTrue(shortToast.contains("Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()"))
        assertTrue(shortToast.contains("Toast.makeText(context, message, Toast.LENGTH_SHORT).show()"))

        sources.forEach { source ->
            assertTrue(source.contains("ShortToast.show("))
            assertFalse(source.contains("Toast.makeText("))
            assertFalse(source.contains("Toast.LENGTH_SHORT"))
        }
    }
}
