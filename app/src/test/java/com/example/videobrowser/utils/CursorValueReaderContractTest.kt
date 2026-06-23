package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CursorValueReaderContractTest {
    @Test
    fun `cursor callers share nullable cursor value readers`() {
        val reader = projectFile(
            "src/main/java/com/example/videobrowser/utils/CursorValueReader.kt"
        ).readText()
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/localfiles/LocalDocumentRepository.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/PageActionsController.kt"),
            projectFile(
                "src/main/java/com/example/videobrowser/download/AndroidDownloadStatusSnapshotReader.kt"
            )
        ).map { file -> file.readText() }

        assertTrue(reader.contains("class CursorColumnValueReader"))
        assertTrue(reader.contains("columnIndexes.getOrPut(columnName)"))
        assertTrue(reader.contains("cursor.getColumnIndex(columnName)"))

        sources.forEach { source ->
            assertTrue(source.contains("columnValueReader()"))
            assertFalse(source.contains("getColumnIndex("))
            assertFalse(source.contains("private fun Cursor.getStringOrNull"))
            assertFalse(source.contains("private fun Cursor.getLongOrNull"))
            assertFalse(source.contains("private fun Cursor.getIntOrNull"))
        }
    }

}
