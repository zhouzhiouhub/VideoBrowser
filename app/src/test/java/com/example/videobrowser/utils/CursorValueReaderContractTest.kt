package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CursorValueReaderContractTest {
    @Test
    fun `cursor callers share nullable cursor value readers`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/localfiles/LocalDocumentRepository.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/PageActionsController.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("stringOrNull"))
            assertFalse(source.contains("private fun Cursor.getStringOrNull"))
            assertFalse(source.contains("private fun Cursor.getLongOrNull"))
            assertFalse(source.contains("private fun Cursor.getIntOrNull"))
        }
    }

}
