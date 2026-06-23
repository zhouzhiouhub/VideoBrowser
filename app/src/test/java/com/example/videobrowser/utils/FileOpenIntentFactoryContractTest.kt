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

        assertTrue(factory.contains("object FileOpenIntentFactory"))
        assertTrue(factory.contains("Intent(Intent.ACTION_VIEW)"))
        assertTrue(factory.contains("setDataAndType(uri, mimeType ?: \"*/*\")"))
        assertTrue(factory.contains("Intent.FLAG_GRANT_READ_URI_PERMISSION"))

        listOf(downloadedFileLauncher, pageActionsController).forEach { source ->
            assertTrue(source.contains("FileOpenIntentFactory.create("))
            assertFalse(source.contains("Intent(Intent.ACTION_VIEW)"))
            assertFalse(source.contains("setDataAndType("))
        }
    }
}
