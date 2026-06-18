package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectableItemBackgroundContractTest {
    @Test
    fun `row factories share selectable item background extension`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/functioncenter/FunctionCenterGridFactory.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRowFactory.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/AddressSuggestionRowFactory.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/SearchProviderItemFactory.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("import com.example.videobrowser.utils.setBoundedSelectableItemBackground"))
            assertFalse(source.contains("private fun View.setBoundedSelectableItemBackground"))
            assertFalse(source.contains("android.R.attr.selectableItemBackground"))
        }
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
