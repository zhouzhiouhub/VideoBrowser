package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidUriParserContractTest {
    @Test
    fun androidUriCallersShareParser() {
        val parser = projectFile(
            "src/main/java/com/example/videobrowser/utils/AndroidUriParser.kt"
        ).readText()
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/download/DownloadEnqueueController.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserRequest.kt"),
            projectFile(
                "src/main/java/com/example/videobrowser/video/PlayableMediaItemMedia3Converter.kt"
            )
        ).map { file -> file.readText() }

        assertTrue(parser.contains("object AndroidUriParser"))
        assertTrue(parser.contains("Uri.parse(value)"))
        assertTrue(parser.contains("fun parseTrimmedOrNull(value: String?): Uri?"))

        sources.forEach { source ->
            assertTrue(source.contains("AndroidUriParser."))
            assertFalse(source.contains("Uri.parse("))
        }
    }
}
