package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileShareIntentFactoryContractTest {
    @Test
    fun streamShareIntentConstructionIsShared() {
        val factory = projectFile(
            "src/main/java/com/example/videobrowser/utils/FileShareIntentFactory.kt"
        ).readText()
        val downloadedFileLauncher = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadedFileLauncher.kt"
        ).readText()
        val localDocumentOperationController = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalDocumentOperationController.kt"
        ).readText()

        assertTrue(factory.contains("object FileShareIntentFactory"))
        assertTrue(factory.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(factory.contains("putExtra(Intent.EXTRA_STREAM, uri)"))
        assertTrue(factory.contains("ClipData.newUri(contentResolver, displayName, uri)"))
        assertTrue(factory.contains("Intent.FLAG_GRANT_READ_URI_PERMISSION"))

        listOf(downloadedFileLauncher, localDocumentOperationController).forEach { source ->
            assertTrue(source.contains("FileShareIntentFactory.create("))
            assertFalse(source.contains("Intent(Intent.ACTION_SEND)"))
            assertFalse(source.contains("putExtra(Intent.EXTRA_STREAM"))
            assertFalse(source.contains("ClipData.newUri"))
        }
    }
}
