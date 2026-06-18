package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FileNameSanitizerTest {
    @Test
    fun `replace invalid characters preserves separate replacements by default`() {
        assertEquals("folder_file_.zip", FileNameSanitizer.replaceInvalidCharacters("folder\\file?.zip"))
    }

    @Test
    fun `replace invalid characters can collapse adjacent invalid runs`() {
        assertEquals(
            "folder_file.zip",
            FileNameSanitizer.replaceInvalidCharacters("folder\\/?file.zip", collapseRuns = true)
        )
    }
}
