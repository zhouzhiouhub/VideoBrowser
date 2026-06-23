package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileOpenIntentFactoryContractTest {
    @Test
    fun fileViewIntentConstructionIsShared() {
        val factory = projectFile(
            "src/main/java/com/example/videobrowser/utils/FileOpenIntentFactory.kt"
        ).readText()
        val downloadedFileLauncher = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadedFileLauncher.kt"
        ).readText()
        val pageActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val chooserLauncher = projectFile(
            "src/main/java/com/example/videobrowser/utils/ChooserIntentLauncher.kt"
        ).readText()

        assertTrue(factory.contains("object FileOpenIntentFactory"))
        assertTrue(factory.contains("Intent(Intent.ACTION_VIEW)"))
        assertTrue(factory.contains("setDataAndType(uri, mimeType ?: \"*/*\")"))
        assertTrue(factory.contains("Intent.FLAG_GRANT_READ_URI_PERMISSION"))
        assertTrue(chooserLauncher.contains("Intent.createChooser(intent, activity.getString(chooserTitleRes))"))
        assertTrue(chooserLauncher.contains("catch (_: ActivityNotFoundException)"))

        listOf(downloadedFileLauncher, pageActionsController).forEach { source ->
            assertTrue(source.contains("FileOpenIntentFactory.create("))
            assertTrue(source.contains("ChooserIntentLauncher.start("))
            assertFalse(source.contains("Intent(Intent.ACTION_VIEW)"))
            assertFalse(source.contains("setDataAndType("))
            assertFalse(source.contains("Intent.createChooser("))
            assertFalse(source.contains("catch (_: ActivityNotFoundException)"))
        }
    }
}
